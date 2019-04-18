# MS

**1、消息队列的作用与使用场景？**

​	异步:批量数据异步处理。例:批量上传文件，比如代发代扣文件。

​	削峰:高负载任务负载均衡。例:电商秒杀抢购。

​	解耦:串行任务并行化。例:退货流程解耦。

​	广播:基于Pub/Sub实现一对多通信。

**2、创建队列和交换机的方法？**

​	1、代码中通过 channel接口创建，channel.queueDeclare() channel.exchangeDeclare() 

​	2、由Spring容器创建:配置文件，包括.xml和Java配置类

​	3、Web管理界面

​	4、命令行 

​	5、HTTP API 

​	可以重复创建相同属性的对象(交换机/队列)，会直接返回成功。 属性不同会报错，所以要修改属性，只能先删除。 一般由消费者创建这些对象，遵循谁使用谁申请的原则。 生产者只需要知道交换机名称和Routing key就可以了。 

**3、多个消费者监听一个生产者（队列）时，消息如何分发？**

* Round-Robin(轮询)

  默认的策略，消费者轮流、平均地收到消息。

* Fair dispatch (公平分发)

  如果要实现根据消费者的处理能力来分发消息，给空闲的消费者发送更多消息，可以用basicQos(int prefetch_count)来设置。prefetch_count的含义:当消费者有多少条消息没有响应ACK时，不再给这个消费者发送消息。

**4、无法被路由的消息，去了哪里？**

​	如果没有任何设置，无法路由的消息会被直接丢弃。

​	无法路由的情况:Routing key不正确。

​	解决方案：

* 使用 mandatory=true 配合 ReturnListener，实现消息回发。
* 声明交换机时，指定备份交换机。

**5、消息在什么时候会变成Dead Letter（死信）？**

* 消息被拒绝并且没有设置重新入队:(NACK || Reject ) && requeue == false

* 消息过期(消息或者队列的TTL设置)

* 消息堆积，并且队列达到最大长度，先入队的消息会变成DL。

  可以在声明队列时，指定一个Dead Letter Exchange，来实现Dead Letter的转发。

**6、RabbitMQ如何实现延迟队列？**

* 利用TTL(队列的消息存活时间或消息存活时间)，加上死信交换机。
* 当然还有一种方式就是先保存消息到数据库，用调度器扫描发送(时间不够精准)。

**7、如何保证消息的可靠性投递？**

![image-20190105211154865](/Users/dingyuanjie/Desktop/MyKnowledge/2.code/java/2.咕泡学院/02.分布式专题/06.分布式消息通信/image-20190105211154865-6693914.png)

 	1. 确保投递到服务端Broker
 	2. 保证正确地路由
 	3. 消息的持久化存储
 	4. 消费者应答ACK
 	5. 消费者回调
 	6. 补偿机制

**8、如何在服务端和消费端做限流？**

* 网关/接入层:其他限流方式
* 服务端(Broker):配置文件中内存和磁盘的控制;队列长度无法实现限流。
* 消费端:prefetch_count。

**9、如何保证消息的顺序性？**

​	比如新增门店、绑定产品、激活门店这种对消息顺序要求严格的场景。

​	一个队列只有一个消费者的情况下才能保证顺序。

​	否则只能通过全局ID来实现。

​	1、每条消息有一个msgId，关联的消息拥有同一个parentMsgId。

​	2、可以在消费端实现前一条消息未消费，不处理下一条消息;也可以在生产端实现前一条消息未处理完毕，不发布下一条消息。	

**10、RabbitMQ的集群节点类型？**

​	集群模式有两种:

* 普通模式:默认模式，以两个节点(rabbit01、rabbit02)为例来进行说明。对于Queue来说，消息实体只存在于其中一个节点rabbit01(或者rabbit02)，rabbit01和rabbit02两个节点仅有相同的元数据，即队列的结构。当消息进入rabbit01节点的Queue后，consumer从rabbit02节点消费时，RabbitMQ会临时在rabbit01、rabbit02间进行消息传输，把A中的消息实体取出并经过B发送给consumer。所以consumer应尽量连接每一个节点，从中取消息。即对于同一个逻辑队列，要在多个节点建立物理Queue。否则无论consumer连rabbit01或rabbit02，出口总在rabbit01，会产生瓶颈。当rabbit01节点故障后，rabbit02节点无法取到rabbit01节点中还未消费的消息实体。如果做了消息持久化，那么得等rabbit01节点恢复，然后才可被消费;如果没有持久化的话，就会产生消息丢失的现象。
* 镜像模式:把需要的队列做成镜像队列，存在与多个节点属于RabbitMQ的HA方案。该模式解决了普通模式中的问题，其实质和普通模式不同之处在于，消息实体会主动在镜像节点间同步，而不是在客户端取数据时临时拉取。该模式带来的副作用也很明显，除了降低系统性能外，如果镜像队列数量过多，加之大量的消息进入，集群内部的网络带宽将会被这种同步通讯大大消耗掉。所以在对可靠性要求较高的场合中适用。

节点分为两种：

* 内存(RAM):保存状态到内存(但持久化的队列和消息还是会保存到磁盘)。
* 磁盘节点:保存状态到内存和磁盘。
  一个集群中至少需要需要一个磁盘节点。

**11、消息幂等性**

​	首先，Broker本身没有消息重复过滤的机制。

​	1、生产者方面，可以对每条消息生成一个msgId，以此控制消息重复投递。

```java
 // 消息属性
AMQP.BasicProperties properties = new AMQP.BasicProperties.Builder()
        .messageId(String.valueOf(UUID.randomUUID()))
		.build(); // 发送消息
channel.basicPublish("", QUEUE_NAME, properties, msg.getBytes());
```

​	2、消费者方面，消息体(比如json报文)中必须携带一个业务ID，比如银行的交易流水号，消费者可以根据业务ID去重，避免重复消费

* 重复消费
  * 正常情况下，消费者在消费消息的时候，消费完毕后，会发送一个确认消息给消息队列，消息队列就知道该消息被消费了，就会将该消息从消息队列中删除
  * 因为网络传输等等故障，确认信息没有传送到消息队列，导致消息队列不知道自己已经消费过该消息了，再次将消息分发给其他的消费者
    * 如果消息做数据库的insert操作，给这个消息做一个唯一的主键
    * 消息做redis的set的操作，不用解决，set操作本来就算幂等操作
    * 准备一个第三方介质，来做消费记录。以redis为例，给消息分配一个全局id，只要消费过该消息，将<id,message>以K-V形式写入redis.那消费者开始消费前，先去redis中查询有没有消费记录即可

**12、MQ同步数据比ETL同步数据的优势在哪里?**

1. 实时性比ETL高，因为ETL不可能一直在跑
2. 耦合性低，避免了一个应用直接去访问另一个应用的数据库，只需要约定接口字段即可。

**13、SpringBoot中如何开启自动ACK?各种模式的含义是什么?**

```properties
# 在Spring Boot工程里面开启RabbitMQ消费者消息的自动确认:
spring.rabbitmq.listener.direct.acknowledge-mode=none
spring.rabbitmq.listener.simple.acknowledge-mode=none
```

如果消费者监听类使用了自定义容器(例如vip工程里面的demo)，需要设置为自动ACK RabbitConfig类里面的
SimpleRabbitListenerContainerFactory：

```java
factory.setAcknowledgeMode(AcknowledgeMode.NONE);
```

NONE：自动ACK

MANUAL：手动ACK

AUTO(默认方式，在AbstractMessageListenerContainer的构造函数里面指定了):如果方法未抛出异常，则执行完毕后发送ack。如果方法抛出异常，并且不是AmqpRejectAndDontRequeueException则发送nack，并且重新入队列。如果抛出异常时AmqpRejectAndDontRequeueException则发送nack不会重新入队列

**14、手动ACK的情况下，prefetch默认是多少条?**

没有默认值。如果没有设置prefetch，队列默认会把所有消息都发给消费者，在消费者没有应答ACK的情况下，发 了多少，就有多少Unacked 

```shell
Ready 		0
Unacked 	30
Total		30
```

如果prefetch是1，那么只要一条消息没有收到消费者的ACK，后续的消息都不会发送到这个消费者，造成消息堵塞。

```shell
Ready		9
Unacked		1
Total 		10
```

**15、SpringBoot中，bean还没有初始好，消费者就开始监听取消息了，导致空指针异常，怎么让消费者在容器启动完毕后才开始监听?**

RabbitMQ中有一个auto_startup参数，可以控制是否在容器启动时就启动监听。

全局参数：

```properties
spring.rabbitmq.listener.auto-startup=true ##默认是true
```

自定义容器(VIP工程消费者的RabbitConfig.java)，容器可以应用到消费者：

```java
// 默认是true 
factory.setAutoStartup(true);
```

消费者单独设置([Spring AMQP 2.0以后的版本才有](https://github.com/spring-projects/spring-amqp/issues/669)):

```java
@RabbitListener( queues = "${com.gupaoedu.thirdqueue}" ,autoStartup = "false")
```

另外可以参考一下动态管理监听的方法:

[浅谈spring-boot-rabbitmq动态管理的方法](https://www.jb51.net/article/131708.htm)
[RabbitMQ异常监控及动态控制队列消费的解决方案](https://blog.csdn.net/u011424653/article/details/79824538)

**16、消费者的集群或者微服务的多个实例，会不会重复接收消息?**

**17、重复创建会有什么问题?**

**18、持久化的队列和非持久化的交换机可以绑定吗?可以**

**19、使用了消息队列会有什么缺点**

* 系统可用性降低：如果消息队列出故障，则系统可用性会降低
* 系统复杂性增加：加入了消息队列，要多考虑很多方面的问题，比如：一致性问题、如何保证消息不被重复消费、如何保证消息可靠性传输等

**20、消息队列如何选型？**

* 看看该MQ的更新频率
* 中小型软件公司，建议选RabbitMQ.
  * RabbitMQ的社区十分活跃，可以解决开发过程中遇到的bug
  * 不考虑rocketmq和kafka的原因是，一方面中
  * 小型软件公司不如互联网公司，数据量没那么大，选消息中间件，应首选功能比较完备的，所以kafka排除。不考虑rocketmq的原因是，rocketmq是阿里出品，如果阿里放弃维护rocketmq，中小型公司一般抽不出人来进行rocketmq的定制化开发，因此不推荐
* 大型软件公司，根据具体使用在rocketMq和kafka之间二选一
  * 大型软件公司，具备足够的资金搭建分布式环境，也具备足够大的数据量
  * 针对rocketMQ,大型软件公司也可以抽出人手对rocketMQ进行定制化开发，毕竟国内有能力改JAVA源码的人，还是相当多的
  * 至于kafka，根据业务场景选择，如果有日志采集功能，肯定是首选kafka了
  * 具体该选哪个，看使用场景

[如何设计一个MQ服务?](http://www.xuxueli.com/xxl-mq/#/)

# 典型应用场景

* 跨系统的异步通信
* 应用内的同步变成异步（应用解耦）
  * 秒杀：自己发送给自己
  * 由于异步线程里的操作都是很耗时间的操作，也消耗系统资源
  * 主线程依旧处理耗时低的入库操作，然后把需要处理的消息写进消息队列中，然后，独立的子系统，同时订阅消息队列，进行单独处理，处理好之后，向队列发送ACK确认，消息队列整条数据删除
* 流量削峰
  * 控制队列长度，当请求来了，往队列里写入，超过队列的长度，就返回失败，给用户报一个提示等等
* 日志处理 
  * 个系统有大量的业务需要各种日志来保证后续的分析工作，而且实时性要求不高，用队列处理再好不过了
* 系统间同步数据
  * 摒弃ELT（）(比如全量同步商户数据); 摒弃API(比如定时增量获取用户、获取产品，变成增量广播)
  * ELT是利用数据库的处理能力，E=从源数据库抽取数据，L=把数据加载到目标库的临时表中，T=对临时表中的数据进行转换，然后加载到目标库目标表中
* 基于Pub/Sub模型实现的事件驱动
  * ETL	HTTP API ——MQ
  * ETL：用来描述将数据从来源端经过抽取（extract）、交互转换（transform）、加载（load）至目的端的过程
* 利用RabbitMQ实现事务的最终一致性
  * 用消息确认机制来保证：**只要消息发送，就能确保被消费者消费**来做到了消息最终一致性

# 基本介绍

是一个Erlang开发的AMQP（Advanced Message Queuing Protocol ）的开源实现

## AMQP协议

AMQP，即AdvancedMessage Queuing Protocol，一个提供统一消息服务的应用层标准高级消息队列协议，是应用层协议的一个开放标准，为面向消息的中间件设计。基于此协议的客户端与消息中间件可传递消息，并不受客户端/中间件同产品、不同的开发语言等条件的限制

AMQP的实现有：

* RabbitMQ
* OpenAMQ、Apache Qpid

## 安装

```shell
# 成功
# 安装socat
yum -y install socat
# 1. 先安装Erlang
rpm -Uvh http://www.rabbitmq.com/releases/erlang/erlang-19.0.4-1.el7.centos.x86_64.rpm
# 2. 安装rabbitmq-server 
rpm -Uvh http://www.rabbitmq.com/releases/rabbitmq-server/v3.6.15/rabbitmq-server-3.6.15-1.el6.noarch.rpm
# 查看rabbitmq-server有没有安装好  #rabbitmq-server-3.6.15-1.el6.noarch
rpm -qa|grep rabbitmq
# 安装维护插件
rabbitmq-plugins enable rabbitmq_management 
# 开启rabbit-server
service rabbitmq-server start
# 关掉服务
service rabbitmq-server stop
# 查看rabbit-server当前状态
rabbitmqctl status
# 重启
service rabbitmq-server restart

# 访问
http://192.168.55.122:15672/
# guest账户只能http://localhost:15672/ 的方式进入，需要修改配置
# 解决办法是：
vi /etc/rabbitmq/rabbitmq.config
# 输入
[{rabbit, [{loopback_users, []}]}].
# 新建账户并赋予权限
rabbitmqctl  add_user  username  password
rabbitmqctl  set_user_tags  username  administrator
# 查看创建完的账号
rabbitmqctl list_users
# 删除用户
rabbitmqctl delete_user username
# 修改密码
rabbitmqctl  oldPassword  Username  newPassword
# 再回到http://外网ip:15672/ 用刚才的用户名和密码登陆就能进去了

# 用户角色
1. administrator超级管理员：
	可登陆管理控制台(启用management plugin的情况下)，可查看所有的信息，并且可以对用户，策略(policy)进行操作
2. monitoring监控者
	可登陆管理控制台(启用management plugin的情况下)，同时可以查看rabbitmq节点的相关信息(进程数，内存使用情况，磁盘使用情况等)
3. policymaker策略制定者
	可登陆管理控制台(启用management plugin的情况下), 同时可以对policy进行管理。但无法查看节点的相关信息(上图红框标识的部分)。与administrator的对比，administrator能看到这些内容。
4. management普通管理者
	仅可登陆管理控制台(启用management plugin的情况下)，无法看到节点信息，也无法对策略进行管理
5. 其他
	无法登陆管理控制台，通常就是普通的生产者和消费者
# 设置用户角色的命令
# User为用户名， Tag为角色名(对应于上面的administrator，monitoring，policymaker，management，或其他自定义名称)
rabbitmqctl  set_user_tags  User  Tag

# 要给新建的用户设置权限，否则项目连接不上

# 设置用户权限
# 用户权限指的是用户对exchange，queue的操作权限，包括配置权限，读写权限。配置权限会影响到exchange，queue的声明和删除。读写权限影响到从queue里取消息，向exchange发送消息以及queue和exchange的绑定(bind)操作
# rabbitmqctl set_permissions -p  VHostPath  User  ConfP  WriteP  ReadP
# 		Conf：一个正则表达式match哪些配置资源能够被该用户访问
#	 	Write：一个正则表达式match哪些配置资源能够被该用户读。
# 		read：一个正则表达式match哪些配置资源能够被该用户访问
rabbitmqctl set_permissions -p "/" username ".*" ".*" ".*"
# 查看(指定hostpath)所有用户的权限信息
rabbitmqctl  list_permissions  [-p  VHostPath]
# 查看指定用户的权限信息
rabbitmqctl  list_user_permissions  User
# 清除用户的权限信息
rabbitmqctl  clear_permissions  [-p VHostPath]  User

# 卸载
[root@localhost shared_docs]# rpm -qa | grep rabbitmq-server
rabbitmq-server-3.7.4-1.el7.noarch
[root@localhost shared_docs]#  rpm -e rabbitmq-server
```

# RabbitMQ的特性

RabbitMQ使用Erlang语言编写，使用Mnesia数据库存储消息。

* 可靠性

  RabbitMQ 使用一些机制来保证可靠性，如持久化、传输确认、发布确认

* 灵活的路由

  在消息进入队列之前，通过 Exchange 来路由消息的。对于典型的路由功能，RabbitMQ 已经提供了一些内置的 Exchange 来实现。针对更复杂的路由功能，可以将多个 Exchange 绑定在一起，也通过插件机制实现自己的 Exchange

* 消息集群

  多个 RabbitMQ 服务器可以组成一个集群，形成一个逻辑 Broker 

* 高可用

   队列可以在集群中的机器上进行镜像，使得在部分节点出问题的情况下队列仍然可用 

* 多种协议

  RabbitMQ 支持多种消息队列协议，比如 AMQP、STOMP、MQTT 等等

* 多语言客户端

  RabbitMQ 几乎支持所有常用语言，比如 Java、.NET、Ruby、PHP、C#、JavaScript 等等

* 管理界面

  RabbitMQ 提供了一个易用的用户界面，使得用户可以监控和管理消息、集群中的节点。

* 插件机制

  RabbitMQ提供了许多插件，以实现从多方面扩展，当然也可以编写自己的插件

## ==工作模型==

![image-20190105182648632](/Users/dingyuanjie/Desktop/MyKnowledge/2.code/java/2.咕泡学院/02.分布式专题/06.分布式消息通信/image-20190105182648632-6684008.png)

* Broker

  即RabbitMQ的实体服务器。提供一种传输服务，维护一条从生产者到消费者的传输线路，保证消息数据能按照指定的方式传输。

* Message

  消息，消息是不具名的，它由消息头和消息体组成。消息体是不透明的，而消息头则由一系列的可选属性组成，这些属性包括routing-key（路由键）、priority（相对于其他消息的优先权）、delivery-mode（指出该消息可能需要持久性存储）等。

* Exchange

  消息交换机。指定消息按照什么规则路由到哪个队列Queue

* Queue

  消息队列。消息的载体，每条消息都会被投送到一个或多个队列中

* Binding

  绑定。作用就是将Exchange和Queue按照某种路由规则绑定起来

* Routing Key

  路由关键字。Exchange根据Routing Key进行消息投递。定义绑定时指定的关键字称为Binding Key

* Vhost

  虚拟主机。一个Broker可以有多个虚拟主机，用作不同用户的权限分离。一个虚拟主机持有一组Exchange、Queue和Binding。

* Producer

  消息生产者。主要将消息投递到对应的Exchange上面。一般是独立的程序。

* Consumer

  消息消费者。消息的接收者，一般是独立的程序

* Connection

  Producer 和 Consumer 与Broker之间的TCP长连接

* Channel

  息通道，也称信道。在客户端的每个连接里可以建立多个Channel，每个Channel代表一个会话任务。在RabbitMQ Java Client API中，channel上定义了大量的编程接口

* 由Exchange、Queue、RoutingKey三个才能决定一个从Exchange到Queue的唯一的线路

## Direct Exchange直连交换机

定义：直连类型的交换机与一个队列绑定时，需要指定一个明确的binding key

路由规则：发送消息到直连类型的交换机时，只有routing key跟binding key完全匹配时，绑定的队列才能收到消息 

```java
// 只有队列1能收到消息 
channel.basicPublish("MY_DIRECT_EXCHANGE", "key1", null, msg.getBytes());
```



![image-20190105182741347](/Users/dingyuanjie/Desktop/MyKnowledge/2.code/java/2.咕泡学院/02.分布式专题/06.分布式消息通信/image-20190105182741347-6684061.png)

## Topic Exchange主题交换机

定义：主题类型的交换机与一个队列绑定时，可以指定按模式匹配的routing key。 

通配符有两个，*代表匹配一个单词。#代表匹配零个或者多个单词。单词与单词之间用 . 隔开。 

路由规则：发送消息到主题类型的交换机时，routing key符合binding key的模式时，绑定的队列才能收到消息 

```java
// 只有队列1能收到消息
channel.basicPublish("MY_TOPIC_EXCHANGE", "sh.abc", null, msg.getBytes());
// 队列2和队列3能收到消息
channel.basicPublish("MY_TOPIC_EXCHANGE", "bj.book", null, msg.getBytes());
// 只有队列4能收到消息
channel.basicPublish("MY_TOPIC_EXCHANGE", "abc.def.food", null, msg.getBytes());
```

![image-20190105182836972](/Users/dingyuanjie/Desktop/MyKnowledge/2.code/java/2.咕泡学院/02.分布式专题/06.分布式消息通信/image-20190105182836972-6684116.png)

## Fanout Exchange 广播交换机

定义：广播类型的交换机与一个队列绑定时，不需要指定binding key。
路由规则：当消息发送到广播类型的交换机时，不需要指定routing key，所有与之绑定的队列都能收到消息。
例如：

```java
// 3个队列都会收到消息
channel.basicPublish("MY_FANOUT_EXCHANGE", "", null, msg.getBytes());
```

![image-20190105182910724](/Users/dingyuanjie/Desktop/MyKnowledge/2.code/java/2.咕泡学院/02.分布式专题/06.分布式消息通信/image-20190105182910724-6684150.png)

# Java API编程

## RabbitMQ官网

## 生产者

```java
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;
public class MyProducer {
    private final static String QUEUE_NAME = "ORIGIN_QUEUE";
	public static void main(String[] args) throws Exception { 
        ConnectionFactory factory = new ConnectionFactory(); 
        // 连接IP
		factory.setHost("127.0.0.1");
		// 连接端口 
        factory.setPort(5672);
		// 虚拟机 
        factory.setVirtualHost("/"); 
        // 用户 
        factory.setUsername("guest"); 
        factory.setPassword("guest");
		// 建立连接
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
        // 连接IP
		factory.setHost("127.0.0.1");
		// 默认监听端口 
        factory.setPort(5672);
		// 虚拟机 
        factory.setVirtualHost("/"); 
        // 设置访问的用户 
        factory.setUsername("guest"); 
        factory.setPassword("guest");
		// 建立连接
		Connection conn = factory.newConnection(); 
        // 创建消息通道
		Channel channel = conn.createChannel();
		// 声明队列
        // String queue, boolean durable, boolean exclusive, boolean autoDelete, Map<String, Object> arguments
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

## 参数详解

* 声明交换机的参数

  String type：交换机的类型，direct, topic, fanout中的一种。

  boolean durable：是否持久化，代表交换机在服务器重启后是否还存在

* 声明队列的参数

  boolean durable：是否持久化，代表队列在服务器重启后是否还存在

  boolean exclusive：是否排他性队列。排他性队列只能在声明它的Connection中使用，连接断开时自动删除

  boolean autoDelete：是否自动删除。如果为true，至少有一个消费者连接到这个队列，之后所有与这个队列连接的消费者都断开时，队列会自动删除

  Map<String, Object> arguments：队列的其他属性，例如x-message-ttl、x-expires、x-max-length、x-max-length-bytes、x-dead-letter-exchange、x-dead-letter-routing-key、x-max-priority

* 消息属性BasicProperties

  消息的全部属性有14个，以下列举了一些主要的参数：

  Map<String,Object> headers			消息的其他自定义参数

  Integer deliveryMode					2持久化，其他:瞬态

  Integer priority						消息的优先级

  String correlationId					关联ID，方便RPC相应与请求关联

  String replyTo						回调队列

  String expiration						TTL，消息过期时间，单位毫秒

```java
public class Producer {
    public static void main(String[] args) throws IOException, TimeoutException {
        //创建连接工厂
        ConnectionFactory factory = new ConnectionFactory();
        factory.setUsername("guest");
        factory.setPassword("guest");
        //设置 RabbitMQ 地址
        factory.setHost("localhost");
        //建立到代理服务器到连接
        Connection conn = factory.newConnection();
        //获得信道
        Channel channel = conn.createChannel();
        //声明交换器
        String exchangeName = "hello-exchange";
        channel.exchangeDeclare(exchangeName, "direct", true);

        String routingKey = "hola";
        //发布消息
        byte[] messageBodyBytes = "quit".getBytes();
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
        //建立到代理服务器到连接
        Connection conn = factory.newConnection();
        //获得信道
        final Channel channel = conn.createChannel();
        //声明交换器
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

## 怎么自动删除没人消费的消息

**TTL(Time To Live)**

* 消息的过期时间

  有两种设置方式：

  1. 通过队列属性设置消息过期时间:

     ```java
     Map<String, Object> argss = new HashMap<String, Object>();
     argss.put("x-message-ttl",6000);
     channel.queueDeclare("TEST_TTL_QUEUE", false, false, false, argss);
     ```

  2. 设置单条消息的过期时间:

     ```java
      AMQP.BasicProperties properties = new AMQP.BasicProperties.Builder() 		
          	.deliveryMode(2) // 持久化消息
             .contentEncoding("UTF-8")
             .expiration("10000") // TTL
             .build();
     channel.basicPublish("", "TEST_TTL_QUEUE", properties, msg.getBytes());
     ```

* 队列的过期时间

  ```java
  Map<String, Object> argss = new HashMap<String, Object>();
  argss.put("x-message-ttl",6000);
  channel.queueDeclare("TEST_TTL_QUEUE", false, false, false, argss);
  ```

  队列的过期时间决定了在没有任何消费者以后，队列可以存活多久


## 无法路由的消息，去了哪里

**死信队列**

有三种情况消息会进入DLX(Dead Letter Exchange)死信交换机。

1. 消费者拒绝或者没有应答，并且没有让消息重新入队(NACK || Reject ) && requeue == false
2. 消息过期
3. 队列达到最大长度(先入队的消息会被发送到DLX)

![image-20190105195816827](/Users/dingyuanjie/Desktop/MyKnowledge/2.code/java/2.咕泡学院/02.分布式专题/06.分布式消息通信/image-20190105195816827-6689496.png)

可以设置一个死信队列(Dead Letter Queue)与DLX绑定，即可以存储Dead Letter，消费者可以监听这个队列取
走消息

```java
Map<String,Object> arguments = new HashMap<String,Object>(); 
arguments.put("x-dead-letter-exchange","DLX_EXCHANGE");
// 指定了这个队列的死信交换机
channel.queueDeclare("TEST_DLX_QUEUE", false, false, false, arguments);
// 声明死信交换机
channel.exchangeDeclare("DLX_EXCHANGE","topic", false, false, false, null); // 声明死信队列
channel.queueDeclare("DLX_QUEUE", false, false, false, null);
// 绑定
channel.queueBind("DLX_QUEUE","DLX_EXCHANGE","#");
```

## 可以让消息优先得到消费吗？

**优先级队列**

设置一个队列的最大优先级：

```java
Map<String, Object> argss = new HashMap<String, Object>(); 
argss.put("x-max-priority",10); // 队列最大优先级
channel.queueDeclare("ORIGIN_QUEUE", false, false, false, argss);
```

发送消息时指定消息当前的优先级：

```java
Map<String, Object> headers = new HashMap<String, Object>();
headers.put("name", "gupao");
headers.put("level", "top");
AMQP.BasicProperties properties = new AMQP.BasicProperties.Builder()
    .deliveryMode(2)   // 2代表持久化
    .contentEncoding("UTF-8")  // 编码
    .expiration("10000")  // TTL，过期时间
    .headers(headers) // 自定义属性
    .priority(5) // 优先级，默认为5，配合队列的 x-max-priority 属性使用
    .messageId(String.valueOf(UUID.randomUUID()))
    .build();
String msg = "Hello world, Rabbit MQ";
channel.queueDeclare(QUEUE_NAME, false, false, false, null);
channel.basicPublish("", QUEUE_NAME, properties, msg.getBytes());
----------------------------------------------------------------
```

优先级高的消息可以优先被消费，但是:只有消息堆积(消息的发送速度大于消费者的消费速度)的情况下优先级才有意义

## ==如何实现延迟发送消息==

**延迟队列**

RabbitMQ本身不支持延迟队列。可以使用TTL结合DLX的方式来实现消息的延迟投递，即把DLX跟某个队列绑定，
到了指定时间，消息过期后，就会从DLX路由到这个队列，消费者可以从这个队列取走消息

另一种方式是使用rabbitmq-delayed-message-exchange插件

当然，将需要发送的信息保存在数据库，使用任务调度系统扫描然后发送也是可以实现的

```java
// 生产者
String msg = "Hello world, Rabbit MQ, DLX MSG";

// 设置属性，消息10秒钟过期
AMQP.BasicProperties properties = new AMQP.BasicProperties.Builder()
    .deliveryMode(2) // 持久化消息
    .contentEncoding("UTF-8")
    .expiration("10000") // TTL
    .build();

// 发送消息
for (int i=0; i<10; i++){
    channel.basicPublish("", "TEST_DLX_QUEUE", properties, msg.getBytes());
}
```

```java
//消费者
// 指定队列的死信交换机
Map<String,Object> arguments = new HashMap<String,Object>();
arguments.put("x-dead-letter-exchange","DLX_EXCHANGE");
// arguments.put("x-expires","9000"); // 设置队列的TTL
// arguments.put("x-max-length", 4); // 如果设置了队列的最大长度，超过长度时，先入队的消息会被发送到DLX

// 声明队列（默认交换机AMQP default，Direct）
channel.queueDeclare("TEST_DLX_QUEUE", false, false, false, arguments);
// 声明死信交换机
channel.exchangeDeclare("DLX_EXCHANGE","topic", false, false, false, null);
// 声明死信队列
channel.queueDeclare("DLX_QUEUE", false, false, false, null);
// 绑定，此处 Dead letter routing key 设置为 #
channel.queueBind("DLX_QUEUE","DLX_EXCHANGE","#");
System.out.println(" Waiting for message....");
// 创建消费者
Consumer consumer = new DefaultConsumer(channel) {
    @Override
    public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties,byte[] body) throws IOException {
        String msg = new String(body, "UTF-8");
        System.out.println("Received message : '" + msg + "'");
    }
};
// 开始获取消息
// String queue, boolean autoAck, Consumer callback
//过期前可以消费
// channel.basicConsume("TEST_DLX_QUEUE", true, consumer);
//过期后消费
channel.basicConsume("DTL_QUEUE", true, consumer);
```

## ==MQ怎么实现RPC==

**RPC**

RabbitMQ实现RPC的原理:服务端处理消息后，把响应消息发送到一个响应队列，客户端再从响应队列取到结果

其中的问题:Client收到消息后，怎么知道应答消息是回复哪一条消息的?所以必须有一个唯一ID来关联，就是correlationId

![image-20190105200154824](/Users/dingyuanjie/Desktop/MyKnowledge/2.code/java/2.咕泡学院/02.分布式专题/06.分布式消息通信/image-20190105200154824-6689714.png)

```java
/**
 * RPC客户端，后启动
 */
public class RPCClient{
    private final static String REQUEST_QUEUE_NAME="RPC_REQUEST";
    private final static String RESPONSE_QUEUE_NAME="RPC_RESPONSE";
    private Channel channel;
    private Consumer consumer;

    //构造函数 初始化连接
    public RPCClient() throws IOException, TimeoutException, NoSuchAlgorithmException, KeyManagementException, URISyntaxException {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setUri(ResourceUtil.getKey("rabbitmq.uri"));

        //创建一个新的连接 即TCP连接
        Connection connection = factory.newConnection();
        //创建一个通道
        channel = connection.createChannel();
        //创建一个请求队列
        channel.queueDeclare(REQUEST_QUEUE_NAME, true, false, false, null);
        //创建一个回调队列
        channel.queueDeclare(RESPONSE_QUEUE_NAME,true,false,false,null);
    }

    /**
     * PRC 远程调用计算平方
     */
    public String getSquare(String message) throws  Exception{
        //定义消息属性中的correlationId
        String correlationId = java.util.UUID.randomUUID().toString();

        //设置消息属性的replyTo和correlationId
        BasicProperties properties = new BasicProperties.Builder()
                .correlationId(correlationId)
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
            public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties,
                                       byte[] body) throws IOException {
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
/**
 * RPC服务端，先启动
 */
public class RPCServer {
    private final static String REQUEST_QUEUE_NAME="RPC_REQUEST";

    public static void main(String[] args) throws Exception{
        ConnectionFactory factory = new ConnectionFactory();
        factory.setUri(ResourceUtil.getKey("rabbitmq.uri"));

        //创建一个新的连接 即TCP连接
        Connection connection = factory.newConnection();
        //创建一个通道
        final Channel channel = connection.createChannel();
        //声明队列
        channel.queueDeclare(REQUEST_QUEUE_NAME, true, false, false, null);
        //设置prefetch值 一次处理1条数据
        channel.basicQos(1);

        // 创建消费者
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

* **服务端流控(Flow Control)**

RabbitMQ 会在启动时检测机器的物理内存数值。默认当 MQ 占用 40% 以上内存时，MQ 会主动抛出一个内存警
告并阻塞所有连接(Connections)。可以通过修改 rabbitmq.config 文件来调整内存阈值，默认值是 0.4，如下所示: [{rabbit, [{vm_memory_high_watermark, 0.4}]}].

默认情况，如果剩余磁盘空间在 1GB 以下，RabbitMQ 主动阻塞所有的生产者。这个阈值也是可调的

注意队列长度只在消息堆积的情况下有意义，而且会删除先入队的消息，不能实现服务端限流

* **消费端限流**

在AutoACK为false的情况下，如果一定数目的消息(通过基于consumer或者channel设置Qos的值)未被确认前，不进行消费新的消息前，不进行消费新的消息

```java
////非自动确认消息的前提下，如果一定数目的消息（通过基于consume或者channel设置Qos的值）未被确认前，不进行消费新的消息。
channel.basicQos(2); // 如果超过2条消息没有发送ACK，当前消费者不再接受队列消息 
channel.basicConsume(QUEUE_NAME, false, consumer);
```

x-max-length = 80

1. 消息堆积的时候才有用
2. 先进入队列的120条消息删除了

# UI管理界面

管理插件提供了更简单的管理方式

* 启用管理插件

  ```shell
  # Windows启用管理插件
  cd C:\Program Files\RabbitMQ Server\rabbitmq_server-3.6.6\sbin
  rabbitmq-plugins.bat enable rabbitmq_management
  
  # Linux启用管理插件
  cd /usr/lib/rabbitmq/bin
  ./rabbitmq-plugins enable rabbitmq_management
  ```

* 管理界面访问端口

  默认端口是15672，默认用户guest，密码guest。guest用户默认只能在本机访问

* Linux 创建RabbitMQ用户

  例如创建用户admin，密码admin，授权访问所有的Vhost

  ```shell
  firewall-cmd --permanent --add-port=15672/tcp
  firewall-cmd --reload
  rabbitmqctl add_user admin admin
  rabbitmqctl set_user_tags admin administrator
  rabbitmqctl set_permissions -p / admin ".*" ".*" ".*"
  ```

# Spring配置方式集成RabbitMQ

1、创建Maven工程，pom.xml引入依赖

```xml
<dependency>
    <groupId>com.rabbitmq</groupId>
    <artifactId>amqp-client</artifactId>
    <version>4.2.0</version>
</dependency>
<dependency>
    <groupId>org.springframework.amqp</groupId>
    <artifactId>spring-rabbit</artifactId>
    <version>1.7.3.RELEASE</version>
</dependency>
```

2、src/main/resouces目录，创建rabbitMQ.xml![image-20190105183224229](/Users/dingyuanjie/Desktop/MyKnowledge/2.code/java/2.咕泡学院/02.分布式专题/06.分布式消息通信/image-20190105183224229-6684344.png)

```xml
<?xml version="1.0" encoding="UTF-8"?>
<beans xmlns="http://www.springframework.org/schema/beans"
       xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xmlns:rabbit="http://www.springframework.org/schema/rabbit"
       xsi:schemaLocation="http://www.springframework.org/schema/beans
     http://www.springframework.org/schema/beans/spring-beans-4.0.xsd
     http://www.springframework.org/schema/rabbit
     http://www.springframework.org/schema/rabbit/spring-rabbit.xsd">

    <!--配置connection-factory，指定连接rabbit server参数 -->
    <rabbit:connection-factory id="connectionFactory" virtual-host="/" username="guest" password="guest" host="127.0.0.1" port="5672" />

    <!--通过指定下面的admin信息，当前producer中的exchange和queue会在rabbitmq服务器上自动生成 -->
    <rabbit:admin id="connectAdmin" connection-factory="connectionFactory" />

    <!--######分隔线######-->
    <!--定义queue -->
    <rabbit:queue name="MY_FIRST_QUEUE" durable="true" auto-delete="false" exclusive="false" declared-by="connectAdmin" />

    <!--定义direct exchange，绑定MY_FIRST_QUEUE -->
    <rabbit:direct-exchange name="MY_DIRECT_EXCHANGE" durable="true" auto-delete="false" declared-by="connectAdmin">
        <rabbit:bindings>
            <rabbit:binding queue="MY_FIRST_QUEUE" key="FirstKey">
            </rabbit:binding>
        </rabbit:bindings>
    </rabbit:direct-exchange>

    <!--定义rabbit template用于数据的接收和发送 -->
    <rabbit:template id="amqpTemplate" connection-factory="connectionFactory" exchange="MY_DIRECT_EXCHANGE" />

    <!--消息接收者 -->
    <bean id="messageReceiver" class="com.gupaoedu.consumer.FirstConsumer"></bean>

    <!--queue listener 观察 监听模式 当有消息到达时会通知监听在对应的队列上的监听对象 -->
    <rabbit:listener-container connection-factory="connectionFactory">
        <rabbit:listener queues="MY_FIRST_QUEUE" ref="messageReceiver" />
    </rabbit:listener-container>

    <!--定义queue -->
    <rabbit:queue name="MY_SECOND_QUEUE" durable="true" auto-delete="false" exclusive="false" declared-by="connectAdmin" />

    <!-- 将已经定义的Exchange绑定到MY_SECOND_QUEUE，注意关键词是key -->
    <rabbit:direct-exchange name="MY_DIRECT_EXCHANGE" durable="true" auto-delete="false" declared-by="connectAdmin">
        <rabbit:bindings>
            <rabbit:binding queue="MY_SECOND_QUEUE" key="SecondKey"></rabbit:binding>
        </rabbit:bindings>
    </rabbit:direct-exchange>

    <!-- 消息接收者 -->
    <bean id="receiverSecond" class="com.gupaoedu.consumer.SecondConsumer"></bean>

    <!-- queue litener 观察 监听模式 当有消息到达时会通知监听在对应的队列上的监听对象 -->
    <rabbit:listener-container connection-factory="connectionFactory">
        <rabbit:listener queues="MY_SECOND_QUEUE" ref="receiverSecond" />
    </rabbit:listener-container>

    <!--######分隔线######-->
    <!--定义queue -->
    <rabbit:queue name="MY_THIRD_QUEUE" durable="true" auto-delete="false" exclusive="false" declared-by="connectAdmin" />

    <!-- 定义topic exchange，绑定MY_THIRD_QUEUE，注意关键词是pattern -->
    <rabbit:topic-exchange name="MY_TOPIC_EXCHANGE" durable="true" auto-delete="false" declared-by="connectAdmin">
        <rabbit:bindings>
            <rabbit:binding queue="MY_THIRD_QUEUE" pattern="#.Third.#"></rabbit:binding>
        </rabbit:bindings>
    </rabbit:topic-exchange>

    <!--定义rabbit template用于数据的接收和发送 -->
    <rabbit:template id="amqpTemplate2" connection-factory="connectionFactory" exchange="MY_TOPIC_EXCHANGE" />

    <!-- 消息接收者 -->
    <bean id="receiverThird" class="com.gupaoedu.consumer.ThirdConsumer"></bean>

    <!-- queue litener 观察 监听模式 当有消息到达时会通知监听在对应的队列上的监听对象 -->
    <rabbit:listener-container connection-factory="connectionFactory">
        <rabbit:listener queues="MY_THIRD_QUEUE" ref="receiverThird" />
    </rabbit:listener-container>

    <!--######分隔线######-->
    <!--定义queue -->
    <rabbit:queue name="MY_FOURTH_QUEUE" durable="true" auto-delete="false" exclusive="false" declared-by="connectAdmin" />

    <!-- 定义fanout exchange，绑定MY_FIRST_QUEUE 和 MY_FOURTH_QUEUE -->
    <rabbit:fanout-exchange name="MY_FANOUT_EXCHANGE" auto-delete="false" durable="true" declared-by="connectAdmin" >
        <rabbit:bindings>
            <rabbit:binding queue="MY_FIRST_QUEUE"></rabbit:binding>
            <rabbit:binding queue="MY_FOURTH_QUEUE"></rabbit:binding>
        </rabbit:bindings>
    </rabbit:fanout-exchange>

    <!-- 消息接收者 -->
    <bean id="receiverFourth" class="com.gupaoedu.consumer.FourthConsumer"></bean>

    <!-- queue litener 观察 监听模式 当有消息到达时会通知监听在对应的队列上的监听对象 -->
    <rabbit:listener-container connection-factory="connectionFactory">
        <rabbit:listener queues="MY_FOURTH_QUEUE" ref="receiverFourth" />
    </rabbit:listener-container>
</beans>
```

3、配置applicationContext.xml

```xml
<?xml version="1.0" encoding="UTF-8"?>
<beans xmlns="http://www.springframework.org/schema/beans"
       xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
       xmlns:context="http://www.springframework.org/schema/context"
       xsi:schemaLocation="http://www.springframework.org/schema/beans
http://www.springframework.org/schema/beans/spring-beans-3.1.xsd
http://www.springframework.org/schema/context
http://www.springframework.org/schema/context/spring-context-3.1.xsd">
	<!-- 扫描指定package下所有带有如 @Controller,@Service,@Resource 并把所注释的注 册为Spring Beans -->
    <context:component-scan base-package="com.woody.framework.rabbitmq" /> 
    <!-- 激活annotation功能 -->
    <context:annotation-config />
    <import resource="classpath:/rabbitmq/rabbitMQ.xml" />
</beans>
</beans>
```

4、src/main/resouces目录，log4j.properties

```java
log4j.rootLogger=INFO,consoleAppender,fileAppender
log4j.category.ETTAppLogger=DEBUG, ettAppLogFile
log4j.appender.consoleAppender=org.apache.log4j.ConsoleAppender
log4j.appender.consoleAppender.Threshold=TRACE
log4j.appender.consoleAppender.layout=org.apache.log4j.PatternLayout
log4j.appender.consoleAppender.layout.ConversionPattern=%-d{yyyy-MM-dd HH:mm:ss SSS} ->[%t]--[%-5p]--[%c{1}]--%m%n
log4j.appender.fileAppender=org.apache.log4j.DailyRollingFileAppender
log4j.appender.fileAppender.File=F:/dev_logs/rabbitmq/debug1.log
log4j.appender.fileAppender.DatePattern='_'yyyy-MM-dd'.log'
log4j.appender.fileAppender.Threshold=TRACE
log4j.appender.fileAppender.Encoding=BIG5
log4j.appender.fileAppender.layout=org.apache.log4j.PatternLayout
log4j.appender.fileAppender.layout.ConversionPattern=%-d{yyyy-MM-dd HH:mm:ss SSS}-->[%t]--[%-5p]--[%c{1}]--%m%n
log4j.appender.ettAppLogFile=org.apache.log4j.DailyRollingFileAppender
log4j.appender.ettAppLogFile.File=F:/dev_logs/rabbitmq/ettdebug.log
log4j.appender.ettAppLogFile.DatePattern='_'yyyy-MM-dd'.log'
log4j.appender.ettAppLogFile.Threshold=DEBUG
log4j.appender.ettAppLogFile.layout=org.apache.log4j.PatternLayout
log4j.appender.ettAppLogFile.layout.ConversionPattern=%-d{yyyy-MM-dd HH\:mm\:ss SSS}-->[%t]--[%-5p]--[%c{1}]--%m%n
```

5、编写生产者

```java
@Service
public class MessageProducer {
    private Logger logger = LoggerFactory.getLogger(MessageProducer.class);

    @Autowired
    @Qualifier("amqpTemplate")
    private AmqpTemplate amqpTemplate;

    @Autowired
    @Qualifier("amqpTemplate2")
    private AmqpTemplate amqpTemplate2;

    /**
     * 演示三种交换机的使用
     */
    public void sendMessage(Object message) {
        logger.info("Send message:" + message);

        // amqpTemplate 默认交换机 MY_DIRECT_EXCHANGE
        // amqpTemplate2 默认交换机 MY_TOPIC_EXCHANGE

        // Exchange 为 direct 模式，直接指定routingKey
        amqpTemplate.convertAndSend("FirstKey", "[Direct,FirstKey] "+message);
        amqpTemplate.convertAndSend("SecondKey", "[Direct,SecondKey] "+message);

        // Exchange模式为topic，通过topic匹配关心该主题的队列
        amqpTemplate2.convertAndSend("msg.Third.send","[Topic,msg.Third.send] "+message);

        // 广播消息，与Exchange绑定的所有队列都会收到消息，routingKey为空
        amqpTemplate2.convertAndSend("MY_FANOUT_EXCHANGE",null,"[Fanout] "+message);
    }
}
```

6、编写4个消费者

```java
public class FirstConsumer implements MessageListener {
    private Logger logger = LoggerFactory.getLogger(FirstConsumer.class);

    public void onMessage(Message message) {
        logger.info("The first consumer received message : " + message.getBody());
    }
}

public class SecondConsumer implements MessageListener {
    private Logger logger = LoggerFactory.getLogger(SecondConsumer.class);

    public void onMessage(Message message) {
        logger.info("The second consumer received message : " + message);
    }
}

public class ThirdConsumer implements MessageListener {
    private Logger logger = LoggerFactory.getLogger(ThirdConsumer.class);

    public void onMessage(Message message) {
        logger.info("The third cosumer received message : " + message);
    }
}

public class FourthConsumer implements MessageListener {
    private Logger logger = LoggerFactory.getLogger(FourthConsumer.class);

    public void onMessage(Message message) {
        logger.info("The fourth consumer received message : " + message);
    }
}
```

7、编写单元测试类

```java
public class RabbitTest {
    private ApplicationContext context = null;

    //main方法或者Test
    @Test
    public void sendMessage() {
        context = new ClassPathXmlApplicationContext(""/spring/application-context.xml");
        MessageProducer messageProducer = (MessageProducer) context.getBean("messageProducer");
        int k = 100;
        while (k > 0) {
            messageProducer.sendMessage("第" + k + "次发送的消息");
            k--;
            try {
                Thread.sleep(1000);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
```

# Spring Boot集成RabbitMQ

![image-20190105183256897](/Users/dingyuanjie/Desktop/MyKnowledge/2.code/java/2.咕泡学院/02.分布式专题/06.分布式消息通信/image-20190105183256897-6684376.png)

消费者

1、配置类：定义交换机、队列、绑定

2、消费者：监听队列

生产者

1、生产者类：注入模板发送消息

2、单元测试类：调用生产者

服务器配置

```properties
# Spring Boot 2.0.6.RELEASE JDK 1.8
# 消息消费者 springboot-rabbit-consumer
# 消息生产者 springboot-rabbit-producer
# 界面测试地址
http://127.0.0.1:9071/merchantList  ，新增、删除、修改信息、修改状态会发送MQ消息到VIP_TOPIC_EXCHANGE，路由到VIP_SECOND_QUEUE
# 测试方法
1. 先启动消费者springboot-rabbit-consumer
2. 在界面上修改商户信息，或者调用生产者的单元测试类AppTests发送消息。
# 其他说明
Spring AMQP默认使用的消息转换器是SimpleMessageConverter。
Jackson2JsonMessageConverter 用于将消息转换为JSON后发送。
FourthConsumer里面实现了Java对象与JSON报文的转换。
```

```java
//生产者
/**
1. 创建生产者，注入gupaoTemplate发送消息。
2. 在任何需要发送MQ消息的地方注入gupaoTemplate，或者在单元测试类中注入生产者，调用send()方法。
*/
```

```java
//消费者
/** 
 1. 创建配置类，定义队列、交换机、绑定
 2. 创建消费者，监听队列
/*
```

# 可靠性投递分析

首先需要明确，效率与可靠性是无法兼得的，如果要保证每一个环节都成功，势必会对消息的收发效率造成影响

如果是一些业务实时一致性要求不是特别高的场合，可以牺牲一些可靠性来换取效率。

![image-20190105200805789](/Users/dingyuanjie/Desktop/MyKnowledge/2.code/java/2.咕泡学院/02.分布式专题/06.分布式消息通信/image-20190105200805789-6690085.png)

1 代表消息从生产者发送到Exchange;

2 代表消息从Exchange路由到Queue;

3 代表消息在Queue中存储;

4 代表消费者订阅Queue并消费消息。

## 1. 确保消息发送到RabbitMQ服务器

可能因为网络或者Broker的问题导致1失败，而生产者是无法知道消息是否正确发送到Broker的。

有两种解决方案，第一种是Transaction(事务)模式，第二种Confirm(确认)模式。

在通过channel.txSelect方法开启事务之后，我们便可以发布消息给RabbitMQ了，如果事务提交成功，则消息一定到达了RabbitMQ中，如果在事务提交执行之前由于RabbitMQ异常崩溃或者其他原因抛出异常，这个时候我们便可以将其捕获，进而通过执行channel.txRollback方法来实现事务回滚。使用事务机制的话会“吸干”RabbitMQ的性能，一般不建议使用。

生产者通过调用channel.confirmSelect方法(即Confirm.Select命令)将信道设置为confirm模式。一旦消息被投
递到所有匹配的队列之后，RabbitMQ就会发送一个确认(Basic.Ack)给生产者(包含消息的唯一ID)，这就使得生产者知晓消息已经正确到达了目的地了。

* 服务端确认——Transaction模式

  ![image-20190105200947722](/Users/dingyuanjie/Desktop/MyKnowledge/2.code/java/2.咕泡学院/02.分布式专题/06.分布式消息通信/image-20190105200947722-6690187.png)

  ```java
  try {
      channel.txSelect();
      // 发送消息
      // String exchange, String routingKey, BasicProperties props, byte[] body
      channel.basicPublish("", QUEUE_NAME, null, (msg).getBytes());
      // int i =1/0;
      channel.txCommit();
      System.out.println("消息发送成功");
  } catch (Exception e) {
      channel.txRollback();
      System.out.println("消息已经回滚");
  }
  ```

* 服务端确认——==Confirm模式==

  ![image-20190105201036954](/Users/dingyuanjie/Desktop/MyKnowledge/2.code/java/2.咕泡学院/02.分布式专题/06.分布式消息通信/image-20190105201036954-6690237.png)

  ```java
  //1. normal
  // 开启发送方确认模式
  channel.confirmSelect();
  channel.basicPublish("", QUEUE_NAME, null, msg.getBytes());
  // 普通Confirm，发送一条，确认一条
  if (channel.waitForConfirms()) {
      System.out.println("消息发送成功" );
  }
  
  //2. AsyncConfirmProducer
  // 开启发送方确认模式
  channel.confirmSelect();
  for (int i = 0; i < 10; i++) {
      // 发送消息
      // String exchange, String routingKey, BasicProperties props, byte[] body
      channel.basicPublish("", QUEUE_NAME, null, (msg +"-"+ i).getBytes());
  }
  
  // 这里不会打印所有响应的ACK；ACK可能有多个，有可能一次确认多条，也有可能一次确认一条
  // 异步监听确认和未确认的消息
  // 如果要重复运行，先停掉之前的生产者，清空队列
  channel.addConfirmListener(new ConfirmListener() {
      public void handleNack(long deliveryTag, boolean multiple) throws IOException {
          System.out.println("Broker未确认消息，标识：" + deliveryTag);
      }
      public void handleAck(long deliveryTag, boolean multiple) throws IOException {
          // 如果true表示批量执行了deliveryTag这个值以前（小于deliveryTag的）的所有消息，如果为false的话表示单条确认
          System.out.println(String.format("Broker已确认消息，标识：%d，多个消息：%b", deliveryTag, multiple));
      }
  });
  System.out.println("程序执行完成");
  
  //3. BatchConfirmProducer
  try {
      channel.confirmSelect();
      for (int i = 0; i < 5; i++) {
          // 发送消息
          // String exchange, String routingKey, BasicProperties props, byte[] body
          channel.basicPublish("", QUEUE_NAME, null, (msg +"-"+ i).getBytes());
      }
      // 批量确认结果，ACK如果是Multiple=True，代表ACK里面的Delivery-Tag之前的消息都被确认了
      // 比如5条消息可能只收到1个ACK，也可能收到2个（抓包才看得到）
      // 直到所有信息都发布，只要有一个未被Broker确认就会IOException
      channel.waitForConfirmsOrDie();
      System.out.println("消息发送完毕，批量确认成功");
  } catch (Exception e) {
      // 发生异常，可能需要对所有消息进行重发
      e.printStackTrace();
  }
  ```

## 2. 确保消息路由到正确的队列

可能因为路由关键字错误，或者队列不存在，或者队列名称错误导致2失败。

使用mandatory参数和ReturnListener，可以实现消息无法路由的时候返回给生产者。

另一种方式就是使用备份交换机(alternate-exchange)，无法路由的消息会发送到这个交换机上。

```java
Map<String,Object> arguments = new HashMap<String,Object>(); 
arguments.put("alternate-exchange","ALTERNATE_EXCHANGE"); // 指定交换机的备份交换机
channel.exchangeDeclare("TEST_EXCHANGE","topic", false, false, false, arguments);
```

```java
public class ReturnListenerProducer {
    public static void main(String[] args) throws Exception{
        ConnectionFactory factory = new ConnectionFactory();
        factory.setUri(ResourceUtil.getKey("rabbitmq.uri"));

        Connection connection = factory.newConnection();

        Channel channel = connection.createChannel();

        channel.addReturnListener(new ReturnListener() {
            public void handleReturn(int replyCode,
                                     String replyText,
                                     String exchange,
                                     String routingKey,
                                     AMQP.BasicProperties properties,
                                     byte[] body)
                    throws IOException {
                System.out.println("=========监听器收到了无法路由，被返回的消息============");
                System.out.println("replyText:"+replyText);
                System.out.println("exchange:"+exchange);
                System.out.println("routingKey:"+routingKey);
                System.out.println("message:"+new String(body));
            }
        });

        AMQP.BasicProperties properties = new AMQP.BasicProperties.Builder().deliveryMode(2).
                contentEncoding("UTF-8").build();

        // 在声明交换机的时候指定备份交换机
        //Map<String,Object> arguments = new HashMap<String,Object>();
        //arguments.put("alternate-exchange","ALTERNATE_EXCHANGE");
        //channel.exchangeDeclare("TEST_EXCHANGE","topic", false, false, false, arguments);

        // 发送到了默认的交换机上，由于没有任何队列使用这个关键字跟交换机绑定，所以会被退回
        // 第三个参数是设置的mandatory，如果mandatory是false，消息也会被直接丢弃
        channel.basicPublish("","gupaodirect",true, properties,"只为更好的你".getBytes());

        TimeUnit.SECONDS.sleep(10);

        channel.close();
        connection.close();
    }
}
```

路由保证：

1. ReturnListener
2. 备份交换机

## 3. 确保消息在队列正确地存储

可能因为系统宕机、重启、关闭等等情况导致存储在队列的消息丢失，即3出现问题。

解决方案：

1. 队列持久化

   ```java
   // String queue, boolean durable, boolean exclusive, boolean autoDelete, Map<String, Object> arguments
   channel.queueDeclare(QUEUE_NAME, true, false, false, null);
   ```

2. 交换机持久化

   ```java
   // String exchange, boolean durable
   channel.exchangeDeclare("MY_EXCHANGE",true);
   ```

3. 消息持久化

```java
 AMQP.BasicProperties properties = new AMQP.BasicProperties.Builder() 
     .deliveryMode(2) // 2代表持久化，其他代表瞬态
	 .build();
channel.basicPublish("", QUEUE_NAME, properties, msg.getBytes());
```

4. 集群，镜像队列，参考下一节

## 4. 确保消息从队列正确地投递到消费者

如果消费者收到消息后未来得及处理即发生异常，或者处理过程中发生异常，会导致4失败

为了保证消息从队列可靠地达到消费者，RabbitMQ提供了消息确认机制(message acknowledgement)。消费
者在订阅队列时，可以指定autoAck参数，当==autoAck==等于false时，RabbitMQ会等待消费者显式地回复确认信号
后才从队列中移去消息。

如果消息消费失败，也可以调用Basic.Reject或者Basic.Nack来拒绝当前消息而不是确认。如果requeue参数设置为true，可以把这条消息重新存入队列，以便发给下一个消费者(当然，只有一个消费者的时候，这种方式可能会出现无限循环重复消费的情况，可以投递到新的队列中，或者只打印异常日志)。

消费者确认：

* channel.basicAck();       //手工应答
* channel.basicReject();  //单条拒绝
* channel.basicNack();    //批量拒绝  

```java
// 声明队列（默认交换机AMQP default，Direct）
// String queue, boolean durable, boolean exclusive, boolean autoDelete, Map<String, Object> arguments
channel.queueDeclare(QUEUE_NAME, false, false, false, null);
System.out.println(" Waiting for message....");

// 创建消费者，并接收消息
Consumer consumer = new DefaultConsumer(channel) {
    @Override
    public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties,
                               byte[] body) throws IOException {
        String msg = new String(body, "UTF-8");
        System.out.println("Received message : '" + msg + "'");

        if (msg.contains("拒收")){
            // 拒绝消息
            // requeue：是否重新入队列，true：是；false：直接丢弃，相当于告诉队列可以直接删除掉
            // TODO 如果只有这一个消费者，requeue 为true 的时候会造成消息重复消费
            channel.basicReject(envelope.getDeliveryTag(), false);
        } else if (msg.contains("异常")){
            // 批量拒绝
            // requeue：是否重新入队列
            // TODO 如果只有这一个消费者，requeue 为true 的时候会造成消息重复消费
            channel.basicNack(envelope.getDeliveryTag(), true, false);
        } else {
            // 手工应答
            // 如果不应答，队列中的消息会一直存在，重新连接的时候会重复消费
            channel.basicAck(envelope.getDeliveryTag(), true);
        }
    }
};

// 开始获取消息，注意这里开启了手工应答
// String queue, boolean autoAck, Consumer callback
channel.basicConsume(QUEUE_NAME, false, consumer);
```

==自动ACK，消息发送给消费者以后就从队列中删除==

## 5. 消费者回调

消费者处理消息以后，可以再发送一条消息给生产者，或者调用生产者的API，告知消息处理完毕。 

参考:二代支付中异步通信的回执，多次交互。某提单APP，发送碎屏保消息后，消费者必须回调API。

业务库 

消息落库

1）生产者提供一个回调的API——耦合

2）消费者可以发送响应消息

## 6. 补偿机制（消息的重发或确认）

ATM存款——5次确认，ATM取款——5次冲正；结合定时任务对账

对于一定时间没有得到响应的消息，可以设置一个定时重发的机制，但要控制次数，比如最多重发3次，否则会造
成消息堆积

参考：ATM存款未得到应答时发送5次确认;ATM取款未得到应答时，发送5次冲正。根据业务表状态做一个重发

## 7. 消息幂等性

一次和多次请求某一个资源**对于资源本身**应该具有同样的结果

服务端是没有这种控制的，只能在消费端控制

如何避免消息的重复消费?

消息重复可能会有两个原因:

1、生产者的问题，环节1重复发送消息，比如在开启了Confirm模式但未收到确认。

2、环节4出了问题，由于消费者未发送ACK或者其他原因，消息重复投递。

对于重复发送的消息，可以对每一条消息生成一个唯一的业务ID，通过日志或者建表来做重复控制

参考:银行的重账控制环节。

## 8. 消息的顺序性

消息的顺序性指的是消费者消费的顺序跟生产者产生消息的顺序是一致的。

在RabbitMQ中，一个队列有多个消费者时，由于不同的消费者消费消息的速度是不一样的，顺序无法保证。

参考:消息:1、新增门店 2、绑定产品 3、激活门店，这种情况下消息消费顺序不能颠倒。

一个队列只有一个消费者的情况下，才能保证顺序

# 高可用架构部署方案

![image-20190105203622226](/Users/dingyuanjie/Desktop/MyKnowledge/2.code/java/2.咕泡学院/02.分布式专题/06.分布式消息通信/image-20190105203622226-6691782.png)

* Rabbit模式：
  * 单一模式：非集群模式
  * 普通模式：默认的集群模式
    * 对于Queue来说，消息实体只存在于其中一个节点，A、B两个节点仅有相同的元数据，即队列结构
    * 当消息进入A节点的Queue中后，consumer从B节点拉取时，RabbitMQ会临时在A、B间进行消息传输，把A中的消息实体取出并经过B发送给consumer
    * 所以consumer应尽量连接每一个节点，从中取消息。即对于同一个逻辑队列，要在多个节点建立物理Queue。否则无论consumer连A或B，出口总在A，会产生瓶颈
    * 存在一个问题就是当A节点故障后，B节点无法取到A节点中还未消费的消息实体
    * 如果做了消息持久化，那么得等A节点恢复，然后才可被消费；如果没有持久化的话，然后就没有然后了……
  * 镜像模式：把需要的队列做成镜像队列，存在于多个节点，属于RabbitMQ的HA方案
    * 其实质和普通模式不同之处在于，消息实体会主动在镜像节点间同步，而不是在consumer取数据时临时拉取
    * 副作用也很明显，除了降低系统性能外，如果镜像队列数量过多，加之大量的消息进入，集群内部的网络带宽将会被这种同步通讯大大消耗掉
    * 所以在对可靠性要求较高的场合中适用

## 集群

* 集群主要用于实现高可用与负载均衡。

* RabbitMQ通过/var/lib/rabbitmq/.erlang.cookie来验证身份，需要在所有节点上保持一致。

* 集群有两种节点类型，一种是磁盘节点，一种是内存节点
  * 内存节点：只保存状态到内存（一个例外的情况是：持久的queue的持久内容将被保存到disk）
  * 磁盘节点：保存状态到内存和磁盘
  * 集群中至少需要一个磁盘节点以实现元数据的持久化，未指定类型的情况下，默认为磁盘节点。

* 集群通过25672端口两两通信，需要开放防火墙的端口。

* 需要注意的是，RabbitMQ集群无法搭建在广域网上，除非使用federation或者shovel等插件。

* 集群的配置步骤：==配置hosts、同步erlang.cookie、加入集群==

1. queue、panyuntao1、panyuntao2做为RabbitMQ集群节点，分别安装RabbitMq-Server ，安装后分别启动RabbitMq-server，分别安装RabbitMq-Server ，安装后分别启动RabbitMq-server

2. 分别修改/etc/hosts文件，指定节点服务器的hosts，如`172.16.3.32 queue`、`172.16.3.107 panyuntao1`、`172.16.3.108 panyuntao2`

   * 请注意RabbitMQ集群节点必须在同一个网段里，如果是跨广域网效果就差

3. 设置每个节点Cookie

   * RabbitMQ的集群是依赖于erlang的集群来工作的，所以必须先构建起erlang的集群环境。
   * Erlang的集群中各节点是通过一个magic cookie来实现的，这个cookie存放在 /var/lib/rabbitmq/.erlang.cookie 中，文件是400的权限。所以必须保证各节点cookie保持一致，否则节点之间就无法通信
   * 将其中一台节点上的`.erlang.cookie`值复制下来保存到其他节点上。或者使用scp的方法也可，但是要注意文件的权限和属主属组
     1. 将queue中的cookie 复制到 panyuntao1、panyuntao2中，先修改下panyuntao1、panyuntao2中的.erlang.cookie权限`#chmod 777  /var/lib/rabbitmq/.erlang.cookie `
     2. 将queue的/var/lib/rabbitmq/.erlang.cookie这个文件，拷贝到panyuntao1、panyuntao2的同一位置（反过来亦可），该文件是集群节点进行通信的验证密钥，所有节点必须一致。拷完后重启下RabbitMQ
     3. 复制好后别忘记还原.erlang.cookie的权限，否则可能会遇到错误`#chmod 400 /var/lib/rabbitmq/.erlang.cookie `
     4. 设置好cookie后先将三个节点的RabbitMQ重启

4. 停止所有节点RabbitMQ服务，然后使用detached参数独立运行，这步很关键，尤其增加节点停止节点后再次启动遇到无法启动都可以参照这个顺序

   ```shell
   queue]#  rabbitmqctl stop
   panyuntao1]# rabbitmqctl stop
   panyuntao2]# rabbitmqctl stop
   queue]#  rabbitmq-server -detached
   panyuntao1]# rabbitmq-server -detached
   panyuntao2]# rabbitmq-server -detached
   # 分别查看下每个节点
   rabbitmqctl cluster_status
   ```

5. 将panyuntao1、panyuntao2作为内存节点与queue连接起来，在panyuntao1上，执行如下命令：

   ```shell
   panyuntao1]# rabbitmqctl stop_app
   panyuntao1]# rabbitmqctl join_cluster --ram rabbit@queue
   panyuntao1]# rabbitmqctl start_app
   panyuntao2]# rabbitmqctl stop_app
   panyuntao2]# rabbitmqctl join_cluster --ram rabbit@queue
   panyuntao2]# rabbitmqctl start_app
   ```

   * (上方已经将panyuntao1与queue连接，也可以直接将panyuntao2与panyuntao1连接，同样而已加入集群中)
   * 上述命令先停掉RabbitMQ应用，然后调用cluster命令，将panyuntao1连接到，使两者成为一个集群，最后重启RabbitMQ应用。在这个cluster命令下，panyuntao1、panyuntao2是内存节点，queue是磁盘节点（RabbitMQ启动后，默认是磁盘节点
   * queue 如果要使panyuntao1或panyuntao2在集群里也是磁盘节点，join_cluster 命令去掉--ram参数即可`#rabbitmqctl join_cluster rabbit@queue  `
   * 只要在节点列表里包含了自己，它就成为一个磁盘节点。在RabbitMQ集群里，必须至少有一个磁盘节点存在。

6. 在queue、panyuntao1、panyuntao2上，运行cluster_status命令查看集群状态：

   * `rabbitmqctl cluster_status`看到每个节点的集群信息，分别有两个内存节点一个磁盘节点

7. 往任意一台集群节点里写入消息队列，会复制到另一个节点上，可以看到两个节点的消息队列数一致：

   * `rabbitmqctl list_queues -p hrsystem`
   * -p参数为vhost名称

8. 这样RabbitMQ集群就正常工作了

   *  这种模式更适合非持久化队列，只有该队列是非持久的，客户端才能重新连接到集群里的其他节点，并重新创建队列。假如该队列是持久化的，那么唯一办法是将故障节点恢复起来

* 数据一致性和高性能 LAN，WAN

1、通信基础

​    erlang.cookie   、  hosts  

2、磁盘节点与内存节点

3、配置步骤

​    join cluster 

## 镜像队列

* RabbitMQ默认集群模式，但并不保证队列的高可用性，尽管交换机、绑定这些可以复制到集群里的任何一个节点，但是队列内容不会复制

* 虽然该模式解决一部分节点压力，但队列节点宕机直接导致该队列无法使用，只能等待重启，所以要想在队列节点宕机或故障也能正常使用，就要复制队列内容到集群里的每个节点，需要创建镜像队列

* 集群方式下，队列和消息是无法在节点之间同步的，因此需要使用RabbitMQ的镜像队列机制进行同步。

* 配置步骤

  1. 增加负载均衡器

     * LVS是一个内核层的产品，主要在第四层负责数据包转发，使用较复杂。HAProxy和Nginx是应用层的产品，但Nginx主要用于处理HTTP，所以这里选择HAProxy作为RabbitMQ前端的LB

     * HAProxy的安装：Centos下直接yum install haproxy、更改/etc/haproxy/haproxy.cfg

       ![image-20190301112901282](/Users/dingyuanjie/Desktop/mynotes/MD/分布式/image-20190301112901282.png)

     * 负载均衡器会监听5672端口，轮询我们的两个内存节点172.16.3.107、172.16.3.108的5672端口，172.16.3.32为磁盘节点，只做备份不提供给生产者、消费者使用，当然如果服务器资源充足情况也可以配置多个磁盘节点，这样磁盘节点除了故障也不会影响，除非同时出故障

  2. 配置策略

     * 使用Rabbit镜像功能，需要基于RabbitMQ策略来实现，政策是用来控制和修改群集范围的某个vhost队列行为和Exchange行为
     * 在cluster中任意节点启用策略，策略会自动同步到集群节点
     * `# rabbitmqctl set_policy -p hrsystem ha-allqueue"^" '{"ha-mode":"all"}'`，这行命令在vhost名称为hrsystem创建了一个策略，策略名称为ha-allqueue,策略模式为 all 即复制到所有节点，包含新增节点，策略正则表达式为 “^” 表示所有匹配所有队列名称
     * `例如rabbitmqctl set_policy -p hrsystem ha-allqueue "^message" '{"ha-mode":"all"}'`
     * 注意："^message" 这个规则要根据自己修改，这个是指同步"message"开头的队列名称，我们配置时使用的应用于所有队列，所以表达式为"^"
     * `官方set_policy说明`:`set_policy [-p vhostpath] {name} {pattern} {definition} [priority]`
     * `ha-mode`：`all`、`exactly`、`nodes`

  3. 创建队列时需要指定ha 参数，如果不指定x-ha-prolicy 的话将无法复制

  4. 客户端使用负载服务器172.16.3.110 （panyuntao3）发送消息，队列会被复制到所有节点，当然策略也可以配置制定某几个节点，这时任何节点故障 、或者重启将不会影响我们正常使用某个队列。到这里我们完成了高可用配置（所有节点都宕机那没有办法了）

| 操作方式              | 命令或步骤                                                   |
| --------------------- | ------------------------------------------------------------ |
| rabbitmqctl (Windows) | rabbitmqctl set_policy ha-all "^ha." "{""ha-mode"":""all""}" |
| HTTP API              | PUT /api/policies/%2f/ha-all {"pattern":"^ha.", "definition":{"ha-mode":"all"}} |
| Web UI                | Navigate to Admin > Policies > Add / update a policy<br/>Name输入:mirror_image Pattern输入:^(代表匹配所有) Definition点击 HAmode，右边输入:all |
![image-20190105201912794](/Users/dingyuanjie/Desktop/MyKnowledge/2.code/java/2.咕泡学院/02.分布式专题/06.分布式消息通信/image-20190105201912794-6690752.png)

https://blog.csdn.net/u013256816/article/details/71097186

## HAproxy负载+Keepalived高可用方案

```shell
# 在两个内存节点上安装HAProxy
yum install haproxy
# 编辑配置文件
vim /etc/haproxy/haproxy.cfg
```

```shell
# 内容修改为：
global
	log 			127.0.0.1 local2
	chroot 			/var/lib/haproxy
	pidfile 		/var/run/haproxy.pid
	maxconn 		4000
	user 			haproxy
	group 			haproxy
	daemon
	stats 			socket /var/lib/haproxy/stats
defaults
	log 			global
	option 			dontlognull
	option 			redispatch
	retries 		3
	timeout connect 10
	stimeout client 1m
	timeout server 	1m
	maxconn 		3000
listen http_front
	mode 			http
	bind 			0.0.0.0:1080 #监听端口
	stats 			refresh 30s #统计页面自动刷新时间
	stats uri 		/haproxy?stats #统计页面
	urlstats realm 	Haproxy Manager #统计页面密码框上提示文本
	stats auth 		admin:123456 #统计页面用户名和密码设置
listen rabbitmq_admin
	bind 			0.0.0.0:15673
	server node1 	192.168.8.40:15672
	server node2 	192.168.8.45:15672
listen rabbitmq_cluster 0.0.0.0:5673
	mode 			tcp
	balance 		roundrobin
	timeout client 	3h
	timeout server 	3h
	timeout connect 3h
	server node1 192.168.8.40:5672 check inter 5s rise 2 fall 3
	server node2 192.168.8.45:5672 check inter 5s rise 2 fall 3
```

```shell
# 启动HAProxy
haproxy -f /etc/haproxy/haproxy.cfg
# 安装Keepalived
yum -y install keepalived
# 修改配置文件
vim /etc/keepalived/keepalived.conf
```

```shell
# 内容改成（物理网卡和当前主机IP要修改）：
global_defs {
   notification_email {
	acassen@firewall.loc 
	failover@firewall.loc 
	sysadmin@firewall.loc
   }
   notification_email_from Alexandre.Cassen@firewall.loc 
   smtp_server 192.168.200.1
   smtp_connect_timeout 30
   router_id LVS_DEVEL
   vrrp_skip_check_adv_addr
   # vrrp_strict # 注释掉，不然访问不到VIP 
   vrrp_garp_interval 0
   vrrp_gna_interval 0
}
global_defs {
	notification_email { 
		acassen@firewall.loc 
		failover@firewall.loc 
		sysadmin@firewall.loc
	}
	notification_email_from Alexandre.Cassen@firewall.loc 
	smtp_server 192.168.200.1
	smtp_connect_timeout 30
	router_id LVS_DEVEL
	vrrp_skip_check_adv_addr
	# vrrp_strict # 注释掉，不然访问不到VIP 
	vrrp_garp_interval 0
	vrrp_gna_interval 0
}
# 检测任务
vrrp_script check_haproxy {
	# 检测HAProxy监本
	script "/etc/keepalived/script/check_haproxy.sh" 
	# 每隔两秒检测
	interval 2
	# 权重
	weight 2
}
# 虚拟组
vrrp_instance haproxy {
	state MASTER # 此处为`主`，备机是 `BACKUP`
 	interface ens33 # 物理网卡，根据情况而定 
 	mcast_src_ip 192.168.8.40 # 当前主机ip 
 	virtual_router_id 51 # 虚拟路由id，同一个组内需要相同 
 	priority 100 # 主机的优先权要比备机高
	advert_int 1 # 心跳检查频率，单位:秒 
	authentication { # 认证，组内的要相同
        auth_type PASS
        auth_pass 1111
    }
	# 调用脚本 
	track_script {
        check_haproxy
    }
	# 虚拟ip，多个换行 
	virtual_ipaddress {
        192.168.8.201
    }
}
```

```shell
# 启动keepalived
keepalived -D
```



![image-20190105201930802](/Users/dingyuanjie/Desktop/MyKnowledge/2.code/java/2.咕泡学院/02.分布式专题/06.分布式消息通信/image-20190105201930802-6690770.png)

## 网络分区

为什么会出现分区?因为RabbitMQ对网络延迟非常敏感，为了保证数据一致性和性能，在出现网络故障时，集群
节点会出现分区。

参考脑图.xmind

[RabbitMQ Network Partitions](https://blog.csdn.net/u013256816/article/details/53588206)

[RabbitMQ Network Partitions 处理策略](https://blog.csdn.net/u013256816/article/details/73757884)

[模拟RabbitMQ网络分区](https://blog.csdn.net/u013256816/article/details/74998896)

## 广域网的同步方案

1. federation插件
2. shovel插件

# 实践经验总结

1、配置文件与命名规范 

​	集中放在properties文件中

​	体现元数据类型(_VHOST _EXCHANGE _QUEUE);

​	体现数据来源和去向(XXX_TO_XXX);

2、调用封装 

​	可以对Template做进一步封装，简化消息的发送。

3、信息落库（可追溯，可重发） + 定时任务（效率降低，占用磁盘空间）

​	将需要发送的消息保存在数据库中，可以实现消息的可追溯和重复控制，需要配合定时任务来实现。

4、如何减少连接数  4M

​	合并消息的发送，建议单条消息不要超过4M(4096KB)

5、生产者先发送消息还是先登记业务表？

​	先登记业务表，再发送消息

6、谁来创建对象（交换机、队列、绑定关系）？

​	消费者创建

7、运维监控  zabbix 

​	[zabbix系列zabbix3.4监控rabbitmq](http://blog.51cto.com/yanconggod/2069376)

8、其他插件

​	tracing

​	[https://www.rabbitmq.com/plugins.html](https://www.rabbitmq.com/plugins.html)

```shell
rabbitmq-plugins list
```


