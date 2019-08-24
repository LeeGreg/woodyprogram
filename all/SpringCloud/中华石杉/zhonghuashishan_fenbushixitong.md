# 分布式系统

##  为什么要使用分布式系统

* 分布式业务系统，把原来用java开发的一个大块系统，给拆分成多个子系统，多个子系统之间互相调用，形成一个大系统的整体
* 拆分系统之后，每个人就负责自己的一小部分就好了，可以随便玩儿随便弄。分布式系统拆分之后，可以大幅度提升复杂系统大型团队的开发效率
  * 自己维护自己的服务，保证可用
* dubbo说白了，是一种rpc框架，就是本地就是进行接口调用，但是dubbo会代理这个调用请求，跟远程机器网络通信，给你处理掉负载均衡了、服务实例上下线自动感知了、超时重试了，等等乱七八糟的问题。那就不用自己做了，用dubbo就可以了

## Dubbo

- dubbo的工作原理？注册中心挂了可以继续通信吗？

  - dubbo工作原理
    - 第一层：service层，接口层，给服务提供者和消费者来实现的
    - 第二层：config层，配置层，主要是对dubbo进行各种配置的
    - 第三层：proxy层，服务代理层，透明生成客户端的stub和服务单的skeleton
    - 第四层：registry层，服务注册层，负责服务的注册与发现
    - 第五层：cluster层，集群层，封装多个服务提供者的路由以及负载均衡，将多个实例组合成一个服
    - 第六层：monitor层，监控层，对rpc接口的调用次数和调用时间进行监控
    - 第七层：protocol层，远程调用层，封装rpc调用
    - 第八层：exchange层，信息交换层，封装请求响应模式，同步转异步
    - 第九层：transport层，网络传输层，抽象mina和netty为统一接口
    - 第十层：serialize层，数据序列化层
  - 工作流程
    - 第一步，provider向注册中心去注册
    - 第二步，consumer从注册中心订阅服务，注册中心会通知consumer注册好的服务
    - 第三步，consumer调用provider
    - 第四步，consumer和provider都异步的通知监控中心
  - 注册中心挂了可以继续通信吗？
    - 可以，因为刚开始初始化的时候，消费者会将提供者的地址等信息拉取到本地缓存，所以注册中心挂了可以继续通信

  ![01_dubbo的工作原理](/Users/dingyuanjie/Documents/study/github/woodyprogram/img/01_dubbo的工作原理.png)

- dubbo支持哪些序列化协议？说一下hessian的数据结构？PB知道吗？为什么PB的效率是最高的？

  - dubbo协议
    - dubbo://192.168.0.1:20188
    - 默认就是走dubbo协议的，单一长连接，NIO异步通信，基于hessian作为序列化协议
    - 适用的场景就是：传输数据量很小（每次请求在100kb以内），但是并发量很高
    - 为了要支持高并发场景，一般是服务提供者就几台机器，但是服务消费者有上百台，可能每天调用量达到上亿次！此时用长连接是最合适的，就是跟每个服务消费者维持一个长连接就可以，可能总共就100个连接。然后后面直接基于长连接NIO异步通信，可以支撑高并发请求
    - 否则如果上亿次请求每次都是短连接的话，服务提供者会扛不住
    - 而且因为走的是单一长连接，所以传输数据量太大的话，会导致并发能力降低。所以一般建议是传输数据量很小，支撑高并发访问
  - rmi协议
    - 走java二进制序列化，多个短连接，适合消费者和提供者数量差不多，适用于文件的传输，一般较少用
  - hessian协议
    - 走hessian序列化协议，多个短连接，适用于提供者数量比消费者数量还多，适用于文件的传输，一般较少用
  - http协议
    - 走json序列化
  - webservice
    - 走SOAP文本序列化
  - dubbo支持的序列化协议
    - 所以dubbo实际基于不同的通信协议，支持hessian、java二进制序列化、json、SOAP文本序列化多种序列化协议。但是hessian是其默认的序列化协议

- dubbo负载均衡策略和高可用策略都有哪些？动态代理策略呢？

  - dubbo负载均衡策略
    - random loadbalance
      - 默认情况下，dubbo是random load balance随机调用实现负载均衡，可以对provider不同实例设置不同的权重，会按照权重来负载均衡，权重越大分配流量越高，一般就用这个默认的就可以了
    - roundrobin loadbalance
      - 还有roundrobin loadbalance，这个的话默认就是均匀地将流量打到各个机器上去，但是如果各个机器的性能不一样，容易导致性能差的机器负载过高。所以此时需要调整权重，让性能差的机器承载权重小一些，流量少一些
    - leastactive loadbalance
      - 这个就是自动感知一下，如果某个机器性能越差，那么接收的请求越少，越不活跃，此时就会给不活跃的性能差的机器更少的请求
    - consistanthash loadbalance
      - 一致性Hash算法，相同参数的请求一定分发到一个provider上去，provider挂掉的时候，会基于虚拟节点均匀分配剩余的流量，抖动不会太大。如果你需要的不是随机负载均衡，是要一类请求都到一个节点，那就走这个一致性hash策略
  - dubbo集群容错策略
    - failover cluster模式
      - 失败自动切换，自动重试其他机器，默认就是这个，常见于读操作
    - failfast cluster模式
      - 一次调用失败就立即失败，常见于写操作
    - failsafe cluster模式
      - 出现异常时忽略掉，常用于不重要的接口调用，比如记录日志
    - failbackc cluster模式
      - 失败了后台自动记录请求，然后定时重发，比较适合于写消息队列这种
    - forking cluster
      - 并行调用多个provider，只要一个成功就立即返回
    - broadcacst cluster
      - 逐个调用所有的provider
  - dubbo动态代理策略
    - 默认使用javassist动态字节码生成，创建代理类
    - 但是可以通过spi扩展机制配置自己的动态代理策略

- dubbo的spi思想是什么？

  - service provider interface

  - 比如有个接口，现在这个接口有3个实现类，那么在系统运行的时候对这个接口到底选择哪个实现类呢？这就需要spi了，需要根据指定的配置或者是默认的配置，去找到对应的实现类加载进来，然后用这个实现类的实例对象

  - 比如说要通过jar包的方式给某个接口提供实现，然后就在自己jar包的META-INF/services/目录下放一个跟接口同名的文件，里面指定接口的实现里是自己这个jar包里的某个类。ok了，别人用了一个接口，然后用了你的jar包，就会在运行的时候通过你的jar包的那个文件找到这个接口该用哪个实现类

  - 这是jdk提供的一个功能

  - 比如说你有个工程A，有个接口A，接口A在工程A里是没有实现类的 -> 系统在运行的时候，怎么给接口A选择一个实现类呢？

    - 就可以自己搞一个jar包，META-INF/services/，放上一个文件，文件名就是接口名，接口A，接口A的实现类=com.zhss.service.实现类A2。让工程A来依赖你的这个jar包，然后呢在系统运行的时候，工程A跑起来，对接口A，就会扫描自己依赖的所有的jar包，在每个jar里找找，有没有META-INF/services文件夹，如果有，在里面找找，有没有接口A这个名字的文件，如果有在里面找一下你指定的接口A的实现是你的jar包里的哪个类？

  - SPI机制，一般来说用在哪儿？插件扩展的场景，比如说你开发的是一个给别人使用的开源框架，如果你想让别人自己写个插件，插到你的开源框架里面来，扩展某个功能

  - 经典的思想体现，大家平时都在用，比如说jdbc

  - java定义了一套jdbc的接口，但是java是没有提供jdbc的实现类

  - 但是实际上项目跑的时候，要使用jdbc接口的哪些实现类呢？一般来说，我们要根据自己使用的数据库，比如msyql，你就将mysql-jdbc-connector.jar，引入进来；oracle，你就将oracle-jdbc-connector.jar，引入进来

  - 在系统跑的时候，碰到你使用jdbc的接口，他会在底层使用你引入的那个jar中提供的实现类

  - 但是dubbo也用了spi思想，不过没有用jdk的spi机制，是自己实现的一套spi机制

  - `Protocol protocol = ExtensionLoader.getExtensionLoader(Protocol.class).getAdaptiveExtension();`

  - Protocol接口，dubbo要判断一下，在系统运行的时候，应该选用这个Protocol接口的哪个实现类来实例化对象来使用呢？

  - 他会去找一个你配置的Protocol，他就会将你配置的Protocol实现类，加载到jvm中来，然后实例化对象，就用你的那个Protocol实现类就可以了

  - 微内核，可插拔，大量的组件，Protocol负责rpc调用的东西，你可以实现自己的rpc调用组件，实现Protocol接口，给自己的一个实现类即可

  - 这行代码就是dubbo里大量使用的，就是对很多组件，都是保留一个接口和多个实现，然后在系统运行的时候动态根据配置去找到对应的实现类。如果你没配置，那就走默认的实现好了，没问题

    ```java
    @SPI("dubbo")  
    public interface Protocol {  
        int getDefaultPort();  
        @Adaptive  
        <T> Exporter<T> export(Invoker<T> invoker) throws RpcException;  
        @Adaptive  
        <T> Invoker<T> refer(Class<T> type, URL url) throws RpcException;  
        void destroy();  
    } 
    ```

  * 在dubbo自己的jar里，在/META_INF/dubbo/internal/com.alibaba.dubbo.rpc.Protocol文件中：

  * dubbo=com.alibaba.dubbo.rpc.protocol.dubbo.DubboProtocol

    http=com.alibaba.dubbo.rpc.protocol.http.HttpProtocol

    hessian=com.alibaba.dubbo.rpc.protocol.hessian.HessianProtocol

  * 所以说，这就看到了dubbo的spi机制默认是怎么玩儿的了，其实就是Protocol接口，@SPI(“dubbo”)说的是，通过SPI机制来提供实现类，实现类是通过dubbo作为默认key去配置文件里找到的，配置文件名称与接口全限定名一样的，通过dubbo作为key可以找到默认的实现了就是com.alibaba.dubbo.rpc.protocol.dubbo.DubboProtocol

  * dubbo的默认网络通信协议，就是dubbo协议，用的DubboProtocol

  * 如果想要动态替换掉默认的实现类，需要使用@Adaptive接口，Protocol接口中，有两个方法加了@Adaptive注解，就是说那俩接口会被代理实现

    * 比如这个Protocol接口搞了俩@Adaptive注解标注了方法，在运行的时候会针对Protocol生成代理类，这个代理类的那俩方法里面会有代理代码，代理代码会在运行的时候动态根据url中的protocol来获取那个key，默认是dubbo，你也可以自己指定，你如果指定了别的key，那么就会获取别的实现类的实例了
    * 通过这个url中的参数不通，就可以控制动态使用不同的组件实现类

  * 怎么来自己扩展dubbo中的组件

    * 自己写个工程，要是那种可以打成jar包的，里面的src/main/resources目录下，搞一个META-INF/services，里面放个文件叫：com.alibaba.dubbo.rpc.Protocol，文件里搞一个my=com.zhss.MyProtocol。自己把jar弄到nexus私服里去
    * 然后自己搞一个dubbo provider工程，在这个工程里面依赖你自己搞的那个jar，然后在spring配置文件里给个配置：`<dubbo:protocol name=”my” port=”20000” />`
    * 这个时候provider启动的时候，就会加载到我们jar包里的my=com.zhss.MyProtocol这行配置里，接着会根据你的配置使用你定义好的MyProtocol了，这个就是简单说明一下，你通过上述方式，可以替换掉大量的dubbo内部的组件，就是扔个你自己的jar包，然后配置一下即可
    * dubbo里面提供了大量的类似上面的扩展点，就是说，你如果要扩展一个东西，只要自己写个jar，让你的consumer或者是provider工程，依赖你的那个jar，在你的jar里指定目录下配置好接口名称对应的文件，里面通过key=实现类
    * 然后对对应的组件，用类似`<dubbo:protocol>`用你的哪个key对应的实现类来实现某个接口，你可以自己去扩展dubbo的各种功能，提供你自己的实现

- 如何基于dubbo进行服务治理、服务降级、失败重试以及超时重试？

  - 服务治理
    1. 调用链路自动生成
       * 需要基于dubbo做的分布式系统中，对各个服务之间的调用自动记录下来，然后自动将各个服务之间的依赖关系和调用链路生成出来，做成一张图，显示出来，大家才可以看到
    2. 服务访问压力以及时长统计
       * 需要自动统计各个接口和服务之间的调用次数以及访问延时，而且要分成两个级别。一个级别是接口粒度，就是每个服务的每个接口每天被调用多少次，TP50，TP90，TP99，三个档次的请求延时分别是多少；第二个级别是从源头入口开始，一个完整的请求链路经过几十个服务之后，完成一次请求，每天全链路走多少次，全链路请求延时的TP50，TP90，TP99，分别是多少
       * 这些东西都搞定了之后，后面才可以来看当前系统的压力主要在哪里，如何来扩容和优化啊
    3. 其他的
       * 服务分层（避免循环依赖），调用链路失败监控和报警，服务鉴权，每个服务的可用性的监控（接口调用成功率？几个9？）99.99%，99.9%，99%

  * 服务降级
    * 比如说服务A调用服务B，结果服务B挂掉了，服务A重试几次调用服务B，还是不行，直接降级，走一个备用的逻辑，给用户返回响应
    * 将mock修改为true，然后在跟接口同一个路径下实现一个Mock类，命名规则是接口名称加Mock后缀。然后在Mock类里实现自己的降级逻辑
  * 失败重试和超时重试
    * `<dubbo:reference id="xxxx" interface="xx" check="true" async="false" retries="3" timeout="2000"/>`
    * 如果是超时了，timeout就会设置超时时间；如果是调用失败了自动就会重试指定的次数

- 分布式服务接口的幂等性如何设计（比如不能重复扣款）？

  - 这个不是技术问题，这个没有通用的一个方法，这个是结合业务来看应该如何保证幂等性的，你的经验
  - 所谓幂等性，就是说一个接口，多次发起同一个请求，你这个接口得保证结果是准确的，比如不能多扣款，不能多插入一条数据，不能将统计值多加了1。这就是幂等性
  - 保证幂等性主要是三点：
    - 对于每个请求必须有一个唯一的标识
      - 举个例子：订单支付请求，肯定得包含订单id，一个订单id最多支付一次，对吧
    - 每次处理完请求之后，必须有一个记录标识这个请求处理过了
      - 比如说常见的方案是在mysql中记录个状态啥的，比如支付之前记录一条这个订单的支付流水，而且支付流水采
    - 每次接收请求需要进行判断之前是否处理过的逻辑处理
      - 比如说，如果有一个订单已经支付了，就已经有了一条支付流水，那么如果重复发送这个请求，则此时先插入支付流水，orderId已经存在了，唯一键约束生效，报错插入不进去的。然后你就不用再扣款了
    - 实际运作过程中，你要结合自己的业务来，比如说用redis用orderId作为唯一键。只有成功插入这个支付流水，才可以执行实际的支付扣款
      - 要求是支付一个订单，必须插入一条支付流水，order_id建一个唯一键，unique key
      - 所以你在支付一个订单之前，先插入一条支付流水，order_id就已经进去了
      - 你就可以写一个标识到redis里面去，set order_id payed，下一次重复请求过来了，先查redis的order_id对应的value，如果是payed就说明已经支付过了，你就别重复支付了
      - 然后呢，你再重复支付这个订单的时候，你写尝试插入一条支付流水，数据库给你报错了，说unique key冲突了，整个事务回滚就可以了
      - 来保存一个是否处理过的标识也可以，服务的不同实例可以一起操作redis

- 分布式服务接口请求的顺序性如何保证？

  - 首先，一般来说，个人的建议是，你们从业务逻辑上最好设计的这个系统不需要这种顺序性的保证，因为一旦引入顺序性保障，会导致系统复杂度上升，而且会带来效率低下，热点数据压力过大，等问题
  - 用过的方案吧，简单来说，首先你得用dubbo的一致性hash负载均衡策略，将比如某一个订单id对应的请求都给分发到某个机器上去，接着就是在那个机器上因为可能还是多线程并发执行的，你可能得立即将某个订单id对应的请求扔一个内存队列里去，强制排队，这样来确保他们的顺序性
  - 但是这样引发的后续问题就很多，比如说要是某个订单对应的请求特别多，造成某台机器成热点怎么办？解决这些问题又要开启后续一连串的复杂技术方案。。。曾经这类问题弄的我们头疼不已，所以，还是建议什么呢？
  - 最好是比如说刚才那种，一个订单的插入和删除操作，能不能合并成一个操作，就是一个删除，或者是什么，避免这种问题的产生

- 如何自己设计一个类似dubbo的rpc框架？

  - 最简单的回答思路：
  - 上来你的服务就得去注册中心注册吧，你是不是得有个注册中心，保留各个服务的信心，可以用zookeeper来做
  - 然后你的消费者需要去注册中心拿对应的服务信息吧，对吧，而且每个服务可能会存在于多台机器上
  - 接着你就该发起一次请求了，咋发起？蒙圈了是吧。当然是基于动态代理了，你面向接口获取到一个动态代理，这个动态代理就是接口在本地的一个代理，然后这个代理会找到服务对应的机器地址
  - 然后找哪个机器发送请求？那肯定得有个负载均衡算法了，比如最简单的可以随机轮询是不是
  - 接着找到一台机器，就可以跟他发送请求了，第一个问题咋发送？你可以说用netty了，nio方式；第二个问题发送啥格式数据？你可以说用hessian序列化协议了，或者是别的，对吧。然后请求过去了。。
  - 服务器那边一样的，需要针对你自己的服务生成一个动态代理，监听某个网络端口了，然后代理你本地的服务代码。接收到请求的时候，就调用对应的服务代码

## 分布式锁

- 使用redis如何设计分布式锁？

- 使用zk来设计分布式锁可以吗？这两种分布式锁的

  - zk的使用场景

    - 分布式协调：这个其实是zk很经典的一个用法，简单来说，就好比，你A系统发送个请求到mq，然后B消息消费之后处理了。那A系统如何知道B系统的处理结果？用zk就可以实现分布式系统之间的协调工作。A系统发送请求之后可以在zk上对某个节点的值注册个监听器，一旦B系统处理完了就修改zk那个节点的值，A立马就可以收到通知，完美解决
    - 分布式锁：对某一个数据连续发出两个修改操作，两台机器同时收到了请求，但是只能一台机器先执行另外一个机器再执行。那么此时就可以使用zk分布式锁，一个机器接收到了请求之后先获取zk上的一把分布式锁，就是可以去创建一个znode，接着执行操作；然后另外一个机器也尝试去创建那个znode，结果发现自己创建不了，因为被别人创建了。。。。那只能等着，等第一个机器执行完了自己再执行
    - 元数据/配置信息管理：zk可以用作很多系统的配置信息的管理，比如kafka、storm等等很多分布式系统都会选用zk来做一些元数据、配置信息的管理，包括dubbo注册中心不也支持zk么
    - HA高可用性：这个应该是很常见的，比如hadoop、hdfs、yarn等很多大数据系统，都选择基于zk来开发HA高可用机制，就是一个重要进程一般会做主备两个，主进程挂了立马通过zk感知到切换到备用进程

  - redis分布式锁

    - 官方叫做RedLock算法，是redis官方支持的分布式锁算法

      - 这个分布式锁有3个重要的考量点：互斥（只能有一个客户端获取锁），不能死锁，容错（大部分redis节点或者这个锁就可以加可以释放）

      - 第一个最普通的实现方式，如果就是在redis里创建一个key算加锁

      - SET my:lock 随机值 NX PX 30000，这个命令就ok，这个的NX的意思就是只有key不存在的时候才会设置成功，PX 30000的意思是30秒后锁自动释放。别人创建的时候如果发现已经有了就不能加锁了

      - 释放锁就是删除key，但是一般可以用lua脚本删除，判断value一样才删除

        ```lua
        if redis.call("get",KEYS[1]) == ARGV[1] then
        return redis.call("del",KEYS[1])
        else
            return 0
        end
        ```

      * 为啥要用随机值呢？因为如果某个客户端获取到了锁，但是阻塞了很长时间才执行完，此时可能已经自动释放锁了，此时可能别的客户端已经获取到了这个锁，要是你这个时候直接删除key的话会有问题，所以得用随机值加上面的lua脚本来释放锁
      * 但是这样是肯定不行的。因为如果是普通的redis单实例，那就是单点故障。或者是redis普通主从，那redis主从异步复制，如果主节点挂了，key还没同步到从节点，此时从节点切换为主节点，别人就会拿到锁

    - 第二个问题，RedLock算法

      - 这个场景是假设有一个redis cluster，有5个redis master实例。然后执行如下步骤获取一把锁
      - 1）获取当前时间戳，单位是毫秒
      - 2）跟上面类似，轮流尝试在每个master节点上创建锁，过期时间较短，一般就几十毫秒
      - 3）尝试在大多数节点上建立一个锁，比如5个节点就要求是3个节点（n / 2 +1）
      - 4）客户端计算建立好锁的时间，如果建立锁的时间小于超时时间，就算建立成功
      - 5）要是锁建立失败了，那么就依次删除这个锁
      - 6）只要别人建立了一把分布式锁，你就得不断轮询去尝试获取锁

    - zk分布式锁

      - zk分布式锁，其实可以做的比较简单，就是某个节点尝试创建临时znode，此时创建成功了就获取了这个锁；这个时候别的客户端来创建锁会失败，只能注册个监听器监听这个锁。释放锁就是删除这个znode，一旦释放掉就会通知客户端，然后有一个等待着的客户端就可以再次重新枷锁

        ```java
        /**
         * ZooKeeperSession
         * @author Administrator
         *
         */
        public class ZooKeeperSession {
        	private static CountDownLatch connectedSemaphore = new CountDownLatch(1);
        	private ZooKeeper zookeeper;
        	private CountDownLatch latch;
        
        	public ZooKeeperSession() {
        		try {
        			this.zookeeper = new ZooKeeper(
        					"192.168.31.187:2181,192.168.31.19:2181,192.168.31.227:2181", 
        					50000, 
        					new ZooKeeperWatcher());			
        			try {
        				connectedSemaphore.await();
        			} catch(InterruptedException e) {
        				e.printStackTrace();
        			}
        			System.out.println("ZooKeeper session established......");
        		} catch (Exception e) {
        			e.printStackTrace();
        		}
        	}
          /**
        	 * 获取分布式锁
        	 * @param productId
        	 */
        	public Boolean acquireDistributedLock(Long productId) {
        		String path = "/product-lock-" + productId;
        		try {
        			zookeeper.create(path, "".getBytes(), 
        					Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL);
        			return true;
        		} catch (Exception e) {
        			while(true) {
        				try {
        					Stat stat = zk.exists(path, true); // 相当于是给node注册一个监听器，去看看这个监听器是否存在
        					if(stat != null) {
        						this.latch = new CountDownLatch(1);
        						this.latch.await(waitTime, TimeUnit.MILLISECONDS);
        						this.latch = null;
        					}
        					zookeeper.create(path, "".getBytes(), 
        						Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL);
        					return true;
        				} catch(Exception e) {
        					continue;
        			}
        }
        // 很不优雅，我呢就是给大家来演示这么一个思路
        // 比较通用的，我们公司里我们自己封装的基于zookeeper的分布式锁，我们基于zookeeper的临时顺序节点去实现的，比较优雅的
        		}
        return true;
        	}
          
          /**
        	 * 释放掉一个分布式锁
        	 */
        	public void releaseDistributedLock(Long productId) {
        		String path = "/product-lock-" + productId;
        		try {
        			zookeeper.delete(path, -1); 
        			System.out.println("release the lock for product[id=" + productId + "]......");  
        		} catch (Exception e) {
        			e.printStackTrace();
        		}
        	}
          /**
        	 * 建立zk session的watcher
        	 */
        	private class ZooKeeperWatcher implements Watcher {
        		public void process(WatchedEvent event) {
        			System.out.println("Receive watched event: " + event.getState());
        
        			if(KeeperState.SyncConnected == event.getState()) {
        				connectedSemaphore.countDown();
        			} 
            if(this.latch != null) {  
              this.latch.countDown();  
            }
        		}
        	}
        	
        	/**
        	 * 封装单例的静态内部类
        	 */
        	private static class Singleton {
        		private static ZooKeeperSession instance;
        		static {
        			instance = new ZooKeeperSession();
        		}
        		public static ZooKeeperSession getInstance() {
        			return instance;
        		}	
        	}
        	
        	/**
        	 * 获取单例
        	 * @return
        	 */
        	public static ZooKeeperSession getInstance() {
        		return Singleton.getInstance();
        	}
        	
        	/**
        	 * 初始化单例的便捷方法
        	 */
        	public static void init() {
        		getInstance();
        	}
        }
        ```

- 实现方式哪种效率比较高？

  - redis分布式锁和zk分布式锁的对比

    - redis分布式锁，其实需要自己不断去尝试获取锁，比较消耗性能

    - zk分布式锁，获取不到锁，注册个监听器即可，不需要不断主动尝试获取锁，性能开销较小

    - 另外一点就是，如果是redis获取锁的那个客户端bug了或者挂了，那么只能等待超时时间之后才能释放锁；而zk的话，因为创建的是临时znode，只要客户端挂了，znode就没了，此时就自动释放锁

    - redis分布式锁大家每发现好麻烦吗？遍历上锁，计算时间等等。。。zk的分布式锁语义清晰实现简单

    - 所以先不分析太多的东西，就说这两点，我个人实践认为zk的分布式锁比redis的分布式锁牢靠、而且模型简单易用

      ```java
      public class ZooKeeperDistributedLock implements Watcher{
          private ZooKeeper zk;
          private String locksRoot= "/locks";
          private String productId;
          private String waitNode;
          private String lockNode;
          private CountDownLatch latch;
          private CountDownLatch connectedLatch = new CountDownLatch(1);
      private int sessionTimeout = 30000; 
      
          public ZooKeeperDistributedLock(String productId){
              this.productId = productId;
               try {
      	   String address = "192.168.31.187:2181,192.168.31.19:2181,192.168.31.227:2181";
                  zk = new ZooKeeper(address, sessionTimeout, this);
                  connectedLatch.await();
              } catch (IOException e) {
                  throw new LockException(e);
              } catch (KeeperException e) {
                  throw new LockException(e);
              } catch (InterruptedException e) {
                  throw new LockException(e);
              }
          }
      
          public void process(WatchedEvent event) {
              if(event.getState()==KeeperState.SyncConnected){
                  connectedLatch.countDown();
                  return;
              }
              if(this.latch != null) {  
                  this.latch.countDown(); 
              }
          }
      
          public void acquireDistributedLock() {   
              try {
                  if(this.tryLock()){
                      return;
                  }
                  else{
                      waitForLock(waitNode, sessionTimeout);
                  }
              } catch (KeeperException e) {
                  throw new LockException(e);
              } catch (InterruptedException e) {
                  throw new LockException(e);
              } 
      }
      
          public boolean tryLock() {
              try {
       		// 传入进去的locksRoot + “/” + productId
      		// 假设productId代表了一个商品id，比如说1
      		// locksRoot = locks
      		// /locks/10000000000，/locks/10000000001，/locks/10000000002
                  lockNode = zk.create(locksRoot + "/" + productId, new byte[0], ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.EPHEMERAL_SEQUENTIAL);
         
                  // 看看刚创建的节点是不是最小的节点
      	 	// locks：10000000000，10000000001，10000000002
                  List<String> locks = zk.getChildren(locksRoot, false);
                  Collections.sort(locks);
                  if(lockNode.equals(locksRoot+"/"+ locks.get(0))){
                      //如果是最小的节点,则表示取得锁
                      return true;
                  }
                  //如果不是最小的节点，找到比自己小1的节点
      	 				  int previousLockIndex = -1;
                  for(int i = 0; i < locks.size(); i++) {
      							if(lockNode.equals(locksRoot + “/” + locks.get(i))) {
      	         	    previousLockIndex = i - 1;
      		   					break;
      							}
      	  				 }
      	   				this.waitNode = locks.get(previousLockIndex);
              } catch (KeeperException e) {
                  throw new LockException(e);
              } catch (InterruptedException e) {
                  throw new LockException(e);
              }
              return false;
          }
           
          private boolean waitForLock(String waitNode, long waitTime) throws InterruptedException, KeeperException {
              Stat stat = zk.exists(locksRoot + "/" + waitNode, true);
              if(stat != null){
                  this.latch = new CountDownLatch(1);
                  this.latch.await(waitTime, TimeUnit.MILLISECONDS);            	   this.latch = null;
              }
              return true;
      }
        
          public void unlock() {
              try {
      		// 删除/locks/10000000000节点
      		// 删除/locks/10000000001节点
                  System.out.println("unlock " + lockNode);
                  zk.delete(lockNode,-1);
                  lockNode = null;
                  zk.close();
              } catch (InterruptedException e) {
                  e.printStackTrace();
              } catch (KeeperException e) {
                  e.printStackTrace();
              }
      }
      
          public class LockException extends RuntimeException {
              private static final long serialVersionUID = 1L;
              public LockException(String e){
                  super(e);
              }
              public LockException(Exception e){
                  super(e);
              }
      }
      // 如果有一把锁，被多个人给竞争，此时多个人会排队，第一个拿到锁的人会执行，然后释放锁，后面的每个人都会去监听排在自己前面的那个人创建的node上，一旦某个人释放了锁，排在自己后面的人就会被zookeeper给通知，一旦被通知了之后，就ok了，自己就获取到了锁，就可以执行代码了
      ```

      

## 分布式事务

- 分布式事务了解吗？你们如何解决分布式事务问题的？
  - 两阶段提交：询问和提交
    - 事务管理器负责协调多个数据库的事务，先询问每个数据库是否准备好，如果每个数据库都回复ok，那么就正式提交事务，如果有一个没回复ok则回滚事务
    - 适用场景：单块应用里，跨多个数据库的分布式事务，而且严重依赖于数据库层面来搞定复杂的事务，效率低，不适合高并发的场景
    - 基于Sping+JTA
    - 分布式系统规范和规定：要求每个服务只能操作自己对应的一个数据库，不允许直接连接别的服务的数据库，否则没法管理，如果需要操作其他服务的数据库，必须调用相应服务的接口来实现
  - TCC方案：Try、Confirm、Cancel
    - Try阶段：对各个服务的资源做检测以及对资源进行锁定或者预留
    - Confirm阶段：在各个服务中执行实际的操作
    - Cancel阶段：如果任何一个服务的业务方法执行出错，那么这里就需要进行补偿，就是执行已经执行成功的业务逻辑的回滚操作
    - 事务回滚实际上是严重依赖于自己写代码来回滚和补偿了，会造成补偿代码巨大，非常之繁琐，业务代码很难维护
    - 一般来说跟钱相关的，跟钱打交道的，支付、交易相关的场景，会用TCC，最好是各个业务执行的时间都比较短
  - 本地消息表
    - 国外的ebay
    - A系统在自己本地一个事务里操作同时，插入一条数据到消息表
    - 接着A系统将这个消息发送到MQ中去
    - B系统接收到消息之后，在一个事务里，往自己本地消息表里插入一条数据，同时执行其他的业务操作，如果这个消息已经被处理过了，那么此时这个事务会回滚，这样保证不会重复处理消息
    - B系统执行成功之后，就会更新自己本地消息表的状态以及A系统消息表的状态
    - 如果B系统处理失败了，那么就不会更新消息表状态，那么此时A系统会定时扫描自己的消息表，如果有没处理的消息，会再次发送到MQ中去，让B再次处理
    - 这个方案保证了最终一致性，哪怕B事务失败了，但是A会不断重发消息，直到B那边成功为止
    - 最大的问题就在于严重依赖于数据库的消息表来管理事务，高并发场景很难扩展
  - 可靠消息最终一致性方案
    - 不要用本地的消息表了，直接基于MQ来实现事务。比如阿里的RocketMQ就支持消息事务
    - A系统先发送一个prepared消息到mq，如果这个prepared消息发送失败那么就直接取消操作别执行了
    - 如果这个消息发送成功过了，那么接着执行本地事务，如果成功就告诉mq发送确认消息，如果失败就告诉mq回滚消息
    - 如果发送了确认消息，那么此时B系统会接收到确认消息，然后执行本地的事务
    - mq会自动定时轮询所有prepared消息回调你的接口，问你，这个消息是不是本地事务处理失败了，所有没发送确认消息？那是继续重试还是回滚？一般来说这里你就可以查下数据库看之前本地事务是否执行，如果回滚了，那么这里也回滚吧。这个就是避免可能本地事务执行成功了，别确认消息发送失败了
    - 这个方案里，要是系统B的事务失败了咋办？重试咯，自动不断重试直到成功，如果实在是不行，要么就是针对重要的资金类业务进行回滚，比如B系统本地回滚后，想办法通知系统A也回滚；或者是发送报警由人工来手工回滚和补偿
    - 目前国内互联网公司大都是这么玩儿的，要不举用RocketMQ支持的，要不就自己基于类似ActiveMQ？RabbitMQ？自己封装一套类似的逻辑出来，总之思路就是这样子的
    - ![06_可靠消息最终一致性方案](/Users/dingyuanjie/Documents/study/github/woodyprogram/img/06_可靠消息最终一致性方案.png)
  - 最大努力通知方案
    - 系统A本地事务执行完之后，发送个消息到MQ
      - 这里会有个专门消费MQ的最大努力通知服务，这个服务会消费MQ然后写入数据库中记录下来，或者是放入个内存队列也可以，接着调用系统B的接口
    - 要是系统B执行成功就ok了；要是系统B执行失败了，那么最大努力通知服务就定时尝试重新调用系统B，反复N次，最后还是不行就放弃
  - byton做法
    - 利用redis
  - 特别严格的场景，用的是TCC来保证强一致性；然后其他的一些场景基于了阿里的RocketMQ来实现了分布式事务
    - 严格资金要求绝对不能错的场景，可以说是用的TCC方案；如果是一般的分布式事务场景，订单插入之后要调用库存服务更新库存，库存数据没有资金那么的敏感，可以用可靠消息最终一致性方案
    - rocketmq 3.2.6之前的版本，是可以按照上面的思路来的，但是之后接口做了一些改变
  - 其实用任何一个分布式事务的这么一个方案，都会导致那块儿代码会复杂10倍。很多情况下，系统A调用系统B、系统C、系统D，可能根本就不做分布式事务。如果调用报错会打印异常日志
  - 每个月也就那么几个bug，很多bug是功能性的，体验性的，真的是涉及到数据层面的一些bug，一个月就几个，两三个？如果为了确保系统自动保证数据100%不能错，上了几十个分布式事务，代码太复杂；性能太差，系统吞吐量、性能大幅度下跌
  - 99%的分布式接口调用，不要做分布式事务，直接就是监控（发邮件、发短信）、记录日志（一旦出错，完整的日志）、事后快速的定位、排查和出解决方案、修复数据
  - 每个月，每隔几个月，都会对少量的因为代码bug，导致出错的数据，进行人工的修复数据，自己临时动手写个程序，可能要补一些数据，可能要删除一些数据，可能要修改一些字段的值
  - 比做50个分布式事务，成本要来的低上百倍，低几十倍
  - trade off，权衡，要用分布式事务的时候，一定是有成本，代码会很复杂，开发很长时间，性能和吞吐量下跌，系统更加复杂更加脆弱反而更加容易出bug；好处，如果做好了，TCC、可靠消息最终一致性方案，一定可以100%保证你那快数据不会出错
  - 1%，0.1%，0.01%的业务，资金、交易、订单，会用分布式事务方案来保证，会员积分、优惠券、商品信息，其实不要这么搞了
- TCC如果出现网络连不通怎么办？XA的一致性如何保证？

## 分布式会话

- 集群部署时的分布式session如何实现？

  - tomcat + redis

    - 使用session的代码跟以前一样，还是基于tomcat原生的session支持即可，然后就是用一个叫做Tomcat RedisSessionManager的东西，让所有我们部署的tomcat都将session数据存储到redis即可

    - 在tomcat的配置文件中，配置一下

      ```xml
      <Valve className="com.orangefunction.tomcat.redissessions.RedisSessionHandlerValve" />
      <Manager className="com.orangefunction.tomcat.redissessions.RedisSessionManager"
               host="{redis.host}"
               port="{redis.port}"
               database="{redis.dbnum}"
               maxInactiveInterval="60"/>
      
      搞一个类似上面的配置即可，你看是不是就是用了RedisSessionManager，然后指定了redis的host和 port就ok了。
      
      <Valve className="com.orangefunction.tomcat.redissessions.RedisSessionHandlerValve" />
      <Manager className="com.orangefunction.tomcat.redissessions.RedisSessionManager"
      	 sentinelMaster="mymaster"
      	 sentinels="<sentinel1-ip>:26379,<sentinel2-ip>:26379,<sentinel3-ip>:26379"
      	 maxInactiveInterval="60"/>
      
      还可以用上面这种方式基于redis哨兵支持的redis高可用集群来保存session数据，都是ok的
      ```

  - spring session + redis

    - 分布式会话的这个东西重耦合在tomcat中，如果我要将web容器迁移成jetty，难道你重新把jetty都配置一遍吗？

    - 因为上面那种tomcat + redis的方式好用，但是会严重依赖于web容器，不好将代码移植到其他web容器上去，尤其是你要是换了技术栈咋整？比如换成了spring cloud或者是spring boot之类的。还得好好思忖思忖

    - 所以现在比较好的还是基于java一站式解决方案，spring了。人家spring基本上包掉了大部分的我们需要使用的框架了，spirng cloud做微服务了，spring boot做脚手架了，所以用sping session是一个很好的选择

      - pom.xml

        ```xml
        <dependency>
          <groupId>org.springframework.session</groupId>
          <artifactId>spring-session-data-redis</artifactId>
          <version>1.2.1.RELEASE</version>
        </dependency>
        <dependency>
          <groupId>redis.clients</groupId>
          <artifactId>jedis</artifactId>
          <version>2.8.1</version>
        </dependency>
        ```

      * spring配置文件中

        ```xml
        <bean id="redisHttpSessionConfiguration"  class="org.springframework.session.data.redis.config.annotation.web.http.RedisHttpSessionConfiguration">
            <property name="maxInactiveIntervalInSeconds" value="600"/>
        </bean>
        
        <bean id="jedisPoolConfig" class="redis.clients.jedis.JedisPoolConfig">
            <property name="maxTotal" value="100" />
            <property name="maxIdle" value="10" />
        </bean>
        
        <bean id="jedisConnectionFactory"     class="org.springframework.data.redis.connection.jedis.JedisConnectionFactory" destroy-method="destroy">
            <property name="hostName" value="${redis_hostname}"/>
            <property name="port" value="${redis_port}"/>
            <property name="password" value="${redis_pwd}" />
            <property name="timeout" value="3000"/>
            <property name="usePool" value="true"/>
            <property name="poolConfig" ref="jedisPoolConfig"/>
        </bean>
        ```

      * web.xml

        ```xml
        <filter>
            <filter-name>springSessionRepositoryFilter</filter-name>
            <filter-class>org.springframework.web.filter.DelegatingFilterProxy</filter-class>
        </filter>
        <filter-mapping>
            <filter-name>springSessionRepositoryFilter</filter-name>
            <url-pattern>/*</url-pattern>
        </filter-mapping>
        ```

      * 示例代码

        ```java
        @Controller
        @RequestMapping("/test")
        public class TestController {
        	@RequestMapping("/putIntoSession")
        	@ResponseBody
          public String putIntoSession(HttpServletRequest request, String username){
            request.getSession().setAttribute("name",  “leo”);
        
            return "ok";
          }
        
        	@RequestMapping("/getFromSession")
        	@ResponseBody
          public String getFromSession(HttpServletRequest request, Model model){
            String name = request.getSession().getAttribute("name");
            return name;
          }
        }
        ```

      * 给sping session配置基于redis来存储session数据，然后配置了一个spring session的过滤器，这样的话，session相关操作都会交给spring session来管了。接着在代码中，就用原生的session操作，就是直接基于spring sesion从redis中获取数据了
      * 实现分布式的会话，有很多种很多种方式，我说的只不过比较常见的两种方式，tomcat + redis早期比较常用；近些年，重耦合到tomcat中去，通过spring session来实现