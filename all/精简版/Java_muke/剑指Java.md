# Readme

```java
// 如何介绍项目
1. 站在码农的角度介绍项目，专注技术指标以及解决思路
2. 自信，脉络要清晰：项目用途->自己的角色->如何解决难题
3. 项目若找不到难点，则谈谈改进，前提是熟悉相关涉及的知识点
4. 事前用图形将你的项目勾画清楚
// 编译java源文件
javac xxx.xxx.java
// 查看class文件内容
javap -verbose xxx.xxx.clas
```

# 数据库

## 架构

* 如何设计一个关系型数据库
  * RDBMS
    * 程序实例
      * 存储管理、缓存机制、SQL解析、日志管理、权限划分、容灾机制、索引管理、锁管理
    * 存储（文件系统）

## 索引

* 为什么要使用索引

  - 快速查询数据，避免全表扫描

* 什么样的信息能够成为索引

  - 主键、唯一键以及普通键

* 索引的数据结构

  - 生成索引，建立二叉查找树进行二分查找
    - 每个节点最多有两个节点
    - 左子节点小于父节点，右子节点大于父节点
    - 平衡二叉树
    - 缺点：O(logn) - 会变成 - O(n)、树的深度会很深导致IO次数变多
  - 生成索引，建立B-Tree结构进行查找
    - B-Tree
      - 根节点至少包括两个孩子
      - 树中每个节点最多含有m个孩子(m >= 2)
      - 除根节点和叶节点外，其他每个节点至少有ceil(m/2)个孩子（ceil-取上）
      - 所有的叶子节点都位于同一层
      - ![image-20190807135334991](/Users/dingyuanjie/Documents/study/github/woodyprogram/img/image-20190807135334991.png)
      - ![image-20190807135404619](/Users/dingyuanjie/Documents/study/github/woodyprogram/img/image-20190807135404619.png)
  - 生成索引，建立B+-Tree结构进行查找
    - B+树是B树的变体，其定义基本与B树相同，除了：
      - 非叶子节点的子树指针与关键字个数相同
      - 非叶子节点的子树指针P[i]，指向关键字值[K[i], K[i+1])的子树
      - 非叶子节点仅用来索引，数据都保存在叶子节点中
      - 所有叶子节点均有一个链指针指向下一个叶子节点
      - ![image-20190807135854117](/Users/dingyuanjie/Documents/study/github/woodyprogram/img/image-20190807135854117.png)
    - B+Tree更适合用来做存储索引
      - B+树的磁盘读写代价更低
      - B+树的查询效率更加稳定
      - B+树更有利于对数据库的扫描
  - 生成索引，建立Hash结构进行查找
    - 仅能满足“=”，“IN”，不能使用范围查询
    - 无法被用来避免数据的排序操作
    - 不能利用部分索引键查询
    - 不能避免表扫描
    - 遇到大量Hash值相等的情况后性能并不一定就会比B-Tree索引高

* 密集索引和稀疏索引的区别

  * 密集索引文件中的每个搜索码值都对应一个索引值
  * 稀疏索引文件只为索引码的某些值建立索引项

* InnoDB

  * 若一个主键被定义，该主键则作为密集索引
  * 若没有主键被定义，该表的第一个唯一非空索引则作为密集索引
  * 若不满足以上条件，innodb内部会生成一个隐藏主键（密集索引）
  * 非主键索引存储相关键位和其对应的主键值，包含两次查找

* innodb的索引和数据是存在一起的；MyISAM的索引和数据是分开存储的

* 如何定位并优化慢查询SQL

  * 具体场景具体分析，大致思路

  * 根据慢日志定位慢查询SQL

    * MySQL Console中执行：

    * `show variables like '%quer%';` 显示查询设置，关注下面三个

      * `long_query_time:10.000000`
      * `slow_query_log:OFF`
      * `slow_query_log_file:/var/lib/mysql/e1a7801d0387-slow.log`

    * `show status like '%slow_queries%;'`，显示慢查询SQL数

      * `Slow_queries:0`

    * 设置配置，重新连接后生效

      * 打开慢查询日志功能：`set global slow_query_log=on;`
      * 慢查询时间：`set global long_query_time=1;`

    * 查找具体慢查询SQL

      * 到`slow_query_log_file`设置的位置`/var/lib/mysql/e1a7801d0387-slow.log`中查看

        * docker中mysql

        * > docker container ls
          >
          > docker exec -it e1a7801d0387 bash
          >
          > cd /var/lib/mysql
          >
          > cat xxx.slow.log

  * 使用explain等工具分析SQL

    * `explain select name from person_info_large order by name desc`
      * 重点关注`type`和`extra`，type是index或all时需要优化
        * type：system>const>eq_ref>ref>fulltext>ref_or_null>index_merge>unique_subquery>index_subquery>range>index>all
        * extra需优化情况
          * Using filesort
            * 表示MySQL会对结果使用一个外部索引排序，而不是从表里按索引次序读到相关内容。可能在内存或磁盘上进行排序。MySQL中无法利用索引完成的排序操作称为文件排序
          * Using temporary
            * 表示MySQL在对查询结果排序时使用临时表，常见于排序order by和分组查询group by

  * 修改sql或尽量让sql走索引

    * ![image-20190807225222917](/Users/dingyuanjie/Documents/study/github/woodyprogram/img/image-20190807225222917.png)
    * `alter table person_info_large add index idx_name(name);`
    * ![image-20190807225637130](/Users/dingyuanjie/Documents/study/github/woodyprogram/img/image-20190807225637130.png)
    * ![image-20190807225857733](/Users/dingyuanjie/Documents/study/github/woodyprogram/img/image-20190807225857733.png)

* 联合索引的最左匹配原则的成因

  * 联合索引(a,b)，当where a = ''and b='' 或where a=''或where b=''and a=''时使用了联合索引；where b=''则没使用联合索引
  * ![image-20190807230307576](/Users/dingyuanjie/Documents/study/github/woodyprogram/img/image-20190807230307576.png)
  * ![image-20190807230704172](/Users/dingyuanjie/Documents/study/github/woodyprogram/img/image-20190807230704172.png)
  * 先给最先的字段排序，然后在该字段基础上给接下来的字段排序

* 索引是建立越多越好吗

  * 数据量小的表不需要建立索引，建立会增加额外的索引开销
  * 数据变更需要维护索引，因此更多的索引意味着更多的维护成本
  * 更多的索引也意味着需要更多的空间

## 锁模块

* MyISAM与InnoDB关于锁方面的区别是什么

  * MyISAM默认用的是表级锁，不支持行级锁
  * InnoDB默认用的是行级锁，支持表级锁

* MyISAM

  - `select * from person where id between 1 and 2000000;`时会表锁 `update person set account = account  where id = 2000001;`会等待前面的select执行完才执行（如果是行级锁，则不会等待而是直接执行）
  - select时会加读锁，update需等待读锁释放后才加写锁进行数据更新
  - 显示加读锁/写锁：`lock tables person read | write;`
  - 释放读锁：`unlock tables;`
  - 读锁，也是共享锁；写锁，也是排他锁（读或写是不允许的）；
    - 除了给`insert、delete、update`加排他锁，也能给select加排他锁（`select ... for update;`）
  - 先加写锁，再加读锁，需先等待写锁释放后才能加读锁
    - `update person set account=account where id between 1 and 2000000;`
    - `select * from person where id in (2000000,2000001);`
  - 适合场景
    - 频繁执行全表count语句：不用扫描表，有个变量保存了整个表的行数，直接读取即可
    - 对数据进行增删改的频率不高，查询非常频繁
    - 没有事务

* InnoDB

  * `show variables like 'autocommit';`
  * 关闭自动提交，只对当前session有效`set autocommit=0`
  * InnoDB对select进行了改进：并未对select加读锁
  * 加共享锁：select .... lock in share mode;
  * InnoDB默认支持行级锁，不同行数据互不影响
  * InnoDB没有用到索引时，使用的是表级锁
  * 表级的意向锁
    - IS共享读锁、IX排他写锁
    - 与MyISAM的表锁差不多，表级别操作的时候不用去轮询每一行有没有上行锁
  * 适用场景
    - 数据增删改查都很频繁
    - 可靠性要求比较高，要求支持事务

* 数据库锁的分类

  - 按锁的粒度划分，可分为表级锁、行级锁、页级锁
  - 按锁级别划分，可分为共享锁、排他锁
  - 按加锁方式划分，可分为自动锁、显示锁
  - 按操作划分，可分为DML锁、DDL锁
  - 按使用方式划分，可分为悲观锁、乐观锁

* 数据库事务四大特性

  - 原子性（Atomic）
  - 一致性（Consistency）
  - 隔离性（Isolation）
    - 查看隔离级别`select @@tx_isolation`
    - 更改当前Session的隔离级别`set session transaction isolation level read uncommitted`
    - 持久性（Durability）

* 事务隔离级别以及各级别下的并发访问问题

  * 事务并发访问引起的问题以及如何避免

    - 更新丢失-MySQL所有事物隔离级别在数据库层面上均可避免

    - 脏读-READ-COMMITTED事务隔离级别以上均可避免

      - `start transaction;`
      - `sql...;`
      - `rollback; 或 commit;`

    - 不可重复读-REPEATABLE-READ事务隔离级别以上可避免

      - REPEATABLE-READ，sessionA在其事务期间多次读取的数据都是一样的，即使期间被其他事物修改并提交
      - sessionA在sessionB事务提交之前更改了数据
      - sessionB查询得到的一直是sessionA更改之前的数据，但是sessionB是在sessionA提交后的数据基础之上更新数据

    - 幻读-SERIALIABLE事务隔离级别可避免

    - - 幻读：事务A先查询（当前读-`select ... lock share in mode`）某范围内数据，事务B在该范围内新增或删除一条数据并提交，事务A再更新该范围内数据时会多或少更新数据
      - InnoDB的REPEATABLE-READ隔离级别可避免幻读

    - ![image-20190808070758066](/Users/dingyuanjie/Documents/study/github/woodyprogram/img/image-20190808070758066.png)

  * InnoDB可重复读隔离级别下如何避免幻读

    * 表象：快照读（非阻塞读）- 伪MVCC

    * - 当前读：共享锁和排他锁均为当前读，读取的是最新结果，并且读取后要保证其他并发事务不能修改结果，对结果加锁
        - `select ... lock in share mode`，共享锁
        - `select ... for update`，排他锁
        - `update、insert、delete`，排他锁
      - 快照读：不加锁的非阻塞读（SERIALIABLE级别的读是加锁的，为当前读模式select ... lock in share mode），select
        - 快照读是基于提升并发性能的考虑，是基于多版本并发控制MVCC来实现的，可以认为MVCC是行级锁的一个变种，在很多情况下避免加锁操作，开销更低，读取的可能不是最新版本而是历史版本
        - READ COMMITTED级别下，当前读和快照读的结果一致（事务期间读取到其他事物提交的最新结果）
        - READ REPEATBLE级别下，当前读读取的是最新数据，快照读可能读取的是历史版本数据（事务期间读取），快照创建的时期决定了事务读取的版本（快照读：sessionA期间先查询数据，sesionB更改并提交数据，sessionA再查询数据则为更改前的版本数据；sessionA先不查询数据，sessionB更改并提交数据，sessionA查询数据为最新版本数据）

    * 内在：next-key锁（行锁 + gap锁）
      - 行锁
      - gap锁（READ REPEATBLE、 SERIALIABLE）是个范围（左开右闭），不包括本身
        - 对主键索引或唯一索引会用gap锁吗？
          - 如果where条件全部命中，则不会用Gap锁、只会加记录锁
          - 如果where条件部分命中或者全不命中，则会加Gap锁
        - Gap锁会用在非唯一索引或者不走索引的当前读中
    * RC、RR级别下的InnoDB的非阻塞读如何实现
      - 数据行里的DB_TRX_ID（最后一次修改本行数据的事务ID）、DB_ROLL_PTR（回滚指针）、DB_ROW_ID字段
      - undo日志
        - insert undo log
        - update undo log：delete和update时
          - 更新数据时，首先用排他锁锁住某行数据，将该行数据拷贝一份到undo log，然后修改当前行的值，填写事务id，使用回滚指针（DB_ROLL_PTR）指向undo log中那条修改前的数据  
          - ![image-20190808212343234](/Users/dingyuanjie/Documents/study/github/woodyprogram/img/image-20190808212343234.png)
      - read view：可见性判断

* 语法部分

  - 关键语法（先根据需求列出sql子句，然后拼接）
    - Group By
      - 满足“SELECT 子句中的列名必须为分组列或列函数“
      - 列函数对于group by子句定义的每个组各返回一个结果
      - ![image-20190808220214233](/Users/dingyuanjie/Documents/study/github/woodyprogram/img/image-20190808220214233.png)
      - ![image-20190808220416827](/Users/dingyuanjie/Documents/study/github/woodyprogram/img/image-20190808220416827.png)
    - Having
      - 通常与Group By子句一起使用，指定Group By的过滤条件
      - where过滤行，Having过滤组；（如果省略了group by，那么having作用和where一样）
      - 出现在同一SQL中顺序：w here > group by > having
      - ![image-20190808220954877](/Users/dingyuanjie/Documents/study/github/woodyprogram/img/image-20190808220954877.png)
      - 统计相关：COUNT、SUM、MAX、MIN、AVG
      - ![image-20190808221240919](/Users/dingyuanjie/Documents/study/github/woodyprogram/img/image-20190808221240919.png)
      - ![image-20190808221352400](/Users/dingyuanjie/Documents/study/github/woodyprogram/img/image-20190808221352400.png)
    - 

# Redis

* 缓存中间件—Memcache和Redis的区别

  - Memcache代码层次类似Hash
    - 支持简单数据类型、不支持数据持久化存储、不支持主从、不支持分片
  - Redis
    - 数据类型丰富、支持数据磁盘持久化存储、支持主从、支持分片

* Redis为什么这么快

  - 完全基于内存，绝大部分请求是纯粹的内存操作，执行效率高
  - 数据结构简单，对数据操作也简单
  - 采用单线程，单线程也能处理高并发请求，想多核也可启动多实例
    - 主线程是单线程，包括IO事件的处理、IO对应相关业务的处理、主线程还负责过期键的处理、复制协调、集群协调，这些除了IO事件之外的逻辑会被封装成周期性的任务由主线程周期性的处理，正因为采用单线程的处理，对于客户端的所有读写请求都由一个主线程串行处理，因此多个客户端同时对一个键进行写操作时就不会有并发的问题，避免了频繁的上下文切换和锁竞争，使得redis执行起来效率更高
  - 使用多路I/O复用模型，非阻塞IO

* 多路I/O复用模型

  - FD：File Descriptor，文件描述符
    - 一个打开的文件通过唯一的描述符进行引用，该描述符是打开文件的元数据到文件本身的映射
  - Select函数
    - 可以同时监控并返回多个文件描述符的读写情况
  - Redis采用的I/O多路复用函数：epoll/kqueue/evport/select？
    - 因地制宜，优先选择时间复杂度为O(1)的I/O多路复用函数作为底层实现
    - 以时间复杂度为O(n)的select作为保底
    - 基于react设计模式监听I/O事件

* Redis的数据类型

  - String，值最大512M，二进制安全（图片等），set
  - Hash，String元素组成的字典，适合用于存储对象，hset
  - List，按照String元素插入顺序排序，lpush
  - Set，String元素组成的无序集合，通过哈希表实现，不允许重复，sadd
  - Sorted Set，通过分数来为集合中的成员进行从小到大的排序，zadd

* 从海量key里查询出某一固定前缀的Key

  * > 批量生成redis测试数据 
    >
    > 1.Linux Bash下面执行 for((i=1;i<=20000000;i++)); do echo "set k$i v$i" >> /tmp/redisTest.txt ;done; 生成2千万条redis批量设置kv的语句(key=kn,value=vn)写入到/tmp目录下的redisTest.txt文件中 
    >
    > 2.用vim去掉行尾的^M符号，使用方式如下：： vim /tmp/redisTest.txt  :set fileformat=dos #设置文件的格式，通过这句话去掉每行结尾的^M符号  ::wq #保存退出 
    >
    > 3.通过redis提供的管道--pipe形式，去跑redis，传入文件的指令批量灌数据，需要花10分钟左右 cat /tmp/redisTest.txt | 路径/redis-5.0.0/src/redis-cli -h 主机ip -p 端口号 --pipe

  * 摸清数据规模，即问清楚边界，`dbsize返回key的数量`

  * keys pattern：查找所有符合给定模式pattern的key

    - keys指令一次性返回所有匹配的key
    - 键的数量过大会导致服务卡顿

  * `SCAN cursor [MATCH pattern] [COUNT count]`

    * 基于游标的迭代器，需要基于上一次的游标延续之前的迭代过程
    * 以0作为游标开始一次新的迭代，直到命令返回游标0完成一次遍历
    * 不保证每次执行都返回某个给定数量的元素，支持模糊查询
    * 一次返回的数量不可控，只能是大概率符合count参数
    * ![image-20190808232453790](/Users/dingyuanjie/Documents/study/github/woodyprogram/img/image-20190808232453790.png)

* 如何通过redis实现分布式锁
  - 互斥性、安全性、死锁、容错
  - `SETNX key value`，如果key不存在则创建并赋值，时间复杂度O(1)，设置成功返回1，失败返回0
  - `EXPIRE key time`，单独都是原子操作，前后执行就可能不是原子操作
  - `SET key value [EX seconds] [PX milliseconds] [NX|XX]`
    - EX second：设置键的过期时间为second秒
    - PX millisecond：设置键的过期时间为milliseconds毫秒
    - NX：只在键不存在时，才对键进行设置操作
    - XX：只在键已经存在时，才对键进行设置操作
    - SET操作完成时，返回OK，否则返回nil
  - ![image-20190808233444983](/Users/dingyuanjie/Documents/study/github/woodyprogram/img/image-20190808233444983.png)
  
* 大量的key同时过期的注意事项
  - 集中过期，由于清除大量的key很耗时，会出现短暂的卡顿现象
  - 解决方案：在设置key的过期时间时，给每个key加上随机值

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
  * RDB（快照）持久化：保存某个时间点的全量数据快照
    * SAVE：阻塞Redis的服务器进程，直到RDB文件被创建完毕
    * BGSAVE：Fork出一个子进程来创建RDB文件，不阻塞服务器进程
  * `lastsave`返回最近一次持久化成功的时间
    * 自动化触发RDB持久化的方式
      * 根据redis.conf配置里的SAVE m n定时触发（用的是BGSAVE）
      * 主从复制时，主节点自动触发
      * 执行Debug Reload
      * 执行Shutdown且没有开启AOF持久化
    * 缺点：
      * 内存数据的全量同步，数据量大会由于I/O而严重影响性能
      * 可能会因为Redis挂掉而丢失从当前至最近一次快照期间的数据
  * ![image-20190809162352367](/Users/dingyuanjie/Documents/study/github/woodyprogram/img/image-20190809162352367.png)
    * Copy-on-Write
      * 如果有多个调用者同时要求相同资源（如内存或磁盘上的数据存储），它们会共同获取相同的指针指向相同的资源，直到某个调用者试图修改资源的内容时，系统才会真正复制一份专用副本给该调用者，而其他调用者所见到的最初的资源仍然保持不变
  * AOF（Append-Only-File）持久化：保存写状态
    * 记录下除了查询以外的所有变更数据库状态的指令
    * 以append的形式追加保存到AOF文件中（增量）
    * `redis.conf`
      - `appendonly yes`
      - `appendfsync everysec`
    * `config set appendonly yes`
  * 日志重写解决AOF文件大小不断增大的问题，原理如下：
    * 调用fork()，创建一个子进程
    * 子进程把新的AOF写到一个临时文件里，不依赖原来的AOF文件
    * 主进程持续将新的变动同时写到内存和原来的AOF里
    * 主进程获取子进程重写AOF的完成信号，往新AOF同步增量变动
    * 使用新的AOF文件替换掉旧的AOF文件
  * Redis数据恢复
    * ![image-20190809163555684](/Users/dingyuanjie/Documents/study/github/woodyprogram/img/image-20190809163555684.png)
  * RDB和AOF的优缺点
    * RDB优点：全量数据快照，文件小、恢复快
    * RDB缺点：无法保存最近一次快照之后的数据
    * AOF优点：可读性高，适合保存增量数据，数据不易丢失
    * AOF缺点：文件体积大，恢复时间长
  * RDB - AOF混合持久化方式
    - BGSAVE做镜像全量持久化，AOF做增量持久化
  
* 使用Pipeline的好处

  * Pipeline和Linux的管道类似
  * Redis基于请求/响应模型，单个请求处理需要一一应答
  * Pipeline批量执行指令，节省多次IO往返的时间
  * 有顺序依赖的指令建议分批发送

* Redis的同步机制

  - 主从同步原理
    - 全同步过程
      - Salve发送sync命令到Master
      - Master启动一个后台进程，将Redis中的数据快照保存到文件中
      - Master将保存数据快照期间接收到的写命令缓存起来
      - Master完成写文件操作后，将该文件发送给Slave
      - 使用新的AOF文件替换掉旧的AOF文件
      - Master将这期间收集的增量写命令发送给Slave端
    - 增量同步过程
      - Master接收到用户的操作指令，判断是否需要传播到Slave
      - 将操作记录追加到AOF文件
      - 将操作传播到其他Slave：1、对齐主从库；2、往响应缓存写入指令
      - 将缓存中的数据发送给Slave

* Redis Sentinel

  * 解决主从同步Master宕机后的主从切换问题
    * 监控：检查主从服务器是否运行正常
    * 提醒：通过API向管理员或者其他应用程序发送故障通知
    * 自动故障迁移：主从切换

* 流言协议Gossip

  * 在杂乱无章中寻求一致
    * 每个节点都随机地与对方通信，最终所有节点的状态达成一致
    * 种子节点定期随机向其他节点发送节点列表以及需要传播的信息
    * 不保证信息一定会传递给所有节点，但是最终会趋于一致

* Redis的集群原理

  * 如何从海量数据里快速找到所需？
    * 分片：按照某种规则去划分数据，分散存储在多个节点上
    * 常规的按照哈希划分无法实现节点的动态增减
    * 一致性Hash算法：对2^32取模，将Hash值空间组织成虚拟的圆环
    * ![image-20190809165236475](/Users/dingyuanjie/Documents/study/github/woodyprogram/img/image-20190809165236475.png)
    * 取服务器的主机名或IP进行Hash以确定每台服务器在Hash环上的位置
    * 对数据进行同样的hash算法去定位访问到相应的服务器，对数据key使用和服务器IP使用的相同的Hash函数计算出Hash值确定在环上的位置，顺时针遇到的第一个服务器即存储位置
    * 如果某个Node宕机，影响的是与其逆时针方向Node之间的数据，则其上数据会继续顺时针方向存储到相邻的节点上，影响最小化
    * 如果新增一个Node，受影响的是与其逆时针方向Node之间的数据，会存储到新的Node上
    * Hash环的数据倾斜问题：环上服务器数量少，大部分数据都存储在某一台上
      - 引入虚拟节点解决数据倾斜的问题
        - 对每一个服务器节点计算多个Hash，服务器节点的服务名或IP后增加编号

# JVM

* 谈谈对Java的理解
  
  * 平台无关性、GC、语言特性、面向对象、类库、异常处理
  
* “一次编译，到处运行”，如何实现
  * Java源码(.java文件)首先被编译（javac编译）成字节码（.class文件），再由不同平台的JVM进行解析
  * Java语言在不同的平台上运行时不需要进行重新编译，Java虚拟机在执行字节码的时候，把字节码转换成具体平台上的机器指令
  
* 为什么JVM不直接将源码解析成机器码去执行
  * 准备工作：每次执行都需要各种检查
  * 兼容性：也可以将别的语言解析成字节码
  
* JVM如何加载.class文件
  
  * ![image-20190809220541839](/Users/dingyuanjie/Documents/study/github/woodyprogram/img/image-20190809220541839.png)
  
* 谈谈反射
  
  * 在运行状态中，对于任意一个类都能够知道这个类的所有属性和方法；对于任意一个对象，都能够调用它的任意方法和属性；这种动态获取信息以及动态调用对象方法的功能称为java语言的反射机制
  
  * ```java
    public class Robot {
        private String name;
        public void sayHi(String helloSentence){
            System.out.println(helloSentence + " " + name);
        }
        private String throwHello(String tag){
            return "Hello " + tag;
        }
        static {
            System.out.println("Hello Robot");
        }
    }
    ```
  
    ```java
    public class ReflectSample {
        public static void main(String[] args) throws ClassNotFoundException, IllegalAccessException, InstantiationException, InvocationTargetException, NoSuchMethodException, NoSuchFieldException {
            Class rc = Class.forName("com.interview.javabasic.reflect.Robot");
            Robot r = (Robot) rc.newInstance();
            System.out.println("Class name is " + rc.getName());
            Method getHello = rc.getDeclaredMethod("throwHello", String.class);
            getHello.setAccessible(true);
            Object str = getHello.invoke(r, "Bob");
            System.out.println("getHello result is " + str);
            Method sayHi = rc.getMethod("sayHi", String.class);
            sayHi.invoke(r, "Welcome");
            Field name = rc.getDeclaredField("name");
            name.setAccessible(true);
            name.set(r, "Alice");
            sayHi.invoke(r, "Welcome");
            System.out.println(System.getProperty("java.ext.dirs"));
            System.out.println(System.getProperty("java.class.path"));
        }
    }
    ```
  
* 类从编译到执行的过程

  * 编译器将Java源文件编译为字节码文件
  * ClassLoader将字节码转换为JVM中的Class对象
  * JVM利用Class对象实例化为类对象

* 谈谈ClassLoader

  * 工作在Class装载的加载阶段，其主要作用是从系统外部获得Class二进制数据流，它是Java的核心组件，所有的Class都是由ClassLoader进行加载的，ClassLoader负责通过将Class文件里的二进制数据流装载进系统，然后交给Java虚拟机进行连接、初始化等操作

  * 种类

    * BootStrapClassLoader：C++编写，加载核心库java.*
    * ExtClassLoader：Java编写，加载扩展库javax.*
    * AppClassLoader：Java编写，加载程序所在目录
    * 自定义ClassLoader：Java编写，定制化加载

  * 自定义ClassLoader的实现

    * 关键函数

    * ```java
      protected Class<?> findClass(String name) throws ClassNotFoundException {
        throw new ClassNotFoundException(name);
      }
      protected final Class<?> defineClass(byte[] b, int off, int len) throws ClassFormatError {
        return defineClass(null, b, off, len, null);
      }
      ```

    * 例子

    * ```java
      public class MyClassLoader extends ClassLoader {
          private String path;
          private String classLoaderName;
      
          public MyClassLoader(String path, String classLoaderName) {
              this.path = path;
              this.classLoaderName = classLoaderName;
          }
      
          //用于寻找类文件
          @Override
          public Class findClass(String name) {
              byte[] b = loadClassData(name);
              return defineClass(name, b, 0, b.length);
          }
      
          //用于加载类文件
          private byte[] loadClassData(String name) {
              name = path + name + ".class";
              InputStream in = null;
              ByteArrayOutputStream out = null;
              try {
                  in = new FileInputStream(new File(name));
                  out = new ByteArrayOutputStream();
                  int i = 0;
                  while ((i = in.read()) != -1) {
                      out.write(i);
                  }
              } catch (Exception e) {
                  e.printStackTrace();
              } finally {
                  try {
                      out.close();
                      in.close();
                  } catch (Exception e) {
                      e.printStackTrace();
                  }
              }
              return out.toByteArray();
          }
      }
      ```

      ```java
      public class ClassLoaderChecker {
          public static void main(String[] args) throws ClassNotFoundException, IllegalAccessException, InstantiationException {
              MyClassLoader m = new MyClassLoader("/Users/baidu/Desktop/", "myClassLoader");
              Class c = m.loadClass("Wali");
              System.out.println(c.getClassLoader());
              System.out.println(c.getClassLoader().getParent());
              System.out.println(c.getClassLoader().getParent().getParent());
              System.out.println(c.getClassLoader().getParent().getParent().getParent());
              c.newInstance();
          }
      }
      ```

* 类加载器的双亲委派机制

  * 自底向上检查类是否已经加载
  * 自顶向下尝试加载类

* 为什么要使用双亲委派模式

  * 避免多份同样字节码的加载

* 类的加载方式

  * 隐式加载：new
  * 显示加载：loadClass，forName等

* 类的装载过程

  * 加载
    * 通过ClassLoader加载class文件字节码，生成Class对象
  * 链接
    * 校验：检查加载的class的正确性和安全性
    * 准备：为类变量分配存储空间并设置类变量初始值
    * 解析：JVM将常量池内的符号引用转换为直接引用
  * 初始化
    * 执行类变量赋值和静态代码块

* loadClass和forName的区别

  * Class.forName得到的class是已经初始化完成的
  * Classloader.loadClass得到的class是还没有链接的

* JVM内存模型-JDK8

  * 线程私有：
    * 程序计数器
      * 当前线程所执行的字节码行号指示器（逻辑）
      * 改变计数器的值来选取下一条需要执行的字节码指令
      * 对Java方法计数，如果是Native方法则计数器值为Undefined
      * 不会发生内存泄漏
    * 虚拟机栈
      * Java方法执行的内存模型
      * 包含多个栈帧：局部变量表（包含方法执行过程中的所有变量）、操作栈（入栈、出栈、复制、交换、产生消费变量）、动态链接、返回地址
      * 递归为什么会引发java.lang.StackOverflowError异常
        * 递归过深，栈帧数超出虚拟栈深度
        * 限制递归的次数
        * 虚拟机栈过多会引发java.lang.OutOfMemoryError异常
    * 本地方法栈
      * 与虚拟机栈相似，主要作用于标注了native的方法
  * 线程共享
    * MetaSpace
    * Java堆

* 元空间（MetaSpace）与永久代（PermGen）的区别
  * 元空间使用本地内存，而永久代使用的是jvm的内存
  * java.lang.OutOfMemeroyError:PermGen space
* MetaSpace相比PermGen的优势
  * 字符串常量池存在永久代中，容易出现性能问题和内存溢出
  * 类和方法的信息大小难以确定，给永久代的大小指定带来困难
  * 永久代会为GC带来不必要的复杂性
  * 方便HotSpot与其他JVM如Jrockit的集成
* Java的内存模型（Java堆-Heap）
  * 对象实例的分配区域
  * GC管理的主要区域
  * 新生代（Eden、survivor、survivor）和老年代
* JVM三大性能调优参数 -Xms -Xmx -Xss的含义
  * `java -Xms128m -Xmx128m -Xss256k -jar xxx.jar`
  * `-Xss`：规定了每个线程虚拟机栈（堆栈）的大小（会影响并发线程数的大小）
  * `-Xms`：堆的初始值
  * `-Xmx`：堆能达到的最大值
* Java内存模型中堆和栈的区别-内存分配策略
  * 静态存储：编译时确定每个数据目标在运行时的存储空间需求
  * 栈式存储：数据区需求在编译时未知，运行时模块入口前确定
  * 堆式存储：编译时或运行时模块入口都无法确定，动态分配
* Java内存模型中堆和栈的区别
  * 联系：引用对象、数组时，栈里定义变量保存堆中目标的首地址
    * 使用new开辟堆内存空间
    * 堆内存存储的是对象实例和数组
    * 栈内存存的是地址，指向对应堆内存空间
  * 管理方式：栈自动释放，堆需要GC
  * 空间大小：栈比堆小
  * 碎片相关：栈产生的碎片远小于堆
  * 分配方式：栈支持动态和静态分配，而堆仅支持动态分配
  * 效率：栈的效率比堆高

# GC

* 对象被判定为垃圾的标准
  
  - 没有被其他对象引用
* 判定对象是否为垃圾的算法
  - 引用计数算法
    - 通过判定对象的引用数量来决定对象是否可以被回收
    - 每个对象实例都有一个引用计数器，被引用则+1，完成引用则-1
    - 任何引用计数为0的对象实例可以被当作垃圾收集
    - 优点：执行效率高，程序执行受影响小
    - 缺点：无法检测出循环引用的情况，导致内存泄漏
  - 可达性分析算法
    - 通过判断对象的引用链是否可达来决定对象是否可以被回收
    - 可做GC Root的对象
      - 虚拟机栈中引用的对象（栈帧中的本地变量表）
      - 方法区中的常量引用的对象
      - 方法区中的类静态属性引用的对象
      - 本地方法栈中JNI（Native方法）的引用对象
      - 活跃线程的引用对象

* 垃圾回收算法
  - 标记-清除算法
    - 标记：从根集合进行扫描，对存活的对象进行标记
    - 清除：对堆内存从头到尾进行线性遍历，回收不可达对象内存
    - 缺点：碎片化
  - 复制算法
    - 分为对象面和空闲面，对象在对象面上创建
    - 存活的对象被从对象面复制到空闲面
    - 将对象面的所有对象内存清除
    - 适合对象存活率低的场景，如年轻代（10%的存活率）
    - 优点：解决碎片化问题、顺序分配内存，简单高效
  - 标记-整理算法
    - 标记：从根集合进行扫描，对存活的对象进行标记
    - 清除：移动所有存活的对象，且按照内存地址次序依次排列，然后将末端内存地址以后的内存全部回收
    - 优点：避免内存的不连续性、不用设置两块内存互换、适用于存活率高的场景（老年代）
  - 分代收集算法
    - 垃圾回收算法的组合拳（年轻代存活率低一般采用复制算法，老年代存活率高一般采用标记清除算法或标记整理算法）
    - 按照对象生命周期的不同划分区域以采用不同的垃圾回收算法
    - 目的：提高JVM的回收效率
    - ![image-20190810090622547](/Users/dingyuanjie/Documents/study/github/woodyprogram/img/image-20190810090622547.png)

* GC的分类
  - Minor GC
  - Full GC
* 年轻代：尽可能快速的收集掉那些生命周期短的对象
  - Eden区
  - 两个Survivor区
  - ![image-20190810091315200](/Users/dingyuanjie/Documents/study/github/woodyprogram/img/image-20190810091315200.png)

* 年轻代垃圾回收过程

  - 对象在Eden区出生并挤满Eden区，触发Minor GC将存活的对象复制到S0区并将年龄设置为1，清除所有的Eden区；然后Eden区再次被填满，触发Minor GC，将Eden和S0区存活的对象复制到S1并将年龄加1，清空Eden区和S0区；Eden区被填满，触发Minor GC，将Enen区和S1区存活的对象拷贝到S0区并且年龄加1，清空Eden和S1

* 对象如何晋升到老年代

  * 经历一定Minor次数依然活跃的对象
  * Survivor区中存放不下的对象
  * 新生成的大对象（-XX:+PretenuerSizeThreshold）

* 常用的调优参数

  * -XX:SurvivorRatio：Eden和Survivor的比值，默认8:1
  * -XX:NewRatio：老年代和年轻代内存大小的比例
  * -XX:MaxTenuringThreshold：对象从年轻代晋升到老年代经过GC次数的最大阀值

* 老年代：存放生命周期较长的对象

  * 标记-清理算法
  * 标记-整理算法

* 老年代

  - Full GC和Minor GC
  - Full GC比Minor GC慢，但执行频率低

* 触发Full GC的条件

  * 老年代空间不足、永久代空间不足、CMS GC时出现promotion failed，concurrent mode failure、Minor GC晋升到老年代的平均大小大于老年代的剩余空间、调用System.gc()、使用RMI来进行RPC或管理的JDK应用，每小时执行1次Full GC

* Stop-the-World

  * JVM由于要执行GC而停止了应用程序的执行
  * 任何一种GC算法中都会发生
  * 多数GC优化通过减少STW发生的时间来提高程序性能

* Safepoint

  * 分析过程中对象引用关系不会发生变化的点
  * 产生Safepoint的地方：方法调用；循环跳转；异常跳转等
  * 安全点数量得适中

* 常见的垃圾收集器

  - JVM的运行模式`java -version`
    - Server
      - 启动慢，运行一段时间稳定后速度较快，重量级虚拟机对程序进行了相应的优化
    - Client
      - 启动较快
  - ![image-20190810100709419](/Users/dingyuanjie/Documents/study/github/woodyprogram/img/image-20190810100709419.png)
  - 年轻代常见的垃圾收集器
    - Serial收集器（-XX:+UseSerialGC，复制算法）
      - 单线程收集，进行垃圾收集时，必须暂停所有工作线程
      - 简单高效，Client模式下默认的年轻代收集器
      - 新生代采取复制算法暂停所有用户线程
    - ParNew收集器（-XX:+UseParNewGC，复制算法）
      - 多线程收集，其余的行为，特点和Serial收集器一样
      - 单核执行效率不如Serial，在多核下执行才有优势
      - 新生代采取复制算法暂停所有用户线程
    - Parallel Scavenge收集器（-XX:+UseParallelGC，复制算法）
      - 吞吐量=运行用户代码时间/（运行用户代码时间+垃圾收集时间）
      - 比起关注用户线程停顿时间，更关注系统的吞吐量
      - 在多核下执行才有优势，Server模式下默认的年轻代收集器
      - 新生代采取复制算法暂停所有用户线程
      - -XX:+UseAdaptiveSizePolicy

  * 老年代常见的垃圾收集器
    * Serial Old收集器（-XX:+UseSerialOldGC，标记-整理算法）
      * 单线程收集，进行垃圾收集时，必须暂停所有工作线程
      * 简单高效，Client模式下默认的老年代收集器
      * 老年代采取标记-整理算法暂停所有用户线程
    * Parallel Old收集器（-XX:+UseParallelOldGC，标记-整理算法）
      * 多线程，吞度量优先
    * CMS收集器（-XX:+UseConcMarkSweepGC，标记-清除算法）
      * 垃圾回收线程和用户线程几乎可以做到同时工作
        - 初始化标记：会stop-the-world，扫描能和根对象关联的对象做标记，很快完成
        - 并发标记：并发追溯标记，程序不会停顿
        - 并发预清理：查找执行并发标记阶段从年轻代晋升到老年代的对象
        - 重新标记：暂停虚拟机，扫描CMS堆中的剩余对象
        - 并发清理：清理垃圾对象，程序不会停顿
        - 并发重置：重置CMS收集器的数据结构
    * G1收集器（-XX:+UseG1GC，复制+标记-整理算法）
      * 既能用于年轻代，又能用于老年代
      * 特点
        - 并发与并行
          - 使用多个CPU来缩短stop the world的时间，与用户并发执行
        - 分代收集
          - 独立管理整个堆，但是能够采用不同的方式去处理新创建的对象和已经存活一段时间并且经过多次GC的对象来达到更好的收集效果
        - 空间整合
          - 基于标记-整理算法，解决了内存碎片问题
        - 可预测的停顿
          - 可设置停顿时间
        - 将整个Java堆内存划分为多个大小相等的Region
          - 年轻代和老年代不再物理隔离

* Object的finalize()方法的作用是否与C++的析构函数作用相同

  * 与C++的析构函数不同，析构函数调用确定，而它的是不确定的
  * 将未被引用的对象放置于F-Queue队列
  * 方法执行随时可能会被终止
  * 给予对象最后一次重生的机会

* Java中的强引用、软引用、弱引用、虚引用有什么用？

  * 强引用

    * 抛出OutOfMemoryError终止程序也不会回收具有强引用的对象，Object obj = new Object();
    * 通过将对象设置为null来弱化引用，使其被回收

  * 软引用

    * 只有当内存空间不足时，GC会回收该引用的对象的内存
    * 可以用来实现高速缓存

  * 弱引用

    * GC时会被回收
    * 适用于引用偶尔被使用且不影响垃圾收集的对象

  * 虚引用

    * 不会决定对象的生命周期，任何时候都可能被垃圾收集器回收
    * 跟踪对象被垃圾收集器回收的活动，起哨兵作用
    * 必须和引用队列ReferenceQueue联合使用

  * ```java
    String str = new String("abc"); // 强引用
    SoftReference<String> softRef = new SoftReference<String>(str); //软引用
    WeakReference<String> weakRef = new WeakReference<String>(str); //弱引用
    ReferenceQueue queue = new ReferenceQueue();
    PhantomReference ref = new PhantomReference(str, queue);  //虚引用
    ```

  * ![image-20190810103912680](/Users/dingyuanjie/Documents/study/github/woodyprogram/img/image-20190810103912680.png)

  * 引用队列

    * 无实际存储结构，存储逻辑依赖于内部节点之间的关系来表达
    * 存储关联的且被GC的软引用，弱引用以及虚引用
    * 外部可对ReferenceQueue进行监控，如果有对象即将被回收，相应的Reference对象将被放到该ReferenceQueue，然后就可以对Reference进行操作，如果不带ReferenceQueue，则会不断轮询Reference对象，通过判断get方法是否返回null来确认是否被回收了

  * ```java
    public class NormalObject {
        public String name;
        public NormalObject(String name){
            this.name = name;
        }
    
        @Override
        protected void finalize(){
            System.out.println("Finalizing obj " + name);
        }
    }
    ```

    ```java
    public class NormalObjectWeakReference extends WeakReference<NormalObject> {
        public String name;
    
        public NormalObjectWeakReference(NormalObject normalObject, ReferenceQueue<NormalObject> rq) {
            super(normalObject, rq);
            this.name = normalObject.name;
        }
        @Override
        protected void finalize(){
            System.out.println("Finalizing NormalObjectWeakReference " + name);
        }
    }
    ```

    ```java
    public class ReferenceQueueTest {
        private static ReferenceQueue<NormalObject> rq = new ReferenceQueue<NormalObject>();
    
        private static void checkQueue(){
            Reference<NormalObject> ref = null;
            while ((ref = (Reference<NormalObject>)rq.poll()) != null){
                if (ref != null){
                    System.out.println("In queue: " + ((NormalObjectWeakReference)(ref)).name);
                    System.out.println("reference object:" + ref.get());
                }
            }
        }
    
        public static void main(String[] args) {
            ArrayList<WeakReference<NormalObject>> weakList = new ArrayList<WeakReference<NormalObject>>();
            for (int i =0; i < 3 ; i++){
                weakList.add(new NormalObjectWeakReference(new NormalObject("Weak " + i),rq));
                System.out.println("Created weak:" + weakList.get(i));
            }
            System.out.println("first time");
            checkQueue();
            System.gc();
            try {
                Thread.currentThread().sleep(1000);
            } catch (InterruptedException e){
                e.printStackTrace();
            }
            System.out.println("second time");
            checkQueue();
        }
    }
    ```

    ![image-20190810105650499](/Users/dingyuanjie/Documents/study/github/woodyprogram/img/image-20190810105650499.png)

# 多线程

* 进程和线程的区别

  * 进程和线程的由来
    * 串行：初期的计算机只能串行执行任务，并且需要长时间等待用户输入
    * 批处理：预先将用户的指令集中成清单，批量串行处理用户指令，仍然无法并发执行
    * 进程：进程独占内存空间，保存各自运行状态，相互间不干扰且可以互相切换，为并发处理任务提供了可能
    * 线程：共享进程的内存资源，相互间切换更快捷，支持更细粒度的任务控制，使进程内的子任务得以并发执行
  * 区别
    * 线程不能看作独立应用，而进程可看作独立应用
    * 进程有独立的地址空间，相互不影响，线程只是进程的不同执行路径
    * 线程没有独立的地址空间，多进程的程序比多线程程序健壮
    * 进程的切换比线程的切换开销大

* Java进程和线程的关系

  * Java对操作系统提供的功能进行封装，包括进程和线程
  * 运行一个程序会产生一个进程，进程包含至少一个线程
  * 每个进程对应一个JVM实例，多个线程共享JVM里的堆
  * Java采用单线程编程模型，程序会自动创建主线程
  * 主线程可以创建子线程，原则上要后于子线程完成执行

* Thread中的start和run方法的区别

  * 调用start()方法会创建一个新的子线程并启动
  * run()方法只是Thread的一个普通方法的调用

* Thread和Runnable时什么关系

  * Thread是实现了Runnable接口的类，使得run支持多线程
  * 因类的单一继承原则，推荐多使用Runnable接口

* 如何给run()方法传参

  * 构造函数传参、成员变量传参、回调函数传参

* 如何实现处理线程的返回值

  - 主线程等待法

  - 使用Thread类的join()阻塞当前线程以等待子线程处理完毕

  - 通过Callable接口实现：通过FutureTask Or 线程池获取

  - ```java
    public class CycleWait implements Runnable{
        private String value;
        public void run() {
            try {
                Thread.currentThread().sleep(5000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            value = "we have data now";
        }
    
        public static void main(String[] args) throws InterruptedException {
            CycleWait cw = new CycleWait();
            Thread t = new Thread(cw);
            t.start();
            // 主线程等待法
    //        while (cw.value == null){
    //            Thread.currentThread().sleep(100);
    //        }
            // join阻塞当前线程以等待子线程处理完毕
            t.join();
            System.out.println("value : " + cw.value);
        }
    }
    ```

    ```java
    public class MyCallable implements Callable<String> {
        @Override
        public String call() throws Exception{
            String value="test";
            System.out.println("Ready to work");
            Thread.currentThread().sleep(5000);
            System.out.println("task done");
            return  value;
        }
    }
    
    public class FutureTaskDemo {
        public static void main(String[] args) throws ExecutionException, InterruptedException {
            FutureTask<String> task = new FutureTask<String>(new MyCallable());
            new Thread(task).start();
            if(!task.isDone()){
                System.out.println("task has not finished, please wait!");
            }
            System.out.println("task return: " + task.get());
        }
    }
    
    public class ThreadPoolDemo {
        public static void main(String[] args) {
            ExecutorService newCachedThreadPool = Executors.newCachedThreadPool();
            Future<String> future = newCachedThreadPool.submit(new MyCallable());
            if(!future.isDone()){
                System.out.println("task has not finished, please wait!");
            }
            try {
                System.out.println(future.get());
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            } finally {
                newCachedThreadPool.shutdown();
            }
        }
    }
    ```

* 线程的状态

  * 新建（New）：创建后尚未启动
  * 运行（Runnable）：包含Running和Ready
  * 无限期等待（Waiting）：不会被分配CPU执行时间，需要显示被唤醒
    * 没有设置Timeout参数 的Object.wait()
    * 没有设置Timeout参数的Thread.join()
    * LockSupport.park()
  * 限期等待（Timed Waiting）：在一定时间后会由系统自动唤醒
    * Thread.sleep()
    * 设置了Timeout参数 的Object.wait()
    * 设置了Timeout参数的Thread.join()
    * LockSupport.parkNanos()
    * LockSupport.parkUntil()
  * 阻塞（Blocked）：等待获取排他锁
  * 结束（Terminated）：已终止线程的状态，线程已经结束执行

* sleep和wait的区别

  * sleep是Thread类的方法，wait是Object类中定义的方法

  * sleep()可以在任何地方使用

  * wait()只能在synchronized方法或synchronized块中使用

    * 需要先获得锁

  * Thread.sleep只会让出CPU，不会导致锁行为的改变

  * Object.wait不仅让出CPU，还会释放已经占有的同步资源锁

    ```java
    public class WaitSleepDemo {
        public static void main(String[] args) {
            final Object lock = new Object();
            new Thread(new Runnable() {
                @Override
                public void run() {
                    System.out.println("thread A is waiting to get lock");
                    synchronized (lock){
                        try {
                            System.out.println("thread A get lock");
                            Thread.sleep(20);
                            System.out.println("thread A do wait method");
                            lock.wait();
                            System.out.println("thread A is done");
                        } catch (InterruptedException e){
                            e.printStackTrace();
                        }
                    }
                }
            }).start();
            try{
                Thread.sleep(10);
            } catch (InterruptedException e){
                e.printStackTrace();
            }
            new Thread(new Runnable() {
                @Override
                public void run() {
                    System.out.println("thread B is waiting to get lock");
                    synchronized (lock){
                        try {
                            System.out.println("thread B get lock");
                            System.out.println("thread B is sleeping 10 ms");
                            Thread.sleep(10);
                            lock.notifyAll();
                            Thread.yield();
                            Thread.sleep(2000);
                            System.out.println("thread B is done");
                        } catch (InterruptedException e){
                            e.printStackTrace();
                        }
                    }
                }
            }).start();
        }
    }
    ```

* notify和notifyAll的区别

  * 锁池（EntryList）

    * 假设线程A已经拥有了某个对象（不是类）的锁，而其他线程B、C想要调用这个对象的某个synchronized方法（或者块），由于B、C线程在进入对象的synchronized方法（或者块）之前必须先获得该对象锁的拥有权，而恰巧该对象的锁目前正被线程A所占用，此时B、C线程就会被阻塞，进入一个地方去等待锁的释放——这个地方便是该对象的锁池

  * 等待池（WaitSet）

    * 假设线程A调用了某个对象的wait()，线程A就会释放该对象的锁，同时线程A就进入到了该对象的等待池中，进入到等待池中的线程不会去竞争该对象的锁

  * notifyAll会让所有处于等待池的线程全部进入锁池去竞争获取锁的机会

  * notify只会随机选取一个处于等待池中的线程进入锁池去竞争获取锁的机会

  * ```java
    public class NotificationDemo {
        private volatile boolean go = false;
    
        public static void main(String args[]) throws InterruptedException {
            final NotificationDemo test = new NotificationDemo();
    
            Runnable waitTask = new Runnable(){
    
                @Override
                public void run(){
                    try {
                        test.shouldGo();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    System.out.println(Thread.currentThread().getName() + " finished Execution");
                }
            };
    
            Runnable notifyTask = new Runnable(){
    
                @Override
                public void run(){
                    test.go();
                    System.out.println(Thread.currentThread().getName() + " finished Execution");
                }
            };
    
            Thread t1 = new Thread(waitTask, "WT1"); //will wait
            Thread t2 = new Thread(waitTask, "WT2"); //will wait
            Thread t3 = new Thread(waitTask, "WT3"); //will wait
            Thread t4 = new Thread(notifyTask,"NT1"); //will notify
    
            //starting all waiting thread
            t1.start();
            t2.start();
            t3.start();
    
            //pause to ensure all waiting thread started successfully
            Thread.sleep(200);
    
            //starting notifying thread
            t4.start();
    
        }
        /*
         * wait and notify can only be called from synchronized method or bock
         */
        private synchronized void shouldGo() throws InterruptedException {
            while(go != true){
                System.out.println(Thread.currentThread()
                        + " is going to wait on this object");
                wait(); //release lock and reacquires on wakeup
                System.out.println(Thread.currentThread() + " is woken up");
            }
            go = false; //resetting condition
        }
    
        /*
         * both shouldGo() and go() are locked on current object referenced by "this" keyword
         */
        private synchronized void go() {
            while (go == false){
                System.out.println(Thread.currentThread()
                        + " is going to notify all or one thread waiting on this object");
    
                go = true; //making condition true for waiting thread
                //notify(); // only one out of three waiting thread WT1, WT2,WT3 will woke up
                notifyAll(); // all waiting thread  WT1, WT2,WT3 will woke up
            }
    
        }
    }
    ```

    ![image-20190810143044680](/Users/dingyuanjie/Documents/study/github/woodyprogram/img/image-20190810143044680.png)

    ![image-20190810143015163](/Users/dingyuanjie/Documents/study/github/woodyprogram/img/image-20190810143015163.png)

* yield

  * 当调用Thread.yield()函数时，会给线程调度器一个当前线程愿意让出CPU使用的暗示，但是线程调度器可能会忽略这个暗示

* 如何中断线程

  * 调用interrupt()，通知线程应该中断了

    * 如果线程处于被阻塞状态，那么线程将立即退出被阻塞状态，并抛出一个InterruptedException异常
    * 如果线程处于正常活动状态，那么会将该线程的中断标识设置为true，被设置中断标志的线程将继续正常运行，不受影响

  * 需要被调用的线程配合中断

    * 在正常运行任务时，经常检查本线程的中断标志位，如果被设置了中断标志就自行停止线程

    * 如果线程处于正常活动状态，那么会将该线程的中断标志设置为true，被设置中断标志的线程将继续正常运行，不受影响

    * ```java
      public class InterruptDemo {
          public static void main(String[] args) throws InterruptedException {
              Runnable interruptTask = new Runnable() {
                  @Override
                  public void run() {
                      int i = 0;
                      try {
                          //在正常运行任务时，经常检查本线程的中断标志位，如果被设置了中断标志就自行停止线程
                          while (!Thread.currentThread().isInterrupted()) {
                              Thread.sleep(100); // 休眠100ms
                              i++;
                              System.out.println(Thread.currentThread().getName() + " (" + Thread.currentThread().getState() + ") loop " + i);
                          }
                      } catch (InterruptedException e) {
                          //在调用阻塞方法时正确处理InterruptedException异常。（例如，catch异常后就结束线程。）
                          System.out.println(Thread.currentThread().getName() + " (" + Thread.currentThread().getState() + ") catch InterruptedException.");
                      }
                  }
              };
              Thread t1 = new Thread(interruptTask, "t1");
              System.out.println(t1.getName() +" ("+t1.getState()+") is new.");
      
              t1.start();                      // 启动“线程t1”
              System.out.println(t1.getName() +" ("+t1.getState()+") is started.");
      
              // 主线程休眠300ms，然后主线程给t1发“中断”指令。
              Thread.sleep(300);
              t1.interrupt();
              System.out.println(t1.getName() +" ("+t1.getState()+") is interrupted.");
      
              // 主线程休眠300ms，然后查看t1的状态。
              Thread.sleep(300);
              System.out.println(t1.getName() +" ("+t1.getState()+") is interrupted now.");
          }
      }
      ```

* ![image-20190810144025915](/Users/dingyuanjie/Documents/study/github/woodyprogram/img/image-20190810144025915.png)

* 线程安全问题的主要诱因

  * 存在共享数据（也称临界资源）
  * 存在多条线程共同操作这些共享数据
  * 解决
    * 同一时刻有且只有一个线程在操作共享数据，其他线程必须等到该线程处理完数据后再对共享数据进行操作

* 互斥锁的特性

  * 互斥性：即在同一时间只允许一个线程持有某个对象锁，通过这种特性来实现多线程的协调机制，这样在同一时间只有一个线程对需要同步的代码块（复合操作）进行访问。互斥性也被称为操作的原子性
  * 可见性：必须确保在锁被释放之前，对共享变量所做的修改，对于随后获得该锁的另一个线程是可见的（即在获得锁时应获得最新共享变量的值），否则另一个线程可能是在本地缓存的某个副本上继续操作，从而引起不一致
  * synchronized锁的不是代码，锁的都是对象

* synchronized

  * 根据获取的锁的分类：获取对象锁和获取类锁

    * 获取对象锁的两种方法
      * 同步代码块
        * synchronized(this)、synchronized(类实例对象)
        * 锁是小括号中的实例对象
      * 同步非静态方法
        * 锁是当前对象的实例对象
    * 获取类锁的两种方法
      * 同步代码块
        * synchronized(类.class)，锁是Class对象
      * 同步静态方法
        * synchronized static method，锁是Class对象

  * 对象锁和类锁的总结

    * 有线程访问对象的同步代码块时，另外的线程可以访问该对象的非同步代码块

    * 若锁住的是同一个对象，一个线程在访问对象的同步代码块时，另一个访问对象的同步代码块的线程会被阻塞

    * 若锁住的是同一个对象，一个线程在访问对象的同步方法时，另一个访问该对象的同步方法的线程会被阻塞

    * 若锁住的是同一个对象，一个线程在访问对象的同步代码块时，另一个访问对象同步方法的线程会被阻塞，反之亦然

    * 同一个类的不同对象的对象锁互不干扰

    * 类锁由于也是一种特殊的对象锁，因此表现和上述1，2，3，4一致，而由于一个类只有一把对象锁，所以同一个类的不同对象使用类锁将会是同步的

    * 类锁和对象锁互不干扰

    * ```java
      public class SyncThread implements Runnable {
      
          @Override
          public void run() {
              String threadName = Thread.currentThread().getName();
              if (threadName.startsWith("A")) {
                  async();
              } else if (threadName.startsWith("B")) {
                  syncObjectBlock1();
              } else if (threadName.startsWith("C")) {
                  syncObjectMethod1();
              } else if (threadName.startsWith("D")) {
                  syncClassBlock1();
              } else if (threadName.startsWith("E")) {
                  syncClassMethod1();
              }
      
          }
      
          /**
           * 异步方法
           */
          private void async() {
              try {
                  System.out.println(Thread.currentThread().getName() + "_Async_Start: " + new SimpleDateFormat("HH:mm:ss").format(new Date()));
                  Thread.sleep(1000);
                  System.out.println(Thread.currentThread().getName() + "_Async_End: " + new SimpleDateFormat("HH:mm:ss").format(new Date()));
              } catch (InterruptedException e) {
                  e.printStackTrace();
              }
          }
      
          /**
           * 方法中有 synchronized(this|object) {} 同步代码块
           */
          private void syncObjectBlock1() {
              System.out.println(Thread.currentThread().getName() + "_SyncObjectBlock1: " + new SimpleDateFormat("HH:mm:ss").format(new Date()));
              synchronized (this) {
                  try {
                      System.out.println(Thread.currentThread().getName() + "_SyncObjectBlock1_Start: " + new SimpleDateFormat("HH:mm:ss").format(new Date()));
                      Thread.sleep(1000);
                      System.out.println(Thread.currentThread().getName() + "_SyncObjectBlock1_End: " + new SimpleDateFormat("HH:mm:ss").format(new Date()));
                  } catch (InterruptedException e) {
                      e.printStackTrace();
                  }
              }
          }
      
          /**
           * synchronized 修饰非静态方法
           */
          private synchronized void syncObjectMethod1() {
              System.out.println(Thread.currentThread().getName() + "_SyncObjectMethod1: " + new SimpleDateFormat("HH:mm:ss").format(new Date()));
              try {
                  System.out.println(Thread.currentThread().getName() + "_SyncObjectMethod1_Start: " + new SimpleDateFormat("HH:mm:ss").format(new Date()));
                  Thread.sleep(1000);
                  System.out.println(Thread.currentThread().getName() + "_SyncObjectMethod1_End: " + new SimpleDateFormat("HH:mm:ss").format(new Date()));
              } catch (InterruptedException e) {
                  e.printStackTrace();
              }
          }
      
          private void syncClassBlock1() {
              System.out.println(Thread.currentThread().getName() + "_SyncClassBlock1: " + new SimpleDateFormat("HH:mm:ss").format(new Date()));
              synchronized (SyncThread.class) {
                  try {
                      System.out.println(Thread.currentThread().getName() + "_SyncClassBlock1_Start: " + new SimpleDateFormat("HH:mm:ss").format(new Date()));
                      Thread.sleep(1000);
                      System.out.println(Thread.currentThread().getName() + "_SyncClassBlock1_End: " + new SimpleDateFormat("HH:mm:ss").format(new Date()));
                  } catch (InterruptedException e) {
                      e.printStackTrace();
                  }
              }
          }
      
          private synchronized static void syncClassMethod1() {
              System.out.println(Thread.currentThread().getName() + "_SyncClassMethod1: " + new SimpleDateFormat("HH:mm:ss").format(new Date()));
              try {
                  System.out.println(Thread.currentThread().getName() + "_SyncClassMethod1_Start: " + new SimpleDateFormat("HH:mm:ss").format(new Date()));
                  Thread.sleep(1000);
                  System.out.println(Thread.currentThread().getName() + "_SyncClassMethod1_End: " + new SimpleDateFormat("HH:mm:ss").format(new Date()));
              } catch (InterruptedException e) {
                  e.printStackTrace();
              }
          }
      }
      ```

      ```java
      public class SyncDemo {
          public static void main(String... args) {
              SyncThread syncThread = new SyncThread();
              Thread A_thread1 = new Thread(syncThread, "A_thread1");
              Thread A_thread2 = new Thread(syncThread, "A_thread2");
              Thread B_thread1 = new Thread(syncThread, "B_thread1");
              Thread B_thread2 = new Thread(syncThread, "B_thread2");
              Thread C_thread1 = new Thread(syncThread, "C_thread1");
              Thread C_thread2 = new Thread(syncThread, "C_thread2");
              Thread D_thread1 = new Thread(syncThread, "D_thread1");
              Thread D_thread2 = new Thread(syncThread, "D_thread2");
              Thread E_thread1 = new Thread(syncThread, "E_thread1");
              Thread E_thread2 = new Thread(syncThread, "E_thread2");
              A_thread1.start();
              A_thread2.start();
              B_thread1.start();
              B_thread2.start();
              C_thread1.start();
              C_thread2.start();
              D_thread1.start();
              D_thread2.start();
              E_thread1.start();
              E_thread2.start();
          }
      }
      ```

      ![image-20190810144918539](/Users/dingyuanjie/Documents/study/github/woodyprogram/img/image-20190810144918539.png)

      ![image-20190810145152619](/Users/dingyuanjie/Documents/study/github/woodyprogram/img/image-20190810145152619.png)

      ![image-20190810145212451](/Users/dingyuanjie/Documents/study/github/woodyprogram/img/image-20190810145212451.png)

* synchronized底层实现原理

  * 实现synchronized的基础
    * Java对象头
      * Mark Word，默认存储对象的hashCode，分代年龄，锁类型，锁标志位等信息
      * ![image-20190810145948338](/Users/dingyuanjie/Documents/study/github/woodyprogram/img/image-20190810145948338.png)
      * Class Metadata Address，类型指针指向对象的类元数据，JVM通过这个指针确定该对象是那个类的数据
    * Monitor
      * 每个Java对象天生自带了一把看不见的锁
  * 对象在内存中的布局
    * 对象头、实例数据、对齐填充

* 什么是重入

  * 从互斥锁的设计上来说，当一个线程试图操作一个由其他线程持有的对象锁的临界资源时，将会处于阻塞状态，但当一个线程再次请求自己持有对象锁的临界资源时，这种情况属于重入

* 自旋锁

  * 许多情况下，共享数据的锁定状态持续时间较短，切换线程不值得
  * 通过让线程执行忙循环等待锁的释放，不让出CPU
  * 缺点：若锁被其他线程长时间占用，会带来许多性能上的开销

* 自适应自旋锁

  * 自旋的次数不再固定
  * 由前一次在同一个锁上的自旋时间及锁的拥有者的状态来决定

* 锁消除

  * 更彻底的优化，JIT编译时，对运行上下文进行扫描，去除不可能存在竞争的锁

* 锁粗化

  * 另一种极端，通过扩大加锁的范围，避免反复加锁和解锁

* synchronized的四种状态

  * （锁膨胀方向）无锁-偏向锁-轻量级锁-重量级锁

* 偏向锁

  * 减少同一线程获取锁的代价，CAS（Compare And Swap）
  * 大多数情况下，锁不存在多线程竞争，总是由同一线程多次获得
  * 核心思想：
    * 如果一个线程获得了锁，那么锁就进入偏向模式，此时Mark Word的结构也变为偏向锁结构，当该线程再次请求锁时，无需再做任何同步操作，即获取锁的过程只需要检查Mark Word的锁标记位为偏向锁以及当前线程Id等于Mark Word的ThreadID即可，这样就省去了大量有关锁申请的操作
    * 不适用于锁竞争比较激烈的多线程场合

* 轻量级锁

  * 是由偏向锁升级而来的，偏向锁运行在一个线程进入同步块的情况下，当第二个线程加入锁争用的时候，偏向锁就会升级为轻量级锁

  * 适用场景：线程交替执行同步块

  * 若存在同一时间访问同一锁的情况，就会导致轻量级锁膨胀为重量级锁

  * ![image-20190810151847944](/Users/dingyuanjie/Documents/study/github/woodyprogram/img/image-20190810151847944.png)

  * ![image-20190810151939210](/Users/dingyuanjie/Documents/study/github/woodyprogram/img/image-20190810151939210.png)

  * 

    ![image-20190810152014284](/Users/dingyuanjie/Documents/study/github/woodyprogram/img/image-20190810152014284.png)

* ![image-20190810152101938](/Users/dingyuanjie/Documents/study/github/woodyprogram/img/image-20190810152101938.png)

* ![image-20190810152634452](/Users/dingyuanjie/Documents/study/github/woodyprogram/img/image-20190810152634452.png)

* ReentrantLock（再入锁）

  * 位于java.util.concurrent.locks包
  * 和CountDownLatch、FutureTask、Semaphore一样基于AQS实现
  * 能够实现比synchronized更细粒度的控制，如控制fairness
  * 调用lock()之后，必须调用unlock()释放锁
  * 性能未必比synchronized高，并且也是可重入的

* ReentrantLock公平性的设置

  * ReentrantLock fairLock = new ReentrantLock(true);
  * 参数为true时，倾向于将锁赋予等待时间最久的线程
  * 公平锁：获取锁的顺序按先后调用lock()的顺序（慎用）
  * 非公平锁：抢占的顺序不一定，看运气
  * synchronized是非公平锁

  * ```java
    public class ReentrantLockDemo implements  Runnable{
        private static ReentrantLock lock = new ReentrantLock(false);
        @Override
        public void run(){
            while (true){
                try{
                    lock.lock();
                    System.out.println(Thread.currentThread().getName() + " get lock");
                    Thread.sleep(1000);
                } catch (Exception e){
                    e.printStackTrace();
                } finally {
                    lock.unlock();
                }
            }
        }
    
        public static void main(String[] args) {
            ReentrantLockDemo rtld = new ReentrantLockDemo();
            Thread thread1 = new Thread(rtld);
            Thread thread2 = new Thread(rtld);
            thread1.start();
            thread2.start();
        }
    }
    ```

* ReentrantLock将锁对象化

  * 判断是否有线程，或者某个特定线程，在排队等待获取锁
  * 带超时的获取锁的尝试
  * 感知有没有成功获取锁

* synchronized和ReentrantLock的区别

  * synchronized是关键字，ReentrantLock是类
  * ReentrantLock可以对获取锁的等待时间进行设置，避免死锁
  * ReentrantLock可以获取各种锁的信息
  * ReentrantLock可以灵活地实现多路通知
  * 机制：sync操作Mark Word，lock调用Unsafe类的park()

* 什么是Java内存模型中的happens-before
  
  * ![image-20190810153605210](/Users/dingyuanjie/Documents/study/github/woodyprogram/img/image-20190810153605210.png)
* JMM中的主内存
  
  * 存储Java实例对象，包括成员变量、类信息、常量、静态变量等，属于数据共享的区域，多线程并发操作时会引发线程安全问题
* JMM中的工作内存
  * 存储当前方法的所有本地变量信息，本地变量对其他线程不可见
  * 字节码行号指示器、Native方法信息
  * 属于线程私有数据区域，不存在线程安全问题
* JMM与Java内存区域划分是不同的概念层次
  * JMM描述的是一组规则，围绕原子性、有序性、可见性展开
  * 相似点：存在共享区域和私有区域
* 主内存与工作内存的数据存储类型以及操作方式归纳
  * 方法里的基本数据类型本地变量将直接存储在工作内存的栈帧结构中
  * 引用类型的本地变量：引用存储在工作内存中，实例存储在主内存中
  * 成员变量、static变量、类信息均会被存储在主内存中
  * 主内存共享的方式是线程各拷贝一份数据到工作内存，操作完成后刷新回主内存
  * ![image-20190810153942006](/Users/dingyuanjie/Documents/study/github/woodyprogram/img/image-20190810153942006.png)
* 指令重排序需要满足的条件
  * 在单线程环境下不能改变程序运行的结果
  * 存在数据依赖关系的不允许重排序
  * 无法通过happens-before原则推导出来的，才能进行指令重排序
* happens-before
  * A操作的结果需要对B操作可见，则A与B存在happens-before关系
  * ![image-20190810154149465](/Users/dingyuanjie/Documents/study/github/woodyprogram/img/image-20190810154149465.png)
  * 如果两个操作不满足上述任意一个happens-before规则，那么这两个操作就没有顺序的保障，JVM可以对这两个操作进行重排序
  * 如果操作A happens-before操作B，那么操作A在内存上所作的操作对操作B都是可见的
* volatile
  * JVM提供的轻量级同步机制
  * 保证被volatile修饰的共享变量对所有线程总是可见的
  * 禁止指令重排序优化
* volatile变量为何立即可见
  * 当写一个volatile变量时，JMM会把该线程对应的工作内存中的共享变量值刷新到主内存中
  * 当读取一个volatile变量时，JMM会把该线程对应的工作内存置为无效
* volatile如何禁止重排优化
  * 内存屏障（Memory Barrier）
    * 保证特定操作的执行顺序
    * 保证某些变量的内存可见性
  * 通过插入内存屏障指令禁止在内存屏障前后的指令执行重排序优化
  * 强制刷出各种CPU的缓存数据，因此任何CPU上的线程都能读取到这些数据的最新版本
* ![image-20190810155308354](/Users/dingyuanjie/Documents/study/github/woodyprogram/img/image-20190810155308354.png)
* ![image-20190810155331839](/Users/dingyuanjie/Documents/study/github/woodyprogram/img/image-20190810155331839.png)
* CAS（Compare and Swap）
  * 一种高效实现线程安全性的方法
    * 支持原子更新操作，适用于计数器，序列发生器等场景
    * 属于乐观锁机制，号称lock-free
    * CAS操作失败时由开发者决定是继续尝试，还是执行别的操作
  * 思想：包含三个操作数：内存位置（V）、预期原值（A）和新值（B）
  * CAS多数情况下对开发者来说是透明的
    * J.U.C的atomic包提供了常用的原子性数据类型以及引用、数组等相关原子类型和更新操作工具，是很多线程安全程序的首选
    * Unsafe类虽提供CAS服务，但因能够操纵任意内存地址读写而有隐患
    * Java9以后，可以使用Variable Handle API来替代Unsafe
  * 缺点
    * 若循环时间长，则开销很大
    * 只能保证一个共享变量的原子操作
    * ABA问题，结局：AtomicStampedReference
* ![image-20190810155937758](/Users/dingyuanjie/Documents/study/github/woodyprogram/img/image-20190810155937758.png)
* ![image-20190810160133836](/Users/dingyuanjie/Documents/study/github/woodyprogram/img/image-20190810160133836.png)
* 为什么要使用线程池
  * 降低资源消耗
  * 提高线程的可管理性
* ![image-20190810160352674](/Users/dingyuanjie/Documents/study/github/woodyprogram/img/image-20190810160352674.png)
* J.U.C的三个Executor接口
  * Executor：运行新任务的简单接口，将任务提交和任务执行细节解耦
  * ExecutorService：具备管理执行器和任务生命周期的方法，提交任务机制更完善
  * ScheduledExecutorService：支持Future和定期执行任务
* ![image-20190810160622158](/Users/dingyuanjie/Documents/study/github/woodyprogram/img/image-20190810160622158.png)
* ThreadPoolExecutor的构造函数
  * corePoolSize：核心线程数量
  * maximumPoolSize：线程不够用时能够创建的最大线程数
  * workQueue：任务等待队列
  * keepAliveTime：抢占的顺序不一定，看运气
  * threadFactory：创建新线程，Executor.defaultThreadFactory()
  * handler：线程池的饱和策略
    * AbortPolicy：直接抛出异常（默认）
    * CallerRunsPolicy：用调用者所在的线程来执行任务
    * DiscardOldestPolicy：丢弃队列中最靠前的任务，并执行当前任务
    * DiscardPolicy：直接丢弃任务
    * 实现RejectedExecutionHandler接口的自定义handler
  * ![image-20190810161010929](/Users/dingyuanjie/Documents/study/github/woodyprogram/img/image-20190810161010929.png)
  * ![image-20190810161101074](/Users/dingyuanjie/Documents/study/github/woodyprogram/img/image-20190810161101074.png)
* 线程池的状态
  * RUNNING：能接受新提交的任务，并且也能处理阻塞队列中的任务
  * SHUTDOWN：不再接受新提交的任务，但可以处理存量任务
  * STOP：不再接受新提交的任务，也不处理存量任务
  * TIDYING：所有的任务都已终止
  * TERMINATED：terminated()方法执行完后进入该状态
* ![image-20190810161316160](/Users/dingyuanjie/Documents/study/github/woodyprogram/img/image-20190810161316160.png)
* ![image-20190810161343084](/Users/dingyuanjie/Documents/study/github/woodyprogram/img/image-20190810161343084.png)
* 线程池的大小如何选定
  * CPU密集型：线程数=按照核数或者核数+1 设定
  * I/O密集型：线程数=CPU核数*（1+平均等待时间/平均工作时间）

# 类库

* Java异常
  * 什么被抛出：异常类型
  * 在哪抛出：异常堆栈跟踪
  * 为什么被抛出：异常信息
* Error和Exception的区别
  * Java的异常体系
    * Throwable
      * Error
      * Exception
        * RuntimeException：不可预知的，程序应当自自行避免
        * 非RuntimeException：可预知的，从编译器校验的异常
  * 从责任角度看
    * Error属于JVM需要负担的责任
    * RuntimeException是程序应该负担的责任
    * CheckedException可检查异常是Java编译器应该负担的责任
  * 从概念角度解析Java的异常处理机制
    * Error：程序无法处理的系统错误，编译器不做检查
    * Exception：程序可以处理的异常，捕获后可能恢复
  * 前者是程序无法处理的错误，后者是可以处理的异常
  * ![image-20190810163717836](/Users/dingyuanjie/Documents/study/github/woodyprogram/img/image-20190810163717836.png)
  * Java的异常处理机制
    * 抛出异常：创建异常对象，交由运行时系统处理
    * 捕获异常：寻找合适的异常处理器处理异常，否则终止运行
  * Java异常的处理原则
    * 具体明确：抛出的异常应能通过异常类名和message准确说明异常的类型和产生异常的原因
    * 提早抛出：应尽可能早地发现并抛出异常，便于精确定位问题
    * 延迟捕获：异常的捕获和处理应尽可能延迟，让掌握更多信息的作用域来处理异常
  * ![image-20190810164536725](/Users/dingyuanjie/Documents/study/github/woodyprogram/img/image-20190810164536725.png)
    * 设计一个通用的继承自RuntimeException的异常来统一处理
    * 其余异常都统一转译为上述异常AppException
    * 在catch之后，抛出上述异常的子类，并提供足以定位的信息
    * 由前端接收AppException做统一处理
  * t ry-catch的性能
    * Java异常处理消耗性能的地方
      * try-catch块影响JVM的优化
      * 异常对象实例需要保存栈快照等信息，开销大
* ![image-20190810164827033](/Users/dingyuanjie/Documents/study/github/woodyprogram/img/image-20190810164827033.png)
* ![image-20190810164914716](/Users/dingyuanjie/Documents/study/github/woodyprogram/img/image-20190810164914716.png)
* ![image-20190810165033259](/Users/dingyuanjie/Documents/study/github/woodyprogram/img/image-20190810165033259.png)
* ![image-20190810165144006](/Users/dingyuanjie/Documents/study/github/woodyprogram/img/image-20190810165144006.png)
* ![image-20190810170557357](/Users/dingyuanjie/Documents/study/github/woodyprogram/img/image-20190810170557357.png)
* ![image-20190810170744029](/Users/dingyuanjie/Documents/study/github/woodyprogram/img/image-20190810170744029.png)
* ![image-20190810170828128](/Users/dingyuanjie/Documents/study/github/woodyprogram/img/image-20190810170828128.png)
* ![image-20190810171213444](/Users/dingyuanjie/Documents/study/github/woodyprogram/img/image-20190810171213444.png)
* HashMap：如何有效减少碰撞
  * 扰动函数：促使元素位置分布均匀，减少碰撞几率
  * 使用final对象，并采用合适的equals()和hashCode()方法
* ![image-20190810171608898](/Users/dingyuanjie/Documents/study/github/woodyprogram/img/image-20190810171608898.png)
* HashMap：扩容的问题
  * 多线程环境下，调整大小会存在条件竞争，容易造成死锁
  * rehashing是一个比较耗时的过程
* 如何优化Hashtable？
  * 通过锁细粒度化，将整锁拆解成多个锁进行优化
* ![image-20190810172146869](/Users/dingyuanjie/Documents/study/github/woodyprogram/img/image-20190810172146869.png)
* ![image-20190810172225888](/Users/dingyuanjie/Documents/study/github/woodyprogram/img/image-20190810172225888.png)
* ![image-20190810172733996](/Users/dingyuanjie/Documents/study/github/woodyprogram/img/image-20190810172733996.png)
* ConcurrentHashMap总结
  * 比起Segment，锁拆得更细
    - 首先使用无锁操作CAS插入头节点，失败则循环重试
    - 若头节点已存在，则尝试获取头节点的同步锁，再进行操作
  * 别的需要注意的点
    - size()和mappingCount()方法的异同，两者计算是否准确
    - 多线程环境下如何进行扩容
  * 三者的区别
    * HashMap线程不安全，数组+链表+红黑树
    * Hashtable线程安全，锁住整个对象，数组+链表
    * ConcurrentHashMap线程安全，CAS+同步锁，数组+链表+红黑树
    * HashMap的key、value均可为null，而其他的两个类不支持
* J.U.C知识点梳理
  * java.util.concurrent：提供了并发编程的解决方案
    - CAS是java.util.concurrent.atomic包的基础
    - AQS是java.util.concurrent.locks包以及一些常用类，比如Semophore、ReentrantLock等类的基础
  * J.U.C包的分类
    - 线程执行器executor、锁locks、原子变量类atomic、并发工具类tools、并发合集collections
* ![image-20190810174026698](/Users/dingyuanjie/Documents/study/github/woodyprogram/img/image-20190810174026698.png)
* 并发工具类
  * 闭锁CountDownLatch：
    * 让主线程等待一组事件发生后继续执行，事件指的是CountDownLatch里的countDown()
    * ![image-20190814230506543](/Users/dingyuanjie/Documents/study/github/woodyprogram/img/image-20190814230506543.png)
    * ![image-20190810174620932](/Users/dingyuanjie/Documents/study/github/woodyprogram/img/image-20190810174620932.png)
  * 栅栏CyclicBarrier：阻塞当前线程，等待其他线程
    * 等待其他线程，且会阻塞自己当前线程，所有线程必须同时到达栅栏位置后，才能继续执行
    * 所有线程到达栅栏处，可以触发执行另外一个预先设置的线程
    * ![image-20190814230945784](/Users/dingyuanjie/Documents/study/github/woodyprogram/img/image-20190814230945784.png)
    * ![image-20190810174811678](/Users/dingyuanjie/Documents/study/github/woodyprogram/img/image-20190810174811678.png)
  * 信号量Semaphor：控制某个资源可被同时访问的线程个数
    * ![image-20190814231458130](/Users/dingyuanjie/Documents/study/github/woodyprogram/img/image-20190814231458130.png)
    * ![image-20190810174902311](/Users/dingyuanjie/Documents/study/github/woodyprogram/img/image-20190810174902311.png)
  * 交换器Exchanger：两个线程到达同步点后，相互交换数据
    * ![image-20190814231620414](/Users/dingyuanjie/Documents/study/github/woodyprogram/img/image-20190814231620414.png)
    * ![image-20190810175105722](/Users/dingyuanjie/Documents/study/github/woodyprogram/img/image-20190810175105722.png)
* BlockingQueue：提供可阻塞的入队和出队操作
  * ![image-20190814232229962](/Users/dingyuanjie/Documents/study/github/woodyprogram/img/image-20190814232229962.png)
  * 主要用于生产者-消费者模式，在多线程场景时生产者线程在队列尾部添加元素，而消费者线程则在队列头部消费元素，通过这种方式能够达到将任务的额生产和消费进行隔离的目的
  * ![image-20190810175601872](/Users/dingyuanjie/Documents/study/github/woodyprogram/img/image-20190810175601872.png)
* ![image-20190810175931244](/Users/dingyuanjie/Documents/study/github/woodyprogram/img/image-20190810175931244.png)
* ![image-20190810180025226](/Users/dingyuanjie/Documents/study/github/woodyprogram/img/image-20190810180025226.png)
* NIO的核心
  - Channel
    - FileChannel
      - transferTo：把FileChannel中的数据拷贝到另外一个Channel
      - transferFrom：把另外一个Channel中的数据拷贝到FileChannel
      - 避免了两次用户态和内核态间的上下文切换，即“零拷贝”，效率较高
    - DatagramChannel、SocketChannel、ServerSocketChannel
  - Buffer
  - Selector
* ![image-20190810180536626](/Users/dingyuanjie/Documents/study/github/woodyprogram/img/image-20190810180536626.png)
* ![image-20190810180626671](/Users/dingyuanjie/Documents/study/github/woodyprogram/img/image-20190810180626671.png)
* ![image-20190810180714296](/Users/dingyuanjie/Documents/study/github/woodyprogram/img/image-20190810180714296.png)
* ![image-20190810180732485](/Users/dingyuanjie/Documents/study/github/woodyprogram/img/image-20190810180732485.png)
* ![image-20190810180751244](/Users/dingyuanjie/Documents/study/github/woodyprogram/img/image-20190810180751244.png)
* AIO如何进一步加工处理结果
  - 基于回调：实现CompletionHandler接口，调用时触发回调函数
  - 返回Future：通过isDone()查看是否准备好，通过get()等待返回数据
* ![image-20190810181410837](/Users/dingyuanjie/Documents/study/github/woodyprogram/img/image-20190810181410837.png)

# Spring

- Spring IOC
  - 控制反转
  - Spring Core最核心的部分
  - 需先了解依赖注入
- 依赖注入
  - 把底层类作为参数传递给上层类，实现上层对下层的“控制”
- 依赖注入的方式
  - Setter、Interface、Constructor、Annotation
- 依赖倒置原则、IOC、DI、IOC容器的关系
  - ![image-20190815095800038](/Users/dingyuanjie/Documents/study/github/woodyprogram/img/image-20190815095800038.png)
- IOC容器的优势
  - 避免在各处使用new来创建类，并且可以做到统一维护
  - 创建实例的时候不需要了解其中的细节
  - ![image-20190810195535801](/Users/dingyuanjie/Documents/study/github/woodyprogram/img/image-20190810195535801.png)
- Spring IOC支持的功能
  - 依赖注入、依赖检查、自动装配、支持集合、指定初始化方法和销毁方法、支持回调方法
- Spring IOC容器的核心接口
  - BeanFactory
  - ApplicationContext
- BeanDefinition
  - 主要用来描述Bean的定义
- BeanDefinitionRegistry
  - 提供向IOC容器注册BeanDefinition对象的方法
- BeanFactory
  - 提供IOC的配置机制
  - 包含Bean的各种定义，便于实例化Bean
  - 建立Bean之间的依赖关系
  - Bean的生命周期控制
- BeanFactory与ApplicationContext的比较
  - BeanFactory是Spring框架的基础设施，面向Spring
  - ApplicationContext面向使用Spring框架的开发者
- ApplicationContext的功能（继承多个接口）
  - BeanFactory：能够管理、装配Bean
  - ResourcePatternResolver：能够加载资源文件
  - MessageSource：能够实现国际化等功能
  - ApplicationEventPublisher：能够注册监听器，实现监听机制
- getBean方法的代码逻辑
  - 转换BeanName、从缓存中加载实例、实例化Bean、检测parentBeanFactory、初始化依赖的Bean、创建Bean

- SpringBean的作用域
  - singleton：Spring的默认作用域，容器里拥有唯一的Bean实例
  - prototype：针对每个getBean请求，容器都会创建一个Bean实例
  - request：会为每个Http请求创建一个Bean实例
  - session：会为每个session创建一个Bean实例
  - globalSession：会为每个全局Http Session创建一个Bean实例，该作用域仅对Portlet有效

- SpringBean的生命周期

  - 创建过程
    - 实例化Bean 
    - Aware（注入Bean ID、 BeanFactory和ApplicationContext） 
    - BeanPostProcessor(s)，postProcessBeforeInitialization
    - InitializingBean(s)，afterPropertiesSet
    - 定制的Bean init方法
    - BeanPostProcessor(s)，postProcessAfterInitialization
    - Bean初始化完毕
  - 销毁过程
    - 若实现了DisposableBean接口，则会调用destory方法
    - 若配置了destry-method属性，则会调用其配置的销毁方法

- Sprint AOP

  - 关注点分离：不同的问题交给不同的部分去解决
    - 面向切面编程AOP正是此种技术的体现
    - 通用化功能代码的实现，对应的就是所谓的切面（Aspect）
    - 业务功能代码和切面代码分开后，架构将变得高内聚低耦合
    - 确保功能的完整性：切面最终需要被合并到业务中（Weave）
  - 三种织入方式
    - 编译时织入：需要特殊的Java编译器，如AspectJ
    - 类加载时织入：需要特殊的Java编译器，如AspectJ和AspectWerkz
    - 运行时织入：Spring采用的方式，通过动态代理的方式，实现简单
  - ![image-20190810203223341](/Users/dingyuanjie/Documents/study/github/woodyprogram/img/image-20190810203223341.png)
  - ![image-20190810203240946](/Users/dingyuanjie/Documents/study/github/woodyprogram/img/image-20190810203240946.png)
  - 主要名词概念
    - Aspect：通用功能的代码体现
    - Target：被织入Aspect的对象
    - Join Point：可以作为切入点的机会，所有方法都可以作为切入点
    - Pointcut：Aspect实际被应用在的Join Point，支持正则
    - Advice：类里的方法以及这个方法如何织入到目标方法的方式
    - Weaving：Aop的实现过程

  - Advice的种类
    - 前置通知-Before、后置通知-AfterReturning、异常通知-AfterThrowing、最终通知-After、环绕通知-Around
  - AOP的实现：JdkProxy和Cglib
    - 由AopProxyFactory根据AdvisedSupport对象的配置来决定
    - 默认策略如果目标类是接口，则用JDKProxy来实现，否则用后者
    - JDKProxy的核心：InvocationHandler接口和Proxy类
    - Cglib：以继承的方式动态生成目标类的代理
    - JDKProxy：通过Java的内部反射机制实现
    - Cglib：借助ASM实现
    - 反射机制在生成类的过程中比较高效
    - ASM在生成类之后的执行过程中比较高效

- Spring里的代理模式的实现

  - 真实实现类的逻辑包含在了getBean方法里
  - getBean方法返回的实际上是Proxy的实例
  - Proxy实例是Spring采用JDK Proxy或CGLIB动态生成的

- Spring事务相关

  - ACID
  - 隔离级别
  - 事务传播