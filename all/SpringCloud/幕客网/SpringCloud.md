项目演示：`微信公众号：师兄干货`

Web后端：`http://sell.springboot.cn/sell/seller/order/list`

版本：`SpringBoot1.5.2`、`Linux centos7.3`、`Maven 3.3.9`、`MySQL 5.7.17`、`Nginx 1.11.7`、`Redis 3.2.8`

* 角色划分
  * 买家（手机端）
  * 卖家（PC端）

![image-20190802165211895](/Users/dingyuanjie/Documents/study/github/woodyprogram/img/image-20190802165211895.png)

![image-20190802165254507](/Users/dingyuanjie/Documents/study/github/woodyprogram/img/image-20190802165254507.png)

![image-20190802165355202](/Users/dingyuanjie/Documents/study/github/woodyprogram/img/image-20190802165355202.png)

![image-20190802165608466](/Users/dingyuanjie/Documents/study/github/woodyprogram/img/image-20190802165608466.png)

```sql
-- 类目
create table `product_category` (
    `category_id` int not null auto_increment,
    `category_name` varchar(64) not null comment '类目名字',
    `category_type` int not null comment '类目编号',
    `create_time` timestamp not null default current_timestamp on update current_timestamp comment '创建时间',
    `update_time` timestamp not null default current_timestamp on update current_timestamp comment '修改时间',
    primary key (`category_id`),
    unique key `uqe_category_type` (`category_type`)
);
INSERT INTO `product_category` (`category_id`, `category_name`, `category_type`, `create_time`, `update_time`)
VALUES
	(1,'热榜',11,'2017-03-28 16:40:22','2017-11-26 23:39:36'),
	(2,'好吃的',22,'2017-03-14 17:38:46','2017-11-26 23:39:40');

-- 商品
create table `product_info` (
    `product_id` varchar(32) not null,
    `product_name` varchar(64) not null comment '商品名称',
    `product_price` decimal(8,2) not null comment '单价',
    `product_stock` int not null comment '库存',
    `product_description` varchar(64) comment '描述',
    `product_icon` varchar(512) comment '小图',
    `product_status` tinyint(3) DEFAULT '0' COMMENT '商品状态,0正常1下架',
    `category_type` int not null comment '类目编号',
    `create_time` timestamp not null default current_timestamp on update current_timestamp comment '创建时间',
    `update_time` timestamp not null default current_timestamp on update current_timestamp comment '修改时间',
    primary key (`product_id`)
);
INSERT INTO `product_info` (`product_id`, `product_name`, `product_price`, `product_stock`, `product_description`, `product_icon`, `product_status`, `category_type`, `create_time`, `update_time`)
VALUES
	('157875196366160022','皮蛋粥',0.01,39,'好吃的皮蛋粥','//fuss10.elemecdn.com/0/49/65d10ef215d3c770ebb2b5ea962a7jpeg.jpeg',0,1,'2017-03-28 19:39:15','2017-07-02 11:45:44'),
	('157875227953464068','慕斯蛋糕',10.90,200,'美味爽口','//fuss10.elemecdn.com/9/93/91994e8456818dfe7b0bd95f10a50jpeg.jpeg',1,1,'2017-03-28 19:35:54','2017-04-21 10:05:57'),
	('164103465734242707','蜜汁鸡翅',0.02,982,'好吃','//fuss10.elemecdn.com/7/4a/f307f56216b03f067155aec8b124ejpeg.jpeg',0,1,'2017-03-30 17:11:56','2017-06-24 19:20:54');

-- 订单
create table `order_master` (
    `order_id` varchar(32) not null,
    `buyer_name` varchar(32) not null comment '买家名字',
    `buyer_phone` varchar(32) not null comment '买家电话',
    `buyer_address` varchar(128) not null comment '买家地址',
    `buyer_openid` varchar(64) not null comment '买家微信openid',
    `order_amount` decimal(8,2) not null comment '订单总金额',
    `order_status` tinyint(3) not null default '0' comment '订单状态, 默认为新下单',
    `pay_status` tinyint(3) not null default '0' comment '支付状态, 默认未支付',
    `create_time` timestamp not null default current_timestamp on update current_timestamp comment '创建时间',
    `update_time` timestamp not null default current_timestamp on update current_timestamp comment '修改时间',
    primary key (`order_id`),
    key `idx_buyer_openid` (`buyer_openid`)
);

-- 订单商品
create table `order_detail` (
    `detail_id` varchar(32) not null,
    `order_id` varchar(32) not null,
    `product_id` varchar(32) not null,
    `product_name` varchar(64) not null comment '商品名称',
    `product_price` decimal(8,2) not null comment '当前价格,单位分',
    `product_quantity` int not null comment '数量',
    `product_icon` varchar(512) comment '小图',
    `create_time` timestamp not null default current_timestamp on update current_timestamp comment '创建时间',
    `update_time` timestamp not null default current_timestamp on update current_timestamp comment '修改时间',
    primary key (`detail_id`),
    key `idx_order_id` (`order_id`)
);

-- 用户
CREATE TABLE `user_info` (
  `id` varchar(32) NOT NULL,
  `username` varchar(32) DEFAULT '',
  `password` varchar(32) DEFAULT '',
  `openid` varchar(64) DEFAULT '' COMMENT '微信openid',
  `role` tinyint(1) NOT NULL COMMENT '1买家2卖家',
  `create_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP on update current_timestamp COMMENT '创建时间',
  `update_time` timestamp NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '修改时间',
  PRIMARY KEY (`id`)
);
```

## 环境

* 虚拟机的使用
  * 虚拟机与本机互通(ping)
    * virtual虚拟机
* jdk
* maven

## 日志

* 日志门面SLF4j - 日志实现Logback
* logback-spring.xml
  * 区分info和error日志
  * 每天产生一个日志文件

![image-20190802174711946](/Users/dingyuanjie/Documents/study/github/woodyprogram/img/image-20190802174711946.png)

* logback-spring.xml
  * 要弄的非常熟

```xml
<?xml version="1.0" encoding="UTF-8" ?>
<configuration>
    <appender name="consoleLog" class="ch.qos.logback.core.ConsoleAppender">
        <layout class="ch.qos.logback.classic.PatternLayout">
            <pattern>
                %d - %msg%n
            </pattern>
        </layout>
    </appender>

    <!--配置成 INFO，不打印ERROR信息（ERROR级别比INFO高，所以级别为INFO时会打印ERROR，要避免打印ERROR）-->
    <appender name="fileInfoLog" class="ch.qos.logback.core.rolling.RollingFileAppender">
        <filter class="ch.qos.logback.classic.filter.LevelFilter">
            <level>ERROR</level>
            <!--如果匹配到ERROR，则不打印-->
            <onMatch>DENY</onMatch>
            <!--匹配到非ERROR则打印-->
            <onMismatch>ACCEPT</onMismatch>
        </filter>
        <encoder>
            <pattern>
                %msg%n
            </pattern>
        </encoder>
        <!--滚动策略-->
        <rollingPolicy class="ch.qos.logback.core.rolling.TimeBasedRollingPolicy">
            <!--路径-->
            <fileNamePattern>/Users/dingyuanjie/video/SpringBoot企业微信点餐系统/资料/coding-117/log/tomcat/sell/info.%d.log</fileNamePattern>
            <!-- <fileNamePattern>/var/log/tomcat/sell/info.%d.log</fileNamePattern>-->
        </rollingPolicy>
    </appender>

    <appender name="fileErrorLog" class="ch.qos.logback.core.rolling.RollingFileAppender">
        <filter class="ch.qos.logback.classic.filter.ThresholdFilter">
            <level>ERROR</level>
        </filter>
        <encoder>
            <pattern>
                %msg%n
            </pattern>
        </encoder>
        <!--滚动策略-->
        <rollingPolicy class="ch.qos.logback.core.rolling.TimeBasedRollingPolicy">
            <!--路径-->
						<!--<fileNamePattern>/var/log/tomcat/sell/error.%d.log</fileNamePattern>-->
            <fileNamePattern>/Users/dingyuanjie/video/SpringBoot企业微信点餐系统/资料/coding-117/log/tomcat/sell/error.%d.log</fileNamePattern>
        </rollingPolicy>
    </appender>

    <root level="info">
        <appender-ref ref="consoleLog" />
        <appender-ref ref="fileInfoLog" />
        <appender-ref ref="fileErrorLog" />
    </root>
</configuration>
```

```yaml
# 设置日志级别：打印sql
logging:
  level:
    com.imooc.dataobject.mapper: trace
```

* `git clone https://git.imooc.com/coding-117/coding-117.git`

* jpa

  ```java
  // JSON String 转为 List
  6110 8987 2431 036
  6110 8987 2431 192
  ```

  ```java
  // Date 转 Long  （毫秒转为秒）
  public class Date2LongSerializer extends JsonSerializer<Date> {
    @Override
    public void serialize(Date date, JsonGenerator jsonGenerator, SerializerProvider serializerProvider) throws IOException, JsonProcessingException {
      jsonGenerator.writeNumber(date.getTime() / 1000);
    }
  }
  
  // 
  /** 创建时间. */
  @JsonSerialize(using = Date2LongSerializer.class)
  private Date createTime;
  
  /** 更新时间. */
  @JsonSerialize(using = Date2LongSerializer.class)
  private Date updateTime;
  
  // 字段值为null，则不返回该字段
  //@JsonInclude(JsonInclude.Include.NON_NULL)
  // 配置文件中配置
  spring.jackson.default-property-inclusion=non_null
  
  // 将对象格式化成json格式
  public class JsonUtil {
      public static String toJson(Object object) {
          GsonBuilder gsonBuilder = new GsonBuilder();
          gsonBuilder.setPrettyPrinting();
          Gson gson = gsonBuilder.create();
          return gson.toJson(object);
      }
  }
  
  ```

 public ProductInfo findOne(String productId) {
  		Optional<ProductInfo> productInfoOptional = repository.findById(productId);
//        if (productInfoOptional.isPresent()) {
  //            return productInfoOptional.get().addImageHost(upYunConfig.getImageHost());
  //        }
  //        return null;

  		productInfoOptional.ifPresent(e -> e.addImageHost(upYunConfig.getImageHost()));
  		return productInfoOptional.orElse(null);
  }
  ```
  
  ```java
  // 分页
  //订单列表
  @GetMapping("/list")
  public ResultVO<List<OrderDTO>> list(@RequestParam("openid") String openid,
  @RequestParam(value = "page", defaultValue = "0") Integer page,
  @RequestParam(value = "size", defaultValue = "10") Integer size) {
    if (StringUtils.isEmpty(openid)) {
      log.error("【查询订单列表】openid为空");
      throw new SellException(ResultEnum.PARAM_ERROR);
    }
  
    PageRequest request = PageRequest.of(page, size);
    Page<OrderDTO> orderDTOPage = orderService.findList(openid, request);
  
    return ResultVOUtil.success(orderDTOPage.getContent());
  }
  
@Override
  public Page<OrderDTO> findList(String buyerOpenid, Pageable pageable) {
  Page<OrderMaster> orderMasterPage = orderMasterRepository.findByBuyerOpenid(buyerOpenid, pageable);
  
    List<OrderDTO> orderDTOList = OrderMaster2OrderDTOConverter.convert(orderMasterPage.getContent());
  
    return new PageImpl<OrderDTO>(orderDTOList, pageable, orderMasterPage.getTotalElements());
  }
  ```

  

  ```java
  // entity 类
  @Entity
  @Data
  @DynamicUpdate  // 自动更新mysql的设置UPDATE CURRENT_TIMESTAMP的update_time字段
  // create_time   规则：_改为驼峰形式 
  private Date createTime;
  
  @Id
  @GeneratedValue
  
  // jpa interface，使用时，直接@Autowired，自带默认方法
  // 实体，主键类型
  public interface ProductCategoryRepository extends JpaRepository<ProductCategory, Integer> {
      // 定义符合规则的方法名findByCategoryTypeIn，不用实现，直接使用
      List<ProductCategory> findByCategoryTypeIn(List<Integer> categoryTypeList);
  }
  public interface ProductInfoRepository extends JpaRepository<ProductInfo, String> {
      List<ProductInfo> findByProductStatus(Integer productStatus);
  }
  public interface OrderMasterRepository extends JpaRepository<OrderMaster, String> {
      Page<OrderMaster> findByBuyerOpenid(String buyerOpenid, Pageable pageable);
  }
  
  // 验证数据不能为空
  // OrderForm
  @NotEmpty(message = "姓名必填")
  private String name;
  //...
  
  //创建订单
  @PostMapping("/create")
  public ResultVO<Map<String, String>> create(@Valid OrderForm orderForm,
                                              BindingResult bindingResult) {
    if (bindingResult.hasErrors()) {
      log.error("【创建订单】参数不正确, orderForm={}", orderForm);
      throw new SellException(ResultEnum.PARAM_ERROR.getCode(),
                              bindingResult.getFieldError().getDefaultMessage());
    }
    ...
  }
  
  // 测试类  右键类名 - GOTO - Test
  @RunWith(SpringRunner.class)
  @SpringBootTest
  
  @Test
  @Transactional //不向数据库中插入数据
  	Assert.assertNotNull(result);
  //        Assert.assertNotEquals(null, result);
  
  // jpa 分页
  @Override
  public Page<ProductInfo> findAll(Pageable pageable) {
    Page<ProductInfo> productInfoPage = repository.findAll(pageable);
    productInfoPage.getContent().stream()
      .forEach(e -> e.addImageHost(upYunConfig.getImageHost()));
    return productInfoPage;
  }
  
  @Test
  public void findAll() throws Exception {
    // 第几页，该页有多少数据
    PageRequest request = PageRequest.of(0, 2);
    Page<ProductInfo> productInfoPage = productService.findAll(request);
    //        System.out.println(productInfoPage.getTotalElements());
    Assert.assertNotEquals(0, productInfoPage.getTotalElements());
  }
  
  @JsonProperty("id")
  private String productId;
  
  // 分页  byton 请求（page，perPage）、返回（总数和当前页面数据量）
  
  //lambda
  // 获取list对象中某个属性（List）
  List<Integer> categoryTypeList = productInfoList.stream()
                  .map(e -> e.getCategoryType())
                  .collect(Collectors.toList());
  // 给List对象中的list属性设置值
List<CartDTO> cartDTOList = orderDTO.getOrderDetailList().stream().map(e ->
                  new CartDTO(e.getProductId(), e.getProductQuantity())
        ).collect(Collectors.toList());
  
  // 业务
  商品，有些字段不由前端传过来，而是再次从数据库查询，然后更新，如商品单价、库存等
  判断状态（特定状态下才能改状态），再更改状态
  限制只能自己查自己的订单
  ```

  * 联调

  ```java
  // 开启virtual虚拟机
  192.168.1.105/#/order
  //Chrome - F12 - Application
  // console中设置 document.cookie='openid=abc123'
  // Application 中 Cookies：openid abc123
  // 再访问 192.168.1.105 就会有页面
  // 进入虚拟机，设置nginx代理 (虚拟机能够访问本地)
  vim /usr/local/nginx/conf/nginx.conf
  	// 微信需要使用域名
  	server_name 改为 sell.com
  	location /sell/ {
  		proxy_pass http://本地ip:8080/sell/; 
  	}
  nginx -s reload
  
  //修改本机host
  vim etc/hosts
  // 虚拟机ip
  192.168.1.102 sell.com
  
  //访问 sell.com
  
  ```

## 微信授权

### 微信授权

* 获取OpenID
  * 手动方式
    * 设置域名、获取code、换取access_token
  * 利用第三方SDK

```properties r
# 微信网页授权  以微信文档为准
官方文档=https://mp.weixin.qq.com/wiki
调试=https://natapp.cn
第三方SDK=https://github.com/Wechat-Group/weixin-java-tools
```

```java
// 微信访问到自己开发的电脑
https://natapp.cn/
复制 authtoken ，下载客户端并解压，进入目录
ll
chmod 777 natapp
./natapp -authtoken=68a5946a2edc73fa

// 启动本地项目
http://127.0.0.1:8080/sell/buyer/product/list
// 开启 natapp
cd /Users/dingyuanjie/video/code
./natapp -authtoken=68a5946a2edc73fa  // 68a5946a2edc73fa为natapp上获取authtoken
// 返回 Forwarding              http://2dk2sp.natappfree.cc -> 127.0.0.1:8080
// 因为是免费的，所以是随机的，每次都变2dk2sp.natappfree.cc
// 将 其他电脑通过 http://2dk2sp.natappfree.cc 可以访问自己电脑的8080端口
// 可用于微信访问本级端口进行调试
http://2dk2sp.natappfree.cc/sell/buyer/product/list
```

```java
// 微信公众号【服务号-有微信支付】
// 网页授权域名（域名需备案）
【公众号设置】- 【功能设置】- 【网页授权域名】- 【设置】- 填写 natapp的域名 - 根据提示下载文件并放到项目的resources/static目录下（MP_xxx.txt）
通过 http://2dk2sp.natappfree.cc/MP_xxx.txt访问
如sell.nataapp4.cc

https://mp.weixin.qq.com/wiki?t=resource/res_main&id=mp1421140842
// 按照步骤依次操作，url中的参数按照说明依次填写，其中redirect_uri为自己开发认证登录接口
https://open.weixin.qq.com/connect/oauth2/authorize?appid=APPID&redirect_uri=REDIRECT_URI&response_type=code&scope=SCOPE&state=STATE#wechat_redirect
// 将上述url发送到微信并点击访问，就会调用redirect_uri接口生成code（开发时可断点调试）
```

```java
@RestController
@RequestMapping("/weixin")
@Slf4j
public class WeixinController {
    @GetMapping("/auth")
    public void auth(@RequestParam("code") String code) {
        log.info("进入auth方法。。。");
        log.info("code={}", code);

        String url = "https://api.weixin.qq.com/sns/oauth2/access_token?appid=wxd898fcb01713c658&secret=29d8a650db31472aa87800e3b0d739f2&code=" + code + "&grant_type=authorization_code";
        RestTemplate restTemplate = new RestTemplate();
        String response = restTemplate.getForObject(url, String.class);
        log.info("response={}", response);
    }
}
```

![image-20190803184008443](/Users/dingyuanjie/Documents/study/github/woodyprogram/img/image-20190803184008443.png)

* 第三方sdk（推荐方式）

  ```java
  // https://github.com/Wechat-Group/weixin-java-tools
  https://github.com/Wechat-Group/WxJava/wiki
  网页授权
  https://github.com/Wechat-Group/WxJava/wiki/MP_OAuth2%E7%BD%91%E9%A1%B5%E6%8E%88%E6%9D%83
  // WechatController.java
  // 微信上访问
  http://微信可以访问的本地内网域名/sell/wechat/authorize?returnUrl=http://www.imooc.com
  // 微信上会跳转到 http://www.imooc.com
  ```

  ```java
  // 虚拟机上配置前端
  cd /opt/code/sell_fe_buyer
  cd config
  vim index.js
  sellUrl:'http://sell.com',
  openidUrl:'http://sell.natapp4.cc/sell/wechat/authorize',
  wechatPayUrl:'http://sell.natapp4.cc/sell/pay/create'
  // 回到前端项目根目录 /opt/code/sell_fe_buyer
  // 重新构建
  npm run build
  // 构建好所在目录
  ls -al dist/
  // 把该目录下文件都拷贝到网站根目录
  cp -r dist/* /opt/data/wwwroot/sell/  
  // 重新访问
  sell.com
  
  // 抓包工具 charles
  // 手机ip 192.168.30.143
  // 电脑ip 192.168.30.112
  电脑上ping下手机ip
  手机上-无线局域网-HTTP代理-手动-服务器-填写电脑ip，端口8888
  ```

### 微信支付

* 第三方sdk,`https://github.com/Pay-Group/best-pay-sdk/`

  ![image-20190803194002712](/Users/dingyuanjie/Documents/study/github/woodyprogram/img/image-20190803194002712.png)

```java
// https://pay.weixin.qq.com/wiki/doc/api/index.html
// PayController.java
// create.ftl
http://localhost:8080/sell/pay/create?orderId=233r&returnUrl=http://www.baidu.com
// 前端配置 index.js
wechatPayUrl:'http://sell.natapp4.cc/sell/pay/create'
```

### 微信退款

```java
// 【微信支付-商户平台】- 【账户中心】 - 【API安全】- 【下载证书】
配置文件中配置证书路径keyPath
```

## 支付流程

* 支付需要openid（是微信用户在公众号appid下的唯一用户标识，appid不同，则获取到的openid不同）

  * JSAPI支付

* 微信授权

  * 获取openid
    * 微信公众平台-微信网页授权

  ```java
  https://open.weixin.qq.com/connect/oauth2/authorize?appid=APPID&redirect_uri=REDIRECT_URI&response_type=code&scope=SCOPE&state=STATE#wechat_redirect
  // REDIRECT_URI 需要到 natapp.cn购买域名，例如 http://lsx888.natapp1.cc
  // lsx888.natapp1.cc 需要到 测试号管理-网页帐号中配置
  // 使用urlEncoder对链接进行处理
  // scope=snsapi_base
  
  浏览器访问
  http://localhost:8080/sell/wechat/userInfo?code=abc&state=edc
  {
  "code": 20,
  "msg": "invalid code, hints: [ req_id: RHcDlfyFe-Cymf9a ]"
  }
  // 将 抓包获取的 code 填写到url参数里
  http://localhost:8080/sell/wechat/userInfo?code=抓包获取的code&state=edc
  ```

```java
// 第一步开发：授权
	// 【开始开发】- 【接口测试号申请】
https://mp.weixin.qq.com/wiki?t=resource/res_main&id=mp1421137522
		appID(mpAppId)：wx83b775319ba15501
		appsecret(mpAppSecret)：53bb9a26149bb9f1451f1cb9e8427930
扫描关注【测试号二维码】
【网页帐号】 - 【网页授权获取用户基本信息】 - 【修改】配置自己的外网地址，如【sell.natapp4.cc】
测试：方法中断点
1. 先本地：http://localhost:8080/sell/wechat/authorize?returnUrl=http://www.imooc.com
2. 再域名：http://2dk2sp.natappfree.cc/sell/wechat/authorize?returnUrl=http://www.imooc.com
3. 域名再发送到手机微信中去访问：http://2dk2sp.natappfree.cc/sell/wechat/authorize?returnUrl=http://sell.com/#/

// 第二步开发：支付
// 第三步开发：授权 + 支付
```

## 管理平台

```java
// 卖家端订单
// SellerOrderController.java
//list.ftl  freemarker  修改前端代码后不用重启  build - build project
<h1>orderDTOPage.totalPages</h1>
<h1>orderDTOPage.getTotalPages()</h1>
// 前端页面 www.ibootstrap.cn
// 拖拽控件、下载选择控件的code

// 卖家端通用功能和上下架

// 卖家端新增商品和类目

// 买家和卖家端联通

// 项目优化

// 项目部署

```

```java
// 枚举  根据 code 获取 message
public interface CodeEnum {
    Integer getCode();
}

public class EnumUtil {
    public static <T extends CodeEnum> T getByCode(Integer code, Class<T> enumClass) {
        for (T each: enumClass.getEnumConstants()) {
            if (code.equals(each.getCode())) {
                return each;
            }
        }
        return null;
    }
}

EnumUtil.getByCode(payStatus, PayStatusEnum.class);

@Getter
public enum PayStatusEnum implements CodeEnum {

    WAIT(0, "等待支付"),
    SUCCESS(1, "支付成功"),
    ;

    private Integer code;
    private String message;

    PayStatusEnum(Integer code, String message) {
        this.code = code;
        this.message = message;
    }
}
```

## 分布式Session

* 登录
  
  * 验证身份，存储信息
* 登出
  
  * 失效浏览状态
* 分布式系统
  
  * 旨在支持应用程序和服务的开发，可以利用物理架构由多个自治的处理元素，不共享主内存，但通过网络发送消息合作
* 多节点、消息通信、不共享内存
* 分布式系统、集群、分布式计算

* 微信扫码登录

  ```java
  //微信开放平台 - 资源中心 - 网站应用 - 网站应用微信登录开发指南
  // 公司资质 - 公众平台
  openAppId
  openAppSecret
  1. 请求code
  2. 根据code获取access_token
  
  // 获取code、openid
  	// WechatController.java
  // 登录、登出
  	// SellerUserController.java
  	// 登录二维码测试：sell.natapp4.cc/sell/wechat/qrAuthorize?returnUrl=www.imooc.com
  	// 登录：sell.natapp4.cc/sell/wechat/qrAuthorize?returnUrl=http://sell.natapp4.cc/sell/seller/login
  String.format("token_%s", token);
  // 跳转时用绝对地址，不要用相对地址
  return new ModelAndView("redirect:" + projectUrlConfig.getSell() + "/sell/seller/order/list");
  
  // aop访问登录验证
  // SellerAuthorizeAspect.java
  // 拦截登录异常
  // SellExceptionHandler.java
  "112".concat("sf");
  
  // 消息
  // 微信模版消息、WebSocket
  【微信公众平台】 - 【消息管理】 - 【发送消息-模版消息接口】- 选择模版
  // PushMessageImpl.java
  ```

  ```js
  // 订单消息通知  websocket
  // order/list.ftl    https://www.bootcdn.cn/
  
  <#--弹窗-->
  <div class="modal fade" id="myModal" role="dialog" aria-labelledby="myModalLabel" aria-hidden="true">
      <div class="modal-dialog">
          <div class="modal-content">
              <div class="modal-header">
                  <button type="button" class="close" data-dismiss="modal" aria-hidden="true">×</button>
                  <h4 class="modal-title" id="myModalLabel">
                      提醒
                  </h4>
              </div>
              <div class="modal-body">
                  你有新的订单
              </div>
              <div class="modal-footer">
                  <button onclick="javascript:document.getElementById('notice').pause()" type="button" class="btn btn-default" data-dismiss="modal">关闭</button>
                  <button onclick="location.reload()" type="button" class="btn btn-primary">查看新的订单</button>
              </div>
          </div>
      </div>
  </div>
  
  <#--播放音乐-->
  <audio id="notice" loop="loop">
      <source src="/sell/mp3/song.mp3" type="audio/mpeg" />
  </audio>
  
  <script src="https://cdn.bootcss.com/jquery/1.12.4/jquery.min.js"></script>
  <script src="https://cdn.bootcss.com/bootstrap/3.3.5/js/bootstrap.min.js"></script>
  <script>
      var websocket = null;
      if('WebSocket' in window) {
          websocket = new WebSocket('ws://sell.natapp4.cc/sell/webSocket');
      }else {
          alert('该浏览器不支持websocket!');
      }
  
      websocket.onopen = function (event) {
          console.log('建立连接');
      }
  
      websocket.onclose = function (event) {
          console.log('连接关闭');
      }
  
      websocket.onmessage = function (event) {
          console.log('收到消息:' + event.data)
          //弹窗提醒, 播放音乐
          $('#myModal').modal('show');
  
          document.getElementById('notice').play();
      }
  
      websocket.onerror = function () {
          alert('websocket通信发生错误！');
      }
  
      window.onbeforeunload = function () {
          websocket.close();
      }
  
  </script>
  ```

  ```java
  @Autowired
  private WebSocket webSocket;
  
  //发送websocket消息
  webSocket.sendMessage(orderDTO.getOrderId());
  
  // WebSocket.java
  @Component
  @ServerEndpoint("/webSocket")
  @Slf4j
  public class WebSocket {
  
      private Session session;
  
      private static CopyOnWriteArraySet<WebSocket> webSocketSet = new CopyOnWriteArraySet<>();
  
      @OnOpen
      public void onOpen(Session session) {
          this.session = session;
          webSocketSet.add(this);
          log.info("【websocket消息】有新的连接, 总数:{}", webSocketSet.size());
      }
  
      @OnClose
      public void onClose() {
          webSocketSet.remove(this);
          log.info("【websocket消息】连接断开, 总数:{}", webSocketSet.size());
      }
  
      @OnMessage
      public void onMessage(String message) {
          log.info("【websocket消息】收到客户端发来的消息:{}", message);
      }
  
      public void sendMessage(String message) {
          for (WebSocket webSocket: webSocketSet) {
              log.info("【websocket消息】广播消息, message={}", message);
              try {
                  webSocket.session.getBasicRemote().sendText(message);
              } catch (Exception e) {
                  e.printStackTrace();
              }
          }
      }
  }
  ```

## 异常的捕获

```java
// 异常捕获类（异常统一处理）
@ControllerAdvice
public class SellExceptionHandler {
    @Autowired
    private ProjectUrlConfig projectUrlConfig;

    //拦截登录异常
    //http://sell.natapp4.cc/sell/wechat/qrAuthorize?returnUrl=http://sell.natapp4.cc/sell/seller/login
    @ExceptionHandler(value = SellerAuthorizeException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public ModelAndView handlerAuthorizeException() {
        return new ModelAndView("redirect:"
        .concat(projectUrlConfig.getWechatOpenAuthorize())
        .concat("/sell/wechat/qrAuthorize")
        .concat("?returnUrl=")
        .concat(projectUrlConfig.getSell())
        .concat("/sell/seller/login"));
    }

    @ExceptionHandler(value = SellException.class)
    @ResponseBody
    public ResultVO handlerSellerException(SellException e) {
        return ResultVOUtil.error(e.getCode(), e.getMessage());
    }

    @ExceptionHandler(value = ResponseBankException.class)
    @ResponseStatus(HttpStatus.FORBIDDEN)
    public void handleResponseBankException() {

    }
}

// 异常类
@Getter
public class SellException extends RuntimeException{
    private Integer code;
    public SellException(ResultEnum resultEnum) {
        super(resultEnum.getMessage());

        this.code = resultEnum.getCode();
    }
    public SellException(Integer code, String message) {
        super(message);
        this.code = code;
    }
}

@Getter
public enum ResultEnum {
	SUCCESS(0, "成功"),
	PARAM_ERROR(1, "参数不正确"),
	//...
}
```

## MyBatis

```mysql
// 引入依赖
// 引导类上扫描Mapper接口文件
@MapperScan(basePackages = "com.imooc.dataobject.mapper")
// 配置文件中配置mapper的xml
mybatis:
  mapper-locations: classpath:mapper/*.xml
  
# 注解方式 或 xml方式

建表用sql，不用JPA建表
慎用@ManyToOne 和 @OneToMany
```

## 用压力测试模拟高并发

```java
// 使用简易工具  Apache ab
// n 100个请求、c 100个并发，相当于100个人同时访问
// ab -n 100 -c 100 http://www.baidu.com
// t 60 秒
// ab -t 60 -c 100 http://www.baidu.com
```

## 分布式锁

* 秒杀

```java
// http://localhost:8080/sell/skill/order/123456 多刷新几次
// mac电脑
sudo apachectl -v
sudo apachectl start    // 浏览器访问 http://localhost/   显示 it works
ab -n 100 -c 10 http://localhost:8080/sell/skill/order/123456
ab -n 500 -c 100 http://localhost:8080/sell/skill/order/123456
浏览器访问 http://localhost:8080/sell/skill/order/123456  数据对应不上
// orderProductMockDiffUser 方法加上synchronized关键字 重启应用，再压测
ab -n 500 -c 100 http://localhost:8080/sell/skill/order/123456
synchronized
	数据对应上了，是一种解决办法，但无法做到细粒度控制，只适合单点的情况
redis分布式锁 redis.cn
	setnx(设置成功返回1，失败返回0)、getset(设置新值返回旧值)
  //http://localhost:8080/sell/skill/order/123456
  ab -n 500 -c 100 http://localhost:8080/sell/skill/order/123456
  // 速度快，数目能够对应上，虽然只有部分用户能够拿到
	// 支持分布式，可以更细粒度的控制，多台机器上多个进程对一个数据进行操作的互斥
  
 
// synchronized关键字修饰
@Override
public synchronized void orderProductMockDiffUser(String productId)
{
  //1.查询该商品库存，为0则活动结束。
  int stockNum = stock.get(productId);
  if(stockNum == 0) {
    throw new SellException(100,"活动结束");
  }else {
    //2.下单(模拟不同用户openid不同)
    orders.put(KeyUtil.genUniqueKey(),productId);
    //3.减库存
    stockNum =stockNum-1;
    try {
      Thread.sleep(100);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
    stock.put(productId,stockNum);
  }
}
  
// 分布式锁  
@Override
public void orderProductMockDiffUser(String productId)
{
  //加锁
  long time = System.currentTimeMillis() + TIMEOUT;
  if(!redisLock.lock(productId, String.valueOf(time))) {
    throw new SellException(101, "1111111");
  }

  //1.查询该商品库存，为0则活动结束。
  int stockNum = stock.get(productId);
  if(stockNum == 0) {
    throw new SellException(100,"活动结束");
  }else {
    //2.下单(模拟不同用户openid不同)
    orders.put(KeyUtil.genUniqueKey(),productId);
    //3.减库存
    stockNum =stockNum-1;
    try {
      Thread.sleep(100);
    } catch (InterruptedException e) {
      e.printStackTrace();
    }
    stock.put(productId,stockNum);
  }

  //解锁
  redisLock.unlock(productId, String.valueOf(time));
}
```

```java
@Component
@Slf4j
public class RedisLock {

    @Autowired
    private StringRedisTemplate redisTemplate;

    /**
     * 加锁
     * @param key
     * @param value 当前时间+超时时间
     * @return
     */
    public boolean lock(String key, String value) {
        if(redisTemplate.opsForValue().setIfAbsent(key, value)) {
            return true;
        }
        // 下面代码防止加锁后，后面的代码出错导致锁无法释放
        //currentValue=A   这两个线程的value都是B  其中一个线程拿到锁
        String currentValue = redisTemplate.opsForValue().get(key);
        //如果锁过期
        if (!StringUtils.isEmpty(currentValue)
                && Long.parseLong(currentValue) < System.currentTimeMillis()) {
            //获取上一个锁的时间
            String oldValue = redisTemplate.opsForValue().getAndSet(key, value);
            if (!StringUtils.isEmpty(oldValue) && oldValue.equals(currentValue)) {
                return true;
            }
        }

        return false;
    }

    /**
     * 解锁
     * @param key
     * @param value
     */
    public void unlock(String key, String value) {
        try {
            String currentValue = redisTemplate.opsForValue().get(key);
            if (!StringUtils.isEmpty(currentValue) && currentValue.equals(value)) {
                redisTemplate.opsForValue().getOperations().delete(key);
            }
        }catch (Exception e) {
            log.error("【redis分布式锁】解锁异常, {}", e);
        }
    }

}
```

## Redis缓存

```java
//命中、失效、更细

// 启动类上加上
@EnableCaching

要实现序列化

//controller中的方法上
// 查询方法 和 保存方法 都要加 换粗注解，否则不会更新缓存
//list，第一次访问时返回的ResultVO对象会缓存到redis，后面访问直接从redis中返回，不会再执行代码
// 动态key：sellerId是方法的参数  condition条件成立时才缓存 unless依照结果判断是否缓存（result为返回对象-固定名称）
@Cacheable(cacheNames = "product", key = "#sellerId", condition = "#sellerId.length() > 3", unless = "#result.getCode() != 0")
// save， 会把内容更新到redis中
@Cacheable(cacheNames = "product", key = "123")   // 但是返回的ModelAndView无法序列化，也不是ResuleVO
//需使用 @CacheEvict，使缓存失效，下次list查询时再缓存进redis
@CacheEvict(cacheNames = "product", allEntries = true, beforeInvocation = true)

// 上述返回的不是同一个对象
// 返回是同一个对象   key要相同，否则是方法的参数值
@Cacheable(cacheName = "product" key = "123")
public ProductInfo findOne(String productId) {...}
@CachePut(cacheName = "product" key = "123")
public ProductInfo save(ProductInfo productInfo) {...}
// 可以直接在类上加
@CacheConfig(cacheNames = "product")
// 方法上的@Cache相关的注解就不用加 cacheNames参数了
```

## 部署

* tomcat

* java -jar

  ```java
  mvn clean package -Dmaven.test.skip=true
    
  <build>
  	<!--最终打包名称-->
  		<finalName>sell</finalName>
      ..
    
  连接虚拟机，将jar包放到 /opt/javaapps目录
  scp target/sell.jar root@ip:/opt/javaapps
  java -jar sell.jar
  //访问 虚拟机ip:8080/sell/buyer/product/list
  
  java -jar -Dserver.port=8090 -Dspring.profiles.actives=product sell.jar
  
  // 后台启动，返回启动进程号
  vim start.sh
  #!/bin/sh
  nohup java -jar sell.jar > /dev/null 2>&1 &
  // 执行
  bash start.sh
  ps -ef|grep sell
  
  cd /etc/systemd/system/
  ll
  vim sell.service
  ```

  ![image-20190804225131840](/Users/dingyuanjie/Documents/study/github/woodyprogram/img/image-20190804225131840.png)

  ```shell
  systemctl start sell
  ps -ef|grep sell
  systemctl stop sell
  ps -ef|grep sell
  # 开机启动
  systemctl enable sell
  # 禁止开机启动
  systemctl disable sell
  ```

  ## 总结

  * 项目分析设计
  * 微信特性
  * 微信支付与退款
  * Token认证
  * WebSocket消息
  * Redis缓存 + 分布式锁

  

  ## 抓包

  ```shell
  路由器ip  192.168.31.1
  手机ip 192.168.31.18
  电脑ip 192.168.31.32
  电脑上ping手机ip
  手机-无线局域网-配置HTTP代理：手动-服务器（电脑ip），端口8888
  ```
```
  
![image-20190804230743636](/Users/dingyuanjie/Documents/study/github/woodyprogram/img/image-20190804230743636.png)
  

![image-20190804231451984](/Users/dingyuanjie/Documents/study/github/woodyprogram/img/image-20190804231451984.png)

​```java
302 GET sell.springboot.cn 
	访问：http://sell.springboot.cn/sell/wechat/authorize?returnUrl=http://sell.springboot.cn/openid.html
	重定向，看Headers，跳转到微信授权地址：https://open.weixin.qq.com/connect/oauth2/authorize?appid=wxd898xxx&redirect_uri=http://sell.springboot.cn/sell/wechat/userInfo&response_type=code&scope=snsapi_base&state=http://sell.springboot.cn/openid.html#wechat_redirect
302 GET sell.springboot.cn
	访问：http://sell.springboot.cn/sell/wechat/userInfo?code=02123w3dxxx&state=http://sell.springboot.cn/openid.html
	//拿到code，请求微信接口获取openid
	重定向到：http://sell.springboot.cn/openid.html?openid=xcsfsfsxxxx
```

![image-20190805155513786](/Users/dingyuanjie/Documents/study/github/woodyprogram/img/image-20190805155513786.png)

* 把连接发送到微信里访问，再次调试

![image-20190805165757041](/Users/dingyuanjie/Documents/study/github/woodyprogram/img/image-20190805165757041.png)

