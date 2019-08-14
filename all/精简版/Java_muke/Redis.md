* 缓存中间件—Memcache和Redis的区别

  * Memcache代码层次类似Hash
    * 支持简单数据类型、不支持数据持久化存储、不支持主从、不支持分片
  * Redis
    * 数据类型丰富、支持数据磁盘持久化存储、支持主从、支持分片

* Redis为什么这么快

  * 完全基于内存，绝大部分请求是纯粹的内存操作，执行效率高
  * 数据结构简单，对数据操作也简单
  * 采用单线程，单线程也能处理高并发请求，想多核也可启动多实例
    * 主线程是单线程，包括IO事件的处理、IO对应相关业务的处理、主线程还负责过期键的处理、复制协调、集群协调，这些除了IO事件之外的逻辑会被封装成周期性的任务由主线程周期性的处理，正因为采用单线程的处理，对于客户端的所有读写请求都由一个主线程串行处理，因此多个客户端同时对一个键进行写操作时就不会有并发的问题，避免了频繁的上下文切换和锁竞争，使得redis执行起来效率更高
  * 使用多路I/O复用模型，非阻塞IO

* 多路I/O复用模型

  * FD：File Descriptor，文件描述符

    * 一个打开的文件通过唯一的描述符进行引用，该描述符是打开文件的元数据到文件本身的映射

  * Select函数

    * 可以同时监控并返回多个文件描述符的读写情况

    ![image-20190808230006421](/Users/dingyuanjie/Documents/study/github/woodyprogram/img/image-20190808230006421.png)

* Redis的数据类型

  * String，值最大512M，二进制安全（图片等），set
  * Hash，String元素组成的字典，适合用于存储对象，hset
  * List，按照String元素插入顺序排序，lpush
  * Set，String元素组成的无序集合，通过哈希表实现，不允许重复，sadd
  * Sorted Set，通过分数来为集合中的成员进行从小到大的排序，zadd

* 从海量key里查询出某一固定前缀的Key

  * > 批量生成redis测试数据
    > 1.Linux Bash下面执行
    >   for((i=1;i<=20000000;i++)); do echo "set k$i v$i" >> /tmp/redisTest.txt ;done;
    >   生成2千万条redis批量设置kv的语句(key=kn,value=vn)写入到/tmp目录下的redisTest.txt文件中
    > 2.用vim去掉行尾的^M符号，使用方式如下：：
    >   vim /tmp/redisTest.txt
    >     :set fileformat=dos #设置文件的格式，通过这句话去掉每行结尾的^M符号
    >     ::wq #保存退出
    > 3.通过redis提供的管道--pipe形式，去跑redis，传入文件的指令批量灌数据，需要花10分钟左右
    >   cat /tmp/redisTest.txt | 路径/redis-5.0.0/src/redis-cli -h 主机ip -p 端口号 --pipe

  * 摸清数据规模，即问清楚边界，`dbsize返回key的数量`

  * keys pattern：查找所有符合给定模式pattern的key

    * keys指令一次性返回所有匹配的key
    * 键的数量过大会导致服务卡顿

  * SCAN cursor [MATCH pattern] [COUNT count]

    ![image-20190808232549071](/Users/dingyuanjie/Documents/study/github/woodyprogram/img/image-20190808232549071.png)

    * `scan 0 match k1* count 10`

      * 0：开始迭代

        ![image-20190808232453790](/Users/dingyuanjie/Documents/study/github/woodyprogram/img/image-20190808232453790.png)

* 如何通过redis实现分布式锁

  * 互斥性、安全性、死锁、容错
  * SETNX key value，如果key不存在则创建并赋值，时间复杂度O(1)，设置成功返回1，失败返回0
  * EXPIRE key time，单独都是原子操作，前后执行就可能不是原子操作

  ![image-20190808233250886](/Users/dingyuanjie/Documents/study/github/woodyprogram/img/image-20190808233250886.png)

  ![image-20190808233444983](/Users/dingyuanjie/Documents/study/github/woodyprogram/img/image-20190808233444983.png)

  ![image-20190808233513459](/Users/dingyuanjie/Documents/study/github/woodyprogram/img/image-20190808233513459.png)

* 大量的key同时过期的注意事项

  * 集中过期，由于清除大量的key很耗时，会出现短暂的卡顿现象
  * 解决方案：在设置key的过期时间时，给每个key加上随机值

* 如何使用Redis做异步队列

  * 使用List做队列，RPUSH生产消息，LPOP消费消息
    * 缺点：没有等待队列里有值就直接消费
    * 弥补：可以通过在应用层引入Sleep机制去调用LPOP重试
  * BLPOP key [key ...] timeout
    * 阻塞直到队列有消息或者超时
    * 缺点：只能供一个消费者消费
  * pub/sub：主题订阅者模式
    * 订阅者可以订阅多个频道
      * `subscribe topic`
      * `publish topic “hello”`
    * 缺点：消息的发布是无状态的，无法保证可达

* Redis如何持久化

  ![image-20190808235124447](/Users/dingyuanjie/Documents/study/github/woodyprogram/img/image-20190808235124447.png)

  `lastsave`返回最近一次持久化成功的时间

  ![image-20190808235322982](/Users/dingyuanjie/Documents/study/github/woodyprogram/img/image-20190808235322982.png)

  ![image-20190809162352367](/Users/dingyuanjie/Documents/study/github/woodyprogram/img/image-20190809162352367.png)
  
  ![image-20190809162524260](/Users/dingyuanjie/Documents/study/github/woodyprogram/img/image-20190809162524260.png)
  
  ![image-20190809162807869](/Users/dingyuanjie/Documents/study/github/woodyprogram/img/image-20190809162807869.png)
  
* ![image-20190809162950035](/Users/dingyuanjie/Documents/study/github/woodyprogram/img/image-20190809162950035.png)

  * `redis.conf`

    * `appendonly yes`
    * `appendfsync everysec`

  * `config set appendonly yes`

    ![image-20190809163512426](/Users/dingyuanjie/Documents/study/github/woodyprogram/img/image-20190809163512426.png)

  * ![image-20190809163555684](/Users/dingyuanjie/Documents/study/github/woodyprogram/img/image-20190809163555684.png)

  * ![image-20190809163705223](/Users/dingyuanjie/Documents/study/github/woodyprogram/img/image-20190809163705223.png)

  * RDB - AOF混合持久化方式

    * BGSAVE做镜像全量持久化，AOF做增量持久化

* 使用Pipeline的好处

  * Pipeline和Linux的管道类似
  * ![image-20190809164141352](/Users/dingyuanjie/Documents/study/github/woodyprogram/img/image-20190809164141352.png)

* Redis的同步机制

  * 主从同步原理

    * 全同步过程

      ![image-20190809164425012](/Users/dingyuanjie/Documents/study/github/woodyprogram/img/image-20190809164425012.png)

    * 增量同步过程

      ![image-20190809164540093](/Users/dingyuanjie/Documents/study/github/woodyprogram/img/image-20190809164540093.png)

    * ![image-20190809164659256](/Users/dingyuanjie/Documents/study/github/woodyprogram/img/image-20190809164659256.png)

    * ![image-20190809164808371](/Users/dingyuanjie/Documents/study/github/woodyprogram/img/image-20190809164808371.png)

    * ![image-20190809164930580](/Users/dingyuanjie/Documents/study/github/woodyprogram/img/image-20190809164930580.png)

      * 一致性Hash算法：对2^32取模，将Hash值空间组织成虚拟的圆环
      * ![image-20190809165236475](/Users/dingyuanjie/Documents/study/github/woodyprogram/img/image-20190809165236475.png)
      * 取服务器的主机名或IP进行Hash以确定每台服务器在Hash环上的位置
      * 对数据进行同样的hash算法去定位访问到相应的服务器，对数据key使用和服务器IP使用的相同的Hash函数计算出Hash值确定在环上的位置，顺时针遇到的第一个服务器即存储位置
      * 如果某个Node宕机，影响的是与其逆时针方向Node之间的数据，则其上数据会继续顺时针方向存储到相邻的节点上，影响最小化
      * 如果新增一个Node，受影响的是与其逆时针方向Node之间的数据，会存储到新的Node上
      * Hash环的数据倾斜问题：环上服务器数量少，大部分数据都存储在某一台上
        * 引入虚拟节点解决数据倾斜的问题
          * 对每一个服务器节点计算多个Hash，服务器节点的服务名或IP后增加编号