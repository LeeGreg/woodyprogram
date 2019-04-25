##==《Java编程思想》第21章并发==

# MS

* 提到线程安全，要将读写分开考虑

  * 线程安全：读或者写都是数据一致
  * 只读必然是线程安全
  * 线程不安全：读写并存的情况

* ⽤什么工具和方法分析线程问题

  * 死锁问题,可以使用java自带的==jstack==命令进行排查
    1. jps、ps -ef | grep java查看==当前java进程的pid==。（严重情况下可以使用top命令查看当前系统cpu/内存使用率最高的进程pid）
       - 这里我们的死锁的pid是：3429，这里程序很简单，虽然程序死锁，没有占用很多资源。
    2. 使用top -Hp 3429命令==查看进程里面占用最多的资源的线程==。
       - 占用最多资源的线程是：3440
    3. 使用命令printf "%x\n" 3440 把==线程pid转换成16进制数==，得到：d70
    4. 使用jstack 3429 | grep -20 d70命令==查询该线程阻塞==的地方。
       - 会显示具体代码位置

* 各种线程池的使用场景

* final、finalize和finally的不同之处？

  * final 是一个修饰符，可以修饰变量、方法和类。如果 final 修饰变量，意味着该变量的值在初始化后不能被改变
  * finalize 方法是在对象被回收之前调用的方法，给对象自己最后一个复活的机会，但是什么时候调用 finalize 没有保证
  * finally 是一个关键字，与 try 和 catch 一起用于异常的处理。finally 块一定会被执行，无论在 try 块中是否有发生异常

* 在多线程环境下，SimpleDateFormate是线程安全的吗？

  * 是线程不安全的类，一般不要定义为static变量，如果定义为 static，必须加锁，或者使用 DateUtils 工具类 

  * 说明:如果是 JDK8 的应用，可以使用 Instant 代替 Date，LocalDateTime 代替Calendar，DateTimeFormatter 代替 SimpleDateFormat，官方给出的解释:simple beautiful strong immutable thread-safe。

  * ==使用局部变量==，需要的时候创建新实例，将有线程安全问题的对象由共享变为局部私有都能避免多线程问题，不过也加重了创建对象的负担

  * 使用同步：同步SimpleDateFormat对象，多线程并发量大的时候会对性能有一定的影响

  * 使用ThreadLocal：（推荐）

    * 使用ThreadLocal, 也是将共享变量变为独享，线程独享肯定能比方法独享在并发环境中能减少不少创建对象的开销。如果对性能要求比较高的情况下，一般推荐使用这种方法

    ```java
    private static ThreadLocal<DateFormat> threadLocal = new ThreadLocal<DateFormat>() {
      @Override
      protected DateFormat initialValue() {
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
      }
    };
    
    public static Date parse(String dateStr) throws ParseException {
      return threadLocal.get().parse(dateStr);
    }
    public static String format(Date date) {
      return threadLocal.get().format(date);
    }
    ```

* 线程安全的单例模式

  ```java
  //静态内部类方式
  public class Singleton {  
      private static class SingletonHolder {  
          private static final Singleton INSTANCE = new Singleton();  
      }  
      private Singleton (){}  
      public static final Singleton getInstance() {  
          return SingletonHolder.INSTANCE; 
      }  
  }
  
  //双重检查锁
  public class Singleton {
      private volatile static Singleton instance; //声明成 volatile,禁止指令重排序优化
      private Singleton (){}
  
      public static Singleton getSingleton() {
          if (instance == null) {                         
              synchronized (Singleton.class) {
                  if (instance == null) {      
                      //1. 给 instance 分配内存
                      //2. 调用 Singleton 的构造函数来初始化成员变量
                      //3. 将instance对象指向分配的内存空间
                      //JVM 的即时编译器中存在指令重排序的优化，2和3的顺序是不能保证的
                      instance = new Singleton();
                  }
              }
          }
          return instance;
      }
  }
  ```

* 三条线程依次交替输出⼗次ABC

  ```java
  public static void main(String[] args) {
          PrintABC printABC = new PrintABC();         // 同一个对象
          new Thread(new Runnable() {
              @Override
              public void run() {
                  for (int i = 0; i < 10; i++) {
                      printABC.printA();               //同步执行,java7时printABC得是final
                  }
              }
          }).start();
  
          new Thread(new Runnable() {
              @Override
              public void run() {
                  for (int i = 0; i < 10; i++) {
                      printABC.printB();
                  }
              }
          }).start();
  
          new Thread(new Runnable() {
              @Override
              public void run() {
                  for (int i = 0; i < 10; i++) {
                      printABC.printC();
                  }
              }
          }).start();
      }//main
  }
  
  class PrintABC {
      private int flag = 1;
  
      public synchronized void printA() {
          //注意是while，不是if
          while (flag != 1) {
              try {
                  this.wait();// this表示当前方法所在的对象
              } catch (InterruptedException e) {
                  e.printStackTrace();
              }
          }
          System.out.println("A"+ "," + Thread.currentThread().getName());
          flag = 2;
          this.notifyAll();
      }
  
      public synchronized void printB() {
          while (flag != 2) {
              try {
                  this.wait();
              } catch (InterruptedException e) {
                  e.printStackTrace();
              }
          }
          System.out.println("B"+ "," + Thread.currentThread().getName());
          flag = 3;
          this.notifyAll();
      }
  
      public synchronized void printC() {
          while (flag != 3) {
              try {
                  this.wait();
              } catch (InterruptedException e) {
                  e.printStackTrace();
              }
          }
          System.out.println("C"+ "," + Thread.currentThread().getName());
          flag = 1;
          this.notifyAll();
      }
  }
  ```

* 三个线程T1,T2,T3,保证三个线程顺序执行T1结束执行T2,T2结束执行T3

  ```java
  public static void main(String[] args) {
      final Thread t1 = new Thread(new Runnable() {
          @Override
          public void run() {
              System.out.println("t1");
          }
      });
      final Thread t2 = new Thread(new Runnable() {
          @Override
          public void run() {
              try {
                  // 引用t1线程，等待t1线程执行完
                  t1.join();           // java1.7 t1得是final修饰
              } catch (InterruptedException e) {
                  e.printStackTrace();
              }
              System.out.println("t2");
          }
      });
      Thread t3 = new Thread(new Runnable() {
          @Override
          public void run() {
              try {
                  // 引用t2线程，等待t2线程执行完
                  t2.join();
              } catch (InterruptedException e) {
                  e.printStackTrace();
              }
              System.out.println("t3");
          }
      });
      //这里三个线程的启动顺序可以任意，大家可以试下！
      t1.start();
      t2.start();
      t3.start();
  }
  ```

* ++i是线程安全的吗?为什么?如何保证++i是线程安全的

  ```java
  //++i和i++操作并不是线程安全的，不是原子操作，在使用的时候，不可避免的会用到synchronized关键字
  private static int count = 0;
  public static void main(String[] args) {
      for (int i = 0; i < 2; i++) {
          new Thread(new Runnable() {
              @Override
              public void run() {
                  try {
                      Thread.sleep(10);
                  } catch (Exception e) {
                      e.printStackTrace();
                  }
                  //每个线程让count自增100次
                  for (int i = 0; i < 100; i++) {
                      synchronized (ThreadTest.class){         //全局锁
                          count++;
                      }
                  }
              }
          }).start();
      }
  
      try{
          Thread.sleep(2000);
      }catch (Exception e){
          e.printStackTrace();
      }
      System.out.println(count);
  }
  ```

# 多线程基础

## 为什么使用多线程

* 前提：
  * 线程允许在同一个进程中同时存在多个程序控制流
  * 线程会共享进程范围内的资源，例如内存句柄和文件句柄，但每个线程都有各自的程序计数器、栈以及局部变量等
  * 线程还提供了一种直观的分解模式来充分利用多处理器系统中的硬件并行性，而在同一个程序中的多个线程也可以被同时调度到多个CPU上运行

* 解决“阻塞”的问题
  * 阻塞的意思就是程序需要等待网络、I/O==响应==而暂时停止 CPU 占用的情况，也就是说会使得 ==CPU 闲置==。此时可以采用异步线程的方式来减少阻塞
* 通过并行计算提高程序执行性能
  * 通过多线程的技术，使得一个函数中的多个逻辑运算通过多线程技术达到一个==并行执行==，从而提升性能

## 如何理解多线程

* 定义：多线程是指==从软件或者硬件上实现多个线程并发执行的技术==。具有多线程能力的计算机因有硬件支持而==能够在同一时间执行多于一个线程，进而提升整体处理性能==
* 存在的原因：因为单线程处理能力低
* 实现：在Java里如何实现线程，继承Thread、实现Runnable接口、Callable
* ==优势==
  * 提高系统的吞吐率：多线程编程使得一个进程中可以有多个并发（即同时进行的）的操作。例如，当一个线程因为I/O操作而处于等待时，其他线程仍然可以执行其他操作
  * 提高响应性：在使用多线程编程的情况下，对于Web应用程序而言，一个请求的处理慢了并不会影响其他请求的处理
  * 充分利用多核处理器资源
* ==风险==
  * 线程安全：多个线程共享数据时，如果没有采用相应的并发访问控制措施，那么就可能产生==数据一致性的问题==，如读取脏数据（过期的数据）、丢失更新（某些线程所做的更新被其他线程所做的更新覆盖）等
  * 死锁
  * 上下文切换（处理器从执行一个线程转向执行另外一个线程的时候，操作系统所需要做的一个动作）导致的资源消耗

* 进程
  * 定义：==当⼀个程序进入内存运行时，即变成一个进程。进程里包含很多线程，线程是进程中负责执行程序的执行单元，线程本身是依靠程序进行运行的，线程本身是程序的顺序控制流==
  * 系统中独立存在的实体，==拥有⾃己的独立资源==；
  * 进程==是一个正在系统中活动的指令集合==，程序只是⼀个静态的指令集合
  * ==多个进程可以在单个处理器上并发执行，多个进程之间不会互相影响==
    * 同⼀时刻只能有一条指令执行，但多个进程指令被快速轮换执行，使得宏观上具有多个进程同时执行的效果
* 线程
  * 进程的执⾏单元，其在程序中是独立、并发的执行流。不拥有系统资源，与父进程的其他线程共享该进程所拥有的全部资源。线程的调度和管理，由进程本身负责完成
  * 优势
    * 线程的上下文转换比进程快
    * Java内置多线程功能⽀持
    * 系统创建进程需为该进程重新分配系统资源，但线程代价小，效率⾼
    * 进程之间不能共享内存，但线程非常容易
  * 单线程，是指每一个时刻，cpu只能运行一个线程
  * 多线程存在上下文的切换，会导致程序执行时间变慢，但是用户响应的时间会减少
* 任务：线程所要完成的计算
* 多线程编程的优势和风险

## 线程API

* 创建、启动

  * 继承Thread类，重写run()，调用start()启动，this表示当前线程

    * start()方法是一个native 方法，它会启动一个新线程，并执行 run()方法

      ```java
      public class MyThread extends Thread{
          @Override
          public void run() {
              // TODO Auto-generated method stub
              
          }
      }
      //线程使用
      MyThread mt = new MyThread();  //创建线程
      mt.start(); 
      ```

  * 实现Runnable接⼝，重写run()，调用start()启动，Thread.currentThread()获取当前线程

    * 实际上也是实现重写run方法，==实现runnable接口之后，需要将runnable实例当做参数传给thread类构造函数，完成thread的创建==

    * 属于组合，耦合性低==。==无返回值的任务==，使用run方法

    * ==一个Runnable实例可以被多个线程实例共享，当做参数，构造多个Thread实例==

      ```java
      public class MyThread implements Runnable{
          @Override
          public void run() {
              // TODO Auto-generated method stub
          }
      }
      //线程使用
      MyThread mt = new MyThread();
      Thread thread = new Thread(mt);//创建线程
      thread.start();                   //启动线程
      ```

  * 实现 Callable 接口通过 FutureTask 包装器来创建Thread 线程

    * ==使用call方法且有返回值、能抛出异常，而Runnable定义的方法是run且没有返回值、不能抛出异常==

    * ==使用FutureTask类来包装Callable对象，FutureTask对象封装了该Callable对象的call()方法的返回值==

      - 执行的线程在执行完成以后，提供一个返回值给到当前的主线程，主线程需要依赖这个值进行后续的逻辑处理

    * Future

      - Future表示异步计算的结果，它提供了检查计算是否完成的方法，以等待计算的完成，并检索计算的结果

      - ==Future的cancel方法可以取消任务的执行，它有一布尔参数，参数为 true 表示立即中断任务的执行，参数为 false 表示允许正在运行的任务运行完成==

      - ==Future的 get 方法等待（阻塞）计算完成，获取计算结果==

        ```java
        //创建Callable接口的实现类，并实现call()方法，再创建Callable实现类的实例，使用FutureTask类来包装Callable对象，FutureTask对象作为Thread对象的target创建并启动新线程，FutureTask对象的get()方法来获得子线程执行结束后的返回值
        public class CallableDemo implements Callable<String> {
        	public static void main(String[] args) throws ExecutionException,InterruptedException {
        	ExecutorService executorService=Executors.newFixedThreadPool(1);
        	CallableDemo callableDemo=new CallableDemo();
                Future<String> future=executorService.submit(callableDemo);
        	System.out.println(future.get());
        	executorService.shutdown();
        }
            
            @Override
            public String call() throws Exception {
                int a=1;
                int b=2;
                System.out.println(a+b);
                return "执行结果:"+(a+b);
        	}
        }
        ```

* Runnable接口

* 线程属性

* Thread类的常用方法

## 线程的生命周期状态

* 线程的运行是由线程调度器决定的，只能通过start、wait、sleep、notify来控制线程的状态

* ==NEW==：初始状态，线程被构建，但是还没有调用 start 方法

* ==RUNNABLED==：运行状态，JAVA 线程把操作系统中的==就绪和运行==两种状态统一 称为“运行中” 

  - 当线程对象调用了start()方法之后，该线程就进⼊就绪状态。就绪状态的线程处于就绪队列中， 要等待JVM里线程调度器的调度
  - 如果就绪状态的==线程获取 CPU 资源==，就可以执行 run()，此时线程便处于运行状态。处于运行状态的线程最为复杂，它可以变为阻塞状态、就绪状态和死亡状态 

* ==BLOCKED==：阻塞状态，表示线程进入等待状态,也就是线程因为某种原因放弃了 CPU 使用权，阻塞也分为几种情况

  - ==等待阻塞==：运行的线程执行wait方法，jvm会把当前线程放入到等待队列
  - ==同步阻塞==：运行的线程在获取对象的同步锁时，若该同步锁被其他线程锁占用了，那么 jvm 会把当前的线程放入到锁池中
  - ==其他阻塞==：运行的线程执行Thread.sleep或者t.join方法，或者发出了I/O请求时，JVM 会把当前线程设置为阻塞状态，==当 sleep 结束、join 线程终止、io 处理完毕则线程恢复==

* ==TIME_WAITING==：超时等待状态，超时以后自动返回

* ==TERMINATED==：终止状态，表示当前线程执行完毕

  ![image-20181212154854384](/Users/dingyuanjie/Desktop/MyKnowledge/2.code/java/2.咕泡学院/04.多线程高并发/image-20181212154854384-4600934.png)

## ==其他==

* JAVA编译器（javac.exe）的作用是将java源程序编译成中间代码字节码文件

* JIT编译器（即时编译器），动态编译的一种形式，是一种提高程序运行效率的方法

* 无状态对象：不包含任何实例变量以及可更新的静态变量，具有固有的线程安全性

* 串行、并行与并发
  * 并发是指系统在同一时段内，交替执行多个计算程序（时间片-占用处理器资源时间）
  * 并行是指系统在同一时段内，同时执行多个计算程序

* 竞态

  * 多线程编程中，对于同样的输入，程序的输出有时候是正确，而有时候确实是错误的，这种一个计算结果的正确性与时间有关的现象被称为竞态
  * 当两个线程竞争同一资源时，如果对资源的访问顺序敏感，就称存在竞态条件。导致竞态条件发生的代码区称作临界区

* 线程安全性

* 上下文切换

  * 单处理器也能够以多线程的方式实现并发，即一个处理器可以在同一时间段内运行多个线程，每个线程分配一个时间片，时间片决定了一个线程可以连续占用处理器运行的时间长度
  * 一个线程被剥夺处理器的使用权，另一个线程被选中开始或继续运行的过程，叫上下文切换
  * 从Java应用角度来看，一个线程的生命周期状态在RUNNABLE状态与非RUNNABLE状态（包括BLOCKED、WAITING和TIMED_WAITING中的任意一个子状态）之间切换的过程就是一个上下文切换的过程
  * 一个线程被唤醒仅代表该线程获得了一个继续运行的机会，而并不代表其立刻可以占用处理器运行
  * 自发性上下文切换（线程由于其自身因素导致的切出）
    - Thread.sleep(long millis)
    - Object.wait()/wait(long timeout)/wait(long timeout, int nanos)
    - Thread.yield()
      - 是线程方法，当一个线程抢到执行权后，执行到yield()方法后，就会放弃执行权，其他线程就可以拿到执行权了
    - Thread.join()/Thread.join(long timeout)
    - LockSupport.park()
    - 线程发起了I/O操作（如读取文件）或者等待其他线程持有的锁
  * 非自发性上下文切换（线程由于线程调度器的原因被迫切出）
    - 时间片用完
    - 一个优先级更高的线程需要被运行
    - Java虚拟机的垃圾回收过程中可能需要暂停所有应用线程（StopTheWorld）才能完成其工作

* 无处不在的线程

* 线程的层次关系

* 线程的监视

* ThreadLocal关键字

  * 建议使用 static修饰

  * 当使用 ThreadLocal 维护变量时，其==为每个使用该变量的线程提供独立的变量副本==，所以每一个线程都可以独立的改变自己的副本，而不会影响其他线程对应的副本

  * 内部实现机制，是每个线程都会维护一个ThreadLocalMap

  * 最常见的ThreadLocal使⽤场景为用来解决数据库连接、Session管理等

    ```java
    //⽤来获取ThreadLocal在当前线程中保存的变量副本 
    public T get() { } //用来设置当前线程中变量的副本
    public void set(T value) { } //⽤来移除当前线程中变量的副本
    public void remove() { } //一般是用来在使用时进行重写的，它是⼀个延迟加载⽅法 
    protected T initialValue() { }
    //实际的通过ThreadLocal创建的副本是存储在每个线程⾃己的threadLocals中的; 
    //为何threadLocals的类型ThreadLocalMap的键值为ThreadLocal对象，因为每个线程中可有多个threadLocal变量，就像上面代码中的longLocal和stringLocal; 
    //在进行get之前，必须先set，否则会报空指针异常; 
    //在main线程中，没有先set，直接get的话，运行时会报空指针异常
    ```

* Unsafe可认为是Java中留下的后门，提供了一些低层次操作，如直接内存访问、线程调度等

* 线程的优先级

  * 每⼀个 Java 线程都有一个优先级，这样有助于操作系统确定线程的调度顺序取值范围是 1 (Thread.MIN_PRIORITY ) - 10 (Thread.MAX_PRIORITY )
  * `setPriority(int newPriority)更改线程的优先级`

* 线程执行顺序

  * join()，通过join⽅法去保证多线程的顺序性的特性，让一个线程等待另⼀个线程完成的方法，通常由使用线程的程序调⽤

    * 当在某个程序执行流中调用其他线程的join()方法时，调用线程将被阻塞，直到被join()方法加⼊的join线程执行完为止

    * join(long)允许指定一个超时时间，如果目标线程没有在指定的时间内终止，那么当前线程也会继续执行

  * 多线程操作的api

    * 创建只有一个线程的线程池，不限数量的对点，把线程放进队列里FIFO(先进先出)

      ```java
      ExecutorService excutorService = Executors.newSingleThreadExecutor();
      excutorService.submit(thread1);
      excutorService.submit(thread2);
      excutorService.submit(thread3);
      ```

* static关键字

  * 在多线程环境下，它能够保证一个线程即使在未使用其他同步机制的情况下也总是可以读取到一个类的静态变量的初始值（而不是默认值），但是，这种可见性保障仅限于线程初次读取该变量。如果这个静态变量在相应类初始化完毕之后被其他线程更新过，那么一个线程要读取该变量的相对新值仍然需要借助锁、volatile关键字等同步机制
  * 对于引用型静态变量，static关键字还能够保障一个线程读取到该变量的初始值时，这个值所指向（引用）的对象已经初始化完毕
  * static关键字仅仅保障读线程能够读取到相应字段的初始值，而不是相对新值

## 简单运用实例

## 原子性

* 线程安全问题表现为3个方面：原子性、可见性和有序性
* ==可见性、原子性、有序性==的核心本质就是==缓存一致性问题==、==处理器优化问题导致的指令重排序问题==
* 原子性：（共享变量、多线程环境下）
  - ==指的是一个线程更新一个共享变量，对于别的线程来说，要么完成，要么不完成，即其他线程不会看到该操作执行了部分的中间效果==
  - 实现：
    - 使用锁：锁具有排他性，即它能够保障一个共享变量在任意一个时刻只能够被一个线程访问
    - 利用处理器提供的专门CAS（Compare-and-Swap）指令，与锁实现原子性的方式实质上是相同的，差别在于CAS是直接在硬件（处理器和内存）这一层次实现的
  - Java原子操作：
    - 基础类型（除long/double，因为其是64位，一个线程可能操作高32位，另一个线程操作低32位，导致结果不正确）的变量和引用类型变量的写操作都是原子的。Java语言规范规定对于volatile关键字修饰的long/double型变量的写操作具有原子性
* volatile不能保证原子性：
  * 对一个原子递增的操作，会分为三个步骤：
    1. 读取volatile变量的值到local;
    2. 增加变量的值;
    3. 把local的值写回让其他线程可见

## 可见性

* ==描述一个线程对共享变量的更新，后续其他线程能否立刻得知这个更新结果==
* 实现：
  - volatile关键字，所起作用：
    - 提示JIT编译器被修饰的变量可能被多个线程共享，以阻止JIT编译器做出可能导致程序运行不正常的优化
    - 读取一个volatile关键字修饰的变量会使相应的处理器执行刷新处理器缓存的动作，写一个volatile关键字修饰的变量会使相应的处理器执行冲刷处理器缓存的动作，从而保障可见性
* 可见性的保障仅仅意味着一个线程能够读取到共享变量的相对新值，而不能保障该线程能够读取到相应变量的最新值
* Java语言规范保证，
  - 父线程在启动子线程之前对共享变量的更新对于子线程来说是可见的
  - 一个线程终止后该线程对共享变量的更新对于调用该线程的join方法的线程而言是可见的
* 定位内存可⻅性问题
  - 什么对象是内存共享的，什么不是
    - 实例、静态、数组存在堆内存中，==堆内存==在线程之间共享
    - 局部变量、方法、定义参数、异常处理都不会在线程之间共享，因此不会有可见性问题
  - Java线程之间通信是由Java的内存模型处理(JMM)的，JMM决定一个线程对共享变量的写入何时对另外一个线程可⻅，定义了线程和主内存之间的关系
  - ==JMM模型下的线程间通信：==共享变量存储在主内存中。每个线程有私有的本地内存(用来存储共享变量副本——从主内存中拿共享变量，缓存到副本中)
    - 线程A和线程B之间通信必须经过主内存：线程A会把修改的值刷新到主内存中，然后线程B同步主内存中变量的值
    - ==JMM通过控制主内存的每个线程的本地内存之间的交互（通信）来实现多个线程之间的内存可见性问题==
* 解决内存可见性问题：
  - ==线程A具体在什么时候刷新共享内存数据是不确定的，⽽线程B什么时候去同步主内存的数据也是不确定的——导致线程安全、可见性问题——通过volatile、synchronized 可以解决==
  - ==Java中的volatile关键字==：volatile关键字可以保证直接从主存中读取一个变量，如果这个变量被修改后，总是会被写回到主存中去。Java内存模型是通过在变量修改后将新值同步回主内存，在变量读取前从主内存刷新变量值，这种依赖主内存作为传递媒介的方式来实现可见性的。volatile关键字本身就包含了禁止指令重排序的语义。
  - ==Java中的synchronized关键字==：实现锁机制，可重入锁、互斥性、可⻅性。一个变量在同一个时刻只允许一条线程对其进行操作。线程A释放锁后，会把数据刷新到主内存，而线程B在获取锁后会从主内存中同步数据到本地内存。基于JVM调用monitorenter(监视器)、...、monitorexit指令
  - ==Java中的final关键字==：final关键字的可见性是指，被final修饰的字段在构造器中一旦被初始化完成，并且构造器没有把“this”的引用传递出去，那么在其他线程就能看见final字段的值（无须同步）
  - Volatile具有可见性，不具有原子性，不能做到复合操作的原子性，开销比synchronized更小，修饰一个成员变量，volatile int i= 1;
    - 对于声明了volatile的变量进行写操作时，JVM会向处理器发送一条lock前缀的指令， 会将这个变量所在的缓存行的数据写回到系统内存
* ==在多处理器的情况下，保证各个处理器缓存一致性的特点，就会实现缓存一致性协议==(每个处理器会通过嗅探到总线上所传播的数据来检测自⼰缓存的值是不是过期了，当处理器发现⾃己的缓存对应的内存地址被修改过后，就会把当前处理器的缓存设置为失效状态，这时，当处理器对数据进行修改的时候，会从新把值读到处理器缓存中

## 有序性

* 指在什么情况下一个处理器上运行的一个线程所执行的内存访问操作在另外一个处理器上运行的其他线程看来是乱序的（乱序，是指内存访问操作的顺序看起来像是发生了变化）
  * 除了增加高速缓存以外，为了更充分利用处理器内部的运算单元，==处理器==可能会对输入的代码进行乱序执行优化，处理器会在计算之后将乱序执行的结果重组，保证该结果与顺序执行的结果一致，但并不保证程序中各个语句计算的先后顺序与输入代码中的顺序一致，这个是==处理器的优化执行==
  * 还有一个就是编程语言的==编译器==也会有类似的优化，比如做==指令重排==目的是为了最大化的提高CPU利用率以及性能
    * 指令重排序必须要遵循的原则是，==不影响代码执行的最终结果==，编译器和处理器不会改变存在**数据依赖关系**的两个操作的执行顺序，(这里所说的数据依赖性仅仅是针对单个处理器中执行的指令和单个线程中执行的操作.)
* 重排序：多核处理器的环境下
  - 编译器可能改变两个操作的先后顺序
  - 处理器可能不是完全依照程序的目标代码所指定的顺序执行命令
  - 一个处理器上执行的多个操作，从其他处理器的角度来看其顺序可能与目标代码所指定的顺序不一致
* 重排序是对内存访问有关操作（读和写）所做的一种优化，它可以在不影响单线程程序正确性的情况下提升程序性能，但是可能对多线程程序的正确性产生影响，即可能导致线程安全问题
* 指令重排序：
  - 程序顺序与源代码顺序不一致（来源编译器），执行顺序与程序顺序不一致（JIT编译器、处理器）
  - 处理器对指令进行重排序也被称为处理器的乱序执行
* 实现有序性
  - volatile关键字
    - volatile关键字会禁止指令重排。
  - synchronized关键字
    - 

# 线程同步机制

* 从应用程序的角度来看，线程安全问题的产生是由于多线程应用

  ![image-20190331161303420](/Users/dingyuanjie/Library/Application Support/typora-user-images/image-20190331161303420.png)

## 锁

* 线程安全问题的产生前提是多个线程并发访问共享变量、共享资源
* 锁：将多个线程对共享数据的并发访问转换为串行访问，即一个共享数据一次只能被一个线程访问，该线程访问结束后其他线程才能对起进行访问。具有排他性
* 临界区：获得锁和释放锁时所执行的代码
* 作用：
  - 保护共享数据以实现线程安全，作用包括：（条件：同一个锁；不仅更新需要加锁，读取也需加相应锁）
    - 保障原子性：通过互斥实现
    - 保障可见性：通过写线程冲刷处理器缓存和读线程刷新处理器缓存这两个动作实现。Java平台中，锁的获得隐含着刷新处理器缓存这个动作（使得读线程可以将写线程对共享变量所做的更新同步到该线程执行处理器的高速缓存中），而锁的释放隐含着冲刷处理器缓存这个动作（使得写线程对共享变量所做的更新能够被"推送"到该线程执行处理器的高速缓存中，从而对读线程可同步）
    - 保障有序性：写线程在临界区中所执行的一系列操作在读线程所执行的临界区看起来像是完全按照源代码顺序执行的
* 特性：
  - 可重入性

    - 一个线程持有一个锁时还能够继续成功申请该锁

    - 在一个线程中可以多次获取同一把锁，比如：一个线程在执行一个带锁的方法，该方法中又调用了另一个需要相同锁的方法，则该线程可以直接执行调用的方法，而无需重新获得锁；

    - synchronized和ReentrantLock都是可重入锁

      ```java
      public class Test implements Runnable{
      	public synchronized void get(){
      		System.out.println(Thread.currentThread().getId());
      		set();
      	}
          
      	public synchronized void set(){
      		System.out.println(Thread.currentThread().getId());
      	}
      
      	@Override
      	public void run() {
      		get();
      	}
      	public static void main(String[] args) {
      		Test ss=new Test();
      		new Thread(ss).start();
      		new Thread(ss).start();
      		new Thread(ss).start();
      	}
      }
      //结果 同一个线程id被连续输出两次。
      ```

      ```java
      public class Test implements Runnable {
      	ReentrantLock lock = new ReentrantLock();
      
      	public void get() {
      		lock.lock();
      		System.out.println(Thread.currentThread().getId());
      		set();
      		lock.unlock();
      	}
      
      	public void set() {
      		lock.lock();
      		System.out.println(Thread.currentThread().getId());
      		lock.unlock();
      	}
      
      	@Override
      	public void run() {
      		get();
      	}
      
      	public static void main(String[] args) {
      		Test ss = new Test();
      		new Thread(ss).start();
      		new Thread(ss).start();
      		new Thread(ss).start();
      	}
      }
      //同一个线程id被连续输出两次。
      ```

  - 公平和非公平

    - 公平锁就是==等待时间最长的线程最优先获取锁==。非公平锁无法保证锁的获取是按照请求锁的顺序进行
    - 内部锁属于非公平锁
    - 显示锁有公平锁和非公平锁

  - 锁的粒度

    - 一个锁实例所保护的共享数据的数量大小
* 开销：
  - 申请锁和释放锁产生的开销、上下文切换的开销，这些开销主要是处理器时间
  - 线程活性故障
    - 锁泄漏：一个线程获得锁后由于程序错误、缺陷使该锁一直无法被释放而导致其他线程一直无法获得该锁
* 分类：
  * 内部锁：synchronized关键字
  * 显示锁：Lock接口实现类的实例

## 内部锁synchronized

* 内部锁：synchronized关键字
  - 线程对内部锁的申请与释放的动作由Java虚拟机负责代为实施——叫内部锁原因

  - Java平台中的任何一个对象都有唯一一个与之关联的锁（即监视器或者内部锁）

  - 属于排他锁，主要作用保障

    - 原子性：确保线程互斥的访问同步代码；
    - 可见性：保证共享变量的修改能够及时可见，一个变量在同一个时刻只允许一条线程对其进行操作。线程A释放锁后，会把数据刷新到主内存，而线程B在获取锁后会从主内存中同步数据到本地内存
    - 有序性：有效解决重排序问题，即 **“一个unlock操作先行发生(happen-before)于后面对同一个锁的lock操作”**；

  - 加锁方式
    - 修饰代码块，指定加锁对象，对给定对象加锁，进入同步代码库前要获得给定对象的锁

    - 修饰静态方法，作用于当前类对象加锁，进入同步代码前要获得当前类对象的锁

      - static synchronized方法，static方法可以直接类名加方法名调用，方法中无法使用this，所以它锁的不是this，而是类的Class对象，所以，static synchronized方法也相当于==全局锁==，相当于锁住了代码段

    - 修饰实例方法，作用于当前实例加锁，进入同步代码前要获得当前实例的锁

      ```java
       run(){ 
           //线程开始执行，同步代码块之前，必须先获得对同步监视器器的锁定 
           synchronized(obj){
      		//obj 同步监视器:阻⽌两个线程对同⼀个共享资源进行并发访问
      		//可能被并发访问的共享资源 
           }
      }
      ```

  - ==synchronized锁住的是括号里的对象（同步监视器），而不是代码==

    - 即使两个不同的代码段，都要锁同一个对象，那么这两个代码段也不能在多线程环境下同时运行

  - 释放对同步监视器的锁定

    - 执行结束、break、return、未处理的Error或Exception、wait()

  - 锁句柄：是一个对象的引用，通常采用private final修饰，因为锁句柄变量的值一旦改变，会导致执行同一个同步快的多个线程实际上使用不同的锁，从而导致竞态

  - ==通常和wait，notify，notifyAll⼀块使⽤==

    - wait：是object的方法，必须与syn关键字一起使用，线程进入阻塞状态，当notify被调用后，会解除阻塞。但是只有占用互斥锁之后才会进入运行状态，也就是说wait的同时，也释放了锁
      - 调用wait方法，首先会获取监视器锁，获得成功以后，会让当前线程进入等待状态进入等待队列并且释放锁;
      - 然后当其他线程调用notify或者notifyall以后，会选择从等待队列中唤醒任意一个线程，而执行完notify方法以后，并不会立马唤醒线程，原因是当前的线程仍然持有这把锁，处于等待状态的线程无法获得锁。必须要等到当前的线程执行完按monitorexit指令以后，也就是锁被释放以后，处于等待队列中的线程就可以开始竞争锁了
    - sleep：则是释放CPU，是thread的静态方法，让线程进入阻塞状态，但是不释放锁（如果有），只要睡眠时间一过，就会进入运行状态
    - notify：唤醒等待队列中的一个线程，使其获得锁进行访问
    - notifyAll：唤醒等待队列中等待该对象锁的全部线程，让其竞争去获得锁

  - wait和notify为什么需要在synchronized里面

    - 一个线程只有在持有一个对象的内部锁的情况下才能够执行该对象的nofity方法，Object.wait()在暂停其执行线程的同时必须释放相应的内部锁，否则通知线程无法获得相应的内部锁，也就无法执行相应对象的notify方法来通知等待线程
    - wait方法的语义有两个，一个是释放当前的对象锁、另一个是使得当前线程进入阻塞队列， 而这些操作都和监视器是相关的，所以wait必须要获得一个监视器锁 
    - 而对于notify来说也是一样，它是唤醒一个线程，既然要去唤醒，首先得知道它在哪里?所以就必须要找到这个对象获取到这个对象的锁，然后到这个对象的等待队列中去唤醒一个线程

  - ==底层的实现==：一个线程进入时，执行monitorEnter将计数器加一，释放时执行monitorExit，计数器减一。当一个线程请求锁时，判断到计数器为0，则可进入锁；反之线程进入等待

  - Synchronized关键字是对多种锁进行了封装（优化）

  ```java
  // 实例方法，只能防止多个线程同时执行同一个对象的同步代码
  class Sync {
  	public synchronized void test() {         //锁的是对象
  		System.out.println("test开始..");
  		try {
  			Thread.sleep(1000);
  		} catch (InterruptedException e) {
  			e.printStackTrace();
  		}
  		System.out.println("test结束..");
  	}
  }
   
  class MyThread extends Thread {
  	public void run() {
  		Sync sync = new Sync();
  		sync.test();
  	}
  }
   
  public class Main {
  	public static void main(String[] args) {
  		for (int i = 0; i < 3; i++) {
  			Thread thread = new MyThread();   //是三个不同的对象，所以没能同步
              //由于不是同一个对象，所以可以多线程同时运行synchronized方法或代码段
  			thread.start();
  		}
  	}
  }
  //结果：test开始.. test开始.. test开始.. test结束.. test结束.. test结束..
  ```

  ```java
  // 代码块，只能防止多个线程同时执行同一个对象的同步代码
  public void test() {
      // 由于不是同一个对象，所以可以多线程同时运行synchronized方法或代码段
  	synchronized(this){                      //锁的是对象
  		System.out.println("test开始..");
  		try {
  			Thread.sleep(1000);
  		} catch (InterruptedException e) {
  			e.printStackTrace();
  		}
  		System.out.println("test结束..");
  	}
  }
  //结果：test开始.. test开始.. test开始.. test结束.. test结束.. test结束.. 
  ```

  ```java
  class MyThread extends Thread {
  	private Sync sync;
  	public MyThread(Sync sync) {
  		this.sync = sync;
  	}
   
  	public void run() {
  		sync.test();
  	}
  }
   
  public class Main {
  	public static void main(String[] args) {
  		Sync sync = new Sync();
  		for (int i = 0; i < 3; i++) {
  			Thread thread = new MyThread(sync);   // 三个线程共用同一个对象
  			thread.start();
  		}
  	}
  }
  //结果：test开始.. test结束.. test开始.. test结束.. test开始.. test结束.. 
  ```

  ```java
  class Sync {
  	public void test() {
          //synchronized(Sync.class)实现了全局锁的效果
  		synchronized (Sync.class) {             // 常用做法，锁这个类对应的Class对象
  			System.out.println("test开始..");
  			try {
  				Thread.sleep(1000);
  			} catch (InterruptedException e) {
  				e.printStackTrace();
  			}
  			System.out.println("test结束..");
  		}
  	}
  }
   
  class MyThread extends Thread {
  	public void run() {
  		Sync sync = new Sync();
  		sync.test();
  	}
  }
   
  public class Main {
  	public static void main(String[] args) {
  		for (int i = 0; i < 3; i++) {
  			Thread thread = new MyThread();
  			thread.start();
  		}
  	}
  }
  ```

## 显示锁Lock

* 显示锁：Lock接口实现类的实例
  - JDK1.5开始，提供一些内部锁不具备的特性，但并不是内部锁的替代品

  - lock()申请锁，unlock()释放锁（避免锁泄漏，一般放在finally块中），之间为临界区

  - 使用

    - Lock接口的默认实现类ReentrantLock，通过构造函数布尔参数指定是公平锁（true）还是非公平锁（false，默认），公平锁的开销比非公平锁开销大

  - 改进：读写锁

    - 同一时刻可以允许多个线程访问共享变量，但是在写线程访问时，所有的读线程和其他写线程都会被阻塞，readLock()获取读锁和writeLock()获取写锁
    - 读锁是共享的，此时其他线程无法更新这些变量；写锁是排他的，此时其他线程无法访问该变量
    - 使用场景：（同时满足）
      - 只读操作比写（更新）操作要频繁的多
      - 读线程持有锁的时间比较长

    ```java
    public class LockDemo {
        static Map<String,Object> cacheMap=new HashMap<>();
        static ReentrantReadWriteLock rwl=new ReentrantReadWriteLock();
        static Lock read=rwl.readLock();
        static Lock write=rwl.writeLock();
        
    	public static final Object get(String key){ 
            System.out.println("开始读取数据"); 
            //在并发访问的时候，读锁不会被阻塞，因为读操作不会影响执行结果
            read.lock(); //读锁
    		try {
            	return cacheMap.get(key);
            }finally {
                read.unlock();
            }
        }
        public static final Object put(String key,Object value){
            //当已经有线程持有写锁的情况下，当前线程会被阻塞，只有当写锁释放以后，其他读写操作才能继续执行
            write.lock(); 
            System.out.println("开始写数据"); 
            try{
        		return cacheMap.put(key,value);
    		}finally {
        		write.unlock();
    		}
    	}
    }
    ```

  - 在获取同步状态时，同步器维护一个同步队列，获取状态失败的线程都会被加入到队列中并在队列中进行自旋;移出队列(或停止自旋)的条件是前驱节点为头节点且成功获取了同步状态。在释放同步状态时，同步器调用tryRelease(int arg)方法释放同步状态，然后唤醒头节点的后继节点

  - AQS

    - Lock之所以能实现线程安全的锁，主要的核心是AQS(AbstractQueuedSynchronizer)
    - AbstractQueuedSynchronizer==提供了一个FIFO队列，可以看做是一个用来实现锁以及其他需要同步功能的框架==
    - AQS的使用依靠继承来完成，子类通过继承自AQS并实现所需的方法来管理同步状态。例如常见的==ReentrantLock，CountDownLatch==等AQS的两种功能
    - AQS的功能可以分为两种：==独占和共享==
      - 独占锁模式下，每次只能有一个线程持有锁，比如ReentrantLock就是以独占方式实现的互斥锁
        - 独占锁是一种悲观保守的加锁策略，它限制了读/读冲突，如果某个只读线程获取锁，则其他读线程都只能等待，这种情况下就限制了不必要的并发性，因为读操作并不会影响数据的一致性。
      - 共享锁模式下，允许多个线程同时获取锁，并发访问共享资源，比如ReentrantReadWriteLock。
        - 共享锁则是一种乐观锁，它放宽了加锁策略，允许多个执行读操作的线程同时访问共享资源
    - ==AQS的内部实现==
      - 同步器依赖内部的同步队列(一个FIFO双向队列)来完成同步状态的管理。
      - 当前线程获取同步状态失败时，同步器会将当前线程以及等待状态等信息构造成为一个节点(Node)并将其加入同步队列，同时会阻塞当前线程，当同步状态释放时，会把首节点中的线程唤醒，使其再次尝试获取同步状态
      - AQS类底层的数据结构是使用双向链表，是队列的一种实现
      - ==同步队列遵循FIFO，首节点是获取同步状态成功的节点，首节点的线程在释放同步状态时，将会唤醒后继节点，而后继节点将会在获取同步状态成功时将自己设置为首节点==

## 比较

* java提供了两种内置的锁的实现，一种是由JVM实现的synchronized和JDK提供的Lock

* 内部锁是一个关键字、基于代码块的锁，在java中任意一个对象都可以成为锁，申请与释放只能在同一个方法中，简单易用且不会导致锁泄漏；显示锁是基于对象的锁，支持一个方法中申请锁，另一个方法中释放锁，容易被错用而导致锁泄漏，释放锁要放在finally块中
* ==从使用上，lock具备更大的灵活性，可以控制锁的释放和获取以及中断（ lock.lockInterruptibly()）等待锁；而synchronized的锁的释放是被动的==，当出现异常或者同步代码块执行完以后，才会释放锁。读和写都会用到synchronized这个锁。
* 如果一个内部锁的持有线程一直不释放这个锁，那么同步在该锁之上的所有线程就会一直被暂停；显示锁有个tryLock方法可以指定一个时间去尝试申请相应锁，没有获得锁就直接就返回false
* 内部锁仅仅支持非公平锁，显示锁支持公平锁和非公平锁
* 显示锁提供了一些方法可以用来对锁的相关信息进行监控，而内部锁不支持
  - RentrantLock中isLocked()可用于检测相应锁是否被某个线程持有，getQueueLength()检查相应锁的等待线程的数量
* 性能差异：Java1.6/1.7对内部锁做了一些优化，性能差异不大

## 锁的选用

* 内部锁简单易用，显示锁功能强大
* 一般来说，新开发代码中可以选用显示锁，但要注意：
  - 显示锁的不正确使用会导致锁泄漏这样严重的问题
  - 线程转储可能无法包含显示锁的相关信息，从而导致问题定位的困难
* 也可以使用相对保守的策略，默认情况下选用内部锁，仅在需要显示锁所提供的特性的时候才选用显示锁，比如，在多线程持有一个锁的时间相对长或者线程申请锁的平均时间间隔相对长的情况下可考虑使用公平锁（显示锁）

## 锁的适用场景

* check-then-act操作：一个线程读取共享数据并在此基础上决定其下一个操作是什么
* read-modify-write操作：一个线程读取共享数据并在此基础上更新该数据
* 多个线程对多个共享数据进行更新：如果这些共享数据之间存在关联关系，那么为了保障操作的原子性可以考虑使用锁

## 内存屏障

* 线程同步机制的底层助手：内存屏障
* 内存屏障是被插入到对内存读、写操作指令之间进行使用的，其作用是禁止编译器、处理器重排序从而保障有序性，它在指令序列中就像一堵墙一样使其两侧的指令无法穿越它（一旦穿越了就是重排序），但是为了实现禁止重排序的功能，这些指令也往往具有一个副作用——刷新处理器缓存、冲刷处理器缓存，从而保证可见性
  - 分类：
    - 按可见性保障划分：
      - 加载屏障（LoadBarrier）：刷新处理器缓存
      - 存储屏障（StoreBarrier）：冲刷处理器缓存
    - 按有序性保障划分：
      - 获取屏障（AcquireBarrier）：使用方式是在一个读操作之后插入该内存屏障，其作用是禁止该读操作与其后的任何读写操作之间进行重排序，这相当于在进行后续操作之前先要获得相应共享数据的所有权
      - 释放屏障（ReleaseBarrier）：使用方式是在一个写操作之前插入该内存屏障，其作用是禁止该写操作与前面的任何读写操作之间进行重排序，相当于在对应共享数据操作结束后释放所有权
  - 锁对有序性的保障是通过写线程和读线程配对使用释放屏障与加载屏障实现的
  - 为了保障线程安全，需要使用Java线程同步机制，而内存屏障则是Java虚拟机在实现Java线程同步机制时所使用的具体"工具"，开发人员一般无须也不能直接使用内存屏障
  - 优化屏障解决编译器的优化乱序
    - 在编译器层面，通过volatile关键字，取消编译器层面的缓存和重排序
    - 保证编译程序时在优化屏障之前的指令不会在优化屏障之后执行。这就保证了编译时期的优化不会影响到实际代码逻辑顺序
  - 内存屏障解决CPU的执行乱序
    - CPU的乱序执行，本质还是，由于在多CPU的机器上，每个CPU都存在cache，存在多个cache时，必须通过一个cache一致性协议来避免数据不一致的问题，而这个通讯的过程就可能导致乱序访问的问题，也就是运行时的内存乱序访问
    - CPU架构都提供了内存屏障功能：
      - 写屏障(store barrier)
        - 强制所有在storestore内存屏障之前的所有执行，都要在该内存屏障之前执行，并发送缓存失效的信号。所有在storestore barrier指令之后的store指令，都必须在storestore barrier屏障之前的指令执行完后再被执行
        - 也就是禁止了写屏障前后的指令进行重排序，是对所有store barrier之前发生的内存更新都是可见的（这里的可见指的是修改值可见以及操作结果可见）
      - 读屏障(load barrier)
        - 强制所有在load barrier读屏障之后的load指令，都在loadbarrier屏障之后执行。
        - 也就是禁止对load barrier读屏障前后的load指令进行重排序， 配合store barrier，使得所有store barrier之前发生的内存更新，对load barrier之后的load操作是可见的
      - 全屏障(Full Barrier)
        - 同时具备前面两种屏障的效果
        - 强制了所有在storeload barrier之前的store/load指令，都在该屏障之前被执行，所有在该屏障之后的的store/load指令，都在该屏障之后被执行
        - 禁止对storeload屏障前后的指令进行重排序
  - 主要的作用是
    - 防止指令之间的重排序
    - 保证数据的可见性
  - ==内存屏障只是解决顺序一致性问题，不解决缓存一致性问题，缓存一致性是由cpu的缓存锁以及MESI协议来完成的。而缓存一致性协议只关心缓存一致性，不关心顺序一致性。所以这是两个问题==

## 锁与重排序

* 无论是编译器还是处理器，均需遵守以下重排序规则
  - 规则1：临界区内的操作不允许被重排序到临界区之外。是锁保障原子性和可见性的基础
  - 规则2：临界区内的操作之间允许被重排序。锁的排他性保证了临界区内的操作是一个原子操作
  - 规则3：临界区外的操作之间可以被重排序
  - 规则4：锁申请与锁释放操作不能被重排序
  - 规则5：两个锁申请操作不能被重排序
  - 规则6：两个锁释放操作不能被重排序

## volatile

* 轻量级同步机制：volatile关键字

* 用于修饰共享可变变量，即没有使用final关键字修饰的实例变量或静态变量

* volatile修饰的变量的值容易被其他线程更改而变化，对这种变量的读和写操作都必须从高速缓存或者主内存中读取，以读取变量的相对新值。（对volatile变量的读写操作都是内存访问）

* 被称为轻量级锁，其作用与锁的作用有相同的地方：保证可见性和有序性，所不同的是，在原子性方面它仅能保障写volatile变量操作的原子性，没有锁的排他性（不能保障其他操作的原子性）。其次，其不会引起上下文切换

* 作用：

  - 保障可见性
  - 保障有序性
  - 保障long/double型变量读写操作的原子性
  - 写线程对volatile变量的写操作会产生类似于释放锁的效果。读线程对volatile变量的读操作会产生类似于获得锁的效果。因此，volatile具有保障有序性和可见性的作用
  - 不具备排他性，所以并不能保障其他操作的原子性，只能够保障对被修饰变量的写操作的原子性。因此，volatile变量写操作之前的操作如果涉及共享可变变量，那么竞态仍然可能产生，这是因为共享变量被给赋值给volatile变量的时候其他线程可能已经更新了该共享变量的值

* 典型应用场景：

  - 使用volatile变量作为状态标志。一个线程能够通知另外一个线程某种事件的发生，而无需使用锁
  - 使用volatile保障可见性。多个线程共享一个可变状态变量，其中一个线程更新了该变量之后，其他线程在无需加锁的情况下也能够看到该更新
  - 使用volatile变量替代锁。利用其写操作具有原子性，可以把一组可变状态变量封装成一个对象，那么对这些状态变量的更新操作就可以通过创建一个新的对象并将该对象引用赋值给相应的引用型变量来实现。在这个过程中，volatile保障了原子性和可见性，从而避免了锁的使用
  - 使用volatile实现简易版读写锁。通过混合使用锁和volatile变量实现，其中锁用于保障共享变量写操作的原子性，volatile变量用于保障共享变量的可见性

  ```java
  //基于volatile的简易读写锁
  public class Counter{
    private volatile long count;
    public long value(){
      return count;
    }
    public void increment() {
      synchronized(this) {
        count++;
      }
    }
  }
  ```

## CAS与原子变量

* Compare And Swap
* synchronized是悲观锁，这种线程一旦得到锁，其他需要锁的线程就挂起的情况就是悲观锁
* ==CAS操作的就是乐观锁==，每次不加锁而是假设没有冲突而去完成某项操作，如果因为冲突失败就重试，直到成功为止
* ==Atomic操作的底层实现正是利用的CAS机制==
* CAS机制当中使用了3个基本操作数：内存地址V，旧的预期值A，要修改的新值B。==更新一个变量的时候，只有当变量的旧的预期值A和内存地址V当中的实际值相同时，才会将内存地址V对应的值修改为B。==
* 在 java 中可以通过锁和==循环 CAS 的方式来实现原子操作==
  - 使用锁机制实现原子操作
    - 锁机制保证了只有获得锁的线程能够操作锁定的内存区域。JVM 内部实现了很多种锁机制，有偏向锁，轻量级锁和互斥锁，有意思的是除了偏向锁，JVM 实现锁的方式都用到的循环 CAS，当一个线程想进入同步块的时候使用循环 CAS 的方式来获取锁，当它退出同步块的时候使用循环 CAS 释放锁
  - CAS基本思路就是==循环进行 CAS 操作直到成功为止==
* 三大问题
  - ABA 问题
    - 因为 CAS 需要在操作值的时候检查下值有没有发生变化，如果没有发生变化则更新，但是如果一个值原来是 A，变成了 B，又变成了 A，那么使用 CAS 进行检查时会发现它的值没有发生变化，但是实际上却变化了
    - 解决思路就是使用==版本号==。在变量前面追加上版本号，每次变量更新的时候把版本号加一，那么 A－B－A 就会变成 1A-2B－3A
    - 从 Java1.5 开始 JDK 的 atomic 包里提供了一个类 AtomicStampedReference 来解决 ABA 问题。这个类的 compareAndSet 方法作用是首先检查当前引用是否等于预期引用，并且当前标志是否等于预期标志，如果全部相等，则以原子方式将该引用和该标志的值设置为给定的更新值
  - 循环时间长开销大
    - 自旋 CAS 如果长时间不成功，会给 CPU 带来非常大的执行开销
    - 如果 JVM 能支持处理器提供的 pause 指令那么效率会有一定的提升，pause 指令有两个作用，第一它可以延迟流水线执行指令（de-pipeline）, 使 CPU 不会消耗过多的执行资源，延迟的时间取决于具体实现的版本，在一些处理器上延迟时间是零。第二它可以避免在退出循环的时候因内存顺序冲突（memory order violation）而引起 CPU 流水线被清空（CPU pipeline flush），从而提高 CPU 的执行效率。
  - 只能保证一个共享变量的原子操作
    - 当对一个共享变量执行操作时，我们可以使用循环 CAS 的方式来保证原子操作，但是对多个共享变量操作时，循环 CAS 就无法保证操作的原子性
    - 这个时候就可以用锁，或者有一个取巧的办法，就是把多个共享变量合并成一个共享变量来操作。比如有两个共享变量 i＝2,j=a，合并一下 ij=2a，然后用 CAS 来操作 ij。从 Java1.5 开始 JDK 提供了 AtomicReference 类来保证引用对象之间的原子性，你可以把多个变量放在一个对象里来进行 CAS 操作
* 原子操作工具：原子变量类
  * Atomic包下的类，该类==实现基本数据类型以原子的方式自增自减==
  * 原子操作类==提供了一种简单、高效以及线程安全的更新操作==。而由于变量的类型很多，所以Atomic一共提供了12个类分别对应四种类型的原子更新操作
    * 基本类型对应：AtomicBoolean、AtomicInteger、AtomicLong
    * 数组类型对应：AtomicIntegerArray、AtomicLongArray、AtomicReferenceArray
    * 引用类型对应：AtomicReference、AtomicReferenceFieldUpdater、AtomicMarkableReference
    * 字段类型对应：AtomicIntegerFieldUpdater、AtomicLongFieldUpdater、AtomicStampedReference

## 对象的发布与逸出

* 对象的发布：使对象能够被其作用域之外的线程访问
  - 将对象引用存储到public变量中
  - 在非private方法中返回一个对象
  - 创建内部类，使得当前对象（this）能够被这个内部类使用
  - 通过方法调用将对象传递给外部方法
* 实现对象的安全发布，通常可以依照以下顺序选择适用且开销小的线程同步机制
  - 使用static关键字修饰引用该对象的变量
  - 使用final关键字修饰引用该对象的变量
  - 使用volatile关键字修饰引用该对象的变量
  - 使用AtomicReference来引用该对象
  - 对访问该对象的代码进行加锁
* 为避免将this代表的当前对象逸出到其他线程，应该避免在构造器中启动工作者线程。通常可以定义一个init方法，在该方法中启动工作者线程。在此基础之上，定义一个工厂方法来创建（并返回）相应的实例，并在该方法中调用该实例的init方法

## 一些相关API

* 如Object.wait()/Object.notify()

# 线程间协作

* 并发编程中两个关键问题

  * 线程之间如何通信
    * 命令编程模型中线程通信有两种
      * ==共享内存-隐式通信==
        * 在共享内存的并发模型⾥面，线程之间共享的是一些公共状态，通过写或者读内存的公共状态进行一个隐式通信
        * synchronized
      * ==消息传递-显示通信==
        * ==显示调用wait()、notify()、notifyall()==

  * 线程之间如何同步
    * 同步：==指的程序中⽤于控制不同线程之间操作发生的相对顺序的机制==
    * 在共享内存的并发模型中(Java并发采用)，同步是显示做的(程序员显示调⽤用某个方法、某个代码块，达到线程互斥)==synchronized==
    * 在消息传递的并发模型中，由于消息的发送必须在消息接收之前，所以同步是隐式的
    * Java之间的线程通信是隐式的，通信过程对于程序员来说是透明的，编写多线程的Java程序的时候，如果并不清楚隐式、显示、多线程之间的通信机制，很可能遇到内存可见性问题 

## wait/notify

* 等待与通知：使用Java中的任何对象都能够实现等待与通知

  * 等待：一个线程因其执行目标动作所需的保护条件未满足而被暂停的过程
  * 通知：一个线程更新了系统的状态，使得其他线程所需的保护条件得以满足的时候唤醒那些被暂停的线程的过程

  ```java
  // Object.wait()实现等待
  //在调用wait方法前获得相应对象的内部锁
  synchronized(someObject) {
    // 1. 保护条件
    while(保护条件不成立) {
      //一个线程只有在持有一个对象的内部锁的情况下才能够调用该对象的wait方法
      //2.调用Object.wait()暂停当前线程，当前线程只释放该wait方法所属对象的内部锁
      someObject.wait();
    }
    //保护条件已满足
    //3. 执行目标动作
    doAction();
  }
  ```

  ```java
  // Object.notify()实现通知，执行线程持有的锁在临界区代码执行完后才被释放
  synchronized(someObject) {
    // 更新等待线程的保护条件涉及的共享变量
    updateSharedState();
    // 唤醒其他线程
    someObject.notify();    //本身不会将这个内部锁释放，尽量靠近临界区结束的地方
  }
  ```

  * Object.wait()在暂停其执行线程的同时必须释放相应的内部锁，否则通知线程无法获得相应的内部锁，也就无法执行相应对象的notify方法来通知等待线程
  * 等待线程和通知线程是同步在同一对象之上的两种线程

* wait/notify的开销及问题
  * 过早唤醒：notifyAll()唤醒对象上所有等待的线程，这种等待线程在其所需的保护条件并未成立的情况下被唤醒的现象，从而造成资源浪费
  * 信号丢失：本质是一种代码错误
    * 等待线程直接执行了Object.wait()而被暂停的时候，该线程由于没有其他线程进行通知而一直处于等待状态，错过了一个本来发送给它的信号
      * 避免：对保护条件的判断和Object.wait()调用放在一个循环语句之中
    * 应该调用Object.notifyAll()的地方却调用了Object.notify()，其最多唤醒一个等待线程
      * 避免：使用notifyAll()来通知
  * 上下文切换问题：
    * 锁的申请与释放可能导致上下文切换
    * 等待线程从被暂停到唤醒这个过程本身就会导致上下文切换
    * 被唤醒的等待线程在继续运行时需要再次申请相应对象的内部锁，可能与其他线程争用相应内部锁，这又可能导致上下文切换
    * 过早被唤醒的线程仍然需要继续等待，即再次经历被暂停和唤醒的过程
    * 减少或避免wait/notify导致过多的上下文切换
      * 使用Object.notify()替代Object.notifyAll()
      * 通知线程在执行完Object.notify()/notifyAll()之后尽快释放相应的内部锁
* 为了避免信号丢失问题，等待线程将等待线程对保护条件的判断、Object.wait()的调用必须放在相应对象所引导的临界区中的一个循环语句之中

* notify/notifyAll选用
  * Object.notify()可能导致信号丢失这样的正确性问题，而Object.notifyAll()虽然效率不高，但是正确性有保障，因此比较流行的保守性方法是优先使用Object.notifyAll()以保障正确性
  * Object.notify()唤醒的是其所属对象上的一个任意等待线程，本身在唤醒线程时是不考虑保护条件的
  * 只有在同时满足以下条件才使用Object.notify()
    * 一次通知仅需要唤醒至多一个线程
    * 相应对象的等待集中仅包含同质等待线程
      * 同质等待线程：这些线程使用同一个保护条件，并且这些线程在Object.wait()调用返回之后的处理逻辑一致。
      * 如使用同一个Runnable接口实例创建的不同线程（实例）或者从同一个Thread子类的new出来的多个实例

* synchronized关键字来保证同步
  - 当前线程等待，直到其他线程调用该同步监视器的notify()或notifyAll()来唤醒 wait()
  - notify()：唤醒在此同步监视器上等待的单个线程
  - notifyAll()：唤醒在此同步监视器上等待的所有线程 

## Java条件变量

* Condition接口可作为wait/notify的替代品来实现等待/通知，为解决过早唤醒问题提供支持，并解决了Object.wait(long)不能区分其返回是否是由等待超时而导致的问题
  * await、signal、signalAll相当于wait、notify、notifyAll
* Lock.newCondition()的返回值就是一个Condition实例——条件变量
  * Condition.await()/signal()要求其执行线程持有创建该Condition实例的显示锁
  * 每个Condition实例内部都维护了一个用于存储等待线程的队列（等待队列）

```java
class ConditionUsage {
  private final Lock lock = new ReentrantLock();
  private final Condition condition = lock.newCondition();
  public void aGuaredMethod() throws InterruptedExecption {
    lock.lock();
    try {
      while(保护条件不成立) {
        condition.await();
      }
      //执行目标动作
      doAction();
    } finally {
      lock.unlock();
    }
  }
  public void anNotificationMethod() throws InterruptedException {
    lock.lock();
    try {
      //更新共享变量
      changeState();
      condition.signal();
    } finally {
      lock.unlock();
    }
  }
}
```

* 解决过早唤醒问题：
  * 使用不同保护条件的等待线程调用不同的条件变量的await方法来实现其等待，并让通知线程在更新了共享变量之后，仅调用涉及了这些共享变量的保护条件所对应的条件变量的signal/signalAll方法来实现通知
* 解决Object.wait(long)无法区分其返回是由于等待超时还是被通知的
  * Condition.awaitUntil(Date deadline)，返回true表示被通知

## CountDownLatch

* 倒计时协调器

* 一个同步工具类，它允许一个或多个线程一直等待，直到其他线程的操作执行完毕再执行

* 内部会维护一个用于表示未完成的先决操作数量的计数器。当计数器值不为0时，CountDownLatch.await()的执行线程会被暂停，这些线程就被称为相应CountDownLatch上的等待线程。CountDownLatch.countDown()相当于一个通知方法，它会在计数器值达到0的时候唤醒相应实例上的所有等待线程

* 一个CountDownLatch实例只能够实现一次等待和唤醒

* 客户端代码在使用CountDownLatch实现等待/通知的时候调用await、countDown方法都无须加锁

* 两个方法，一个是countDown，一个是await， countdownlatch初始化的时候需要传入一个整数，在这个整数倒数到0之前，调用了await方法的程序都必须要等待，然后通过countDown来倒数（进行减1）

  ```java
  public static void main(String[] args) throws InterruptedException {
      CountDownLatch countDownLatch=new CountDownLatch(3);
      new Thread(()->{
          countDownLatch.countDown();
      },"t1").start();
      new Thread(()->{
          countDownLatch.countDown();
      },"t2").start();
      new Thread(()->{
          countDownLatch.countDown();
  	},"t3").start();
      countDownLatch.await();
  	System.out.println("所有线程执行完毕"); 
   }
  ```

* countDown()调用必须放在代码中总是可以被执行到的地方，例如finally块中

* await(long)指定一个超时时间，如果在该时间内计数器还未达到0，则所有执行该实例的await方法的线程都会被唤醒，返回值可用于区分其返回是否是由于等待超时

* 使用场景：
  - 通过countdownlatch实现最大的并行请求，也就是可以让N个线程同时执行
  - 比如应用程序启动之前，需要确保相应的服务已经启动

* 调用thread.join() 方法必须等thread 执行完毕，当前线程才能继续往下执行，⽽CountDownLatch通过计数器提供了更灵活的控制，只要检测到计数器为0当前线程就可以往下执行⽽不用管相应的thread是否执行完毕

## Semaphore

* Semaphore.acquire()在成功获得一个配额后会立即返回，如果当前的可用配额不足，那么Semaphore.acquire()会使其执行线程暂停
* Semaphore内部会维护一个等待队列用于存储这些被暂停的线程

* 信号灯，semaphore可以控制同时访问的线程个数，通过acquire获取一个许可，如果没有就等待，通过release释放一个许可（应该放在finally块中），并唤醒相应Semaphore实例的等待队列中的一个任意等待线程（采用非公平性调度策略）

* 创建Semaphore实例时如果构造器中的参数permits值为1，那么所创建的Semaphore实例相当于一个互斥锁。与其他互斥锁不同的是，由于一个线程可以在未执行过Semaphore.acquire()的情况下执行相应的Semaphore.release()，因此这种互斥锁允许一个线程释放另外一个线程锁所持有的锁

* 使用场景

  - 可以实现对某些接口访问的限流

  ```java
  public static void main(String[] args) {
      Semaphore semaphore=new Semaphore(5);
      for(int i=0;i<10;i++){
          new Car(i,semaphore).start();
      }
  }
  
  static class Car extends Thread{
      private int num;
      private Semaphore semaphore;
      public Car(int num, Semaphore semaphore) {
          this.num = num;
          this.semaphore = semaphore;
      }
      public void run(){
          try {
  			semaphore.acquire();//获取一个许可 
              System.out.println("第"+num+"占用一个停车位"); 
              TimeUnit.SECONDS.sleep(2); 
              System.out.println("第"+num+"俩车走喽"); 
              semaphore.release();
          } catch (InterruptedException e) {
              e.printStackTrace();
  		} 
      }
  }
  ```

## ==栅栏CyclicBarrier==

* 多个线程可能需要相互等待对方执行到代码中的某个地方，这时这些线程才能够继续执行
* 参与方（等待的线程）只需执行CyclicBarrier.await()就可以实现等待，其既是等待方法又是通知方法。CyclicBarrier实例的所有参与方除最后一个线程外都相当于等待线程，最后一个线程则相当于通知线程
* 参与方是并发执行CyclicBarrier.await()的，但是，CyclicBarrier内部维护了一个显示锁，这使得其总是可以在所有参与方中区分出一个最后执行CyclicBarrier.await()的线程，除最后一个线程外的任何参与方执行CyclicBarrier.await()都会导致该线程被暂停（线程生命周期状态为WAITING），最后一个线程执行CyclicBarrier.await()会使得使用相应CyclicBarrier实例的其他所有参与方被唤醒，而最后一个线程自身并不会被暂停
* 与CountDownLatch不同的是，CyclicBarrier实例是可重复使用的：所有参与方被唤醒的时候，任何线程再次执行CyclicBarrier.await()又会被暂停，直到这些线程中的最后一个线程执行了CyclicBarrier.await()
* 主要开销在可能产生的上下文切换
* 典型应用场景
  * 使迭代算法并发化
    * 在并发的迭代算法中，迭代操作是由多个工作者线程并行执行的，中间结果作为下一轮迭代的基础（输入）
  * ==模拟高并发==
* 如果代码对CyclicBarrier.await()调用不是放在一个循环之中，并且使用CyclicBarrier的目的也不是为了模拟高并发操作，那么此时对CyclicBarrier的使用可能是一种滥用

## 生产者-消费者模式

* 生产者和消费者是并发地运行在各自的线程之中，可以使程序中原本串行的处理得以并发化
* 生产者-传输通道-消费者
  * 传输通道通常可以使用一个线程安全的队列来实现
  * 阻塞队列
    * 即从传输通道中存入一个产品或者取出一个产品时，相应的线程可能因为传输通道中没有产品或者其存储空间已满而被阻塞（暂停）
    * BlockingQueue是一种线程安全的阻塞队列，实现类
      * 有界队列和无界队列（最大存储容量为Integer.MAX_VALUE(2^31-1)个元素）
      * ArrayBlockingQueue
        * 有界队列
        * 内部使用一个数组作为其存储空间，而数组的存储空间是预先分配的，因此其put操作、take操作本身不会增加垃圾回收的负担
        * 缺点是内部在实现put、take操作的时候使用的是同一个锁（显示锁），从而可能导致锁的高争用，进而导致较多的上下文切换
      * LinkedBlockingQueue
        * 有界队列（构造函数指定队列容量）、无界队列
        * 优点，其内部在实现put、take操作的时候分别使用了两个显示锁（putLock和takeLock），这降低了锁争用的可能性
        * 内部存储空间是一个链表，而链表节点（对象）所需的存储空间是动态分配的，put操作、take操作都会导致链表节点的动态创建和移除，因此其缺点是它可能增加垃圾回收的负担
        * 如果生产者线程和消费者线程之间的并发程度比较大，那么这些线程对传输通道内部所使用的锁的争用可能性也随之增加，这时，有界队列的实现适合选用LinkedBlockingQueue，否则考虑使用ArrayBlockQueue
      * SynchronousQueue
        * 特殊的有界队列
        * 内部并不维护用于存储队列元素的存储空间
        * 在使用其作为传输通道的生产者-消费者模式中，生产者线程生产好一个产品之后，会等待消费者线程来取走这个产品才继续生产下一个产品
        * 适合于在消费者处理能力与生产者处理能力相差不大的情况下使用，避免较多的线程等待导致上下文切换
  * 阻塞队列也支持非阻塞操纵（即不会导致执行线程被暂停），比如，BlockingQueue接口定义的offer(E)-（返回false表示入队列失败-队列已满）和poll()-（返回null表示队列为空）分别相当于put(E)和take()的非阻塞版
* 管道
  * 线程间的直接输出和输入
  * PipedOutputStream和PipedInputStream
    * 适合在两个线程间使用，即适用于单生产者-单消费者情形
    * 输出异常的处理，生产者线程出现异常时需要通过关闭PipedOutputStream.close()实例来实现通知相应的消费者线程，否则消费者线程可能会无限制地等待新的数据
* 双缓冲与Exchange
  * 当消费者线程消费一个已填充的缓冲区时，另外一个缓冲区可以由生产者线程进行填充，从而实现了数据生成与消费的并发
  * Exchanger可以用来实现双缓冲

## 线程停止与线程中断机制

* 主线程调⽤⼦线程的==stop()方法==，但是主线程并不知道子线程的情况，强制中断会造成不可预料的问题，并不会保证线程的资源正常释放

* Java平台会为每个线程维护一个被称为中断标记的布尔型状态变量用于表示相应线程是否接收到了中断，中断标记值为true表示相应线程收到了中断。

* 目标线程可以通过Thread.currentThread().isInterrupted()调用来获取该线程的中断标记值，也可以通过Thread.interrupted()来获取并重制（清空）中断标记值，即Thread.interrupted()会返回当前线程的中断标记值并将当前线程中断标记重置为false

* 调用一个线程的interrupt()相当于将该线程（目标线程）的中断标记置为true

* interrupt 方法

  * ==当其他线程通过调用当前线程的 interrupt 方法，表示向当前线程打个招呼，告诉他可以中断线程的执行了，至于什么时候中断，取决于当前线程自己==

  * ==线程通过检查自身是否被中断来进行响应，可以通过 isInterrupted()来判断是否被中断==

  * 通过标识位或者中断操作的方式能够使线程在终止时==有机会去清理资源==，而不是武断地将线程停止，因此这种终止线程的做法显得更加安全和优雅

    ```java
    public class InterruptDemo {
    	private static int i;
    	public static void main(String[] args) throws InterruptedException {
    		Thread thread=new Thread(()->{
    			while(!Thread.currentThread().isInterrupted()){
    				i++;
    			}
    		System.out.println("Num:"+i);
    	},"interruptDemo");
            
    	thread.start();
    	TimeUnit.SECONDS.sleep(1);
    	thread.interrupt();
    	}
    }
    ```

* volatile变量

  * 定义一个 volatile 修饰的成员变量，来控制线程的终止，是==应用了volatile 能够实现多线程之间共享变量的可见性==这一特点来实现的

  * 单个线程修改变量，多个线程读取变量的地方。也就是对内存可见性要求高，而对原子性要求低的地方，可以使用该关键字

  * 功能：

    - 主内存和⼯作内存，直接与主内存产⽣交互，进行读写操作，保证可见性

    - 禁⽌ JVM 进行的指令重排序(==在 volatile 变量的赋值操作后面会有一个内存屏障(生成的汇编代码上)，屏障之前的操作执行完后执行屏障之后的操作，读操作不会被重排序到内存屏障之前；内存屏障防止指令重排序==)   

      ```java
      public class VolatileDemo {
      	private volatile static boolean stop=false;
      	public static void main(String[] args) throws InterruptedException {
      		Thread thread=new Thread(()->{
      			int i=0;
      			while(!stop){
      				i++;
      			}
      		});
      		thread.start();
      		System.out.println("begin start thread");
      		Thread.sleep(1000);
      		stop=true;
      	}
      }
      ```

## 线程复位

* Thread.interrupted

  * 静态方法 Thread.interrupted()对设置中断标识的线程复位

    ```java
    //外面的线程调用 thread.interrupt 来设置中断标识，而在线程里面，又通过 Thread.interrupted 把线程的标识又进行了复位
    public class InterruptDemo {
    	public static void main(String[] args) throws InterruptedException{
    		Thread thread=new Thread(()->{
    			while(true){
    				boolean ii=Thread.currentThread().isInterrupted();
    				if(ii){
    					System.out.println("before:"+ii);
    					Thread.interrupted();//对线程进行复位，中断标识为false
    					System.out.println("after:"+Thread.currentThread()
    .isInterrupted());
    				}
    			}
    		});
    		thread.start();
    		TimeUnit.SECONDS.sleep(1);
    		thread.interrupt();//设置中断标识,中断标识为 true
    	}
    }
    ```

* 其他的线程复位

  * 被动复位的场景，就是对抛出 InterruptedException 异常的方法

  * 在InterruptedException 抛出之前，JVM 会先把线程的中断标识位清除，然后才会抛出 InterruptedException，这个时候如果调用 isInterrupted 方法，将会返回 false

  * Java应用层代码通常可以通过对InterruptException等异常进行处理的方式来实现中断响应

    * 不捕获InterruptedExecption，方法的异常声明throws中加一个InterruptedExecption，抛给上层代码（调用方）
    * 捕获InterruptedExecption后重新将该异常抛出，捕获并做一些中间处理，接着再抛给其上层代码
    * 捕获InterruptedException并在捕获该异常后中断当前线程，实际上在捕获到InterruptedException后又恢复中断标志，相当于当前代码告诉其他代码：我发现了中断，但我并不知道如何处理比较妥当，因此保留了中断标记

    ```java
    //捕获InterruptedException后恢复中断标志
    public final class Tools {
      public static void randomPause(int maxPauseTime) {
        int sleepTime = rnd.nextInt(maxPauseTime);
        try {
          Thread.sleep(sleepTime); 
        } catch(InterruptedException e) {
          Thread.currentThread().interrupt(); //保留线程中断标记
        }
      }
    }
    ```

    ```java
    public class InterruptDemo {
    	public static void main(String[] args) throws InterruptedException{
    		Thread thread=new Thread(()->{
    			while(true){
    				try {
    					Thread.sleep(10000);
    				} catch (InterruptedException e) {
    				//抛出该异常，会将复位标识设置为 false
    					e.printStackTrace();
    				}
    			}
    		});
    		thread.start();
    		TimeUnit.SECONDS.sleep(1);
    		thread.interrupt();//设置复位标识为 true
    		TimeUnit.SECONDS.sleep(1);
    		System.out.println(thread.isInterrupted());//false
    	}
    }
    ```

  * 目标线程可能因为执行CountDownLatch.await()、CyclicBarrier.await()以及ReentrantLock.lockInterruptibly()能够响应中断的阻塞方法/操作而被暂停时，发起线程会给这些方法的执行线程发送中断会导致Java虚拟机将相应的线程唤醒并使其抛出InterruptedException。可见，给目标线程发送中断还能够产生唤醒目标线程的效果

# 保障线程安全

## Java运行时存储空间

## 无状态对象

## 不可变对象

## 线程持有对象

## 装饰器模式

## 并发集合

# 线程的活性故障

## 死锁

* 一般是指两个线程相互等待对⽅释放同步监视器
* 描述：
  - 线程a，需要操作x、y两个文件，在已获得x文件的操作权限后，由于线程b在使用y资源，故线程a进入阻塞状态，但是x资源并未释放，此时线程b又需要使用x资源，也进入了阻塞状态，此时这两线程就会进入死锁状态
* 如何避免：
  - ==可以指定获取锁的顺序==
    - ==只有在获取x资源的锁之后才能获取y资源的锁==，这样一个线程就能确认保持能够同时操作x和y资源，完成线程任务，释放x、y资源

## 锁死

## 线程饥饿

## 线程的暂挂与恢复

## ==线程池==

* 线程的高效利用
* Java1.5中引入的Executor框架==把任务的提交和执行进行解耦，只需要定义好任务，然后提交给线程池，而不用关心该任务是如何执行、被哪个线程执行，以及什么时候执行；负责执行任务的线程的生命周期都由Executor框架进行管理==
* 作用
  * 每次启动线程都需要new Thread新建对象与线程，性能差。线程池能==重用存在的线程，降低创建线程和销毁线程的性能开销==
  * 线程缺乏统一管理，可以无限制的新建线程，导致OOM。线程池可以==控制可以创建、 执行的最⼤并发线程数，避免导致OOM==
  * 缺少工程实践的一些高级的功能如定期执行、线程中断。线程池==提供定期执⾏、并发数控制功能==

# Java异步编程

* 从多个任务的角度来看，任务可以是串行执行的，也可以是并发执行的；从单个任务的角度来看，任务的执行方式可以是同步的，也可以是异步的
  * 同步方式和异步方式是相对的，如果将该任务提交给线程池执行，从任务提交线程的角度来看则为异步任务，而从线程池中的工作者线程（实际执行该任务的线程）的角度来看该任务则可能是一个同步任务

* 同步任务，任务的发起与任务的执行是串行的
  * 同步任务的发起线程在其发起该任务之后必须等待该任务执行结束才能够执行其他操作，这种等待往往意味着阻塞，即任务的发起线程会被暂停，直到任务执行结束
  * 同步任务的发起线程是采用阻塞的方式还是轮询方式来等待任务的结束很大程度上取决于所使用的API
    * 阻塞，意味着在同步任务执行结束前，该任务的发起线程并没有在运行（其生命周期状态不为RUNNABLE），多数情况下只能够以阻塞方式来实现等待同步任务的结束
    * 轮询，意味着在同步任务执行结束前，该任务的发起线程仍然在运行，只不过此时该线程的主要动作是不断地检查相应的任务是否执行结束，如使用java.nio.channels.Selector类来编写网络应用程序的服务端代码
* 异步任务，任务的发起与任务的执行是并发的
  * 多线程编程本质上是异步的
  * 比如一个线程通过ThreadPoolExecutor.submit(Callable<T>)调用向线程池提交一个任务（任务的发起），在该调用返回之后该线程便可以执行其他操作了，而该任务可能在此后才被线程池中的某一个工作者线程所执行，这里任务的提交与执行是并发的，而不是串行的。异步任务可以使其发起线程不必因等待其执行结束而被阻塞，即异步任务执行方式往往意味着非阻塞
* 同步任务执行方式多数情况下意味着阻塞，但是也可能意味着非阻塞（轮询）
* 异步任务执行方式多数情况下意味着非阻塞，但是也可能意味着阻塞，如向线程池提交一个任务后立刻调用Future.get()来试图获取该任务的处理结果，发起线程因此可能因为该任务尚未被线程池执行结束而被阻塞
* 异步任务的执行需要借助多个线程来实现。多个异步任务能够以并发的方式被执行

## 同步计算与异步计算

* 同步需要等待也就意味着阻塞，异步不用阻塞

* Java Executor：

1. runnable和callable接口，都是对处理事务的抽象

2. Executor接口则是对事务的执行进行抽象

   - ==将任务提交给executor接口执行，执行方式为同步执行==
   - ==将任务提交给ThreadPoolExecutor执行，则为异步执行==

3. 异步任务的批量执行

   Future可以获取任务执行的结果，而completionservice，可以一次性提交一批任务执行，并返回所有结果

## Java Exexutor框架

* Runnable接口和Callable接口都是对任务处理逻辑的抽象
* Executor接口则是对任务的执行进行的抽象
  * 使得任务的提交能够与任务执行的具体细节解耦，好处：
    * 在一定程度上能够屏蔽任务同步执行与异步执行的差异。如对于同一个任务（Runnable实例），如果将其提交给一个ThreadPoolExecutor执行，则该任务就是异步执行；如果将其提交给Executor实例执行，则是同步执行。不论是同步执行，还是异步执行，对于提交方来说并没有太大差别，为更改任务的具体执行方式提供了灵活性和便利

* ==`Executor` - `ExecutorService`-`AbstractExecutorService`-`ThreadPoolExecutor`==

* Executor是一个顶层接口，在它里面只声明了一个⽅法execute(Runnable)，返回值为void，参数为Runnable类型，用来执行传进去的任务的。空闲时无法主动释放内部工作者线程所占用的资源

* 然后ExecutorService接口继承了Executor接口，并声明了一些方法：submit（可以返回future执行结果）、invokeAll、invokeAny以及shutDown、shutdownNow等。

  * ThreadPoolExecutor是executorservice的默认实现类

* 抽象类AbstractExecutorService实现了ExecutorService接口，基本实现了
  ExecutorService中声明的所有⽅法

* 然后ThreadPoolExecutor继承了类AbstractExecutorService

* ==在ThreadPoolExecutor类中有⼏个非常重要的方法==：

  - ==execute()== 通过这个方法可以向线程池提交一个任务，交由线程池去执行,execute只能接受Runnable类型的任务
  - ==submit()== 也是⽤来向线程池提交任务的，但是它和execute()⽅法不同，它能够返回任务执行的结果，实际上还是调用的execute()方法，只不过它利用了Future来获取任务执行结果；不管是Runnable还是Callable类型的任务都可以接受，但是Runnable返回值均为void，所以使用 Future的get()获得的还是null
  - ==shutdown()== 不会立即终止线程池，而是要等所有任务缓存队列中的任务都执行完后才终止，但再也不会接受新的任务
  - ==shutdownNow()== 立即终⽌线程池，尝试打断正在执行的任务，并且清空任务缓存队列，返回尚未执行的任务

* 使用（通过Executors工厂类可以创建特定功能的线程池）

  * 固定型线程池newFixedThreadPool

    * 该方法返回一个固定数量的线程池，线程数不变，核心线程数和最大线程数都是指定值，线程数少于核心线程数，新建线程执行任务，当线程池中的线程数超过核心线程数后，任务都会被放到阻塞队列中（LinkedBlockingQueue），由于队列容量非常大，可以一直添加，执行完任务的线程反复去队列中取任务执行。不过当线程池没有可执行任务时，也不会释放线程

    * 用途：==FixedThreadPool 用于负载比较大的服务器，为了资源的合理利用，需要限制当前线程数量。必须在不再需要该线程池时主动将其关闭==

      ```java
      ExecutorService service3 = Executors.newFixedThreadPool(10);
      public static ExecutorService newFixedThreadPool(int nThreads) {
          return new ThreadPoolExecutor(nThreads, nThreads,
      		0L, TimeUnit.MILLISECONDS,
      		new LinkedBlockingQueue<Runnable>());
      }
      ```

  * 单例线程池newSingleThreadExecutor

    * 创建一个线程的线程池，保证所有任务按照指定顺序(FIFO, LIFO, 优先级)执行。若空闲则执行，若没有空闲线程则暂缓在任务队列中。如果该线程异常结束，会重新创建一个新的线程继续执行任务，唯一的线程可以保证所提交任务的顺序执行，内部使用LinkedBlockingQueue作为阻塞队列
    * 适合用来实现单（多）生产者-单消费者模式

  * 缓存线程池newCachedThreadPool

    * 创建一个可缓存线程池

    * 执行任务的流程：

      1. 没有核心线程，直接向 SynchronousQueue 中提交任务
      2. 如果有空闲线程，就去取出任务执行;如果没有空闲线程，就新建一个， ==线程数可达到Integer.MAX_VALUE(2的31次方减1)==
      3. 执行完任务的线程有 60 秒生存时间，如果在这个时间内可以接到新任务，就可以继续活下去，否则就被回收

    * 用途：==这类线程池适合用来执行大量耗时较短且提交频率较高的任务==

      ```java
      ExecutorService service2 = Executors.newCacheThreadPool();
      public static ExecutorService newCachedThreadPool() {
          return new ThreadPoolExecutor(0, Integer.MAX_VALUE,
                                        60L, TimeUnit.SECONDS,
      								  new SynchronousQueue<Runnable>());
      }
      ```

  * 调度型线程池newScheduledThreadPool

    * 创建一个可以指定线程的数量的线程池，但是这个线程池还带有==延迟和周期性执行任务的功能==，类似定时器 

      * 延迟执行提交的任务`schedule`
      * 周期性地执行提交的任务`scheduleAtFixedRate`

    * 调度型线程池会根据Scheduled(任务列表)进行延迟执行，或者是进行周期性的执行.适用于一些周期性的工作

    * 典型的计划任务有：清理系统的垃圾数据、系统监控和数据备份等

      * ScheduledExecutorService接口是ExecutorService的子接口
      * ScheduledExecutorService接口的默认实现类是ScheduledThreadPoolExecutor
      * ScheduledThreadPoolExecutor类是ThreadPoolExecutor的子类

      ```java
      ExecutorService service4 = Executors.newScheduledThreadPool(10);
      static ExecutorService service=Executors.newFixedThreadPool(3);
        service.execute(...);
        
        service.shutdown();
      ```

* ThreadpoolExecutor

  * ==Executors是java线程池的工厂类，通过它可以快速初始化一个符合业务需求的线程池，其本质是通过不同的参数初始化一个ThreadPoolExecutor对象==，具体参数描述如下：

    1. ==corePoolSize==

       线程池中的核心线程数，当提交一个任务时，线程池创建一个新线程执行任务，直到当前线程数等于corePoolSize；如果当前线程数为corePoolSize，继续提交的任务被保存到阻塞队列中，等待被执行

    2. ==maximumPoolSize==

       线程池中允许的最大线程数。如果当前阻塞队列满了，且继续提交任务，则创建新的线程执行任务，前提是当前线程数小于maximumPoolSize，否则任务将被拒绝。

    3. ==keepAliveTime==

       线程空闲时的存活时间，即当线程没有任务执行时，继续存活的时间；默认情况下，该参数只在线程数大于corePoolSize时才有用

    4. ==unit==

       keepAliveTime的单位，线程没有任务执行时可以保持的时间。即当线程池中的线程数大于corePoolSize时，如果一个线程空闲的时间达到keepAliveTime，则会终⽌，直到线程池中的线程数不超过corePoolSize

    5. ==workQueue==

       用来保存等待被执行的任务的阻塞队列，且任务必须实现Runable接口，在JDK中提供了如下阻塞队列：

       - ArrayBlockingQueue：基于数组结构的有界阻塞队列，按FIFO排序任务；
       - LinkedBlockingQuene：基于链表结构的阻塞队列，按FIFO排序任务，吞吐量通常要高于ArrayBlockingQuene；
       - SynchronousQuene：一个不存储元素的阻塞队列，每个插入操作必须等到另一个线程调用移除操作，否则插入操作一直处于阻塞状态，吞吐量通常要高于LinkedBlockingQuene；
       - priorityBlockingQuene：具有优先级的无界阻塞队列；

    6. ==threadFactory==

       创建线程的工厂，通过自定义的线程工厂可以给每个新建的线程设置一个具有识别度的线程名

    7. ==handler==

       线程池的饱和策略，当阻塞队列满了，且没有空闲的工作线程，如果继续提交任务，必须采取一种策略处理该任务，线程池提供了4种策略：

       1. AbortPolicy：直接抛出异常，==默认策略==；
       2. CallerRunsPolicy：用调用者所在的线程来执行任务
       3. DiscardOldestPolicy：丢弃阻塞队列中靠最前的任务，并执行当前任务
       4. DiscardPolicy：直接丢弃任务

       ```java
       //ThreadPoolExecutor类中提供了四个构造方法
       public ThreadPoolExecutor(
            int corePoolSize, //核心线程数量 int maximumPoolSize, //最大线程数
       	 long keepAliveTime, //超时时间,超出核心线程数量以外的线程空余存活时间
       	 TimeUnit unit, //存活时间单位
       	 BlockingQueue<Runnable> workQueue, //保存执行任务的队列 
            ThreadFactory threadFactory,//创建新线程使用的工厂
            RejectedExecutionHandler handler //当任务无法执行的时候的处理方式 
        ){
           this(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue,
                Executors.defaultThreadFactory(), defaultHandler);
       }
       ```

  * 状态

    * 在ThreadPoolExecutor中定义了一个volatile变量(保证线程之间的可⻅性)，另外定义了几个static final变量表示线程池的各个状态，volatile int ==runState==;
      - RUNNING(创建线程池时)
      - SHUTDOWN（调用了shutdown()方法，此时不能够接受新的任务，它会等待所有任务执行完毕）
      - STOP（调用了shutdownNow()方法，不能接受新的任务，并且会去尝试终止正在执行的任务）
      - TERMINATED（处于SHUTDOWN或STOP状态，并且所有工作线程已经销毁，任务缓存队列已经清空或执行结束后的状态）

  * 创建线程的逻辑

    * 以下任务提交逻辑来自==ThreadPoolExecutor.execute方法==，任务提交给线程池之后的处理策略：

    1. 如果运行的线程数 < corePoolSize，直接创建新线程去执行这个任务，即使有其他线程是空闲的
    2. 如果运行的线程数 >= corePoolSize，则每来一个任务，会尝试将其添加到任务缓存队列当中 
       1. 如果插入队列成功，则完成本次任务提交，该任务会等待空闲线程将其取出去执行 
       2. 如果插入队列失败，一般来说是任务缓存队列已满，则会尝试创建新的线程去执行这个任务
       3. 如果当前线程数 < maximumPoolSize，创建新的线程放到线程池中 
       4. 如果当前线程数 >= maximumPoolSize，会执行指定的拒绝策略
    3. 如果线程池中的线程数量⼤于 corePoolSize时，如果某线程空闲时间超过keepAliveTime，线程将被终止，直⾄线程池中的线程数目不大于corePoolSize；如果允许为核⼼池中的线程设置存活时间，那么核心池中的线程空闲时间超过keepAliveTime，线程也会被终⽌

  * 任务执行

    * 通过java.util.concurrent.==ExecutorService==接口对象来执行任务，该对象有两个方法可以执行任务execute和submit
    * submit和execute的区别
      - execute只能接受Runnable类型的任务，不能获取返回值，因此无法判断任务是否执行成功
      - submit不管是Runnable还是Callable类型的任务都可以接受，这种方式它会返回一个Future对象，通过future的get方法来获取返回值，Runnable返回值均为void，所以使用Future的get()获得的还是null，get方法会阻塞住直到任务完成
      - ==execute是Executor接口中唯一定义的方法；submit是ExecutorService（该接口继承Executor）中定义的方法==
    * Callable任务除了返回正常结果之外，如果发生异常，该异常也会被返回，即Future可以拿到异步执行任务各种结果
    * Future.get方法会导致主线程阻塞，直到Callable任务执行完成

## 异步任务的批量执行

* CompletionService接口：提供异步任务的批量提交一级获取这些任务的处理结果
  * 提交异步任务：`Future<V> submit(Callable<V> task)`
  * 获取批量提交的异步任务的处理结果：`Future<V> take() `，阻塞直到执行结束，批量提交了多少个异步任务，则多少次连续调用该方法获取这些任务的处理结果

## FutureTask

* 异步计算助手
* Runnable实例或Callable实例交给线程池执行就是异步任务
  * Runnable任务既可以交给一个专门的工作者线程执行，也可以交给一个线程池或者Executor的其他实现类来执行，缺点是无法直接获取任务执行结果
  * Callable任务可以通过`ThreadPoolExecutor.submit(Callable<T>)`的返回值获取任务的处理结果，缺点是只能交给线程池执行
* FutureTask类结合了Runnable接口和Callable接口的优点：
  * FutureTask是Runnable接口的一个实现类：其表示的异步任务既可以交给专门的工作者线程执行，也可以交给Executor实例（比如线程池）执行
  * 还能够返回其代表的异步任务的处理结果
  * ThreadPoolExecutor.submit(Callable<T> task)的返回值就是一个FutureTask实例，FutureTask是RunnableFuture接口的一个实现类，RunnableFuture接口继承了Future接口和Runnable接口
  * FutureTask的一个构造器可以将Callable实例转换为Runnable实例`public FutureTask(Callable<V> callable)`

# Java多线程程序的调试与测试

# 多线程编程的硬件基础

* 存储转发：处理器直接从写缓冲器中读取数据来实现内存读操作的技术。使得写操作的执行处理器能够在不影响该处理器执行读操作的情况下将写操作的结果存入写缓冲器

## 高速缓存

* 填补处理器与内存之间的鸿沟
* 起源
  * ==运算任务不仅依赖处理器计算，处理器还需与内存交互==（比如读取运算数据、存储运算结果）
  * ==在多处理器系统中，每个处理器都有自己的高速缓存，而它们又共享同一主内存（MainMemory）==
  * 高速缓存来作为内存和处理器之间的缓冲：将运算需要使用的数据复制到缓存中，让运算能快速进行，当运算结束后再从缓存同步到内存之中
  * 高速缓存从下到上（L1 cache）越接近 CPU 速度越快，同时容量也越小
* 问题
  * ==多核心 CPU 情况下存在指令并行执行，而各个CPU 核心之间的数据不共享从而导致缓存一致性问题==
  * ==不同CPU读取主缓存中数据进行修改并缓存到各自高速缓存中，但没有写入主内存中，会导致数据不一致性问题==
* 解决：
  * 缓存一致性协议
  * 缓存一致性机制会阻止同时修改被两个以上处理器缓存的内存区域的数据，当其他处理器回写已经被锁定的缓存行的数据时会导致该缓存行无效 

## 缓存一致性协议

* 数据世界的交通规则，处理器之间的一种通信机制
  * 一个处理器对其副本数据进行更新之后，其他处理器如何察觉到该更新并作出适当反应，以确保这些处理器后续读取该共享变量时能够读取到这个更新
* MESI协议是一种广为使用的缓存一致性协议，其对内存数据访问的控制类似于读写锁，它使得针对同一地址的读内存操作是并发的，而针对同一地址的写内存操作是独占的
* ==`处理器`上有一套完整的协议，来保证 Cache 的一致性，比较经典的应该就是 MESI 协议了，为了保障数据的一致性，它的方法是在 CPU 缓存中保存一个标记位==，这个标记为有四种状态：
  1. M(Modified) 修改缓存，当前 CPU 缓存已经被修改，表示已经和内存中的数据不一致了，如果别的CPU内核要读主存这块数据，该缓存行必须回写到主存，状态变为共享(S).
  2. I(Invalid) 失效缓存，说明 CPU 的缓存已经不能使用了
  3. E(Exclusive) 独占缓存，当前cpu的缓存和内存中数据保持一直，而且其他处理器没有缓存该数据 ，当别的缓存读取它时，状态变为共享；当前写数据时，变为已修改状态。
  4. S(Shared) 共享缓存，数据和内存中数据一致，并且该数据存在多个 cpu缓存中
* 嗅探(snooping)"协议：
  - ==每个 Core 的 Cache 控制器不仅知道自己的读写操作，也监听其它 Cache 的读写操作==
* ==CPU 的读取会遵循几个原则：==
  - 如果缓存处于 M 或者 E 的 CPU 嗅探到其他 CPU 有读的操作，就把自己的缓存写入到内存，并把自己的状态设置为 S
  - 只有缓存状态是M或E的时候，CPU才可以修改缓存中的数据，修改后，缓存状态变为 M
  - 如果缓存的状态是 I，那么就从内存中读取，否则直接从缓存读取
* 在多个线程共享变量的情况下，MESI协议已经能够保障一个线程对共享变量的更新对其他处理器上运行的线程来说是可见的

## 硬件缓冲区

* 写缓冲器与无效化队列

## 基本内存屏障

# Java同步机制与内存屏障

* Java虚拟机对synchronized、volatile和final关键字的语义的实现就是借助内存屏障

## volatile关键字的实现

* Java虚拟机（JIT编译器）在volatile变量写操作之前插入的释放屏障使得该屏障之前的任何读、写操作都先于这个volatile变量写操作被提交，而Java虚拟机（JIT编译器）在volatile变量读操作之后插入的获取屏障使得这个volatile变量读操作先于该屏障之后的任何读、写操作被提交。
* 写线程和读线程通过各自执行的释放屏障和获取屏障保障了有序性
* Java虚拟机（JIT编译器）在volatile变量读操作前插入的一个加载屏障相当于LoadLoad屏障，它通过清空无效化队列来使得其后的读操作（包括volatile读操作）有机会读取到其他处理器对共享变量所做的更新。读线程能够读取到写线程对volatile变量所做的更新，有赖于写线程在volatile写操作后所执行的存储屏障。可见，volatile对可见性的保障是通过写线程、读线程配对使用存储屏障和加载屏障实现的

* 原理：
  - volatile变量修饰的共享变量，在进行写操作的时候会多出一个lock前缀的汇编指令，这个指令会触发总线锁或者缓存锁，==通过缓存一致性协议来解决可见性问题==
  - 对于声明了volatile的变量进行写操作，JVM就会向处理器发送一条Lock前缀的指令，把这个变量所在的缓存行的数据写回到系统内存，再根据MESI的缓存一致性协议，来保证多CPU下的各个高速缓存中的数据的一致性

##synchronized关键字的实现

* Java虚拟机（JIT编译器）会在monitorenter（用于申请锁的字节码指令）对应的指令后临界区开始前的地方插入一个获取屏障。Java虚拟机会在临界区结束后monitorexit（用于释放锁的字节码指令）对应的指令前的地方插入一个释放屏障。这里，获取屏障和释放屏障一起保障了临界区内的任何读、写操作都无法被重排序到临界区之外，再加上锁的排他性，这使得临界区内的操作具有原子性
* synchronized关键字对有序性的保障与volatile关键字对有序性的保障实现原理是一样的，也是通过释放屏障和获取屏障的配对使用实现的。释放屏障使得写线程在临界区中执行的读、写操作先于monitorexit对应的指令（相当于写操作）被提交，而获取屏障使得读线程必须在获得锁（相当于read-modify-write操作）之后才能够执行临界区中的操作。写线程以及读线程通过这种释放屏障和获取屏障的配对使用实现了有序性。
* Java虚拟机也会在monitorexit对应的指令（相当于写操作）之后插入一个StoreLoad屏障。这个处理的目的与在volatile写操作之后插入一个StoreLoad屏障类似。该屏障充当了存储屏障，从而确保锁的持有线程在释放锁之前所执行的所有操作的结果能够到达高速缓存，并消除了存储转发的副作用。另外，该屏障禁止了monitorexit对应的指令与其他同步块的monitoenter对应的指令进行重排序，这保障了monitoreneter与monitorexit总是成对的，从而使得synchronized块的并列以及synchronized块的嵌套称为可能

* Java对象头
  - 在Hotspot虚拟机中，对象在内存中的布局分为三块区域：对象头、实例数据和对齐填充
  - Java对象头是实现synchronized的锁对象的基础，一般而言，synchronized使用的锁对象是存储在Java对象头里。它是轻量级锁和偏向锁的关键
    - Klass Pointer（类型指针）
      - 虚拟机通过这个指针来确定这个对象是哪个类的实例
    - Mark Word（标记字段）
      - 用于存储对象自身的运行时数据
      - 如哈希码(HashCode)、GC分代年龄、锁状态标志、线程持有的锁、偏向线程 ID、偏向时间戳等等。Java对象头一般占有两个机器码(在32位虚拟机中，1个机器码等于4字节，也就是32bit)
* Monitor
  - 一个同步工具，也可以描述为一种同步机制
  - 所有的Java对象是天生的Monitor

* 锁升级

## Java虚拟机对内存屏障使用的优化

## final关键字的实现

* 在多线程环境下
  - 当一个对象被发布到其他线程的时候，该对象的所有final字段（实例变量）都是初始化完毕的，即其他线程读取这些字段的时候所读取到的值都是相应字段的初始值（而不是默认值）
  - 而非final字段没有这种保障，即这些线程读取该对象的非final字段时所读取到的值可能仍然是相应字段的默认值
  - 对于引用型final字段，final关键字还进一步确保该字段所引用的对象已经初始化完毕，即这些线程读取该字段所引用的对象的各个字段时所读取到的值都是相应字段的初始值
  - final关键字只能保障有序性，即保障一个对象对外可见的时候该对象的final字段必然是初始化完毕的。final关键字并不保障对象引用本身对外的可见性
  - 当一个对象的引用对其他线程可见的时候，这些线程所看到的该对象的final字段必然是初始化完毕的。final关键字的左右仅是这种有序性的保障，它并不能保障包含final字段的对象的引用自身对其他线程的可见性

# Java内存模型

* 缓存一致性协议确保了一个处理器对某个内存地址进行的写操作的结果==最终==能够被其他处理器所读取，但是一个处理器对共享变量所做的更新具体在什么时候能够被其他处理器读取这一点，缓存一致性协议本身还是不保障的
* 一个处理器对共享变量所做的更新在什么时候或者说什么情况下才能够被其他处理器所读取，即可见性问题
* 可见性问题又衍生出一个新的问题：一个处理器先后更新多个共享变量的情况下，其他处理器是以何种顺序读取到这些更新的，即有序性问题
* Java内存模型定义了final、volatile和synchronized关键字的行为并确保同步的Java程序能够正确地运行在不同架构的处理器之上
* Java内存模型用于线程安全方面的可见性、有序性和原子性问题
  * 原子性问题：
    * 针对实例变量、静态变量（即共享变量而非局部变量）的读、写操作，哪些是具备原子性的，哪些可能不具备原子性
    * 规定对引用类型以及非long/double的基本数据类型的共享变量进行读、写操作具有原子性，long/double基本数据类型可以通过volatile关键字修饰来保证原子性
  * 可见性问题：
    * 一个线程对实例变量、静态变量（即共享变量）进行的更新在什么情况下能够被其他线程所读取？
    * 解决：Java内存模型使用happens-before解决
  * 有序性问题：
    * 一个线程对多个实例变量、静态变量（即共享变量）进行的更新在什么情况下在其他线程看来是可以乱序的（即感知顺序与程序顺序不同）
    * 解决：Java内存模型使用happens-before解决

* 内存模型
  * ==定义了共享内存系统中多线程程序读写操作行为的规范==，来屏蔽各种硬件和操作系统的内存访问差异，来实现 Java 程序在各个平台下都能达到一致的内存访问效果 
    * Java 内存模型==定义了线程和内存的交互方式==，在 JMM 抽象模型中，分为==主内存==（所有线程共享的）、==工作内存==（每个线程独有的） 
    * 线程对变量的所有操作(读取、赋值)都必须在工作内存中进行，不能直接读写主内存中的变量，并且不同的线程之间无法访问对方工作内存中的变量，线程间的变量值的传递都需要通过主内存来完成
  * 规定了一个线程如何和何时可以看到由其他线程修改过后的共享变量的值，以及在必须时如何同步的访问共享变量
  * 调用栈和本地变量存放在线程栈上，对象存放在堆上
  * 总的来说，==**JMM 是一种规范，目的是解决由于多线程通过共享内存进行通信时，存在的本地内存数据不一致、编译器会对代码指令重排序、处理器会对代码乱序执行等带来的问题。目的是保证并发编程场景中的原子性、可见性和有序性**== 
  * ==Java内存模型封装了一系列和并发处理相关的关键字（比如volatile、Synchronized、final等）提供给开发人员使用==
    * 在开发多线程代码的时候，可以直接使用synchronized等关键词来控制并发，使得不需要关心底层的编译器优化、缓存一致性的问题了，所以在Java内存模型中，除了定义了一套规范，还提供了开放的指令在底层进行封装后，提供给开发人员使用
* ==它解决了CPU多级缓存、处理器优化、指令重排等导致的内存访问问题，保证了并发场景下的可见性、原子性和有序性==
* 解决并发问题方式
  * 限制处理器优化和使用内存屏障

## happen(s)-before关系

* 从可见性角度出发去描述有序性的
* Java内存模型定义了变量的读/写、锁的申请/释放以及线程的启动（Thread.start()调用）和加入（Thread.join()调用）等==动作==
* 假设动作A和动作B之间存在happens-before关系，那么称A happens-before B，那么Java内存模型保证A操作结果对B可见，即A操作结果会在B被执行前提交（比如写入高速缓存或者主内存）
* 具有传递性，A happend-before B，B happens-before C，那么A happens-before C，使得可见性保障具有累积的效果
* happens-before关系中两个动作既可以是同一个线程执行的，也可以是不同线程执行的
* Java内存模型定义了一些happens-before关系规则：
  * 程序顺序规则：
    * 一个线程内任何一个动作的结果对程序顺序上该动作之后的其他动作都是可见的，并且这些动作在该线程自身看来就像是完全依照程序顺序执行和提交的
  * 内部锁规则：
    * 内部锁的释放happens-before后续每一个对该锁的申请。（同一个锁实例；时间上的先后关系）
    * 内部锁规则和程序顺序规则一起确保了锁对可见性和有序性的保障
  * volatile变量规则：
    * 对一个volatile变量的写操作happens-before后续每一个针对该变量的读操作
    * 保障可见性和有序性
  * 线程启动规则：
    * 调用一个线程的start方法happens-before被启动的这个线程中的任何一个动作
    * 线程T1执行过程中启动了线程T2.start()，T2能看到T1启动其之前T1所执行的任何动作
  * 线程终止规则：
    * 线程T1等待线程T2结束，T2所执行的任何动作的结果对T1中程序顺序上在T2.join()调用之后的任何一个动作是可见的，并因此是有序的
* Java标准类库也定义了一些happens-before规则：
  * 对于任意的CountDownLatch实例，一个线程在countDownLatch.countDown()调用前所执行的所有操作与另外一个线程在countDownLatch.wait()调用成功返回之后所执行的所有动作之间存在happens-before关系
  * 对于任意的BlockingQueue实例，一个线程在blockingQueue.put(E)调用所执行的所有动作与另外一个线程在blockingQueue.take()调用返回之后所执行的所有动作之间存在happens-before关系
* 应用代码层次来看，可见性和有序性的保障是通过应用代码使用Java线程同步机制实现的
  * happens-before规则通过使用Java线程同步机制实现的
    * 内部锁规则涉及内部锁或者显示锁
    * volatile变量规则涉及volatile关键字
    * 线程启动规则和线程终止规则所涉及的Thread.start()/Thread.join()的内部实现都依赖于锁（内部锁）
* Java内存模型将一个程序中的所有动作看作一个集合，该集合中的任意两个动作之间可能存在happens-before关系，也可能不存在happens-before关系，只有正确地使用同步机制的两个动作之间才存在happen-before关系，从而使可见性、有序性有所保障

## 安全发布

## JSR

# Java多线程程序的性能调校

## Java虚拟机对内部锁的优化

自从Java6/7

* JIT编译器，即时编译，在Java中，是一个把Java的字节码（二进制文件）（包括需要被解释的指令的程序）转换成可以直接发送给处理器的指令的程序

* 锁消除

  * 是JIT编译器对内部锁的具体实现所做的一种优化

  * 如果同步块锁使用的锁对象通过逃逸分析技术被证实只能够被一个线程访问，那么JIT编译器在编译这个同步块的时候并不生成synchronized所表示的锁的申请与释放对应的机器码，消除了锁的使用

  * 可以使用ThreadLocal将一个线程安全的对象（比如Random）作为一个线程持有对象来使用，不仅仅可以避免锁的争用，还可以彻底消除这些对象内部锁使用的锁的开销

  * 逃逸分析：

    * 是分析对象动态作用域：当一个对象在方法中被定义后，它可能被外部方法所引用，称为方法逃逸。甚至还有可能被外部线程访问到，譬如赋值给类变量或可以在其他线程中访问的实例变量，称为线程逃逸

      ```java
      //方法逃逸
      public class EscapeTest {
          public static Object obj;
          public void globalVariableEscape() {  // 给全局变量赋值，发生逃逸
              obj = new Object();
          }
          public Object methodEscape() {  // 方法返回值，发生逃逸
              return new Object();
          }
          public void instanceEscape() {  // 实例引用发生逃逸
              test(this); 
          }
      }
      ```

* 锁粗化

  * 是JIT编译器对内部锁的具体实现所做的一种优化
  * 对于相邻的几个同步块，如果这些同步块所使用的是同一个锁实例，那么JIT编译器会将这些同步块合并为一个大同步块，从而避免一个线程反复申请、释放同一个锁所导致的开销
  * 两个同步块之间的其他语句，会因为指令重排序而移到后一个同步块的临界区之中

* 偏向锁

  * 是Java虚拟机对锁的实现所做的一种优化
  * 优化基于：大多数锁并没有被争用，并且这些锁在其整个生命周期内至多只会被一个线程持有。
  * Java虚拟机在实现申请锁和释放锁时需要借助CAS原子操作，代价相对来说比较昂贵，因此Java虚拟机会为每个对象维护一个偏好，即一个对象对应的内部锁第一次被一个线程获得，那么这个线程就会被记录为该对象的偏好线程，后续该线程申请锁和释放锁都无须借助CAS原子操作，从而减少锁申请和释放开销
  * 当其他线程申请该对象的内部锁时，Java虚拟机会回收并重新设置该对象的偏好线程
  * 如果存在比较多的锁争用的情况，这种偏好回收和重新分配代价会被放大，因此，偏向锁优化只适合于存在相当大一部分锁并没有被争用的系统之中。反之，可以通过命令行参数关闭偏向锁优化

* 适应性锁

  * 是JIT编译器对内部锁的具体实现所做的一种优化
  * 存在锁争用的情况下，一个线程申请一个锁的时候如果这个锁恰好被其他线程持有，那么这个线程就需要等待该锁被其持有的线程释放
    * 暂停等待策略，需要将这个线程暂停（线程的生命周期状态变为非Runnable状态），会导致上下文切换，这种实现策略比较适合于系统中绝大多数线程对该锁持有较长时间的场景，这样才能抵消上下文切换的开销
    * 忙等策略，反复执行空操作直到所需要的条件成立为止而实现等待的，不会导致上下文切换，如果所需条件在相当长一段时间内未能成立，那么忙等会一直被执行而耗费处理器资源，比较适合绝大多数线程对该锁的持有时间较短的场景，这样能够避免过多的处理器时间开销
    * Java虚拟机会根据其在运行过程中收集到的信息来判断这个锁是属于被线程持有时间较长（暂停等待策略）还是较短（忙等策略）来综合使用这两种策略，也可以先使用忙等策略失败后，再使用暂停等待策略
    * Java虚拟机的这种优化被称为适应性锁，需要JIT编译器介入

## 优化对锁的使用

* 锁的开销与锁争用监视
* 使用可参数化锁
* 减小临界区的长度
* 减小锁的粒度
* 考虑锁的替代品

## 减少系统内耗

* 上下文切换

## 多线程设计模式 

## 性能隐形杀手

* 伪共享





