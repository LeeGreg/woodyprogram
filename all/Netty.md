# MS

- 默认情况下，Netty服务端会启动多个线程？何时启动？
- Netty如何解决空轮询bug问题？
- Netty如何保证异步串行无锁化？
- 对比Java标准NIO类库，你知道Netty是如何实现更高性能的吗?
  - Netty在基础的NIO等类库之上进行了很多改进
    - 更加优雅的Reactor模式实现、灵活的线程模型、利用EventLoop等创新性的机制，可以非常高效地管理成百上千的Channel
    - 充分利用了Java的Zero-Copy机制，降低内存分配和回收的开销
      - 例如使用池化的Direct Bufer等技术，在提高IO性能的同时，减少了对象的创建和销毁
      - 利用反射等技术直接操纵SelectionKey，使用数组而不是Java容器等
    - 在通信协议、序列化等其他角度的优化

![image-20190519115323107](/Users/dingyuanjie/Documents/study/github/woodyprogram/img/image-20190519115323107.png)

# Netty简介

* Netty 是一款异步事件驱动的网络应用程序框架，主要用于开发Java网络应用程序
  * 统一的 API，支持阻塞和非阻塞传输（封装了JDK底层BIO和NIO模型，提供高度可用的API）
  * 自带编码解码器解决拆包粘包问题，用户只用关心业务逻辑
  * 精心设计的Reactor线程模型支持高并发海量连接
  * 自带协议栈，无需用户关心（HTTP协议、WebSocket协议、自定义协议）
  * 链接逻辑组件以支持复用

- NIO，非阻塞IO/异步IO，是计算机操作系统对输入输出的一种处理方式：
  - 发起IO请求的线程不等IO操作完成，就继续执行随后的代码，IO结果用其他方式通知发起IO请求的程序
- BIO，阻塞IO/同步IO，
  - 发起IO请求的线程被阻塞直至IO操作完成后返回
- 作为一个异步非阻塞框架，Netty 的所有 IO 操作都是异步非阻塞的，通过 FutureListener机制，用户可以方便的主动获取或者通过通知机制获得 IO 操作结果
- 使用场景
  1. 互联网领域 ：构建高性能RPC框架基础通信框架，如`Dubbo`
  2. 大数据领域、游戏行业、企业软件、通信行业
- Reacotr设计模式
  - 定义：将并发服务请求提交到一个或多个服务处理程序的事件设计模式
    - 当请求抵达后，服务处理程序使用解多路分配策略，然后同步地派发这些请求至相关的请求处理程序
    - 单线程的，但是可以在多线程环境中存在
  - 结构
    - **资源：** 可供系统输入或输出的资源
    - **同步事件解多路器：** 使用一个事件循环 ，以阻止所有的资源，当可以启动一个同步操作上的资源不会阻塞，多路分解器发送资源到分发器
    - **分发器：** 处理请求程序的注册和注销，将资源分发到相关的处理程序
    - **请求处理器：** 应用程序定义的请求处理程序（同步调用）和相关资源
  - 优点：
    - 可完全分离程序特定代码，这意味着应用可分为模块化的，可复用的组件
    - 由于请求的处理程序是同步调用，可允许简单粗粒并发而不必添加多线程并发系统的复杂性
  - 限制：
    - 请求处理器只会被同步调用，限制最大并发数
    - 可扩展性不仅受限于请求处理器的同步调用，同时也受解多路器限制

# Netty架构图

- 核⼼部分，是底层的网络通⽤抽象和部分实现
  - 可拓展的事件模型。Netty 是基于事件模型的网络应⽤框架
  - 通用的通信 API 层。Netty 定义了一套抽象的通用通信层的 API 
  - ⽀持零拷贝特性的 Byte Buffer 实现
- 传输服务，具体的网络传输的定义与实现
  - Socket & Datagram ：TCP 和 UDP 的传输实现
  - HTTP Tunnel ：HTTP 通道的传输实现
  - In-VM Piple ：JVM 内部的传输实现
- 协议支持。
  - Netty 对于一些通⽤协议的编解码实现。例如:HTTP、WebSocket、Redis、DNS 等等

![image-20190420115756469](/Users/dingyuanjie/Library/Application Support/typora-user-images/image-20190420115756469.png)

# 核心组件

## 关联关系

- Netty 通过触发事件将 Selector 从应用程序中抽象出来，消除了所有本来将需要手动编写的派发代码。在内部，将会为每个 Channel 指定一个 EventLoop，用以处理所有事件：
  1. 注册感兴趣的事件
  2. 将事件派发给 ChannelHandler
  3. 安排进一步的动作

- 当⼀个连接到达时，Netty 就会创建⼀个 Channel，然后从 EventLoopGroup 中分配一个 EventLoop
  来给这个 Channel 绑定上，在该 Channel 的整个⽣命周期中都是由这个绑定的 EventLoop 来服务的（避免线程安全和同步问题）

  1. 一个 EventLoopGroup 包含一个或多个 EventLoop 
  2. ⼀个 EventLoop 在它的生命周期内，只能与⼀个 Thread 绑定，所有由 EventLoop 处理的 I/O 事件都将在它专有的 Thread 上被处理，从而保证线程安全
  3. 一个 Channel 在它的生命周期内只能注册到一个 EventLoop 上
  4. ⼀个 EventLoop 可被分配至⼀个或多个 Channel
  5. 在这种设计中，一个给定 Channel 的 I/O 操作都是由相同的 Thread 执行，实际上消除了对于同步的需要

  ==补充完流程，流程中添加上所有组件==
  
  ==补充完流程，流程中添加上所有组件==
  
  ==补充完流程，流程汇总添加所有组件== 
  
- 回调-逻辑

  - 在操作完成后通知相关方

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
  
- Future-通知

  - Future 提供了另一种在操作完成时通知应用程序的方式。

    - JDK的Future只允许手动检查对应的操作是否已经完成，或者一直阻塞直到它完成

    - Netty的ChannelFuture，用于在执行异步操作的时候使用

      - ChannelFuture— 异步通知（Netty 中所有的 I/O 操作都是异步非阻塞的）
      
      - Netty 提供了ChannelFuture接口，其addListener()方法注册了一个ChannelFutureListener，以
        便在某个操作完成时调用(无论是否成功)得到通知——监听器的回调方法operationComplete()
      
        - 可以将 ChannelFuture 看作是将来要执行的操作的结果的占位符。
        - 不能确定执行，但是肯定会被执行
        - 所有属于同一个 Channel 的操作将按顺序执行
        
      - 由ChannelFutureListener提供的通知机制消除了手动检查对应的操作是否完成的必要
      
        - 线程不用阻塞以等待对应的操作完成，所以它可以同时做其他的工作
      
      - 每个 Netty 的出站 I/O 操作都将返回一个 ChannelFuture（因为是异步通知，所以都不会阻塞）
      
        ```java
        //回调
        Channel channel = ...;
        //异步地连接 到远程节点
        ChannelFuture future = channel.connect( new InetSocketAddress("192.168.0.1", 25));
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

- 事件和ChannelHandler

  - Netty 使用不同的事件来通知我们状态的改变或者是操作的状态。

    - 这能够基于已经发生的事件来触发适当的动作：记录日志、数据转换、流控制、应用程序逻辑

  - Netty 是一个网络编程框架，所有事件是按照它们与入站或出站数据流的相关性进行分类的
  
    - 可能由`入站数据`或者相关的状态更改而触发的事件包括：
      1. 连接已被激活或者连接失活
      2. 数据读取
      3. 用户事件、错误事件
    - `出站事件`是未来将会触发的某个动作的操作结果
  1. 打开或者关闭到远程节点的连接
      2. 将数据写到或者冲刷到套接字
  
- 每个事件都可以被分发给 ChannelHandler 类中的某个用户实现的方法，将事件驱动范式直接转换为应用程序构件块
  
    ![image-20181025102529305](/Users/dingyuanjie/Desktop/MyKnowledge/2.code/java/2.%E5%92%95%E6%B3%A1%E5%AD%A6%E9%99%A2/04.%E5%A4%9A%E7%BA%BF%E7%A8%8B%E9%AB%98%E5%B9%B6%E5%8F%91/02.Netty/image-20181025102529305.png)
  
    - 在内部，ChannelHandler 自己也使用了事件和 Future，使得它们也成为了应用程序将使用的相同抽象的消费者

## EventLoop

- 定义了 Netty 的核心抽象，用于处理连接的生命周期中所发生的事件

- Netty 之所以能提供高性能网络通讯，其中一个原因是因为它使用 Reactor 线程模型

- 在 netty 中每个EventLoopGroup 本身是一个线程池，其中包含了自定义个数的 NioEventLoop，每个NioEventLoop 中会管理自己的一个 selector 选择器和监控选择器就绪事件的线程

- 在Netty 中客户端持有一个 EventLoopGroup 用来处理网络 IO操作

  - 当 Channel 是客户端通道 NioSocketChannel 时候，会注册到自己关联的 NioEventLoop 的 selector 选择器上，然后NioEventLoop 对应的线程会通过 select 命令监控感兴趣的网络读写事件。

- 在服务器端持有两个 EventLoopGroup，其中 boss组是专门用来接收客户端发来的 TCP 链接请求的，worker组是专门用来具体处理完成三次握手的链接套接字的网络IO 请求的

  - 当Channel是服务端通道NioServerSocketChannel 时候，NioServerSocketChannel本身会被注册到 boss EventLoopGroup 里面的某一个NioEventLoop 管理的 selector 选择器上，而完成三次握手的链接套接字是被注册到了 worker EventLoopGroup 里面的某一个 NioEventLoop 管理的 selector 选择器上

- 需要注意是，多个 Channel 可以注册到同一个 NioEventLoop管理的 selector 选择器上，这时候 NioEventLoop 对应的单个线程就可以处理多个 Channel 的就绪事件；但是每个Channel 只能注册到一个固定的 NioEventLoop 管理的selector 选择器上

- `线程模型`指定了操作系统、编程语言、框架或者应用程序的上下文中的线程管理的关键方面

- 基本的线程池化模式可以描述为：

  1. 从池的空闲线程列表中选择一个 Thread去运行一个已提交的任务(一个Runnable 的实现);
  2. 任务完成时将该 Thread 返回给该列表，使其可被重用
  3. 虽然池化和重用线程可以避免频繁创建和销毁线程，但不能消除线程上下文切换的开销

- EventLoop接口

  - 运行任务来处理在连接的生命周期内发生的事件是任何网络框架的基本功能，与之相应的编程上的构造通常被称为事件循环——EventLoop

    ```java
    //事件循环的基本思想，其中每个任务都是一个 Runnable 的实例
    //在事件循环中执行任务
    while (!terminated) {
    	List<Runnable> readyEvents = blockUntilEventsReady(); 
        for (Runnable ev: readyEvents) {
    			ev.run(); 
        }
    }
    ```

  - 根据配置和可用核心的不同，可能会创建多个 EventLoop 实例用以优化资源的使用，并且单个EventLoop 可能会被指派用于服务多个 Channel 

    - 一个EventLoop将由固定的Thread驱动，同时任务（Runnable或Callable）可以以立即执行或调度执行提交给EventLoop
    - 在Netty 4 中，所有的I/O操作和事件都由已经被分配给了EventLoop的那个Thread来处理

  - 事件和任务是以先进先出(FIFO)的顺序执行的。这样可以通过保证字节内容总是按正确的顺序被处理，消除潜在的数据损坏的可能性

  - 由 I/O 操作触发的事件将流经安装了一个或者多个ChannelHandler 的 ChannelPipeline。

    - 传播这些事件的方法调用可以随后被 ChannelHandler 所拦截，并且可以按需地处理事件

- 任务调度

  - 调度一个任务以便稍后(延迟)执行或者周期性地执行

  - JDK任务调度

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

  - EventLoop 调度任务

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

- 线程管理

  - Netty线程模型的卓越性能取决于对于当前执行的Thread是否是分配给当前Channel以及它的EventLoop的那一个线程 （一个Channel的整个生命周期内的所有事件都由一个EventLoop负责处理）
  - EventLoop的执行逻辑——Netty线程模型的关键组成部分
    - 把任务传递给EventLoop的execute方法后，执行检查以确定当前调用线程是否就是分配给EventLoop的那个线程。`Channel.eventLoop().execute(Task)`
      - 如果是，则在EventLoop中直接执行任务，否则将任务放入队列以便EventLoop下次处理它的事件时执行
      - 这也就解释了任何的 Thread 是如何 与 Channel 直接交互而无需在 ChannelHandler 中进行额外同步的
      - 每个EventLoop都有它自己的任务队列，独立于任何其他的EventLoop
  - 不要将一个需要长时间运行的任务放入执行队列，因为其将阻塞在同一线程上执行的其他任务。如有需要，建议使用一个专门的EventExecutor
  - 如同传输所采用的不同的事件处理实现一样，所使用的线程模型也可以强烈地影响到排队的任务对整体系统性能的影响

- EventLoop线程的分配

  - 服务于 Channel 的 I/O 和事件的 EventLoop 包含在 EventLoopGroup 中

    - 根据不同的传输实现，EventLoop 的创建和分配方式也不同

  - 非阻塞传输的EventLoop分配方式，如NIO和AIO

    - 所有EventLoop都由EventLoopGroup分配，每个EventLoop都和各自的一个Thread相关联并且处理分配给他的所有Channel的所有事件和任务
    - EventLoopGroup将为每个新创建的Channel分配一个EventLoop，在每个Channel的整个生命周期内的所有的操作都将由相同的Thread执行
      - 使得可以通过尽量少的线程来支撑大量的Channel，而不是每个Channel分配一个线程

    - EventLoop 通常会被用于支撑多个 Channel，所以对于所有相关联的 Channel 来说，ThreadLocal 都将是一样的。
    - 这使得它对于实现状态追踪等功能来说是个糟糕的选择。
      - 然而，在一些无状态的上下文中，它仍然可以被用于在多个 Channel 之间共享一些重度的或者代价昂贵的对象，甚至是事件
  
  - 阻塞传输
  
    - 每一个 Channel 都将被分配给一个 EventLoop(以及它的 Thread)

## Channel

- `Channel `是对` socket` 的装饰或者门面，其封装了对`socket` 的原子操作。
- 传入或者传出数据的载体，可以被打开或者被关闭，连接或者断开连接
  
- `Netty` 实现的客户端NIO 套接字通道是 `NioSocketChannel`
- 用来创建`SocketChannel` 实例和设置该实例的属性，并调用`Connect` 方法向服务端发起 TCP 链接等	
  
- 提供的服务器端NIO 套接字通道是 `NioServerSocketChannel`

  - 用来创建`ServerSocketChannel` 实例和设置该实例属性，并调用该实例的 `bind` 方法在指定端口监听客户端的链接

- `NioDatagramChannel`、`OioDatagramChannel`、`NioSocketChannel`、`OioServerSocketChannel`

- 每个` Channel` 都将会被分配一个 `ChannelPipeline` 和 `ChannelConfig`。

  - `ChannelConfig` 包含了该 `Channel` 的所有配置设置，并且支持热更新

- 由于 Channel 是独一无二的，所以为了保证顺序将 Channel 声明为 `java.lang.Comparable` 的一个子接口。因此，如果两个不同的 Channel 实例都返回了相同的散列码，那么 AbstractChannel 中的 compareTo()方法的实现将会抛出一个 Error

- Netty的Channel实现是线程安全的

  - Channel只注册到一个EventLoop，而EventLoop只和一个线程绑定
  - 因此可以存储一个到Channel的引用，并且每当需要向远程节点写数据时，都可以使用它，即使当时许多线程都在使用它，消息将会保证按顺序发送

- Channel的注册过程

  - 就是将 Channel 与对应的 EventLoop 关联
    -  因此这也体现了, 在 Netty 中, 每个 Channel 都会关联一个特定的 EventLoop, 并且这个 Channel中的所有 IO 操作都是在这个 EventLoop 中执行的; 
    - 当关联好 Channel 和 EventLoop 后, 会继续调用底层的 Java NIO SocketChannel 的 register 方法, 将底层的 Java NIOSocketChannel 注册到指定的 selector 中. 
    - 通过这两步, 就完成了 Netty Channel 的注册过程

- NIO-选择器

  - 选择器背后的基本概念是充当一个注册表，在那里将可以请求在 Channel 的状态发生变化时得到通知。可能的状态变化有：

    1. 新的 Channel 已被接受并且就绪
    2. Channel 连接已经完成
    3. Channel 有已经就绪的可供读取的数据
    4. Channel 可用于写数据
- 选择器运行在一个检查状态变化并对其做出相应响应的线程上，在应用程序对状态的改变做出响应之后，选择器将会被重置，并将重复这个过程
  - OP_ACCEPT，请求在接受新连接并创建Channel时获得通知
  - OP_CONNECT，请求在建立一个连接时获得通知
  - OP_READ，请求当数据已经就绪，可以从Channel中读取时获得通知
  - OP_WRITE，请求当可以向Channel中写更多的数据时获得通知（处理了套接字缓冲区被完全填满时的情况——通常发生在数据的发送速度比远程节点可处理的速度更快的时候）
  
* 选择并处理状态的变化
    * 新的Channel注册到选择器上
    * 选择器的select()将会阻塞直到接收到新的状态变化或者配置的超时时间已过时
      * 检查是否有状态变化
        * 没有则执行其他Task
        * 有则处理所有的状态变化
      * 在选择器运行的同一线程中执行其他任务
    
    
  
  ![image-20181031225619763](/Users/dingyuanjie/Desktop/MyKnowledge/2.code/java/2.%E5%92%95%E6%B3%A1%E5%AD%A6%E9%99%A2/04.%E5%A4%9A%E7%BA%BF%E7%A8%8B%E9%AB%98%E5%B9%B6%E5%8F%91/02.Netty/image-20181031225619763.png)

## ByteBuf

- 作用是通过Netty的管道传输数据，缓冲区传送数据都是通过Netty的ChannelPipeline和ChannelHandler

- 优点：

  - 容量可以按需增长，读和写使用了不同的索引，读写两种模式之间切换不需调用flip()
  - 通过内置的复合缓冲区类型实现了透明的零拷贝
  - 支持方法的链式调用及引用计数（能自动释放资源）
  - 支持池化
  - 可以被用户自定义的缓冲区类型扩展

- 如何工作的？

  - 维护读取（readIndex）和写入（writeIndex）两个索引
  - read或者write开头的ByteBuf 方法，将会推进其对应的索引，而以set或者get开头的操作则不会
  - 指定 ByteBuf 的最大容量，默认的限制是 Integer.MAX_VALUE。
  - 只有读写索引处于同一位置。然后ByteBuf就不可读了，如果继续读的话就会抛出IndexOutOfBoundsException异常，类似读数组越界

- 使用模式

  - 堆缓冲区（支撑数组）

    - 将数据存储在 JVM 的堆空间中，能在没有使用池化的情况下提供快速的分配和释放，非常适合于有遗留的数据需要处理的情况

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

  - 直接缓冲区

    - 为了避免在每次调用本地 I/O 操作之前(或者之后)将缓冲区的内容复制到一个中间缓冲区(或者从中间缓冲区把内容复制到缓冲区)

    - 直接缓冲区的内容将驻留在常规的会被垃圾回收的堆之外-直接缓冲区对于网络数据传输是理想的选择

    - 如果数据包含在一个在堆上分配的缓冲区中，那么事实上，在通过套接字发送它之前，JVM将会在内部把缓冲区复制到一个直接缓冲区中

    - 缺点：相对于基于堆的缓冲区，它们的分配和释放都较为昂贵；正在处理遗留代码，因为数据不是在堆上，所以不得不进行一次复制

      ```java
      //访问直接缓冲区的数据
      ByteBuf directBuf = ...;
      // 检查 ByteBuf 是否由数组支撑。如果不是，则这是一个直接缓冲区
      if (!directBuf.hasArray()) {
      	int length = directBuf.readableBytes();
      	byte[] array = new byte[length]; 
          //将字节复制到该数组
          directBuf.getBytes(directBuf.readerIndex(), array); 
          handleArray(array, 0, length);
      }
      ```

    - 直接缓冲区需要将数据复制到数组，如果想直接通过字节数组访问数据，用堆缓冲区更好一些

  - 复合缓冲区

    - 为多个 ByteBuf 提供一个聚合视图

    - CompositeByteBuf提供了一个将多个缓冲区表示为单个合并缓冲区的虚拟表示

    - 发送的消息往往只修改一部分，比如消息体一样，改变消息头。这里就不用每次都重新分配缓存了

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

    - CompositeByteBuf 可能不支持访问其支撑数组，因此访问 CompositeByteBuf 中的数据类似于(访问)直接缓冲区的模式

      ```java
      //访问 CompositeByteBuf 中的数据
      CompositeByteBuf compBuf = Unpooled.compositeBuffer(); 
      int length = compBuf.readableBytes();
      byte[] array = new byte[length]; 
      compBuf.getBytes(compBuf.readerIndex(), array); 
      handleArray(array, 0, array.length);
      ```

    - Netty使用了CompositeByteBuf来优化套接字的I/O操作，尽可能地消除了由JDK的缓冲区实现所导致的性能以及内存使用率的惩罚

- 字节级操作

  - 随机访问索引

    - ByteBuf的索引也是从0开始的，最后一个字节的位置是容量-1

      ```java
      ByteBuf buffer = ...;
      for (int i = 0; i < buffer.capacity(); i ++) {
      	byte b = buffer.getByte(i);
      	System.out.println((char) b);
      }
      ```

  - 顺序访问索引

    - ByteBuf 被它的两个索引划分成 3 个区域
    - 0-readerIndex：已被读取可丢弃的字节
    - readerIndex-writerIndex：可读字节
    - writerIndex-capacity：可写字节

  - 派生缓冲区

    - 即创建一个已经存在的缓冲区的视图

      - 数据共享

        - `duplicate();` `slice();` `slice(int, int)` `Unpooled.unmodifiableBuffer(...);`

          `order(ByteOrder);` `readSlice(int)`

        - 返回一个新的 ByteBuf 实例，具有自己的读索引、写索引和标记索引

      - 独立数据副本

        - `copy`或者`copy(int,int)`
        - 需要进行内存复制操作，不仅消耗更多资源，执行方法也会更耗时 

  - 其他操作

    - `isReadable()` `isWritable()` `readableBytes()`  `writableBytes()`  `capacity()` 

      `maxCapacity()`

    -  `hasArray()` 如果 ByteBuf 由一个字节数组支撑，则返回 true

    - `array()`如果 ByteBuf 由一个字节数组支撑则返回该数组;否则，它将抛出一个
      UnsupportedOperationException 异常

- ByteBufHolder

  - HTTP响应数据有很多属性，如果状态码，Cookie等等，而且它实际的内容都是以字节方式传输的存储各种属性值
  - ByteBufHolder 也为 Netty 的高级特性提供了支持，如缓冲区池化，其中可以从池中借用 ByteBuf，并且在需要时自动释放
  - ByteBufHolder 只有几种用于访问底层数据和引用计数的方法
    1. `content()` 返回由这个 ByteBufHolder 所持有的 ByteBuf
    2. `copy()`返回这个 ByteBufHolder 的一个深拷贝，包括一个其所包含的 ByteBuf 的非共享拷贝
    3. `duplicate()`返回这个ByteBufHolder的一个浅拷贝，包括一个其所包含的ByteBuf的共享拷贝
  - 如果想要实现一个将其有效负载存储在 ByteBuf 中的消息对象，那么 ByteBufHolder 将是个不错的选择

- 分配

  - 按需分配：ByteBufAllocator 接口

    - 为了降低分配和释放内存的开销，Netty 通过 interface `ByteBufAllocator `实现了(ByteBuf 的)池化，它可以用来分配所描述过的任意类型的 ByteBuf 实例

      1. `buffer()`返回一个基于堆或者直接内存存储的 ByteBuf
      2. `heapBuffer()`返回一个基于堆内存存储的 ByteBuf 
      3. `directBuffer()`返回一个基于直接内存存储的ByteBuf 
      4. `compositeBuffer()`返回一个可以通过添加最大到指定数目的基于堆的或者直接内存存储的缓冲区来扩展的CompositeByteBuf
      5. `ioBuffer()`返回一个用于套接字的 I/O 操作的 ByteBuf
    - 获取一个ByteBufAllocator的引用
      - `channel.alloc();`
      - `ChannelHandlerContext对象.alloc();`
    - 两种ByteBufAllocator的实现：

      1. `PooledByteBufAllocator`默认，池化了ByteBuf的实例以提高性能并最大限度地减少内存碎片
      2. `UnpooledByteBufAllocator`不池化，并且在每次它被调用时都会返回一个新的实例

  - Unpooled 缓冲区

    - Unpooled 工具类，提供了静态的辅助方法来创建未池化的 ByteBuf实例，没有使用池技术
    - `buffer()` `directBuffer()` `wrappedBuffer()` `copiedBuffer()`

  - ByteBufUtil 类

    - 提供了用于操作 ByteBuf 的静态的辅助方法

- 引用计数器

  - 某个对象所持有的资源不再被其他对象引用时，释放该对象所持有的资源

    - 可优化内存使用和性能的技术

  - 主要涉及跟踪到某个特定对象的活动引用的数量

    - 一个 ReferenceCounted 实现的实例将通常以活动的引用计数为 1 作为开始。
    - 只要引用计数大于 0，对象不会被释放。当活动引用的数量减少到 0 时，该实例就会被释放
  
  - 引用计数对于池化实现(如 PooledByteBufAllocator)来说是至关重要的，它降低了内存分配的开销
  
    ```java
    Channel channel = ...;
    ByteBufAllocator allocator = channel.alloc();
    ByteBuf buffer = allocator.directBuffer(); 
  //检查引用计数器是否为预期的1
    assert buffer.refCnt() == 1;
    ```
  
    ```java
    //减少到该对象的活动引用。当减少到 0 时 该对象被释放，并且该方法返回 true
  ByteBuf buffer = ...;
    boolean released = buffer.release();
    ```
  
  - 可以设想一个类，其 release()方法的实现总是将引用计数设为零，而不用关心它的当前值，从而一次性地使所有的活动引用都失效
  
  - 谁负责释放 ：一般来说，是由最后访问(引用计数)对象的那一方来负责将它释放

## 引导类

- 引导一个应用程序是指对它进行配置，并使它运行起来的过程

  - Netty处理引导的方式：使应用程序的逻辑或实现和网络层相隔离，无论它是客户端还是服务器

  - 将`ChannelPipeline`、`ChannelHandler `和 `EventLoop`这些部分组织起来，成为一个可实际运行的应用程序

- `AbstractBootStrap`类

  - `<interface> Cloneable` - `AbstractBootStrap` -` BootStrap/ServerBootStrap`

  - `服务器`：使用一个父 Channel 接受来自客户端的连接，并创建子 Channel 以用于它们之间的通信

  - `客户端`：使用一个单独的、没有父 Channel 的 Channel 来用于所有的网络交互

    ```java
    //AbstractBootstrap 类的完整声明
    //在这个签名中，子类型 B 是其父类型的一个类型参数，因此可以返回到运行时实例的引用以支持方法的链式调用
    public abstract class AbstractBootstrap <B extends AbstractBootstrap<B,C>,C extends Channel>
    //子类的声明
    public class Bootstrap extends AbstractBootstrap<Bootstrap,Channel>
    public class ServerBootstrap extends AbstractBootstrap<ServerBootstrap,ServerChannel>
    ```

  - 为什么引导类是 Cloneable 的?
    - 有时可能会需要创建多个具有类似配置或者完全相同配置的Channel。为了支持这种模式而又不需要为每个Channel都创建并配置一个新的引导类实例，AbstractBootstrap被标记为了 Cloneable。
    - 在一个已经配置完成的引导类实例上调用clone()方法将返回另一个可以立即使用的引导类实例 
    - 这种方式只会创建引导类实例的EventLoopGroup的一个浅拷贝，所以，被浅拷贝的EventLoopGroup将在所有克隆的Channel实例之间共享。
      - 这是可以接受的，因为通常这些克隆的Channel的生命周期都很短暂，一个典型的场景是——创建一个Channel以进行一次HTTP请求

- `BootStrap`

  - `Bootstrap` 类负责为客户端和使用无连接协议的应用程序创建 Channel
  - 用于客户端 ，连接到远程主机和端口；EventLoopGroup 的数目为1
    - Bootstrap类将会在bind()方法被调用后创建一个新的Channel，在这之后将会调用connect()方法以建立连接
    - 在connect()方法被调用后，Bootstrap类将会创建一个新的Channel

  ```java
  //引导一个客户端
  EventLoopGroup group = new NioEventLoopGroup(); 
  Bootstrap bootstrap = new Bootstrap(); 
  bootstrap.group(group)   //设置用于处理 Channel 所有事件的 EventLoopGroup
  .channel(NioSocketChannel.class)//指定了Channel的实现类
  //设置将被添加到 ChannelPipeline 以接收事件通知的ChannelHandler
  .handler(new SimpleChannelInboundHandler<ByteBuf>() {
  	@Override
  	protected void channeRead0(ChannelHandlerContext channelHandlerContext, ByteBuf byteBuf) throws Exception { 
          System.out.println("Received data");
      }
  });
  //连接到远程节点并返回一个 ChannelFuture，其将会在连接操作完成后接收到通知
  //在调用bind()或者connect()方法之前必须先调用group()、channel()、handler()设置所需的组件
  ChannelFuture future = bootstrap.connect(new InetSocketAddress("www.manning.com",80)); 
  future.addListener(new ChannelFutureListener() {
  	@Override
  	public void operationComplete(ChannelFuture channelFuture)throws Exception {
  		if (channelFuture.isSuccess()) {
  			System.out.println("Connection established"); 
      } else {
  			System.err.println("Connection attempt failed"); 
      } 
   }
  });
  ```

  - `option`设置`ChannelOption`，设置TCP参数（socket options），通过bind()或connect()方法设置到每个新建（之前已创建的Channel不会有效果）的Channel的ChannelConfig中

    - 可用的`ChannelOption`包括了底层连接的详细信息，如keep-alive 或者超时属性以及缓冲区设置

  - `attr`指定新创建的`Channel` 的属性值，在 `Channel` 被创建后将不会有任何的效果，通过bind()或者 connect()方法设置到 Channel

  - 像 Channel 这样的组件可能甚至会在正常的 Netty 生命周期之外被使用

    - 在某些常用的属性和数据不可用时，Netty 提供了`AttributeMap`抽象(一个由 Channel 和引导类提供的集合)以及` AttributeKey`<T>(一个用于插入和获取属性值的泛型类)。
    - 使用这些工具，便可以安全地将任何类型的数据项与客户端和服务器 Channel(包含 ServerChannel 的子 Channel)相关联了

    ```java
    final AttributeKey<Integer> id = new AttributeKey<Integer>("ID");
    bootstrap.option(ChannelOption.SO_KEEPALIVE,true)
    		     .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, 5000); 
    bootstrap.attr(id, 123456);
    
    Integer idValue = ctx.channel().attr(id).get();
    ```

  - `clone`创建一个当前 Bootstrap 的克隆，其具有和原始的Bootstrap 相同的设置信息 

  - `bind`绑定Channel并返回一个ChannelFuture，其将会在绑定操作完成后接收到通知，在那之后必须调用 Channel. connect()方法来建立连接

  - 无连接的协议

    - Netty 提供了各种`DatagramChannel`的实现。唯一区别就是，不再调用 connect()方法，而是只调用 bind()方法

      ```java
      Bootstrap bootstrap = new Bootstrap(); 
      bootstrap.group(new OioEventLoopGroup())
      .channel(OioDatagramChannel.class)
      .handler(new SimpleChannelInboundHandler<DatagramPacket>(){
      	@Override
      	public void channelRead0(ChannelHandlerContext ctx,DatagramPacket msg) throws Exception {
      		// Do something with the packet
      	}
      }); 
      //调用 bind()方法，因为该协议是无连接的
      ChannelFuture future = bootstrap.bind(new InetSocketAddress(0)); future.addListener(new ChannelFutureListener() {
      	@Override
      	public void operationComplete(ChannelFuture channelFuture)throws Exception {
      	if (channelFuture.isSuccess()) {
      		System.out.println("Channel bound"); 
        } else {
      		System.err.println("Bind attempt failed");
      		channelFuture.cause().printStackTrace(); 
       }
      }); 
      ```

- `ServerBootstrap` 

  - 用于服务器，绑定到一个本地端口；`EventLoopGroup`的数目为1或2
  - 服务器需要两组不同的`Channel`：
    - 第一组只包含一个`ServerChannel`，代表服务器自身的已绑定到某个本地端口的正在监听的套接字
    - 第二组包含所有已创建的用来处理传入客户端连接的 Channel
    - ServerBootstrap调用bind()方法将创建一个ServerChannel，当连接被接受时，ServerChannel将会创建一个新的子Channel
      - 与 ServerChannel 相关联的 EventLoopGroup 将分配一个EventLoop负责为传入连接请求创建Channel，一旦连接被接受，第二个 EventLoopGroup 就会给它的 Channel分配一个 EventLoop

  ```java
  //引导服务器
  NioEventLoopGroup group = new NioEventLoopGroup(); 
  ServerBootstrap bootstrap = new ServerBootstrap(); 
  bootstrap.group(group)
           .channel(NioServerSocketChannel.class)
  //设置将被添加到已被接受的子Channel的ChannelPipeline中的ChannelHandler,将由已被接受的子 Channel处理，其代表一个绑定到远程节点的套接字
           .childHandler(new SimpleChannelInboundHandler<ByteBuf>() {
  	           @Override
  	           protected void channelRead0(ChannelHandlerContext ctx,ByteBuf byteBuf) throws Exception { 
                   System.out.println("Received data");
  	          } 
            });
  ChannelFuture future = bootstrap.bind(new InetSocketAddress(8080)); future.addListener(new ChannelFutureListener() {
  	@Override
  	public void operationComplete(ChannelFuture channelFuture)throws Exception {
  		if (channelFuture.isSuccess()) {
  			System.out.println("Server bound"); 
      } else {
  			System.err.println("Bound attempt failed");
  			channelFuture.cause().printStackTrace();
  		}
  	}                                                                                 );
  ```

  - `childOption`指定当子 Channel 被接受时，应用到子 Channel 的 ChannelConfig 的ChannelOption
  - `childAttr`将属性设置给已经被接受的子 Channel。接下来的调用将不会有任何的效果

- 从 Channel 引导客户端

  - 编写 Netty 应用程序的一个一般准则：

    - 尽可能地重用 EventLoop，以减少线程创建所带来的开销

  - 服务器正在处理一个客户端的请求，这个请求需要它充当第三方系统的客户端。

    - 当一个应用程序(如一个代理服务器)必须要和组织现有的系统(如 Web 服务或者数据库)集成时，就可能发生这种情况。
    - 在这种情况下，将需要从已经被接受的子 Channel 中引导一个客户端 Channel
      - 通过Bootstrap为每个新创建的客户端 Channel 定义另一个 EventLoop。
        - 这会产生额外的线程，以及在已被接受的子 Channel 和客户端 Channel 之间交换数据时不可避免的上下文切换
    - 一个更好的解决方案是：通过将已被接受的子 Channel 的 EventLoop 传递给 Bootstrap的 group()方法来共享该 EventLoop。
        - 因为分配给 EventLoop 的所有 Channel 都使用同一个线程，所以这避免了额外的线程创建，以及相关的上下文切换

    ![image-20181030111616868](/Users/dingyuanjie/Desktop/MyKnowledge/2.code/java/2.%E5%92%95%E6%B3%A1%E5%AD%A6%E9%99%A2/04.%E5%A4%9A%E7%BA%BF%E7%A8%8B%E9%AB%98%E5%B9%B6%E5%8F%91/02.Netty/image-20181030111616868.png)
    
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

- 引导过程中添加多个 ChannelHandler

  - 一个必须要支持多种协议的应用程序将会有很多的ChannelHandler，而不会是一个庞大而又笨重的类
  - 通过在 ChannelPipeline 中将它们链接在一起来部署尽可能多的 ChannelHandler
  - Netty 提供了一个特殊的ChannelInboundHandlerAdapter子类ChannelInitializer<Channel>

  ```java
  public abstract class ChannelInitializer<C extends Channel> extends ChannelInboundHandlerAdapter
  //将多个 ChannelHandler 添加到一个 ChannelPipeline 中的简便方法
  protected abstract void initChannel(C ch) throws Exception;
  //一旦 Channel 被注册到了它的 EventLoop 之后，就会调用重写的 initChannel()版本。在该方法返回之后，ChannelInitializer 的实例将会从 ChannelPipeline 中移除它自己
  ```

  ```java
  //引导和使用 ChannelInitializer
  ...
  .childHandler(new ChannelInitializerImpl());
  
  final class ChannelInitializerImpl extends ChannelInitializer<Channel> {
      @Override
  	protected void initChannel(Channel ch) throws Exception { 
          ChannelPipeline pipeline = ch.pipeline(); 
          pipeline.addLast(new HttpClientCodec());
          pipeline.addLast(new HttpObjectAggregator(Integer.MAX_VALUE));
      }
  }
  ```

- 关闭

  - 调用`EventLoopGroup.shutdownGracefully()`方法关闭 `EventLoopGroup`，它将处理任何挂起的事件和任务，并且随后将释放所有的资源，并且关闭所有的当前正在使用中的 `Channel`

  - 这个方法调用将会返回一个`Future`，这个`Future`将在关闭完成时接收到通知

  - `shutdownGracefully()`方法也是一个异步的操作，所以需要阻塞等待直到它完成，或者向所返回的 Future 注册一个监听器以在关闭完成时获得通知

    ```java
    Future<?> future = group.shutdownGracefully();
    future.syncUninterruptibly();
    ```

- Netty 的引导类为应用程序的网络层配置提供了容器，这涉及将一个进程绑定到某个指定的端口，或者将一个进程连接到另一个运行在某个指定主机的指定端口上的进程

## ChannelHandler

- `handler`：处理服务端逻辑，比如handler添加了、handler注册上了

- `childHandler`：对于连接上的处理

- 典型用途：

  1. 将数据从一种格式转换为另一种格式
  2. 提供异常的通知
  3. 提供 Channel 变为活动的或者非活动的通知
  4. 提供当 Channel 注册到 EventLoop 或者从 EventLoop 注销时的通知
  5. 提供有关用户自定义事件的通知

- channel的生命周期

  - `ChannelRegistered` -> `ChannelActive` -> `ChannelInactive` -> `ChannelUnregistered`
  - 当这些状态发生变化时，将会生成对应的事件，这些事件将会被转发给`ChannelPipeline`中的`ChannelHandler`，其可以随后对它们作出响应

- 在`ChannelHandler`被添加到`ChannelPipeline` 中或者被从`ChannelPipeline`中移除时会调用这些操作。这些方法中的每一个都接受一个`ChannelHandlerContext` 参数

  - `handlerAdded` `handlerRemoved` `exceptionCaught`

- `Handler`的添加过程

  - Netty 的一个强大和灵活之处就是基于`Pipeline`的自定义`handler`机制. 
  - 基于此, 可以像添加插件一样自由组合各种各样的 handler 来完成业务逻辑.
  - 例如需要处理 HTTP 数据，那么就可以在 pipeline 前添加一个 Http 的编解码的 Handler, 然后接着添加自己的业务逻辑的 handler, 这样网络上的数据流就向通过一个管道一样, 从不同的 handler 中流过并进行编解码, 最终在到达自定义的 handler 中

- `ChannelHandler`子接口

  - ChannelHandler里面定义三个生命周期方法，`handlerAdded(ChannelHandlerContext)`、`handlerRemoved(ChannelHandlerContext)`、`exceptionCaught(ChannelHandlerContext)`，分别会在当前ChannelHander加入ChannelHandlerContext中，从ChannelHandlerContext中移除，以及ChannelHandler回调方法出现异常时被回调

  - ChannelInboundHandler——处理入站数据以及各种状态变化

    - 在数据被接收时或者与其对应的 Channel 状态发生改变时被调用

    - 回调方法及被触发的时机

      ![image-20181214225622121](/Users/dingyuanjie/Desktop/MyKnowledge/2.code/java/2.%E5%92%95%E6%B3%A1%E5%AD%A6%E9%99%A2/04.%E5%A4%9A%E7%BA%BF%E7%A8%8B%E9%AB%98%E5%B9%B6%E5%8F%91/02.Netty/image-20181214225622121-4799382.png)

    - 每个方法都带了ChannelHandlerContext作为参数，具体作用是：

      - 在每个回调事件里面，处理完成之后，使用ChannelHandlerContext的fireChannelXXX方法来传递给下个ChannelHandler，netty的code模块和业务处理代码分离就用到了这个链路处理

    - `channelRead`当从 Channel 读取数据时被调用，当某个 ChannelInboundHandler 的实现重写 channelRead()方法时，它将负责显式地释放与池化的 ByteBuf 实例相关的内存`ReferenceCountUtil.release(msg);`丢弃已接收的消息。

      - 当消息被`ctx.write(msg);`、`ctx.flush(); `后会自动释放

    - SimpleChannelInboundHandler的channelRead0会自动释放资源，消息被 channelRead0()方法消费之后自动释放消息

    - `ChannelWritabilityChanged` 

      `userEventTriggered`当 ChannelnboundHandler.fireUserEventTriggered()方法被调用时，因为一个 POJO 被传经了 ChannelPipeline

  - `ChannelOutboundHandler`——处理出站数据并且允许拦截所有的操作

    - 它的方法将被`Channel`、`ChannelPipeline` 以及 `ChannelHandlerContext` 调用

    - 一个强大的功能是可以按需推迟操作或者事件

      - 这使得可以通过一些复杂的方法来处理请求。例如，如果到远程节点的写入被暂停了，那么可以推迟冲刷操作并在稍后继续

      ![image-20181214225758377](/Users/dingyuanjie/Desktop/MyKnowledge/2.code/java/2.%E5%92%95%E6%B3%A1%E5%AD%A6%E9%99%A2/04.%E5%A4%9A%E7%BA%BF%E7%A8%8B%E9%AB%98%E5%B9%B6%E5%8F%91/02.Netty/image-20181214225758377-4799478.png)

    - `read` 当请求从`Channel` 读取更多的数据时被调用

    - `write`当请求通过`Channel` 将数据写到远程节点时被调用

    - `flush`当请求通过`Channel` 将入队数据冲刷到远程节点时被调用 
  
    - `ChannelOutboundHandler`中的大部分方法都需要一个`ChannelPromise`参数，以便在操作完成时得到通知
  
      - 可以调用它的`addListener`注册监听，当回调方法所对应的操作完成后，会触发这个监听
  
    - `ChannelPromise`是`ChannelFuture`的一个子类，其定义了一些可写的方法，如`setSuccess()`和`setFailure()`，从而使`ChannelFuture`不可变
  
      ```java
      public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
       ctx.write(msg,promise);
       System.out.println("out write");
       promise.addListener(new GenericFutureListener<Future<? super Void>>() {
           @Override
           public void operationComplete(Future<? super Void> future) throws Exception {
               if(future.isSuccess()){
                 System.out.println("OK");
      			} 
           }
       }); 
      }
      ```
  
  - `ChannelInboundHandler`和`ChannelOutboundHandler`的区别
    
    - 区别主要在于`ChannelInboundHandler`的`channelRead`和`channelReadComplete`回调和
      `ChannelOutboundHandler`的`write`和`flush`回调上
    - `ChannelInboundHandler`的`channelRead`回调负责执行入栈数据的`decode`逻辑，`ChannelOutboundHandler`的`write`负责执行出站数据的`encode`工作
    - 其他回调方法和具体触发逻辑有关，和in与out无关
    
  - `ChannelHandler` 适配器
    
    - 只需要简单地扩展它们，并且重写那些想要自定义的方法
    - ![image-20181029215620920](/Users/dingyuanjie/Desktop/MyKnowledge/2.code/java/2.%E5%92%95%E6%B3%A1%E5%AD%A6%E9%99%A2/04.%E5%A4%9A%E7%BA%BF%E7%A8%8B%E9%AB%98%E5%B9%B6%E5%8F%91/02.Netty/image-20181029215620920.png)
    - 在`ChannelInboundHandlerAdapter` 和` ChannelOutboundHandlerAdapter` 中所提供的方法体调用了其相关联的`ChannelHandlerContext` 上的等效方法，从而将事件转发到了`ChannelPipeline` 中的下一个 `ChannelHandler` 中
      - 通过`ChannelHandlerContext`将事件从一个`ChannelHandler`转发到`ChannelPipeline中`的下一个`ChannelHandler`
    - `ChannelInboundHandlerAdapter `实现了`ChannelInboundHandler`的所有方法，作用就是处理消息并将消息转发到`ChannelPipeline` 中的下一个 `ChannelHandler`
      - `ChannelInboundHandlerAdapter` 的 `channelRead` 方法处理完消息后不会自动释放消息，若想自动释放收到的消息，可以使用 `SimpleChannelInboundHandler`
    - `ChannelInitializer` 用来初始化`ChannelHandler`，将自定义的各种`ChannelHandler`添加到`ChannelPipeline`中
      - 在 Netty 中，从网络读取的 Inbound 消息，需要经过解码，将二进制的数据报转换成应用层协议消息或者业务消息，才能够被上层的应用逻辑识别和处理;同理，用户发送到网络的 Outbound 业务消息，需要经过编码转换成二进制字节数组(对于 Netty 就是 ByteBuf)才能够发送到网络对端。编码和解码功能是 NIO 框架的有机组成部分，无论是由业务定制扩展实现，还是 NIO 框架内置编解码能力，该功能是必不可少的
    - `ChannelHandlerAdapter` 还提供了实用方法` isSharable()`。如果其对应的实现被标注为 `Sharable`，那么这个方法将返回 true，表示它可以被添加到多个`ChannelPipeline`中
    - `@Sharable`
      - 为何要共享同一个`ChannelHandler` ?
      - 在多个`ChannelPipeline`中安装同一个`ChannelHandler`的一个常见的原因是用于收集跨越多个 `Channel` 的统计信息
      - 只应该在确定了ChannelHandler 是线程安全的时才使用@Sharable 注解
    
  - 资源管理
    - 诊断潜在的(资源泄漏)问题，Netty提供了class ResourceLeakDetector1，它将对应用程序的缓冲区分配做大约 1%的采样来检测内存泄露
    - ![image-20181029220036773](/Users/dingyuanjie/Desktop/MyKnowledge/2.code/java/2.%E5%92%95%E6%B3%A1%E5%AD%A6%E9%99%A2/04.%E5%A4%9A%E7%BA%BF%E7%A8%8B%E9%AB%98%E5%B9%B6%E5%8F%91/02.Netty/image-20181029220036773.png)
    - 不会通过调用 ChannelHandlerContext.fireChannelRead()方法将入站消息转发给下一个 ChannelInboundHandler
    - 如果一个消息被消费或者丢弃了，并且没有传递给 ChannelPipeline 中的下一个
      ChannelOutboundHandler，那么用户就有责任调用 ReferenceCountUtil.release()释放资源，还要通知 ChannelPromise。否则可能会出现 ChannelFutureListener 收不到某个消息已经被处理了的通知的情况
    
  - ChannelHandler，接口族的父接口，它的实现负责接收并响应事件通知，充当了所有处理入站和出站数据的应用程序逻辑的容器，其方法是由网络事件触发的
  
  - 有许多不同类型的 ChannelHandler，它们各自的功能主要取决于它们的超类
  
    - Netty 以适配器类的形式提供了大量默认的 ChannelHandler 实现，其旨在简化应用程序处理逻辑的开发过程。
  
  - 每个 Channel 都拥有一个与之相关联的 ChannelPipeline，其持有一个 ChannelHandler 的实例链。在默认的情况下，ChannelHandler 会把对它的方法的调用转发给链中的下一个 ChannelHandler
    - 针对不同类型的事件来调用 ChannelHandler
    - 应用程序通过实现或扩展ChannelHandler来挂钩到事件的生命周期，并提供自定义的应用程序逻辑
    - 在架构上，`ChannelHandler` 有助于保持业务逻辑与网络处理代码的分离
    
  - 服务器会响应传入的消息，所以它需要实现 ChannelInboundHandler 接口，用来接收入站事件和数据，要给连接的客户端发送响应时，也可以从ChannelInboundHandler冲刷数据。
  
  - channelRead()— 每当接收数据时，都会调用这个方法
    - 由服务器发送的消息可能会被分块接收，即使是对于这么少量的数据，channelRead0()方法也可能会被调用两次
    - 作为一个面向流的协议，TCP 保证了字节数组将会按照服务器发送它们的顺序被接收。
    
  - channelReadComplete()— 通知ChannelInboundHandler最后一次对channelRead()的调用是当前批量读取中的最后一条消息
  
  - exceptionCaught()—在读取操作期间，有异常抛出时会调用 
  
  - 在客户端，当 channelRead0()方法完成时，已经有了传入消息，并且已经处理完它了。当该方法返回时，SimpleChannelInboundHandler 负责释放指向保存该消息的 ByteBuf 的内存引用
  
  - 在 ChannelInboundHandler 中，仍然需要将传入消息回送给发送者，而 write()操作是异步的，直到 channelRead()方法返回后可能仍然没有完成。消息在 ChannelInboundHandler 的channelReadComplete()方法中，当 writeAndFlush()方法被调用时被释放
  
  - 为什么需要适配器类
    - 有一些适配器类可以将编写自定义的 ChannelHandler 所需要的努力降到最低限度，因为它们提
      供了定义在对应接口中的所有方法的默认实现
    - 编写自定义ChannelHandler时经常用到的适配器类；
      - ChannelHandlerAdapter
      - ChannelInboundHandlerAdapter
      - ChannelOutboundHandlerAdapter
      - ChannelDuplexHandler

## ChannelPipeline

- 在Channel创建的时候，会同时创建ChannelPipeline并相互关联，在ChannelPipeline中也会持有Channel的引用，ChannelPipeline 主要由一系列的 ChannelHandler 所组成，ChannelPipeline会维护一个ChannelHandlerContext的双向链表

- 添加的自定义ChannelHandler会插入到head和tail之间，如果是ChannelInboundHandler的回调，根据插入的顺序从左向右进行链式调用，ChannelOutboundHandler则相反

- 需要注意一点是虽然每个 Channel(更底层说是每个socket)有自己的 ChannelPipeline，但是每个ChannelPipeline 里面可以复用一个 ChannelHandler
  
- ChannelPipeline 实现了一种常见的设计模式— 拦截过滤器(InterceptingFilter)

  ![image-20181029221307994](/Users/dingyuanjie/Desktop/MyKnowledge/2.code/java/2.%E5%92%95%E6%B3%A1%E5%AD%A6%E9%99%A2/04.%E5%A4%9A%E7%BA%BF%E7%A8%8B%E9%AB%98%E5%B9%B6%E5%8F%91/02.Netty/image-20181029221307994.png)

- 根据事件的起源，事件将会被 ChannelInboundHandler 或者 ChannelOutboundHandler处理。随后，通过调用 ChannelHandlerContext 实现，它将被转发给同一超类型的下一个 ChannelHandler

  - 整条链式的调用是通过Channel接口的方法直接触发的

  - 如果使用ChannelContextHandler的接口方法间接触发，链路会从ChannelContextHandler对应的ChannelHandler开始，而不是从头或尾开始

- 在 ChannelPipeline 传播事件时，它会测试 ChannelPipeline 中的下一个 ChannelHandler 的类型是否和事件的运动方向相匹配。直到它找到和该事件所期望的方向相匹配的为止

- 通常 ChannelPipeline 中的每一个 ChannelHandler 都是通过它的 EventLoop(I/O 线程)来处理传递给它的事件的。所以至关重要的是不要阻塞这个线程，因为这会对整体的 I/O 处理产生负面的影响。

- ChannelPipeline 提供了 ChannelHandler 链的容器，并定义了用于在该链上传播入站和出站事件流的 API。

  1. 一个ChannelInitializer的实现被注册到了ServerBootstrap中
  2. 当 ChannelInitializer.initChannel()方法被调用时，ChannelInitializer将在 ChannelPipeline 中安装一组自定义的 ChannelHandler 
  3. ChannelInitializer 将它自己从 ChannelPipeline 中移除

  - ChannelHandler 是专为支持广泛的用途而设计的，可以将它看作是处理往来 ChannelPipeline 事件(包括数据)的任何代码的通用容器 
  - 使得事件流经 ChannelPipeline 是 ChannelHandler 的工作，它们是在应用程序的初始化或者引导阶段被安装的。这些对象接收事件、执行它们所实现的处理逻辑，并将数据传递给链中的下一个 ChannelHandler（这些适配器类(及它们的子类)将自动执行这个操作）。它们的执行顺序是由它们被添加的顺序所决定的。
  - 实际上，被我们称为 ChannelPipeline 的是这些 ChannelHandler 的编排顺序
  - 如果一个消息或者任何其他的入站事件被读取，那么它会从 ChannelPipeline 的头部开始流动，并被传递给第一个 ChannelInboundHandler。这个 ChannelHandler 不一定会实际地修改数据，具体取决于它的具体功能，在这之后，数据将会被传递给链中的下一个ChannelInboundHandler。最终，数据将会到达 ChannelPipeline 的尾端，届时，所有处理就都结束了
  - 数据的出站运动(即正在被写的数据)在概念上也是一样的。在这种情况下，数据将从ChannelOutboundHandler 链的尾端开始流动，直到它到达链的头部为止。在这之后，出站数据将会到达网络传输层

- ChannelHandlerContext

  - 每当有ChannelHandler添加到ChannelPipeline中时，都会创建对应的ChannelHandlerContext

    - ChannelPipeline实际维护的是ChannelHandlerContext 的关系，主要功能是管理它所关联的 ChannelHandler 和在同一个 ChannelPipeline 中的其他 ChannelHandler 之间的交互

    - 每个ChannelHandlerContext之间形成双向链表

    - 通过使用作为参数传递到每个方法的 ChannelHandlerContext，事件可以被传递给当前ChannelHandler 链中的下一个 ChannelHandler 

  - 虽然 ChannelInboundHandle 和ChannelOutboundHandle 都扩展自 ChannelHandler，但是 Netty 能区分 ChannelInboundHandler 实现和 ChannelOutboundHandler 实现，并确保数据只会在具有相同定
    向类型的两个 ChannelHandler 之间传递

  - 因为有时会忽略那些不感兴趣的事件，所以 Netty提供了抽象基类 ChannelInboundHandlerAdapter 和 ChannelOutboundHandlerAdapter。通过调用 ChannelHandlerContext 上的对应方法，每个都提供了简单地将事件传递给下一个 ChannelHandler的方法的实现。随后，可以通过重写所感兴趣的那些方法来扩展这些类

  - 当 ChannelHandler 被添加到 ChannelPipeline 时，它将会被分配一个 ChannelHandlerContext，其代表了 ChannelHandler 和 ChannelPipeline 之间的绑定。虽然这个对象可以被用于获取底层的Channel，但是它主要还是被用于写出站数据

    ![image-20181029223711787](/Users/dingyuanjie/Desktop/MyKnowledge/2.code/java/2.%E5%92%95%E6%B3%A1%E5%AD%A6%E9%99%A2/04.%E5%A4%9A%E7%BA%BF%E7%A8%8B%E9%AB%98%E5%B9%B6%E5%8F%91/02.Netty/image-20181029223711787.png)

    ![image-20181029223731772](/Users/dingyuanjie/Desktop/MyKnowledge/2.code/java/2.%E5%92%95%E6%B3%A1%E5%AD%A6%E9%99%A2/04.%E5%A4%9A%E7%BA%BF%E7%A8%8B%E9%AB%98%E5%B9%B6%E5%8F%91/02.Netty/image-20181029223731772.png)

    1. 每个Channel会绑定一个ChannelPipeline，ChannelPipeline中也会持有Channel的引用 
    2. ChannelPipeline持有ChannelHandlerContext链路，保留ChannelHandlerContext的头尾节点指针 
    3. 每个ChannelHandlerContext会对应一个ChannelHandler，也就相当于ChannelPipeline持有 

    ChannelHandler链路 

    4. ChannelHandlerContext同时也会持有ChannelPipeline引用，也就相当于持有Channel引用 
    5. ChannelHandler链路会根据Handler的类型，分为InBound和OutBound两条链路 

  - 具有丰富的用于处理事件和执行 I/O 操作的 API

    1. `alloc`返回和这个实例相关联的 Channel 所配置的 ByteBufAllocator
    2. `channel`返回绑定到这个实例的 Channel
    3. `handler`返回绑定到这个实例的 ChannelHandler
    4. `read ` 将数据从Channel读取到第一个入站缓冲区;如果读取成功则触发一个channelRead事件，并(在最后一个消息被读取完成后) 通知 ChannelInboundHandler 的 channelReadComplete (ChannelHandlerContext)方法 
    5. `write`通过这个实例写入消息并经过 ChannelPipeline
    6. `writeAndFlush`通过这个实例写入并冲刷消息并经过 ChannelPipeline

  - ChannelHandler可以通知其所属的ChannelPipeline中的下个 ChannelHandler，甚至可以动态修改它所属的ChannelPipeline中的ChannelHandler的编排

    - 通过将 ChannelHandler 添加到 ChannelPipeline 中来实现动态的协议切换
    - 缓存到 ChannelHandlerContext 的引用以供稍后使用

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

- HeadContext

  - 实现了ChannelOutboundHandler，ChannelInboundHandler这两个接口
  - 因为在头部，所以说HeadContext中关于in和out的回调方法都会触发关于ChannelInboundHandler
  - HeadContext的作用是进行一些前置操作，以及把事件传递到下一个ChannelHandlerContext
  - 在把这个事件传递给下一个ChannelHandler之前会回调ChannelHandler的handlerAdded方法而有关ChannelOutboundHandler接口的实现，会在链路的最后执行
  - 通过Channel接口执行write之后，会执行ChannelOutboundHandler链式调用，在链尾的HeadContext ，在通过unsafe回到对应Channel做相关调用

- TailContext

  - 实现了ChannelInboundHandler接口，会在ChannelInboundHandler调用链最后执行，只要是对调用链完成处理的情况进行处理
  - 自定义的最后一个ChannelInboundHandler，也把处理操作交给下一个ChannelHandler，那么就会到
    TailContext

- 如果要执行整个链路，必须通过调用Channel方法触发，ChannelHandlerContext引用了
  ChannelPipeline，所以也能间接操作channel的方法，但是会从当前ChannelHandlerContext绑定的
  ChannelHandler作为起点开始，而不是ChannelHandlerContext的头和尾 这个特性在不需要调用整个链路的情况下可以使用，可以增加一些效率

- 触发事件

  - ChannelPipeline的入站操作
    - `fireChannelRead`调用 ChannelPipeline 中下一个 ChannelInboundHandler 的
      channelRead(ChannelHandlerContext, Object msg)方法
    - `fire...`调用下一个ChannelHandler的相关方法
  - ChannelPipeline的出站操作
    - `bind` `connect` `disconnect` `close` `deregister` `flush` `write`  `writeAndFlush ` `read`

- 在Netty中，有两种发送消息的方式：

  1. 直接写到Channel中
     - 将会导致消息从 ChannelPipeline 的尾端开始流动
  2. 写到和ChannelHandler 相关联的 ChannelHandlerContext 对象中
     - 将导致消息从 ChannelPipeline 中的下一个 ChannelHandler 开始流动

## 编码器和解码器

编码器操作出站数据，而解码器处理入站数据

- 解码器

  1. 将字节解码为消息——ByteToMessageDecoder 和 ReplayingDecoder;
     * `ChannelInboundHandler`接口的实现
  2. 将一种消息类型解码为另一种——MessageToMessageDecoder

  - 什么时候会用到解码器呢?

    - 每当需要为 ChannelPipeline 中的下一个 ChannelInboundHandler 转换入站数据时会用到

  - `ByteToMessageDecoder`由于不可能知道远程节点是否会一次性地发送一个完整的消息，所以这个类会对入站数据进行缓冲，直到它准备好处理

    - ByteToMessageDecoder会一直调用decode()直到数据都读完

    ```java
    //对这个方法的调用将会重复进行，直到确定没有新的元素被添加到该 List，或者该 ByteBuf 中没有更多可读取的字节时为止。然后，如果该 List 不为空，那么它的内容将会被传递给 ChannelPipeline 中的下一个 ChannelInboundHandler    
    decode( ChannelHandlerContextctx, ByteBuf in,List<Object> out)
    ```

    ```java
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

  - 编解码器中的引用计数`retain`

    - 一旦消息被编码或者解码，它就会被 ReferenceCountUtil.release(message)调用自动释放。
    - 如果需要保留引用以便稍后使用，那么可以调用 ReferenceCountUtil.retain(message)方法。这将会增加该引用计数，从而防止该消息被释放

  - ` ReplayingDecoder`不必调用`readableBytes()`方法，它通过使用一个自定义的`ByteBuf`实现，`ReplayingDecoderByteBuf`，包装传入的`ByteBuf`实现了这一点，其将在内部执行该调用

    ```java
    //类型参数 S 指定了用于状态管理的类型，其中 Void 代表不需要状态管理
    public class ToIntegerDecoder2 extends ReplayingDecoder<Void> { 
        // 传入的 ByteBuf 是 ReplayingDecoderByteBuf
        @Override
    	public void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
            //从入站ByteBuf中读取一个 int，并将其添加到 解码消息的 List 中
                 out.add(in.readInt());
             }
    }
    ```

  - 如果使用`ByteToMessageDecoder` 不会引入太多的复杂性，那么请使用它；否则，请使用 `ReplayingDecoder`

    - `LineBasedFrameDecoder`使用了行尾控制字符(\n 或者\r\n)来解析消息数据
    - `HttpObjectDecoder` HTTP 数据的解码器

  - `MessageToMessageDecoder`在两个消息格式之间进行转换

    ```java
    //对于每个需要被解码为另一种格式的入站消息来说，该方法都将会被调用。解码消息随后会被传递给ChannelPipeline 中的下一个 ChannelInboundHandler
    decode(ChannelHandlerContext ctx, I msg,List<Object> out)
    ```

    ```java
    //对于每个需要被解码为另一种格式的入站消息来说，该方法都将会被调用。解码消息随后会被传递给ChannelPipeline 中的下一个 ChannelInboundHandler
    public class IntegerToStringDecoder extends MessageToMessageDecoder<Integer> {
    	@Override
    	public void decode(ChannelHandlerContext ctx, Integer msg,List<Object> out) throws Exception {
            // 将Integer消息转换为它的String表示，并将其添加到输出的 List 中
            out.add(String.valueOf(msg));
        }
    }
    ```

  - 由于 Netty 是一个异步框架，所以需要在字节可以解码之前在内存中缓冲它们。因此，不能让解码器缓冲大量的数据以至于耗尽可用的内存

  - `TooLongFrameException`其将由解码器在帧超出指定的大小限制时抛出。

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

- 编码器

  - 实现了 ChannelOutboundHandler，并将出站数据从一种格式转换为另一种格式
    1. 将消息编码为字节
    2. 将消息编码为消息 

  - `MessageToByteEncoder`

    ```java
    //被调用时将会传入要被该类编码为 ByteBuf 的(类型为I的)出站消息。该 ByteBuf 随后将会被转发给 ChannelPipeline 中的下一个 ChannelOutboundHandler
    encode(ChannelHandlerContext ctx, I msg,ByteBuf out)
    ```

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

  - ` MessageToMessageEncoder`出站数据从一种消息编码为另一种

    ```java
    //每个通过 write()方法写入的消息都将会被传递给 encode()方法，以编码为一个或者多个出站消息。随后，这些出站消息将会被转发给ChannelPipeline中的下一个ChannelOutboundHandler
    encode(ChannelHandlerContext ctx, I msg,List<Object> out)  
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

- 抽象的编解码器类

  - `ByteToMessageCodec`要将字节解码为某种形式的消息，可能是 POJO，随后再次对它进行编码

    ```java
    //只要有字节可以被消费，这个方法就将会被调用。它将入站 ByteBuf 转换为指定的消息格式，并将其转发给 ChannelPipeline 中的下一个 ChannelInboundHandler
    decode(ChannelHandlerContext ctx, ByteBuf in,List<Object>)
        
    //这个方法的默认实现委托给了 decode()方法。它只会在 Channel 的状态变为非活动时被调用一次。它可以被重写以实现特殊的处理
    decodeLast( ChannelHandlerContext ctx, ByteBuf in,List<Object> out)    
        
    //对于每个将被编码并写入出站 ByteBuf 的(类型为 I 的) 消息来说，这个方法都将会被调用
    encode(ChannelHandlerContext ctx, I msg,ByteBuf out)  
    ```

  - `MessageToMessageCodec`一种消息格式转换为另外一种消息格式的往返过程

    ```java
    //被调用时会被传入 INBOUND_IN 类型的消息(通过网络发送的类型)。 它将把它们解码为 OUTBOUND_IN 类型的消息(应用程序所处理的类型)，这些消息将被转发给 ChannelPipeline 中的下一个 ChannelInboundHandler
    protected abstract decode(ChannelHandlerContext ctx,INBOUND_IN msg,List<Object> out)
        
    //对于每个 OUTBOUND_IN 类型的消息，这个方法都将会被调用。这些消息将会被编码为 INBOUND_IN 类型的消 息，然后被转发给 ChannelPipeline 中的下一个 ChannelOutboundHandler
    protected abstract encode(ChannelHandlerContext ctx,OUTBOUND_IN msg,List<Object> out)
    ```

  - `CombinedChannelDuplexHandler`充当了`ChannelInboundHandler` 和 `ChannelOutboundHandler`的容器

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

- 网络数据总是一系列的字节，所有由`Netty` 提供的编码器/解码器适配器类都实现了 `ChannelOutboundHandler` 或者 `ChannelInboundHandler` 接口
- 对于入站数据来说，channelRead 方法/事件已经被重写了。对于每个从入站Channel 读取的消息，这个方法都将会被调用。随后，它将调用由预置解码器所提供的decode()方法，并将已解码的字节转发给 ChannelPipeline 中的下一个 ChannelInboundHandler
- 出站消息的模式是相反方向的：编码器将消息转换为字节，并将它们转发给下一个`ChannelOutboundHandler`
- `SimpleChannelInboundHandler`
  
  - 应用程序会利用一个`ChannelHandler` 来接收解码消息，并对该数据应用业务逻辑
  - 要创建一个这样的 `ChannelHandler`，只需要扩展基类 `SimpleChannelInboundHandler<T>`，其中 T 是要处理的消息的 `Java` 类型
  - 在这个`ChannelHandler`中，将需要重写基类的一个或者多个方法，并且获取到一个到`ChannelHandlerContext`的引用，这个引用将作为输入参数传递给`ChannelHandler` 的所有方法

## 预置的 ChannelHandler和编解码器

- SSL/TLS

  - 为了支持 SSL/TLS，Java 提供了 javax.net.ssl 包，它的 SSLContext 和 SSLEngine类使得实现解密和加密相当简单直接。

    - Netty 通过一个名为`SslHandler` 的 ChannelHandler实现利用了这个 API，其中 SslHandler 在内部使用 SSLEngine 来完成实际的工作

    ![image-20181030215807745](/Users/dingyuanjie/Desktop/MyKnowledge/2.code/java/2.%E5%92%95%E6%B3%A1%E5%AD%A6%E9%99%A2/04.%E5%A4%9A%E7%BA%BF%E7%A8%8B%E9%AB%98%E5%B9%B6%E5%8F%91/02.Netty/image-20181030215807745.png)
    
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

- HTTP 解码器、编码器和编解码器

  - HTTP 是基于请求/响应模式：客户端向服务器发送一个 HTTP 请求，然后服务器将会返回一个 HTTP 响应

  - 一个 HTTP 请求/响应可能由多个数据部分组成，HttpRequeset/HttpResponse、多个HttpContent、并且它总是以一个 LastHttpContent 部分作为结束

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

- 聚合 HTTP 消息

  - 由于 HTTP 的请求和响应可能由许多部分组成，因此需要聚合它们以形成完整的消息
  - Netty 提供了一个聚合器，它可以将多个消息部分合并为`FullHttpRequest` 或者`FullHttpResponse` 消息。通过这样的方式，将总是看到完整的消息内容
  - 由于消息分段需要被缓冲，直到可以转发一个完整的消息给下一个`ChannelInboundHandler`，所以这个操作有轻微的开销。其所带来的好处便是不必关心消息碎片了
  - 引入这种自动聚合机制只不过是向 ChannelPipeline 中添加另外一个 ChannelHandler罢了

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

- HTTP 压缩

  - 当使用 HTTP 时，建议开启压缩功能以尽可能多地减小传输数据的大小
  - Netty 为压缩和解压缩提供了 ChannelHandler 实现，它们同时支持 gzip 和 deflate 编码

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

- 使用 `HTTPS`

  - 启用`HTTPS` 只需要将`SslHandler` 添加到`ChannelPipeline` 的`ChannelHandler` 组合

- `WebSocket`

  - `WebSocket`提供了在一个单个的`TCP`连接上提供双向的通信，结合`WebSocket API`，它为网页和远程服务器之间的双向通信提供了一种替代HTTP轮询的方案
  - 应用程序中添加对于 `WebSocket` 的支持，需要将适当的客户端或者服务器`WebSocket` ChannelHandler 添加到 ChannelPipeline 中。这个类将处理由 WebSocket 定义的称为帧的特殊消息类型。`WebSocketFrame` 可以被归类为数据帧或者控制帧
  - 要想为 WebSocket 添加安全性，只需将 SslHandler 作为第一个ChannelHandler 添加到 ChannelPipeline 中
- 客户端通过`HTTP(S)`向服务器发起`WebSocket`握手，并等待确认，服务端同意后，连接协议升级到`WebSocket`
  
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

- 空闲的连接和超时

  - `IdleStateHandler`当连接空闲时间太长时，将会触发一个`IdleStateEvent` 事件

    - 然后，可以通过在ChannelInboundHandler 中重写 userEventTriggered()方法来处理该 IdleStateEvent 事件

  - `ReadTimeoutHandler`如果在指定的时间间隔内没有收到任何的入站数据，则抛出一个 `ReadTimeoutException` 并关闭对应的`Channel`。可以通过重写`ChannelHandler` 中的 `exceptionCaught()`方法来检测该 `ReadTimeoutException`

  - `WriteTimeoutHandler`如果在指定的时间间隔内没有任何出站数据写入，则抛出一个 `WriteTimeoutException` 并关闭对应的 `Channel`。可以通过重写`ChannelHandler`的 `exceptionCaught()`方法检测该 `WriteTimeoutException`

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

- 解码基于分隔符的协议和基于长度的协议

  - 基于分隔符的消息协议使用定义的字符来标记的消息或者消息段(通常被称为帧)的开头或者结尾

  - `DelimiterBasedFrameDecoder`使用任何由用户提供的分隔符来提取帧的通用解码器

  - `LineBasedFrameDecoder`提取由行尾符(\n或\r\n)分隔的帧的解码器，比`DelimiterBasedFrameDecoder`更快

    - 由尾行符分隔的帧，字节流：`ABC\r\nDEF\r\n`—帧：`ABC\r\n`（第一帧）、`DEF\r\n`（第二帧）

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

  - 基于长度的协议通过将它的长度编码到帧的头部来定义帧，而不是使用特殊的分隔符来标记它的结束

  - `FixedLengthFrameDecoder`提取在调用构造函数时指定的定长帧

  - `LengthFieldBasedFrameDecoder`根据编码进帧头部中的长度值提取帧;该字段的偏移量以及长度在构造函数中指定

- 写大型数据

  - `NIO`的零拷贝特性，这种特性消除了将文件的内容从文件系统移动到网络栈的复制过程

  - `Netty`使用一个`FileRegion`接口的实现，通过支持零拷贝的文件传输的`Channel`来发送的文件区域

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

  - 在需将数据从文件系统复制到用户内存中时，可以使用`ChunkedWriteHandler`，它支持异步写大型数据流，而又不会导致大量的内存消耗

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
    		pipeline.addLast(new ChunkedWriteHandler());                                         //一旦连接建立，WriteStreamHandler就开始写文件数据
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

- 序列化数据

  - JDK序列化

    - `ObjectDecoder`/`ObjectEncoder`构建于 JDK 序列化之上的使用自定义的序列化来解码/编码的解码器
  
- JBoss Marshalling 进行序列化
  
  - 比JDK序列化最多快 3 倍，而且也更加紧凑
    - `MarshallingDecoder`  `MarshallingEncoder`
  
  ```java
    // 添加 MarshallingDecoder 以 将 ByteBuf 转换为 POJO
    pipeline.addLast(new MarshallingDecoder(unmarshallerProvider));
    // 添加 MarshallingEncoder 以将 POJO 转换为 ByteBuf
    pipeline.addLast(new MarshallingEncoder(marshallerProvider));
    // 添加 ObjectHandler， 以处理普通的实现了 Serializable 接口的 POJO
    pipeline.addLast(new ObjectHandler());
  ```
  
- Protocol Buffers 序列化
    - 一种紧凑而高效的方式对结构化的数据进行编码以及解码
    - `ProtobufDecoder`/`ProtobufEncoder`使用 protobuf 对消息进行解码/编码

## 异常处理

- 入站异常

  - 如果在处理入站事件的过程中有异常被抛出，那么它将从它在`ChannelInboundHandler`里被触发的那一点开始流经 `ChannelPipeline`
  - 重写`exceptionCaught`处理入站异常
    1. 默认实现是简单地将当前异常转发给 `ChannelPipeline` 中的下一个 `ChannelHander`
    2. 如果异常到达了` ChannelPipeline`的尾端，它将会被记录为未被处理
    3. 重写`exceptionCaught()`方法自定义处理逻辑，需要决定是否需要将该异常传播出去

- 出站异常

  1. 每个出站操作都将返回一个`ChannelFuture`。注册到`ChannelFuture`的`ChannelFutureListener`将在操作完成时被通知该操作是成功了还是出错了 

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

  2. 几乎所有的`ChannelOutboundHandler` 上的方法都会传入一个`ChannelPromise` 的实例。作为 `ChannelFuture` 的子类，`ChannelPromise` 也可被分配用于异步通知的监听器。但是，`ChannelPromise` 还具有提供立即通知的可写方法 `setSuccess()` `setFailure`

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

     - 通过调用`ChannelPromise` 上的 `setSuccess()`和` setFailure()`方法，可以使一个操作的状态在 `ChannelHandler` 的方法返回给其调用者时便即刻被感知到

# 网络协议

## WebSocket

# 传统RPC调用性能差的三宗罪

## 网络传输方式问题

- 传统的 RPC 框架或者基于 RMI 等方式的远程服务(过程)调用采用了同步阻塞 IO
  - 当并发访问量增加后，服务端的线程个数和并发访问数成线性正比
  - 同步阻塞IO会由于频繁的wait导致IO线程经常性的阻塞

## 序列化方式问题

- Java 序列化机制
  - 是 Java 内部的一种对象编解码技术，无法跨语言使用；
  - Java 序列化后的码流太大，无论是网络传输还是持久化到磁盘，都会导致额外的资源占用;
  - 序列化性能差(CPU 资源占用高)

## 线程模型问题

- 由于采用同步阻塞 IO，这会导致每个 TCP 连接都占用 1 个线程
- 由于线程资源是 JVM 虚拟机非常宝贵的资源，当 IO 读写阻塞导致线程无法及时释放时，会导致系统性能急剧下降，严重的甚至会导致虚拟机无法创建新的线程

# 高性能的三个主题

## 传输

- 用什么样的通道将数据发送给对方，BIO、NIO 或AIO
- IO 模型在很大程度上决定了框架的性能 

## 协议

- 协议的选择不同，性能模型也不同。相比于公有协议，内部私有协议的性能通常可以被设计的更优

## 线程

- 数据报如何读取？读取之后的编解码在哪个线程进行，编解码后的消息如何派发？
- `Reactor`线程模型的不同，对性能的影响也非常大

# Netty高性能

## 异步非阻塞通信

在 IO 编程过程中，当需要同时处理多个客户端接入请求时，可以利用多线程或者 IO 多路复用技术进行处理

- IO多路复用技术（事件驱动模型）

  - 通过把多个 IO 的阻塞复用到同一个 select 的阻塞上，从而使得系统在单线程的情况下可以同时处理多个客户端请求
  - 通过一种机制，可以监视多个描述符（连接），一旦某个描述符（连接）就绪（一般是读就绪或者写就绪），能够通知程序进行相应的读写操作
  - 与传统的多线程/多进程模型比，I/O 多路复用的最大优势是系统开销小，系统不需要创建新的额外进程或者线程，也不需要维护这些进程和线程的运行，降低了系统的维护工作量，节省了系统资源

- Netty 的 IO 线程 NioEventLoop 由于聚合了多路复用器 Selector，可以同时并发处理成百上千个客户端 Channel，由于读写操作都是非阻塞的，这就可以充分提升 IO 线程的运行效率，避免由于频繁 IO 阻塞导致的线程挂起

- 另外，由于 Netty 采用了异步通信模式，一个 IO线程可以并发处理 N 个客户端连接和读写操作，这从根本上解决了传统同步阻塞 IO 一连接一线程模型，架构的性能、弹性伸缩能力和可靠性都得到了极大的提升

- 向下兼容，NIO默认阻塞，需手动设置非阻塞

- 与 Socket 类和 ServerSocket 类相对应，NIO 也提供了 `SocketChannel` 和`ServerSocketChannel `两种不同的套接字通道（都支持阻塞和非阻塞两种模式）实现

- 阻塞模式使用非常简单，但是性能和可靠性都不好，非阻塞模式正好相反

- 一般来说，低负载、低并发的应用程序可以选择同步阻塞 IO 以降低编程复杂度。但是对于高负载、高并发的网络应用，需要使用 NIO的非阻塞模式进行开发

  ![Netty服务端通信序列图](/Users/dingyuanjie/Desktop/notes/%E4%B9%A6%E5%8D%95/Netty%E6%9C%8D%E5%8A%A1%E7%AB%AF%E9%80%9A%E4%BF%A1%E5%BA%8F%E5%88%97%E5%9B%BE.png)

  ![Netty客户端通信序列图-9180101](/Users/dingyuanjie/Desktop/notes/%E4%B9%A6%E5%8D%95/Netty%E5%AE%A2%E6%88%B7%E7%AB%AF%E9%80%9A%E4%BF%A1%E5%BA%8F%E5%88%97%E5%9B%BE-9180101.png)

## 零拷贝

- 零拷贝(zero-copy)是一种目前只有在使用 NIO 和 Epoll 传输时才可使用的特性。
- 它可以快速高效地将数据从文件系统移动到网络接口，而不需要将其从内核空间复制到用户空间，其在像 FTP 或者HTTP 这样的协议中可以显著地提升性能
- Netty 的接收和发送 ByteBuffer 采用 DIRECT BUFFERS，使用堆外直接内存进行 Socket读写，不需要进行字节缓冲区的二次拷贝。（不需要经过JVM，直接读取操作系统内存，提升数据读取性能）
- 如果使用传统的堆内存(HEAP BUFFERS)进行Socket 读写，JVM 会将堆内存 Buffer 拷贝一份到直接内存中，然后才写入 Socket 中。相比于堆外直接内存，消息在发送过程中多了一次缓冲区的内存拷贝 
- Netty 提供了组合 Buffer 对象，可以聚合多个 ByteBuffer 对象，用户可以像操作一个Buffer 那样方便的对组合 Buffer 进行操作，避免了传统通过内存拷贝的方式将几个小Buffer 合并成一个大Buffer
- Netty 的文件传输采用了 transferTo 方法，它可以直接将文件缓冲区的数据发送到目标Channel，避免了传统通过循环 write 方式导致的内存拷贝问题。

## 内存池

- 随着 JVM 虚拟机和 JIT 即时编译技术的发展，对象的分配和回收是个非常轻量级的工作，但对于缓冲区 Buffer，情况却稍有不同，特别是对于堆外直接内存的分配和回收，是一件耗时的操作。
- 为了尽量重用缓冲区，Netty 提供了基于内存池的缓冲区重用机制
- Netty 提供了多种内存管理策略，通过在启动辅助类中配置相关参数，可以实现差异化的定制。
- 采用内存池的 ByteBuf 相比于朝生夕灭的 ByteBuf，性能高很多

## 高效的Reactor线程模型

- Reactor单线程模型

  - 指的是所有的 IO 操作都在同一个 NIO 线程上面完成， NIO 线程的职责：

    1. 作为 NIO 服务端，接收客户端的 TCP 连接
    2. 作为 NIO 客户端，向服务端发起 TCP 连接
    3. 读取通信对端的请求或者应答消息
    4. 向通信对端发送消息请求或者应答消息

  - 由于 Reactor 模式使用的是异步非阻塞 IO，所有的 IO 操作都不会导致阻塞，理论上一个线程可以独立处理所有 IO 相关的操作。

    ![Reactor单线程模型](/Users/dingyuanjie/Desktop/notes/%E4%B9%A6%E5%8D%95/Reactor%E5%8D%95%E7%BA%BF%E7%A8%8B%E6%A8%A1%E5%9E%8B.png)

  - 从架构层面看，一个 NIO 线程确实可以完成其承担的职责。例如，通过 Acceptor 接收客户端的 TCP 连接请求消息，链路建立成功之后，通过Dispatch 将对应的 ByteBuffer 派发到指定的Handler 上进行消息解码。用户 Handler 可以通过 NIO 线程将消息发送给客户端。

  - 对于一些小容量应用场景，可使用单线程模型。但对于高负载、大并发的应用却不合适，主要原因如下：

    1. 一个 NIO 线程同时处理成百上千的链路，性能上无法支撑，即便 NIO 线程的 CPU 负荷达到 100%，也无法满足海量消息的编码、解码、读取和发送;
    2. 当 NIO 线程负载过重之后，处理速度将变慢，这会导致大量客户端连接超时，超时之后往往会进行重发，这更加重了 NIO 线程的负载，最终会导致大量消息积压和处理超时，NIO线程会成为系统的性能瓶颈;
    3. 可靠性问题：一旦 NIO 线程意外跑飞，或者进入死循环，会导致整个系统通信模块不可用，不能接收和处理外部消息，造成节点故障
    4. 为了解决这些问题，演进出了 Reactor 多线程模型

- Reactor多线程模型

  - 特点：

    1. 有专门一个 NIO 线程-Acceptor 线程用于监听服务端，接收客户端的 TCP 连接请求
    2. 网络IO操作-读、写等由一个NIO线程池负责，线程池可以采用标准的JDK线程池实现，它包含一个任务队列和 N 个可用的线程，由这些 NIO 线程负责消息的读取、解码、编码和发送;
    3. 1 个 NIO 线程可以同时处理 N 条链路，但是 1 个链路只对应 1 个 NIO 线程，防止发生并发操作问题。

    ![Reactor多线程模型](/Users/dingyuanjie/Desktop/notes/%E4%B9%A6%E5%8D%95/Reactor%E5%A4%9A%E7%BA%BF%E7%A8%8B%E6%A8%A1%E5%9E%8B.png)

  - 在绝大多数场景下，Reactor 多线程模型都可以满足性能需求；但是，在极特殊应用场景中，一个 NIO 线程负责监听和处理所有的客户端连接可能会存在性能问题。例如百万客户端并发连接，或者服务端需要对客户端的握手消息进行安全认证，认证本身非常损耗性能。在这类场景下，单独一个 Acceptor 线程可能会存在性能不足问题

  - 为了解决性能问题，产生了第三种 Reactor 线程模型-主从 Reactor 多线程模型。

- 主从Reactor线程模型（官方推荐）

  - 特点

    1. 服务端用于接收客户端连接的不再是1 个单独的 NIO线程，而是一个独立的 NIO 线程池。

    2. Acceptor 接收到客户端 TCP 连接请求处理完成后(可能包含接入认证等)，将新创建的 SocketChannel 注册到 IO 线程池(sub reactor 线程池)的某个 IO 线程上，由它负责 SocketChannel 的读写和编解码工作

    3. Acceptor 线程池仅仅只用于客户端的登陆、握手和安全认证，一旦链路建立成功，就将链路注册到后端 subReactor线程池的 IO 线程上，由 IO 线程负责后续的 IO 操作。

       ![主从Reactor线程模型](/Users/dingyuanjie/Desktop/notes/%E4%B9%A6%E5%8D%95/%E4%B8%BB%E4%BB%8EReactor%E7%BA%BF%E7%A8%8B%E6%A8%A1%E5%9E%8B.png)

  - 利用主从 NIO 线程模型，可以解决 1 个服务端监听线程无法有效处理所有客户端连接的性能不足问题。因此，在 Netty 的官方 demo 中，推荐使用该线程模型。

  - 事实上，Netty 的线程模型并非固定不变，通过在启动辅助类中创建不同的 EventLoopGroup实例并通过适当的参数配置，就可以支持上述三种 Reactor 线程模型。

  - 正因为 Netty 对Reactor 线程模型的支持提供了灵活的定制能力，所以可满足不同业务场景的性能诉求

## 无锁化的串行设计理念

- 在大多数场景下，并行多线程处理可以提升系统的并发性能。但是，如果对于共享资源的并发访问处理不当，会带来严重的锁竞争，这最终会导致性能的下降

- 为了尽可能的避免锁竞争带来的性能损耗，可以通过串行化设计，即消息的处理尽可能在同一个线程内完成，期间不进行线程切换，这样就避免了多线程竞争和同步锁

- 为了尽可能提升性能，Netty 采用了串行无锁化设计，在 IO 线程内部进行串行操作，避免多线程竞争导致的性能下降

- 表面上看，串行化设计似乎 CPU 利用率不高，并发程度不够。但是，通过调整 NIO 线程池的线程参数，可以同时启动多个串行化的线程并行运行，这种局部无锁化的串行线程设计相比一个队列-多个工作线程模型性能更优。

  ![Netty 的串行化设计工作原理图](/Users/dingyuanjie/Desktop/notes/%E4%B9%A6%E5%8D%95/Netty%20%E7%9A%84%E4%B8%B2%E8%A1%8C%E5%8C%96%E8%AE%BE%E8%AE%A1%E5%B7%A5%E4%BD%9C%E5%8E%9F%E7%90%86%E5%9B%BE.png)

  - 单个通道下所有业务逻辑处理都是有顺序的，可控的（每一个逻辑都是一个线程），多个通道之间是互不干扰的，并行的

## 高效的并发编程

1. `volatile`的大量、正确使用
2. `CAS` 和原子类的广泛使用
3. 线程安全容器的使用
4. 通过读写锁提升并发性能

## 高性能的序列化框架

- 影响序列化性能的关键因素总结如下：
  1. 序列化后的码流大小(网络带宽的占用);
  2. 序列化&反序列化的性能(CPU 资源占用);
  3. 是否支持跨语言(异构系统的对接和开发语言切换)
- Netty 默认提供了对 Google Protobuf 的支持，Protobuf 序列化后的码流只有 Java 序列化的 1/4 左右，通过扩展 Netty 的编解码接口，用户可以实现其它的高性能序列化框架，例如 Thrift 的压缩二进制编解码框架

## 灵活的TCP参数配置能力

- 合理设置 TCP 参数在某些场景下对于性能的提升可以起到显著的效果，例如 `SO_RCVBUF` 和`SO_SNDBUF`。如果设置不当，对性能的影响是非常大的
  1. `SO_RCVBUF` 和 `SO_SNDBUF`：通常建议值为 128K 或者 256K;
  2. `SO_TCPNODELAY`：`NAGLE` 算法通过将缓冲区内的小封包自动相连，组成较大的封包，阻止大量小封包的发送阻塞网络，从而提高网络应用效率。但是对于时延敏感的应用场景需要关闭该优化算法;
  3. 软中断：如果 Linux 内核版本支持 RPS(2.6.35 以上版本)，开启 RPS 后可以实现软中断，提升网络吞吐量。RPS 根据数据包的源地址，目的地址以及目的和源端口，计算出一个hash 值，然后根据这个 hash 值来选择软中断运行的 cpu，从上层来看，也就是说将每个连接和 cpu 绑定，并通过这个hash 值，来均衡软中断在多个 cpu 上，提升网络并行处理性能
- Netty 在启动辅助类中可以灵活的配置 TCP 参数，满足不同的用户场景

# 基于Netty实现RPC

- Dubbo集成了Spring，底层使用了Netty，用和Spring一样的代理
- RMI不能跨语言，只能Java用（只支持Java序列化），是最原始的一种RPC协议，走TCP
- RPC支持多种协议
- Dobbo走TCP、支持多种序列化协议，实现跨平台调用

![image-20181106140203347](/Users/dingyuanjie/Desktop/MyKnowledge/2.code/java/2.%E5%92%95%E6%B3%A1%E5%AD%A6%E9%99%A2/04.%E5%A4%9A%E7%BA%BF%E7%A8%8B%E9%AB%98%E5%B9%B6%E5%8F%91/02.Netty/image-20181106140203347.png)

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

# WebSocket聊天系统

# Netty-SocketIO

- `socket.io`封装了`Websocket`以及其他的一些协议，并且实现了`Websocket`的服务端代码。同时还有很强的兼容性，兼容各种浏览器以及移动设备
- 是一个不错的websocket项目
- 支持实时、双向和基于事件的浏览器和服务器之间的通信

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