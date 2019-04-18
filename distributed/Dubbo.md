# MS

* Dubbo 和 Spring Cloud 有什么区别？	

  * 通信方式不同
    * Dubbo 使用的是 RPC 通信，而 Spring Cloud 使用的是 HTTP RESTFul 方式
  * 组成部分不同

* dubbo都支持什么协议，推荐用哪种？

  * dubbo://（推荐）、rim://、hessian://、http://、webservice://、redis://、rest://

  1. dubbo： 单一长连接和NIO异步通讯，适合大并发小数据量的服务调用，以及消费者远大于提供者。传输协议TCP，异步，Hessian序列化；
  2. rmi： 采用JDK标准的rmi协议实现，传输参数和返回参数对象需要实现Serializable接口，使用java标准序列化机制，使用阻塞式短连接，传输数据包大小混合，消费者和提供者个数差不多，可传文件，传输协议TCP。 多个短连接，TCP协议传输，同步传输，适用常规的远程服务调用和rmi互操作。在依赖低版本的Common-Collections包，java序列化存在安全漏洞；
  3. webservice： 基于WebService的远程调用协议，集成CXF实现，提供和原生WebService的互操作。多个短连接，基于HTTP传输，同步传输，适用系统集成和跨语言调用；http： 基于Http表单提交的远程调用协议，使用Spring的HttpInvoke实现。多个短连接，传输协议HTTP，传入参数大小混合，提供者个数多于消费者，需要给应用程序和浏览器JS调用；
  4. hessian： 集成Hessian服务，基于HTTP通讯，采用Servlet暴露服务，Dubbo内嵌Jetty作为服务器时默认实现，提供与Hession服务互操作。多个短连接，同步HTTP传输，Hessian序列化，传入参数较大，提供者大于消费者，提供者压力较大，可传文件；
  5. memcache： 基于memcached实现的RPC协议
  6. redis： 基于redis实现的RPC协议

* Dubbo需要 Web 容器吗？

  * 不需要，如果硬要用 Web 容器，只会增加复杂性，也浪费资源。

* Dubbo内置了哪几种服务容器？

  * Spring Container、Jetty Container、Log4j Container
  * Dubbo 的服务容器只是一个简单的 Main 方法，并加载一个简单的 Spring 容器，用于暴露服务

* Dubbo里面有哪几种节点角色？

  * Provider：暴露服务的服务提供方
  * Consumer：调用远程服务的服务消费方
  * Registry：服务注册与发现的注册中心
  * Monitor：统计服务的调用次数和调用时间的监控中心 
  * Container：服务运行容器

* Dubbo默认使用什么注册中心，还有别的选择吗？

  * 推荐使用 Zookeeper 作为注册中心，还有Multicast、Simple、 Redis，但不推荐

* Dubbo有哪几种配置方式

  * Spring 配置方式 
  * Java API 配置方式

* Dubbo 核心的配置有哪些？

  * dubbo:service - 服务配置、dubbo:reference - 引用配置、 dubbo:protocol  -  协议配置、 dubbo:application -  应用配置 、dubbo:module  -  模块配置、 dubbo:registry -  注册中心配置 、dubbo:monitor  -  监控中心配置、 dubbo:provider  -  提供方配置、 dubbo:consumer  -  消费方配置、 dubbo:method  -  方法配置、 dubbo:argument - 参数配置

* 在 Provider 上可以配置的 Consumer 端的属性有哪些？

  * timeout：方法调用超时 、retries：失败重试次数，默认重试 2 次 、loadbalance：负载均衡算法，默认随机 、actives 消费者端，最大并发调用限制

* Dubbo启动时如果依赖的服务不可用会怎样？

  * Dubbo 缺省会在启动时检查依赖的服务是否可用，不可用时会抛出异常，阻止 Spring 初始化完成，默认 check="true"，可以通过 check="false" 关闭检查

* Dubbo推荐使用什么序列化框架，你知道的还有哪些？

  * 推荐使用Hessian序列化，还有Duddo、FastJson、Java自带序列化

* Dubbo默认使用的是什么通信框架，还有别的选择吗？

  * Dubbo 默认使用 Netty 框架，也是推荐的选择，另外内容还集成有Mina、Grizzly。

* 注册了多个同一样的服务，如果测试指定的某一个服务呢？

  * 可以配置环境点对点直连，绕过注册中心，将以服务接口为单位，忽略注册中心的提供者列表

* Dubbo支持服务多协议吗？

  * Dubbo 允许配置多协议，在不同服务上支持不同协议或者同一服务上同时支持多种协议

* dubbo在安全机制方面如何解决的？

  * dubbo通过token令牌防止用户绕过注册中心直连，然后在注册中心管理授权，dubbo提供了黑白名单，控制服务所允许的调用方。

* 在使用过程中都遇到了些什么问题？ 如何解决的？

  * 同时配置了XML和properties文件，则properties中的配置无效，只有XML没有配置时，properties才生效
  * dubbo缺省会在启动时检查依赖是否可用，不可用就抛出异常，阻止spring初始化完成，check属性默认为true。测试时有些服务不关心或者出现了循环依赖，将check设置为false
  * 为了方便开发测试，线下有一个所有服务可用的注册中心，这时，如果有一个正在开发中的服务提供者注册，可能会影响消费者不能正常运行
    * 解决：让服务提供者开发方，只订阅服务，而不注册正在开发的服务，通过直连测试正在开发的服务。设置dubbo:registry标签的register属性为false
  * spring 2.x初始化死锁问题
    * 在spring解析到dubbo:service时，就已经向外暴露了服务，而spring还在接着初始化其他bean，如果这时有请求进来，并且服务的实现类里有调用applicationContext.getBean()的用法。getBean线程和spring初始化线程的锁的顺序不一样，导致了线程死锁，不能提供服务，启动不了
    * 解决：不要在服务的实现类中使用applicationContext.getBean();如果不想依赖配置顺序，可以将dubbo:provider的deplay属性设置为-1，使dubbo在容器初始化完成后再暴露服务
  * 服务注册不上
    * 检查dubbo的jar包有没有在classpath中，以及有没有重复的jar包
    * 检查暴露服务的spring配置有没有加载
    * 在服务提供者机器上测试与注册中心的网络是否通
  * 出现RpcException: No provider available for remote service异常
    * 表示没有可用的服务提供者
      * 检查连接的注册中心是否正确
      * 到注册中心查看相应的服务提供者是否存在
      * 检查服务提供者是否正常运行
  * 出现”消息发送失败”异常
    * 通常是接口方法的传入传出参数未实现Serializable接口

* 当一个服务接口有多种实现时怎么做？

  * 当一个接口有多种实现时，可以用 group 属性来分组，服务提供方和消费方都指定同一个 group 即可

* 服务上线怎么兼容旧版本？

  * 可以用版本号（version）过渡，多个不同版本的服务注册到注册中心，版本号不同的服务相互间不引用。这个和服务分组的概念有一点类似

* Dubbo可以对结果进行缓存吗？

  * 可以，Dubbo 提供了声明式缓存，用于加速热门数据的访问速度，以减少用户加缓存的工作量

* Dubbo服务之间的调用是阻塞的吗？

  * 默认是同步等待结果阻塞的，支持异步调用
  * Dubbo 是基于 NIO 的非阻塞实现并行调用，客户端不需要启动多线程即可完成并行调用多个远程服务，相对多线程开销较小，异步调用会返回一个 Future 对象

* Dubbo支持分布式事务吗？

  * 目前暂时不支持

* Dubbo telnet 命令能做什么？

  * dubbo 通过 telnet 命令来进行服务治理
  * telnet localhost 8090

* Dubbo支持服务降级吗？

  * Dubbo 2.2.0 以上版本支持

* Dubbo如何优雅停机？

  * Dubbo 是通过 JDK 的 ShutdownHook 来完成优雅停机的，所以如果使用 kill -9 PID 等强制关闭指令，是不会执行优雅停机的，只有通过 kill PID 时，才会执行

* 服务提供者能实现失效踢出是什么原理？

  * 服务失效踢出基于 Zookeeper 的临时节点原理

* 如何解决服务调用链过长的问题？

  * Dubbo 可以使用 Pinpoint 和 Apache Skywalking(Incubator) 实现分布式服务追踪，当然还有其他很多方案
  * 可以结合zipkin实现分布式服务追踪

* 服务读写推荐的容错策略是怎样的？

  * 读操作建议使用 Failover 失败自动切换，默认重试两次其他服务器
  * 写操作建议使用 Failfast 快速失败，发一次调用失败就立即报错

* Dubbo必须依赖的包有哪些？

  * Dubbo 必须依赖 JDK，其他为可选

* Dubbo的管理控制台能做什么

  * 管理控制台主要包含：路由规则，动态配置，服务降级，访问控制，权重调整，负载均衡，等管理功能

* 说说 Dubbo 服务暴露的过程

  * Dubbo 会在 Spring 实例化完 bean 之后，在刷新容器最后一步发布 ContextRefreshEvent 事件的时候，通知实现了 ApplicationListener 的 ServiceBean 类进行回调 onApplicationEvent 事件方法，Dubbo 会在这个方法中调用 ServiceBean 父类 ServiceConfig 的 export 方法，而该方法真正实现了服务的（异步或者非异步）发布

* Dubbo 停止维护了吗

  * 2014 年开始停止维护过几年，17 年开始重新维护，并进入了 Apache 项目

* Dubbo 和 Dubbox 有什么区别

  * Dubbox 是继 Dubbo 停止维护后，当当网基于 Dubbo 做的一个扩展项目，如加了服务可 Restful 调用，更新了开源组件等

* 你还了解别的分布式框架吗

  * 别的还有 Spring cloud、Facebook 的 Thrift、Twitter 的 Finagle 等、Hessian

* Dubbo 能集成 Spring Boot 吗？
  * 可以
* 在使用过程中都遇到了些什么问题
  * Dubbo 的设计目的是为了满足高并发小数据量的 rpc 调用，在大数据量下的性能表现并不好，建议使用 rmi 或 http 协议

# Dubbo

## 简介

### 是什么

是一款高性能、轻量级的开源Java RPC框架，它提供了三大核心能力：==面向接口的远程方法调用==，==智能容错和负载均衡==，以及==服务自动注册和发现==。

### 为什么使用

* 当服务越来越多时，服务 URL 配置管理变得非常困难，F5 硬件负载均衡器的单点压力也越来越大，此时需要一个服务注册中心，动态的注册和发现服务，使服务的位置透明

  并通过在消费方获取服务提供方地址列表，实现软负载均衡和 Failover，降低对 F5 硬件负载均衡器的依赖，也能减少部分成本

* 当进一步发展，服务间依赖关系变得错踪复杂，甚至分不清哪个应用要在哪个应用之前启动，架构师都不能完整的描述应用的架构关系

  这时，需要自动画出应用间的依赖关系图，以帮助架构师理清理关系

* 服务的调用量越来越大，服务的容量问题就暴露出来，这个服务需要多少机器支撑?什么时候该加机器?

  第一步，要将服务现在每天的调用量，响应时间，都统计出来，作为容量规划的参考指标

  其次，要可以动态调整权重，在线上，将某台机器的权重一直加大，并在加大的过程中记录响应时间的变化，直到响应时间到达阀值，记录此时的访问量，再以此访问量乘以机器数反推总容量

### 特性

* #### 面向接口代理的高性能RPC调用

  提供高性能的基于代理的远程调用能力，服务以接口为粒度，为开发者屏蔽远程调用底层细节

* #### 智能负载均衡

  内置多种负载均衡策略，智能感知下游节点健康状况，显著减少调用延迟，提高系统吞吐量

* #### 服务自动注册与发现

    支持多种注册中心服务，服务实例上下线实时感知

* #### 高度可扩展能力

  遵循微内核+插件的设计原则，所有核心能力如Protocol、Transport、Serialization被设计为扩展点，平等对待内置实现和第三方实现

* #### 运行期流量调度

  内置条件、脚本等路由策略，通过配置不同的路由规则，轻松实现灰度发布，同机房优先等功能

* #### 可视化的服务治理与运维

  提供丰富服务治理、运维工具：随时查询服务元数据、服务健康状态及调用统计，实时下发路由策略、调整配置参数

## 原理

![image-20181220174611212](/Users/dingyuanjie/Desktop/MyKnowledge/2.code/java/2.咕泡学院/02.分布式专题/05.分布式服务治理/image-20181220174611212-5299171.png)

## 使用

dubbo服务容器是一个standalone的启动程序，因为后台服务不需要Tomcat或JBoss等Web容器的功能，如果硬要用Web容器去加载服务提供方，增加复杂性，也浪费资源

==服务容器只是一个简单的Main方法，并加载一个简单的Spring容器，用于暴露服务==

服务容器的加载内容可以扩展，内置了spring, jetty, log4j等加载，可通过Container扩展点进行扩展

> 1. 配置pom文件，MAVEN打包duboo可执行jar——Maven install
> 2. 在启动服务的时候，直接使用java -jar xxx.jar & 命令执行jar包即可 (&-后台运行)
>
> 由框架本身提供，可实现优雅关机

```java
//dubbo-server
server-api
	提供接口
------------------------------------------------------------------------------------------
server-provider
	加入dubbo、zookeeper、zkClient依赖
	//1. tomcat启动服务
		//将dubbo服务项目直接添加到容器中启动即可，不需要做任何配置
		//但是增加了复杂性（端口、管理等方面），也浪费资源（内存）
	//2. 自建main方法启动服务
		//可用于本地调试
		public static void main(String[] args) throws IOException {
			ClassPathXmlApplicationContext context=  new ClassPathXmlApplicationContext
				("META-INF/spring/dubbo-server.xml");
			context.start();
			System.in.read(); //阻塞当前进程
		}
	//（推荐）3. dubbo提供的main方法启动服务
	//默认加载main目录下resources中META-INF/spring目录中的所有Spring配置文件（../spring目录中dubbo-server.xml）
	// META-INF中log4j.properties
		public class Main(){
			public static void main(String[] args) {
			//默认情况下会使用spring容器来启动服务
			 //com.alibaba.dubbo.container.Main.main(args);
 			 com.alibaba.dubbo.container.Main.main(new String[]{"spring","log4j"};
			}
		}
------------------------------------------------------------------------------------------
//dubbo-client
加入dubbo、zookeeper、zkclient依赖
 public static void main( String[] args ) throws IOException, InterruptedException {
 	//resources/dubbo-client.xml
	ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext
		("dubbo-client.xml");
	//得到IGpHello的远程代理对象
	IGpHello iGpHello = (IGpHello) context.getBean("gpHelloService");
	System.out.println(iGpHello.sayHello("Mic"));
	Thread.sleep(4000);
	System.in.read();
}
```

```xml
<?xml version="1.0" encoding="UTF-8"?>
<beans xmlns="http://www.springframework.org/schema/beans"
       xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
       xmlns:dubbo="http://code.alibabatech.com/schema/dubbo"
       xsi:schemaLocation="http://www.springframework.org/schema/beans        http://www.springframework.org/schema/beans/spring-beans.xsd        http://code.alibabatech.com/schema/dubbo        http://code.alibabatech.com/schema/dubbo/dubbo.xsd">
------------------------------------------------------------------------------------------
 <!--dubbo-server.xml-->   
    <!--提供方应用信息，用于计算依赖关系-->
    <dubbo:application name="dubbo-server" owner="mic"/>
    <!--注册中心，暴露服务地址-->
    <dubbo:registry id="zk1" address="zookeeper://192.168.11.156:2181"/>
    <dubbo:registry id="zk2" address="zookeeper://192.168.11.157:2181"/>
    <!--协议，暴露服务-->
    <dubbo:protocol port="20880" name="dubbo"/>
    <dubbo:protocol port="8080" name="hessian"/>
    <!--声明需要暴露的服务接口-->
    <dubbo:service interface="com.gupaoedu.dubbo.IGpHello"
                   ref="gpHelloService" 
                   protocol="dubbo,hessian" 
                   registry="zk1"/>
    <dubbo:service interface="com.gupaoedu.dubbo.IDemoService"
                   ref="demoService" 
                   protocol="hessian"/>
    <!--和本地bean一样实现服务，接口实现类-->
    <bean id="gpHelloService" class="com.gupaoedu.dubbo.GpHelloImpl"/>
    <bean id="demoService" class="com.gupaoedu.dubbo.DemoService"/>
</beans>
------------------------------------------------------------------------------------------
<!--dubbo-client.xml-->
	<!--调用方信息-->
	<dubbo:application name="dubbo-client" owner="mic"/>
	<!--注册中心-->
	<!--file：缓存目录（服务地址信息）-->
	<dubbo:registry address="zookeeper://192.168.11.156:2181?                        register=false" check="false" file="d:/dubbo-server"/>
	<!--调用的服务-->                                                              
	<dubbo:reference id="gpHelloService"
                 interface="com.gupaoedu.dubbo.IGpHello"
                 protocol="dubbo"/>  
```

### 负载均衡

缺省为 `random` 随机调用

* Random LoadBalance

  **随机**，按权重设置随机概率

  在一个截面上碰撞的概率高，但调用量越大分布越均匀，而且按概率使用权重后也比较均匀，有利于动态调整提供者权重

* RoundRobin LoadBalance

  **轮循**，按公约后的权重设置轮循比率

  存在慢的提供者累积请求的问题，比如：第二台机器很慢，但没挂，当请求调到第二台时就卡在那，久而久之，所有请求都卡在调到第二台上

* LeastActive LoadBalance

  **最少活跃调用数**，相同活跃数的随机，活跃数指调用前后计数差

  使慢的提供者收到更少请求，因为越慢的提供者的调用前后计数差会越大

* ConsistentHash LoadBalance

  **一致性 Hash**，相同参数的请求总是发到同一提供者

  当某一台提供者挂时，原本发往该提供者的请求，基于虚拟节点，平摊到其它提供者，不会引起剧烈变动

```xml
服务端级别：
<dubbo:service interface="..." loadbalance="roundrobin" />
客户端级别：
<dubbo:reference interface="..." loadbalance="roundrobin" />
```

### 多版本支持

不同服务发布不同协议、相同服务发布多个协议

多注册中心支持

服务间相互调用：check="true" 检测调用的服务是否启动

设置不同版本的目的，就是要考虑到接口升级以后带来的兼容问题。在Dubbo中配置不同版本的接口，会在Zookeeper地址中有多个协议url的体现

```xml
dubbo-server.xml
<dubbo:service interface="com.gupaoedu.dubbo.IGpHello"
                   ref="gpHelloService" protocol="dubbo" version="1.0.0"/>
<dubbo:service interface="com.gupaoedu.dubbo.IGpHello"
                   ref="gpHelloService2" protocol="dubbo" version="1.0.1"/>
<bean id="gpHelloService" class="com.gupaoedu.dubbo.GpHelloImpl"/>
<bean id="gpHelloService2" class="com.gupaoedu.dubbo.GpHelloImpl2"/>

dubbo-client.xml
<dubbo:reference id="gpHelloService"
                     interface="com.gupaoedu.dubbo.IGpHello"
                     protocol="dubbo"
                     version="1.0.1"/>
```

### 主机绑定

在发布一个Dubbo服务的时候，会生成一个dubbo://ip:port的协议地址

在生成绑定的主机的时候，会通过一层一层的判断，直到获取到合法的ip地址（ServiceConfig.java）

`dubbo:20880` `rmi:1099` `http:80` `hessian:80` `webservice:80`

```java
//从配置文件中获取host 1-4依次获取到为止
1. NetUtils.isInvalidLocalHost(host)
2. host = InetAddress.getLocalHost().getHostAddress();
3. Socket socket = new Socket();
try {
    SocketAddress addr = new InetSocketAddress(registryURL.getHost(), registryURL.getPort());
    socket.connect(addr, 1000);
    host = socket.getLocalAddress().getHostAddress();
    break;
} finally {
    try {
        socket.close();
    } catch (Throwable e) {}
}
4. public static String getLocalHost(){
    InetAddress addres = getLocalAddress();
    return address == null ? LOCALHOST : address.getHostAddress();
}
```

### 集群容错

某种系统控制在一定范围内的一种允许或包容犯错情况的发生

例如，在电脑上运行一个程序，有时候会出现无响应的情况，然后系统会弹出一个提示框让我们选择，是立即结束还是继续等待，然后根据我们的选择执行对应的操作，这就是“容错”

在分布式架构下，网络、硬件、应用都可能发生故障，由于各个服务之间可能存在依赖关系，如果一条链路中的其中一个节点出现故障，将会导致雪崩效应。为了减少某一个节点故障的影响范围，所以我们才需要去构建容错服务，来优雅的处理这种中断的响应结果

**Dubbo提供了6种容错机制，分别如下**

* failover(默认)   失败自动切换，重试其他服务器； **retries（2）,不包含第一次**
* failsafe 失败安全，出现异常时，直接忽略（记录日志）
* failfast 快速失败， 失败以后立马报错，只发起一次调用
* failback  失败自动恢复，记录失败请求，定时重发 
* forking  forks. 并行调用多个服务器，只要一个成功即返回 
* broadcast 广播逐个调用所有提供者，任意一个报错则报错

```xml
配置方式如下，通过cluster方式，配置指定的容错方案
<!--声明需要暴露的服务接口 -->
<dubbo:reference id="demoService" interface="com.gupaoedu.dubbo.IGpHello"
                 registry="zookeeper" version="1.0.0"
                 cluster="failsafe" />
```

### 服务降级

**降级的目的是为了保证核心服务可用**

* 对一些非核心服务进行人工降级，在大促之前通过降级开关关闭那些推荐内容、评价等对主流程没有影响的功能
* 故障降级，比如调用的远程服务挂了，网络故障、或者RPC服务返回异常。 那么可以直接降级，降级的方案比如设置默认值、采用兜底数据（系统推荐的行为广告挂了，可以提前准备静态页面做返回）等等 

* 限流降级，在秒杀这种流量比较集中并且流量特别大的情况下，因为突发访问量特别大可能会导致系统支撑不了。这个时候可以采用限流来限制访问量。当达到阀值时，后续的请求被降级，比如进入排队页面，比如跳转到错误页（活动太火爆，稍后重试等）

**方式**

1. 在client端创建一个TestMock类，实现对应IGpHello的接口（需要对哪个接口进行mock，就实现哪个），名称必须以Mock结尾 
2. 在client端的xml配置文件中，添加如下配置，增加一个mock属性指向创建的TestMock 
3. 模拟错误（设置timeout），模拟超时异常，运行测试代码即可访问到TestMock这个类。当服务端故障解除以后，调用过程将恢复正常

```xml
<!--声明需要暴露的服务接口 -->
<dubbo:reference id="demoService" interface="com.gupaoedu.dubbo.IGpHello"
                 registry="zookeeper" version="1.0.0"
                 mock="com.gupaoedu.dubbo.TestMock" timeout="50" />
```

```java
public class TestMock implements IGHello{
    @Override
    public String sayHello(String s) {
        return "系统繁忙:" + s;
    }
}
```

### 配置优先级别

以timeout为例，显示了配置的查找顺序，其它retries, loadbalance等类似

dubbo-server.xml和client-client.xml中服务都设置了timeout，客户端的配置优于服务端

1. 方法级优先，接口级次之，全局配置再次之。

2. 如果级别一样，则消费方优先，提供方次之。

   retires、LoadBalance、cluster（客户端）、timeout（服务端）

   其中，服务提供方配置，通过URL经由注册中心传递给消费方

建议由服务提供方设置超时，因为一个方法需要执行多长时间，服务提供方更清楚，如果一个消费方同时引用多个服务，就不需要关心每个服务的超时设置

## Dubbo SPI

* Java SPI

  ==SPI全称（service provider interface），是JDK内置的一种服务提供发现机制，目前市面上有很多框架都是用它来做服务的扩展发现，如JDBC、日志框架==

  简单来说，它是一种==动态替换发现的机制==。

  举个简单的例子，如果定义了一个规范，需要第三方厂商去实现，那么对于应用方来说，只需要集成对应厂商的插件，既可以完成对应规范的实现机制。形成一种插拔式的扩展手段

* SPI规范

  1. 需要在classpath下创建一个目录，该目录命名必须是：META-INF/services

  2. 在该目录下创建一个properties文件，该文件需要满足以下几个条件

     a)  文件名必须是扩展的接口的全路径名称

     b) 文件内部描述的是该扩展接口的所有实现类

     c) 文件的编码格式是UTF-8

  3. 通过java.util.ServiceLoader的加载机制来发现

  ![image-20190102212324956](/Users/dingyuanjie/Desktop/MyKnowledge/2.code/java/2.咕泡学院/02.分布式专题/05.分布式服务治理/image-20190102212324956-6435405.png)

  ```java
  官方接口规范
  厂商添加官方依赖、实现官方接口规范
      resources目录下META-INF/services
          com.gupaoedu.spi.DataBaseDriver(实现类的package.接口规范名)
              内容：com.gupaoedu.spi.MysqlDriver(实现类全路径名)
  调用
      加入官方接口规范依赖、厂商依赖
      public static void main(String[] args){
          ServiceLoader<DataBaseDriver> serviceLoader = ServiceLoader.load(DataBaseDriver.class);
          for(DataBaseDriver dirver : serviceLoader) {
              System.out.println(driver.connect("localhost"));
          }
      }
  ```

* SPI的缺点

  1. JDK标准的SPI会一次性加载实例化扩展点的所有实现

     就是如果在META-INF/service下的文件里面加了N个实现类，那么JDK启动的时候都会一次性全部加载，那么如果有的扩展点实现初始化很耗时或者如果有些实现类并没有用到，那么会很浪费资源

  2. 如果扩展点加载失败，会导致调用方报错，而且这个错误很难定位到是这个原因

* Dubbo 优化后的SPI实现

  **Dubbo是基于Java原生SPI机制思想的一个改进**

  大部分的思想都是和SPI是一样，只是下面两个地方有差异

  1. 需要在resource目录下配置META-INF/dubbo或者META-INF/dubbo/internal或者META-INF/services，并基于SPI接口去创建一个文件
  2. 文件名称和接口名称保持一致，文件内容和SPI有差异，内容是KEY对应Value

  ```java
  dubbo-client
  package com.gupaoedu.dubbo.protocol;
  public class DefineProtocol implements Protocol {...}
  
  resources/MEAT-INF/dubbo
      com.alibaba.dubbo.rpc.Protocol
          defineProtocol=com.gupaoedu.dubbo.protocol.DefineProtocol
          
  public static void main( String[] args ) throws IOException, InterruptedException {
          ClassPathXmlApplicationContext context=new
                  ClassPathXmlApplicationContext
                  ("dubbo-client.xml");
         Protocol protocol=ExtensionLoader.getExtensionLoader(Protocol.class).
                  getExtension("defineProtocol");
          System.out.println(protocol.getDefaultPort());
          System.in.read();
  }
  ```
