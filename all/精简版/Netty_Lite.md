# MS

* 默认情况下，Netty服务端会启动多个线程？何时启动？

  * 默认创建2倍CPU个数线程，调用execute方法时会判断当前线程是否在本线程， 如果是在本线程则说明线程已启动，如果是外部线程调用execute方法，那么首先会调用startThread方法，该方法会判断当前线程是否有启动，如果没有启动则启动这个线程

* Netty如何解决空轮询bug问题？

  * Netty通过计数的方式去判断， 如果当前阻塞的是一个select操作并没有花费很长时间，则有可能触发空轮训bug，默认情况达到512次，然后重建一个select，将原select上的key重新移交到新的select上，通过这种方式巧妙地避免了jdk空轮训Bug 

* Netty如何保证异步串行无锁化？

  * Netty在所有外部线程去调用inEventLoop或Channel方式时，通过inEventLoop方法来判断得出是外部线程，这种情况下把所有操作封装成一个Task丢到MpscQueue中，然后在NioEventLoop执行逻辑的第三过程这些task会被挨个执行

* Netty是在哪里检测有新连接接入的？

  * Boss线程的第一个过程，轮询出Accept事件，然后Boss线程的第二个过程，通过jdk底层的Channel的Accept方法去创建这条连接

* 新连接是怎样注册到NioEventLoop线程的？

  * Boss线程调用Chooser的next方法拿到一个NioEventLoop，然后将这条连接注册到NioEventLoop的select上去

* Netty新连接接入处理逻辑

  * 检测新连接 -> 创建NioSocketChannel -> 分配线程及注册selector -> 向selector注册读事件

* 服务端启动核心路径？

  * 创建Channel，初始化服务端Channel，保存用户自定义的属性，通过这些属性来创建连接接收器，每次接收新的连接后都会使用这些属性对新的连接进行配置，注册Selector，绑定端口

* netty是如何判断ChannelHandler类型的？

  * 添加ChannelHandler时使用instances of关键词来判断ChannelHandler类型，如果实现了ChannelInBoundHandler，那么通过设置一个boolean类型字段inBound来标识该Handler是inBoundHandler类型

* 对于ChannelHandler的添加应该遵循什么样的顺序？

  * inBound是被动的触发（Pipeline中添加的顺序和实际传播的顺序是相同的），outBind主动发起（Pipeline中添加的顺序和实际传播的顺序是相反的）

* 添加删除ChannelHandler

  * 判断ChannelHandler是否重复添加

    * 判断ChannelHandler是否是ChannelHandler实例
      * 如果是，则判断该ChannelHandlerAdapter（强转）没有标有iaSharable注解和added属性是否为true，则抛出重复创建异常，否则added属性设置为true
      * 如果不是则啥也不做

  * 创建节点并AbstractChannelHandlerContext添加至链表（head-tail之间）

  * 回调添加完成事件

  * 删除ChannelHandler

    * 找到节点、链表的删除、回调删除Handler事件

  * 异常和事件的传播

    * 异常按照ChannelHandler的添加顺序流传，与inBound和outBound无关

    * 异常处理的最佳实践-在Pipeline最后添加自定义异常处理器来处理异常

      ```java
      public class OutBoundHandlerC extends ChannelOutboundHandlerAdapter {
        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
          System.out.println("OutBoundHandlerC.exceptionCaught");
          ctx.fireExceptionCaught(cause);
        }
      }
      ```

* 如何把对象变成字节流，最终写到socket底层

  * 当bizHandler通过write方法将User对象向Pipeline的Head方向传播到encoder节点（继承MessageToByteEncoder，复写encode()把自定义User对象转为ByteBuf，然后继续调用write()将ByteBuf向Head传播，Head接收到ByteBuf后会调用unsafe方法将数据存入底层缓存区）,同理flush()，向Head传播，最终在Head接收flush事件后通过循环从缓冲区中取出ByteBuf转成jdk底层能够接收的对象并写出去，每写完一个就将当前缓冲区中节点删除
  * writeAndFlush()
    * 从tail节点开始往前传播、逐个调用channelHandler的write方法、逐个调用channelHandler的flush方法

* Netty 服务端启动的流程：创建一个引导类，然后指定线程模型，IO模型，连接读写处理逻辑，绑定端口之后，服务端就启动起来了，bind 方法是异步的，可通过这个异步机制来实现端口递增绑定

  * 服务端 Channel 或者客户端 Channel 设置属性值，设置底层 TCP 参数

* Netty 客户端启动的流程：创建一个引导类，然后指定线程模型，IO 模型，连接读写处理逻辑，连接上特定主机和端口，客户端就启动起来了， `connect` 是异步的，可通过异步回调机制来实现指数退避重连逻辑

  * 客户端 Channel 绑定自定义属性值，设置底层 TCP 参数

* Netty 对二进制数据的抽象 ByteBuf 的结构，本质上它的原理就是，它引用了一段内存，这段内存可以是堆内也可以是堆外的，然后用引用计数来控制这段内存是否需要被释放，使用读写指针来控制对 ByteBuf 的读写

  * 基于读写指针和容量、最大可扩容容量
  * 多个 ByteBuf 可引用同一段内存，通过引用计数来控制内存的释放，遵循谁 retain() 谁 release() 的原则

* 连接服务端：`telnet 127.0.0.1 8888`

* 
  序列化和编码都是把 Java 对象封装成二进制数据的过程，这两者有什么区别和联系？

  * 序列化是关于移动结构化数据在存储/传输介质上的结构可以保持的方式
  * 编码更广泛，数据如何转换为不同的形式

* 传统RPC调用性能差的三宗罪
  * 网络传输方式
    * 传统的 RPC 框架或者基于 RMI 等方式的远程服务(过程)调用采用了同步阻塞 IO
      * 当并发访问量增加后，服务端的线程个数和并发访问数成线性正比
      * 同步阻塞IO会由于频繁的wait导致IO线程经常性的阻塞
  * 序列化方式问题
    * Java 序列化机制
      * 是 Java 内部的一种对象编解码技术，无法跨语言使用
      * Java 序列化后的码流太大，无论是网络传输还是持久化到磁盘，都会导致额外的资源占用;
      * 序列化性能差(CPU 资源占用高)
  * 线程模型问题
    * 由于采用同步阻塞 IO，这会导致每个 TCP 连接都占用 1 个线程，当 IO 读写阻塞导致线程无法及时释放时，会导致系统性能急剧下降
  
* 高性能的三个主题
  * 传输，用什么样的通道将数据发送给对方，BIO、NIO 或AIO
  * 协议，相比于公有协议，内部私有协议的性能通常可以被设计的更优
  * 线程，数据报如何读取？读取之后的编解码在哪个线程进行，编解码后的消息如何派发？

# Netty简介

* 异步事件驱动的网络应用程序框架，用于快速开发高性能服务端和客户端
  
  * 封装了JDK底层BIO和NIO模型，提供高度可用的API
  * 自带编解码器解决拆包粘包问题，用户只用关心业务逻辑
  * 精心设计的reactor线程模型支持高并发海量连接
  * 自带各种协议栈可处理任何一种通用协议
  * 异步非阻塞IO，发起IO请求的线程不等IO操作完成，就继续执行随后的代码，IO结果用其他方式通知发起IO请求的程序
  * 同步阻塞IO，发起IO请求的线程被阻塞直至IO操作完成后返回
  
* 作为一个异步非阻塞框架，Netty 的所有 IO 操作都是异步非阻塞的，通过 FutureListener机制，用户可以方便的主动获取或者通过通知机制获得 IO 操作结果

* Netty高性能

  * 异步非阻塞通信

    * Netty 的 IO 线程 NioEventLoop 由于聚合了多路复用器 Selector，可以同时并发处理成百上千个客户端 Channel，由于读写操作都是非阻塞的，可以充分提升 IO 线程的运行效率，避免由于频繁 IO 阻塞导致的线程挂起
      * 通过把多个 IO 的阻塞复用到同一个 select 的阻塞上，从而使得系统在单线程的情况下可以同时处理多个客户端请求

    ![Netty服务端通信序列图](/Users/dingyuanjie/Desktop/notes/%E4%B9%A6%E5%8D%95/Netty%E6%9C%8D%E5%8A%A1%E7%AB%AF%E9%80%9A%E4%BF%A1%E5%BA%8F%E5%88%97%E5%9B%BE.png)

    ![Netty客户端通信序列图-9180101](/Users/dingyuanjie/Desktop/notes/%E4%B9%A6%E5%8D%95/Netty%E5%AE%A2%E6%88%B7%E7%AB%AF%E9%80%9A%E4%BF%A1%E5%BA%8F%E5%88%97%E5%9B%BE-9180101.png)

  * 零拷贝

    * 可快速高效地将数据从文件系统移动到网络接口，而不需要将其从内核空间复制到用户空间
    * Netty 的接收和发送 ByteBuffer 采用 DIRECT BUFFERS，使用堆外直接内存进行 Socket读写，不需要进行字节缓冲区的二次拷贝
    * 如果使用传统的堆内存(HEAP BUFFERS)进行Socket 读写，JVM 会将堆内存 Buffer 拷贝一份到直接内存中，然后才写入 Socket 中。相比于堆外直接内存，消息在发送过程中多了一次缓冲区的内存拷贝 
    * Netty 提供了组合 Buffer 对象，可以聚合多个 ByteBuffer 对象，用户可以像操作一个Buffer 那样方便的对组合 Buffer 进行操作，避免了传统通过内存拷贝的方式将几个小Buffer 合并成一个大Buffer
    * Netty 的文件传输采用了 transferTo 方法，它可以直接将文件缓冲区的数据发送到目标Channel，避免了传统通过循环 write 方式导致的内存拷贝问题

  * 内存池

    * 基于内存池的缓冲区重用机制
    * 采用内存池的 ByteBuf 相比于朝生夕灭的 ByteBuf，性能高很多
    * Netty 提供了多种内存管理策略，通过在启动辅助类中配置相关参数，可以实现差异化的定制

  * 高效的Reactor线程模型

    * Reactor单线程模型，指的是所有的 IO 操作都在同一个 NIO 线程上面完成
      * 作为 NIO 服务端，接收客户端的 TCP 连接、作为 NIO 客户端，向服务端发起 TCP 连接、读取通信对端的请求或者应答消息、向通信对端发送消息请求或者应答消息
      * 由于 Reactor 模式使用的是异步非阻塞 IO，所有的 IO 操作都不会导致阻塞，理论上一个线程可以独立处理所有 IO 相关的操作，但是一个 NIO 线程同时处理成百上千的链路，性能上无法支撑
    *  Reactor 多线程模型，一个 专门的NIO 线程-Acceptor 线程用于监听服务端，接收客户端的 TCP 连接请求，网络IO操作-读、写等由一个NIO线程池负责
      * 1 个 NIO 线程可以同时处理 N 条链路，但是 1 个链路只对应 1 个 NIO 线程，防止发生并发操作问题
      * 一个 NIO 线程负责监听和处理所有的客户端连接可能会存在性能问题
    * 主从 Reactor 线程模型，Acceptor 线程池仅仅只用于客户端的登陆、握手和安全认证，一旦链路建立成功，就将链路注册到后端 subReactor线程池的 IO 线程上，由 IO 线程负责后续的 IO 操作（读写和编解码）
    * Netty 的线程模型并非固定不变，通过在启动辅助类中创建不同的 EventLoopGroup实例并通过适当的参数配置，就可以支持上述三种 Reactor 线程模型

  * 无锁化的串行设计理念

    * 并行多线程如果对共享资源的并发访问处理不当，会带来严重的锁竞争，这最终会导致性能的下降
    * 通过串行无锁化设计，消息的处理尽可能在同一个线程内完成，期间不进行线程切换，这样就避免了多线程竞争和同步锁
    * 通过调整 NIO 线程池的线程参数，可以同时启动多个串行化的线程并行运行，这种局部无锁化的串行线程设计相比一个队列-多个工作线程模型性能更优
    * Netty的NioEventLoop读取到消息后，直接调用ChannelPipeline的fireChannelRead(Object msg)，只要用户不主动切换线程，一直会由NioEventLoop调用到用户的Handler，期间不进行线程切换，这种串行化处理方式避免了多线程操作导致的锁的竞争，从性能角度来看是最优的

  * 高效的并发编程

    * `volatile`的大量、正确使用
    * `CAS` 和原子类的广泛使用、线程安全容器的使用、通过读写锁提升并发性能

  * 高性能的序列化框架

    * 影响序列化性能的关键因素总结如下：
      * 序列化后的码流大小(网络带宽的占用)
      * 序列化&反序列化的性能(CPU 资源占用)
      * 是否支持跨语言(异构系统的对接和开发语言切换)
    * Netty 默认提供了对 Google Protobuf 的支持，Protobuf 序列化后的码流只有 Java 序列化的 1/4 左右，通过扩展 Netty 的编解码接口，用户可以实现其它的高性能序列化框架，例如 Thrift 的压缩二进制编解码框架

  * 灵活的TCP参数配置能力

    * Netty 在启动辅助类中可以灵活的配置 TCP 参数，满足不同的用户场景
    * `SO_RCVBUF` 和 `SO_SNDBUF`：通常建议值为 128K 或者 256K
    * `SO_TCPNODELAY`：`NAGLE` 算法通过将缓冲区内的小封包自动相连，组成较大的封包，阻止大量小封包的发送阻塞网络，从而提高网络应用效率。但是对于时延敏感的应用场景需要关闭该优化算法
    * `SO_BACKLOG`：服务端处理客户端连接请求是顺序处理的，所以同一时间只能处理一个客户端连接，多个客户端来的时候，服务端将不能处理的客户端连接请求放在队列中等待处理，backlog参数指定了队列的大小
    * `SO_KEEPALIVE`：如果在两小时内没有数据的通信时，TCP会自动发送一个活动探测数据报文

## 特性

* 核⼼部分，是底层的网络通⽤抽象和部分实现
  * 可拓展的事件模型
  * 通用的通信 API 层，统一的 API，支持阻塞和非阻塞传输
    * 封装了JDK底层BIO和NIO模型，提供高度可用的API
  * ⽀持零拷贝特性的 Byte Buffer 实现

* 传输服务，具体的网络传输的定义与实现
  * TCP 和 UDP、HTTP、JVM内部的传输实现
* 自带协议栈
  * Netty 对于一些通⽤协议的编解码实现。例如：HTTP协议、WebSocket协议、自定义协议
    * 自带编码解码器解决拆包粘包问题，用户只用关心业务逻辑

* 精心设计的Reactor线程模型支持高并发海量连接
* 链接逻辑组件以支持复用

## 与NIO的对比

* Netty在NIO基础上做了很多改进
  * 更加优雅的Reactor模式实现、灵活的线程模型、利用EventLoop等创新性的机制，可以非常高效地管理成百上千的Channel
  * 充分利用了Java的零拷贝机制，降低内存分配和回收的开销
    * 例如使用池化的Direct Bufer等技术，在提高IO性能的同时，减少了对象的创建和销毁
    * 利用反射等技术直接操纵SelectionKey，使用数组而不是Java容器等
  * 在通信协议、序列化等其他角度的优化

## Reactor设计模式

* 将并发服务请求提交到一个或多个服务处理程序的事件设计模式
  * 当请求抵达后，服务处理程序使用解多路分配策略，然后同步地派发这些请求至相关的请求处理程序
* 优点
  * 可完全分离程序特定代码，应用可分为模块化、可复用的组件
  * 由于请求的处理程序是同步调用，可允许简单粗粒并发而不必添加多线程并发系统的复杂性
* 限制
  * 请求处理器只会被同步调用，限制最大并发数
  * 可扩展性不仅受限于请求处理器的同步调用，同时也受解多路器限制
* 虽然池化和重用线程可以避免频繁创建和销毁线程，但不能消除线程上下文切换的开销

## 设计模式

* 单例模式：一个类全局只有一个对象

* 策略模式：封装一系列可相互替换的算法家族、动态选择某一个策略

* 装饰者模式：装饰者和被装饰者继承同一个接口、装饰者给被装饰者动态修改行为

* 观察者模式：观察者订阅消息，被观察者发布消息

  ```java
  // channelFuture 被观察者
  ChannelFuture channelFuture = channel.writeAndFlush(object);
  // 添加观察者
  channelFuture.addListener(future -> {
    
  });
  ```

* 迭代器模式：对容器里面各个对象进行访问
* 责任链模式：将一些对象连接成一条链，消息在这条链之间传递，链中对象能够处理自己关心的消息
  
  * 继承ChannelInboundHandlerAdapter，如果不重写channelRead，默认会继续往下传播，而fireChannelRead是显示传播

## 调优

* 默认在Netty的主进程中运行，如果处理业务逻辑阻塞堵住了主线程，那么就会阻塞Reactive所有管理的连接，一条线程可能影响所管理的所有的连接
* 优化方式一：
  * 将业务逻辑放到单独的业务线程池中去运行——继承ServerBusinessHandler，重写channelRead0方法，调整业务线程池的线程数量
* 优化方式二：
  * 业务逻辑Handler在 单独的NioEventLoopGroup中运行
  * `EventLoopGroup businessGroup = new NioEventLoopGroup(1000);`
  * `ch.pipeline().addLast(businessGroup, ServerBusinessHandler.INSTANCE);`

# Netty 组件

* 处理逻辑
  * 服务端NioEventLoop监听端口
  * 客户端与服务端建立新连接Channel
  * 服务端接收客户端发送的数据ByteBuf
  * 服务端进行业务逻辑处理ChannelPipeline-ChannelHandler
  * 服务端发送数据ByteBuf给客户端
* 基本组件
  * NioEventLoop
  * Channel、Pipeline
  * ChannelHandler、ByteBuf

![image-20190420115756469](/Users/dingyuanjie/Library/Application Support/typora-user-images/image-20190420115756469.png)

## 协议

* 服务端与客户端的通信协议
  * 无论是使用 Netty 还是原始的 Socket 编程，基于 TCP 通信的数据包格式均为二进制
  * 协议指的就是客户端与服务端事先商量好的，每一个二进制数据包中每一段字节分别代表什么含义的规则
  * 客户端（对象 -> 二进制）—— 服务端（二进制 -> 对象）

  ![image-20190623065639942](/Users/dingyuanjie/Documents/study/github/woodyprogram/img/image-20190623065639942.png)

  * 第一个字段是魔数，通常为固定的几个字节，服务端首先取出前面四个字节（魔数）进行比对，能够在第一时间识别出这个数据包是否遵循自定义协议，为了安全考虑可以直接关闭连接以节省资源。在 Java 的字节码的二进制文件中，开头的 4 个字节为`0xcafebabe` 用来标识这是个字节码文件，亦是异曲同工之妙
  * 版本号，通常情况下是预留字段，用于协议升级时
  * 序列化算法表示如何把 Java 对象转换二进制数据以及二进制数据如何转换回 Java 对象，比如 Java 自带的序列化，json，hessian 等序列化方式
  * 指令，服务端或者客户端每收到一种指令都会有相应的处理逻辑
  * 数据部分的长度，占四个字节
  * 数据内容，每一种指令对应的数据是不一样的

* 把 Java 对象根据协议封装成二进制数据包的过程成为编码，而把从二进制数据包中解析出 Java 对象的过程成为解码

## EventLoop

* 定义了 Netty 的核心抽象，用于处理连接的生命周期中所发生的事件
  
  * 运行任务来处理在连接的生命周期内发生的事件是任何网络框架的基本功能，与之相应的编程上的构造通常被称为事件循环——EventLoop，其中每个任务都是一个 Runnable 的实例，在事件循环中执行任务
* Netty 之所以能提供高性能网络通讯，其中一个原因是因为它使用 Reactor 线程模型
* 在 Netty 中每个EventLoopGroup 本身是一个线程池，其中包含了自定义个数的 NioEventLoop，每个NioEventLoop 中会管理自己的一个 selector 选择器和监控选择器就绪事件的线程
  * 客户端
    * 持有一个 EventLoopGroup 用来处理网络 IO操作
    * 当 Channel 是客户端通道 NioSocketChannel 时候，会注册到自己关联的 NioEventLoop 的 selector 选择器上，然后NioEventLoop 对应的线程会通过 select 命令监控感兴趣的网络读写事件
  * 服务端
    * 有两个 EventLoopGroup，其中 boss组是专门用来接收客户端发来的 TCP 链接请求的，worker组是专门用来具体处理完成三次握手的链接套接字的网络IO 请求
    * 当Channel是服务端通道NioServerSocketChannel 时候，NioServerSocketChannel本身会被注册到 boss EventLoopGroup 里面的某一个NioEventLoop 管理的 selector 选择器上，而完成三次握手的链接套接字是被注册到了 worker EventLoopGroup 里面的某一个 NioEventLoop 管理的 selector 选择器上
  * 多个 Channel 可以注册到同一个 NioEventLoop管理的 selector 选择器上，这时 NioEventLoop 对应的单个线程就可以处理多个 Channel 的就绪事件；但是每个Channel 只能注册到一个固定的 NioEventLoop 管理的selector 选择器上
  * 根据配置和可用核心的不同，可能会创建多个 EventLoop 实例用以优化资源的使用
* 选择器selector
  * 充当一个注册表，在那里将可以请求在 Channel 的状态发生变化时得到通知。可能的状态变化有
    * 新的 Channel 已被接受并且就绪、Channel 连接已经完成、Channel 有已经就绪的可供读取的数据、Channel 可用于写数据
  * 其运行在一个检查状态变化并对其做出相应响应的线程上，在应用程序对状态的改变做出响应之后，选择器将会被重置，并将重复这个过程
    * OP_ACCEPT，请求在接受新连接并创建Channel时获得通知
    * OP_CONNECT，请求在建立一个连接时获得通知
    * OP_READ，请求当数据已经就绪，可以从Channel中读取时获得通知
    * OP_WRITE，请求当可以向Channel中写更多的数据时获得通知
  * 选择并处理状态的变化
    * 新的Channel注册到选择器上
    * 选择器的select()将会阻塞直到接收到新的状态变化或者配置的超时时间已过时
      * 检查是否有状态变化
        * 没有则在选择器运行的同一线程中执行其他任务
        * 有则处理所有的状态变化

* 当⼀个连接到达时，Netty 就会创建⼀个 Channel，然后从 EventLoopGroup 中分配一个 EventLoop来与该Channel绑定用以处理所有事件：注册感兴趣的事件、将事件派发给 ChannelHandler、安排进一步的动作

  * 在该 Channel 的整个⽣命周期中都是由这个绑定的 EventLoop 来服务的（避免线程安全和同步问题）

* 一个 EventLoopGroup 包含一个或多个 EventLoop ；

* EventLoop只能与⼀个 Thread 绑定并且由其处理的 I/O操作和事件都将在它专有的 Thread 上被处理，从而保证线程安全

  * 任务（Runnable或Callable）可以立即执行或调度执行提交给EventLoop

    ```java
    //JDK任务调度，在高负载下它将带来性能上的负担
    ScheduledExecutorService executor=Executors.newScheduledThreadPool(10);
    ScheduledFuture<?> future = executor.schedule( new Runnable() {
        @Override
        public void run() {
            System.out.println("60 seconds later"); 
        }
    }, 60, TimeUnit.SECONDS);
        ...
    executor.shutdown(); // 一旦调度任务执行完成，就关闭 ScheduledExecutorService 以释放资源
    ```

    ```java
    //使用 EventLoop 调度周期性的任务
    Channel ch = ...
    ScheduledFuture<?> future = ch.eventLoop().scheduleAtFixedRate(
    	new Runnable() {
            @Override
            public void run() {
    					System.out.println("Run every 60 seconds"); 
            }
    }, 60, 60, TimeUnit.Seconds);
    
    //使用 ScheduledFuture 取消任务
    boolean mayInterruptIfRunning = false; 
    future.cancel(mayInterruptIfRunning);
    ```

  * 事件和任务是以先进先出(FIFO)的顺序执行的，保证字节内容总是按正确的顺序被处理

* ⼀个 EventLoop 可被分配至⼀个或多个 Channel，对于所有相关联的 Channel 来说，ThreadLocal 都将是一样的

* 一个 Channel 在它的生命周期内只能注册到一个 EventLoop 上（实际上消除了对于同步的需要）

* Netty线程模型的卓越性能取决于对于当前执行的Thread是否是分配给当前Channel以及它的EventLoop的那一个线程 

  * 把任务传递给EventLoop的execute方法后，执行检查以确定当前调用线程是否就是分配给EventLoop的那个线程。`Channel.eventLoop().execute(Task)`
    * 如果是，则在EventLoop中直接执行任务，否则将任务放入队列以便EventLoop下次处理它的事件时执行
    * 这也就解释了任何的 Thread 是与 Channel 直接交互而无需在 ChannelHandler 中进行额外同步
    * 每个EventLoop都有它自己的任务队列，独立于任何其他的EventLoop
  * 不要将一个需要长时间运行的任务放入执行队列，因为其将阻塞在同一线程上执行的其他任务。如有需要，建议使用一个专门的EventExecutor
  * 如同传输所采用的不同的事件处理实现一样，所使用的线程模型也可以强烈地影响到排队的任务对整体系统性能的影响

* 非阻塞传输的EventLoop分配方式，如NIO和AIO、

* 阻塞传输，每一个 Channel 都将被分配给一个 EventLoop(以及它的 Thread)

## Channel

* 是对` socket` 的装饰或者门面，其封装了对`socket` 的原子操作
  
  * 传入或者传出数据的载体，可以被打开或者被关闭，连接或者断开连接
* channel的生命周期
  * `ChannelRegistered` -> `ChannelActive` -> `ChannelInactive` -> `ChannelUnregistered`
  * 当这些状态发生变化时，将会生成对应的事件，这些事件将会被转发给`ChannelPipeline`中的`ChannelHandler`，其可以随后对它们作出响应
* 客户端
  * NIO 套接字通道是 `NioSocketChannel`
    * 用来创建`SocketChannel` 实例和设置该实例的属性，并调用`Connect` 方法向服务端发起 TCP 链接等
* 服务端
  * NIO 套接字通道是 `NioServerSocketChannel`
    * 用来创建`ServerSocketChannel` 实例和设置该实例属性，并调用该实例的 `bind` 方法在指定端口监听客户端的链接
* 每个` Channel` 都将会被分配一个 `ChannelPipeline` 和 `ChannelConfig`
* 由于 Channel 是独一无二的，所以为了保证顺序将 Channel 声明为 `java.lang.Comparable` 的一个子接口。因此，如果两个不同的 Channel 实例都返回了相同的散列码，那么 AbstractChannel 中的 compareTo()方法的实现将会抛出一个 Error
* Netty的Channel实现是线程安全的
  * Channel只注册到一个EventLoop，而EventLoop只和一个线程绑定
  * 因此可存储一个到Channel的引用，并且每当需要向远程节点写数据时，都可使用它，即使当时许多线程都在使用它，消息将会保证按顺序发送
* Channel的注册过程
  
  * 即将 Channel 与对应的 EventLoop 关联，然后调用底层的 Java NIO SocketChannel 的 register 方法, 将底层的 Java NIOSocketChannel 注册到指定的 selector 中
  
* Future在操作完成时通知应用程序

  * JDK的Future只允许手动检查对应的操作是否已经完成，或者一直阻塞直到它完成

  * Netty的ChannelFuture异步通知，用于在执行异步操作的时候使用

    * 每个 Netty 的出站 I/O 操作都将返回一个 ChannelFuture

      * 如channel.connect(...)

    * 其addListener()方法注册了一个ChannelFutureListener，以
      便在某个操作完成时调用(无论是否成功)得到通知——监听器的回调方法operationComplete()

    * 线程不用阻塞以等待对应的操作完成，消除了手动检查对应的操作是否完成的必要

    * 所有属于同一个 Channel 的操作将按顺序执行

      ```java
      //回调
      Channel channel = ...;
      //异步地连接 到远程节点
      ChannelFuture future = channel.connect( new InetSocketAddress("192.168.0.1", 25));
      //ChannelFuture future = bootstrap.connect(new InetSocketAddress("www.manning.com",80)); 
      //ChannelFuture future = serverBootstrap.bind(new InetSocketAddress(8080));
      //注册一个 ChannelFutureListener 以便在操作完成时获得通知
      future.addListener(new ChannelFutureListener() {
       //检查操作的状态
       @Override
       public void operationComplete(ChannelFuture future) {
           // 如果操作是成功的，则创建 一个 ByteBuf 以持有数据
           if (future.isSuccess()){
               ByteBuf buffer = Unpooled.copiedBuffer("Hello",Charset.defaultCharset());
               // 将数据异步地发送到远程节点。 返回一个 ChannelFuture
               ChannelFuture wf = future.channel().writeAndFlush(buffer);
           } else {
               // 如果发生错误，则访问描述原因 的 Throwable
               Throwable cause = future.cause();
               cause.printStackTrace();
           }
       }
      );
      ```

## ChannelPipeline

* 每个 Channel 都拥有一个与之相关联的 ChannelPipeline，ChannelPipeline持有一个 ChannelHandler 的实例链并且会维护一个ChannelHandlerContext的双向链表
* 由 I/O 操作触发的事件将流经安装了一个或多个ChannelHandler 的 ChannelPipeline，传播这些事件的方法调用可以随后被 ChannelHandler 所拦截，并且可以按需地处理事件。
* 根据事件的起源，事件将会被 ChannelInboundHandler 或者 ChannelOutboundHandler处理。随后，通过调用 ChannelHandlerContext 实现，它将被转发给同一超类型的下一个 ChannelHandler（Netty 能区分且确保数据只会在具有相同定向类型的两个 ChannelHandler 之间传递）
  * 针对不同类型的事件来调用 ChannelHandler
  * 应用程序通过实现或扩展ChannelHandler来挂钩到事件的生命周期，并提供自定义的应用程序逻辑
  * 在 ChannelPipeline 传播事件时，它会测试 ChannelPipeline 中的下一个 ChannelHandler 的类型是否和事件的运动方向相匹配。直到它找到和该事件所期望的方向相匹配的为止
* 添加的自定义ChannelHandler会插入到head和tail之间，如果是ChannelInboundHandler的回调，根据插入的顺序从左向右进行链式调用，ChannelOutboundHandler则相反
* 通常 ChannelPipeline 中的每一个 ChannelHandler 都是通过它的 EventLoop(I/O 线程)来处理传递给它的事件的。所以至关重要的是不要阻塞这个线程，因为这会对整体的 I/O 处理产生负面的影响。
* ChannelPipeline 提供了 ChannelHandler 链的容器，并定义了用于在该链上传播入站和出站事件流的 API
  * 一个ChannelInitializer的实现被注册到了ServerBootstrap中
  * 当 ChannelInitializer.initChannel()方法被调用时，ChannelInitializer将在 ChannelPipeline 中安装一组自定义的 ChannelHandler，执行顺序是由它们被添加的顺序所决定的 
  * ChannelInitializer 将它自己从 ChannelPipeline 中移除
* 如果一个消息或者任何其他的入站事件被读取，那么它会从 ChannelPipeline 的头部开始流动，并被传递给第一个 ChannelInboundHandler。这个 ChannelHandler 不一定会实际地修改数据，具体取决于它的具体功能，在这之后，数据将会被传递给链中的下一个ChannelInboundHandler。最终，数据将会到达 ChannelPipeline 的尾端，届时，所有处理就都结束了
* 数据的出站运动(即正在被写的数据)在概念上也是一样的。在这种情况下，数据将从ChannelOutboundHandler 链的尾端开始流动，直到它到达链的头部为止。在这之后，出站数据将会到达网络传输层
* 触发事件
  * ChannelPipeline的入站操作
    * `fireChannelRead`调用 ChannelPipeline 中下一个 ChannelInboundHandler 的
      channelRead(ChannelHandlerContext, Object msg)方法
    * `fire...`调用下一个ChannelHandler的相关方法
  * ChannelPipeline的出站操作
    * `bind` `connect` `disconnect` `close` `deregister` `flush` `write`  `writeAndFlush ` `read`
* 在Netty中，有两种发送消息的方式
  * 直接写到Channel中，将会导致消息从 ChannelPipeline 的尾端开始流动
  * 写到和ChannelHandler 相关联的 ChannelHandlerContext 对象中，将导致消息从 ChannelPipeline 中的下一个 ChannelHandler 开始流动

## ChannelHandlerContext

* 每当有ChannelHandler添加到ChannelPipeline中时，都会创建对应的ChannelHandlerContext，其代表了 ChannelHandler 和 ChannelPipeline 之间的绑定。虽然这个对象可以被用于获取底层的Channel，但是它主要还是被用于写出站数据

  * ChannelPipeline实际维护的是ChannelHandlerContext 的关系，主要功能是管理它所关联的 ChannelHandler 和在同一个 ChannelPipeline 中的其他 ChannelHandler 之间的交互
  * 每个ChannelHandlerContext之间形成双向链表
  * 通过使用作为参数传递到每个方法的 ChannelHandlerContext，事件可以被传递给当前ChannelHandler 链中的下一个 ChannelHandler

* 具有丰富的用于处理事件和执行 I/O 操作的 API

  * `alloc`返回和这个实例相关联的 Channel 所配置的 ByteBufAllocator
  * `channel`返回绑定到这个实例的 Channel
  * `handler`返回绑定到这个实例的 ChannelHandler
  * `read ` 将数据从Channel读取到第一个入站缓冲区;如果读取成功则触发一个channelRead事件，并(在最后一个消息被读取完成后) 通知 ChannelInboundHandler 的 channelReadComplete (ChannelHandlerContext)方法 
  * `write`通过这个实例写入消息并经过 ChannelPipeline
  * `writeAndFlush`通过这个实例写入并冲刷消息并经过 ChannelPipeline

* 缓存到 ChannelHandlerContext 的引用以供稍后使用

  ```java
  public class WriteHandler extends ChannelHandlerAdapter {
     private ChannelHandlerContext ctx;
     @Override
     public void handlerAdded(ChannelHandlerContext ctx) {
         this.ctx = ctx;
     }
     public void send(String msg) {
         ctx.writeAndFlush(msg);
  	} 
  }
  ```

* HeadContext
  * 实现了ChannelOutboundHandler，ChannelInboundHandler这两个接口
  * 因为在头部，所以说HeadContext中关于in和out的回调方法都会触发关于ChannelInboundHandler
  * HeadContext的作用是进行一些前置操作，以及把事件传递到下一个ChannelHandlerContext
  * 在把这个事件传递给下一个ChannelHandler之前会回调ChannelHandler的handlerAdded方法而有关ChannelOutboundHandler接口的实现，会在链路的最后执行
  * 通过Channel接口执行write之后，会执行ChannelOutboundHandler链式调用，在链尾的HeadContext ，在通过unsafe回到对应Channel做相关调用
* TailContext
  * 实现了ChannelInboundHandler接口，会在ChannelInboundHandler调用链最后执行，只要是对调用链完成处理的情况进行处理
  * 自定义的最后一个ChannelInboundHandler，也把处理操作交给下一个ChannelHandler，那么就会到
    TailContext

## ChannelHandler

* 作用

  * 将数据从一种格式转换为另一种格式（通过将 ChannelHandler 添加到 ChannelPipeline 中来实现动态的协议切换）、提供异常的通知、提供 Channel 变为活动的或者非活动的通知、提供当 Channel 注册到 EventLoop 或者从 EventLoop 注销时的通知、提供有关用户自定义事件的通知

* 在架构上，`ChannelHandler` 有助于保持业务逻辑与网络处理代码的分离，处理往来 ChannelPipeline 事件(包括数据)的任何代码的通用容器，ChannelHandler链路会根据Handler的类型，分为InBound和OutBound两条链路 

* 在`ChannelHandler`被添加到`ChannelPipeline` 中或者被从`ChannelPipeline`中移除时会调用这些操作。这些生命周期方法中的每一个都接受一个`ChannelHandlerContext` 参数

  * `handlerAdded` `handlerRemoved` `exceptionCaught`

* ChannelHandler子接口

  * `ChannelInboundHandler`——处理入站数据以及各种状态变化

    * 服务器会响应传入的消息，所以它需要实现 ChannelInboundHandler 接口，用来接收入站事件和数据，要给连接的客户端发送响应时，也可以从ChannelInboundHandler冲刷数据
    * 在数据被接收时或者与其对应的 Channel 状态发生改变时被调用

    ![image-20181214225622121](/Users/dingyuanjie/Desktop/MyKnowledge/2.code/java/2.%E5%92%95%E6%B3%A1%E5%AD%A6%E9%99%A2/04.%E5%A4%9A%E7%BA%BF%E7%A8%8B%E9%AB%98%E5%B9%B6%E5%8F%91/02.Netty/image-20181214225622121-4799382.png)

    * 每个方法都带了ChannelHandlerContext作为参数，具体作用是：在每个回调事件里面，处理完成之后，使用ChannelHandlerContext的fireChannelXXX方法来传递给下个ChannelHandler，netty的code模块和业务处理代码分离就用到了这个链路处理
    * `channelRead`当从 Channel 读取数据时被调用，当某个 ChannelInboundHandler 的实现重写 channelRead()方法时，它将负责显式地释放与池化的 ByteBuf 实例相关的内存`ReferenceCountUtil.release(msg);`丢弃已接收的消息
      * 作为一个面向流的协议，TCP 保证了字节数组将会按照服务器发送它们的顺序被接收
      * 由服务器发送的消息可能会被分块接收，即使是对于这么少量的数据，`channelRead0()`方法也可能会被调用两次
      * 当消息被`ctx.write(msg);`、`ctx.flush(); `后会自动释放
    * 消息被`SimpleChannelInboundHandler`的 `channelRead0()`方法消费之后自动释放消息

  * `ChannelOutboundHandler`——处理出站数据并且允许拦截所有的操作

    * 它的方法将被`Channel`、`ChannelPipeline` 以及 `ChannelHandlerContext` 调用

    * 一个强大的功能是可以按需推迟操作或者事件

      * 这使得可以通过一些复杂的方法来处理请求。例如，如果到远程节点的写入被暂停了，那么可以推迟冲刷操作并在稍后继续

      ![image-20181214225758377](/Users/dingyuanjie/Desktop/MyKnowledge/2.code/java/2.%E5%92%95%E6%B3%A1%E5%AD%A6%E9%99%A2/04.%E5%A4%9A%E7%BA%BF%E7%A8%8B%E9%AB%98%E5%B9%B6%E5%8F%91/02.Netty/image-20181214225758377-4799478.png)

      * `		read` 当请求从`Channel` 读取更多的数据时被调用

      - `write`当请求通过`Channel` 将数据写到远程节点时被调用
      - `flush`当请求通过`Channel` 将入队数据冲刷到远程节点时被调用 
      - `ChannelOutboundHandler`中的大部分方法都需要一个`ChannelPromise`参数，以便在操作完成时得到通知
        - 可调用它的`addListener`注册监听，当回调方法所对应的操作完成后，会触发这个监听
        - `ChannelPromise`是`ChannelFuture`的一个子类，其定义了一些可写的方法，如`setSuccess()`和`setFailure()`，从而使`ChannelFuture`不可变

  * `ChannelInboundHandler`的`channelRead`回调负责执行入栈数据的`decode`逻辑，`ChannelOutboundHandler`的`write`负责执行出站数据的`encode`工作

* `ChannelHandler` 适配器

  * 有一些适配器类可以将编写自定义的 ChannelHandler 所需要的努力降到最低限度，因为它们提
    供了定义在对应接口中的所有方法的默认实现

  * 因为有时会忽略那些不感兴趣的事件，所以 Netty提供了抽象基类 ChannelInboundHandlerAdapter 和 ChannelOutboundHandlerAdapter。通过调用 ChannelHandlerContext 上的对应方法，每个都提供了简单地将事件传递给下一个 ChannelHandler的方法的实现。随后，可以通过重写所感兴趣的那些方法来扩展这些类

    * 编写自定义`ChannelHandler`时经常用到的适配器类
      * `ChannelHandlerAdapter`、`ChannelInboundHandlerAdapter`、`ChannelOutboundHandlerAdapter`、`ChannelDuplexHandler`

  * 只需要简单地扩展它们，并且重写那些想要自定义的方法

    ![image-20181029215620920](/Users/dingyuanjie/Desktop/MyKnowledge/2.code/java/2.%E5%92%95%E6%B3%A1%E5%AD%A6%E9%99%A2/04.%E5%A4%9A%E7%BA%BF%E7%A8%8B%E9%AB%98%E5%B9%B6%E5%8F%91/02.Netty/image-20181029215620920.png)

  * ChannelHandler，接口族的父接口，它的实现负责接收并响应事件通知，充当了所有处理入站和出站数据的应用程序逻辑的容器，其方法是由网络事件触发的

    * 有许多不同类型的 ChannelHandler，它们各自的功能主要取决于它们的超类
    * Netty 以适配器类的形式提供大量默认的 ChannelHandler 实现来简化应用程序处理逻辑的开发过程

  * 在`ChannelInboundHandlerAdapter` 和` ChannelOutboundHandlerAdapter` 中所提供的方法体调用了其相关联的`ChannelHandlerContext` 上的等效方法，从而将事件转发到了`ChannelPipeline` 中的下一个 `ChannelHandler` 中

    * 通过`ChannelHandlerContext`将事件从一个`ChannelHandler`转发到`ChannelPipeline中`的下一个`ChannelHandler`

  * `ChannelInboundHandlerAdapter `实现了`ChannelInboundHandler`的所有方法，作用就是处理消息并将消息转发到`ChannelPipeline` 中的下一个 `ChannelHandler`

    * `ChannelInboundHandlerAdapter` 的 `channelRead` 方法处理完消息后不会自动释放消息，若想自动释放收到的消息，可以使用 `SimpleChannelInboundHandler`

* `ChannelInitializer` 用来初始化`ChannelHandler`，将自定义的各种`ChannelHandler`添加到`ChannelPipeline`中

  * 在 Netty 中，从网络读取的 Inbound 消息，需要经过解码，将二进制的数据报转换成应用层协议消息或者业务消息，才能够被上层的应用逻辑识别和处理；同理，用户发送到网络的 Outbound 业务消息，需要经过编码转换成二进制字节数组(对于 Netty 就是 ByteBuf)才能够发送到网络对端。
  * 编码和解码功能是 NIO 框架的有机组成部分，无论是由业务定制扩展实现，还是 NIO 框架内置编解码能力，该功能是必不可少的

* `ChannelHandlerAdapter` 还提供了实用方法` isSharable()`。如果其对应的实现被标注为 `Sharable`，那么这个方法将返回 true，表示它可以被添加到多个`ChannelPipeline`中

  * `@Sharable`
    * 为何要共享同一个`ChannelHandler` ?用于收集跨越多个 `Channel` 的统计信息
    * 只应该在确定了`ChannelHandler` 是线程安全的时才使用`@Sharable` 注解

* `Handler`的添加过程

  * 基于`Pipeline`的自定义`handler`机制，可以像添加插件一样自由组合各种各样的 handler 来完成业务逻辑
    * 例如需要处理 HTTP 数据，那么就可以在 pipeline 前添加一个 Http 的编解码的 Handler, 然后接着添加自己的业务逻辑的 handler, 这样网络上的数据流就向通过一个管道一样, 从不同的 handler 中流过并进行编解码, 最终在到达自定义的 handler 中

* Netty 使用不同的事件来通知状态的改变或者是操作的状态，所有事件是按照它们与入站或出站数据流的相关性进行分类的
  * 可能由`入站数据`或者相关的状态更改而触发的事件包括
    * 连接已被激活或者连接失活、数据读取、用户事件、错误事件
  * `出站事件`是未来将会触发的某个动作的操作结果
    * 打开或者关闭到远程节点的连接、将数据写到或冲刷到套接字
* 每个事件都可以被分发给 ChannelHandler 类中的某个实现的方法，将事件驱动范式直接转换为应用程序构件块
* 在内部，ChannelHandler 自己也使用了事件和 Future，使得它们也成为了应用程序将使用的相同抽象的消费者

* 回调-逻辑，在操作完成后通知相关方

  ```java
  //被回调触发的 ChannelHandler
  public class ConnectHandler extends ChannelInboundHandlerAdapter { 
      //当一个新的连接已经被建立时channelActive(ChannelHandler Context)将会被调用
    @Override
  	public void channelActive(ChannelHandlerContext ctx) throws Exception {
  		System.out.println("Client " + ctx.channel().remoteAddress() + " connected"); 	
    }
  }
  ```

* 资源管理

  * 诊断潜在的(资源泄漏)问题，Netty提供了class ResourceLeakDetector1，它将对应用程序的缓冲区分配做大约 1%的采样来检测内存泄露
  * 如果一个消息被消费或者丢弃了，并且没有传递给 ChannelPipeline 中的下一个
    ChannelOutboundHandler，那么用户就有责任调用 ReferenceCountUtil.release()释放资源，还要通知 ChannelPromise。否则可能会出现 ChannelFutureListener 收不到某个消息已经被处理了的通知的情况

## ByteBuf

* 引用了一段内存，这段内存可以是堆内也可以是堆外的，然后用引用计数来控制这段内存是否需要被释放，使用读写指针来控制对 ByteBuf 的读写

* ByteBuf分类
  * Pooled和Unpooled
  * Unsafe和非Unsafe：unsafe会调用jdk底层的api直接操作内存
  * Heap和Direct：Direct不会被JVM回收

* 作用是通过Netty的管道传输数据，缓冲区传送数据都是通过Netty的ChannelPipeline和ChannelHandler

* 优点

  * 容量可以按需增长，读和写使用了不同的索引，读写两种模式之间切换不需调用flip()
  * 通过内置的复合缓冲区类型实现了透明的零拷贝
  * 支持方法的链式调用及引用计数（能自动释放资源）
  * 支持池化
  * 可以被用户自定义的缓冲区类型扩展

* 如何工作

  * 维护读取（readIndex）和写入（writeIndex）两个索引
    * read或者write开头的ByteBuf 方法，将会推进其对应的索引，而以set或者get开头的操作则不会
  * 指定 ByteBuf 的最大容量，默认的限制是 Integer.MAX_VALUE
  * 只有读写索引处于同一位置。然后ByteBuf就不可读了，如果继续读的话就会抛出IndexOutOfBoundsException异常，类似读数组越界

* 字节级操作

  * 随机访问索引，ByteBuf的索引也是从0开始的，最后一个字节的位置是容量-1
  * 顺序访问索引，ByteBuf 被它的两个索引划分成 3 个区域，0-readerIndex：已被读取可丢弃的字节，readerIndex-writerIndex：可读字节，writerIndex-capacity：可写字节
    * 还有个容量上限：maxCapacity，当向 ByteBuf 写数据的时候，如果容量不足时可进行扩容，直到 capacity 扩容到 maxCapacity，超过 maxCapacity 就会报错
  * `markReaderIndex() 与 resetReaderIndex()`
    * 前者表示把当前的读指针保存起来，后者表示把当前的读指针恢复到之前保存的值
  * `release() 与 retain()`
    * Netty 的 ByteBuf 是通过引用计数的方式管理，默认情况下，当创建完一个 ByteBuf，它的引用为1，然后每次调用 retain() 方法， 它的引用就加一， release() 方法原理是将引用计数减一，减完之后如果发现引用计数为0，则直接回收 ByteBuf 底层的内存

* 类型

  * 由于 Netty 使用了堆外内存不被 jvm 直接管理，需手动回收，`release() 与 retain()`

    * Netty 的 ByteBuf 是通过引用计数的方式管理，如果一个 ByteBuf 没有地方被引用到，需要回收底层内存，现引用计数为0，则直接回收 ByteBuf 底层的内存

  * 堆缓冲区（支撑数组）

    * 将数据存储在 JVM 的堆空间中，能在没有使用池化的情况下提供快速的分配和释放，非常适合于有遗留的数据需要处理的情况

      ```java
      //支撑数组
      ByteBuf heapBuf = ...;
      // 检查 ByteBuf 是否有一个支撑数组
      //如果访问非堆内存ByteBuf的数组，就会抛出UnsupportedOperationException。因此访问ByteBuf数组之前应该 使用hasArray()方法检测此缓冲区是否支持数组
      if (heapBuf.hasArray()) {
          //如果有，则获取对该数组的引用
      	byte[] array = heapBuf.array();
          // 计算第一个字节 的偏移量
      	int offset = heapBuf.arrayOffset() + heapBuf.readerIndex(); 
          //获得可读字节数
        int length = heapBuf.readableBytes();
          //使用数组、偏移量和长度作为参数调用自定义方法
      	handleArray(array, offset, length);
      }
      ```

  * 直接缓冲区

    * 其内容将驻留在常规的会被垃圾回收的堆之外，对于网络数据传输是理想的选择

    * 避免在每次调用本地 I/O 操作之前(或者之后)将缓冲区的内容复制到一个中间缓冲区(或者从中间缓冲区把内容复制到缓冲区)

    * 如果数据包含在一个在堆上分配的缓冲区中，那么事实上，在通过套接字发送它之前，JVM将会在内部把缓冲区复制到一个直接缓冲区中

    * 缺点：相对于基于堆的缓冲区，它们的分配和释放都较为昂贵；正在处理遗留代码，因为数据不是在堆上，所以不得不进行一次复制

      ```java
      //访问直接缓冲区的数据
      ByteBuf directBuf = ...;
      // 检查 ByteBuf 是否由数组支撑。如果不是，则这是一个直接缓冲区
      if (!directBuf.hasArray()) {
      	int length = directBuf.readableBytes();
      	byte[] array = new byte[length]; 
          //将字节复制到该数组
        	//直接缓冲区需要将数据复制到数组，如果想直接通过字节数组访问数据，用堆缓冲区更好一些
          directBuf.getBytes(directBuf.readerIndex(), array); 
          handleArray(array, 0, length);
      }
      ```

  * 复合缓冲区

    * CompositeByteBuf提供了一个将多个缓冲区表示为单个合并缓冲区的虚拟表示
      * 发送的消息往往只修改一部分，比如消息体一样，改变消息头。这里就不用每次都重新分配缓存了

    ```java
    //使用 CompositeByteBuf 的复合缓冲区模式
    CompositeByteBuf messageBuf = Unpooled.compositeBuffer(); 
    ByteBuf headerBuf = ...; // can be backing or direct 
    ByteBuf bodyBuf = ...; // can be backing or direct 
    messageBuf.addComponents(headerBuf, bodyBuf);
      .....
    //删除位于索引位置为 0 (第一个组件)的 ByteBuf
    //移除索引是0的数据，就像List
    messageBuf.removeComponent(0); // remove the header 
    for (ByteBuf buf : messageBuf) {
    	System.out.println(buf.toString()); 
    }
    ```
    * CompositeByteBuf 可能不支持访问其支撑数组，因此访问 CompositeByteBuf 中的数据类似于(访问)直接缓冲区的模式

    ```java
    //访问 CompositeByteBuf 中的数据
    CompositeByteBuf compBuf = Unpooled.compositeBuffer(); 
    int length = compBuf.readableBytes();
    byte[] array = new byte[length]; 
    compBuf.getBytes(compBuf.readerIndex(), array); 
    handleArray(array, 0, array.length);
    ```

* 派生缓冲区

  * 即创建一个已经存在的缓冲区的视图

    * 数据共享，`duplicate();` `slice();` `slice(int, int)` `Unpooled.unmodifiableBuffer(...);`

      `order(ByteOrder);` `readSlice(int)`

      * 返回一个新的 ByteBuf 实例，具有自己的读索引、写索引和标记索引
      
    * slice() 只截取从 readerIndex 到 writerIndex 之间的数据，它返回的 ByteBuf 的最大容量被限制到原始 ByteBuf 的 readableBytes(), 而 duplicate() 是把整个 ByteBuf 都与原始的 ByteBuf 共享
    
    * 独立数据副本，`copy`或者`copy(int,int)`，需进行内存复制操作，不仅消耗更多资源，执行方法也会更耗时 
    
    * slice()、duplicate()、copy()均维护着自己的读写指针，与原始的 ByteBuf 的读写指针无关，相互之间不受影响
    
    * 在一个函数体里面，只要增加了引用计数（包括 ByteBuf 的创建和手动调用 retain() 方法），就必须调用 release() 方法。遵循谁 retain() 谁 release() 的原则

* 分配

  * 按需分配：ByteBufAllocator 接口实现了(ByteBuf 的)池化，它可以用来分配所描述过的任意类型的 ByteBuf 实例，可降低分配和释放内存的开销，获取引用：`channel.alloc();`、`ChannelHandlerContext对象.alloc();`
    * `buffer()`返回一个基于堆或直接内存存储的 ByteBuf
    * `heapBuffer()`返回一个基于堆内存存储的 ByteBuf 
    * `directBuffer()`返回一个基于直接内存存储的ByteBuf 
    * `compositeBuffer()`返回一个可以通过添加最大到指定数目的基于堆的或者直接内存存储的缓冲区来扩展的CompositeByteBuf
    * `ioBuffer()`返回一个用于套接字的 I/O 操作的 ByteBuf
    * 两种实现，`PooledByteBufAllocator`默认，池化了ByteBuf的实例以提高性能并最大限度地减少内存碎片；`UnpooledByteBufAllocator`不池化（heap内存的分配、direct内存的分配），并且在每次它被调用时都会返回一个新的实例（拿到线程局部缓存PoolThreadCache，在线程局部缓存的Area上进行内存分配，每个线程对应一个Area）
  * Unpooled 缓冲区，提供了静态的辅助方法来创建未池化的 ByteBuf实例
  * ByteBufUtil 类，提供了用于操作 ByteBuf 的静态的辅助方法

* 引用计数器

  * 某个对象所持有的资源不再被其他对象引用时，释放该对象所持有的资源

  * 对于池化实现(如 PooledByteBufAllocator)来说是至关重要的，它降低了内存分配的开销

    ```java
    Channel channel = ...;
    ByteBufAllocator allocator = channel.alloc();
    ByteBuf buffer = allocator.directBuffer(); 
    //检查引用计数器是否为预期的1
    assert buffer.refCnt() == 1;
    //减少到该对象的活动引用。当减少到 0 时 该对象被释放，并且该方法返回 true
    ByteBuf buffer = ...;
    boolean released = buffer.release();
    ```

  * 谁负责释放 ：一般来说，是由最后访问(引用计数)对象的那一方来负责将它释放

* ByteBufHolder

  * 可实现一个将其有效负载存储在 ByteBuf 中的消息对象
  * 提供缓冲区池化，其中可以从池中借用 ByteBuf，并且在需要时自动释放
  * `content()` 返回由这个 ByteBufHolder 所持有的 ByteBuf
  * `copy()`返回这个 ByteBufHolder 的一个深拷贝，包括一个其所包含的 ByteBuf 的非共享拷贝
  * `duplicate()`返回这个ByteBufHolder的一个浅拷贝，包括一个其所包含的ByteBuf的共享拷贝

* 其他操作

  * `isReadable()` `isWritable()` `readableBytes()`  `writableBytes()`  `capacity()` 

    `maxCapacity()`

  * `hasArray()` 如果 ByteBuf 由一个字节数组支撑，则返回 true

  * `array()`如果 ByteBuf 由一个字节数组支撑则返回该数组;否则，它将抛出一个
    UnsupportedOperationException 异常

## 引导类

* 对应用程序进行配置并使其运行起来的过程

  * Netty 的引导类为应用程序的网络层配置提供了容器，这涉及将一个进程绑定到某个指定的端口，或者将一个进程连接到另一个运行在某个指定主机的指定端口上的进程

* Netty处理引导的方式：使应用程序的逻辑或实现和网络层相隔离，无论它是客户端还是服务器

  * 将`EventLoop`、`ChannelPipeline`、`ChannelHandler `和 这些部分组织起来，成为一个可实际运行的应用程序

* `AbstractBootStrap`类

  * `<interface> Cloneable` - `AbstractBootStrap` -` BootStrap/ServerBootStrap`
  * `服务器`：使用一个父 Channel 接受来自客户端的连接，并创建子 Channel 以用于它们之间的通信
  * `客户端`：使用一个单独的、没有父 Channel 的 Channel 来用于所有的网络交互
  * 为什么引导类是 Cloneable 的？
    * 有时可能会需要创建多个具有类似配置或者完全相同配置的Channel。为了支持这种模式而又不需要为每个Channel都创建并配置一个新的引导类实例，AbstractBootstrap被标记为了 Cloneable
    * 在一个已经配置完成的引导类实例上调用clone()方法将返回另一个可以立即使用的引导类实例 
    * 这种方式只会创建引导类实例的EventLoopGroup的一个浅拷贝，所以，被浅拷贝的EventLoopGroup将在所有克隆的Channel实例之间共享
      * 这是可以接受的，因为通常这些克隆的Channel的生命周期都很短暂，一个典型的场景是——创建一个Channel以进行一次HTTP请求

* BootStrap

  * 负责为客户端和使用无连接协议的应用程序创建 Channel

  * 用于客户端 ，连接到远程主机和端口；EventLoopGroup 的数目为1

    * Bootstrap类将会在bind()方法被调用后创建一个新的Channel，在这之后将会调用connect()方法以建立连接

    * 在connect()方法被调用后，Bootstrap类将会创建一个新的Channel

    * `option`设置`ChannelOption`，设置TCP参数（socket options），通过bind()或connect()方法设置到每个新建（之前已创建的Channel不会有效果）的Channel的ChannelConfig中

      * 可用的`ChannelOption`包括了底层连接的详细信息，如keep-alive 或者超时属性以及缓冲区设置

    * `attr`指定新创建的`Channel` 的属性值，在 `Channel` 被创建后将不会有任何的效果，通过bind()或者 connect()方法设置到 Channel

    * `clone`创建一个当前 Bootstrap 的克隆，其具有和原始的Bootstrap 相同的设置信息

    * `bind`绑定Channel并返回一个ChannelFuture，其将会在绑定操作完成后接收到通知，在那之后必须调用 Channel. connect()方法来建立连接

    * 像 Channel 这样的组件可能甚至会在正常的 Netty 生命周期之外被使用

      * 在某些常用的属性和数据不可用时，Netty 提供了`AttributeMap`抽象(一个由 Channel 和引导类提供的集合)以及` AttributeKey`<T>(一个用于插入和获取属性值的泛型类)

      * 使用这些工具，便可以安全地将任何类型的数据项与客户端和服务器 Channel(包含 ServerChannel 的子 Channel)相关联

        ```java
        final AttributeKey<Integer> id = new AttributeKey<Integer>("ID");
        bootstrap.option(ChannelOption.SO_KEEPALIVE,true)
        		     .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 5000); 
        bootstrap.attr(id, 123456);
        
        Integer idValue = ctx.channel().attr(id).get();
        ```

  * 无连接的协议
    * Netty 提供了各种`DatagramChannel`的实现。唯一区别就是，不再调用 connect()方法，而是只调用 bind()方法
    * `new OioEventLoopGroup()`、`OioDatagramChannel.class`、`new SimpleChannelInboundHandler<DatagramPacket>`

* ServerBootstrap

  * 用于服务器，绑定到一个本地端口；`EventLoopGroup`的数目为1或2
  * 服务器需要两组不同的`Channel`
    * 第一组只包含一个`ServerChannel`，代表服务器自身的已绑定到某个本地端口的正在监听的套接字
    * 第二组包含所有已创建的用来处理传入客户端连接的 Channel
    * ServerBootstrap调用bind()方法将创建一个ServerChannel，当连接被接受时，ServerChannel将会创建一个新的子Channel
      * 与 ServerChannel 相关联的 EventLoopGroup 将分配一个EventLoop负责为传入连接请求创建Channel，一旦连接被接受，第二个 EventLoopGroup 就会给它的 Channel分配一个 EventLoop
    * `childOption`指定当子 Channel 被接受时，应用到子 Channel 的 ChannelConfig 的ChannelOption
    * `childAttr`将属性设置给已经被接受的子`Channel`。接下来的调用将不会有任何的效果
  * `handler`：处理服务端逻辑，比如handler添加了、handler注册上了
  * `childHandler`：对于连接上的处理

* 从Channel引导客户端

  * 编写 Netty 应用程序的一个一般准则：尽可能地重用 EventLoop，以减少线程创建所带来的开销

  * 服务器正在处理一个客户端的请求，这个请求需要它充当第三方系统的客户端，从已经被接受的子 Channel 中引导一个客户端 Channel

    * 通过将已被接受的子 Channel 的 EventLoop 传递给 Bootstrap的 group()方法来共享该 EventLoop，避免产生额外的线程及相关的上下文切换

    ```java
    //引导服务器
    ServerBootstrap bootstrap = new ServerBootstrap();
    bootstrap.group(new NioEventLoopGroup(), new NioEventLoopGroup())
    .channel(NioServerSocketChannel.class) 
    .childHandler(new SimpleChannelInboundHandler<ByteBuf>() { 
      ChannelFuture connectFuture;
    	@Override
    	public void channelActive(ChannelHandlerContext ctx) throws Exception {
    		Bootstrap bootstrap = new Bootstrap(); 		
        bootstrap.channel(NioSocketChannel.class)
          .handler(new SimpleChannelInboundHandler<ByteBuf>() { 
            @Override
            protected void channelRead0( ChannelHandlerContext ctx, ByteBuf in) throws Exception { 
              System.out.println("Received data");
            } 
          });
        //尽可能地重用 EventLoop，以减少线程创建所带来的开销
        // 使用与分配给已被接受的子 Channel 相同的 EventLoop
        bootstrap.group(ctx.channel().eventLoop()); 
        connectFuture = bootstrap.connect(new InetSocketAddress("www.manning.com", 80));
    	}
      
      @Override
    	protected void channelRead0(ChannelHandlerContext channelHandlerContext, ByteBuf byteBuf) throws Exception {
        // 当连接完成时，执行一 些数据操作(如代理）
    		if (connectFuture.isDone()) {
    		   // do something with the data
    		}
    	} 
     );
      
      ChannelFuture future = bootstrap.bind(new InetSocketAddress(8080)); 		
      future.addListener(new ChannelFutureListener() {
        @Override
        public void operationComplete(ChannelFuture channelFuture)throws Exception {
          if (channelFuture.isSuccess()) {
            System.out.println("Server bound"); 
          } else {
            System.err.println("Bind attempt failed");
            channelFuture.cause().printStackTrace(); 
          }
        }
    	);
    ```

* 引导过程中添加多个 ChannelHandler

  * 通过在 ChannelPipeline 中将它们链接在一起来部署尽可能多的 ChannelHandler

  * Netty 提供了一个特殊的`ChannelInboundHandlerAdapter`子类`ChannelInitializer<Channel>`

    ```java
    public abstract class ChannelInitializer<C extends Channel> extends ChannelInboundHandlerAdapter
    //将多个 ChannelHandler 添加到一个 ChannelPipeline 中的简便方法
    protected abstract void initChannel(C ch) throws Exception;
    //一旦 Channel 被注册到了它的 EventLoop 之后，就会调用重写的 initChannel()版本。在该方法返回之后，ChannelInitializer 的实例将会从 ChannelPipeline 中移除它自己
    ```

* 关闭
  * 调用`EventLoopGroup.shutdownGracefully()`方法关闭 `EventLoopGroup`，它将处理任何挂起的事件和任务，并且随后将释放所有的资源，并且关闭所有的当前正在使用中的 `Channel`
    * `shutdownGracefully()`方法也是一个异步的操作，所以需要阻塞等待直到它完成，或者向所返回的 Future 注册一个监听器以在关闭完成时获得通知

## 编解码器

* 编码器操作出站数据，而解码器处理入站数据

  * 网络数据总是一系列的字节，所有由`Netty` 提供的编码器/解码器适配器类都实现了 `ChannelOutboundHandler` 或者 `ChannelInboundHandler` 接口
    * 对于入站数据来说，channelRead 方法/事件已经被重写了。对于每个从入站Channel 读取的消息，这个方法都将会被调用。随后，它将调用由预置解码器所提供的decode()方法，并将已解码的字节转发给 ChannelPipeline 中的下一个 ChannelInboundHandler
    * 出站消息的模式是相反方向的：编码器将消息转换为字节，并将它们转发给下一个`ChannelOutboundHandler`
    * SimpleChannelInboundHandler
      * 扩展基类 `SimpleChannelInboundHandler<T>`，其中 T 是要处理的消息的 `Java` 类型，重写基类的一个或者多个方法，并且获取到一个到`ChannelHandlerContext`的引用，这个引用将作为输入参数传递给`ChannelHandler` 的所有方法
  * 编解码器中的引用计数`retain`
    * 一旦消息被编码或者解码，它就会被 ReferenceCountUtil.release(message)调用自动释放
    * 如果需要保留引用以便稍后使用，那可调用 ReferenceCountUtil.retain(message)方法。这将会增加该引用计数，从而防止该消息被释放
  * 编码器处理逻辑：MessageToByteEncoder
    * 匹配对象，判断当前编码器能否处理该对象，如果不能则将该对象向前传播
    * 分配内存
    * 编码实现，encode将cast对象转为ByteBuf
    * 释放对象，ReferenceCountUtil.release(cast);
    * 传播数据，传播ByteBuf
    * 释放内存，如果出现异常则需释放内存buf.release()，如果原生对象就是一个ByteBuf，Netty在自定义编码结束之后自动释放对象，不需要在Encode方法里释放原生对象
  * write-写buffer队列
    * direct化ByteBuf、插入写队列、设置写状态
  * flush-刷新buffer队列
    * 添加刷新标识并设置写状态、遍历buffer队列，过滤ByteBuf、调用jdk底层api进行自旋写

* 解码器

  * 每当需要为 ChannelPipeline 中的下一个 ChannelInboundHandler 转换入站数据时会用到
  * 将字节解码为消息——ByteToMessageDecoder 和 ReplayingDecoder
    * `ChannelInboundHandler`接口的实现
    * 字节到对象的解码步骤
      * 累加字节流、调用子类的decode方法进行解析、将解析到的ByteBuf向下传播
    * `ByteToMessageDecoder`由于不可能知道远程节点是否会一次性地发送一个完整的消息，所以这个类会对入站数据进行缓冲，直到它准备好处理（不能让解码器缓冲大量的数据以至于耗尽可用的内存）
      * `ByteToMessageDecoder`会一直调用decode()直到数据都读完
    * `ReplayingDecoder`不必调用`readableBytes()`方法，它通过使用一个自定义的`ByteBuf`实现，`ReplayingDecoderByteBuf`，包装传入的`ByteBuf`实现了这一点，其将在内部执行该调用
    * 如果使用`ByteToMessageDecoder` 不会引入太多的复杂性，那么请使用它；否则，请使用 `ReplayingDecoder`
      - `LineBasedFrameDecoder`使用了行尾控制字符(\n 或者\r\n)来解析消息数据
      - `HttpObjectDecoder` HTTP 数据的解码器
  * 将一种消息类型解码为另一种——MessageToMessageDecoder，在两个消息格式之间进行转换
  * `TooLongFrameException`其将由解码器在帧超出指定的大小限制时抛出

  ```java
  //对decode方法的调用将会重复进行，直到确定没有新的元素被添加到该 List，或者该 ByteBuf 中没有更多可读取的字节时为止。然后，如果该 List 不为空，那么它的内容将会被传递给 ChannelPipeline 中的下一个 ChannelInboundHandler
  public class ToIntegerDecoder extends ByteToMessageDecoder { 
    @Override
  	public void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
      //检查是否至少有 4 字节可读(一个 int 的字节长度)
  		if (in.readableBytes() >= 4) {
  			out.add(in.readInt());
  		}
  	}
  }
  ```
  ````java
  //类型参数 S 指定了用于状态管理的类型，其中 Void 代表不需要状态管理
  public class ToIntegerDecoder2 extends ReplayingDecoder<Void> { 
    // 传入的 ByteBuf 是 ReplayingDecoderByteBuf
    @Override
  	public void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
      //从入站ByteBuf中读取一个 int，并将其添加到 解码消息的 List 中
      out.add(in.readInt());
    }
  }
  ````

  ```java
  //对于每个需要被解码为另一种格式的入站消息来说，该方法都将会被调用。解码消息随后会被传递给ChannelPipeline 中的下一个 ChannelInboundHandler
  public class IntegerToStringDecoder extends MessageToMessageDecoder<Integer> {
  	@Override
  	public void decode(ChannelHandlerContext ctx, Integer msg,List<Object> out) throws Exception {
    //将Integer消息转换为它的String表示，并将其添加到输出的 List 中
      out.add(String.valueOf(msg));
    }
  }
  ```

  ```java
  public class SafeByteToMessageDecoder extends ByteToMessageDecoder { 
    private static final int MAX_FRAME_SIZE = 1024;
  	@Override
  	public void decode(ChannelHandlerContext ctx, ByteBuf in,List<Object> out) throws Exception { 
  		int readable = in.readableBytes(); 
  		if (readable > MAX_FRAME_SIZE) {
  				//跳过所有可读字节
  				in.skipBytes(readable);
  				//抛出 TooLongFrameException 并通知 ChannelHandler
  				throw new TooLongFrameException("Frame too big!");
  		}
  				// do something
  				... 
  	}
  }
  ```

* 编码器

  * 实现了 `ChannelOutboundHandler`，并将出站数据从一种格式转换为另一种格式
    * `MessageToByteEncoder`将消息编码为字节
    * `MessageToMessageEncoder`出站数据从一种消息编码为另一种

  ```java
  //接受一个Short类型的实例作为消息，将它编码 为Short的原子类型值，并将它写入ByteBuf中，其将随后被转发给ChannelPipeline中的下一个ChannelOutboundHandler
  public class ShortToByteEncoder extends MessageToByteEncoder<Short> { 
    @Override
    //被调用时将会传入要被该类编码为 ByteBuf 的(类型为I的)出站消息。该 ByteBuf 随后将会被转发给 ChannelPipeline 中的下一个 ChannelOutboundHandler
  	public void encode(ChannelHandlerContext ctx, Short msg, ByteBuf out) throws Exception {
      out.writeShort(msg);
    }
  }
  ```

  ```java
  public class IntegerToStringEncoder extends MessageToMessageEncoder<Integer> {
  	@Override
    //每个通过 write()方法写入的消息都将会被传递给 encode()方法，以编码为一个或者多个出站消息。随后，这些出站消息将会被转发给ChannelPipeline中的下一个ChannelOutboundHandler
  	public void encode(ChannelHandlerContext ctx, Integer msg,List<Object> out) throws Exception {
  			out.add(String.valueOf(msg)); 
      }
  }
  ```

* 抽象的编解码器类

  * `ByteToMessageCodec`要将字节解码为某种形式的消息，可能是 POJO，随后再次对它进行编码

    ```java
    //只要有字节可以被消费，这个方法就将会被调用。它将入站 ByteBuf 转换为指定的消息格式，并将其转发给 ChannelPipeline 中的下一个 ChannelInboundHandler
    decode(ChannelHandlerContext ctx, ByteBuf in,List<Object>)
        
    //这个方法的默认实现委托给了 decode()方法。它只会在 Channel 的状态变为非活动时被调用一次。它可以被重写以实现特殊的处理
    decodeLast( ChannelHandlerContext ctx, ByteBuf in,List<Object> out)    
        
    //对于每个将被编码并写入出站 ByteBuf 的(类型为 I 的) 消息来说，这个方法都将会被调用
    encode(ChannelHandlerContext ctx, I msg,ByteBuf out) 
    ```

  * `MessageToMessageCodec`一种消息格式转换为另外一种消息格式的往返过程

    ```java
    //被调用时会被传入 INBOUND_IN 类型的消息(通过网络发送的类型)。 它将把它们解码为 OUTBOUND_IN 类型的消息(应用程序所处理的类型)，这些消息将被转发给 ChannelPipeline 中的下一个 ChannelInboundHandler
    protected abstract decode(ChannelHandlerContext ctx,INBOUND_IN msg,List<Object> out)
        
    //对于每个 OUTBOUND_IN 类型的消息，这个方法都将会被调用。这些消息将会被编码为 INBOUND_IN 类型的消 息，然后被转发给 ChannelPipeline 中的下一个 ChannelOutboundHandler
    protected abstract encode(ChannelHandlerContext ctx,OUTBOUND_IN msg,List<Object> out)
    ```

  * `CombinedChannelDuplexHandler`充当了`ChannelInboundHandler` 和 `ChannelOutboundHandler`的容器

    ```java
    public class CombinedChannelDuplexHandler <I extends ChannelInboundHandler,
    O extends ChannelOutboundHandler>
    //ByteToCharDecoder
    //CharToByteEncoder
    public class CombinedByteCharCodec extends CombinedChannelDuplexHandler<ByteToCharDecoder, CharToByteEncoder> { 
    	public CombinedByteCharCodec() {
    		super(new ByteToCharDecoder(), new CharToByteEncoder()); 
    	}
    }
    ```

## 预置的ChannelHandler和编码器

* SSL/TLS

  * 为了支持 SSL/TLS，Java 提供了 javax.net.ssl 包，它的 SSLContext 和 SSLEngine类使得实现解密和加密相当简单直接
  * Netty 通过一个名为`SslHandler` 的 ChannelHandler实现利用了这个 API，其中 SslHandler 在内部使用 SSLEngine 来完成实际的工作
  * SslHandler拦截了加密的入栈数据并对其进行解密，使其走向入栈端
  * 出站数据被传递通过SslHandler，SslHandler对数据进行了加密，并且传递给出站端

  ```java
  public class SslChannelInitializer extends ChannelInitializer<Channel>{ 
    private final SslContext context;
  	private final boolean startTls;
  	public SslChannelInitializer(SslContext context, boolean startTls) {
  		this.context = context;
  		this.startTls = startTls; 
      }
  	@Override
  	protected void initChannel(Channel ch) throws Exception {
  		SSLEngine engine = context.newEngine(ch.alloc()); 
          //大多数情况下作为第一个被加入
          ch.pipeline().addFirst("ssl",new SslHandler(engine, startTls)); 
      }
  }
  ```

* HTTP 解码器、编码器和编解码器

  * 一个 HTTP 请求/响应可能由多个数据部分组成，HttpRequeset/HttpResponse、多个HttpContent、并且它总是以一个 LastHttpContent 部分作为结束

  ```java
  //添加 HTTP 支持
  public class HttpPipelineInitializer extends ChannelInitializer<Channel> { 
    private final boolean client;
  	public HttpPipelineInitializer(boolean client) {
    	this.client = client;
    }
  	@Override
  	protected void initChannel(Channel ch) throws Exception {
  		ChannelPipeline pipeline = ch.pipeline(); 
        if (client) {
              //将字节解码为 HttpResponse、HttpContent 和 LastHttpContent 消息
  						pipeline.addLast("decoder", new HttpResponseDecoder());
              //将 HttpRequest、HttpContent 和 LastHttpContent 消息编码为字节
  						pipeline.addLast("encoder", new HttpRequestEncoder()); 
          } else {
              //将字节解码为 HttpRequest、HttpContent 和 LastHttpContent 消息
  						pipeline.addLast("decoder", new HttpRequestDecoder());
              //将 HttpResponse、HttpContent 和 LastHttpContent 消息编码为字节
  						pipeline.addLast("encoder", new HttpResponseEncoder()); 
          }
  	} 
  }
  ```

* 聚合 HTTP 消息

  * 由于 HTTP 的请求和响应可能由许多部分组成，因此需要聚合它们以形成完整的消息
  * Netty 提供了一个聚合器，它可以将多个消息部分合并为`FullHttpRequest` 或者`FullHttpResponse` 消息。通过这样的方式，将总是看到完整的消息内容
  * 由于消息分段需要被缓冲，直到可以转发一个完整的消息给下一个`ChannelInboundHandler`，所以这个操作有轻微的开销。其所带来的好处便是不必关心消息碎片了
  * 引入这种自动聚合机制只不过是向 `ChannelPipeline` 中添加另外一个`ChannelHandler`罢了

  ```java
  //自动聚合 HTTP 的消息片段
  public class HttpAggregatorInitializer extends ChannelInitializer<Channel> { 
    private final boolean isClient;
  	public HttpAggregatorInitializer(boolean isClient) {
        this.isClient = isClient;
  	}
  	@Override
  	protected void initChannel(Channel ch) throws Exception {
  		ChannelPipeline pipeline = ch.pipeline(); 
          if (isClient) {
  						pipeline.addLast("codec", new HttpClientCodec()); 
          } else {
  						pipeline.addLast("codec", new HttpServerCodec());
          }
  		pipeline.addLast("aggregator",new HttpObjectAggregator(512 * 1024));
  	} 
  }
  ```

* HTTP 压缩

  ```java
  //自动压缩 HTTP 消息以尽可能多地减小传输数据的大小
  public class HttpCompressionInitializer extends ChannelInitializer<Channel> { 
    private final boolean isClient;
  	public HttpCompressionInitializer(boolean isClient) {
    	this.isClient = isClient;
    }
      @Override
  	protected void initChannel(Channel ch) throws Exception {
  		ChannelPipeline pipeline = ch.pipeline(); 
      if (isClient) {
  			pipeline.addLast("codec", new HttpClientCodec());
        //如果是客户端，则添加 HttpContentDecompressor 以 处理来自服务器的压缩内容
  			pipeline.addLast("decompressor",new HttpContentDecompressor());
  		} else {       
  			pipeline.addLast("codec", new HttpServerCodec()); 	
       // 如果是服务器，则添加 HttpContentCompressor 来压缩数据(如果客户端支持它)
        pipeline.addLast("compressor",new HttpContentCompressor());
  		} 
    }
  }
  ```

* 使用 `HTTPS`

  * 启用`HTTPS` 只需要将`SslHandler` 添加到`ChannelPipeline` 的`ChannelHandler` 组合

* `WebSocket`

  * `WebSocket`提供了在一个单个的`TCP`连接上提供双向的通信，结合`WebSocket API`，它为网页和远程服务器之间的双向通信提供了一种替代HTTP轮询的方案

  * 应用程序中添加对于 `WebSocket` 的支持，需要将适当的客户端或者服务器`WebSocket` ChannelHandler 添加到 ChannelPipeline 中。这个类将处理由 WebSocket 定义的称为帧的特殊消息类型。`WebSocketFrame` 可以被归类为数据帧或者控制帧

  * 要想为 WebSocket 添加安全性，只需将 SslHandler 作为第一个ChannelHandler 添加到 ChannelPipeline 中

  * 客户端通过`HTTP(S)`向服务器发起`WebSocket`握手，并等待确认，服务端同意后，连接协议升级到`WebSocket`

    ```java
    //在服务器端支持 WebSocket
    public class WebSocketServerInitializer extends ChannelInitializer<Channel>{ 
      @Override
      protected void initChannel(Channel ch) throws Exception {
        ch.pipeline().addLast(
          new HttpServerCodec(),
          new HttpObjectAggregator(65536), // 为握手提供聚合的HttpRequest
          // 如果被请求的端点是 "/websocket"， 则处理该升级握手
          new WebSocketServerProtocolHandler("/websocket"), 
          // TextFrameHandler 处理 TextWebSocketFrame
          new TextFrameHandler(),  //继承SimpleChannelInboundHandler<自己类型>
          // BinaryFrameHandler 处理 BinaryWebSocketFrame
          new BinaryFrameHandler(),//继承SimpleChannelInboundHandler<自己类型>
          // ContinuationFrameHandler 处理 ContinuationWebSocketFrame
          new ContinuationFrameHandler());//继承SimpleChannelInboundHandler<自己类型>
      }
      ...
    }
    ```

* 空闲的连接和超时

  * `IdleStateHandler`当连接空闲时间太长时，将会触发一个`IdleStateEvent` 事件，然后，可以通过在`ChannelInboundHandler` 中重写`userEventTriggered()`方法来处理该 `IdleStateEvent` 事件
  * `ReadTimeoutHandler`如果在指定的时间间隔内没有收到任何的入站数据，则抛出一个 `ReadTimeoutException` 并关闭对应的`Channel`。可以通过重写`ChannelHandler` 中的 `exceptionCaught()`方法来检测该 `ReadTimeoutException`
  * `WriteTimeoutHandler`如果在指定的时间间隔内没有任何出站数据写入，则抛出一个 `WriteTimeoutException` 并关闭对应的 `Channel`。可以通过重写`ChannelHandler`的 `exceptionCaught()`方法检测该 `WriteTimeoutException`

  ```java
  //发送心跳
  //使用通常的发送心跳消息到远程节点的方法时，如果在60秒之内没有接收或者发送任何的数据，将如何得到通知;如果没有响应，则连接会被关闭
  public class IdleStateHandlerInitializer extends ChannelInitializer<Channel>{
    @Override
  	protected void initChannel(Channel ch) throws Exception {
     		ChannelPipeline pipeline = ch.pipeline();
          //IdleStateHandler 将在被触发时发送一个IdleStateEvent 事件
          pipeline.addLast(new IdleStateHandler(0, 0, 60, TimeUnit.SECONDS)); 		
          pipeline.addLast(new HeartbeatHandler());
          
      public static final class HeartbeatHandler extends ChannelInboundHandlerAdapter {
          //发送到远程节点的心跳消息
  		private static final ByteBuf HEARTBEAT_SEQUENCE =
  Unpooled.unreleasableBuffer(Unpooled.copiedBuffer( "HEARTBEAT", CharsetUtil.ISO_8859_1));
          //实现 userEventTriggered()方法 如果这个方法检测到 IdleStateEvent 事件，它将会发送心 跳消息，并且添加一个将在发送操作失败时关闭该连接的 ChannelFutureListener
          @Override
  		public void userEventTriggered(ChannelHandlerContext ctx,Object evt) throws Exception {
  		if (evt instanceof IdleStateEvent) {
              //发送心跳消息，并在发送失败时关闭该连接
  					ctx.writeAndFlush(HEARTBEAT_SEQUENCE.duplicate())
           		 .addListener(ChannelFutureListener.CLOSE_ON_FAILURE);
      } else {
              //不是IdleStateEvent事件，所以将它传递给下一个 ChannelInboundHandler
  						super.userEventTriggered(ctx, evt); 
      }
  	}
  }
  ```

* 解码基于分隔符的协议和基于长度的协议

  * 基于分隔符的消息协议使用定义的字符来标记的消息或者消息段(通常被称为帧)的开头或者结尾
    * `DelimiterBasedFrameDecoder`使用任何由用户提供的分隔符来提取帧的通用解码器
  * `LineBasedFrameDecoder`提取由行尾符(\n或\r\n)分隔的帧的解码器，比`DelimiterBasedFrameDecoder`更快
    * 由尾行符分隔的帧，字节流：`ABC\r\nDEF\r\n`—帧：`ABC\r\n`（第一帧）、`DEF\r\n`（第二帧）

  ```java
  // 该LineBasedFrameDecoder将提取的帧转发给下一个ChannelInboundHandler
  pipeline.addLast(new LineBasedFrameDecoder(64 * 1024));
  // 添加 FrameHandler 以接收帧
  pipeline.addLast(new FrameHandler());
  
  public static final class FrameHandler extends SimpleChannelInboundHandler<ByteBuf> { 	@Override
  	public void channelRead0(ChannelHandlerContext ctx, ByteBuf msg) throws Exception {
  		// Do something with the data extracted from the frame
  	} 
  }
  ```

  * 基于长度的协议通过将它的长度编码到帧的头部来定义帧，而不是使用特殊的分隔符来标记它的结束
  * `FixedLengthFrameDecoder`提取在调用构造函数时指定的定长帧
    * 根据指定的长度自动对消息进行解码，开发者不需要考虑TCP拆包/粘包问题
    * FixedLengthFrameDecoder extends ByteToMessageDecoder
      - A|BC|DEFG|HI ——code为3——ABC|DEF|GHI
      - `new FixedLengthFrameDecoder(20)`，服务端一次接收20个字符长度数据，多于的在下次中接收
  * `LengthFieldBasedFrameDecoder`根据编码进帧头部中的长度值提取帧;该字段的偏移量以及长度在构造函数中指定
    * 计算需要抽取的数据包长度、跳过字节逻辑处理、丢弃模式下的处理

  ```java
  /**
   * ---------------------
   *|   4    |  4  |  ?   |
   * ---------------------
   *| length | age | name |
   * ---------------------
   */
  public class Encoder extends MessageToByteEncoder<User> {
      @Override
      protected void encode(ChannelHandlerContext ctx, User user, ByteBuf out) throws Exception {
  			 //对象转为字节流并写入到底层
          byte[] bytes = user.getName().getBytes();
          out.writeInt(4 + bytes.length);
          out.writeInt(user.getAge());
          out.writeBytes(bytes);
      }
  }
  ```

* 写大型数据

  * `NIO`的零拷贝特性，这种特性消除了将文件的内容从文件系统移动到网络栈的复制过程

  * `Netty`使用一个`FileRegion`接口的实现，通过支持零拷贝的文件传输的`Channel`来发送的文件区域

    ```java
    //使用 FileRegion 传输文件的内容
    //只适用于文件内容的直接传输，不包括应用程序对数据的任何处理
    FileInputStream in = new FileInputStream(file); 
    FileRegion region = new DefaultFileRegion(in.getChannel(), 0, file.length()); channel.writeAndFlush(region).addListener(new ChannelFutureListener() {
    	@Override
    	public void operationComplete(ChannelFuture future)throws Exception {
    		if (!future.isSuccess()) {
    				Throwable cause = future.cause();
    				// Do something 处理失败
        }
    });
    ```

  * 在需将数据从文件系统复制到用户内存中时，可以使用`ChunkedWriteHandler`，它支持异步写大型数据流，而又不会导致大量的内存消耗

    ```java
    //使用 ChunkedStream 传输文件内容
    public class ChunkedWriteHandlerInitializer extends ChannelInitializer<Channel> { 		private final File file;
    	private final SslContext sslCtx;
    	public ChunkedWriteHandlerInitializer(File file, SslContext sslCtx) { 
        this.file = file;
    		this.sslCtx = sslCtx;
    	}
    	@Override
    	protected void initChannel(Channel ch) throws Exception {
    		ChannelPipeline pipeline = ch.pipeline(); 
        pipeline.addLast(new SslHandler(sslCtx.newEngine(ch.alloc()); 
    		//添加 ChunkedWriteHandler 以处理作为 ChunkedInput 传入的数据
    		pipeline.addLast(new ChunkedWriteHandler());                                         		//一旦连接建立，WriteStreamHandler就开始写文件数据
    		pipeline.addLast(new WriteStreamHandler());
    	}
    	public final class WriteStreamHandler extends ChannelInboundHandlerAdapter {
    		@Override
    		public void channelActive(ChannelHandlerContext ctx) throws Exception {
    			super.channelActive(ctx);
    			ctx.writeAndFlush(new ChunkedStream(new FileInputStream(file)));
    		} 
        }
    }
    ```

  * 序列化数据

    * JDK序列化

      * `ObjectDecoder`/`ObjectEncoder`构建于 JDK 序列化之上的使用自定义的序列化来解码/编码的解码器

    * JBoss Marshalling 进行序列化

      * 比JDK序列化最多快 3 倍，而且也更加紧凑

        * `MarshallingDecoder`  `MarshallingEncoder`

        ```java
        // 添加 MarshallingDecoder 以 将 ByteBuf 转换为 POJO
        pipeline.addLast(new MarshallingDecoder(unmarshallerProvider));
        // 添加 MarshallingEncoder 以将 POJO 转换为 ByteBuf
        pipeline.addLast(new MarshallingEncoder(marshallerProvider));
        // 添加 ObjectHandler， 以处理普通的实现了 Serializable 接口的 POJO
        pipeline.addLast(new ObjectHandler());
        ```

    * Protocol Buffers 序列化
      * 一种紧凑而高效的方式对结构化的数据进行编码以及解码
      * `ProtobufDecoder`/`ProtobufEncoder`使用 protobuf 对消息进行解码/编码

## 异常处理

* 入站异常

  - 如果在处理入站事件的过程中有异常被抛出，那么它将从它在`ChannelInboundHandler`里被触发的那一点开始流经 `ChannelPipeline`
  - 重写`exceptionCaught`处理入站异常
    1. 默认实现是简单地将当前异常转发给 `ChannelPipeline` 中的下一个 `ChannelHander`
    2. 如果异常到达了` ChannelPipeline`的尾端，它将会被记录为未被处理
    3. 重写`exceptionCaught()`方法自定义处理逻辑，需要决定是否需要将该异常传播出去

* 出站异常

  * 每个出站操作都将返回一个`ChannelFuture`

    * 注册到`ChannelFuture`的`ChannelFutureListener`将在操作完成时被通知该操作是成功还是出错

  * 几乎所有的`ChannelOutboundHandler` 上的方法都会传入一个`ChannelPromise` 的实例

    * 作为 `ChannelFuture` 的子类，`ChannelPromise` 也可被分配用于异步通知的监听器。但是，`ChannelPromise` 还具有提供立即通知的可写方法 `setSuccess()` `setFailure` 
    * 通过调用`ChannelPromise` 上的 `setSuccess()`和` setFailure()`方法，可以使一个操作的状态在 `ChannelHandler` 的方法返回给其调用者时便即刻被感知到

    ```java
    ChannelFuture future = channel.write(someMessage); 
    future.addListener(new ChannelFutureListener() {
    	@Override
    	public void operationComplete(ChannelFuture f) {
    		if (!f.isSuccess()) { 
            f.cause().printStackTrace();
    				f.channel().close();
    		}
    	}); 
    ```

    ```java
    public class OutboundExceptionHandler extends ChannelOutboundHandlerAdapter { 
    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) {
    	promise.addListener(new ChannelFutureListener() {
            @Override
            public void operationComplete(ChannelFuture f) {
                if (!f.isSuccess()) { 
                    f.cause().printStackTrace();
                    f.channel().close();
                }
            } 
        }); 
      }
    }
    ```

* 两大性能分析工具类
  * FastThreadLocal
    * 每个线程拿到对象后都是线程独享
    * 一个线程修改该对象后不影响其他线程
    * 创建时每个都带有各自的索引值，代表身份标识
  * Recycler轻量级对象池

# Demo

## 通信协议

* 协议

```java
//通信过程中 Java 对象的抽象类
@Data
public abstract class Packet {
    //协议版本
    private Byte version = 1;
    //指令,指令数据包都必须实现这个方法，这样就可知道某种指令的含义
    public abstract Byte getCommand();
}
```

```java
public interface Command {
    Byte LOGIN_REQUEST = 1;
    Byte LOGIN_RESPONSE = 2;
    Byte MESSAGE_REQUEST = 3;
    Byte MESSAGE_RESPONSE = 4;
}

//客户端登录请求为例，定义登录请求数据包
@Data
public class LoginRequestPacket extends Packet {
    private Integer userId;
    private String username;
    private String password;

    @Override
    public Byte getCommand() {
        return LOGIN_REQUEST;
    }
}

@Data
public class LoginResponsePacket extends Packet {
    private boolean success;
    private String reason;

    @Override
    public Byte getCommand() {
        return LOGIN_RESPONSE;
    }
}
```

```java
public class PacketCodeC {
    private static final int MAGIC_NUMBER = 0x12345678;
    private static final Map<Byte, Class<? extends Packet>> packetTypeMap;
    private static final Map<Byte, Serializer> serializerMap;

    static {
        packetTypeMap = new HashMap<>();
        packetTypeMap.put(LOGIN_REQUEST, LoginRequestPacket.class);

        serializerMap = new HashMap<>();
        Serializer serializer = new JSONSerializer();
        serializerMap.put(serializer.getSerializerAlogrithm(), serializer);
    }
		//编码：封装成二进制的过程
    public ByteBuf encode(Packet packet) {
        // 1. 创建 ByteBuf 对象
      	//ioBuffer()返回适配io读写相关的内存，它会尽可能创建一个直接内存（可以理解为不受 jvm 堆管理的内存空间，写到 IO 缓冲区的效果更高）
        ByteBuf byteBuf = ByteBufAllocator.DEFAULT.ioBuffer();
        // 2. 序列化 java 对象
        byte[] bytes = Serializer.DEFAULT.serialize(packet);
        // 3. 实际编码过程
        byteBuf.writeInt(MAGIC_NUMBER);
        byteBuf.writeByte(packet.getVersion());
        byteBuf.writeByte(Serializer.DEFAULT.getSerializerAlogrithm());
        byteBuf.writeByte(packet.getCommand());
        byteBuf.writeInt(bytes.length);
        byteBuf.writeBytes(bytes);
        return byteBuf;
    }
		//解码：解析 Java 对象的过程
    public Packet decode(ByteBuf byteBuf) {
        // 跳过 magic number
        byteBuf.skipBytes(4);
        // 跳过版本号
        byteBuf.skipBytes(1);
        // 序列化算法
        byte serializeAlgorithm = byteBuf.readByte();
        // 指令
        byte command = byteBuf.readByte();
        // 数据包长度
        int length = byteBuf.readInt();
        byte[] bytes = new byte[length];
        byteBuf.readBytes(bytes);
      
        Class<? extends Packet> requestType = getRequestType(command);
        Serializer serializer = getSerializer(serializeAlgorithm);
        if (requestType != null && serializer != null) {
            return serializer.deserialize(requestType, bytes);
        }
        return null;
    }

    private Serializer getSerializer(byte serializeAlgorithm) {
        return serializerMap.get(serializeAlgorithm);
    }

    private Class<? extends Packet> getRequestType(byte command) {
        return packetTypeMap.get(command);
    }
}
```

* 序列化

```java
//把一个 Java 对象转换成二进制数据
// 如果想要实现其他序列化算法的话，只需要继承一下 Serializer，然后定义一下序列化算法的标识，再覆盖一下两个方法即可
public interface Serializer {
  	//序列化算法的类型以及默认序列化算法
  	//json 序列化
    byte JSON_SERIALIZER = 1;
    Serializer DEFAULT = new JSONSerializer();
  
    //序列化算法， 获取具体的序列化算法标识
    byte getSerializerAlgorithm();
    //java 对象转换成二进制
    byte[] serialize(Object object);
    //二进制转换成 java 对象
    <T> T deserialize(Class<T> clazz, byte[] bytes);
}

public interface SerializerAlgorithm {
    //json 序列化标识
    byte JSON = 1;
}
```

```java
//使用最简单的 json 序列化方式，使用阿里巴巴的 fastjson 作为序列化框架
public class JSONSerializer implements Serializer {
    @Override
    public byte getSerializerAlgorithm() {
        return SerializerAlgorithm.JSON;
    } 

    @Override
    public byte[] serialize(Object object) {
        return JSON.toJSONBytes(object);
    }

    @Override
    public <T> T deserialize(Class<T> clazz, byte[] bytes) {
        return JSON.parseObject(bytes, clazz);
    }
}
```

## 客户端登录

* 客户端登录成功或者失败之后，如果把成功或者失败的标识绑定在客户端的连接上？服务端又是如何高效避免客户端重新登录？
  * 可以给客户端连接，也就是 Channel 绑定属性，通过 `channel.attr(xxx).set(xx)` 的方式，可在登录成功之后，给 Channel 绑定一个登录成功的标志位，然后判断是否登录成功的时候取出这个标志位就可以了

![image-20190623080206778](/Users/dingyuanjie/Documents/study/github/woodyprogram/img/image-20190623080206778.png)



```java
//定义一下是否登录成功的标志位
public interface Attributes {
    AttributeKey<Boolean> LOGIN = AttributeKey.newInstance("login");
}
```

```java
public class LoginUtil {
    public static void markAsLogin(Channel channel) {
        channel.attr(Attributes.LOGIN).set(true);
    }

    public static boolean hasLogin(Channel channel) {
        Attribute<Boolean> loginAttr = channel.attr(Attributes.LOGIN);
        return loginAttr.get() != null;
    }
}
```

* 

  ![image-20190623104514638](/Users/dingyuanjie/Documents/study/github/woodyprogram/img/image-20190623104514638.png)

  * 避免if else，优化：
    * 模块化处理，不同的逻辑放置到单独的类来处理，最后将这些逻辑串联起来，形成一个完整的逻辑处理链
    * Netty 中的 pipeline 和 channelHandler：它通过责任链设计模式来组织代码逻辑，并且能够支持逻辑的动态添加和删除 ，Netty 能够支持各类协议的扩展，比如 HTTP，Websocket，Redis
    * `ChannelInboundHandlerAdapter`的 `channelRead()` 方法里面，调用父类的 `channelRead()` 方法`super.channelRead(ctx, msg);`，而这里父类的 `channelRead()` 方法会自动调用到下一个 inBoundHandler 的 `channelRead()` 方法，并且会把当前 inBoundHandler 里处理完毕的对象传递到下一个 inBoundHandler，如传递的对象都是同一个 msg
      * `ctx.fireChannelRead(packet)`，解码后的对象传递到下一个 handler 处理
    * `ChannelOutboundHandlerAdapter`的 `write()` 方法里面调用父类的 `write()` 方法，而这里父类的 `write()` 方法会自动调用到下一个 outBoundHandler 的 `write()` 方法`super.write(ctx, msg, promise);`，并且会把当前 outBoundHandler 里处理完毕的对象传递到下一个 outBoundHandler
      * inboundA <-> inboundB <-> inboundC <-> outboundA <-> outboundB <-> outboundC
    * inBoundHandler 的执行顺序与实际的添加顺序相同，而 outBoundHandler 则相反

* git：构建客户端与服务端pipeline

  ![image-20190623112722309](/Users/dingyuanjie/Documents/study/github/woodyprogram/img/image-20190623112722309.png)

  * 基于 ByteToMessageDecoder，可实现自定义解码，而不用关心 ByteBuf 的强转和解码结果的传递
    * 无论是在客户端还是服务端，当收到数据之后，首先把二进制数据转换到一个 Java 对象
    * 继承 `ByteToMessageDecoder` 只需实现 `decode()` 方法， in传递进来时已是 ByteBuf 类型，所以不再需要强转，第三个参数是 `List` 类型，通过往这个 `List` 里面添加解码后的结果对象，就可以自动实现结果往下一个 handler 进行传递，这样就实现了解码的逻辑 handler
    *  Netty 里面的 ByteBuf，使用 `4.1.6.Final` 版本，默认情况下用的是堆外内存，需要自行手动释放，否则存在内存泄漏
    *  `ByteToMessageDecoder`，Netty 会自动进行内存的释放
  * 继承 `SimpleChannelInboundHandler<参数>`，可实现每一种指令的处理，不再需要强转，不再有冗长乏味的 `if else` 逻辑，不需要手动传递对象，自动实现类型判断和对象传递，可专注于处理所关心的指令即可
  * 继承 `MessageToByteEncoder<参数>`，可实现自定义编码，而不用关心 ByteBuf 的创建，不用每次向对端写 Java 对象都进行一次编码，它的功能就是将对象转换到二进制数据

## 拆包粘包解决方案

* 对于操作系统来说，只认 TCP 协议，尽管应用层是按照 ByteBuf 为单位来发送数据，但到了底层操作系统仍然是按照字节流发送数据，因此，数据到了服务端，也是按照字节流的方式读入，然后到了 Netty 应用层面，重新拼装成 ByteBuf，而这里的 ByteBuf 与客户端按顺序发送的 ByteBuf 可能是不对等的。因此，需要在客户端根据自定义协议来组装应用层的数据包，然后在服务端根据应用层的协议来组装数据包，这个过程通常在服务端称为拆包，而在客户端称为粘包

* 拆包器的作用就是根据自定义协议，把数据拼装成一个个符合自定义数据包大小的 ByteBuf，然后送到自定义协议解码器去解码

* Netty 自带的一些开箱即用的拆包器

  * 固定长度拆包器-FixedLengthFrameDecoder
    * 如果应用层协议非常简单，每个数据包的长度都是固定的，比如 100，那只需把这个拆包器加到 pipeline 中，Netty 会把一个个长度为 100 的数据包 (ByteBuf) 传递到下一个 channelHandler
  * 行拆包器-LineBasedFrameDecoer
    * 每个数据包之间以换行符作为分隔，接收端通过 LineBasedFrameDecoder 将粘过的 ByteBuf 拆分成一个个完整的应用层数据包
  * 分隔符拆包器-DelimiterBasedFrameDecoder
  * 基于长度域拆包器-LengthFieldBasedFrameDecoder
    * 最通用的一种拆包器，只要自定义协议中包含长度域字段，均可以使用这个拆包器来实现应用层拆包
    * 基于自定义协议，长度域相对整个数据包的偏移量为4+1+1+1=7，即数据长度开始位置，长度域的长度为4，即数据长度大小，非数据大小
    * 构造拆包器：`new LengthFieldBasedFrameDecoder(Integer.MAX_VALUE, 7, 4);`
      * 第一个参数指的是数据包的最大长度，第二个参数指的是长度域的偏移量，第三个参数指的是长度域的长度，只需在 pipeline 的最前面加上这个拆包器
      * 在后续进行 decode 操作的时候，ByteBuf 就是一个完整的自定义协议数据包

* 拒绝非本协议连接

  * 魔数的原因是为了尽早屏蔽非本协议的客户端

  * 每个客户端发过来的数据包都做一次快速判断是否满足自定义协议

  * 只需要继承自 LengthFieldBasedFrameDecoder 的 `decode()` 方法，然后在 decode 之前判断前四个字节是否是等于自定义的魔数 `0x12345678`

    ```java
    public class Spliter extends LengthFieldBasedFrameDecoder {
        private static final int LENGTH_FIELD_OFFSET = 7;
        private static final int LENGTH_FIELD_LENGTH = 4;
    
        public Spliter() {
            super(Integer.MAX_VALUE, LENGTH_FIELD_OFFSET, LENGTH_FIELD_LENGTH);
        }
    
      	//第二个参数 in，每次传递进来的时候，均为一个数据包的开头
        @Override
        protected Object decode(ChannelHandlerContext ctx, ByteBuf in) throws Exception {
            // 屏蔽非本协议的客户端
            if (in.getInt(in.readerIndex()) != PacketCodeC.MAGIC_NUMBER) {
                ctx.channel().close();
                return null;
            }
            return super.decode(ctx, in);
        }
    }
    ```

    ```java
    //ch.pipeline().addLast(new LengthFieldBasedFrameDecoder(Integer.MAX_VALUE, 7, 4));
    // 替换为
    ch.pipeline().addLast(new Spliter());
    ```

* 服务端和客户端的pipeline结构

  ![image-20190623152257830](/Users/dingyuanjie/Documents/study/github/woodyprogram/img/image-20190623152257830.png)

## ChannelHandler的生命周期

![image-20190623154000457](/Users/dingyuanjie/Documents/study/github/woodyprogram/img/image-20190623154000457.png)

* 基于 `ChannelInboundHandlerAdapter`回调方法的执行顺序为
  * 客户端连接到服务端时
    * handlerAdded() -> channelRegistered() -> channelActive() -> channelRead() -> channelReadComplete()
    * `handlerAdded()` ：当检测到新连接之后，调用 `ch.pipeline().addLast(new LifeCyCleTestHandler());` 之后的回调，表示在当前的 channel 中，已经成功添加了一个 handler 处理器。通常用在一些资源的申请。
    * `channelRegistered()`：当前的 channel 的所有的逻辑处理已经和某个 NIO 线程建立了绑定关系， Netty 使用了线程池的方式，只需从线程池里抓一个线程绑定在这个 channel 上即可，这里的 NIO 线程通常指的是 `NioEventLoop`
    * `channelActive()`：当 channel 的所有的业务逻辑链准备完毕（也就是说 channel 的 pipeline 中已经添加完所有的 handler）以及绑定好一个 NIO 线程之后，这条连接算是真正激活了，然后就会回调到此方法。 TCP 连接的建立，可统计单机的连接数，加一。可实现对客户端连接 ip 黑白名单的过滤
    * `channelRead()`：客户端向服务端发来数据，每次都会回调此方法，表示有数据可读。服务端根据自定义协议来进行拆包，其实就是在这个方法里面，每次读到一定的数据，都会累加到一个容器里面，然后判断是否能够拆出来一个完整的数据包，如果够的话就拆了之后，往下进行传递
    * `channelReadComplete()`：服务端每次读完一次完整的数据之后，回调该方法，表示数据读取完毕。每次向客户端写数据的时候，都通过 `writeAndFlush()` 的方法写并刷新到底层，其实这种方式不是特别高效，可在之前调用 `writeAndFlush()` 的地方都调用 `write()` 方法，然后在这个方面里面调用 `ctx.channel().flush()` 方法，相当于一个批量刷新的机制，当然，如果对性能要求没那么高，`writeAndFlush()` 足矣
  * 客户端关闭
    * channelInactive() -> channelUnregistered() -> handlerRemoved()
    * `channelInactive()`: 这条连接已经被关闭了，这条连接在 TCP 层面已经不再是 ESTABLISH 状态了。 TCP 连接的释放，统计单机的连接数，减一
    * `channelUnregistered()`: 与这条连接对应的 NIO 线程移除掉对这条连接的处理
    * `handlerRemoved()`：最后，给这条连接上添加的所有的业务逻辑处理器都给移除掉。通常用在一些资源的释放
* ChannelInitializer的实现原理
  * `ChannelInitializer` 定义了一个抽象的方法 `initChannel()`，自行实现，在服务端启动的流程里面的实现逻辑就是往 pipeline 里面塞自定义的 handler 链
  * `handlerAdded()` 和 `channelRegistered()` 方法，都会尝试去调用 `initChannel()` 方法，`initChannel()` 使用 `putIfAbsent()` 来防止 `initChannel()` 被调用多次
  * 在 `handlerAdded()` 方法被调用的时候，channel 其实已经和某个线程绑定上了，所以，就的应用程序来说，这里的 `channelRegistered()` 其实是多余的
    * 防止写了个类继承自 `ChannelInitializer`，然后覆盖掉了 `handlerAdded()` 方法，这样即使覆盖掉，在 `channelRegistered()` 方法里面还有机会再调一次 `initChannel()`，把自定义的 handler 都添加到 pipeline 中去

## 热插拔实现客户端身份校验

* 客户端连上服务端之后，即使没有进行登录校验，服务端在收到消息之后仍然会进行消息的处理，这个逻辑其实是有问题的
* 身份检验
  * 只需在后续所有的指令处理 handler 之前插入一个用户认证 handle
    * 在 `MessageRequestHandler` 之前插入了一个 `AuthHandler`来处理掉身份认证相关的逻辑，后续所有的 handler 都不用操心身份认证这个逻辑

* 移除校验逻辑

  * 只要连接未断开，客户端只要成功登录过，后续就不需要再进行客户端的身份校验

  ```java
  public class AuthHandler extends ChannelInboundHandlerAdapter {
    @Override
      public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
          if (!LoginUtil.hasLogin(ctx.channel())) {
              ctx.channel().close();
          } else {
              // 一行代码实现逻辑的删除，删除之后，这条客户端连接的逻辑链中就不再有这段逻辑了
              ctx.pipeline().remove(this);
            	////把读到的数据向下传递，传递给后续指令处理器
              super.channelRead(ctx, msg);
          }
      }
  
      @Override
      public void handlerRemoved(ChannelHandlerContext ctx) {
          if (LoginUtil.hasLogin(ctx.channel())) {
              System.out.println("当前连接登录验证完毕，无需再次验证, AuthHandler 被移除");
          } else {
              System.out.println("无登录验证，强制关闭连接!");
          }
      }
  }
  ```

## 客户端互聊

* 一对一单聊
  * 客户端启动之后，在控制台输入用户名，服务端随机分配一个 userId 给客户端（省去账号密码注册）
  * 当有两个客户端登录成功之后，在控制台输入`userId + 空格 + 消息`，这里的 userId 是消息接收方的标识， 消息接收方的控制台接着就会显示另外一个客户端发来的消息
    * A 要和 B 聊天，首先 A 和 B 需要与服务器建立连接，然后进行一次登录流程，服务端保存用户标识和 TCP 连接的映射关系
    * A 发消息给 B，首先需要将带有 B 标识的消息数据包发送到服务器，然后服务器从消息数据包中拿到 B 的标识，找到对应的 B 的连接，将消息发送给 B
* 定义一个会话类 `Session` 用户维持用户的登录信息，用户登录的时候绑定 Session 与 channel，用户登出或者断线的时候解绑 Session 与 channel
* 服务端处理消息的时候，通过消息接收方的标识，拿到消息接收方的 channel，调用 `writeAndFlush()` 将消息发送给消息接收方
* 群聊的发起与通知

## 成功启动服务端

```java
// 启动服务端直到成功
private static void bind(final ServerBootstrap serverBootstrap, final int port) {
    serverBootstrap.bind(port).addListener(new GenericFutureListener<Future<? super Void>>() {
        public void operationComplete(Future<? super Void> future) {
            if (future.isSuccess()) {
                System.out.println("端口[" + port + "]绑定成功!");
            } else {
                System.err.println("端口[" + port + "]绑定失败!");
                bind(serverBootstrap, port + 1);
            }
        }
    });
}
```

## 成功启动客户端

```java
// 启动客户端，通常情况下，连接建立失败不会立即重新连接，而是会通过一个指数退避的方式，比如每隔 1 秒、2 秒、4 秒、8 秒，以 2 的幂次来建立连接，然后到达一定次数之后就放弃连接，默认重试 5 次
connect(bootstrap, "juejin.im", 80, MAX_RETRY);
private static void connect(Bootstrap bootstrap, String host, int port, int retry) {
    bootstrap.connect(host, port).addListener(future -> {
        if (future.isSuccess()) {
            System.out.println("连接成功!");
        } else if (retry == 0) {
            System.err.println("重试次数已用完，放弃连接！");
        } else {
            // 第几次重连
            int order = (MAX_RETRY - retry) + 1;
            // 本次重连的间隔
            int delay = 1 << order;
            System.err.println(new Date() + ": 连接失败，第" + order + "次重连……");
            bootstrap.config().group().schedule(() -> connect(bootstrap, host, port, retry - 1), delay, TimeUnit
                    .SECONDS);
        }
    });
}
```

## 基于Netty实现RPC

```java
//api
public interface Hello {
    String sayHello(String name);
}
```

```java
//provider
public class HelloImpl implements Hello {
    @Override
    public String sayHello(String name) {
        return "hello, " + name;
    }
}
```

```java
//注册中心
public class RPCRegistry {
    private int port;
    public RPCRegistry(int port) {
        this.port = port;
    }
    public static void main(String[] args) {
        new RPCRegistry(8085).start();
    }
    
    public void start() {
		//NioEventLoopGroup bossGroup = new NioEventLoopGroup();
		//NioEventLoopGroup workerGroup = new NioEventLoopGroup();
        //NioEventLoopGroup 是用来处理I/O操作的多线程事件循环器
        //Netty提供了许多不同的EventLoopGroup的实现用来处理不同传输协议
        //创建boss线程组 ⽤于服务端接受客户端的连接
        //一旦‘boss’接收到连接，就会把连接信息注册到‘worker’上
        EventLoopGroup boosGroup = new NioEventLoopGroup();
        // 创建 worker 线程组 ⽤于进行已连接客户端的 SocketChannel 的数据读写
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        //如何知道多少个线程已经被使用，如何映射到已经创建的Channels上都需依赖于EventLoopGroup的实现，并且可以通过构造函数来配置他们的关系

        try {
            //ServerBootstrap 是一个启动NIO服务的辅助启动类
            ServerBootstrap server = new ServerBootstrap();
            server.group(boosGroup, workerGroup)
                	//设置要被实例化的为 NioServerSocketChannel 类
                    .channel(NioServerSocketChannel.class)
                	// 设置 NioServerSocketChannel 的处理器
					//.handler(new LoggingHandler(LogLevel.INFO))
                	//犯过的错
                	//.childHandler(new ChannelInitializer<ServerSocketChannel>() {
                	// 设置连入服务端的 Client 的 SocketChannel 的处理器
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) throws Exception {
                            ChannelPipeline pipeline = ch.pipeline();
                            // 添加帧限定符来防⽌止粘包现象
        					//pipeline.addLast(new DelimiterBasedFrameDecoder(8192,
                //Delimiters.lineDelimiter()));
//                            pipeline.addLast(new LengthFieldBasedFrameDecoder(Integer.MAX_VALUE, 0, 4, 0, 4));
//                            pipeline.addLast(new LengthFieldPrepender(4));
                            pipeline.addLast("encoder", new ObjectEncoder());
                            pipeline.addLast("decoder", new ObjectDecoder(Integer.MAX_VALUE, ClassResolvers.cacheDisabled(null)));
                            //业务逻辑
                            pipeline.addLast(new RegistryHandler());
                        }
                    })
                //SO_REUSEADDR,允许重复使用本地地址和端口
                //服务端处理客户端连接请求是顺序处理的，所以同一时间只能处理一个客户端连接，多个客户端来的时候，服务端将不能处理的客户端连接请求放在队列中等待处理，backlog参数指定了队列的大小
                    .option(ChannelOption.SO_BACKLOG, 128)
                //如果在两小时内没有数据的通信时，TCP会自动发送一个活动探测数据报文
                    .childOption(ChannelOption.SO_KEEPALIVE, true);
            // 绑定端口，并同步等待成功，即启动服务端
            ChannelFuture future = server.bind(this.port).sync();
            System.out.println("RPC Registry start listen at " + this.port);
            // 监听服务端关闭，并阻塞等待
            // 在这里阻塞，帮助服务端正常工作，如果监听到服务端要关闭事件，则执行完这段代码，把sync释放掉，服务端关闭，然后往下执行
            future.channel().closeFuture().sync();
        } catch (Exception e) {
            boosGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }
}
```

```java
//业务逻辑处理
//继承 SimpleChannelInboundHandler 类之后，会在接收到数据后会自动 release 掉数据占用的 Bytebuffer 资源。并且继承该类需要指定数据格式。 ⽽继承ChannelInboundHandlerAdapter 则不会⾃动释放，需要⼿动调用 ReferenceCountUtil.release()等⽅法进行释放。继承该类不需要指定数据格式
//推荐服务端继承 ChannelInboundHandlerAdapter ，⼿动进行释放，防⽌数据未处理完就⾃动释放了。而且服务端可能有多个客户端进行连接，并且每⼀个客户端请求的数据格式都不⼀致，这时便可以进行相应的处理
//客户端根据情况可以继承 SimpleChannelInboundHandler 类。好处是直接指定好传输的数据格式，就不需再进行格式的转换

//注解 Sharable 主要是为了多个handler可以被多个channel安全地共享，也就是保 证线程安全
 
@Sharable
public class RegistryHandler extends ChannelInboundHandlerAdapter {

    //注册中心容器
    public static ConcurrentHashMap<String, Object> registryMap = new ConcurrentHashMap<>();
    //缓存
    private List<String> classCache = new ArrayList<>();

    public RegistryHandler() {
        //扫描服务提供类
        scanerClass("com.woody.framework.netty.my.rpc.provider");
        //注册服务提供类到注册中心容器
        doRegister();
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        Object result = new Object();

        InvokeMsg request = (InvokeMsg) msg;

        if (registryMap.containsKey(request.getClassName())) {
            Object clazz = registryMap.get(request.getClassName());
            Method method = clazz.getClass().getMethod(request.getMethodName(), request.getParams());
            result = method.invoke(clazz, request.getValue());
        }

        ctx.writeAndFlush(result);
        //犯过的错
        ctx.close();
    }


    private void scanerClass(String packageName) {
        URL url = this.getClass().getClassLoader().getResource(packageName.replaceAll("\\.", "/"));
        File dir = new File(url.getFile());
        for (File file : dir.listFiles()) {
            //如果是个文件夹，则继续递归
            if (file.isDirectory()) {
                scanerClass(packageName + "." + file.getName());
            } else {
                //犯过的错
                //serviceCacheList.add(packages.replace(".class", ""));
                classCache.add(packageName + "." + file.getName().replace(".class", "").trim());
            }
        }
    }

    private void doRegister() {
        if (classCache.size() == 0) {
            return;
        }
        for (String className : classCache) {
            try {
                Class<?> clazz = Class.forName(className);
                Class<?> interfaces = clazz.getInterfaces()[0];
                registryMap.put(interfaces.getName(), clazz.newInstance());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }
}
```

```java
//协议
private String className;    //类名
private String methodName;  //函数名称
private Class<?>[] params;  //参数类型
//犯过的错
//private Class<?>[] value;
private Object[] value;  //参数列表
```

```java
//消费者
public class RPCConsumer {
    public static void main(String[] args) {
        Hello rpcHello = RPCProxy.create(Hello.class);
        System.out.println(rpcHello.sayHello("Woody"));
    }
}
```

```java
//代理
public class RPCProxy {
    public static <T> T create(Class<?> clazz) {
        MethodProxy methodProxy = new MethodProxy(clazz);
		//犯过的错，因为clazz本身就是接口 new Class<?>[]{clazz}
        //T result = (T) Proxy.newProxyInstance(clazz.getClassLoader(), clazz.getInterfaces(), methodProxy);
        T result = (T) Proxy.newProxyInstance(clazz.getClassLoader(), new Class[]{clazz}, methodProxy);
        return result;
    }
}

class MethodProxy implements InvocationHandler {
    private Class<?> clazz;
    public MethodProxy(Class<?> clazz) {
        this.clazz = clazz;
    }

    //代理，调用接口中每一个方法的时候，实际上就是发起一次网络请求
    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        //如果传进来的是一个已实现的具体类（略过此逻辑）
        //犯过的错
        //if(proxy.getClass().isInterface()) {
        if (Object.class.equals(method.getDeclaringClass())) {
            //犯过的错
            //return method.invoke(proxy, args);
            return method.invoke(this, args);
        } else {
            //传进来的是接口
            return rpcInvoke(proxy, method, args);
        }
    }

    private Object rpcInvoke(Object proxy, Method method, Object[] args) {
      	//构造请求消息
        InvokeMsg msg = new InvokeMsg();
        //犯过的错
        //protol.setClassName(method.getClass().getName());
        msg.setClassName(this.clazz.getName());
        msg.setMethodName(method.getName());
        msg.setParams(method.getParameterTypes());
        msg.setValue(args);
				
      	//发送并接收消息
        Object result = rpcRequest(requestMsg);
      	//返回结果
        return result;
    }
  	
  	private Object rpcRequest(InvokeMsg requestMsg) {
        final RPCProxyHandler proxyHandler = new RPCProxyHandler();

        EventLoopGroup eventLoopGroup = new NioEventLoopGroup();
        Bootstrap bootstrap = new Bootstrap();
        bootstrap.group(eventLoopGroup)
                .channel(NioSocketChannel.class)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel socketChannel) throws Exception {
                        ChannelPipeline pipeline = socketChannel.pipeline();
                      // pipeline.addLast(new LengthFieldBasedFrameDecoder(Integer.MAX_VALUE, 0, 4, 0, 4));
											//pipeline.addLast(new LengthFieldPrepender(4));
                        pipeline.addLast("encoder", new ObjectEncoder());
                        pipeline.addLast("decoder", new ObjectDecoder(Integer.MAX_VALUE, ClassResolvers.cacheDisabled(null)));
                        // 业务逻辑处理
                        pipeline.addLast("handler", proxyHandler);
                    }
                });
        try {
            ChannelFuture channelFuture = bootstrap.connect("localhost", 8081).sync();
            //犯过的错
            //future.channel().writeAndFlush(requestMsg);
            channelFuture.channel().writeAndFlush(requestMsg).sync();
            channelFuture.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            e.printStackTrace();
            logger.error("RPC调用失败！", e);
        } finally {
            eventLoopGroup.shutdownGracefully();
        }
        return proxyHandler.getResult();
    }
}
```

```java
//业务处理
public class RPCProxyHandler extends ChannelInboundHandlerAdapter {
 		private Logger logger = LoggerFactory.getLogger(RPCProxyHandler.class);
    private Object response;
    public Object getResponse() {
        return response;
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        response = msg;
        //犯过的错
        //ctx.close();
        System.out.println("Client 接收到服务器端返回的消息 " + msg);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
      System.out.println("client exception is general");
      logger.error("读取远程消息失败！", cause);
     	cause.printStackTrace();
      ctx.close();
    }
}
```

## Netty-SocketIO

* `socket.io`封装了`Websocket`以及其他的一些协议，并且实现了`Websocket`的服务端代码。同时还有很强的兼容性，兼容各种浏览器以及移动设备
* 是一个不错的websocket项目
* 支持实时、双向和基于事件的浏览器和服务器之间的通信

```java
public class NettySocketIOServer {

    public NettySocketIOServer() throws InterruptedException {
        //配置连接信息
        Configuration configuration = new Configuration();
        configuration.setHostname("127.0.0.1");
        configuration.setPort(8099);
				//初始化服务器
        SocketIOServer server = new SocketIOServer(configuration);
				//监听连接事件
        server.addConnectListener(new ConnectListener() {
            @Override
            public void onConnect(SocketIOClient client) {
                String token = client.getHandshakeData().getUrlParams().get("token").get(0);
                if (!StringUtils.isEmpty(token) && "1323sdfsfsdsdfsaf13".equals(token)) {
                    System.out.println("client sid :" + client.getSessionId() + " 成功连接服务器");
                } else {
                    client.disconnect();
                }

                String msg = "服务端Msg——1";
                client.sendEvent("ServerConnectedEvent", new AckCallback<String>(String.class) {
                    @Override
                    public void onSuccess(String result) {
                        System.out.println("服务端收到客户端的连接确认消息" + client.getSessionId() + " data: " + result);
                    }
                }, msg);
            }
        });
			
      	//final SocketIONamespace chat1namespace = server.addNamespace("/chat1");
      	// broadcast messages to all clients
				//chat1namespace.getBroadcastOperations().sendEvent("message", data);
      
        // 监听自定义事件
        server.addEventListener("HelloWorldEvent", Object.class, new DataListener<Object>() {
            @Override
            public void onData(SocketIOClient client, Object data, AckRequest ackSender) throws Exception {
                System.out.println("服务端接收来自客户端请求的信息 ： " + data.toString());
                if (ackSender.isAckRequested()) {
                    ackSender.sendAckData("服务端发送确认收到客户端HelloWorldEvent的信息" + data.toString());
                }
            }
        });

        //Ping事件
        server.addPingListener(new PingListener() {
            @Override
            public void onPing(SocketIOClient client) {
                System.out.println("Ping..." + client.getSessionId());
            }
        });

      	//监听断开事件
        server.addDisconnectListener(new DisconnectListener() {
            @Override
            public void onDisconnect(SocketIOClient client) {
                System.out.println("Hi " + client.getSessionId() + "已经断开连接");
            }
        });

        //开启服务器
        server.start();
        Thread.sleep(Integer.MAX_VALUE);
        server.stop();
    }

    public static void main(String[] args) throws InterruptedException {
        new NettySocketIOServer();
    }
}
```

```java
public class NettySocketIOClient {
    public NettySocketIOClient() throws URISyntaxException, InterruptedException {
        //客户端配置
        IO.Options options = new IO.Options();
        options.transports = new String[]{"websocket"};  //传输协议
        options.timeout = 5000;
        options.reconnectionAttempts = 50;
        options.reconnectionDelay = 500;

        //连接服务端
        Socket socket = IO.socket("http://127.0.0.1:8099?token=1323sdfsfsdsdfsaf13");

        //监听自定义事件
        socket.on("ServerConnectedEvent", new Emitter.Listener() {
            @Override
            public void call(Object... objects) {
                System.out.println("客户端收到服务端的消息（ServerConnectedEvent）" + objects[0].toString());

                Ack ack = (Ack) objects[objects.length - 1];
                ack.call("客户端确认收到服务端发送的连接成功信息" + objects[0].toString());
            }
        });
        
        // 客户端主动推送消息
        socket.emit("HelloWorldEvent", "客户端主动给服务端发送HelloWorldEvent事件消息", new Ack() {
            @Override
            public void call(Object... objects) {
                for (Object obj : objects) {
                    System.out.println("客户端收到服务端确认HelloWorldEvent事件消息" + obj.toString());
                }
            }
        });
        //重新连接到服务端
        socket.io().reconnection(true);
				//连接到服务端
        socket.connect();
//        Thread.sleep(6000);
//        socket.disconnect();
    }

    public static void main(String[] args) throws Exception {
        new NettySocketIOClient();
    }
}
```

## WebSocket聊天系统

## 问题

* 在服务端每隔一秒输出当前客户端的连接数，需要建立多个客户端
* 统计客户端的入口流量，以字节为单位
* 对于客户端在登录情况下发送消息以及客户端在未登录情况下发送消息，`AuthHandler` 的其他回调方法分别是如何执行的，为什么？
* 其实还少了用户登出请求和响应的指令处理，你是否能说出，对登出指令来说，服务端和客户端分别要干哪些事情？是否能够自行实现？
* 如何实现在某个客户端拉取群聊成员的时候，不需要输入自己的用户 ID，并且展示创建群聊消息的时候，不显示自己的昵称？
* 客户端加入或者退出群聊，将加入群聊的消息也通知到群聊中的其他客户端，这个消息需要和发起群聊的客户端区分开，类似 "xxx 加入群聊 yyy" 的格式
* 实现当一个群的人数为 0 的时候，清理掉内存中群相关的信息
* `IMIdleStateHandler` 能否实现为单例模式，为什么
* 如何实现客户端在断开连接之后自动连接并重新登录？
* 对于客户端在登录情况下发送消息以及客户端在未登录情况下发送消息，`AuthHandler` 的其他回调方法分别是如何执行的，为什么？