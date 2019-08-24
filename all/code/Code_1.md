# Readme

## Socket

* 编写一个网络应用程序，有客户端与服务器端，客户端向服务器发送一个字符串，服务器收到该字符串后将其打印到命令行上，然后向客户端返回改字符串的长度，最后，客户端输出服务器端返回的该字符串的长度，分别用TCP和UDP两种方式去实现

  ```java
  public class LengthCalculator extends Thread {
      //以socket为成员变量
      private Socket socket;
      public LengthCalculator(Socket socket) { this.socket = socket; }
  
      @Override
      public void run() {
          try {
              //获取socket的输出流
              OutputStream os = socket.getOutputStream();
              //获取socket的输入流
              InputStream is = socket.getInputStream();
              int ch = 0;
              byte[] buff = new byte[1024];
              //buff主要用来读取输入的内容，存成byte数组，ch主要用来获取读取数组的长度
              ch = is.read(buff);
              //将接收流的byte数组转换成字符串，这里获取的内容是客户端发送过来的字符串参数
              String content = new String(buff, 0, ch);
              System.out.println(content);
              //往输出流里写入获得的字符串的长度，回发给客户端
              os.write(String.valueOf(content.length()).getBytes());
              //不要忘记关闭输入输出流以及socket
              is.close();
              os.close();
              socket.close();
          } catch (IOException e) {
              e.printStackTrace();
          }
      }
  }
  ```

  ```java
  public class TCPClient {
      public static void main(String[] args) throws Exception {
          //创建socket，并指定连接的是本机的端口号为65000的服务器socket
          Socket socket = new Socket("127.0.0.1", 65000);
          //获取输出流
          OutputStream os = socket.getOutputStream();
          //获取输入流
          InputStream is = socket.getInputStream();
          //将要传递给server的字符串参数转换成byte数组，并将数组写入到输出流中
          os.write(new String("hello world").getBytes());
          int ch = 0;
          byte[] buff = new byte[1024];
          //buff主要用来读取输入的内容，存成byte数组，ch主要用来获取读取数组的长度
          ch = is.read(buff);
          //将接收流的byte数组转换成字符串，这里是从服务端回发回来的字符串参数的长度
          String content = new String(buff, 0, ch);
          System.out.println(content);
          //不要忘记关闭输入输出流以及socket
          is.close();
          os.close();
          socket.close();
      }
  }
  ```

  ```java
  public class TCPServer {
      public static void main(String[] args) throws Exception {
          //创建socket,并将socket绑定到65000端口
          ServerSocket ss = new ServerSocket(65000);
          //死循环，使得socket一直等待并处理客户端发送过来的请求
          while (true) {
              //监听65000端口，直到客户端返回连接信息后才返回
              Socket socket = ss.accept();
              //获取客户端的请求信息后，执行相关业务逻辑
              new LengthCalculator(socket).start();
          }
      }
  }
  ```

  ```java
  public class UDPClient {
      public static void main(String[] args) throws Exception {
          // 客户端发数据报给服务端
          DatagramSocket socket = new DatagramSocket();
          // 要发送给服务端的数据
          byte[] buf = "Hello World".getBytes();
          // 将IP地址封装成InetAddress对象
          InetAddress address = InetAddress.getByName("127.0.0.1");
          // 将要发送给服务端的数据封装成DatagramPacket对象 需要填写上ip地址与端口号
          DatagramPacket packet = new DatagramPacket(buf, buf.length, address,
                  65001);
          // 发送数据给服务端
          socket.send(packet);
  
          // 客户端接受服务端发送过来的数据报
          byte[] data = new byte[100];
          // 创建DatagramPacket对象用来存储服务端发送过来的数据
          DatagramPacket receivedPacket = new DatagramPacket(data, data.length);
          // 将接受到的数据存储到DatagramPacket对象中
          socket.receive(receivedPacket);
          // 将服务器端发送过来的数据取出来并打印到控制台
          String content = new String(receivedPacket.getData(), 0,
                  receivedPacket.getLength());
          System.out.println(content);
      }
  }
  ```

  ```java
  public class UDPServer {
      public static void main(String[] args) throws Exception {
          // 服务端接受客户端发送的数据报
          DatagramSocket socket = new DatagramSocket(65001); //监听的端口号
          byte[] buff = new byte[100]; //存储从客户端接受到的内容
          DatagramPacket packet = new DatagramPacket(buff, buff.length);
          //接受客户端发送过来的内容，并将内容封装进DatagramPacket对象中
          socket.receive(packet);
          byte[] data = packet.getData(); //从DatagramPacket对象中获取到真正存储的数据
          //将数据从二进制转换成字符串形式
          String content = new String(data, 0, packet.getLength());
          System.out.println(content);
          //将要发送给客户端的数据转换成二进制
          byte[] sendedContent = String.valueOf(content.length()).getBytes();
          // 服务端给客户端发送数据报
          //从DatagramPacket对象中获取到数据的来源地址与端口号
          DatagramPacket packetToClient = new DatagramPacket(sendedContent,
                  sendedContent.length, packet.getAddress(), packet.getPort());
          socket.send(packetToClient); //发送数据给客户端
      }
  }
  ```

  

