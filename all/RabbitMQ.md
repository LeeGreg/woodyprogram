# MS

**1、创建队列和交换机的方法？**

​	1、代码中通过 channel接口创建，`channel.queueDeclare()` 、`channel.exchangeDeclare() `

​	2、由Spring容器创建：配置文件，包括`.xml`和`Java`配置类

* 一般由消费者创建交换机和队列，生产者只需要知道交换机名称和`Routing key`就可以了
* 可以重复创建相同属性的交换机和队列

**2、多个消费者监听一个生产者（队列）时，消息如何分发？**

- `Round-Robin`(轮询)：默认的策略，消费者轮流、平均地收到消息，不是每个消费者都收到所有消息

- `Fair dispatch` (公平分发)

  - 根据消费者的处理能力来分发消息，给空闲的消费者发送更多消息，用`basicQos(int prefetch_count)`来设置

  - 使用公平分发，必须关闭自动应答，改为手动应答

    ```java
    int prefetchCount = 1; // 0则没有上限
    //允许限制信道上的消费者所能保持的最大未确认消息的数量，否则不会向这个消费者再发送任何消息
    channel.basicQos(prefetchCount);
    ```

**3、手动ACK的情况下，prefetch默认是多少条?**

* 没有默认值。如果没有设置prefetch，队列默认会把所有消息都发给消费者，在消费者没有应答ACK的情况下，发了多少，就有多少Unacked 
* 如果prefetch是1，那么只要一条消息没有收到消费者的ACK，后续的消息都不会发送到这个消费者，造成消息堵塞
* Ready：待消费的消息总数
* Unacked：待应答的消息总数
* Total：总数 Ready+Unacked

**4、SpringBoot中如何开启自动ACK?各种模式的含义是什么?**

* ```properties
  # none默认，自动ack，方法未抛出异常则执行完毕后自动发送ack
  # manual,手动ack
  spring.rabbitmq.listener.direct.acknowledge-mode=none
  spring.rabbitmq.listener.simple.acknowledge-mode=none
  ```

  * 手动应答后，通过显示调用`channel.basicAck(envelope.getDeliveryTag(), false);`来告诉消息服务器来删除消息

**5、SpringBoot中，Bean还没有初始好，消费者就开始监听取消息了，导致空指针异常，怎么让消费者在容器启动完毕后才开始监听?**

* RabbitMQ中有一个`auto_startup`参数，可以控制是否在容器启动时就启动监听。
  
  * `spring.rabbitmq.listener.auto-startup=true 默认是true`
  
* 自定义容器，容器可以应用到消费者：
  
  * `factory.setAutoStartup(true); 默认true`
* 消费者单独设置([Spring AMQP 2.0以后的版本才有](https://github.com/spring-projects/spring-amqp/issues/669)):
  
* `@RabbitListener( queues = "${com.gupaoedu.thirdqueue}" ,autoStartup = "false")`
  
* 另外可以参考一下动态管理监听的方法:

  [浅谈spring-boot-rabbitmq动态管理的方法](https://www.jb51.net/article/131708.htm)
  [RabbitMQ异常监控及动态控制队列消费的解决方案](https://blog.csdn.net/u011424653/article/details/79824538)

**6、持久化的队列和非持久化的交换机可以绑定吗?**

* 可以（待确定）

**7、使用了消息队列会有什么缺点**

- 系统可用性降低：如果消息队列出故障，则系统可用性会降低
- 系统复杂性增加：加入了消息队列，要多考虑很多方面的问题
  - 比如：一致性问题、如何保证消息不被重复消费、如何保证消息可靠性传输等

**8、消息队列如何选型？**

- 看看该MQ的更新频率
- 中小型软件公司，建议选RabbitMQ.
  - RabbitMQ的社区十分活跃，可以解决开发过程中遇到的bug，且功能比较完备
- 大型软件公司，根据具体使用在rocketMq和kafka之间二选一
  - 具备足够的资金搭建分布式环境，也具备足够大的数据量
  - 至于kafka，根据业务场景选择，如果有日志采集功能，肯定是首选kafka了

[如何设计一个MQ服务?](http://www.xuxueli.com/xxl-mq/#/)

# 典型应用场景

- 跨系统的异步通信
- 应用内的同步变成异步
  - 应用解耦，串行任务并行化
  - 由于异步线程里的操作都是很耗时间的操作，也消耗系统资源
  - 比如注册时，可以将发送邮件、送积分等动作交给独立的子系统去处理，处理好之后向队列发送ACK确认
- 流量削峰
  - 控制队列长度，将请求写入队列，超过队列长度则返回失败，返回给用户一个提示信息。
- 日志处理 
  - 系统有大量的业务需要各种日志来保证后续的分析工作，而且实时性要求不高，可以用队列处理
- 系统间同步数据
  - ELT，将源数据库数据存入临时表进行转换，然后加载到目标库目标表中
  - 实时性比ETL高，因为ETL不可能一直在跑
  - 耦合性低，避免了一个应用直接去访问另一个应用的数据库，只需要约定接口字段即可。
- 广播
  - 基于Pub/Sub模型实现的事件驱动，一对多通信
- 利用RabbitMQ实现事务的最终一致性
  - 用消息确认机制来保证：只要消息发送，就能确保被消费者消费来做到了消息最终一致性

# 基本介绍

* 是一个Erlang开发的基于`AMQP`（`Advanced Message Queuing Protocol `）的开源消息队列中间件

## AMQP协议

* AMQP，应用层标准高级消息队列协议，是应用层协议的一个开放标准

* 基于此协议的客户端与消息中间件可传递消息，并不受客户端/中间件同产品、不同的开发语言等条件的限制

* 定义了以下这些特性 
  * 消息路由、消息方向 、消息队列（包括点到点和-发布订阅模式）、可靠性和安全性
* AMQP与JMS不同，JMS定义了一个API和一组消息收发必须实现的行为，而AMQP是一个线路级协议
  - 线路级协议描述的是通过网络发送的数据传输格式
  - 任何符合该数据格式的消息发送和接收工具都能互相兼容和进行操作，能轻易实现跨技木平台的架构方案

* AMQP的实现有：RabbitMQ、OpenAMQ

## 安装

* 开启/停止/重启`service rabbitmq-server start/stop/restart`
* 查看状态`rabbitmqctl status`
* 页面访问`http://192.168.55.122:15672/`
* 新建用户设置权限

# RabbitMQ的特性

- 可靠性，RabbitMQ 使用如发布确认、传输确认、持久化等机制来保证可靠性

- 灵活的路由，消息通过Exchange 来路由到相应的队列，有直连、主题和广播交换机

- 消息集群，多个 RabbitMQ 服务器可以组成一个集群，形成一个逻辑 Broker 

- 高可用，队列可以在集群中的机器上进行镜像，即使部分节点出问题队列仍然可用 

- 多种协议，RabbitMQ 支持多种消息队列协议，比如 AMQP、STOMP、MQTT 等等

- 多语言客户端，RabbitMQ 几乎支持所有常用语言，比如 Java、.NET、Ruby、PHP、C#、JavaScript 等等

- 管理界面，RabbitMQ 提供了一个易用的用户界面可以用来监控和管理消息

- 插件机制，RabbitMQ提供了许多插件，以实现从多方面扩展，当然也可以编写自己的插件


## 工作模型

![image-20190105182648632](/Users/dingyuanjie/Desktop/MyKnowledge/2.code/java/2.%E5%92%95%E6%B3%A1%E5%AD%A6%E9%99%A2/02.%E5%88%86%E5%B8%83%E5%BC%8F%E4%B8%93%E9%A2%98/06.%E5%88%86%E5%B8%83%E5%BC%8F%E6%B6%88%E6%81%AF%E9%80%9A%E4%BF%A1/image-20190105182648632-6684008.png)

- Producer，消息生产者，主要将消息投递到对应的Exchange上
- Message，消息，消息头可以设置routing-key、优先级、持久化、过期时间等属性，消息体则包含传输信息
- Broker，RabbitMQ的实体服务器，维护一条从生产者到消费者的传输线路，保证消息数据能按照指定的方式传输
- Vhost，虚拟主机。一个Broker可以有多个虚拟主机，用作不同用户的权限分离
- Exchange，消息交换机，指定消息按照什么规则路由到哪个队列Queue
- Binding，将Exchange和Queue按照某种路由规则绑定起来
- Queue，消息队列，消息的载体，每条消息都会被投送到一个或多个队列中
- Routing Key，指定消息的路由规则，与交换机类型和BindingKey联合使用，Exchange根据routing Key将消息投递到相应队列
- Consumer，消息消费者
- Connection，Producer 和 Consumer 与Broker之间的TCP长连接
  - Connection 可以用来创建多个 Channel 实例，但是 Channel 实例不能在线程问共享，应用程序应该为每一个线程开辟一个 Channel
  - 多线程间共享 Channel 实例是非线程安全的 
- Channel，消息通道，在客户端的每个连接里可以建立多个Channel，每个Channel代表一个会话任务。在RabbitMQ Java Client API中，Channel上定义了大量的编程接口
  - 每个线程把持一个信道，所以信道复用了 Connection 的 TCP 连接
- 由RoutingKey、Exchange、Queue三个才能决定一个从Exchange到Queue的唯一的线路
  - 生产者将消息发送给交换器时， 需要一个 RoutingKey， 当 BindingKey和 RoutingKey相匹配时， 消息会被路由到对应的队列中
  - 交换器相当于投递包裹的邮箱， RoutingKey相当于填写在包裹上的地址， BindingKey相当于包裹的目的地，目的地的"主人"-队列
  - 在某些情形下 ， RoutingKey 与 BindingKey 可以看作同一个东西
    - 在发送消息的时候，其中需要的路由键是 RoutingKey。涉及的客户端方法如channel .basicPublish
    - 在使用绑定的时候，其中需要的路由键是 BindingKey。涉及的客户端方法如: channel.exchangeBind、 channel .queueBind 

## Direct Exchange直连交换机

* 发送消息到直连交换机时，只有routing key跟binding key完全匹配时，绑定的队列才能收到消息

```java
// 只有队列1能收到消息 
channel.basicPublish("MY_DIRECT_EXCHANGE", "key1", null, msg.getBytes());
```

![image-20190105182741347](/Users/dingyuanjie/Desktop/MyKnowledge/2.code/java/2.%E5%92%95%E6%B3%A1%E5%AD%A6%E9%99%A2/02.%E5%88%86%E5%B8%83%E5%BC%8F%E4%B8%93%E9%A2%98/06.%E5%88%86%E5%B8%83%E5%BC%8F%E6%B6%88%E6%81%AF%E9%80%9A%E4%BF%A1/image-20190105182741347-6684061.png)

## Topic Exchange主题交换机

* 发送消息到主题类型的交换机时，routing key符合binding key模式的绑定的队列才能收到消息 
  * 通配符有两个，*代表匹配一个单词。#代表匹配零个或多个单词。单词与单词之间用 . 隔开。 

```java
// 只有队列1能收到消息
channel.basicPublish("MY_TOPIC_EXCHANGE", "sh.abc", null, msg.getBytes());
// 队列2和队列3能收到消息
channel.basicPublish("MY_TOPIC_EXCHANGE", "bj.book", null, msg.getBytes());
// 只有队列4能收到消息
channel.basicPublish("MY_TOPIC_EXCHANGE", "abc.def.food", null, msg.getBytes());
```

![image-20190105182836972](/Users/dingyuanjie/Desktop/MyKnowledge/2.code/java/2.%E5%92%95%E6%B3%A1%E5%AD%A6%E9%99%A2/02.%E5%88%86%E5%B8%83%E5%BC%8F%E4%B8%93%E9%A2%98/06.%E5%88%86%E5%B8%83%E5%BC%8F%E6%B6%88%E6%81%AF%E9%80%9A%E4%BF%A1/image-20190105182836972-6684116.png)

## Fanout Exchange 广播交换机

定义：广播交换机与一个队列绑定时，不需要指定binding key，所有与之绑定的队列都能收到消息

```java
// 3个队列都会收到消息
channel.basicPublish("MY_FANOUT_EXCHANGE", "", null, msg.getBytes());
```

![image-20190105182910724](/Users/dingyuanjie/Desktop/MyKnowledge/2.code/java/2.%E5%92%95%E6%B3%A1%E5%AD%A6%E9%99%A2/02.%E5%88%86%E5%B8%83%E5%BC%8F%E4%B8%93%E9%A2%98/06.%E5%88%86%E5%B8%83%E5%BC%8F%E6%B6%88%E6%81%AF%E9%80%9A%E4%BF%A1/image-20190105182910724-6684150.png)

# Java API编程

```java
// 连接RabbitMQ
ConnectionFactory factory = new ConnectionFactory(); 
factory.setHost("127.0.0.1");
factory.setPort(5672);
factory.setVirtualHost("/");   // 默认vhost
factory.setUsername("guest"); 
factory.setPassword("guest");
Connection conn = factory.newConnection(); 
//或者
ConnectionFactory factory = new ConnectionFactory();
factory.setUri("amqp://userName:password@ipAddress:portNumber/virtualHost"); 
Connection conn = factory.newConnection();
```

## 生产者

```java
public class MyProducer {
    private final static String QUEUE_NAME = "ORIGIN_QUEUE";
		public static void main(String[] args) throws Exception { 
    		// 连接RabbitMQ
				Channel channel = conn.createChannel();
				String msg = "Hello world, Rabbit MQ";
        channel.queueDeclare(QUEUE_NAME, false, false, false, null);
				// 发送消息(发送到默认交换机AMQP Default，Direct)
				// String exchange, String routingKey, BasicProperties props, byte[] body 
        channel.basicPublish("", QUEUE_NAME, null, msg.getBytes());
        channel.close();
        conn.close();
    }
}
```

## 消费者

```java
public class MyConsumer {
    private final static String QUEUE_NAME = "ORIGIN_QUEUE";
		public static void main(String[] args) throws Exception { 
        // 连接RabbitMQ
				Channel channel = conn.createChannel();
				// 声明队列
        // String queue, boolean durable, boolean exclusive, boolean autoDelete, Map<String, Object> arguments
    		// durable，是否持久化，代表队列在服务器重启后是否还存在
    		// exclusive，是否排他性队列。排他性队列只能在声明它的Connection中使用，连接断开时自动删除
    		// autoDelete，是否自动删除，为true则至少有一个消费者连接到这个队列，后续都断开时队列自动删除
    		// arguments，队列的其他属性，例如x-message-ttl、x-expires（队列过期时间）、x-max-length、x-max-length-bytes、x-dead-letter-exchange、x-dead-letter-routing-key、x-max-priority
        channel.queueDeclare(QUEUE_NAME, false, false, false, null);
        System.out.println(" Waiting for message....");
				// 创建消费者，不要使用QueueingConsumer
				Consumer consumer = new DefaultConsumer(channel) {
					@Override
            public void handleDelivery(String consumerTag, Envelope envelope,
AMQP.BasicProperties properties, byte[] body) throws IOException {
                String msg = new String(body, "UTF-8");
                System.out.println("Received message : '" + msg + "'");
            }
		};
		// 开始获取消息
		// String queue, boolean autoAck, Consumer callback 
        channel.basicConsume(QUEUE_NAME, true, consumer);
    } 
}
```

```java
public class Producer {
    public static void main(String[] args) throws IOException, TimeoutException {
        // 连接RabbitMQ
        Channel channel = conn.createChannel();
        String exchangeName = "hello-exchange";
      	// exchange : 交换器的名称
      	// type：交换机的类型，direct, topic, fanout中的一种
      	// durable：是否持久化，代表交换机在服务器重启后是否还存在
      	// autoDelete: 设置是否自动删除。自动删除的前提是至少有一个队列或者交换器与这个交换器绑定，之后所有与这个交换器绑定的队列或者交换器都与此解绑。注意不能错误地把这个参数理解为: "当与此交换器连接的客户端都断开时，RabbitMQ会自动删除本交换器 "
      	// internal: 设置是否是内置的，只能通过交换器路由到交换器这种方式。
      	// argument: 其他一些结构化参数，比如 alternate-exchange
        channel.exchangeDeclare(exchangeName, "direct", true);  
        String routingKey = "hola";
        //发布消息
        byte[] messageBodyBytes = "quit".getBytes();
      	// String exchange, String routingKey, BasicProperties props, byte[] body 
      	// 消息属性BasicProperties：headers-消息的其他自定义参数、deliveryMode-2持久化、priority-消息的优先级、correlationId-关联ID、replyTo-回调队列、expiration-TTL消息过期时间（毫秒）
        channel.basicPublish(exchangeName, routingKey, null, messageBodyBytes);
        channel.close();
        conn.close();
    }
}
```

```java
public class Consumer {
    public static void main(String[] args) throws IOException, TimeoutException {
        // 连接RabbitMQ
        final Channel channel = conn.createChannel();
        String exchangeName = "hello-exchange";
        channel.exchangeDeclare(exchangeName, "direct", true);
        String queueName = channel.queueDeclare().getQueue();
        String routingKey = "hola";
        //绑定队列，通过键 hola 将队列和交换器绑定起来
        channel.queueBind(queueName, exchangeName, routingKey);

        while(true) {
            //消费消息
            boolean autoAck = false;
            String consumerTag = "";
          	channel.basicQos(64);
          	//queue : 队列的名称
          	//autoAck: 设置是否自动确认
          	//consumerTag: 消费者标签，用来区分多个消费者
          	//noLocal: 设置为 true 则表示不能将同一个Connectio口中生产者发送的消息传送给这个 Connection中的消费者
          	//exclusive: 设置是否排他
          	//arguments : 设置消费者的其他参数
          	//callback: 设置消费者的回调函数。用来处理 RabbitMQ 推送过来的消息，比如DefaultConsumer， 使用时需要客户端重写 (override) 其中的方法
            channel.basicConsume(queueName, autoAck, consumerTag, new DefaultConsumer(channel) {
                @Override
                public void handleDelivery(String consumerTag,
                                           Envelope envelope,
                                           AMQP.BasicProperties properties,
                                           byte[] body) throws IOException {
                    String routingKey = envelope.getRoutingKey();
                    String contentType = properties.getContentType();
                    System.out.println("消费的路由键：" + routingKey);
                    System.out.println("消费的内容类型：" + contentType);
                    long deliveryTag = envelope.getDeliveryTag();
                    //配合autoAck=false、basicQos手动确认消息
                    channel.basicAck(deliveryTag, false);
                    System.out.println("消费的消息体内容：");
                    String bodyStr = new String(body, "UTF-8");
                    System.out.println(bodyStr);
                }
            });
        }
    }
}
```

```java
// 持久化的、非自动删除的、绑定类型为 direct 的交换器
channel.exchangeDeclare(exchangeName, "direct" , true); 
// 非持久化的、排他的、自动删除的队列(此队列的名称由 RabbitMQ 自动生成)
// 只对当前应用中同一个 Connection 层面可用，同一个 Connection 的不同 Channel 可共用，并且也会在应用连接断开时自动删除
String queueName = channel.queueDeclare().getQueue(); 
channel.queueBind(queueName, exchangeName, routingKey);
```

* 生产者和消费者都可以声明一个交换器或者队列。如果尝试声明一个已经存在的交换器或者队列，只要声明的参数完全匹配现存的交换器或者队列，RabbitMQ就可以什么都不做，并成功返回。如果声明的参数不匹配则会抛出异常
* 生产者和消费者都能够使用 queueDeclare 来声明一个队列，但是如果消费者在同一个信道上订阅了另一个队列，就无法再声明队列了。必须先取消订阅，然后将信道直为"传输"模式，之后才能声明队列。

```java
channel.exchangeDeclare("source", "direct", false, true, null) ; channel.exchangeDeclare("destination", "fanout", false, true, null); 
// 交换机绑定交换机，消息从一个交换机路由到另一个交换机
channel.exchangeBind( "destination " , "source " , "exKey"); 
channel.queueDeclare("queue", false, false, true, null); 
channel.queueBind("queue", "destination"， "");
channel.basicPublish( "source", "exKey", null , "exToExDemo". getBytes ()) ;
```

* 消费模式：

  * 推：通过持续订阅的方式来消费消息，接收消息一般通过实现 Consumer 接口或者继承 DefaultConsumer 类来实现 

  * 拉：通过 channel.basicGet 方法可以单条地获取消息，其返回值是 GetRespone

    ```java
    //GetResponse basicGet(String queue, boolean autoAck) throws IOException;
    GetResponse response = channel.basicGet(QUEUE NAME, false) ; 
    System.out.println(new String(response.getBody()));
    channel.basicAck(response.getEnvelope().getDeliveryTag() ,false);
    ```

  * Basic.Consume将信道 (Channel) 直为接收模式，直到取消队列的订阅为止。在接收模式期间，RabbitMQ会不断地推送消息给消费者，推送消息的个数还是会受到 Basic.Qos的限制
  * 如果只想从队列获得单条消息而不是持续订阅，建议还是使用 Basic.Get进行消费.但是不能将 Basic.Get 放在一个循环里来代替 Basic.Consume ，这样做会严重影响 RabbitMQ 的性能.如果要实现高吞吐量，消费者理应使用 Basic.Consume 方法 

# 进阶知识

* 生产者将消息发送给交换器，交换器和队列绑定 。当生产者发送消息时所携带的 RoutingKey与绑定时的 BindingKey相匹配时，消息即被存入相应的队列之中 。 消费者可以订阅相应的队列来获取消息

## 消息投递到队列过程

1. 客户端连接到RabbitMQ Broker， 打开一 个Channel 
2. 客户端声明一个Exchange, 并设置相关属性，如交换机类型、是否持久化
3. 客户端声明一个Queue, 并设置相关属性，如是否排它、是否持久化、是否自动删除等
4. 客户端使用Routing Key将Exchange和Queue绑定起来
5. 客户端发送消息到RabbitMQ Broker，其中包含Rounting Key，交换机等信息
6. 相应的交换器根据接收到的路由键查找相匹配的队列  
7. 如果找到 ，则将从 生产者发送过来的消息存入相应的队列中 
8. 如果没有找到 ，则根据生产者配置的属性选择丢弃还是回退给生产者 
9. 关闭信道、关闭连接

## 消费者接收消息的过程

1. 消费者连接到 RabbitMQ Broker，建立一个连接(Connection)，开启一个信道(Channel)
2. 消费者向 RabbitMQ Broker请求消费相应队列中的消息，可能会设置相应的回调函数
3. 消费者接收并确认(Ack)接收到的消息
4. RabbitMQ 从队列中删除相应己经被确认的消息
5. 关闭信道、关闭连接

## 怎么自动删除没人消费的消息

设置过期时间（TTL(Time To Live)

- 消息的过期时间

  1. `x-message-ttl`通过队列属性设置消息过期时间，队列中所有消息都有相同的过期时间
  2. `expiration`设置单条消息的过期时间:
- 在RabbitMQ 重启后 ，持久化的队列的过期时间会被重新计算。
- 队列的过期时间

  * `x-expires`队列的过期时间决定了在没有任何消费者以后，队列可以存活多久

## 消息在什么时候会变成Dead Letter（死信）

* 有三种情况消息会进入DLX(Dead Letter Exchange)死信交换机。
  1. 消费者拒绝或者没有应答，并且没有让消息重新进入队列`(NACK || Reject ) && requeue == false`
  2. 消息过期（消息或队列的ttl设置）
  3. 队列达到最大长度(先入队的消息会被发送到DLX)
     * 对队列中消息的条数进行限制  x-max-length
       * 处于 Unacked 状态的消息不纳入消息总数计算。但是，当 Unacked 消息被 reject 并重新入队时，就会受 x-max-length 参数限制，可能回不了队列
     * 对队列中消息的总量进行限制  x-max-length-bytes

* 可设置一个死信队列(Dead Letter Queue)与DLX绑定，即可以存储Dead Letter，消费者可以监听这个队列取走消息

```java
channel.exchangeDeclare("exchange.dlx", "direct", true); channel.exchangeDeclare("exchange.normal", "fanout", true); 
Map<String, Object> args = new HashMap<String, Object>(); 
args.put("x-message- ttl ", 10000);
args.put("x-dead-letter-exchange", "exchange .dlx");
args.put("x-dead-letter-routing-key", "routingkey");
channe1.queueDec1are("queue.norma1", true, fa1se, fa1se, args); 
channe1.queueBind("queue.normal" , "exchange.normal" ,""); 
channe1.queueDec1are("queue.d1x", true, false, false, null); 
channel.queueBind("queue.dlx", "exchange.dlx", "routingkey"); channel.basicPublish("exchange.normal", "rk",
MessageProperties.PERSISTENT_TEXT_PLAIN, "dlx".getBytes()) ;
```

## 可以让消息优先得到消费吗？

**优先级队列**

* 只有消息堆积(消息的发送速度大于消费者的消费速度)的情况下优先级才有意义

* 设置一个队列的最大优先级`x-max-priority:10`，发送消息时在队列优先级范围内指定消息当前的优先级`priority:5`


## 如何实现延迟发送消息

**延迟队列**

* `RabbitMQ`本身不支持延迟队列。但是可设置一个队列A的死信交换机，然后死信交换机与另一个队列B绑定，  当队列A的消息过期后会通过死信交换机路由到队列B，然后可以从队列B消费消息

* 另一种方式是使用`rabbitmq-delayed-message-exchange`插件

* 当然，将需要发送的信息保存在数据库，使用任务调度系统扫描然后发送也是可以实现的

## MQ怎么实现RPC

* RPC的协议有很多，比如 JavaRMI、WebService的RPC风格、Hessian、Thrift甚至还有 RestfulAPI

* 客户端将消息设置一个回调队列后发送到请求队列，服务端从请求队列中获取消息并将处理后的消息发送到回调队列，客户端再从回调队列中获取消息，通过设置消息的`correlationId`唯一ID属性来确定是同一条消息

![image-20190105200154824](/Users/dingyuanjie/Desktop/MyKnowledge/2.code/java/2.%E5%92%95%E6%B3%A1%E5%AD%A6%E9%99%A2/02.%E5%88%86%E5%B8%83%E5%BC%8F%E4%B8%93%E9%A2%98/06.%E5%88%86%E5%B8%83%E5%BC%8F%E6%B6%88%E6%81%AF%E9%80%9A%E4%BF%A1/image-20190105200154824-6689714.png)

```java
// RPC客户端，后启动
public class RPCClient{
    private final static String REQUEST_QUEUE_NAME="RPC_REQUEST";
    private final static String RESPONSE_QUEUE_NAME="RPC_RESPONSE";
    private Channel channel;
    private Consumer consumer;

    //构造函数 初始化连接
    public RPCClient() throws IOException, TimeoutException, NoSuchAlgorithmException, KeyManagementException, URISyntaxException {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setUri(ResourceUtil.getKey("rabbitmq.uri"));
        Connection connection = factory.newConnection();
        channel = connection.createChannel();
        channel.queueDeclare(REQUEST_QUEUE_NAME, true, false, false, null);
        //创建一个回调队列（为每个客户端创建一个单一的回调队列）
      	// 可以使用默认队列，String callbackQueueName = channel.queueDeclare().getQueue();
        channel.queueDeclare(RESPONSE_QUEUE_NAME,true,false,false,null);
    }

    // PRC 远程调用计算平方
    public String getSquare(String message) throws  Exception{
        //定义消息属性中的correlationId
        String correlationId = java.util.UUID.randomUUID().toString();
        BasicProperties properties = new BasicProperties.Builder()
          			// correlationld标记请求
          			// 用来关联请求 (request) 和其调用 RPC 之后的回复 (response)
                .correlationId(correlationId)
          			// 设置回调队列
                .replyTo(RESPONSE_QUEUE_NAME)
                .build();
        // 发送消息到请求队列rpc_request队列
        // 消息发送到与routingKey参数相同的队列中
        channel.basicPublish("",REQUEST_QUEUE_NAME, properties,message.getBytes());
        // 从匿名内部类中获取返回值
        final BlockingQueue<String> response = new ArrayBlockingQueue<String>(1);
        // 创建消费者
        consumer = new DefaultConsumer(channel) {
            @Override
            public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
              	// 如果回调队列接收到一条未知 correlationld 的回复消息 ， 可以简单地将其丢弃 
              	if(correlationId.equals(properties.getCorrelationId())) {
                  response.offer(new String(body, "UTF-8"));
                }
            }
        };
        // 开始获取消息
        // String queue, boolean autoAck, Consumer callback
        channel.basicConsume(RESPONSE_QUEUE_NAME, true, consumer);
        return response.take();
    }

    public static void main(String[] args) throws Exception {
        RPCClient rpcClient = new RPCClient();
        String result = rpcClient.getSquare("4");
        System.out.println("response is : " + result);
    }
}

```

```java
// RPC服务端，先启动
public class RPCServer {
    private final static String REQUEST_QUEUE_NAME="RPC_REQUEST";
    public static void main(String[] args) throws Exception{
        ConnectionFactory factory = new ConnectionFactory();
        factory.setUri(ResourceUtil.getKey("rabbitmq.uri"));
        Connection connection = factory.newConnection();
        final Channel channel = connection.createChannel();
        channel.queueDeclare(REQUEST_QUEUE_NAME, true, false, false, null);
        //设置prefetch值 一次处理1条数据
        channel.basicQos(1);
        Consumer consumer = new DefaultConsumer(channel) {
            @Override
            public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties,byte[] body) throws IOException {
                BasicProperties replyProperties = new BasicProperties.Builder()
                        .correlationId(properties.getCorrelationId())
                        .build();
                //获取客户端指定的回调队列名
                String replyQueue = properties.getReplyTo();
              	String repMsg = "";
              	try {
                  //返回获取消息的平方
                	String message = new String(body,"UTF-8");
                	// 计算平方
                	Double mSquare =  Math.pow(Integer.parseInt(message),2);
                	repMsg += mSquare;
                } catch(RuntimeException e) {
                  System.out.println(" [.] " + e.toString());
                } finally {
                  // 把结果发送到回复队列
                	channel.basicPublish("",replyQueue,replyProperties,repMsg.getBytes());
                	//手动回应消息应答
               		 channel.basicAck(envelope.getDeliveryTag(),false);
                }
            }
        };
        channel.basicConsume(REQUEST_QUEUE_NAME, false, consumer);
    }
}
```

## RabbitMQ流量控制怎么做？设置队列大小有用吗？

- 网关/接入层：其他限流方式
- 消费端：`prefetch_count`

- **服务端流控(Flow Control)**
  
  - 配置文件中内存和磁盘的控制
    - 启动时检测机器的物理内存数值，默认占用40%以上时抛出内存警告并阻塞所有连接，可通过配置文件修改默认值
    - 默认情况下磁盘剩余空间在`1GB`以下，`RabbitMQ`主动阻塞所有生产者，阀值可调
  - 队列长度无法实现限流
  - 注意队列长度只在消息堆积的情况下有意义，而且会删除先入队的消息，不能实现服务端限流
  
- **消费端限流**

  - 通过用`basicQos(int prefetch_count)`来设置

    - 在`AutoACK`为`false`的非自动确认消息的情况下，如果一定数目的消息(通过基于`consumer`或者`channel`设置`Qos`的值)未被确认前，不进行消费新的消息

    ```java
    channel.basicQos(2); // 如果超过2条消息没有发送ACK，当前消费者不再接受队列消息 
    channel.basicConsume(QUEUE_NAME, false, consumer);
    ```

* `x-max-length = 80`

1. 消息堆积的时候才有用
2. 先进入队列的120条消息删除了

# UI管理界面

* 默认端口是`15672`，默认用户`guest`，密码`guest`。`guest`用户默认只能在本机访问
* `Linux/Windows`启用管理插件命令
* 创建`RabbitMQ`用户并设置访问权限

# Spring配置方式集成RabbitMQ

* `amqp-client`、`spring-rabbit`依赖

![image-20190105183224229](/Users/dingyuanjie/Desktop/MyKnowledge/2.code/java/2.%E5%92%95%E6%B3%A1%E5%AD%A6%E9%99%A2/02.%E5%88%86%E5%B8%83%E5%BC%8F%E4%B8%93%E9%A2%98/06.%E5%88%86%E5%B8%83%E5%BC%8F%E6%B6%88%E6%81%AF%E9%80%9A%E4%BF%A1/image-20190105183224229-6684344.png)

# Spring Boot集成RabbitMQ

![image-20190105183256897](/Users/dingyuanjie/Desktop/MyKnowledge/2.code/java/2.%E5%92%95%E6%B3%A1%E5%AD%A6%E9%99%A2/02.%E5%88%86%E5%B8%83%E5%BC%8F%E4%B8%93%E9%A2%98/06.%E5%88%86%E5%B8%83%E5%BC%8F%E6%B6%88%E6%81%AF%E9%80%9A%E4%BF%A1/image-20190105183256897-6684376.png)

# 可靠性投递分析

* 效率与可靠性是无法兼得的

![image-20190105200805789](/Users/dingyuanjie/Desktop/MyKnowledge/2.code/java/2.%E5%92%95%E6%B3%A1%E5%AD%A6%E9%99%A2/02.%E5%88%86%E5%B8%83%E5%BC%8F%E4%B8%93%E9%A2%98/06.%E5%88%86%E5%B8%83%E5%BC%8F%E6%B6%88%E6%81%AF%E9%80%9A%E4%BF%A1/image-20190105200805789-6690085.png)

1 代表消息从生产者发送到`Exchange`;

2 代表消息从`Exchange`路由到`Queue`;

3 代表消息在`Queue`中存储;

4 代表消费者订阅`Queue`并消费消息。

## 1. 确保消息发送到RabbitMQ服务器

* 可能因为网络或者`Broker`的问题导致消息无法发送到交换机，而生产者无法得知消息是否正确发送到`Broker`

- 服务端确认—事务机制`Transaction`模式，发布消息前开启事务，消息发送成功则提交事务，否则捕获异常回滚事务

  ```java
try {
    	// 耗性能，不建议使用
      channel.txSelect();   // 将chanel设置为事务模式
      channel.basicPublish("", QUEUE_NAME, null, (msg).getBytes());
      channel.txCommit();   // 提交事务
      System.out.println("消息发送成功");
  } catch (Exception e) {
      channel.txRollback();  // 发布消息异常则回滚事务
      System.out.println("消息已经回滚");
  }
  ```
  
- 服务端确认—`Confirm`模式，消息投递到所匹配的队列后，RabbitMQ发送一个确认给生产者

  ```java
//1. normal
  channel.confirmSelect();  // 将channel设置为confirm模式
  channel.basicPublish("", QUEUE_NAME, null, msg.getBytes());
  if (channel.waitForConfirms()) { // 普通Confirm，发送一条，确认一条
      System.out.println("消息发送成功" );
  }
  
  //2. AsyncConfirmProducer（推荐）
  channel.confirmSelect();
  channel.addConfirmListener(new ConfirmListener () (
  	public void handleAck(long deliveryTag, boolean multiple) throws IOException (
  		System.out.println( "Nack, SeqNo : " + deliveryTag + ", multiple : " + multiple) ;
  		if (multiple) (
        confirmSet.headSet(deliveryTag - 1).clear();
      } else {
        confirmSet.remove(deliveryTag);
      }
  	}
    public void handleNack(long deliveryTag, boolean multiple) throws IOException{
      if(multiple) {
        confirmSet.headSet(delivery - 1).clear();
      } else {
        confirmSet.remove(deliveryTag);
      }
      // 处理消息重发场景
    }
  });
  // 一直发送消息
  while(true) {
    long nextSeqNo = channel.getNextPublishSeqNo();
    channel.basicPublish(ConfirmConfig.exchangeName, ConfirmConfig.routingKey, MessageProperties.PERSISTENT_TEXT_PLAIN, ConfirmConfig.msg_10B.getBytes());
    confirmSet.add(nextSeqNo);
  }
        
  //3. BatchConfirmProducer
  try{
    channel.confirmSelect();
    int MsgCount = 0;
    while(true) {
      channel.basicPublish("exchange","routingKey", null, "batch confirm test".getBytes());
      // /将发送出去 的消息存入缓存中，缓存可以是一个 ArrayList 或者 BlockingQueue 之类的
      if(++MsgCount >= BATCH_COUNT) {
        MsgCount = 0;
        try{
          if(channel.waitForConfirms()){
           //将缓存中的消息清空
          }
          //将缓存中的消息重新发送
        } catch (InterruptedException e) {
          	e.printStackTrace();
          //将缓存中 的消息 重新发送
        }
      }
    }
  }catch (IOException e) {
    e.printStackTrace() ;
  }
  ```

## 2. 确保消息路由到正确的队列

* 无法路由的消息去了哪里？
  
  * 如果没有任何设置，无法路由的消息会被直接丢弃
  
* 可能因为路由关键字错误、队列不存在、队列名称错误等导致消息无法路由到相应队列

  * mandatory 和 immediate 是 channel .basicPublish 方法中的两个参数，都有当消息传递过程中不可达目的地时将消息返回给生产者的功能（immediate在3.0版本中去掉了）
  
    * 使用`mandatory=true`（`false`则消息被直接丢弃）配合`ReturnListener`，可以实现消息无法路由的时候返回给生产者
    * ` channel.addReturnListener(new ReturnListener(){...});`
    * `channel.basicPublish("","gupaodirect",true(mandatory), properties,"1".getBytes());`
    
  * 另一种方式是设置交换机的备份交换机(`alternate-exchange`)，无法路由的消息会发送到这个交换机上
  
    ```java
    Map<String, Object> args = new HashMap<String, Object>(); 
    args.put("a1ternate-exchange", "myAe");
    channe1.exchangeDec1are("norma1Exchange", "direct", true, fa1se, args); 
    channe1.exchangeDec1are("myAe", "fanout", true, fa1se, nu11) ; channe1.queueDec1are("norma1Queue", true, fa1se, fa1se, nu11);
    channe1.queueBind("norma1Queue"，"norma1Exchange" , "norma1Key"); 
    channe1.queueDec1are("unroutedQueue", true, fa1se, fa1se, nu11);
    channel.queueBind("unroutedQueue", "myAe" , "") ;
    ```
  
  * 如果备份交换器和 mandatory 参数一起使用，那么 mandatory 参数无效。

## 3. 确保消息在队列正确地存储

* 可能因为系统宕机、重启、关闭等等情况导致存储在队列的消息丢失

* 解决方案：

1. 队列持久化，声明时`durable`设置为`true`，否则RabbitMQ服务器重启后数据会丢失
2. 交换机持久化，声明时`durable`设置为`true`，如果交换器不设置持久化，那么在 RabbitMQ 服务重启之后，相关的交换器元数据会丢失，不过消息不会丢失，只是不能将消息发送到这个交换器中
3. 消息持久化，`deliveryMode`属性设置为2，单单设置消息持久化而不设置队列的持久化毫无意义
4. 集群，镜像队列

* 数据丢失的其他场景
  * 订阅消费队列时将 autoAck 设置为 true，当消费者接收到相关消息之后，还没来得及处理就宕机了
    * autoAck 参数设置为 false， 并进行手动确认
  * 持久化的消息正确存入RabbitMQ之后，还需一段时间才能存入磁盘，消息保存还没来得及落盘就宕机了
    * 引入 RabbitMQ 的镜像队列机制相当于配置了副本，如果主节点 Cmaster) 在此特殊时间内挂掉，可以自动切换到从节点 Cslave )，样有效地保证了高可用性，除非整个集群都挂掉

## 4. 确保消息从队列正确地投递到消费者

* 消息确认机制
  * 消费者在订阅队列指定 autoAck=false时，RabbitMQ 会等待消费者显式地回复确认信号后才移除消息
    * `channel.basicConsume(QUEUE_NAME, false, consumer);`
    * `channel.basicAck(envelope.getDeliveryTag(), true);`
    * 如果 RabbitMQ 一直没有收到消费者的确认信号，并且消费此消息的消费者己经断开连接，则 RabbitMQ 会安排该消息重新进入队列，等待投递给下一个消费者，当然也有可能还是原来的那个消费者
    * RabbitMQ 不会为未确认的消息设置过期时间，它判断此消息是否需要重新投递给消费者的唯一依据是消费该消息的消费者连接是否己经断开。RabbitMQ 允许消费者消费一条消息的时间可以很久。
  * 当 autoAck = true 时， RabbitMQ 会自动把发送出去的消息置为确认，然后从内存(或者磁盘)中删除，而不管消费者是否真正地消费到了这些消息

* 如果消费者收到消息后未来得及处理即发生异常，或者处理过程中发生异常

* 如果消息消费失败，则调用`Basic.Reject`或`Basic.Nack`来拒绝当前消息而不是确认，如果`requeue`参数为`true`，则消息会重入队列以发送给下一个消费者

  ```java
  //消费者客户端可以调用与其对应的 channel.basicReject 方法来告诉 RabbitMQ 拒绝这个消息
  //单条拒绝：deliveryTag消息的编号;requeue为true则消息重入队列，可发给下一个订阅的消费者，false则删除
  void basicReject(long deliveryTag, boolean requeue) throws IOException;
  // 批量拒绝：multiple 参数设置为false则表示拒绝编号为deliveryTag的这一条消息，这时候basicNack和 basicReject 方法一样; multiple为true则表示拒绝 deliveryTag 编号之前所有未被当前消费者确认的消息。
  void basicNack(long deliveryTag, boolean multiple, boolean requeue) throws IOException
  ```

* channel.basicRecover 方法用来请求 RabbitMQ 重新发送还未被确认的消息

* 消费者确认：
  * `channel.basicAck(long deliveryTag, boolean multiple)` ;  手工应答
    * deliveryTag发布的每一条消息都会获得一个唯一的deliveryTag，(任何channel上发布的第一条消息的deliveryTag为1，此后的每一条消息都会加1)，deliveryTag在channel范围内是唯一的 
    * multiple批量确认标志。如果值为true，则执行批量确认，此deliveryTag之前收到的消息全部进行确认; 如果值为false，则只对当前收到的消息进行确认
* `channel.basicReject(long deliveryTag, boolean requeue);`  单条拒绝
  
  * `channel.basicNack(long deliveryTag, boolean multiple, boolean requeue);`   批量拒绝  

## 5. 消费者回调

* 消费者处理消息以后，可以再发送一条消息给生产者，或者调用生产者的API，告知消息处理完毕。 

1）生产者提供一个回调的API——耦合

2）消费者可以发送响应消息

## 6. 补偿机制（消息的重发或确认）

`ATM`存款——5次确认，`ATM`取款——5次冲正；结合定时任务对账

* 对于一定时间没有得到响应的消息，可以设置一个定时重发的机制，但要控制次数，比如最多重发3次，否则会造成消息堆积

* 参考：`ATM`存款未得到应答时发送5次确认；`ATM`取款未得到应答时，发送5次冲正。根据业务表状态做一个重发

## 7. 消息幂等性

* 一次和多次请求某一个资源**对于资源本身**应该具有同样的结果
* 消息传输保障分为三个层级
  * 最多一次，消息可能丢失但不会重复
  * 最少一次，消息不会丢失但可能重复传输
    * 消息生产者开启事务机制或者confirm 机制确保消息传递到RabbitMQ
    * 消息生产者需配合使用 mandatory 参数或者备份交换器来确保消息能够从交换器路由到队列中
    * 消息和队列都需要进行持久化处理
    * 消费者在消费消息的同时需要将 autoAck 设置为 false，然后通过手动确认的方式去确认己经正确消费的消息，以避免在消费端引起不必要的消息丢失 
  * 恰好一次，消息会传输且仅一次

- 正常情况下，消息消费完毕后会发送一个确认消息给消息队列，消息队列就会将该消息删除
- 重复消费
  - 生产者重复发送消息到交换机，比如在开启了`Confirm`模式但未收到确认
    - 消息体(比如json)中必须携带一个业务ID，消费者可以根据业务ID去重，避免重复消费
    - 可以对每一条消息生成一个唯一的业务ID-messageId，通过日志或者建表来做重复控制
  - 消费者消费消息后未发送ACK导致消息重复投递消费	
    - 如果消息做数据库的insert操作，给这个消息做一个唯一的主键
    - 消息做redis的set的操作，不用解决，set操作本来就算幂等操作
    - 准备一个第三方介质，来做消费记录
    - 以redis为例，给消息分配一个全局id，只要消费过该消息，将<id,message>以K-V形式写入redis.那消费者开始消费前，先去redis中查询有没有消费记录即可

## 8. 消息的顺序性

* 指消费者消费的顺序跟生产者产生消息的顺序是一致的。

* 在RabbitMQ中，一个队列有多个消费者时，由于不同的消费者消费消息的速度是不一样的，顺序无法保证。
* 一个队列只有一个消费者的情况下，才能保证顺序，否则只能通过全局ID来实现
  * 每条消息有一个msgId，关联的消息拥有同一个parentMsgId
  * 消费端未消费前一条消息时不处理下一条消息
  * 也可以在生产端实现前一条消息未处理完毕，不发布下一条消息

* 参考：消息:1、新增门店 2、绑定产品 3、激活门店，这种情况下消息消费顺序不能颠倒。

# 高可用架构部署方案

- Rabbit模式：
  - 单一模式：
    - 非集群模式
  - 集群模式：
    - 普通模式：默认的集群模式
      - 消息只存在于队列A中，当从队列B（与队列A有相同队列结构）消费该消息时会将消息从队列A取出并经过队列B发送给消费者。出口总在队列A，会产生瓶颈，并且队列A故障后无法消费其中剩余消息
    - [镜像模式](https://blog.csdn.net/u013256816/article/details/71097186)：
      - 把需要的队列做成镜像队列，存在于多个节点，属于RabbitMQ的HA方案
      - 消息实体会主动在镜像节点间同步
      - 节点间消息同步会占用网络带宽、降低系统性能，在对可靠性要求较高的场合中适用
- 节点分为两种：
  - 内存(RAM)：保存状态到内存(但持久化的队列和消息还是会保存到磁盘)
  - 磁盘节点：保存状态到内存和磁盘
    - 一个集群中至少需要需要一个磁盘节点
- Federation插件的设计目标是使 RabbitMQ在不同的 Broker节点之间进行消息传递而无须建立集群
- 与 Federation 具备的数据转发功能类似， Shovel 能够可靠、持续地从一个 Broker 中的队列(作为源端，即 source)拉取数据并转发至另一个 Broker中的交换器(作为目的端，即 destination)
- Federation/Shovel
  - 各个 Broker节点之间逻缉分离
  - 各个 Broker节点之间可以运行不同版本的 Erlang和 RabbitMQ
  - 各个 Broker节点之间可以在广域网中相连，当然必须要授予适当的用户和权限
  - 各个 Broker节点之间能以任何拓扑逻辑部署，连接可以是单向的或者是双向的
  - 从 CAP 理论中选择可用性和分区耐受性，即 AP
  - 一个 Broker中的交换器可以是 Federation生成的或者是本地的
  - 客户端所能看到它所连接的 Broker节点上的队列
- 集群
  - 逻辑上是个 Broker节点
  - 各个 Broker 节点之间必须运行相同版本的 Erlang 和 RabbitMQ 
  - 各个 Broker 节点之间必须在可信赖的局域网中相连 ， 通过 Erlang 内部节点传递消息，但节点问需要有相同的Erlang cookie
  - 所有 Broker节点都双向连续所有其他节点
  - 从 CAP 理论中选择致性和可用性 ， CA
  - 集群中所有 Broker 节点中的交换器都是一样的，要么全有要么全无
  - 客户端连接到集群中的任何 Broker 节点都可以看到所有的队列

## HAproxy负载+Keepalived高可用方案

![image-20190105201930802](/Users/dingyuanjie/Desktop/MyKnowledge/2.code/java/2.%E5%92%95%E6%B3%A1%E5%AD%A6%E9%99%A2/02.%E5%88%86%E5%B8%83%E5%BC%8F%E4%B8%93%E9%A2%98/06.%E5%88%86%E5%B8%83%E5%BC%8F%E6%B6%88%E6%81%AF%E9%80%9A%E4%BF%A1/image-20190105201930802-6690770.png)

## 网络分区

* 为什么会出现[分区](https://blog.csdn.net/u013256816/article/details/53588206)?
  * 因为RabbitMQ对网络延迟非常敏感，为了保证数据一致性和性能，在出现网络故障时，集群节点会出现分区。
  * [解决](https://blog.csdn.net/u013256816/article/details/73757884)
  * [模拟RabbitMQ网络分区](https://blog.csdn.net/u013256816/article/details/74998896)

## 广域网的同步方案

1. federation插件
2. shovel插件

# 实践经验总结

1、配置文件与命名规范 

* 集中放在properties文件中
* 体现元数据类型(_VHOST _EXCHANGE _QUEUE);
* 体现数据来源和去向(XXX_TO_XXX);

2、调用封装 

* 可以对Template做进一步封装，简化消息的发送。

3、信息落库（可追溯，可重发） + 定时任务（效率降低，占用磁盘空间）

* 将需要发送的消息保存在数据库中，可以实现消息的可追溯和重复控制，需要配合定时任务来实现。

4、如何减少连接数  4M

* 合并消息的发送，建议单条消息不要超过4M(4096KB)

5、生产者先发送消息还是先登记业务表？

* 先登记业务表，再发送消息

6、谁来创建对象（交换机、队列、绑定关系）？

* 消费者创建

7、运维监控  zabbix 

* [zabbix系列zabbix3.4监控rabbitmq](http://blog.51cto.com/yanconggod/2069376)

8、[其他插件](https://www.rabbitmq.com/plugins.html)

* tracing，`rabbitmq-plugins list`

