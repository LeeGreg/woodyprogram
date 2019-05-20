# Spring

## 是什么

* Spring 一个为 Java 应用程序开源开发框架，帮助开发者解决了开发中基础性的问题，使得开发人员可以专注于应用程序的开发
* 狭义的是指Spring Framework，广义是指庞大的生态系统，如SpringBoot、SpringCloud、Spring Security、SpringData等

## 配置Spring方式

* 基于XML配置

  * XML文件中`<bean>`定义Bean、id/name属性定义名称、`<property>`子元素或p命名空间动态属性注入Bean、init-method和destroy-method属性Bean生命过程方法、scope属性指定Bean作用范围、lazy-init属性指定Bean延迟初始化
  * Bean定义信息和Bean实现类本身分离
  * web.xml 仅仅配置了DispatcherServlet

* 基于注解配置

  * @Component定义Bean、value属性指定Bean名称、@Autowired/@qualifier自动Bean注入、@Scope指定Bean作用范围、@Lazy指定Bean延迟初始化、@Resource、@PostConstruct 和 @PreDestroy
    * @Required：该注解应用于设值方法
    * @Autowired：该注解应用于有值设值方法、非设值方法、构造方法和变量 
    * @Qualifier：该注解和@Autowired 注解搭配使用，用于消除特定 bean 自动装配的歧义
  * 开启注解装配模式`<context:annotation-config/>`
    * 其作用是隐式的向Spring容器注册Autowired、Common、Persistence、Required这4个AnnotationBeanPostProcessor，然后可以使用@Autowired**、**@Required、@Resource、@ PostConstruct、@ PreDestroy、@PersistenceContext等注解
  * 扫描注解定义的Bean`<context:component-scan base-package="..."/>`
    * 其不但启用了对类包进行扫描以实施注释驱动 Bean 定义的功能，同时还启用了注释驱动自动注入的功能，即包含`<context:annotation-config/> `功能
  * XML注入属性会覆盖注解注入属性

* 基于Java类配置

  * @Configuration注解类，类中方法上标注@Bean定义Bean、@Bean的name属性定义Bean名称、@Autowired注入Bean/调用配置类的@Bean方法进行注入、@Bean的initMethod或destroyMethod指定Bean生命过程方法、@Bean方法定义标注@Scope指定Bean作用范围、@Lazy指定Bean延迟初始化

  * **AnnotationConfigApplicationContext**或子类进行加载基于java类的配置，能够直接通过标注@Configuration的Java类启动Spring容器

  * 组件扫描：`@Configuration`、`@ComponentScan(basePackages = "xxx")`

  * 被@Configuration 所注解的类则表示这个类的主要目的是作为 bean 定义的资源

  * 被@Configuration 声明的类可以通过在同一个类的内部调用@bean 方法来设置嵌入 bean 的依赖关系

  * 由@Bean 注解的方法将会实例化、配置和初始化一个新对象，这个对象将由 Spring 的 IOC 容器来管理

  * @Bean 声明所起到的作用与元素类似。该方法名默认就是Bean的名称，该方法返回值就是Bean的对象

  * 使用bean注解的方法不能是private、final、static的

    ```java
    //最简单的@Configuration 声明类
    //com.gupaoedu 包首先会被扫到，然后再容器内查找被@Component 声明的类，找到 后将这些类按照 Sring bean 定义进行注册
    @Configuration
    @ComponentScan(basePackages = "com.gupaoedu") 
    public class AppConfig{
       @Bean
       public MyService myService() {
         return new MyServiceImpl();
       } 
    }
    //等同于 XML 配置
    <beans>
    	<bean id="myService" class="com.gupaoedu.services.MyServiceImpl"/>
    </beans>
    
    //上述注解配置方式的实例化方式如下:利用 AnnotationConfigApplicationContext 类进行实例化 
    //Spring提供了一个AnnotationConfigApplicationContext类，它能够直接通过标注@Configuration的Java类启动Spring容器
    public static void main(String[] args) {
    	ApplicationContext ctx = new AnnotationConfigApplicationContext(AppConfig.class);
        //注册多个@Configuration配置类
        ctx.register(ServiceConfig.class);
        //刷新容器以应用这些注册的配置类
        ctx.refresh();
        MyService myService = ctx.getBean(MyService.class);
    		myService.doStuff();
    }
    //@Import(...class)可将多个配置类组装到一个配置类中
    //@ImportResource("classpath:com/conf/bean.xml")引入XML配置文件，通过@Autowired可自动注入XML文件中定义的Bean
    ```

  * web应用开发中使用Java配置方式

    * 需要用`AnnotationConfigWebApplicationContext`类来读取配置文件，可以用来配置 Spring 的 Servlet 监听器 `ContrextLoaderListener`
    * 或者 Spring MVC 的 `DispatcherServlet`

    ```xml
    <!--web应用中-->
    <web-app>
       <context-param>
         <param-name>contextClass</param-name>
         <param-value>
            org.springframework.web.context.support.AnnotationConfigWebApplicationContext
         </param-value>
       </context-param>
       <context-param>
         <param-name>contextConfigLocation</param-name>
         <param-value>com.gupaoedu.AppConfig</param-value>
       </context-param>
       <listener>
         <listener-class>
           org.springframework.web.context.ContextLoaderListener
         </listener-class>
       </listener>
     <!--或 Spring MVC 的DispatcherServlet-->  
       <servlet>
         <servlet-name>dispatcher</servlet-name>
         <servlet-class>org.springframework.web.servlet.DispatcherServlet</servlet-class>
         <init-param>
            <param-name>contextClass</param-name>
            <param-value>
     	 		org.springframework.web.context.support.AnnotationConfigWebApplicationContext
            </param-value>
         </init-param>
         <init-param>
            <param-name>contextConfigLocation</param-name>
            <param-value>com.gupaoedu.web.MVCConfig</param-value>
         </init-param>
       </servlet>
       <servlet-mapping>
         <servlet-name>dispatcher</servlet-name>
         <url-pattern>/web/*</url-pattern>
       </servlet-mapping>
    </web-app>
    ```

## 自动装配@Autowired 

* Spring 可以通过向 Bean Factory 中注入的方式自动搞定 bean 之间的依赖关系 

* Spring 容器可以在不使用`< constructor-arg >`和`< property >`元素的情况下自动装配相互协作的 bean 之间的关系

* 基于XML配置文件的自动装配

  ```xml
  <!-- XML 配置文件将一个 bean 设置为自动装配 -->
  <bean id="employeeDAO" class="com.gupaoedu.EmployeeDAOImpl" autowire="byName" />
  ```

* @Autowired 注解来自动装配指定的 bean

  * 在使用@Autowired 注解之前需要在按照如下的配置方式在 Spring 配置文件进行配置才可以使用。 `<context:annotation-config />`

  * 默认按类型（byType）匹配的方式在容器中查找匹配的 Bean，当有且仅有一个匹配的Bean时，Spring将其注入@Autowired标注的变量中，匹配多个类型时配合使用**@Qualifier**注解来指定名称注入

  * 使用required=false属性来标注来防止注入所需类型在Spring容器中不存在而抛异常

  * @Autowired可以对成员变量、方法的入参和构造函数进行标注，来完成自动装配的工作

    * 建议采用在方法上标注@Autowired注解

  * 延迟依赖注入：延迟到调用此属性时才注入属性值，@Lazy注解必须同时标注在目标Bean和属性上

  * @Resource注解也是依赖注入，但要求提供一个Bean名称的属性，如果属性为空，则自动采用标注处的变量名或方法名作为Bean的名称

  * 可以对类中集合类的变量或方法入参进行标注，此时会将容器中类型匹配的所有Bean都注入进

    ```java
  //Spring会将容器中所有类型为Plugin的bean都注入到集合中去
    public class loginService{
       @Autowired(required=false)
       public List<Plugin> pligins;
       public List<Plugin> getPlugins(){
          return plugins;
       }
    }
    ```
  
* 自动装配有哪些局限性?

  * 重写： 仍需用` <constructor-arg>`和 `<property>` 配置来定义依赖，意味着总要重写自动装配。
  * 不能自动装配简单的属性，如基本数据类型，String字符串和类
  * 自动装配总是没有自定义装配精确，因此，如果可能尽量使用自定义装配

* 请解释各种自动装配模式的区别

  * 使用< bean >元素的 autowire 属性为一个 bean 定义指定自动装配模式

  * 在 Spring 框架中共有 5 种自动装配

    * no：默认的方式是不进行自动装配，通过手工设置ref 属性来进行装配bean

    * byName：通过参数名自动装配，如果一个bean的name 和另外一个bean的 property 相同，就自动装配。如果找到的话，就装配这个属性，如果没找到的话就报错

      ```java
    <bean id="signle" class="com.st.sig.Single" autowire="byName" />
      <bean id="mege" name="mege" class="com.st.sig.Mege" />
      public class Single{
          private Mege mege;
          get、set...
      }
      ```
  
    * byType：通过参数的数据类型自动装配，如果一个bean的数据类型和另外一个bean的property属性的数据类型兼容，就自动装配。如果没找到的话就报错 

    * constructor：构造器的自动装配和 byType 模式类似，但是仅仅适用于与有构造器相同参数的 bean， 如果在容器中没有找到与构造器参数类型一致的 bean，那么将会抛出异常 。构造方法中的参数通过byType的形式，自动装配

      ```java
    <bean id="signle" class="com.st.sig.Single" autowire="constructor" >
          	<property name="name" value="zxx"></property>
      </bean>
      public class Single{
      	private String name ;
      	private Single(String name) {
      		this.name = name;
      	}
      	...
      }
      ```
  
    * autodetect：首先尝试使用构造器自动装配，然后才自动选择 byTpe 的自动装配方式 

## @Required

* 适用于bean属性setter方法，并表示受影响的bean属性必须在XML配置文件在配置时进行填充。否则，容器会抛出一个BeanInitializationException异常

  ```java
  public class Student {
     private String name;
     @Required
     public void setName(String name) {
        this.name = name;
     }
     public String getName() {
        return name;
     }
  }
  
  <context:annotation-config/>
  <bean id = "student" class = "com.tutorialspoint.Student">
  	<property name = "name" value = "Zara" />
  </bean>
  ```

## 小问题

* `<mvc:annotation-driven />`会自动注册`RequestMappingHandlerMapping`、`RequestMappingHandlerAdapter`、`ExceptionHandlerExceptionResolver`三个bean，支持使用了像
  * `@RquestMapping`、`ExceptionHandler`等的注解的controller 方法去处理请求
  * `@RequestBody`、`@ResponseBody`
  * `@Valid`对javaBean进行JSR-303验证
  * `ConversionService`的实例对表单参数进行类型转换
  * `@NumberFormat`、`@NumberFormat`注解对数据类型进行格式化

* 解决循环依赖
  * 使用字段注入且有无参数构造器
  * Spring先是用无参构造器实例化Bean对象，此时Spring会将这个对象放入到一个Map中，并且Spring提供了获取这个未设置属性的实例化对象的引用方法

* 可通过注册监听器监听Spring 容器在启动和启动完成的时，并做一个服务性的操作

  ```java
  //可以的
  public class SystemLoaderListener extends ContextLoaderListener{
  	private Logger log = Logger.getLogger(SystemLoaderListener.class);
  	@Override
  	public void contextInitialized(ServletContextEvent e) {
  		super.contextInitialized(e);
  	}
  }
  ```

* Spring怎么保证bean不被回收？

  * IOC容器会一直持有对象的引用，IOC容器本身不会被回收

* 什么是 Spring inner beans? 

  * 在 Spring 框架中，无论何时 bean 被使用时，当仅被调用了一个属性。一个明智的做法是将这个 bean 声明为内部bean。内部bean可以用setter注入“属性”和构造方法注入“构造参数”的方式来实现 

  ```xml
  <!--内部 bean 的声明方式如下: -->
  <bean id="CustomerBean" class="com.gupaoedu.common.Customer">
     <property name="person">
  	<bean class="com.gupaoedu.common.Person"> 
          <property name="name" value="lokesh" /> 
          <property name="address" value="India" /> 
          <property name="age" value="34" />
       </bean>
     </property>
  </bean>
  ```

* Spring 框架中都用到了哪些设计模式?

  - 代理模式：AOP 思想的底层实现技术，Spring 中采用 JDK Proxy 和 CgLib 类库  
  - 工厂模式：BeanFactory 用来创建对象的实例，贯穿BeanFactory / ApplicationContext 接口的核心理念  
  - 委派模式：Srping 提供了 DispatcherServlet 来对请求进行分发 
  - 单例模式：在 Spring 配置文件中定义的 bean 默认为单例模式 
  - 模板模式：用来解决代码重复的问题 
    - 比如. RestTemplate, JmsTemplate, JpaTemplate 

* 在 Spring 中可以注入 null 或空字符串吗? 

  * 完全可以

* Spring 框架中的单例 Beans 是线程安全的么?

  * Spring 框架并没有对单例 bean 进行任何多线程的封装处理。
  * 最浅显的解决办法就是将多态 bean 的作用域由“singleton”变更为“prototype”。
  
* Spring 如何保证 Controller 并发的安全？

  * Spring5的新特性就开始考虑这个问题了
  * 如果对并发有要求，推荐用SpringBoot 

* 请举例说明如何在 Spring 中注入一个 Java 集合?

  * Spring 提供了四种集合类的配置元素:  list、set、map、props

* Spring5对WebFlux的支持

  * Sevlet2.x  单实例多线程的阻塞式IO （BIO）
  * Sevlet3.x  单实例多线程异步非阻塞式IO  （AIO异步非阻塞、NIO 同步非阻塞）
  * WebFlux 是在Servlet3.x 的基础之上应运而生

* WebFlux特点

  * 基于Servlet3.x 实现异步非阻塞的HTTP响应
  * API完美支持Rest风格
  * 支持函数式编程
  * Mono是表示返回单个对象、Flux表示返回集合

## 对象注入方式

### 属性注入

* 通过setter方法注入Bean的属性值或者依赖对象

* 前提：要求Bean提供一个默认的构造函数，并为需要注入的属性提供对应的setter方法

* Spring先调用Bean的默认构造函数实例化Bean对象，然后通过反射的方式调用setter方法注入属性值

  ```java
  public class DemoController {
      private Demo demo;
      public void setDemo(Demo demo) {
          this.demo = demo;
      }
  }
  
  <bean name="DemoController" class="...XX.DemoController">
  	<property name="demo" ref="demo" />
  </bean>
  <bean name="demo" clas="xxx.Demo" />
  ```

### 构造注入

* 保证一些必要的属性在Bean实例化时就得到设置，确保Bean在实例化后就可以使用

* 前提：Bean必须提供带参数的构造函数

  * 按类型匹配入参，type="",但具有多个相同类型的入参的构造函数，就失效
  * 按索引匹配入参，index="0"
  * 混合使用
  * 通过自身类型反射匹配入参，构造函数入参的类型是可辨别的（非基础数据类型且入参类型各异）

* 循环依赖问题

  * Spring容器能对构造函数配置的 Bean 进行实例化有一个前提，即Bean构造函数入参引用的对象必须已经准备就绪，如果两个 Bean 都采用构造函数注入，而且都采用构造函数入参引用对方，就会发生类似线程死锁的循环依赖问题

  * 解决 ：将构造函数注入方式调整为属性注入方式

    ```java
    public class DemoController {
        private Demo demo;
        public DemoController(Demo demo) {
            this.demo = demo;
        }
    }
    
    <bean name="DemoController" class="...XX.DemoController">
        <!--index="0" type="java.lang.Object" 识别参数-->
    	<constructor-arg index="0" ref="demo" />
    </bean>
    <bean name="demo" clas="xxx.Demo" />
    ```

### 静态工厂方法注入

* 需要额外的类和代码，这些功能和业务是没有关系的

  ```java
  public class DemoFactory {
      public static final Demo getInstance() {
          return new Demo();
      }
  }
  
  public class DemoController {
      private Demo demo;
      public void setDemo(Demo demo) {
          this.demo = demo;
      }
  }
  
  <bean name="DemoController" class="...XX.DemoController">
  	<property ref="demo" />
  </bean>
  <bean name="demo" clas="xxx.DemoFactory" factory-method="getInstance"/>
  ```

## 事件

* 如果一个bean实现了 ApplicationListener 接口，当相应的ApplicationEvent被发布以后，bean会自动被通知

  ```java
  public class AllApplicationEventListener implements ApplicationListener<ApplicationEvent> { 
      @Override
  	public void onApplicationEvent(ApplicationEvent applicationEvent) { 
          //process event
  	} 
  }
  ```

* Spring 提供了以下 5 种标准的事件  
  1. 上下文更新事件(ContextRefreshedEvent)：该事件会在 ApplicationContext 被初始化或者更新时发布。也可以在调用 ConfigurableApplicationContext 接口中的 refresh()方法时被触发 
  2. 上下文开始事件(ContextStartedEvent)：当容器调用 ConfigurableApplicationContext 的 Start()方法开始/重新开始容器时触发该事件 
  3. 上下文停止事件(ContextStoppedEvent)：当容器调用 ConfigurableApplicationContext 的 Stop()方法停止容器时触发该事件 
  4. 上下文关闭事件(ContextClosedEvent)：当 ApplicationContext 被关闭时触发该事件。容器被关闭时，其管理的所有单例 Bean 都被销毁  
  5. 请求处理事件(RequestHandledEvent)：在 Web 应用中，当一个 http 请求(request)结束触发该事件 

### 自定义事件

1. 事件类继承ApplicationContextEvent

2. 创建事件监听器监听事件类：监听器实现ApplicationListener<事件类>

3. 通过 ApplicationContext 接口的 publishEvent()方法来发布自定义事件

   ```java
   //1. 事件类
   public class MailSendEvent extends ApplicationContextEvent {
       private String msg;
       //source指定事件源，有两个子类，ApplicationContextEvent、RequestHandleEvent
       public MailSendEvent(ApplicationContext source, String msg) {
           super(source);
           this.msg = msg;
       }
   
       public String getMsg() {
           return msg;
       }
   }
   ```

   ```java
   //2. 事件监听器
   public class MailSendListener implements ApplicationListener<MailSendEvent> {
       @Override
       public void onApplicationEvent(MailSendEvent event) {
           //事件处理逻辑
           System.out.println("Hi" + event.getMsg());
       }
   }
   ```

   ```java
   public class MailSender implements ApplicationContextAware {
       private ApplicationContext applicationContext;
       //容器启动时注入容器实例
       @Override
       public void setApplicationContext(ApplicationContext applicationContext) throws BeansException {
           this.applicationContext = applicationContext;
       }
   
       public void sendMail() {
           System.out.println("Hi, 正在发送邮件");
           MailSendEvent ms = new MailSendEvent(this.applicationContext, "发给章三");
           //向容器中所有事件监听器发送事件
           applicationContext.publishEvent(ms);
           System.out.println(ms.getMsg());
       }
   }
   ```

   ```java
   public static void main(String[] args){
           ClassPathXmlApplicationContext ctx = new ClassPathXmlApplicationContext("classpath:/spring/application-beans.xml");
           MailSender mailSender = (MailSender)ctx.getBean("mailSender");
           mailSender.sendMail();
   }
   ```

## **自定义全局异常处理器**

`HandlerExceptionResolver`

1. 解析出异常类型

2. 如果该异常类型是系统自定义的异常，直接取出异常信息，在错误页面展示

3. 如果该异常类型不是系统自定义的异常，构造一个自定义的异常类型（信息为“未知错误”）

   ```java
   public class CustomExceptionResolver implements HandlerExceptionResolver {
   
       @Override
       public ModelAndView resolveException(HttpServletRequest request,
               HttpServletResponse response, Object handler, Exception ex) {
   
           ex.printStackTrace();
           CustomException customException = null;
   
           //如果抛出的是系统自定义的异常则直接转换
           if(ex instanceof CustomException) {
               customException = (CustomException) ex;
           } else {
               //如果抛出的不是系统自定义的异常则重新构造一个未知错误异常
               //这里我就也有CustomException省事了，实际中应该要再定义一个新的异常
               customException = new CustomException("系统未知错误");
           }
   
           //向前台返回错误信息
           ModelAndView modelAndView = new ModelAndView();
           modelAndView.addObject("message", customException.getMessage());
           modelAndView.setViewName("/WEB-INF/jsp/error.jsp");
   
           return modelAndView;
       }
   }
   ```

   ```xml
   <!--springmvc.xml中配置这个自定义的异常处理器-->
   <!-- 自定义的全局异常处理器 只要实现HandlerExceptionResolver接口就是全局异常处理器-->
   <bean class="ssm.exception.CustomExceptionResolver"></bean> 
   ```

## 资源访问

* Resource接口，提供底层资源访问能力，有对应不同资源类型的实现类
  * ClassPathResource：类路径下的资源（src/main/resources下的）
  * FileSystemResource：以文件系统绝对路径的方式进行访问，如D:/conf/bean.xml
  * ServletContextResource：以相对于Web应用根目录的方式进行访问
* classpath：只会在第一个加载xxx包路径下查找，而classpath*:会扫描所有这些JAR包及路径下出现的xxx包路径
* 资源加载器
  * ResourceLoader接口：仅有一个getResource(String location)方法，可以根据一个资源地址加载文件资源，不支持Ant风格
  * ResourcePatternResolver扩展了ResourceLoader接口，支持Ant风格
  * Spring提供的标准实现类：PathMatchingResourcePatternResolver

```java
//basePackage：com.tkrng.community.manage
ResourcePatternResolver resourcePatternResolver = new PathMatchingResourcePatternResolver();
String resolveBasePackage = ClassUtils.convertClassNameToResourcePath(
		this.applicationContext.getEnvironment().resolveRequiredPlaceholders(basePackage));
String packageSearchPath = "classpath*:" + resolveBasePackage + "/" + "**/*.class";
Resource[] resources = resourcePatternResolver.getResources(packageSearchPath);

遍历resources：resource
MetadataReaderFactory metadataReaderFactory = new CachingMetadataReaderFactory(
			resourcePatternResolver);
MetadataReader metadataReader = metadataReaderFactory.getMetadataReader(resource);
ScannedGenericBeanDefinition sbd = new ScannedGenericBeanDefinition(metadataReader);
sbd.setResource(resource);
sbd.setSource(resource);
```

## Bean的作用域

* 通过属性scope来设置bean的作用域范围
  - singleton：单例模式(默认的)，每个容器中只有一个共享的 bean 的实例
  - prototype：原型模式，每次通过Spring容器获取bean时，容器都将创建一个新的Bean实例
  - request：每个HTTP请求创建单独的Bean实例
  - session：Bean实例的作用域是Session范围。
  - global Session：用于Portlet容器，因为每个Portlet有单独的Session，GlobalSession提供一个全局性的HTTP Session

## Bean的生命周期

* Spring bean factory 负责管理在 Spring 容器中被创建的 bean 的生命周期 

* 可以分为创建和销毁两个过程
  * 创建Bean会经过一系列的步骤，主要包括:
    * 实例化Bean对象：Spring根据配置情况调用Bean构造函数或工厂方法对Bean进行实例化
    * 设置Bean属性，Spring将值和Bean的引用注入进Bean对应的属性中
    * Bean实现 Aware接口后，容器会把Spring上下文注入Bean
      * 例如实现BeanFactoryAware的Bean在初始后，Spring容器将会注入BeanFactory的实例
      * 而实现ApplicationContextAware的Bean，在Bean被初始后，将会被注入 ApplicationContext的实例等
      * 如果通过各种Aware接口声明了依赖关系，则会注入Bean对容器基础设施层面的依赖。具体包括BeanNameAware、BeanFactoryAware和ApplicationContextAware，分别会注入Bean ID、Bean Factory或者ApplicationContext
    * 如果BeanFactory装配了BeanPostProcessor后置处理器
      * 调用BeanPostProcessor的前置初始化方法postProcessBeforeInitialization
    * 当前正在初始化的bean对象会被传递进来，可以对这个bean作任何处理
    * 如果Bean实现了InitializingBean接口，则会调用afterPropertiesSet方法
      * 不会把当前bean对象传进来，只能增加一些额外的逻辑
    * 调用Bean自身定义的init方法
    * 调用BeanPostProcessor的后置初始化方法postProcessAfterInitialization
      * 当前正在初始化的bean对象会被传递进来，可以对这个bean作任何处理
    * 创建过程完毕
  * Spring Bean的销毁过程会依次调用DisposableBean的destroy方法和Bean自身定制的destroy方法
  
* Bean的完整生命周期经历了各种方法调用，这些方法可以划分为以下几类：

  * Bean自身方法：如调用Bean构造函数实例化Bean、调用setter设置Bean的属性值及通过`<bean>`的init-method和destroy-method所指定的方法
  * Bean级生命周期接口方法：如BeanNameAware、BeanFactoryAware、InitializingBean和DisposableBean，这些接口方法由Bean类直接实现
  * 容器级生命周期接口方法：InstantiationAwareBeanPostProcessor和BeanPostProcessor两个接口——后置处理器，不由Bean本身实现，实现类以容器附加装置的形式注册到Spring容器中，并通过接口反射为Spring容器扫描识别。
    * 当Spring容器创建任何Bean的时候，这些后置处理器都会发挥作用——影响时全局性的，不过可以合理编写后置处理器对要改造的Bean进行加工处理
  * 工厂后置处理器接口方法：AspectJWeavingEnabler、CustomeAutowireConfiguer、ConfigurationClassPostProcessor等，也是容器级的，在应用上下文装配配置文件后立即调用


## Spring MVC

* 基于项目开发的设计模式，用来解决用户和后台交互问题
  - Model：将传输数据封装成一个完整的载体
  - View：视图，用来展示或者输出的模块（HTML、JSP、JSON、String、Swing、xml...）
  - Controller：控制交互一个中间组件，由它来根据用户请求分发不同任务从而得到不同的结果
* Spring MVC
  - 只是MVC设计模式的应用典范，给MVC的实现定制了一套标准
  - M：支持将url参数自动封装成一个Object或者Map
  - V：自己只有一个默认的template、支持扩展、自定义View，而且能够自定义介些
  - C：把限制放宽了，任何一个类，都有可能是一个Controller

* Spring MVC原理
  * Servlet是j2ee的标准，Spring MVC是对于Servlet的再包装，使得更易容，更专注于业务开发

    * 因为单纯的使用Servlet，需要考虑线程安全，请求分发，权限控制等等方面的问题
    * 在Spring MVC的配置中，如果将Servlet的配置与MVC的配置写在一起，有没有contexloaderListener无所谓的

  * 配置阶段

    * `web.xml`
      * 配置DispatcherServlet，作为SpringMVC的启动入口
      * 配置init-param，固定属性名字contextConfigLocation，指定application.xml路径，通常会配置成classpath:application.xml
      * 配置servlet-pattern，配置一个请求路径的规则，通常会配置成/*

  * 初始化阶段

    * `	ApplicationContext`实例化，通过onRefresh()调用`DispatchServlet`的initStrategies()——有九种策略，针对每个用户请求都会经过一些处理的策略之后，最终才能有结果输出，每种策略可以自定义干预，但是最终返回ModelAndView

  * 请求处理阶

    ![image-20190519215044972](/Users/dingyuanjie/Documents/study/github/woodyprogram/img/image-20190519215044972.png)

  1. DispatcherServlet接收请求后查询一个或多个处理映射器来确定将请求发送给相应的控制器。（处理映射器会根据请求所携带的URL信息来进行决策）
  2. 控制器接受请求并根据请求的类型（Get/Post）调用相应的服务方法完成业务逻辑后，将产生的模型数据和用于渲染输出的视图逻辑名返回给DispatcherServlet
  3. DispatcherServlet通过视图解析器来将逻辑视图名匹配为一个特定的视图实现，视图将使用模型数据渲染输出
  4. 输出会通过响应对象传递给客户端

## BeanFactory

* 一般称BeanFactory为IOC容器，是Spring框架的基础设施，面向Spring本身，是类的通用工厂，可以创建并管理各种类的对象

  * 是工厂模式的一个实现，提供了控制反转功能，用来把应用的配置和依赖从正真的应用代码中分离

* 最常用的BeanFactory 实现是XmlBeanFactory 类

  ```java
  public static void main(String[] args){
  	PathMatchingResourcePatternResolver patternResolver = new PathMatchingResourcePatternResolver();
      Resource resource = patternResolver.getResource("classpath:/spring/application-beans.xml");
      //BeanFactory启动IOC容器时，并不会初始化配置文件中定义的Bean，初始化发生在第一次调用时
      DefaultListableBeanFactory factory = new DefaultListableBeanFactory();
      XmlBeanDefinitionReader xmlBeanDefinitionReader = new XmlBeanDefinitionReader(factory);
      xmlBeanDefinitionReader.loadBeanDefinitions(resource);
      Persom person = factory.getBean("persom", Persom.class);
      person.saySomething();
  }
  ```

## FactoryBean

* 通过实现该工厂类接口定制实例化Bean的逻辑

  * 隐藏了实例化一些复杂Bean的细节，给上层应用带来了便利

* 当配置文件中Bean的class属性配置的实现类是FactoryBean时，通过getBean()方法返回的不是FactoryBean本身，而是FactoryBean#getObject()方法所返回的对象，相当于FactoryBean#getObject()代理了getBean()

  ```java
  public class PersonFactoryBean implements FactoryBean<Person> {
      private Person person;
      public void setPerson(Person person) {  //构造方法注入
          this.person = person;
      }
  
      @Override
      public Person getObject() throws Exception {
          person.setAge(18);
          person.setName("里斯本");
          person.setSex(0);
          return person;
      }
  
      @Override
      public Class<?> getObjectType() {
          return Person.class;
      }
  
      @Override
      public boolean isSingleton() {
          return false;
      }
  }
  
  //当调用getBean("personFactoryBean")时，Spring通过反射机制发现PersonFactoryBean实现了FactoryBean接口，这时Spring容器就调用接口方法PersonFactoryBean#getObject()返回工厂类创建的对象
  Persom personFactoryBean =(Persom)factory.getBean("personFactoryBean");
          personFactoryBean.saySomething();
  //显示地在beanName前加上&前缀，返回FactoryBean实例
  PersonFactoryBeanTest personFactoryBeanTest =(PersonFactoryBeanTest)factory.getBean("&personFactoryBean");
          personFactoryBeanTest.getObject();
  ```

  ```xml
  <bean id="personFactoryBean" class="com.woody.framework.spring.factorybean.PersonFactoryBeanTest">
          <property name="person" ref="person" />
  </bean>
  ```

## ApplicationContext

* 应用上下文，由BeanFactory派生而来

* 面向使用Spring框架开发者，几乎所有的应用场合都可以直接使用ApplicationContext，而非底层的BeanFactory

  * ClassPathXmlApplicationContext：从类路径加载配置文件
  * FileSystemXmlApplicationContext：从文件系统中装载配置文件
  * AnnotationConfigApplicationContext：基于注解类配置

* ApplicationContext在初始化应用上下文时就实例化所有单实例的Bean

* WebApplicationContext

  - 允许从相对于Web根目录的路径中装载配置文件完成初始化工作

  - 从WebApplicationContext中可以获取ServletConext的引用，整个Web应用上下文对象将作为属性放置到ServletContext中，以便Web应用环境可以访问 Spring应用上下文

  - WebApplicationContextUtils.getWebApplicationContext(ServletConfig sc)，可以从ServletConfig中获取WebApplicationContext实例

  - 初始化：

    - WebApplicationContext需要ServletConfig实例，它必须在拥有Web容器的前提下才能完成启动工作

    - 可以在web.xml中

      - 配置自启动的Servlet（ContextLoadServlet）
      - 或定义Web容器监听器（ServletContextListener）
      - 借助二者中的任何一个，就可以完成启动SpringWeb应用上下文的工作
      
      ```xml
      <context-param>
          <param-name>contextConfigLocation</param-name>
          <param-value>
              classpath*:/spring-context.xml
              classpath*:/spring-remote-servlet.xml
          </param-value>
      </context-param>
      <context-param>
          <param-name>log4jConfigLocation</param-name>
          <param-value>classpath:/log4j.xml</param-value>
      </context-param>
      
      <listener>
      	<listener-class>
          org.springframework.web.util.Log4jConfigListener
        </listener-class>
      </listener>
      <listener>
      	<listener-class>
          org.springframework.web.context.ContextLoaderListener
        </listener-class>
      </listener>
      ```

* BeanFactory 和 ApplicationContext 有什么区别?  

  * BeanFactory 可以理解为含有 bean 集合的工厂类

    * BeanFactory 包含了种 bean 的定义，以便在接收到客户端请求时将对应的 bean 实例化
  * BeanFactory 还能在实例化对象的时生成协作类之间的关系。此举将 bean 自身与 bean 客户端的配置中解放出来
    
* BeanFactory 还包含了 bean 生命周期的控制，调用客户端的初始化方法(initialization Methods)和销毁方法(destruction Methods)
    
* 从表面上看，ApplicationContext 如同 BeanFactory 一样具有 bean 定义、bean 关联关系的设置， 根据请求分发 bean 的功能。但 ApplicationContext 在此基础上还提供了其他的功能
  
    * 提供了支持国际化的文本消息 
    * 统一的资源文件读取方式  
  * 已在监听器中注册的 bean 的事件
  
* 三种较常见的 ApplicationContext 实现方式：
  
  - ClassPathXmlApplicationContext：从 classpath 的 XML 配置文件中读取上下文，并生成上下文定义。应用程序上下文从程序环境变量中取得
  
      ```java
      ApplicationContext context = newClassPathXmlApplicationContext(“application.xml”);
    ```
  
  - FileSystemXmlApplicationContext：由文件系统中的 XML 配置文件读取上下文
  
    - XmlWebApplicationContext：由 Web 应用的 XML 文件读取上下文

## Spring IOC

* Spring IOC 负责创建对象，管理对象（通过依赖注入DI），装配对象，配置对象，并且管理这些对象的整个生命周期
* 控制反转或依赖注入
  * 让调用类对某一接口实现类的依赖关系由第三方（容器）注入，以移除调用类对某一接口实现类的依赖

* 初始化过程
  1. BeanDifinition的Resource定位
     * 指的是BeanDifinition的资源定位，它由ResourceLoader通过统一的Resource接口来完成，这个Resource对各种形式的BeanDifinition的使用都提供了统一的接口
  2. BeanDifinition的载入与解析
     * 把用户定义好的Bean表示成Ioc容器内部的数据结构，而这个容器内部的数据结构就是BeanDifinition
     * 具体来说，BeanDifinition实际上就是POJO对象在IOC容器中的抽象，通过这个BeanDifinition定义的数据结构，使IOC容器能够方便的对POJO对象也就是Bean进行管理
  3. BeanDifinition在Ioc容器中的注册
     * 通过调用BeanDifinitionRegistry接口来实现
     * 这个注册过程把载入过程中解析得到的BeanDifinition向Ioc容器进行注册

* IOC容器的初始化过程，一般不包含Bean依赖注入的实现
* 在Ioc的设计中，Bean定义的载入和依赖注入是俩个独立的过程
  * 依赖注入一般发生在应用第一次通过getBean向容器索取Bean的时候。（使用预实例化的配置除外）

## Spring AOP

* 面向切面的编程，是一种编程技术，是OOP（面向对象编程）的补充和完善
  * OOP的执行是一种从上往下的流程，并没有从左到右的关系。因此在OOP编程中，会有大量的重复代码
  * 而AOP则是将这些与业务无关的重复代码抽取出来，然后再嵌入到业务代码当中
    * 常见的应用有：权限管理、日志、事务管理等
* 应用场景
  * 性能检测、访问控制、事务管理及日志记录
* Spring AOP 使用动态代理技术在运行期为目标Bean织入增强的代码
* AOP：解耦（专人干专事）Aspect切面，如何切——规则。所谓面向切面编程，就是面向规则编程
  * 切面-两面，事务管理的切面由Spring提供实现，业务相关的切面由自己实现
  * 切点-能够满足虚拟切面规则的所有入口
  * 通知-满足条件回调

* 实现方式
  * Spring AOP实现用的是动态代理的方式（jdk、cglib）
  * Spring AOP通过Pointcut（切点）指定在那些类的那些方法上织入横切逻辑
  * 通过Advice（增强）描述横切逻辑和方法的具体织入点（方法前、方法后、方法的两端等）
  * Spring通过Advisor（切面）将Pointcut和Advice组装起来。
  * 有了Advisor的信息，Spring就可以利用JDK或CGLib动态代理技术采用统一的方式为目标Bean创建织入切面的代理对象
* Annation方式实现
* XML方式实现

## 事务

* 事务：访问并可能更新数据库中各种数据项的一个程序执行单元

* 特点：是恢复和并发控制的基本单位

* 都是对Connection进行操作，这个类就是Java客户端和数据库服务通信的桥梁，也就是一个包装类，就是一个TCP连接，底层就是Socket

* service层把异常抛出去，事务才会回滚；如果try-catch不抛出去，那事务不会回滚

* DataSource是Connection 的扩展，动态切换，事务的统一管理

* 事务特性：ACID

* 事务安全问题：脏读、不可重复读、幻读

* 事务隔离：

  * 未提交读、提交读、可重复读、串行化

* 事务传播特性

  - **PROPAGATION_REQUIRED**
    - 0，默认，当前有事务就用当前的，没有就⽤新的
  - **PROPAGATION_SUPPORTS**
    - 1，事务可有可无，不是必须的
  - **PROPAGATION_MANDATORY**
    - 2，当前⼀定要有事务，不然就抛异常
  - **PROPAGATION_REQUIRES_NEW**
    - 3，无论是否有事务，都起个新的事务。两个事务没有关联
  - **PROPAGATION_NOT_SUPPORTED**
    - 4，不支持事务，按非事务方式运⾏
  - **PROPAGATION_NEVER**
    - 5，不支持事务，如果有事务则抛异常
  - **PROPAGATION_NESTED**
    - 6，当前有事务就在当前事务⾥再起一个事务。两个事务有关联。外部事务回滚，内嵌事务也会回滚

* 注解配置声明式事务
  * 通过@Transactional对需要事务增强的Bean接口、实现类或方法进行标注

  * 在容器中配置基于注解的事务增强驱动，即可启用基于注解的声明式事务

  * 注解不会被继承，所以一般在具体业务上标注

  * 属性：
    * propagation，事务传播行为
    * isolation，事务隔离级别
    * timeout，超时时间，int，秒
    * rollbackFor，一组异常类，遇到时进行回滚

  ```xml
  <bean id="transactionManager" class="org.springframework.jdbc.datasource.DataSourceTransactionManager">  
      <property name="dataSource" ref="dataSource"></property>  
  </bean>  
  <!--对标注@Transactional注解的Bean进行加工处理，以织入事务管理切面-->        
  <tx:annotation-driven  transaction-manager="transactionManager" proxy-target-class="true"/>
  //为true，Spring通过创建子类来代理业务类，需要在类路径中添加CGLib.jar类库；为false，则使用基于接口的代理
  ```

  ```shell
  update member set age = age - 1 where name = 'tom'
  1. 执行SELECT，将满足条件的记录找出来
  2. 把找出来的记录放到内存中   --锁
  3. 进行数据检查，数据是否有效、合法
  4. 数据操作没有任何异常的话，更新原始表，写日志（根据日志回滚）
  5. 更新状态返回状态码
  6. 内存中该数据消失s
  ```

* 不同数据源的事务如何处理
  * 原理：在Spring中，事务是不支持跨数据源，即一个事务不能操作两个数据库
  * DataSouce是Connection，当创建语句集的时候开启事务
  * 解决：使用中间件，做一些消息同步，利用分布式锁去实现分布式事务

## ORM

* ORM-对象关系映射，将已持久化的数据内容转换为一个Java对象，用Java对象来描述对象与对象之间的关系和数据内容
* Hibernate：全自动挡，不需要写一句SQL语句，烧油（牺牲性能）
* MyBatis：手自一体（半自动），支持单表映射，多表关联需要配置，轻量级一些
* SpringJDBC：手动挡，包括SQL语句，映射都是要自己实现的（最省油的）

```java
//规范
统一API
- 统一方法名
  select、insert、delete、update
  如果是删、改，以ID作为唯一的检索条件，如果没有ID，那么要先查出来的到ID
- 统一参数
  条件查询：QueryRule（自己封装）
  批量更新和插入，方法名以All结尾，参数为List
  删、改、插入一条数据，参数用T
- 统一返回结果
  所有的分页操作返回Page
  所有的集合查询返回List
  所有的单条查询返回T
  所有的ID采用Long类型
  所有的删除、修改、增加操作返回boolean
  对外输出都用ResultMsg

只要是Spring相关的配置都以application-开头
建议不要把所有的东西都写在一个文件中
aop	配置切面，代理规则
beans 配置单例对象
common 配置通用的配置
context 主入口
db 数据库相关
web 跟页面打交道的、拦截器、过滤器、监听器、模版
每个表必须有主键
```



