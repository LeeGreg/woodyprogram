# MS

* Java程序运行在Docker等容器环境有哪些新问题?
  
  * 主要是运行环境所需资源问题，如容器未设置合适的JVM堆和元数据区、直接内存等参数，导致OOM；容器限制CPU核数可能导致JVM设置不合适的GC并行线程数；
    * Docker之类容器和虚拟机非常相似
        * 例如，它也有自己的shell，能独立安装软件包，运行时与其他容器互不干扰
    * Docker并不是一种完全的虚拟化技术，而更是一种轻量级的隔离技术
        * Docker仅在类似Linux内核之上实现了有限的隔离和虚拟化，并不是像传统虚拟化软件那样，独立运行一个新的操作系统。
        * 如果是虚拟化的操作系统，不管是Java还是其他程序，只要调用的是同一个系统API，都可以透明地获取所需的信息，基本不需要额外的兼容性改变
        * 容器虽然省略了虚拟操作系统的开销，实现了轻量级的目标，但也带来了额外复杂性，它限制对于应用不是透明的
  
* 后台服务出现明显“变慢”，谈谈你的诊断思路?
  
  * 慢的意思是指请求的反应延时变长吗？
  * 突然变慢还是长时间运行后变慢，类似问题是否重复出现
  * 问题可能是Java服务本身，也可能是受系统里其他服务的影响
    * 检查应用本身的错误日志
    * 检查系统级别的资源等情况，监控CPU、内存等资源是否被其他进程大量占用
  * 监控Java服务自身
      * 例如GC日志里面是否观察到Full GC等恶劣情况出现，或者是否Minor GC在变长等
      * 利用jstat等工具，获取内存使用的统计信息
      * 利用jstack等工具检查是否出现死锁
  
* JDK，JRE 和 JVM 的联系和区别

    * JVM 代表 Java 虚拟机
        * 将编译后的字节码文件转换为特定的机器代码，提供内存回收和安全机制
        * 它的责任是运行 Java 应用，独立于操作系统和硬件，是java语言一次编译到处运行的原因
    * JRE 代表 Java 运行时环境
        * 是运行 Java程序所必须的，包含jvm和一些依赖
    * JDK 代表 Java 开发工具
        * 包含 Java 编译器、 JRE
    * JIT 代表即时编译
        * 混合了静态编译和动态解释，一句一句编译源代码，但是会将翻译过的代码缓存起来以降低性能损耗
            * 静态编译的程序在执行前全部被翻译为机器码
            * 解释执行的则是一句一句边运行边翻译

* Java8

  * HashMap底层实现新增了红黑树；

  * jvm内存管理：由元空间代替了永久代

      * 区别：
          * 元空间不再存在虚拟机内存中，而是本地内存，大小默认情况下只受本地内存控制

  * lambda表达式：允许把函数当成参数，传递给某个方法，或者把代码本身当做数据处理；

  * 引入重复注解：@repeatable注解，来定义注解为重复注解

  * 注解的使用场景拓宽：注解可以使用在任何元素上

  * 新的包，java.time包

      * 该包包含了所有关于日期、时间、时区、持续时间、和时钟操作的类
      * 这些类都是不可变的，线程安全的

  * 接口中增加静态方法和默认方法
    * 默认方法：
      * 接口中方法：返回值前用default修饰且有方法体
      * 如果一个类实现了多个接口，且这些接口中有相同签名的默认方法，则实现类需要重写默认方法
    * 静态方法：
      * 属于接口，只能通过接口名调用，不能被实现类重写，不能通过实现类实例调用

  * 函数接口

    * @FunctionalInterface标记的接口且只有一个抽象方法

  * 可以使用lambda表达式创建函数接口的实例`(argument) -> (body)`

  * 简化代码；支持Stream API

  * Stream API

    * 不存储数据，操作源数据结构，产生并使用管道数据，实现具体的操作
    * 基于条件的从list和filter中创建Stream，使用函数接口

    ```java
    private static int sumIterator(List<Integer> list) {
    	Iterator<Integer> it = list.iterator();
    	int sum = 0;
    	while (it.hasNext()) {
    		int num = it.next();
    		if (num > 10) {
    			sum += num;
    		}
    	}
    	return sum;
    }
    
    private static int sumStream(List<Integer> list) {
    	return list.stream().filter(i -> i > 10).mapToInt(i -> i).sum();
    }
    ```

  * Collection的forEach()方法，接收Consumer参数，使用lambda表达式

    ```java
  List<Integer> myList = Arrays.asList(1,2,3,4,5,6,7,8,9,11,12,12);
    myList.forEach(t -> System.out.println("forEach anonymous class Value::"+t));
  ```
  
* 为什么String是不可变的？
  * final修饰、不可变
    * 使字符串常量池成为可能，能够节省很多堆空间，因为不同的String变量可引用池中相同String变量
    * 可避免使用过程中一些敏感信息被修改，一旦被修改了就不是原来的String
    * 多线程环境下可共享而不用采取而外的线程同步机制
    * String的哈希码在创建时被缓存，不需要再次计算，它的处理速度比其他HashMap键对象快

* 全局唯一有序 ID
    * UUID
        * 无序32位数的16进制数字所构成
    * snowFlake雪花算法
      * 64位的二进制Long型正整数，按照时间自增排序，效率高
    * redis
      * 单线程的Redis实现了一个原子操作`INCR`和`INCRBY`实现递增的操作

* 冯诺依曼体系

    *  计算机处理的数据和指令一律用二进制数表示
    * 顺序执行程序
        *  计算机运行时，把要执行的程序和处理的数据首先存入主存储器（内存），在执行程序时，将自动按顺序从主存储器中取出指令一条一条地执行
    * 计算机硬件由运算器、控制器、存储器、输入设备和输出设备五大部分组成

* Java 中应该使用什么数据类型来代表价格？

  * 如果不是特别关心内存和性能的话，使用BigDecimal，否则使用预定义精度的 double 类型。
* 怎么将 byte 转换为 String

  * 可以使用 String 接收 byte[] 参数的构造器来进行转换，需要注意的点是要使用的正确的编码
* 并发与并行
    * 并发：多个线程间断执行
    * 并行：多个线程同时执行


* a++和++a
  
  
  * 单独使用，都是让a增加1
  * int a = 1; int b = a++;   a为2，b为1
  * int a = 1; int b = ++a;   a为2，b为2
  
* a = a + b 与 a += b 的区别(int a , char b)

  
  * a和b是同类型，则没区别
  * 否则存在类型转换，低精度自动转向高精度，高精度显示强制转向低精度
  * +=运算中，`b + = a`结合了强制类型转换的功能，因此，不会出现编译错误
  * 而对于`b=a+b;`简单的运算，没有类型转换（`b = (char)(a+b)`），在编译过程中会报错
  
* ```java
  HashSet shortSet = new HashSet();
  for (short i = 0; i < 100; i++) {
    shortSet.add(i);
    // 发生类型转换，set中不存在Integer类型数据
    // int——Integer——删除不存在的会返回false
    shortSet.remove(i - 1);
  }
  // 100
  System.out.println(shortSet.size());
  
  String s1 ="abc";
  String s2 ="abc";
  // 优先级
  System.out.println("s1 == s2 is:" + s1 == s2); //false
  ```
* `Java` 中的编译期常量是什么？使用它又什么风险？

  
  * 是`public static final`修饰的变量，编译器知道这些变量的值，会在编译时会被替换掉，且在运行时不能改变
  * 一旦改变，需重新编译程序
  
* Java 中怎么打印数组

  * `Arrays.toString() `和 `Arrays.deepToString()` 方法来打印数组
  
* `try {return …} finally {}`
  
  
  * try语句退出时肯定会执行`finally`语句
  
    * 确保了即使发生意想不到的异常也会执行`finally`语句块，也不会因为`return`、`continue`、或者`break`语句而忽略了清理代码
    * 如果`try`语句里有`return`，如果有返回值，就把返回值保存到局部变量中，执行`finally`语句后，返回之前保存在局部变量表里的值
  * `JVM`退出或线程被中断，那么`finally`语句块不会执行
  * 规范规定，当`try`和`finally`里都有`return`时，会忽略`try`的`return`，而使用`finally`的`return`
  
* `object`有哪些方法-9
  * `clone()` 、`toString()` 、`equals(Object obj)` 、`hashCode() `
  * `finalize()` 对象逃脱死亡命运的最后一次机会
  * `getClass()` 返回此 Object 的运行时类
  * `notify() `唤醒在此对象监视器上等待的单个线程
  * `notifyAll() `唤醒在此对象监视器上等待的所有线程
  * `wait() `在其他线程调用此对象的` notify()` 方法或 `notifyAll()` 方法前，导致当前线程等待
  * `wait(long timeout)`,超过指定的时间量前，导致当前线程等待
  * `wait(long timeout, int nanos)`或者其他某个线程中断当前线程，或者已超过某个实际时间量前，导致当前线程等待

# 经验

* 【强制】线程池不允许使用 Executors 去创建，而是通过 ThreadPoolExecutor 的方式，更加明确线程池的运行规则，规避资源耗尽的⻛风险
  * 说明：Executors 返回的线程池对象的弊端如下:
    * 1）FixedThreadPool 和 SingleThreadPool
      * 允许的请求队列长度为 Integer.MAX_VALUE，可能会堆积大量的请求，从而导致OOM。
    * 2）CachedThreadPool 和 ScheduledThreadPool：
      * 允许的创建线程数量为 Integer.MAX_VALUE，可能会创建⼤量的线程，从⽽导致OOM。
* 【强制】对多个资源、数据库表、对象同时加锁时，需要保持一致的加锁顺序，否则可能会造成死锁

* 【强制】并发修改同一记录时，避免更新丢失，需要加锁。要么在应用层加锁，要么在缓存加锁，要么在数据库层使用乐观锁，使用 version 作为更新依据。

  * 如果每次访问冲突概率⼩于 20%，推荐使用乐观锁，否则使用悲观锁。乐观锁的重试次数不得⼩于3次。
* 【强制】多线程并行处理定时任务时，Timer 运行多个 TimeTask 时，只要其中之一没有捕获抛出的异常，其它任务便会自动终止运行，使用 ScheduledExecutorService 则没有这个问题
* 【参考】volatile 解决多线程内存不可见问题。对于一写多读，是可以解决变量同步问题，但是如果多写，同样⽆法解决线程安全问题。如果是 count++操作，使⽤用如下类实现： 
  * `AtomicInteger count = new AtomicInteger(); count.addAndGet(1);`
*  如果是`JDK8`，推荐使⽤`LongAdder`对象，⽐`AtomicLong `性能更好(减少乐观锁的重试次数)。
* 【参考】 HashMap 在容量不够进行 resize 时由于高并发可能出现死链，导致 CPU 飙升，在开发过程中可以使⽤其它数据结构或加锁来规避此⻛险。
* 【参考】下列情形，需要进行参数校验：

  *  调⽤频次低的方法、 执行时间开销很大的⽅法、需要极高稳定性和可用性的方法、 
  * 对外提供的开放接口，不管是RPC/API/HTTP接⼝。 
  * 敏感权限⼊口
* 【参考】下列情形，不需要进行参数校验：

  * 极有可能被循环调用的方法、底层调用频度⽐较高的方法
  * 被声明成`private`只会被⾃己代码所调用的方法，如果能够确定调用⽅法的代码传入参数已经做过检查或者肯定不会有问题，此时可以不校验参数。
* 【强制】在使⽤正则表达式时，利用好其预编译功能，可以有效加快正则匹配速度

  * 说明：不要在方法体内定义：`Pattern pattern = Pattern.compile(“规则”);`
* 【强制】注意 `Math.random()` 这个⽅法返回是` double` 类型，注意取值的范围 `0≤x<1`，如果想获取整数类型的随机数，直接使⽤`Random` 对象的`nextInt`或者`nextLong`⽅法。
* 【强制】获取当前毫秒数`System.currentTimeMillis();`而不是`new Date().getTime();`
* 【强制】Java 类库中定义的可以通过预检查⽅式规避的`RuntimeException` 异常不应该通过`catch `的方式来处理，比如：`NullPointerException`，`IndexOutOfBoundsException` 等等。 

  * 说明：无法通过预检查的异常除外，比如，在解析字符串形式的数字时，不得不通过 `catch NumberFormatException` 来实现
  * 正例：`if (obj != null) {…}`
* 【强制】捕获异常是为了处理它，不要捕获了却什么都不处理而抛弃之，如果不想处理它，请将该异常抛给它的调用者。最外层的业务使用者，必须处理异常，将其转化为用户可以理理解的内容
* 【强制】有 try 块放到了事务代码中，catch 异常后，如果需要回滚事务，⼀定要注意手动回滚事务
* 【推荐】防止 NPE，是程序员的基本修养，注意 NPE 产生的场景：

  * 返回类型为基本数据类型，return 包装数据类型的对象时，自动拆箱有可能产⽣ NPE
    * 反例：`public int f() { return Integer 对象}`， 如果为 null，⾃动解箱抛 NPE。
  * 数据库的查询结果可能为null
  * 集合⾥的元素即使isNotEmpty，取出的数据元素也可能为null
  * 远程调用返回对象时，一律要求进行空指针判断，防⽌NPE
  * 对于Session中获取的数据，建议NPE检查，避免空指针
  * 级联调用obj.getA().getB().getC();一连串调用，易产生NPE
  * 正例：使用 JDK8 的 Optional 类来防止 NPE 问题
* 对于公司外的` http/api `开放接口必须使用“错误码”；而应⽤内部推荐异常抛出； 跨应用间 `RPC `调⽤优先考虑使用` Result` 方式，封装` isSuccess()`方法、“错误码”、“错误简短信息”。

  * 说明：关于RPC 方法返回方式使用 Result ⽅式的理由：
    * 使用抛异常返回⽅式，调用⽅如果没有捕获到就会产⽣运行时错误
    * 如果不加栈信息，只是new自定义异常，加⼊⾃己的理解的error message，对于调⽤端解决问题的帮助不会太多。如果加了栈信息，在频繁调用出错的情况下，数据序列化和传输的性能损耗也是问题
* 【强制】用户请求传入的任何参数必须做有效性验证。 
  * 说明：忽略参数校验可能导致
    * page size 过大导致内存溢出、恶意 order by 导致数据库慢查询、任意重定向、SQL 注入、反序列化注⼊、正则输入源串拒绝服务 ReDoS
  * 说明：Java 代码⽤正则来验证客户端的输入，有些正则写法验证普通用户输⼊没有问题， 但是如果攻击人员使用的是特殊构造的字符串来验证，有可能导致死循环的结果。
* 【强制】表单、A JAX提交必须执行CSRF安全验证。 

  * 说明：`CSRF(Cross-site request forgery)`跨站请求伪造是一类常⻅编程漏洞。对于存在 CSRF 漏洞的应用/网站，攻击者可以事先构造好URL，只要受害者⽤户一访问，后台便在用户不知情的情况下对数据库中⽤户参数进行相应修改
* 【强制】在使用平台资源，譬如短信、邮件、电话、下单、⽀付，必须实现正确的防重放的机制，如数量限制、疲劳度控制、验证码校验，避免被滥刷而导致资损。

  * 说明：如注册时发送验证码到⼿机，如果没有限制次数和频率，那么可以利⽤用此功能骚扰到其它用户，并造成短信平台资源浪费。
* 【推荐】发贴、评论、发送即时消息等用户生成内容的场景必须实现防刷、⽂本内容违禁词过滤等⻛控策略
* `fnal`、`fnally`、` finalize`有什么不同?
* `fnal`，关键字，修饰的：`class`不可以被继承、变量不可以修改，方法不可以重写
    * 方法或者类声明为fnal后不能修改，保证平台安全的手段
    * 修饰参数或者变量，避免意外赋值导致的编程错误
    * 可用于保护只读数据，并发编程中有利于减少额外的同步开销
    * `fnal`不是`immutable`!
  * `fnally`，是`Java`保证重点代码一定要被执行的一种机制，通常和`try、try/catch`结合使用关闭连接资源
  * `finalize`，`Object`的方法，对象逃脱死亡命运的最后一次机会，无法保证什么时候执行

# 类

## 基础

* 类中5个成员：成员变量、初始化块、构造器、方法、内部类

* `byte` 1个字节 、`short` 2个字节 、`char` 2个字节 、`int` 4个字节、` float` 4个字节、`long` 8个字节 、`double` 8个字节、 `boolean` Java规范中没有明确指定`boolean`大小，1个字节（编译后1和0）或4个字节（当作`int`来处理）

  * `Integer`：-128~127自动装箱(数组中缓存起来，不在这个范围内的，每次都是新建实例)
  
* 自动装箱：算一种语法糖-Java平台自动进行一些转换，保证不同写法在运行时等价，发生在编译阶段
  
  * `javac`自动把装箱转换为`Integer.valueOf()` ，把拆箱替换为`Integer.intValue()`
  
* 静态工厂方法`valueOf`会使用到缓存机制
  
* `Integer.valueof(6) `-128~127会缓存该方法创建的对象，若new，则新对象
  
* 3*0.1==0.3是true还是false？false，因为有些浮点数不能完全精确的表示出来
  
* 基本类型和字符串之间的转换

  * 包装类提供的 `parseXxx(String str)`
    * `int i = Integer.parseInt("123");` `Integer integer = Integer.valueOf("123");`
  
* 静态⽅法`.Xxx(String str)`构造器
  
* `Integer it = new Integer("123");`
  * `String.valueof(XXX xx)` 、`Xxx + ""` =>基本类型转字符串
  
* 定义类的主要作⽤：定义变量、创建实例和作为⽗类被继承

* 面向对象三大特征：封装、继承、多态

* 变量：

  1. 成员变量：(实例变量、静态变量）  =>  ⽆需显示初始化，具有默认初始化，堆内存中
  2. 局部变量：(形参、⽅法局部变量、代码块局部变量)  =>  除形参外，使用之前都必须显示初始化

* 面向对象编程中的基本目的：

  * 让代码只操纵对基类的引用。这样，如果要添加一个新类来扩展程序，就不会影响到原来的代码

* `this`总是指向调⽤该方法的对象

  1. 构造器中引用该构造器正在初始化的对象
  2. 在⽅法中引用调用该方法的对象

  最大作用：让类中的一个方法，访问该类里的另一个方法或实例变量

  允许对象的⼀个成员直接调用另一个成员，可以省略this前缀

  如果确实需要在静态方法中访问另一个普通⽅法，则只能重新创建一个对象去访问

* 形参个数可变的⽅法` test(int a, String ... books) `books可以是数组

* 向下类型转型前，先`instanceof`（对象是不是某个特定类型的实例）

* 值传递，引用变量存放在栈内存里，指向真正存储在堆内存中的对象

  1. 基本类型：传递值的副本。封装类型（如`Integer`）：传递的也是值的副本，约定
  2. 引用类型：传递内存空间地址

  ```java
public static void main(String[] args) throws NoSuchFieldException, IllegalAccessException {
      //不是int类型
      Integer i1 = 1, i2 =2;
      //自动装箱  Integer a = 1 => Integer a = Integer.valueOf(1);
      // -128-127缓存，提前分配好地址
      System.out.println("Before: i1 = " + i1 + ", i2 = " + i2);
      swap(i1, i2);
      System.out.println("After: i1 = " + i1 + ", i2 = " + i2);
  }
  
  private static void swap(Integer i1, Integer i2) throws NoSuchFieldException, IllegalAccessException {
      Field field = Integer.class.getDeclaredField("value");
      field.setAccessible(true);  //绕过安全检查
      int tmp = i1.intValue();
      field.setInt(i1, i2.intValue());
      field.setInt(i2, tmp);
  }
  ```

* java和c++的区别

  1. 都是面向对象的语言，封装、继承、多态
  2. java中没有指针用来访问内存，更加安全
  3. java中有自动的内存回收机制
  4. java中的类是单继承，c++多继承，但是java可以通过接口实现多继承

## Class对象

* `Java`程序在它开始运行之前并非完全被加载，其各个部分是在必须时才加载的
  * 所有的类都是在对其第一次使用时，动态加载到JVM中

* 当程序创建第一个对类的静态成员的引用时，就会加载这个类。
  * 这个证明构造器也是类的静态方法，即使在构造器之前并没有使用static关键字。因此，使用new操作符创建类的新对象也会被当作对类的静态成员的引用
* `Class`对象仅在需要的时候才被加载，static初始化是在类加载时进行的
* 使用`newInstance()`来创建的类，必须带有默认的构造器
* 获取`class`对象
  1. `Class.forName()`不需要持有该类型的对象。`会自动地初始化该Class对象`
  2. `obj.getClass()`返回该对象的实际类型的Class引用
  3. 类字面量`Demo.class`生成对`Class`对象的引用，编译时就会受到检查，并且根除了对`forName()`的调用，更高效。应用于普通的类、接口、数组、基本数据类型。`不会自动地初始化该Class对象`

## 类初始化

```java
//编译期常量，不需要初始化类就可以读取，调用其他的需要先初始化类
static final int staticFinal = 47;
//域设置成static final的，需要先初始化类，再调用
static final int staticFinal2 = ClassInitialization.rand.nextInt(1000);  //A
static {  
    System.out.println("Initializing Initable!");  //B
}
//调用A时，先执行B，再执行A
```

## 对象

* Java创建（实例化）对象的五种方式

  1. 用`new`语句创建对象
  2. 运用反射手段。调用`java.lang.Class`或`java.lang.reflect.Constructor`的`newInstance()`实例方法
  3. 调用对象的`clone()`方法
  4. 通过工厂方法返回对象，如：`String str = String.valueOf(23); `
  5. 通过`I/O流`（包括反序列化），如运用反序列化手段，调用`java.io.ObjectInputStream`对象的 `readObject()`方法

* Java创建一个对象，系统先为该对象的所有实际变量分配内存 =》执行初始化(初始化块/指定初始值—>构造器器里指定的初始值)

* Java对象创建过程

  1. 父类【静态成员】和【静态代码块】，按在代码中出现的顺序依次执行。
  2. 子类【静态成员】和【静态代码块】，按在代码中出现的顺序依次执行。
  3. 父类的【普通成员变量被普通成员方法赋值】和【普通代码块】，按在代码中出现的顺序依次执行。
  4. 执行父类的构造方法。
  5. 子类的【普通成员变量被普通成员方法赋值】和【普通代码块】，按在代码中出现的顺序依次执行。
  6. 执行子类的构造方法。
  7. 一般顺序：静态块（静态变量）——>成员变量——>构造方法——>静态方法， 静态方法需要调用才会执行

* 类成员不能访问实例成员：类成员的作用域比实例成员的作用域更大，完全可能出现类成员已经初始化完成，但实例成员还不曾初始化的情况

* 封装

  * 将对象状的态信息隐藏在对象内部，不允许外部程序直接访问对象内部信息，而是通过该类所提供的方法来实现对内部信息的操作和访问

  * private - default - protected - public

  * 类            包           子类+包       所有

* 继承

  1. 实现代码复用的重要手段
  2. 一般和特殊的关系 is a关系，父类包含的范围总⽐子类大，单继承
  3. 子类扩展了父类，将可以获得父类的全部成员变量和方法，除了父类构造器，但是可以用super()调⽤
  4. 子类调⽤⽗类中被覆盖的方法：super.实例方法、⽗类名.类方法
  5. 不管是否使用super调用来执行父类构造器的初始化代码，子类构造器总会调用⽗类构造器一次
  6. 子类不能继承父类的构造器，如果父类的构造器带有参数，则必须在子类的构造器中显式地通过 super 关键字调用父类的构造器并配以适当的参数列表
  7. 如果父类构造器没有参数，则在子类的构造器中不需要使用 super 关键字调⽤父类构造器，系统会自动调用⽗类的⽆参构造器
  8. 创建任何对象总是从该类所在继承树最顶层类的构造器开始执行，然后依次向下执行，最后才执行本类的构造器
  9. 继承：is a关系，严重地破坏了父类的封装性
  10. 组合：has a关系，把旧类对象作为新类的成员变量组合进来

* 多态

  1. 可以使程序有良好的扩展，并可以对所有类的对象进行通用处理
  2. Java 引⽤变量有两种类型，不一致时产⽣多态
     * 编译时类型：由声明该变量时使用的类型决定
     * 运⾏时类型：由实际赋给该变量的对象决定
  3. 实际执行子类覆盖父类后的方法，不能调用子类特有的方法
     * 引用变量只能调用它编译时类型的方法
     * 如果需要让这个引用变量调用它运行时类型的方法，则必须把它强制类型转换成运行时类型
  4. 对象的实例变量，则不具备多态性，实际访问编译时类型(父类)所定义的成员变量
  5. 多态实现方式：接口、抽象类和抽象方法、重写

* 不可变对象

  * 一旦被创建，它们的状态就不能被改变的对象，每次对它们的改变都是产生了新的不可变的对象
  * `String`和`StringBuilder`，`String`是`immutable`的，每次对`String`对象的修改都将产生一个新的`String`对象，而原来的对象保持不变；而`StringBuilder`是`mutable`，因为每次对于它的对象的修改都作用于该对象本身，并没有产生新的对象
  * 实际上JDK本身就自带了一些`immutable`类，比如`String`，`Integer`以及其他包装类
  * 实现不可变类
    * 将`class`自身声明为`final`
    * 将所有成员变量定义为`private`和`fnal`，并不实现`setter`方法
    * 通常构造对象时，成员变量使用深度拷贝来初始化
  
  * 不可变类对象的状态在创建之后就不能发生改变，任何对它的改变都应该产生一个新的对象
  
  * 为类、属性添加了final修饰，从而避免因为继承和多态引起的不可变风险
  * 可以创建一个包含可变对象的不可变对象的，不要共享可变对象的引用，如果需要变化时，就返回原对象的一个拷贝。最常见的例子就是对象中包含一个日期对象的引用
  * 好处是线程安全的、且可以被重复使用
  * 缺点就是会制造大量垃圾

## 序列化

* Java对象的序列化机制  `=>` 可把内存中的Java对象转换成二进制字节流

  * 可以把Java对象存储到磁盘里，或者在⽹络上传输 Java 对象
  * 也是Java提供分布式编程的重要基础，使得对象可以脱离程序的运行而独立存在

  ```java
  ObjectOutputStream -> writeObject() 对象输出到输出流
  ObjectInputStream -> readObject() 读取流中的对象 -> 反序列化:⽆需通过构造器来初始化Java对象
  ```

* 浅克隆

  * 快速构造一个已有对象的副本
* `Object-clone()`

实现`Cloneable`接口（标识）

1. 原对象和克隆对象中的引用类型属性是同一个，基本类型复制

2. 原对象`.clone();`

* 深克隆

  1. 原对象和克隆对象中的引用类型属性不是同一个

  2. 方法1：引用类型属性也实现`Cloneable`接口

  3. 方法2：序列化和反序列化（实现`Cloneable`、`Serialiable`） `public User deepClone(){…}`

     `ByteArrayInputStream`、`ByteArrayOutputStream`、`ObjectInputStream`、`ObjectOutputStream`

## 跨域

* 是指一个域下的文档或脚本试图去请求另一个域下的资源，由浏览器的同源策略造成的，是一种安全策略

* 同源是指，域名、协议、端口均为相同

* 常见跨域场景：

  1. 主域不同、子域名不同、端口不同、协议不同

  2. `localhost`调用`127.0.0.1` 跨域

* 同源策略限制了以下行为：
  *  `Cookie`、`LocalStorage` 和 `IndexDB` 无法读取
  * `DOM`和`JS`对象无法获取
  * `Ajax`请求不能发送

[解决方法](<https://segmentfault.com/a/1190000011145364>)：

1. `jsonp`跨域

   * 在`html`页面中通过`script`标签从不同域名下加载静态资源文件是被浏览器允许的
   
   * 可以通过动态创建`script`，再请求一个带参网址实现跨域通信
     * 原生
     * `jquery ajax`，`dataType: 'jsonp'`
     * 最大的缺陷是，只能够实现`get`请求

2. `document.domain` +` iframe`跨域

   * 仅限主域相同，子域不同的跨域应用场景
   * 实现原理：两个页面都通过js强制设置`document.domain`为基础主域，就实现了同域

3. 跨域资源共享（`CORS`）

   * 普通跨域请求，只服务端设置`Access-Control-Allow-Origin`即可，前端无须设置

     * 支持所有类型的`HTTP`请求
     * 浏览器在头信息之中，增加一个`Origin`字段用来说明，本次请求来自哪个源（协议 + 域名 + 端口）。服务器根据这个值，决定是否同意这次请求，只要服务器实现了`CORS`接口，就可以跨源通信

   * 若要带`cookie`请求：前后端都需要设置

     * 前端设置是否带`cookie`

     * 后端

       ```java
       // 允许跨域访问的域名：若有端口需写全（协议+域名+端口），若没有端口末尾不用加'/'
       response.setHeader("Access-Control-Allow-Origin", "http://www.domain1.com"); 
       // 允许前端带认证cookie：启用此项后，上面的域名不能为'*'，必须指定具体的域名，否则浏览器会提示
       response.setHeader("Access-Control-Allow-Credentials", "true"); 
       // 提示OPTIONS预检时，后端需要设置的两个常用自定义头
       response.setHeader("Access-Control-Allow-Headers", "Content-Type,X-Requested-With");
       ```

4. nginx代理跨域

   * 浏览器跨域访问js、css、img等常规静态资源被同源策略许可，但iconfont字体文件(eot|otf|ttf|woff|svg)例外，可在nginx的静态资源服务器中加入以下配置

     ```json
     location / {
       add_header Access-Control-Allow-Origin *;
     }
     ```

   * ##### nginx反向代理接口跨域

     * 跨域原理： 同源策略是浏览器的安全策略，不是`HTTP`协议的一部分

       * 服务器端调用HTTP接口只是使用HTTP协议，不会执行JS脚本，不需要同源策略，也就不存在跨域问题

     * 实现思路：
     
       * 通过nginx配置一个代理服务器做跳板机，反向代理访问服务接口
         * 通过nginx配置一个代理服务器（域名与domain1相同，端口不同）做跳板机，反向代理访问domain2接口，并且可以顺便修改cookie中domain信息，方便当前域cookie写入，实现跨域登录
       
       ```shell
       #proxy服务器
       server {
           listen       81;
           server_name  www.domain1.com;
       
           location / {
               proxy_pass   http://www.domain2.com:8080;  #反向代理
               proxy_cookie_domain www.domain2.com www.domain1.com; #修改cookie里域名
               index  index.html index.htm;
               # 当用webpack-dev-server等中间件代理接口访问nignx时，此时无浏览器参与，故没有同源限制，下面的跨域配置可不启用
               add_header Access-Control-Allow-Origin http://www.domain1.com;  
               #当前端只跨域不带cookie时，可为*
               add_header Access-Control-Allow-Credentials true;
           }
       }
       ```

5. `WebSocket`协议跨域

   * `WebSocket protocol`是HTML5一种新的协议。它实现了浏览器与服务器全双工通信，同时允许跨域通讯


## 泛型

* 泛型：把类型明确的工作推迟到创建对象或调用方法的时候才去明确的特殊的类型，为编译期提供类型检查

  * 用在编译期，编译过后泛型擦除（消失掉）（可以通过反射越过泛型检查）
  * Java泛型设计原则：只要在编译时期没有出现警告，那么运行时期就不会出现ClassCastException异常
  
* 泛型类：把泛型定义在类上，⽤户使⽤该类的时候，才把类型明确下来

  * 在类上定义的泛型，在类的方法中也可以使⽤

    ```java
    //1:把泛型定义在类上
    //2:类型变量定义在类上,⽅法中也可以使用
    public class ObjectTool<T> {
       private T obj;
       public T getObj() {
          return obj;
    	 }
       public void setObj(T obj) {
          this.obj = obj;
    	} 
      //定义泛型⽅法..
    	 public <T> void show(T t) {
          System.out.println(t);
       }
      
       public static void main(String[] args) { //创建对象
          ObjectTool tool = new ObjectTool();
          //调⽤⽅法,传入的参数是什么类型,返回值就是什么类型 
    	    tool.show("hello");
    	    tool.show(12);
    	    tool.show(12.5);
       }
    }
    ```

* 泛型⼦类

  ```java
  //把泛型定义在接⼝上
  public interface Inter<T> {
      public abstract void show(T t);
  }
  
  //⼦类明确泛型类的类型参数变量:
  public class InterImpl implements Inter<String> {
      @Override
      public void show(String s) {
          System.out.println(s);
  	} 
  }
  
  //⼦类不明确泛型类的类型参数变量: 
  //实现类也要定义出<T>类型的
  public class InterImpl<T> implements Inter<T> {
      @Override
      public void show(T t) {
          System.out.println(t);
  	  } 
  }
  
  public static void main(String[] args) { 
      //测试第⼀种情况
      //Inter<String> i = new InterImpl();
      //i.show("hello");
  		//第二种情况测试
  		Inter<String> ii = new InterImpl<>(); 
      ii.show("100");
  }
  ```
  
* 类型通配符

  * 方法接收一个集合参数，遍历集合并把集合元素打印出来，怎么办?

  ```java
   public void test(List<?> list){
      for(int i=0;i<list.size();i++){
          System.out.println(list.get(i));
      }
  }
  ```

  * ?号通配符表示可以匹配任意类型，任意的Java类都可以匹配

  * 使用?号通配符的时候：就只能调对象与类型无关的⽅法，不能调用对象与类型有关的⽅法，因为直到外界使⽤才知道具体的类型是什么
  * 设定通配符上限
      * `List<? extends Number>`  //List集合装载的元素只能是`Number`的⼦类或自身
  * 设定通配符下限
      * `<? super Type>`   //传递进来的只能是`Type`或`Type`的父类
* 如果参数之间的类型有依赖关系，或者返回值是与参数之间有依赖关系的。那么就使用泛型方法
  
  * 如果没有依赖关系的，就使⽤通配符，通配符会灵活⼀些

## JSON

```java
//创建json对象
1. JSONObject jo = new JSONObject();
   jo.put("name", "jon doe");
2. new JSONObject(Map);
3. new JSONObject(Object);
4. new JSONObject("{\"city\":\"chicago\",\"name\":\"jon doe\",\"age\":\"22\"}");

//创建json数组
1. JSONArray ja = new JSONArray();
   ja.put(Boolean.TRUE);
   ja.put(Object)
2. new JSONArray(List);
2. JSONArray ja = new JSONArray("[true, \"lorem ipsum\", 215]");

//json字符串转json对象
1. String json = "{\"2\":\"efg\",\"1\":\"abc\"}";   
   JSONObject json_test = JSONObject.fromObject(json); 

//import com.alibaba.fastjson.JSON;
String str = wsRequest.getJson();
JSONObject jsonObject = JSON.parseObject(str);
String data = jsonObject.getString("DATA");

List<HomsUserInfo> list = JSON.parseObject(data, new TypeReference<List<HomsUserInfo>>() {});
houseHolderBean.setEsb_ext(JSON.toJSONString(homsUserInfo));
JSONObject obj = new JSONObject();
obj.put("project_name", homsUserInfo.getProject_name());
houseHolderBean.setEsb_ext(obj.toJSONString());
```

```java
//@JSONField
//import com.alibaba.fastjson.annotation.JSONField;
1. 指定字段的名称
	@JSONField(name="role_name")  
	private String roleName;
2. 使用format制定日期格式
	// 配置date序列化和反序列使用yyyyMMdd日期格式  
	@JSONField(format="yyyyMMdd")  
	public Date date; 
3. 指定字段的顺序
	@JSONField(ordinal = 3)  
   private int f0;  
4. 使用serialize/deserialize指定字段不序列化
	@JSONField(serialize=false)
	public Date date;
```

## String

* `String`是不可变类，被声明成为`fnal class`，所有属性都是`fnal`的，除读操作外都会产生新的String对象
  * 存储于字符串常量池、不能修改、线程安全、性能快
* `StringBuffer`可变、存储于堆、线程安全、性能相对慢
* `StringBuilder`可变，存储于堆、线程不安全、性能快

`new String("hello")`  => JVM会先使⽤常量池来管理`hello`直接量，再调⽤`String`类的构造器来创建⼀个新的`String`对象(堆内存中)  =》共产⽣两个字符串对象

`String:equals()`  =〉两个字符串所包含的字符序列相同，则返回true

重写类Object的equals()=》比较对象的地址 ==

String字符串变量的连接动作，在编译阶段会被转化成`StringBuilder`的`append`操作，变量最终指向`Java`堆上新建的`String`对象

当使用+进行多个字符串连接时，实际上是产生了一个`StringBuilder`对象和一个`String`对象

* 字符串常量池

  * 指的是在编译期被确定，并被保存在已编译的.class文件中的一些数据
* Java7之前运行时常量池是方法区的一部分，Java7之后存储于堆中
  
* 常量池主要用于存放两大类常量：
    * 字面量：如文本字符串，声明为final的常量值等
    * 符号引用：属于编译原理方面的概念，包括：类和接口的全限定名、字段名称和描述符、方法名称和描述符


## 方法

* 方法重载（两同一不同）
  * 同⼀个类中，⽅法名相同，参数列表不同
  * 不用返回值作区分：因为可以忽略方法的返回值 =>可能让系统不能区分调用了那一个
* 方法重写（两同两小一大）
  * 子类包含与父类同名方法的现象(要么都实例方法，要么都类方法)
    * ⼦类方法访问权限 ⼤于等于 父类
    * 子类⽅法返回值类型 小于等于 父类的方法返回值类型
    * 方法名相同、形参列表相同
    * ⼦类方法声明抛出的异常类 小于等于 父类

## 接口和抽象类

* 接口
  * 规范、可以理解为抽象类的特列，多实现
  * 静态常量；不包含初始化块；不包含构造器；抽象方法（`public abstract`）、静态方法、默认方法
* 抽象类
  * 模版式设计、单继承
  * 成员变量、静态常量；可包含初始化块；可包含构造器让子类调用完成属于抽象类的初始化操作、不能创建实例对象；抽象方法(`public，protected`和默认)、普通方法、静态方法

* 共同点
  * 都不能被实例化，都可包含抽象⽅法


## 内部类

* 作用：更好的封装，内部类成员可以直接访问外部类的私有数据
* 匿名内部类

  1. ⽐外部类多使用`private、protected、static`
  2. 适合创建那种只需要一次使用的类，创建时会立即创建⼀个该类的实例
  3. new 实现接口() | 父类构造器(实参列表) {...}
  4. 必须，但最多只能继承⼀个父类或实现一个接⼝
  5. Java8开始匿名内部类、局部内部类允许访问⾮final的局部变量(相当于⾃动使用了final修饰)
* 静态：
  * 属于外部类本身，可以包含静态成员，也可以包含非静态成员，其实例方法不能访问外部类的实例属性
  * 在外部类以外使⽤静态内部类(与外部类相关)，外部类当成静态内部类的包空间即可
* 非静态:
  * 不能拥有静态成员
  * 构造器必须通过其外部类对象来调⽤
  * 其实例必须寄生在外部类实例里，this.变量、外部类.this.变量

## Lambda表达式

* 会被编译成一个函数式接口，会使代码更加简洁
* 作用：
  * 代替匿名内部类的繁琐语法
  * ⽀持将代码块作为方法参数
  * 允许使用更加简洁的代码来创建函数式接口的实例
* 与匿名内部类的主要区别
  * 对于匿名类，关键词 this 解读为匿名类，⽽对于 Lambda 表达式，关键词 this 解读为写就Lambda 的外部类

```java
// 编译器自动推断
new Thread(
        () -> System.out.println("hello world")
).start();
```

* ⾃定义的函数式接⼝：

```java
@FunctionalInterface
public interface WorkerInterface {
    public void doSomeWork();
}
```

```java
public class WorkerInterfaceTest {
    public static void execute(WorkerInterface worker) {
      worker.doSomeWork();
    }
    public static void main(String[] args){
      //匿名内部类方式
      execute(new WorkerInterface() {
        @Override
        public void doSomeWork() {
          System.out.println("Worker invoked using Anonymous class");
        } 
      });
      //Lambda方式更简洁
      execute( () -> System.out.println("Worker invoked using Lambda expression") );
   } 
}
```

```java
//Old way:
List<Integer> list1 = Arrays.asList(1, 2, 3, 4, 5, 6, 7);
for(Integer n: list1) {
    System.out.println(n);
}
常⽤的箭头语法创建 Lambda 表达式，
List<Integer> list2 = Arrays.asList(1, 2, 3, 4, 5, 6, 7); 
list2.forEach(n -> System.out.println(n));
Java 8 全新的双冒号(::)操作符将一个常规⽅法转化为 Lambda 表达式 
//or we can use :: double colon operator in Java 8 
list2.forEach(System.out::println);
```

```java
将 Lambda 表达式 x -> x*x 传给 map() 方法，该⽅法会作用于流中的所有元素 
//Old way:
List<Integer> list = Arrays.asList(1,2,3,4,5,6,7);
for(Integer n : list) {
	int x = n * n;
    System.out.println(x);
}
//New way:
List<Integer> list = Arrays.asList(1,2,3,4,5,6,7);
list.stream().map((x) -> x*x).forEach(System.out::println);
```

```java
计算给定数值中每个元素平⽅后的总和
是 MapReduce 的⼀个初级例⼦
使⽤ map() 给每个元素求平方，再使用 reduce() 将所有元素计⼊一个数值:
//Old way:
List<Integer> list = Arrays.asList(1,2,3,4,5,6,7);
int sum = 0;
for(Integer n : list) {
	int x = n * n;
	sum = sum + x; 
}
System.out.println(sum);
//New way:
List<Integer> list = Arrays.asList(1,2,3,4,5,6,7);
int sum = list.stream().map(x -> x*x).reduce((x,y) -> x + y).get();
System.out.println(sum);
```

## 枚举类

* 能创建的实例有限且确定，不能自由创建对象，其对象在定义类时已经固定下来，
  * 可以实现⼀个或多个接口，默认继承了`java.lang.Enum`类，⽽不是`Object`类

  * 默认使⽤`final`修饰，不能派⽣子类

  * 构造器只能使⽤`private`修饰

  * 所有实例(系统自动添加`public static final`修饰)必须在枚举类第一行显示列出，否则永远都不能产⽣实例

* 通过`Enum的valueOf()`⽅法来获取指定枚举类的枚举值，如 

  `A a = Enum.valueOf(A.class, "aa");`

```java
public enum AppVersionTypeEnum {
    ENTRANCE_MACHINE("Community.EQUIPMENT_TYPE.ENTRANCE_MACHINE", "");
     
		private final String val;
  	private final String desc;
    AppVersionTypeEnum(String val, String desc) {
        this.val = val;
        this.desc = desc;
    }
  	// AppVersionTypeEnum.values();
    public String getVal() {
        return val;
    }
    public String getDesc() {
        return desc;
    }
}
AppVersionTypeEnum.ENTRANCE_MACHINE.getVal()
```

## final关键字

1. final关键字

   * 修饰的类不可以有子类
* 修饰的变量不可改变
   * 修饰引用类型变量的地址不会改变，但对象完全可发⽣改变
   * 修饰的⽅法不可被重写
   
2. 好处：
   * final关键字提高了性能。JVM和Java应用都会缓存final变量
   * final变量可以安全的在多线程环境下进行共享，而不需要额外的同步开销
   * 使用final关键字，JVM会对方法、变量及类进行优化

3. 宏变量，定义final变量，指定初始值，并在编译时就确定下来，编译器会把程序中所有用到该变量的地方，直接替换成该变量的值

   ```java
   String str1 = "疯狂";
   String str2 = "Java";
   String str3 = str1 + str2; //编译器不会执行宏替换，无法在编译时确定s3的值 
   String s1 = "疯狂Java";
   s1 == s3 =>false //但 str1、str2使⽤final修饰=>true
   ```

# 集合

* 数组和集合的区别
  * 数组长度固定，能存储基本类型和引用类型 `int[] arr = [1,2,3,4];`
  * 集合长度不固定，只能存储引用类型（基本类型自动装箱）
* `Iterator`：仅用于遍历集合，必须有⼀个被迭代的集合
  * 不包括`Map`，`hasNext()`、`next()`、`remove()`
* `Collections`工具类：
  * 同步控制：解决多线程并发访问
  * `XXX xx = Collection.synchronized.Xxx(new Xxx());`
* 【强制】不要在`foreach` 循环里进行元素的 `remove/add` 操作。
  * `remove` 元素请使⽤ `Iterator`方式，如果并发操作，需要对` Iterator` 对象加锁
* 【参考】利用 Set 元素唯一的特性，可以快速对⼀个集合进行去重操作，避免使用 List 的contains 方法进行遍历、对比、去重操作。
* `Hashtable`、`concurrentHashMap`的`key`和`value`不允许为`null`，线程安全；`TreeMap的key`不允许为`null`，`value`允许为`null`，线程不安全；`HashMap`的`key`和`value`允许为`null`，线程不安全；

## map集合接口

* 底层是基于 数组 + 链表 组成的，不过在 jdk1.7 和 1.8 中具体实现稍有不同
  
  * `entrySet()`、`keySet()`
  
* `HashMap`

  * 可以使用`null`作为`key`或`value`、线程不安全、无序（`key`的`hash`算法讲究随机均匀）、
  * 多线程下容易出现`resize()`死循环：并发执行`put`操纵导致触发扩容行为，从而导致环形链表，在获取数据遍历链表时形成死循环
  * 判断两个`key`相等：`equals()`返回`true`，且`hashCode`值也相等
  * 存储位置随时间变化
  * 存在扩容操作，从而导致存储位置重新计算
  * 初始容量为：16，加载因子为0.75，扩容为：1倍
  * key的定位是需要结合key的hash和map的长度-1，相与计算来决定
      * 当长度为2的次幂再减一时，转换为二进制就全都为1，在和key的hash值进行计算，更加简便，避免内存碎片，导致的浪费内存空间
  
* `TreeMap`

  * 可对`key`进行排序，实现有序，`key`不允许为`nul`l、`value`允许为`null`，线程不安全
  * 红黑树数据结构，可以保证所有的`key-value`对处于有序状态
    * 自然排序：所有`key`应是同⼀个类的对象，且`key`必须实现`Comparable`接⼝
    * 定制排序：创建`TreeMap`时，传⼊一个`Comparator`对象，其负责对`TreeMap`中的所有key进行排序，不要求`Map`的`key`实现`Comparable`接⼝
    * 判断两个`key`相等：两个`key`通过`equals()`返回`true`，那通过`compareTo()`返回0
  
* `Java`中`LinkedHashMap`和`PriorityQueue`的区别是什么

  * `PriorityQueue` 保证最高或者最低优先级的的元素总是在队列头部，迭代器遍历时没有任何顺序保证
    * 元素大小的评判可以通过元素本身的自然顺序，也可以通过构造时传入的比较器`Comparator`
    * 实现了`Queue`接口，不允许放入`null`元素，创建时可指定初始大小，增加元素的时候，队列大小会自动增加
    * 是非线程安全的，所以Java提供了`PriorityBlockingQueue`（实现[BlockingQueue接口）用于Java多线程环境
  *  `LinkedHashMap` 维持元素插入的顺序，遍历顺序是元素插入的顺序
  
* `HashTable`

  * key、value都不能为null，且线程安全

* `properties`

  * 是`Hashtable`类的⼦类，其key、value都是字符串类型，相当于⼀个key、value都是String类型的Map

  ```java
  void load(InputStream inStream) 
      从属性⽂件中加载key-value对，把加载到的key-value对追加到Properties⾥
      new Properties().load(new FileInputStream("a.ini"));
  void store(OutputStream out, String comments) 
      将Properties中的key-value对输出到指定的属性⽂件中
  	new Properties().store(new FileOutputStream("a.ini"));
  ```
  
* 【强制】关于 `hashCode `和 `equals` 的处理，遵循如下规则:
  1. 只要重写`equals`，就必须重写`hashCode`
  2. Set存储的是不重复的对象，依据`hashCode`和`equals`进行判断，所以`Set`存储的对象必须重写这两个方法
  3. 如果自定义对象作为Map的键，那么必须重写`hashCode`和`equals`
     *  说明：`String`重写了 `hashCode` 和 `equals`方法，所以可使用`String` 对象作为key 来使⽤

## collection集合接口

### list

* 能存放`null`，有序（插⼊顺序）、可重复
* 判断相等：`equals()`返回`true`即可
* `ArrayList`：
  * 基于动态数组实现，线程不安全，以数组形式存储适合随机访问，插入和删除最后一个元素效率高，中间插入和删除需移动后面元素导致性能差
    * 初始容量为：10，加载因子为0.5，原来的基础上扩展0.5倍

* `LinkedList`
  * 基于双向链表实现，线程不安全，便于增删操作，随机访问效率低
* `Vector`
  * 基于动态数组实现，以数组形式存储适合随机访问，线程安全
    * 对所有方法添加`synchronized`同步关键字
    * 扩容时容量提高1倍

`ArrayList`和`LinkedList`的区别：

1. `ArrayList`是实现了基于动态数组的数据结构，`LinkedList`基于链表的数据结构
2. 对于随机访问`get`和`set`，`ArrayList`觉得优于`LinkedList`，因为`LinkedList`要移动指针
3. 对于新增和删除操作`add`和`remove`，`LinedList`比较占优势，因为`ArrayList`要移动数据。

`ArrayList`与`Vector`区别

1. `Vector`是线程安全的，⽽`ArrayList`不是。
2. `ArrayList`和`Vector`都采⽤线性连续存储空间，当存储空间不足的时候，`ArrayList`默认增加为原来的50%，`Vector`默认增加为原来的一倍
3. Vector可以设置capacityIncrement，⽽ArrayList不可以，从字⾯理解就是capacity容量，Increment增加，容量增长的参数

### set

* 基于hashMap来实现
* 不能储存重复值，且无序（sortedSet可以实现有序）
* 初始容量为：16，加载因子为0.75，扩容为：1倍
* Set检索效率低下，删除和插⼊效率高，插入和删除不会引起元素位置改变

* Hashset

  1. 不能保证顺序、线程不安全、值可以是null
  2. 判断元素相等：两个对象equals()相等且hashCode()也相等

* LinkedHashSet

  1. 根据元素的hashCode值来决定元素的存储位置
  2. 内部构建了一个记录插入顺序的双向链表，提供了按照插入顺序遍历的能力
  
* TreeSet

  * 支持自然顺序访问，但是添加、删除、包含等操作要相对低效(log(n)时间)

  1. 确保集合元素处于排序状态(元素实际值的⼤小进行排序)

  2. 采⽤红黑树的数据结构来存储集合元素

  3. 自然排序：CompareTo(Object obj)⽐较元素之间的⼤小，按升序

  4. 定制排序：int compare(T o1, T o2)

     * 实现：创建TreeSet集合对象时，提供⼀个Comparator对象与该TreeSet集合关联，由该
       Comparator对象负责集合元素的排序逻辑

     ```java
     TreeSet ts = new TreeSet((o1,o2)->{
         M m1 = (M)o1;
     	  M m2 = (M)o2;
         return m1.age > m2.age ? -1 : m1.age < m2.age ? 1 : 0;
     });
     ```

     * TreeSet中只能添加同⼀种类型的对象，且其类必须实现CompareTo接⼝

     * 判断相等：两个对象equals()相等，且compareTo(Object obj)返回0

* EnumSet

  1. 有序，以枚举值在Enum类内的定义顺序来决定集合元素的顺序

  2. 不允许null元素，内部以位向量的形式存储，批量操作执行速度非常快

  3. 只能保存同一个枚举类的枚举值作为集合元素

只有当需要一个保持排序的Set时，才应该使⽤用TreeSet，否则都应该使用HashSet

一般要对list进行除重的话，可以将list转化为set

`Set<String> set = new HashSet<>(List);`

### Queue

* Queue保持一个队列(FIFO先进先出)的顺序
* poll() 和 remove() 都是从队列中取出一个元素
  *  poll() 在获取元素失败的时返回null
  *  remove() 失败的时候会抛出异常
* Deque代表了双端队列，既可以作为队列使用、也可以作为栈使⽤
* Java中的队列都有哪些，有什么区别
  * BlockingQueue有四个具体的实现类，根据不同需求，选择不同的实现类: 
    * ArrayBlockingQueue、LinkedBlockingQueue、PriorityBlockingQueue、SynchronousQueue

## HashMap

### HashMap、HashTable的区别

1. Hashtable是线程安全，而HashMap则非线程安全
2. HashMap可以使用null作为key和value，而Hashtable则不允许null作为key和value
3. HashMap是对Map接口的实现，HashTable实现了Map接口和Dictionary抽象类
4. HashMap的初始容量为16，Hashtable初始容量为11，两者的填充因⼦默认都是0.75，HashMap扩容时是当前容量翻倍，Hashtable扩容时是容量翻倍+1

5. 两者计算hash的方法不同
* Hashtable计算hash是直接使用key的hashcode值对数组的长度直接进行取模

* HashMap计算hash对key的hashcode值进行了二次hash，以获得更好的散列值，然后对数组长度取模，JDK8将key的hashcode高16位异或下来，然后对数组长度取模

6. HashMap和Hashtable的底层实现都是数组+链表结构实现，添加、删除、获取元素时都是先计算hash，根据hash和table.length计算index也就是table数组的下标，然后进行相应操作

### Java7

1. 基于数组和链表实现（元素为Entry对象-键值对）结合组成的复合结构
   * 数组被分为一个个桶(bucket)，通过哈希值决定了键值对在这个数组的寻址
   * 哈希值相同的键值对，则以链表形式存储
2. 初始容量16（数组长度）、加载因子0.75、扩容1倍
3. 计算数组下标方法：

   * 计算key的hashCode返回一个哈希码
   * 二次处理哈希码，最终求得键对应的hash值
     * 扰动处理=4次位运算+5次异或运算，加大哈希码低位的随机性，使得分布更均匀，最终减少冲突
   * 哈希码和(数组长度-1)相与(&)，可获得一个哈希码与数组大小范围匹配的数组位置
   
4. put过程：
   * 对Key求Hash值，然后再根据hash值&(n-1)，得到数组的下标位置
   * 如果没有碰撞，直接放入桶（数组）中
   * 如果碰撞了（key的hash值冲突），以链表（链地址法）的方式链接到后面（1.7单链表是头插法、1.8转为红黑树是尾插法）
   * 如果链表长度超过阀值(8)，就把链表转为红黑树（java 1.8）
   * 如果节点已经存在就替换旧值
   * 如果桶满了（容量*加载因子），就需要resize
5. 扩容机制

   1. 保存旧的数组，根据设置的扩容大小（原来的1倍）新建数组
   2. 遍历旧数组数据，并重新计算存储下表位置，然后逐个插入新数组中
   3. 新数组table引用到HashMap的table属性上
   4. 重新弄设置扩容阀值
6. 线程不安全：

   * 并发情形下，两个线程同时对HashMap进行扩容，各线程重新对节点rehash时，会造成节点间的相互引用，从而形成一个环，当数组在该位置get寻找对应的key时，就发生了死循环

* 与1.8比较

  * Java1.7
    * 数组+链表
    * 无冲突时存放数组，冲突时存放单链表（头插法）， 遍历的时间复杂度 O(n)
    * hash值计算方法：hashCode()、扰动处理-4次位运算+5次异或运算
    * 扩容后重新计算存储位置
  * Java1.8
    * 数组+链表+红黑树
    * 无冲突时存放数组，冲突时存放单链表，单链表长度大于8时转为红黑树（尾插法），遍历的时间复杂度就是 O(logn)
    * hash值计算方法：hashCode()、扰动处理-1次位运算+1次异或运算
    * 扩容后位置为原位置或者原位置加旧容量

* 为什么HashMap中String、Integer这样的包装类适合作为key键

  * String、Integer等包装类是final类型，即具有不可变性，保证了key的不可更改性，不会出现放入和获取时哈希码不同的情况

* HashMap中的key若是Object类型，则需要实现哪些方法？

  * hashCode()：计算存储数据的存储位置，实现不恰当会导致严重的Hash碰撞

  * equals()：比较存储位置上是否存在需要存储数据的键key，若存在，则替换为新值，否则，直接插入数据。保证键key在哈希表中的唯一性

* 抛开HashMap，hash冲突有哪些解决方法

  * 哈希算法：将一组关键字映射到一个有限的地址区间上的算法
  * 哈希冲突：由于哈希算法被计算的数据是无限的，而计算后的结果范围有限，因此总会存在不同的数据经过计算后得到的值相同
  
  1. 开放定址法
     * 从发生冲突的那个单元起，按照一定的次序，从哈希表中找到一个空闲的单元。然后把发生冲突的元素存入到该单元的一种方法
  2. 链地址法（拉链法）
     * 将哈希值相同的元素构成一个同义词的单链表
  3. 再哈希法
     * 当发生冲突时，再次通过其他的哈希函数进行计算，直到冲突不再产生
  4. 建立公共溢出区
     * 将哈希表分为公共表和溢出表，当溢出发生时，将所有溢出数据统一放到溢出区

局限：

1. 为什么线程不安全?
   * 并发情形下，两个线程同时对HashMap进行扩容，各线程重新对节点rehash时，会造成节点间的相互引用，从而形成一个环，当在数组该位置get寻找对应的key时，就发生了死循环

2. 在使用迭代器的过程中如果HashMap被修改，那么 ConcurrentModificationException 将被抛出，也即Fast-fail策略。
   * 使用Iterator遍历时使用Iterator的方法进行元素的修改、删除不会报错，但是期间使用map的方法时报错
   * 单线程条件下，为避免出现ConcurrentModificationException，需要保证只通过HashMap本身或者只通过Iterator去修改数据，不能在Iterator使用结束之前使用HashMap本身的方法修改数据 

3. 多线程条件下，可使用Collections.synchronizedMap方法构造出一个同步Map，或者直接使用线程安全的ConcurrentHashMap

* 红黑树

  * 是一种自平衡二叉查找树
  * 在进行插入和删除操作时通过对树进行旋转保持二叉查找树的平衡，从而获得较高的查找性能
  * 可以在O(log n)时间内做查找、插入和删除
  
  * 性质
  
    1. 节点是红色或黑色
    2. 根节点是黑色
    3. 每个叶节点是黑色的
    4. 每个红色节点的两个子节点都是黑色。(从每个叶子到根的所有路径上不能有两个连续的红色节点)
    5. 从任一节点到其每个叶子的所有路径都包含相同数目的黑色节点
  
  * 这些约束强制了红黑树的关键性质：
    * 从根到叶子的最长的可能路径不多于最短的可能路径的两倍长
    * 结果是这个树大致上是平衡的

## ConcurrentHashmap

* 各种并发容器，比如`ConcurrentHashMap`、`CopyOnWriteArrayList`

* 各种线程安全队列(`Queue/Deque`)，如`ArrayBlockingQueue`、`SynchronousQueue`

* key、value不允许为null

* java1.7

  * 整个结构按照`key`的`hash`分成16个`Segment`，每个`Segment`维护一个`HashEntry`数组，`Segment`在实现上继承了`ReentrantLock`，自带锁的功能，能同时让16个线程来操作
  * 采用分段锁，避免了HashTable的整体同步
    * `ConcurrentHashMap`初始化时，计算出`Segment`数组的大小`ssize`和每个`Segment`中`HashEntry`数组的大小`cap`，并初始化`Segment`数组的第一个元素；其中`ssize`大小为2的幂次方，默认为16，`cap`大小也是2的幂次方，最小值为2
  * 其中增删改操作，也可以实施分段锁机制，但是size方法，则需要对整个结构进行上锁

  ![image-20190221100831169](/Users/dingyuanjie/Desktop/mynotes/MD/Java/image-20190221100831169.png)

  * put实现
    * 当执行`put`方法插入数据时，根据key的hash值，在`Segment`数组中找到相应的位置，如果相应位置的`Segment`对象还未初始化，则通过CAS进行赋值初始化，接着执行`Segment`对象的`put`方法通过加锁机制插入数据：
      * 如果线程A和线程B同时执行相同`Segment`对象的`put`方法
        * 线程A执行`tryLock()`方法成功获取锁，则把`HashEntry`对象插入到相应的位置
        * 线程B获取锁失败，会通过重复执行`tryLock()`方法尝试获取锁，次数超过上限时，则执行`lock()`方法挂起线程B；
        * 当线程A执行完插入操作时，会通过`unlock()`方法释放锁，接着唤醒线程B继续执行

  * size实现
    * 先采用不加锁的方式，连续计算元素的个数，最多计算3次：

    1. 如果前后两次计算结果相同，则说明计算出来的元素个数是准确的；
    2. 如果前后两次计算结果都不同，则给每个`Segment`进行加锁，再计算一次元素的个数；

* java1.8

  * 取消了`Segment`分段锁的数据结构，取而代之的是`Node`数组+链表（红黑树）的结构
  * 定位节点的`hash`算法被简化了，这样带来的弊端是`Hash`冲突会加剧。因此在链表节点数量大于8时，会将链表转化为红黑树进行存储
    * `Node` + `CAS` + `Synchronized`来保证并发安全进行实现
  * 锁的粒度调整为对每个数组元素加锁
  

![image-20190221100815834](/Users/dingyuanjie/Desktop/mynotes/MD/Java/image-20190221100815834.png)

* put实现
    * 当执行`put`方法插入数据时，根据`key`的`hash`值，在`Node`数组中找到相应的位置：
    * 如果`key`对应的数组元素为`null`，则通过`CAS`操作将其设置为当前值
      * 如果`key`对应的数组元素(即链表头或者树的根元素)不为`null`，则对该元素使用`synchronized` 关键字加锁，遍历链表更新节点或插入新节点。如果当前链表的长度超过8，则转化为红黑树
    
  * size实现
    * 元素个数保存`baseCount`中（`volatile`类型），部分元素的变化个数保存在`CounterCell`数组中，通过累加`baseCount`和`CounterCell`数组中的数量，即可得到元素的总个数


# 反射机制

* 在运行时能够获得并操作一个类的所有属性和方法

  * 把java类中的各种成分映射成一个个的Java对象
* 应用场景
  * 动态获取类的结构信息（如变量、方法等）和调用对象的方法
  * 动态代理、`Java JDBC`数据库操作等
* 使用
  * 主要通过操作java.lang.Class类，其存放着对应类型对象的运行时信息
  * Class对象的获取
    1. 实例.getClass()
    2. Class类的静态方法：Class.forName("类的权限定名称");
    3. 类的Class属性：类名.Class

* 在`Java`程序运行时，`Java`虚拟机为所有类型维护一个`java.lang.Class`对象

  * 泛型形式为`Class<T>`
  * 每种类型的`Class`对象只有1个 = 地址只有1个

  ```java
  // 对于2个String类型对象，它们的Class对象相同
  Class c1 = "Carson".getClass();
  Class c2 =  Class.forName("java.lang.String");
  // 用==运算符实现两个类对象地址的比较
  System.out.println(c1 ==c2);
  // 输出结果：true
  ```

* Class类中方法

  1. 获取目标类型的`Class`对象

  2. 通过 `Class` 对象分别获取`Constructor`类对象、`Method`类对象 和 `Field` 类对象

  3. 通过 `Constructor`类对象、`Method`类对象和 `Field`类对象分别获取类的构造函数、方法和属性的具体信息，并进行后续操作

     ```java
//在list<String>的实例中，插入int型的数据
     List<String> list = new ArrayList<String>();
     Class<?> clazz = list.getClass();
     //第一个是调用的方法名称，第二个是方法的形参类型
     Method m = clazz.getMethod("add", Object.class);
     //需要两个参数，一个是要调用的对象，一个是实参
     m.invoke(list, 1);
     m.invoke(list, "a");
     m.invoke(list, 2);
     for(Object obj : list) {
         System.out.println(obj);
     }
     //由于反射是在运行时发生，故可以躲避编译
     ```
  
  * 不带`Declared`的方法支持取出包括继承、公有（`Public`）、不包括有（`Private`）的构造函数

  * 带 `Declared`的方法是支持取出包括公共（`Public`）、保护（`Protected`）、默认（包）访问和私有（`Private`）的构造方法，但不包括继承的构造函数

  * 访问权限问题

    * `setAccessible(true) `  //暴力访问(忽略掉访问修饰符)

# 异常处理

`java.lang.Throwable`

- `Error`：与程序本身⽆关，不捕获，如`OutOfMemoryError`、`NoClassDefFoundError`
- `Exception`：程序本身有关，可捕获可不捕获
  - 受检异常
    - 编译期检查的一部分，必须捕获
    - `throws、try/catch`
    - `IOException`、`SQLException`
  - 非受检异常
    - 运行时异常，通常可通过编码避免的逻辑错误
      - 根据需要来判断是否需要捕获，并不会在编译期强制要求
    - `RuntimeException`
      -  `NullPointerException`、`ArrayIndexOutOfBoundsException`、`ClassCastException`
- `NoClassDefFoundError`和`ClassNotFoundException`有什么区别
  - `ClassNotFoundException`：在`ClassPath`下找不到该类
  - `NoClassDefFoundError`：JVM在其内部类定义数据结构中查找了类的定义但未找到它，不一定是类路径问题
- 使用
  - 应该捕获特定异常，尽量不要捕获类似`Exception`这样的通用异常
  - 不要生吞异常、向上抛出或处理异常
  - `e.printStackTrace();`
    - 很难判断出到底输出到哪里去了，无法找到堆栈轨迹
    - 最好使用产品日志，详细地输出到日志系统里
  - `try/catch`主要代码，范围太大会影响JVM对代码进行优化，也比条件语句低效
    - 异常是主逻辑的补充逻辑， 修改补充逻辑会导致主逻辑的修改
    - 实现类的变更会影响调用者，破坏封装性
  - Java每实例化一个`Exception`，都会对当时的栈进行快照，频繁会影响开销

* ⾃定义异常
  * 继承`RuntimeException`
  * 避免包含敏感信息

```java
//throws表示⼀个方法声明可能抛出⼀个异常
//throw表示此处抛出一个已定义的异常(可以是自定义需继承Exception，也可以是java⾃己给出的异常类)
//在Java中只有Throwable类型的实例才可以被抛出(throw)或者捕获(catch)
```

# Servlet

* `Servlet` 是一种基于 `Java` 技术的` Web` 组件，用于生成动态内容，由容器管理

  * 在一个应用程序中，每个Servlet类型只能有一个实例，`Servlet`执行完它的第一个请求之后，就会驻留在内存中，等待后续的请求
  * 一个`Servlet`程序其实就是一个实现了`Java`特殊接口的类，通过实现接口中的方法，告诉程序，初始化需要干什么，接收到请求该做什么，销毁需要干什么
  * `Servlet`接口定义了`Servlet`与`Servlet`容器之间的一个契约：`Servlet`容器会把`Servlet`类加载到内存中，并在`Servlet`实例中调用特定的方法
  * `JSP`页面其实是一个`Servlet`，但是不需要编译`JSP`页面，可以在任何文本编译器中编写，不需要在部署描述符中进行标注或映射成一个`URL`

* `Tomcat`是`Servlet/JSP`容器、`Web`服务器，管理`Servlets`实例以及它们的生命周期，并监听端口

  * 当客户端的请求发送过来后，其根据`url`等信息确定要将请求交给哪个`Servlet`去处理，然后调用该`Servlet`的`service()`方法返回一个`response`对象，`tomcat`再把这个`response`对象返回给客户端，并提供静态内容

* `Tomcat`

  * `conf/server.xml`该文件用于配置`server`相关的信息，比如`tomcat`启动的端口号，配置主机(Host)

  * `web.xml`文件配置与`web`应用（`web`应用相当于一个`web`站点）

  * `tomcat-user.xml`配置用户名密码和相关权限.

  * `webapps`：放置web应用：`./web2/index.html - localhost:8080/web2/index.html`

  * 虚拟主机

    * 多个不同域名的网站共存于一个`Tomcat`中

* 声明
  
  * 注解类型声明`Servlet`：`@WebServlet(...)`
  * `web.xml`中声明`Servlet`
  
* `WEB-INF`目录：可以被`Servlet` 访问，但是不能被用户访问

* `http://localhost:8080/模块名(ServletContext)/servlet名`

* `ServletContext`，表示`Servlet`应用程序，每个`web`应用程序只有一个`context`

* 对于每次访问请求，`Servlet`引擎都会创建一个新的`HttpServletRequest`请求对象和一个新的`HttpServletResponse`响应对象，然后将这两个对象作为参数传递给它调用的`Servlet`的`service()`方法，`service`方法再根据请求方式分别调用`doXXX`方法

* `Tomcat`有哪几种`Connector` 运行模式(优化)？

  * `bio(blocking I/O)`、`nio（non-blocking I/O`）、`apr`（阿帕奇可移植库）
  
* `get`：在URL地址后附带的参数是有限制的，其数据容量通常不能超过1，一般用来获取数据

* `post`：可以在请求的实体内容中向服务器发送数据，传送的数据量无限制，一般用来提交数据

* 转发(`forward`)和重定向(`redirect`)

  * 转发是发生在服务器的，只能去往当前`web`应用的资源，浏览器的地址栏没有发生变化，一次`http`请求
    * A找B借钱，B说没有，B去找C借，借到借不到都会把消息传递给A
    * `request.getRequestDispacther("/资源名 URI").forward(request,response)`
    * 转发时"/"代表的是本应用程序的根目录
  * 重定向是发生在浏览器的，可以去往任何的资源，浏览器的地址会发生变化，两个`http`请求，只能传递字符串，整个页面执行完之后才执行跳转
    * A找B借钱，B说没有，让A去找C借
    * `response.sendRedirect()`
    * `response.send("/web应用/资源名 URI");`
    * 重定向时"/"代表的是`webapps`目录
  
* 线程安全问题

  * 当多个用户访问`Servlet`的时候，服务器会为每个用户创建一个线程**。**当多个用户并发访问`Servlet`共享资源的时候就会出现线程安全问题。
    1. 如果一个变量需要多个用户共享，则应当在访问该变量的时候，加同步机制`synchronized` (对象){}
    2. 如果一个变量不需要共享，则直接在` doGet() `或者 `doPost()`定义.这样不会存在线程安全问题
  
* 生命周期方法

  ```java
  void init(ServletConfig config) throws ServletException
  	//只是第一次请求Servlet时调用，可以编写一些应用程序初始化相关的代码
  void service(ServletRequest request, ServletResponse response)
      //每次请求Servlet时，Servlet容器都会调用
  void destroy()
      //销毁Servlet时调用
      
  String getServletInfo()
      //返回Servlet的描述
  ServletConfig getServletConfig()
      //返回由Servlet容器传给init方法的ServletConfig
  ```

* 隐式对象

  * `request`、`response`、`out`、`session`、`application`、`config`、`pageContext`、`page`、`exception`
  * 属性四种范围：`page`、`request`、`session`、`application`

* EL
  
  * 被设计成能够轻松的编写无脚本或不包含`Java`代码的JSP页面，`${}`
  
* JSTL
  * 是一个定制标签类库的集合
  * 是指标准标签类库，通过多个标签类库来显露其动作指令的

## 监听器

* 创建监听器：只要创建一个实现相关接口的`Java`类即可
* 注册监听器：`@WebListener`、部署描述符中`<listener>`
  * `ServletContext`监听器
    * `ServletContextListener`会对`ServletContext`的初始化和解构做出响应
  * `Session`监听器
    * `HttpSessionListener`：当有`HttpSession`被创建或者销毁时，`Servlet`容器就会调用所有已注册的`HttpSessionListener`
  * `ServletRequest`监听器
    * `ServletRequestListener`对`ServletRequest`的创建和销毁做出响应

## 过滤器

* 拦截请求，并对传给被请求资源的`ServletRequest`或`ServletResponse`进行处理的一个对象
* 可以配置为拦截一个或多个资源，过滤顺序为`web.xml`中出现顺序；
* 可通过注解或者部署描述符
* 必须实现`Filter`接口，为每个过滤器暴露3个生命周期方法
  * `init()`、`doFilter`、`destroy`

## Servlet中Filter和SpringMVC的Interceptor区别

* `Filter`过滤器

  * 基于`Servlet`的规范，采用函数回调机制实现
  
  * 主要功能是对web资源访问的管理控制
  
  * 比如实现URL级别的权限访问控制、设置字符编码、过滤敏感词汇等
  
  * 在每次请求过来时，都会调用`doFilter`方法
  
    * 其有个`FilterChain`的对象参数，它提供了一个`doFilter`方法，可以根据需求决定是否调用，如果调用，则`web`服务器就会调用`web`资源的`service`方法，即`web`资源就会被访问，否则`web`资源不会被访问
  
    ```java
    public interface Filter {   
      public void init(FilterConfig filterConfig) throws ServletException;  
      public void doFilter(ServletRequest request, ServletResponse response,
                           FilterChain chain)
              throws IOException, ServletException;
      public void destroy();
    }
    ```
  
* `Interceptor`拦截器

  * 是在`Spring`容器内的，是`Spring`框架支持的
  
  * 主要是提供一种机制可以在一个`Action`执行前后执行一些操作
  
    ```java
    public interface HandlerInterceptor {
        //在请求处理之前调用，也就是Controller方法之前；
        boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception;
       //Controller 方法调用之后执行，但是它会在DispatcherServlet 进行视图返回渲染之前被调用
        void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView)throws Exception;
       //将在整个请求结束之后，也就是在DispatcherServlet 渲染了对应的视图之后执行，这个方法的主要作用是用于进行资源清理工作的
        void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex)throws Exception;
      }
    ```
* `Filter`和`Interceptor`的执行顺序
* 过滤前-拦截前-action执行-拦截后-过滤后
  
* 主要区别

  1. `Filter`依赖于`Servlet`容器，只能用于`web`程序中，而`Interceptor`不依赖于`Servlet`容器，在`Spring`容器中，既可用于`web`容器，也可以用于`Application`
  2. `Filter`是基于函数回调，而`Interceptor`是基于java的反射机制
     * `Spring`里的`HandlerInterceptor`，是通过循环的方式在`handler`处理请求前后分别调用`preHandle()`方法和`postHandle()`方法对请求和响应进行处理
  3. `Interceptor`配置在`Spring`文件中，可以获取`Spring`里任何资源和对象，比如`Controller`上下文；而`Filter`不能访问

# Session 

* 在`Java`中，`HTTP`的`Session`对象用`javax.servlet.http.HttpSession`来表示
  * `HttpSession session = request.getSession();`
    * request中没有则新建
  * `HttpSession session = request.getSession(true);`
    * request中没有则新建
  * `HttpSession session = request.getSession(false);`
    * request中没有则返回null
* `Session`代表服务器与浏览器的一次会话过程，这个过程是连续的，也可以时断时续的
* `Session`创建的时间
  * 服务端程序调用`HttpServletRequest.getSession(true)`时才创建
  * `Session`因为请求（`request`对象）而产生，同一个会话中多个`request`共享了一`session`对象，可以直接从请求中获取到`Session`对象
  * 其实，`Session`的创建和使用总在服务端，而浏览器从来都没得到过`Session`对象。但浏览器可以请求`Servlet`（`jsp`也是`Servlet`）来获取`Session`的信息。
  * 客户端浏览器真正紧紧拿到的是`session ID`，而这个对于浏览器操作的人来说，是不可见的，并且用户也无需关心自己处于哪个会话过程中
* `Session`使用的基本原理
  * 当JSP页面没有显式禁止`Session`的时候，在打开浏览器第一次请求该`jsp`的时候，服务器会自动为其创建一个`Session`，并赋予其一个`SessionID`用来标识该`Session`对象，发送给客户端的浏览器。
  * 以后客户端接着请求本应用中其他资源的时候，会自动在请求头上添加：`Cookie:JSESSIONID`=客户端第一次拿到的`Session ID`
  * 这样，服务器端在接到请求时候，就会收到`Session ID`，并根据`Session ID`在内存中找到之前创建的`Session`对象，提供给请求使用
* `Session`删除的时间是
  * `Session`超时：在设置`Session`有效期内没有再次收到该`Session`所对应客户端的请求
  * 程序调用`HttpSession.invalidate()`
  * 服务器关闭或服务停止
  * `Session`不会因为浏览器的关闭而删除
* `Session`存放在哪里：
  * 服务器端的内存中。不过`Session`可以通过特殊的方式做持久化管理
  * `Session`是一个容器，可以存放会话过程中的任何对象
* `Cookie`和`Session`的关联关系
* `HTTP`是无状态协议：客户端发起N次请求，服务端并不知道是同一个客户端发送的；
  * 当客户端第一次请求服务器时，服务器为客户端用户创建对应的`Session`对象，并返回标识该`Session`对象的`SessionID`给客户端，下次客户端带着`SessionID`，即`Cookie`来请求服务器时，服务器就能知道是哪个客户端用户请求的，然后就可以做相应的响应等——基本原理
    * `Session`保存用户信息，用户每次在访问同一个客户端向服务端发送请求时，服务端就能知道是同一个客户端。
    * `Cookie`：存储客户端信息
    * 在`HTTP`协议有状态操作中依赖`Session`和`Cookie`完成有状态协议过程
  * 区别：
    * `Cookie`只能存储字符串，`Session`可以存储任何类型数据，可以把`Session`看成是一个容器
    * `Cookie`存储在浏览器中，`Session`存储在服务器上
    * 如果浏览器禁用了`Cookie`，那么`Cookie`是无用的了；如果浏览器禁用了`Cookie`，`Session`可以通过`URL`地址重写来进行会话跟踪

# 注解

* 其实就是代码中的特殊标记，可以在编译、类加载、运行时被读取，并执行相对应的处理

* 作用
  * 让编译器检查代码
  * 让数据注入到类、成员变量、方法上

* 在JDK中注解分为了：

1. 基本`Annotation`
   * 常用于标记方法来抑制编译器警告
2. 元`Annotaion`
   * 标记其他注解的注解

* 自定义注解

  * 标记注解：没有任何成员变量的注解，`@Overried`就是一个标记注解

    ```java
    //有点像定义⼀个接⼝⼀样，只不过它多了⼀个@ 
    public @interface MyAnnotation {
    }
    ```

  * 元数据`Annotation`：定义带成员变量的注解

    ```java
    //在注解上定义的成员变量只能是String、数组、Class、枚举类、注解 
    //注解的作⽤就是给类、方法注⼊信息
    public @interface MyAnnotation {
    	//定义了两个成员变量 
        String username(); 
        int age();
    }
    ```

  * 使⽤⾃定义注解

    ```java
    public @interface MyAnnotation { 
      //定义了两个成员变量
    	String username() default "zicheng"; 
      int age() default 23;
      int[] arr() default {1,2,3}; //这里不能使用new int[]{1,2,3}，@MyAnn(arr={1,2,3})
    }
    //注解拥有什什么属性，在修饰的时候就要给出相对应的值 
    @MyAnnotation(username = "zhongfucheng", age = 20) 
    public void add(String username, int age) {
    }
    
    //在修饰的时候就不需要给出具体的值了 
    @MyAnnotation()
    public void add(String username, int age) {
    }
    ```

    ```java
    //注解上只有一个属性，并且属性的名称为value，那么在使⽤的时候，可以不写value，直接赋值给 它就行
    public @interface MyAnnotation2 {
        String value();
    }
    //使用注解，可以不指定value，直接赋值 
    @MyAnnotation2("zhongfucheng") 
    public void find(String id) {
    }
    ```

* 把自定义注解的基本信息注入到方法上

  * 利用的是反射技术`getAnnotation(XXXAnnotation.class)`
* 反射出该类的方法
  * 通过方法得到注解上具体的信息
  * 将注解上的信息注⼊到方法上
  
```java
  //需要在自定义注解上加入这样⼀句代码 
  @Retention(RetentionPolicy.RUNTIME)
  //反射出该类的⽅法
  Class aClass = Demo2.class;
  Method method = aClass.getMethod("add", String.class, int.class);
  //通过该方法得到注解上的具体信息
  MyAnnotation annotation = method.getAnnotation(MyAnnotation.class); 
  String username = annotation.username();
  int age = annotation.age();
  //将注解上的信息注⼊到方法上
  Object o = aClass.newInstance(); 
  method.invoke(o, username, age);
```

* JDK的元注解
  * `@Retention`

    * ⽤于指定被修饰的`Annotation`被保留多⻓时间

      ```java
      //SOURCE、CLASS(默认)、 RUNTIME（运行时，一般反射时用）
      //运行时
      @Retention(RetentionPolicy.RUNTIME)
      ```

  * `@Target`

    * 指定被修饰的`Annotation`⽤于修饰哪些程序单元（如类、方法、变量等）

      ```java
      //注解只能用在类和方法上
      @Target({ElementType.TYPE, ElementType.METHOD}) 
      ```

    * 只有一个`value`成员变量的，该成员变量的值是以下的

      * `TYPE`（类）, `FIELD`,`METHOD`, `PARAMETER`,`CONSTRUCTOR`,`LOCAL_VARIABLE`,`ANNOTATION_TYPE`,` PACKAGE`

  * `@Documented`

    * 指定被该`Annotation`修饰的`Annotation`类将被`javadoc`工具提取成⽂档

  * `@Inherited`

    * 被修饰过的`Annotation`将具有继承性

    * `@Inherited`标记注解@A，@A标记类B，C类继承B类，那么C类也具有注解@A

* 使用过程
  * 定义注解类：框架的工作
  * 注解的使用范围：类（接口和枚举类型）、属性、方法、构造器、包、参数、局部变量

  ```java
  //定义注解处理器
  //可通过反射获取method上的注解信息
  method.getAnnotation(Retry.class);
  //可通过反射执行方法
  method.invoke(new Object(), "57890");  
  ```

# IO

* `IO`操作目标：文件、`Socket`通信等
* 输入流、输出流`(InputStream/OutputStream)`是用于读取或写入字节的，例如操作图片文件
* `Reader/Writer`则是用于操作字符，增加了字符编解码等功能，适用于类似从文件中读取或者写入文本信息
* `BuferedOutputStream`等带缓冲区的实现，可以避免频繁的磁盘读写，进而提高IO处理效率，flush

* `Java`提供的`IO`方式
  * `BIO`，基于流模型实现
    * 如`File`抽象、输入输出流，交互方式是同步、阻塞的方式
    * 进行同步操作时，后续的任务是等待当前调用返回，才会进行下一步
  * `NIO`，提供了`Channel`、`Selector`、`Bufer`等新的抽象，可以构建多路复用的、同步非阻塞IO程序
    * `Selector`，是NIO实现多路复用的基础，它提供了一种高效的机制，可以检测到注册在`Selector`上的多个`Channel`中，是否有`Channel`处于就绪状态，进而实现了单线程对多`Channel`的高效管理
    * 对于多路复用`IO`，当出现有的`IO`请求在数据拷贝阶段，会出现由于资源类型过份庞大而导致线程长期阻塞，最后造成性能瓶颈的情况
  * `AIO`，异步非阻塞`IO`
    * 异步`IO`操作基于事件和回调机制
    * 可以简单理解为，应用操作直接返回，而不会阻塞在那里，当后台处理完成，操作系统会通知相应线程进行后续工作

* 内存⻆度

* 字节流：8位字节`（InputStream、OutputStream）`
* 字符流：16位字符`（Reader、Writer）`
* IO流：
  * 底层节点流 (连接实际数据源)—— 包装 —— 上层处理流
  * 上层处理流(对已存在的流进行连接或封装)
* `windows`路径下包括反斜线，应使用两条`F:\abc\a.txt`，或直接使⽤`/`，换⾏符`\r\n`
* `Linux` 换行符 `\n`

## File

* `File`类：⽂件和⽬录(新建、删除、重命名文件目录)，但不能访问文件内容本身——输入/输出流

* `InputStream`

  ```java
  int read() 从输入流中读取单个字节，返回所读取的字节数据
  int read(byte[] b) 从输入流中最多读取b.length个字节数据，并存在b中，返回实际读取的字节数
  int read(byte[] b ,int off, int len)
  ```

  ```java
  FileOutputStream fos = new FileOutputStream("b.java"); 
  FileInputStream fis = new FileInputStream("a.java"); 
  byte[] buf = new byte[1024];
  int hasRead = 0; //用于保存实际读取的字节数
  while((hasRead = fis.read(buf)) > 0) {
      fos.write(buf, 0, hasRead);
  }
  fos != null fos.close();
  fis != null fis.close();
  ```

* `Reader`

  ```java
  int read() 从输⼊流中读取单个字符，返回所读取的字符数据
  int read(char[] b) 从输⼊流中最多读取b.length个字符数据，并存在b中，返回实际读取的字符数
  int read(char[] b ,int off, int len)
  ```

* 处理流

  * 使用处理流来包装节点流(构造器参数物理IO节点)，程序通过处理流(构造器参数为已存的流)来执行输入/输出的功能

  * 让节点流与底层的I/O设备、⽂件交互
  * 关闭资源时，只要关闭最上层的处理流

* 缓冲流

  * `BufferedInputStream`

  * `flush()`

  * `BufferedReader readLine() != null`

* 转换流

  * `InputStreamReader` 字节输⼊流 —— > 字符输入流

  * `OutputStreamWriter` 字节输出流 ——> 字符输出流

* 与系统有关的默认名称分隔符(跨系统的斜杠)：`File.separator`

* 与系统有关的路径分隔符：在 UNIX 系统上，此字段为 ':'；在` Microsoft Windows` 系统上，它为 ';' `File.pathSeparatorChar`

* [文件工具](http://blog.sina.com.cn/s/blog_abb3c2e90102uyz3.html)

* Java有几种文件拷贝方式?哪一种最高效?

  * `Java.io`类库直接为源文件构建一个`FileInputStream`读取，然后再为目标文件构建一个`FileOutputStream`，完成写入工作
    * 使用输入输出流进行读写时，实际上是进行了多次上下文切换，会带来一定的额外开销降低IO效率
      * 比如应用读取数据时，先在内核态将数据从磁盘读取到内核缓存，再切换到用户态将数据从内核缓存读取到用户缓存。写入操作也是类似，仅仅是步骤相反
  * `java.nio`类库提供的`transferTo` 或`transferFrom`方法实现
    * `NIO transferTo/From`的方式可能更快，因为它更能利用现代操作系统底层机制，避免不必要拷贝和上下文切换 
    * 在`Linux`和`Unix`上，则会使用到零拷贝技术，数据传输并不需要用户态参与，省去了上下文切换的开销和不必要的内存拷贝，进而可能提高应用拷贝性能 
      * `transferTo` 不仅可用在文件拷贝中，也可用于读取磁盘文件，然后进行`Socket`发送
  * `Files.copy`的实现

# 工具类

* `replace`与`replaceAll`的区别
* `replace`既可以支持字符的替换，也支持字符串的替换
  * `replaceAll`的参数是`regex`，即基于规则表达式的替换，比如：可以通过`replaceAll("\d", "*")`把一个字符串所有的数字符都换成星号
  * 都是全部替换，即把源字符串中的某一字符或字符串全部换成指定的字符或字符串
* `Path`是操作系统环境变量，`ClassPath`是`Class`文件路径
* 接口不能实现其他接口，可以继承其他接口

# 简单算法

## Java实现LRU（最近最少使用）策略的缓存

```java
public class LRUCache2 {
    private HashMap<Integer, Integer> cacheMap = new HashMap<>();
    private LinkedList<Integer> recentlyList = new LinkedList<>();
    private int capacity;

    public LRUCache2(int capacity) {
        this.capacity = capacity;
    }

    private int get(int key) {
        if (!cacheMap.containsKey(key)) {
            return -1;
        }
       // 不带（Integer）则是根据下标删除，可能会导致数组越界异常
        recentlyList.remove((Integer) key);
        recentlyList.add(key);
        return cacheMap.get(key);
    }

    private void put(int key, int value) {
        if (cacheMap.containsKey(key)) {
           // 不带（Integer）则是根据下标删除，可能会导致数组越界异常
            recentlyList.remove((Integer) key);
        }
        if (cacheMap.size() == capacity) {
            cacheMap.remove(recentlyList.removeFirst());
        }
        cacheMap.put(key, value);
        recentlyList.add(key);
    }

    public static void main(String[] args) {
        LRUCache2 cache = new LRUCache2(2);
        cache.put(1, 1);
        cache.put(2, 2);
        System.out.println(cache.get(1)); // returns 1
        cache.put(3, 3); // 驱逐 key 2
        System.out.println(cache.get(2)); // returns -1 (not found)
        cache.put(4, 4); // 驱逐 key 1
        System.out.println(cache.get(1)); // returns -1 (not found)
        System.out.println(cache.get(3)); // returns 3
        System.out.println(cache.get(4)); // returns 4
    }
}
```

## 排序

* 冒泡

  ```java
  public static int[] bubbleSort(int[] arr) {
      int len = arr.length;
      for (int i = 0; i < len - 1; i++) {
          for (int j = 0; j < len - 1 - i; j++) {
              //内循环比较
              if (arr[j] > arr[j+1]) {        // 相邻元素两两对比
                  int temp = arr[j+1];        // 元素交换
                  arr[j+1] = arr[j];
                  arr[j] = temp;
              }
          }
      }
      return arr;
  }
  ```

* 选择

  ```java
  public static int[] selectionSort(int[] arr) {
      int len = arr.length;
      int minIndex, temp;
      for (int i = 0; i < len - 1; i++) {
          minIndex = i;
          for (int j = i + 1; j < len; j++) {
              if (arr[j] < arr[minIndex]) {     // 寻找最小的数
                  minIndex = j;                 // 将最小数的索引保存
              }
          }
          temp = arr[i];
          arr[i] = arr[minIndex];
          arr[minIndex] = temp;
      }
      return arr;
  }
  ```

* 插入

  ```java
  public static int[] insertionSort(int[] arr) {
      int len = arr.length;
      int preIndex, current;
      for (int i = 1; i < len; i++) {
          preIndex = i - 1;
          current = arr[i];
          while (preIndex >= 0 && arr[preIndex] > current) {
              arr[preIndex + 1] = arr[preIndex];
              preIndex--;
          }
          arr[preIndex + 1] = current;
      }
      return arr;
  }
  ```

* 快速

  ```java
  /*
  1. 设置两个变量i和j，排序开始的时候i=0, j=6;
  2. 以第一个数组元素作为关键数据，赋值给key，即key=a[0]
  3. 从j开始向前搜索，即由后开始向前搜索(j--)，找到第一个小于key的值，两者交换
  4. 从i开始向后搜索，即由前开始向后搜索(i++)，找到第一个大于key的值，两者交换
  5. 重复第3、4步，直到i=j；此时将key赋值给a[i]
  */
   public static int[] quickSort(int[]a, int low, int high) {
       int i = low;
       int j = high;
       //当数组的第一位序号小于最后一位序号时，需要排序；也就是传进来的低位和高位是一样的，数组长度为1的时候，不需要排序
       if(low < high) {
           int key = a[low];
           //由于每次比较i++、j--，直到i = j时停止循环
           while (i < j) {
               //先将最后一位和key比较，直到找到比key小的，进行互换位置，然后再从头和key比较
               while (key <= a[j] && i < j) {
                   j --;
               }
               a[i] = a[j];
               while (key >= a[i] && i < j) {
                   i++;
               }
               a[j] = a[i];
               a[i] = key;
           }
           //采用递归，分别对两边再进行排序
           //左边
           quickSort(a, low, i - 1);
           //右边
           quickSort(a, i + 1, high);
       }
       return a;
   }
  ```

## 二分法查找

```java
//递归
public static int halfSearch(int[] a, int left, int right, int target) {
    int mid = (left + right) / 2;
    int midValue = a[mid];
    if(left <= right) {
        if(midValue > target) {
            return halfSearch(a, left, mid - 1, target);
        } else if(midValue < target) {
            return halfSearch(a, mid + 1, right, target);
        } else {
            return mid;
        }
    }
    return -1;
}
```

```java
//非递归
public static int halfSearch2(int []a, int target) {
    int i = 0;
    int j = a.length - 1;

    while (i <= j) {
        int mid = (i + j) / 2;
        int midValue = a[mid];
        if(midValue > target) {
            j = mid - 1;
        } else if(midValue < target) {
            i = mid + 1;
        } else {
            return mid;
        }
    }
    return -1;
}
```

## 斐波拉契数列

```java
//0、1、1、2、3、5、8、13、21、34 ...
// 返回斐波那契数第n个值,n从0开始
public static int getFib(int n){
    if(n < 0){
        return -1;
    }else if(n == 0){
        return 0;
    }else if(n == 1 || n ==2){
        return 1;
    }else{
        return getFib(n - 1) + getFib(n - 2);
    }
}
```

```java
public static int getFib2(int n){
    if(n < 0){
        return -1;
    }else if(n == 0){
        return 0;
    }else if (n == 1 || n == 2){
        return 1;
    }else{
        int a = 1, b = 1,c = 0;
        for(int i = 3; i <= n; i++){
            c = a + b;
            a = b;
            b = c;
        }
        return c;
    }
}
```
