## 问题

* 服务端如何启动
* 客户端如何启动
* 数据载体ByteBuf
* 长连自定义协议该如何设计
* 粘包拆包原理与实践
* 如何实现自定义编解码
* pipeline与channelHandler
* 定时发心跳怎么做
* 如何进行空闲连接检测

## Netty客户端程序逻辑结构

* 解析控制台指令-创建指令对象-（协议的编码，通过自定义二进制协议将指令对象封装成二进制）二进制转换-发送服务端
* 接收服务端-粘包拆包处理-（协议的解码，二进制数据包解析成指令对象）解析指令对象-指令处理-创建指令对象

## Netty服务端程序逻辑结构

* 接收客户端数据-粘包拆包-（协议的解码，二进制数据包解析成指令对象）解析指令对象指令处理-创建指令对象-（协议的编码，通过自定义二进制协议将指令对象封装成二进制）二进制转换-发送客户端

## BIO

* 问题
  * 线程资源受限：一个连接需创建一个线程，对应一个 while 死循环不断监测这条连接上是否有数据可以读，大多循环数浪费资源
    * NIO
      * 一条连接来了之后，不创建一个 while 死循环去监听是否有数据可读，而是直接把这条连接注册到 selector 上，然后，通过检查这个 selector，就可以批量监测出有数据可读的连接，进而读取数据
      * 开多个线程，每个线程都管理着一批连接，相对于 IO 模型中一个线程管理一条连接，消耗的线程资源大幅减少
  * 线程切换效率低
    *  NIO 
      * 线程数量大大降低，线程切换效率因此也大幅度提高
  * 面向流，一次性只能从流中读取一个或者多个字节，且读完之后流无法再读取
    * NIO
      * NIO 的读写面向 Buffer ，可随意读取里面任何一个字节数据，不需缓存数据，只需移动读写指针即可

## Netty

* Netty 是一个异步事件驱动的网络应用框架，用于快速开发可维护的高性能服务器和客户端

* 服务端启动

  * 必须：引导类、线程模型、IO模型、连接读写处理逻辑、bind绑定端口（异步，可添加监听）
  * 其他方法：
    * handler()：指定在服务端启动过程中的一些逻辑
    * attr()：给服务端的 channel指定一些自定义属性，通过`channel.attr()`取出这个属性
    * childAttr()：给每一条连接指定自定义属性，然后后续可通过`channel.attr()`取出该属性
    * childOption()：给每条连接设置一些TCP底层相关的属性
      * `ChannelOption.SO_KEEPALIVE`表示是否开启TCP底层心跳机制，true为开启
      * `ChannelOption.TCP_NODELAY`表示是否开启Nagle算法，true表示关闭，false表示开启，通俗地说，如果要求高实时性，有数据发送时就马上发送，就关闭，如果需要减少发送次数减少网络交互，就开启
    * option()：服务端channel设置一些属性
      * ChannelOption.SO_BACKLOG：表示系统用于临时存放已完成三次握手的请求的队列的最大长度，如果连接建立频繁，服务器处理创建新连接较慢，可以适当调大这个参数

* 客户端启动

  * 必须：引导类、线程模型、IO 模型，以及 IO 业务处理逻辑三大参数、connect建立连接（异步，可添加监听）

  * 定时任务：是调用 `bootstrap.config().group().schedule()`

  * 其他方法：attr() 、option()-（CONNECT_TIMEOUT_MILLIS，5000）

    ```java
    private ByteBuf getByteBuf(ChannelHandlerContext ctx) {
      // 1. 获取二进制抽象 ByteBuf
      ByteBuf buffer = ctx.alloc().buffer();
      // 2. 准备数据，指定字符串的字符集为 utf-8
      byte[] bytes = "你好，闪电侠!".getBytes(Charset.forName("utf-8"));
      // byteBuf.toString(Charset.forName("utf-8"))
      // 3. 填充数据到 ByteBuf
      buffer.writeBytes(bytes);
      return buffer;
    }
    ```

* ByteBuf

  * 引用一段内存，可是堆内也可是堆外的，然后用引用计数来控制这段内存是否需要被释放，使用读写指针来控制对 ByteBuf 的读写，可以理解为是外观模式的一种使用
  * maxCapacity：capacity及可扩容字节
    * capacity被readIndex、writeIndex分为三部分-废弃、可读、可写
  * API
    * get/set 不会改变读写指针，而 read/write 会改变读写指针
    * capacity()、maxCapacity()、readableBytes() 与 isReadable()、writableBytes()、 isWritable() 与 maxWritableBytes()
    * readerIndex() 与 readerIndex(int设置读指针)、writeIndex() 与 writeIndex(int)、markReaderIndex() 与 resetReaderIndex()
    * writeBytes(byte[] src) ：表示把字节数组 src 里面的数据全部写到 ByteBuf
    *  buffer.readBytes(byte[] dst)：把 ByteBuf 里面的数据全部读取到 dst
    * release() 与 retain()
      * Netty 使用了堆外内存是不被 jvm 直接管理，即申请到的内存无法被垃圾回收器直接回收，需手动回收
      * Netty 的 ByteBuf 是通过引用计数的方式管理，如果一个 ByteBuf 没有地方被引用到，需要回收底层内存。默认情况下，当创建完一个 ByteBuf，它的引用为1，然后每次调用 retain() 方法， 它的引用就加一， release() 方法原理是将引用计数减一，减完之后如果发现引用计数为0，则直接回收 ByteBuf 底层的内存
    * slice()、duplicate()、copy()，都维护着自己的读写指针
      * slice() ：从 readerIndex 到 writeIndex中截取一段
      * duplicate() ：把整个 ByteBuf 都截取出来，包括所有的数据，指针信息
        * slice()、duplicate()底层内存以及引用计数与原始的 ByteBuf 共享、不会改变 ByteBuf 的引用计数（原始的 ByteBuf 调用 release() 之后发现引用计数为零，就开始释放内存，调用这两个方法返回的 ByteBuf 也会被释放——retainedSlice() 与 retainedDuplicate()-在截取内存片段的同时，增加内存的引用计数）
      *  copy()：直接从原始的 ByteBuf 中拷贝所有的信息，不会影响到原始的 ByteBuf
      * 使用到 slice 和 duplicate 方法的时候，千万要理清内存共享，引用计数共享，读写指针不共享
        * 错误：多次释放、不释放造成内存泄漏
        * 避免：在一个函数体里面，只要增加了引用计数（包括 ByteBuf 的创建和手动调用 retain() 方法），就必须调用 release() 方法
        * ByteBuf buffer = ByteBufAllocator.DEFAULT.buffer(9, 100);

* 客户端与服务端通信协议编解码

  * 无论是使用 Netty 还是原始的 Socket 编程，基于 TCP 通信的数据包格式均为二进制
  * 协议指的就是客户端与服务端事先商量好的，每一个二进制数据包中每一段字节分别代表什么含义的规则
  * 通信协议的设计
    * 魔数(4)版本号(1)序列化算法(1)指令(1)数据长度(4)数据(n)
    * 魔数：固定的数，如0x12345678，识别出这个数据包是否遵循自定义协议
  * ByteBuf byteBuf = ByteBufAllocator.DEFAULT.ioBuffer();
    * 返回适配 io 读写相关的内存，它会尽可能创建一个直接内存，直接内存可以理解为不受 jvm 堆管理的内存空间，写到 IO 缓冲区的效果更高
  * **编解码过程**

* pipeline与channelHandler

  * 通过责任链设计模式来组织代码逻辑，并且能够支持逻辑的动态添加和删除

  * 在 Netty 整个框架里面，一条连接对应着一个 Channel，这条 Channel 所有的处理逻辑都在`ChannelPipeline` 的对象里面，`ChannelPipeline` 是一个双向链表结构，里面每个节点都是一个 `ChannelHandlerContext` 对象，这个对象能够拿到和 Channel 相关的所有的上下文信息，然后这个对象包着逻辑处理器 `ChannelHandler`

  * 在 `channelRead()` 方法里面，打印当前 handler 的信息，然后调用父类的 `super.channelRead(ctx, msg);` 方法，而这里父类的 `channelRead()` 方法会自动调用到下一个 inBoundHandler 的 `channelRead()` 方法，并且会把当前 inBoundHandler 里处理完毕的对象传递到下一个 inBoundHandler。inBoundHandler 的执行顺序与通过 `addLast()` 方法添加的顺序保持一致

  * 在 `write()` 方法里面，打印当前 handler 的信息，然后调用父类的 `super.write(ctx, msg, promise);` 方法，而这里父类的 `write()` 方法会自动调用到下一个 outBoundHandler 的 `write()` 方法，并且会把当前 outBoundHandler 里处理完毕的对象传递到下一个 outBoundHandler。outBoundHandler 的执行顺序与添加的顺序相反

  * ChannelInboundHandlerAdapter

    * 用于实现其接口 `ChannelInboundHandler` 的所有方法，这样在编写自己的 handler 的时候就不需要实现 handler 里面的每一种方法，而只需要实现所关心的方法，如channelRead接收上一个 handler 的输出，默认情况下 adapter 会通过 `fireChannelRead()` 方法直接把上一个 handler 的输出结果传递到下一个 handler

      ```java
      @Override
      public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        ByteBuf requestByteBuf = (ByteBuf) msg;
        // 解码
        Packet packet = PacketCodeC.INSTANCE.decode(requestByteBuf);
        // 解码后的对象传递到下一个 handler 处理
        ctx.fireChannelRead(packet)
      }
      ```

  * ChannelOutboundHandlerAdapter

    * write，默认情况下，这个 adapter 也会把对象传递到下一个 outBound 节点，它的传播顺序与 inboundHandler 相反

  * ByteToMessageDecoder

    * 二进制数据转换到一个 Java 对象，Netty 会自动进行内存的释放

    * 继承然后实现decode()，通过往 `List`参数里面添加解码后的结果对象，就可自动实现结果往下一个 handler 进行传递，这样，就实现了解码的逻辑 handler

      ```java
      public class PacketDecoder extends ByteToMessageDecoder {
          @Override
          protected void decode(ChannelHandlerContext ctx, ByteBuf in, List out) {
              out.add(PacketCodeC.INSTANCE.decode(in));
          }
      }
      ```

  * SimpleChannelInboundHandler

    * 类型判断和对象传递的活都自动实现了，可专注于处理所关心的业务指令即可

      ```java
      public class LoginRequestHandler extends SimpleChannelInboundHandler<LoginRequestPacket> {
        @Override
        protected void channelRead0(ChannelHandlerContext ctx, LoginRequestPacket loginRequestPacket) {
          // 登录逻辑
        }
      }
      ```

  * MessageToByteEncoder

    * 专门处理编码逻辑，将对象转换到二进制数据，不再需要自行去分配 ByteBuf

    * 可以实现自定义编码，而不用关心 ByteBuf 的创建，不用每次向对端写 Java 对象都进行一次编码

      ```java
      public class PacketEncoder extends MessageToByteEncoder<Packet> {
        @Override
        protected void encode(ChannelHandlerContext ctx, Packet packet, ByteBuf out) {
          PacketCodeC.INSTANCE.encode(out, packet);
        }
      }
      ```

  * 服务端和客户端

    * 第一个添加解码器（ByteToMessageDecoder的子类），最后一个添加编码器（MessageToByteEncoder的子类）

  * MessageToMessageCodec

    * 编解码操作放到一个类里面去实现

    * 客户端服务端放在首位

      ```java
      @ChannelHandler.Sharable
      public class PacketCodecHandler extends MessageToMessageCodec<ByteBuf, Packet> {
          public static final PacketCodecHandler INSTANCE = new PacketCodecHandler();
          private PacketCodecHandler() {
          }
      
          @Override
          protected void decode(ChannelHandlerContext ctx, ByteBuf byteBuf, List<Object> out) {
              out.add(PacketCodec.INSTANCE.decode(byteBuf));
          }
      
          @Override
          protected void encode(ChannelHandlerContext ctx, Packet packet, List<Object> out) {
              ByteBuf byteBuf = ctx.channel().alloc().ioBuffer();
              PacketCodec.INSTANCE.encode(byteBuf, packet);
              out.add(byteBuf);
          }
      }
      ```

* 粘包拆包

  * 底层操作系统是按照字节流发送数据，到了 Netty 应用层面，重新拼装成 ByteBuf，而这里的 ByteBuf 与客户端按顺序发送的 ByteBuf 可能是不对等的
  * 需要在客户端根据自定义协议来组装应用层的数据包，然后在服务端根据应用层的协议来组装数据包，这个过程通常在服务端称为拆包，而在客户端称为粘包
  * 固定长度的拆包器 FixedLengthFrameDecoder
    * 每个数据包的长度都是固定的
  * 行拆包器 LineBasedFrameDecoder
    * 每个数据包之间以换行符作为分隔
  * 分隔符拆包器 DelimiterBasedFrameDecoder
    * 拆包器的通用版本，只不过可自定义分隔符
  * 基于长度域拆包器 LengthFieldBasedFrameDecoder
    * 最通用，只要自定义协议中包含长度域字段，均可以使用这个拆包器来实现应用层拆包
    * 拆包器
      * 作用就是根据自定义协议，把数据拼装成一个个符合自定义数据包大小的 ByteBuf，然后送到自定义协议解码器去解码
      * 只需要关注自定义协议中长度域（数据长度）在整个数据包的哪个地方（相对整个数据包的偏移量是多少）、长度域的长度是多少
      * new LengthFieldBasedFrameDecoder(Integer.MAX_VALUE, 7, 4);
      * 只需要在 pipeline 的最前面加上这个拆包器
      * 在后续 `PacketDecoder` 进行 decode 操作的时候，ByteBuf 就是一个完整的自定义协议数据包
    * 拒绝非本协议连接
      * 只需要继承自 LengthFieldBasedFrameDecoder 的 `decode()` 方法，然后在 decode 之前判断前四个字节是否是等于自定义的魔数 `0x12345678`

* channelHandler的生命周期

  * handlerAdded() -> channelRegistered() -> channelActive() -> channelRead() -> channelReadComplete()
    * handlerAdded()：当检测到新连接之后的回调，表示在当前的 channel 中，已经成功添加了一个 handler 处理器
    * channelRegistered()：当前的 channel 的所有的逻辑处理已经和某个 NIO 线程建立了绑定关系
    * channelActive()： channel 的 pipeline 中已经添加完所有的 handler以及绑定好一个 NIO 线程之后
    * channelRead()：客户端向服务端发来数据，每次都会回调此方法，表示有数据可读
    * channelReadComplete()：服务端每次读完一次完整的数据之后，回调该方法，表示数据读取完毕
  * channelInactive() -> channelUnregistered() -> handlerRemoved()
  * 在服务端每隔一秒输出当前客户端的连接数、统计客户端的入口流量，以字节为单位

* 热插拔

  *  `AuthHandler` 里面处理掉身份认证相关的逻辑，后续所有的 handler 都不用操心身份认证这个逻辑

    ```java
    // 继承自 ChannelInboundHandlerAdapter，覆盖了 channelRead() 方法，表明可处理所有类型的数据
    public class AuthHandler extends ChannelInboundHandlerAdapter {
    
        @Override
        public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
            if (!LoginUtil.hasLogin(ctx.channel())) {
                ctx.channel().close();
            } else {
                // 一行代码实现逻辑的删除
                ctx.pipeline().remove(this);
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

* 服务端Handler单例模式

  * 问题：每次有新连接到来的时候，都会调用 `ChannelInitializer` 的 `initChannel()` 方法，然后这里多个指令相关的 handler 都会被 new 一次

  * 解决：每一个指令 handler内部都是没有成员变量的，即无状态，完全可以使用单例模式，不需要每次都 new，提高效率，也避免了创建很多小的对象。不过，其实所有的状态都可以绑定在 channel 的属性上，依然是可以改造成单例模式

    ```java
    // 1. 加上注解标识，表明该 handler 是可以多个 channel 共享的
    @ChannelHandler.Sharable
    public class LoginRequestHandler extends SimpleChannelInboundHandler<LoginRequestPacket> {
        // 2. 构造单例
        public static final LoginRequestHandler INSTANCE = new LoginRequestHandler();
        protected LoginRequestHandler() {
        }
    }
    ```

  * 如果Handler内部实现是与每个 `channel` 有关，如每个 `Spliter` 需要维持每个 channel 当前读到的数据，也就是说它是有状态的

* 缩短事件传播路径

  ```java
  @ChannelHandler.Sharable
  // Packet接口
  public class IMHandler extends SimpleChannelInboundHandler<Packet> {
  		// IMHandler 是无状态的，依然是可以写成一个单例模式的类
      public static final IMHandler INSTANCE = new IMHandler();
      private Map<Byte, SimpleChannelInboundHandler<? extends Packet>> handlerMap;
  
      private IMHandler() {
          handlerMap = new HashMap<>();
  
          handlerMap.put(MESSAGE_REQUEST, MessageRequestHandler.INSTANCE);
          handlerMap.put(CREATE_GROUP_REQUEST, CreateGroupRequestHandler.INSTANCE);
          //...
      }
  
      @Override
      protected void channelRead0(ChannelHandlerContext ctx, Packet packet) throws Exception {
          handlerMap.get(packet.getCommand()).channelRead(ctx, packet);
      }
  }
  ```

  * 客户端多数情况下是单连接的，还是保持原样即可

* 更改事件传播源

  * `ctx.writeAndFlush()` 是从 pipeline 链中的当前节点开始往前找到第一个 outBound 类型的 handler 把对象往前进行传播，如果这个对象确认不需经过其他 outBound 类型的 handler 处理，就使用这个方法
  * `ctx.channel().writeAndFlush()` 是从 pipeline 链中的最后一个 outBound 类型的 handler 开始，把对象往前进行传播，如果确认当前创建的对象需要经过后面的 outBound 类型的 handler，那么就调用此方法

* 减少阻塞主线程的操作及准确统计处理时长

  * 对于耗时的操作，需要把这些耗时的操作丢到业务线程池中去处理，NIO 线程是会有很多 channel 共享的，不能阻塞他

  * 对于统计耗时的场景，如果在自定义业务线程中调用类似 `writeAndFlush()` 的异步操作，需要通过添加监听器的方式来统计

    ```java
    ThreadPool threadPool = xxx;
    
    protected void channelRead0(ChannelHandlerContext ctx, T packet) {
        threadPool.submit(new Runnable() {
            long begin = System.currentTimeMillis();
            // 1. balabala 一些逻辑
            // 2. 数据库或者网络等一些耗时的操作
            // 3. writeAndFlush
            xxx.writeAndFlush().addListener(future -> {
                if (future.isDone()) {
                    // 4. balabala 其他的逻辑
                    long time =  System.currentTimeMillis() - begin;
                }
            });
        })
    }
    ```

* 心跳

  * 假死

    * 在某一端（服务端或者客户端）看来，底层的 TCP 连接已经断开了，但是应用程序并没有捕获到，因此会认为这条连接仍然是存在的，从 TCP 层面来说，只有收到四次握手数据包或者一个 RST 数据包，连接的状态才表示已断开

    * 问题

      * 对于服务端来说，因为每条连接都会耗费 cpu 和内存资源，大量假死的连接会逐渐耗光服务器的资源，最终导致性能逐渐下降，程序奔溃。
      * 对于客户端来说，连接假死会造成发送数据超时，影响用户体验

    * 原因

      * 应用程序出现线程堵塞，无法进行数据的读写
      * 客户端或者服务端网络相关的设备出现故障，比如网卡，机房故障
      * 公网丢包。公网环境相对内网而言，非常容易出现丢包，网络抖动等现象，如果在一段时间内用户接入的网络连续出现丢包现象，那么对客户端来说数据一直发送不出去，而服务端也是一直收不到客户端来的数据，连接就一直耗着

    * 解决

      * 服务端

        * 如果能一直收到客户端发来的数据，那么可以说明这条连接还是活的，因此，服务端对于连接假死的应对策略就是空闲检测

        ```java
        // 服务端和客户端最前面 定义检测到假死连接之后的逻辑
        public class IMIdleStateHandler extends IdleStateHandler {
          private static final int READER_IDLE_TIME = 15;
          public IMIdleStateHandler() {
            // 读空闲时间，指的是在这段时间内如果没有数据读到，就表示连接假死
            // 写空闲时间，指的是 在这段时间如果没有写数据，就表示连接假死
            // 读写空闲时间，表示在这段时间内如果没有产生数据读或者写，就表示连接假死
            		//写空闲和读写空闲为0，表示不关心者两类条件
            super(READER_IDLE_TIME, 0, 0, TimeUnit.SECONDS);
          }
        
          @Override
          protected void channelIdle(ChannelHandlerContext ctx, IdleStateEvent evt) {
            System.out.println(READER_IDLE_TIME + "秒内未读到数据，关闭连接");
            ctx.channel().close();
          }
        }
        ```

      * 客户端

        * 服务端在一段时间内没有收到客户端的数据，这个现象产生的原因：连接假死或非假死状态下确实没有发送数据
        * 客户端定时发送心跳

        ```java
        // 最后面
        public class HeartBeatTimerHandler extends ChannelInboundHandlerAdapter {
            private static final int HEARTBEAT_INTERVAL = 5;
            @Override
            public void channelActive(ChannelHandlerContext ctx) throws Exception {
                scheduleSendHeartBeat(ctx);
                super.channelActive(ctx);
            }
        
            private void scheduleSendHeartBeat(ChannelHandlerContext ctx) {
                ctx.executor().schedule(() -> {
                    if (ctx.channel().isActive()) {
                        ctx.writeAndFlush(new HeartBeatRequestPacket());
                        scheduleSendHeartBeat(ctx);
                    }
                }, HEARTBEAT_INTERVAL, TimeUnit.SECONDS);
            }
        }
        ```

        * 服务端只要在收到心跳之后回复客户端，给客户端发送一个心跳响应包即可

        ```java
        // 无需登录，放置在 AuthHandler 前面
        @ChannelHandler.Sharable
        public class HeartBeatRequestHandler extends SimpleChannelInboundHandler<HeartBeatRequestPacket> {
            public static final HeartBeatRequestHandler INSTANCE = new HeartBeatRequestHandler();
            private HeartBeatRequestHandler() {
            }
        
            @Override
            protected void channelRead0(ChannelHandlerContext ctx, HeartBeatRequestPacket requestPacket) {
                ctx.writeAndFlush(new HeartBeatResponsePacket());
            }
        }
        ```

      * 如何实现客户端在断开连接之后自动连接并重新登录？

https://github.com/lightningMan/flash-netty.git

# Fun

* 客户端与服务端双向通信
* 客户端登录
* 客户端与服务端收发消息
* 构建客户端与服务端Pipeline
* 拆包粘包
* 热插拔客户端身份校验
*  客户端互聊
* 群聊发起与通知
* 群聊成员管理：加入、退出、获取成员列表
* 群聊消息的收发
* 心跳与空闲检测

# Util

* IDUtil
* SessionUtil

# Session

* Session

# Attribute

* Attributes

# Serialize

* Serializer：接口，序列化算法、二进制转Java对象、Java对象转二进制、默认实现
* SerializerAlgorithm：接口，序列化算法统计常量
* JsonSerializer：序列化实现

# Protocol

- Packet：版本常量、指令抽象方法
- PacketCodec：构造函数中维护所有的算法实现`Map<Byte, Serializer>`及指令方法`Map<Byte, Class<? extends Packet>>`、根据自定义协议（魔数4版本1算法1指令1数据长度4数据n）组成进行解码和编码：`public void encode(ByteBuf byteBuf, Packet packet)`、`public Packet decode(ByteBuf byteBuf)`

## command

* command：接口，各种指令，如`Byte LOGIN_REQUEST = 1;`

## request

* 都继承Packet，指令对象

* CreateGroupRequestPacket
* GroupMessageRequestPacket
* HeartBeatRequestPacket
* JoinGroupRequestPacket
* ListGroupMembersRequestPacket
* LoginRequestPacket
* LogoutRequestPacket
* MessageRequestPacket
* QuitGroupRequestPacket

## response

* 都继承Packet，指令对象

* CreateGroupResponsePacket
* GroupMessageResponsePacket
* HeartBeatResponsePacket
* JoinGroupResponsePacket
* ListGroupMembersResponsePacket
* LoginResponsePacket
* LogoutResponsePacket
* MessageResponsePacket
* QuitGroupResponsePacket

# Codec

- Spliter：拆包处理器，继承`LengthFieldBasedFrameDecoder`，数据为自定义协议大小时才进行下一步处理及判断魔数以检测是否是自定义协议
- PacketDecoder：继承`MessageToMessageDecoder<ByteBuf>`，解码器
- PacketEncoder：继承`MessageToByteEncoder<Packet>`，编码器
- PacketCodecHandler：继承`MessageToMessageCodec<ByteBuf, Packet>`，编解码器

# Handler

* IMIdleStateHandler：继承`IdleStateHandler`，空闲检测

# Server

* NettyServer

## Handler

* 业务逻辑处理器，SimpleChannelInboundHandler自动判断对象类型并向下传递

* AuthHandler：继承`ChannelInboundHandlerAdapter`，判断是否登录，是则移除自己
* CreateGroupRequestHandler：继承`SimpleChannelInboundHandler<CreateGroupRequestPacket>`
* GroupMessageRequestHandler：继承`SimpleChannelInboundHandler<GroupMessageRequestPacket>`
* HeartBeatRequestHandler：继承`SimpleChannelInboundHandler<HeartBeatRequestPacket>`
* IMHandler：继承`SimpleChannelInboundHandler<Packet>`，维护各种单例类业务处理器，统一入口
* JoinGroupRequestHandler：继承`SimpleChannelInboundHandler<JoinGroupRequestPacket>`
* ListGroupMembersRequestHandler：继承`SimpleChannelInboundHandler<ListGroupMembersRequestPacket>`
* LoginRequestHandler：继承`SimpleChannelInboundHandler<LoginRequestPacket>`
* LogoutRequestHandler：继承`SimpleChannelInboundHandler<LogoutRequestPacket>`
* MessageRequestHandler：继承`SimpleChannelInboundHandler<MessageRequestPacket>`
* QuitGroupRequestHandler：继承`SimpleChannelInboundHandler<QuitGroupRequestPacket>`

# Client

* NettyClient

## console

* ConsoleCommand：接口，`void exec(Scanner scanner, Channel channel);`

### command

* 实现`ConsoleCommand`

* ConsoleCommandManager：维护各种登录后的客户端操作`private Map<String, ConsoleCommand>`
* CreateGroupConsoleCommand
* JoinGroupConsoleCommand
* ListGroupMembersConsoleCommand
* LoginConsoleCommand
* LogoutConsoleCommand
* QuitGroupConsoleCommand
* SendToGroupConsoleCommand
* SendToUserConsoleCommand

## handler

* CreateGroupResponseHandler：继承`SimpleChannelInboundHandler<CreateGroupResponsePacket>`
* GroupMessageResponseHandler：继承`SimpleChannelInboundHandler<GroupMessageResponsePacket>`
* HeartBeatTimerHandler：继承`ChannelInboundHandlerAdapter`
* JoinGroupResponseHandler：继承`SimpleChannelInboundHandler<JoinGroupResponsePacket>`
* ListGroupMembersResponseHandler：继承`SimpleChannelInboundHandler<ListGroupMembersResponsePacket>`
* LoginResponseHandler：继承`SimpleChannelInboundHandler<LoginResponsePacket>`
* LogoutResponseHandler：继承`SimpleChannelInboundHandler<LogoutResponsePacket>`
* MessageResponseHandler：继承`SimpleChannelInboundHandler<MessageResponsePacket>`
* QuitGroupResponseHandler：继承`SimpleChannelInboundHandler<QuitGroupResponsePacket>`







