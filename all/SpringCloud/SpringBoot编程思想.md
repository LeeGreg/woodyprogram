# 第一章 初览SpringBoot

## Spring Framework

* 为Java应用程序开发提供了基础性支持，使得开发人员可以专注于应用程序的业务开发
* IOC/DI：控制反转或依赖注入，让调用类对某一接口实现类的依赖关系由第三方（容器）注入，以移除调用类对某一接口实现类的依赖
* 优点：对事务的抽象-AOP、Spring Web MVC
* 局限：自身并非容器，不得不随Java EE容器启动而装载

## SpringBoot

* 为快速启动且最小化配置的Spring应用而设计
* Spring Framework是SpringBoot的核心，Java规范才是它们的基石
* SpringBoot 2.0 — Spring Framework 5.0 — Java8
* 特性
  1. 创建独立的Spring应用
  2. 直接嵌入Tomcat、Jetty或Undertow等Web容器，不需要部署WAR文件
  3. 提供固化的"starter"依赖，简化构建配置
  4. 当条件满足时自动地装配Spring或第三方类库
  5. 提供运维-为生产准备特性，如指标信息、健康检查以及外部化配置
  6. 绝无代码生成，并且不需要XML配置

## SpringBoot 五大特性

* SpringApplication
* 自动装配
* 外部化配置
* SpringBoot Actuator
* 嵌入式Web容器

五大特性构成了SpringBoot作为微服务中间件的基础，又提供了SpringCloud的基础设施

* 微服务架构必须建立在分布式系统中，然而SpringBoot天然性地缺少快速构建分布式系统的能力，为此，Spring官方在SpringBoot的基础上研发出SpringCloud，致力于提供一些快速构建通用的分布式系统，包含的核心特性如下：
  * 服务注册和发现
  * 服务调用
  * 负载均衡
  * 熔断机制
  * 分布式配置
  * 路由
  * 分布式消息
* SpringCloud优势：
  * 高度抽象的接口，不需要关心底层的实现，当需要更替实现时，按需要配置即可，不需要过多的业务回归测试
  * SpringCloud Stream整合，通过Stream编程模式，使得不同的通道之间可以自由的切换传输介质，达到数据通信的目的，比如通过消息、文件、网络等

# 第二章 理解独立的Spring应用

* 在大多数SpringBoot应用场景中，程序直接或者间接地使用SpringApplication API引导应用，其中又结合嵌入式Web容器，对外提供HTTP服务

## Spring Web应用

* SpringBoot1.x
  * 仅有Servlet容器实现
    * 传统的Servlet
    * Spring Web MVC
* SpringBoot2.0
  * 新增Reactive Web容器实现，即Spring 5.0 WebFlux
  * 在SpringApplication API上增加setWebApplicationType(WebApplicationType)方法，允许程序显示地设置Web应用的枚举类型
  * 搭配SpringBoot starter技术直接或间接地引入相关的依赖，结合SpringBoot自动装配，再利用SpringBoot和SpringFramework的生命周期，创建并启动嵌入式的Web容器，如Servlet Web和Reactive Web
  * SpringBoot应用无需再像传统的Java EE应用那样，将应用打包成WAR文件或EAR文件，并部署到Java EE容器中运行
  * SpringBoot应用采用嵌入式容器，独立于外部容器，对应用生命周期拥有完全自主的控制——方便快捷的启动方式，可以提升开发和部署效率
* 传统Spring应用
  * 外置容器需要启动脚本将其引导，随其生命周期回调执行Spring上下文的初始化
    * SpringWeb中的ContextLoaderListener
      * 利用ServletContext生命周期构建Web ROOT Spring应用上下文
    * WebMVC中的DispatcherServlet
      * 结合Servlet生命周期创建DispatcherServlet的Spring应用上下文
    * 均属于被动的回调执行，没有完整的应用主导权

* SpringBoot
  * 嵌入式容器启动，嵌入式容器成为应用的一部分，它属于Spring应用上下文中的组件Beans，这些组件和其他组件均由自动装配特性组装成Spring Bean定义（BeanDefinition），随Spring应用上下文启动而注册并初始化，而驱动Spring应用上下文启动的核心组件则是Spring Boot核心API SpringApplication

## Spring 非Web应用

* 主要用于服务提供、调度任务、消息处理等场景

## 启动

* 在Java启动命令中，通过-D命令行参数设置Java的系统属性：System.getProperties()
* @RestController = @Controller + @ResponseBody，直接输出文本内容，不导向某个页面
* 开发阶段运行方式：`mvn spring-boot:run`，`curl http://127.0.0.1:8080/ | json_pp`

* Spring Boot默认的应用外部配置文件`application.properties`

## 创建可执行Jar

* 前提需添加`spring-boot-maven-plugin`到`pom.xml`文件中

* `mvn package`

* 生产环境运行方式：`java -jar target/xxx.jar`

## Spring-Boot-loader

* FAT JAR和WAR执行模块

* java -jar命令引导的是标准可执行JAR文件

  * 按Java官方文档规定，java -jar命令引导的具体启动类必须配置在MANIFEST.MF资源（/META-INF/目录下）的Main-Class属性中

  ```json
  # META-INF/MANIFEST.MF
  ...
  Main-Class:org.springframework.boot.loader.JarLauncher 或 WarLauncher
  Start-Class:thinkinginspringboot.firstappbygui.FirstAppByGuiApplication
  ...
  ```

* 启动类`JarLauncher`并非项目中的文件，是由`spring-boot-maven-plugin`插件`repackage`追加进去的

* 项目引导类`FirstAppByGuiApplication`被`JarLauncher`装载并执行,`JarLauncher`会将当前SpringBoot依赖的JAR文件（均放在BOOT-INF/lib目录下）作为引导类的类库依赖，所以`JarLauncher`能够引导，反之直接`java 引导类`因未指定`Class Path`而启动不了
  
  * JarLauncher实际上是同进程内调用Start-Class类的main(String[])方法，并且在启动前准备好Class Path
  
* WarLauncher与JarLauncher主要区别在于：项目类文件和JAR Class Path路径的不同

* 打包WAR文件是一种兼容措施，既能被WarLauncher启动，又能兼容Servlet容器环境，WarLauncher与JarLauncher并无本质差别，SpringBoot应用使用非传统Web部署时，尽可能地使用JAR归档方式

# 第三章 理解固化的Maven依赖

* 打war包
  * `<packaging>war</packaging>`
  * `<plugin>`
    * `org.apache.maven.plugins:maven-war-plugin:3.1.0`
    * `org.springframework.boot:spring-boot-maven-plugin:2.0.2.RELEASE:executions:execution:goals:goal:repackage`

* 老版本`maven-war-plugin:2.2`中，默认的打包规则是必须存在Web应用部署描述文件`WEB-INF/web.xml`，而`maven-war-plugin:3.1.0`调整了该默认行为
* 单独引入`spring-boot-maven-plugin`插件时，需要配置`repackage<goal>`元素，否则不会添加SpringBoot引导依赖，进而无法引导当前应用
* SpringBoot利用Maven的依赖管理特性，进而固化其Maven依赖，该特性并非SpringBoot专属

# 第四章 理解嵌入式Wev容器

* `spring-boot-starter-tomcat`是由`spring-boot-starter-web`间接依赖
* Spring Boot项目可以通过指定容器的Maven依赖来切换Spring Boot应用的嵌入式容器类型，无需代码层面的调整，不同的嵌入式容器存在专属的配置属性，自然也不再需要以WAR文件方式进行部署

## 嵌入式Servlet Web容器

| Servlet规范 | Tomcat | Jetty | Undertow |
| :---------: | :----: | :---: | :------: |
|     4.0     |  9.x   |  9.x  |   2.x    |
|     3.1     |  8.x   |  8.x  |   1.x    |
|     3.0     |  7.x   |  7.x  |   N/A    |

* Tomcat

  * tomcat maven插件

    * 用于快速开发Servlet Web应用，并非嵌入式Tomcat
    * 仍旧利用了传统Tomcat容器部署方式，先将Web应用打包为ROOT.war文件，然后在Tomcat应用启动的过程，将ROOT.war文件解压至webapps目录，支持指定ServletContext路径
    * 与嵌入式Tomcat的差异在于，它不需要编码，也不需要外置Tomcat容器，将当前应用直接打包时将完整的Tomcat运行时资源添加至当前可执行JAR或WAR文件中，通过java -jar命令启动，类似于Spring Boot FAR  JAR或FAT WAR

  * SpringBoot2.0的实现，利用嵌入式Tomcat API构建为TomcatWebServer Bean，由Spring应用上下文将其引导，其嵌入式Tomcat组件的运行（如Context、Connector等），以及ClassLoader的装载均由SpringBoot框架代码实现

  * Tomcat Maven插件打包后的JAR或WAR文件属于非FAT模式，因为归档文件存在压缩的情况，所以Spring Boot Maven插件spring-boot-maven-plugin采用零压缩模式，将应用目录归档到JAR或WAR文件，相当于在jar命令归档过程中添加-0参数

    ```xml
    <build>
      <plugins>
        <!-- Tomcat 8 Maven 插件用于构建可执行 war -->
        <!-- https://mvnrepository.com/artifact/org.apache.tomcat.maven/tomcat8-maven-plugin -->
        <plugin>
          <groupId>org.apache.tomcat.maven</groupId>
          <artifactId>tomcat8-maven-plugin</artifactId>
          <version>3.0-r1655215</version>
          <executions>
            <execution>
              <id>tomcat-run</id>
              <goals>
                <!-- 最终打包成可执行的jar包 -->
                <goal>exec-war-only</goal>
              </goals>
              <phase>package</phase>
              <configuration>
                <!-- ServletContext 路径 -->
                <path>/</path>
              </configuration>
            </execution>
          </executions>
        </plugin>
      </plugins>
    </build>
    
    <pluginRepositories>
      <pluginRepository>
        <!-- tomcat8-maven-plugin 所在仓库 -->
        <id>Alfresco</id>
        <name>Alfresco Repository</name>
        <url>https://artifacts.alfresco.com/nexus/content/repositories/public/</url>
        <snapshots>
          <enabled>false</enabled>
        </snapshots>
      </pluginRepository>
    </pluginRepositories>
    ```

  * 从Servlet3.0开始，Servlet组件均能通过ServletContext API在运行时装配，如Servlet、Filter和Listener，再结合ServletContainerInitializer生命周期回调，可实现Servlet组件的自动装配
    
    * Spring Framework3.1同样运用了这些特性，抽象出WebApplicationInitializer接口，降低ServletContainerInitializer接口的理解成本
  * Tomcat 7+ Maven插件能构建可执行JAR或WAR文件，实现独立的Web应用程序，也支持Servlet组件的自动装配

* Jetty和Undertow

  * 引入相应的依赖，排除web模块中的tomcat依赖

## 嵌入式Reactive Web容器

* 为SpringBoot2.0的新特性，通常处于被动激活状态，如增加`spring-boot-starter-webflux`依赖，当与`spring-boot-starter-web`同时存在时，其会被忽略，这是由SpringApplication实现中的Web应用类型(WebApplicationType)推断逻辑决定的

* UndertowServletWebServer作为嵌入式Reactive Web容器

  * web模块排除tomcat依赖，添加undertow、webflux依赖

* UndertowWebServer作为嵌入式Reactive Web容器

  * 添加undertow、webflux依赖

  * maven-compiler-plugin采用的Java编译级别版本（默认java1.5）要与spring-boot设置的版本一致，例如设置成Java1.8

    ```xml
    pom.xml
    <properties>
      <project.build.sourceEncoding>UTF-8</project.build.sourceEncoding>
      <project.reporting.outputEncoding>UTF-8</project.reporting.outputEncoding>
      <java.version>1.8</java.version>
    </properties>
    
    <!-- 保持与 spring-boot-dependencies 版本一致 -->
    <plugin>
      <groupId>org.apache.maven.plugins</groupId>
      <artifactId>maven-compiler-plugin</artifactId>
      <version>3.7.0</version>
      <configuration>
        <source>${java.version}</source>
        <target>${java.version}</target>
      </configuration>
    </plugin>
    ```

* 无论Servlet实现ServletWebServerApplicationContext，还是Reactive实现ReactiveWebServerApplicationContext，只需注入WebServerApplicationContext对象，并且在SpringBoot应用启动后再输出其关联的WebServer实现类即可，如某个@Bean方法的参数中注入WebServerApplicationContext对象，通过该对象的getWebServer().getClass().getName()可以获取当前WebServer的实现类

  * 没有考虑非Web应用类型的场景

* Web服务器已初始化事件-`WebServerInitializedEvent`

  * 更健壮，即使在非Web应用中运行，也不至于注入`WebServerApplicationContext`失败

  ```java
  @EventListener(WebServerInitializedEvent.class)
  	public void onWebServerReady(WebServerInitializedEvent event) {
  		System.out.println("当前WebServer实现类为：" +  event.getWebServer().getClass().getName());
  	}
  ```

* Jetty作为嵌入式ReactiveWeb容器
  
  * 添加jetty依赖
* Tomcat作为嵌入式ReactiveWeb容器
  
  * 添加spring-boot-starter-tomcat依赖
* 三种嵌入式Web容器

|   容器   |          Maven依赖           |  WebServer实现类  |
| :------: | :--------------------------: | :---------------: |
|  Tomcat  |  spring-boot-starter-tomcat  |  TomcatWebServer  |
|  Jetty   |  spring-boot-starter-jetty   |  JettyWebServer   |
| Undertow | spring-boot-starter-undertow | UndertowWebServer |

* maven-starter添加到应用的Class Path中时，其关联的特性随应用的启动而自动地装载——自动装配

# 第五章 理解自动装配

* 自动装配存在前提，取决于应用的Class Path下添加的JAR文件依赖，而且并非一定装载，需要条件

* SpringBoot自动装配的对象是SpringBean，例如通过XML配置文件或Java编码等方式组装Bean

## 激活自动装配

* `@EnableAutoConfiguration`和@`SpringBootApplication`，二选一标注`在@Configuration`类上
* `Spring Framework`，三种方式，不过都需Spring应用上下文引导
  1. XML元素`<context:component-scan>`，采用`ClassPathXmlApplicationContext`加载
  2. `@Import`，需要`AnnotationConfigApplicationContext`注册
  3. `@ComponentScan`，需要`AnnotationConfigApplicationContext`注册

## @SpringBootApplication

* 被用于激活
  * @EnableAutoConfiguration，负责激活SpringBoot自动装配机制
  * @ComponentScan，激活@Component的扫描
    * 扫描标有该注解类所在的包
    * 添加了排除的TypeFilter实现：
      * TypeExcludeFilter
      * AutoConfigurationExcludeFilter：排除其他同时标注@Configuration和@EnableAuto Configuration的类
  * @Configuration，申明被标注为配置类
    * 1.4开始，换成@SpringBootConfiguration，但是两者运行上无差异

* 多层次@Component派生性

  * 简言之，@Configuration注解上标注了@Component
  * 都能被@ComponentScan扫描识别
  * 如直接派生，@Service、@Controller、@Repository——Spring模式注解

* @SpringBootApplication属性别名，用于自定义@EnableAutoConfiguration、@ComponentScan的属性

  * @AliasFor，用于桥接其他注解的属性，能够将一个或多个注解的属性"别名"到某个注解中

    ```java
    //如@SpringBootApplication利用@AliasFor注解别名了@ComponentScan注解的basePackages()属性
    @SpringBootApplication(scanBasePackages="thinking.in.spring.boot.config")
    ```

* @SpringBootApplication标注非引导类
  * @SpringBootApplication标注非引导类A，在引导类的main方法的SpringApplication.run中使用该非引导类的class对象作为参数
  * @EnableAutoConfiguration标注非引导类A，在引导类的main方法的SpringApplication.run中使用该非引导类的class对象作为参数

## @EnableAutoConfiguration

* 激活自动装配，并非@Configuration类的派生注解

* @Bean在@Component类与@Configuration类中存在差异
  * @Component类中@Bean的声明为"轻量模式Lite"
  * @Configuration类中@Bean的声明为"完全模式Full"，会执行CGLIB提升操作

## 自动配置机制

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

    ```json
    #自动装配
    org.springframework.boot.autoconfigure.EnableAutoConfiguration=\
    thinking.in.spring.boot.autoconfigure.WebAutoConfiguration
    ```

* @Indexed

  * Spring Framework5.0引入，在代码编译时，向@Component和派生注解添加索引，从而减少运行时性能消耗

# 第六章 生产特性

* 指标、健康检查、外部配置
* 是DevOps的立足点

## SpringBoot Actuator

* 使用场景：监管投入生产的应用
* 监管媒介：HTTP或JMX端点（Endpoints）
* 端点类型：审计、健康、指标收集
* 基本特点：自动运用（基于SpringBoot自动装配实现） 

简言之：SpringBoot Actuator用于监管Spring应用，可通过HTTP Endpoint或JMX Bean与其交互

## SpringBoot Actuator Endpoints

* `http://127.0.0.1:8080/actuator/beans`

* 常用的Endpoins：
  * beans：显示当前Spring应用上下文的SpringBean完整列表
  * conditions：显示当前应用所有配置类和自动装配类的条件评估结果（包含匹配和非匹配）
  * evn：暴露Spring ConfigurableEnvironment中的PropertySource属性
  * health：显示应用的健康信息
  * info：显示任意的应用信息
* 默认暴露health和info，增加`management.endpoints.web.exposure.include=*`的配置属性到application.properties或启动参数中

## 外部化配置

* 可通过Properties文件、YAML文件、环境变量或命令行参数
  * Bean的@Value注入
  * Spring Environment 读取
  * @ConfigurationProperties绑定到结构化对象
* 17种内建PropertySource顺序
  * @TestPropertySource、@SpringBootTest
  * 命令行参数
  * ServletConfig init参数、ServletContext init参数
  * JNDI属性、Java系统属性，System.getProperties()、系统环境变量、随机属性源
  * jar包外部配置：application-{profile}.properties或YAML
  * jar包内配置：application-{profile}.properties或YAML
  * jar包外配置：application.properties或YAML
  * jar包内配置：application.properties或YAML
  * @Configuration类上@PropertySource
  * 默认属性，SpringApplication.setDefaultProperties

## 规约大于配置

* 从技术角度，SpringFramework是SpringBoot的基础设施，SpringBoot的基本特性均来自Spring Framework
* Spring Framework 2.5
  * 通过@Component及派生注解，与XML元素`<context:component-scanbase-package="…"/>`相互配合，扫描相当于ClassPath下的指定Java根包，将Spring @Component及派生 Bean扫描并注册至Spring Bean容器（BeanFactory），通过DI注解@Autowired获取相依的Spring组件Bean
  * @Component Bean必须在`<context:component-scan>`规定的base-package集合范围中

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
    * 其value()属性可指定Condition的实现类，而Condition提供装配条件的实现逻辑
    * 更直观地表达了Spring Bean装载时所需的前置条件，使得条件性装配成为可能

* ClassPathXmlApplicationContext

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

* AnnotationConfigApplicationContext

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

# 第七章 注解驱动编程

* Spring Framework两大核心思想：IOC（控制反转）和DI（依赖注入）
  
* 应用系统不应以Java代码的方式直接控制依赖关系，而是通过容器来加以管理
  
* Spring Framework 2.5

  * 依赖注入，@Autowired，
    * 注入的Spring Bean可以来自XML元素`<bean>`定义，或Spring模式注解，如@Component、@Repository、@Service
    * 也允许注入某种类型Spring Bean 的集合，如`@Autowired private Collection<NameRepository> repository`
  * 依赖查找，@Qualifier
    * @Autowired依赖查找的实现均属于限定类型（Class）的方式
    * 再细粒度的筛选，则需要使用@Qualifier的配合，如使用其依赖查找某个命名的Bean
    * 也支持"逻辑类型"的限定，如SpringBoot中的外部化配置注解@ConfigurationPropertiesBinding，以及SpringCloud中的负载均衡@LoadBalanced，在Spring应用上下文生命周期中，这两个注解的Bean的处理实现先通过@Qualifier筛选，再对其进行加工处理
  * 组件声明，@Component、@Service
  * SpringMVC，@Controller、@RequestMapping及ModelAttribute
  * 声明周期回调注解
    * @PostConstruct，可替代`<bean init-method="…"/>`或Spring InitializingBean接口回调
    * @PreDestroy，可替代`<bean destroy-method="…" />`或DisposableBean
  * 通过在@Component Class中标注@Order的方式对多个Spring Bean进行排序

* Spring Framework 3.0

  * 替换XML配置方式，引入了配置类注解@Configuration，也是内建@Component派生注解
  * @ImportResource允许导入遗留的XML配置文件，被其标注的类需再标注@Configuration
  * @Import允许导入一个或多个类作为Spring Bean，这些类无需标注Spring模式注解，而被@Import标注的类需再标注@Configuration
  * 引入新的Spring应用上下文实现AnnotationConfigApplicationContext，作为前时代ApplicationContext实现的替代者。先使用AnnotationConfigApplicationContext注册@Configuration Class，然后在该Class上再标注@ImportResource或@Import
  * @Bean、@DependsOn、@Primary、@Lazy

* Spring Framework 3.1

  - @ComponentScan 来代替 XML元素`<context:component-scan>`
  - 在Bean定义的声明中，@Bean允许使用注解@Role设置其角色
  - @Profile使得Spring应用上下文具备条件化Bean定义的能力，比如生产和开发环境采用不同的Bean定义
  - Spring Web MVC，@RequestHeader、@CookieValue和@RequestPart，使得@Controller类不必直接使用Servlet API
  - REST开发
    - @PathVariable便于REST动态路径的开发
    - @RequestBody能够直接反序列化请求内容
    - @ResponseBody将处理方法返回对象序列化为REST主体内容
    - @ResponseStatus可以补充HTTP响应的状态信息
  - 全新并统一的配置属性API，奠定了SpringBoot外部化配置的基础，也是SpringCloud分布式配置的基石
    - 配置属性存储接口Environment
    - 配置属性源抽象PropertySources
      - @PropertySource，该注解的value()属性方法所关联的Properties资源类与SpringBoot应用配置文件application.properties并没有本质的区别
  - 缓存抽象
    - 主要API包括缓存Cache及缓存管理器CacheManager，配套的注解Caching和Cacheable极大简化了数据缓存的开发
  - 异步
    - 异步操作注解@Async、周期异步执行注解@Scheduled及异步Web请求处理DeferredResult
  - 校验注解@Validated
  - @Enable模块驱动
    - 将相同职责的功能组件以模块化的方式装配，极大地简化了Spring Bean配置
    - 如@EnableWebMvc，该注解被@Configuration Class标注后，RequestMappingHandlerMapping、RequestMappingHandlerAdapter及HandlerExceptionResolver等Bean被装配
    - 手动装配——因@Enable模块驱动需要显示地标注在@Configuration Class上

* Spring Framework 4.0

  * @Conditional条件化注解

    * @Profile开始重新声明，通过@Conditional实现

  * @PropertySource提示为可重复标注的注解（一个类上可多次标记该注解）

    * Java8 @Repeatable出现，解决了注解无法重复标注在同一类上的限制

    ```java
    //java 8+
    @PropertySource("classpath:/config/default.properties")
    @PropertySource("classpath:/config/override.properties")
    @Configuration
    public class PropertySourceConfiguration{...}
    //java 8之前
    @PropertySources({
      @PropertySource("classpath:/config/default.properties")
    	@PropertySource("classpath:/config/override.properties")
    })
    ```

  * Spring Framework 4.3 引入@ComponentScans
  * Spring Framework 4.2 新增事件监听器注解@EventListener，作为ApplicationListener接口编程的第二选择
  * @AliasFor
    * 不限制派生注解之间存在相同的属性方法：value()
    * 在同一注解内实现属性方法的别名，如Spring Web MVC中的@RequestMapping
  * @GetMapping（@RequestMapping的派生注解）、@PostMapping、@PutMapping
  * @RestController
    
    * @RestControllerAdvice作为@RestController的AOP拦截通知，类似于@ControllerAdvice对于@Controller的角色
  * 浏览器跨域资源访问
    
    * Spring Framework 4.2引入@CrossOrigin（集中在@Controller的处理方法上），作为CorsRegistry（更关注请求URL）替换注解方案

* Spring Framework 5.0

  * @Indexed，为Spring模式注解添加索引，以提升应用启动性能

    ```java
    @Indexed
    @Configuration
    public class AnnotationIndexedConfiguration{...}
    //pom.xml中添加org.springframework:spring-context-indexer依赖
    ```

  * 当工程打包为JAR或在IDE工具中重新构建后，META-INF/spring.components文件将自动生成（编译时生成）

    * 当Spring应用上下文执行@ComponentScan扫描时，META-INF/spring.components将被CandidateComponentsIndexLoader读取并加载，转化为CandidateComponentsIndex对象，进而@Component不再扫描指定的package，而是读取CandidateComponentsIndex对象，从而达到提升性能的目的
    * 缺陷：仅包含模式注解的类不会被@ComponentScan扫描识别

## Spring注解编程模型

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

      * 元素XML Schema命名空间需要与其处理类建立映射关系，且配置在相对于classpath的规约资源`/META-INF/spring.handler`文件中

        ```java
        http\://www.springframework.org/schema/context=org.springframework.context.config.ContextNamespaceHandler
        ...
        ```

      * Spring容器根据`/META-INF/spring.handler`的配置，定位到命名空间context所对应的处理器ContextNamespaceHandler，当Spring应用上下文启动时，调用ContextNamespaceHandler#init()方法，随后注册该命名空间下所有local元素的Bean定义解析器，包括当前运用的component-scan元素：

    * `<context:component-scan>`元素的Bean定义解析器为ComponentScanBeanDefinitionParser，用于解析Bean定义，其API为BeanDefinitionParser，ComponentScanBeanDefinitionParser为其中一种实现

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

    * `ClassPathScanningCandidateComponentProvider#findCandidateComponents`默认在指定根包路径下，将查找所有标注@Component及派生注解的BeanDefinition集合，并且默认的过滤规则由AnnotationTypeFilter及@Component的元信息（ClassMetaData和AnnotationMetaData）共同决定

* Spring组合注解

  * 包含一个或多个其他注解，目的在于将这些关联的注解行为组合成单个自定义注解
  * 如@SpringBootApplication既是Spring模式注解，又是组合注解
  * 在Java中，Class对象是类的元信息载体，承载了其成员的元信息对象，包括字段(Filed)、方法(Method)、构造器(Constructor)及注解Annotation等，而Class的加载通过ClassLoader#loadClass(String)方法实现
  * Spring Framework的类加载则通过ASM实现，如ClassReader，ASM更为底层，读取的是类资源，直接操作其中的字节码，获取相关元信息，同时便于Spring相关的字节码提升。在读取元信息方面，Spring抽象出MetadataReader接口

* Spring注解属性别名和覆盖

  * 注解属性覆盖：较低层注解能够覆盖其元注解的同名属性
  * 隐性覆盖：属于低层次注解属性覆盖高层次元注解同名属性
  * 显示覆盖：是@AliasFor提供的属性覆盖能力
  * 多层次注解属性之间的@AliasFor关系只能由较低层向较高层建立

# 第八章 Spring注解驱动设计模式

## Spring @Enable模块驱动

* @Enable模块驱动，模块是指具备相同领域的功能组件集合，组合所形成的一个独立单元，如Web MVC模块、AspectJ代理模块、Caching缓存模块、JMX（Java管理扩展）模块、Async（异步处理）模块等
* 意义：能够简化装配步骤，实现按需装配，同时屏蔽组件集合装配的细节，但是该模式必须手动触发，即必须标注在某个配置Bean中
* Spring Framework
  * @EnableMvc、@EnableTransactionManagement、@EnableCaching、@EnableMBeanExport、@EnableAsync、@EnableWebFlux、@EnableAspectJAutoProxy
* Spring Boot
  * @EnableAutoConfiguration、EnableManagementContext(Actuator管理模块)、@EnableConfigurationProperties(配置属性绑定模块)、@EnableOAuth2Sso(OAuth2单点登录模块)
* Spring Cloud
  * @EnableEurekaServer
  * @EnableConfigServer
  * @EnableFeignClients
  * @EnableZuulProxy
  * @EnableCircuitBreaker

## 自定义@Enable模块驱动

* 注解驱动和接口编程都使用了@Import，其职责在于装载导入类，将其定义为Spring Bean

* 注解驱动实现

  1. @Configuration标注一个含有@Bean标注方法的类
  2. 自定义@EnableXXX注解，其中@Import(@Configuration标注的配置类.class)
  3. @Configuration、@EnableXXX标注到引导类上

* 接口编程实现

  * 需实现ImportSelector或ImportBeanDefinitionRegistrar接口

  * 基于ImportSelector接口实现

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

## Spring Web自动装配

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

  ```xml
  pom.xml
  <!-- Servlet 3.0 API -->
  <dependency>
    <groupId>javax.servlet</groupId>
    <artifactId>javax.servlet-api</artifactId>
    <version>3.0.1</version>
    <scope>provided</scope>
  </dependency>
  <!-- Spring 3.x 最新发布版本 -->
  <dependency>
    <groupId>org.springframework</groupId>
    <artifactId>spring-webmvc</artifactId>
  </dependency>
  <!-- Maven war 插件 -->
  <plugin>
    <groupId>org.apache.maven.plugins</groupId>
    <artifactId>maven-war-plugin</artifactId>
    <configuration>
      <!-- 忽略错误，当web.xml不存在时 -->
      <failOnMissingWebXml>false</failOnMissingWebXml>
    </configuration>
  </plugin>
  <!-- Tomcat Maven 插件用于构建可执行 war -->
  <plugin>
    <groupId>org.apache.tomcat.maven</groupId>
    <artifactId>tomcat7-maven-plugin</artifactId>
    <version>2.1</version>
    <executions>
      <execution>
        <id>tomcat-run</id>
        <goals>
          <!-- 最终打包成可执行的jar包 -->
          <goal>exec-war-only</goal>
        </goals>
        <phase>package</phase>
        <configuration>
          <!-- ServletContext 路径 -->
          <path>/</path>
        </configuration>
      </execution>
    </executions>
  </plugin>
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

## Web 自动装配原理

* Spring Framework是基于Servlet 3.0技术，其中"ServletContext配置方法"和"运行时插拔"是技术保障
* ServletContext配置方法
  * 通过编程的方式动态地装配Servlet、Filter及各种Listener，增加了运行时配置的弹性
    * 配置组件—配置方法—配置对象
    * Servlet—ServletContext#addServlet—ServletRegistration或ServletRegistration.Dynamic
    * Filter—ServletContext#addFilter—FilterRegistration或FilterRegistration.Dynamic
    * Listener—ServletContext#addListener—无
  * ServletContext配置方法为Web应用提供了运行时装配的能力，还需在适当的时机加以装配
* 运行时插拔
  * ServletContext配置方法仅能在ServletContextListener#contextInitialized或ServletContainerInitializer#onStartup方法中被调用
  * ServletContextListener的职责，用于监听Servlet上下文（ServletContext）的声明周期事件，包括初始化事件（由ServletContextListener#contextInitialized方法监听）和销毁事件
  * Servlet和Filte对外提供服务之前，必然经过Servlet上下文初始化事件
  * 当容器或应用启动时，ServletContainerInitializer#onStartup(Set<Class<?>>, ServletContext)方法将被调用，同时通过@HandlesTypes#value()属性方法来指定关心的类型。该方法调用早于ServletContextListener#contextInitialized(ServletContextEvent)方法
    * 不过ServletContainerInitializer的一个或多个实现类需要存放在`javax.servlet.ServletContainerInitializer`的文本文件中（独立JAR包中的/META-INF/services目录下）
  * 假设一个Servlet需要装配，并且提供Web服务，首先通过ServletContext配置方法addServlet动态地为其装配，随后，在ServletContainerInitializer#onStartup实现方法中加以实现
    * 如果需要装配N个Servlet或Filter，那么Servlet或Filter及ServletContainerInitializer的实现打包在若干JAR包中，当Servlet应用依赖这些JAR包后，这些Servlet或Filter就自动装配到Web应用中了
    * ==云对讲hessian==
  * AbstractContextLoaderInitializer：如果构建Web Root应用上下文(WebApplicationContext)成功则替代web.xml注册ContextLoaderListener
    * AbstractDispatcherServletInitializer：替代web.xml注册DispatcherServlet，并且如果必要的话，创建Web Root应用上下文(WebApplicationContext)
      * AbstractAnnotationConfigDispatcherServletInitializer：具备Annotation配置驱动能力的AbstractDispatcherServletInitializer
  * 三种实现类均为抽象类
    * 原因一：如果它们是WebApplicationInitialier实现类，那么这三个类均会被SpringServletContainerInitializer作为具体实现添加到WebApplicationInitializer集合initializers中，随后顺序迭代执行onStartup(ServletContext)方法
    * 原因二：抽象实现提供模版化的配置接口，最终将相关配置的决策交给开发人员
  * AbstractContextLoaderInitializer装配原理
    * 在Spring Web MVC中，DispatcherServlet有专属的WebApplicationContext，它继承了来自Root WebApplicationContext的所有Bean，以便@Controller等组件依赖注入
    * 在传统的Servlet应用场景下，Spring Web MVC的Root WebApplicationContext由ContextLoaderListener装载（通常配置在web.xml文件中）
    * ContextLoaderListener是标准的ServletContextListener实现，监听ServletContext生命周期。当Web应用启动时，首先，Servlet容器调用ServletContextListener实现类的默认构造器，随后contextInitialized(ServletContextEvent)方法被调用。反之，当Web应用关闭时，Servlet容器调用其contextDestroyed(ServletContextEvent)方法
    * 当Web应用运行在Servlet3.0+环境中时，以上web.xml部署ContextLoaderListener的方式可替换为实现抽象类AbstractContextLoaderInitializer来完成（不推荐），通常情况下，子类只需要实现createRootApplicationContext()方法。ContextLoaderListener不允许执行重复注册到ServletContext，当多个ContextLoaderListener监听contextInitialized时，其父类ContextLoader禁止Root WebApplicationContext重复关联ServletContext
  * AbstractAnnotationConfigDispatcherServletInitializer装配原理
    * 实现该抽象类（推荐）
  * Spring Framework 基于Servlet3.0特性而构建的Web自动装配的原理
    * SpringServletContainerInitializer通过实现Servlet3.0 SPI 接口ServletContainerInitializer，与@HandlesTypes配合过滤出WebApplicationInitializer具体实现类集合，随后顺序迭代地执行该集合元素，进而利用Servlet3.0配置API实现Web自动装配的目的。同时，结合Spring Framework 3.2抽象实现AbstractAnnotationConfigDispatcherServletInitializer，极大地简化了注解驱动开发的成本

## Spring 条件装配

* 编译时差异化

  * 偏资源处理，构建不同环境的归档文件，通常依赖外部工具，比如使用Maven Profile构建

* 运行时配置化

  * 利用不同环境的配置控制统一归档文件的应用行为，如设置环境变量或Java系统属性
  * Spring Framework

* 基于增加桥接XML上下文配置文件的方式实现

  * `<import resource="classpath:/META-INF/${env}-context.xml"/>`，此时，Spring上下文需要替换占位符变量env，该值可以来自外部化配置，如Java系统属性或操作系统环境变量
  * `String envValue = System.getProperty("env", "prod");`，envValue可能来自-D命令行启动参数，当参数不存在时，使用prod作为默认值。`System.setProperty("env", envValue);`

* Bean 注册方式：XML配置驱动`<beans profile="…">`和注解驱动（`@Profile`）

  * 或，@Profile({"dev","prod"})
  * 非，@Profile("!dev")

* 当有效Profile不存在时，采用默认Profile

  * ConfigurableEnvironment API编码配置
    * 设置Active Profile：setActiveProfiles(String...)
    * 添加Active Profile：addActiveProfile(String)
    * 设置Default Profile：setDefaultProfiles(String...)
  * Java系统属性配置
    * 设置Active Profile：spring.profiles.active
    * 设置Default Profile：spring.profiles.default
  * 应用
    * 接口A，接口B、C实现A，在B上@Profile("java8")、C上@Profile("java7")
    * 设置ConfigurableEnvironment.setActiveProfiles("java8")，ConfigurableEnvironment.setDefaultProfiles("java7")

* @Conditional条件装配

  * 与配置条件装配Profile（偏向于静态激活和配置）职责相似，都是加载匹配的Bean，不同的是@Conditional（关注运行时动态选择）具备更大的弹性

  * 允许指定一个或多个Condition实现类，当所有的Condition均匹配时，说明当前条件成立

  * @ConditionalOnClass、@ConditionOnBean和@ConditionalOnProperty等

  * 自定义@Conditional条件装配

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
    /**
     * 指定系统属性名称与值匹配条件
     */
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

* Spring Framework组件装配的自动化程度仍不失特别理想，比如@Enable模块驱动不仅需要将@Enable注解显示地标注在配置类上，而且该类还依赖@Import或@ComponentScan的配合。同时，Web自动装配必须部署在外部Servlet3.0+容器中，无法做到SpringWeb应用自我驱动

# 第九章 SpringBoot自动装配

* 在Spring Framework时代，当Spring应用的@Component或@Configuration Class需要被装配时，应用需要借助@Import或@ComponentScan的能力
* @ComponentScan(basePackages="")仅扫描其标注类所在包，而不是所有包，考虑替换为@ComponentScan(basePackageClasses="")，其中basePackagesClasses指向默认包中的类即可
* SpringBoot自动装配：整合Spring注解编程模型、@Enable模块驱动及条件装配等Spring Framework原生特性
* @SpringBootConfiguration元标注在@SpringBootApplication上更多的意义在于@SpringBootApplication的配置类能够被@ComponentScan识别
* @SpringBootApplication和@EnableAutoConfiguration都能激活自动装配（标注类作为SpringApplication#run参数），不过@SpringBootApplication能够减少多注解所带来的配置成本，如配置@SpringBootApplication#scanBasePackages属性，等同于配置@Component#basePackages属性等
* 优雅地替换自动装配
  * SpringBoot优先解析自定义配置类，并且内建的自动装配配置类实际上为默认的条件配置，即一旦应用存在自定义实现，则不再将它们（内建）装配
* 失效自动装配
  * 代码配置方式：
    * 配置类型安全的属性方法：@EnableAutoConfiguration.exclude();
    * 配置排除类名的属性方法：@EnableAutoConfiguration.excludeName();
  * 外部化配置方式
    * 配置属性：spring.autoconfiguration.exclude
  * Spring Framework时代失效自动装配，要么阻断@Configuration Class BeanDefinition的注册，那么通过@Conditional实现条件控制
  * @EnableAutoConfiguration可通过ImportSelector实现，且exclude()和excludeName()属性可从其AnnotationMetadata对象中获取

## SpringBoot自动装配原理

* @EnableAutoConfiguration如何装配组件，以及装配哪些组件？
  * getCandidateConfigurations(AnnotationMetadata, AnnotationAttributes)方法读取候选装配组件
    * 搜索META-INF/spring.factories资源内容（可能存在多个），将一个或多个META-INF/spring.factories资源内容作为Properties文件读取，合并为一个Key为接口的全类名，Value是实现类全类名列表的Map，从Map中查找并返回方法指定类名所映射的实现类全类名列表，执行removeDuplicates(List)方法，利用Set不可重复性达到去重的目的
* @EnableAutoConfiguration如何排除某些组件的自动装配，以及与其配置手段是如何交互的？
  * getExclusions(AnnotationMetadata, AnnotationAttributes)方法可以解释
* 总之，AutoConfigurationImportSelector读取自动装配Class的流程为：
  * 通过SpringFactoriesLoader#loadFactoryNames(class,ClassLoader)方法读取所有META-INF/spring.factories资源中@EnableAutoConfiguration所关联的自动装配Class集合
  * 读取当前配置类所标注的@EnableAutoConfiguration属性exclude和excludeName，并与spring.autoconfigure.exclude配置属性合并为自动装配Class排除集合
  * 检查自动装配Class排除集合是否合法
  * 排除候选自动装配Class集合中的排除名单
  * 再次过滤候选自动装配Class集合中Class不存在的成员
* @EnableAutoConfiguration自动装配事件
* @EnableAutoConfiguration自动装配生命周期
* @EnableAutoConfiguration排序自动装配组件
  * 绝对自动装配顺序-@AutoConfigureOrder
  * 相对自动装配顺序-@AutoConfigureBefore和@AutoConfigureAfter，使用name()属性方法
* @EnableAutoConfiguration自动装配BasePackages

## 自定义SpringBoot自动装配

* 当注解@EnableAutoCongiguration激活自动装配后，META-INF/spring.factories资源中声明的配置Class随即被装配——SPI机制
* 自动装配包括配置资源META-INF/spring.factories和配置Class，两者可被关联到名为"starter"的共享类库
* 自定义SpringBoot Starter
  * 命名模式：${module}-spring-boot-starter
  * 新建SpringBoot Starter工程—formatter-spring-boot-starter
    * 基础依赖：org.springframework.boot:spring-boot-starter:optional-true
      * true表明自定义starter不应该传递spring-boot-starter的依赖
  * 新建接口-Formatter、实现接口-DefaultFormatter、JsonFormatter
  * 实现DefaultFormatter自动装配—FormatterAutoConfiguration（@Configuration、@Bean@ConditionalOnMissingClass(value="...")标注方法返回Defaultformatter、@Bean@ConditionalOnClass(name="….")标注方法返回JsonFormatter）
  * META-INF/spring.factories资源声明FormatterAutoConfiguration
  * 构建SpringBoot Starter，`mvn -Dmaven.test.skip -U clean install`
  * 添加该SpringBoot Starter依赖，新建引导类,`context.getBean(Formatter.class)`

## SpringBoot条件化自动装配

* Class条件注解
  * @ConditionalOnClass（当指定类存在于Class Path）和@ConditionalOnMissingClass（当指定类缺失于Class Path）
* Bean条件注解
  * @ConditionalOnBean和@ConditionalOnMissingBean基于BeanDefinition进行名称或类型的匹配
    * @ConditionalOnClass(XXX.class)，XXX必须存在于Class Path
    * @ConditionalOnMissingBean标注的方法返回对象类型必须在所有的Spring应用上下文中不存在
* 属性条件注解
  * `@ConditionalOnProperty(prefix="formatter", name="enabled", havingValue="true", matchIfMissing=true)`
    * `matchIfMissing=true`表明当属性配置不存在时，同样视为匹配，可以使其为false来不装配
    * Spring Environment的属性`formatter.enable=true`时才会自动装配
      * 内部化配置：SpringApplicationBuilder.properties("formatter.enable=true");
      * 外部化配置：application.properties中
* Resource条件注解
  * @ConditionalOnResource属性方法resources()指示只有资源必须存在时条件方可成立
  * 如`@ConditionalOnResource(resource="META-INF/spring.factories")`
* Web应用条件注解
  * @ConditionalOnWebApplication（当前应用是否为Web类型的条件判断注解）和@ConditionalOnNotWebApplication
* Spring表达式条件注解
  * `@ConditionalOnExpression("${formatter.enabled:true} && ${spring.jmx.enabled:true}")`

# 第十章 SpringApplication

* SpringBoot的自动装配所依赖的注解驱动、@Enable模块驱动、条件装配、Spring工厂加载机制等特性均来自Spring Framework，也都围绕Spring应用上下文及其管理的Bean生命周期
* Spring Framework时代，Spring应用上下文通常由容器启动，如ContextLoaderListener或WebApplicationInitializer的实现类由Servlet容器装载并驱动
* SpringBoot时代，Spring应用上下文的启动则通过调用SpringApplication#run(Object,String…)或SpringApplicationBuilder#run(String...)方法配合@SpringBootApplication或@EnableAutoConfiguration注解的方式完成
* 启动失败、自定义Banner、自定义SpringApplication、流式Builder API、Spring应用事件和监听器、Spring应用Web环境、存储Spring应用启动参数、使用ApplicationRunner或CommandLineRunner接口、Spring应用退出、Spring应用管理特性

## SpringApplication初始化阶段

* 属于运行前的准备阶段

  * 允许指定应用的类型，Web应用和非Web应用，SpringBoot2.0开始，Web应用又可分为Servlet Web和Reactive Web，调整Banner输出、配置默认属性等，这些只要在run()方法之前指定即可

* 构造阶段

  * 由构造器完成，SpringApplication.run()等价于new SpringApplication.run()，都需要Class类型的primarySources参数，通常情况下，引导类将作为primarySources参数的内容

  * 主配置类

    * @EnableAutoConfiguration能够激活SpringBoot内建和自定义组件的自动装配特性
    * primarySources参数实际为SpringBoot应用上下文的Configuration Class，该配置类也不一定非的使用引导类，主配置类primarySources与传统Spring Configuration Class并无差异（任何标注@Configuration和@EnableAutoConguration的类都能作为primarySources属性值）
    * 主配置类属性primarySources除初始化构造器参数外，还能通过SpringApplication#addPrimarySources(Collection)方法追加修改

  * SpringApplication的构造过程

    * SpringApplication#run(Class,String...)方法的执行会伴随SpringApplication对象的构造，其调用的构造器为SpringApplication(Class)，实际执行的构造器为SpringApplication(ResourceLoader,Class<?>…)，其中主配置primarySources被SpringApplication对象primarySources属性存储，随后依次调用：

      * deduceWebApplicationType()：推断Web应用类型
      * setInitializers(Collection)：加载Spring应用上下文初始化器
      * setListeners(Collection)：加载Spring应用事件监听器
      * deduceMainApplicationCLass()：推断应用引导类

    * 推断Web应用类型

      * 应用类型可在SpringApplication构造后及run方法之前，再通过setWebApplicationType(WebApplicationType)方法调整，由于当前Spring应用上下文尚未准备，所以实现采用的是检查当前ClassLoader下基准Class的存在性判断（通过ClassLoader判断核心类是否存在来推断Web应用类型）
      * 利用ClassUtils#isPresent(String,ClassLoader)方法依次判断DispatcherHandler、ConfigurableWebApplicationContext、Servlet和DispatcherServlet的存在性组合情况，从而推断Web应用类型
        * 当DispatcherHandler存在，并且DispatcherServlet不存在时，即SpringBoot仅依赖WebFlux存在时，WebApplicationType.REACTIVE
        * 当Servlet和ConfigurableWebApplicationContext均不存在时为非Web应用，即WebApplicationType.NONE
        * 当Spring WebFlux和Spring WebMVC同时存在时，为WebApplicationType.SERVLET

    * 加载Spring应用上下文初始化器ApplicationContextInitializer

      * 运用了Spring工厂加载机制方法SpringFactoriesLoader.loadFactoryNames(Class,ClassLoader)返回所有META-INF/spring.factories资源中配置的ApplicationContextInitializer实现类名单，然后初始化这些实现类（必须存在默认构造器），这些实现类可以实现Ordered接口进行排序
      * 然后将这些初始化后的实现类关联到SpringApplication#initializers属性，供后续操作使用
        * setInitializers(Collection)方法实现属于覆盖更新，即在执行SpringApplication#run方法前，这些在构造过程中加载的ApplicationContextInitializer实例集合存在被setInitializers(Collection)方法覆盖的可能

    * 加载Spring应用事件监听器ApplicationListener

      * 在SpringApplication构建器执行setInitializers(Collection)方法后，立即执行
      * 实现手段与`加载Spring应用上下文初始化器`基本一致
        * 先执行getSpringFactoriesInstances方法，再设置实例集合，只不过初始化的对象类型从ApplicationContextInitializer变成ApplicationListener，setListener(Collection)方法同样时覆盖更新

    * 推断引导类

      * 根据当前线程执行栈来判断其栈中哪个类包含main方法

        ```java
        private Class<?> deduceMainApplicationClass(){
          try {
            StackTraceElement[] stackTrace = new RuntimeException().getStackTrace();
            for(StackElement stackElement : stackTrace) {
              if("main".equals(stackTraceElement.getMethodName())) {
                return Class.forName(stackTraceElement.getClassName());
              }
            }
          }catch(Exception e) {
            //
          }
        }
        ```

* 配置阶段

  * 可选，主要用于调整（Setter方法）或补充（add*方法）构造阶段的状态，左右运行时行为
  * 自定义SpringApplication
    * 调整SpringApplication设置
      * SpringApplivationBuilder
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
      * application.properties文件，实际上它可以覆盖SpringApplication#setDefaultProperties方法的设置，从而影响SpringApplication的行为
      * 同时，SpringApplication#setDefaultProperties方法也能影响application.properties文件搜索的路径，通过属性spring.config.location或spring.config.additional-location实现

## SpringApplication运行阶段

* 属于核心阶段，完整地围绕run(String...)方法展开

  * 结合初始化阶段完成的状态，进一步完善了运行时所需要准备的资源，随后启动Spring应用上下文，在此期间伴随SpringBoot和Spring事件的触发，形成完整的SpringApplication生命周期

* SpringApplication准备阶段

  * 从run(String...)方法调用开始，到refreshContext(ConfigurableApplicationContext)调用前

  * 该过程依次准备的核心对象为

    * ApplicationArguments

    * SpringApplicationRunListeners

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

        * 只要遵照SpringApplicationRunListener构造器参数约定，以及结合SpringFactoriesLoader机制，完全能够将该接口进行扩展

      * SpringBoot事件所对应的ApplicationListener实现是由SpringApplication构造器参数关联并添加到属性SimpleApplicationEventMulticasterinitialMulticaster中的

        * 比如SpringApplicationRunListener#starting()方法运行后，ApplicationStartingEvent随即触发，此时initialMulticaster同步执行`ApplicationListener<ApplicationStartingEvent>`集合的监听回调方法onApplicationEvent(ApplicationStartingEvent)，这些行为保证均源于Spring Framework事件/监听器的机制

        * SpringBoot事件和Spring事件是存在差异的

          * Spring事件是由Spring应用上下文ApplicationContext对象触发的
            * Spring事件/监听机制属于事件/监听器模式，可视为观察者模式的扩展
            * Spring事件监听器通过限定监听方法数量，仅抽象单一方法onApplicationEvent(ApplicationEvent)，将其用于监听Spring事件ApplicationEvent
            * ApplicationListener支持ApplicationEvent泛型监听
              * 当ContextRefreshedEvent事件发布后，`ApplicationListener<ContextRefreshedEvent>`实现的onApplicationEvent方法仅监听具体ApplicationEvent实现，不再监听所有的Spring事件，无需借助instanceof方式进行筛选
              * 由于泛型参数的限制，泛型化的ApplicationListener无法监听不同类型的ApplicationEvent
            * SmartApplicationListener接口
              * 通过supports*方法过滤需要监听的ApplicationEvent类型和事件源类型，从而达到监听不同类型的ApplicationEvent的目的
            * Spring事件发布
              * SimpleApplicationEventMulticaster接口
                * 主要承担两种职责：关联（注册）ApplicationListener和广播ApplicationEvent
                * 默认情况下，无论在传统的Spring应用中，还是在SpringBoot使用场景中，均充当同步广播事件对象的角色，开发者只需关注ApplicationEvent类型及对应的ApplicationListener实现即可
            * Spring内建事件
              * RequestHandledEvent
              * ContextRefreshedEvent：Spring应用上下文就绪事件
                * 当ConfigurableApplicationContext#refresh()方法执行到finishRefresh()方法时，Spring应用上下文发布ContextRefreshedEvent
                * 此时，Spring 应用上下文中Bean均已完成初始化，并能投入使用，通常`ApplicationListener<ContextRefreshedEvent>`实现类监听该事件，用于获取需要的Bean，防止出现Bean提早初始化所带来的潜在风险
              * ContextStartedEvent：Spring应用上下文启动事件
                * AbstractApplicationContext的start()中发布
              * ContextStopedEvent：Spring应用上下文停止事件
                * AbstractApplicationContext的stop()中发布
              * ContextClosedEvent：Spring应用上下文关闭事件
                * 由ConfigurableApplicationContext#close()方法调用时触发，发布ContextClosedEvent
              * Spring事件的API表述为ApplicationEvent，继承于Java规约的抽象类EventObject，并需要显示地调用父类构造器传递事件源参数。以上四种Spring内建事件均继承于抽象类ApplicationContextEvent（Spring上下文事件），并将ApplicationContext作为事件源
            * 自定义Spring事件
              * 需扩展ApplicationEvent，然后由ApplicationEventPublisher#publishEvent()方法发布
            * Spring事件监听
              * Spring事件/监听机制围绕ApplicationEvent、ApplicationListener和ApplicationEventMulticaster三者展开
              * ApplicationListener监听Spring内建事件
                * AbstractApplicationContext提供发布ApplicationEvent和关联ApplicationListener实现的机制，并且任意Spring Framework内建ConfigurableApplicationContext实现类均继承AbstractApplicationContext的事件/监听行为，如`context.addApplicationListener(event->System.out.println("触发事件："+event.getClass().getSimpleName()));`，`context.refresh()`，`context.stop()`，`context.start()`，`context.close()`
              * ApplicationListener监听实现原理
                * Spring事件通过调用SimpleApplicationEventMulticaster#multicastEvent方法广播，根据ApplicationEvent具体类型查找匹配的ApplicationListener列表，然后逐一同步或异步地调用ApplicationListener#onApplicationEvent(ApplicationEvent)方法，实现ApplicationListener事件监听
              * 注解驱动Spring事件监听@EventListener
                * @EventListener必须标记在Spring托管Bean的public方法上，支持返回类型为非void的情况，支持单一类型事件监听，当它监听一个或多个ApplicationEvent时，其参数可为零到一个ApplicationEvent
                * @EventListener异步方法，在原有方法基础上增加注解@Async即可
                  * 类上需要添加@EnableAsync来激活异步，否则@Async无效
                  * 方法返回值类型为void
                * @EventListener方法执行顺序
                  * 通过Ordered接口和标注@Order注解来实现监听次序
                * @EventListener方法监听泛型
            * Spring事件广播器
              * ApplicationEventPublisher#publishEvent方法
                * 由AbstractApplicationContext实现
              * ApplicationEventMulticaster#multicastEvent
                * 由SimpleApplicationEventMulticaster实现，是ApplicationEvent、ApplicationListener和ConfigurableApplicationContext之间连接的纽带
          
          | 监听类型               | 访问性 | 顺序控制       | 返回类型   | 参数数量 | 参数类型               | 泛型事件 |
          | ---------------------- | ------ | -------------- | ---------- | -------- | ---------------------- | -------- |
| @EventListener同步方法 | public | @Order         | 任意       | 0或1     | 事件类型或泛型参数类型 | 支持     |
          | @EventListener异步方法 | public | Order          | 非原生类型 | 0或1     | 事件类型或泛型参数类型 | 支持     |
          | ApplicationListener    | public | Order或Ordered | void       | 1        | 事件类型               | 不支持   |
          
          * SpringBoot事件的发布者则是SpringApplication.initialMulticaster属性(SimpleApplicationEventMulticaster类型)，并且SimpleApplicationEventMulticaster也来自Spring Framework
            * SpringBoot事件/监听机制同样基于ApplicationEventMulticaster、ApplicationEvent和ApplicationListener实现
  * SpringBoot内建事件监听器
              * 在SpringBoot场景中，无论Spring事件监听器，还是SpringBoot事件监听器，均配置在META-INF/spring.factories中，并以org.springframework.context.ApplicationListener作为属性名称，属性值为ApplicationListener的实现类
              * 最重要的SpringBoot内建事件监听器：
                * ConfigFileApplicationListener
                  * 监听事件：ApplicationEnvironmentPreparedEvent和ApplicationPreparedEvent
                  * 负责SPringBoot应用配置属性文件的加载，默认为application.properties或application.yml
                * LoggingApplicationListener
                  * 监听事件：ApplicationStartingEvent或ApplicationEnvironmentPreparedEvent或ApplicationPreparedEvent或ContextClosedEvent或ApplicationFailedEvent
                  * 用于SpringBoot日志系统的初始化（日志框架识别，日志配置文件加载等）
          
            * SpringBoot事件
              * SpringBoot事件类型继承Spring事件类型ApplicationEvent，并且也是SpringApplicationEvent的子类
              * 大多数Spring内建事件为Spring应用上下文事件，即ApplicationContextEvent，其事件源为ApplicationContext。而SpringBoot事件源则是SpringApplication，其内建事件根据EventPublishingRunListener的生命周期回调方法依次发布。ApplicationStartingEvent、ApplicationEnvironmentPreparedEvent、ApplicationPreparedEvent、ApplicationStartedEvent、ApplicationReadyEvent和ApplicationFailedEvent，其中ApplicationReadyEvent和ApplicationFailedEvent在Spring应用上下文初始化后发布，即在ContextRefreshedEvent之后发布
            * SpringBoot事件监听手段
              * 通过SpringApplication关联ApplicationListener对象集合，关联途径有二：
                * 一为SpringApplication构造阶段在Class Path下所加载所有META-INF/spring.factories资源中的ApplicationListener对象集合
                * 二是通过方法SpringApplication#addListeners(ApplicationListener…)或SpringApplicationBuilder#listeners(ApplicationListener…)显示地装配
            * SpringBoot事件广播器
              * 同样来源于Spring Framework的实现类SimpleApplicationEventMulticaster，其广播行为与Spring事件广播毫无差别，只不过SpringBoot中发布的事件类型是特定的
          * SpringBoot事件/监听机制继承于Spring事件/监听机制，其事件类型继承于SpringApplicationEvent，事件监听器仍通过ApplicationListener实现，而广播器实现SimpleApplicationEventMulticaster将它们关联起来
          
      * 装配ApplicationArguments
      
  * 当执行SpringApplicationRunListeners#starting()方法后，SpringApplication运行进入装配ApplicationArguments逻辑，其实现类为DefaultApplicationArguments，一个用于简化SpringBoot应用启动参数的封装接口，它的底层实现基于Spring Framework中的命令行配置源SimpleCommandLinePropertySource
      * 例如命令行参数"—name=woody"将被SimpleCommandLinePropertySource解析为"name:woody"的键值属性
  
    * 准备ConfigurableEnvironment
  
* Banner
  
* 创建Spring应用上下文（ConfigurableApplicationContext）
  
  * SpringApplication通过createApplicationContext()方法创建Spring应用上下文
        * 默认情况下，根据SpringApplication构造阶段所推断的Web应用类型进行ConfigurableApplicationContext的创建
    * 通过setApplicationContextClass(Class)方法或SpringApplicationBuilder#contextClass(Class)方法，根据指定ConfigurableApplicationContext类型创建Spring应用上下文
    
    * Spring应用上下文运行前准备
    
  * 由SpringApplication#prepareContext方法完成
        * Spring应用上下文准备阶段
      * 从prepareContext方法开始，到SpringApplicationRunListeners#contextPrepared
          * 设置Spring应用上下文ConfigurableEnvironment，即context.setEnvironment(environment)
          * Spring应用上下文后置处理
            * 根据SpringApplication#postProcessApplicationContext(ConfigurableApplicationContext)方法的命名而来，允许子类覆盖该实现，可能增加额外需要的附加功能
          * 运用Spring应用上下文初始化器(ApplicationContextInitializer)
          * 执行SpringApplicationRunListener#contextPrepared方法回调，当Spring应用上下文创建并准备完毕时，该方法被回调
        * Spring应用上下文装载阶段
          * 按照SpringApplication#prepareContext方法实现，本阶段可划分为四个过程
            * 注册SpringBoot Bean
              * SpringApplication#prepareContext方法将之前创建的ApplicationArguments对象和可能存在的Banner实例注册为Spring单体Bean
              * `context.getBeanFactory().registerSingleton("springApplicationArguments",applicationArguments);`
            * 合并Spring应用上下文配置源
              * 由getAllResources()方法实现
            * 加载Spring应用上下文配置源
              * load(ApplicationContext,Object[])方法将承担加载Spring应用上下文配置源的职责，该方法将Spring应用上下文Bean装载的任务交给了BeanDefinitionLoader
            * 回调SpringApplicationRunListener#contextLoaded方法
    
    * SpringBootExceptionReporter集合
  
* ApplicationContext启动阶段

  * 由refreshContext(ConfigurableApplicationContext)方法实现
  * 随着refreshContext(ConfigurableApplicationContext)方法的执行，Spring应用上下文正式进入Spring生命周期，SpringBoot核心特性也随之启动，如组件自动装配、嵌入式容器启动Production-Ready特性

* ApplicationContext启动后阶段

  * SpringApplication#afterRefresh(ConfigurableApplicationContext,ApplicationArguments)方法并未给Spring应用上下文启动后阶段提供实现，而是交给开发人员自行扩展
    * afterRefresh()

## SpringApplication 结束阶段

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

## SpringBoot应用退出

* SpringApplication注册shutdownhook线程，当JVM退出时，确保后续Spring应用上下文所管理的Bean能够在标准的Spring生命周期中回调，从而合理地销毁Bean所依赖的资源，如会话状态、JDBC连接、网络连接等
  * 默认情况下，Spring应用上下文将注册shutdownHook线程，实现优雅的SpringBean销毁生命周期回调
* 该特性是SpringApplication借助ConfigurableApplicationContext#registerShutdownHook API实现的

# 总之

* SpringApplication是围绕SpringApplication生命周期来展开讨论的，分为"初始化"、"运行”和”结束“三个阶段，主要的核心特性包括SpringApplicationRunListener、SpringBoot事件和Spring应用上下文的生命周期管理等
* SpringBoot引入SpringApplication是对Spring Framework的应用上下文生命周期的补充
* 在SpringFramework时代，Spring应用上下文通常由容器启动，如ContextLoaderListener或WebApplicationInitializer的实现类由Servlet容器装载并驱动，到了SpringBoot时代，Spring应用上下文的启动则通过调用SpringApplication#run(Object,String…)或SpringApplicationBuilder#run(String…)方法并配合@SpringBootApplication或@EnableAutoConfiguration注解的方式完成。
* SpringApplication可以引导非Web应用和嵌入式Web应用，而且它还能出现在SpringBoot应用部署在传统Servlet3.0+容器中的场景
* 传统的Spring应用上下文生命的起点源于ConfigurableApplicationContext对象的创建，运行则由其refresh()方法引导，而终止于close()方法的调用
* Spring Framework内建的ConfigurableApplicationContext实现类均继承于抽象类AbstractApplicationContext，在AbstractApplicationContext#refresh()方法执行过程中，伴随着组件BeanFactory、Environment、ApplicationEventMulticaster和ApplicationListener的创建，它们的职责分别涉及Bean容器、Spring属性配置、Spring事件广播和监听。
  * 实际上，SpringApplication并未从本质上改变这些，因为AbstractApplicationContext提供了扩展接口，如setEnvironment(ConfigurableEnvironment)方法允许替换默认的Environment对象，以及initApplicationEventMulticaster和ApplicationListener Bean的机制，不过，这些扩展接口被SpringApplication在Spring应用上下文调用refresh()方法之前予以运用，在SpringApplicationRunListener实现类EventPublishingRunListener的帮助下，全新地引入SpringBoot事件