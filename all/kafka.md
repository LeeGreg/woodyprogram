# Kafka

## 简介

- 什么是kafka？

  - 是一款基于发布与订阅的消息系统，具有高性能、高吞吐量的特点而被广泛应用于大数据传输场景
    
    - 无缝支持多个生产者，都以相同的消息格式向某主题写入数据
    - 支持多个消费者从一个单独的消息流上读取数据且互不影响，多个消费者可组成一个群组来共享一个消息流，并保证整个群组对每个给定的消息只处理一次
    - 使用推送和拉取模型解耦生产者和消费者
      - `Producer`使用`push`模式将消息发布到`broker`，`consumer`通过监听使用`pull`模式从`broker`订阅并消费消息
    - 基于磁盘的数据存储，以便支持多个消费者，每个主题可单独设置保留规则，支待实时数据处理和离线数据处理
    - 可伸缩，支持水平扩展，一个包含多个 broker的集群，即使个别 broker失效，仍可持续为客户提供服务
    - 高性能，横向扩展生产者、消费者和 broker, Kafka可以轻松处理巨大的消息流
    
    - 跨平台 : 支待不同技术平台的客户端(如Java、 PHP、 Python 等)
      - Kafka还提供了二进制连接协议，直接向 Kafka 网络端口发送适当的字节序列，就可实现从Kafka读取消息或往 Kafka 写入消息

- 能干什么？
  - 由于 kafka 具有更好的吞吐量、内置分区、冗余及容错性的优点(kafka 每秒可以处理几十万消息)，让 kafka 成为了一个很好的大规模消息处理应用的解决方案 

    1. 行为跟踪：跟踪用户浏览页面、搜索及其他行为。通过订阅-发布模式实时记录到对应的 topic 中，通过后端大数据平台接入处理分析，并做更进一步的实时处理和监控 
2. 收集应用程序和系统度量指标以及日志，应用程序定期把度量指标发布到 Kafka 主题上，监控系统或告警系统读取这些消息。日志消息可被发布到 Kafka主题上，然后被路由到专门的日志搜索系统或安全分析应用程序
    3. 传递消息
    4. 提交日志，把数据库的更新发布到 Kafka上，应用程序通过监控事件流来接收数据库的实时更新
    5. 流处理，通过使用流式处理框架，可编写小型应用程序来操作 Kafka 消息，比如计算度量指标，为其他应用程序有效地处理消息分区，或者对来自多个数据源的消息进行转换

## Kafka 本身的架构

* 基本概念 
  * `zookeeper`：`kafka` 通过`zookeeper` 管理集群配置及服务协同
    * Kafka使用 Zookeeper来保存 broker、主题和分区的元数据信息
    * 最新版本的 Kafka，让消费者把偏移量提交到 Kafka 服务器上，消除对 Zookeeper的依赖 
    * 使用的是一致性协议，只有当zookeeper群组里的大多数节点(二分之一以上)处于可用状态， Zookeeper 才能处理外部的请求
    * 多个 `broker` 协同工作，`producer` 和 `consumer` 部署在各个业务逻辑中，三者通过`zookeeper`管理协调请求和转发。这样就组成了一个高性能的分布式消息发布和订阅系统
  * `Broker`：一个独立的Kafka服务器，接收来自生产者的消息，为消息设置偏移量，并提交到磁盘保存。 为消费者提供服务，对读取分区的请求作出响应，返回已经提交到磁盘上的消息
    - 每个集群都有一个 broker 同时充当了集群控制器的角色(自动从集群的活跃成员中选举出来)，负责将分区分配给 broker和监控broker
    - 一个包含多个 broker的集群，即使个别 broker失效，仍然可以持续地为客户提供服务
  * `Topic`和`Parttion`：消息通过主题（逻辑概念）进行分类，主题可以被分为若干个分区（物理概念），一个分区就是一个提交日志（对应一个文件夹(存储对应分区的消息内容和索引文件)），消息以追加的方式写入分区（只能保证消息在单个分区内的顺序），然后以先入先出的顺序读取，通过分区来实现数据冗余和伸缩性，分区可分布在不同的服务器上
    * Partition 是以文件的形式存储在文件系统中，比如创建一个名为 firstTopic 的 topic，其中有 3 个 partition，那么在kafka 的数据目录(/tmp/kafka-log)中就有 3 个目录，firstTopic-0~3， 命名规则是`<topic_name>-<partition_id>`
  * `Producer`：消息生产者，负责生产消息并发送到`Kafka Broker` ，可以无缝地支持多个生产者，都以相同的消息格式向某主题写入数据
  * `Consumer`：消息消费者，向`Kafka Broker`读取消息并处理的客户端，支持多个消费者从一个单独的消息流上读取数据，且互不影响
  * `ConsumerGroup`：消费者从属于消费者群组，一个群组里的消费者订阅的是同一个主题，每个消费者接收主题一部分分区的消息
    * 为每一个需要获取一个或多个主题全部消息的应用程序创建一个消费者群组，然后往群组里添加消费者来伸缩读取能力和处理能力，群组里的每个消费者只处理一部分消息。
  * 消息：为键生成一个一致性散列值来对主题分区数进行取模，为消息选取分区，这样可保证具有相同键的消息总是被写到相同的分区上
    - broker默认的消息保留策略（主题）：默认7天或消息达到一定大小字节数（旧的删除）
    - Kafka提供MirrorMaker的工具来实现集群间的消息复制。包含了一个生产者和一个消费者，两者之间通过一个队列相连。消费者从一个集群读取消息，生产者把消息发送到另一个集群上
  * 批次：属于同一个主题和分区的消息被分批次写入Kafka以减少网络开销来提高效率
  * 偏移量：一个不断递增的整数值，在创建消息时， Kafka 会把它添加到消息里，分区里每个消息的偏移量都是唯一的，kafka 通过 offset保证消息在同一个分区内的顺序，消费者把每个分区最后读取的消息偏移量保存在 Zookeeper或 Kafka上，如果消费者关闭或重启，它的读取状态不会丢失
  * Avro序列化器

## 安装部署

- 下载、解压安装包

- 启动/停止

  1. 需要先启动`zookeeper`，如果没有搭建`zookeeper`环境，可以直接运行`kafka`内嵌的`zookeeper`

     启动命令: `bin/zookeeper-server-start.sh config/zookeeper.properties &`

  2. 进入 kafka 目录 `bin/kafka-server-start.sh {-daemon 后台启动} config/server.properties &`

  3. 进入 kafka 目录 `bin/kafka-server-stop.sh config/server.properties`

### 安装集群环境

* 可跨服务器进行负载均衡，可使用复制功能来避免因单点故障造成的数据丢失
* 一个 broker加入到集群里，所有 broker都必须配置相同的 zookeeper.connect，每个 broker都必须为 broker.id参数设置唯一的值

* zookeeper

  ```properties
  tickTime=2000
  dataDir=/var/lib/zookeeper
  # 客户端只需要通过 clientPort就能连接到群组
  clientPort=2181
  # 用于在从节点与主节点之间建立初始化连接的时间上限，tickTime的倍数,20*2000
  initLimit=20
  # 允许从节点与主节点处于不同步状态的时间上限，tickTime的倍数,20*2000
  syncLimit=5
  # server.X=hostname:peerPort:leaderPort
  # 服务器的ID整数、服务器的机器名或IP地址、节点间通信的TCP端口、首领选举的TCP端口
  server.1=zoo1.example.com:2888:3888
  server.2=zoo2.example.com:2888:3888
  server.3=zoo3.example.com:2888:3888
  ```

- 修改`server.properties` 配置

  1. 修改 `server.properties.broker.id=0 / 1`

  2. 修改 `server.properties` 修改成本机 IP

     `advertised.listeners=PLAINTEXT://192.168.11.153:9092`

     当`Kafka broker`启动时，它会在 ZK 上注册自己的 IP 和端口号，客户端就通过这个 IP 和端口号来连接

## 基本操作

```shell
# 创建topic
bin/kafka-topics.sh --create --zookeeper localhost:2181 --replication-factor 1 --partitions 1 --topic test
# Replication-factor 表示该 topic 需要在不同的 broker 中保存几份，这里设置成 1，表示在两个 broker 中保存两份
# Partitions 分区数
bin/kafka-topics.sh --zookeeper localhost:2181 --describe --topic test

# 查看topic
bin/kafka-topics.sh --list --zookeeper localhost:2181

# 查看topic属性
./kafka-topics.sh --describe --zookeeper localhost:2181 --topic test

# 消费消息
./kafka-console-consumer.sh –bootstrap-server localhost:9092 --topic test --from-beginning

# 发送消息
bin/kafka-console-producer.sh --broker-list localhost:9092 --topic test
# 读取消息
bin/kafka-console-consumer.sh --zookeeper localhost:2181 --topic test --from-beginning
```

* broker配置

  ```java
  broker.id，broker标识符，默认0，任意整数，集群里唯一
  prot，默认监听9092端口
  zookeeper.connect，用于保存 broker元数据的zookeeper地址，hostname:port/path
  log.dirs，所有消息都保存在磁盘上，存放这些日志片段的目录是通过log.dirs指定，多个路径逗号分隔
  num.recovery.threads.per.data.dir，默认每个日志目录只使用一个线程，线程数=..dir * log.dirs
  auto.create.topics.enable，自动创建主题
  ```

* 主题配置

  ```java
  num.partitions，默认1，新创建的主题将包含多少个分区，可以用主题吞吐量除以消费者吞吐量算出分区的个数
  log.retention.ms，根据时间来决定数据可以被保留多久，通过检查磁盘上日志片段文件的最后修改时间来实现，即日志片段的关闭时间，也就是文件里最后一个消息的时间戳
  log.retention.bytes,作用在每一个分区上,通过保留的消息字节数来判断消息是否过期
  如果同时指定了log.retention.bytes和log.retention.ms 只要任意一个条件得到满足，消息就会被删除
  log.segment.bytes,默认1GB，达到日志片段上限后，当前日志片段关闭等待过期，然后开启新的日志片段
  log.segment.ms,多长时间之后日志片段关闭，默认没有设定值
  message.max.bytes,限制单个消息大小（压缩后），默认1MB，消费者客户端设置的fetch.message.max.bytes需大于该值
  ```

* 内存
  * 磁盘性能影响生产者 ，而内存影响消费者 
  * 消费者一般从分区尾部读取消息，如果有生产者存在，就紧跟在生产者后面。在这种情况下，消费者读取的消息会直接存放在系统的页面缓存里，这比从磁盘上重新读取要快得多 
  * 不建议把 Kafka 同其他重要的应用程序部署在一起的原因，它们需要共享页面缓存，最终会降低 Kafka 消费者的性能
* 网络
  
  * 网络吞吐量决定了 Kafka 能够处理的最大数据流量。它和磁盘存储是制约 Kafka 扩展规模的主要因素。 
* CPU
  
  * 客户端为了优化网络和磁盘空间，会对消息进行压缩 。 服务器需要对消息进行批量解压，设置偏移量，然后重新进行批量压缩，再保存到磁盘上

### Spring Kafka集成

## 配置信息分析

### 发送端的可选配置信息分析

- `acks`，表示 `producer` 发送消息到`broker`上以后的确认值
  - 0：表示producer不需要等待broker的消息确认。延时最小但风险最大(当 server 宕机时，数据将会丢失)
  - 1：表示producer只需获得集群中leader节点确认即可，延时较小同时确保了 leader 节点确认接收成功
  - all(-1)：需要ISR中所有的Replica给予接收确认，速度最慢，安全性最高，但是由于 ISR 可能会缩小到仅包含一个 Replica，所以设置参数为 all 并不能一定避免数据丢失   
- `batch.size`，生产者批量发送消息到`broker` 上的同一个分区，默认大小`16kb`
- `linger.ms`，每次发送到 broker 的请求增加一些延时来聚合更多的 Message 请求
  - batch.size和linger.ms这两个参数是kafka性能优化的关键参数，这两者的作用是一样的，当二者都配置的时候，只要满足其中一个要求，就会发送请求到 broker 上 
- `max.request.size`，请求的数据的最大字节数，为防止发生较大的数据包影响到吞吐量，默认值为 1MB
- `buffer.memeory`：生产者内缓冲区的大小
- `client.id`：可任意字符串，服务器用其标识消息的来源
- `compression.type`：默认消息发送时不会被压缩，压缩算法，snappy关注性能和网络宽带，gzip占用CPU但更高压缩比
- `retries`：生产者收到服务器错误后重发消息的次数，超过后返回错误，默认每次重试之间等待100ms
- `max.in.flight.request.per.connection`：生产者在收到服务器晌应之前可以发送多少个消息
- `timeout.ms`： broker 等待同步副本返回消息确认的时间
- `request.timeout.ms`：生产者在发送数据时等待服务器返回响应的时间
- `metadata.fetch.timeout.ms`：生产者在获取元数据(比如目标分区的首领是谁)时等待服务器返回响应的时间
- `max.block.ms`：在调用 send() 方法或使用 partitionsFor() 方能获取元数据时生产者的阻塞时间

```java
// 创建一个ProducerRecord对象包含目标主题和要发送的内容,还可指定键或分区
// 发送ProducerRecord对象时，生产者要先把键和值对象序列化成字节数组在网络上传输 
// 数据被传给分区器,如果在ProducerRecord对象里指定了分区则直接返回，否则分区器根据键选择一个分区
// 记录被添加到一个记录批次里，一个独立线程负责将这些记录发送到相同的主题和分区上
// 服务器在收到这些消息时会返回一个响应，成功写入Kafka，就返回一个RecordMetaData对象包含了主题和分区信息，以及记录在分区里的偏移量。如果写入失败，则会返回一个错误。生产者在收到错误之后会尝试重新发送消息，几次之后如果还是失败，就返回错误信息
//bootstrap.servers,指定 broker 的地址清单host:port
//key.serializer,默认提供了 ByteArraySerializer、StringSerializer和IntegerSerializer
//value.serializer
private Properties kafkaProps = new Properties();
kafkaProps.put("bootstrap.servers","broker1:9092,broker2:9092");
kafkaProps.put("key.serializer","org.apache.kafka.common.serialization.StringSerializer");
kafkaProps.put("value.serializer","org.apache.kafka.common.serialization.StringSerializer");
producer = new KafkaProducer<String, String>(kafkaProps);
// 目标主题、键、值
PorducerRecord<String, String> record = new ProducerRecord<>("CustomerCountry","Precision Products", "France");
try {
  // 消息先是被放进缓冲区，然后使用单独的线程发送到服务器端
  // send()方法会返回一个包含RecordMetadata的Future对象
  //producer.send(record);
  // 同步发送如果服务器返回错误，get()方怯会抛出异常。如果没有发生错误，得到可获取消息的偏移量的RecordMetadata对象
  // producer.send(record).get(); 
  // 异步发送
  producer.send(record, new DemoProducerCallback());
} catch(Exception e) {
  // 一般会发生两类错误。可重试错误，可通过重发消息来解决；另一类错误无出通过重试解决，直接抛出异常
  e.printStackTrace();
}
// 异步发送回调
private class DemoProducerCallback implements Callback {
  @Override
  public void onCompletion(RecordMetadata recordMetadata, Exception e) {
    if(e != null) {
      e.printStackTrace();
    }
  }
}
```

### 消费端的可选配置分析

- `group.id`，组内多个消费者共享一个公共的`group ID`，一起消费订阅主题的所有分区，每个分区只能由一个消费者来消费
  * `consumer group` 是 `kafka` 提供的可扩展且具有容错性的消费者机制
- `enable.auto.commit`，消费消息以后自动提交，该消息才不会被再次接收到，可配合 `auto.commit.interval.ms` 控制自动提交的频率
  * 设置为false，然后通过`consumer.commitSync()`的方式实现手动提交
- `auto.offset.reset`
  - `auto.offset.reset=latest` ，新消费者将会从其他消费者最后消费的offset 处开始消费 Topic 下的消息
  - `auto.offset.reset= earliest` ，新的消费者会从该 topic 最早的消息开始消费
  - `auto.offset.reset=none` 情况下，新的消费者加入以后，由于之前不存在offset，则会直接抛出异常
- `max.poll.records`，限制每次调用`poll` 返回的消息数
- `fetch.min.bytes`：消费者从服务器获取记录的最小字节数
- `fetch.max.wait.ms`：指定 broker的等待时间，默认是 500ms
- `max.partition.fectch.bytes`：服务器从每个分区里返回给消费者的最大字节数。默认值是 lMB
- `session.timeout.ms`：消费者可以多久不发送心跳，默认3秒
- `heartbeat.interval.ms`：指定了poll()方法向协调器发送心跳的频率，默认1秒
- `auto.offset.reset`：消费者在读取一个没有偏移量的分区或者偏移量无效的情况下该作何处理，默认latest，在偏移量无效的情况下，消费者将从最新的记录开始读取数据(在消费者启动之后生成的记录)；earliest，在偏移量无效的情况下，消费者将从起始位置读取分区的记录
- `enable.auto.commit`：消费者是否自动提交偏移量，默认 true
- `partition.assignment.strategy`：消费者分区分配策略，默认Range，还有RoundRobin
- `client.id`：可任意字符串，broker用它来标识从客户端发送过来的消息

```java
private Properties props = new Properties();
props.put("bootstrap.servers","broker1:9092,broker2:9092");
props.put("group.id","CountryCounter");      //不必须
props.put("key.serializer","org.apache.kafka.common.serialization.StringDeserializer");
props.put("value.serializer","org.apache.kafka.common.serialization.StringDeserializer");
KafkaConsumer<String, String> consumer = new KafkaConsumer<String, String>(props);
// 订阅主题，可传入正则表达式consumer.subscribe("test.*");
consumer.subscribe(Collections.singletonList("customerCountries"));
// 轮询向服务器请求数据，轮询就会处理所有的细节，包括群组协调、分区再均衡、发送心跳和获取数据
try {
  while(true) {
    //消费者必须持续对Kafka进行轮询，否则会被认为己经死亡，它的分区会被移交给群组里的其他消费者
    //超时参数指定了方法在多久之后可以返回，不管有没有可用的数据都要返回，返回一个记录列表
    //每条记录都包含了记录所属主题、分区、分区里的偏移量，以及记录的键值对
    //第一次调用新消费者的poll()方法时，它会负责查找GroupCoordinator，然后加入群组，接受分配的分区
    // 按照规则， 一个消费者使用一个线程
    ConsumerRecords<String, String> records = consumer.poll(100);
    for(ConsumerRecord<String, String> record : records) {
      log.debug("topic = %s, partition = %s, offset = %d, customer = %s, country = %s\n", record.topic(), record.partition(), record.offset(), record.key(), record.value());
      int updateCount = 1;
      if(custCountryMap.countainsValue(record.value())) {
        updatedCount = custCountryMap.get(record.value()) + 1;
      }
      custCountryMap.put(record.value(), updatedCount);
      JSONObject json = new JSONObject(custCountryMap);
      System.out.println(json.toString());
      try {
        consumer.commitSync();
      } catch(CommitFailedException e) {
        log.error("commit failed", e);
      }
    }
  }
} finally {
  // 关闭消费者，网络连接和socket也会随之关闭，并立即触发一次再均衡
  consumer.close();
}
```

### 如何退出

* 如果确定要退出循环，需要通过另一个线程调用consumer.wakeup()方法可退出poll()，并抛出WakeupException异常（不需处理）
* 在退出线程之前调用 consumer.close()会提交任何还没有提交的东西，并向群组协调器发送消息，告知自己要离开群组，接下来就会触发再均衡 ，而不需要等待会话超时

![image-20181218141750289](/Users/dingyuanjie/Desktop/MyKnowledge/2.code/java/2.%E5%92%95%E6%B3%A1%E5%AD%A6%E9%99%A2/02.%E5%88%86%E5%B8%83%E5%BC%8F%E4%B8%93%E9%A2%98/06.%E5%88%86%E5%B8%83%E5%BC%8F%E6%B6%88%E6%81%AF%E9%80%9A%E4%BF%A1/image-20181218141750289-5113870.png)

## 消息分发策略

* 在 kafka 中，一条消息由 key、value 两部分构成
  * 发送消息时可指定 key，那么 producer 会根据 key 和 partition 机制来判断当前这条消息应该发送并存储到哪个 partition 中。
    * 可以根据需要进行扩展 producer 的 partition 机制

- 消息默认的分发机制

  - 默认情况下，kafka 采用的是 hash 取模的分区算法
  - 为键生成一个一致性散列值来对主题分区数进行取模，为消息选取分区
  
  * 如果Key 为 null，则被随机地发送到主题内各个可用的分区上
  - 自定义分区，implement Partitioner
  * Topic/Partition 和 broker 的映射关系
    * 每一个 topic 的每一个 partition，需要知道对应的 broker 列表是什么，leader是谁、follower 是谁。这些信息都是存储在 Metadata 这个Metadata类里

## 消息消费原理

在实际生产过程中，每个 topic 都会有多个 partitions，多个 partitions 的好处在于：

1. 能够对 broker 上的数据进行分片有效减少了消息的容量从而提升 io 性能
2. 为了提高消费端的消费能力，一般会通过多个consumer 去消费同一个 topic ，也就是消费端的负载均衡机制

- 在多个 partition 以及多个 consumer 的情况下，消费者是如何消费消息的

  - 分区分配策略：通过partition.assignment.strategy 这个参数来设置

  1. Range(默认)范围分区分配策略

     * 将同一个topic中的 partitions 的个数除于消费者线程的总数来决定每个消费者线程消费几个分区，如果除不尽，那么前面几个消费者线程将会多消费一个分区
     * 如消费者C1、C2同时订阅主题T1和T2，两个主题各有3个分区，则C1可能分配T1的0和1、T2的0和1分区，C2分配到T1的2、T2的2分区
     * 弊端：某些消费者线程比其他消费者线程多消费分区

  2. RoundRobin(轮询)分区分配策略

     * 把所有 partition 和所有 consumer 线程都列出来，然后按照 hashcode 进行排序。最后通过轮询算法分配 partition 给消费线程。如果所有 consumer 实例的订阅是相同的，那么 partition 会均匀分布

     * 必须满足两个条件：
       * 每个主题的消费者实例具有相同数量的流
       * 每个消费者订阅的主题必须是相同的

     * 假如按照 hashCode 排序完的 topic- partitions 组依次为 T1-5, T1-3, T1-0, T1-8, T1-2, T1-1, T1-4, T1-7, T1-6, T1-9，消费者线程排序为 C1-0, C1-1, C2- 0, C2-1，最后分区分配的结果为:

       * C1-0 将消费 T1-5, T1-2, T1-6 分区;	C1-1 将消费 T1-3, T1-1, T1-9 分区; 

       * C2-0 将消费 T1-0, T1-4 分区;	C2-1 将消费 T1-8, T1-7 分区;  

- 什么时候会触发这个策略呢？

  - 当出现以下几种情况时，kafka 会进行一次分区分配操作，也就是 kafka consumer 的 rebalance
    1. 同一个 consumer group 内新增了消费者 
    2. 消费者离开当前所属的 consumer group，比如主动停机或者宕机 
       * 消费者会在轮询消息(为了获取消息)或提交偏移量时发送心跳。如果停止发送心跳的时间足够长，会话就会过期，群组协调器认为它已经死亡，就会触发一次再均衡
    3. topic 新增了分区

  * kafka consuemr 的 rebalance 机制规定了一个 consumer group 下的所有 consumer 如何达成一致来分配订阅 topic的每个分区。而具体如何执行分区策略，就是其两种内置的分区策略

- 谁来执行 Rebalance 以及管理 consumer 的 group 呢?

  * Kafka 提供了一个角色：coordinator 来执行对于 consumer group 的管理，当 consumer group 的第一个 consumer 启动的时候，它会去和 kafka server 确定谁是它们组的 coordinator。之后该 group 内的所有成员都会和该 coordinator 进行协调通信

- 如何确定 coordinator（consumer group 如何确定自己的 coordinator 是谁呢）

  - 消费者向 kafka 集群中的任意一个 broker 发送一个GroupCoordinatorRequest 请求，服务端会返回一个负载最小的 broker 节点的 id，并将该 broker 设置为coordinator


* Rebalance过程
  * 在 rebalance 之前，需保证 coordinator 已确定好
  * 第一步：Join Group
    * 所有成员都向coordinator发送JoinGroup请求，请求入组，coordinator会从中选择一个consumer担任leader的角色，并把组成员信息以及订阅信息发给leader，leader负责消费分配方案的制定
  * 第二步：Synchronizing Group
    * leader开始分配消费方案，即哪个consumer负责消费哪些topic的哪些partition
    * leader会将这个方案封装进SyncGroup请求中发给coordinator，非leader也会发SyncGroup请求，只是内容为空
    * coordinator接收到分配方案之后会把方案塞进SyncGroup的response中发给各个consumer
    * 这样组内的所有成员就都知道自己应该消费哪些分区了
  * consumer group的分区分配方案是在客户端执行的，可以有更好的灵活性
    * protocol_metadata：序列化后的消费者的订阅信息

    * leader_id：消费组中的消费者，coordinator 会选择一个作为 leader，对应的就是 member_id

    * member_metadata 对应消费者的订阅信息

    * members：consumer group 中全部的消费者的订阅信息

    * generation_id：年代信息，对于每一轮 rebalance，generation_id 都会递增，主要用来保护 consumer group。隔离无效的 offset 提交。即上一轮的 consumer 成员无法提交 offset 到新的 consumer group 中

- 如何保存消费端的消费位置

  - offset

    - 每个 Topic 至少有一个分区，同一topic 下的不同分区包含的消息不同
      - 每个消息在被添加到分区时，都会被分配一个 offset，它是消息在此分区中的唯一编号，kafka 通过 offset 保证消息在分区内的顺序，但只保证在同一个分区内的消息是有序的; 

    * 对于应用层的消费来说，每次消费一个消息并且提交以后，会保存当前消费到的最近的一个 offset

  - offset 在哪里维护

    - 在 kafka 中，提供了一个__consumer_offsets_* 的一个topic，把 offset 信息写入到这个 topic 中。__
    - consumer_offsets——按保存了每个 consumer group某一时刻提交的 offset 信息。
    - __consumer_offsets 默认有50 个分区

    ```java
    //groupid为KafkaConsumerDemo
    //找到这 个 consumer_group 保存在哪个分区中
    properties.put(ConsumerConfig.GROUP_ID_CONFIG, "KafkaConsumerDemo");
    ```

    * 计算公式：
      * Math.abs(“groupid”.hashCode())%groupMetadataTopicPartitionCount ; 
         * 由于默认情况下groupMetadataTopicPartitionCount 有 50 个分区，计算得到的结果为:35, 意味着当前的consumer_group的位移信息保存在__consumer_offsets 的第 35 个分区

    ```shell
    # 执行如下命令，可以查看当前 consumer_goup 中的 offset 位移信息
    sh kafka-simple-consumer-shell.sh --topic __consumer_offsets --partition 35 --broker-list 192.168.11.153:9092,192.168.11.154:9092,192.168.11.157:90 92 --formatter
    "kafka.coordinator.group.GroupMetadataManager\$ OffsetsMessageFormatter"
    # 从输出结果中，就可以看到 test 这个 topic 的 offset 的位移日志
    ```

- 消费端如何消费指定的分区

  ```java
  //消费指定分区的时候，不需要再订阅 
  //kafkaConsumer.subscribe(Collections.singletonList(topic));
  //消费指定的分区
  //消费指定该 topic 下的 0 号分区。 其他分区的数据就无法接收
  TopicPartition topicPartition=new TopicPartition(topic,0); kafkaConsumer.assign(Arrays.asList(topicPartition));
  ```

## 消息的存储策略

### 消息的保存路径

* `kafka` 使用日志文件的方式来保存生产者和发送者的消息，每条消息都有一个`offset` 值来表示它在分区中的偏移量
* `Kafka`中存储的一般都是海量的消息数据，为了避免日志文件过大，`Log` 并不是直接对应在一个磁盘上的日志文件，而是对应磁盘上的一个目录，这个目录的命名规则是`<topic_name>_<partition_id>`
  * 比如创建一个名为`firstTopic`的 `topic`，其中有 3 个 `partition`，那么在 kafka 的数据目录`(/tmp/kafka-log)`中就有 3 个目录，`firstTopic-0~3`
* 消息从发送到落地保存，broker 维护的消息日志本身就是文件目录，每个文件都是二进制保存，生产者和消费者使用相同的格式来处理

### 多个分区在集群中的分配

- 如果对于一个`topic`，在集群中创建多个`partition`，那么`partition` 是如何分布的呢
  1. 将所有 `n` `Broker` 和待分配的 i 个 `Partition` 排序
  2. 将第 `i` 个 `Partition` 分配到第`(i mod n)`个 `Broker` 上

### 消息写入的性能

* 为避免在机械硬盘寻址过程中消耗大量时间以及随机读写带来的时间消耗，`kafka`采用顺序写的方式存储数据，但是频繁的`I/O` 操作仍然会造成磁盘的性能瓶颈，所以`kafka`还有一个性能策略

- 零拷贝
- Kafka直接把消息从Linux 文件系统缓存里发送到网络通道，而不需经过任何中间缓冲区，避免了字节复制，也不需要管理内存缓冲区，从而获得更好的性能

### 消息的文件存储机制

* 分析日志的存储方式。通过如下命令找到对应 partition 下的日志内容
  * `ls /tmp/kafka-logs/firstTopic-1/`

- `LogSegment`（逻辑上的概念）

  - 假设 `kafka` 以 `partition` 为最小存储单位，生产者不断发送消息必然会引起`partition`文件无限扩张，这样将带来消息文件的维护及被消费消息的清理很多麻烦，所以`kafka` 以 `segment` 为单位又把 `partition` 进行细分

  * 每个`partition` 相当于一个巨型文件被平均分配到多个大小相等的`segment` 数据文件中(每个`segment `文件中的消息不一定相等)，这种特性方便已经被消费的消息的清理，提高磁盘的利用率
  * `log.segment.bytes=107370` (设置分段大小)，默认是1gb
  * `kafka` 是通过分段的方式将 `Log` 分为多个 `LogSegment`
    * 一个 `LogSegment` 对应磁盘上的一个记录消息的日志文件`.log`和一个保存消息索引的索引文件`.index`
  * `segment` 文件命名规则：`partion`全局的第一个`segment`从 0 开始，后续每个`segment` 文件名为上一个 `segment`文件最后一条消息的 `offset` 值进行递增。数值最大为 `64 `位`long` 大小，20 位数字字符长度，没有数字用 0 填充

  ```shell
  # 看到 kafka 消息日志的内容
  sh kafka-run-class.sh kafka.tools.DumpLogSegments -- files /tmp/kafka-logs/test- 0/00000000000000000000.log --print-data-log
  # 第一个 log 文件的最后一个 offset 为:5376,所以下一个 segment 的文件命名为: 00000000000000005376.log。对 应的 index 为 00000000000000005376.index
  ```

  * `segment` 中` index` 和` log` 的对应关系：
    * 索引文件存储大量元数据，数据文件存储大量消息，索引文件中元数据指向对应数据文件中`message`的物理偏移地址
      * 其中以索引文件中元数据`[3,497]`为例，依次在数据文件中表示第`3`个`message`(在全局`partiton`表示第`368772`个`message`)、以及该消息的物理偏移地址为`497`
      * `position` 是`ByteBuffer` 的指针位置
    * 自0.10.0.1开始，Kafka为每个topic分区增加了新的索引文件：
      * 基于时间的索引文件：<segment基础位移>.timeindex，索引项间隔由**index.interval.bytes确定**
      * TimeIndex 索引文件格式：它是映射时间戳和相对 offset

  ```shell
  # 查 看 索 引 内 容
  sh kafka-run-class.sh kafka.tools.DumpLogSegments --files /tmp/kafka- logs/test-0/00000000000000000000.index --print-data- log
  ```

### 消费者如何提交偏移量

* 把更新分区当前位置的操作叫作提交

* 消费者往一个叫作 _consumer_offset 的特殊主题发送消息，消息里包含每个分区的偏移量

* 如果消费者发生崩溃或者有新的消费者加入群组，就会触发再均衡，完成再均衡之后，每个消费者可能分配到新的分区，消费者需要读取每个分区最后一次提交的偏移量，然后从偏移量指定的地方继续处理

* 如果提交的偏移量小于客户端处理的最后一个消息的偏移量 ，那处于两个偏移量之间的消息就会被重复处理

* 如果提交的偏移量大于客户端处理的最后一个消息的偏移量，那么处于两个偏移量之间的消息将会丢失

* 消费者自动提交偏移量`enable.auto.commit`为true，每过5s（`auto.commit.interval.ms`），消费者会自动把从 poll() 方法接收到的最大偏移量提交上去，也是在轮询里进行

  * 在使用自动提交时 ，每次调用轮询方法都会把上一次调用返回的偏移量提交上去

* 提交当前偏移量，`auto.commit.offset`为false，使用commitSync()提交由poll()方法返回的最新偏移量，提交成功后立马返回，失败则抛出异常。在成功提交或碰到无法恢复的错误之前，commitSync()会一直重试。broker在对提交请求作出回应之前，应用程序会一直阻塞

* 异步提交，只管发送提交请求，无需等待broker的响应`consumer.commitAsync()`，不会进行重试，因为在它收到服务器响应的时候，可能有一个更大的偏移量已经提交成功。支持回调，在broker作出响应时会执行回调，回调经常被用于记录提交错误或生成度量指标

  ```java
  consumer.commitAsync(new OffsetCommitCallback() {
    public void onComplete(Map<TopicPartition, OffsetAndMetadata> offsets, Exception e) {
      if(e != null) {
        log.error("commit failed for offsets {}", offsets, e);
      }
    }
  });
  // 重试异步提交
  // 使用一个单调递增的序列号来维护异步提交的顺序，在进行重试前，先检查回调的序列号和即将提交的偏移量是否相等，如果相等可安全地进行重试，否则停止重试
  ```

* 同步和异步组合提交，轮询一次用异步提交`consumer.commitAsync()`，最后finally里用同步提交`consumer.commitSync();`

* 提交特定偏移量

  ```java
  private Map<TopicPartition, OffsetAndMetadata> currentOffsets = new HashMap<>();
  while ... for ...
    currentOffsets.put(new TopicPartition(record.topic(), record.partition()), new OffsetAndMetadata(record.offset()+1, "no metadata"));
  consumer.commitAsync(currentOffsets, null);
  ```

### 在 partition 中如何通过 offset 查找 message

查找的算法是：

- 根据`offset` 的值，查找`segment` 段中的 `index` 索引文件。由于索引文件命名是以上一个文件的最后一个 `offset` 进行命名的，所以，使用二分查找算法能够根据`offset` 快速定位到指定的索引文件 
- 找到索引文件后，根据`offset` 进行定位，找到索引文件中的符合范围的索引。(`kafka` 采用稀疏索引的方式来提高查找性能) 
- 得到`position` 以后，再到对应的`log` 文件中，从 `position` 处开始查找`offset`对应的消息，将每条消息的 `offset` 与目标`offset`进行比较，直到找到消息 

比如要查找 `offset=2490` 这条消息，那么先找到`00000000000000000000.index`, 然后找到`[2487,49111]`这个索引，再到 `log` 文件中，根据 `49111` 这个`position` 开始查找，比较每条消息的 `offset` 是否大于等于 2490。最后查找到对应的消息以后返回

### Log 文件的消息内容分析

查看二进制的日志文件信息，一条消息，会包含很多的字段。

```shell
offset: 5371 position: 102124 CreateTime: 1531477349286 isvalid: true keysize: -1 valuesize: 12 magic: 2 compresscodec: NONE producerId: -1 producerEpoch: - 1 sequence: -1 isTransactional: false headerKeys: [] payload: message_5371
```

`keysize` 和`valuesize` 表示`key` 和 `value`的大小、`compresscodec`表示压缩编码、`payload`：表示消息的具体内容

### 日志的清除策略

日志的分段存储，一方面能够减少单个文件内容的大小，另一方面，方便 kafka 进行日志清理

日志的清理策略有两个：

- 根据消息的保留时间（默认7天），当消息在 kafka 中保存的时间超过了指定的时间，就会触发清理过程
- 根据 topic 存储的数据大小，当 topic 所占的日志文件大小大于一定的阀值，则可以开始删除最旧的消息。kafka 会启动一个后台线程，定期检查是否存在可以删除的消息 

通过 `log.retention.bytes `和 `log.retention.hours `设置，当其中任意一个达到要求，都会执行删除

为了彻底把一个键从系统里删除，应用程序必须发送一个包含该键且值为 null 的消息

### 清理的工作原理

* 每个日志片段可以分为：
  * 干净的部分，消息之前被清理过，每个键只有一个上一次清理时保留下来的值
  * 污浊的部分，消息是在上一次清理之后写入的
* 清理线程会读取分区的污浊部分，并在内存里创建一个map。 map 里的每个元素包含了消息键的散列值和消息的偏移量，键的散列值是 16B，加上偏移量总共是24B
  * 清理线程在创建好偏移量 map后，开始从干净的片段处读取消息，从最旧的消息开始，把它们的内容与 map 里的内容进行比对
    * 它会检查消息的键是否存在于 map 中，如果不存在，那么说明消息的值是最新的，就把消息复制到替换片段上
    * 如果键已存在，消息会被忽略，因为在分区的后部已经有一个具有相同键的消息存在
  * 在复制完所有的消息后，将替换片段与原始片段进行交换，然后开始清理下一个片段。完成整个清理过程后，每个键对应一个不同的消息——这些消息的值都是最新的

### 日志压缩策略

* 消费者只关心`key` 对应的最新的`value`
* 可以开启`kafka`的日志压缩功能，服务端会在后台启动启动`Cleaner` 线程池，定期将相同的`key`进行合并，只保留最新的`value` 值

## 深入Kafka

* Kafka使用 Zookeeper来维护集群成员的信息，在 broker 启动的时候，它通过创建临时节点把自己的 ID 注册到 Zookeeper。 Kafka 组件订阅 Zookeeper 的/brokers/ids 路径(broker在 Zookeeper上的注册路径)，当有 broker加入集群或退出集群时，这些组件就可以获得通知
* 集群里第一个启动的 broker 通过在Zookeeper 里创建一个临时节点/controller让自己成为控制器，其他 broker在控制器节点上创建Zookeeper watch 对象，这样它们就可以收到这个节点的变更通知
* 副本
  * 每个分区都有一个首领副本 。 为了保证一致性，所有生产者请求和消费者请求都会经过这个副本
    * 首领的另一个任务是搞清楚哪个跟随者的状态与自己是一致的
    * 为了与首领保持同步，跟随者向首领发送获取数据的请求
    * 通过查看每个跟随者请求的最新偏移量，首领就会知道每个跟随者复制的进度
    * 持续请求得到的最新悄息副本被称为同步的副本 。在首领发生失效时，只有同步副本才有可能被选为新首领
  * 首领以外的副本都是跟随者副本。跟随者副本不处理来自客户端的请求，它们唯一的任务就是从首领那里复制消息，保持与首领一致的状态。如果首领发生崩渍，其中的一个跟随者会被提升为新首领
* broker会在它所监听的每一个端口上运行一个 Acceptor线程，这个钱程会创建一个连接，并把它交给 Processor 线程去处理，Processor线程负责从客户端获取请求悄息，把它们放进请求队列，然后从晌应队列获取响应消息，把它们发送给客户端

## Partition副本机制

* `kafka`为了提高 `partition` 的可靠性而提供了副本的概念(`Replica`)，通过副本机制来实现冗余备份

* 每个分区可以有多个副本，副本集合中有一个`leader` 副本，所有的读写请求都是由 `leader` 副本来进行处理。剩余的其他副本都做为`follower` 副本，`follower` 副本会从`leader` 副本同步消息日志

* 副本集会存在一主多从的关系，通过副本机制提高`kafka`集群可用性
  * 一般情况下，同一个分区的多个副本会被均匀分配到集群中的不同`broker`上，当`leader` 副本所在的 `broker` 出现故障后，可以重新选举新的`leader` 副本继续对外提供服务

### 副本分配算法

1. 将所有 `N` `Broker` 和待分配的 `i` 个 `Partition` 排序
2. 将第 `i` 个`Partition` 分配到第`(i mod n)`个`Broker`上
3. 将第 `i` 个`Partition`的第 `j` 个副本分配到第`((i + j) mod n)`个`Broker` 上

### 创建一个带副本机制的 topic

```shell
# 创建带 2 个副本的 topicg
./kafka-topics.sh --create --zookeeper 192.168.11.156:2181 --replication-factor 2 --partitions 3 -- topic secondTopic
# 可以在/tmp/kafka-log 路径下看到对应 topic 的副本信息
```

### 如何知道那个各个分区中对应的 leader 是谁呢?

* 在 zookeeper 服务器上，通过如下命令去获取对应分区的信息, 比如下面这个是获取 secondTopic 第 1 个分区的状态信息

```shell
get /brokers/topics/secondTopic/partitions/1/state
{"controller_epoch":12,"leader":0,"version":1,"leader_epoch":0,"isr":[0,1]}
# leader 表示当前分区的leader是那个 broker-id
```

* `Kafka` 提供了数据复制算法保证，如果`leader` 发生故障或挂掉，一个新`leader` 被选举并被接受客户端的消息成功写入
* `Kafka` 确保从同步副本列表中选举一个副本为`leader`
  * `leader` 负责维护和跟踪 `ISR(in-Sync replicas` ， 副本同步队列)中所有`follower` 滞后的状态
  * 当`producer` 发送一条消息到`broker`后，`leader` 写入消息并复制到所有 `follower`
  * 消息提交之后才被成功复制到所有的同步副本

### 副本协同机制

* 写请求首先由`Leader` 副本处理，之后`follower` 副本会从`leader` 上拉取写入的消息，这个过程会有一定的延迟，导致 `follower` 副本中保存的消息略少于 `leader` 副本，但是只要没有超出阈值都可以容忍

* 但是如果一个 `follower` 副本出现异常，比如宕机、网络断开等原因长时间没有同步到消息，那这个时候，`leader` 就会把它踢出去

* kafka 通过 ISR集合来维护一个分区副本信息

- ISR

  - ISR 表示目前可用且消息量与`leader` 相差不多的副本集合，这是整个副本集合的一个子集

  * ISR 集合中的副本必须满足两个条件：
  1. 副本所在节点必须维持着与`zookeeper` 的连接 
  
  2. 副本最后一条消息的`offset` 与`leader` 副本的最后一条消息的`offset` 之间的差值不能超过指定的阈值 `(replica.lag.time.max.ms) `
   * `replica.lag.time.max.ms`：如果该 `follower` 在此时间间隔内一直没有追上过`leader`的所有消息，则该`follower` 就会被剔除`isr` 列表 
  
  * `ISR` 数据保存在`Zookeeper` 的`/brokers/topics/<topic>/partitions/<partitionId>/state` 节点中

### 数据是如何同步的

* `zookeeper` 的 `leader` 和`follower` 的同步机制和 `kafka` 副本的同步机制的实现是完全不同的

* 副本根据角色的不同可分为 3 类：
  * `leader` 副本：响应 `clients` 端读写请求的副本
  * `follower` 副本：被动地备份`leader` 副本中的数据，不能响应 `clients` 端读写请求
  * `ISR` 副本：包含了`leader` 副本和所有与`leader` 副本保持同步的`follower` 副本

* 如何判定是否与`leader` 同步：

  * 每个`Kafka` 副本（不只是`leader`副本）对象都有两个重要的属性：`LEO`（Log End Offset）和`HW`（`HighWatermark`），这两个参数跟 ISR 集合紧密关联
  * `LEO`：即日志末端位移(`log end offset`)，记录了该副本底层日志(`log`)中下一条消息的位移值
    * 如果`LEO=10`，那么表示该副本保存了 10 条消息，位移值范围是[0, 9]。
    * `leader LEO` 和 `follower LEO` 的更新是有区别的
  * `HW`：即水位值。对于同一个副本对象而言，其HW 值不会大于 `LEO` 值。小于等于 `HW` 值的所有消息都被认为是“已备份”的(`replicated`)。同理，`leader` 副本和`follower` 副本的 HW 更新是有区别的
    * `HW` 标记了一个特殊的`offset`，当消费者处理消息的时候，只能拉去到 HW 之前的消息，HW之后的消息对消费者来说是不可见的

  * 取`partition`对应`ISR` 中最小的 `LEO` 作为 `HW`，`consumer` 最多只能消费到`HW` 所在的位置

  * 每个 replica 都有 HW，leader 和 follower 各自维护更新自己的 HW 的状态。
    * 一条消息只有被 ISR 里的所有 Follower 都从 Leader 复制过去才会被认为已提交。
      * 这样就避免了部分数据被写进了Leader，还没来得及被任何 Follower 复制就宕机了，而造成数据丢失(Consumer 无法消费这些数据)。
      * 而对于Producer 而言，它可以选择是否等待消息 commit，这可以通过 acks 来设置。这种机制确保了只要 ISR 有一个或以上的 Follower，一条被 commit 的消息就不会丢失

### 数据的同步过程

* 需要解决：
  1. 怎么传播消息
  2. 在向消息发送端返回 `ack` 之前需要保证多少个 `Replica` 已经接收到这个消息

* 数据的处理过程是：
  * Producer 在发布消息到某个 Partition 时，先通过ZooKeeper找到该Partition的Leader 
    * `get/brokers/topics/<topic>/partitions/2/state`
  * 无论该 Partition 有多少个副本，Producer 只将该消息发送到该 Partition 的Leader，Leader 会将该消息写入其本地 Log
  * 每个 Follower都从 Leader pull 数据并写入其Log 后，向 Leader 发送 ACK。
  * 一旦 Leader 收到了 ISR 中的所有 Replica 的 ACK，该消息就被认为已经 commit 了，Leader 将增加 HW(HighWatermark)并且向 Producer 发送ACK 

### 初始状态

* leader 和 follower 的 HW 和 LEO 都是 0，leader 副本会保存 remote LEO=0（表示所有 follower LEO也会被初始化为 0）
* 此时，producer没有发送消息，follower 会不断地向 leader 发送 FETCH 请求，但请求因没数据而被leader阻塞，在指定时间（`replica.fetch.wait.max.ms`）之后会强制完成请求
* 如果在指定时间内producer发送消息，那么kafka会唤醒fetch请求，让leader继续处理

* 这里会分两种情况
  * 第一种是 leader 处理完 producer 请求之后，follower 发送一个 fetch 请求过来

  * 生产者发送一条消息

    * leader 副本收到请求以后：
      1. 把消息追加到 log 文件，同时更新 leader 副本的 LEO 
      2. 尝试更新 leader HW 值
         * 这个时候由于 follower 副本还没有发送 fetch 请求，那么leader 的remote LEO仍然 是 0
         * leader 会比较自己的LEO 以及 remote LEO的值发现最小值是0，与HW 的值相同，所以不会更新 HW
    * follower fetch 请求消息，leader 副本的处理逻辑是：
      1. 读取 log 数据、更新 remote LEO=0(follower 还没有写 入这条消息，这个值是根据 follower 的 fetch请求中的 offset 来确定的) 
      2. 尝试更新 HW，因为这个时候 LEO 和 remoteLEO 还是不一致，所以仍然是 HW=0 
      3. 把消息内容和当前分区的 HW 值发送给 follower 副本 
    * follower 副本收到 response 以后
      1. 将消息写入到本地 log，同时更新 follower 的 LEO 
      2. 更新 follower HW，本地的 LEO 和 leader 返回的 HW 进行比较取小的值，所以仍然是 0
    * 第一次交互结束以后，HW 仍然还是 0，这个值会在下一次follower 发起 fetch 请求时被更新

  * follower 发第二次 fetch 请求

    * leader 收到请求以后：

      1. 读取 log 数据 

      2. 更新 remote LEO=1， 因为这次 fetch 携带的 offset 是 1
      3. 更新当前分区的 HW，这个时候 leader LEO 和 remote LEO 都是 1，所以 HW 的值也更新为 1 
      4. 把数据和当前分区的 HW 值返回给 follower 副本，这个时候如果没有数据，则返回为空

    * follower 副本收到 response 以后：

      1. 如果有数据则写本地日志，并且更新 LEO

      2. 更新 follower 的 HW 值

  * 到目前为止，数据的同步就完成了，意味着消费端能够消费 offset=0 这条消息

* 第二种是follower 阻塞在 leader 指定时间之内，leader 副本收到producer 的请求。

  * 当 leader 收到请求以后会唤醒处于阻塞的fetch 请求：

  1. leader 将消息写入本地日志，更新 Leader 的 LEO
  2. 唤醒 follower 的 fetch 请求
  3. 更新 HW

### 数据丢失的问题

* kafka 使用 HW 和 LEO 的方式来实现副本数据的同步
* 特定的背景下存在一个数据丢失的问题
  * 前提：ISR中最小副本数为1（`min.insync.replicas=1`）且ack设置为-1（需所有副本确认才算已提交），即一旦消息被写入 leader 端  则被认为是“已提交”
  * 延迟一轮fetch rpc更新HW值的设计使得follower HW值是异步延迟更新的，此过程中当leader发生变更，那么成为新leader的follower的HW值就有可能是过期的，原leader重启后会做日志截断，使得原leader已提交但未被之前是follower的现leader同步的数据发生丢失

### 数据丢失的解决方案

*  kafka0.11.0.0 版本以后使用 leader epoch 来解决这个问题
* leader epoch 实际上是一对之(epoch,offset)
  * epoch 表示 leader 的版本号，从 0开始，当 leader 变更过 1 次时 epoch 就会+1，而 offset 则对应于该 epoch 版本的 leader 写入第一条消息的位移
  * 比如说 (0,0) ; (1,50); 表示第一个 leader 从 offset=0 开始写消息， 一共写了 50 条，第二个 leader 版本号是 1，从 50 条处开始写消息
  * leader broker 会保存并定期写入leader epoch信息到对应分区本地磁盘checkpoint文件中
    * `/tml/kafka-log/topic/leader-epoch- checkpoint`
  * 每次副本重新成为 leader 时会查询这leader epoch部分缓存，获取出对应 leader 版本的 offset
  * 恢复的时候使用这些信息而非水位来判断是否需要截断日志
    * 日志截断操作

### 如何处理所有的 Replica 不工作的情况

* 当ISR中至少有一个follower时，Kafka可以确保已经提交的数据不丢失，但如果某个Partition的所有副本都宕机了，则无法保证数据不丢失
  * 等待ISR中的任何一个副本复活并选为leader。时间可能比较长，有可能活不过来
  * 选择第一个复活的副本（不一定是ISR集合中的副本）选为leader。可能存在消息丢失
  * 需要在可用性和一致性之间做个简单折衷

### ISR 的设计原理

* 冗余备份是分布式存储的一种常见设计方式，有同步复制和异步复制
* kafka
  * 如采用同步复制，需所有能工作的`Follower`副本都复制完后消息才算是提交成功，一个`Follower`副本出现故障则导致`HW`无法完成递增，消息则提交失败，故障的`Follower`副本会拖慢整个系统性能导致不可用
  * 如采用异步复制，`leader` 副本收到生产者推送的消息后，就认为此消息提交成功，`follower` 副本则异步从`leader` 副本同步。但如果有`follower` 副本的同步速度比较慢从而导致其消息量远落后于`leader`副本，则从`Follower`副本中选举出来的新`leader`则存在消息丢失
  * 权衡同步和异步策略，采用`ISR`集合
    * 当`follower`副本同步延迟过高则`leader`将该`follower`副本踢出`ISR`集合，消息依然可以快速提交
    * 新`leader`优先从`ISR`集合中`follower`中选举，新`leader`副本包含了`HW`之前全部消息，避免了消息丢失



