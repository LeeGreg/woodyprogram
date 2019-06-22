# 同步与阻塞

* 阻塞和非阻塞是进程在访问数据的时候，数据是否准备就绪的一种处理方式
  * 阻塞：当进程访问数据缓冲区的时候，进程需要等待缓冲区中的数据准备好后才处理其他的事情，否则一直等待在那
  * 非阻塞：当进程访问数据缓冲区的时候，如果数据没有准备好则直接返回，不会等待
* 同步和异步都是基于应用程序和操作系统处理 IO 事件所采用的方式
  * 同步：应用程序直接参与 IO 读写操作
    * 同步方式在处理 IO 事件的时候，必须阻塞在某个方法上面等待 IO 事件完成
  * 异步：所有的 IO 读写交给操作系统去处理，应用程序可以去做其他的事情，只需要等待通知

# Java IO与Java NIO

* IO
  * 面向流，从流中（file或者socket）一次读取一个或多个字节（不会缓存在任何地方）
  * 阻塞IO，每个流都需要一个线程去处理，当线程调用`read()`或`write()`时，该线程将被阻塞，直到有一些数据要读取，或者数据被完全写入。在此期间，该线程无法执行任何其他操作
* NIO
  * 面向`Buffer`，数据被读入缓冲区，稍后处理该缓冲区
  * 非阻塞，线程从`Channel`中请求或写入数据时，线程不会阻塞直到所有可读数据准备好才读取，也不必等所有可写数据都准备好才写入到`Channel`中，线程也可以去做其他事情
  * `Selector`
* 如果要同时管理上千个只发送少量数据的连接，例如聊天服务器，那使用NIO作为服务器有优势；如果需要维护一些和其他计算机的连接，例如在P2P网络中，使用单个线程去管理所有外部连接有优势。如有需发送大量数据、占用高宽带的少量连接，则使用IO Server比较适合，一个连接通过一个线程处理

# Java IO模型

- 1：1同步阻塞IO通信模型，一个客户端对应一个线程
- M：N同步阻塞IO通信模型，使用线程池
- 非阻塞式IO模型(NIO)，NIO+单线程`Reactor`模式
- 非阻塞式IO模型(NIO)，NIO+多线程`Reactor`模式
- NIO+主从多线程Reactor模式

# Java NIO组件

* 在 Java 1.4 中推出了 NIO
* 线程从channel中读取数据到buffer时，该线程还能够做其他事情，一旦数据读取到buffer后，线程能够继续处理该数据。对于写数据也是一样的
* 网络传输一定要编码。网络通信的时候采用二进制形式，用可传输字节流的方式进行传输

## Buffer

* 可以理解成一个特殊的数组，内置一些属性能够跟踪和记录缓冲区的状态变化情况
  * 数据总是从channel读取到buffer，或者从buffer写入到channel
* 属性
  * `capacity`
  * `position`：值由 `get()/put()`方法自动更新
  * `limit`
    - `capacity`：`buffer`初始大小
    - 写模式：开始`position`为0，写入数据就增加，最大为`capacity-1`，`limit=capacity`
    - 读模式：调用`flip()`从写模式切换到读模式，`position`为0，`limit`为切换前`position`大小，读出数据`position`就增加，最大为`limit`大小
* 分类
  * 缓冲区分片，`buffer.slice()`，创建一个数据共享的子缓冲区
  * 只读缓冲区，`asReadOnlyBuffer()`，与原缓冲区共享数据，只读
  * 直接缓冲区，`allocateDirect()`，创建和销毁比一般堆内Buffer增加部分开销，但可加快 `I/O`速度
  * 内存映射文件`I/O`，`MappedByteBufer`，直接操作将文件按照指定大小直接映射为内存区域内的文件数据，省去了将数据从内核空间向用户空间传输的损耗，本质上也是种`Direct Bufer`
* 使用
  * 分配`buffer`、 数据写入`buffer`、 `buffer.flip()`、从`buffer`读出数据、调用`buffer.clear()`或`buffer.compact()`
* 分配`buffer`，`ByteBuffer buf = ByteBuffer.allocate(48);`
* 写入`Buffer`
  * `int bytesRead = channel.read(buf);`或`buf.put(127)`
* 切换模式`flip（）`
  * 将`Buffer`的写入模式切换到读取模式，将`position`属性重置为0，`limit`设置为`position`重置前大小
* 读取`Buffer`
  * `int bytesWritten = channel.write(buf)`或`byte aByte = buf.get();`
* `Buffer.rewind()`，将`position`重置为0，可以再次读取所有`Buffer`中数据，`limit`不变
* 读取完数据之后，写入数据之前调用
  * `clear()`把所有状态设置为初始值，将`position`设置为0，`limit`设置为`capacity`
  * `compact()`保留未读取数据到最前，`position`为最后一个未读取数据后一位置，`limit`设置为`capacity`
* `mark()`标记`position`位置，后面调用`reset()`可将`position`设置为`mark()`标记时位置
* `equals()`相同条件：类型相同、剩余未读数据数量相等、剩余未读数据`equal`为`true`
* `Scatter`：可以将数据从`Channel`读取到多个`Buffer`中，使用`ByteBuffer[]`
* `Gather`：将多个`Buffer`中数据写入到`Channel`中，使用`ByteBuffer[]`

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

## Channel 

* `ServerSocketChannel`，监听`tcp`连接，对于每个连接都创建一个`SocketChannel`
* `SocketChannel`，连接到一个`tcp`的`Socket`，等价于`Java`网络编程的`Socket`
* `FileChannel`
* `DatagramChannel`，是一个用来接收和发送UPD数据包的`Channel`

## Selector

* 是一个组件，能够注册、监听多个Channel，通过事件通知判断哪些Channel已经准备好读取和写入，使得单个线程能够管理多个Channel，因此能够管理多个网络连接

  * `channel`注册到`selector`，然后调用`selector`的`select()`方法阻塞直到某个已注册`channel`的事件准备好，一旦`select()`方法返回，该线程能够处理这些事件，例如连接事件、数据到达事件
  * 当有读或写等任何注册的事件发生时，可从` Selector `中获得相应的`SelectionKey`，同时从 `SelectionKey`中可找到发生的事件和该事件所发生的具体的`SelectableChannel`，以获得客户端发送过来的数据
  * 使用较少的线程便可以处理许多连接，因此也减少了内存管理和上下文切换所带来开销
  * 适用于打开很多`channel`，但是只是传输少量数据
  * 反应堆是个抽象的概念，`selector`是具体的行为的体现

* 监听`channel`事件

  * `Connect（SelectionKey.OP_CONNECT）`：一个`Channel`已成功连接到一个服务器
  * `Accept（SelectionKey.OP_ACCEPT）`：一个`ServerSocketChannel`接收到一个连接
  * `Read（SelectionKey.OP_READ）`：一个`Channel`有数据可以被读取
  * `Write（SelectionKey.OP_WRITE）`：一个`Channel`准备好被写入

* `Selector`选择`Channel`

  * 通过调用`select()`，返回已经准备好的`Channel`，并且对该`Channel`的某些事件(`connect`,` accept`, `read` 、` write`)感兴趣
  * 调用`selector`的`selectedKeys()`返回已经准备好的`Channel`

* `SelectionKey`，`Channel`注册到`Selector`时返回，代表一个`Channel`与`Selector`的注册关系

  * `selectionKey.channel();`，返回的可能是`ServerSocketChannel`或者`SocketChannel`

  * `Selector selector = selectionKey.selector();`

  * `An attached object (optional)`，可通过在`SelectionKey`中附件一个对象来识别一个给定的`Channel`，或在`Channel`上附件一些信息（如`Buffer`、对象）

    ```java
    selectionKey.attach(theObject);
    Object attachedObj = selectionKey.attachment();
    // Channel注册时即附加一些信息
    SelectionKey key = channel.register(selector, SelectionKey.OP_READ, theObject);
    ```

* 当从`SelectableChannel`中读取数据时不能确定读取了多少数据，可能得到部分、完整、超过完整的数据，所以需要

  - 检查是否读取了完整数据
  - 如果只读取了部分数据，则需存储该数据直到下部分数据到来

  1. 尽可能复制少的数据，数据越多性能越低
  2. 将完整的数据存入到一个连续的字节序列中使其容易处理

* 一些协议消息格式使用`TLV(Type, Length, Value)`格式编码，当消息到达时，消息的总长度存储在消息的开头，这样就可以立即知道为整个消息分配多少内存

* `wakeUp()`，通过一个不同的线程调用`Selector.wakeup()`，这个在`select()`中等待的线程将立即返回

* `close()`，关闭`Selector`并且使其上注册的`SelectionKey`实例失效，但是这些`Channel`没有关闭

* IO多路复用（同步非阻塞）：简单理解为多个客户端使用了同一个`selector`线程

# AIO

* jdk1.7，把 IO 读写操作完全交给操作系统，然后等待通知
* 在IO多路复用模型中，事件循环将文件句柄的状态事件通知给用户线程，由用户线程自行读取数据、处理数据
* 异步IO模型中，内核将数据读取并放在用户线程指定的缓冲区内后通知用户线程去直接处理

# 其他

* `Java NIO Pipe`是两个线程之间的单向数据连接
  * 管道具有源通道和接收器通道。将数据写入接收器通道（`sink channel`），然后可以从源通道读取该数据（ `source channel`）
* `Java Path`（`java.nio.file.Path`）实例代表了一个文件系统的路径，能够指向一个文件或目录
  * 分绝对路径和相对路径

# Demo

## BIO

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

## NIO

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

## AIO

```java
Path path = Paths.get("data/test.xml");
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



