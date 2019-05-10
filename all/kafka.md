# Kafka

## 简介

- 什么是kafka？

  - 是一款分布式消息订阅和发布系统，具有高性能、高吞吐量的特点而被广泛应用于大数据传输场景

- 能干什么？
  - 由于 kafka 具有更好的吞吐量、内置分区、冗余及容错性的优点(kafka 每秒可以处理几十万消息)，让 kafka 成为了一个很好的大规模消息处理应用的解决方案 

    1. 行为跟踪：跟踪用户浏览页面、搜索及其他行为。通过订阅-发布模式实时记录到对应的 topic 中，通过后端大数据平台接入处理分析，并做更进一步的实时处理和监控 

    2. 日志收集：使用kafka 代理日志聚合从服务器上收集日志文件，然后放到一个集中的平台(文件服务器)进行处理

## Kafka 本身的架构

一个典型的 kafka 集群包含：

- 若干 Producer、若干个 Broker(kafka 支持水平扩展)、若干个 ConsumerGroup、以及一个 zookeeper 集群
  - kafka 通过 zookeeper 管理集群配置及服务协同

* Producer使用push模式将消息发布到broker，consumer通过监听使用pull模式从broker订阅并消费消息
* 多个 broker 协同工作，producer 和 consumer 部署在各个业务逻辑中。三者通过zookeeper 管理协调请求和转发。这样就组成了一个高性能的分布式消息发布和订阅系统

## 安装部署

- 下载、解压安装包

- 启动/停止

  1. 需要先启动 zookeeper，如果没有搭建 zookeeper 环境，可以直接运行kafka 内嵌的 zookeeper

     启动命令: `bin/zookeeper-server-start.sh config/zookeeper.properties &`

  2. 进入 kafka 目录 `bin/kafka-server-start.sh {-daemon 后台启动} config/server.properties &`

  3. 进入 kafka 目录 `bin/kafka-server-stop.sh config/server.properties`

### 安装集群环境

- 修改 server.properties 配置

  1. 修改 server.properties. broker.id=0 / 1

  2. 修改 server.properties 修改成本机 IP

     advertised.listeners=PLAINTEXT://192.168.11.153:9092

     当 Kafka broker 启动时，它会在 ZK 上注册自己的 IP 和端口号，客户端就通过这个 IP 和端口号来连接

## 基本操作

```shell
# 创建topic
./kafka-topics.sh --create --zookeeper localhost:2181 --replication-factor 1 --partitions 1 --topic test
# Replication-factor 表示该 topic 需要在不同的 broker 中保存几份，这里设置成 1，表示在两个 broker 中保存两份
# Partitions 分区数

# 查看topic
./kafka-topics.sh --list --zookeeper localhost:2181

# 查看topic属性
./kafka-topics.sh --describe --zookeeper localhost:2181 --topic test

# 消费消息
./kafka-console-consumer.sh –bootstrap-server localhost:9092 --topic test --from-beginning

# 发送消息
./kafka-console-producer.sh --broker-list localhost:9092 --topic test
```

### Spring Kafka集成

## 配置信息分析

### 发送端的可选配置信息分析

- acks，表示 producer 发送消息到 broker上以后的确认值。有三个可选项
  - 0：表示producer不需要等待broker的消息确认。延时最小但风险最大(当 server 宕机时，数据将会丢失)
  - 1：表示producer只需要获得kafka集群中的leader节点确认即可，延时较小同时确保了 leader 节点确认接收成功
  - all(-1)：需要ISR中所有的Replica给予接收确认，速度最慢，安全性最高，但是由于 ISR 可能会缩小到仅包含一个 Replica，所以设置参数为 all 并不能一定避免数据丢失   
- batch.size，生产者批量发送消息到 broker 上的同一个分区，默认大小16kb
- linger.ms，为每次发送到 broker 的请求增加一些 delay，以此来聚合更多的 Message 请求。
  - batch.size和linger.ms这两个参数是kafka性能优化的关键参数， batch.size 和 linger.ms 这两者的作用是一样的，当二者都配置的时候，只要满足其中一个要求，就会发送请求到 broker 上 
- max.request.size，设置请求的数据的最大字节数，为了防止发生较大的数据包影响到吞吐量，默认值为 1MB。

### 消费端的可选配置分析

- group.id，组内多个消费者共享一个公共的group ID，一起消费订阅主题的所有分区，每个分区只能由一个消费者来消费
  * consumer group 是 kafka 提供的可扩展且具有容错性的消费者机制
- enable.auto.commit，消费消息以后自动提交，该消息才不会被再次接收到，可配合 auto.commit.interval.ms 控制自动提交的频率
  * 可通过 consumer.commitSync()的方式实现手动提交
- auto.offset.reset
  - auto.offset.reset=latest ，新的消费者将会从其他消费者最后消费的offset 处开始消费 Topic 下的消息
  - auto.offset.reset= earliest ，新的消费者会从该 topic 最早的消息开始消费
  - auto.offset.reset=none 情况下，新的消费者加入以后，由于之前不存在offset，则会直接抛出异常

- max.poll.records，限制每次调用 poll 返回的消息数，这样可以更容易的预测每次 poll 间隔要处理的最大值。通过调整此值，可以减少 poll 间隔

## Topic和Partition

- Topic

  - 在 kafka 中，topic 是一个存储消息的逻辑概念，可以认为是一个消息集合
  - 每条发送到 kafka 集群的消息都有 一个类别。
  - 每个topic 可以有多个生产者向它发送消息，也可以有多个消费者去消费其中的消息 

- Partition

  - 每个Topic至少有一个分区，同一topic 下的不同分区包含的消息是不同的
  - 每个消息在被添加到分区时，都会被分配一个 offset(称之为偏移量)，它是消息在此分区中的唯一编号
  - kafka 通过 offset保证消息在分区内的顺序，只保证在同一个分区内的消息是有序的

  * 每一条消息发送到 broker 时，会根据 partition 的规则选择存储到哪一个 partition。
    * 如果 partition 规则设置合理，那么所有的消息会均匀的分布在不同的 partition 中

  ![image-20181218141750289](/Users/dingyuanjie/Desktop/MyKnowledge/2.code/java/2.%E5%92%95%E6%B3%A1%E5%AD%A6%E9%99%A2/02.%E5%88%86%E5%B8%83%E5%BC%8F%E4%B8%93%E9%A2%98/06.%E5%88%86%E5%B8%83%E5%BC%8F%E6%B6%88%E6%81%AF%E9%80%9A%E4%BF%A1/image-20181218141750289-5113870.png)

- Topic和Partition的存储

  - Partition 是以文件的形式存储在文件系统中
    - 比如创建一个名为 firstTopic 的 topic，其中有 3 个 partition，那么在kafka 的数据目录(/tmp/kafka-log)中就有 3 个目录，firstTopic-0~3， 命名规则是`<topic_name>-<partition_id>`

  ```shell
  ./kafka-topics.sh --create --zookeeper 192.168.11.156:2181 --replication-factor 1 --partitions 3 -- topic firstTopic
  ```

## 消息分发策略

* 消息是 kafka 中最基本的数据单元

* 在 kafka 中，一条消息由 key、value 两部分构成
  * 在发送一条消息时，可以指定这个 key，那么 producer 会根据 key 和 partition 机制来判断当前这条消息应该发送并存储到哪个 partition 中。
    * 可以根据需要进行扩展 producer 的 partition 机制

- 消息默认的分发机制

  - 默认情况下，kafka 采用的是 hash 取模的分区算法

  * 如果Key 为 null，则会随机分配一个分区
    * 这个随机是在这个参数”metadata.max.age.ms”的时间范围内随机选择一个。对于这个时间段内，如果 key 为 null，则只会发送到唯一的分区。这个值默认情况下是 10 分钟更新一次

  * Topic/Partition 和 broker 的映射关系
    * 每一个 topic 的每一个 partition，需要知道对应的 broker 列表是什么，leader是谁、follower 是谁。这些信息都是存储在 Metadata 这个Metadata类里

## 消息消费原理

在实际生产过程中，每个 topic 都会有多个 partitions，多个 partitions 的好处在于：

1. 能够对 broker 上的数据进行分片有效减少了消息的容量从而提升 io 性能
2. 为了提高消费端的消费能力，一般会通过多个consumer 去消费同一个 topic ，也就是消费端的负载均衡机制

- 在多个 partition 以及多个 consumer 的情况下，消费者是如何消费消息的

  - 分区分配策略：通过partition.assignment.strategy 这个参数来设置

  1. Range(默认)范围分区分配策略

     * Range 策略是针对每个topic的，将同一个topic中的 partitions 的个数除于消费者线程的总数来决定每个消费者线程消费几个分区，如果除不尽，那么前面几个消费者线程将会多消费一个分区

     * 弊端：某些消费者线程比其他消费者线程多消费分区

  2. RoundRobin(轮询)分区分配策略

     * 是把所有 partition 和所有 consumer 线程都列出来，然后按照 hashcode 进行排序。最后通过轮询算法分配 partition 给消费线程。如果所有 consumer 实例的订阅是相同的，那么 partition 会均匀分布

     * 必须满足两个条件：
       * 每个主题的消费者实例具有相同数量的流
       * 每个消费者订阅的主题必须是相同的

     * 假如按照 hashCode 排序完的 topic- partitions 组依次为 T1-5, T1-3, T1-0, T1-8, T1-2, T1-1, T1-4, T1-7, T1-6, T1-9，消费者线程排序为 C1-0, C1-1, C2- 0, C2-1，最后分区分配的结果为:

       * C1-0 将消费 T1-5, T1-2, T1-6 分区;	C1-1 将消费 T1-3, T1-1, T1-9 分区; 

       * C2-0 将消费 T1-0, T1-4 分区;	C2-1 将消费 T1-8, T1-7 分区;  

- 什么时候会触发这个策略呢?

  - 当出现以下几种情况时，kafka 会进行一次分区分配操作，也就是 kafka consumer 的 rebalance
    1. 同一个 consumer group 内新增了消费者 
    2. 消费者离开当前所属的 consumer group，比如主动停机或者宕机 
    3. topic 新增了分区(也就是分区数量发生了变化) 

  * kafka consuemr 的 rebalance 机制规定了一个 consumer group 下的所有 consumer 如何达成一致来分配订阅 topic的每个分区。而具体如何执行分区策略，就是其两种内置的分区策略

- 谁来执行 Rebalance 以及管理 consumer 的 group 呢?

  * Kafka 提供了一个角色：coordinator 来执行对于 consumer group 的管理，当 consumer group 的第一个 consumer 启动的时候，它会去和 kafka server 确定谁是它们组的 coordinator。之后该 group 内的所有成员都会和该 coordinator 进行协调通信

- 如何确定 coordinator（consumer group 如何确定自己的 coordinator 是谁呢）

  - 消费者向 kafka 集群中的任意一个 broker 发送一个GroupCoordinatorRequest 请求，服务端会返回一个负载最小的 broker 节点的 id，并将该 broker 设置为coordinator


* Rebalance过程
  * 在 rebalance 之前，需要保证 coordinator 是已经确定好了
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

    - 每个 Topic 至少有一个分区，同一topic 下的不同分区包含的消息是不同的。
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
    # 从输出结果中，我们就可以看到 test 这个 topic 的 offset 的位移日志
    ```

- 消费端如何消费指定的分区

  ```java
  //消费指定分区的时候，不需要再订阅 
  //kafkaConsumer.subscribe(Collections.singleto nList(topic));
  //消费指定的分区
  //消费指定该 topic 下的 0 号分区。 其他分区的数据就无法接收
  TopicPartition topicPartition=new TopicPartition(topic,0); kafkaConsumer.assign(Arrays.asList(topicPartit ion));
  ```

## 消息的存储策略

### 消息的保存路径

* kafka 是使用日志文件的方式来保存生产者和发送者的消息，每条消息都有一个 offset 值来表示它在分区中的偏移量。
* Kafka 中存储的一般都是海量的消息数据，为了避免日志文件过大，Log 并不是直接对应在一个磁盘上的日志文件，而是对应磁盘上的一个目录，这个目录的命名规则是`<topic_name>_<partition_id>`
  * 比如创建一个名为 firstTopic 的 topic，其中有 3 个 partition，那么在 kafka 的数据目录(/tmp/kafka-log)中就有 3 个目录，firstTopic-0~3
* 消息从发送到落地保存，broker 维护的消息日志本身就是文件目录，每个文件都是二进制保存，生产者和消费者使用相同的格式来处理

### 多个分区在集群中的分配

- 如果对于一个 topic，在集群中创建多个 partition，那么 partition 是如何分布的呢
  1. 将所有 N Broker 和待分配的 i 个 Partition 排序
  2. 将第 i 个 Partition 分配到第(i mod n)个 Broker 上

### 消息写入的性能

* 为避免在机械硬盘寻址过程中消耗大量时间以及随机读写带来的时间消耗，kafka 采用顺序写的方式存储数据，但是频繁的 I/O 操作仍然会造成磁盘的性能瓶颈，所以 kafka还有一个性能策略

- 零拷贝

  - 在消费者获取消息时，服务器先从硬盘读取数据到内存，然后把内存中的数据原封不动的通过 socket 发送给消费者

    1. 操作系统将数据从磁盘读入到内核空间的页缓存
    2. 应用程序将数据从内核空间读入到用户空间缓存中
    3. 应用程序将数据写回到内核空间的 socket 缓存中
    4. 操作系统将数据从 socket 缓冲区复制到网卡缓冲区，以便将数据经网络发出

    * 这个过程涉及到 4 次上下文切换以及 4 次数据复制，并且有两次复制操作是由 CPU 完成。
    * 但是这个过程中，数据完全没有进行变化，仅仅是从磁盘复制到网卡缓冲区

  * 通过“零拷贝”技术，可去掉没必要的数据复制操作，同时也减少上下文切换次数。
    * 现代的 unix 操作系统提供一个优化的代码路径
      * 用于将数据从页缓存传输到 socket;
      * 在 Linux 中，是通过 sendfile 系统调用来完成的。
        * 只用将磁盘文件的数据复制到页面缓存中一次，然后将数据从页面缓存直接发送到网络中（发送给不同的订阅者时，都可以使用同一个页面缓存），避免了重复复制操作
      * Java 提供了访问这个系统调用的法：`FileChannel.transferTo` API

### 消息的文件存储机制

* 分析日志的存储方式。通过如下命令找到对应 partition 下的日志内容
  * `ls /tmp/kafka-logs/firstTopic-1/`

- LogSegment（逻辑上的概念）

  - 假设 kafka 以 partition 为最小存储单位，生产者不断发送消息必然会引起partition文件无限扩张，这样将带来消息文件的维护及被消费消息的清理很多麻烦，所以 kafka 以 segment 为单位又把 partition 进行细分

  * 每个 partition 相当于一个巨型文件被平均分配到多个大小相等的 segment 数据文件中(每个 segment 文件中的消息不一定相等)，这种特性方便已经被消费的消息的清理，提高磁盘的利用率
  * `log.segment.bytes=107370` (设置分段大小)，默认是1gb
  * kafka 是通过分段的方式将 Log 分为多个 LogSegment
    * 一个 LogSegment 对应磁盘上的一个记录消息的日志文件`.log`和一个保存消息索引的索引文件`.index`
  * segment 文件`命名规则`：partion 全局的第一个 segment从 0 开始，后续每个 segment 文件名为上一个 segment文件最后一条消息的 offset 值进行递增。数值最大为 64 位long 大小，20 位数字字符长度，没有数字用 0 填充

  ```shell
  # 看到 kafka 消息日志的内容
  sh kafka-run-class.sh kafka.tools.DumpLogSegments -- files /tmp/kafka-logs/test- 0/00000000000000000000.log --print-data-log
  # 第一个 log 文件的最后一个 offset 为:5376,所以下一个 segment 的文件命名为: 00000000000000005376.log。对 应的 index 为 00000000000000005376.index
  ```

  * segment 中 index 和 log 的对应关系：
    * 索引文件存储大量元数据，数据文件存储大量消息，索引文件中元数据指向对应数据文件中message的物理偏移地址
      * 其中以索引文件中元数据[3,497]为例，依次在数据文件中表示第3个message(在全局partiton表示第368772个message)、以及该消息的物理偏移地址为497
      * position 是ByteBuffer 的指针位置
    * 自0.10.0.1开始，Kafka为每个topic分区增加了新的索引文件：
      * 基于时间的索引文件：<segment基础位移>.timeindex，索引项间隔由**index.interval.bytes确定**
      * TimeIndex 索引文件格式：它是映射时间戳和相对 offset

  ```shell
  # 查 看 索 引 内 容
  sh kafka-run-class.sh kafka.tools.DumpLogSegments --files /tmp/kafka- logs/test-0/00000000000000000000.index --print-data- log
  ```

### 在 partition 中如何通过 offset 查找 message

查找的算法是：

- 根据 offset 的值，查找 segment 段中的 index 索引文 件。由于索引文件命名是以上一个文件的最后一个 offset 进行命名的，所以，使用二分查找算法能够根据 offset 快速定位到指定的索引文件 
- 找到索引文件后，根据 offset 进行定位，找到索引文件 中的符合范围的索引。(kafka 采用稀疏索引的方式来提高查找性能) 
- 得到 position 以后，再到对应的 log 文件中，从 position 出开始查找offset对应的消息，将每条消息的 offset 与目标offset进行比较，直到找到消息 

比如说，我们要查找 offset=2490 这条消息，那么先找到00000000000000000000.index, 然后找到[2487,49111]这个索引，再到 log 文件中，根据 49111 这个 position 开始查找，比较每条消息的 offset 是否大于等于 2490。最后查找到对应的消息以后返回

### Log 文件的消息内容分析

查看二进制的日志文件信息，一条消息，会包含很多的字段。

```shell
offset: 5371 position: 102124 CreateTime: 1531477349286 isvalid: true keysize: -1 valuesize: 12 magic: 2 compresscodec: NONE producerId: -1 producerEpoch: - 1 sequence: -1 isTransactional: false headerKeys: [] payload: message_5371
```

 createTime 表示创建时间、keysize 和 valuesize 表示 key 和 value 的大小、 compresscodec 表示压缩编码、payload:表示消息的具体内容

### 日志的清除策略

日志的分段存储，一方面能够减少单个文件内容的大小，另一方面，方便 kafka 进行日志清理

日志的清理策略有两个：

- 根据消息的保留时间，当消息在 kafka 中保存的时间超过了指定的时间，就会触发清理过程
- 根据 topic 存储的数据大小，当 topic 所占的日志文件大小大于一定的阀值，则可以开始删除最旧的消息。kafka 会启动一个后台线程，定期检查是否存在可以删除的消息 

通过 log.retention.bytes 和 log.retention.hours 这两个参数来设置，当其中任意一个达到要求，都会执行删除。
默认的保留时间是:7 天

### 日志压缩策略

* 消费者只关心 key 对应的最新的 value
* 可以开启 kafka 的日志压缩功能，服务端会在后台启动启动 Cleaner 线程池，定期将相同的 key 进行合并，只保留最新的 value 值

## Partition副本机制

* kafka为了提高 partition 的可靠性而提供了副本的概念(Replica)，通过副本机制来实现冗余备份

* 每个分区可以有多个副本，副本集合中有一个leader 副本，所有的读写请求都是由 leader 副本来进行处理。剩余的其他副本都做为 follower 副本，follower 副本会从 leader 副本同步消息日志

* 副本集会存在一主多从的关系，通过副本机制提高kafka集群可用性
  * 一般情况下，同一个分区的多个副本会被均匀分配到集群中的不同 broker 上，当 leader 副本所在的 broker 出现故障后，可以重新选举新的 leader 副本继续对外提供服务。

### 副本分配算法

1. 将所有 N Broker 和待分配的 i 个 Partition 排序.
2. 将第 i 个 Partition 分配到第(i mod n)个 Broker 上
3. 将第 i 个 Partition 的第 j 个副本分配到第((i + j) mod n)个Broker 上

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

* Kafka 提供了数据复制算法保证，如果 leader 发生故障或挂掉，一个新 leader 被选举并被接受客户端的消息成功写入
* Kafka 确保从同步副本列表中选举一个副本为 leader
  * leader 负责维护和跟踪 ISR(in-Sync replicas ， 副本同步队列)中所有 follower 滞后的状态。
  * 当 producer 发送一条消息到 broker 后，leader 写入消息并复制到所有 follower。
  * 消息提交之后才被成功复制到所有的同步副本

### 副本协同机制

* 写请求首先由 Leader 副本处理，之后 follower 副本会从leader 上拉取写入的消息，这个过程会有一定的延迟，导致 follower 副本中保存的消息略少于 leader 副本，但是只要没有超出阈值都可以容忍

* 但是如果一个 follower 副本出现异常，比如宕机、网络断开等原因长时间没有同步到消息，那这个时候，leader 就会把它踢出去

* kafka 通过 ISR集合来维护一个分区副本信息

- ISR

  - ISR 表示目前可用且消息量与 leader 相差不多的副本集合，这是整个副本集合的一个子集”

  * ISR 集合中的副本必须满足两个条件：
  1. 副本所在节点必须维持着与 zookeeper 的连接 
    2. 副本最后一条消息的 offset 与 leader 副本的最后一条消息的 offset 之间的差值不能超过指定的阈值 (replica.lag.time.max.ms) 
     * replica.lag.time.max.ms：如果该 follower 在此时间间隔内一直没有追上过leader的所有消息，则该 follower 就会被剔除 isr 列表 
  * ISR 数据保存在 Zookeeper 的`/brokers/topics/<topic>/partitions/<partitionId>/state` 节点中

### 数据是如何同步的

* zookeeper 的 leader 和follower 的同步机制和 kafka 副本的同步机制的实现是完全不同的

* 副本根据角色的不同可分为 3 类：
  * leader 副本：响应 clients 端读写请求的副本
  * follower 副本：被动地备份 leader 副本中的数据，不能响应 clients 端读写请求
  * ISR 副本：包含了 leader 副本和所有与 leader 副本保持同步的 follower 副本

* 如何判定是否与 leader 同步：

  * 每个 Kafka 副本（不只是leader副本）对象都有两个重要的属性：LEO（Log End Offset）和HW（HighWatermark）。这两个参数跟 ISR 集合紧密关联
  * LEO：即日志末端位移(log end offset)，记录了该副本底层日志(log)中下一条消息的位移值。
    * 如果 LEO=10，那么表示该副本保存了 10 条消息，位移值范围是[0, 9]。
    * leader LEO 和 follower LEO 的更新是有区别的
  * HW：即水位值。对于同一个副本对象而言，其HW 值不会大于 LEO 值。小于等于 HW 值的所有消息都被认为是“已备份”的(replicated)。同理，leader 副本和follower 副本的 HW 更新是有区别的
    * HW 标记了一个特殊的 offset，当消费者处理消息的时候，只能拉去到 HW 之前的消息，HW之后的消息对消费者来说是不可见的

  * 取partition 对应 ISR 中最小的 LEO 作为 HW，consumer 最多只能消费到 HW 所在的位置

  * 每个 replica 都有 HW，leader 和 follower 各自维护更新自己的 HW 的状态。
    * 一条消息只有被 ISR 里的所有 Follower 都从 Leader 复制过去才会被认为已提交。
      * 这样就避免了部分数据被写进了Leader，还没来得及被任何 Follower 复制就宕机了，而造成数据丢失(Consumer 无法消费这些数据)。
      * 而对于Producer 而言，它可以选择是否等待消息 commit，这可以通过 acks 来设置。这种机制确保了只要 ISR 有一个或以上的 Follower，一条被 commit 的消息就不会丢失

### 数据的同步过程

* 需要解决：
  1. 怎么传播消息
  2. 在向消息发送端返回 ack 之前需要保证多少个 Replica 已经接收到这个消息

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
  * 如采用同步复制，需要所有能工作的Follower副本都复制完后消息才算是提交成功，一个Follower副本出现故障则导致HW无法完成递增，消息则提交失败，故障的Follower副本会拖慢整个系统性能导致不可用
  * 如采用异步复制，leader 副本收到生产者推送的消息后，就认为此消息提交成功，follower 副本则异步从 leader 副本同步。但如果有follower 副本的同步速度比较慢从而导致其消息量远落后于leader副本，则从Follower副本中选举出来的新leader则存在消息丢失
  * 权衡同步和异步策略，采用ISR集合
    * 当follower副本同步延迟过高则leader将该follower副本踢出ISR集合，消息依然可以快速提交
    * 新leader优先从ISR集合中follower中选举，新leader副本包含了HW之前全部消息，避免了消息丢失



