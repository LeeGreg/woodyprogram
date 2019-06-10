# Java NIO 组件

* 在 Java 1.4 中推出了 NIO
* Java NIO 的工作原理：
  * 由一个专门的线程来处理所有的 IO 事件，并负责分发
  * 事件驱动机制：事件到的时候触发，而不是同步的去监视事件
  * 线程通讯：线程之间通过 `wait,notify` 等方式通讯。保证每次上下文切换都是有意义的。减少无谓的线程切换
  * 反应堆是个抽象的概念，selector是具体的行为的体现

* Java NIO channel和buffer

  * 数据总是从channel读取到buffer，或者从buffer写入到channel

* Java NIO Selector对象

  * 能够通过事件（如连接打开、数据到达）来监听多个channel，因此，单个线程能够监听多个channel的数据

* Java NIO 是非阻塞IO

  * 线程从channel中读取数据到buffer时，该线程还能够做其他事情，一旦数据读取到buffer后，线程能够继续处理该数据。对于写数据也是一样的。

  ```java
  //1. 分配空间
  ByteBuffer bb = ByteBuffer.allocate(1024);
  //2. 向Buffer中写入数据
   从Channel -> Buffer：channel.read(bb);
   从Client -> Buffer：bb.put();
  //3. 从Buffer中读取数据
   从Buffer -> Channel：channel.write(bb);
   从Buffer -> Server：bb.get();
  ```

* 网络传输一定要编码。网络通信的时候采用二进制形式，用可传输字节流的方式进行传输

# 同步与阻塞

* 阻塞和非阻塞是进程在访问数据的时候，数据是否准备就绪的一种处理方式
  * 阻塞：当进程访问数据缓冲区的时候，进程需要等待缓冲区中的数据准备好过后才处理其他的事情，否则一直等待在那
  * 非阻塞：当进程访问数据缓冲区的时候，如果数据没有准备好则直接返回，不会等待
* 同步和异步都是基于应用程序和操作系统处理 IO 事件所采用的方式
  * 同步：是应用程序要直接参与 IO 读写的操作
    * 同步方式在处理 IO 事件的时候，必须阻塞在某个方法上面等待 IO 事件完成(阻塞 IO 事件或者通过轮询 IO 事件的方式)
  * 异步：所有的 IO 读写交给操作系统去处理，应用程序可以去做其他的事情，只需要等待通知

# Java IO模型

- 1:1同步阻塞IO通信模型，一个客户端对应一个线程
- M：N同步阻塞IO通信模型，使用线程池
- 非阻塞式IO模型(NIO)，NIO+单线程Reactor模式
- 非阻塞式IO模型(NIO)，NIO+多线程Reactor模式
- NIO+主从多线程Reactor模式

## Channel

* `FileChannel(Files)`、`DatagramChannel(UDP)`、

* `SocketChannel`(TCP)

* `ServerSocketChannel`(能够像web服务器一样监听tcp连接，对于每个连接都创建一个`SocketChannel`)

* `Channel`能够读写，流只能够读或者写；Channel能够异步读写；

* `FileChannel`

  * Java NIO FileChannel可以使用Java IO API读取文件
  * 两个FileChannel之间可以直接传输数据，连接文件，只能是阻塞模式

  * `transferFrom()`和`transferTo()`

    ```java
    // 必须通过InputStream, OutputStream, or a RandomAccessFile来打开FileChannel
    RandomAccessFile fromFile = new RandomAccessFile("fromFile.txt", "rw");
    FileChannel	fromChannel = fromFile.getChannel();
    
    RandomAccessFile toFile = new RandomAccessFile("toFile.txt", "rw");
    FileChannel	toChannel = toFile.getChannel();
    
    long position = 0;
    long count = fromChannel.size();
    
    //某个资源FileChannel数据传输到FileChannel
    toChannel.transferFrom(fromChannel, position, count);
    //一个FileChannel数据传输到其他FileChannel
    fromChannel.transferTo(position, count, toChannel);
    ```

    ```java
    // 从FileChannel中读取数据 
    ByteBuffer buf = ByteBuffer.allocate(48);
    int bytesRead = inChannel.read(buf);  // -1 代表读取完
    // 写入数据到FileChannel
    String newData = "New String to write to file..." + System.currentTimeMillis();
    ByteBuffer buf = ByteBuffer.allocate(48);
    buf.clear();
    buf.put(newData.getBytes());
    buf.flip();
    while(buf.hasRemaining()) {
        channel.write(buf);
    } 
    //关闭FileChannel
    channel.close();  
    
    //FileChannel Position
    //position()获取当前FileChannel的position
    long pos channel.position();
    //position(long pos)设置当前FileChannel的position
    channel.position(pos +123);
    
    //获取FileChannel连接的文件大小
    long fileSize = channel.size();
    //截取文件
    channel.truncate(1024);
    // FileChannel force()
    // 将Channel中所有未写的数据刷新到硬盘中，避免缓冲在内存中
    channel.force(true); //是否需要被刷新到硬盘
    ```

* SocketChannel

  * 连接到一个TCP的Socket，等价于Java网络编程的Socket

    ```java
    // 创建SocketChannel
    // 方式一，创建SocketChannel并连接到网络上的一个服务器
    SocketChannel socketChannel = SocketChannel.open();
    socketChannel.connect(new InetSocketAddress("http://jenkov.com", 80));
    // 方式二，当一个连接到到ServerSocketChannel时，一个SocketChannel将被创建
    
    // 关闭SocketChannel
    socketChannel.close(); 
    
    // 从SocketChannel读取数据到Buffer
    ByteBuffer buf = ByteBuffer.allocate(48);
    int bytesRead = socketChannel.read(buf);  //-1表示读完，channel关闭
    
    // 将Buffer中数据写入到SocketChannel中
    String newData = "New String to write to file..." + System.currentTimeMillis();
    ByteBuffer buf = ByteBuffer.allocate(48);
    buf.clear();
    buf.put(newData.getBytes());
    buf.flip();
    while(buf.hasRemaining()) {
        channel.write(buf);
    }
    
    //将SocketChannel设置成非阻塞模式，此时可以异步连接、读取、写入
    socketChannel.configureBlocking(false);
    socketChannel.connect(new InetSocketAddress("http://jenkov.com", 80));
    //connect方法返回时连接可能还没建立
    while(! socketChannel.finishConnect() ){
        //wait, or do something else...    
    }
    // write()
    // 由于是非阻塞，异步形式，所以可能没有写入任何数据，需要在循环里调用这个方法
    // read()
    // 由于是非阻塞，异步形式，可能没有数据可读取，需要注意返回的int（多少数据被读取）
    ```

* ServerSocketChannel

  * 是一个能够监听TCP连接的Channel，就像一个Java网络编程中的ServerSocket

    ```java
    // 打开一个ServerSocketChannel
    ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
    serverSocketChannel.socket().bind(new InetSocketAddress(9999));
    while(true){
      	// accept() 监听连接，该方法返回时建立一个连接的SocketChannel
        // 该方法将阻塞直到一个连接到来
        SocketChannel socketChannel = serverSocketChannel.accept();
        //do something with socketChannel...
    }
    // 关闭一个ServerSocketChannel
    serverSocketChannel.close();
    
    // ServerSocketChannel设置成非阻塞模式，立即返回，所以需要判断返回的SocketChannel是否为null
    ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
    serverSocketChannel.socket().bind(new InetSocketAddress(9999));
    serverSocketChannel.configureBlocking(false);
    while(true){
        SocketChannel socketChannel = serverSocketChannel.accept();
        if(socketChannel != null){
            //do something with socketChannel...
        }
    }
    ```

## Buffer

* `ByteBuffer`、`ShortBuffer`、`CharBuffer`、`IntBuffer`、`FloatBuffer`、`DoubleBuffer`、`LongBuffer`

* 可以理解成一个特殊的数组，内置一些属性能够跟踪和记录缓冲区的状态变化情况

* 属性

  - capacity
  - position：值由 get()/put()方法自动更新
  - limit
    - capacity：buffer初始大小
    - 写模式：开始position为0，写入数据就增加，最大为capacity-1，limit=capacity
    - 读模式：调用flip()从写模式切换到读模式，position为0，limit为切换前position大小，读出数据position就增加，最大为limit大小

* 解决缓冲区满了：

  * 通过协议：先解析存储消息体长度的字节来确定创建多大的缓冲区

* * 分配Buffer，如`ByteBuffer buf = ByteBuffer.allocate(48);`

* 写入Buffer
    * 从Channel写入Buffer，如`int bytesRead = inChannel.read(buf); `
    * Buffer自己写入`buf.put(127);    `
  * 切换模式`flip（）`
    * 将Buffer的写入模式切换到读取模式，将position属性重置为0，limit设置为position重置前大小
  * 读取Buffer
    * 将Buffer读取到Channel，如`int bytesWritten = inChannel.write(buf);`
    * Buffer自己读取，`byte aByte = buf.get(); `
  * `Buffer.rewind()`，将position重置为0，可以再次读取所有Buffer中数据，limit不变
  * `Buffer.clear()`和`Buffer.compact()`
    * 读取完数据之后，写入数据之前调用
      * `clear()`把所有状态设置为初始值，将`position`设置为0，`limit`设置为`capacity`
      * `compact()`保留未读取数据到最前，`position`为最后一个未读取数据后一位置，`limit`设置为`capacity`
  * `Buffer.mark()`和`Buffer.reset()`
    * `mark()`标记`position`位置，后面调用`reset()`可将`position`设置为`mark()`标记时位置
  * `Buffer.equals()`和`Buffer.compareTo()`
    * `equals()`相同条件：类型相同、剩余未读数据数量相等、剩余未读数据`equal`为`true`
    * `compareTo()`，小于另一个条件：
      * 甲Buffer第一个元素和乙Buffer的某个元素相等，则甲小于乙
      * 所有元素相等，但是甲Buffer先消耗完
  
* Scatter和Gather

  * Scatter：可以将数据从Channel读取到多个Buffer中

    ```java
    ByteBuffer header = ByteBuffer.allocate(128);
    ByteBuffer body   = ByteBuffer.allocate(1024);
    //第一个Buffer写满了再写入到第二个
    ByteBuffer[] bufferArray = { header, body };
    channel.read(bufferArray);
    ```

  * Gather：将多个Buffer中数据写入到Channel中

    ```java
    ByteBuffer header = ByteBuffer.allocate(128);
    ByteBuffer body   = ByteBuffer.allocate(1024);
    //write data into buffers
    // 只写入Buffer的capacity到limit之间的数据
    ByteBuffer[] bufferArray = { header, body };
    channel.write(bufferArray);
    ```

* 使用

  ```java
  //1. 数据写入buffer，
  //2. buffer.flip()
  //3. 从buffer读出数据
  //4. 调用buffer.clear()或buffer.compact()
  buffer能够记录写入了多少，读取buffer之前要先buffer.flip()切换成buffer读取模式，能够读取之前所有写入的数据，读取后调用buffer.clear()清空buffer或buffer.compact()清空已读取并将未读取数据置前
  ```

  ```java
  RandomAccessFile aFile = new RandomAccessFile("data/nio-data.txt", "rw");
  FileChannel inChannel = aFile.getChannel();
  // buffer容量为48字节
  // 初始状态：position=0；limit=capacity;
  ByteBuffer buf = ByteBuffer.allocate(48);
  //将数据从channel读入到buffer
  int bytesRead = inChannel.read(buf);
  while (bytesRead != -1) {
    System.out.println("Read " + bytesRead);
    //切换成读取模式
    buf.flip();
    while(buf.hasRemaining()){
      // 一次获得一个字节数据
      System.out.print((char) buf.get());
    }
    //清空buffer，准备读入
    buf.clear();
    bytesRead = inChannel.read(buf);
  }
  aFile.close();
  ```

* 缓冲区分片

  * 在现有缓冲区上切出一片来作为一个新的缓冲区，但现有的缓冲区与创建的子缓冲区在底层数组层面上是数据共享的

    ```java
    // 创建子缓冲区  
    buffer.position( 3 );  
    buffer.limit( 7 );  
    ByteBuffer slice = buffer.slice(); //调用 slice()方法可以创建一个子缓冲区
    ```

* 只读缓冲区

  * 调用缓冲区的`asReadOnlyBuffer()`方法，将任何常规缓冲区转换为只读缓冲区，这个方法返回一个与原缓冲区完全相同的缓冲区，并与原缓冲区共享数据，只不过它是只读的

* 直接缓冲区

  * 为加快 I/O 速度，使用一种特殊方式为其分配内存的缓冲区
  * 需要调用` allocateDirect()`方法，而不是 allocate()方法，使用方式与普通缓冲区并无区别
  * Direct Bufer创建和销毁过程中，都会比一般的堆内Bufer增加部分开销，所以通常都建议用于长期使用、数据较大的场景

* 内存映射文件 I/O

  * MappedByteBufer将文件按照指定大小直接映射为内存区域，当程序访问这个内存区域时将直接操作这块儿文件数据，省去了将数据从内核空间向用户空间传输的损耗。本质上也是种Direct Bufer

  * 是一种读和写文件数据的方法，它可以比常规的基于流或者基于通道的 I/O 快的多

  * 是通过使文件中的数据出现为内存数组的内容来完成的，这其初听起来似乎不过就是将整个文件读到内存中，但是事实上并不是这样。一般来说，只有文件中实际读取或者写入的部分才会映射到内存中
  
    ```java
    RandomAccessFile raf = new RandomAccessFile( "e:\\test.txt", "rw" );  
    FileChannel fc = raf.getChannel();    
    //把缓冲区跟文件系统进行一个映射关联
    //只要操作缓冲区里面的内容，文件内容也会跟着改变
    MappedByteBuffer mbb = fc.map( FileChannel.MapMode.READ_WRITE,start, size );       
    mbb.put( 0, (byte)97 );  
    mbb.put( 1023, (byte)122 );  
    raf.close(); 
    ```

## Selector

* 是一个组件，能够注册、检查多个Channel，通过事件通知判断哪些Channel已经准备好读取和写入，使得单个线程能够管理多个Channel，因此能够管理多个网络连接
  * channel注册到selector，然后调用selector的select()方法阻塞直到某个已注册channel的事件准备好，一旦select()方法返回，该线程能够处理这些事件，例如连接事件、数据到达事件
  * 当有读或写等任何注册的事件发生时，可从` Selector `中获得相应的`SelectionKey`，同时从 `SelectionKey`中可找到发生的事件和该事件所发生的具体的`SelectableChannel`，以获得客户端发送过来的数据
  * 使用较少的线程便可以处理许多连接，因此也减少了内存管理和上下文切换所带来开销
  * 当没有 I/O 操作需要处理的时候，线程也可以被用于其他任务
* 适用于打开很多channel，但是只是传输少量数据

* NIO 中非阻塞 I/O 采用了基于 Reactor 模式的工作方式，I/O 调用不会被阻塞，相反是注册感兴趣的特定 I/O 事件，如可读数据到达，新的套接字连接等等，在发生特定事件时，系统再通知我们

* IO多路复用（同步非阻塞）：简单理解为多个客户端使用了同一个selector线程

* 使用

  ```java
  //创建Selector
  Selector selector = Selector.open();
  //Channel必须是非阻塞的
  channel.configureBlocking(false);
  //1. 注册Channel到Selector selector，监听该channel的某些事件集（对该Channel的某些事件感兴趣）
  SelectionKey key = channel.register(selector, SelectionKey.OP_READ);
  // 2. 监听该Channel的事件，有四个，
  //Connect（SelectionKey.OP_CONNECT）：一个Channel已成功连接到一个服务器
  //Accept（SelectionKey.OP_ACCEPT）：一个Server Socket Channel接收到一个连接
  //Read（SelectionKey.OP_READ）：一个Channel有数据可以被读取
  //Write（SelectionKey.OP_WRITE）：一个Channel准备好被写入
  
  //对多个事件感兴趣
  int interestSet = SelectionKey.OP_READ | SelectionKey.OP_WRITE; 
  // 3. 根据不同的事件进行相应的处理
  ```

* SelectionKey

  * Channel注册到Selector时返回

  * 包含属性：

    * The interest set

      ```java
      // selecting时对Channel的某些事件集感兴趣
      // 通过以下SelectionKey可以兴趣集进行读和写
      int interestSet = selectionKey.interestOps();
      
      boolean isInterestedInAccept  = interestSet & SelectionKey.OP_ACCEPT;
      boolean isInterestedInConnect = interestSet & SelectionKey.OP_CONNECT;
      boolean isInterestedInRead    = interestSet & SelectionKey.OP_READ;
      boolean isInterestedInWrite   = interestSet & SelectionKey.OP_WRITE;    
      ```

    * The ready set

      * Channel准备好能够做的操作集

      ```java
      int readySet = selectionKey.readyOps();
      
      selectionKey.isAcceptable();
      selectionKey.isConnectable();
      selectionKey.isReadable();
      selectionKey.isWritable();
      ```

    * The Channel

      `Channel  channel  = selectionKey.channel();`
      
    * The Selector

      `Selector selector = selectionKey.selector(); `
  
    * An attached object (optional)
    
      * 可以通过在SelectionKey中附件一个对象来识别一个给定的Channel，或者在Channel上附件一些信息（如Buffer、对象）

      ```java
  selectionKey.attach(theObject);
      Object attachedObj = selectionKey.attachment();
  // Channel注册时即附加一些信息
      SelectionKey key = channel.register(selector, SelectionKey.OP_READ, theObject);
      ```
  
* 通过Selector选择Channel

  * 通过调用selector()，返回已经准备好的Channel，并且对该Channel的某些事件(connect, accept, read or write)感兴趣

    * 如果对一个Channel的读取事件感兴趣，能够通过select()获得准备被读取的Channel

    ```java
    // 返回的int表示有多少个Channel准备好
    //阻塞直到至少一个所注册Channel的事件准备好
    int select()、int select(long timeout)
    //不阻塞，不管有没有准备好都直接返回
    int selectNow()
    ```

  * 通过调用selector的selectedKeys()返回 "selected key set"来获得已准备好的Channel

    ```java
    Set<SelectionKey> selectedKeys = selector.selectedKeys(); 
    // SelectionKey代表一个Channel与Selector的注册关系
    // 通过selectedKeySet() 获取这些注册关系
    ```

* wakeUp()

  * 一个线程调用一个已阻塞的select()，能够使该线程离开该select()，即使没有Channel准备好
  * 通过一个不同的线程调用Selector.wakeup()，这个在select()中等待的线程将立即返回.

* close()

  * 如果selector使用完了，可以调用其close()，将关闭Selector并且使其上注册的SelectionKey实例失效，但是这些Channel没有关闭

  ```java
  Selector selector = Selector.open();
  channel.configureBlocking(false);
  SelectionKey key = channel.register(selector, SelectionKey.OP_READ);
  while(true) {
    int readyChannels = selector.selectNow();
    if(readyChannels == 0) continue;
    // 可以通过迭代selected key set去获得已准备的Channel
    Set<SelectionKey> selectedKeys = selector.selectedKeys();
    Iterator<SelectionKey> keyIterator = selectedKeys.iterator();
    while(keyIterator.hasNext()) {
      SelectionKey key = keyIterator.next();
      if(key.isAcceptable()) {
          // a connection was accepted by a ServerSocketChannel.
      } else if (key.isConnectable()) {
          // a connection was established with a remote server.
      } else if (key.isReadable()) {
          // a channel is ready for reading
      } else if (key.isWritable()) {
          // a channel is ready for writing
      }
      // selector自己不会主动去移除这些SelectionKey实例
      // 当该Channel事件处理完后，必须手动去移除，下次该Channel的事件已准备时，selector将再次将它添加到selected key set
      keyIterator.remove();
    }
  }
  // SelectionKey.channel() 返回的可能是ServerSocketChannel或者SocketChannel
  ```

# Java NIO: No-blocking Server

## Non-blocking IO Pipelines

* 一个非阻塞IO Pipeline

  * 使用一系列组件来进行非阻塞IO，包括读取IO和写入IO
    * Channel-Selector-Component-Channel
  * 不需要具备读取和写入，有些pipeline只能读取数据，有些只能写入数据
  * 能够同时从多个Channel中读取数据，例如从多个SocketChannel中读取数据

* 数据从哪里读取

  * BIO从流（file或者socket）中读取到一个连续的消息中

    * BIO通过InputStream一次只能读取一个字节，并且InputStream将阻塞直到有数据可读
    * 每个流都需要一个线程去处理消息，因为每个流将阻塞直到有数据可读。单个线程读取流的时候，流中没有数据也不能去读取其他流
    * BIO使用一个线程对应一个连接，当连接很多时会非常占用系统资源

  * NIO可以使用单个线程从多个流中读取数据，要求流能够切换到非阻塞模式

    * 在非阻塞模式中，一次可以从流中读取0个或者多个字节
    * 通过Selector来避免检查流读取0个字节
  
* 一个或多个SelectableChannel实例注册到Selector，通过调用Selector的select()或selectNow()将返回有数据可读的SelectableChannel实例
  
* 当从SelectableChannel中读取数据时不能确定读取了多少数据，可能得到部分、完整、超过完整的数据，所以需要
  
  *  检查是否读取了完整数据
  
  * 如果只读取了部分数据，则需存储该数据直到下部分数据到来
  1.  尽可能复制少的数据，数据越多性能越低
  
  2. 将完整的数据存入到一个连续的字节序列中使其容易处理
  
* 一些协议消息格式使用TLV(Type, Length, Value)格式编码，当消息到达时，消息的总长度存储在消息的开头，这样就可以立即知道为整个消息分配多少内存
  
    * 写入部分数据
      * write(ByteBuffer)返回写入的字节数
      * 确保只有具有要写入消息的Channel实例才能实际注册到Selector

## Server Thread Model

* 两个线程的线程模型

  * 第一个线程从ServerSocketChannel处接收传入连接

  * 第二个线程处理接受的连接，即读取消息，处理消息和将响应写回连接

    ![image-20190417113551410](/Users/dingyuanjie/Library/Application Support/typora-user-images/image-20190417113551410.png)

## Java NIO DatagramChannel

* 是一个用来接收和发送UPD数据包的Channel

  ```java
  // 打开一个DatagramChannel
  DatagramChannel channel = DatagramChannel.open();
  channel.socket().bind(new InetSocketAddress(9999));
  // 接收数据
  ByteBuffer buf = ByteBuffer.allocate(48);
  buf.clear();
  channel.receive(buf);  //数据长度超过Buffer部分将被丢弃
  //发送数据
  String newData = "New String to write to file..."+ System.currentTimeMillis();
  ByteBuffer buf = ByteBuffer.allocate(48);
  buf.clear();
  buf.put(newData.getBytes());
  buf.flip();
  //UDP面向非连接，所以不知道对方是否接收到数据
  int bytesSent = channel.send(buf, new InetSocketAddress("jenkov.com", 80));
  
  // 连接到特定的地址 
  channel.connect(new InetSocketAddress("jenkov.com", 80));  
  int bytesRead = channel.read(buf);    
  int bytesWritten = channel.write(buf);
  ```

## Java NIO Pipe

* Java NIO Pipe是两个线程之间的单向数据连接

* 管道具有源通道和接收器通道。将数据写入接收器通道（sink channel），然后可以从源通道读取该数据（ source channel）

  ![image-20190417115904872](/Users/dingyuanjie/Library/Application Support/typora-user-images/image-20190417115904872.png)

  ```java
  // 创建一个Pipe
  Pipe pipe = Pipe.open();
  // 写入到Pipe
  Pipe.SinkChannel sinkChannel = pipe.sink();
  
  String newData = "New String to write to file..." + System.currentTimeMillis();
  ByteBuffer buf = ByteBuffer.allocate(48);
  buf.clear();
  buf.put(newData.getBytes());
  buf.flip();
  while(buf.hasRemaining()) {
      sinkChannel.write(buf);
  }
  
  //从Pipe中读取
  Pipe.SourceChannel sourceChannel = pipe.source();
  ByteBuffer buf = ByteBuffer.allocate(48);
  int bytesRead = inChannel.read(buf);
  ```

# Java NIO 与 Java IO

* IO：

  * 面向流
    * 从流中一次读取一个或多个字节，不会缓存在任何地方，无法在流中前后移动数据，除非先将其缓存在缓冲区中。
  * 阻塞IO
    * Java IO的各种流都是阻塞的。当线程调用read()或write()时，该线程将被阻塞，直到有一些数据要读取，或者数据被完全写入。在此期间，该线程无法执行任何其他操作。

* NIO：

  * 面向Buffer
    * 数据被读入缓冲区，稍后处理该缓冲区。可以根据需要在缓冲区中前后移动，处理具有灵活性。但是，还需要检查缓冲区是否包含完整的所需处理数据，并且要确保后面的数据不覆盖前面尚未处理的数据
  * 非阻塞
    * 线程从Channel中请求数据时，读取不到或者只能读取部分数据，该线程不会阻塞直到所有可读数据准备好可读取，而是可以去做其他事情。线程不必等所有可写数据都准备好才写入到Channel中，当没有数据可写入时，线程也可以去做其他事情
  * Selector
    * 一个线程可以管理多个Channel的输入和输出：当线程没有阻塞在IO调用上时，它将执行其他Channel的IO
    * 线程通常将非阻塞 IO 的空闲时间用于在其它通道上执行 IO 操作，所以一个单独的线程现在可以管理多个输入和输出通道(channel)
    * 选择器机制使单个线程可以轻松管理多个通道：
      - 注册多个Channel到Selector，然后单个线程去检查并获取那些能够写入和读取的Channel

* NIO和IO如何影响应用程序设计

  * API使用

    * Java NIO并不是仅从一个InputStream 逐字节读取，而是数据必须先读入缓冲区再处理

  * 数据处理

    * IO从InputStream或者Reader中逐个字节读取

      ```java
      FileInputStream input = new FileInputStream("d://info.txt");  // get the InputStream from the client socket
      BufferedReader reader = new BufferedReader(new InputStreamReader(input));
      //readLine()阻塞直到一行读完，仅在有新数据读入时运行，并知道每步的数据是什么
      String nameLine   = reader.readLine();
      String ageLine    = reader.readLine();
      ```

    * NIO从Channel中读取数据到Buffer中

      ```java
      ByteBuffer buffer = ByteBuffer.allocate(48);
      // read方法返回时，只知道读取了一些数据，不知道是否读取了完整的数据
      int bytesRead = inChannel.read(buffer);
      //  bufferFull方法返回Buffer是否填充满
      while(! bufferFull(bytesRead) ) {
          bytesRead = inChannel.read(buffer);
      }
      ```

  * 处理数据所使用的线程数

    * NIO 可让只使用一个(或几个)单线程管理多个通道(网络连接或文件)，但付出的代价是解析数据可能会比从一个阻塞流中读取数据更复杂

* 总之

  * 如果要同时管理上千个只发送少量数据的连接，例如聊天服务器，那么使用NIO作为服务器有优势；同样，如果需要维护一些和其他计算机的连接，例如在P2P网络中，使用单个线程去管理所有外部连接有优势
  * 如果有需发送大量数据、占用高宽带的少量连接，则使用IO Server比较适合，一个连接通过一个线程处理
  * ![各IO操作对比](/Users/dingyuanjie/Desktop/notes/书单/各IO操作对比.png)

# Java Path 

* Java Path（`java.nio.file.Path`）实例代表了一个文件系统的路径，能够指向一个文件或目录，分绝对路径和相对路径

  ```java
  //创建Path实例
  // 创建绝对路径
  // Windows系统绝对路径
  Path path = Paths.get("c:\\data\\myfile.txt");
  // Linux, MacOS系统绝对路径
  Path path = Paths.get("/home/jakobjenkov/myfile.txt");
  
  // 创建相对路径
  Paths.get(basePath, relativePath)
  Path projects = Paths.get("d:\\data", "projects");
  Path file     = Paths.get("d:\\data", "projects\\a-project\\myfile.txt");
  //. 表示 当前目录
  Path currentDir = Paths.get(".");
  System.out.println(currentDir.toAbsolutePath());
  //d:\data\projects\a-project
  Path currentDir = Paths.get("d:\\data\\projects\.\a-project");
  //..表示父级目录
  Path parentDir = Paths.get("..");
  //d:\data\projects\another-project
  String path = "d:\\data\\projects\\a-project\\..\\another-project";
  Path parentDir2 = Paths.get(path);
  
  //Path.normalize()
  //移除. and .. 
  ```

# Java AIO

* jdk1.7 (NIO2)才是实现真正的异步 aio、把 IO 读写操作完全交给操作系统，学习了 linux epoll 模式
* 服务端：`AsynchronousServerSocketChannel`
* 客服端：`AsynchronousSocketChannel`
* 用户处理器：`CompletionHandler` 这个接口实现应用程序向操作系统发起 IO 请求，当完成后处理具体逻辑，否则做自己该做的事情
* “真正”的异步IO需要操作系统更强的支持。
* 在IO多路复用模型中，事件循环将文件句柄的状态事件通知给用户线程，由用户线程自行读取数据、处理数据
* 而在异步IO模型中，当用户线程收到通知时，数据已经被内核读取完毕，并放在了用户线程指定的缓冲区内，内核在IO完成后通知用户线程直接使用即可
* 异步IO模型使用了`Proactor`设计模式实现了这一机制
* ![Proactor设计模式](/Users/dingyuanjie/Desktop/notes/书单/Proactor设计模式.png)

# AsynchronousFileChannel

```java
// 创建
Path path = Paths.get("data/test.xml");
AsynchronousFileChannel fileChannel =
    AsynchronousFileChannel.open(path, StandardOpenOption.READ);

// 通过Future读取数据，（ByteBuffer，position）
AsynchronousFileChannel fileChannel = 
    AsynchronousFileChannel.open(path, StandardOpenOption.READ);
ByteBuffer buffer = ByteBuffer.allocate(1024);
long position = 0;
Future<Integer> operation = fileChannel.read(buffer, position);
//read会立即返回，所需需要阻塞到数据读取完
while(!operation.isDone());
buffer.flip();
byte[] data = new byte[buffer.limit()];
buffer.get(data);
System.out.println(new String(data));
buffer.clear();

// 通过CompletionHandler读取数据
fileChannel.read(buffer, position, buffer, new CompletionHandler<Integer, ByteBuffer>() {
    // 当数据读取操作完成后，completed方法将被调用（读取了多少字节，附加Buffer）
    @Override
    public void completed(Integer result, ByteBuffer attachment) {
        System.out.println("result = " + result);
        attachment.flip();
        byte[] data = new byte[attachment.limit()];
        attachment.get(data);
        System.out.println(new String(data));
        attachment.clear();
    }
    @Override
    public void failed(Throwable exc, ByteBuffer attachment) {

    }
});
```

```java
// 通过Future写入数据
Path path = Paths.get("data/test-write.txt");
if(!Files.exists(path)){
    Files.createFile(path);
}
AsynchronousFileChannel fileChannel = 
    AsynchronousFileChannel.open(path, StandardOpenOption.WRITE);
ByteBuffer buffer = ByteBuffer.allocate(1024);
long position = 0;
buffer.put("test data".getBytes());
buffer.flip();
Future<Integer> operation = fileChannel.write(buffer, position);
buffer.clear();
while(!operation.isDone());
System.out.println("Write done");

//通过CompletionHandler写入数据
Path path = Paths.get("data/test-write.txt");
if(!Files.exists(path)){
    Files.createFile(path);
}
AsynchronousFileChannel fileChannel = 
    AsynchronousFileChannel.open(path, StandardOpenOption.WRITE);
ByteBuffer buffer = ByteBuffer.allocate(1024);
long position = 0;
buffer.put("test data".getBytes());
buffer.flip();

fileChannel.write(buffer, position, buffer, new CompletionHandler<Integer, ByteBuffer>() {
    @Override
    public void completed(Integer result, ByteBuffer attachment) {
        System.out.println("bytes written: " + result);
    }
    @Override
    public void failed(Throwable exc, ByteBuffer attachment) {
        System.out.println("Write failed");
        exc.printStackTrace();
    }
});
```

# BIO Demo

```java
public class ThreadPoolUtil {

    private ThreadPoolUtil(){}

    private static class ThreadPoolHolder{
        static ThreadFactory nameThreadFactory = new ThreadFactoryBuilder().setNameFormat("demo-pool-%d").build();
        private static final ExecutorService threadPool = new ThreadPoolExecutor(5, 200,
                0L,
                TimeUnit.MILLISECONDS,
                new LinkedBlockingQueue<Runnable>(1024),
                nameThreadFactory,
                new ThreadPoolExecutor.AbortPolicy());
    }

    public static final ExecutorService getThreadPool() {
        return ThreadPoolHolder.threadPool;
    }
}
```

```java
public class Test {

    public static void main(String[] args) throws InterruptedException {

        //运行服务器
        ThreadPoolUtil.getThreadPool().execute(new Runnable() {
            @Override
            public void run() {
                try {
                    BIOServer.start();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

        //防止客户端先于服务器启动前执行代码
        Thread.sleep(100);

        final char[] op = {'+', '-', '*', '/'};
        final Random random = new Random(System.currentTimeMillis());

        ThreadPoolUtil.getThreadPool().execute(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    //随机产生算术表达式
                    String expression = random.nextInt(10) + "" + op[random.nextInt(4)] +
                            (random.nextInt(10) + 1);
                    try {
                        BIOClient.send(expression);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    try {
                        Thread.sleep(random.nextInt(1000));
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }
}
```

```java
public class BIOServer {

    private static int DEFAULT_PORT = 7777;
    // 单例的ServerSocket
    private static ServerSocket serverSocket;

    public static void start() throws IOException {
        start(DEFAULT_PORT);
    }

    //不会被大量访问，不太需要考虑效率，直接进行方法同步就行了
    public synchronized static void start(int port) throws IOException {
        if (serverSocket != null) {
            return;
        }
        try {
            serverSocket = new ServerSocket(port);
            System.out.println("服务端已启动，端口号：" + port);

            while (true) {
                // 如果没有客户端连接，则阻塞在accept操作上
                Socket socket = serverSocket.accept();
                ThreadPoolUtil.getThreadPool().execute(new ServerHandler(socket));
            }
        } finally {
            if (serverSocket != null) {
                System.out.println("服务端已关闭");
                serverSocket.close();
            }
        }
    }
}
```

```java
public class ServerHandler implements Runnable {

    private Socket socket;

    public ServerHandler(Socket socket) {
        this.socket = socket;
    }

    @Override
    public void run() {
        BufferedReader in = null;
        PrintWriter out = null;

        try {
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            //问题所在out = new PrintWriter(socket.getOutputStream());
            //没有自动冲刷消息到通道 应该 out = new PrintWriter(socket.getOutputStream(), true);
            out = new PrintWriter(socket.getOutputStream(), true);
            String expression;
            int result;
            while (true) {
                // 通过BufferReader读取一行，读完返回null
                if ((expression = in.readLine()) == null) {
                    break;
                }
                System.out.println("服务端收到消息：" + expression);
                result = Calculator.cal(expression);
                out.println(result);
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            //清理 in out socket
            if(in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            if(out != null) {
                out.close();
            }
            if(socket != null) {
                try {
                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
```

```java
public class Calculator {

    public static int cal(String expression) throws Exception {
        char op = expression.charAt(1);
        switch (op) {
            case '+':
                return (expression.charAt(0) - 48) + (expression.charAt(2) - 48);
            case '-':
                return (expression.charAt(0) - 48) - (expression.charAt(2) - 48);
            case '*':
                return (expression.charAt(0) - 48) * (expression.charAt(2) - 48);
            case '/':
                return (expression.charAt(0) - 48) / (expression.charAt(2) - 48);
            default:
                throw new Exception("Calculator error");
        }
    }
}
```

```java
public class BIOClient {

    //默认的端口号
    private static int DEFAULT_SERVER_PORT = 7777;
    private static String DEFAULT_SERVER_IP = "127.0.0.1";
    public static void send(String expression) throws IOException {
        send(DEFAULT_SERVER_PORT, expression);
    }
    public static void send(int port, String expression) throws IOException {
        System.out.println(("算术表达式为：" + expression));
        Socket socket = null;

        BufferedReader in = null;
        PrintWriter out = null;

        try {
            socket = new Socket(DEFAULT_SERVER_IP, port);
            out = new PrintWriter(socket.getOutputStream(), true);
            out.println(expression);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            System.out.println(("结果为：" + in.readLine()));
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            //清理 in、out、socket
            //不清理会导致阻塞
            if(in != null) {
                in.close();
            }
            if(out != null) {
                out.close();
            }
            if(socket != null) {
                socket.close();
            }
        }
    }
}
```

# NIO Demo

```java
public class CodecUtil {
    public static ByteBuffer read(SocketChannel channel) {
        // 注意，不考虑拆包的处理
        ByteBuffer buffer = ByteBuffer.allocate(1024);
        try {
            int count = channel.read(buffer);
            if (count == -1) {
                return null;
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        return buffer;
    }

    public static void write(SocketChannel channel, String content) {
        // 写入 Buffer
        ByteBuffer buffer = ByteBuffer.allocate(1024);
        try {
            buffer.put(content.getBytes("UTF-8"));
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
        // 写入 Channel
        buffer.flip();
        try {
            // 注意，不考虑写入超过 Channel 缓存区上限。
            channel.write(buffer);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public static String newString(ByteBuffer buffer) {
        buffer.flip();
        byte[] bytes = new byte[buffer.remaining()];
        System.arraycopy(buffer.array(), buffer.position(), bytes, 0, buffer.remaining());
        try {
            return new String(bytes, "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }
}
```

```java
public class NIOServer {

    private Integer port;
    private Charset charset = Charset.forName("UTF-8");
    private Selector selector;

    //启动服务端
    public NIOServer(Integer port) throws IOException {
        this.port = port;
        ServerSocketChannel server = ServerSocketChannel.open();
        server.configureBlocking(false);
        server.bind(new InetSocketAddress(port));
        selector = Selector.open();
        server.register(selector, SelectionKey.OP_ACCEPT);
        System.out.println("服务端已启动，监听端口：" + this.port);
    }

    public static void main(String[] args) throws IOException {
        new NIOServer(8082).listener();
    }

    //监听事件
    public void listener() {
        try {
            //轮询
            while (true) {
                int wait = selector.select(30 * 1000L);
                if (wait == 0) {
                    continue;
                }
                Set<SelectionKey> selectionKeys = selector.selectedKeys();
                Iterator<SelectionKey> keyIterator = selectionKeys.iterator();
                while (keyIterator.hasNext()) {
                    SelectionKey key = keyIterator.next();
                    keyIterator.remove();
                    if (!key.isValid()) {
                        continue;     //忽略无效的SelectionKey
                    }
                    //处理事件
                    process(key);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //处理事件
    private void process(SelectionKey key) throws IOException {
        if (key.isAcceptable()) {
            handleAcceptableKey(key);
        }
        if (key.isReadable()) {
            handleReadableKey(key);
        }
    }

    private void handleAcceptableKey(SelectionKey key) throws IOException {
        ServerSocketChannel server = (ServerSocketChannel) key.channel();
        SocketChannel client = server.accept();
        client.configureBlocking(false);
        client.register(selector, SelectionKey.OP_READ);
        client.write(charset.encode("已连接到服务器，请输入昵称："));
    }

    private void handleReadableKey(SelectionKey key) throws IOException {
        try {
            //服务器端从通道中读取客户端发送的信息到缓存
            SocketChannel client = (SocketChannel) key.channel();
            ByteBuffer readBuffer = CodecUtil.read(client);
            // 处理连接已经断开的情况
            if (readBuffer == null) {
                System.out.println("断开Channel");
                client.register(selector, 0);
                return;
            }
            // 打印数据
            if (readBuffer.position() > 0) {
                String content = CodecUtil.newString(readBuffer);
                System.out.println("读取数据客户端数据：" + content);
                //将此客户端发送的消息，通过服务器器端广播给其他客户端
                broadcast(client, content.toString());
            }
//            StringBuilder msg = new StringBuilder();
//            ByteBuffer buffer = ByteBuffer.allocate(1024);
//            while (client.read(buffer) > 0) {
//                buffer.flip();
//                msg.append(charset.decode(buffer));
//            }
            //将此客户端发送的消息，通过服务器器端广播给其他客户端
//            broadcast(client, msg.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    //服务器广播消息给客户端
    private void broadcast(SocketChannel client, String msg) throws IOException {
        // keys()
        Set<SelectionKey> keys = selector.keys();
        for (SelectionKey key : keys) {
            SelectableChannel channel = key.channel();
            if (channel instanceof SocketChannel && channel != client) {
                SocketChannel targetChannel = (SocketChannel) channel;
                targetChannel.write(charset.encode(msg));
            }
        }
    }
}
```

```java
public class NIOClient {

    //客户端连接服务器端
    private InetSocketAddress serverAddress = new InetSocketAddress("localhost", 8082);
    private Selector selector;
    private Charset charset = Charset.forName("UTF-8");
    private SocketChannel client;     //当前客户端

    //启动客户端，连接服务器
    public NIOClient() throws IOException {
        client = SocketChannel.open(serverAddress);
        client.configureBlocking(false);
        selector = Selector.open();
        //注册可读事件，接收服务端发送给自己的的消息
        client.register(selector, SelectionKey.OP_READ);
    }

    public static void main(String[] args) throws IOException {
        new NIOClient().session();
    }

    //客户端开始读写线程
    public void session() {
        ThreadPoolUtil.getThreadPool().execute(new Reader());
        ThreadPoolUtil.getThreadPool().execute(new Writer());
//        new Reader().start();
//        new Writer().start();
    }

    //客户端读操作（读是"被动的"，需要轮询有谁给自己发送消息）
    public class Reader extends Thread {
        @Override
        public void run() {
            try {
                //轮询
                while (true) {
                    int wait = selector.select();
                    if (wait == 0) {
                        continue;
                    }
                    Set<SelectionKey> selectionKeys = selector.selectedKeys();
                    Iterator<SelectionKey> keyIterator = selectionKeys.iterator();
                    while (keyIterator.hasNext()) {
                        SelectionKey key = keyIterator.next();
                        keyIterator.remove();
                        process(key);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        public void process(SelectionKey key) throws IOException {
            //读取服务器端发送过来的数据
            if (key.isReadable()) {
                SocketChannel client = (SocketChannel) key.channel();
//                ByteBuffer buffer = ByteBuffer.allocate(1024);
//                StringBuilder msg = new StringBuilder();
//                while (client.read(buffer) > 0) {
//                    buffer.flip();
//                    msg.append(charset.decode(buffer));
//                }
//                System.out.println("收到服务器的信息为：" + msg);
                ByteBuffer readBuffer = CodecUtil.read(client);
                if(readBuffer.position() > 0) {
                    String msg = CodecUtil.newString(readBuffer);
                    System.out.println("收到服务器的信息为：" + msg);
                }
            }
        }
    }

    //客户端写操作（写是"主动"的）
    public class Writer extends Thread {
        @Override
        public void run() {
            Scanner scanner = new Scanner(System.in);
            while (scanner.hasNextLine()) {
                String msg = scanner.nextLine();
//                try {
////                    client.write(charset.encode(msg));
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
                CodecUtil.write(client, msg);
            }
            scanner.close();
        }
    }
}
```

# TODO

```java
//NIOServer
/**
 * 网络多客户端聊天室
 * 功能1： 客户端通过Java NIO连接到服务端，支持多客户端的连接
 * 功能2：客户端初次连接时，服务端提示输入昵称，如果昵称已经有人使用，提示重新输入，如果昵称唯一，则登录成功，之后发送消息都需要按照规定格式带着昵称发送消息
 * 功能3：客户端登录后，发送已经设置好的欢迎信息和在线人数给客户端，并且通知其他客户端该客户端上线
 * 功能4：服务器收到已登录客户端输入内容，转发至其他登录客户端。
 * 
 * TODO 客户端下线检测
 */
```









