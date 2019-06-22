* Netty是什么？
  * 异步事件驱动框架，用于快速开发高性能服务端和客户端
  * 封装了JDK底层BIO和NIO模型，提供高度可用的API
  * 自带编解码器解决拆包粘包问题，用户只用关心业务逻辑
  * 精心设计的reactor线程模型支持高并发海量连接
  * 自带各种协议栈可处理任何一种通用协议
* Dubbo、RocketMQ、Spark、Elasticsearch、Cassandra、Flink、Netty-SocketIO、Spring5、Play、Grpc

1. 服务端NioEventLoop监听端口
2. 客户端与服务端建立新连接Channel
3. 服务端接收客户端发送的数据ByteBuf
4. 服务端进行业务逻辑处理ChannelPipeline-ChannelHandler
5. 服务端发送数据ByteBuf给客户端

* Netty基本组件
  * NioEventLoop
  * Channel、Pipeline
  * ChannelHandler、ByteBuf
* 服务端的Socket在哪里初始化？
* 在哪里accept连接？
* Netty服务端启动
  * 创建服务端Channel
    * bind()用户代码入口——initAndRegister()初始化并注册——newChannel()创建服务端Channel
    * 反射创建服务端Channel
      * newSocket()，通过jdk来创建底层jdk Channel
      * NioServerSocketChannelConfig()，tcp参数配置类
      * AbstractNioChannel()
        * configureBlocking(false)，非阻塞模式
        * AbstractChannel()，创建id、unsafe、pipeline
  * 初始化服务端Channel
    * bind()，用户代码入口
      * initAndRegister()，初始化并注册
        * newChannel()，创建服务端channel
        * init()，初始化服务端channel，初始化入口
          * set ChannelOptions，ChannelAttrs
          * set ChildOptions，ChildAttrs
          * config handler，配置服务端pipeline
          * add ServerBootstrapAcceptor，添加连接器
        * 
  
    * 初始化服务端Channel，保存用户自定义的属性，通过这些属性来创建连接接收器，每次接收新的连接后都会使用这些属性对新的连接进行配置
  * 注册Selector
    * AbstractChannel.register(channel)，入口
      * this.eventLoop = eventLoop，绑定线程
      * register0()，实际注册
        * doRegister()，调用jdk底层注册
        * invokeHandlerAddedIfNeeded()
        * fireChannelRegistered()，传播事件
    * ChannelAdded、ChannelRegister
  * 端口绑定
    * AbstractUnsafe.bind()，入口
      * doBind()
        * javaChannel.bind()，jdk底层绑定
      * pipeline.fireChannelActive()，传播事件
        * HeadContext.readIfIsAutoRead()

* 服务端启动核心路径总结
  * newChannel() -> init() -> register() -> doBind()



* 默认情况下，Netty服务端起多少线程？何时启动？
  * 默认创建2倍CPU个数线程，调用execute方法时会判断当前线程是否在本线程， 如果是在本线程则说明线程已启动，如果是外部线程调用execute方法，那么首先会调用startThread方法，该方法会判断当前线程是否有启动，如果没有启动则启动这个线程
* Netty是如何解决jdk空轮询bug的？
  * Netty通过计数的方式去判断， 如果当前阻塞的是一个select操作并没有花费很长时间，则有可能触发空轮训bug，默认情况达到512次，然后重建一个select，将原select上的key重新移交到新的select上，通过这种方式巧妙地避免了jdk空轮训Bug 
* Netty如何保证异步串行无锁化？
  * Netty在所有外部线程去调用inEventLoop或Channel方式时，通过inEventLoop方法来判断得出是外部线程，这种情况下把所有操作封装成一个Task丢到MpscQueue中，然后在NioEventLoop执行逻辑的第三过程这些task会被挨个执行



* NioEventLoop
  * NioEventLoop创建
    * 创建NioEventLoopGroup和OioEventLoopGroup时，NioEventLoop被创建，默认创建2倍CPU数个NioEventLoop
    * NioEventLoop在创建时会创建一个Selector和定时任务队列  
    * new NioEventLoopGroup()，线程组，默认2*cpu
      * new ThreadPerTaskExecutor()，线程创建器
        * 每次执行任务都会创建一个线程实体
        * NioEventLoop线程命名规则nioEventLoop-1-xx
      * for(){newChild()}，构造NioEventLoop
        * 保存线程执行器ThreadPerTaskExecutor
        * 创建一个MpscQueue
        * 创建一个selector
      * chooseFactory.newChooser()，线程选择器
  * NioEventLoop启动触发器
    * 服务端启动绑定端口
    * 新连接接入通过chooser绑定一个NioEventLoop
  * NioEventLoop启动
    * bind() -> execute(task)，入口
      * startThread() -> doStartThread()，创建线程
        * ThreadPerTaskExecutor.execute()
          * thread = Thread.currentThread()，判断是否是当前线程
          * NioEventLoop.run()，启动
            * run() -> for(;;)
              * select()，检查是否有io事件
              * processSelectedKeys()，处理IO事件
                * 执行逻辑
                  * selected keySet优化
                  * processSelectedKeysOptiomized()
              * runAllTasks()，处理异步任务队列
                * 执行逻辑
                  * task的分类和添加
                  * 任务的聚合
                  * 任务的执行
  * NioEventLoop执行逻辑
    * SingleThreadEventExecutor.this.run()



* Netty是在哪里检测有新连接接入的？
  * Boss线程的第一个过程，轮询出Accept事件，然后Boss线程的第二个过程，通过jdk底层的Channel的Accept方法去创建这条连接
* 新连接是怎样注册到NioEventLoop线程的？
  * Boss线程调用Chooser的next方法拿到一个NioEventLoop，然后将这条连接注册到NioEventLoop的select上去

* Netty新连接接入处理逻辑
  * 检测新连接 -> 创建NioSocketChannel -> 分配线程及注册selector -> 向selector注册读事件
    * 检测新连接
      * processSelectedKey(key,channel)，入口
        * NioMessageUnsafe.read()
          * doReadMessages()，while循环
            * javaChannel.accept()
    * 创建NioSocketChannel
      * new NioSocketChannel(parent,ch)，入口
        * AbstractNioByteChannel(p, ch, op_read)
          * configureBlocking(false) & save op(读)
          * create id, unsafe, pipeline
        * new NioSocketChannelConfig()
          * setTcpNoDelay(true)禁止Nagle算法，小的数据包尽快发送出去来降低延时
    * 服务端Channel的pipeline构成
      * Head -> ServerBootstrapAcceptor -> Tail
      * ServerBootstrapAcceptor
        * 添加childHandler
        * 设置options和attrs
        * 选择NioEventLoop并注册selector

* NioServerSocketChannel主要是用来接收连接事件
* NioSocketChannel主要是用来读取数据



* netty是如何判断ChannelHandler类型的？
  * 添加ChannelHandler时使用instances of关键词来判断ChannelHandler类型，如果实现了ChannelInBoundHandler，那么通过设置一个boolean类型字段inBound来标识该Handler是inBoundHandler类型
* 对于ChannelHandler的添加应该遵循什么样的顺序？
  * inBound是被动的触发（Pipeline中添加的顺序和实际传播的顺序是相同的），outBind主动发起（Pipeline中添加的顺序和实际传播的顺序是相反的）
* 用户手动触发事件传播，不同的触发方式有什么样的区别？

* pipeline 

  * pipeline的初始化

    * pipeline在创建Channel的时候被创建
    * Pipeline节点数据结构：ChannelHandlerContext
    * Pipeline中的两大哨兵：head和tail

  * 添加删除ChannelHandler

    * 判断是否重复添加

      * 判断ChannelHandler是否是ChannelHandler实例

        - 如果是，则判断该ChannelHandlerAdapter（强转）没有标有iaSharable注解和added属性是否为true，则抛出重复创建异常，否则added属性设置为true
        - 如果不是则啥也不做

        ```java
        private static void checkMultiplicity(ChannelHandler handler) {
          if(handler instanceof ChannelHandlerAdapter) {
            ChannelHandlerAdapter h = (ChannelHandlerAdapter)handler;
            if(!h.isSharable() && h.added){
              throw new ChannelPipelineException(h.getClass().getName() + "is not a @Sharable handler, so can't ba added")
            }
            h.added = true;
          }
        }
        ```

    * 创建节点并添加至链表

      ```java
      private void addLast0(AbstractChannelHandlerContext newCtx){
        AbstractChannelHandlerContext prev = tail.prev;
        newCtx.prev = prev;
        newCtx.next = tail;
        newCtx.next = newCtx;
        tail.prev = newCtx;
      }
      ```

    * 回调添加完成事件

      ```java
        EventExecutor executor = newCtx.executor();
        if(!executor.inEventLoop()) {
          newCtx.setAddPending();
          executor.execute(new Runnable(){
            @Override
            public void run() {
              callHandlerAdded0(newCtx);
            }
          });
          return this;
        }
      }
      callHandlerAdded0(newCtx);
      return this;
      ```

    * 删除ChannelHandler

      * 找到节点
      * 链表的删除
      * 回调删除Handler事件
  
      ```java
      public class AuthHandler extends SimpleChannelInboundHandler<ByteBuf> {
        @Override
        protected void channelRead0(ChannelHandlerContext ctx, ByteBuf password) {
          if(pass(password)) {
            ctx.pipeline().remove(this);
          } else {
            ctx.close();
          }
        }
        private boolean paas(ByteBuf password){
          return false;
        }
      }
      ```
  
      ```java
      private static void remove0(AbstractChannelHandlerContext ctx) {
        AbstractChannelHandlerContext prev = ctx.prev;
        AbstractChannelHandlerContext next = ctx.next;
        prev.next = next;
        next.prev = prev;
      }
      ```
  
      ```java
      private void callHandlerRemoved0(final AbstractChannelHandlerContext ctx) {
        try {
          
        } catch(Throwable t) {
          fireExceptionCaught(new ChannelPipelineException(ctx.handler().getClass().getName() + ".handlerRemove")) 
        }
      }
      ```
  
  * 事件和异常的传播



* inBound事件的传播

  * 何为inBound事件以及ChannelInboundHandler

  * ChannelRead事件的传播

  * SimpleInBoundHandler处理器

    ```java
    // msg 对于服务端来说是个连接，对于客户端来说是ByteBuf
    void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception;
    ```

    ```java
    public class InboundHandlerC extends ChannelInBoundHandlerAdapter {
      @Override
      public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        System.out.println("InBoundHandlerC:" + msg);
        // 显示向下个Handler传播消息，否则不传播
        ctx.fireChannelRead(msg);
      }
    }
    
    // 测试
    telnet 127.0.0.1 8888
    ```

![image-20190618221045500](/Users/dingyuanjie/Documents/study/github/woodyprogram/img/image-20190618221045500.png)



* outBound事件的传播
  * 何为outBound事件以及ChannelOutBoundHandler
  * write()事件的传播



* 异常的传播

  * 异常的触发链

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

![image-20190618223202132](/Users/dingyuanjie/Documents/study/github/woodyprogram/img/image-20190618223202132.png)

* pipeline默认结构
  * Head  <——> Tail
  * head节点里的unsafe负责实现Channel具体协议，而tail节点终止事件和异常传播的作用



* 内存的类别有哪些？
* 如何减少多线程内存分配之间的竞争？
* 不同大小的内存是如何进行分配的？



* ByteBuf
  * 内存与内存管理器的抽象
  * 不同规格大小和不同类别的内存的分配策略
  * 内存的回收过程
* ByteBuf结构以及重要API
  * ByteBuf结构
  * read、write、set方法
  * mark和reset方法

* ByteBuf分类
  * Pooled和Unpooled
  * Unsafe和非Unsafe
    * unsafe会调用jdk底层的api直接操作内存
  * Heap和Direct
    * Direct不会被JVM回收
* ByteBufAllocator分析
  * ByteBufAllocator功能
    * ByteBufAllocator内存分配器，负责分配所有类型的ByteBuf
    * unsafe和safe由底层自动判断
  * AbstractByteBufAllocator
  * ByteBufAllocator两大子类
    * AbstractByteBufAllocator
      * UnpooledByteBufAllocator
        * heap内存的分配
        * direct内存的分配
      * PooledByteBufAllocator
        * 拿到线程局部缓存PoolThreadCache
        * 在线程局部缓存的Area上进行内存分配
          * 每个线程对应一个Area
* ByteBuf implements ReferenceCounted Comparable
  * AbstractByteBuf
    * PooledHeapByteBuf
      * PooledUnsafeHeapByteBuf
    * UnpooledUnsafeDirectByteBuf
    * UnpooledDirectByteBuf
    * UnpooledHeapByteBuf
      * UnpooledUnsafeHeapByteBuf
    * PooledUnsafeDirectByteBuf
    * PooledDirectByteBuf

* unsafe通过内存地址+偏移量拿到对应的数据，非unsafe通过数组+下标 或jdk底层ByteBuf api 去拿数据，通常unsafe比非unsafe要快
* ByteBuf的释放
  * 连续的内存区段加到缓存
  * 标记连续的内存区段为未使用
  * ByteBuf加到对象池





* 解码器基类

  * 解码器抽象的解码过程
  * ByteToMessageDecoder解码步骤（字节到对象的解码步骤）
      * 累加字节流
      * 调用子类的decode方法进行解析
      * 将解析到的ByteBuf向下传播

* netty中常见的解码器分析

  * netty里面有哪些拆箱即用的解码器
* 基于固定长度解码器
    * 根据指定的长度自动对消息进行解码，开发者不需要考虑TCP拆包/粘包问题
  * FixedLengthFrameDecoder extends ByteToMessageDecoder
      * A|BC|DEFG|HI ——code为3——ABC|DEF|GHI
      * `new FixedLengthFrameDecoder(20)`，服务端一次接收20个字符长度数据，多于的在下次中接收
  * 行解码器
    * \r\n、\n
    * LineBasedFrameDecoder
  * 基于分隔符解码器
    * DelimiterBasedFrameDecoder
    * 行处理器
    * 找到最小分隔符
  * 基于长度域解码器
    * LengthFieldBasedFrameDecoder
      * lengthFieldOffset
      * lengthFieldLength
    * 计算需要抽取的数据包长度
    * 跳过字节逻辑处理
    * 丢弃模式下的处理



* 如何把对象变成字节流，最终写到socket底层

  * 当bizHandler通过write方法将User对象向Pipeline的Head方向传播到encoder节点（继承MessageToByteEncoder，复写encode()把自定义User对象转为ByteBuf，然后继续调用write()将ByteBuf向Head传播，Head接收到ByteBuf后会调用unsafe方法将数据存入底层缓存区）,同理flush()，向Head传播，最终在Head接收flush事件后通过循环从缓冲区中取出ByteBuf转成jdk底层能够接收的对象并写出去，每写完一个就将当前缓冲区中节点删除

  * writeAndFlush()

    * 从tail节点开始往前传播
    * 逐个调用channelHandler的write方法
    * 逐个调用channelHandler的flush方法

    ```java
    public class BizHandler extends ChannelInboundHandlerAdapter {
        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
            //...
            User user = new User(19, "zhangsan");
            ctx.channel().writeAndFlush(user);
        }
    }
    ```

    ![image-20190620225030141](/Users/dingyuanjie/Documents/study/github/woodyprogram/img/image-20190620225030141.png)

    

    

```java
public final class Server {

    public static void main(String[] args) throws Exception {
        EventLoopGroup bossGroup = new NioEventLoopGroup();
        EventLoopGroup workerGroup = new NioEventLoopGroup();

        try {
            ServerBootstrap b = new ServerBootstrap();
            b.group(bossGroup, workerGroup).channel(NioServerSocketChannel.class).childHandler(new ChannelInitializer<SocketChannel>() {
                @Override
                public void initChannel(SocketChannel ch) {
                    ch.pipeline().addLast(new Encoder());
                    ch.pipeline().addLast(new BizHandler());
                }
            });

            ChannelFuture f = b.bind(8888).sync();

            f.channel().closeFuture().sync();
        } finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }
}
```

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

* 编码器处理逻辑：MessageToByteEncoder
  * 匹配对象，判断当前编码器能否处理该对象，如果不能则将该对象向前传播
  * 分配内存
  * 编码实现，encode将cast对象转为ByteBuf
  * 释放对象，ReferenceCountUtil.release(cast);
  * 传播数据，传播ByteBuf
  * 释放内存，如果出现异常则需释放内存buf.release()，如果原生对象就是一个ByteBuf，Netty在自定义编码结束之后自动释放对象，不需要在Encode方法里释放原生对象
* write-写buffer队列
  * direct化ByteBuf
  * 插入写队列
  * 设置写状态
* flush-刷新buffer队列
  * 添加刷新标识并设置写状态
  * 遍历buffer队列，过滤ByteBuf
  * 调用jdk底层api进行自旋写



* 两大性能分析工具类
  * FastThreadLocal
    * 每个线程拿到对象后都是线程独享
    * 一个线程修改该对象后不影响其他线程
    * 创建
      * 每个都带有各自的索引值，代表身份标识
    * get()方法实现
      * 获取ThreadLocalMap
      * 直接通过索引取出对象
      * 初始化
    * set()方法实现
      * 获取ThreadLocalMap
      * 直接通过索引set对象
      * remove对象
  * Recycler轻量级对象池
    * 同线程回收对象
    * 异线程回收对象



* 单例模式

  * 一个类全局只有一个对象
  * 延迟创建
  * 避免线程安全问题

  ![image-20190621072308518](/Users/dingyuanjie/Documents/study/github/woodyprogram/img/image-20190621072308518.png)

  ![image-20190621072352635](/Users/dingyuanjie/Documents/study/github/woodyprogram/img/image-20190621072352635.png)

* 策略模式

  * 封装一系列可相互替换的算法家族
  * 动态选择某一个策略

* 装饰者模式

  * 装饰者和被装饰者继承同一个接口
  * 装饰者给被装饰者动态修改行为
  * wrappedBuffer

* 观察者模式

  * 观察者和被观察者
  * 观察者订阅消息，被观察者发布消息
  * 订阅则能收到，取消订阅则收不到

  ```java
  // channelFuture 被观察者
  ChannelFuture channelFuture = channel.writeAndFlush(object);
  // 添加观察者
  channelFuture.addListener(future -> {
    
  });
  ```

* 迭代器模式

  * 可实现内存的零拷贝
  * 迭代器接口
  * 对容器里面各个对象进行访问

  ![image-20190621075116013](/Users/dingyuanjie/Documents/study/github/woodyprogram/img/image-20190621075116013.png)

  ![image-20190621075146619](/Users/dingyuanjie/Documents/study/github/woodyprogram/img/image-20190621075146619.png)

* 责任链模式

  * 将一些对象连接成一条链，消息在这条链之间传递，链中对象能够处理自己关心的消息

  * 责任处理器接口

  * 创建链，添加删除责任处理器接口

  * 上下文

  * 责任终止机制

    ```java
public class InBoundHandlerB extends ChannelInboundHandlerAdapter {
      @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) {
        System.out.println("InBoundHandlerB:" + msg);
      //显示地继续传递msg，否则不传递msg
        ctx.fireChannelRead(msg);
      }
    }
    ```
    
    
    
    ![image-20190621075936599](/Users/dingyuanjie/Documents/study/github/woodyprogram/img/image-20190621075936599.png)
    
    * 继承ChannelInboundHandlerAdapter，如果不重写channelRead，默认会继续往下传播，而fireChannelRead是显示传播
    
      ```java
      private AbstractChannelHandlerContext findContextInbound() {
        AbstractChannelHandlerContext ctx = this;
        do {
          ctx = ctx.next;
        } while(!ctx.inbound);
        return ctx;
      }
      ```
  
  

* Netty高并发性能调优

  * 单机百万连接调优

    * 如何模拟百万连接

      ![image-20190622073233315](/Users/dingyuanjie/Documents/study/github/woodyprogram/img/image-20190622073233315.png)

    * 突破局部文件句柄限制

      * ulimit -n，一个JVM进程能够打开多少个文件

      * /etc/security/limits.conf

        * 以sudo打开

        * 文件末尾加上

          ```properties
          # *当前用户，soft警告限制，hard真正的限制，nofile最大打开文件数
          # 任何一个用户可以打开一百万个文件
          * hard nofile 1000000
          * soft nofile 1000000
          ```

      * Linux单个进程默认打开的文件句柄数是有限的，而一个TCP连接就对应一个句柄，一个应用程序服务端默认建立的TCP连接是有限制的

    * 突破全局文件句柄限制

      * cat /proc/sys/fs/file-max

        * Linux中所有进程能够打开的文件句柄数

        ![image-20190622074624448](/Users/dingyuanjie/Documents/study/github/woodyprogram/img/image-20190622074624448.png)

        ```properties
        sudo vi /etc/sysctl.conf
        fs.file-max=1000000
        # 使修改生效
        sudo sysctl -p
        ```

  * Netty应用级别性能调优



