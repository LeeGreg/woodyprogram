# MS

- 如何保证缓存里都是热点数据？
  - 可以将内存最大使用量设置为热点数据占用的内存量，然后启用allkeys-lru淘汰策略，将最近最少使用的数据淘汰
- 为什么使用Redis而不是使用Map做缓存
  - 应用内缓存（如Map）和组件缓存（如Redis、Memached）
  - 以java为例，使用自带的map实现的是应用内缓存，最主要的特点是轻量以及快速，生命周期随着jvm的销毁而结束，并且在多实例的情况下，每个实例都需要各自保存一份缓存，缓存不具有一致性
  - 使用redis或memcached之类的称为组件缓存，在多实例的情况下，各实例共用一份缓存数据，缓存具有一致性。缺点是需要保持redis或memcached服务的高可用，整个程序架构上较为复杂
- 如果在setnx之后执行expire之前进程意外crash或者要重启维护了，那会怎么样？
  - 先拿setnx来争抢锁，抢到之后，再用expire给锁加一个过期时间防止锁忘记了释放
  - Set有同时把setnx和expire合成一条指令来用的指令
  - 针对字符串独有的过期时间设置方式`setex(String key,int seconds,String value)`
- 假如Redis里面有1亿个key，其中有10w个key是以某个固定的已知的前缀开头的，如果将它们全部找出来？
  - 使用keys指令可以扫出指定模式的key列表
  - redis的单线程的。keys指令会导致线程阻塞一段时间，线上服务会停顿，直到指令执行完毕，服务才能恢复
  - 这个时候可以使用`scan`指令，`scan`指令可以无阻塞的提取出指定模式的key列表，但是会有一定的重复概率，在客户端做一次去重就可以了，但是整体所花费的时间会比直接用keys指令长
- 使用过Redis做异步队列么，你是怎么用的？
  - 一般使用list结构作为队列，`rpush`生产消息，`lpop`消费消息。当lpop没有消息的时候，要适当sleep一会再重试
  - 不用sleep，list还有个指令叫`blpop`，在没有消息的时候，它会阻塞住直到消息到来
  - 生产一次消费多次，使用pub/sub主题订阅者模式，可以实现1:N的消息队列；消费者下线的情况下，生产的消息会丢失，得使用专业的消息队列如rabbitmq等
- redis如何实现延时队列？
  - 使用sortedset，拿时间戳作为score，消息内容作为key调用zadd来生产消息，消费者用zrangebyscore指令获取N秒之前的数据轮询进行处理
- 持久化时突然断电？
  - 取决于aof日志sync属性的配置，如果不要求性能，在每条写指令时都sync一下磁盘，就不会丢失数据。但是在高性能的要求下每次都sync是不现实的，一般都使用定时sync，比如1s1次，这个时候最多就会丢失1s的数据
- bgsave的原理是什么？
  - fork是指redis通过创建子进程来进行bgsave操作，cow指的是copy on write，子进程创建后，父子进程共享数据段，父进程继续提供读写服务，写脏的页面数据会逐渐和子进程分离开来
- Pipeline有什么好处，为什么要用pipeline？
  - 可以将多次IO往返的时间缩减为一次，前提是pipeline执行的指令之间没有因果相关性
- 是否使用过Redis集群，集群的原理是什么？
  - Redis Sentinal着眼于高可用，在master宕机时会自动将slave提升为master，继续提供服务
  - Redis Cluster着眼于扩展性，在单个redis内存不足时，使用Cluster进行分片存储
- Redis相比memcached有哪些优势？
  - memcached所有的值均是简单的字符串，redis作为其替代者，支持更为丰富的数据类型
  - redis的速度比memcached快很多
  - redis可以持久化其数据
- 修改配置不重启Redis会实时生效吗？
  - 针对运行实例，有许多配置选项可以通过 CONFIG SET 命令进行修改，而无需执行任何形式的重启
- Redis常见性能问题和解决方案？
  - Master最好不要做任何持久化工作，如RDB内存快照和AOF日志文件
  - 如果数据比较重要，某个Slave开启AOF备份数据，策略设置为每秒同步一次
- Redis回收使用的是什么算法？
  - LRU算法
- 请用Redis和任意语言实现一段恶意登录保护的代码，限制1小时内每用户Id最多只能登录5次。具体登录函数或功能用空函数即可，不用详细写出。
  - 用列表实现:列表中每个元素代表登陆时间,只要最后的第5次登陆时间和现在时间差不超过1小时就禁止登陆

# Redis

## 简介

- 定义：
  - 是单线程、基于内存存取的缓存中间件，是一个基于KEY-VALUE的高性能的存储系统，通过提供多种键值数据类型来适应不同场景下的缓存与存储需求
  - 缓存：应用内缓存（如Map）和组件缓存（如Redis、Memached）
  - 字典类型的数据结构，比如map ，通过key value的方式存储的结构
  - redis的全称是remotedictionary server(远程字典服务器)，它以字典结构存储数据，并允许其他应用通过TCP协议读写字典中的内容
- 为什么使用缓存
  - 一般数据库的读写都是要经过**磁盘**的，而磁盘的速度可以说是相当慢的(相对内存来说)
- 高性能原因：
  - 基于内存操作
  - 核心是基于非阻塞的IO多路复用机制
  - 单线程避免了多线程的频繁上下文切换问题，同一时刻它只能处理一个客户端请求，避免了线程安全问题
  - 单线程，多路复用机制，接收所有请求，一个线程（复用）（redis服务端）可以处理很多IO请求，节省上下文切换的开销。
    - Redis为单进程单线程模式，采用队列模式将并发访问变为串行访问
    - 劣势：不能很好利用多核CPU资源
    - redis为什么是单线程? 
      - 官方的解释是：CPU并不是Redis的瓶颈所在，Redis的瓶颈主要在机器的内存和网络的带宽

## 应用场景

- 数据缓存、单点登录、秒杀、抢购、网站访问排名、计数器、队列

- 应用和数据库之间==读操作==

  - 先从缓存中查找，如果有，则返回数据；如果没有，则去数据库查找，数据缓存，返回数据

- ==数据一致性问题：缓存与数据库双写一致问题==

  - 数据库数据和缓存数据一致性问题（业务问题）——  最终一致性
    1. 更新缓存还是让缓存失效
       - 如果更新缓存代价小（前置操作复杂），则先更新缓存，否则让缓存失效，后续的请求如果在缓存中找不到，自然去数据库中检索
       - 一般我们都是采取**删除缓存**缓存策略的，原因如下：
         1. 高并发环境下，无论是先操作数据库还是后操作数据库而言，如果加上更新缓存，那就**更加容易**导致数据库与缓存数据不一致问题。(删除缓存**直接和简单**很多)
         2. 如果每次更新了数据库，都要更新缓存【这里指的是频繁更新的场景，这会耗费一定的性能】，倒不如直接删除掉。等再次读取时，缓存里没有，那我到数据库找，在数据库找到再写到缓存里边(体现**懒加载**)
    2. 先操作数据库还是缓存
       - 当客户端发起事务类型请求时，假设我们以让缓存失效作为缓存的的处理方式，那么又会存在两个情况
         - 先更新数据库再让缓存失效
           - 第一步成功(操作数据库)，第二步失败(删除缓存)，会导致**数据库里是新数据，而缓存里是旧数据**
           - 如果第一步(操作数据库)就失败了，我们可以直接返回错误(Exception)，不会出现数据不一致
           - 在高并发下表现优异，在原子性被破坏时表现不如意
         - 先让缓存失效，再更新数据库
           - 第一步成功(删除缓存)，第二步失败(更新数据库)，数据库和缓存的数据还是一致的。
           - 如果第一步(删除缓存)就失败了，我们可以直接返回错误(Exception)，数据库和缓存的数据还是一致的。
           - 在高并发下表现不如意，在原子性被破坏时表现优异
       - 更新数据库和更新缓存这两个操作，是无法保证原子性的，所以我们需要根据当前业务的场景的容忍性来选择。也就是如果出现不一致的情况下，哪一种更新方式对业务的影响最小，就先执行影响最小的方案
    3. 最终一致性
       - App：1-更新数据库，2-让缓存失效
       - 如果缓存失效失败，则将这个消息添加到消息中间件MQ，下次让App再次进行缓存失效操作
  - 先删除缓存，再更新数据库
    - 问题:如果存在并发操作，一个更新数据库，另一个查询数据库，如果没有在缓存中查找到数据，那么就会到数据库中查找，而数据库更新操作还没有完全更新的数据时，缓存又load老的数据，会造成后续缓存中的数据都是脏数据
  - 两个不同的存储要保证强⼀致性:
    - 要么2pc、3pc强一致性提交协议，或者..算法完成强一致性,但是不可能牺性能去完成强一致性
  - 先更新数据库，更新成功后，让缓存失效
    - 存在脏数据的可能性⽐第一种低很多
  - 更新数据的时候，只更新缓存，不更新数据库，然后同步异步调度去批量更新数据库
    - 性能会提⾼，但是不是强一致性

- ==缓存雪崩==

  - 指设置缓存时采用了相同的过期时间，导致缓存在某一个时刻同时失效，或者缓存服务器宕机导致
    - 缓存全面失效，请求全部转发到了DB层面，DB由于瞬间压力增大而导致崩溃

  1. 对缓存的访问，如果发现从缓存中取不到值，那么通过加锁或者队列的方式保证缓存的单进程操作，从而避免失效时并发请求全部落到底层的存储系统上；但是这种方式会带来性能上的损耗
     - 当某个key的缓存失效后，不是立即去load数据，而是使用readis提供的setnx去设置锁，获得锁后才能去load数据，load完后再设置到缓存里面，后续所有的操作都会排队等待。如果后续通过get得到值后，再从缓存取
  2. 将缓存失效的时间分散，降低每一个缓存过期时间的重复率
  3. 如果是因为缓存服务器故障导致的问题，一方面需要保证缓存服务器的高可用、另一方面，应用程序中可以采用多级缓存 

- ==缓存穿透==

  - 访问量⾮常⾼情形，请求的数据在缓存大量不命中，导致请求走数据库
    - 查询一个根本不存在的数据，缓存和数据源都不会命中
    - 查询时，很多key不存在，恶意攻击，所有的请求都会到数据库层
    - 可能会使后端数据源负载加大，由于很多后端数据源不具备高并发性，甚至可能造成后端数据源宕掉

  1. 如果查询数据库也为空，直接设置一个默认值存放到缓存，这样第二次到缓冲中获取就有值了，而不会继续访问数据库，这种办法最简单粗暴。比如，”key” , “&&” 

     - 在返回这个&&值的时候，我们的应用就可以认为这是不存在的key，那我们的应用就可以决定是否继续等待继续访问，还是放弃掉这次操作。如果继续等待访问，过一个时间轮询点后，再次请求这个key，如果取到的值不再是&&，则可以认为这时候key有值了，从而避免了透传到数据库，从而把大量的类似请求挡在了缓存之中

  2. 设置key规则，如果请求的key不符合规则，则不请求到数据库

  3. 布隆过滤器

     - 将所有可能存在的数据哈希到一个足够大的BitSet中，不存在的数据将会被拦截掉，从而避免了对底层存储系统的查询压力

     - 一种空间效率极高的概率型算法和数据结构，主要用来判断一个元素是否在集合中存在

     - 先到布隆过滤器中查找key，如果存在key，则到缓存中请求

       ```java
       //Google
       BloomFilter bloomFilter=BloomFilter.create(Funnels.stringFunnel(Charset.defaultCharset()), 1000000, 0.001);   //容错率
       bloomFilter.put("mic");
       System.out.println(bloomFilter.mightContain("mic"));
       ```

- redis完全挂掉

  - 高可用方案、redis集群
  - 多级缓存
    - 不同层级存储不同类型的数据，如果有一级挂了，其他的作为支撑

- ==热点key==

  - 缓存中的某些Key(可能对应用与某个促销商品)对应的value存储在集群中一台机器，使得所有流量涌向同一机器，成为系统的瓶颈，该问题的挑战在于它无法通过增加机器容量来解决。

  1. 客户端热点key缓存：将热点key对应value并缓存在客户端本地，并且设置一个失效时间
  2. 将热点key分散为多个子key，然后存储到缓存集群的不同机器上，这些子key对应的value都和热点key是一样的

- 消息队列

  - 是为了解决**生产和消费的速度不一致**导致的问题
  - 好处
    1. 减少请求响应时间。比如注册功能需要调用第三方接口来发短信，如果等待第三方响应可能会需要很多时间
    2. 服务之间解耦。主服务只关心核心的流程，其他不重要的、耗费时间流程是否如何处理完成不需要知道，只通知即可
    3. 流量削锋。对于不需要实时处理的请求来说，当并发量特别大的时候，可以先在消息队列中作缓存，然后陆续发送给对应的服务去处理

## 安装使用

- `4.0.10`、`6379`

- 进入`bin`，指定配置文件启动`./redis-server ../conf/redis.conf`

- 使用密码连接：`./redis-cli -a qwer6379`

- `./redis-cli [-h 127.0.0.1 -p 6379]`

- `./redis-cli shutdown`

  ```shell
  # 获得这个key的数据结构类型
  type key
  
  # --port可以自定义端口
  redis-server --port 6380
  
  # 配置文件配置
  redis.conf
  #以守护进程的方式启动
  daemonize yes
  protected-mode no 
  #bind ...
  requirepass qwer6379
  ```

## 数据类型

- Redis中的key一定是字符串，value可以是string、list、hash、set、sortset这几种常用的

- 字符串

  - key value，默认存储最大容量512M

  - 能存储任何形式的字符串，包括二进制数据

    ```shell
    # 递增数字
    incr key
    # 递增指定的整数
    incryby key increment
    # 原子递减
    decr key
    # 向指定的key追加字符串
    append key value
    # 获得key对应的value的⻓度
    strlen key
    # 同时获得多个key的value
    mget key key..
    mset key value key value key value ...
    # setnx在指定的 key 不存在时，为 key 设置指定的值,设置成功，返回 1 。 设置失败，返回 0
    setnx key value
    # ttl以秒为单位返回 key 的剩余过期时间,当 key 不存在时，返回 -2 。 当 key 存在但没有设置剩余生存时间时，返回 -1 否则，以毫秒为单位，返回 key 的剩余⽣存时间
    ttl key
    ```

  - 使用场景

    - session共享、ip限制、手机号限制（短信验证）
    - namespace——key命名上体现namespace

- 列表类型

  - list，可以存储一个有序的字符串列表

  - 常用的操作是向列表两端添加元素或者获得列表的某一个片段

  - 内部使用双向链表实现，所以向列表两端添加元素的时间复杂度为O(1), 获取越接近两端的元素速度就越快

  - 应⽤场景：

    - 可以⽤来做分布式消息队列，生产者lpush消息到list中，消费者们brpop消息
      - brpop：移出并获取列表的最后一个元素， 如果列表没有元素会阻塞列表直到等待超时或发现可弹出元素为止。`BRPOP LIST1 LIST2 .. LISTN TIMEOUT `
      - 假如在指定时间内没有任何元素被弹出，则返回一个 nil 和等待时长。 反之，返回一个含有两个元素的列表，第一个元素是被弹出元素所属的 key ，第二个元素是被弹出元素的值。
    - 栈，后进先出，lpush、lpop
    - 队列，先进先出，lpush、rpop

  - key——（value、value、value....）

    ```shell
    lpush mylist hello
    rpush mylist world
    # 取数据(移除数据，同时返回被移除数据的值)
    lpop mylist —— hello
    lpop mylist —— world
    # 都取出来了，mylist为空
    lrange mylist 0 -1
    # 获得列表的长度
    llen num
    # 设置
    lset key index value
    # 删除
    lrem key count value
    ```

- 散列类型（Hash类型）

  - 整体看作一个对象，每个filed-value相当于对象的属性和属性值

  - key——table（field-value、field-value、field-value）value不支持其他类型嵌套

  - 比较适合存储对象

    ```shell
    # hset key field value
    hset userinfo age 18
    hset userinfo name mic
    hset userinfo sex male
    # hget key filed
    hget userinfo name
    hgetall userinfo
    # 判断字段是否存在。 存在返回1. 不存在返回0
    # hexists key field
    hexists userinfo birthday
    # 一次性设置多个值
    hmset key filed value [filed value ...]
    hmset user name zhangsan age 25 sex male
    # ⼀次性获得多个值
    hmget key field field ... 
    hmget user name age sex
    # 获得hash的所有信息，包括key和value
    hgetall key
    # 删除一个或者多个字段
    hdel key field [field ...]
    # hincryby
    # hsetnx
    ```

- 集合类型

  - set，无序且唯一

  - 常用操作是向集合中加入或删除元素、判断某个元素是否存在

  - 由于集合类型在redis内部是使用的值为空的散列表(hash table)，所以这些操作的时间复杂度都是O(1) 

  - 一个集合类型键可以存储至多232-1个 。集合类型和列表类型的最大的区别是有序性和唯一性 

  - 使用场景：打标签，根据不同标签采取不同操作、交集、并集

    ```shell
    # 增加数据，如果value已经存在，则会忽略存在的值，并且返回成功加入的元素的数量
    sadd key member [member ...] 
    sadd bbs "bbs.gupaoedu.com" "csdn.com"
    # 获得所有数据
    smembers bbs
    # 删除元素
    srem key member
    # 对多个集合执行差集运算
    sdiff key key ...
    # 对多个集合执行并集操作, 同时存在两个集合里的所有值
    sunion
    ```

- 有序集合

  - key——(value，score)

  - 为集合中的每个元素都关联了一个分数，这使得不仅可以完成插入、删除和判断元素是否存在等集合类型支持的操作，还能获得分数最高(或最低)的前N个元素、获得指定分数范围内的元素等与分数有关的操作

  - 虽然集合中每个元素都是不同的，但是它们的分数却可以相同

  - 如果两个元素的score是相同的话，那么根据(0<9<A<Z<a<z) ⽅式从小到⼤

    ```shell
    # 决定元素顺序
    # zadd key score member score
    zadd page_rank 9 google.com 8 baidu.com 7 bing.com
    zscore page_rank google.com
    # 去获得元素，withscores是可以获得元素的分数
    # zrange key start stop [withscores] 
    zrange page_rank 0 3 with score
    zrevrange page_rank 0 -1 withscores
    ```

## 事务处理

- 事务提供了一种“将多个命令打包， 然后一次性、按顺序地执行”的机制， 并且事务在执行的期间不会主动中断 —— 服务器在执行完事务中的所有命令之后， 才会继续处理其他客户端的其他命令
- 一个事务从开始到执行会经历以下三个阶段：
  1. 开始事务，先以 MULTI 开始一个事务
     - 让客户端从非事务状态切换到事务状态
       - 当客户端处于非事务状态下时， 所有发送给服务器端的命令都会立即被服务器执行
       - 当客户端进入事务状态之后， 服务器在收到来自客户端的命令时， 不会立即执行命令， 而是将这些命令全部放进一个事务队列里， 然后返回 `QUEUED` ， 表示命令已入队
     - 非事务状态下的命令以单个命令为单位执行，前一个命令和后一个命令的客户端不一定是同一个
     - 在非事务状态下，执行命令所得的结果会立即被返回给客户端
     - 事务则是将所有命令的结果集合到回复队列，再作为 EXEC命令的结果返回给客户端
  2. 命令入队，多个命令入队到事务中
  3. 执行事务，EXEC 命令触发事务
- ACID
  - Redis 事务保证了其中的一致性（C）和隔离性（I），但并不保证原子性（A）和持久性（D）
  - 原子性
    - 单个 Redis 命令的执行是原子性的
    - 如果 Redis 服务器进程在执行事务的过程中被停止 —— 比如接到 KILL 信号、宿主机器停机，等等，那么事务执行失败，Redis 也不会进行任何的重试或者回滚动作
  - 一致性
    - 带有不正确入队命令的事务不会被执行，也不会影响数据库的一致性
    - 如果命令在事务执行的过程中发生错误，比如说，对一个不同类型的 key 执行了错误的操作， 那么 Redis 只会将错误包含在事务的结果中， 这不会引起事务中断或整个失败，不会影响已执行事务命令的结果，也不会影响后面要执行的事务命令， 所以它对事务的一致性也没有影响
  - 隔离性
    - Redis 是单进程程序，并且它保证在执行事务时，不会对事务进行中断，事务可以运行直到执行完所有事务队列中的命令为止。因此，Redis 的事务是总是带有隔离性的
  - 持久性
    - 因为事务不过是用队列包裹起了一组 Redis 命令，并没有提供任何额外的持久性功能，所以事务的持久性由 Redis 所使用的持久化模式决定
      - 在单纯的内存模式下，事务肯定是不持久的
      - 在 RDB 模式下，服务器可能在事务执行之后、RDB 文件更新之前的这段时间失败，所以 RDB 模式下的 Redis 事务也是不持久的
      - 在 AOF 的“总是 SYNC ”模式下，事务的每条命令在执行成功之后，都会立即调用 `fsync` 或 `fdatasync` 将事务数据写入到 AOF 文件。但是，这种保存是由后台线程进行的，主线程不会阻塞直到保存成功，所以从命令执行成功到数据保存到硬盘之间，还是有一段非常小的间隔，所以这种模式下的事务也是不持久的

## 过期时间

```shell
# EXPIRE 返回值为1表示设置成功，0表示设置失败或者键不存在
expire key seconds
# 针对字符串独有的过期时间设置方式
setex(String key,int seconds,String value)
# 当前key是否设置过期时间或过期剩余秒 -1没设置、-2key不存在
ttl key
# 以毫秒为单位返回键的剩余生存时间
PTTL key
#取消键的过期时间设置(使该键恢复成为永久的)，成功返回1，否则返回0(键不存在或者本身就是永久的)
PERSIST key
```

- 过期策略
  - 消极方法：访问的时候发现不存在，则删除
  - 积极方法：周期性的从设置了过期时间的key中选择一部分的key进行删除
    - 对于那些从未被查询的key，即便它们已经过期，被动方式也无法清除。因此Redis会周期性地随机测试一些key， 已过期的key将会被删掉。Redis每秒会进行10次操作，具体的流程:
      1. 随机测试20个带有timeout信息的key
      2. 删除其中已经过期的key;
      3. 如果超过25%的key被删除，则重复执行整个流程
         这是一个简单的概率算法(trivial probabilistic algorithm)，基于假设我们随机抽取的key代表了全部的key空间

## 订阅发布

- 可以用于消息的传输、该模式同样可以实现进程间的消息传递

- 原理

  - 发布/订阅模式包含两种角色，分别是发布者和订阅者。订阅者可以订阅一个或多个频道，而发布者可以向指定的频道发送消息，所有订阅此频道的订阅者都会收到该消息
  - `producer——channel——consumer`

  ```shell
  # 发布者发布消息的命令是PUBLISH，该命令的返回值表示接收到这条消息的订阅者数量
  # 消息发送出去不会持久化，如果发送之前没有订阅者，那么后续再有订阅者订阅该频道，之前的消息就收不到了
  # PUBLISH channel message
  publish channle.mic hello
  
  # 订阅者订阅消息的命令是，SUBSCRIBE channel [channel ...]
  # 执行SUBSCRIBE命令后客户端会进入订阅状态
  subscribe channel.mic
  ```

  - redis的发布订阅不支持其他消息中间键所能支持的协议、持久化
  - channel分两类，一个是普通channel、另一个是pattern channel(规则匹配)
  - producer1发布了一条消息【publish abc hello】,redis server发给abc这个普通channel上的所有订阅者，同时abc也匹配上了pattern channel的名字，所以这条消息也会同时发送给pattern channel *bc上的所有订阅者

## 数据持久化

### RDB

- 根据指定的规则“定时”将内存中的数据存储在硬盘上`dump.rdb`

- 原理：

  1. redis使用fork函数复制一份当前进程的副本(子进程)，fork进程负责把内存的数据同步到磁盘的临时⽂件，父进程继续处理客户端请求
  2. 父进程继续接收并处理客户端发来的命令，而子进程开始将内存中的数据写入硬盘中的临时文件
  3. 当子进程写入完所有数据后会用该临时文件替换旧的RDB文件，至此，一次快照操作完成。

- redis在进行快照的过程中不会修改RDB文件，只有快照结束后才会将旧的文件替换成新的

- RDB文件是经过压缩的二进制⽂件，占用的空间会小于内存中的数据，更加利于传输

- 如需要进行大规模数据的恢复，且对于数据恢复的完整性不是非常敏感，那RDB方式要比AOF方式更加的高效

  缺点是最后一次持久化后的数据可能丢失

- 快照方式，当符合【条件】时，reids会fork子进程（不会影响主线程）把数据写进磁盘，写完后生成dump.rdb快照文件

  1. 根据配置规则进行自动快照：redis.conf

     ```shell
     # save 时间窗口	键的个数
     # 在第一个时间参数配置范围内被更改的键的个数大于后面 的changes时，即符合快照条件
     # 每条规则之间是“或”的关系
     save seconds changes 
     save 900 1 #900秒内有1个key的值被改变就会触发快照
     save 300 10
     save 60 10000
     ```

  2. 用户执行SAVE或者GBSAVE命令

     - 当执行save命令时，Redis同步做快照操作，在快照执行过程中会阻塞所有来自客户端的请求
     - bgsave可以在后台异步地进行快照操作，快照的同时服务器还可以继续响应来自客户端的请求
     - 通过LASTSAVE命令可以获取最近一次成功执行快照的时间; (自动快照采用的是异步快照操作)
     - 执行`SAVE`或者`BGSAVE`命令创建出的RDB文件，程序会对数据库中的过期键检查，**已过期的键不会保存在RDB文件中**。
     - 载入RDB文件时，程序同样会对RDB文件中的键进行检查，**过期的键会被忽略**。

  3. 执行FLUSHALL命令

     - 会清除redis在内存中的所有数据
     - 执行该命令后，只要redis中配置的快照规则不为空，也就是save 的规则存在。redis就会执行一次快照操作。不管规则是什么样的都会执行。如果没有定义快照规则，就不会执行快照操作

  4. 执行复制操作

     - 在主从模式下，redis会在复制初始化时进行自动快照
     - 当执行复制操作时，即使没有定义自动快照规则，并且没有手动执行过快照操作，它仍然会生成RDB快照文件

- 缺点：

  - 使用RDB⽅式实现持久化，一旦Redis异常退出，就会丢失最后一次快照以后更改的所有数据

- 优点：

  - RDB可以最大化Redis的性能，⽗进程⽆需执行任何磁盘I/O操作，fork出的⼦进程会处理接下来的所有保存工作，但是，如果数据集比较⼤的时候，fork可能⽐较耗时，CPU压力过大，造成服务器在一段时间内停止处理客户端的请求
  - 数据恢复速度比aof方式快

### AOF

- `append-only-file`，每次执行命令后将命令本身记录下来，`appendonly.aof`

- redis.conf中打开AOP：`appendonly yes`

- AOF文件的保存位置和RDB文件的位置相同，都是通过dir参数设置的，默认的文件名是apendonly.aof. 

- 对于同一个key，只需要记录最后一次数据变更指令就行，所以需要重写aof文件

  - Redis 可以在 AOF 文件体积变得过大时，自动地在后台对 AOF 进行重写：重写后的新 AOF 文件包含了恢复当前数据集所需的最小命令集合
  - 重写原理：主进程会fork一个子进程出来进行AOF重写，这个重写过程并不是基于原有的aof文件来做的，而是有点类似于快照的方式，全量遍历内存中的数据，然后逐个序列到aof文件中。在fork子进程这个过程中，服务端仍然可以对外提供服务，这个过程中，主进程的数据更新操作，会缓存到aof_rewrite_buf中，也就是单独开辟一块缓存来存储重写期间收到的命令，当子进程重写完以后再把缓存中的数据追加到新的aof文件（只有当rewrite完成后才会切换文件）
  - 重写AOF文件时，程序会对RDB文件中的键进行检查，**过期的键会被忽略**
  - 如果数据库的键已过期，但还没被惰性/定期删除，AOF文件不会因为这个过期键产生任何影响(也就说会保留)，当过期的键被删除了以后，会追加一条DEL命令来显示记录该键被删除了

  ```shell
  # redis每次更改数据的时候， aof机制都会将命令记录到aof文件，但是实际上由于操作系统的缓存机制，数据并没有实时写入到硬盘，而是进入硬盘缓存。再通过硬盘缓存机制去刷新到保存到文件
  appendfsync always    # 每次执行写入都会进行同步，这个是最安全但是是效率比较低的方式
  appendfsync everysec  # 每秒从操作系统磁盘缓存的数据同步到文件里面
  appendfsync no		  # 不主动进行同步操作，由操作系统去执行，这个是最快但是最不安全的方式
  ```

  ```shell
  # aof文件重写配置
  # 可以配置一个条件，每当达到一定条件时Redis就会自动重写AOF文件，这个条件的配置为
  # 当目前的AOF文件大小超过上一次重写时的AOF文件大小的百分之多少时会 再次进行重写，如果之前没有重写过，则以启动时AOF文件大小为依据
  auto-aof-rewrite-percentage 100 
  # 限制了允许重写的最小AOF文件大小，通常在AOF文件很小的情况下即使其中有很多冗余的命令我们也并不太关心
  auto-aof-rewrite-min-size 64mb
  #重写aof文件时，会把新产生的数据存入重写缓存中，当aof重写完成后就会把重写缓存中的数据追加到aof文件后面
  ```

- 还可以通过BGREWRITEAOF 命令手动执行AOF，执行完以后冗余的命令已经被删除了

- 在启动时，Redis会逐个执行AOF文件中的命令来将硬盘中的数据载入到内存中，载入的速度相对于RDB会慢一些

- 同时使用RDB和AOF方式，可以保证数据安全，以及快速恢复数据

- RDB和AOF如何选择

  - RDB的优点：载入时**恢复数据快**、文件体积小
  - RDB的缺点：会一定程度上**丢失数据**(因为系统一旦在定时持久化之前出现宕机现象，此前没有来得及写入磁盘的数据都将丢失。)
  - AOF的优点：丢失数据少(默认配置只丢失一秒的数据)
  - AOF的缺点：恢复数据相对较慢，文件体积大

  1. ⼀般来说，如果对数据的安全性要求非常高的话，应该同时使用两种持久化功能
  2. 如果可以承受数分钟以内的数据丢失，那么可以只使用 RDB 持久化
  3. 有很多用户都只使用 AOF 持久化， 但并不推荐这种⽅式：定时⽣成 RDB 快照(snapshot)非常便于进⾏数据库备份， 并且RDB 恢复数据集的速度也要比 AOF 恢复的速度要快
  4. 两种持久化策略可以同时使用，也可以使⽤其中一种。如果同时使⽤的话， 那么Redis重启时，会优先使用AOF文件来还原数据（因为AOF更新频率比RDB更新频率要高，还原的数据更完善）

## 回收策略

- 默认策略为`maxmemory-policy noeviction`策略，当内存使用达到阈值的时候，所有引起申请内存的命令会报错
- allkeys-lru，从数据集(server.db[i].dict)中挑选最近最少使用的数据淘汰，应用对于缓存访问都是相对热点数据时使用
- alleys-random，随机移除某个key，应用对于缓存key的访问概率相等时使用
- 从已经设置了过期时间的数据集(server.db[i].expires)key中去选择：
  - volatile-random：随机选择
  - volatile-lru：最近最少使用的key去淘汰 ==访问的都是热点数据==
  - volatile-ttl：即将过期的数据进行淘汰

## Lua

- 是一个高效的轻量级脚本语言，==设计目的==是为了嵌⼊应用程序中，从而为应用程序提供灵活的扩展和定制功能

- 原子性问题：

  - redis是一个单线程程序，也就说同一时刻它只能处理一个客户端请求
  - 多个客户端的命令之间没有做请求同步，导致实际执行顺序可能会不一致，最终的结果也就无法满足原子性了
  - Redis作为数据服务器，是提供给多个客户端使用的。多个客户端的操作就相当于同一个进程下的多个线程，如果多个客户端之间没有做好数据的同步策略，就会产生数据不一致的问题
    - 不同的客户端读取了相同的数据，并各自设置了值，然后再各自存入
  - Redis提供了lua-time-limit参数限制脚本的最长运行时间。默认是5秒钟。超过返回BUSY的错误
  - lua保证原子性：`shutdown nosave`让redis服务器关掉，之前执行的脚本不生效

- 效率问题

  - 使用redis实现某些特定功能的时候，很可能需要多个命令或者多个数据类型的交互才能完成，那么这种多次网络请求对性能影响比较大
  - pipeline管道模型：减少网络开销去执行多个指令；满足原子性；复用性；但是它有一定的局限性，就是执行的多个命令和响应之间是不存在相互依赖关系的

- lua

  - Redis中内嵌了对Lua环境的支持，允许开发者使用Lua语言编写脚本传到Redis中执行，Redis客户端可以使用Lua 脚本，直接在服务端原子的执行多个Redis命令

- 使用脚本的好处:

  - 减少网络开销，在Lua脚本中可以把多个命令放在同一个脚本中运行

  - 原子操作，redis会将整个脚本作为一个整体执行，中间不会被其他命令插入

  - 复用性，客户端发送的脚本会永远存储在redis中，这意味着其他客户端可以复用这一脚本来完成同样的逻辑

    ```shell
    # lua的语法
    # 变量:全局变量、局部变量 a= 1; local b = 2;
    # 逻辑表达式：+-*/ % - a == b ~=不等于
    # 逻辑运算符：and or not
    #获取字符⻓长度 #”hello world”
    
    [root@CentOS bin]# lua
    > a=1
    > print(a)
    1
    > print(1==a)
    true
    > a='hello'
    > b='world'
    > print(a..b) helloworld
    > print(#"hello world") 11
    > for i=1,100 do
    >> print(i)
    >> end
    1
    2
    --[[...]]注释
    -- for循环
    local xx={"a","b","c"}
    for i,v in ipairs(xx) do print(v) end
    -- 函数
    > function add(a,b) >> return a+b; >> end
    > print(add(1,2))
    3
    >
    ```

- 在lua脚本中去执行redis命令——call

  - `redis.call`函数会将这5种类型的返回值转化对应的Lua的数据类型

    ```shell
    redis.call('set','gupao','123')
    local val=redis.call('get','gupao') 
    ```

- redis中执行lua脚本——eval

  - redis会自动将脚本返回值的Lua数据类型转化为Redis的返回值类型

  - 在脚本中可以使用return 语句将值返回给redis客户端，通过return语句来执行，如果没有执行return， 默认返回为nil

  - `[EVAL][脚本内容] [key参数的数量][key ...] [arg …]`

  - 可以通过key和arg这两个参数向脚本中传递数据，他们的值可以在脚本中分别使用KEYS和ARGV这两个类型的全局变量访问

    ```shell
    eval "return redis.call('get','gupao')" 0
    eval "return redis.call('set',KEYS[1],ARGV[1])" 1 mic val
    ```

- EVALSHA命令

  - 可以避免每次调用脚本都需要把整个脚本传给redis

  - 允许开发者通过脚本内容的SHA1摘要来执行脚本。该命令的用法和EVAL一样，只不过是将脚本内容替换成脚本内容的SHA1摘要

    1. Redis在执行EVAL命令时会计算脚本的SHA1摘要并记录在脚本缓存中
    2. 执行EVALSHA命令时Redis会根据提供的摘要从脚本缓存中查找对应的脚本内容，如果找到了就执行脚本，否则 返回“NOSCRIPT No matching script,Please use EVAL”

    ```shell
    # # 在调用eval命令之前，先执行evalsha命令，如果提示脚本不存在，则再调用eval命令
    script load "return redis.call('get','lua1')"  #将脚本加入缓存并生成sha1命令
    evalsha "a5a402e90df3eaeca2ff03d56d99982e05cf6574" 0
    ```

- 应用

  `语法为 ./redis-cli –eval [lua脚本] [key...]空格,空格[args…]`

  ```shell
  # demo.lua
  return redis.call('get','gupao')
  # 运行脚本
  bin] ./redis-cli --eval demo.lua
  ```

  ```shell
  # 限流，限制ip访问频率，1分钟访问10次
  [root@CentOS redis-4.0.10]# mkdir lua_demo 
  [root@CentOS redis-4.0.10]# cd lua_demo/ 
  [root@CentOS lua_demo]# vi ip_limit.lua 
  --限流，对某个ip的频率进⾏行行限制 1分钟访问10次 
  local num=redis.call('incr', KEYS[1])
  if tonumber(num)==1 then
          redis.call('expire',KEYS[1], ARGV[1])l
          return 1
  elseif tonumber(num)>tonumber(ARGV[2]) then
          return 0
  else
  		return 1
  end
  [root@CentOS lua_demo]# cd ..
  [root@CentOS redis-4.0.10]# cd bin/
  [root@CentOS bin]# ./redis-cli --eval "../lua_demo/ip_limit.lua" ip:limit:192.168.3.125 , 6000 10
  (integer) 1
  ```

  ```shell
  # 限制ip访问次数 ip_ratelimit.lua
  local key="ratelimit:"..KEYS[1]
  local limit=tonumber(ARGV[1])
  local expireTime=ARGV[2]
  local times=redis.call('incr',key)
  
  if times==1 then
  	redis.call('expire',key,expireTime)
  end
  
  if times > limit then
  	return 0
  end
  
  return 1
  # 运行
  ./redis-cli --eval ../lua/ip_ratelimit.lua 192.168.56.121 , 3 10
  ```

  ```shell
  # 实现一个针对某个手机号的访问频次，以下是lua脚本，保存为phone_limit.lua 
  local num=redis.call('incr',KEYS[1])
  if tonumber(num)==1 then
         redis.call('expire',KEYS[1],ARGV[1])
         return 1
      elseif tonumber(num)>tonumber(ARGV[2]) then
  		return 0 
  	else
  		return 1 
  	end
  # 通过如下命令调⽤用
  ./redis-cli --eval phone_limit.lua rate.limiting:13700000000 , 10 3 
  ```

  ```java
  public static void main(String[] args) throws Exception {
      Jedis jedis = ReidsManager.getJedis();
      String lua = "local num=redis.call('incr', KEYS[1])\n" +
              "if tonumber(num)==1 then\n" +
              " redis.call('expire',KEYS[1], ARGV[1])\n" +
              "return 1\n" +
              " elseif tonumber(num)>tonumber(ARGV[2]) then\n" +
                  "return 0\n" +
              "else\n" +
                  "return 1\n" +
              "end\n" ;
     List<String> keys = new ArrayList<String>();
     keys.add("ip:limit:127.0.0.1");
     List<String> arggs = new ArrayList<String>();
     arggs.add("6000");
     arggs.add("10");
     Object obj = jedis.eval(lua, keys, arggs);
     System.out.println(obj);
  }
  ```

```java
@Autowired
private StringRedisTemplate stringRedisTemplate;
private static final String IPACCESS_KEY_PREX = "ipaccess_";

DefaultRedisScript<Long> = new DefaultRedisScript<Long>();
			script.setResultType(Long.class);
			script.setScriptSource(new ResourceScriptSource(new ClassPathResource("lua/phoneLimit.lua")));

List<String> keys = new ArrayList<>();
keys.add(IPACCESS_KEY_PREX + mobile);
// 因为序列化原因，只能用StringRedisTemplate
Long execute = stringRedisTemplate.execute(script, keys, "60","1");

//resources/lua/phoneLimit.lua
local num=redis.call('incr',KEYS[1])
if tonumber(num)==1 then
    redis.call('expire',KEYS[1],ARGV[1])
    return 1
elseif tonumber(num)>tonumber(ARGV[2]) then
    return 0
else
	return 1
end
```

# 分布式Redis

- 主从复制就是master/slave模式， 主数据库可以进行读写操作，当写操作导致数据发生变化时会自动将数据同步给从数据库。
- 而一般情况下，从数据库是只读的，并接收主数据库同步过来的数据。 一个主数据库可以有多个从数据库
- 主从架构的**好处**：
  - 读写分离(主服务器负责写，从服务器负责读)
  - 高可用(某一台从服务器挂了，其他从服务器还能继续接收请求，不影响服务)
  - 处理更多的并发量(每台从服务器**都可以接收读请求**，读QPS就上去了)

## 配置

- 主服务器redis.conf不用增加，注释掉 #bind 127.0.0.1，允许其他服务器访问

- 从服务器redis.conf添加配置

  ```shell
  # slaveof <masterip> <masterport>
  slaveof 192.168.0.1 6379
  
  # 默认情况下，slave是只读的
  protected -mode no
  ```

- 启动`./redis-cli`，查看节点身份、信息`info replication`

## 数据同步

- 全量复制
  - 一般发生在Slave初始化阶段，这时Slave需要将Master上的所有数据都复制一份
    1. slave第⼀次或者重连到master上以后，会向master发送一个SYNC的命令
    2. master收到SYNC的时候，会做两件事
       - 执行bgsave(rdb的快照⽂件)
       - 并记录（缓存）在此期间的写命令
    3. master发送快照给slave，slave接收并载入快照
    4. master发送缓存的写命令给slave，slave执行相应的写命令
  - master/slave 复制策略是采用乐观复制，也就是说可以容忍在一定时间内master/slave数据的内容是不同的，但是两者的数据会最终同步。具体来说，==redis的主从同步过程本身是异步的，意味着master执行完客户端请求的命令后会立即返回结果给客户端，然后异步的方式把命令同步给slave==，这一特征保证启用master/slave后 master的性能不会受到影响
  - 但是另一方面，如果在这个数据不一致的窗口期间，master/slave因为网络问题断开连接，而这个时候，master是无法得知某个命令最终同步给了多少个slave数据库。不过redis提供了一个配置项来限制只有数据至少同步给多少个slave的时候，master才是可写的:
    - `min-slaves-to-write 3 `表示只有当3个或以上的slave连接到master，master才是可写的
    - `min-slaves-max-lag 10` 表示允许slave最长失去连接的时间，如果10秒还没收到slave的响应，则master认为该slave以断开 
- 增量复制
  - 从redis 2.8开始，就支持主从复制的断点续传，如果主从复制过程中，网络连接断掉了，那么可以接着上次复制的地方，继续复制下去，而不是从头开始复制一份
  - master node会在内存中创建一个backlog，master和slave都会保存一个replica offset还有一个master id，offset就是保存在backlog中的。如果master和slave网络连接断掉了，slave会让master从上次的replica offset开始继续复制，但是如果没有找到对应的offset，那么就会执行一次全量同步
  - 从服务器监听主服务器端口发送的数据：
    - `replconf listening-port 6379`
    - `sync`
- 无磁盘复制
  - Redis复制的工作原理基于RDB方式的持久化实现的，也就是master在后台保存RDB快照，slave接收到rdb文件并载入，但是这种方式会存在一些问题 ：
    - 当master禁用RDB时，如果执行了复制初始化操作，Redis依然会生成RDB快照，当master下次启动时执行该RDB文件的恢复，但是因为复制发生的时间点不确定，所以恢复的数据可能是任何时间点的。就会造成数据出现问题（RDB快照磁盘复制影响性能）
    - 当硬盘性能比较慢的情况下(网络硬盘)，那初始化复制过程会对性能产生影响（磁盘性能影响）
  - 可以不需要通过RDB文件去同步，直接发送数据，通过以下配置来开启该功能 
    - 不生成磁盘快照，即不需要通过rdb文件同步，在内存里传输
    - master在内存中直接创建rdb，然后发送给slave，不会在自己本地落地磁盘
    - `repl-diskless-sync no`

## 选主

- redis并没有提供自动master选举功能，而是需要借助一个==哨兵==来进行监控：

  1. 监控master和slave是否正常工作
  2. master出故障时，从slave中选举master

- 哨兵是个单独的进程，可以和master、slave在同一个服务器中，也可以单独部署在一台服务器

- 哨兵监控一个系统时，只需要配置监控master即可，哨兵会自动发现所有slave;

- 在一个一主多从的Redis系统中，可以使用多个哨兵进行监控任务以保证系统足够稳定。此时哨兵不仅会监控master和slave，同时还会互相监控，这种方式称为哨兵集群，`raft算法`选取哨兵集群中的leader

- 哨兵集群需要解决故障发现和master决策的协商机制问题

  - 哨兵之间的相互感知：每个哨兵都会监控共同的master，通过共同的master，哨兵能够知道彼此的存在
    1. 需要相互感知的sentinel都向它们共同监视的master节点订阅channel:sentinel:hello
    2. 新加入的sentinel节点向这个channel发布一条消息，包含自己本身的信息，这样订阅了这个channel的sentinel就可以发现这个新的sentinel
    3. 新加入得sentinel和其他sentinel节点建立长连接

- 配置

  1. 复制一份默认的sentinel.conf到配置文件目录

     - `cp ../redis-3.2.8/sentinel.conf sentinel.conf`

  2. 修改复制后的sentinel.conf

     ```shell
     # 1 表示有几个sentinel认为master挂了就算是挂了
     sentinel monitor mymaster 192.168.11.153 6379 1 
     ```

  3. 启动sentinel

     ```shell
     bin] ./redis-sentinel ../sentinel.conf
     # 或者
     redis-server /path/to/sentinel.conf --sentinel
     # 启动客户端，master查看信息
     info replication
     ```

- master故障发现

  - sentinel节点会定期向master节点发送==心跳包==来判断存活状态，一旦master节点没有正确响应，sentinel会把master设置为“主观不可用状态”，然后它会把“主观不可用”发送给其他所有的sentinel节点去确认，当确认的sentinel节点数大于>quorum时，则会认为master是“客观不可用”，接着就开始进入选举新的master流程

  - 但是这里又会遇到一个问题，就是sentinel中，本身是一个集群，如果多个节点同时发现master节点达到客观不可用状态，那谁来决策选择哪个节点作为maste呢?

    - 这个时候就需要从sentinel集群中选择一个leader来做决策。而这里用到了一致性算法`Raft`算法，是分布式一致性算法，基于投票算法，只要保证过半数节点通过提议即可

      ```shell
      # 配置实现
      # 在其中任意一台服务器上创建一个sentinel.conf文件，文件内容
      # 其中name表示要监控的master的名字，这个名字是自己定义。 ip和port表示master的ip和端口号。 最后一个1表示最低 通过票数，也就是说至少需要几个哨兵节点统一才可以
      sentinel monitor name ip port quorum
      # --表示如果5s内mymaster没响应，就认为SDOWN
      sentinel down-after-milliseconds mymaster 5000 
      # --表示如果15秒后,mysater仍没活过来，则启动failover，从剩下的 slave中选一个升级为master
      sentinel failover-timeout mymaster 15000 
      ```

- 哨兵模式下，客户端应该连接到哪个redis-server

  - 客户端通过连接到哨兵集群，通过发送Protocol.SENTINEL_GET_MASTER_ADDR_BY_NAME 命令，==从哨兵机器中询问master节点的信息，拿到master节点的ip和端口号以后，再到客户端发起连接。连接以后，需要在客户端建立监听机制，当master重新选举之后，客户端需要重新连接到新的master节点==

## 分片（不强制）

- 分片其实就是一个==hash的过程==，对key做hash取模然后划分到不同的机器上

- Redis集群的每个数据库依然存有集群中的所有数据，从而导致集群的总数据存储量受限于可用存储内存最小的节点，形成了==木桶效应==

- 哨兵和集群是两个独立的功能，当不需要对数据进行分片使用哨兵就够了，如果要进行水平扩容，集群是一个比较好的方式

- redis-cluster是基于gossip协议实现的无中心化节点的集群，因为去中心化的架构不存在统一的配置中心，各个节点对整个集群状态的认知来自于节点之间的信息交互。

  - 在Redis Cluster，这个信息交互是通过Redis Cluster Bus来完成的

- 分布式数据库首要解决把整个数据集按照==分区规则映射==到多个节点的问题，即把数据集划分到多个节点上，每个节点负责整个数据的一个子集, Redis Cluster采用==哈希分区规则==，采用==虚拟槽分区==

  - 虚拟槽分区巧妙地使用了哈希空间，使用分散度良好的哈希函数把所有的数据映射到一个固定范围内的整数集合，整数定义为==槽(slot)==
  - 比如Redis Cluster槽的范围是0 ~ 16383。槽是集群内==数据管理==和==迁移==的基本单位。采用大范围的槽的主要目的是为了方便数据的拆分和集群的扩展，每个节点负责一定数量的槽。
  - 对于每个进入Redis的键值对，根据==key进行散列==，分配到这16384个slot中的某一个中。使用的hash算法也比较简单，就是CRC16后16384取模
    - 计算公式：`slot = CRC16(key)%16383`。每一个集群节点负责维护一部分槽以及槽所映射的键值数据。
    - 假如有3个节点：node1 0 5460、node2 5461 10922、node3 10923 16383
    - 节点新增：node4 0-1364,5461-6826,10923-12287
  - 将数据分散到多个Redis实例中的方法，分片之后，每个redis拥有一部分原数据集的子集。能够将数据量分散到若干主机的redis实例上，进而减轻单台redis实例的压力
    1. 允许使用多台服务器的内存总和来支持更大的数据库。没有分片，就被局限于单机能支持的内存容量
    2. 允许伸缩计算能力到多核或多服务器，伸缩网络带宽到多服务器或多网络适配器

- 分片迁移：节点增加或者减少情形

  - 节点和分片对应的关系会发生变更：
    - 新加入master节点
      - 新增节点时，会从已存在的各个节点上分配一些槽点(slot)给新节点
      - 删除一个master节点，先将节点的数据移动到其他节点上，然后才能执行删除
    - 某个节点宕机
      - 当动态添加或减少node节点时，需要将16384个槽做个再分配，槽中的键值也要迁移。
      - 当然，这一过程，在目前实现中，还处于半自动状态，需要人工介入

- 如何将相同业务的数据存储到指定的redis节点上

  - 在redis中引入了`HashTag`的概念，可以使得数据分布算法可以根据key的某一个部分进行计算，然后 让相关的key落到同一个数据分片

    ```shell
    # HashTag 括号内相同的数据会存储到相同的节点上
    user:{user1}:id
    user:{user1}:name
    # 当一个key包含 {} 的时候，就不对整个key做hash，而仅对 {} 包括的字符串做hash
    ```

- 如果在某个redis节点上没找到数据，该节点会返回MOVED ip:端口——重定向

  - 表示客户端想要的254槽由运行在IP为127.0.0.1，端口为6381的Master实例服务。如果根据key计算得出的槽恰好由当前节点负责，则当期节点会立即返回结果
  - `-MOVED 254 127.0.0.1:6381`

- 企业级分片解决方案

  - twemproxy
    - 有成熟的解决方案、没办法动态扩容；分片迁移比较麻烦
  - codis
    - 基于redis源码、迁移过程简单、支持动态扩容或缩容、操作方式透明、支持多CPU，能合理利用多CPU
  - redis-cluster

# 应用

- Jedis是Redis的Java实现的客户端，其API提供了比较全面的Redis命令的支持
  - RedisTemplate——Spring
    - `JedisPoolConfig`、`JedisConnectionFactory`、`StringRedisTemplate`
- Redisson实现了分布式和可扩展的Java数据结构，和Jedis相比，功能较为简单，不支持字符串操作，不支持排序、事务、管道、分区等Redis特性。
  - Redisson主要是促进使用者对Redis的关注分离，从而让使用者能够将精力更集中地放在处理业务逻辑上。
- lettuce是基于Netty构建的一个可伸缩的线程安全的Redis客户端，支持同步、异步、响应式模式。多个线程可以共享一个连接实例，而不必担心多线程并发问题;

## 分布式锁

- 单进程多线程场景

  - `synchronized`、`lock`

- 分布式，多进程、跨进程，保证原子性

  - 数据库锁
    - 悲观锁、乐观锁
  - zookeeper
    - 节点的唯一性、有序性
  - redis
    - `setnx`

- 通过Redis来构建分布式锁，其实最根本原因就是Score(范围)

- ==因为在分布式架构中，所有的应用都是进程隔离的，在多进程访问共享资源的时候需要满足互斥性，就需要设定一个所有进程都能看得到的范围，而这个范围就是Redis本身。所以才需要把锁构建到Redis中==

- redis中有⼀个`setNx`命令，这个命令只有在key不存在的情况下为key设置值，设置成功返回1，失败返回0，所以可以利用这个特性来实现分布式锁的操作

- ==获得锁、释放锁、超时时间（到期自动释放锁）、判断是否重入==

- Jedis

  ```java
  public class RedisUtil {
      private static JedisPool jedisPool = null;
      static {
          JedisPoolConfig jedisPoolConfig = new JedisPoolConfig();
          jedisPoolConfig.setMaxTotal(10);
          jedisPoolConfig.setMaxIdle(20);
  //        jedisPool = new JedisPool(jedisPoolConfig, ConfigUitl.getRedisHost(), ConfigUitl.getRedisPort(), ConfigUitl.getRedisTimeOut(), ConfigUitl.getRedisPw());
          jedisPool = new JedisPool(jedisPoolConfig,
                  "10.211.55.6",
                  6379,
                  5000);
      }
  
      public static Jedis getJedisPool() throws Exception {
          if (jedisPool != null) {
              return jedisPool.getResource();
          } else {
              throw new Exception("JedisPool was not init!");
          }
      }
  }
  ```

  ```java
  public class DistributeRedisLock {
  
      /**
       * 获得锁
       *
       * @param lockName       锁的名字
       * @param acquireTimeout 尝试获取锁的时间
       * @param lockTimeout    锁本身的过期时间
       * @return
       */
      public String acquireLock(String lockName, long acquireTimeout, long lockTimeout) {
          //保证释放锁的时候是同一个锁持有锁的人
          String identifier = UUID.randomUUID().toString();
          String lockKey = "lock:" + lockName;
          int lockExpire = (int) (lockTimeout / 1000);
          Jedis jedis = null;
  
          //尝试获取锁的时
          long end = System.currentTimeMillis() + acquireTimeout;
          try {
              jedis = RedisUtil.getJedisPool();
              //在尝试时间内尝试获取锁
              while (System.currentTimeMillis() < end) {
                  //获取锁
                  if (jedis.setnx(lockKey, identifier) == 1) {
                      //设置过期时间
                      jedis.expire(lockKey, lockExpire);
                      //获取锁成功
                      return identifier;
                  }
                  //如果锁没有设置超时时间，则设置超时时间
                  if (jedis.ttl(lockKey) == -1) {
                      jedis.expire(lockKey, lockExpire); //设置过期时间
                  }
                  try {
                      //等待片刻尝试获取锁
                      Thread.sleep(100);
                  } catch (Exception e) {
                      e.printStackTrace();
                  }
              }
          } catch (Exception e) {
              e.printStackTrace();
          } finally {
              jedis.close();
          }
          return null;
      }
   
      //基于lua脚本释放锁，保证原子性
      public boolean releaseLockWithLua(String lockName, String identifier) {
          try {
              Jedis jedis = RedisUtil.getJedisPool();
              String lockKey = "lock:" + lockName;
              String lua = "if redis.call(\"get\",KEYS[1]) == ARGV[1] then " +
                      "return redis.call(\"del\", KEYS[1]) " +
                      "else return 0 end";
              Long rs = (Long)jedis.eval(lua, 1, new String[]{lockKey, identifier});
              //jedis.evalsha();  //缓存lua脚本，不应每次传输
              if(rs.intValue() > 0) {
                  return true;
              }
              return false;
  
          } catch (Exception e) {
              e.printStackTrace();
          }
          return false;
      }
  }
  ```

  ```java
  public class RedisLockTest extends Thread {
      @Override
      public void run() {
          while (true) {
              DistributeRedisLock redisLock = new DistributeRedisLock();
              String rs = redisLock.acquireLock("updateOrder", 2000, 5000);
              if(rs != null) {
                  System.out.println(Thread.currentThread().getName() + "->成功获得锁：" + rs);
                  try {
                      Thread.sleep(1000);
                      redisLock.releaseLock("updateOrder", rs);
                  } catch (Exception e) {
                      e.printStackTrace();
                  }
                  break;
              }
          }
      }
  
      public static void main(String[] args){
          RedisLockTest redisLockTest = new RedisLockTest();
          for(int i=0; i < 10; i++) {
              new Thread(redisLockTest, "tName" + i).start();
          }
      }
  }
  ```

- Redisson分布式锁

  - Redisson它除了常规的操作命令以外，还基于redis本身的特性去实现了很多功能的封装，比如分布式锁、原子操作、布隆过滤器、队列等等。可以直接利用这个api提供的功能去实现 

  ```java
  //Redisson
  public static void main(String[] args){
      Config config = new Config();
      config.useSingleServer().setAddress("redis://10.211.55.6:6379");
      RedissonClient redissonClient = Redisson.create(config);
      RLock rLock = redissonClient.getLock("updateOrder");
      //最多等待100秒、上锁10s以后自动解锁 
      //if(rLock.tryLock(100,10,TimeUnit.SECONDS)){   
  	//	System.out.println("获取锁成功");
      //}
      try {
          rLock.tryLock(100, 10, TimeUnit.SECONDS);
          System.out.println("Redisson test");
          Thread.sleep(1000);
          rLock.unlock();
      } catch (InterruptedException e) {
          e.printStackTrace();
      } finally {
          rLock.unlock();
          redissonClient.shutdown();
      }
  }
  ```

## 管道模式

- Redis服务是一种C/S模型，提供请求-响应式协议的TCP服务，所以当客户端发起请求，服务端处理并返回结果到客户端，一般是以阻塞形式等待服务端的响应，但这在批量处理连接时延迟问题比较严重，所以Redis为了提升或弥补这个问题，引入了管道技术：

  - 可以做到服务端未及时响应的时候，客户端也可以继续发送命令请求，做到客户端和服务端互不影响，服务端并最终返回所有服务端的响应，大大提高了C/S模型交互的响应速度上有了质的提高

    ```java
    //pipleline
    Jedis jedis = RedisUtil.getJedisPool();
    Pipeline pipeline = jedis.pipelined();
    pipeline.set("aa", "11");
    pipeline.set("bb", "22");
    pipeline.sync();
    ```

    

