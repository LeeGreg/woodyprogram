```java
// http://localhost:8080/actuator/beans
可以查看Spring已经自动注册的Bean，业务中可以直接注入
```

```java
类上注解@Slf4j，然后直接使用log.info()...
```

## Spring常用注解

* Java Config相关注解
  * @Configuration、@ImportResource、@ComponentScan、@Bean、@ConfigurationProperties
* 定义相关注解
  * @Component / @Repository / @Service
  * @Controller / @RestController
  * @RequestMapping
* 注⼊相关注解
  * @Autowired / @Qualifier / @Resource（根据名字来注入）
  * @Value
* Actuator的Endpoint
  * /actuator/health，健康检查
  * /actuator/beans，查看容器中的所有Bean
  * /actuator/mappings，查看Web的URL映射
  * /actuator/env，查看环境信息
* 解禁 Endpoint
  * 默认，/actuator/health 和 /actuator/info 可 Web 访问
  * 解禁所有Endpoint
    * `management.endpoints.web.exposure.include=*`

## 多数据源

* 不同数据源的配置要分开、关注每次使用的数据源

* ⼿工配置两组 **DataSource** 及相关内容、与**Spring Boot**协同⼯工作

  * 排除Spring Boot的⾃自动配置
    * DataSourceAutoConfiguration
    * DataSourceTransactionManagerAutoConfiguration
    * JdbcTemplateAutoConfiguration

  ```properties
  management.endpoints.web.exposure.include=*
  spring.output.ansi.enabled=ALWAYS
  
  foo.datasource.url=jdbc:h2:mem:foo
  foo.datasource.username=sa
  foo.datasource.password=
  
  bar.datasource.url=jdbc:h2:mem:bar
  bar.datasource.username=sa
  bar.datasource.password=
  ```

  ```java
  @SpringBootApplication(exclude = { DataSourceAutoConfiguration.class,
          DataSourceTransactionManagerAutoConfiguration.class,
          JdbcTemplateAutoConfiguration.class})
  @Slf4j
  public class MultiDataSourceDemoApplication {
    
      public static void main(String[] args) {
          SpringApplication.run(MultiDataSourceDemoApplication.class, args);
      }
  
      @Bean
      @ConfigurationProperties("foo.datasource")
      public DataSourceProperties fooDataSourceProperties() {
          return new DataSourceProperties();
      }
  
      @Bean
      public DataSource fooDataSource() {
          DataSourceProperties dataSourceProperties = fooDataSourceProperties();
          log.info("foo datasource: {}", dataSourceProperties.getUrl());
          return dataSourceProperties.initializeDataSourceBuilder().build();
      }
  
      @Bean
      @Resource
      public PlatformTransactionManager fooTxManager(DataSource fooDataSource) {
          return new DataSourceTransactionManager(fooDataSource);
      }
  
      @Bean
      @ConfigurationProperties("bar.datasource")
      public DataSourceProperties barDataSourceProperties() {
          return new DataSourceProperties();
      }
  
      @Bean
      public DataSource barDataSource() {
          DataSourceProperties dataSourceProperties = barDataSourceProperties();
          log.info("bar datasource: {}", dataSourceProperties.getUrl());
          return dataSourceProperties.initializeDataSourceBuilder().build();
      }
  
      @Bean
      @Resource
      public PlatformTransactionManager barTxManager(DataSource barDataSource) {
          return new DataSourceTransactionManager(barDataSource);
      }
  }
  ```

  ```xml
  <dependency>
    <groupId>org.springframework.boot</groupId>
    <artifactId>spring-boot-starter-jdbc</artifactId>
  </dependency>
  <dependency>
    <groupId>com.h2database</groupId>
    <artifactId>h2</artifactId>
    <scope>runtime</scope>
  </dependency>
  <dependency>
    <groupId>org.projectlombok</groupId>
    <artifactId>lombok</artifactId>
    <optional>true</optional>
  </dependency>
  ```

## 分库分表

* 系统需要访问⼏个完全不同的数据库
* 系统需要访问同一个库的主库与备库
* 系统需要访问一组做了分库分表的数据库

## 好用的连接池

* HikariCP

  * **Spring Boot 2.x**
    * 默认使⽤用 HikariCP
    * 配置 spring.datasource.hikari.* 配置
  * **Spring Boot 1.x**
    * 默认使用 Tomcat 连接池，需要移除 tomcat-jdbc 依赖
    * spring.datasource.type=com.zaxxer.hikari.HikariDataSource
  * 常用参数
    * spring.datasource.hikari.maximumPoolSize=10
    * spring.datasource.hikari.minimumIdle=10
    * spring.datasource.hikari.idleTimeout=600000
    * spring.datasource.hikari.connectionTimeout=30000
    * spring.datasource.hikari.maxLifetime=1800000

* Druid

  * 详细的监控、SQL 防注⼊、内置加密配置、众多扩展点，方便进行定制

  * ExceptionSorter，针对主流数据库的返回码都有支持

  * 慢 SQL 日志

    * 系统属性配置

      ```properties
      druid.stat.logSlowSql=true
      druid.stat.slowSqlMillis=3000
      ```

    * SpringBoot

      ```properties
      spring.datasource.druid.filter.stat.enabled=true
      spring.datasource.druid.filter.stat.log-slow-sql=true
      spring.datasource.druid.filter.stat.slow-sql-millis=3000
      ```

    * 注意事项

      * 没特殊情况，不要在生产环境打开监控的 Servlet
      * 没有连接泄露可能的情况下，不要开启 removeAbandoned
      * testXxx 的使⽤需要注意
      * 务必配置合理的超时时间

  * 配置

    * **druid-spring-boot-starter**

      ```xml
      <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-jdbc</artifactId>
        <exclusions>
          <exclusion>
            <artifactId>HikariCP</artifactId>
            <groupId>com.zaxxer</groupId>
          </exclusion>
        </exclusions>
      </dependency>
      
      <dependency>
        <groupId>com.alibaba</groupId>
        <artifactId>druid-spring-boot-starter</artifactId>
        <version>1.1.10</version>
      </dependency>
      ```

      * spring.datasource.druid.*

        ```properties
        spring.output.ansi.enabled=ALWAYS
        
        spring.datasource.url=jdbc:h2:mem:foo
        spring.datasource.username=sa
        spring.datasource.password=n/z7PyA5cvcXvs8px8FVmBVpaRyNsvJb3X7YfS38DJrIg25EbZaZGvH4aHcnc97Om0islpCAPc3MqsGvsrxVJw==
        
        spring.datasource.druid.initial-size=5
        spring.datasource.druid.max-active=5
        spring.datasource.druid.min-idle=5
        spring.datasource.druid.filters=conn,config,stat,slf4j
        
        # 密码加密
        spring.datasource.druid.connection-properties=config.decrypt=true;config.decrypt.key=${public-key}
        spring.datasource.druid.filter.config.enabled=true
        
        spring.datasource.druid.test-on-borrow=true
        spring.datasource.druid.test-on-return=true
        spring.datasource.druid.test-while-idle=true
        
        public-key=MFwwDQYJKoZIhvcNAQEBBQADSwAwSAJBALS8ng1XvgHrdOgm4pxrnUdt3sXtu/E8My9KzX8sXlz+mXRZQCop7NVQLne25pXHtZoDYuMh3bzoGj6v5HvvAQ8CAwEAAQ==
        
        # SQL防注入
        spring.datasource.druid.filter.wall.enabled=true 
        spring.datasource.druid.filter.wall.db-type=h2 
        spring.datasource.druid.filter.wall.config.delete-allow=false 
        spring.datasource.druid.filter.wall.config.drop-table-allow=false
        ```

    * Druid Filter

      * ⽤用于定制连接池操作的各种环节

      * 可以继承 FilterEventAdapter 以⽅便地实现 Filter

        ```java
        package geektime.spring.data.druiddemo;
        @Slf4j
        public class ConnectionLogFilter extends FilterEventAdapter {
        
            @Override
            public void connection_connectBefore(FilterChain chain, Properties info) {
                log.info("BEFORE CONNECTION!");
            }
        
            @Override
            public void connection_connectAfter(ConnectionProxy connection) {
                log.info("AFTER CONNECTION!");
            }
        }
        ```

      * 修改 META-INF/druid-filter.properties 增加 Filter 配置

        * `druid.filters.conn=geektime.spring.data.druiddemo.ConnectionLogFilter`

      ```java
      @SpringBootApplication
      @Slf4j
      public class DruidDemoApplication implements CommandLineRunner {
      	@Autowired
      	private DataSource dataSource;
      	@Autowired
      	private JdbcTemplate jdbcTemplate;
      
      	public static void main(String[] args) {
      		SpringApplication.run(DruidDemoApplication.class, args);
      	}
      
      	@Override
      	public void run(String... args) throws Exception {
      		log.info(dataSource.toString());
      	}
      }
      ```

## Spring-JDBC

* core，JdbcTemplate 等相关核心接⼝和类
* datasource，数据源相关的辅助类
* object，将基本的 JDBC 操作封装成对象
* support，错误码等其他辅助⼯工具

* @Repository

* **JdbcTemplate**

  * query、queryForObject、queryForList、update、execute
  * batchUpdate
    * BatchPreparedStatementSetter
  * NamedParameterJdbcTemplate
    * batchUpdate
      * SqlParameterSourceUtils.createBatch

  ```properties
  # application.properties
  spring.output.ansi.enabled=ALWAYS
  ```

  ```java
  @SpringBootApplication
  @Slf4j
  public class SimpleJdbcDemoApplication implements CommandLineRunner {
      @Autowired
      private FooDao fooDao;
      @Autowired
      private BatchFooDao batchFooDao;
  
      public static void main(String[] args) {
          SpringApplication.run(SimpleJdbcDemoApplication.class, args);
      }
  
      @Bean
      @Autowired
      public SimpleJdbcInsert simpleJdbcInsert(JdbcTemplate jdbcTemplate) {
          return new SimpleJdbcInsert(jdbcTemplate)
                  .withTableName("FOO").usingGeneratedKeyColumns("ID");
      }
  
      @Bean
      @Autowired
      public NamedParameterJdbcTemplate namedParameterJdbcTemplate(DataSource dataSource) {
          return new NamedParameterJdbcTemplate(dataSource);
      }
  
      @Override
      public void run(String... args) throws Exception {
          fooDao.insertData();
          batchFooDao.batchInsert();
          fooDao.listData();
      }
  
  }
  ```

  ```java
  @Data
  @Builder
  public class Foo {
      private Long id;
      private String bar;
  }
  
  
  @Slf4j
  @Repository
  public class FooDao {
      @Autowired
      private JdbcTemplate jdbcTemplate;
      @Autowired
      private SimpleJdbcInsert simpleJdbcInsert;
  
      public void insertData() {
          Arrays.asList("b", "c").forEach(bar -> {
              jdbcTemplate.update("INSERT INTO FOO (BAR) VALUES (?)", bar);
          });
  
          HashMap<String, String> row = new HashMap<>();
          row.put("BAR", "d");
          Number id = simpleJdbcInsert.executeAndReturnKey(row);
          log.info("ID of d: {}", id.longValue());
      }
  
      public void listData() {
          log.info("Count: {}",
                  jdbcTemplate.queryForObject("SELECT COUNT(*) FROM FOO", Long.class));
  
          List<String> list = jdbcTemplate.queryForList("SELECT BAR FROM FOO", String.class);
          list.forEach(s -> log.info("Bar: {}", s));
  
          List<Foo> fooList = jdbcTemplate.query("SELECT * FROM FOO", new RowMapper<Foo>() {
              @Override
              public Foo mapRow(ResultSet rs, int rowNum) throws SQLException {
                  return Foo.builder()
                          .id(rs.getLong(1))
                          .bar(rs.getString(2))
                          .build();
              }
          });
          fooList.forEach(f -> log.info("Foo: {}", f));
      }
  }
  ```

  ```java
  @Repository
  public class BatchFooDao {
      @Autowired
      private JdbcTemplate jdbcTemplate;
      @Autowired
      private NamedParameterJdbcTemplate namedParameterJdbcTemplate;
  
      public void batchInsert() {
          jdbcTemplate.batchUpdate("INSERT INTO FOO (BAR) VALUES (?)",
                  new BatchPreparedStatementSetter() {
                      @Override
                      public void setValues(PreparedStatement ps, int i) throws SQLException {
                          ps.setString(1, "b-" + i);
                      }
  
                      @Override
                      public int getBatchSize() {
                          return 2;
                      }
                  });
  
          List<Foo> list = new ArrayList<>();
          list.add(Foo.builder().id(100L).bar("b-100").build());
          list.add(Foo.builder().id(101L).bar("b-101").build());
          namedParameterJdbcTemplate
                  .batchUpdate("INSERT INTO FOO (ID, BAR) VALUES (:id, :bar)",
                          SqlParameterSourceUtils.createBatch(list));
      }
  }
  ```

## Spring事务

* 一致的事务模型	

  * JDBC/Hibernate/myBatis
  * DataSource/JTA

* 事务抽象的核⼼接⼝

  * **PlatformTransactionManager**
    * DataSourceTransactionManager
    * HibernateTransactionManager
    * JtaTransactionManager
  * **TransactionDefinition**
    * Propagation
    * Isolation
    * Timeout
    * Read-only status

* 事务传播特性

  * **PROPAGATION_REQUIRED**
    * 0，默认，当前有事务就用当前的，没有就⽤新的
  * **PROPAGATION_SUPPORTS**
    * 1，事务可有可无，不是必须的
  * **PROPAGATION_MANDATORY**
    * 2，当前⼀定要有事务，不然就抛异常
  * **PROPAGATION_REQUIRES_NEW**
    * 3，无论是否有事务，都起个新的事务
      * 两个事务没有关联
  * **PROPAGATION_NOT_SUPPORTED**
    * 4，不支持事务，按非事务方式运⾏

  * **PROPAGATION_NEVER**
    * 5，不支持事务，如果有事务则抛异常
  * **PROPAGATION_NESTED**
    * 6，当前有事务就在当前事务⾥再起一个事务
      * 两个事务有关联
      * 外部事务回滚，内嵌事务也会回滚

* 事务隔离特性

  * **ISOLATION_READ_UNCOMMITTED**
    * 1，脏读、不可重复读、幻读
  * **ISOLATION_READ_COMMITTED**
    * 2，不可重复读、幻读
  * **ISOLATION_REPEATABLE_READ**
    * 3，幻读
  * **ISOLATION_SERIALIZABLE**
    * 4

* 编程式事务
  * **TransactionTemplate**
    * TransactionCallback、TransactionCallbackWithoutResult
  * **PlatformTransactionManager**
    * 可以传⼊TransactionDefinition进⾏定义
* 声明式事务
  * 本质上是通过 **AOP** 来增强了类的功能
    *  **AOP** 本质上就是为类做了一个代理
      * 看似在调⽤自⼰写的类，实际用的是增强后的代理类
  * 问题的解法
    * 访问增强后的代理类的⽅法，⽽非直接访问⾃身的方法
* 基于注解的配置方式
  * 开启事务注解的⽅式
    * @EnableTransactionManagement
    * `<tx:annotation-driven/>`
  * 一些配置
    * proxyTargetClass、mode、order
  * **@Transactional**
    * transactionManager、propagation、isolation、timeout、readOnly
    * 怎么判断回滚

```java
//编程式事务
@SpringBootApplication
@Slf4j
public class ProgrammaticTransactionDemoApplication implements CommandLineRunner {
	@Autowired
	private TransactionTemplate transactionTemplate;
	@Autowired
	private JdbcTemplate jdbcTemplate;

	public static void main(String[] args) {
		SpringApplication.run(ProgrammaticTransactionDemoApplication.class, args);
	}

	@Override
	public void run(String... args) throws Exception {
		log.info("COUNT BEFORE TRANSACTION: {}", getCount());
		transactionTemplate.execute(new TransactionCallbackWithoutResult() {
			@Override
			protected void doInTransactionWithoutResult(TransactionStatus transactionStatus) {
				jdbcTemplate.execute("INSERT INTO FOO (ID, BAR) VALUES (1, 'aaa')");
				log.info("COUNT IN TRANSACTION: {}", getCount());
				transactionStatus.setRollbackOnly();
			}
		});
		log.info("COUNT AFTER TRANSACTION: {}", getCount());
	}

	private long getCount() {
		return (long) jdbcTemplate.queryForList("SELECT COUNT(*) AS CNT FROM FOO")
				.get(0).get("CNT");
	}
}
```

```java
//声明式事务
@SpringBootApplication
@EnableTransactionManagement(mode = AdviceMode.PROXY)
@Slf4j
public class DeclarativeTransactionDemoApplication implements CommandLineRunner {
	@Autowired
	private FooService fooService;
	@Autowired
	private JdbcTemplate jdbcTemplate;

	public static void main(String[] args) {
		SpringApplication.run(DeclarativeTransactionDemoApplication.class, args);
	}

	@Override
	public void run(String... args) throws Exception {
		fooService.insertRecord();
		log.info("AAA {}",
				jdbcTemplate
						.queryForObject("SELECT COUNT(*) FROM FOO WHERE BAR='AAA'", Long.class));
		try {
			fooService.insertThenRollback();
		} catch (Exception e) {
			log.info("BBB {}",
					jdbcTemplate
							.queryForObject("SELECT COUNT(*) FROM FOO WHERE BAR='BBB'", Long.class));
		}

		try {
			fooService.invokeInsertThenRollback();
		} catch (Exception e) {
			log.info("BBB {}",
					jdbcTemplate
							.queryForObject("SELECT COUNT(*) FROM FOO WHERE BAR='BBB'", Long.class));
		}
	}
}

public interface FooService {
    void insertRecord();
    void insertThenRollback() throws RollbackException;
    void invokeInsertThenRollback() throws RollbackException;
}

@Component
public class FooServiceImpl implements FooService {
    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Override
    @Transactional
    public void insertRecord() {
        jdbcTemplate.execute("INSERT INTO FOO (BAR) VALUES ('AAA')");
    }

    @Override
    @Transactional(rollbackFor = RollbackException.class)
    public void insertThenRollback() throws RollbackException {
        jdbcTemplate.execute("INSERT INTO FOO (BAR) VALUES ('BBB')");
        throw new RollbackException();
    }

    @Override
    public void invokeInsertThenRollback() throws RollbackException {
        insertThenRollback();
    }
}

public class RollbackException extends Exception {
}
```

## Spring的JDBC异常处理

* **Spring** 会将数据操作的异常转换为 **DataAccessException**
* ⽆论使⽤何种数据访问⽅式，都能使⽤⼀样的异常
* Spring是怎么认识那些错误码的
  * 通过 **SQLErrorCodeSQLExceptionTranslator** 解析错误码
    * **ErrorCode** 定义
    * org/springframework/jdbc/support/sql-error-codes.xml
    * Classpath 下的 sql-error-codes.xml
* 定制错误码解析逻辑

## Spring Data JPA

* **JPA** 为对象关系映射提供了了⼀一种基于 **POJO** 的持久化模型
  * 简化数据持久化代码的开发⼯作
  * 为 Java 社区屏蔽不同持久化 API 的差异
* Spring Data
  * 在保留底层存储特性的同时，提供相对一致的、基于 **Spring** 的编程模型
* 定义 JPA 实体对象
  * 实体
    * @Entity、@MappedSuperclass、@Table(name)
  * 主键
    * @Id
      * @GeneratedValue(strategy, generator)
      * @SequenceGenerator(name, sequenceName)
  * 映射
    * @Column(name, nullable, length, insertable, updatable)
    * @JoinTable(name)、@JoinColumn(name)
  * 关系
    * @OneToOne、@OneToMany、@ManyToOne、@ManyToMany
    * @OrderBy
* Project Lombok
  - 能够自动嵌入 **IDE** 和构建工具，提升开发效率
  - 常用功能
    - @Getter / @Setter、@ToString、@NoArgsConstructor / @RequiredArgsConstructor / @AllArgsConstructor、@Data、@Builder、@Slf4j / @CommonsLog / @Log4j2

## 通过Spring Data JPA 操作数据库

* Repository
  * **@EnableJpaRepositories**
  * **Repository<T, ID>** 接⼝
    * CrudRepository<T, ID>
    * PagingAndSortingRepository<T, ID>
    * JpaRepository<T, ID>
* 定义查询
  * 根据⽅法名定义查询
    * find...By... / read...By... / query...By... / get...By...
    * count...By...
    * ...OrderBy...[Asc / Desc]
    * And / Or / IgnoreCase
    * Top / First / Distinct
* 分页查询
  * PagingAndSortingRepository<T, ID>
  * Pageable / Sort
  * Slice<T> / Page<T>
* Repository 是怎么从接⼝变成 Bean 的(Repository Bean 是如何创建的)
  * **JpaRepositoriesRegistrar**
    * 激活了 @EnableJpaRepositories
    * 返回了 JpaRepositoryConfigExtension
  * **RepositoryBeanDefinitionRegistrarSupport.registerBeanDefinitions**
    * 注册 Repository Bean(类型是 JpaRepositoryFactoryBean )
  * **RepositoryConfigurationExtensionSupport.getRepositoryConfigurations**
    * 取得 Repository 配置
  * **JpaRepositoryFactory.getTargetRepository**
    * 创建了 Repository
* 接口中的⽅法是如何被解释的
  * **RepositoryFactorySupport.getRepository** 添加了**Advice**
    * DefaultMethodInvokingMethodInterceptor
    * QueryExecutorMethodInterceptor
  * **AbstractJpaQuery.execute** 执⾏具体的查询

## 线上咖啡项目

![image-20190518145554699](/Users/dingyuanjie/Documents/study/github/woodyprogram/img/image-20190518145554699.png)

![image-20190518145624853](/Users/dingyuanjie/Documents/study/github/woodyprogram/img/image-20190518145624853.png)

```xml
<dependency>
  <groupId>org.springframework.boot</groupId>
  <artifactId>spring-boot-starter-data-jpa</artifactId>
</dependency>
<dependency>
  <groupId>org.joda</groupId>
  <artifactId>joda-money</artifactId>
  <version>1.0.1</version>
</dependency>
<dependency>
  <groupId>org.jadira.usertype</groupId>
  <artifactId>usertype.core</artifactId>
  <version>6.0.1.GA</version>
</dependency>
<dependency>
  <groupId>com.h2database</groupId>
  <artifactId>h2</artifactId>
  <scope>runtime</scope>
</dependency>
<dependency>
  <groupId>org.projectlombok</groupId>
  <artifactId>lombok</artifactId>
  <optional>true</optional>
</dependency>
```

```java
@MappedSuperclass
@Data
@NoArgsConstructor
@AllArgsConstructor
public class BaseEntity implements Serializable {
    @Id
    @GeneratedValue
    private Long id;
    @Column(updatable = false)
    @CreationTimestamp
    private Date createTime;
    @UpdateTimestamp
    private Date updateTime;
}

@Entity
@Table(name = "T_MENU")
@Builder
@Data
@ToString(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
public class Coffee extends BaseEntity implements Serializable {
    private String name;
    @Type(type = "org.jadira.usertype.moneyandcurrency.joda.PersistentMoneyAmount",
            parameters = {@org.hibernate.annotations.Parameter(name = "currencyCode", value = "CNY")})
    private Money price;
}

@Entity
@Table(name = "T_ORDER")
@Data
@ToString(callSuper = true)
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CoffeeOrder extends BaseEntity implements Serializable {
    private String customer;
    @ManyToMany
    @JoinTable(name = "T_ORDER_COFFEE")
    @OrderBy("id")
    private List<Coffee> items;
    @Enumerated
    @Column(nullable = false)
    private OrderState state;
}

public enum OrderState {
    INIT, PAID, BREWING, BREWED, TAKEN, CANCELLED
}
```

```properties
spring.jpa.hibernate.ddl-auto=create-drop
spring.jpa.properties.hibernate.show_sql=true
spring.jpa.properties.hibernate.format_sql=true
```

```java
@NoRepositoryBean
public interface BaseRepository<T, Long> extends PagingAndSortingRepository<T, Long> {
    List<T> findTop3ByOrderByUpdateTimeDescIdAsc();
}
public interface CoffeeOrderRepository extends BaseRepository<CoffeeOrder, Long> {
    List<CoffeeOrder> findByCustomerOrderById(String customer);
    List<CoffeeOrder> findByItems_Name(String name);
}
public interface CoffeeRepository extends BaseRepository<Coffee, Long> {
}
```

```java
@SpringBootApplication
@EnableJpaRepositories
@EnableTransactionManagement
@Slf4j
public class JpaDemoApplication implements ApplicationRunner {
	@Autowired
	private CoffeeRepository coffeeRepository;
	@Autowired
	private CoffeeOrderRepository orderRepository;

	public static void main(String[] args) {
		SpringApplication.run(JpaDemoApplication.class, args);
	}

	@Override
	@Transactional
	public void run(ApplicationArguments args) throws Exception {
		initOrders();
		findOrders();
	}

	private void initOrders() {
    //保存实体
		Coffee latte = Coffee.builder().name("latte")
				.price(Money.of(CurrencyUnit.of("CNY"), 30.0))
				.build();
		coffeeRepository.save(latte);
		log.info("Coffee: {}", latte);

		Coffee espresso = Coffee.builder().name("espresso")
				.price(Money.of(CurrencyUnit.of("CNY"), 20.0))
				.build();
		coffeeRepository.save(espresso);
		log.info("Coffee: {}", espresso);

		CoffeeOrder order = CoffeeOrder.builder()
				.customer("Li Lei")
				.items(Collections.singletonList(espresso))
				.state(OrderState.INIT)
				.build();
		orderRepository.save(order);
		log.info("Order: {}", order);

		order = CoffeeOrder.builder()
				.customer("Li Lei")
				.items(Arrays.asList(espresso, latte))
				.state(OrderState.INIT)
				.build();
		orderRepository.save(order);
		log.info("Order: {}", order);
	}

	private void findOrders() {
		coffeeRepository
				.findAll(Sort.by(Sort.Direction.DESC, "id"))
				.forEach(c -> log.info("Loading {}", c));

		List<CoffeeOrder> list = orderRepository.findTop3ByOrderByUpdateTimeDescIdAsc();
		log.info("findTop3ByOrderByUpdateTimeDescIdAsc: {}", getJoinedOrderId(list));

		list = orderRepository.findByCustomerOrderById("Li Lei");
		log.info("findByCustomerOrderById: {}", getJoinedOrderId(list));

		// 不开启事务会因为没Session而报LazyInitializationException
		list.forEach(o -> {
			log.info("Order {}", o.getId());
			o.getItems().forEach(i -> log.info("  Item {}", i));
		});

		list = orderRepository.findByItems_Name("latte");
		log.info("findByItems_Name: {}", getJoinedOrderId(list));
	}

	private String getJoinedOrderId(List<CoffeeOrder> list) {
		return list.stream().map(o -> o.getId().toString())
				.collect(Collectors.joining(","));
	}
}
```

## MyBatis

* 简单配置

  ```properties
  mybatis.mapper-locations = classpath*:mapper/**/*.xml 
  mybatis.type-aliases-package = 类型别名的包名
  mybatis.type-handlers-package = TypeHandler扫描包名 
  mybatis.configuration.map-underscore-to-camel-case = true
  ```

* Mapper 的定义与扫描
  * @MapperScan 配置扫描位置
  * @Mapper 定义接⼝
  * 映射的定义—— XML 与注解
* **MyBatis Generator**
  * MyBatis 代码⽣成器
  * 根据数据库表⽣成相关代码
    * POJO、Mapper 接⼝、SQL Map XML
* 运⾏ MyBatis Generator
  * 命令行
    * `java -jar mybatis-generator-core-x.x.x.jar -configfile generatorConfig.xml`
  * **Maven Plugin**(**mybatis-generator-maven-plugin**)
    * mvn mybatis-generator:generate
    * ${basedir}/src/main/resources/generatorConfig.xml
* 配置 MyBatis Generator
  * **generatorConfiguration**
  * **context**
    * jdbcConnection
    *  javaModelGenerator
    *  sqlMapGenerator
    *  javaClientGenerator (ANNOTATEDMAPPER / XMLMAPPER / MIXEDMAPPER) 
    * table 
* 生成时可以使⽤的插件
  * 内置插件都在 **org.mybatis.generator.plugins** 包中
    * FluentBuilderMethodsPlugin
    * ToStringPlugin
    * SerializablePlugin
    * RowBoundsPlugin
* 使⽤用生成的对象
  * 简单操作，直接使⽤生成的 xxxMapper 的⽅法
  * 复杂查询，使用生成的 xxxExample 对象
* 工具：MyBatis PageHelper
  * ⽀持多种数据库
  * ⽀持多种分页⽅式
  * `pagehelper-spring-boot-starter`

```java
package geektime.spring.data.mybatisdemo.mapper;
@Mapper
public interface CoffeeMapper {
    @Insert("insert into t_coffee (name, price, create_time, update_time)"
            + "values (#{name}, #{price}, now(), now())")
    @Options(useGeneratedKeys = true)
    int save(Coffee coffee);

    @Select("select * from t_coffee where id = #{id}")
    @Results({
            @Result(id = true, column = "id", property = "id"),
            @Result(column = "create_time", property = "createTime"),
            // map-underscore-to-camel-case = true 可以实现一样的效果
            // @Result(column = "update_time", property = "updateTime"),
    })
    Coffee findById(@Param("id") Long id);
}
```

```java
@SpringBootApplication
@Slf4j
@MapperScan("geektime.spring.data.mybatisdemo.mapper")
public class MybatisDemoApplication implements ApplicationRunner {
	@Autowired
	private CoffeeMapper coffeeMapper;
	public static void main(String[] args) {
		SpringApplication.run(MybatisDemoApplication.class, args);
	}

	@Override
	public void run(ApplicationArguments args) throws Exception {
		Coffee c = Coffee.builder().name("espresso")
				.price(Money.of(CurrencyUnit.of("CNY"), 20.0)).build();
		int count = coffeeMapper.save(c);
		log.info("Save {} Coffee: {}", count, c);

		c = Coffee.builder().name("latte")
				.price(Money.of(CurrencyUnit.of("CNY"), 25.0)).build();
		count = coffeeMapper.save(c);
		log.info("Save {} Coffee: {}", count, c);

		c = coffeeMapper.findById(c.getId());
		log.info("Find Coffee: {}", c);
	}
}
```

```xml
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE generatorConfiguration
        PUBLIC "-//mybatis.org//DTD MyBatis Generator Configuration 1.0//EN"
        "http://mybatis.org/dtd/mybatis-generator-config_1_0.dtd">

<generatorConfiguration>
    <context id="H2Tables" targetRuntime="MyBatis3">
        <plugin type="org.mybatis.generator.plugins.FluentBuilderMethodsPlugin" />
        <plugin type="org.mybatis.generator.plugins.ToStringPlugin" />
        <plugin type="org.mybatis.generator.plugins.SerializablePlugin" />
        <plugin type="org.mybatis.generator.plugins.RowBoundsPlugin" />

        <jdbcConnection driverClass="org.h2.Driver"
                        connectionURL="jdbc:h2:mem:testdb"
                        userId="sa"
                        password="">
        </jdbcConnection>

        <javaModelGenerator targetPackage="geektime.spring.data.mybatis.model"
                            targetProject="./src/main/java">
            <property name="enableSubPackages" value="true" />
            <property name="trimStrings" value="true" />
        </javaModelGenerator>

        <sqlMapGenerator targetPackage="geektime.spring.data.mybatis.mapper"
                         targetProject="./src/main/resources/mapper">
            <property name="enableSubPackages" value="true" />
        </sqlMapGenerator>

        <javaClientGenerator type="MIXEDMAPPER"
                             targetPackage="geektime.spring.data.mybatis.mapper"
                             targetProject="./src/main/java">
            <property name="enableSubPackages" value="true" />
        </javaClientGenerator>

        <table tableName="t_coffee" domainObjectName="Coffee" >
            <generatedKey column="id" sqlStatement="CALL IDENTITY()" identity="true" />
            <columnOverride column="price" javaType="org.joda.money.Money" jdbcType="BIGINT"
                            typeHandler="geektime.spring.data.mybatis.handler.MoneyTypeHandler"/>
        </table>
    </context>
</generatorConfiguration>
```



