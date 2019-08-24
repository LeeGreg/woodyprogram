# Readme

```shell
# 注意坑，一定是要在完成对整个购物车扣库存完成后再发送MQ消息，不能遍历的发送MQ消息，因为要防止抛出异常，数据库可以回滚，但是MQ消息不能

# 启动eureka
cd /Users/dingyuanjie/code/SpringCloud_liao/liao_SpringCloud/SpringCloud_Sell/eureka
mvn clean package
nohup java -jar target/eureka-0.0.1-SNAPSHOT.jar > /dev/null 2>&1 &
# 【查看源码时，看类之间的关系/maven依赖关系】，maven依赖关系-查看【根据图，搜索相关依赖】以检查是否缺少相关依赖  启动项目时，控制台没有打印接口映射地址，可能是缺少web依赖
右键 - Diagrams - show Diagrams...
# 使用了@RequestBody注解后，需使用@PostMapping注解
# Post请求参数使用form形式
# 本地测试：启动一个服务多个实例时，直接在 Run/Debug Configurations中 VM options中 -Dserver.port=8081 指定启动参数
# 本地启动时，可查看控制台有哪些映射路径
# 版本先和例子里的一样，避免踩坑
# service【方法上】加上@Transactional
# 【要保证服务不去调用不是自己的数据库】
# 【幕客网 ajax跨域完全讲解】
# 【postman的runner可以设置自动调用接口】
# 【容器部署、版本更新】以后多看几遍
# 【zuul组件】超时 配置 - springcloud官方文档 - 搜 zuul - zuul timeout
	ribbon:
		ReadTimeout: 5000
		SocketTimeout: 5000
	management:
  	security:
  		enabled: false
# 调用接口返回500时，代码提示NPE，可能是超时导致的  		
# zuul、fegin超时设置  springcloud 官网 - 根据版本搜索相关超时配置进行设置
# 检测配置中心（git）上的配置文件格式是否正确：直接访问配置中心http://xxx:xx/order-test.yml，能正常显示配置则格式正确
# 用postman请求接口时，注意查看请求时间
# 设置 打印 feign 日志级别
	#全局搜索 FeignClient，选取其包名
	logging:
		level:
			org.springframework.cloud.bus: debug
			org.springframework.cloud.openfegin: debug
# zipkin 虽然引入了rabbitmq，但是还是想使用http方式发送追踪信息
	spring:
		zipkin:
			base-url: http://zipkin:9411/
			sender:
				type: web      # http方式
# gitlab 设置 配置自动刷新  webhooks
	# Settings - Integrations - url：natapp中配置的-127.0.0.1:8080

# 将源码clone 到与项目相同目录，在项目里点击依赖可以直接跳到源码文件上，则说明源码配置正确
# 可以修改源码并启动，然后启动项目、断点源码调试
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
  @EnableEurekaClient(只为Eureka) / @EnableDiscoveryClient(所有服务注册组件)
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
  * Fegin 调用 server中 controller
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
  * 可复用、版本化
* 微服务、Docket、Devops

## 统一配置中心

* 为什么需要

  * 不方便维护
  * 配置内容安全与权限
  * 更新配置项目需重启

* config-server

  ```yaml
  @EnableDiscoveryClient
  @EnableConfigServer
  # application.yml
  # git上创建一个仓库 config-repo，然后创建一个order.yml文件（该格式是【通用配置】，不管指定哪个环境，其都会加载并合并到那个环境一起返回），内容为
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
  #          basedir: /Users/admin/code/myProjects/java/imooc/SpringCloud_Sell/config/basedir
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
  #然后启动config-server，访问以下链接都会显示上述配置信息
  http://localhost:8080/order-a.yml 
  http://localhost:8080/order-a.properties
  http://localhost:8080/order-a.json
  #访问规则: label 分支，name 服务名，profiles 环境
  /{name}-{profiles}.yml
  /{label}/{name}-{profiles}.yml
  #新建 order-test.yml - http://localhost:8080/order-test.yml 
  #新建 order-dev.yml - http://localhost:8080/order-dev.yml
  #新建分支 release - http://localhost:8080/release/order-dev.yml
  #指定不同端口，多启动几个实例注册到eureka，client端负载均衡访问
  【git上每个服务都有各自的配置文件】
  【可以配置公共配置文件】
  ```

*  config-client

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
          // routingKey, object
          amqpTemplate.convertAndSend("myQueue", "now " + new Date());
      }
  
      @Test
      public void sendOrder() {
          // exchange, routingKey, object
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

  ```yaml
  # 也需设置 hystrix
  hystrix:
    command:
      default:  # 全局配置
        execution:
          isolation:
            thread:
              timeoutInMilliseconds: 3000
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
      	// com.netflix.zuul.context
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
    import com.google.common.util.concurrent.RateLimiter;
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
            // /order/finish 只能卖家访问（cookie里有token，并且对应的redis中有值）
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
            // /order/create 只能买家访问（cookie里有openid）
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

* 防止雪崩效应

  * SpringCloud Hystrix
    * 服务降级：优先核心服务，非核心服务不可用或弱可用
      * 通过HystrixCommand注解指定
      * fallbackMethod（回退函数）中具体实现降级逻辑
    * 服务熔断
    * 依赖隔离
      * 线程池隔离
      * Hystrix自动实现了依赖隔离
    * 监控

  ```java
  // 添加依赖
  // 主类上添加注解 @EnableCircuitBreaker
  // 类上可注解 
  //@RestController
  //@DefaultProperties(defaultFallback = "defaultFallback")  // 不指定fallback时的默认调用
  
  //超时配置
  //	@HystrixCommand(commandProperties = {
  			// HystrixCommandProperties中可以找到
  //		@HystrixProperty(name = "execution.isolation.thread.timeoutInMilliseconds", value = "3000")
  //	})
  
  // 服务熔断
  //	@HystrixCommand(commandProperties = {
  			// 设置熔断
  //			@HystrixProperty(name = "circuitBreaker.enabled", value = "true"),  	
  			// 请求数达到后才计算
  //			@HystrixProperty(name = "circuitBreaker.requestVolumeThreshold", value = "10"),	
  		  // 休眠时间窗
  //			@HystrixProperty(name = "circuitBreaker.sleepWindowInMilliseconds", value = "10000"), 
  			// 错误率
  //			@HystrixProperty(name = "circuitBreaker.errorThresholdPercentage", value = "60"),
  //	})
  @HystrixCommand
  @GetMapping("/getProductInfoList")
  public String getProductInfoList(@RequestParam("number") Integer number) {
    if (number % 2 == 0) {
      return "success";
    }
    // 调用目标服务
    RestTemplate restTemplate = new RestTemplate();
    return restTemplate.postForObject("http://127.0.0.1:8005/product/listForOrder",
                                      Arrays.asList("157875196366160022"), String.class);
    //		throw new RuntimeException("发送异常了");  // 抛出异常也可触发降级
  }
  
  private String fallback() { return "太拥挤了, 请稍后再试~~"; }
  private String defaultFallback() {  return "默认提示：太拥挤了, 请稍后再试~~"; }
  ```
  
* 微服务和分布式中容错是必须要考虑的
    * 重试机制：对于预期的短暂问题，重试是可以解决的，更长时间的故障问题则无意义
    * 断路器模式：将受保护的服务封装在一个可以监控故障的断路器对象里，当故障达到一定的值，短路器将会跳闸，断路器返回错误

![image-20190806160257813](/Users/dingyuanjie/Documents/study/github/woodyprogram/img/image-20190806160257813.png)

* Circuit Breaker：断路器
  
  ```java
    // 请求数达到后才计算，设置在滚动时间窗口中，断路器的最小请求数
    @HystrixProperty(name = "circuitBreaker.requestVolumeThreshold", value = "10"),
    // 断路器确定是否要打开统计请求错误数据时会有个时间范围，该范围称为时间窗口，当断路器打开对主逻辑进行熔断后，Hystrix会启动一个休眠时间窗
    // 休眠时间窗，在这期间，降级逻辑成为临时的主逻辑，休眠期到期时会进入半开状态，释放一次请求到原来的主逻辑上，如果此次正常返回，则断路器将继续闭合，主逻辑恢复；如果此次返回异常，断路器将继续进入打开状态，休眠时间窗将继续计时
    @HystrixProperty(name = "circuitBreaker.sleepWindowInMilliseconds", value = "10000"),
    // 错误率，断路器打开的错误百分比条件，在滚动时间窗口中，10次调用有7次异常，超过设置百分比，则断路器打开，否则设置关闭状态
    @HystrixProperty(name = "circuitBreaker.errorThresholdPercentage", value = "60"),	
  ```
  
  * 状态机状态：
      * Closed：调用失败次数累计到一定阈值或一定比例就会启动熔断机制
      * Open：此时服务直接返回错误，设置了一个时钟选项，到了时钟会进入半熔断状态允许定量服务请求，如果调用成功占到了一定比例，则认为服务恢复了，会关闭断路器，否则任务服务还没恢复，会回到断路器打开状态
      * Half Open
  
  ```yaml
    # 记得加 @HystrixCommand 注解
    # application.yml
    hystrix:
      command:
        default:  # 全局配置
          execution:
            isolation:
              thread:
                timeoutInMilliseconds: 3000
        getProductInfoList:  # 针对某个方法配置
          execution:
            isolation:
              thread:
                timeoutInMilliseconds: 1000
  ```
  
  * 可视化组件
  
    ```yaml
      # 添加依赖 spring-cloud-starter-hystrix-dashboard
      management:
      	context-path: /
      	
      http://localhost:8081/hystrix
      	http://localhost:8081/hystrix.stream
      	100 order
      【postman能够设置自动调用接口】	
    ```

## 服务追踪

* 链路监控

  * SpringCloudSleuth

  ```yaml
  # 引入依赖、启动ZipKin Server、配置参数
  <!--包含sleuth和zipkin-->
  <dependency>
  	<groupId>org.springframework.cloud</groupId>
  	<artifactId>spring-cloud-starter-zipkin</artifactId>
  </dependency>
  # 调整日志级别
  logging:
    level:
      org.springframework.cloud.openfeign: debug
  
  # 通过docker安装 zipkin
  docker run -d -p 9411:9411 openzipkin/zipkin  
  # 查看追踪
  http://localhost:9411/zipkin/   
  
  # 配置追踪
  spring:
  	zipkin:
      base-url: http://zipkin:9411/
      sender:
        type: web
    sleuth:
      sampler:
      	# percentage: 1      # 抽样百分比
        probability: 1    # 新版 抽样百分比
  ```

* 分布式追踪系统

  * 核心步骤

    * 数据采集、数据存储、查询展示

  * OpenTracing

    * 标准

    ![image-20190806215720539](/Users/dingyuanjie/Documents/study/github/woodyprogram/img/image-20190806215720539.png)

  * ZipKin

    * traceId、spanId、parentId

## 容器部署

* docker

```shell
docker run -p 8761:8761 -d hub.c.163.com/springcloud/eureka
docker run -p 18761:8761 -d hub.c.163.com/springcloud/eureka

# eureka项目
Dockerfile
FROM hub.c.163.com/library/java:8-alpine
MAINTAINER XXX XXX@imooc.com
ADD target/*.jar app.jar
EXPOSE 8761
ENTRYPOINT ["java", "-jar", "/app.jar"]

mvn clean package -Dmaven.skip.test=true 
docker build -t springcloud2/eureka
docker images|grep eureka
docker run -p 8762:8761 -d springcloud2/eureka
```

* rancher

```shell
# http://www.cnrancher.com  【快速入门】
# 更方便的管理docker
# 需要下载
centos7.4.ova，到virtual box中，命名为rancher-server
# 设置网络 - 连接方式：桥接网卡 - wifi
# 虚拟机和本地可以互相ping通
# 虚拟机 ssh root@ip
# 1. 先装docker 
yum install docker
systemctl start docker
docker verion
# 2. 安装racher
sudu docker run -d --restart=unless-stopped -p 8080:8080 rancher/server:stable
# 虚拟机中启动镜像加速器
	vim /etc/docker/daemon.json
		{
			"registry-mirrors":["https://fy707np5.mirror.aliyuncs.com"]
		}
	systemctl daemon-reload
  systemctl restart docker
# rancher启动后访问：http://虚拟机ip:8080

# 重启再倒入一个centos7.4.ova到virtual box中，命名为rancher-agent
ssh root@ip(agent)
yum install docker
systemctl start docker
# 【】第四步骤 填写rancher-agent的ip
# 复制第五步的命令到rancher-agent上执行

# 页面[基础架构-主机]  （http://虚拟机ip:8080）
```

* 部署

```shell
// eureka、config，都packge、Dockerfile，将镜像推送到仓库（网易云）,并设置为公开，否则拉去不下来
docker images|grep config
# docker tag 镜像id hub.c.163.com/网易云仓库用户名/镜像名(config)
docker images|grep config
docker push hub.c.163.com/网易云仓库用户名/镜像名(config)
```

```bash
# build.sh  脚本，直接执行bash build.sh

#!/usr/bin/env bash
mvn clean package -Dmaven.skip.test=true -U
docker build -t hub.c.163.com/springcloud/config .
docker push hub.c.163.com/springcloud/config
```

```shell
# 访问http://虚拟机ip:8080
添加应用：【应用-用户】：名称：SpringCloud，其他不用填
添加服务(对应java里的一个应用)（同样依照此步骤添加config）：
	名称：eureka
	勾选创建前总是拉取镜像
	选择镜像：hub.c.163.com/springcloud/eureka:latest
	端口映射：公有：8761 私有：8761
	# config的端口映射共有可不填（因为不需要通过主机去访问），私有填8080
	# 创建config服务后会显示主机ip、分配端口，如64641，访问：http://主机ip:64641/product-dev.yml测试
	# 
	
# 注意ip，如config中注册的eureka，使用 rancher中eureka的ip，直接用eureka的名称eureka即可，如
eureka:
  client:
    service-url:
      defaultZone: http://eureka:8761/eureka/
```

```shell
# eureka高可用,互相注册
application-eureka1.yml  # 8761  
application-eureka2.yml  # 8762
application.yml
# java -jar Dspring.profiles.active=eureka1 target/*.jar
# java -jar Dspring.profiles.active=eureka2 target/*.jar
```

## 版本更新



