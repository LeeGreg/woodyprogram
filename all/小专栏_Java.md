# Readme

```java
// 必看
https://juejin.im/post/5b55b842f265da0f9e589e79
https://segmentfault.com/a/1190000012650596
https://blog.csdn.net/qq_34337272/article/details/81252853
https://blog.csdn.net/qq_34337272/article/details/80611486
https://segmentfault.com/a/1190000006158186
https://mp.weixin.qq.com/s?__biz=Mzg2OTA0Njk0OA==&mid=2247485097&idx=1&sn=84c89da477b1338bdf3e9fcd65514ac1&chksm=cea24962f9d5c074d8d3ff1ab04ee8f0d6486e3d015cfd783503685986485c11738ccb542ba7&token=79317275&lang=zh_CN#rd
https://mp.weixin.qq.com/s?__biz=Mzg2OTA0Njk0OA==&mid=2247485117&idx=1&sn=92361755b7c3de488b415ec4c5f46d73&chksm=cea24976f9d5c060babe50c3747616cce63df5d50947903a262704988143c2eeb4069ae45420&token=79317275&lang=zh_CN#rd
https://mp.weixin.qq.com/s?__biz=Mzg2OTA0Njk0OA==&mid=2247485185&idx=1&sn=66ef08b4ab6af5757792223a83fc0d45&chksm=cea248caf9d5c1dc72ec8a281ec16aa3ec3e8066dbb252e27362438a26c33fbe842b0e0adf47&token=79317275&lang=zh_CN#rd
https://github.com/Snailclimb/JavaGuide/blob/master/docs/database/Redis/Redis%E6%8C%81%E4%B9%85%E5%8C%96.md
https://blog.csdn.net/zeb_perfect/article/details/54135506
https://www.jianshu.com/p/8bddd381de06
https://xiaozhuanlan.com/topic/9546203817
https://www.cnblogs.com/zrtqsk/p/3735273.html
https://mp.weixin.qq.com/s?__biz=Mzg2OTA0Njk0OA==&mid=2247485303&idx=1&sn=9e4626a1e3f001f9b0d84a6fa0cff04a&chksm=cea248bcf9d5c1aaf48b67cc52bac74eb29d6037848d6cf213b0e5466f2d1fda970db700ba41&token=255050878&lang=zh_CN#rd
https://github.com/Snailclimb/springboot-integration-examples/blob/master/md/springboot-dubbo.md
https://mp.weixin.qq.com/s?__biz=Mzg2OTA0Njk0OA==&mid=2247484766&idx=1&sn=9823b8378c5378280d826547b1ae64a3&chksm=cea24a95f9d5c38314de52cc2e1229dd530f523669ba401db3ebbdcd0b61f2505f1cd1c1443f&token=1883509977&lang=zh_CN#rd
https://blog.csdn.net/qiangcuo6087/article/details/79067136
```

# Java基础

* JDK、JRE、JVM、字节码
  * 字节码：在 Java 中，JVM可以理解的代码，即扩展名为 `.class` 的文件，只面向虚拟机
  * JVM：是运行 Java 字节码的虚拟机，有针对不同系统的特定实现（Linux、Windows），目的是使用相同的字节码，它们都会给出相同的结果
    * 字节码和不同系统的 JVM 实现是 Java 语言“一次编译，随处可以运行”的关键所在
  * JDK：拥有JRE所拥有的一切，还有编译器（javac）和工具（如javadoc和jdb）。它能够创建和编译程序
  * JRE 是 Java运行时环境。它是运行已编译 Java 程序所需的所有内容的集合，包括 Java虚拟机（JVM），Java类库，java命令和其他的一些基础构件。但是，它不能用于创建新程序
  * Java程序运行过程：
    * .java文件（源代码）通过JDK中的javac编译成.class文件（字节码文件，JVM可以理解）， JVM 类加载器首先加载字节码文件，然后通过解释器逐行解释执行
    *  JIT 编译器：有些方法和代码块是经常需要被调用的(也就是所谓的热点代码)，JIT 属于运行时编译，当 JIT 编译器完成第一次编译后，其会将字节码对应的机器码保存下来，下次可以直接使用
      * Java 是编译与解释共存的语言：机器码的运行效率肯定是高于 Java 解释器的
* Java和C++的区别
  * 都是面向对象的语言，都支持封装、继承和多态
  * Java 不提供指针来直接访问内存，程序内存更加安全
  * Java 的类是单继承的，C++ 支持多重继承；虽然 Java 的类不可以多继承，但是接口可以多实现
  * Java 有自动内存管理机制，不需要程序员手动释放无用内存
* 重载和重写
  * 重载：同一个类中，方法名必须相同，参数类型不同、个数不同、顺序不同，方法返回值和访问修饰符可以不同，发生在编译时
  * 重写：父子类中，方法名、参数列表必须相同，访问修饰符范围大于等于父类，返回值范围小于等于父类，抛出的异常范围小于等于父类；如果父类方法访问修饰符为 private 则子类就不能重写该方法
* 封装、继承、多态
  * 封装：把一个对象的属性私有化，同时提供一些可以被外界访问的属性的方法
  * 继承：使用已存在的类的定义作为基础建立新类，能够非常方便地复用以前的代码
    * 子类拥有父类对象所有的属性和方法（包括私有属性和私有方法），但是父类中的私有属性和方法子类是无法访问，只是拥有
  * 多态：运行期间才能决定一个引用变量到底会指向哪个类的实例对象
    * 在Java中有两种形式可以实现多态：继承（多个子类对同一方法的重写）和接口（实现接口并覆盖接口中同一方法）
* String
  * String 类中使用 final 关键字修饰字符数组来保存字符串，所以 String 对象是不可变的
  * StringBuffer 对方法加了同步锁或者对调用的方法加了同步锁，所以是线程安全的。StringBuilder 并没有对方法进行加同步锁，所以是非线程安全的
* ==和equals
  * ==， 基本数据类型比较的是值，引用数据类型比较的是内存地址
  * equals，类没有覆盖 equals() 方法则等价于==；类覆盖了 equals() 方法则可用于比较内容相等
* hashCode和equals
  * hashCode() 的作用是获取哈希码，也称为散列码；它实际上是返回一个int整数，确定该对象在哈希表中的索引位置
    * 先通过比较hashCode()，可以减少了 equals 的次数来提高执行速度
  * hashCode() 的默认行为是对堆上的对象产生独特值。如果没有重写 hashCode()，则该 class 的两个对象无论如何都不会相等（即使这两个对象指向相同的数据）
* 反射
  * 能够在运行时获取类的信息以及动态调用对象的方法
* IO流
  * 按照流的流向分，可以分为输入流和输出流
  * 按照操作单元划分，可以划分为字节流和字符流
  * 按照流的角色划分为节点流和处理流
    * 节点流：如FileInputStream
    * 处理流：如BufferedInputStream
  * InputStream/Reader: 所有的输入流的基类，前者是字节输入流，后者是字符输入流
  * OutputStream/Writer: 所有输出流的基类，前者是字节输出流，后者是字符输出流
* BIO、NIO、AIO
  * **BIO (Blocking I/O):** 同步阻塞I/O模式，数据的读取写入必须阻塞在一个线程内等待其完成
  * **NIO (New I/O):** NIO是一种同步非阻塞的I/O模型
  * **AIO (Asynchronous I/O):** 异步非阻塞的IO模型。异步 IO 是基于事件和回调机制实现的，即应用操作之后会直接返回，不会堵塞在那里，当后台处理完成，操作系统会通知相应的线程进行后续的操作
* ArrayList和LinkedList的区别
  * ArrayList 和 LinkedList 都是不同步的，即不保证线程安全
  *  Arraylist 底层使用的是Object数组；LinkedList 底层使用的是双向链表数据结构
  * ArrayList默认末尾插入时间复杂度就是O(1)，指定位置 i 插入和删除元素时间复杂度就为 O(n-i)； LinkedList 采用链表存储，所以插入，删除元素时间复杂度不受元素位置的影响，都是近似 O（1）
  * LinkedList 不支持高效的随机元素访问，而 ArrayList 支持
  *  ArrayList的空间浪费主要体现在在list列表的结尾会预留一定的容量空间，而LinkedList因为要存放直接后继和直接前驱以及数据，它的每一个元素都需要消耗比ArrayList更多的空间
* HashMap和HashTable的区别
  * HashMap 是非线程安全的，HashTable 是线程安全的；HashTable 内部的方法基本都经过`synchronized` 修饰，因为线程安全的问题，HashMap 要比 HashTable 效率高一点
  *  HashMap 中，null 可以作为键，而HashTable不可以
  * Hashtable 默认的初始大小为11，之后每次扩充，容量变为原来的2n+1；HashMap 默认的初始化大小为16。之后每次扩充，容量变为原来的2倍；
  *  JDK1.8，HashMap链表长度大于阈值（默认为8）时，将链表转化为红黑树，以减少搜索时间。Hashtable 没有这样的机制
* HashSet如何检查重复
  * 先计算对象的hashcode值来判断对象加入的位置并与其他加入的对象的hashcode值作比较，如果发现有相同hashcode值的对象，这时会调用equals（）方法来检查hashcode相等的对象是否真的相同。如果两者相同，HashSet就不会让加入操作成功
* HashMap的底层实现
  * jdk8之前
    * 底层是数组和链表结构，HashMap 通过 key 的 hashCode 经过扰动函数（可减少碰撞）处理过后得到 hash 值，然后通过 (数组的长度 - 1) & hash 判断当前元素存放的位置，如果当前位置存在元素的话，就判断该元素与要存入的元素的 hash 值以及 key 是否相同，如果相同的话，直接覆盖，不相同就通过拉链法解决冲突
      * 拉链法：将链表和数组相结合，数组中每一格就是一个链表。若遇到哈希冲突，则将冲突的值加到链表中即可
  * jdk8之后
    * 解决哈希冲突时，当链表长度大于阈值（默认为8）时，将链表转化为红黑树，以减少搜索时间
    * [HashMap](https://zhuanlan.zhihu.com/p/21673805)
  * HashMap 的长度为什么是2的幂次方
    * 取余(%)操作中如果除数是2的幂次则等价于与其除数减一的与(&)操作（也就是说 hash%length==hash&(length-1)的前提是 length 是2的 n 次方；）。并且采用二进制位操作 &，相对于%能够提高运算效率
  * HashMap多线程操作导致死循环
    * [原因](https://coolshell.cn/articles/9606.html)在于并发下的Rehash 会造成元素之间会形成一个循环链表
* ConcurrentHashMap
  * jdk1.7
    * 将数据分为一段一段的存储，然后给每一段数据配一把锁，当一个线程占用锁访问其中一个段数据时，其他段的数据也能被其他线程访问
      * **由 Segment 数组结构和 HashEntry 数组结构组成**
      * Segment 实现了 ReentrantLock,所以 Segment 是一种可重入锁，扮演锁的角色。HashEntry 用于存储键值对数据
    * 一个 ConcurrentHashMap 里包含一个 Segment 数组
      * Segment 的结构和HashMap类似，是一种数组和链表结构，一个 Segment 包含一个 HashEntry 数组，每个 HashEntry 是一个链表结构的元素，每个 Segment 守护着一个HashEntry数组里的元素，当对 HashEntry 数组的数据进行修改时，必须首先获得对应的 Segment的锁
  * jdk1.8
    * 取消了Segment分段锁，采用CAS和synchronized来保证并发安全。数据结构跟HashMap1.8的结构类似，数组+链表/红黑二叉树。Java 8在链表长度超过一定阈值（8）时将链表（寻址时间复杂度为O(N)）转换为红黑树（寻址时间复杂度为O(long(N))）
    * synchronized只锁定当前链表或红黑二叉树的首节点，这样只要hash不冲突，就不会产生并发，效率又提升N倍
* ConcurrentHashMap与HashTable
  * JDK1.7的 [ConcurrentHashMap](http://www.cnblogs.com/chengxiao/p/6842045.html) 底层采用 **分段的数组+链表** 实现，JDK1.8 采用的数据结构跟HashMap1.8的结构一样，数组+链表/红黑二叉树。Hashtable 和 JDK1.8 之前的 HashMap 的底层数据结构类似都是采用 **数组+链表** 的形式，数组是 HashMap 的主体，链表则是主要为了解决哈希冲突
  * **在JDK1.7的时候，ConcurrentHashMap（分段锁）** 对整个桶数组进行了分割分段(Segment)，每一把锁只锁容器其中一部分数据，多线程访问容器里不同数据段的数据，就不会存在锁竞争，提高并发访问率
  * **JDK1.8 的时候已经摒弃了Segment的概念，而是直接用 Node 数组+链表+红黑树的数据结构来实现，并发控制使用 synchronized 和 CAS 来操作**
  * **Hashtable(同一把锁)** 

# Java并发

* 线程

  * 多个线程共享进程的**堆**和**方法区**(**JDK1.8 元空间**)资源，但每个线程有自己的**程序计数器**、**虚拟机栈**和**本地方法栈**
    * 程序计数器私有主要是为了**线程切换后能恢复到正确的执行位置**
    * 虚拟机栈和本地方法栈是线程私有是为了**保证线程中的局部变量不被别的线程访问到**
      * **虚拟机栈：**每个 Java 方法在执行的同时会创建一个栈帧用于存储局部变量表、操作数栈、常量池引用等信息。从方法调用直至执行完成的过程，就对应着一个栈帧在 Java 虚拟机栈中入栈和出栈的过程
      * **本地方法栈：**和虚拟机栈所发挥的作用非常相似，区别是： **虚拟机栈为虚拟机执行 Java 方法 （也就是字节码）服务，而本地方法栈则为虚拟机使用到的 Native 方法服务。** 在 HotSpot 虚拟机中和 Java 虚拟机栈合二为一
    * 堆和方法区是所有线程共享的资源，其中堆是进程中最大的一块内存，主要用于存放新创建的对象 (所有对象都在这里分配内存)，方法区主要用于存放已被加载的类信息、常量、静态变量、即时编译器编译后的代码等数据

* 并发与并行

  * **并发：** 同一时间段，多个任务都在执行 (单位时间内不一定同时执行)；
  * **并行：**单位时间内，多个任务同时执行。

* 为什么使用多线程

  * 线程间的切换和调度的成本小于进程
  * 多核 CPU 时代意味着多个线程可以同时运行，这减少了线程上下文切换的开销
  * 利用好多线程机制可以大大提高系统整体的并发能力以及性能
  * 在单核时代多线程主要是为了提高 CPU 和 IO 设备的综合利用率
    * 当一个线程执行 CPU 计算时，另外一个线程可以进行 IO 操作
  * 多核时代多线程主要是为了提高 CPU 利用率
    * 创建多个线程就可让多个 CPU 核心被利用来提高 CPU 的利用率

* 多线程问题

  * 内存泄漏、上下文切换、死锁还有受限于硬件和软件的资源闲置问题

* 线程生命周期和状态

  * NEW，被创建还没调用start()
  * RUNNABLE，运行中，包括就绪和运行（获取CPU时间片）
  * BLOCKED，线程阻塞于锁
  * WAITING，当前线程需等待其他线程作出一些动作（中断或通知）
  * TIME_WAITING，超时等待，可在指定时间内自行返回
  * TERMINATED，终止，线程执行完毕

* 上下文切换

  * 一个 CPU 核心只能被一个线程使用，CPU 为每个线程分配时间片并轮转的形式让这些线程都能得到有效执行，切换会消耗大量的 CPU 时间

* 线程死锁

  * 多个线程同时被阻塞，它们中的一个或者全部都在等待某个资源被释放
  * 如何避免
    * 一次性申请所有的资源
    * 占用部分资源的线程进一步申请其他资源时，如果申请不到，可以主动释放它占有的资源
    * 靠按序申请资源来预防。按某一顺序申请资源，释放资源则反序释放

  ```java
  public class DeadLockDemo {
      private static Object resource1 = new Object();//资源 1
      private static Object resource2 = new Object();//资源 2
  
      public static void main(String[] args) {
          new Thread(() -> {
              synchronized (resource1) {
                  System.out.println(Thread.currentThread() + "get resource1");
                  try {
                      Thread.sleep(1000);
                  } catch (InterruptedException e) {
                      e.printStackTrace();
                  }
                  System.out.println(Thread.currentThread() + "waiting get resource2");
                  synchronized (resource2) {
                      System.out.println(Thread.currentThread() + "get resource2");
                  }
              }
          }, "线程 1").start();
  
          new Thread(() -> {
              synchronized (resource2) {
                  System.out.println(Thread.currentThread() + "get resource2");
                  try {
                      Thread.sleep(1000);
                  } catch (InterruptedException e) {
                      e.printStackTrace();
                  }
                  System.out.println(Thread.currentThread() + "waiting get resource1");
                  synchronized (resource1) {
                      System.out.println(Thread.currentThread() + "get resource1");
                  }
              }
          }, "线程 2").start();
      }
  }
  ```

* sleep()和wait()

  * **sleep方法没有释放锁，而wait方法释放了锁**
  * 都可以暂停线程的执行
  * wait通常被用于线程间交互/通信，sleep通常被用于暂停执行
  * wait()方法被调用后，线程不会自动苏醒，需要别的线程调用同一个对象上的notify()或者notifyAll()方法。sleep()方法执行完成后，线程会自动苏醒

* 为什么调用start()方法时会执行run()方法，为什么不能直接调用run()方法？

  * start()会执行线程的相应准备工作使线程进入就绪状态，然后自动执行run()方法的内容，这是真正的多线程工作。 而直接执行run()方法，会把run方法当成一个main线程下的普通方法去执行，并不会在某个线程中执行它，所以这并不是多线程工作

* synchronized关键字

  * 解决多个线程之间访问资源的同步性，可保证被它修饰的方法或者代码块在任意时刻只能有一个线程执行

  * **修饰实例方法**：作用于当前对象实例加锁，进入同步代码前要获得当前对象实例的锁

  * **修饰静态方法**：访问静态 synchronized 方法占用的锁是当前类的锁，而访问非静态 synchronized 方法占用的锁是当前实例对象锁

  * **修饰代码块:** 指定加锁对象，对给定对象加锁，进入同步代码库前要获得给定对象的锁

  * synchronized 关键字加到 static 静态方法和 synchronized(class)代码块上都是是给 Class 类上锁。synchronized 关键字加到实例方法上是给对象实例上锁

  * **双重校验锁实现对象单例（线程安全）**

    ```java
    public class Singleton {
        // volatile 可以禁止 JVM 的指令重排，保证在多线程环境下也能正常运行
        private volatile static Singleton uniqueInstance;
        private Singleton() {}
    
        public static Singleton getUniqueInstance() {
           //先判断对象是否已经实例过，没有实例化过才进入加锁代码
            if (uniqueInstance == null) {
                //类对象加锁
                synchronized (Singleton.class) {
                    if (uniqueInstance == null) {
                      // 分三步执行
                      // 1. 为 uniqueInstance 分配内存空间
                      // 2. 初始化 uniqueInstance
                      // 3. 将 uniqueInstance 指向分配的内存地址
                        uniqueInstance = new Singleton();
                    }
                }
            }
            return uniqueInstance;
        }
    }
    ```

  * 底层原理

    * 属于 JVM 层面
    * 查看相关字节码信息：执行 `javac SynchronizedDemo.java` 命令生成编译后的 .class 文件，然后执行`javap -c -s -v -l SynchronizedDemo.class`
    * synchronized 同步语句块的实现使用的是 monitorenter 和 monitorexit 指令，
      * monitorenter 指令指向同步代码块的开始位置，monitorexit 指令则指明同步代码块的结束位置
      * 当执行 monitorenter 指令时，线程试图获取锁也就是获取 monitor（monitor对象存在于每个Java对象的对象头中）的持有权
      * 当计数器为0则可以成功获取，获取后将锁计数器设为1也就是加1。相应的在执行 monitorexit 指令后，将锁计数器设为0，表明锁被释放。如果获取对象锁失败，那当前线程就要阻塞等待，直到锁被另外一个线程释放为止
    * synchronized 修饰方法
      * 没有 monitorenter 指令和 monitorexit 指令，有一个同步标识，JVM 通过该同步标志来辨别一个方法是否声明为同步方法，从而执行相应的同步调用

  * 优化

    * JDK1.6开始引入偏向锁、轻量级锁、自旋锁、适应性自旋锁、锁消除、锁粗化等技术来减少锁操作的开销。
    * 锁主要存在四种状态，依次是：无锁状态、偏向锁状态、轻量级锁状态、重量级锁状态，会随着竞争的激烈而逐渐升级
    * **偏向锁**
      * 没有多线程竞争的前提下，减少传统的重量级锁使用操作系统互斥量产生的性能消耗。
      * 偏向锁在无竞争的情况下会把整个同步都消除
      * 偏向于第一个获得它的线程，如果在接下来的执行中，该锁没有被其他线程获取，那么持有偏向锁的线程就不需要进行同步
      * 对于锁竞争比较激烈的场合，偏向锁升级为轻量级锁
    * **轻量级锁**
      * 没有多线程竞争的前提下使用 CAS 操作（加锁、释放锁）去代替使用互斥量，减少传统的重量级锁使用操作系统互斥量产生的性能消耗
      * 轻量级锁能够提升程序同步性能的依据是“对于绝大部分锁，在整个同步周期内都是不存在竞争的”。如果锁竞争激烈，那么轻量级将很快膨胀为重量级锁！
    * **自旋锁和自适应自旋**
      * 轻量级锁失败后，虚拟机为了避免线程真实地在操作系统层面挂起，还会进行一项称为自旋锁的优化手段
      * 互斥同步对性能最大的影响就是阻塞的实现，因为挂起线程/恢复线程的操作都需要转入内核态中完成（用户态转换到内核态会耗费时间）
      * **一般线程持有锁的时间都不是太长，所以仅仅为了这一点时间去挂起线程/恢复线程是得不偿失的**。**为了让一个线程等待，只需要让线程执行一个忙循环（自旋）**
      * 自旋等待不能完全替代阻塞，因为它还是要占用处理器时间，自旋等待的时间必须要有限度（默认10次）。如果自旋超过了限定次数任然没有获得锁，就应该挂起线程
      * **JDK1.6 中引入了自适应的自旋锁**，**自旋的时间不在固定了，而是和前一次同一个锁上的自旋时间以及锁的拥有者的状态来决定，虚拟机变得越来越“聪明”了**
    * **锁消除**
      * 虚拟机即时编译器在运行时，如果检测到那些共享数据不可能存在竞争，那么就执行锁消除，可以节省毫无意义的请求锁的时间
    * **锁粗化**
      * 使用同一个对象锁的相邻代码块进行合并处理

* Synchronized和ReenTrantLock的对比

  * 都是可重入锁
    * 一个线程获得了某个对象的锁，此时这个对象锁还没有释放，当其再次想要获取这个对象的锁的时候还是可以获取的，如果不可锁重入的话，就会造成死锁
  * synchronized 依赖于 JVM 而 ReenTrantLock 依赖于 API
  * ReenTrantLock增加一些功能
    * 等待可中断
      * 正在等待的线程可以选择放弃等待，改为处理其他事情，`lock.lockInterruptibly()`
    * 可实现公平锁
      * ReenTrantLock可指定是公平锁还是非公平锁，构造方法参数指定
      * 而synchronized只能是非公平锁。公平锁就是先等待的线程先获得锁
    * 可实现选择性通知
      * synchronized关键字与wait()和notify/notifyAll()方法相结合可以实现等待/通知机制，被通知的线程是由 JVM 选择的。
      * ReentrantLock类需要借助于Condition接口与newCondition() 方法，可实现多路通知功能，在一个Lock对象中可以创建多个Condition实例（即对象监视器），**线程对象可以注册在指定的Condition中，从而可以有选择性的进行线程通知，在调度线程上更加灵活**
  * jdk1.6之后性能差不多

* volatile关键字

  * Java内存模型
  * 主要作用就是保证变量的可见性和防止指令重排序

* synchronized关键字和volatile关键字

  * volatile关键字是线程同步的轻量级实现，但只能用于变量；synchronized关键字可以修饰方法以及代码块
  * 多线程访问volatile关键字不会发生阻塞，而synchronized关键字可能会发生阻塞
  * volatile关键字能保证数据的可见性，但不能保证数据的原子性。synchronized关键字两者都能保证
  * volatile关键字主要用于解决变量在多个线程之间的可见性，而 synchronized关键字解决的是多个线程之间访问资源的同步性

* Atomic原子类

  * AtomicInteger 类主要利用 CAS (compare and swap) + volatile 和 native 方法来保证原子操作，从而避免 synchronized 的高开销
  * CAS 的原理是拿期望的值和原本的一个值作比较，如果相同则更新成新的值

* AQS，AbstractQueuedSynchronizer

  * 用来构建锁和同步器的框架，使用 AQS 能简单且高效地构造出应用广泛的大量的同步器，比如 ReentrantLock，Semaphore，其他的诸如 ReentrantReadWriteLock，SynchronousQueue，FutureTask
  * AQS 核心思想是，如果被请求的共享资源空闲，则将当前请求资源的线程设置为有效的工作线程，并且将共享资源设置为锁定状态。如果被请求的共享资源被占用，那么就需要一套线程阻塞等待以及被唤醒时锁分配的机制，这个机制 是将暂时获取不到锁的线程加入到队列中
  * AQS 定义两种资源共享方式
    * **Exclusive**（独占）：只有一个线程能执行，如 ReentrantLock
    * **Share**（共享）：多个线程可同时执行，如 Semaphore/CountDownLatch
      * Semaphore(信号量)-允许多个线程同时访问某个资源
      * **CountDownLatch （倒计时器）：** 是一个同步工具类，用来协调多个线程之间的同步。这个工具通常用来控制线程等待，它可以让某一个线程等待直到倒计时结束，再开始执行
      * **CyclicBarrier(循环栅栏)**：可循环使用的屏障，让一组线程到达一个屏障（同步点）时被阻塞，直到最后一个线程到达屏障时，屏障才会开门，所有被屏障拦截的线程才会继续干活

# JVM

* jdk8之前
  * 线程私有
    * 程序计数器
      * 字节码解释器通过改变程序计数器来依次读取指令，从而实现代码的流程控制
      * 在多线程的情况下，程序计数器用于记录当前线程执行的位置，从而当线程被切换回来的时候能够知道该线程上次运行到哪儿了
      * 唯一一个不会出现 OutOfMemoryError 的内存区域
    * 虚拟机栈
      * 描述的是 Java 方法执行的内存模型，每次方法调用的数据都是通过栈传递的
      * Java 虚拟机栈会出现两种异常
        * **StackOverFlowError：** 若Java虚拟机栈的内存大小不允许动态扩展，那么当线程请求栈的深度超过当前Java虚拟机栈的最大深度的时候，就抛出StackOverFlowError异常
        * **OutOfMemoryError：** 若 Java 虚拟机栈的内存大小允许动态扩展，且当线程请求栈时内存用完了，无法再动态扩展了，此时抛出OutOfMemoryError异常
    * 本地方法栈
      * 虚拟机栈为虚拟机执行 Java 方法 （也就是字节码）服务，而本地方法栈则为虚拟机使用到的 Native 方法服务
      * 本地方法被执行的时候，在本地方法栈也会创建一个栈帧，用于存放该本地方法的局部变量表、操作数栈、动态链接、出口信息
      * 方法执行完毕后相应的栈帧也会出栈并释放内存空间，也会出现 StackOverFlowError 和 OutOfMemoryError 两种异常
  * 线程共享
    * 堆
      * 存放对象实例，几乎所有的对象实例以及数组都在这里分配内存
      * 垃圾收集器管理的主要区域
        * 分代垃圾收集算法
          * 新生代：Eden空间、From Survivor、To Survivor空间
          * 老年代
          * 大部分情况，对象都会首先在 Eden 区域分配，在一次新生代垃圾回收后，如果对象还存活，则会进入 s0 或者 s1，并且对象的年龄还会加 1(Eden区->Survivor 区后对象的初始年龄变为1)，当它的年龄增加到一定程度（默认为15岁），就会被晋升到老年代中
    * 方法区
      * 存储已被虚拟机加载的类信息、常量、静态变量、即时编译器编译后的代码等数据
      * 运行时常量池，用于存放编译期生成的各种字面量和符号引用
        * JDK1.7及之后版本的 JVM 已经将运行时常量池从方法区中移了出来，在 Java 堆（Heap）中开辟了一块区域存放运行时常量池
          * 字面量：文本字符串、被声明为final的常量值、基本数据类型的值
          * 符号引用：类的全限定名称、字段名称和描述符、方法名称和描述符
      * 永久代是HotSpot虚拟机对虚拟机规范中方法区的一种实现
      * JDK 1.8 的时候，方法区（HotSpot的永久代）被彻底移除了，取而代之是元空间，元空间使用的是直接内存
        * 整个永久代有一个 JVM 本身设置固定大小上线，无法进行调整，而元空间使用的是直接内存，受本机可用内存的限制，并且永远不会得到java.lang.OutOfMemoryError
    * 直接内存(非运行时数据区的一部分)
      * 避免了在 Java 堆和 Native 堆之间来回复制数据
* Java对象的创建过程
  * **1. 类加载检查**：虚拟机遇到一条 new 指令时，首先将去检查这个指令的参数是否能在常量池中定位到这个类的符号引用，并且检查这个符号引用代表的类是否已被加载过、解析和初始化过。如果没有，那必须先执行相应的类加载过程
  * **2. 分配内存**：在**类加载检查**通过后，虚拟机将为新生对象**分配内存**。对象所需的内存大小在类加载完成后便可确定，为对象分配空间的任务等同于把一块确定大小的内存从 Java 堆中划分出来
    * **分配方式**有 **“指针碰撞”** 和 **“空闲列表”** 两种，**选择那种分配方式由 Java 堆是否规整决定，而Java堆是否规整又由所采用的垃圾收集器是否带有压缩整理功能决定**（"标记-清除"，还是"标记-整理"（也称作"标记-压缩"），值得注意的是，复制算法内存也是规整的）
      * 指针碰撞
        * 堆内存规整，没有内存碎片的情况下，用过的在一边，没用过的在另一边，中间用指针分隔，分配内存时，指针向未用过那边移动对象大小位置即可
        * Serial、ParNew
      * 空闲列表
        * 堆内存不规整，虚拟机维护一个列表来记录那些内存块是可用的，在给对象分配内存时找块足够大的空间分配给对象，然后更新列表
        * CMS
    * 内存分配并发问题：
      * 虚拟机采用两种方式保证线程安全
        * **CAS+失败重试：** CAS 是乐观锁的一种实现方式。每次不加锁而是假设没有冲突而去完成某项操作，如果因为冲突失败就重试，直到成功为止。**虚拟机采用 CAS 配上失败重试的方式保证更新操作的原子性**
        * **TLAB：** 为每一个线程预先在Eden区分配一块儿内存，JVM在给线程中的对象分配内存时，首先在TLAB分配，当对象大于TLAB中的剩余内存或TLAB的内存已用尽时，再采用上述的CAS进行内存分配
  * **3. 初始化零值**
    * 内存分配完成后，虚拟机需要将分配到的内存空间都初始化为零值，保证对象的实例字段在 Java 代码中可以不赋初始值就直接使用
  * **4. 设置对象头**
    * 初始化零值完成之后，**虚拟机要对对象进行必要的设置**，例如这个对象是那个类的实例、如何才能找到类的元数据信息、对象的哈希吗、对象的 GC 分代年龄等信息。 **这些信息存放在对象头中。** 另外，根据虚拟机当前运行状态的不同，如是否启用偏向锁等，对象头会有不同的设置方式
  * **5. 执行 init 方法**
    * 把对象按照程序员的意愿进行初始化，这样一个真正可用的对象才算完全产生出来
* 对象的访问定位方式
  * Java程序通过栈上的 reference 数据来操作堆上的具体对象。对象的访问方式有虚拟机实现而定，目前主流的访问方式有两种：
    * **句柄：** Java堆中将会划分出一块内存来作为句柄池，reference 中存储的就是对象的句柄地址，而句柄中包含了对象实例数据（堆）与类型数据（方法区）各自的具体地址信息
      * **最大好处**是 reference 中存储的是稳定的句柄地址，在对象被移动时只会改变句柄中的实例数据指针，而 reference 本身不需要修改
    * **直接指针**： Java 堆对象的布局中就必须考虑如何放置访问类型数据的相关信息，而reference 中存储的直接就是对象的地址
      * **最大的好处**就是速度快，它节省了一次指针定位的时间开销
* 堆内存中对象的分配基本策略
  * 对象优先在Eden区分配、大对象直接进入老年代、长期存活的对象进入老年代
* Minor GC和Full GC
  * **新生代GC（Minor GC）**：大多数情况下，对象在新生代中 eden 区分配。当 eden 区没有足够空间进行分配时，虚拟机将发起一次Minor GC，Minor GC非常频繁，回收速度一般也比较快
  * **老年代GC（Major GC/Full GC）**：发生在老年代的GC，Major GC速度一般会比Minor GC的慢10倍以上
* 如何判断对象是否死亡
  * 引用计数法
    * 给对象中添加一个引用计数器，每当有一个地方引用它，计数器就加1；当引用失效，计数器就减1；任何时候计数器为0的对象就是不可能再被使用的
  * 可达性分析
    * 通过一系列的称为 **“GC Roots”** 的对象作为起点，从这些节点开始向下搜索，节点所走过的路径称为引用链，当一个对象到 GC Roots 没有任何引用链相连的话，则证明此对象是不可用的
* 各种引用类型
  * 强引用：当内存空间不足，Java虚拟机宁愿抛出OutOfMemoryError错误，使程序异常终止也不会去回收
  * 软引用：内存空间不足时回收，可用来实现内存敏感的高速缓存
  * 弱引用：垃圾收集器执行时就会回收
  * 虚引用：主要用来跟踪对象被垃圾回收的活动
* 判断一个常量是废弃的常量
  * 常量池中，当前没有任何String对象引用该字符串常量
* 判断一个无用类
  * 该类所有的实例都已经被回收，加载该类的 ClassLoader 已经被回收，该类对应的 java.lang.Class 对象没有在任何地方被引用，无法在任何地方通过反射访问该类的方法
* 垃圾收集算法
  * 标记-清除算法
    * 首先标记出所有需要回收的对象，在标记完成后统一回收所有被标记的对象
    * 问题：效率问题、空间问题（标记清除后会产生大量不连续的碎片）
  * 复制算法
    * 将内存分为大小相同的两块，每次使用其中的一块。当这一块的内存使用完后，就将还存活的对象复制到另一块去，然后再把使用的空间一次清理掉
  * 标记-整理算法
    * 老年代，首先标记出所有需要回收的对象，让所有存活的对象向一端移动，然后直接清理掉端边界以外的内存
  * 分代收集算法
    * 分代主要是为了提升GC效率
    * 新生代中，每次收集都会有大量对象死去，所以可以选择复制算法，只需要付出少量对象的复制成本就可以完成每次垃圾收集。
    * 老年代的对象存活几率是比较高的，而且没有额外的空间对它进行分配担保，所以必须选择“标记-清除”或“标记-整理”算法进行垃圾收集
* 垃圾收集器
  * 如果说收集算法是内存回收的方法论，那么垃圾收集器就是内存回收的具体实
  * 根据具体应用场景选择适合自己的垃圾收集器
  * **Serial收集器**
    * 串行收集器，最基本、历史最悠久、单线程，执行的时候必须暂停其他所有的工作线程（ **"Stop The World"** ），直到它收集结束。简单而高效
    * 新生代采用复制算法，老年代采用标记-整理算法
  * **ParNew收集器**
    * Serial收集器的多线程版本
    * 新生代采用复制算法，老年代采用标记-整理算法
  * **Parallel Scavenge收集器**
    * 关注点是吞吐量（高效率的利用CPU）
    * 吞吐量就是CPU中用于运行用户代码的时间与CPU总消耗时间的比值
    * 新生代采用复制算法，老年代采用标记-整理算法
  * **CMS收集器**
    * 以获取最短回收停顿时间为目标的收集器，注重用户体验
    * 并发收集器，让垃圾收集线程与用户线程（基本上）同时工作
    * 是一种“标记-清除”算法实现（**导致收集结束时会有大量空间碎片产生**）
      * **初始标记**：暂停所有的其他线程，并记录下直接与root相连的对象，速度很快
      * **并发标记**：同时开启GC和用户线程，用一个闭包结构去记录可达对象
      * **重新标记**：为了修正并发标记期间因为用户程序继续运行而导致标记产生变动的那一部分对象的标记记录，这个阶段的停顿时间一般会比初始标记阶段的时间稍长，远远比并发标记阶段时间短
      * **并发清除**：开启用户线程，同时GC线程开始对为标记的区域做清扫
    * 主要优点：**并发收集、低停顿**
  * **G1收集器**（**Garbage-First**）
    * 主要针对配备多颗处理器及大容量内存的机器，以极高概率满足GC停顿时间要求的同时，还具备高吞吐量性能特征
    * **并行与并发**：能充分利用CPU、多核环境下的硬件优势，使用多个CPU来缩短Stop-The-World停顿时间。部分其他收集器原本需要停顿Java线程执行的GC动作，G1收集器仍然可以通过并发的方式让java程序继续执行
    * **分代收集**、**可预测的停顿**
    * **空间整合**：整体来看是基于“标记整理”算法实现的收集器；从局部上来看是基于“复制”算法实现的

* 类加载过程
  * **加载->连接->初始化**，连接过程又分为:**验证->准备->解析**

# 计算机网络

* 从输入URL到页面加载发生了什么
  * DNS解析、TCP连接、发送HTTP请求、服务器处理请求并返回HTTP报文、浏览器解析渲染页面、连接结束

# Linux

* 在`/home`目录下查找以.txt结尾的文件名:`find /home -name "*.txt"`，忽略大小写: `find /home -iname "*.txt"`
* **touch 文件名称:** 文件的创建（增）
* 文件查看
  * **cat：** 查看显示文件内容
  * **more：** 可以显示百分比，回车可以向下一行， 空格可以向下一页，q可以退出查看
  * **less：** 可以使用键盘上的PgUp和PgDn向上 和向下翻页，q结束查看
  * **tail-10 ：** 查看文件的后10行，Ctrl+C结束
    * 命令 tail -f 文件 可以对某个文件进行动态监控，例如tomcat的日志文件
* **tar -zcvf 打包压缩后的文件名 要打包压缩的文件**
* **tar -zxvf xxx.tar.gz -C /usr**（- C代表指定解压的位置）
* **ls -l** 命令可查看某个目录下的文件或目录的权限
  * d/-/l(文件类型)rwx(属主权限)rwx(属组权限)r—(其他用户权限)
    * d目录、-文件、l-软链接
    * r可读4、w可写2、x可执行1
  * **修改文件/目录的权限的命令：chmod**
    * 修改/test下的aaa.txt的权限为属主有全部权限，属主所在的组有读写权限，
      其他用户只有读的权限
    * `chmod u=rwx,g=rw,o=r aaa.txt`或`chmod 764 aaa.txt`
* 每次开机到要求其自动启动
  * 新建脚本zookeeper，添加可执行权限：`chmod +x zookeeper`
  * 把zookeeper这个脚本添加到开机启动项里面：`chkconfig --add zookeeper`
  * 是否添加成功：`chkconfig --list`
* 查看当前系统的端口使用：netstat -an

# MySQL

* 存储过程：可以看成是一些 SQL 语句的集合，中间加了点逻辑控制语句，预编译过

* drop、delete与truncate区别？

  * `drop table 表名` ，直接将表都删除掉
  * `truncate table 表名` ，只删除表中的数据，再插入数据的时候自增长id又从1开始
  * `delete from 表名 where 列名=值`
  * truncate和drop 属于DDL(数据定义语言)语句，操作立即生效，原数据不放到 rollback segment 中，不能回滚，操作不触发 trigger。而 delete 语句是DML (数据库操作语言)语句，这个操作会放到 rollback segement 中，事务提交之后才生效

* 事务

  * ACID
    * **原子性：** 事务是最小的执行单位，不允许分割。确保动作要么全部完成，要么全部失败
    * **一致性：** 执行事务前后，数据保持一致
    * **隔离性：** 并发访问数据库时，一个用户的事务不被其他事务所干扰，各并发事务之间数据库是独立的
    * **持久性：** 一个事务被提交之后。它对数据库中数据的改变是持久的。
  * **不可重复度和幻读区别：**
    * 不可重复读的重点是修改，幻读的重点在于新增或者删除
    * 事务A，读取两次数据之间时刻，其他事物B提交修改或新增/删除的事务
    * ![f3f34283a61ddb8d86bad05062e1f824](/Users/dingyuanjie/Documents/study/github/woodyprogram/img/f3f34283a61ddb8d86bad05062e1f824.jpg)

* [索引](https://juejin.im/post/5b55b842f265da0f9e589e79)

  * 最左前缀原则

    * 如果查询的时候查询条件精确匹配索引的左边连续一列或几列，则此列就可以被用到，如User表的name和city加联合索引就是(name,city)，查询的时候如果两个条件都用上了，但是顺序不同，查询引擎会自动优化为匹配联合索引的顺序

  * 如何为表字段添加索引

    * ```sql
      -- 添加PRIMARY KEY（主键索引）
      ALTER TABLE `table_name` ADD PRIMARY KEY ( `column` ) 
      -- 添加UNIQUE(唯一索引)
      ALTER TABLE `table_name` ADD UNIQUE ( `column` ) 
      -- 添加INDEX(普通索引)
      ALTER TABLE `table_name` ADD INDEX index_name ( `column` )
      -- 添加FULLTEXT(全文索引)
      ALTER TABLE `table_name` ADD FULLTEXT ( `column`) 
      -- 添加多列索引
      ALTER TABLE `table_name` ADD INDEX index_name ( `column1`, `column2`, `column3` )
      ```

# Redis

* redis和memcached的区别
  * **redis支持更丰富的数据类型**
    * memcache支持简单的数据类型，String
    * Redis不仅仅支持简单的k/v类型的数据，同时还提供list，set，zset，hash等数据结构的存储
  * **Redis支持数据的持久化**，**而Memecache不支持**
  * **Memcached是多线程，非阻塞IO复用的网络模型；Redis使用单线程的多路 IO 复用模型**
* redis 常见数据结构以及使用场景
  * String
    * `set,get,decr,incr,mget`
  * Hash
    * `hget,hset,hgetall`
    * 是一个 string 类型的 field 和 value 的映射表，hash 特别适合用于存储对象
  * List
    * `lpush,rpush,lpop,rpop,lrange`
    * 双向链表，即可以支持反向查找和遍历
  * Set
    * `sadd,spop,smembers,sunion`
    * `sinterstore key1 key2 key3  将交集存在key1`
  * Sorted Set
    * `zadd,zrange,zrem,zcard`
    * 和set相比，sorted set增加了一个权重参数score，使得集合中的元素能够按score进行有序排列
* redis淘汰策略
  * **volatile-lru**：从已设置过期时间的数据集（server.db[i].expires）中挑选最近最少使用的数据淘汰
  * **volatile-ttl**：从已设置过期时间的数据集（server.db[i].expires）中挑选将要过期的数据淘汰
  * **volatile-random**：从已设置过期时间的数据集（server.db[i].expires）中任意选择数据淘汰
  * **allkeys-lru**：当内存不足以容纳新写入数据时，在键空间中，移除最近最少使用的key
  * **allkeys-random**：从数据集（server.db[i].dict）中任意选择数据淘汰
  * **no-eviction**：禁止驱逐数据，也就是说当内存不足以容纳新写入数据时，新写入操作会报错
  * **volatile-lfu**：从已设置过期时间的数据集(server.db[i].expires)中挑选最不经常使用的数据淘汰
  * **allkeys-lfu**：当内存不足以容纳新写入数据时，在键空间中，移除最不经常使用的key
* 缓存雪崩
  * 缓存同一时间大面积的失效，所以，后面的请求都会落到数据库上，造成数据库短时间内承受大量请求而崩掉
  * 解决办法
    * 事前：尽量保证整个 redis 集群的高可用性，发现机器宕机尽快补上。选择合适的内存淘汰策略
    * 事中：本地ehcache缓存 + hystrix限流&降级，避免MySQL崩掉
    * 事后：利用 redis 持久化机制保存的数据尽快恢复缓存
    * ![image-20190816143323082](/Users/dingyuanjie/Documents/study/github/woodyprogram/img/image-20190816143323082.png)
* **缓存穿透**
  * 故意请求缓存中不存在的数据，导致所有的请求都落到数据库上，造成数据库短时间内承受大量请求而崩掉
  * 解决
    * 采用布隆过滤器，将所有可能存在的数据哈希到一个足够大的bitmap中，一个一定不存在的数据会被 这个bitmap拦截掉，从而避免了对底层存储系统的查询压力
    * 更为简单粗暴的方法，如果一个查询返回的数据为空（不管是数据不存在，还是系统故障），仍然把这个空结果进行缓存，但它的过期时间会很短，最长不超过五分钟

# Spring

* IOC
  * 控制反转，是一种**设计思想**，就是 **将原本在程序中手动创建对象的控制权，交由Spring框架来管理**
  * 将对象之间的相互依赖关系交给 IoC 容器来管理，并由 IoC 容器完成对象的注入
* AOP
  * 将那些与业务无关，**却为业务模块所共同调用的逻辑或责任（例如事务处理、日志管理、权限控制等）封装起来**，便于**减少系统的重复代码**，**降低模块间的耦合度**，并**有利于未来的可拓展性和可维护性**
* Spring AOP 和 AspectJ AOP有什么区别
  * **Spring AOP 属于运行时增强，而 AspectJ 是编译时增强。** Spring AOP 基于代理，而 AspectJ 基于字节码操作
  * Spring AOP 已经集成了 AspectJ ，AspectJ 应该算的上是 Java 生态系统中最完整的 AOP 框架了。AspectJ 相比于 Spring AOP 功能更加强大，但是 Spring AOP 相对来说更简单，如果切面比较少，那么两者性能差异不大。但是，当切面太多的话，最好选择 AspectJ ，它比Spring AOP 快很多
* Spring Bean 单例模式线程安全问题
  * 单例 bean 存在线程问题，主要是因为当多个线程操作同一个对象的时候，对这个对象的非静态成员变量的写操作会存在线程安全问题
  * 解决
    * 在Bean对象中尽量避免定义可变的成员变量（不太现实）
    * 在类中定义一个ThreadLocal成员变量，将需要的可变成员变量保存在 ThreadLocal 中
* Spring Bean生命周期
  * Bean 容器找到配置文件中 Spring Bean 的定义。
  * Bean 容器利用 Java Reflection API 创建一个Bean的实例。
  * 如果涉及到一些属性值 利用 `set()`方法设置一些属性值。
  * 如果 Bean 实现了 `BeanNameAware` 接口，调用 `setBeanName()`方法，传入Bean的名字。
  * 如果 Bean 实现了 `BeanClassLoaderAware` 接口，调用 `setBeanClassLoader()`方法，传入 `ClassLoader`对象的实例。
  * 如果Bean实现了 `BeanFactoryAware` 接口，调用 `setBeanClassLoader()`方法，传入 `ClassLoade` r对象的实例。
  * 与上面的类似，如果实现了其他 `*.Aware`接口，就调用相应的方法。
  * 如果有和加载这个 Bean 的 Spring 容器相关的 `BeanPostProcessor` 对象，执行`postProcessBeforeInitialization()` 方法
  * 如果Bean实现了`InitializingBean`接口，执行`afterPropertiesSet()`方法。
  * 如果 Bean 在配置文件中的定义包含 init-method 属性，执行指定的方法。
  * 如果有和加载这个 Bean的 Spring 容器相关的 `BeanPostProcessor` 对象，执行`postProcessAfterInitialization()` 方法
  * 当要销毁 Bean 的时候，如果 Bean 实现了 `DisposableBean` 接口，执行 `destroy()`方法。
  * 当要销毁 Bean 的时候，如果 Bean 在配置文件中的定义包含 destroy-method 属性，执行指定的方法
  * ![image-20190816145935778](/Users/dingyuanjie/Documents/study/github/woodyprogram/img/image-20190816145935778.png)
* Spring MVC
  1. 客户端（浏览器）发送请求，直接请求到 `DispatcherServlet`。
  2. `DispatcherServlet` 根据请求信息调用 `HandlerMapping`，解析请求对应的 `Handler`。
  3. 解析到对应的 `Handler`（也就是我们平常说的 `Controller` 控制器）后，开始由 `HandlerAdapter` 适配器处理。
  4. `HandlerAdapter` 会根据 `Handler`来调用真正的处理器开处理请求，并处理相应的业务逻辑。
  5. 处理器处理完业务后，会返回一个 `ModelAndView` 对象，`Model` 是返回的数据对象，`View` 是个逻辑上的 `View`。
  6. `ViewResolver` 会根据逻辑 `View` 查找实际的 `View`。
  7. `DispaterServlet` 把返回的 `Model` 传给 `View`（视图渲染）。
  8. 把 `View` 返回给请求者（浏览器）
* Spring中的设计模式
  * **工厂设计模式** : Spring使用工厂模式通过 `BeanFactory`、`ApplicationContext` 创建 bean 对象。
  * **代理设计模式** : Spring AOP 功能的实现。
  * **单例设计模式** : Spring 中的 Bean 默认都是单例的。
  * **模板方法模式** : Spring 中 `jdbcTemplate`、`hibernateTemplate` 等以 Template 结尾的对数据库操作的类，它们就使用到了模板模式。
  * **包装器设计模式** : 我们的项目需要连接多个数据库，而且不同的客户在每次访问中根据需要会去访问不同的数据库。这种模式让我们可以根据客户的需求能够动态切换不同的数据源。
  * **观察者模式:** Spring 事件驱动模型就是观察者模式很经典的一个应用。
  * **适配器模式** :Spring AOP 的增强或通知(Advice)使用到了适配器模式、spring MVC 中也是用到了适配器模式适配`Controller`
* Spring管理事务的方式
  1. 编程式事务，在代码中硬编码。(不推荐使用)
  2. 声明式事务，在配置文件中配置（推荐使用）
     * 基于XML的声明式事务
     * 基于注解的声明式事务
* Spring事务中隔离级别
  * **TransactionDefinition.ISOLATION_DEFAULT:** 使用后端数据库默认的隔离级别，Mysql 默认采用的 REPEATABLE_READ隔离级别 Oracle 默认采用的 READ_COMMITTED隔离级别.
  * **TransactionDefinition.ISOLATION_READ_UNCOMMITTED:** 最低的隔离级别，允许读取尚未提交的数据变更，**可能会导致脏读、幻读或不可重复读**
  * **TransactionDefinition.ISOLATION_READ_COMMITTED:** 允许读取并发事务已经提交的数据，**可以阻止脏读，但是幻读或不可重复读仍有可能发生**
  * **TransactionDefinition.ISOLATION_REPEATABLE_READ:** 对同一字段的多次读取结果都是一致的，除非数据是被本身事务自己所修改，**可以阻止脏读和不可重复读，但幻读仍有可能发生。**
  * **TransactionDefinition.ISOLATION_SERIALIZABLE:** 最高的隔离级别，完全服从ACID的隔离级别。所有的事务依次逐个执行，这样事务之间就完全不可能产生干扰，也就是说，**该级别可以防止脏读、不可重复读以及幻读**。但是这将严重影响程序的性能。通常情况下也不会用到该级别
* Spring事务传播行为
  * 支持当前事务的情况
    * **TransactionDefinition.PROPAGATION_REQUIRED：** 如果当前存在事务，则加入该事务；如果当前没有事务，则创建一个新的事务。
    * **TransactionDefinition.PROPAGATION_SUPPORTS：** 如果当前存在事务，则加入该事务；如果当前没有事务，则以非事务的方式继续运行。
    * **TransactionDefinition.PROPAGATION_MANDATORY：** 如果当前存在事务，则加入该事务；如果当前没有事务，则抛出异常。（mandatory：强制性）
  * 不支持当前事务情况
    * **TransactionDefinition.PROPAGATION_REQUIRES_NEW：** 创建一个新的事务，如果当前存在事务，则把当前事务挂起。
    * **TransactionDefinition.PROPAGATION_NOT_SUPPORTED：** 以非事务方式运行，如果当前存在事务，则把当前事务挂起。
    * **TransactionDefinition.PROPAGATION_NEVER：** 以非事务方式运行，如果当前存在事务，则抛出异常
  * 其他情况
    * **TransactionDefinition.PROPAGATION_NESTED：** 如果当前存在事务，则创建一个事务作为当前事务的嵌套事务来运行；如果当前没有事务，则该取值等价于TransactionDefinition.PROPAGATION_REQUIRED

# Zookeeper

* 是一个开源的分布式协调服务
* **设计目标是将那些复杂且容易出错的分布式一致性服务封装起来，构成一个高效可靠的原语集，并以一系列简单易用的接口提供给用户使用**
  
  * 原语：是由若干条指令组成的，用于完成一定功能的一个过程
* **ZooKeeper 是一个典型的分布式数据一致性解决方案，分布式应用程序可以基于 ZooKeeper 实现诸如数据发布/订阅、负载均衡、命名服务、分布式协调/通知、集群管理、Master 选举、分布式锁和分布式队列等功能**
* **Zookeeper 一个最常用的使用场景就是用于担任服务生产者和服务消费者的注册中心(提供发布订阅服务)。** 服务生产者将自己提供的服务注册到Zookeeper中心，服务的消费者在进行服务调用的时候先到Zookeeper中查找服务，获取到服务生产者的详细信息之后，再去调用服务生产者的内容与数据

* ZooKeeper 的典型应用场景发布订阅功能有啥用

  * 作为配置中心
    * **应用配置保存在 Zookeeper 的某个目录节点中，对指定的节点设置一个 Watcher 监听** ，**一旦配置信息发生变化，每个应用程序就会收到 Zookeeper 的通知，然后可以从 Zookeeper 获取新的配置信息应用到系统中**
  * 作为Dubbo 的注册中心也就是发布订阅中心
    * 服务生产者将自己提供的服务注册到 Zookeeper 的一个或一系列节点上去，服务的消费者在进行服务调用的时候先到 Zookeeper 中查找服务，获取到服务生产者的详细信息之后，再去调用服务生产者的内容与数据

* 数据节点

  * 数据模型：树型结构
  * Node由stat-状态信息、data-数据内容，组成

* 概念

  * **ZooKeeper 本身就是一个分布式程序（只要半数以上节点存活，ZooKeeper 就能正常服务）。**
  * **为了保证高可用，最好是以集群形态来部署 ZooKeeper，这样只要集群中大部分机器是可用的（能够容忍一定的机器故障），那么 ZooKeeper 本身仍然是可用的。**
  * **ZooKeeper 将数据保存在内存中，这也就保证了 高吞吐量和低延迟**（但是内存限制了能够存储的容量不太大，此限制也是保持znode中存储的数据量较小的进一步原因）。
  * **ZooKeeper 是高性能的。 在“读”多于“写”的应用程序中尤其地高性能，因为“写”会导致所有的服务器间同步状态。**（“读”多于“写”是协调服务的典型场景。）
  * **ZooKeeper有临时节点的概念。 当创建临时节点的客户端会话一直保持活动，瞬时节点就一直存在。而当会话终结时，瞬时节点被删除。持久节点是指一旦这个ZNode被创建了，除非主动进行ZNode的移除操作，否则这个ZNode将一直保存在Zookeeper上。**
  * ZooKeeper 底层其实只提供了两个功能：①管理（存储、读取）用户程序提交的数据；②为用户程序提供数据节点监听服务。

* 特点

  * **顺序一致性：** 从同一客户端发起的事务请求，最终将会严格地按照顺序被应用到 ZooKeeper 中去。
  * **原子性：** 所有事务请求的处理结果在整个集群中所有机器上的应用情况是一致的，也就是说，要么整个集群中所有的机器都成功应用了某一个事务，要么都没有应用。
  * **单一系统映像 ：** 无论客户端连到哪一个 ZooKeeper 服务器上，其看到的服务端数据模型都是一致的。
  * **可靠性：** 一旦一次更改请求被应用，更改的结果就会被持久化，直到被下一次更改覆盖

* 设计目标

  * 简单的数据模型
    * 允许分布式进程通过共享的层次结构命名空间进行相互协调
    * 数据保存在内存中，这意味着ZooKeeper可以实现高吞吐量和低延迟
  * 可构建集群
    * 组成 ZooKeeper 服务的服务器都会在内存中维护当前的服务器状态，并且每台服务器之间都互相保持着通信。集群间通过 Zab 协议（Zookeeper Atomic Broadcast）来保持数据的一致性
  * 顺序访问
    * **对于来自客户端的每个更新请求，ZooKeeper 都会分配一个全局唯一的递增编号，这个编号反应了所有事务操作的先后顺序，应用程序可以使用 ZooKeeper 这个特性来实现更高层次的同步原语。** **这个编号也叫做时间戳——zxid（Zookeeper Transaction Id）**
  * 高性能
    *  **在“读”多于“写”的应用程序中尤其地高性能，因为“写”会导致所有的服务器间同步状态。（“读”多于“写”是协调服务的典型场景。）**

* 集群角色

  * **最典型集群模式： Master/Slave 模式（主备模式）**
    * 通常 Master服务器作为主服务器提供写服务，其他的 Slave 服务器从服务器通过异步复制的方式获取 Master 服务器最新的数据提供读服务
    * **ZooKeeper 集群中的所有机器通过一个 Leader 选举过程来选定一台称为 “Leader” 的机器，Leader 既可以为客户端提供写服务又能提供读服务。除了 Leader 外，Follower 和 Observer 都只能提供读服务。Follower 和 Observer 唯一的区别在于 Observer 机器不参与 Leader 的选举过程，也不参与写操作的“过半写成功”策略，因此 Observer 机器可以在不影响写性能的情况下提升集群的读性能**
    * **当 Leader 服务器出现网络中断、崩溃退出与重启等异常情况时，ZAB 协议就会进人恢复模式并选举产生新的Leader服务器**
      1. Leader election（选举阶段）：节点在一开始都处于选举阶段，只要有一个节点得到超半数节点的票数，它就可以当选准 leader。
      2. Discovery（发现阶段）：在这个阶段，followers 跟准 leader 进行通信，同步 followers 最近接收的事务提议。
      3. Synchronization（同步阶段）:同步阶段主要是利用 leader 前一阶段获得的最新提议历史，同步集群中所有的副本。同步完成之后
         准 leader 才会成为真正的 leader。
      4. Broadcast（广播阶段）
         到了这个阶段，Zookeeper 集群才能正式对外提供事务服务，并且 leader 可以进行消息广播。同时如果有新的节点加入，还需要对新节点进行同步

* ZAB协议

  * 作为其保证数据一致性的核心算法，是一种特别为Zookeeper设计的崩溃可恢复的原子消息广播算法
  * **在 ZooKeeper 中，主要依赖 ZAB 协议来实现分布式数据一致性，基于该协议，ZooKeeper 实现了一种主备模式的系统架构来保持集群中各个副本之间的数据一致性**
  * 两种基本模式
    * 崩溃恢复
      * 当整个服务框架在启动过程中，或是当 Leader 服务器出现网络中断、崩溃退出与重启等异常情况时，ZAB 协议就会进人恢复模式并选举产生新的Leader服务器。当选举产生了新的 Leader 服务器，同时集群中已经有过半的机器与该Leader服务器完成了状态同步之后，ZAB协议就会退出恢复模式。其中，**所谓的状态同步是指数据同步，用来保证集群中存在过半的机器能够和Leader服务器的数据状态保持一致**
    * 消息广播
      * **当集群中已经有过半的Follower服务器完成了和Leader服务器的状态同步，那么整个服务框架就可以进人消息广播模式了。** 当一台同样遵守ZAB协议的服务器启动后加人到集群中时，如果此时集群中已经存在一个Leader服务器在负责进行消息广播，那么新加人的服务器就会自觉地进人数据恢复模式：找到Leader所在的服务器，并与其进行数据同步，然后一起参与到消息广播流程中去。正如上文介绍中所说的，ZooKeeper设计成只允许唯一的一个Leader服务器来进行事务请求的处理。Leader服务器在接收到客户端的事务请求后，会生成对应的事务提案并发起一轮广播协议；而如果集群中的其他机器接收到客户端的事务请求，那么这些非Leader服务器会首先将这个事务请求转发给Leader服务器

* 常用命令

  ```shell
  # 连接 ZooKeeper 服务, bin 目录下
  ./zkCli.sh -server 127.0.0.1:2181
  # 创建节点(create 命令)
  create /node1 “node1”
  create /node1/node1.1 123
  # 更新节点数据内容(set 命令)
  set /node1 "set node1"
  # 获取节点的数据(get 命令)
  #  查看某个目录下的子节点(ls 命令)
  ls /
  ls /node1
  # 查看节点状态(stat 命令)
  stat /node1
  # 查看节点信息和状态(ls2 命令)
  ls2 /node1
  # 删除节点(delete 命令)
  delete /node1/node1.1
  ```

  