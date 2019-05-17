# Spring Cloud Bus：消息总线

* 消息总线：系统中所有的微服务实例通过监听和消费轻量级消息代理构建的共用消息主题的消息
  * 总线上的各个实例都可以方便地广播一些需要让其他连接在该主题上的实例都知道的消息
  * 例如配置信息的变更或者其他一些管理操作等 
* 消息总线中的常用功能
  * 比如配合Spring Cloud Config 实现微服务应用配置信息的动态更新等

## 消息代理

* 消息代理 (Message Broker) 是一种消息验证、传输、路由的架构模式
  * 它在应用程序之间起到通信调度并最小化应用之间的依赖的作用，使得应用程序可以高效地解耦通信过程
* 消息代理是一个中间件产品，它的核心是一个消息的路由程序，用来实现接收和分发消息，并根据设定好的消息处理流来转发给正确的应用
  * 它包括独立的通信和消息传递协议， 能够实现组织内部和组织间的网络通信
  * 设计代理的目的就是为了能够从应用程序中传入消息， 并执行一些特别的操作
* 使用消息代理的场景：
  * 将消息路由到一个或多个目的地
  * 消息转化为其他的表现方式 
  * 执行消息的聚集、 消息的分解， 并将结果发送到它们的目的地， 然后重新组合响应返回给消息用户 
  * 调用Web服务来检索数据 
  * 响应事件或错误 
  * 使用发布-订阅模式来提供内容或基于主题的消息路由 
* Kafka 、RabbitMQ消息中间件与Spring Cloud Bus 配合实现消息总线

## RabbitMQ实现消息总线

* RabbitMQ是实现了高级消息队列协议（AMQP）的开源消息代理软件，也称为面向消息的中间件 
* 在SpringCloudBus中包含了对Rabbit的自动化默认配置

## 使用

* 安装web页面管理插件，`web页面`：`http://localhost:15672`，默认guest/guest

  * Tags 标签是 RabbitMQ 中的角色分类

* `api端口`：`5672`

* `rabbitmq-hello`：`amqp`依赖

* 主类不需要加额外注解

  ```properties
  # application.properties
  spring.application.name=rabbitmq-hello
  
  spring.rabbitmq.host=192.168.55.121
  spring.rabbitmq.port=5672
  spring.rabbitmq.username=vagrant
  spring.rabbitmq.password=vagrant
  ```

  ```java
  // 消息生产者
  @Component
  public class Sender {
  		//AmqpTemplate接口定义了一套针对AMQP协议的基础操作。 在SpringBoot中会根据配置来注入其具体实现。
      @Autowired
      private AmqpTemplate amqpTemplate;
  
      public void send() {
          String str = "Hello " + new Date();
          System.out.println("Sender:" + str);
        	//产生一个字符串，并发送到名为hello的队列中
          this.amqpTemplate.convertAndSend("hello", str);
      }
  }
  ```

  ```java
  //消息接收者
  @Component
  //通过@RabbitListener注解定义该类对hello队列的监听
  @RabbitListener(queues = "hello")
  public class Receiver {
  		//用@RabbitHandler注解来指定对消息的处理方法
      @RabbitHandler
      public void process(String hello) {
          System.out.println("Receiver：" + hello);
      }
  }
  ```

  ```java
  //RabbitMQ 的配置类 RabbitConfig，用来配置队列、交换器、路由等高级信息
  @Configuration
  public class RabbitConfig {
      @Bean
      public Queue helloQueue() {
          return new Queue("hello");
      }
  }
  ```

  ```java
  //单元测试类， 用来调用消息生产
  @RunWith(SpringRunner.class)
  @SpringBootTest
  public class RabbitHelloApplicationTests {
  
  	@Autowired
  	private Sender sender;
  
  	@Test
  	public void contextLoads() {
  		sender.send();
  	}
  }
  ```

* 在整个生产消费过程中，生产和消费是一个异步操作，以此可以使用通信来解耦业务逻辑

## 整合Spring Cloud Bus

* 通过使用Spring Cloud Bus与Spring Cloud Config的整合， 并以RabbitMQ作为消息代理， 实现了应用配置的动态更新 
* 微服务应用的实例中都引入了Spring Cloud Bus, 所以它们都连接到了RabbitMQ的消息总线上 

* 扩展config-client应用

  * 增加`bus-amqp`、`actuator`模块

  * `application.properties`中增加`RabbitMQ`的连接和用户信息

    ```properties
    spring.rabbitmq.host=192.168.55.121
    spring.rabbitmq.port=5672
    spring.rabbitmq.username=vagrant
    spring.rabbitmq.password=vagrant
    ```

* 启动 `config-server`

* 再启动两个config-client（分别在不同的端口上，比如7002、7003）

* 先访问两个 config-client-eureka 的/from 请求

* 修改git上配置文件中的from属性值

* 发送PSOT请求到其中的一个`config-client`的/bus/refresh

* 再访问两个 config-client-eureka 的/from 请求

* 向"Service A" 的实例3发送POST请求，访问/bus/refresh接口。此时，"Service A"的实例3就会将刷新请求发送到消息总线中，该消息事件会被"Service A"的实例l和实例2从总线中获取到，并重新从ConfigServer中获取它们的配置信息，从而实现配置信息的动态更新 

* 而从 Git仓库中配置的修改到发 起 /bus/refresh的POST请求这 一 步可以通过Git仓库的Web Hook来自动触发。 由于所有连接到消息总线上的应用都会接收到更新请求，所以在Web Hook中就不需要维护所有节点内容来进行更新，从而解决仅通过Web Hook来逐个进行刷新的问题

* 指定刷新范围

  * 刷新微服务中某个具体实例的配置
    * `/bus/refresh`接口提供了一个`destination`参数， 用来定位具体要刷新的应用程序
    * 可请求`/bus/refresh?destination= customers:9000`，此时总线上的各应用实例会根据`destination`属性的值来判断是否为自己的实例名，若符合才进行配置刷新，若不符合就忽略该消息 
  * 用来定位具体的服务
    * 定位服务的原理是通过使用 Spring的PathMatecher（路径匹配)来实现的，比如/bus/refresh?destination= customers:**, 该请求会触发 customers服务的所有实例进行刷新 

* 架构优化

  * 服务的配置更新需要通过向具体服务中的某个实例发送请求， 再触发对整个服务集群的配置更新
  * 结果是， 指定的应用实例会不同于集群中的其他应用实例， 这样会增加集群内部的复杂度， 不利于将来的运维工作。 比如， 需要对服务实例进行迁移， 那么 我们不得不修改Web Hook中的配置等。 所以要尽可能地让服务集群中的各个节点是对等的 

  * 改动
    * ConfigServer中也引入Spring Cloud Bus，将配置服务端也加入到消息总线中来
    * /bus/refresh请求不再发送到具体服务实例上， 而是发送给Config Server, 并通过destination参数来指定需要更新配置的服务或实例
    * 服务实例不需要再承担触发配置更新的职责。同时，对于Git的触发等配置都只需要针对ConfigServer即可， 从而简化了集群上的一些维护工作

* RabbitMQ配置

  * Spring Cloud Bus中的RabbitMQ整合使用了Spring Boot的ConnectionFactory，所以在SpringCloud Bus中支持使用以spring.rabbitmq为前缀的Spring Boot配置属性 

## Kafka实现消息总线

* Kafka是一个分布式消息订阅和发布系统 

## 事件驱动模型

* Spring 的事件驱动模型中包含了三个基本概念: 事件、 事件监听者和事件发布者
* 事件：
  * Spring中定义了事件的抽象类ApplicationEvent， 它继承自JDK的Event Object类 
  * 包含了两个成员变量：timestamp，用于存储事件发生的时间戳， 以及父类中的source , 表示源事件对象
  * 当需要自定义事件的时候，只需要继承ApplicationEvent
    * 比如RernoteApplicationEvnet、 RefreshRemoteApplicationEvent等，可以在自定义的Event中增加一些事件的属性来给事件监听者处理 
* 事件监听者：
  * Spring中定义了事件监听者的接口ApplicationListener, 它继承自 JDK 的EventListener接口， 同时ApplicationListener接口限定了ApplicationEvent子类作为该接口中onApplicationEvent(E event); 函数 的参数 
  * 所以，每一个ApplicationListener 都是针对某个ApplicationEvent子类的监听和处理者
* 事件发布者：
  *  Spring中定义了ApplicationEventPublisher和Application­EventMulticaster两个接口用来发布事件
  * ApplicationEventPublisher 接口定义了发布事件的函数publishEvent(ApplicationEvent event)和 publishEvent(Object event); 
    * ApplicationEventPublisher的publishEvent实现在AbstractApp让cation­Context中 
    * 它最终会调用 ApplicationEventMulticaster的multicastEvent来具体实现发布事件给监听者的操作
  * 而ApplicationEventMulticaster接口中定义了对ApplicationListener 的维护操作(比如新增、 移除等)以及将 ApplicationEvent多播给可用ApplicationListener的操作  
    * ApplicationEventMulticaster在Spring的默认实现位于SimpleApplicationEventMulticaster中，SimpleApplicationEventMulticaster 通过遍历维护的 ApplicationListener集合来找到对应 ApplicationEvent的监听器，然后调用监听器的 onApplicationEvent函数来对具体事件做出处理操作

## 总结

* Spring Cloud Bus 在绑定具体消息代理的输入与输出通道时均使用了抽象接口的方式， 所以真正的实现来自于spring-cloud-starter-bus-amqp 和 spring-cloud-starter-bus-kafka 的依赖
* spring-cloud-starter-bus-amqp依赖了spring-cloud-starter-stream­-rabbit 
*  spring-cloud-starter-bus-kafka依赖了spring-cloud-starter-stream-kafka
* 真正实现与这些消息代理进行交互操作的是Spring Cloud Stream。
  * 使用的所有Spring Cloud Bus的消息通信基础实际上都是由Spring Cloud Stream所提供的
  * 一定程度上，可以将Spring Cloud Bus理解为是一个使用了Spring Cloud Stream构建的上层应用
* 由于Spring Cloud Stream为了让开发者屏蔽各个消息代理之间的差异，将来能够方便地切换不同的消息代理而不影响业务程序，所以在业务程序与消息代理之间定义了一层抽象，称为绑定器 （Binder) 
  * 在整合RabbitMQ和Kafka的时候就是分别引入了它们各自的绑定器实现
  * 不论使用RabbitMQ还是Kafka实现，在程序上其实没有任何变化，变化的只是对绑定器的配置，所以，当要在其他消息代理上使用Spring Cloud Bus消息总线时， 只需要去实现一套指定消息代理的绑定器即可 

