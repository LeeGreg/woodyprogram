# MS

**1、创建队列和交换机的方法？**

​	1、代码中通过 channel接口创建，channel.queueDeclare() channel.exchangeDeclare() 

​	2、由Spring容器创建：配置文件，包括.xml和Java配置类

* 一般由消费者创建交换机和队列，生产者只需要知道交换机名称和Routing key就可以了
* 可以重复创建相同属性的交换机和队列

**2、多个消费者监听一个生产者（队列）时，消息如何分发？**

- Round-Robin(轮询)：默认的策略，消费者轮流、平均地收到消息。

- Fair dispatch (公平分发)

* 如果根据消费者的处理能力来分发消息，给空闲的消费者发送更多消息，可以用basicQos(int prefetch_count)来设置。
  * `prefetch_count`的含义：当消费者有多少条消息没有响应ACK时，不再给这个消费者发送消息。	

**3、手动ACK的情况下，prefetch默认是多少条?**

* 没有默认值。如果没有设置prefetch，队列默认会把所有消息都发给消费者，在消费者没有应答ACK的情况下，发了多少，就有多少Unacked 

* 如果prefetch是1，那么只要一条消息没有收到消费者的ACK，后续的消息都不会发送到这个消费者，造成消息堵塞。

**4、SpringBoot中如何开启自动ACK?各种模式的含义是什么?**

* ```properties
  # none默认，自动ack，方法未抛出异常则执行完毕后自动发送ack
  # manual,手动ack
  spring.rabbitmq.listener.direct.acknowledge-mode=none
  spring.rabbitmq.listener.simple.acknowledge-mode=none
  ```

* 如果消费者监听类使用了自定义容器，需要设置为自动ACK RabbitConfig类里面的

  ```java
  //SimpleRabbitListenerContainerFactory：
  factory.setAcknowledgeMode(AcknowledgeMode.NONE);
  ```

**5、SpringBoot中，bean还没有初始好，消费者就开始监听取消息了，导致空指针异常，怎么让消费者在容器启动完毕后才开始监听?**

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
- 系统复杂性增加：加入了消息队列，要多考虑很多方面的问题，比如：一致性问题、如何保证消息不被重复消费、如何保证消息可靠性传输等

**8、消息队列如何选型？**

- 看看该MQ的更新频率
- 中小型软件公司，建议选RabbitMQ.
  - RabbitMQ的社区十分活跃，可以解决开发过程中遇到的bug
  - 不考虑rocketmq和kafka的原因是，一方面中
  - 小型软件公司不如互联网公司，数据量没那么大，选消息中间件，应首选功能比较完备的，所以kafka排除。不考虑rocketmq的原因是，rocketmq是阿里出品，如果阿里放弃维护rocketmq，中小型公司一般抽不出人来进行rocketmq的定制化开发，因此不推荐
- 大型软件公司，根据具体使用在rocketMq和kafka之间二选一
  - 大型软件公司，具备足够的资金搭建分布式环境，也具备足够大的数据量
  - 针对rocketMQ,大型软件公司也可以抽出人手对rocketMQ进行定制化开发，毕竟国内有能力改JAVA源码的人，还是相当多的
  - 至于kafka，根据业务场景选择，如果有日志采集功能，肯定是首选kafka了
  - 具体该选哪个，看使用场景

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

* 是一个Erlang开发的基于AMQP（Advanced Message Queuing Protocol ）的开源消息队列中间件
* 使用Mnesia数据库存储消息

## AMQP协议

AMQP，应用层标准高级消息队列协议，是应用层协议的一个开放标准。基于此协议的客户端与消息中间件可传递消息，并不受客户端/中间件同产品、不同的开发语言等条件的限制

* 定义了以下这些特性 
  * 消息方向 、消息队列、消息路由（包括点到点和-发布订阅模式）、可靠性和安全性
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

- 可靠性，RabbitMQ 使用如持久化、传输确认、发布确认等机制来保证可靠性，

- 灵活的路由，消息通过Exchange 来路由到相应的队列，有直连、主题和广播交换机

- 消息集群，多个 RabbitMQ 服务器可以组成一个集群，形成一个逻辑 Broker 

- 高可用，队列可以在集群中的机器上进行镜像，即使部分节点出问题队列仍然可用 

- 多种协议，RabbitMQ 支持多种消息队列协议，比如 AMQP、STOMP、MQTT 等等

- 多语言客户端，RabbitMQ 几乎支持所有常用语言，比如 Java、.NET、Ruby、PHP、C#、JavaScript 等等

- 管理界面，RabbitMQ 提供了一个易用的用户界面可以用来监控和管理消息

- 插件机制，RabbitMQ提供了许多插件，以实现从多方面扩展，当然也可以编写自己的插件


## 工作模型

![image-20190105182648632](/Users/dingyuanjie/Desktop/MyKnowledge/2.code/java/2.%E5%92%95%E6%B3%A1%E5%AD%A6%E9%99%A2/02.%E5%88%86%E5%B8%83%E5%BC%8F%E4%B8%93%E9%A2%98/06.%E5%88%86%E5%B8%83%E5%BC%8F%E6%B6%88%E6%81%AF%E9%80%9A%E4%BF%A1/image-20190105182648632-6684008.png)

- Broker，RabbitMQ的实体服务器，维护一条从生产者到消费者的传输线路，保证消息数据能按照指定的方式传输。

- Message，消息，消息头可以设置routing-key、优先级、持久化、过期时间等属性，消息体则包含传输信息

- Exchange，消息交换机，指定消息按照什么规则路由到哪个队列Queue

- Queue，消息队列，消息的载体，每条消息都会被投送到一个或多个队列中

- Binding，绑定。作用就是将Exchange和Queue按照某种路由规则绑定起来

- Routing Key，路由关键字，Exchange根据routing Key将消息投递到相应队列

- Vhost，虚拟主机。一个Broker可以有多个虚拟主机，用作不同用户的权限分离。一个虚拟主机持有一组Exchange、Queue和Binding。

- Producer，消息生产者，主要将消息投递到对应的Exchange上

- Consumer，消息消费者。消息的接收者

- Connection，Producer 和 Consumer 与Broker之间的TCP长连接

- Channel，消息通道，在客户端的每个连接里可以建立多个Channel，每个Channel代表一个会话任务。在RabbitMQ Java Client API中，channel上定义了大量的编程接口

- 由RoutingKey、Exchange、Queue三个才能决定一个从Exchange到Queue的唯一的线路

## Direct Exchange直连交换机

* 发送消息到直连交换机时，只有routing key跟binding key完全匹配时，绑定的队列才能收到消息

```java
// 只有队列1能收到消息 
channel.basicPublish("MY_DIRECT_EXCHANGE", "key1", null, msg.getBytes());
```

![image-20190105182741347](/Users/dingyuanjie/Desktop/MyKnowledge/2.code/java/2.%E5%92%95%E6%B3%A1%E5%AD%A6%E9%99%A2/02.%E5%88%86%E5%B8%83%E5%BC%8F%E4%B8%93%E9%A2%98/06.%E5%88%86%E5%B8%83%E5%BC%8F%E6%B6%88%E6%81%AF%E9%80%9A%E4%BF%A1/image-20190105182741347-6684061.png)

## Topic Exchange主题交换机

* 发送消息到主题类型的交换机时，routing key符合binding key模式的绑定的队列才能收到消息 
  * 通配符有两个，*代表匹配一个单词。#代表匹配零个或者多个单词。单词与单词之间用 . 隔开。 

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

## 生产者

```java
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
public class MyProducer {
    private final static String QUEUE_NAME = "ORIGIN_QUEUE";
	public static void main(String[] args) throws Exception { 
    		ConnectionFactory factory = new ConnectionFactory(); 
				factory.setHost("127.0.0.1");
        factory.setPort(5672);
        factory.setVirtualHost("/"); 
        factory.setUsername("guest"); 
        factory.setPassword("guest");
				Connection conn = factory.newConnection(); 
        // 创建消息通道
				Channel channel = conn.createChannel();
				String msg = "Hello world, Rabbit MQ";
				// 声明队列
				// String queue, boolean durable, boolean exclusive, boolean autoDelete, Map<String, Object> arguments
        channel.queueDeclare(QUEUE_NAME, false, false, false, null);
		// 发送消息(发送到默认交换机AMQP Default，Direct)
		// 如果有一个队列名称跟Routing Key相等，那么消息会路由到这个队列
		// String exchange, String routingKey, BasicProperties props, byte[] body 
        channel.basicPublish("", QUEUE_NAME, null, msg.getBytes());
        channel.close();
        conn.close();
    }
}
```

## 消费者

```java
import com.rabbitmq.client.*;
import java.io.IOException;
public class MyConsumer {
    private final static String QUEUE_NAME = "ORIGIN_QUEUE";
	public static void main(String[] args) throws Exception { 
        ConnectionFactory factory = new ConnectionFactory(); 
				factory.setHost("127.0.0.1");
        factory.setPort(5672);
        factory.setVirtualHost("/"); 
        factory.setUsername("guest"); 
        factory.setPassword("guest");
				Connection conn = factory.newConnection(); 
				Channel channel = conn.createChannel();
				// 声明队列
        // String queue, boolean durable, boolean exclusive, boolean autoDelete, Map<String, Object> arguments
    		// durable，是否持久化，代表队列在服务器重启后是否还存在
    		// exclusive，是否排他性队列。排他性队列只能在声明它的Connection中使用，连接断开时自动删除
    		// autoDelete，是否自动删除，为true则至少有一个消费者连接到这个队列，后续都断开时队列自动删除
    		// arguments，队列的其他属性，例如x-message-ttl、x-expires（队列过期时间）、x-max-length、x-max-length-bytes、x-dead-letter-exchange、x-dead-letter-routing-key、x-max-priority
        channel.queueDeclare(QUEUE_NAME, false, false, false, null);
        System.out.println(" Waiting for message....");
				// 创建消费者
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
        ConnectionFactory factory = new ConnectionFactory();
        factory.setUsername("guest");
        factory.setPassword("guest");
        factory.setHost("localhost");
        Connection conn = factory.newConnection();
        Channel channel = conn.createChannel();
        String exchangeName = "hello-exchange";
      	// type：交换机的类型，direct, topic, fanout中的一种
      	// durable：是否持久化，代表交换机在服务器重启后是否还存在
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
        ConnectionFactory factory = new ConnectionFactory();
        factory.setUsername("guest");
        factory.setPassword("guest");
        factory.setHost("localhost");
        Connection conn = factory.newConnection();
        final Channel channel = conn.createChannel();
        String exchangeName = "hello-exchange";
        channel.exchangeDeclare(exchangeName, "direct", true);
        //声明队列
        String queueName = channel.queueDeclare().getQueue();
        String routingKey = "hola";
        //绑定队列，通过键 hola 将队列和交换器绑定起来
        channel.queueBind(queueName, exchangeName, routingKey);

        while(true) {
            //消费消息
            boolean autoAck = false;
            String consumerTag = "";
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
                    //确认消息
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

# 进阶知识

## 消息投递到队列过程

1. 客户端连接到消息队列服务器， 打开一 个Channel 
2. 客户端声明一个Exchange, 并设置相关属性
3. 客户端声明一个Queue, 并设置相关属性
4. 客户端使用Routing Key, 在Exchange和Queue之间建立好绑定关系
5. 客户端投递消息到Exchange
6. Exchange接收到消息后，根据消息的Key和已经设置的Binding,进行消息路由，将消息投递到一个或多个Queue里

## 怎么自动删除没人消费的消息

设置过期时间（TTL(Time To Live)

- 消息的过期时间

  1. `x-message-ttl`通过队列属性设置消息过期时间:

  2. `expiration`设置单条消息的过期时间:

- 队列的过期时间

  `x-expires`队列的过期时间决定了在没有任何消费者以后，队列可以存活多久

## 消息在什么时候会变成Dead Letter（死信）

* 有三种情况消息会进入DLX(Dead Letter Exchange)死信交换机。
  1. 消费者拒绝或者没有应答，并且没有让消息重新进入队列`(NACK || Reject ) && requeue == false`
  2. 消息过期（消息或队列的ttl设置）
  3. 队列达到最大长度(先入队的消息会被发送到DLX)

![image-20190105195816827](/Users/dingyuanjie/Desktop/MyKnowledge/2.code/java/2.%E5%92%95%E6%B3%A1%E5%AD%A6%E9%99%A2/02.%E5%88%86%E5%B8%83%E5%BC%8F%E4%B8%93%E9%A2%98/06.%E5%88%86%E5%B8%83%E5%BC%8F%E6%B6%88%E6%81%AF%E9%80%9A%E4%BF%A1/image-20190105195816827-6689496.png)

* 可以设置一个死信队列(Dead Letter Queue)与DLX绑定，即可以存储Dead Letter，消费者可以监听这个队列取走消息

```java
Map<String,Object> arguments = new HashMap<String,Object>(); 
arguments.put("x-dead-letter-exchange","DLX_EXCHANGE");
// 指定了这个队列的死信交换机
channel.queueDeclare("TEST_DLX_QUEUE", false, false, false, arguments);
// 声明死信交换机和死信队列，然后绑定
channel.exchangeDeclare("DLX_EXCHANGE","topic", false, false, false, null); 
channel.queueDeclare("DLX_QUEUE", false, false, false, null);
channel.queueBind("DLX_QUEUE","DLX_EXCHANGE","#");
```

## 可以让消息优先得到消费吗？

**优先级队列**

* 只有消息堆积(消息的发送速度大于消费者的消费速度)的情况下优先级才有意义

* 设置一个队列的最大优先级`x-max-priority:10`

* 发送消息时在队列优先级范围内指定消息当前的优先级`priority:5`

## 如何实现延迟发送消息

**延迟队列**

* RabbitMQ本身不支持延迟队列。但是可以设置一个队列A的死信交换机，然后死信交换机与另一个队列B绑定，  当队列A的消息过期后会通过死信交换机路由到队列B，然后可以从队列B消费消息

* 另一种方式是使用rabbitmq-delayed-message-exchange插件

* 当然，将需要发送的信息保存在数据库，使用任务调度系统扫描然后发送也是可以实现的

## MQ怎么实现RPC

* 客户端将消息发送到请求队列，服务端从请求队列中获取消息并将处理后的消息发送到响应队列，客户端再从响应队列中获取消息，通过设置消息的`correlationId`唯一ID属性来确定是同一条消息

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
        //创建一个回调队列
        channel.queueDeclare(RESPONSE_QUEUE_NAME,true,false,false,null);
    }

    // PRC 远程调用计算平方
    public String getSquare(String message) throws  Exception{
        //定义消息属性中的correlationId
        String correlationId = java.util.UUID.randomUUID().toString();
        //设置消息属性的replyTo和correlationId
        BasicProperties properties = new BasicProperties.Builder()
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
                response.offer(new String(body, "UTF-8"));
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
                //返回获取消息的平方
                String message = new String(body,"UTF-8");
                // 计算平方
                Double mSquare =  Math.pow(Integer.parseInt(message),2);
                String repMsg = String.valueOf(mSquare);
                // 把结果发送到回复队列
                channel.basicPublish("",replyQueue,replyProperties,repMsg.getBytes());
                //手动回应消息应答
                channel.basicAck(envelope.getDeliveryTag(),false);
            }
        };
        channel.basicConsume(REQUEST_QUEUE_NAME, true, consumer);
    }
}
```

## RabbitMQ流量控制怎么做？设置队列大小有用吗？

- 网关/接入层：其他限流方式
- 消费端:prefetch_count。

- **服务端流控(Flow Control)**
  - 配置文件中内存和磁盘的控制
  - 启动时检测机器的物理内存数值，默认占用40%以上时抛出内存警告并阻塞所有连接，可通过配置文件修改默认值
  - 默认情况下磁盘剩余空间在1GB以下，RabbitMQ主动阻塞所有生产者，阀值可调
  - 队列长度无法实现限流
    - 注意队列长度只在消息堆积的情况下有意义，而且会删除先入队的消息，不能实现服务端限流

- **消费端限流**

  - 通过用basicQos(int prefetch_count)来设置。

    - 在AutoACK为false的非自动确认消息的情况下，如果一定数目的消息(通过基于consumer或者channel设置Qos的值)未被确认前，不进行消费新的消息

    ```java
    channel.basicQos(2); // 如果超过2条消息没有发送ACK，当前消费者不再接受队列消息 
    channel.basicConsume(QUEUE_NAME, false, consumer);
    ```

* x-max-length = 80

1. 消息堆积的时候才有用
2. 先进入队列的120条消息删除了

# UI管理界面

* 默认端口是15672，默认用户guest，密码guest。guest用户默认只能在本机访问
* Linux/Windows启用管理插件命令
* 创建RabbitMQ用户并设置访问权限

# Spring配置方式集成RabbitMQ

* `amqp-client`、`spring-rabbit`依赖

![image-20190105183224229](/Users/dingyuanjie/Desktop/MyKnowledge/2.code/java/2.%E5%92%95%E6%B3%A1%E5%AD%A6%E9%99%A2/02.%E5%88%86%E5%B8%83%E5%BC%8F%E4%B8%93%E9%A2%98/06.%E5%88%86%E5%B8%83%E5%BC%8F%E6%B6%88%E6%81%AF%E9%80%9A%E4%BF%A1/image-20190105183224229-6684344.png)

# Spring Boot集成RabbitMQ

![image-20190105183256897](/Users/dingyuanjie/Desktop/MyKnowledge/2.code/java/2.%E5%92%95%E6%B3%A1%E5%AD%A6%E9%99%A2/02.%E5%88%86%E5%B8%83%E5%BC%8F%E4%B8%93%E9%A2%98/06.%E5%88%86%E5%B8%83%E5%BC%8F%E6%B6%88%E6%81%AF%E9%80%9A%E4%BF%A1/image-20190105183256897-6684376.png)

# 可靠性投递分析

* 效率与可靠性是无法兼得的

![image-20190105200805789](/Users/dingyuanjie/Desktop/MyKnowledge/2.code/java/2.%E5%92%95%E6%B3%A1%E5%AD%A6%E9%99%A2/02.%E5%88%86%E5%B8%83%E5%BC%8F%E4%B8%93%E9%A2%98/06.%E5%88%86%E5%B8%83%E5%BC%8F%E6%B6%88%E6%81%AF%E9%80%9A%E4%BF%A1/image-20190105200805789-6690085.png)

1 代表消息从生产者发送到Exchange;

2 代表消息从Exchange路由到Queue;

3 代表消息在Queue中存储;

4 代表消费者订阅Queue并消费消息。

## 1. 确保消息发送到RabbitMQ服务器

* 可能因为网络或者Broker的问题导致消息无法发送到交换机，而生产者无法得知消息是否正确发送到Broker

- 服务端确认——Transaction模式，发布消息前开启事务，消息发送成功则提交事务，否则捕获异常回滚事务

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
  
- 服务端确认——Confirm模式，消息投递到所匹配的队列后，RabbitMQ会发送一个确认给生产者

  ```java
//1. normal
  channel.confirmSelect();  // 将channel设置为confirm模式
  channel.basicPublish("", QUEUE_NAME, null, msg.getBytes());
  if (channel.waitForConfirms()) { // 普通Confirm，发送一条，确认一条
      System.out.println("消息发送成功" );
  }
  //2. AsyncConfirmProducer
  //3. BatchConfirmProducer
  ```

## 2. 确保消息路由到正确的队列

* 无法路由的消息去了哪里？
  * 如果没有任何设置，无法路由的消息会被直接丢弃

* 可能因为路由关键字错误、队列不存在、队列名称错误等导致消息无法路由到相应队列

  * 使用mandatory=true参数（false则消息被直接丢弃）配合ReturnListener，可以实现消息无法路由的时候返回给生产者
    * ` channel.addReturnListener(new ReturnListener(){...});`
    * `channel.basicPublish("","gupaodirect",true(mandatory), properties,"1".getBytes());`
  * 另一种方式是设置交换机的备份交换机(`alternate-exchange`)，无法路由的消息会发送到这个交换机上

## 3. 确保消息在队列正确地存储

* 可能因为系统宕机、重启、关闭等等情况导致存储在队列的消息丢失

* 解决方案：

1. 队列持久化，声明时durable设置为true

2. 交换机持久化，声明时durable设置为true

3. 消息持久化，deliveryMode属性设置为2

4. 集群，镜像队列

## 4. 确保消息从队列正确地投递到消费者

* 如果消费者收到消息后未来得及处理即发生异常，或者处理过程中发生异常
* 消费者订阅队列指定autoAck参数为false时`channel.basicConsume(QUEUE_NAME, false, consumer);`，RabbitMQ会等待消费者显示回复确认信号后才删除队列中信息`channel.basicAck(envelope.getDeliveryTag(), true);`
* 如果消息消费失败，则调用Basic.Reject或者Basic.Nack来拒绝当前消息而不是确认，如果requeue参数为true，则消息会重入队列以发送给下一个消费者

* 消费者确认：
  * `channel.basicAck(); `  手工应答
  * `channel.basicReject();`  单条拒绝
  * `channel.basicNack();`   批量拒绝  

* 自动ACK，消息发送给消费者以后就从队列中删除

## 5. 消费者回调

* 消费者处理消息以后，可以再发送一条消息给生产者，或者调用生产者的API，告知消息处理完毕。 


1）生产者提供一个回调的API——耦合

2）消费者可以发送响应消息

## 6. 补偿机制（消息的重发或确认）

ATM存款——5次确认，ATM取款——5次冲正；结合定时任务对账

* 对于一定时间没有得到响应的消息，可以设置一个定时重发的机制，但要控制次数，比如最多重发3次，否则会造成消息堆积

* 参考：ATM存款未得到应答时发送5次确认；ATM取款未得到应答时，发送5次冲正。根据业务表状态做一个重发

## 7. 消息幂等性

* 一次和多次请求某一个资源**对于资源本身**应该具有同样的结果

- 正常情况下，消息消费完毕后会发送一个确认消息给消息队列，消息队列就会将该消息删除
- 重复消费
  - 生产者重复发送消息到交换机，比如在开启了Confirm模式但未收到确认
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

![image-20190105203622226](/Users/dingyuanjie/Desktop/MyKnowledge/2.code/java/2.%E5%92%95%E6%B3%A1%E5%AD%A6%E9%99%A2/02.%E5%88%86%E5%B8%83%E5%BC%8F%E4%B8%93%E9%A2%98/06.%E5%88%86%E5%B8%83%E5%BC%8F%E6%B6%88%E6%81%AF%E9%80%9A%E4%BF%A1/image-20190105203622226-6691782.png)

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
  - 内存(RAM)：保存状态到内存(但持久化的队列和消息还是会保存到磁盘)。
  - 磁盘节点：保存状态到内存和磁盘。
    - 一个集群中至少需要需要一个磁盘节点。

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

