# 长连接

## Spring版本

![Netty-SocketIO](/Users/dingyuanjie/Desktop/MS/png/Netty-SocketIO.png)

* ==SocketServerStarter==
  * Socket io 长连接server启动者，在Spring容器启动的之后或者关闭的时候进行定生成的起停操作
  * 实现`ApplicationListener`接口，重写`onApplicationEvent(ApplicationEvent event)`监听`Spring`容器事件
  * 在xml配置文件中将其配置成Bean，并设置所需扫描包路径属性`basePackage`
    * 该路径下是标注自定义注解的一些自定义事件类
* 监听容器事件
  * 可以从事件中获取`ApplicationContext`
  * 如果是`ContextRefreshedEvent`Spring容器刷新事件，则启动SocketServer服务
  * 如果是`ContextStoppedEvent`Spring容器停止事件或`ContextClosedEvent`容器关闭事件，则停止SocketServer长连接服务
* 启动SocketServer服务
  * 获取配置的属性：host、group（用于分组）
  * 获取长连接事件类的class对象
    * 扫描指定包路径下包含自定义注解标注的事件类，如@SocketIOListener、@SocketIOEvent以及@SocketDisconnectListener注解的类，并存储到各自相应的Map（`Map<String, List<Class<?>>>`）中，key表示组，Value表示标注事件注解的class对象
      * 将包路径转换成`classpath*:`文件路径形式
      * `ResourcePatternResolver`根据路径获取其下所有的`Resource`
      * 遍历`Resource`，`MetadataReaderFactory`根据`Resource`获取`MetadataReader`元数据
      * `ScannedGenericBeanDefinition`根据`MetadataReader`作为构造函数参数来生成`BeanDefinition`
      * `BeanDefinitionFilter`的`filter`方法来过滤掉不符合条件的`BeanDefinition`
        * `filter`方法根据`ScannedGenericBeanDefinition`参数，可以通过反射获取其class对象（`definition.getBeanClassName()`）
        * 获取class对象上的注解，如果`SocketIOEvent`、`SocketIOListener`和`SocketDisconnectListener`都不存在，则不符合条件，返回false
      * 遍历所有`Set<BeanDefinition>`，通过反射获取其上的注解（`SocketIOEvent`、`SocketIOListener`和`SocketDisconnectListener`），通过判断，分别存入相应的Map中
  * 遍历groups（分组，目前就一个分组`common`），设置SocketIO服务端配置
    * 启动前，先把在线客户端下线
      
      * ...
    * 根据key为group来获取各事件Map中的事件class对象`List<Class<?>>`
    * 构造`SocketIO`服务配置`Configuration`
      * `port（根据group来设置）`
      * 客户端连接认证`config.setAuthorizationListener(new AuthorizationListener(group));`
        * 实现`AuthorizationListener`接口，重写`isAuthorized(HandshakeData data)`
        * 从`HandshakeData`的`URL`或`Header`中获取`sid`
        * 根据`sid`，获取`Redission` (==登录这块==)，如果不为空且没过期，则从中获取登录信息（username、companyCode等）
        * 构建根据这些信息（username、companyCode、group、sid等）构建`SocketAuthBean`，并把这些信息设置到==`HandshakeData`==的==URL参数==中（前缀`SOCKET_PARAMS_KEY`）
        * 将`SocketAuthBean`，存入到redis的Hash中，key为`rs:socket:cu:+companyCode+username`，value为 `SocketAuthBean`转成的Map
        * 将`companyCode:username`存入redis的Set中，key为`rs:socket:c:CompanyCode`，value为`companyCode:username`
        * 将`companyCode:username`存入redis的Set中，key为`rs:socket:ng:node:group`，value为`companyCode:username` (node为port)
      * 传输协议(websocket)、方式(polling轮询)、传输最大内容长度、ping超时时间、轮询间隔、boss线程数、worker线程数
      * Storefactory，使用redis存储socket会话，否则使用默认（存储在内存）
        * `Config`中配置redis连接信息，连接超时时间、重试间隔时间、重新连接超时时间、host、port、pass、db
        * 根据`redis`配置创建`redission`，然后传入`RedissonStoreFactory`构造函数中创建该实例，然后设置到`SocketIO`的`StoreFactory`配置中
    * 根据SocketIO配置文件来构造`SocketIOServer`
      * 添加服务端事件监听
        * 连接事件`server.addConnectListener(new SocketIOConnectListener(group));`
          * `SocketIOConnectListener实现ConnectListener`，重写`onConnect(SocketIOClient client)`
            * 从`SocketIOClient`中获取`HandshakeData（包含认证拦截时设置的一些登录数据，如username、companyCode、sid、group等）`
            * ==当客户端连接上socket io服务时，创建对应的redis通道监听器监听消息==
              * 根据`SOCKET_MESSAGE_BY_companyCode_username`构建redis的`ChannelTopic`
              * ==初始化redis容器==`RedisMessageListenerContainer`和`MessageListenerAdapter`
              * redis消息监听容器添加通道监听`RedisMessageListenerContainer.addMessageListener(messageListenerAdapter, topic);`
              * 建立连接后存储通道与对应消息监听器`ConcurrentHashMap<String, MessageListener> currentUserRedisMessageListeners.put(topic.getTopic(), messageListenerAdapter);`
              * 将group、companyCode、username拼接成key，SocketIOClient为value，存入用户的当前客户端socket io连接ConcurrentHashMap中
          
        * 断开连接事件`server.addDisconnectListener(new SocketIODisconnectListener(group));`
        
          * `SocketIODisconnectListener implements DisconnectListener`，重写`void onDisconnect(SocketIOClient client)`方法
            * 从SocketIOClient中获取客户端信息
            * 从redis中删除登录成功信息
            * 从当前客户端socket io连接ConcurrentHashMap中删除对应SocketIOClient
            * 然后从redis消息监听容器删除对应通道
        
        * 增加客户端连接事件监听
        
          * server.addConnectListener(connectListener);
          * connectListener 实现ConnectListener接口，主要做一些日志记录
          * connectListener通过class对象反射获取
          * class对象扫描对应的注解获取
        
        * 增加客户端断开连接监听事件
        
          * server.addDisconnectListener(disconnectListener)
          * 实现DisconnectListener接口，主要做一些日志记录
          * onnectListener通过class对象反射获取
          * class对象扫描对应的注解获取
        
        * 自定义事件监听
        
          * 遍历自定义事件class对象
        
          ```java
          server.addEventListener(simpleName, String.class, new DataListener<String>() {
            @Override
            public void onData(SocketIOClient client, String dataStr, AckRequest ackRequest) {
              // 1. 从client中获取客户端信息，如companyCode、username、sid
              // 2. 构建长连接日志信息，插入日志表
              // 3. 通过事件类class对象、事件类方法名(onMessage)、参数(SocketIOClient，data)，利用反射调用事件类中方法，获取返回值SocketCallbackData
              // 4. 如果要求有回调ackRequest.isAckRequested，则将返回值发送给客户端
              // 5. 根据MessageId更新长连接日志信息记录状态
          ```
  
* 服务端异步启动，并添加监听

  * `server.startAsync().addListener{new ChannelFutureListener(){...}}`

## SpringBoot版

## 基础设施

* `SocketIOData`，Socket io服务 接收消息数据临时对象

  - `id`（消息ID）、`group`、`eventName`、`busObject`（业务数据JSON）、`success`、`sendTime`、`errorCode`、`errorMessage`、`username`、`companyCode`、`operator`、`operationCompany`、`clientIp`

* `SocketCallbackData`，消息推送 客户端回调消息格式

  * `messageId`、`success`、`busObject`、`errorCode`、`errorMessage`、`processTime`

* ==初始化redis容器==

  ```java
  //注册到Spring容器中
  public class RedisSubConfiguration {
    @Bean
    public RedisMessageListenerContainer redisMessageListenerContainer(RedisConnectionFactory redisConnectionFactory) {
      RedisMessageListenerContainer container = new RedisMessageListenerContainer();
      container.setConnectionFactory(redisConnectionFactory);
      return container;
    }
    @Bean
    public MessageListenerAdapter messageListenerAdapter() {
      MessageListenerAdapter messageListenerAdapter = new MessageListenerAdapter();
      //设置消息监听代理：自定义代理消息处理PushMessageDelegateListenerImpl
      // 用于服务端主动推送事件给相应的客户端
      messageListenerAdapter.setDelegate(pushMessageDelegateListenerImpl());
      messageListenerAdapter.setDefaultListenerMethod("handleMessage");
      messageListenerAdapter.setSerializer(new JdkSerializationRedisSerializer());
      return messageListenerAdapter;
    }
  
    @Bean
    public PushMessageDelegateListenerImpl pushMessageDelegateListenerImpl() {
      return new PushMessageDelegateListenerImpl();
    }
  }
  ```
  
  ```java
  public interface MessageDelegate {
  		// 消息处理
      public void handleMessage(Serializable message, String channel);
  }
  public class PushMessageDelegateListenerImpl implements MessageDelegate{
    // 1. 将message转为SocketIOData  if message instanceof SocketIOData
    // 2. 从SocketIOData中获取group、companyCode、username，组成key，从存放用户的 当前客户端socket io连接的Map中获取SocketIOClient
    // 3. 从SocketIOData中构建PortalSocketMsg长连接日志信息（事件名、group、companyCode、是否发送成功、IP地址）
    // 4. 如果SocketIOClient的channel是通的，client.isChannelOpen()
    // 4.1 将上述日志信息插入到长连接日志表
    // 4.2 SocketIOClient推送事件并有回调  事件名 回调方法 发送数据
    client.sendEvent(data.getEventName(), new PushMessageAckCallback(msg), JSON.toJSONString(out, SerializerFeature.WriteMapNullValue));
    class PushMessageAckCallback extends AckCallback<String>
      重写void onSuccess(String data)方法，根据messigeId更新上面长连接日志信息发送状态为成功
  }
  ```

# Hessian

# 登录

 # 人脸

## 技术

* 引入封装的依赖`com.arcsoft`、`face`、`2.0`
* 配置文件中添加人脸配置信息`appId`、`key`、以及`timeout`，官网申请
* 在`ServletContextListener`实现类中调用人脸识别工具类的init(appid,key)激活人脸识别引擎
* 人脸识别工具类中，根据人脸图片（byte数组）生成特征文件方法、比对人脸相似度方法（之前生成的人脸图片和新拍的人脸图片对比，返回一个0-100之间整数）

## App

* Android和IOS的App中嵌入人脸识别SDK
* 如果用户使用人脸，App将根据用户人脸照片生成人脸特征，然后调用接口将人脸照片和人脸特征上传到OSS上存储，返回存储相对路径，绑定房间时将这两个路径一起上传到平台审核

## 平台

* 生成人脸特征接口
  * 根据传入的人脸照片生成人脸特征文件，将人脸照片和人脸特征存入redis并设置有效期，返回相应的key值（文件名）-imgId、faceId
* 比对人脸相似度接口
  * 根据传入的faceId从redis中取出人脸特征和根据传入的人脸照片生成的人脸特征进行比对，返回比对结果（0-100之间整数）

* 审核通过用户
  * 解析并存储读取的身份证信息（包括证件号、姓名、籍贯等）
  * 如果人脸有效期值不为null，设置有效期值（开始-结束）（一个月、三个月等），启始时间为审核时间
  * 如果有人脸照片（传入的faceId不为null）
    * App提交的人脸审核和平台修改提交的人脸审核，通过和前端约定faceId格式来判断
      * 设置人脸图片和特征在OSS上存储的路径imgPath、facePath，使用人脸属性为1，人脸照片状态为审核通过
      * 平台修改人脸后的审核（是生成人脸特征接口返回的faceId格式）
        * 获取generateFormId，如果不为null，则App提交就有人脸，然后平台修改了人脸
          * 根据ImgId、faceId从redis中获取人脸图片和特征，然后调用阿里OSS工具类方法(输入流,文件名,存储路径)存入相应的OSS目录中
          * (文件路径及名称,companyCode,formId,groupId)将人脸照片和特征信息存入附件表单表和附件表，附件表单表中formId和generateFormId对应、groupId来对应不同状态的人脸审核（待审核、审核通过、审核驳回，每条绑定信息的三个状态值分别最多只有一个），人脸特征的groupId为face
          * 根据generateFormId、groupId为待审核删除app提交过来待审核的附件信息
        * generateFormId为null，则app没有提交人脸，平台给用户新增
          * 设置generateFormId，和上面一样（文件存入OSS、文件信息添加到两个附件表）
      * App提交的人脸审核
        * 根据generateFormId、groupId，将人脸特征groupId从待审核改为审核通过
        * 通过generateFormId和groupId为face从附件表中找到人脸特征在OSS上的路径
  * 如果没有人脸照片
    * 使用人脸属性为0，人脸照片状态为无照片
  * 更新用户绑定信息（性别、人脸有效期、人员居住类型、生日、generateFormId、证件姓名/编码、人脸图片路径）
  * 生成随机6位整数的紧急密码，存入紧急密码表（和绑定信息表关联）
  * 更新用户绑定状态信息（基本信息审核通过、人脸照片状态、紧急密码等）
  * 获取网易云账号
    * 每个用户分配一个网易云账号，如果绑定多个房间，则共用同一个网易云账号
    * 根据注册时的userId和app类型去网易云账号表中查找，如果没有，则分配网易云账号
      * 网易账号表和网易账号关联表（通过网易账号表id关联），查询出网易云账号关联表中没关联的网易云账号信息
        * 如果为空，则调用网易云工具类中方法来生成网易云账号，返回并保存到这两个表中
        * 如果不为空，则返回第一个并保存到网易云账号关联表中
    * 如果有则直接返回
  * 获取需推送的门口机
    * 通过companyCode获取所有社区节点信息（单表中，上下级节点通过parentId关联，根节点parentId为0）
    * 遍历所有节点，构造社区树（遍历，如果parentId为0，则返回，否则找到该节点的上一级节点并设置其子节点为该节点），然后根据绑定房间的areaId，找到房间节点到根节点的节点路径（所需推送节点）
      * 需推送app用户信息（基本信息、网易云账号）
      * 推送紧急密码
      * 推送用户ID、人脸特征OSS地址及有效期
* 审核拒绝
  * 如果存在人脸照片，删除之前审核拒绝的、将groupId从待审核改为审核拒绝、照片状态改为审核拒绝、基本信息审核拒绝，更新绑定状态表
* 用户冻结/解冻
  * 将用户绑定状态改为冻结/审核通过
  * 将用户的人脸特征（如果有使用人脸）、网易云账号推送到门口机删除/新增（和门口机定义操作状态新增、修改、删除 ）
* 用户解绑
  - 将用户绑定状态改为解绑
  - 将用户的人脸特征（如果有使用人脸）、网易云账号（判断没有其他绑定关系）推送到门口机删除
* 通过易销盒子读取身份证信息
  * 获取tokenKey：根据易销盒子的配置信息（serverUrl、username、passward），通过httpClient调用，返回TokenKey
    * 创建默认的httpClient实例
    * 创建httppost并设置Header、Entity属性值以及连接超时等配置
    * httpClient执行httppost返回结果
  * 获取读卡器信息：根据读卡器的（CardUrl）和TokenKey，通过httpClient调用，返回读卡器信息
  * 获取身份证信息：根据读卡器的（ServerUrl）和读卡器信息，通过httpClient调用，返回身份证信息
  * 返回的身份证信息有固定格式，根据格式解析得到相关数据
* 后台新增用户
  * 用户名UUID、generateFormId为UUID、绑定状态为已绑定、人脸照片状态为审核通过、设置人脸有效期
  * 平台页面会先调用根据人脸照片生成人脸特征接口，将人脸照片和特征存入redis，返回相应的key：imgId、faceId
  * 从redis中取出人脸照片和特征，上传到OSS；并将人脸照片和人脸特征相关信息存入附件及附件表单表（generateFormId、groupId-人脸特征为face、人脸照片为userPicture））
  * 将用户信息、人脸特征OSS路径推送给相关门口机
* 信息管理
  * 当有人脸照片时（根据前端faceId是否为null来判断）
    * 如果人脸照片修改过，则faceId为生成特征时UUID格式，否则不是该格式
      * 如果generateFormId为null，则说明之前没有人人脸照片，则UUID生成generateFormId
      * 根据前端传递的faceId、imgId，从redis中找到对应人脸特征和人脸图片，上传到OSS，然后，存入附件表和附件表单表（groupId分别为userPicture、face）
      * 
    * 如果没有修改过，则判断人脸照片状态不能是审核拒绝的状态
    * 人脸照片状态为审核通过
  * 没有人脸照片时，人脸照片状态为无照片
  * 更新用户基本信息、绑定状态

## 门口机

* 重启时，回主动去平台获取用户信息
* 平台审核通过后，会主动推送到门口机

# 设计模式

* 简单工厂模式 + 策略模式

  ```java
  public interface OpenDoorLog {
      ResultBean onMessage(String msg);
  }
  ```

  ```java
  @Component
  public class CardOpenDoorLog implements OpenDoorLog {
      @Override
      public ResultBean onMessage(String msg) {
          return new ResultBean(true, "CardOpenDoorLog---" + msg);
      }
  }
  ```

  ```java
  @Component
  public class FaceOpenDoorLog implements OpenDoorLog {
  
      @Override
      public ResultBean onMessage(String msg) {
          return new ResultBean(true, "FaceOpenDoorLog---" + msg);
      }
  }
  ```

  ```java
  public enum OpenDoorTypeEnum {
      CARD_OPENDOOR("card", CardOpenDoorLog.class),
      FACE_OPENDOOR("face", FaceOpenDoorLog.class);
  
      private String type;
      private Class<? extends OpenDoorLog> clazz;
  
      OpenDoorTypeEnum(String type, Class<? extends OpenDoorLog> clazz) {
          this.type = type;
          this.clazz = clazz;
      }
  
      public static Class getOpenDoorClassByType(String type) {
          if(!StringUtils.isNullOrBlank(type)) {
              OpenDoorTypeEnum[] openDoors = OpenDoorTypeEnum.values();
              for(OpenDoorTypeEnum openDoor : openDoors) {
                  if(type.equals(openDoor.getType())) {
                      return openDoor.getClazz();
                  }
              }
          }
          return null;
      }
     // getter、setter...
  }
  ```

  ```java
  public static void main(String[] args) throws IllegalAccessException, InstantiationException {
    String openDoorType = "face";
    Class<? extends OpenDoorLog> clazz = OpenDoorTypeEnum.getOpenDoorClassByType(openDoorType);
    //如果是在SpringBoot项目中，可通过@Autowired ApplicationConext的getBean(processClass)获取相应的处理对象
    //前提是处理类被@Component注解标注并被Spring扫描到
    OpenDoorLog openDoorLog = clazz.newInstance();
    System.out.println(JSON.toJSONString(openDoorLog.onMessage("11111111")));
  }
  ```

* 装饰器模式

# Lua

```xml
<bean id="stringRedisTemplate" class="org.springframework.data.redis.core.StringRedisTemplate">
        <property name="connectionFactory" ref="jedisConnectionFactory"/>
</bean>
```

```lua
// resources/lua/PhoneLimit.lua
local num=redis.call('incr',KEYS[1])
if tonumber(num)==1 then
    redis.call('expire',KEYS[1],ARGV[1])
    return 1
elseif tonumber(num)>tonumber(ARGV[2]) then
    return 0
else
	return 1
end
```

```java
@Autowired
private StringRedisTemplate stringRedisTemplate;
private static final String IPACCESS_KEY_PREX = "ipaccess_";

private DefaultRedisScript<Long> script = new DefaultRedisScript<Long>();
			script.setResultType(Long.class);
			script.setScriptSource(new ResourceScriptSource(new ClassPathResource("lua/PhoneLimit.lua")));

List<String> keys = new ArrayList<>();
keys.add(IPACCESS_KEY_PREX + mobile);
// 因为序列化原因，只能用StringRedisTemplate
Long execute = stringRedisTemplate.execute(script, keys, "60","1");
```

# 其他

## ServiceException

```java
public class ServiceException extends RuntimeException {

    private static final long serialVersionUID = 315849313532101582L;
    
    public ServiceException(String exceptionMsg) {
        super(exceptionMsg);
    }
    
    public ServiceException(Throwable cause) {
        super(cause);
    }
    
    public ServiceException(String exceptionMsg, Throwable cause) {
        super(exceptionMsg, cause);
    }
    
    private String code;
    private String[] params;
    //getter、setter
}
```

## Controller

```java
@RequestMapping("/facePhoto/appFaceReject")
@ResponseBody
public ResultBean rejectFacePhoto(HttpServletRequest request, HttpServletResponse response,
                             HouseHolderBean houseHolderBean) {
  ResultBean result = new ResultBean(true, SUCCESS);
  try {
    holderManageService.rejectFacePhoto(houseHolderBean);
  } catch (Exception e) {
    result.setSuccess(false);
    if (e instanceof ServiceException) {
      result.setMessage(e.getMessage());
    } else {
      result.setMessage("人脸照片驳回失败!");
    }
  }
  return result;
}
```

## AbstractController

```java
public abstract class AbstractController {
	protected final Logger logger = LogManager.getLogger(getClass());
  //定义些常量、公共参数的校验方法（如Header中参数）、及获取当前登录用户信息的方法
	//...
}
```

## SpringWebUtils

* 工具类，封装一些从request、session中获取信息的方法，如用户的登录信息

```java
@Component
public class SpringWebUtils {
  public static HttpServletRequest getRequest() {
        return ((ServletRequestAttributes)RequestContextHolder.getRequestAttributes()).getRequest();
    }
  //...
}
```

## SpringUtils

* 单例模式

* 根据带`ApplicationContext`参数的构造函数创建

  * `private static ApplicationContext ctx;`
  * 获取`ApplicationContext`中`Bean`
    * `ApplicationContext.getBean(beanName)、ApplicationContext.getBean(beanClassPath)`

  ```java
  // 将 basePackage = com.tkrng.community.socket 转为 com/tkrng/community/socket
  private String resolveBasePackage(String basePackage) {
          return ClassUtils.convertClassNameToResourcePath(ctx.getEnvironment().resolveRequiredPlaceholders(basePackage));
      }
  
  // 
  private ResourcePatternResolver resourcePatternResolver = new PathMatchingResourcePatternResolver();
  //
  private MetadataReaderFactory metadataReaderFactory = new CachingMetadataReaderFactory(resourcePatternResolver);
    
  // 扫描包路径获取Set<BeanDefinition>
  // BeanDefinitionFilter：扫描时可进行条件过滤，过滤掉不需要的BeanDefinition
  public Set<BeanDefinition> findCandidateComponents(String basePackage, BeanDefinitionFilter sbdFlter) {
    Set<BeanDefinition> candidates = new LinkedHashSet<BeanDefinition>();
    try {
      // 获取指定包下
      String packageSearchPath = "classpath*:" + resolveBasePackage(basePackage) + "/" + "**/*.class";
      // 获取指定包中资源
      Resource[] resources = resourcePatternResolver.getResources(packageSearchPath);
      // 遍历资源
      for (Resource resource : resources) {
        if (resource.isReadable()) {
          try {
            // 将资源转换成元数据
            MetadataReader metadataReader = metadataReaderFactory.getMetadataReader(resource);
            // 将元数据转为BeanDefinition
            ScannedGenericBeanDefinition sbd = new ScannedGenericBeanDefinition(metadataReader);
            sbd.setResource(resource);
            sbd.setSource(resource);
            // 根据传入的条件过滤对象来过滤掉不符合条件的BeanDefinition
            // sbdFlter.filter(sbd)为true时表示保留
            if ((sbdFlter != null) && (sbdFlter.filter(sbd))) {  
               candidates.add(sbd);
            }
          } catch (Throwable ex) {
            throw new BeanDefinitionStoreException("Failed to read candidate component class: " + resource, ex);
          }
        }
      }
    } catch (IOException ex) {
      throw new BeanDefinitionStoreException("I/O failure during classpath scanning", ex);
    }
    return candidates;
  }
  
  // 调用根据包来获取包内BeanDefinition
  // 扫描时，BeanDefinitionFilter（相当于过滤条件）过滤掉不符合条件的BeanDefinition
  Set<BeanDefinition> candidateComponents = springUtils.findCandidateComponents(packageStr,
                      new BeanDefinitionFilter() {
     //匿名内部类               
  	@Override
  	public boolean filter(ScannedGenericBeanDefinition sbd) {
  		try {
  			GenericBeanDefinition definition = sbd;
  			Class<?> entityClazz = Class.forName(definition.getBeanClassName());
  			SocketIOEvent annotation = (SocketIOEvent) entityClazz
                              .getAnnotation(SocketIOEvent.class);
  			SocketIOListener listener = (SocketIOListener) 		 	
          										entityClazz.getAnnotation(SocketIOListener.class);
  			SocketDisconnectListener dicconnectListener = (SocketDisconnectListener) 
          										entityClazz.getAnnotation(SocketDisconnectListener.class);
  			if (annotation == null && listener == null && dicconnectListener == null) {
  					return false;        
  			}
  					return true;
  			} catch (ClassNotFoundException e) {
  					logger.error("", e);
  			}
  			 	return false;
  		}
  	});
  }
  ```

  

## 分页

