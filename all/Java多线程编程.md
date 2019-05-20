# MS

* 提到线程安全，要将读写分开考虑

  - 线程安全：读或者写都是数据一致
  - 只读必然是线程安全
  - 线程不安全：读写并存的情况

* Java并发包juc提供了哪些并发工具类?

  * 提供了比synchronized更加高级的各种同步结构
    * 包括CountDownLatch、CyclicBarrier、Semaphore等，可以实现更加丰富的多线程操作
  * 各种线程安全的容器
    * 比如最常见的ConcurrentHashMap、有序的ConcunrrentSkipListMap，或者通过类似快照机制，实现线程安全的动态数组CopyOnWriteArrayList等
      * CopyOnWrite原理是任何修改操作，如add、set、remove，都会拷贝原数组，修改后替换原来的数组，通过这种防御性的方式，实现另类的线程安全
    * 三类
      * Concurrent
        * Concurrent类型没有类似CopyOnWrite之类容器相对较重的修改开销
        * 弱一致性，即当利用迭代器遍历时，如果容器发生修改，迭代器仍然可以继续进行遍历
        * 与弱一致性对应的同步容器常见的行为“fast-fail”，即检测到容器在遍历过程中发生了修改，则抛出ConcurrentModifcationException，不再继续遍历
      * CopyOnWrite
      * Blocking
  * 各种并发队列实现
    * 如各种BlockedQueue实现，比较典型的ArrayBlockingQueue、 SynchorousQueue或针对特定场景的PriorityBlockingQueue等
    * Deque的侧重点是支持对队列头尾都进行插入和删除，ConcurrentLinkedDeque和LinkedBlockingDeque
  * 强大的Executor框架
    * 可以创建各种不同类型的线程池，调度任务运行等，绝大部分情况下，不再需要自己从头实现线程池和任务调度器 

* ⽤什么工具和方法分析线程问题

  * 死锁问题,可以使用java自带的==jstack==命令进行排查
    1. jps、ps -ef | grep java查看==当前java进程的pid==。（严重情况下可以使用top命令查看当前系统cpu/内存使用率最高的进程pid）
       - 死锁的pid是：3429（占用很多资源）
    2. 使用top -Hp 3429命令==查看进程里面占用最多的资源的线程==。“-H”代表thread模式
       - 占用最多资源的线程是：3440
    3. 使用命令printf "%x\n" 3440 把==线程pid转换成16进制数==，得到：d70
    4. 使用jstack 3429 | grep -20 d70命令==查询该线程阻塞==的地方。
       - 会显示具体代码位置

* 并发包中的ConcurrentLinkedQueue和LinkedBlockingQueue有什么区别?

  * 把并发包下面的所有容器都习惯叫作并发容器，但是严格来讲，类似ConcurrentLinkedQueue这种“Concurrent*”容器，才是真正代表并发
  * Concurrent类型在常见的多线程访问场景，一般可以提供较高吞吐量
  * LinkedBlockingQueue内部则是基于锁，并提供了BlockingQueue的等待性方法

* 在多线程环境下，SimpleDateFormate是线程安全的吗？

  * 是线程不安全的
  * 使用局部变量，由共享变为局部私有都能避免多线程问题，不过也加重了创建对象的负担
  * 使用同步：同步SimpleDateFormat对象，多线程并发量大的时候会对性能有一定的影响
  * 使用ThreadLocal：（推荐）

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
  ```

* 三个线程T1,T2,T3,保证三个线程顺序执行T1结束执行T2,T2结束执行T3

  ```java
  // 有问题，还是用Executors.newSingleThreadExecutor();比较好
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
      //这里三个线程的启动顺序可以任意
      t1.start();
      t2.start();
      t3.start();
  }
  ```

* ++i是线程安全的吗?为什么?如何保证++i是线程安全的

  ```java
  //++i和i++操作并不是线程安全的，不是原子操作，在使用的时候，不可避免的会用到synchronized关键字
  synchronized (ThreadTest.class){         //全局锁
  	count++;
  }
  ```

# 多线程基础

## 为什么使用多线程

* 解决“阻塞”的问题
  * 即程序需要等待网络、I/O响应而暂时停止 CPU 占用的情况，此时可以采用多线程的方式来减少阻塞
* 通过并行计算提高程序执行性能
  * 通过多线程的技术，使得一个函数中的多个逻辑运算通过多线程技术达到一个并行执行，从而提升性能

## 如何理解多线程

* 多线程是指从软件或者硬件上实现多个线程并发执行的技术
* 优势
  * 提高系统的吞吐率
    * 使得一个进程中可以有多个并发的操作，例如，当一个线程因为I/O操作而处于等待时，其他线程仍然可以执行其他操作
  * 提高响应性
    * 对于Web应用程序而言，一个请求的处理慢了并不会影响其他请求的处理
  * 充分利用多核处理器资源
    * 在同一个程序中的多个线程也可以被同时调度到多个CPU上运行
* 风险
  * 线程安全
    * 多个线程共享数据时，如果没有采用相应的并发访问控制措施就可能产生数据一致性的问题，如读取过期数据、某些线程所做的更新被其他线程所做的更新覆盖等
  * 死锁
  * 上下文切换
    * 处理器从执行一个线程转向执行另外一个线程会导致资源消耗
* 线程安全问题
  * 如果状态不是共享的，或者不是可修改的，也就不存在线程安全问题
    * 封装、不可变
* 进程和线程
  * 当⼀个程序进入内存运行时，即变成一个进程。
  * 进程是系统中独立存在的实体，拥有⾃己的独立资源
  * 进程里包含很多线程，线程是进程中负责执行程序的执行单元
  * 线程本身是程序的顺序控制流，与父进程的其他线程共享该进程所拥有的全部资源
    * 线程是系统调度的最小单元
  * 线程优点：
    * 线程的上下文转换比进程快
    * Java内置多线程功能⽀持
    * 系统创建进程需为该进程重新分配系统资源，但线程创建代价小，效率⾼
    * 进程之间不能共享内存，但同一个进程内的线程之间可以
    * 进程通常会有数量和开销的限制
  * 单线程，是指每一个时刻，cpu只能运行一个线程
  * 多个进程可以在单个处理器上并发执行，多个进程之间不会互相影响
    * 同⼀时刻只能有一条指令执行，但多个进程指令被快速轮换执行，使得宏观上具有多个进程同时执行的效果

## 线程API

* 继承Thread类，重写run()，调用thread的start()启动，this表示当前线程

* 实现Runnable接⼝，重写run()，作为参数传入Thread构造器完成thread的创建，调用thread的start()启动，Thread.currentThread()获取当前线程

* 实现 Callable 接口通过 FutureTask 包装器来创建Thread 线程

  * 使用call方法且有返回值、能抛出异常，而Runnable定义的方法是run且没有返回值、不能抛出异常

  * 使用FutureTask类来包装Callable对象，FutureTask对象封装了该Callable对象的call()方法的返回值

  * Future

    * 表示异步计算的结果，它提供了检查计算是否完成的方法，以等待计算的完成，并检索计算的结果
    * cancel方法可以取消任务的执行，参数true为立即中断，false为允许运行任务完成
    * get 方法等待（阻塞）计算完成，获取计算结果

    ```java
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

## 生命周期

* NEW：初始状态，线程被构建，但是还没有调用 start 方法
* RUNNABLED：运行状态，JAVA 线程把操作系统中的==就绪和运行==两种状态统一 称为“运行中” 
  - 当线程对象调用了start()方法之后，该线程就进⼊就绪状态。就绪状态的线程处于就绪队列中， 要等待JVM里线程调度器的调度
  - 如果就绪状态的==线程获取 CPU 资源==，就可以执行 run()，此时线程便处于运行状态。处于运行状态的线程最为复杂，它可以变为阻塞状态、就绪状态（调用yield()放弃CPU使用权）和死亡状态 
* BLOCKED：阻塞状态，表示线程进入等待状态,也就是线程因为某种原因放弃了 CPU 使用权，阻塞也分为几种情况
  - ==等待阻塞==：运行的线程执行wait方法，jvm会把当前线程放入到等待队列
  - ==同步阻塞==：运行的线程在获取对象的同步锁时，若该同步锁被其他线程锁占用了，那么 jvm 会把当前的线程放入到锁池中
  - ==其他阻塞==：运行的线程执行Thread.sleep或者t.join方法，或者发出了I/O请求时，JVM 会把当前线程设置为阻塞状态，==当 sleep 结束、join 线程终止、io 处理完毕则线程恢复==
* WAITING：等待状态
* ==TIME_WAITING==：超时等待状态，超时以后自动返回
* ==TERMINATED==：终止状态，表示当前线程执行完毕

![image-20181212154854384](/Users/dingyuanjie/Desktop/MyKnowledge/2.code/java/2.咕泡学院/04.多线程高并发/image-20181212154854384-4600934.png)

## 上下文切换

* 一个处理器可以在同一时间段内运行多个线程，每个线程分配一个时间片，时间片决定了一个线程可以连续占用处理器运行的时间长度
* 一个线程被剥夺处理器的使用权，另一个线程被选中开始或继续运行的过程，叫上下文切换
* 自发性上下文切换（线程由于其自身因素导致的切出）
  * Thread.sleep(long millis)
  * Object.wait()
  * Thread.yield()
  * Thread.join()
  * LockSupport.park()
  * 线程发起了I/O操作（如读取文件）或者等待其他线程持有的锁
* 非自发性上下文切换（线程由于线程调度器的原因被迫切出）
  * 时间片用完
  * 一个优先级更高的线程需要被运行
  * Java虚拟机的垃圾回收过程中可能需要暂停所有应用线程（StopTheWorld）才能完成其工作

## 其他

* 线程的优先级

  * 每⼀个 Java 线程都有一个优先级，这样有助于操作系统确定线程的调度顺序取值范围是 1 (Thread.MIN_PRIORITY ) - 10 (Thread.MAX_PRIORITY )
  * `setPriority(int newPriority)更改线程的优先级`

* 线程执行顺序

  * join()/join(long)，让一个线程等待另⼀个线程完成的方法

  * 创建只有一个线程的线程池，把线程放进队列里FIFO(先进先出)执行

    ```java
    ExecutorService excutorService = Executors.newSingleThreadExecutor();
    excutorService.submit(thread1);
    excutorService.submit(thread2);
    excutorService.submit(thread3);
    ```

* static关键字

  * 在多线程环境下，线程总是可以读取到一个类的静态变量（基本类型和引用类型）的初始值（而不是默认值），但是获取相对新值则还是需要借助同步机制

* 基本的Web库类、Servlet具有天生的多线程性
* Unsafe可认为是Java中留下的后门，提供了一些低层次操作，如直接内存访问、线程调度等
* Java的线程机制是==抢占式==的，即调度机制会周期性地中断线程，将上下文切换到另一个线程，从而为每个线程都提供时间片，使得每个线程都会分配到数量合理的时间去驱动它的任务
* JAVA编译器（javac.exe）的作用是将java源程序编译成中间代码字节码文件
* JIT编译器（即时编译器），动态编译的一种形式，是一种提高程序运行效率的方法
* 无状态对象：不包含任何实例变量以及可更新的静态变量，具有固有的线程安全性
* 并行：是指系统在同一时段内，多个处理器同时执行多个计算程序
* 并发：是指系统在同一时段内，交替执行多个计算程序（时间片-快速切换）
* 竞态条件：当某个计算的正确性取决于多个线程的交替执行时序时，那么就会发生竞态条件
* ThreadLocal关键字
  * 建议使用 static修饰
  * 当使用 ThreadLocal 维护变量时，其为每个使用该变量的线程提供独立的变量副本
  * 最常见的ThreadLocal使⽤场景为用来解决数据库连接、Session管理等

# Java内存模型

* 定义了共享内存系统中多线程程序读写操作行为的规范
* 定义了线程和内存的交互方式，在 JMM 抽象模型中，分为主内存（所有线程共享的）、工作内存（每个线程独有的），线程间的变量值的传递都需要通过主内存来完成
* Java内存模型定义了final、volatile和synchronized关键字的行为并确保同步的Java程序能够正确地运行在不同架构的处理器之上
* JMM 是一种规范，目的是解决由于多线程通过共享内存进行通信时，存在的本地内存数据不一致、编译器会对代码指令重排序、处理器会对代码乱序执行等带来的问题。目的是保证并发编程场景中的原子性、可见性和有序性
* 对于编译器、JVM开发者，关注点可能是如何使用类似内存屏障(Memory-Barrier)之类技术，保证执行结果符合JMM的推断
* 对于Java应用开发者，则可能更加关注volatile、synchronized等语义，如何利用类似happen-before的规则，写出可靠的多线程应用，而不是利用一些“秘籍”去糊弄编译器、JVM
* JMM内部的实现通常是依赖于所谓的内存屏障，通过禁止某些重排序的方式，提供内存可见性保证，也就是实现了各种happen-before规则

## 原子性问题

* 原子性，简单说就是相关操作不会中途被其他线程干扰，一般通过同步机制实现

* 原子性指的是一个线程更新一个共享变量，对于别的线程来说，要么完成，要么不完成，其他线程不会看到该操作执行了部分的中间效果

* 对于共享变量的读写操作，哪些是具备原子性的，哪些可能不具备原子性
* 规定对引用类型以及非long/double（因为其是64位，一个线程可能操作高32位，另一个线程操作低32位，导致结果不正确）的基本数据类型的共享变量进行读、写操作具有原子性，long/double基本数据类型可以通过volatile关键字修饰来保证原子性

* 实现
  * 使用锁：锁具有排他性，即它能够保障一个共享变量在任意一个时刻只能够被一个线程访问
  * 利用处理器提供的专门CAS（Compare-and-Swap）指令，与锁实现原子性的方式实质上是相同的，差别在于CAS是直接在硬件（处理器和内存）这一层次实现的
* volatile不能保证复合操作的原子性：
  - 对一个原子递增的操作，会分为三个步骤：
    1. 读取volatile变量的值到local;
    2. 增加变量的值;
    3. 把local的值写回让其他线程可见

## 可见性问题

* 可见性，是一个线程修改了某个共享变量，其状态能够立即被其他线程知晓
  * 通常被解释为将线程本地状态反映到主内存上，volatile就是负责保证可见性的

* 描述一个线程对共享变量的更新在什么情况下能够被其他线程所读取？
* 解决：Java内存模型使用happens-before解决
* Java语言规范保证，
  - 父线程在启动子线程之前对共享变量的更新对于子线程来说是可见的
  - 一个线程终止后该线程对共享变量的更新对于调用该线程的join方法的线程而言是可见的
* 实现
  * volatile关键字：可以保证直接从主存中读取一个变量，如果这个变量被修改后，总会被写回到主存中去
    * Java内存模型是通过在变量修改后将新值同步回主内存，在变量读取前从主内存刷新变量值，这种依赖主内存作为传递媒介的方式来实现可见性的
  * synchronized关键字：一个变量在同一个时刻只允许一条线程对其进行操作。线程A释放锁后，会把数据刷新到主内存，而线程B在获取锁后会从主内存中同步数据到本地内存
  * final关键字：被final修饰的字段在构造器中一旦被初始化完成，那么在其他线程就能看见final字段的值（无须同步）

## 有序性问题

* 有序性，是保证线程内串行语义，避免指令重排等

* 一个处理器上运行的一个线程所执行的内存访问操作在另外一个处理器上运行的其他线程看来是乱序的
* 处理器优化执行：处理器可能会对输入的代码进行乱序执行优化
* 编译器指令重排：对内存访问有关操作（读和写）所做的一种优化，它可以在不影响单线程程序正确性的情况下提升程序性能，但是可能对多线程程序的正确性产生影响，即可能导致线程安全问题
* 实现：
  * volatile关键字：volatile关键字会禁止指令重排
  * synchronized关键字：适用内存屏障保障了临界区内的任何读、写操作都无法被重排序到临界区之外

## happens-before

* 是Java内存模型中保证多线程操作可见性的机制，不仅对执行时间的保证，也包括对内存读写操作顺序的保证
  * 它的具体表现形式，包括但远不止synchronized、volatile、lock操作顺序等方面

* 假设动作A和动作B之间存在happens-before关系，那么称A happens-before B，那么Java内存模型保证A操作结果对B可见，即A操作结果会在B被执行前提交（比如写入高速缓存或者主内存）
* 具有传递性
* happens-before关系中两个动作既可以是同一个线程执行的，也可以是不同线程执行的
* Java内存模型定义了一些happens-before关系规则：
  * 程序顺序规则，一个线程内任何一个动作的结果对程序顺序上该动作之后的其他动作都是可见的
  * 内部锁规则，内部锁的释放happens-before后续每一个对该锁的申请
  * volatile变量规则，对一个volatile变量的写操作happens-before后续每一个针对该变量的读操作
  * 线程启动规则，线程T1执行过程中启动了线程T2.start()，T2能看到T1启动其之前T1所执行的任何动作
  * 线程终止规则，线程T1等待线程T2结束，T2所执行的任何动作的结果对T1中程序顺序上在T2.join()调用之后的任何一个动作是可见的，并因此是有序的
* 应用代码层次来看，可见性和有序性的保障是通过应用代码使用Java线程同步机制实现的
  * happens-before规则通过使用Java线程同步机制实现的
    * 内部锁、显示锁、volatile关键字

# 同步机制

* 从应用程序的角度来看，线程安全问题的产生是由于多线程应用

![image-20190331161303420](/Users/dingyuanjie/Library/Application Support/typora-user-images/image-20190331161303420.png)

## 锁

* 线程安全问题：多个线程并发访问共享变量、共享资源
* 锁：具有排他性，一个共享数据一次只能被一个线程访问。（同一个锁且更新读取都需加锁）
  - 保障原子性：通过互斥实现
  - 保障可见性：线程A释放锁后，会把数据刷新到主内存，而线程B在获取锁后会从主内存中同步数据到本地内存
  - 保障有序性：通过互斥实现
* 临界区：获得锁和释放锁时所执行的代码
* 特性：
  * 可重入性
    * 一个线程试图获取一个它已经获取的锁时，这个获取动作就自动成功
    * 一个线程在执行一个带锁的方法，该方法中又调用了另一个需要相同锁的方法，则该线程可以直接执行调用的方法，而无需重新获得锁
    * synchronized和ReentrantLock都是可重入锁
  * 公平和非公平
    * 公平锁就是等待时间最长的线程最优先获取锁。非公平锁无法保证锁的获取是按照请求锁的顺序进行
    * 内部锁属于非公平锁；显示锁有公平锁和非公平锁
  * 锁的粒度
    * 一个锁实例所保护的共享数据的数量大小
* 开销
  * 申请锁和释放锁产生的开销、上下文切换的开销，这些开销主要是处理器时间
  * 锁泄漏：由于程序错误导致线程获得所有无法释放，其他线程一直无法获得该锁

## 内部锁Synchronized

* 线程对内部锁的申请与释放的动作由Java虚拟机负责代为实施
* Java平台中的任何一个对象都有唯一一个与之关联的锁，即同步监视器
  * 同步监视器：阻⽌两个线程对同⼀个共享资源进行并发访问
* 加锁方式
  * 修饰代码块，指定加锁对象，进入同步代码库前要获得给定对象的锁
  * 修饰静态方法，作用于当前类对象加锁，进入同步代码前要获得当前类对象（Class对象）的锁-全局锁
  * 修饰实例方法，作用于当前实例加锁，进入同步代码前要获得当前实例的锁
* 释放对同步监视器的锁定
  - 执行结束、break、return、未处理的Error或Exception、wait()
* wait：是object的方法，线程进入阻塞状态并释放了锁
* notify：唤醒等待队列中的一个线程，使其获得锁进行访问
* notifyAll：唤醒等待队列中等待该对象锁的全部线程，让其竞争去获得锁
* sleep：是thread的静态方法，让线程进入阻塞状态，但是不释放锁（如果有），只要睡眠时间一过，就会进入运行状态
* wait、notify、notifyAll为什么需要在synchronized里面
  * wait方法释放当前的对象锁并使得当前线程进入阻塞队列， 所以wait必须先要获得一个监视器锁 
  * nofity方法获得锁后才能通知在该同步监视器锁上等待的线程
* 底层的实现：一个线程进入时，执行monitorEnter将计数器加一，释放时执行monitorExit，计数器减一。当一个线程请求锁时，判断到计数器为0，则可进入锁；反之线程进入等待
  * synchronized代码块是由一对儿monitorenter/monitorexit指令实现的，Monitor对象是同步的基本实现单元
* Synchronized关键字是对多种锁进行了封装（优化）
  * 由于不是同一个对象，所以可以多线程同时运行synchronized方法或代码段，而没有同步互斥执行
  * 如果代码块使用了全局锁，则实现了同步互斥执行的效果`synchronized (Sync.class){...}`
* 锁的升级、降级
  * JVM优化synchronized运行的机制，当JVM检测到不同的竞争状况时，会自动切换到适合的锁实现
    * 当没有竞争出现时，默认会使用偏向锁
      * JVM会利用CAS操作在对象头上的Mark Word部分设置线程ID，以表示这个对象偏向于当前线程，并不涉及真正的互斥锁
      * 基于大部分对象在其生命周期中最多会被一个线程锁定，使用偏斜锁可以降低无竞争开销
    * 当另外的线程试图锁定某个已经被偏向过的对象，JVM就撤销偏向锁，并切换到轻量级锁实现
      * 轻量级锁依赖CAS操作Mark Word来试图获取锁，如果重试成功，就使用普通的轻量级锁
      * 否则，进一步升级为重量级锁
* 优化
  * 锁消除
    * 如果同步块锁使用的锁对象通过逃逸分析技术被证实只能够被一个线程访问，那么JIT编译器在编译这个同步块的时候并不生成synchronized所表示的锁的申请与释放对应的机器码，消除了锁的使用
    * 当一个对象在方法中被定义后，它可能被外部方法所引用，称为方法逃逸，甚至还有可能被外部线程访问到，称为线程逃逸
  * 锁粗化
    * 对于相邻的几个同步块，如果这些同步块所使用的是同一个锁实例，那么JIT编译器会将这些同步块合并为一个大同步块，从而避免一个线程反复申请、释放同一个锁所导致的开销
    * 两个同步块之间的其他语句，会因为指令重排序而移到后一个同步块的临界区之中
  * 偏向锁
    * 基于大多数锁并没有被争用，并且这些锁在其整个生命周期内至多只会被一个线程持有
    * Java虚拟机会为每个对象维护一个偏好，即一个对象对应的内部锁第一次被一个线程获得，那么这个线程就会被记录为该对象的偏好线程，后续该线程申请锁和释放锁都无须借助CAS原子操作，从而减少锁申请和释放开销
    * 当其他线程申请该对象的内部锁时，Java虚拟机会回收并重新设置该对象的偏好线程
  * 适应性锁
    * 存在锁争用的情况下，一个线程申请一个锁的时候如果这个锁恰好被其他线程持有，那么这个线程就需要等待该锁被其持有的线程释放
    * Java虚拟机会根据其在运行过程中收集到的信息来判断这个锁是属于被线程持有时间较长（暂停等待策略-线程暂停）还是较短（忙等策略-反复执行空操作）来综合使用这两种策略，也可以先使用忙等策略失败后，再使用暂停等待策略

## 显示锁

* Lock接口实现类的实例
* JDK1.5开始，提供一些内部锁不具备的特性，但并不是内部锁的替代品
* lock()申请锁，unlock()释放锁（避免锁泄漏，一般放在finally块中），之间为临界区
* 使用
  - Lock接口的默认实现类ReentrantLock，通过构造函数布尔参数指定是公平锁（true）还是非公平锁（false，默认），公平锁的开销比非公平锁开销大
* 改进：读写锁`ReentrantReadWriteLock`、`rwl.readLock()`、`rwl.writeLock();`
  - 同一时刻可以允许多个线程访问共享变量，但是在写线程访问时，所有的读线程和其他写线程都会被阻塞，readLock()获取读锁和writeLock()获取写锁
  - 读锁是共享的，此时其他线程无法更新这些变量；写锁是排他的，此时其他线程无法访问该变量

## 比较

* java提供了两种内置的锁的实现，一种是由JVM实现的synchronized和JDK提供的Lock
* 内部锁是一个关键字、基于代码块的锁，在java中任意一个对象都可以成为锁，申请与释放只能在同一个方法中，简单易用且不会导致锁泄漏；显示锁是基于对象的锁，支持一个方法中申请锁，另一个方法中释放锁，容易被错用而导致锁泄漏，释放锁要放在finally块中
* 从使用上，lock具备更大的灵活性，可以控制锁的释放和获取以及中断（ lock.lockInterruptibly()）等待锁；而synchronized的锁的释放是被动的，当出现异常或者同步代码块执行完以后，才会释放锁。读和写都会用到synchronized这个锁。
* 如果一个内部锁的持有线程一直不释放这个锁，那么同步在该锁之上的所有线程就会一直被暂停；显示锁有个tryLock方法可以指定一个时间去尝试申请相应锁，没有获得锁就直接就返回false
* 内部锁仅仅支持非公平锁，显示锁支持公平锁和非公平锁

## 选用

* 内部锁简单易用，显示锁功能强大
* 一般来说，新开发代码中可以选用显示锁，但要注意：
  - 显示锁的不正确使用会导致锁泄漏这样严重的问题
  - 线程转储可能无法包含显示锁的相关信息，从而导致问题定位的困难
* 相对保守的策略，默认情况下选用内部锁，仅在需要显示锁所提供的特性的时候才选用显示锁
* 比如，在多线程持有一个锁的时间相对长或者线程申请锁的平均时间间隔相对长的情况下可考虑使用公平锁（显示锁）

## 适用场景

* 一个线程读取共享数据并在此基础上决定其下一个操作是什么
* 一个线程读取共享数据并在此基础上更新该数据
* 多个线程对多个共享数据进行更新时使用锁保障操作原子性

## Volatile

* 轻量级同步机制：volatile关键字
* 用于修饰共享可变变量，即没有使用final关键字修饰的实例变量或静态变量
* 被称为轻量级锁，其作用与锁的作用有相同的地方：保证可见性和有序性（禁止指令重排序），所不同的是，在原子性方面它仅能保障写volatile变量操作的原子性，没有锁的排他性（不能保障其他操作的原子性，如复合操作）。其次，其不会引起上下文切换
* 典型使用场景
  * 使用volatile变量作为状态标志
  * 使用volatile保障可见性。多个线程共享一个可变状态变量，其中一个线程更新了该变量之后，其他线程在无需加锁的情况下也能够看到该更新
  * 使用volatile变量替代锁。利用其写操作具有原子性，可以把一组可变状态变量封装成一个对象，并将该对象引用使用volatile修饰
* 如何利用内存屏障实现JMM定义的可见性?
  * 对于一个volatile变量:
    * 对该变量的写操作之后，编译器会插入一个写屏障。
    * 对该变量的读操作之前，编译器会插入一个读屏障。
    * 线程写入，写屏障会刷新处理器缓存让其他线程能够拿到最新数值

## 内存屏障

* 为了保障线程安全，需要使用Java线程同步机制，而内存屏障则是Java虚拟机在实现Java线程同步机制时所使用的具体"工具"，开发人员一般无须也不能直接使用内存屏障

* 是被插入到对内存读、写操作指令之间进行使用的，其作用是禁止编译器、处理器重排序从而保障有序性，还能刷新处理器缓存、冲刷处理器缓存，从而保证可见性
* 写屏障、读屏障、全屏障

## CAS

* Compare And Swap
* synchronized是悲观锁，这种线程一旦得到锁，其他需要锁的线程就挂起
* CAS操作的就是乐观锁，每次不加锁而是假设没有冲突而去完成某项操作，如果因为冲突失败就重试，直到成功为止
* 更新一个变量的时候，只有当变量的旧的预期值A和内存地址V当中的实际值相同时，才会将内存地址V对应的值修改为新值

* Java中通过锁和循环CAS的方式来实现原子操作
  * CAS基本思路就是循环进行 CAS 操作直到成功为止
* CAS是Java并发中所谓lock-free机制的基础
* ABA问题
  * 期间发生了 A -> B -> A的更新，仅仅判断数值是A，可能导致不合理的修改操作
    * Java提供了AtomicStampedReference工具类，通过为引用建立类似版本号(stamp)的方式，来保证CAS的正确性

## Atomic

* Atomic操作的底层实现正是利用的CAS机制
  * Atomic包下的类，该类实现基本数据类型以原子的方式自增自减，简单、高效且线程安全
  * Atomic一共提供了12个类分别对应四种类型的原子更新操作

## AQS

* AbstractQueuedSynchronizer(AQS)
* 其是Java并发包中，实现各种同步结构和部分其他组成单元(如线程池中的Worker)的基础
* AQS内部数据和方法，可以简单拆分为:
  * 一个volatile的整数成员表征状态，同时提供了setState和getState方法
  * 一个先入先出(FIFO)的等待线程队列，以实现多线程间竞争和等待，这是AQS机制的核心之一
  * 各种基于CAS的基础操作方法，以及各种期望具体同步结构去实现的acquire/release方法
* 利用AQS实现一个同步结构，至少要实现两个基本类型的方法，分别是acquire操作，获取资源的独占权;还有就是release操作，释放对某个资源的独占

## 对象的发布与逸出

* 对象的发布：使对象能够被其作用域之外的线程访问

* 实现对象的安全发布，选择适用且开销小的线程同步机制，如使用static关键字、final关键字、volatile关键字、AtomicReference等来引用该对象的变量，也可对访问该对象的代码进行加锁

# 线程间协作

* 共享内存-隐式通信
  * 在共享内存的并发模型⾥面，线程之间共享的是一些公共状态，通过写或者读内存的公共状态进行一个隐式通信
  * 通过线程同步机制，如锁、volatile关键字
* 消息传递-显示通信
  * 显示调用wait()、notify()、notifyall()
* 同步：指的程序中⽤于控制不同线程之间操作发生的相对顺序的机制

## wait/notify

* 等待与通知：使用Java中的任何对象都能够实现等待与通知

  * 等待：线程被暂停
    * Object.wait()在暂停其执行线程的同时必须释放相应的内部锁，否则通知线程无法获得相应的内部锁，也就无法执行相应对象的notify方法来通知等待线程
  * 通知：线程被唤醒
    * 比较流行的保守性方法是优先使用Object.notifyAll()以保障正确性

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

## Java条件变量

* Condition接口可作为wait/notify的替代品来实现等待/通知，为解决过早唤醒问题提供支持，并解决了Object.wait(long)不能区分其返回是否是由等待超时而导致的问题

  * await、signal、signalAll相当于wait、notify、notifyAll

  ```java
  private final Lock lock = new ReentrantLock();
  private final Condition condition = lock.newCondition();
  ```
  
* Condition将wait、notify、notifyAll等操作转化为相应的对象，将复杂而晦涩的同步操作转变为直观可控的对象行为 

  * 条件变量最为典型的应用场景就是标准类库中的ArrayBlockingQueue等

## CountDownLatch

* 倒计时协调器

* 一个同步工具类，它允许一个或多个线程一直等待，直到其他线程的操作执行完毕再执行

* 内部会维护一个用于表示未完成的先决操作数量的计数器

  * 当计数器值不为0时，CountDownLatch.await()/await(long)的执行线程会被暂停
  * CountDownLatch.countDown()相当于一个通知方法，它会在计数器值达到0的时候唤醒相应实例上的所有等待线程
  * 无须加锁

* 一个CountDownLatch实例只能够实现一次等待和唤醒

* 使用场景：

  - 通过countdownlatch实现最大的并行请求，也就是可以让N个线程同时执行
  - 比如应用程序启动之前，需要确保相应的服务已经启动

* 调用thread.join() 方法必须等thread 执行完毕，当前线程才能继续往下执行，⽽CountDownLatch通过计数器提供了更灵活的控制，只要检测到计数器为0当前线程就可以往下执行⽽不用管相应的thread是否执行完毕

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

## Semaphore

* 信号灯，semaphore可以控制同时访问的线程个数，通过acquire获取一个许可，如果没有就等待，通过release释放一个许可（应该放在finally块中），并唤醒相应Semaphore实例的等待队列中的一个任意等待线程（采用非公平性调度策略）

* 由于一个线程可以在未执行过Semaphore.acquire()的情况下执行相应的Semaphore.release()，因此这种互斥锁(许可为1)允许一个线程释放另外一个线程锁所持有的锁

* 使用场景

  * 可以实现对某些接口访问的限流

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

## 栅栏CyclicBarrier

* 允许多个线程等待到达某个屏障

* 多个线程可能需要相互等待对方执行到代码中的某个地方，这时这些线程才能够继续执行
* 参与方（等待的线程）只需执行CyclicBarrier.await()就可以实现等待，其既是等待方法又是通知方法。CyclicBarrier实例的所有参与方除最后一个线程外都相当于等待线程，最后一个线程则相当于通知线程
* 与CountDownLatch对比
  * CountDownLatch不可重置、无法重用；CyclicBarrier可重置可重用
  * CountDownLatch的基本操作组合是countDown/await
    * 调用await的线程阻塞等待countDown足够的次数，不管是在一个线程还是多个线程里countDown，只要次数足够即可 。CountDownLatch操作的是事件
  * CyclicBarrier的基本操作组合，则就是await，当所有的伙伴都调用了await，才会继续进行任务，并自动进行重置
    * CyclicBarrier侧重点是线程，而不是调用事件，它的典型应用场景是用来等待并发线程结束
* 主要开销在可能产生的上下文切换
* 典型应用场景
  * 使迭代算法并发化，在并发的迭代算法中，迭代操作是由多个工作者线程并行执行的，中间结果作为下一轮迭代的基础（输入）
  * 模拟高并发
* 如果代码对CyclicBarrier.await()调用不是放在一个循环之中，并且使用CyclicBarrier的目的也不是为了模拟高并发操作，那么此时对CyclicBarrier的使用可能是一种滥用

```java
public class CyclicBarrierTest2 {

    private static final int count = 12;
    final CyclicBarrier cyclicBarrier = new CyclicBarrier(3, new Runnable() {
        @Override
        public void run() {
            System.out.println("已有三位，准备放行！");
        }
    });

    public static void main(String[] args) {
        CyclicBarrierTest2 test = new CyclicBarrierTest2();
        for (int i = 0; i < count; i++) {
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        test.goHome();
                    } catch (BrokenBarrierException e) {
                        e.printStackTrace();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }, "学生" + i).start();
        }
    }

    public void goHome() throws BrokenBarrierException, InterruptedException {
        System.out.println(Thread.currentThread().getName() + "已准备好！");
        cyclicBarrier.await();
        System.out.println(Thread.currentThread().getName() + "已离开！");
    }
}
```

## 生产者-消费者模式

* 生产者和消费者是并发地运行在各自的线程之中，可以使程序中原本串行的处理得以并发化
* 生产者-传输通道-消费者
  * 传输通道通常可以使用一个线程安全的队列来实现
  * 阻塞队列
    * 即从传输通道中存入一个产品或者取出一个产品时，相应的线程可能因为传输通道中没有产品或者其存储空间已满而被阻塞（暂停）
      * Blocking意味着其提供了特定的等待性操作，获取时(take)等待元素进队，或者插
        入时(put)等待队列出现空位
    * BlockingQueue是一种线程安全的阻塞队列，基本都是基于锁实现，实现类
      * ArrayBlockingQueue	
        * 有界队列，内部的数组存储空间是预先分配的，其put操作、take操作本身不会增加垃圾回收的负担但是使用的是同一个锁（显示锁），增加了锁争用可能导致较频繁的上下文切换
      * LinkedBlockingQueue
        * 构造函数中指定容量时是有界队列，否则是无界队列，内部的链表存储空间动态分配，put和take操作可能增加垃圾回收的负担，但是使用了两个显示锁（putLock、takeLock）降低了锁争用
        * 如果生产者线程和消费者线程之间的并发程度比较大，有界队列的实现适合选用LinkedBlockingQueue，否则考虑使用ArrayBlockQueue
      * SynchronousQueue
        * 特殊的有界队列，内部并不维护用于存储队列元素的存储空间，生产者线程生产好一个产品之后，会等待消费者线程来取走这个产品才继续生产下一个产品
        * 每个删除操作都要等待插入操作，反之每个插入操作也都要等待删除动作
        * 适合于在消费者处理能力与生产者处理能力相差不大的情况下使用，避免较多的线程等待导致上下文切换
      * PriorityBlockingQueue
        * 无边界的优先队列
      * 
    * 阻塞队列也支持非阻塞操纵，即不会导致执行线程被暂停，比如，BlockingQueue接口定义的offer(E)-（返回false表示入队列失败-队列已满）和poll()-（返回null表示队列为空）分别相当于put(E)和take()的非阻塞版
  * 管道
    * 线程间的直接输出和输入，PipedOutputStream和PipedInputStream
    * 适合在两个线程间使用，即适用于单生产者-单消费者情形
    * 输出异常的处理，生产者线程出现异常时需要通过关闭PipedOutputStream.close()实例来实现通知相应的消费者线程，否则消费者线程可能会无限制地等待新的数据
  * 双缓冲与Exchange
    * 当消费者线程消费一个已填充的缓冲区时，另外一个缓冲区可以由生产者线程进行填充，从而实现了数据生成与消费的并发
    * Exchanger可以用来实现双缓冲

## 线程停止与中断机制

* 主线程调⽤⼦线程的stop()方法进行强制中断子线程

* 线程中断标记表示是否收到中断通知

  * 调用一个线程的`interrupt()`相当于将该线程（目标线程）的中断标记置为true，表示向当前线程打个招呼，告诉他可以中断线程的执行了，至于什么时候中断，取决于当前线程自己（有机会去清理资源，而不是武断地将线程停止）
  * 目标线程可以通过`Thread.currentThread().isInterrupted()`调用来获取该线程的中断标记值
  * 也通过`Thread.interrupted()`来获取并重制中断标记值为false

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

  * 应用了volatile 能够实现多线程之间共享变量的可见性
  * 主内存和⼯作内存，直接与主内存产⽣交互，进行读写操作，保证可见性

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

* `Thread.interrupted`
  * 静态方法`Thread.interrupted()`对设置中断标识的线程复位，设置成false
* 被动复位
  * JVM 会先把线程的中断标识位清除（设置成false），然后才会抛出`InterruptedException`
  * 目标线程可能因为执行CountDownLatch.await()、CyclicBarrier.await()以及ReentrantLock.lockInterruptibly()能够响应中断的阻塞方法/操作而被暂停时，发起线程会给这些方法的执行线程发送中断会导致Java虚拟机将相应的线程唤醒并使其抛出InterruptedException。可见，给目标线程发送中断还能够产生唤醒目标线程的效果

# 死锁

* 一般指两个或多个线程之间，由于互相持有对方需要的锁，而永久处于阻塞的状态
  * 只能重启、修正程序本身问题

* 描述：
  
  - 线程a，需要操作x、y两个文件，在已获得x文件的操作权限后，由于线程b在使用y资源，故线程a进入阻塞状态，但是x资源并未释放，此时线程b又需要使用x资源，也进入了阻塞状态，此时这两线程就会进入死锁状态
  
* 定位死锁

  * 利用jstack等工具获取线程栈，然后定位互相之间的依赖关系，进而找到死锁
    * jps确定进程ID
    * 调用jstack获取线程栈，`${JAVA_HOME}\bin\jsack your_pid`
    * 结合代码分析线程栈信息，找到处于BLOCKED状态的线程

* 如何避免：
  
  * 尽量避免使用多个锁，并且只有需要时才持有锁
  * 可以指定获取锁的顺序
    * 只有在获取x资源的锁之后才能获取y资源的锁
  * 使用带超时的方法，为程序带来更多可控性
  
  ```java
  public class DeadLockSample extends Thread {
  	private String frs;
  	private String second;
  	public DeadLockSample(String name, String frs, String second) {
  		super(name); 
      this.frs = frs; 
      this.second = second;
  	}
  	public void run() { 
      synchronized (frs) {
  			Sysem.out.println(this.getName() + " obtained: " + frs); 
        try {
  				Thread.sleep(1000L); 
          synchronized (second) {
  					Sysem.out.println(this.getName() + " obtained: " + second); 
          }
  			} catch (InterruptedException e) { 
          // Do nothing
  			} 
      }
  	}
    public satic void main(String[] args) throws InterruptedException {
  		String lockA = "lockA";
  		String lockB = "lockB";
  		DeadLockSample t1 = new DeadLockSample("Thread1", lockA, lockB); 
      DeadLockSample t2 = new DeadLockSample("Thread2", lockB, lockA); 
      t1.sart();
  		t2.sart();
  		t1.join();
  		t2.join();
  	}
  }
  ```

# Java异步编程

* 同步方式和异步方式是相对的，如果将该任务提交给线程池执行，从任务提交线程的角度来看则为异步任务，而从线程池中的工作者线程（实际执行该任务的线程）的角度来看该任务则可能是一个同步任务
* 同步任务，任务的发起与任务的执行是串行的
  * 任务的发起线程会被暂停（阻塞），直到任务执行结束才能够执行其他操作
  * 同步任务执行方式多数情况下意味着阻塞，但是也可能意味着非阻塞（轮询）
  * 同步任务的发起线程是采用阻塞的方式还是轮询方式来等待任务的结束很大程度上取决于所使用的API
    * 阻塞，同步任务执行结束前，该任务的发起线程并没有在运行
    * 轮询，同步任务执行结束前，该线程的主要动作是不断地检查相应的任务是否执行结束，如使用java.nio.channels.Selector类来编写网络应用程序的服务端代码
* 异步任务，任务的发起与任务的执行是并发的，而不是串行的
  * 多线程编程本质上是异步的
  * 如一个线程向线程池提交一个任务后便执行其他操作，该任务可能在此后才被线程池中的某一个工作者线程所执行
  * 异步任务执行方式多数情况下意味着非阻塞，但是也可能意味着阻塞，如向线程池提交一个任务后立刻调用Future.get()来阻塞获取该任务的处理结果
* 异步任务的批量执行
  * Future可以获取任务执行的结果，而completionservice，可以一次性提交一批任务执行，并返回所有结果

# 线程池

* Java并发类库提供的线程池有哪几种? 分别有什么特点?
  * 通常开发者都是利用Executors提供的通用线程池创建方法，去创建不同配置的线程池，主要区别在于不同的ExecutorService类型或者不同的初始参数

* 线程的高效利用，Java1.5中引入的Executor框架

* 把任务的提交和执行进行解耦，只需要定义好任务，然后提交给线程池，而不用关心该任务是如何执行、被哪个线程执行，以及什么时候执行；负责执行任务的线程的生命周期都由Executor框架进行管理

* 优点：

  * 能重用存在的线程，降低创建线程和销毁线程的性能开销
  * 可以控制可以创建、 执行的最⼤并发线程数，避免导致OOM
  * 提供定期执⾏、并发数控制功能

* 线程池大小的选择策略

  * 如果任务主要是进行计算，意味着CPU的处理能力是稀缺的资源，通常建议按照CPU核的数目N或者N+1
  * 如果是需要较多等待的任务，例如I/O操作比较多，线程数 = CPU核数 × (1 + 平均等待时间/平均工作时间)

* runnable和callable接口，都是对任务处理逻辑的抽象

* Executor接口则是对事务的执行进行抽象

  - 将任务提交给Executor接口执行，执行方式为同步执行
  - 将任务提交给ThreadPoolExecutor执行，则为异步执行

* `Executor` - `ExecutorService`-`AbstractExecutorService`-`ThreadPoolExecutor`

* Executor是一个顶层接口，只声明了一个⽅法`void execute(Runnable)`，空闲时无法主动释放内部工作者线程所占用的资源，其初衷是将任务提交和任务执行细节解耦

* ExecutorService接口继承了Executor接口，新增了submit（可以返回future执行结果）、shutdown方法，默认实现类为ThreadPoolExecutor

* 抽象类AbstractExecutorService实现了ExecutorService接口，基本实现了
  ExecutorService中声明的所有⽅法

* 然后ThreadPoolExecutor继承了类AbstractExecutorService

  * execute()，向线程池提交一个Runnable任务
  * submit()，向线程池提交Runnable或Callable任务，利用了Future来获取任务执行结果
  * shutdown()，等待队列中的任务都执行完后才终止
  * shutdownNow()，立即终止任务，清空任务队列

* 通过`Executors`工厂类可以创建特定功能的线程池，其本质是通过不同的参数初始化一个`ThreadPoolExecutor`对象

  * 固定型线程池newFixedThreadPool
    * 返回一个固定数量的线程池，核心线程数和最大线程数都是指定值，当线程池中的线程数超过核心线程数后，任务都会被放到阻塞队列中（LinkedBlockingQueue，容量非常大），线程池没有可执行任务时，也不会释放线程。`Executors.newFixedThreadPool(10)`
    * 适用场景：
      * 用于负载比较大的服务器，为了资源的合理利用，需要限制当前线程数量。必须在不再需要该线程池时主动将其关闭
  * 单例线程池newSingleThreadExecutor
    * 一个线程的线程池，保证所有任务按照指定顺序(FIFO, LIFO, 优先级)执行。若空闲则执行，若没有空闲线程则暂缓在任务队列中。内部使用LinkedBlockingQueue作为阻塞队列
    * 适用场景：
      * 适合用来实现单（多）生产者-单消费者模式
  * 缓存线程池newCachedThreadPool
    * 没有核心线程，直接向 SynchronousQueue 中提交任务，如果有空闲线程，就去取出任务执行;如果没有空闲线程，就新建一个， 线程数可达到Integer.MAX_VALUE(2的31次方减1)，执行完任务的线程空闲60秒后被回收
    * 适用场景：
      * 用来执行大量耗时较短且提交频率较高的任务
  * 调度型线程池newScheduledThreadPool
    * 可以指定线程的数量并带有延迟和周期性执行任务的功能
      * 周期性地执行提交的任务`scheduleAtFixedRate`
      * 延迟执行提交的任务`schedule`
    * 适用场景
      * 根据Scheduled(任务列表)进行延迟执行，或者是进行周期性的执行。适用于一些周期性的工作
      * 典型的计划任务有：清理系统的垃圾数据、系统监控和数据备份等
  * newWorkStealingPool(int parallelism)
    * Java 8，并行地处理任务，不保证处理顺序

* `ThreadpoolExecutor`，提供了四个构造方法

  * corePoolSize
    * 线程池中的核心线程数，当前线程数为corePoolSize时，继续提交的任务被保存到阻塞队列中，等待被执行
  * maximumPoolSize
    * 线程池中允许的最大线程数，当前阻塞队列满了且线程数小于maximumPoolSize，则继续创建新线程执行任务，否则执行饱和策略
  * keepAliveTime
    * 线程空闲时的存活时间，默认情况下，该参数只在线程数大于corePoolSize时才有用
  * unit
    * keepAliveTime的单位
  * workQueue
    * 用来保存等待被执行的任务的阻塞队列，且任务必须实现Runable接口
      * ArrayBlockingQueue：基于数组结构的有界阻塞队列，按FIFO排序任务
      * LinkedBlockingQuene：基于链表结构的阻塞队列，按FIFO排序任务，吞吐量通常要高于ArrayBlockingQuene；
      * SynchronousQuene：一个不存储元素的阻塞队列，每个插入操作必须等到另一个线程调用移除操作，否则插入操作一直处于阻塞状态，吞吐量通常要高于LinkedBlockingQuene
      * priorityBlockingQuene：具有优先级的无界阻塞队列；
  * threadFactory
    * 创建线程的工厂，通过自定义的线程工厂可以给每个新建的线程设置一个具有识别度的线程名
  * handler
    * 线程池的饱和策略
      * AbortPolicy：直接抛出异常，默认策略
      * CallerRunsPolicy：用调用者所在的线程来执行任务
      * DiscardOldestPolicy：丢弃阻塞队列中靠最前的任务，并执行当前任务
      * DiscardPolicy：直接丢弃任务

* 线程池状态

  * RUNNING-创建线程池时，SHUTDOWN-调用了shutdown()方法，STOP-调用了shutdownNow()方法，TERMINATED-处于SHUTDOWN或STOP状态并且所有工作线程已经销毁，任务缓存队列已经清空或执行结束后的状态

* 创建线程的逻辑

  - 以下任务提交逻辑来自ThreadPoolExecutor.execute方法，任务提交给线程池之后的处理策略：

  1. 如果运行的线程数 < corePoolSize，直接创建新线程去执行这个任务，即使有其他线程是空闲的
  2. 如果运行的线程数 >= corePoolSize，则每来一个任务，会尝试将其添加到任务缓存队列当中 
     1. 如果插入队列成功，则完成本次任务提交，该任务会等待空闲线程将其取出去执行 
     2. 如果插入队列失败，一般来说是任务缓存队列已满，则会尝试创建新的线程去执行这个任务
     3. 如果当前线程数 < maximumPoolSize，创建新的线程放到线程池中 
     4. 如果当前线程数 >= maximumPoolSize，会执行指定的拒绝策略
  3. 如果线程池中的线程数量⼤于 corePoolSize时，如果某线程空闲时间超过keepAliveTime，线程将被终止，直⾄线程池中的线程数目不大于corePoolSize；如果允许为核⼼池中的线程设置存活时间，那么核心池中的线程空闲时间超过keepAliveTime，线程也会被终⽌

* 任务执行

  * ExecutorService接口对象来执行任务
  * execute只能接受Runnable类型的任务，不能获取返回值，因此无法判断任务是否执行成功
  * submit接受Runnable或Callable任务，返回一个Future对象，通过future的get方法来获取返回值（包括正常返回结果或异常），Runnable返回值均为void，所以使用Future的get()获得的还是null。get方法会阻塞住直到任务完成
  * 异步任务的批量执行
    * CompletionService接口：提供异步任务的批量提交一级获取这些任务的处理结果

* FutureTask

  * Runnable实例或Callable实例交给线程池执行就是异步任务
    * Runnable任务可以交给一个专门的工作者线程执行或线程池执行，但是无法直接获取任务执行结果
    * Callable任务只能交给线程池执行，能获取任务的处理结果
  * FutureTask类结合了Runnable接口和Callable接口的优点
    * FutureTask是Runnable接口的一个实现类，可以交给一个专门的工作者线程执行或线程池执行，能够返回其代表的异步任务的处理结果
    * `ThreadPoolExecutor.submit(Callable<T> task)`的返回值就是一个FutureTask实例





