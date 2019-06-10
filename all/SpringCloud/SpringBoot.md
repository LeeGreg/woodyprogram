# Spring Framework

* 为Java应用程序开发提供了基础性支持，使得开发人员可以专注于应用程序的业务开发
* IOC/DI：控制反转或依赖注入，让调用类对某一接口实现类的依赖关系由第三方容器注入，以移除调用类对某一接口实现类的依赖
* AOP：面向切面编程
  * 使用动态代理技术在运行期为目标`Bean`织入增强的代码
  * 将这些与业务无关的重复代码抽取出来，然后再嵌入到业务代码当中
* 优点：对事务的抽象-AOP、Spring Web MVC
* 局限：自身并非容器，不得不随Java EE容器启动而装载

# SpringBoot

* 为快速启动且最小化配置的Spring应用而设计，可用于构建微服务的基础框架
  * Spring Framework是SpringBoot的核心，Java规范才是它们的基石
  * SpringBoot 2.0 — Spring Framework 5.0 — Java8
* 搭配`SpringBoot starter`技术引入相关的依赖，结合`SpringBoot`自动装配，利用`SpringBoot`和`SpringFramework`的生命周期，使用嵌入式的Web容器，利用外部化配置影响`SpringApplication`的行为，通过`SpringApplication API`引导应用，`SpringBoot Actuator`提供运维-为生产准备特性
* 特性
  * 创建独立的Spring应用
  * 直接嵌入Tomcat、Jetty或Undertow等Web容器，不需要部署WAR文件
  * 提供固化的"starter"依赖，简化构建配置
    * SpringBoot利用Maven的依赖管理特性，进而固化其Maven依赖，该特性并非SpringBoot专属
    * 不需要在Maven的pom.xml中维护那些错综复杂的依赖关系
  * 当条件满足时自动地装配Spring或第三方类库
    * maven-starter添加到应用的Class Path中时，其关联的特性随应用的启动而自动地装载
  * 提供运维-为生产准备特性，如指标信息、健康检查以及外部化配置
  * 绝无代码生成，并且不需要XML配置
* 五大特性
  * SpringApplication
  * 自动装配
  * 外部化配置
  * SpringBoot Actuator
  * 嵌入式Web容器
* SpringCloud
  * 微服务是系统架构上的一种设计风格， 它的主旨是将一个原本独立的系统拆分成多个小型服务，这些小型服务都在各自独立的进程中运行，服务之间通过基于HTTP的RESTful API进行通信协作
    * 服务组件化，都能够独立部署和扩展，可有效避免修改一个服务而引起整个系统的重新部署
    * 通常会使用两种服务调用方式
      *  使用HTTP的RESTfulAPI或轻量级的消息发送协议， 实现信息传递与服务调用的触发
      *  通过在轻量级消息总线上传递消息，类似 RabbitMQ等一些提供可靠异步交换的中间件
    * 去中心化管理数据，希望让每一个服务来管理其自有的数据库
      * 分布式事务本身的实现难度就非常大， 所以在微服务架构中， 更强调在各服务之间进行 “ 无事务” 的调用， 而对于数据一致性， 只要求数据在最后的处理状态是一致的即可；若在过程中发现错误， 通过补偿机制来进行处理，使得错误数据能够达到最终的一致性
    * 容错设计，快速检测出故障源并尽可能地自动恢复服务
    * 基础设施自动化，构建可持续交付，自动化测试和自动化部署
  * Spring官方在SpringBoot的基础上研发出SpringCloud，致力于提供一些快速构建通用的分布式系统
    * 服务注册和发现、服务调用、负载均衡、熔断机制、分布式配置、路由、分布式消息
    * `SpringCloud Config`：配置管理工具，支持使用Git存储配置内容，可以使用它实现应用配置的外部化存储，并支持客户端配置信息刷新、加密/解密配置内容等
    * `SpringCloudNetflix`：核心组件， 对多个Netflix OSS开源套件进行整合。
      - `Eureka`：服务治理组件， 包含服务注册中心、 服务注册与发现机制的实现
      - `Hystrix`：容错管理组件，实现断路器模式，帮助服务依赖中出现的延迟和为故障提供强大的容错能力
      - `Ribbon`：客户端负载均衡的服务调用组件
      - `Feign`：基于Ribbon 和 Hystrix 的声明式服务调用组件
      - `Zuul`：网关组件， 提供智能路由、 访问过滤等功能
    * `Spring Cloud Bus`：事件、消息总线，用于传播集群中的状态变化或事件，以触发后续的处理，比如用来动态刷新配置等
    * `Spring Cloud Stream`：通过Rabbit 或Kafka 实现的消费微服务，可通过简单的声明式模型来发送和接收消息
    * `Spring Cloud Sleuth`：Spring Cloud 应用的分布式跟踪实现，可以完美整合 Zipkin
  * 优势
    * 高度抽象的接口，不需要关心底层的实现，当需要更替实现时，按需要配置即可，不需要过多的业务回归测试
    * `SpringCloud Stream`整合，通过`Stream`编程模式，使得不同的通道之间可以自由的切换传输介质，达到数据通信的目的，比如通过消息、文件、网络等
  * 版本说明
    * `1.5.8.RELEASE`、`java8`、`Dalston.RELEASE`

## 独立的Spring应用

* 在大多数`SpringBoot`应用场景中，程序使用`SpringApplication API`引导应用，其中又结合嵌入式Web容器，对外提供HTTP服务

* `Spring Web`应用

  * 传统的`Servlet`、`Spring Web MVC``、Reactive Web`（SpringBoot 2.0）
  * 在SpringApplication API上增加setWebApplicationType方法显示设置Web应用的枚举类型
  * 采用嵌入式容器，属于Spring应用上下文中的组件Beans，独立于外部容器，对应用生命周期拥有完全自主的控制（方便快捷的启动方式，可以提升开发和部署效率），这些组件和其他组件均由自动装配特性组装成Spring Bean定义（BeanDefinition），随Spring应用上下文启动而注册并初始化，而驱动Spring应用上下文启动的核心组件则是Spring Boot核心API SpringApplication
    * 外置容器需要启动脚本将其引导，随其生命周期回调执行Spring上下文的初始化
      * SpringWeb中的ContextLoaderListener
        * 利用ServletContext生命周期构建Web ROOT Spring应用上下文
      * WebMVC中的DispatcherServlet
        * 结合Servlet生命周期创建DispatcherServlet的Spring应用上下文
      * 均属于被动的回调执行，没有完整的应用主导权

* Spring非Web应用

  * 主要用于服务提供、调度任务、消息处理等场景

* 创建可执行Jar

  * 打包分为依赖jar包和可执行jar包，两者要分开，可执行包不可以被其他项目依赖
  * 前提需添加`spring-boot-maven-plugin`到`pom.xml`文件中
  * `mvn package`

* 启动

  * 在`Java`启动命令中，通过`-D`命令行参数设置`Java`的系统属性：`System.getProperties()`
    * `java -jar xxx.jar --server.port= 8888`
    * 连续的两个减号—就是对`application.properties`中的属性值进行赋值的标识
  * `Spring Boot`默认的应用外部配置文件`application.properties`
  * 开发阶段运行方式：`mvn spring-boot:run`、直接通过运行拥有 `main` 函数的类来启动
  * 生产环境运行方式：`java -jar target/xxx.jar`

* `SpringBoot Loader`

  * java -jar命令引导的是标准可执行JAR文件

  * 按Java官方文档规定，java -jar命令引导的具体启动类必须配置在MANIFEST.MF资源（/META-INF/目录下）的Main-Class属性中，其值为`JarLauncher`或`WarLauncher`

    ```properties
    # META-INF/MANIFEST.MF
    ...
    Main-Class:org.springframework.boot.loader.JarLauncher 或 WarLauncher
    Start-Class:thinkinginspringboot.firstappbygui.FirstAppByGuiApplication
    ...
    ```

  * `JarLauncher`装载并执行SpringBoot应用引导类，同时会将当前SpringBoot应用依赖的JAR文件作为引导类的类库依赖，所以`JarLauncher`能够引导，反之直接启动引导类会因未指定`Class Path`而启动不了

    * `JarLauncher`实际上是同进程内调用`Start-Class`类（即SpringBoot应用引导类）的main(String[])方法，并且在启动前准备好`Class Path`

  * WarLauncher与JarLauncher主要区别在于：项目类文件和JAR Class Path路径的不同

    * 打包WAR文件是一种兼容措施，既能被WarLauncher启动，又能兼容Servlet容器环境，WarLauncher与JarLauncher并无本质差别，SpringBoot应用使用非传统Web部署时，尽可能地使用JAR归档方式

## 固化的Maven依赖

* 打war包
  - `<packaging>war</packaging>`
  - `<plugin>`
    - `org.apache.maven.plugins:maven-war-plugin:3.1.0`
    - `org.springframework.boot:spring-boot-maven-plugin:2.0.2.RELEASE:executions:execution:goals:goal:repackage`
* 老版本`maven-war-plugin:2.2`中，默认的打包规则是必须存在Web应用部署描述文件`WEB-INF/web.xml`，而`maven-war-plugin:3.1.0`调整了该默认行为
* 单独引入`spring-boot-maven-plugin`插件时，需要配置`repackage<goal>`元素，否则不会添加SpringBoot引导依赖，进而无法引导当前应用
* SpringBoot利用Maven的依赖管理特性，进而固化其Maven依赖，该特性并非SpringBoot专属

## 嵌入式Web容器

* `spring-boot-starter-tomcat`是由`spring-boot-starter-web`间接依赖

* Spring Boot项目可以通过指定容器的Maven依赖来切换Spring Boot应用的嵌入式容器类型，无需代码层面的调整，不同的嵌入式容器存在专属的配置属性，自然也不再需要以WAR文件方式进行部署

  * tomcat、jetty、undertow都能作为嵌入式Servlet容器或ReactiveWeb容器
  * WebServer实现类：TomcatWebServer、JettyWebServer、UndertowWebServer
  * Jetty和Undertow，引入相应的依赖，排除web模块中的tomcat依赖

* 嵌入式Servlet容器

  * Servlet规范-Tomcat：3.0-7.x、3.1-8.x、4.0-9.x
    * 从Servlet3.0开始，Servlet组件均能通过ServletContext API在运行时装配，如Servlet、Filter和Listener，再结合ServletContainerInitializer生命周期回调，可实现Servlet组件的自动装配
    * Tomcat 7+ Maven插件能构建可执行JAR或WAR文件，实现独立的Web应用程序，也支持Servlet组件的自动装配
  * tomcat maven插件
    * 用于快速开发Servlet Web应用，并非嵌入式Tomcat
    * 仍旧利用了传统Tomcat容器部署方式
      * 先将Web应用打包为ROOT.war文件，然后在Tomcat应用启动的过程，将ROOT.war文件解压至webapps目录，支持指定ServletContext路径
    * 与嵌入式Tomcat的差异
      * 它不需要编码，也不需要外置Tomcat容器，将当前应用直接打包时将完整的Tomcat运行时资源添加至当前可执行JAR或WAR文件中，通过java -jar命令启动，类似于Spring Boot FAR  JAR或FAT WAR
  * 嵌入式tomcat
    * SpringBoot2.0的实现，利用嵌入式Tomcat API构建为TomcatWebServer Bean，由Spring应用上下文将其引导，其嵌入式Tomcat组件的运行（如Context、Connector等），以及ClassLoader的装载均由SpringBoot框架代码实现

* 嵌入式Reactive Web容器

  * 为SpringBoot2.0的新特性，通常处于被动激活状态
    * 如增加`spring-boot-starter-webflux`依赖，当与`spring-boot-starter-web`同时存在时，其会被忽略，这是由SpringApplication实现中的Web应用类型(WebApplicationType)推断逻辑决定的

* Web服务器已初始化事件-`WebServerInitializedEvent`

  ```java
  @EventListener(WebServerInitializedEvent.class)
  public void onWebServerReady(WebServerInitializedEvent event) {
  		System.out.println("当前WebServer实现类为：" +  event.getWebServer().getClass().getName());
  }
  ```

## 自动装配

* 自动装配存在前提
  * 取决于应用的Class Path下添加的JAR文件依赖，而且并非一定装载，需要条件
    * SpringBoot自动装配的对象是Spring Bean，例如通过XML配置文件或Java编码等方式组装Bean
  
* 激活
  * `@EnableAutoConfiguration`和@`SpringBootApplication`，二选一标注`在@Configuration`类上
    * `Spring Framework`，三种方式，不过都需Spring应用上下文引导
      * XML元素`<context:component-scan>`，采用`ClassPathXmlApplicationContext`加载
      * `@Import`，需要`AnnotationConfigApplicationContext`注册
      * `@ComponentScan`，需要`AnnotationConfigApplicationContext`注册
  
* @SpringBootApplication
  * 被用于激活
    * @EnableAutoConfiguration，负责激活SpringBoot自动装配机制
      * @EnableAutoConfiguration能够激活SpringBoot内建和自定义组件的自动装配特性
    * @ComponentScan，激活@Component的扫描
      * 扫描标有该注解类所在的包
      * SpringBoot为了改善传统Spring应用繁杂的配置内容，采用了包扫描和自动化配置的机制来加载原本集中于XML文件中的各项内容
      * 添加了排除的TypeFilter实现：
        - TypeExcludeFilter
        - AutoConfigurationExcludeFilter：排除其他同时标注@Configuration和@EnableAuto Configuration的类
    * @Configuration，申明被标注为配置类
      - 1.4开始，换成@SpringBootConfiguration，但是两者运行上无差异
      - @SpringBootConfiguration元标注在@SpringBootApplication上更多的意义在于@SpringBootApplication的配置类能够被@ComponentScan识别
  * 属性别名
    
    * 用于自定义@EnableAutoConfiguration、@ComponentScan的属性
    
    * @AliasFor，能够将一个或多个注解的属性"别名"到某个注解中
    
      ```java
      //如@SpringBootApplication利用@AliasFor注解别名@ComponentScan注解的basePackages()属性
      @SpringBootApplication(scanBasePackages="thinking.in.spring.boot.config")
      ```
  
  * 标注非引导类
    
    * @SpringBootApplication或@SpringBootApplication标注非引导类A，在引导类的main方法的SpringApplication.run中使用该非引导类的class对象作为参数
  
* @EnableAutoConfiguration

  * 激活自动装配，并非@Configuration类的派生注解
  * @Bean在@Component类与@Configuration类中存在差异
    * @Component类中@Bean的声明为"轻量模式Lite"
    * @Configuration类中@Bean的声明为"完全模式Full"，会执行CGLIB提升操作

* 多层次@Component派生性
  * 简言之，@Configuration注解上标注了@Component
  * 都能被@ComponentScan扫描识别
  * 如直接派生，@Service、@Controller、@Repository——Spring模式注解

* 自动装配机制

  * 在SpringBoot出现之前，SpringFramework提供Bean生命周期管理和Spring编程模型

    * 在框架层面，它支持注解的派生或扩展，然而无法自动地装配@Configuration类
    * 为此，SpringBoot添加了约定配置化导入@Configuration类的方式

  * 自动装配类能够打包到外部的JAR文件中，并且将被SpringBoot装载。同时，自动装配也能被关联到starter中，这些starter提供自动装配的代码及关联的依赖

  * SpringBoot自动装配底层实现与Spring Framework注解@Configuration和@Conditional的联系

    * Conditional实现类中方法返回true则条件成立，实例的class对象作为@Conditional的属性值，可标注在@Configuration类、@Bean方法上
      * 最常见：
        * @ConditionalOnClass，标注在@Configuration类上时，当且仅当目标类存在于Class Path下时才予以装配
        * @ConditionalOnMissingBean

  * 创建自动配置类

    * 激活自动装配

    * `@Configuration`标注需要装配的类`WebConfiguration`，类中@Bean方法

    * 创建自动装配类`WebAutoConfiguration`，标注@Configuration，并使用`@Import`导入需被装配的类，如`@Import(WebConfiguration.class)`

    * 在项目`src/main/resource`目录下新建`META-INF/spring.factories`资源，并配置`WebAutoConfiguration`类

      ```properties
      #自动装配
      org.springframework.boot.autoconfigure.EnableAutoConfiguration=\
      thinking.in.spring.boot.autoconfigure.WebAutoConfiguration
      ```

    * @Indexed

      * Spring Framework5.0引入，在代码编译时，向@Component和派生注解添加索引，从而减少运行时性能消耗
    
    * 通过@EnableAutoConfiguration启用自动装配
    
      * SpringFactoriesLoader#loadFactoryNames()会加载classpath下所有JAR文件里面的META-INF/spring.factories文件
    
        * SpringFactoriesLoader的实现类似于SPI（Service Provider Interface），为某个接口寻找服务实现的机制
    
          * 比如有个接口，想运行时动态的给它添加实现，只需要添加一个实现
    
          1. 服务提供者提供了接口的一种具体实现，在jar包的META-INF/services目录下创建一个以“接口全限定名”为命名的文件，内容为实现类的全限定名
          2. 接口实现类所在的jar包放在主程序的classpath中
          3. 主程序通过java.util.ServiceLoder动态装载实现模块，它通过扫描META-INF/services目录下的配置文件找到实现类的全限定名，把类加载到JVM
          4. SPI的实现类必须携带一个不带参数的构造方法
    
      * EnableAutoConfigurationImportSelector类会去读取spring.factories下key为EnableAutoConfiguration对应的全限定名的值，即要自动注册的那些AutoConfiguration Bean
    
      * 根据AutoConfiguration Bean上的@ConditionalOnClass等条件，再进一步判断是否加载

## 生产特性

* 指标、健康检查、外部配置

* 是DevOps的立足点

* SpringBoot Actuator

  * 基于SpringBoot自动装配实现，用于监管Spring应用，可通过HTTP Endpoint与其交互
    * 端点类型：审计、健康、指标收集
  * `http://127.0.0.1:8080/actuator/beans`
  * 常用的`Endpoins`
    * `beans`：显示当前Spring应用上下文的SpringBean完整列表
    * `conditions`：显示当前应用所有配置类和自动装配类的条件评估结果（包含匹配和非匹配）
    * `evn`：暴露Spring ConfigurableEnvironment中的PropertySource属性，获取应用所有可用的环境属性报告。 包括环境变量、JVM属性、应用的配置属性、命令行中的参数
    * `health`：显示应用的健康信息。
      * 可实现一个用来采集健康信息的检测器（实现Healthindicator接口，@Component）
    * `info`：显示任意的应用信息，返回一些应用自定义的信息。 默认清况下， 该瑞点只会返回一个空的JSON内容。可以在application.properties配置文件中通过info前缀来设置一 些属性
    * `mappings`：返回所有Spring MVC的控制器映射关系报告
    * `autoconfig`：获取应用的自动化配置报告， 其中包括所有自动化配置的候选项。 同时还列出了每个候选项是否满足自动化配置的各个先决条件。可以方便地找到一些自动化配置为什么没有生效的具体原因
    * `configprops:`：获取应用中配置的属性信息报告，可看到各个属性的配置路径。通过使用
      `endpoints.configprops.enabled=false` 来关闭
    * `metrics`：返回当前应用的各类重要度量指标，比如内存信息、线程信息、垃圾回收信息等。可以提供应用运行状态的完整度量指标报告。可通过`/metrics/{name}`接口来更细粒度地获取度量信息 ， 比如可以通过访问/metrics/mem.free来获取当前可用内存数量
    * `dump`：暴露程序运行中的线程信息
    * `trace`：返回基本的 HTTP 跟踪信息，始终保留最近的100条请求记录
    * `shutdown`：关闭应用
  * 默认暴露`health`和`info`，增加`management.endpoints.web.exposure.include=*`的配置属性到`application.properties`或启动参数中
  * `management.security.enabled=false`

* 外部化配置

  * 可通过Properties文件、YAML文件、环境变量或命令行参数
    * Bean的@Value注入
    * Spring Environment 读取
    * @ConfigurationProperties绑定到结构化对象
  * 17种内建PropertySource顺序（高-低）
    * @TestPropertySource、@SpringBootTest
    * 命令行参数
    * ServletConfig init参数、ServletContext init参数
    * java:comp/env中的JNDI属性
    * Java系统属性，可以通过System.getProperties()获得的内容 
    * 操作系统的环境变量
    * 随机属性源，通过random.*配置的随机属性
    * jar包外部配置：application-{profile}.properties或YAML
    * jar包内配置：application-{profile}.properties或YAML
    * jar包外配置：application.properties或YAML
    * jar包内配置：application.properties或YAML
    * @Configuration类上@PropertySource注解定义的属性
    * 应用默认属性，SpringApplication.setDefaultProperties 定义的内容 

* 多环境配置

  * 多环境配置的文件名需要满足 application-{profile}. properties的格式， 其中{profile}对应的环境标识
    * 在application.properties文件中通过spring.profiles.active 属性来设置{profile}值
  * 在application.properties中配置通用内容，并设置spring.profiles.active= dev, 以开发环境为默认配置 
  * 在application-{profile}.properties中配置各个环境不同的内容 
  * 通过命令行方式去激活不同环境的配置
    * `java -jar xxx.jar --spring.profiles.active =test`

* 规约大于配置

  * 从技术角度，SpringFramework是SpringBoot的基础设施，SpringBoot的基本特性均来自Spring Framework

  * Spring Framework 2.5

    * 通过@Component及派生注解，与XML元素`<context:component-scanbase-package="…"/>`相互配合，扫描相当于ClassPath下的指定Java根包，将Spring @Component及派生 Bean扫描并注册至Spring Bean容器（BeanFactory），通过DI注解@Autowired获取相依的Spring组件Bean

  * Spring Framework 3.0

    * @Configuration是XML配置文件的替代物
    * Bean的定义不再需要在XML文件中声明`<bean>`元素，可使用`@Bean`来代替
    * 更细粒度的@Import来导入@Configuration Class，将其注册为Spring Bean
    * 仍以硬编码的方式指定范围

  * Spring Framework 3.1

    * @ComponentScan 来代替 XML元素`<context:component-scan>`
    * 应用方可以实现ImportSelector接口（实现selectImports(AnnotationMetadata)方法）（实例必须暴露成Spring Bean），程序动态地决定哪些Spring Bean需要被导入
    * 内建功能模块激活的注解，如@EnableCache，也同样需要通过@ComponentScan或@Import等方式被Spring容器感知

  * Spring Framework 4.0

    * 条件化的Spring Bean装配注解@Conditional，
      - 其value()属性可指定Condition的实现类，而Condition提供装配条件的实现逻辑
      - 更直观地表达了Spring Bean装载时所需的前置条件，使得条件性装配成为可能

  * `ClassPathXmlApplicationContext`

    ```java
    static{
      // Spring2.5.x不兼容java8
      System.setProperty("java.version", "1.7.0");
    }
    public static void main(String[] args) {
      // 构建XML配置驱动Spring上下文
      ClassPathXmlApplicationContext context = new ClassPathXmlApplicationContext();
      // 设置XML配置文件的位置
      context.setConfigLocation("classpath:/META-INF/spring/context.xml");
      // 启动上下文
      context.refresh();  
      // 获取 Bean
      (...)context.getBean(...);
      //关闭上下文
      context.close();
    }
    ```

  * `AnnotationConfigApplicationContext`

    ```java
    public static void main(String[] args) {
      //构建Annotation配置驱动Spring上下文
      AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext();
      //注册当前引导类（被@Configuration标注）到Spring上下文
      context.register(XXX.class);
      //启动上下文
      context.refresh();
      //获取Bean对象
      context.getBean(...);
      //关闭上下文
      context.close();
    }
    ```

## 注解驱动编程

* 元注解

  * 能声明在其他注解上的注解，如@Component
  * 任何被@Component元标注的注解，如@Service，均为组件扫描的候选对象

* Spring模式注解

  * 即@Component派生注解

  * 内建模式注解，如@Component、@Service、@Repository、@Controller、@RestController及@Configuration

  * 可扩展的XML编写机制，提供了一种XML元素与Bean定义解析器之间的扩展机制

    * `<context:component-scan>`

      * 元素前缀context和local元素component-scan

      * XML Schema规范，元素前缀需显示地关联命名空间，如`xmlns:context="http://www.springframework.org/schema/context"`，元素前缀也可以自定义，而命名空间则是预先约定的

        ```java
        http\://www.springframework.org/schema/context=org.springframework.context.config.ContextNamespaceHandler
        ...
        ```

    * 元素XML Schema命名空间需要与其处理类建立映射关系，且配置在相对于`classpath`的规约资源`/META-INF/spring.handler`文件中

      * Spring容器根据`/META-INF/spring.handler`的配置，定位到命名空间context所对应的处理器`ContextNamespaceHandler`，当Spring应用上下文启动时，调用`ContextNamespaceHandler#init()`方法，随后注册该命名空间下所有local元素（包括`component-scan`元素）的Bean定义解析器

    * `<context:component-scan>`元素的Bean定义解析器为`ComponentScanBeanDefinitionParser`，用于解析Bean定义，其API为`BeanDefinitionParser`，`ComponentScanBeanDefinitionParser`为其中一种实现

    * 当Spring应用上下文加载并解析XML配置文件`/META-INF/spring/context.xml`后，当解析至`<context:component-scan`元素时，`ComponentScanBeanDefinitionParser#parse(Element, ParserContext)`方法被调用读取base-package属性后，属性值作为扫描根路径传入`ClassPathBeanDefinitionScanner#doScan(String…)`方法，在该方法中利用basePackages参数迭代地执行`findCandidateComponents(String)`方法，每次执行执行结果都生成候选的BeanDefinition集合，即candidates，最后doScan方法返回`BeanDefinitionHolder`集合—包含Bean定义（BeanDefinition）与其Bean名称相关信息

      ```java
      public class BeanDefinitionHolder implements BeanMetadataElement {
        private final BeanDefinition beanDefinition;
        private final String beanName;
        private final String[] aliases;
        ...
      }
      ```

      ```java
      //ClassPathBeanDefinitionScanner的默认过滤器引入标注@Component、@Repository、@Service或@Controller的类，也能标注所有@Component的派生注解
      public class ClassPathBeanDefinitionScanner extends ClassPathScanningCandidateComponentProvider {
        ...
        // basePackages注解所在的包  
        protected Set<BeanDefinitionHolder> doScan(String...basePackages) {
          Set<BeanDefinitionHolder> beanDefinitions = new LinkedHashSet<>();
          for(int i=0; i<basePackages.length; i++) {
            // findCandidateComponents方法从父类中继承
            Set<BeanDefinition> candidates = findCandidateComponents(basePackages[i]);
            // 加工处理BeanDefinition为BeanDefinitionHolder
            ...
          }
          return beanDefinitions;
        }
      }
      ```

      ```java
      public class ClassPathScanningCandidateComponentProvider implements ResourceLoaderAware{
        ...
        //ClassPathScanningCandidateComponentProvider#findCandidateComponents默认在指定根包路径下，将查找所有标注@Component及派生注解的BeanDefinition集合，并且默认的过滤规则由AnnotationTypeFilter及@Component的元信息（ClassMetaData和AnnotationMetaData）共同决定
        public set<BeanDefinition> findCandidateComponents(String basePackage) {
          set<BeanDefinition> candidates = new LinkedHashSet<>();
          try {
            //resolveBasePackage(basePackage)先处理basePackage中的占位符，将${...}替换为实际的配置值，然后将其中的Java package路径分隔符"."替换成资源路径分隔符"/"
            // 如thinking.in.spring.boot处理后classpath*:thinkubg/in/spring/boot/***/.class
            String packageSearchPath = ResourcePatternResovler.CLASSPATH_ALL_URL_PREFIX + resolveBasePackage(basePackage) + "/" + this.resourcePattern;
            // 类资源集合
            Resource[] resources = this.resourcePatternResolver.getResources(packageSearchPath);
            ...
              for(int i=0; i<resources.length;i++) {
                Resource resource = resources[i];
                ...
                if(resource.isReadable()) {
                  MetadataReader metadataReader = this.metadataReaderFactory.getMetadataReader(resouce);
                  if(isCandidateComonent(metadataReader)) {
                    ScannedGenericBeanDefinition sbd = new ScannedGenericBeanDefinition(metadataReader);
                    sbd.setResource(resouce);
                    sbd.setSource(resouce);
                    if(isCandidateComponent(sbd)) {
                      ...
                        candidates.add(sbd);
                    }
                    ...
                  }
                  ...
                }
                ..
              }
          } 
          ...
          return candidates;
        }
        ...
      }
      ```

  * Spring组合注解
    - 包含一个或多个其他注解，目的在于将这些关联的注解行为组合成单个自定义注解
    - 如@SpringBootApplication既是Spring模式注解，又是组合注解
    - 在Java中，Class对象是类的元信息载体，承载了其成员的元信息对象，包括字段(Filed)、方法(Method)、构造器(Constructor)及注解Annotation等，而Class的加载通过ClassLoader#loadClass(String)方法实现
    - Spring Framework的类加载则通过ASM实现，如ClassReader，ASM更为底层，读取的是类资源，直接操作其中的字节码，获取相关元信息，同时便于Spring相关的字节码提升。在读取元信息方面，Spring抽象出MetadataReader接口
  * Spring注解属性别名和覆盖
    - 注解属性覆盖：较低层注解能够覆盖其元注解的同名属性
    - 隐性覆盖：属于低层次注解属性覆盖高层次元注解同名属性
    - 显示覆盖：是@AliasFor提供的属性覆盖能力
    - 多层次注解属性之间的@AliasFor关系只能由较低层向较高层建立

## Spring注解驱动设计模式

* `Spring` `@Enable`模块驱动

  * 模块是指具备相同领域的功能组件集合，组合所形成的一个独立单元
    * 如Web MVC模块、AspectJ代理模块、Caching缓存模块、JMX（Java管理扩展）模块、Async（异步处理）模块等
  * 能够简化装配步骤，实现按需装配，同时屏蔽组件集合装配的细节，但是该模式必须手动触发，即必须标注在某个配置Bean中
  * Spring Framework
    - @EnableMvc、@EnableTransactionManagement、@EnableCaching、@EnableMBeanExport、@EnableAsync、@EnableWebFlux、@EnableAspectJAutoProxy
  * Spring Boot
    - @EnableAutoConfiguration、EnableManagementContext(Actuator管理模块)、@EnableConfigurationProperties(配置属性绑定模块)、@EnableOAuth2Sso(OAuth2单点登录模块)
      - @ConfigurationProperties注解把properties配置文件转化为Bean
      - @EnableConfigurationProperties注解使@ConfigurationProperties注解生效
  * Spring Cloud
    - @EnableEurekaServer、@EnableConfigServer、@EnableFeignClients、@EnableZuulProxy、@EnableCircuitBreaker

* 自定义@Enable模块驱动

  * 注解驱动和接口编程都使用了@Import，其职责在于装载导入类，将其定义为Spring Bean

  * 注解驱动实现

    * @Configuration标注一个含有@Bean标注方法的类
    * 自定义@EnableXXX注解，其中@Import(@Configuration标注的配置类.class)
    * @Configuration、@EnableXXX标注到引导类上

  * 接口编程实现

    - 需实现ImportSelector或ImportBeanDefinitionRegistrar接口

    - 基于ImportSelector接口实现

      1. @Component标注的类A、类B
      2. 自定义@EnableXXX注解，@Import(ImportSelector实现类.class)，设置属性，如Type type();
      3. ImportSelector实现类，重写方法中AnnotationMetadata参数对象读取自定义注解中所有的属性方法`map = AnnotationMetadata.getAnnotationAttributes(@EnableXXX.class.getName())`，获取某个属性方法，如type的`map.get("type")`，可根据type选择某个@Component标注类的全限定类名称返回——将被装载的类，重写方法返回值是`String[]`
      4. @Configuration、自定义@EnableXXX(可设置属性，供后面加载判断)标注引导类

      ```java
      public interface Server{
        void start();
        void stop();
        enum Type{ HTTP,FTP}
      }
      ```

      ```java
      @Component  //根据ImportSelector的契约，确保实现为@Spring组件
      public HttpServer implements Server{
        ...
      }
      @Component  //根据ImportSelector的契约，确保实现为@Spring组件
      public FTPServer implements Server{
        ...
      }
      ```

      ```java
      @Target(ElementType.TYPE)
      @Retention(RetentionPolicy.RUNTIME)
      @Documented
      @Import(ServerImportSelectro.class) //导入ServerImportSelector
      public @interface EnableServer {
        Server.Type type();
      }
      ```

      ```java
      public class ServerImportSelector implements ImportSelector {
        @Override
        public String[] selectImports(AnnotationMetadata importingClassMetadata) {
          //读取EnableServer中所有的属性方法
          Map<String, Object> annotationAttributes = importingClassMetadata.getAnnotationAttributes(EnableServer.class.getName());
          //获取名为“type”的属性方法，并且强制转化成Server.Type类型
          Server.Type type = (Server.Type)annotationAttributes.get("type");
          //导入的类名称数组
          String[] importClassNames = new String[0];
          switch(type) {
            case HTTP:
              importClassNames = new String[]{HttpServer.class.getName()};
              break;
            case FTP:
              importClassNames = new String[]{FTPServer.class.getName()};
              break;
          }
          return importClassNames;
        }
      }
      ```

      ```java
      @Configuration
      @EnableServer(type=Server.Type.HTTP)   //设置HTTP服务器
      public class EnableServerBootstarp {
         //构建Annotation配置驱动Spring上下文
         //注册当前引导类（被@Configuration标注）到Spring上下文
         //启动上下文
         //获取Bean对象
         context.getBean(Server.class);
      }
      ```

* Spring Web 自动装配

  * 传统Servlet容器web.xml部署DispatcherServlet

    ```xml
    <servlet>
      <servlet-name>example</servlet-name>
      <servlet-class>org.springframework.web.servlet.DispatcherServlet</servlet-class>
      <load-on-startup>1</load-on-startup>
    </servlet>
    <servlet-mapping>  
      <servlet-name>example</servlet-name>  
      <url-pattern>/example/*</url-pattern>  
    </servlet-mapping> 
    ```

  * Servlet3.0+

    * `Tomcat Maven 插件用于构建可执行 war`、`maven-war-plugin`
    * `javax.servlet-api`、`spring-webmvc`依赖

    ```java
    // 属于Spring Java 代码配置驱动
    // 自定义Web自动装配
    // 仅通过Spring Framework和Servlet容器也能实现Spring Web MVC的自动装配
    public class SpringWebMvcServletInitializer extends AbstractAnnotationConfigDispatcherServletInitializer {
        @Override
        protected Class<?>[] getRootConfigClasses() {
            return new Class[0];
        }
        @Override
        protected Class<?>[] getServletConfigClasses() { // DispatcherServlet 配置Bean
            return of(SpringWebMvcConfiguration.class);
        }
        @Override
        protected String[] getServletMappings() {  // DispatcherServlet URL Pattern 映射
            return of("/*");
        }
        private static <T> T[] of(T... values) {    // 便利 API ，减少 new T[] 代码
            return values;
        }
    }
    
    @EnableWebMvc
    @Configuration
    @ComponentScan(basePackages = "thinking.in.spring.boot.samples.spring3.web.controller")
    public class SpringWebMvcConfiguration {
    }
    ```

    ```java
    // 属于Spring XML 配置驱动
    public class MyWebAppInitialier extends AbstractDispatcherServletInitializer {
      @Override
      protected WebApplicationContext createRootApplicationContext() {
        return null;
      }
      @Override
      protected WebApplicationContext createServletApplicationContext() {
        XmlWebApplicationContext cxt = new XmlWebApplicationContext();
        cxt.setConfigLocation("/WEB-INF/spring/dispatcher-config.xml");
        return cxt;
      }
      @Override
      protected String[] getServletMappings() {
    		return new String[]{"/"};
      }
    }
    ```

* Web自动装配原理

  * Spring Framework是基于Servlet 3.0技术，其中`"ServletContext配置方法"`和`"运行时插拔"`是技术保障
  * `ServletContext`配置方法
    - 通过编程的方式动态地装配`Servlet`、`Filter`及各种`Listener`，增加了运行时配置的弹性
      - 配置组件—配置方法—配置对象
      - `Servlet`—`ServletContext#addServlet`—`ServletRegistration或ServletRegistration.Dynamic`
      - `Filter`—`ServletContext#addFilter`—`FilterRegistration或FilterRegistration.Dynamic`
      - `Listener`—`ServletContext#addListener`—无
    - `ServletContext`配置方法为Web应用提供了运行时装配的能力，还需在适当的时机加以装配
  * 运行时插拔
    * `ServletContext`配置方法仅能在`ServletContextListener#contextInitialized`或`ServletContainerInitializer#onStartup`方法中被调用
      * `ServletContextListener`的职责，用于监听`Servlet`上下文`（ServletContext）`的生命周期事件，包括初始化事件（由`ServletContextListener#contextInitialized`方法监听）和销毁事件
      * `Servlet`和`Filte`对外提供服务之前，必然经过Servlet上下文初始化事件
      * 当容器或应用启动时，`ServletContainerInitializer#onStartup(Set<Class<?>>, ServletContext)`方法将被调用，同时通过`@HandlesTypes#value()`属性方法来指定关心的类型。该方法调用早于`ServletContextListener#contextInitialized(ServletContextEvent)`方法
        * 不过`ServletContainerInitializer`的一个或多个实现类需要存放在`javax.servlet.ServletContainerInitializer`的文本文件中（独立JAR包中的`/META-INF/services`目录下）
      * 假设一个Servlet需要装配，并且提供Web服务，首先通过ServletContext配置方法addServlet动态地为其装配，随后，在`ServletContainerInitializer#onStartup`实现方法中加以实现
        - 如果需要装配N个Servlet或Filter，那么Servlet或Filter及`ServletContainerInitializer`的实现打包在若干JAR包中，当Servlet应用依赖这些JAR包后，这些Servlet或Filter就自动装配到Web应用中了
      * `AbstractContextLoaderInitializer`：如果构建`Web Root`应用上下文`(WebApplicationContext)`成功则替代web.xml注册`ContextLoaderListener`
        - `AbstractDispatcherServletInitializer`：替代`web.xml`注册`DispatcherServlet`，并且如果必要的话，创建`Web Root`应用上下文(`WebApplicationContext`)
          - `AbstractAnnotationConfigDispatcherServletInitializer`：具备`Annotation`配置驱动能力的`AbstractDispatcherServletInitializer`
      * 三种实现类均为抽象类
        - 原因一：如果它们是`WebApplicationInitialier`实现类，那么这三个类均会被`SpringServletContainerInitializer`作为具体实现添加到`WebApplicationInitializer`集合`initializers`中，随后顺序迭代执行`onStartup(ServletContext)`方法
        - 原因二：抽象实现提供模版化的配置接口，最终将相关配置的决策交给开发人员
      * `AbstractContextLoaderInitializer`装配原理
        * 在`Spring Web MVC`中，`DispatcherServlet`有专属的`WebApplicationContext`，它继承了来自Root `WebApplicationContext`的所有`Bean`，以便`@Controller`等组件依赖注入
        * 在传统的`Servlet`应用场景下，`Spring Web MVC`的Root `WebApplicationContext`由`ContextLoaderListener`装载（通常配置在`web.xml`文件中）
        * `ContextLoaderListener`是标准的`ServletContextListener`实现，监听`ServletContext`生命周期。当Web应用启动时，首先，Servlet容器调用`ServletContextListener`实现类的默认构造器，随后`contextInitialized(ServletContextEvent)`方法被调用。反之，当Web应用关闭时，`Servlet`容器调用其`contextDestroyed(ServletContextEvent)`方法
        * 当`Web`应用运行在`Servlet3.0+`环境中时，以上`web.xml`部署`ContextLoaderListener`的方式可替换为实现抽象类`AbstractContextLoaderInitializer`来完成（不推荐），通常情况下，子类只需要实现`createRootApplicationContext()`方法。`ContextLoaderListener`不允许执行重复注册到`ServletContext`，当多个`ContextLoaderListener`监听`contextInitialized`时，其父类`ContextLoader`禁止Root `WebApplicationContext`重复关联`ServletContext`
        * `AbstractAnnotationConfigDispatcherServletInitializer`装配原理
          * 实现该抽象类（推荐）
        * `Spring Framework` 基于`Servlet3.0`特性而构建的`Web`自动装配的原理
          * `SpringServletContainerInitializer`通过实现`Servlet3.0 SPI` 接口`ServletContainerInitializer`，与`@HandlesTypes`配合过滤出`WebApplicationInitializer`具体实现类集合，随后顺序迭代地执行该集合元素，进而利用`Servlet3.0`配置`API`实现`Web`自动装配的目的。
          * 同时，结合`Spring Framework 3.2`抽象实现`AbstractAnnotationConfigDispatcherServletInitializer`，极大地简化了注解驱动开发的成本

* Spring条件装配

  * 编译时差异化

    * 偏资源处理，构建不同环境的归档文件，通常依赖外部工具，比如使用Maven Profile构建

  * 运行时配置化

    * 利用不同环境的配置控制统一归档文件的应用行为，如设置环境变量或Java系统属性
    * `Spring Framework`

  * 基于增加桥接XML上下文配置文件的方式实现

    * `<import resource="classpath:/META-INF/${env}-context.xml"/>`，此时，Spring上下文需要替换占位符变量env，该值可以来自外部化配置，如Java系统属性或操作系统环境变量
    * `String envValue = System.getProperty("env", "prod");`，envValue可能来自-D命令行启动参数，当参数不存在时，使用prod作为默认值。`System.setProperty("env", envValue);`

  * Bean 注册方式：XML配置驱动`<beans profile="…">`和注解驱动（`@Profile`）

    * 或，@Profile({"dev","prod"})
    * 非，@Profile("!dev")

  * 当有效`Profile`不存在时，采用默认`Profile`

    * `ConfigurableEnvironment` API编码配置
      * 设置`Active Profile`：`setActiveProfiles(String…)`
      * 添加`Active Profile`：`addActiveProfile(String)`
      * 设置`Default Profile`：`setDefaultProfiles(String…)`
    * Java系统属性配置
      * 设置`Active Profile`：`spring.profiles.active`
      * 设置`Default Profile`：`spring.profiles.default`
    * 应用
      * 接口A，接口B、C实现A，在B上`@Profile("java8")`、C上`@Profile("java7")`
      * 设置`ConfigurableEnvironment.setActiveProfiles("java8")`，`ConfigurableEnvironment.setDefaultProfiles("java7")`

  * `@Conditional`条件装配

    * 与配置条件装配`Profile`（偏向于静态激活和配置）职责相似，都是加载匹配的Bean，不同的是`@Conditional`（关注运行时动态选择）具备更大的弹性

    * 允许指定一个或多个`Condition`实现类，当所有的`Condition`均匹配时，说明当前条件成立

    * `@ConditionalOnClass`、`@ConditionOnBean`和`@ConditionalOnProperty`等

    * 自定义`@Conditional`条件装配

      ```java
      @Target({ElementType.METHOD}) //只能标注在方法上面
      @Retention(RetentionPolicy.RUNTIME)
      @Documented
      @Conditional(OnSystemPropertyCondition.class)
      public @interface ConditionalOnSystemProperty {
        String name();
        String value();
      }
      ```

      ```java
      //指定系统属性名称与值匹配条件
      public class OnSystemPropertyCondition implements Condition {
          @Override
          public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
              // 获取 ConditionalOnSystemProperty 所有的属性方法值
              MultiValueMap<String, Object> attributes = metadata.getAllAnnotationAttributes(ConditionalOnSystemProperty.class.getName());
              // 获取 ConditionalOnSystemProperty#name() 方法值（单值）
              String propertyName = (String) attributes.getFirst("name");
              // 获取 ConditionalOnSystemProperty#value() 方法值（单值）
              String propertyValue = (String) attributes.getFirst("value");
              // 获取 系统属性值
              String systemPropertyValue = System.getProperty(propertyName);
              // 比较 系统属性值 与 ConditionalOnSystemProperty#value() 方法值 是否相等
              if (Objects.equals(systemPropertyValue, propertyValue)) {
                  System.out.printf("系统属性[名称 : %s] 找到匹配值 : %s\n",propertyName,propertyValue);
                  return true;
              }
              return false;
          }
      }
      ```

      ```java
      @Configuration
      public class ConditionalMessageConfiguration {
          @ConditionalOnSystemProperty(name = "language", value = "Chinese")
          @Bean("message") // Bean 名称 "message" 的中文消息
          public String chineseMessage() {
              return "你好，世界";
          }
      
          @ConditionalOnSystemProperty(name = "language", value = "English")
          @Bean("message") // Bean 名称 "message" 的英文消息
          public String englishMessage() {
              return "Hello,World";
          }
      }
      ```

      ```java
      //启动类中
      System.setProperty("language", "Chinese");
      ```

  * `Spring Framework`组件装配的自动化程度仍不失特别理想，比如`@Enable`模块驱动不仅需要将`@Enable`注解显示地标注在配置类上，而且该类还依赖`@Import`或`@ComponentScan`的配合。同时，Web自动装配必须部署在外部`Servlet3.0+`容器中，无法做到`SpringWeb`应用自我驱动

## SpringBoot 自动装配

* 在`Spring Framework`时代，当Spring应用的`@Component`或`@Configuration` Class需要被装配时，应用需要借助`@Import`或`@ComponentScan`的能力
  * `@ComponentScan(basePackages="")`仅扫描其标注类所在包，而不是所有包，考虑替换为`@ComponentScan(basePackageClasses="")`，其中`basePackagesClasses`指向默认包中的类即可
* `SpringBoot`自动装配
  * 整合Spring注解编程模型、`@Enable`模块驱动及条件装配等`Spring Framework`原生特性
  * `@SpringBootApplication`和`@EnableAutoConfiguration`都能激活自动装配（标注类作为`SpringApplication#run`参数），不过`@SpringBootApplication`能够减少多注解所带来的配置成本，如配置`@SpringBootApplication#scanBasePackages`属性，等同于配置`@Component#basePackages`属性等
* 优雅地替换自动装配
  * `SpringBoot`优先解析自定义配置类，并且内建的自动装配配置类实际上为默认的条件配置，即一旦应用存在自定义实现，则不再将它们（内建）装配
* 失效自动装配
  * 代码配置方式
    * 配置类型安全的属性方法：`@EnableAutoConfiguration.exclude();`
    * 配置排除类名的属性方法：`@EnableAutoConfiguration.excludeName();`
  * 外部化配置方式
    * 配置属性：`spring.autoconfiguration.exclude`
  * `Spring Framework`时代失效自动装配
    * 要么阻断`@Configuration` Class `BeanDefinition`的注册，那么通过`@Conditional`实现条件控制
  * `@EnableAutoConfiguration`可通过`ImportSelector`实现，且`exclude()`和`excludeName()`属性可从其`AnnotationMetadata`对象中获取
* SpringBoot自动装配原理
  * 通过`SpringFactoriesLoader`读取所有`META-INF/spring.factories`资源中`@EnableAutoConfiguration`所关联的自动装配Class集合
  * 读取当前配置类所标注的`@EnableAutoConfiguration`属性`exclude`和`excludeName`，并与`spring.autoconfigure.exclude`配置属性合并为自动装配Class排除集合，排除候选自动装配Class集合中的排除名单
* `@EnableAutoConfiguration`排序自动装配组件
  * 绝对自动装配顺序-`@AutoConfigureOrder`
  * 相对自动装配顺序-`@AutoConfigureBefore`和`@AutoConfigureAfter`，使用`name()`属性方法
* 自定义SpringBoot自动装配
  * 当注解`@EnableAutoCongiguration`激活自动装配后，`META-INF/spring.factories`资源中声明的配置Class随即被装配——SPI机制
  * 自动装配包括配置资源`META-INF/spring.factories`和配置Class，两者可被关联到名为"starter"的共享类库
  * 自定义`SpringBoot Starter`
* `SpringBoot`条件化自动装配
  * `Class`条件注解
    - `@ConditionalOnClass`（当指定类存在于Class Path时）和`@ConditionalOnMissingClass`
  * `Bean`条件注解
    * `@ConditionalOnBean`和`@ConditionalOnMissingBean`基于`BeanDefinition`进行名称或类型的匹配
    * `@ConditionalOnBean`标注的方法返回对象类型必须在所有的Spring应用上下文中存在
  * 属性条件注解
    * `@ConditionalOnProperty(prefix="formatter", name="enabled", havingValue="true", matchIfMissing=true)`
      * `matchIfMissing=true`表明当属性配置不存在时，同样视为匹配，可以使其为false来不装配
      * `Spring Environment`的属性`formatter.enable=true`时才会自动装配
        * 内部化配置：`SpringApplicationBuilder.properties("formatter.enable=true");`
        * 外部化配置：`application.properties`中
  * `Resource`条件注解
    * `@ConditionalOnResource`属性方法`resources()`指示只有资源必须存在时条件方可成立
    * 如`@ConditionalOnResource(resource="META-INF/spring.factories")`
  * Web应用条件注解
    * `@ConditionalOnWebApplication`（当前应用是否为Web类型的条件判断注解）和`@ConditionalOnNotWebApplication`
  * Spring表达式条件注解
    * `@ConditionalOnExpression("${formatter.enabled:true} && ${spring.jmx.enabled:true}")`

## SpringApplication

* SpringApplication是围绕SpringApplication生命周期来展开讨论的，分为"初始化"、"运行”和”结束“三个阶段，主要的核心特性包括SpringApplicationRunListener、SpringBoot事件和Spring应用上下文的生命周期管理等
  * SpringBoot引入SpringApplication是对Spring Framework的应用上下文生命周期的补充
  * `Spring Framework`时代，Spring应用上下文通常由容器启动，如`ContextLoaderListener`或`WebApplicationInitializer`的实现类由Servlet容器装载并驱动
  * `SpringBoot`时代，Spring应用上下文的启动则通过调用`SpringApplication#run(Object,String…)`或`SpringApplicationBuilder#run(String...)`方法配合`@SpringBootApplication`或`@EnableAutoConfiguration`注解的方式完成
  * SpringApplication可以引导非Web应用和嵌入式Web应用，而且它还能出现在SpringBoot应用部署在传统Servlet3.0+容器中的场景
  * 传统的Spring应用上下文生命的起点源于ConfigurableApplicationContext对象的创建，运行则由其refresh()方法引导，而终止于close()方法的调用
    * Spring Framework内建的ConfigurableApplicationContext实现类均继承于抽象类AbstractApplicationContext，在AbstractApplicationContext#refresh()方法执行过程中，伴随着组件BeanFactory、Environment、ApplicationEventMulticaster和ApplicationListener的创建，它们的职责分别涉及Bean容器、Spring属性配置、Spring事件广播和监听
    * 实际上，SpringApplication并未从本质上改变这些，因为AbstractApplicationContext提供了扩展接口，如setEnvironment(ConfigurableEnvironment)方法允许替换默认的Environment对象，以及initApplicationEventMulticaster和ApplicationListener Bean的机制，不过，这些扩展接口被SpringApplication在Spring应用上下文调用refresh()方法之前予以运用，在SpringApplicationRunListener实现类EventPublishingRunListener的帮助下，全新地引入SpringBoot事件

* `SpringBoot`的自动装配所依赖的注解驱动、@Enable模块驱动、条件装配、Spring工厂加载机制等特性均来自`Spring Framework`，也都围绕Spring应用上下文及其管理的Bean生命周期

* 启动失败、自定义`Banner`、自定义`SpringApplication`、流式`Builder API`、`Spring`应用事件和监听器、Spring应用Web环境、存储Spring应用启动参数、使用`ApplicationRunner`或`CommandLineRunner`接口、Spring应用退出、Spring应用管理特性

* `SpringApplication`初始化阶段

  * 属于运行前的准备阶段
    * 允许指定应用的类型，调整Banner输出、配置默认属性等，这些只要在run()方法之前指定即可
      * Web应用和非Web应用，SpringBoot2.0开始，Web应用又可分为Servlet Web和Reactive Web
  * 构造阶段
    * 由构造器完成，`SpringApplication.run()`等价于`new SpringApplication.run()`，都需要Class类型的`primarySources`参数，通常情况下，引导类将作为`primarySources`参数的内容
      * 主配置类
        * `primarySources`参数实际为`SpringBoot`应用上下文的`Configuration` Class，该配置类也不一定非的使用引导类，主配置类`primarySources`与传统`Spring Configuration` Class并无差异
          * 任何标注`@Configuration`和`@EnableAutoConguration`的类都能作为`primarySources`属性值
        * 主配置类属性`primarySources`除初始化构造器参数外，还能通过`SpringApplication#addPrimarySources(Collection)`方法追加修改
    * `SpringApplication`的构造过程
      * SpringApplication#run(Class,String...)方法的执行会伴随SpringApplication对象的构造，其中主配置primarySources被SpringApplication对象primarySources属性存储，随后依次执行
        * 推断Web应用类型、加载Spring应用上下文初始化器、加载Spring应用事件监听器、推断应用引导类
      * 推断Web应用类型
        * 应用类型可在SpringApplication构造后及run方法之前，再通过setWebApplicationType(WebApplicationType)方法调整
        * 由于当前Spring应用上下文尚未准备，所以实现采用的是检查当前ClassLoader下基准Class的存在性判断（通过ClassLoader判断核心类是否存在来推断Web应用类型）
          * 当`DispatcherHandler`存在，并且`DispatcherServlet`不存在时，即SpringBoot仅依赖`WebFlux`存在时，`WebApplicationType.REACTIVE`
          * 当`Servlet`和`ConfigurableWebApplicationContext`均不存在时为非Web应用，即`WebApplicationType.NONE`
          * 当`Spring WebFlux`和`Spring WebMVC`同时存在时，为`WebApplicationType.SERVLET`
      * 加载Spring应用上下文初始化器`ApplicationContextInitializer`
        * 运用了Spring工厂加载机制方法`SpringFactoriesLoader.loadFactoryNames(Class,ClassLoader)`返回所有`META-INF/spring.factories`资源中配置的`ApplicationContextInitializer`实现类名单，然后初始化这些实现类（必须存在默认构造器），这些实现类可以实现Ordered接口进行排序
        * 然后将这些初始化后的实现类关联到`SpringApplication#initializers`属性，供后续操作使用
          * `setInitializers(Collection)`方法实现属于覆盖更新，即在执行`SpringApplication#run`方法前，这些在构造过程中加载的`ApplicationContextInitializer`实例集合存在被`setInitializers(Collection)`方法覆盖的可能
      * 加载Spring应用事件监听器ApplicationListener
        * 在`SpringApplication`构建器执行`setInitializers(Collection)`方法后，立即执行
        * 实现手段与加载Spring应用上下文初始化器基本一致
          * 先执行`getSpringFactoriesInstances`方法，再设置实例集合，只不过初始化的对象类型从`ApplicationContextInitializer`变成`ApplicationListener`，`setListener(Collection)`方法同样时覆盖更新
      * 推断引导类
        * 根据当前线程执行栈来判断其栈中哪个类包含main方法
  * 配置阶段
    * 可选，主要用于调整（Setter方法）或补充（add*方法）构造阶段的状态，左右运行时行为
  * 自定义SpringApplication
    * 调整SpringApplication设置
      * SpringApplicationBuilder
        * addCommandLineProperties，是否增加命令行参数，true
        * additionalProfiles，增加附加SpringProfile，空set
        * contextClass，关联当前应用的ApplicationContext实现类，null
        * banner，设置Banner实现
        * bannerMode，设置Banner.Mode，包括日志（LOG）、控制台（CONSOLE）和关闭，CONSOLE
        * beanNameGenerator，设置@Configuration Bean名称的生成器实现，null
        * properties，多个重载方法，配置默认的配置项
        * environment，关联当前应用的Environment实现类，null
        * headless，Java系统属性，当为true时图形化界面交互方式失效
        * initializers，追加ApplicationContextIniaializer集合，所有META-INF/spring.factories资源中声明的ApplicationContextInitializer集合
        * listeners，追加ApplicationListener集合，所有META-INF/spring.factories资源中声明的ApplicationListener集合
        * logStartupInfo，是否日志输出启动时信息，true
        * main，设置Main Class，主要用于调整日志输出，由deduceMainApplicationClass()方法推断
        * registerShutdownHook，设置是否让ApplicationContext注册ShutdownHook线程，true
        * resourceLoader，设置当前ApplicationContext的ResourceLoader，null
        * sources，增加SpringApplication配置源，空Set
        * web，设置WebApplicationType，由deduceWebApplicationType()方法推断
    * 增加SpringApplication配置源
      * SpringBoot 1.x中并不区分主配置类与@Configuration Class，允许参数为Class、Class名称、Package、Package名称或XML配置资源
      * SpringBoot 2.0的SpringApplication配置源分别来自主配置类、@Configuration Class、XML配置文件和package，仅允许Class名称、Package名称和XML配置资源配置
    * 调整SpringBoot外部化配置
      - application.properties文件，实际上它可以覆盖SpringApplication#setDefaultProperties方法的设置，从而影响SpringApplication的行为
      - 同时，SpringApplication#setDefaultProperties方法也能影响application.properties文件搜索的路径，通过属性spring.config.location或spring.config.additional-location实现

* `SpringApplication`运行阶段

  * 属于核心阶段，完整地围绕run(String...)方法展开

  * 结合初始化阶段完成的状态，进一步完善了运行时所需要准备的资源，随后启动Spring应用上下文，在此期间伴随SpringBoot和Spring事件的触发，形成完整的SpringApplication生命周期

    * 事件
      * 实际上，在创建ApplicationContext之前会触发某些事件，所以不能在@Bean上注册一个监听器，可以通过SpringApplication.addListeners(…)或SpringApplicationBuilder.listeners(…)注册监听器
      * 在SpringBoot场景中，无论Spring事件监听器，还是SpringBoot事件监听器，自动注册这些监听器，均配置在META-INF/spring.factories中，并以org.springframework.context.ApplicationListener作为属性名称，属性值为ApplicationListener的实现类

    * SpringApplication准备阶段
      * 从run(String...)方法调用开始，到refreshContext(ConfigurableApplicationContext)调用前
      * 该过程依次准备的核心对象为ApplicationArguments、SpringApplicationRunListeners、Banner、创建Spring应用上下文（ConfigurableApplicationContext）
        * 装配ApplicationArguments
          * 当执行SpringApplicationRunListeners#starting()方法后，SpringApplication运行进入装配ApplicationArguments逻辑，其实现类为DefaultApplicationArguments，一个用于简化SpringBoot应用启动参数的封装接口，它的底层实现基于Spring Framework中的命令行配置源SimpleCommandLinePropertySource
          *  例如命令行参数"—name=woody"将被SimpleCommandLinePropertySource解析为"name:woody"的键值属性
    * ApplicationContext启动阶段
      * 由refreshContext(ConfigurableApplicationContext)方法实现
      * 随着refreshContext(ConfigurableApplicationContext)方法的执行，Spring应用上下文正式进入Spring生命周期，SpringBoot核心特性也随之启动，如组件自动装配、嵌入式容器启动Production-Ready特性
    * ApplicationContext启动后阶段
      * SpringApplication#afterRefresh(ConfigurableApplicationContext,ApplicationArguments)方法并未给Spring应用上下文启动后阶段提供实现，而是交给开发人员自行扩展
        * afterRefresh()

* SpringApplication结束阶段

  * 正常结束

    * 实现完成阶段的监听的两种方法：
      * 实现SpringApplicationRunListener#running(ConfigurableApplicationContext)方法
      * 实现ApplicationReadyEvent事件的ApplicationListener

  * 异常结束

    * SpringBoot 1.x ，异常流程同样作为SpringApplication生命周期的一个环节，将在SpringApplicationRunListener#finished(ConfigurableApplicationContext,Throwable)方法中执行

    * SpringBoot 2.0，替换为SpringApplicationRunListener#failed(ConfigurableApplicationContext,Throwable)方法

    * 自定义实现FailureAnalyzer和FailureAnalysisReporter

      ```java
      public class UnknownErrorFailureAnalyzer implements FailureAnalyzer {
          @Override
          public FailureAnalysis analyze(Throwable failure) {
              if (failure instanceof UnknownError) { // 判断上游异常类型判断
                  return new FailureAnalysis("未知错误", "请重启尝试", failure);
              }
              return null;
          }
      }
      ```

      ```java
      public class ConsoleFailureAnalysisReporter implements FailureAnalysisReporter {
          @Override
          public void report(FailureAnalysis analysis) {
              System.out.printf("故障描述：%s \n执行动作：%s \n异常堆栈：%s \n",
                      analysis.getDescription(),
                      analysis.getAction(),
                      analysis.getCause());
          }
      }
      ```

      ```properties
      resources/META-INF/spring.factories
      # FailureAnalyzer 配置
      org.springframework.boot.diagnostics.FailureAnalyzer=\
      thinking.in.spring.boot.samples.diagnostics.UnknownErrorFailureAnalyzer
      # FailureAnalysisReporter 配置
      org.springframework.boot.diagnostics.FailureAnalysisReporter=\
      thinking.in.spring.boot.samples.diagnostics.ConsoleFailureAnalysisReporter
      ```

      ```java
      public class UnknownErrorSpringBootBootstrap {
          public static void main(String[] args) {
              new SpringApplicationBuilder(Object.class)
                      .initializers(context -> {
                          throw new UnknownError("故意抛出异常");
                      })
                      .web(false) // 非 Web 应用
                      .run(args)  // 运行 SpringApplication
                      .close();   // 关闭 Spring 应用上下文
          }
      }
      ```

* SpringBoot应用退出

  * SpringApplication注册shutdownhook线程，当JVM退出时，确保后续Spring应用上下文所管理的Bean能够在标准的Spring生命周期中回调，从而合理地销毁Bean所依赖的资源，如会话状态、JDBC连接、网络连接等
    * 默认情况下，Spring应用上下文将注册shutdownHook线程，实现优雅的SpringBean销毁生命周期回调
    * 该特性是SpringApplication借助ConfigurableApplicationContext#registerShutdownHook API实现的

## SpringApplicationRunListeners

* 属于组合模式的实现，内部关联了SpringApplicationRunListener的集合

* SpringApplicationRunListener为SpringBoot应用的运行时监听器，其监听方法被SpringApplicationRunListeners迭代执行，其包含的监听方法有：

  * (监听方法，运行阶段说明，SpringBoot事件)
  * starting()，Spring应用刚启动，ApplicationStartingEvent
  * environmentPropared(ConfigurableEnvironment)，ConfigurableEnvironment准备妥当，允许将其调整，ApplicationEnvironmentPrepareEvent
  * contextPrepared(ConfigurabelApplicationContext)，ConfigurabelApplicationContext准备妥当，允许将其调整
  * contextLoaded(ConfigurabelApplicationContext)，ConfigurabelApplicationContext已装载但仍未启动，ApplicationPreparedEvent
  * started(ConfigurabelApplicationContext)，ConfigurabelApplicationContext已启动，此时Spring Bean已初始化完成，ApplicationStartedEvent
  * running(ConfigurabelApplicationContext)，Spring应用正在运行，ApplicationReadyEvent
  * failed(ConfigurabelApplicationContext, Throwable)，Spring应用运行失败，ApplicationFailedEvent

* SpringApplicationRunListener是SpringBoot应用运行时监听器，并非SpringBoot事件监听器

  - 只要遵照SpringApplicationRunListener构造器参数约定，以及结合SpringFactoriesLoader机制，完全能够将该接口进行扩展

* SpringBoot事件所对应的ApplicationListener实现是由SpringApplication构造器参数关联并添加到属性SimpleApplicationEventMulticasterinitialMulticaster中的

  * 比如SpringApplicationRunListener#starting()方法运行后，ApplicationStartingEvent随即触发，此时initialMulticaster同步执行`ApplicationListener<ApplicationStartingEvent>`集合的监听回调方法onApplicationEvent(ApplicationStartingEvent)，这些行为保证均源于Spring Framework事件/监听器的机制

* SpringBoot事件和Spring事件是存在差异的

* Spring事件是由Spring应用上下文ApplicationContext对象触发的

  - Spring事件/监听机制属于事件/监听器模式，可视为观察者模式的扩展
  - Spring事件监听器通过限定监听方法数量，仅抽象单一方法onApplicationEvent(ApplicationEvent)，将其用于监听Spring事件ApplicationEvent
  - ApplicationListener支持ApplicationEvent泛型监听
    - 当ContextRefreshedEvent事件发布后，`ApplicationListener<ContextRefreshedEvent>`实现的onApplicationEvent方法仅监听具体ApplicationEvent实现，不再监听所有的Spring事件，无需借助instanceof方式进行筛选
    - 由于泛型参数的限制，泛型化的ApplicationListener无法监听不同类型的ApplicationEvent
  - SmartApplicationListener接口
    - 通过supports*方法过滤需要监听的ApplicationEvent类型和事件源类型，从而达到监听不同类型的ApplicationEvent的目的

* Spring事件发布

  - SimpleApplicationEventMulticaster接口
    - 主要承担两种职责：关联（注册）ApplicationListener和广播ApplicationEvent
    - 默认情况下，无论在传统的Spring应用中，还是在SpringBoot使用场景中，均充当同步广播事件对象的角色，开发者只需关注ApplicationEvent类型及对应的ApplicationListener实现即可

* Spring内建事件

  - RequestHandledEvent
  - ContextRefreshedEvent：Spring应用上下文就绪事件
    - 当ConfigurableApplicationContext#refresh()方法执行到finishRefresh()方法时，Spring应用上下文发布ContextRefreshedEvent
    - 此时，Spring 应用上下文中Bean均已完成初始化，并能投入使用，通常`ApplicationListener<ContextRefreshedEvent>`实现类监听该事件，用于获取需要的Bean，防止出现Bean提早初始化所带来的潜在风险
  - ContextStartedEvent：Spring应用上下文启动事件
    - AbstractApplicationContext的start()中发布
  - ContextStopedEvent：Spring应用上下文停止事件
    - AbstractApplicationContext的stop()中发布
  - ContextClosedEvent：Spring应用上下文关闭事件
    - 由ConfigurableApplicationContext#close()方法调用时触发，发布ContextClosedEvent
  - Spring事件的API表述为ApplicationEvent，继承于Java规约的抽象类EventObject，并需要显示地调用父类构造器传递事件源参数。以上四种Spring内建事件均继承于抽象类ApplicationContextEvent（Spring上下文事件），并将ApplicationContext作为事件源

* 自定义Spring事件

  - 需扩展ApplicationEvent，然后由ApplicationEventPublisher#publishEvent()方法发布

* Spring事件监听

  - Spring事件/监听机制围绕ApplicationEvent、ApplicationListener和ApplicationEventMulticaster三者展开
  - ApplicationListener监听Spring内建事件
    - AbstractApplicationContext提供发布ApplicationEvent和关联ApplicationListener实现的机制，并且任意Spring Framework内建ConfigurableApplicationContext实现类均继承AbstractApplicationContext的事件/监听行为，如`context.addApplicationListener(event->System.out.println("触发事件："+event.getClass().getSimpleName()));`，`context.refresh()`，`context.stop()`，`context.start()`，`context.close()`
  - ApplicationListener监听实现原理
    - Spring事件通过调用SimpleApplicationEventMulticaster#multicastEvent方法广播，根据ApplicationEvent具体类型查找匹配的ApplicationListener列表，然后逐一同步或异步地调用ApplicationListener#onApplicationEvent(ApplicationEvent)方法，实现ApplicationListener事件监听
  - 注解驱动Spring事件监听@EventListener
    - @EventListener必须标记在Spring托管Bean的public方法上，支持返回类型为非void的情况，支持单一类型事件监听，当它监听一个或多个ApplicationEvent时，其参数可为零到一个ApplicationEvent
    - @EventListener异步方法，在原有方法基础上增加注解@Async即可
      - 类上需要添加@EnableAsync来激活异步，否则@Async无效
      - 方法返回值类型为void
    - @EventListener方法执行顺序
      - 通过Ordered接口和标注@Order注解来实现监听次序
    - @EventListener方法监听泛型

* Spring事件广播器

  - ApplicationEventPublisher#publishEvent方法
    - 由AbstractApplicationContext实现
  - ApplicationEventMulticaster#multicastEvent
    - 由SimpleApplicationEventMulticaster实现，是ApplicationEvent、ApplicationListener和ConfigurableApplicationContext之间连接的纽带

  | 监听类型               | 访问性 | 顺序控制       | 返回类型   | 参数数量 | 参数类型               | 泛型事件 |
  | ---------------------- | ------ | -------------- | ---------- | -------- | ---------------------- | -------- |
  | @EventListener同步方法 | public | @Order         | 任意       | 0或1     | 事件类型或泛型参数类型 | 支持     |
  | @EventListener异步方法 | public | Order          | 非原生类型 | 0或1     | 事件类型或泛型参数类型 | 支持     |
  | ApplicationListener    | public | Order或Ordered | void       | 1        | 事件类型               | 不支持   |

* SpringBoot事件/监听机制同样基于ApplicationEventMulticaster、ApplicationEvent和ApplicationListener实现
* SpringBoot事件/监听机制继承于Spring事件/监听机制，其事件类型继承于SpringApplicationEvent，事件监听器仍通过ApplicationListener实现，而广播器实现SimpleApplicationEventMulticaster将它们关联起来
* SpringBoot内建事件监听器
  * 在SpringBoot场景中，无论Spring事件监听器，还是SpringBoot事件监听器，均配置在META-INF/spring.factories中，并以org.springframework.context.ApplicationListener作为属性名称，属性值为ApplicationListener的实现类
  * 最重要的SpringBoot内建事件监听器
    * ConfigFileApplicationListener
      - 监听事件：ApplicationEnvironmentPreparedEvent和ApplicationPreparedEvent
      - 负责SpringBoot应用配置属性文件的加载，默认为application.properties或application.yml
    * LoggingApplicationListener
      - 监听事件：ApplicationStartingEvent或ApplicationEnvironmentPreparedEvent或ApplicationPreparedEvent或ContextClosedEvent或ApplicationFailedEvent
      - 用于SpringBoot日志系统的初始化（日志框架识别，日志配置文件加载等）

* SpringBoot事件
  * SpringBoot事件类型继承Spring事件类型ApplicationEvent，并且也是SpringApplicationEvent的子类
  * 大多数Spring内建事件为Spring应用上下文事件，即ApplicationContextEvent，其事件源为ApplicationContext。
  * 而SpringBoot事件源则是SpringApplication，其内建事件根据EventPublishingRunListener的生命周期回调方法依次发布。ApplicationStartingEvent、ApplicationEnvironmentPreparedEvent、ApplicationPreparedEvent、ApplicationStartedEvent、ApplicationReadyEvent和ApplicationFailedEvent，其中ApplicationReadyEvent和ApplicationFailedEvent在Spring应用上下文初始化后发布，即在ContextRefreshedEvent之后发布
* SpringBoot事件监听手段
  - 通过SpringApplication关联ApplicationListener对象集合，关联途径有二：
    - 一为SpringApplication构造阶段在Class Path下所加载所有META-INF/spring.factories资源中的ApplicationListener对象集合
    - 二是通过方法SpringApplication#addListeners(ApplicationListener…)或SpringApplicationBuilder#listeners(ApplicationListener…)显示地装配
* SpringBoot事件广播器
  
  - 同样来源于Spring Framework的实现类SimpleApplicationEventMulticaster，其广播行为与Spring事件广播毫无差别，只不过SpringBoot中发布的事件类型是特定的

## 创建Spring应用上下文

* ConfigurableApplicationContext
* SpringApplication通过createApplicationContext()方法创建Spring应用上下文
  * 默认情况下，根据SpringApplication构造阶段所推断的Web应用类型进行ConfigurableApplicationContext的创建
    * 允许通过SpringApplicationBuilder#web(WebApplicationType)或SpringApplication#setWebApplicationType(WebApplicationType)来显示调整web应用类型，从而忽视Web应用类型推断的行为
  * 通过setApplicationContextClass(Class)方法或SpringApplicationBuilder#contextClass(Class)方法，根据指定ConfigurableApplicationContext类型创建Spring应用上下文
* Spring应用上下文运行前准备阶段
  * 由SpringApplication#prepareContext方法完成
  * 根据SpringApplicationRunListener的生命周期回调又分为"Spring应用上下文准备阶段"和"Spring应用上下文装载阶段"

* Spring应用上下文准备阶段
  * SpringApplication#从prepareContext方法开始，到SpringApplicationRunListeners#contextPrepared截止
    * 设置Spring应用上下文ConfigurableEnvironment
      * 即context.setEnvironment(environment)
    * Spring应用上下文后置处理
      * 根据SpringApplication#postProcessApplicationContext(ConfigurableApplicationContext)方法的命名而来，允许子类覆盖该实现，可能增加额外需要的附加功能
    * 运用Spring应用上下文初始化器(ApplicationContextInitializer)
    * 执行SpringApplicationRunListener#contextPrepared方法回调，当Spring应用上下文创建并准备完毕时，该方法被回调

* Spring应用上下文装载阶段
  * 按照SpringApplication#prepareContext方法实现，本阶段可划分为四个过程
    - 注册SpringBoot Bean
      - SpringApplication#prepareContext方法将之前创建的ApplicationArguments对象和可能存在的Banner实例注册为Spring单体Bean
      - `context.getBeanFactory().registerSingleton("springApplicationArguments",applicationArguments);`
    - 合并Spring应用上下文配置源
      - 由getAllResources()方法实现
      - 如Configuration Class、类名、包名及Spring XML配置资源路径
    - 加载Spring应用上下文配置源
      - load(ApplicationContext,Object[])方法将承担加载Spring应用上下文配置源的职责，该方法将Spring应用上下文Bean装载的任务交给了BeanDefinitionLoader
    - 回调SpringApplicationRunListener#contextLoaded方法

## 单元测试

```java
//src/test目录下
import static org.hamcrest.Matchers.equalTo;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootApplication
@WebAppConfiguration
public class HelloServiceApplicationTests {
	private MockMvc mvc;
	@Before
  public void setUp() {
    mvc = MockMvcBuilders.standaloneSetup(new HelloController()).build();
  }
	@Test
	public void contextLoads() throws Exception {
        mvc.perform(MockMvcRequestBuilders.get("/hello").accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(content().string(equalTo("Hello, World!")));
	}
}
```

