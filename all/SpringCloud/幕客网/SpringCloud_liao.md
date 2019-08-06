```shell
# 启动eureka
cd /Users/dingyuanjie/code/SpringCloud_liao/liao_SpringCloud/SpringCloud_Sell/eureka
mvn clean package
nohup java -jar target/eureka-0.0.1-SNAPSHOT.jar > /dev/null 2>&1 &
# 查看源码时，看类之间的关系
右键 - Diagrams - show Diagrams...
# 使用了@RequestBody注解后，需使用@PostMapping注解
# Post请求参数使用form形式
# 本地测试：启动一个服务多个实例时，直接在 Run/Debug Configurations中 VM options中 -Dserver.port=8081 指定启动参数
# 本地启动时，可查看控制台有哪些映射路径
# 版本先和例子里的一样，避免踩坑
# service【方法上】加上@Transactional
# 【要保证服务不去调用不是自己的数据库】
# 【幕客网 ajax跨域完全讲解】
```

# 微服务

* 是一种架构风格
* 一系列微小的服务共同组成、跑在自己的进程里、每个服务为独立的业务开发、独立部署、分布式管理
* 分布式
  * 旨在支持应用程序和服务的开发，可以利用物理架构由多个自治的处理元素，不共享主内存，但通过网络发送消息合作

![image-20190805172620066](/Users/dingyuanjie/Documents/study/github/woodyprogram/img/image-20190805172620066.png)

* 组件
  * 服务注册发现
  * 服务网关
  * 后端通用服务（中间层服务）：启动时将地址信息注册到服务地址表里
  * 前端服务（边缘服务）：通过查询注册表来调用服务，对后端的服务做必要的聚合（多个接口聚合成一个，只需调用一次），裁剪（根据不同的设备返回的信息进行裁剪）暴露给必要的设备
* SpringCloud
  * 是一个开发工具集，含了多个子项目，简化了分布式开发
* 单体架构
  * 优点：容易测试、部署
  * 缺点：开发效率低、代码维护难、部署不灵活、稳定性不高、扩展性不够

## Eureka

* Eureka Server（注册中心）

  ```java
  // 不用每次用idea启动Eureka Server，直接通过终端输入maven命令进行启动
  // 进入 Eureka 项目目录
  mvn clean package
  // 然后通过 java -jar target/xxx.jar 启动
  // 后台启动，返回所占用进程号
    nohup java -jar target/eureka-0.0.1-SNAPSHOT.jar > /dev/null 2>&1 &
    ps -ef|grep eureka
    kill -9 进程号
  ```

  ```java
  @EnableEurekaServer
  // 高可用
  //application-eureka1.yml
  eureka:
    client:
      service-url:
        defaultZone: http://eureka2:8762/eureka/
  #    register-with-eureka: false
    server:
      enable-self-preservation: false
  spring:
    application:
      name: eureka
  server:
    port: 8761
  // application-eureka2.yml
  eureka:
    client:
      service-url:
        defaultZone: http://eureka1:8761/eureka/
  #    register-with-eureka: false
    server:
      enable-self-preservation: false
  spring:
    application:
      name: eureka
  server:
    port: 8762    
  ```

* Eureka Client（服务注册）

  ```yaml
  @EnableEurekaClient
  # 高可用
  eureka:
    client:
      service-url:
      	# 配置hosts  127.0.0.1  eureka1   127.0.0.1  eureka2
        defaultZone: http://eureka1:8761/eureka/,http://eureka2:8762/eureka/
  ```

* 总结
  * `@EnableEurekaServer`和`EnableEurekaClient`
  * 心跳检测、健康检查、负载均衡等功能
  * Eureka的高可用，生产上建议至少两台以上
  * 分布式系统中，服务注册中心是最重要的基础部分
* 服务发现两种方式
  * 客户端发现
    * Eureka
  * 服务端发现
    * Nginx、Zookeeper、Kubernetes

## 服务拆分

* 好服务是一直演进出来的

* 启动和终点
  * 业务形态不适合的
    * 系统中包含很多强事务场景的
    * 业务相对稳定，迭代周期长
    * 访问压力不大，可用性要求不高
* 如何拆分
  * 单一职责，松耦合、高内聚
  * 关注点分离
    * 按职责、按通用性、按粒度级别
  * 针对边界设计API
  * 依据边界权衡数据冗余
  * 服务和数据的关系
    * 先考虑业务功能，再考虑数据
    * 无状态服务
  * 如何拆分数据
    * 每个微服务都有单独的数据存储
    * 依据服务特点选择不同结构的数据库类型
    * 难点在于确定边界

![image-20190805215914126](/Users/dingyuanjie/Documents/study/github/woodyprogram/img/image-20190805215914126.png)

* 商品服务
* 订单服务（订单服务调用商品服务）

## 应用间通信

- RPC
  - Dubbo

* HTTP

  * SpringCloud中服务间两种restful调用方式

    * RestTemplate

      ```java
      // 第一种（直接使用RestTemplate，URL写死）
      RestTemplate restTemplate = new RestTemplate();
      //restTemplate.getForObject(url, 返回类型);
      
      // 第二种（利用LoadBalanceClient通过应用名获取url，然后再使用RestTemplate）
      @Autowired
      private LoadBalancerClient loadBalancerClient;
      RestTemplate restTemplate = new RestTemplate();
      ServiceInstance serviceInstance = loadBalancerClient.choose("PRODUCT");
      String url = String.format("http://%s:%s", serviceInstance.getHost(), serviceInstance.getPort()) + "/msg";
      String response = restTemplate.getForObject(url, String.class);
      
      // 第三种(利用@LoadBalanced，可在RestTemplate里使用应用的名字)
      @Component
      public class RestTemplateConfig {
        
        @Bean
        @LoadBalanced
        public RestTemplate restTemplate() {
          return new RestTemplate();
        }
      }
      
      @Autowired
      private RestTemplate restTemplate;
      String response = restTemplate.getForObject("http://PRODUCT/msg", String.class);
      ```

    * Feign

      ```java
      // 调用方 1. 添加Feign依赖 2.主类上添加@EnableFeignClients 3. 定义要调用的接口
      @FeignClient(name="product")
      public interface ProductClient {
        @GetMapping("/msg")
        String productMsg();
      }
      // 使用
      @Autowired
      private ProductClient productClient;
      String response = productClient.productMsg();
      ```

* 客户端负载均衡器Ribbon
  * RestTemplate、Feign、Zuul
  * 主要原理：
    * 服务发现：依据服务的名字，把该服务下的所有实例查找出来
    * 服务选择规则：依据服务规则，从多个服务中选择一个
    * 服务监听：检测失效的服务，做到高效剔除
  * 主要组件：ServerList、IRule、ServerListFilter
    * 通过ServerList获取所有的可用列表，再通过ServerListFilter过滤掉一部分地址，从剩下的地址中，通过IRule选择一个实例作为最终目标结果
    * IRule默认规则为轮询
      * 可在配置文件中修改规则
        * Order模块中调用Product模块
        * 在Order模块的配置文件中修改调用策略：`PRODUCT.ribbon.NFLoadBalancerRuleClassName=com.netflix.loadbalancer.RandomRule`
  * Feign
    * 声明式REST客户端（伪RPC，还是HTTP调用）
    * 采用了基于接口的注解

## 项目划分为多模块

* product-server
  * 所有业务逻辑
* product-client
  * 对外暴露的接口
* product-common
  * 公用的对象

```java
// 根项目终端上执行命令  -U强制刷新
mvn -Dmaven.test.skip=true -U clean install
// order 服务主类
@EnableFeignClients(basePackages = "com.imooc.product.client")
```

## 同步or异步

* 用户服务 - 可能调用 - 短信服务、积分服务等

  ![image-20190806004933616](/Users/dingyuanjie/Documents/study/github/woodyprogram/img/image-20190806004933616.png)

* 消息中间件RabbitMQ

  ![image-20190806005336563](/Users/dingyuanjie/Documents/study/github/woodyprogram/img/image-20190806005336563.png)

## 微服务和容器

* 天生一对
  * 从系统环境开始，自底至上打包应用，避免环境问题
  * 轻量级，对资源的有效隔离和管理
  * 可服用、版本化
* 微服务、Docket、Devops

## 统一配置中心

* 为什么需要

  * 不方便维护
  * 配置内容安全与权限
  * 更新配置项目需重启

* config-server

  ![image-20190806063930030](/Users/dingyuanjie/Documents/study/github/woodyprogram/img/image-20190806063930030.png)

  ```shell
  # application.yml
  # git上创建一个仓库 config-repo
  # 然后创建一个order.yml文件（该格式是【通用配置】，不管指定哪个环境，其都会加载并合并到那个环境一起返回），内容为
  spring:
    application:
      name: config
    cloud:
      config:
        server:
          git:
            uri: https://gitlab-demo.com/SpringCloud_Sell/config-repo.git
            username: lly835@163.com
            password: nKfm9JMUfWEh
            # basedir - 本地仓库
  #         basedir: /Users/admin/code/myProjects/java/imooc/SpringCloud_Sell/config/basedir
  eureka:
    client:
      service-url:
        defaultZone: http://localhost:8761/eureka/
  management:
    endpoints:
      web:
        expose: "*"
  env: 
  	test
  # 然后启动config-server，访问以下链接都会显示上述配置信息
  http://localhost:8080/order-a.yml 
  http://localhost:8080/order-a.properties
  http://localhost:8080/order-a.json
  # 访问规则
  /{name}-{profiles}.yml
  /{label}/{name}-{profiles}.yml
  # name 服务名
  # profiles 环境
  # label 分支
  # 新建 order-test.yml - http://localhost:8080/order-test.yml 
  # 新建 order-dev.yml - http://localhost:8080/order-dev.yml
  # 新建分支 release - http://localhost:8080/release/order-dev.yml
  
  # 指定不同端口，多启动几个实例注册到eureka，client端负载均衡访问
  
  # 【git上每个服务都有各自的配置文件】
  # 【可以配置公共配置文件】
  ```

* config-client

  ```yaml
  # bootstrap.yml 最先启动
  spring:
    application:
      name: order
    cloud:
      config:
        discovery:
          enabled: true
          service-id: CONFIG   # config-server 服务名
        profile: test
        
  # 测试获取
  @Value("${env}")
  private String env;
  ```

* SpringCloud Bus自动刷新配置

  ![image-20190806064046587](/Users/dingyuanjie/Documents/study/github/woodyprogram/img/image-20190806064046587.png)

  ```shell
  # config-server
  # 引入 spring-cloud-starter-bus-amqp依赖
  # 启动 config-server、rabbitmq
  # 访问 http://localhost:15672 查看会有个默认队列
  # 暴露 /bus-refresh
  	# application.yml
  	management:
    	endpoints:
      	web:
        	expose: "*"
  # 修改 git上配置文件内容，然后刷新:
  	curl -v -X POST "http://localhost:8080/actuator/bus-refresh"
  # 坑，client端需要在 使用 @Value地方的类上加@RefreshScope	才能获取更改后的内容
  # git上配置自动刷新，不用调用/bus-refresh接口
  WebHooks设置 
  	PUSH 
  	url为 http://可以访问本地的IP(natapp内网穿透)/monitor 
  	Content-Type:application/json
  
  # config-client (例如order)
  # 引入 spring-cloud-starter-bus-amqp依赖
  # 访问 http://localhost:15672 查看会有个默认队列
  @Data
  @Component
  @ConfigurationProperties("girl")
  @RefreshScope
  public class GirlConfig {
  	private String name;
  	private Integer age;
  }
  @Autowired
  pricate GirlConfig girlConfig;
  ```

## 异步和消息

* 异步

  * 客户端请求不会阻塞进程，服务端的响应可以是非及时的（HTTP也支持异步）

* 异步常见形态

  * 通知
  * 请求/异步响应
  * 消息

* MQ应用场景

  * 异步处理（注册时发短信、邮件、积分等异步业务）
  * 流量削峰（超过队列长度则返回错误提示）
  * 日志处理（Kafka）
  * 应用解耦（订单下单后，订单服务完成持久化处理，将消息写入消息队列，返回用户订单下单成功，商品服务来订阅这个下单消息，采用拉或推的方式获取订阅信息，商品服务根据下单信息进行商品库存处理，如果商品服务系统出现问题也不会影响商品下单，因为下单后订单服务将消息写入消息队列后就不再关心后续的操作，实现服务解耦）

* MQ使用

  ```java
  //1. 引入依赖 spring-boot-starter-amqp  有默认配置
  //2. config-server git上添加 rabbitmq配置
  spring:
  	rabbitmq:
  		host: localhost
  		port: 5672
      username: guest
      password: guest
  ```

  ```yaml
  # order 上 bootstrap.yml 
  spring:
    application:
      name: order
    cloud:
      config:
        discovery:
          enabled: true
          service-id: CONFIG
        profile: test
      stream:
        bindings:
          myMessage:
            group: order
            content-type: application/json
    zipkin:
      base-url: http://zipkin:9411/
      sender:
        type: web
    sleuth:
      sampler:
        probability: 1
  eureka:
    client:
      service-url:
        defaultZone: http://eureka1:8761/eureka/,http://eureka2:8762/eureka/
    instance:
      prefer-ip-address: true
  
  logging:
    level:
      org.springframework.cloud.openfeign: debug
  ```

  ```java
  // 接收mq消息
  @Slf4j
  @Component
  public class MqReceiver {
  
      //1. @RabbitListener(queues = "myQueue")  需先在15672的管理页面上添加 myQueue 队列
      //2. 自动创建队列 @RabbitListener(queuesToDeclare = @Queue("myQueue"))
      //3. 自动创建, Exchange和Queue绑定
      @RabbitListener(bindings = @QueueBinding(
              value = @Queue("myQueue"),
              exchange = @Exchange("myExchange")
      ))
      public void process(String message) {
          log.info("MqReceiver: {}", message);
      }
  
      /**
       * 数码供应商服务 接收消息
       * @param message
       */
      @RabbitListener(bindings = @QueueBinding(
              exchange = @Exchange("myOrder"),
              key = "computer",
              value = @Queue("computerOrder")
      ))
      public void processComputer(String message) {
          log.info("computer MqReceiver: {}", message);
      }
  
      /**
       * 水果供应商服务 接收消息
       * @param message
       */
      @RabbitListener(bindings = @QueueBinding(
              exchange = @Exchange("myOrder"),
              key = "fruit",
              value = @Queue("fruitOrder")
      ))
      public void processFruit(String message) {
          log.info("fruit MqReceiver: {}", message);
      }
  }
  ```

  ```java
  // 发送mq消息 测试
  @Component
  public class MqSenderTest extends OrderApplicationTests {
  
      @Autowired
      private AmqpTemplate amqpTemplate;
  
      @Test
      public void send() {
          amqpTemplate.convertAndSend("myQueue", "now " + new Date());
      }
  
      @Test
      public void sendOrder() {
          amqpTemplate.convertAndSend("myOrder", "computer", "now " + new Date());
      }
  }
  @RunWith(SpringRunner.class)
  @SpringBootTest
  public class OrderApplicationTests {
  
  	@Test
  	public void contextLoads() {
  	}
  
  }
  ```

* SpringCloud Strem

  ```java
  // Binder：rabbitmq、kafka
  // 1. spring-cloud-starter-stream-rabbitmq 引入依赖
  // 2. 配置文件中配置rabbitmq
  // 3. 使用
  ```

  ```java
  public interface StreamClient {
      String INPUT = "myMessage";
      String INPUT2 = "myMessage2";
  
      @Input(StreamClient.INPUT)
      SubscribableChannel input();
  
      @Output(StreamClient.INPUT2)
      MessageChannel output();
  
  //    @Input(StreamClient.INPUT2)
  //    SubscribableChannel input2();
  //
  //    @Output(StreamClient.INPUT2)
  //    MessageChannel output2();
  }
  ```

  ```java
  @Component
  @EnableBinding(StreamClient.class)
  @Slf4j
  public class StreamReceiver {
       // String
  //    @StreamListener(value = StreamClient.INPUT)
  //    public void process(Object message) {
  //        log.info("StreamReceiver: {}", message);
  //    }
  
      /**
       * 接收orderDTO对象 消息
       * @param message
       */
      @StreamListener(value = StreamClient.INPUT)
      @SendTo(StreamClient.INPUT2)        // 接收消息后返回消息
      public String process(OrderDTO message) {
          log.info("StreamReceiver: {}", message);
          return "received.";
      }
  
      // 接收返回的消息
      @StreamListener(value = StreamClient.INPUT2)
      public void process2(String message) {
          log.info("StreamReceiver2: {}", message);
      }
  }
  ```

  ```java
  // 发送端
  @RestController
  public class SendMessageController {
  
      @Autowired
      private StreamClient streamClient;
  
   			// String
  //    @GetMapping("/sendMessage")
  //    public void process() {
  //        String message = "now " + new Date();
  //        streamClient.output().send(MessageBuilder.withPayload(message).build());
  //    }
  
      /**
       * 发送 orderDTO对象
       */
      @GetMapping("/sendMessage")
      public void process() {
          OrderDTO orderDTO = new OrderDTO();
          orderDTO.setOrderId("123456");
          streamClient.output().send(MessageBuilder.withPayload(orderDTO).build());
      }
  }
  ```

  ```yaml
  # rabbitmq通过分组，防止消息被重复消费；消息堆积时在rabbitmq页面上显示消息的json字符串
  spring:
  	cloud:
      stream:
        bindings:
          myMessage:
            group: order          
            content-type: application/json
  ```

* 在商品和订单服务中使用MQ

  * 订单 <— 库存变化— 消息队列 <— 库存变化—  商品

    * 注意坑，一定是要在完成对整个购物车扣库存完成后再发送MQ消息，不能遍历的发送MQ消息，因为要防止抛出异常，数据库可以回滚，但是MQ消息不能

    ```java
    // product 发送消息到MQ
    @Autowired
    private AmqpTemplate amqpTemplate;
    //发送mq消息
    public void decreaseStock(List<DecreaseStockInput> decreaseStockInputList) {
      // 整个购物车扣库存完成
    	List<ProductInfo> productInfoList =decreaseStockProcess(decreaseStockInputList);
      //发送mq消息
      List<ProductInfoOutput> productInfoOutputList = productInfoList.stream().map(e -> {
        ProductInfoOutput output = new ProductInfoOutput();
        BeanUtils.copyProperties(e, output);
        return output;
      }).collect(Collectors.toList());
      amqpTemplate.convertAndSend("productInfo", JsonUtil.toJson(productInfoOutputList));
    }
    
    // order 接收MQ消息
    @Autowired
    private StringRedisTemplate stringRedisTemplate;
    @RabbitListener(queuesToDeclare = @Queue("productInfo"))
    public void process(String message){
      //message => ProductInfoOutput
    		List<ProductInfoOutput> productInfoOutputList = (List<ProductInfoOutput>)JsonUtil.fromJson(message,
    				new TypeReference<List<ProductInfoOutput>>() {});
    		log.info("从队列【{}】接收到消息：{}", "productInfo", productInfoOutputList);
      //存储到redis中   redis配置信息存入到git上
    		for (ProductInfoOutput productInfoOutput : productInfoOutputList) {
    			stringRedisTemplate.opsForValue().set(String.format(PRODUCT_STOCK_TEMPLATE, productInfoOutput.getProductId()),
    					String.valueOf(productInfoOutput.getProductStock()));
    		}
    }
    ```

  * 原始流程：
    1. 查询商品信息（调用商品服务）
    2. 计算总价（生成订单详情）
    3. 商品服务扣库存（调用商品服务）
    4. 订单入库（生成订单，可变成异步）

  * 异步扣库存分析
    * 订单服务和商品服务都变成异步（适合秒杀类的业务场景，如果业务量不大的情况下，并不需要这样改造-代价太大，前台逻辑需要改，很多细节需要重新考虑）（要根据业务场景考虑是否异步）
    * 订单创建完成后订单状态为排队中，然后发送【Order Created】MQ消息
    * 如果商品服务收到【Order Created】MQ消息后进行扣库存操作，不管扣除成功还是失败，都会返回结果【Decrease Stock】给MQ消息队列，订单服务订阅扣库存的MQ消息，如果扣除库存成功，则将订单的状态改为下单成功，否则改为已取消
    * 前提：
      * 可靠的消息投递（能够收到消息）
      * 用户体验的变化（异步可能导致下单结果稍微有延迟）

  ![image-20190806114257255](/Users/dingyuanjie/Documents/study/github/woodyprogram/img/image-20190806114257255.png)

  ![image-20190806114547595](/Users/dingyuanjie/Documents/study/github/woodyprogram/img/image-20190806114547595.png)

  ​	
  * 秒杀场景
    * 商品的单机、库存都存入redis
    * 分布式锁，读redis，减库存并将新值重新设置进redis
    * @Transactional（订单详情入库、订单主表入库）
    * 订单入库异常，手动回滚redis：try/catch后在redis中将库存加回去
  * 异步扣库存分析
    1. 库存在redis中保存
    2. 收到请求判断redis中库存是否充足，减掉redis中的库存
    3. 订单服务创建订单写入数据库，并发送消息
  * 数据一致性
    * Dubbo + Zookeeper
      * Zookeeper：CP
    * SpringCloud
      * Eureka：AP

## 服务网关和Zuul

* 要素
  * 稳定性，高可用
  * 性能、并发性
  * 安全性，扩展性

* 常用方案

  * Nginx + Lua
  * Kong，商业
  * Tyk，Go语言
  * SpringCloud Zuul：认证、鉴权、限流、动态路由、监控、弹性、 安全、负载均衡、协作单点压测、静态响应

* 特点

  * 路由 + 过滤器 = Zuul
  * 核心是一系列的过滤器

* Zuul的高可用

  * 多个Zuul节点注册到Eureka Server节点上
  * Nginx和Zuul “混搭”

* Zuul四种过滤器API

  * 前置-Pre：限流、鉴权、参数校验调整
  * 路由-Route
  * 后置-Post：统计、日志
  * 错误-Error

  ![image-20190806131914303](/Users/dingyuanjie/Documents/study/github/woodyprogram/img/image-20190806131914303.png)

  ```yaml
  # 1. 添加依赖
  # 2. 主类上 @EnableZuulProxy
  # 原始访问： http://localhost:9000/服务名/服务接口
  # 3. 配置路由
  # bootstrap.yml
  # 【配置动态路由】将下面配置加到 git 上，然后覆盖重写配置类ZuulProperties
  zuul:
  	# 全局设置，保留所有服务的cookie
  	sensitiveHeaders:
  	routes:
  		# /myProduct/product/list -> /product/product/list
  		myProduct:             # 可随意填写
  			path:/myProduct/**
  			serviceId: product
  			# 设置为空，保留单个服务的cookie
  			# sensitiveHeaders:
  		# 简洁写法
      product: /myProduct/**
      # 排除某些路由
      ignored-patterns:
      	- /**/product/list
  # 覆盖重写配置类ZuulProperties
  # 主类中添加
  @ConfigurationProperties("zuul")
  @RefreshScope
  public ZuulProperties zuulProperties() {
  	return new ZuulProperties();
  }
      	
  management:
  	security:
  		enabled: false
  # 启动时从控制台查看路由规则
  http://localhost:9000/application/routes
  ```

## Zuu l综合使用

![image-20190806134727431](/Users/dingyuanjie/Documents/study/github/woodyprogram/img/image-20190806134727431.png)

```java
// 验证请求需带token
@Component
public class TokenFilter extends ZuulFilter {
    @Override
    public String filterType() {
        return PRE_TYPE;
    }

    @Override
    public int filterOrder() {
        return PRE_DECORATION_FILTER_ORDER - 1;
    }

    @Override
    public boolean shouldFilter() {
        return true;
    }

    @Override
    public Object run() {
        RequestContext requestContext = RequestContext.getCurrentContext();
        HttpServletRequest request = requestContext.getRequest();

        //这里从url参数里获取, 也可以从cookie, header里获取
        String token = request.getParameter("token");
        if (StringUtils.isEmpty(token)) {
            requestContext.setSendZuulResponse(false);
            requestContext.setResponseStatusCode(HttpStatus.UNAUTHORIZED.value());
        }
        return null;
    }
}
```

```java
@Component
public class addResponseHeaderFilter extends ZuulFilter{
    @Override
    public String filterType() {
        return POST_TYPE;
    }

    @Override
    public int filterOrder() {
        return SEND_RESPONSE_FILTER_ORDER - 1;
    }

    @Override
    public boolean shouldFilter() {
        return true;
    }

    @Override
    public Object run() {
        RequestContext requestContext = RequestContext.getCurrentContext();
        HttpServletResponse response = requestContext.getResponse();
        response.setHeader("X-Foo", UUID.randomUUID().toString());
        return null;
    }
}
```

* Zuul限流

  * 时机：请求被转发之前调用

    ![image-20190806135348872](/Users/dingyuanjie/Documents/study/github/woodyprogram/img/image-20190806135348872.png)

    ```java
    // 限流拦截器，google已有实现
    @Component
    public class RateLimitFilter extends ZuulFilter{
      // 每秒钟放100个令牌
    	private static final RateLimiter RATE_LIMITER = RateLimiter.create(100);
    
    	@Override
    	public String filterType() {
    		return PRE_TYPE;
    	}
    
    	@Override
    	public int filterOrder() {
    		return SERVLET_DETECTION_FILTER_ORDER - 1;
    	}
      
    	@Override
    	public boolean shouldFilter() {
    		return true;
    	}
    
    	@Override
    	public Object run() {
        // 没有拿到令牌
    		if (!RATE_LIMITER.tryAcquire()) {
    			throw new RateLimitException();
    		}
    		return null;
    	}
    }
    
    public class RateLimitException extends RuntimeException {
    }
    ```

* Zuul权限校验

  * /order/create 只能买家访问（cookie里有openid）

  * /order/finish 只能卖家访问（cookie里有token，并且对应的redis中有值）

  * /product/list 都可访问

    ```java
    // 【建议filter中不要调用与数据库相关的服务，尽量操作redis】
    // api-gateway
    ```

    ```java
    // 卖家
    @Component
    public class AuthSellerFilter extends ZuulFilter {
    
        @Autowired
        private StringRedisTemplate stringRedisTemplate;
    
        @Override
        public String filterType() {
            return PRE_TYPE;
        }
    
        @Override
        public int filterOrder() {
            return PRE_DECORATION_FILTER_ORDER - 1;
        }
    
        @Override
        public boolean shouldFilter() {
            RequestContext requestContext = RequestContext.getCurrentContext();
            HttpServletRequest request = requestContext.getRequest();
    
            if("/order/order/finish".equals(request.getRequestURI())) {
                return true;
            }
            return false;
        }
    
        @Override
        public Object run() {
            RequestContext requestContext = RequestContext.getCurrentContext();
            HttpServletRequest request = requestContext.getRequest();
    
            /**
             * /order/finish 只能卖家访问（cookie里有token，并且对应的redis中有值）
             */
            Cookie cookie = CookieUtil.get(request, "token");
            if(cookie == null
                    || StringUtils.isEmpty(cookie.getValue())
                    || StringUtils.isEmpty(stringRedisTemplate.opsForValue().get(String.format(RedisConstant.TOKEN_TEMPLATE, cookie.getValue())))) {
                requestContext.setSendZuulResponse(false);
                requestContext.setResponseStatusCode(HttpStatus.UNAUTHORIZED.value());
            }
    
            return null;
        }
    }
    ```

    ```java
    // 买家
    @Component
    public class AuthBuyerFilter extends ZuulFilter {
    
        @Autowired
        private StringRedisTemplate stringRedisTemplate;
    
        @Override
        public String filterType() {
            return PRE_TYPE;
        }
    
        @Override
        public int filterOrder() {
            return PRE_DECORATION_FILTER_ORDER - 1;
        }
    
        @Override
        public boolean shouldFilter() {
            RequestContext requestContext = RequestContext.getCurrentContext();
            HttpServletRequest request = requestContext.getRequest();
    
            if("/order/order/create".equals(request.getRequestURI())) {
                return true;
            }
            return false;
        }
    
        @Override
        public Object run() {
            RequestContext requestContext = RequestContext.getCurrentContext();
            HttpServletRequest request = requestContext.getRequest();
    
            /**
             * /order/create 只能买家访问（cookie里有openid）
             */
            Cookie cookie = CookieUtil.get(request, "openid");
            if(cookie == null || StringUtils.isEmpty(cookie.getValue())) {
                requestContext.setSendZuulResponse(false);
                requestContext.setResponseStatusCode(HttpStatus.UNAUTHORIZED.value());
            }
    
            return null;
        }
    }
    ```

    ![image-20190806140615721](/Users/dingyuanjie/Documents/study/github/woodyprogram/img/image-20190806140615721.png)

    ![image-20190806140911545](/Users/dingyuanjie/Documents/study/github/woodyprogram/img/image-20190806140911545.png)

    ![image-20190806141007875](/Users/dingyuanjie/Documents/study/github/woodyprogram/img/image-20190806141007875.png)

    ```yaml
    # user服务  git上配置 数据库和redis配置
    # application.yml
    spring:
      application:
        name: user
      cloud:
        config:
          discovery:
            enabled: true
            service-id: CONFIG
          profile: dev
    eureka:
      client:
        service-url:
          defaultZone: http://localhost:8761/eureka/
    ```

    ```java
    // 买家登录
    //1. openid和数据库里的数据是否匹配
    //2. 判断角色
    //3. cookie里设置openid=abc
    public static void set(HttpServletResponse response,String name,String value,
                           int maxAge) {
      Cookie cookie = new Cookie(name, value);
      cookie.setPath("/");
      cookie.setMaxAge(maxAge);
      response.addCookie(cookie);
    }
    ```

    ```java
    // 卖家登录
    // 判断是否已登录
    Cookie cookie = CookieUtil.get(request, CookieConstant.TOKEN);
    if (cookie != null &&   !StringUtils.isEmpty(stringRedisTemplate.opsForValue().get(String.format(RedisConstant.TOKEN_TEMPLATE, cookie.getValue())))) {
      return ResultVOUtil.success();
    }
    
    public static Cookie get(HttpServletRequest request, String name) {
      Cookie[] cookies = request.getCookies();
      if (cookies != null) {
        for (Cookie cookie: cookies) {
          if (name.equals(cookie.getName())) {
            return cookie;
          }
        }
      }
      return null;
    }
    // 1. openid和数据库里的数据是否匹配
    // 2. 判断角色
    // 3. redis设置key=UUID, value=xyz
    // 4. cookie里设置token=UUID
    ```

  * Zuul跨域

    * 在被调用的类或方法上增加@CrossOrigin注解

      * `@CrossOrigin(allowCredentials="true")`允许`cookie`跨域

    * 在Zuul里增加CorsFilter过滤器

      ```java
      /**
       * 跨域配置
       * C - Cross  O - Origin  R - Resource  S - Sharing
       */
      @Configuration
      public class CorsConfig {
      
      	@Bean
      	public CorsFilter corsFilter() {
      		final UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
      		final CorsConfiguration config = new CorsConfiguration();
      
      		config.setAllowCredentials(true);  // cookie跨域
      		config.setAllowedOrigins(Arrays.asList("*")); //http:www.a.com
      		config.setAllowedHeaders(Arrays.asList("*"));
      		config.setAllowedMethods(Arrays.asList("*"));
      		config.setMaxAge(300l);    // 在某个时间段内，相同的跨域请求不再拦截
      
      		source.registerCorsConfiguration("/**", config);
      		return new CorsFilter(source);
      	}
      }
      ```

## 服务容错





