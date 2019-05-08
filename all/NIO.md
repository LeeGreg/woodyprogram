![image-20181216204455672](/Users/dingyuanjie/Desktop/MyKnowledge/2.code/java/2.咕泡学院/04.多线程高并发/01.NIO/image-20181216204455672-4964296.png)

IO复用：

​	Selector单线程，轮询所有的Channel

![image-20181216205747316](/Users/dingyuanjie/Desktop/MyKnowledge/2.code/java/2.咕泡学院/04.多线程高并发/01.NIO/image-20181216205747316.png)

# NIO

JUC：`java.util.concurrent`，并发编程中常用的工具类

网络传输一定要编码。网络通信的时候采用二进制形式，用可传输字节流的方式进行传输

## 组成

* 通道Channel
  * FileChannel-文件IO流
  * DatagramChannel-UDP协议
  * ServerSocketChannel/SocketChannel-TCP协议
* 缓冲区Buffer
* 选择器Selector

















































## 阻塞和非阻塞

> ==阻塞和非阻塞是进程在访问数据的时候，数据是否准备就绪的一种处理方式==
>
> 阻塞：当进程访问数据缓冲区的时候，进程需要等待缓冲区中的数据准备好过后才处理其他的事情，否则一直等待在那
>
> 非阻塞:当进程访问数据缓冲区的时候，如果数据没有准备好则直接返回，不会等待

## 同步和异步

> ==同步和异步都是基于应用程序和操作系统处理 IO 事件所采用的方式==
>
> 同步：是应用程序要直接参与 IO 读写的操作
>
> > 同步方式在处理 IO 事件的时候，必须阻塞在某个方法上面等待 IO 事件完成(阻塞 IO 事件或者通过轮询 IO 事件的方式)
>
> 异步：所有的 IO 读写交给操作系统去处理，应用程序可以去做其他的事情，只需要等待通知
>

## Java IO 模型

* 1:1同步阻塞IO通信模型，一个客户端对应一个线程
* M：N同步阻塞IO通信模型，使用线程池
* 非阻塞式IO模型(NIO)，NIO+单线程Reactor模式
* 非阻塞式IO模型(NIO)，NIO+多线程Reactor模式
* NIO+主从多线程Reactor模式





![image-20181203154108275](/Users/dingyuanjie/Desktop/MyKnowledge/2.code/java/2.%E5%92%95%E6%B3%A1%E5%AD%A6%E9%99%A2/04.%E5%A4%9A%E7%BA%BF%E7%A8%8B%E9%AB%98%E5%B9%B6%E5%8F%91/02.Netty/image-20181203154108275-3822868.png)

> accept是单线程的，瓶颈
>
> Reactor是抽象的模型，不是具体的组件

![image-20181203154229051](/Users/dingyuanjie/Desktop/MyKnowledge/2.code/java/2.%E5%92%95%E6%B3%A1%E5%AD%A6%E9%99%A2/04.%E5%A4%9A%E7%BA%BF%E7%A8%8B%E9%AB%98%E5%B9%B6%E5%8F%91/02.Netty/image-20181203154229051-3822949.png)

> accept是单线程的，瓶颈

![image-20181203154306716](/Users/dingyuanjie/Desktop/MyKnowledge/2.code/java/2.%E5%92%95%E6%B3%A1%E5%AD%A6%E9%99%A2/04.%E5%A4%9A%E7%BA%BF%E7%A8%8B%E9%AB%98%E5%B9%B6%E5%8F%91/02.Netty/image-20181203154306716-3822986.png)

## BIO与NIO对比

![BIO与NIO对比](/Users/dingyuanjie/Desktop/notes/书单/BIO与NIO对比.png)

* 面向流与面向缓冲

  * IO 是面向流的，每次从流中读一个或多个字节，直至读取所有字节，它们没有被缓存在任何地方
  * NIO 是面向缓冲区的，数据可以读取到一个它稍后处理的缓冲区，需要时可在缓冲区中前后移动

* 阻塞与非阻塞 IO

  * Java IO 的各种流是阻塞的。
    * 当一个线程调用 read() 或 write()时，该线程被阻塞，直到有一些数据被读取，或数据完全写入。该线程在此期间不能再干任何事情了
  * JavaNIO 的非阻塞模式。
    * 使一个线程从某通道发送请求读取数据，但是它仅能得到目前可用的数据，如果目前没有数据可用时，就什么都不会获取。而不是保持线程阻塞，所以直至数据变的可以读取之前，该线程可以继续做其他的事情。非阻塞写也是如此。一个线程请求写入一些数据到某通道，但不需要等待它完全写入，这个线程同时可以去做别的事情。

  > ==线程通常将非阻塞 IO 的空闲时间用于在其它通道上执行 IO 操作，所以一个单独的线程现在可以管理多个输入和输出通道(channel)==

* 选择器(Selector)（触发）

  * ==Java NIO 的选择器允许一个单独的线程来监视多个输入通道，可以注册多个通道使用一个选择器，然后使用一个单独的线程来“选择”通道==（这些通道里已经有可以处理的输入，或者选择已准备写入的通道）
  * 这种选择机制，使得一个单独的线程很容易来管理多个通道

## NIO 和 IO 如何影响应用程序的设计

* API 调用

  > 使用 NIO 的 API 调用时看起来与使用 IO 时有所不同，因为并不是仅从一个InputStream 逐字节读取，而是数据必须先读入缓冲区再处理

* 数据处理

  > 在 IO 设计中，从 InputStream 或 Reader 逐字节读取数据（从一个阻塞的流中读数据）
  >
  > ```java
  > FileInputStream input = new FileInputStream("d://info.txt"); 
  > BufferedReader reader = new BufferedReader(new InputStreamReader(input)); 
  > String nameLine = reader.readLine();
  > String ageLine = reader.readLine();
  > ```
  >
  > 处理状态由程序执行多久决定，即一旦 reader.readLine()方法返回，就知道肯定文本行就已读完， readline()阻塞直到整行读完
  >
  > 该处理程序仅在有新数据读入时运行，并知道每步的数据是什么。一旦正在运行的线程已处理过读入的某些数据，该线程不会再回退数据(大多如此)
  >
  >  NIO 从通道读取字节到 ByteBuffer
  >
  > ```java
  > ByteBuffer buffer = ByteBuffer.allocate(48); 
  > int bytesRead = inChannel.read(buffer);
  > //bufferFull()方法必须跟踪有多少数据读入缓冲区，并返回真或假，这取决于缓冲区是否已满,如果缓冲区准备好被处理，那么表示缓冲区满了
  > while(! bufferFull(bytesRead) ) {
  > 	bytesRead = inChannel.read(buffer);
  > }
  > ```
  >
  > 当这个方法调用返回时，不知道所需的所有数据是否在缓冲区内。所知道的是，该缓冲区包含一些字节，因为不知道数据是否完整，这使得处理有点困难
  >
  > 不知道该缓冲区是否包含足够的可处理数据，发现的方法只能查看缓冲区中的数据。其结果是，在知道所有数据都在缓冲区里之前，必须检查几次缓冲区的数据。这不仅效率低下，而且可以使程序设计方案杂乱不堪
  >
  > bufferFull()方法扫描缓冲区，但必须保持在 bufferFull()方法被调用之前状态相同。
  > 如果没有，下一个读入缓冲区的数据可能无法读到正确的位置。这是不可能的，但却是需要注意的又一问题，如果缓冲区已满，它可以被处理。如果它不满，并且在实际案例中有意义，或许能处理其中的部分数据。但是许多情况下并非如此

* 用来处理数据的线程数

  * NIO 可让只使用一个(或几个)单线程管理多个通道(网络连接或文件)，但付出的代价是解析数据可能会比从一个阻塞流中读取数据更复杂
  * 如果需要管理同时打开的成千上万个连接，这些连接每次只是发送少量的数据，例如聊天服务器，实现 NIO 的服务器可能是一个优势。同样，如果需要维持许多打开的连接到其他计算机上，如 P2P 网络中，使用一个单独的线程来管理所有出站连接，可能是一个优势
  * Java NIO: 单线程管理多个连接
  * 如果有少量的连接使用非常高的带宽，一次发送大量的数据，也许典型的 IO 服务器实现可能非常契合。Java IO: 一个典型的 IO 服务器设计- 一个连接通过一个线程处理

## 各IO操作对比

![各IO操作对比](/Users/dingyuanjie/Desktop/notes/书单/各IO操作对比.png)

## Java NIO(Non-Block IO)

* 在 Java 1.4 中推出了 NIO，这是一个面向块的 I/O 系统，系统以块的方式处理，每一个操作在一步中产生或者消费一个数据块，按块处理要比按字节处理数据快的多

### 缓冲区 Buffer

`capacity`

`position`

`limit`

![image-20181203161441019](/Users/dingyuanjie/Desktop/MyKnowledge/2.code/java/2.咕泡学院/04.多线程高并发/01.NIO/image-20181203161441019-3824881.png)

> 任何时候访问 NIO 中的数据，都是将它放到缓冲区中
>
> 缓冲区实际上是一个容器对象（数组），在 NIO 库中，所有数据都是用缓冲区处理的。在读取数据时，它是直接读到缓冲区中的；在写入数据时，它也是写入到缓冲区中的;
>
> 解决缓冲区满了：
>
> 通过协议：先解析存储消息体长度的字节来确定创建多大的缓冲区
>
> 在面向流 I/O系统中，所有数据都是直接写入或者直接将数据读取到 Stream 对象中
>
> 在 NIO 中，所有的缓冲区类型都继承于抽象类 Buffer，最常用的就是 ByteBuffer，对于 Java中的基本类型，基本都有一个具体 Buffer 类型与之相对应
>
> ```java
> //这用的是文件IO处理
> FileInputStream fin = new FileInputStream("E:/GP_WORKSPACE/test.txt");
> //创建文件的操作管道
> FileChannel fc = fin.getChannel();
> //分配了10个长度的byte数组，初始状态：position=0；limit=capacity;
> ByteBuffer buffer = ByteBuffer.allocate(10); 
> //先读一下，向缓冲区中写入数据
> fc.read(buffer);
> /*for (int i = 0; i < buffer.capacity(); ++i) {  
> 	int j = (i + 1);  
> 	buffer.put(j);  // 将给定整数写入此缓冲区的当前位置，当前位置递增  
> } */
> 
> //准备操作之前，先锁定操作范围
> // 重设此缓冲区，将限制（limit）设置为当前位置，然后将当前位置设置为0
> //固定缓冲区中的某些值，告诉缓冲区，要开始操作了，如果再往缓冲区写数据的话不要再覆盖固定状态以前的数据了
> buffer.flip();	//limit=position; position=0；capacity不变;
> //判断有没有可读数据
> // 查看在当前位置和限制位置之间是否有元素  
> while (buffer.remaining() > 0) {  
>     // 读取此缓冲区当前位置的整数，然后当前位置递增  
>     byte j = buffer.get();  
>     System.out.print(j + "  ");  
> }
> buffer.clear();//重置缓冲区为初始状态
> //最后把管道关闭
> fin.close(); 
> ```
>
> ==缓冲区对象本质上是一个数组，但它其实是一个特殊的数组，缓冲区对象内置了一些机制，能够跟踪和记录缓冲区的状态变化情况，如果使用 get()方法从缓冲区获取数据或者使用 put()方法把数据写入缓冲区，都会引起缓冲区状态的变化==
>
> Buffer缓冲区中，最重要的三个属性一起合作完成对缓冲区内部状态的变化跟踪:
>
> 1. position
>
>    > 指定了下一个将要被写入或者读取的元素索引，它的值由 get()/put()方法自动更新，在
>    > 新创建一个 Buffer 对象时，position 被初始化为 0
>
> 2. limit
>
>    > 指定还有多少数据需要取出(在从缓冲区写入通道时)，或者还有多少空间可以放入数据(在从
>    > 通道读入缓冲区时)，初始化时limit被初始化成capacity大小
>
> 3. capacity
>
>    > 指定了可以存储在缓冲区中的最大数据容量
>
> 0 <= position <= limit <= capacity
>
> - 初始化时：
>
> ​	position=0; limit=capacity;
>
> - 从通道读取数据写入缓冲区时（向缓冲区中添加数据）：
>
> ​	position等于读入数量；limit不变
>
> - 从缓冲区读出数据写入通道时（从缓冲区中取数据）：
>
> ​	必须调用 flip()方法：
>
> 1. 把 limit 设置为当前的 position 值（读取的数据正好是之前写入的数据）；
> 2. 把 position 设置为 0（保证读取出缓冲区第一个字节）
>
> * clear()把所有状态设置为初始值
>
> * 缓冲区分片
>
>   > 在现有缓冲区上切出一片来作为一个新的缓冲区，但现有的缓冲区与创建的子缓冲区在底层数组层面上是数据共享的
>   >
>   > ```java
>   > // 创建子缓冲区  
>   > buffer.position( 3 );  
>   > buffer.limit( 7 );  
>   > ByteBuffer slice = buffer.slice(); //调用 slice()方法可以创建一个子缓冲区
>   > ```
>
> * 只读缓冲区
>
>   > 调用缓冲区的`asReadOnlyBuffer()`方法，将任何常规缓冲区转换为只读缓冲区，这个方法返回一个与原缓冲区完全相同的缓冲区，并与原缓冲区共享数据，只不过它是只读的
>
> * 直接缓冲区
>
>   > 直接缓冲区是为加快 I/O 速度，使用一种特殊方式为其分配内存的缓冲区
>   >
>   > JDK 文档中的描述为：给定一个直接字节缓冲区，Java 虚拟机将尽最大努力直接对它执行本机 I/O 操作。也就是说，它会在每一次调用底层操作系统的本机 I/O 操作之前(或之后)，尝试避免将缓冲区的内容拷贝到一个中间缓冲区中或者从一个中间缓冲区中拷贝数据
>   >
>   > 要分配直接缓冲区，需要调用` allocateDirect()`方法，而不是 allocate()方法，使用方式与普通缓冲区并无区别
>
> * 内存映射文件 I/O
>
>   > 是一种读和写文件数据的方法，它可以比常规的基于流或者基于通道的 I/O 快的多
>   >
>   > 是通过使文件中的数据出现为内存数组的内容来完成的，这其初听起来似乎不过就是将整个文件读到内存中，但是事实上并不是这样。一般来说，只有文件中实际读取或者写入的部分才会映射到内存中
>   >
>   > ```java
>   > RandomAccessFile raf = new RandomAccessFile( "e:\\test.txt", "rw" );  
>   > 	FileChannel fc = raf.getChannel();    
>   > 	//把缓冲区跟文件系统进行一个映射关联
>   > 	//只要操作缓冲区里面的内容，文件内容也会跟着改变
>   > 	MappedByteBuffer mbb = fc.map( FileChannel.MapMode.READ_WRITE,start, size );       
>   > 	mbb.put( 0, (byte)97 );  
>   > 	mbb.put( 1023, (byte)122 );  
>   >     raf.close(); 
>   > ```

### 通道(Channel)

`FileChannel`

`DatagramChannel`

`SocketChannel`

`ServerSocketChannle`

 `Channel` <==> `Buffer`

> ==通道是一个对象，通过它可以读取和写入数据，当然了所有数据都通过 Buffer 对象来处理==
>
> 永远不会将字节直接写入通道中，相反是将数据写入包含一个或者多个字节的缓冲区。同样不会直接从通道中读取字节，而是将数据从通道读入缓冲区，再从缓冲区获取这个字节
>
> 在 NIO 中，提供了多种通道对象，而所有的通道对象都实现了 Channel 接口
>
> 使用 NIO 读取数据：
>
> 1. 从 FileInputStream/FileOutputStream 获取 Channel
> 2. 创建 Buffer
> 3. 将数据从 Channel 读取/写入到 Buffer 中
>
> ```java
> FileInputStream fin = new FileInputStream("c:\\test.txt");  
> // 获取通道  
> FileChannel fc = fin.getChannel();  
> // 创建缓冲区  
> ByteBuffer buffer = ByteBuffer.allocate(1024);  
> // 读取数据到缓冲区  
> fc.read(buffer);  
> buffer.flip();  
> while (buffer.remaining() > 0) {  
>     byte b = buffer.get();  
>     System.out.print(((char)b));  
> }   
> fin.close();
> --------------------------------------------------------------------
> static private final byte message[] = { 83, 111, 109, 101, 32,  
>                                            98, 121, 116, 101, 115, 46 };  
> static public void main( String args[] ) throws Exception {  
>     FileOutputStream fout = new FileOutputStream( "e:\\test.txt" );     
>     FileChannel fc = fout.getChannel();    
>     ByteBuffer buffer = ByteBuffer.allocate( 1024 );       
>     for (int i=0; i<message.length; ++i) {  
>         buffer.put( message[i] );  
>     }     
>     buffer.flip();    
>     fc.write( buffer );     
>     fout.close();  
> }
> ```

### Java NIO通道之间的数据传输

在JavaNIO中，如果两个通道中有一个是FileChannel，那可以直接将数据从一个channel传输到另外一个channel

### 反应堆 Reactor

* 阻塞 I/O 通信模型

  > 阻塞 I/O 在调用` InputStream.read()`方法时是阻塞的，它会一直等到数据到来时(或超时)才会返回
  >
  > 同样，在调用`ServerSocket.accept()`方法时，也会一直阻塞到有客户端连接才会返回，每个客户端连接过来后，服务端都会启动一个线程去处理该客户端的请求
  >
  > 缺点：
  >
  > 1. 当客户端多时，会创建大量的处理线程。且每个线程都要占用栈空间和一些 CPU 时间
  > 2. 阻塞可能带来频繁的上下文切换，且大部分上下文切换可能是无意义的。在这种情况下非阻塞式 I/O 就有了它的应用前景。

* Java NIO 原理及通信模型

  > ==Java NIO 的工作原理==
  >
  > 1. 由一个专门的线程来处理所有的 IO 事件，并负责分发
  >
  > 2. ==事件驱动机制：事件到的时候触发，而不是同步的去监视事件==
  >
  > 3. 线程通讯：线程之间通过 `wait,notify` 等方式通讯。保证每次上下文切换都是有意义的。减少无谓的线程切换
  >
  > ![NIO工作原理图](/Users/dingyuanjie/Desktop/notes/书单/NIO工作原理图.png)
  >
  > ![image-20181203160922293](/Users/dingyuanjie/Desktop/MyKnowledge/2.code/java/2.咕泡学院/04.多线程高并发/01.NIO/image-20181203160922293-3824562.png)
  >
  > > 线程，selector轮询事件，告诉对应客户端
  >
  > ![image-20181203160956981](/Users/dingyuanjie/Desktop/MyKnowledge/2.code/java/2.咕泡学院/04.多线程高并发/01.NIO/image-20181203160956981-3824597.png)
  >
  > ![image-20181203161019431](/Users/dingyuanjie/Desktop/MyKnowledge/2.code/java/2.咕泡学院/04.多线程高并发/01.NIO/image-20181203161019431-3824619.png)
  >
  > 反应堆是个抽象的概念，selector是具体的行为的体现

### 选择器(Selector)（轮询机制）

`selector的创建` - `向selector注册通道` - `通过selector选择通道`

Thread - Selector  - ChannelA / ChannelB / ChannelC...

> 单线程模式，避免线程切换，导致性能差
>
> IO多路复用（同步非阻塞）：简单理解为多个客户端使用了同一个selector线程

![image-20181203160755773](/Users/dingyuanjie/Desktop/MyKnowledge/2.code/java/2.咕泡学院/04.多线程高并发/01.NIO/image-20181203160755773-3824475.png)

> Java 的非阻塞 I/O 实现的关键。它使用了事件通知 API以确定在一组非阻塞套接字中有哪些已经就绪能够进
> 行 I/O 相关的操作。
>
> 因可以在任何的时间检查任意的读操作或者写操作的完成状态，一个单一的线程便可以处理多个并发的连接
>
> * 这种模型提供了更好的资源管理:
>   * 使用较少的线程便可以处理许多连接，因此也减少了内存管理和上下文切换所带来开销
>   * ==当没有 I/O 操作需要处理的时候，线程也可以被用于其他任务==
>
> 传统的 Server/Client 模式会基于 TPR(Thread per Request),服务器会为每个客户端请求建立一个线程，由该线程单独负责处理一个客户请求。
>
> 这种模式带来的一个问题就是线程数量的剧增，大量的线程会增大服务器的开销。大多数的实现为了避免这个问题，都采用了线程池模型，并设置线程池线程的最大数量，这又带来了新的问题，如果线程池中有 200 个线程，而有 200 个用户都在进行大文件下载，会导致第 201 个用户的请求无法及时处理，即便第 201 个用户只想请求一个几 KB 大小的页面
>
> NIO 中非阻塞 I/O 采用了基于 Reactor 模式的工作方式，I/O 调用不会被阻塞，相反是注册感兴趣的特定 I/O 事件，如可读数据到达，新的套接字连接等等，在发生特定事件时，系统再通知我们
>
> NIO中实现非阻塞 I/O 的==核心对象==就是`Selector`，Selector 就是注册各种 I/O 事件地方，而且当那些事件发生时，就是这个对象告诉我们所发生的事件
>
> ==当有读或写等任何注册的事件发生时，可以从` Selecto `中获得相应的`SelectionKey`，同时从 `SelectionKey`中可以找到发生的事件和该事件所发生的具体的`SelectableChannel`，以获得客户端发送过来的数据==

使用 NIO 中非阻塞 I/O 编写服务器处理程序，大体上可以分为下面三个步骤:

1. 向 Selector 对象注册感兴趣的事件
2. 从 Selector 中获取感兴趣的事件
3. 根据不同的事件进行相应的处理  

> ![image-20181021185216479](/Users/dingyuanjie/Desktop/MyKnowledge/2.code/java/2.咕泡学院/04.多线程高并发/01.NIO/image-20181021185216479.png)

```java
自己写的
//server
public class Server {

    private Integer port;
    private Charset charset = Charset.forName("UTF-8");
    private Selector selector;

    public static void main(String[] args) throws IOException {
       new Server(8082).listener();
    }

    //启动服务端
    public Server(Integer port) throws IOException {
        this.port = port;
        ServerSocketChannel server = ServerSocketChannel.open();
        server.configureBlocking(false);
        server.bind(new InetSocketAddress(port));
        selector = Selector.open();
        server.register(selector, SelectionKey.OP_ACCEPT);
        System.out.println("服务端已启动，监听端口：" + this.port);
    }

    //监听事件
    public void listener() {
        try {
            //轮询
            while (true) {
                int wait = selector.select();
                if(wait == 0) {
                    continue;
                }
                Set<SelectionKey> selectionKeys = selector.selectedKeys();
                Iterator<SelectionKey> keyIterator = selectionKeys.iterator();
                while (keyIterator.hasNext()) {
                    SelectionKey key = keyIterator.next();
                    keyIterator.remove();
                    if(!key.isValid()) {
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
        if(key.isAcceptable()) {
            ServerSocketChannel server =(ServerSocketChannel)key.channel();
            SocketChannel client = server.accept();
            client.configureBlocking(false);
            client.register(selector, SelectionKey.OP_READ);
            client.write(charset.encode("已连接到服务器，请输入昵称："));

        } else if(key.isReadable()) {
            try {
                //服务器端从通道中读取客户端发送的信息到缓存
                SocketChannel client = (SocketChannel)key.channel();
                StringBuilder msg = new StringBuilder();
                ByteBuffer buffer = ByteBuffer.allocate(1024);
                while (client.read(buffer) > 0) {
                    buffer.flip();
                    msg.append(charset.decode(buffer));
                }
                //将此客户端发送的消息，通过服务器器端广播给其他客户端
                broadcast(client, msg.toString());
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

    }

    //服务器广播消息给客户端
    private void broadcast(SocketChannel client, String msg) throws IOException {
        // keys()
        Set<SelectionKey> keys = selector.keys();
        for (SelectionKey key : keys) {
            SelectableChannel channel = key.channel();
            if(channel instanceof SocketChannel && channel != client) {
                SocketChannel targetChannel = (SocketChannel)channel;
                targetChannel.write(charset.encode(msg));
            }
        }
    }
}
```

```java
//client
public class Client {

    //客户端连接服务器端
    private InetSocketAddress serverAddress = new InetSocketAddress("localhost", 8082);
    private Selector selector;
    private Charset charset = Charset.forName("UTF-8");
    private SocketChannel client;     //当前客户端

    public static void main(String[] args) throws IOException {
       new Client().session();
    }

    //启动客户端，连接服务器
    public Client() throws IOException {
        client = SocketChannel.open(serverAddress);
        client.configureBlocking(false);
        selector = selector.open();
        //注册可读事件，接收服务端发送给自己的的消息
        client.register(selector, SelectionKey.OP_READ);
    }

    //客户端开始读写线程
    public void session() {
        new Reader().start();
        new Writer().start();
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
                ByteBuffer buffer = ByteBuffer.allocate(1024);
                StringBuilder msg = new StringBuilder();
                while (client.read(buffer) > 0) {
                    buffer.flip();
                    msg.append(charset.decode(buffer));
                }
                System.out.println("收到服务器的信息为：" + msg);
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
                try {
                    client.write(charset.encode(msg));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
            scanner.close();
        }
    }
}
```



```java
//注意
通道是两端，一端是写的话，另一端就是读了
Selector是单例的吗？
客户端的Selector和服务端的Selector

 1. register(selector, SelectionKey.OP_READ);  
适当的场景注册适当的事件类型，否则容器出现问题，比如在key是Acceptable时，继续将通道注册成Acceptable，容器导致死循环
 2. key.interestOps(SelectionKey.OP_READ);  // 此key下一个感兴趣的事件
 3. selector.selectedKeys()和selector.keys()的区别 //此处也需要看书了解
	selector.selectedKeys();获取到key，但是没有被删除的话，这个key所对应的IO事件还在selectedKeys中；如果要一次遍历selector中的SelectionKey的话，可以用selector.keys();

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
public class NIOServer  {

    private Integer port;
    private Selector selector = null;

    //用来记录在线人数，以及昵称
    private Set<String> users = new HashSet<>();
    private static String USER_EXITS = "系统提示：该昵称已存在，请重新换个昵称！";
    //相当于自定义协议格式，与客户端协商好
    private static String USER_CONTENT_SPILT = "#@#";
    private Charset charset = Charset.forName("UTF-8");

    public static void main(String[] args) throws IOException {
        new NIOServer(8081).listener();
    }

    //启动服务端，监听端口
    public NIOServer(Integer port) throws IOException {

        this.port = port;

        //要想富先修路，先把通道打开
        //创建可选择通道，并配置为非阻塞
        ServerSocketChannel server = ServerSocketChannel.open();

        //设置高速公路关卡
        //绑定通道到指定端口
        server.configureBlocking(false);
        server.bind(new InetSocketAddress(this.port));

        //开门迎客，排队叫号大厅开始工作
        //创建Selector对象
        selector = Selector.open();

        //告诉服务大厅的工作人员，你可以开始待客了
        //向Selector中注册感兴趣事件
        //指定的是参数是 OP_ACCEPT，即指定想要监听 accept 事件，也就是新的连接发生时所产生的事件，
        //对于 ServerSocketChannel通道来说，唯一可以指定的参数就是 OP_ACCEPT
        server.register(selector, SelectionKey.OP_ACCEPT);

        System.out.println("Hi, 服务已启动，监听的端口是" + port);
    }

    //从 Selector 中获取感兴趣的事件，即开始监听，进入内部循环:
    public void listener() throws IOException {
        try {
            //死循环，这里不会阻塞
            while (true) {
                //在轮询，服务大厅中有多少人排队
                //轮询（在非阻塞I/O中，内部循环模式基本上都是遵循这种方式）
                //同步非阻塞，每次轮询只能干一件事情
                int wait = selector.select();
                if(wait == 0) { //如果没有人排队，则进入下一次轮询
                    continue;
                }
                //获取发生事件的SelectionKey（获取可用通道的集合）
                Set<SelectionKey> selectionKeys = selector.selectedKeys();
                Iterator<SelectionKey> it = selectionKeys.iterator();
                while (it.hasNext()) {
                    SelectionKey key = (SelectionKey)it.next();
                    it.remove();
                    //根据不同的事件处理
                    process(key);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    //根据不同的事件，编写相应的处理代码
    public void process(SelectionKey key) throws IOException {
        //判断客户端确定已经进入服务大厅并且已经可以实现交互了
        //接收请求
        if(key.isAcceptable()) {
            ServerSocketChannel server = (ServerSocketChannel)key.channel();
            SocketChannel client = server.accept();
//            client.getRemoteAddress();
            //非阻塞模式
            client.configureBlocking(false);
            //注册选择器，并设置为读取模式，收到一个连接请求，然后起一个SocketChannel，并注册到Selector上，
            //之后这个连接的数据，就由这个SocketChannle来处理
            //连接成功之后，下一步操作（读取）
            client.register(selector, SelectionKey.OP_READ);

            //将此对应的channel设置为准备接受其他客户端请求
            key.interestOps(SelectionKey.OP_ACCEPT);

            client.write(charset.encode("请输入你的昵称："));
        }
        //处理来自客户端的数据读取请求
        if(key.isReadable()) {
            //返回该SelectionKey对应的Channel，其中有数据读取
            SocketChannel client = (SocketChannel) key.channel();
            ByteBuffer buffer = ByteBuffer.allocate(1024);
            StringBuffer content = new StringBuffer();
            try {
                //从通道读出数据到缓冲区
                while (client.read(buffer) > 0) {
                    buffer.flip();
                    content.append(charset.decode(buffer));
                    //buffer.clear();
                }
                System.out.println("Hi，从IP地址为：" + client.getRemoteAddress() + "获取的信息为" + content);
                //将此对应的channel设置为准备下一次接受数据
                key.interestOps(SelectionKey.OP_READ);
            } catch (Exception e) {
                key.cancel();
                if(key.channel() != null) {
                    key.channel().close();
                }
            }

            if(content.length() > 0) {
                String[] arrayContent = content.toString().split(USER_CONTENT_SPILT);
                //注册用户
                if(arrayContent!= null && arrayContent.length == 1) {
                    String nickname = arrayContent[0];
                    if(users.contains(nickname)) {
                        client.write(charset.encode(USER_EXITS));
                    } else {
                        users.add(nickname);
                        int onlineCount = onlineCount();
                        String message = "欢迎" + nickname + "加入聊天室，当前在线人数为" + onlineCount;
                        broadcast(null, message);
                    }
                }
                //注册完了，发送消息
                else if(arrayContent != null && arrayContent.length > 1){
                    String nickname = arrayContent[0];
                    String message = content.substring(nickname.length() + USER_CONTENT_SPILT.length());
                    message = nickname + "说" + message;
                    if(users.contains(nickname)) {
                        broadcast(client, message);
                    }
                }
            }

        }
    }

    //TODO 要是能检测下线，就不用这么统计了
    public int onlineCount() {
        int oncount = 0;
        for(SelectionKey key : selector.keys()) {
            Channel target = key.channel();
            if(target instanceof SocketChannel) {
                oncount ++;
            }
        }
        return oncount;
    }

    public void broadcast(SocketChannel client, String message) throws IOException {
        //广播数据到所有的SocketChannel中
        //如果此处用selector.selectedKeys()的话，遍历时还要删除遍历过的key
        for(SelectionKey key : selector.keys()) {
            SelectableChannel targetChannel = key.channel();
            //不回发给发送此内容的客户端
            if(targetChannel instanceof SocketChannel && targetChannel != client) {
                SocketChannel target = (SocketChannel)targetChannel;
                target.write(charset.encode(message));
            }
        }
    }

}
```

```java
//NIOClient
public class NIOClient {

    private static String USER_EXITS = "系统提示：该昵称已存在，请重新换个昵称！";
    private static String USER_CONTENT_SPILT = "#@#";
    private final InetSocketAddress serverAddress = new InetSocketAddress("localhost", 8081);
    private Selector selector;
    private SocketChannel client;
    private String nickname = "";
    private Charset charset = Charset.forName("UTF-8");

    public NIOClient() throws IOException {
        //先修好路，把关卡放开
        //连接远程主机的IP和端口
        client = SocketChannel.open(serverAddress);
        client.configureBlocking(false);

        //开门接待客人
        selector = Selector.open();
        client.register(selector, SelectionKey.OP_READ);

    }

    public static void main(String[] args) throws IOException {
        new NIOClient().session();
    }

    public void session() {
        //开辟一个新的线程从服务器端读取数据
        new Reader().start();
        //开辟一个新的线程向服务器端写入数据
        new Writer().start();
    }

    public class Writer extends Thread {

        @Override
        public void run() {
            try {
                //在主线程中，从键盘读取数据输入到服务器端
                Scanner scanner = new Scanner(System.in);
                while (scanner.hasNext()) {
                    String line = scanner.nextLine();
                    if ("".equals(line)) {
                        continue;  //不允许发送空消息
                    }
                    if ("".equals(nickname)) {
                        nickname = line;
                        line = nickname + USER_CONTENT_SPILT;
                    } else {
                        line = nickname + USER_CONTENT_SPILT + line;
                    }
                    //client既能写，也能读
                    //向通道中写入数据
                    client.write(charset.encode(line));
                }
                scanner.close();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public class Reader extends Thread {
        @Override
        public void run() {
            try {
                while (true) {
                    int readyChannels = selector.select();
                    if (readyChannels == 0) {
                        continue;
                    }
                    //获取通道的集合
                    Set<SelectionKey> selectionKeys = selector.selectedKeys();
                    Iterator<SelectionKey> it = selectionKeys.iterator();
                    while (it.hasNext()) {
                        SelectionKey key = it.next();
                        it.remove();
                        process(key);
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        public void process(SelectionKey key) throws IOException {
            if (key.isReadable()) {
                //client既能写，也能读，这边是读
                SocketChannel channel = (SocketChannel) key.channel();
                ByteBuffer byteBuffer = ByteBuffer.allocate(1024);
                String content = "";
                while (channel.read(byteBuffer) > 0) {
                    byteBuffer.flip();
                    content += charset.decode(byteBuffer);
                }
                //若系统发送通知名字已经存在，则需要换个昵称
                if (USER_EXITS.equals(content)) {
                    nickname = "";
                }
                System.out.println("Hi" + content);
                key.interestOps(SelectionKey.OP_READ);
            }
        }
    }
}
```

## Java AIO(未来趋势)

> jdk1.7 (NIO2)才是实现真正的异步 aio、把 IO 读写操作完全交给操作系统，学习了 linux epoll 模式

### AIO 原理

> 服务端：`AsynchronousServerSocketChannel`
>
> 客服端：`AsynchronousSocketChannel`
>
> 用户处理器：`CompletionHandler` 这个接口实现应用程序向操作系统发起 IO 请求，当完成后处理具体逻辑，否则做自己该做的事情
>
> “真正”的异步IO需要操作系统更强的支持。
>
> 在IO多路复用模型中，事件循环将文件句柄的状态事件通知给用户线程，由用户线程自行读取数据、处理数据
>
> 而在异步IO模型中，当用户线程收到通知时，数据已经被内核读取完毕，并放在了用户线程指定的缓冲区内，内核在IO完成后通知用户线程直接使用即可
>
> 异步IO模型使用了`Proactor`设计模式实现了这一机制：
>
> ![Proactor设计模式](/Users/dingyuanjie/Desktop/notes/书单/Proactor设计模式.png)
>
> ```java
> //AIOServer
> public class AIOServer {
>     private final int port;
>     //注册一个端口，用来给客户端连接
>     public AIOServer(int port) {
>         this.port = port;
>         listen();
>     }
>     public static void main(String args[]) {
>         int port = 8000;
>         new AIOServer(port);
>     }
>     //侦听方法
>     private void listen() {
>         try {
>             //线程缓冲池，为了体现异步
>             ExecutorService executorService = Executors.newCachedThreadPool();
>             //给线程池初始化一个线程
>             AsynchronousChannelGroup threadGroup = AsynchronousChannelGroup.withCachedThreadPool(executorService, 1);
>             //Asynchronous异步
>             //NIO   ServerSocketChannel
>             //BIO   ServerSocket   有那么一点点像
>             //同样的，也是先把高速公路修通
>             final AsynchronousServerSocketChannel server = AsynchronousServerSocketChannel.open(threadGroup);
>             //打开高速公路的关卡
>             server.bind(new InetSocketAddress(port));
>             System.out.println("服务已启动，监听端口" + port);
>             //开始等待客户端连接
>             //实现一个CompletionHandler 的接口，匿名的实现类
>             server.accept(null, new CompletionHandler<AsynchronousSocketChannel, Object>() {
>                 final ByteBuffer buffer = ByteBuffer.allocateDirect(1024);
>                 public void completed(AsynchronousSocketChannel result, Object attachment) {
>                     //只要拿数据，捡现成的,我们都是懒人，IO操作都不用关心了
>                     System.out.println("IO 操作成功，开始获取数据");
>                     try {
>                         buffer.clear();
>                         result.read(buffer).get();
>                         buffer.flip();
>                         result.write(buffer);
>                         buffer.flip();
>                     } catch (Exception e) {
>                         System.out.println(e.toString());
>                     } finally {
>                         try {
>                             result.close();
>                             server.accept(null, this);
>                         } catch (Exception e) {
>                             System.out.println(e.toString());
>                         }
>                     }
>                     System.out.println("操作完成");
>                 }
>                 @Override
>                 public void failed(Throwable exc, Object attachment) {
>                     System.out.println("IO 操作是失败: " + exc);
>                 }
>             });
>             try {
>                 Thread.sleep(Integer.MAX_VALUE);
>             } catch (InterruptedException ex) {
>                 System.out.println(ex);
>             }
>         } catch (IOException e) {
>             System.out.println(e);
>         }
>     }
> }
> ```
>
> ````java
> //AIOClient
> public class AIOClient {
>     private final AsynchronousSocketChannel client;
>     public AIOClient() throws Exception {
>         //Asynchronous
>         //BIO   Socket
>         //NIO   SocketChannel
>         //AIO   AsynchronousSocketChannel
>         //先把高速公路修起来
>         client = AsynchronousSocketChannel.open();
>     }
> 
>     public static void main(String args[]) throws Exception {
>         new AIOClient().connect("localhost", 8000);
>     }
> 
>     public void connect(String host, int port) throws Exception {
>         //开始发车，连上高速公路
>         //Viod什么都不是
>         //也是实现一个匿名的接口
>         //这里只做写操作
>         client.connect(new InetSocketAddress(host, port), null, new
>                 CompletionHandler<Void, Void>() {
>                     @Override
>                     public void completed(Void result, Void attachment) {
>                         try {
>                             client.write(ByteBuffer.wrap("这是一条测试数据 ".getBytes())).get();
> //                            System.out.println("已发送至服务器");
>                         } catch (Exception ex) {
>                             ex.printStackTrace();
>                         }
>                     }
> 
>                     @Override
>                     public void failed(Throwable exc, Void attachment) {
>                         exc.printStackTrace();
>                     }
>                 });
> 
>         //下面这一段代码是只读数据
>         final ByteBuffer bb = ByteBuffer.allocate(1024);
>         client.read(bb, null, new CompletionHandler<Integer, Object>() {
>                     //实现IO操作完成的方法
>                     @Override
>                     public void completed(Integer result, Object attachment) {
>                         System.out.println("IO 操作完成" + result);
>                         System.out.println("获取反馈结果" + new String(bb.array()));
>                     }
> 
>                     @Override
>                     public void failed(Throwable exc, Object attachment) {
>                         exc.printStackTrace();
>                     }
>                 }
>         );
>         try {
>             Thread.sleep(Integer.MAX_VALUE);
>         } catch (InterruptedException ex) {
>             System.out.println(ex);
>         }
>     }
> }
> ````
>

 ## BIO 代码

```java
/**
 * BIO服务端源码
 */
@Slf4j
public class BIOServer {

    //默认的端口号
    private static int DEFAULT_PORT = 7777;

    //单例的ServerSocket
    private static ServerSocket serverSocket;

    //根据传入参数设置监听端口，如果没有参数调用以下方法并使用默认值
    public static void start() throws IOException {
        //使用默认值
        start(DEFAULT_PORT);
    }

    //这个方法不会被大量并发访问，不太需要考虑效率，直接进行方法同步就行了
    public synchronized static void start(int port) throws IOException {
        if (serverSocket != null) return;

        try {
            //通过构造函数创建ServerSocket
            //如果端口合法且空闲，服务端就监听成功
            serverSocket = new ServerSocket(port);
            System.out.println("服务端已启动，端口号:" + port);

            //通过无线循环监听客户端连接
            //如果没有客户端接入，将阻塞在accept操作上。
            while (true) {
                //阻塞
                Socket socket = serverSocket.accept();
                new Thread(new ServerHandler(socket)).start();
            }
        } finally {
            //一些必要的清理工作
            if (serverSocket != null) {
                System.out.println("服务端已关闭。");
                serverSocket.close();
                serverSocket = null;
            }
        }

    }
}
```

```java
@Slf4j
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
            out = new PrintWriter(socket.getOutputStream(), true);
            String expression;
            int result;
            while (true) {
                //通过BufferedReader读取一行
                //如果已经读到输入流尾部，返回null,退出循环
                //如果得到非空值，就尝试计算结果并返回
                if ((expression = in.readLine()) == null) break;
                System.out.println(("服务端收到信息：" + expression));

                result = Calculator.cal(expression);
                out.println(result);
            }
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println((e.getLocalizedMessage()));
        } finally {
            //一些必要的清理工作
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                in = null;

            }

            if (out != null) {

                out.close();
                out = null;

            }

            if (socket != null) {

                try {
                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                socket = null;

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
        }

        throw new Exception("Calculator error");

    }
}
```

```java
/**
 * 阻塞式I/O创建的客户端
 */
@Slf4j
public class BIOClient {

    //默认的端口号
    private static int DEFAULT_SERVER_PORT = 7777;

    private static String DEFAULT_SERVER_IP = "127.0.0.1";

    public static void send(String expression) {

        send(DEFAULT_SERVER_PORT, expression);
    }

    public static void send(int port, String expression) {
        System.out.println(("算术表达式为：" + expression));
        Socket socket = null;

        BufferedReader in = null;

        PrintWriter out = null;

        try {
            socket = new Socket(DEFAULT_SERVER_IP, port);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            out = new PrintWriter(socket.getOutputStream(), true);
            out.println(expression);
            System.out.println(("结果为：" + in.readLine()));
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                in = null;
            }

            if (out != null) {
                out.close();
                out = null;
            }

            if (socket != null) {
                try {
                    socket.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                socket = null;
            }
        }
    }
}
```

```java
public class Test {
    public static void main(String[] args) throws InterruptedException {
        //运行服务器
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    BIOServer.start();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();

        //防止客户端先于服务器启动前执行代码
        Thread.sleep(100);

        final char[] op = {'+', '-', '*', '/'};

        final Random random = new Random(System.currentTimeMillis());
        new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    //随机产生算术表达式
                    String expression = random.nextInt(10) + "" + op[random.nextInt(4)] +
                            (random.nextInt(10) + 1);
                    BIOClient.send(expression);
                    try {
                        Thread.sleep(random.nextInt(1000));
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }).start();
    }
}
```

## Buffer 代码

```java
public class BufferDemo {
    public static void decode(String str) throws UnsupportedEncodingException {
        ByteBuffer byteBuffer = ByteBuffer.allocate(128);
        byteBuffer.put(str.getBytes("UTF-8"));
        byteBuffer.flip();

        /*对获取utf8的编解码器*/
        Charset utf8 = Charset.forName("UTF-8");
        CharBuffer charBuffer = utf8.decode(byteBuffer);/*对bytebuffer中的内容解码*/

        /*array()返回的就是内部的数组引用，编码以后的有效长度是0~limit*/
        char[] charArr = Arrays.copyOf(charBuffer.array(), charBuffer.limit());
        System.out.println(charArr);
    }

    public static void encode(String str){
        CharBuffer charBuffer = CharBuffer.allocate(128);
        charBuffer.append(str);
        charBuffer.flip();

        /*对获取utf8的编解码器*/
        Charset utf8 = Charset.forName("UTF-8");
        ByteBuffer byteBuffer = utf8.encode(charBuffer); /*对charbuffer中的内容解码*/

        /*array()返回的就是内部的数组引用，编码以后的有效长度是0~limit*/
        byte[] bytes = Arrays.copyOf(byteBuffer.array(), byteBuffer.limit());
        System.out.println(Arrays.toString(bytes));
    }

    public static void main(String[] args) throws UnsupportedEncodingException {

        BufferDemo.decode("咕泡学院");
        BufferDemo.encode("咕泡学院");
    }
}
```

```java
/*自定义Buffer类中包含读缓冲区和写缓冲区，用于注册通道时的附加对象*/
public class Buffers {
 
    ByteBuffer readBuffer;
    ByteBuffer writeBuffer;
     
    public Buffers(int readCapacity, int writeCapacity){
        readBuffer = ByteBuffer.allocate(readCapacity);
        writeBuffer = ByteBuffer.allocate(writeCapacity);
    }
     
    public ByteBuffer getReadBuffer(){
        return readBuffer;
    }
     
    public ByteBuffer gerWriteBuffer(){
        return writeBuffer;
    }
}
```

## Channel代码

```java
public class FileChannelDemo {
     
    public static void main(String[] args){ 
         
        /*创建文件，向文件中写入数据*/
        try {
             
            /*如果文件不存在，创建该文件,文件后缀是不是文本文件不重要*/
            File file = new File("/Users/dingyuanjie/Documents/study/test/netty/noi_utf8.data");
            if(!file.exists()){
                file.createNewFile();
            }
             
            /*根据文件输出流创建与这个文件相关的通道*/
            FileOutputStream fos = new FileOutputStream(file);
            FileChannel fc = fos.getChannel();
             
            /*创建ByteBuffer对象， position = 0, limit = 64*/
            ByteBuffer bb = ByteBuffer.allocate(64);
             
            /*向ByteBuffer中放入字符串UTF-8的字节, position = 17, limit = 64*/
            bb.put("Hello,World 123 \n".getBytes("UTF-8"));
             
            /*flip方法  position = 0, limit = 17*/
            bb.flip();
             
            /*write方法使得ByteBuffer的position到 limit中的元素写入通道中*/
            fc.write(bb);
             
            /*clear方法使得position = 0， limit = 64*/
            bb.clear();
             
            /*下面的代码同理*/
            bb.put("你好，世界 456".getBytes("UTF-8"));
            bb.flip();
             
            fc.write(bb);
            bb.clear();
             
            fos.close();
            fc.close();
             
        } catch (FileNotFoundException e) {
             
        } catch (IOException e) {
            System.out.println(e);
        }
         
         
        /*从刚才的文件中读取字符序列*/
        try {
             
            /*通过Path对象创建文件通道*/
            Path path = Paths.get("/Users/dingyuanjie/Documents/study/test/netty/noi_utf8.data");
            FileChannel fc = FileChannel.open(path);
             
            ByteBuffer bb = ByteBuffer.allocate((int) fc.size()+1);
             
            Charset utf8 = Charset.forName("UTF-8");
             
            /*阻塞模式，读取完成才能返回*/
            fc.read(bb);
             
            bb.flip();
            CharBuffer cb = utf8.decode(bb);
            System.out.print(cb.toString());
            bb.clear();
             
 
            fc.close();
             
        } catch (IOException e) {
            e.printStackTrace();
        }
         
    }
}
```

## NIOServer

```java
/*服务器端，:接收客户端发送过来的数据并显示，
 *服务器把上接收到的数据加上"echo from service:"再发送回去*/
public class ServiceSocketChannelDemo {
     
    public static class TCPEchoServer implements Runnable{
         
        /*服务器地址*/
        private InetSocketAddress localAddress;
         
        public TCPEchoServer(int port) throws IOException{
            this.localAddress = new InetSocketAddress(port);
        }
         
         
        @Override
        public void run(){
             
            Charset utf8 = Charset.forName("UTF-8");
             
            ServerSocketChannel ssc = null;
            Selector selector = null;
             
            Random rnd = new Random();
             
            try {
                /*创建选择器*/
                selector = Selector.open();
                 
                /*创建服务器通道*/
                ssc = ServerSocketChannel.open();
                ssc.configureBlocking(false);
                 
                /*设置监听服务器的端口，设置最大连接缓冲数为100*/
                ssc.bind(localAddress, 100);
                 
                /*服务器通道只能对tcp链接事件感兴趣*/
                ssc.register(selector, SelectionKey.OP_ACCEPT);
                 
            } catch (IOException e1) {
                System.out.println("server start failed");
                return;
            } 
             
            System.out.println("server start with address : " + localAddress);
             
            /*服务器线程被中断后会退出*/
            try{
                 
                while(!Thread.currentThread().isInterrupted()){
                     
                    int n = selector.select();
                    if(n == 0){
                        continue;
                    }
 
                    Set<SelectionKey> keySet = selector.selectedKeys();
                    Iterator<SelectionKey> it = keySet.iterator();
                    SelectionKey key = null;
                     
                    while(it.hasNext()){
                             
                        key = it.next();
                        /*防止下次select方法返回已处理过的通道*/
                        it.remove();
                         
                        /*若发现异常，说明客户端连接出现问题,但服务器要保持正常*/
                        try{
                            /*ssc通道只能对链接事件感兴趣*/
                            if(key.isAcceptable()){
                                 
                                /*accept方法会返回一个普通通道，
                                     每个通道在内核中都对应一个socket缓冲区*/
                                SocketChannel sc = ssc.accept();
                                sc.configureBlocking(false);
                                 
                                /*向选择器注册这个通道和普通通道感兴趣的事件，同时提供这个新通道相关的缓冲区*/
                                int interestSet = SelectionKey.OP_READ;                             
                                sc.register(selector, interestSet, new Buffers(256, 256));
                                 
                                System.out.println("accept from " + sc.getRemoteAddress());
                            }
                             
                             
                            /*（普通）通道感兴趣读事件且有数据可读*/
                            if(key.isReadable()){
                                 
                                /*通过SelectionKey获取通道对应的缓冲区*/
                                Buffers buffers = (Buffers)key.attachment();
                                ByteBuffer readBuffer = buffers.getReadBuffer();
                                ByteBuffer writeBuffer = buffers.gerWriteBuffer();
                                 
                                /*通过SelectionKey获取对应的通道*/
                                SocketChannel sc = (SocketChannel) key.channel();
                                 
                                /*从底层socket读缓冲区中读入数据*/
                                sc.read(readBuffer);
                                readBuffer.flip();
                                 
                                /*解码显示，客户端发送来的信息*/
                                CharBuffer cb = utf8.decode(readBuffer);
                                System.out.println(cb.array());
                     
                                readBuffer.rewind();
 
                                 
                                /*准备好向客户端发送的信息*/
                                /*先写入"echo:"，再写入收到的信息*/
                                writeBuffer.put("echo from service:".getBytes("UTF-8"));
                                writeBuffer.put(readBuffer);
                                 
                                readBuffer.clear();
                                 
                                /*设置通道写事件*/
                                key.interestOps(key.interestOps() | SelectionKey.OP_WRITE);
                                                                 
                            }
                             
                            /*通道感兴趣写事件且底层缓冲区有空闲*/
                            if(key.isWritable()){
                                 
                                Buffers  buffers = (Buffers)key.attachment();
                                 
                                ByteBuffer writeBuffer = buffers.gerWriteBuffer();
                                writeBuffer.flip();
                                 
                                SocketChannel sc = (SocketChannel) key.channel();
                                 
                                int len = 0;
                                while(writeBuffer.hasRemaining()){
                                    len = sc.write(writeBuffer);
                                    /*说明底层的socket写缓冲已满*/
                                    if(len == 0){
                                        break;
                                    }
                                }
                                 
                                writeBuffer.compact();
                                 
                                /*说明数据全部写入到底层的socket写缓冲区*/
                                if(len != 0){
                                    /*取消通道的写事件*/
                                    key.interestOps(key.interestOps() & (~SelectionKey.OP_WRITE));
                                }
                                 
                            }
                        }catch(IOException e){
                            System.out.println("service encounter client error");
                            /*若客户端连接出现异常，从Seletcor中移除这个key*/
                            key.cancel();
                            key.channel().close();
                        }
 
                    }
                         
                    Thread.sleep(rnd.nextInt(500));
                }
                 
            }catch(InterruptedException e){
                System.out.println("serverThread is interrupted");
            } catch (IOException e1) {
                System.out.println("serverThread selecotr error");
            }finally{
                try{
                    selector.close();
                }catch(IOException e){
                    System.out.println("selector close failed");
                }finally{
                    System.out.println("server close");
                }
            }
 
        }
    }
     
    public static void main(String[] args) throws InterruptedException, IOException{
        Thread thread = new Thread(new TCPEchoServer(8080));
        thread.start();
        Thread.sleep(100000);
        /*结束服务器线程*/
        thread.interrupt();
    }
     
}
```

## NIOClient

```java
/*客户端:客户端每隔1~2秒自动向服务器发送数据，接收服务器接收到数据并显示*/
public class ClientSocketChannelDemo {
     
    public static class TCPEchoClient implements Runnable{
         
        /*客户端线程名*/
        private String name;
        private Random rnd = new Random();
         
        /*服务器的ip地址+端口port*/
        private InetSocketAddress remoteAddress;
         
        public TCPEchoClient(String name, InetSocketAddress remoteAddress){
            this.name = name;
            this.remoteAddress = remoteAddress;
        }
         
        @Override
        public void run(){
             
            /*创建解码器*/
            Charset utf8 = Charset.forName("UTF-8");
             
            Selector selector;
             
            try {
                 
                /*创建TCP通道*/
                SocketChannel sc = SocketChannel.open();
                 
                /*设置通道为非阻塞*/
                sc.configureBlocking(false);
                 
                /*创建选择器*/
                selector = Selector.open();
                 
                /*注册感兴趣事件*/
                int interestSet = SelectionKey.OP_READ | SelectionKey.OP_WRITE;
                 
                /*向选择器注册通道*/
                sc.register(selector, interestSet, new Buffers(256, 256));
                 
                /*向服务器发起连接,一个通道代表一条tcp链接*/
                sc.connect(remoteAddress);
                 
                /*等待三次握手完成*/
                while(!sc.finishConnect()){
                    ;
                }
 
                System.out.println(name + " " + "finished connection");
                 
            } catch (IOException e) {
                System.out.println("client connect failed");
                return;
            }
             
            /*与服务器断开或线程被中断则结束线程*/
            try{
 
                int i = 1;
                while(!Thread.currentThread().isInterrupted()){
                     
                    /*阻塞等待*/
                    selector.select();
                     
                    /*Set中的每个key代表一个通道*/
                    Set<SelectionKey> keySet = selector.selectedKeys();
                    Iterator<SelectionKey> it = keySet.iterator();
                     
                    /*遍历每个已就绪的通道，处理这个通道已就绪的事件*/
                    while(it.hasNext()){
                         
                        SelectionKey key = it.next();
                        /*防止下次select方法返回已处理过的通道*/
                        it.remove();
                         
                        /*通过SelectionKey获取对应的通道*/
                        Buffers buffers = (Buffers)key.attachment();
                        ByteBuffer readBuffer = buffers.getReadBuffer();
                        ByteBuffer writeBuffer = buffers.gerWriteBuffer();
                         
                        /*通过SelectionKey获取通道对应的缓冲区*/
                        SocketChannel sc = (SocketChannel) key.channel();
                         
                        /*表示底层socket的读缓冲区有数据可读*/
                        if(key.isReadable()){
                            /*从socket的读缓冲区读取到程序定义的缓冲区中*/
                            sc.read(readBuffer);
                            readBuffer.flip();
                            /*字节到utf8解码*/
                            CharBuffer cb = utf8.decode(readBuffer);
                            /*显示接收到由服务器发送的信息*/
                            System.out.println(cb.array());
                            readBuffer.clear();
                        }
                         
                        /*socket的写缓冲区可写*/
                        if(key.isWritable()){
                            writeBuffer.put((name + "  " + i).getBytes("UTF-8"));
                            writeBuffer.flip();
                            /*将程序定义的缓冲区中的内容写入到socket的写缓冲区中*/
                            sc.write(writeBuffer);
                            writeBuffer.clear();
                            i++;
                        }
                    }
                     
                    Thread.sleep(1000 + rnd.nextInt(1000));
                }
             
            }catch(InterruptedException e){
                System.out.println(name + " is interrupted");
            }catch(IOException e){
                System.out.println(name + " encounter a connect error");
            }finally{
                try {
                    selector.close();
                } catch (IOException e1) {
                    System.out.println(name + " close selector failed");
                }finally{
                    System.out.println(name + "  closed");
                }
            }
        }
         
    }
     
    public static void main(String[] args) throws InterruptedException{
         
        InetSocketAddress remoteAddress = new InetSocketAddress("127.0.0.1", 8080);
         
        Thread ta = new Thread(new TCPEchoClient("thread a", remoteAddress));
        Thread tb = new Thread(new TCPEchoClient("thread b", remoteAddress));
        Thread tc = new Thread(new TCPEchoClient("thread c", remoteAddress));
        Thread td = new Thread(new TCPEchoClient("thread d", remoteAddress));
         
        ta.start();
        tb.start();
        tc.start();
         
        Thread.sleep(5000);
 
        /*结束客户端a*/
        ta.interrupt();
         
        /*开始客户端d*/
        td.start();
    }
}
```

## Demo

```java
/**
 * 编解码工具类
 */
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
public class NioServer {

    private ServerSocketChannel serverSocketChannel;
    private Selector selector;

    public NioServer() throws IOException {
        // 打开 Server Socket Channel
        serverSocketChannel = ServerSocketChannel.open();
        // 配置为非阻塞
        serverSocketChannel.configureBlocking(false);
        // 绑定 Server port
        serverSocketChannel.socket().bind(new InetSocketAddress(8080));
        // 创建 Selector
        selector = Selector.open();
        // 注册 Server Socket Channel 到 Selector
        serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
        System.out.println("Server 启动完成");

        handleKeys();
    }

    @SuppressWarnings("Duplicates")
    private void handleKeys() throws IOException {
        while (true) {
            // 通过 Selector 选择 Channel
            int selectNums = selector.select(30 * 1000L);
            if (selectNums == 0) {
                continue;
            }
            System.out.println("选择 Channel 数量：" + selectNums);

            // 遍历可选择的 Channel 的 SelectionKey 集合
            Iterator<SelectionKey> iterator = selector.selectedKeys().iterator();
            while (iterator.hasNext()) {
                SelectionKey key = iterator.next();
                iterator.remove(); // 移除下面要处理的 SelectionKey
                if (!key.isValid()) { // 忽略无效的 SelectionKey
                    continue;
                }

                handleKey(key);
            }
        }
    }

    private void handleKey(SelectionKey key) throws IOException {
        // 接受连接就绪
        if (key.isAcceptable()) {
            handleAcceptableKey(key);
        }
        // 读就绪
        if (key.isReadable()) {
            handleReadableKey(key);
        }
        // 写就绪
        if (key.isWritable()) {
            handleWritableKey(key);
        }
    }

    private void handleAcceptableKey(SelectionKey key) throws IOException {
        // 接受 Client Socket Channel
        SocketChannel clientSocketChannel = ((ServerSocketChannel) key.channel()).accept();
        // 配置为非阻塞
        clientSocketChannel.configureBlocking(false);
        // log
        System.out.println("接受新的 Channel");
        // 注册 Client Socket Channel 到 Selector
        clientSocketChannel.register(selector, SelectionKey.OP_READ, new ArrayList<String>());
    }

    private void handleReadableKey(SelectionKey key) throws IOException {
        // Client Socket Channel
        SocketChannel clientSocketChannel = (SocketChannel) key.channel();
        // 读取数据
        ByteBuffer readBuffer = CodecUtil.read(clientSocketChannel);
        // 处理连接已经断开的情况
        if (readBuffer == null) {
            System.out.println("断开 Channel");
            clientSocketChannel.register(selector, 0);
            return;
        }
        // 打印数据
        if (readBuffer.position() > 0) { // 写入模式下，
            String content = CodecUtil.newString(readBuffer);
            System.out.println("读取数据：" + content);

            // 添加到响应队列
            List<String> responseQueue = (ArrayList<String>) key.attachment();
            responseQueue.add("响应：" + content);
            // 注册 Client Socket Channel 到 Selector
            clientSocketChannel.register(selector, SelectionKey.OP_WRITE, key.attachment());
        }
    }

    @SuppressWarnings("Duplicates")
    private void handleWritableKey(SelectionKey key) throws ClosedChannelException {
        // Client Socket Channel
        SocketChannel clientSocketChannel = (SocketChannel) key.channel();

        // 遍历响应队列
        List<String> responseQueue = (ArrayList<String>) key.attachment();
        for (String content : responseQueue) {
            // 打印数据
            System.out.println("写入数据：" + content);
            // 返回
            CodecUtil.write(clientSocketChannel, content);
        }
        responseQueue.clear();

        // 注册 Client Socket Channel 到 Selector
        clientSocketChannel.register(selector, SelectionKey.OP_READ, responseQueue);
    }

    public static void main(String[] args) throws IOException {
        NioServer server = new NioServer();
    }

}
```

```java
public class NioClient {

    private SocketChannel clientSocketChannel;
    private Selector selector;
    private final List<String> responseQueue = new ArrayList<String>();

    private CountDownLatch connected = new CountDownLatch(1);

    public NioClient() throws IOException, InterruptedException {
        // 打开 Client Socket Channel
        clientSocketChannel = SocketChannel.open();
        // 配置为非阻塞
        clientSocketChannel.configureBlocking(false);
        // 创建 Selector
        selector = Selector.open();
        // 注册 Server Socket Channel 到 Selector
        clientSocketChannel.register(selector, SelectionKey.OP_CONNECT);
        // 连接服务器
        clientSocketChannel.connect(new InetSocketAddress(8080));

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    handleKeys();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }).start();

        if (connected.getCount() != 0) {
            connected.await();
        }
        System.out.println("Client 启动完成");
    }

    @SuppressWarnings("Duplicates")
    private void handleKeys() throws IOException {
        while (true) {
            // 通过 Selector 选择 Channel
            int selectNums = selector.select(30 * 1000L);
            if (selectNums == 0) {
                continue;
            }

            // 遍历可选择的 Channel 的 SelectionKey 集合
            Iterator<SelectionKey> iterator = selector.selectedKeys().iterator();
            while (iterator.hasNext()) {
                SelectionKey key = iterator.next();
                iterator.remove(); // 移除下面要处理的 SelectionKey
                if (!key.isValid()) { // 忽略无效的 SelectionKey
                    continue;
                }

                handleKey(key);
            }
        }
    }

    private synchronized void handleKey(SelectionKey key) throws IOException {
        // 接受连接就绪
        if (key.isConnectable()) {
            handleConnectableKey(key);
        }
        // 读就绪
        if (key.isReadable()) {
            handleReadableKey(key);
        }
        // 写就绪
        if (key.isWritable()) {
            handleWritableKey(key);
        }
    }

    private void handleConnectableKey(SelectionKey key) throws IOException {
        // 完成连接
        if (!clientSocketChannel.isConnectionPending()) {
            return;
        }
        clientSocketChannel.finishConnect();
        // log
        System.out.println("接受新的 Channel");
        // 注册 Client Socket Channel 到 Selector
        clientSocketChannel.register(selector, SelectionKey.OP_READ, responseQueue);
        // 标记为已连接
        connected.countDown();
    }

    @SuppressWarnings("Duplicates")
    private void handleReadableKey(SelectionKey key) throws ClosedChannelException {
        // Client Socket Channel
        SocketChannel clientSocketChannel = (SocketChannel) key.channel();
        // 读取数据
        ByteBuffer readBuffer = CodecUtil.read(clientSocketChannel);
        // 打印数据
        if (readBuffer.position() > 0) { // 写入模式下，
            String content = CodecUtil.newString(readBuffer);
            System.out.println("读取数据：" + content);
        }
    }

    @SuppressWarnings("Duplicates")
    private void handleWritableKey(SelectionKey key) throws ClosedChannelException {
        // Client Socket Channel
        SocketChannel clientSocketChannel = (SocketChannel) key.channel();

        // 遍历响应队列
        List<String> responseQueue = (ArrayList<String>) key.attachment();
        for (String content : responseQueue) {
            // 打印数据
            System.out.println("写入数据：" + content);
            // 返回
            CodecUtil.write(clientSocketChannel, content);
        }
        responseQueue.clear();

        // 注册 Client Socket Channel 到 Selector
        clientSocketChannel.register(selector, SelectionKey.OP_READ, responseQueue);
    }

    public synchronized void send(String content) throws ClosedChannelException {
        // 添加到响应队列
        responseQueue.add(content);
        // 打印数据
        System.out.println("写入数据：" + content);
        // 注册 Client Socket Channel 到 Selector
        clientSocketChannel.register(selector, SelectionKey.OP_WRITE, responseQueue);
        selector.wakeup();
    }

    public static void main(String[] args) throws IOException, InterruptedException {
        NioClient client = new NioClient();
        for (int i = 0; i < 30; i++) {
            client.send("nihao: " + i);
            Thread.sleep(1000L);
        }
    }

}
```





