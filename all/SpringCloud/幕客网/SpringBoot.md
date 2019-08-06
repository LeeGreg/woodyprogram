## Java8

* lambda表达式

  ```java
  // 将List对象中的某个属性取出到另一个List里
  List<Integer> categoryTypeList = productInfoList.stream()
    .map(ProductInfo::getCategoryType)
    .collect(Collectors.toList());
  
  // 
  List<DecreaseStockInput> decreaseStockInputList = orderDTO.getOrderDetailList().stream()
  .map(e -> new DecreaseStockInput(e.getProductId(), e.getProductQuantity()))
  .collect(Collectors.toList());
  ```

## JSON

* String转List

  ```java
  Gson gson = new Gson();
  List<OrderDetail> orderDetailList = new ArrayList<>();
  try {
    orderDetailList = gson.fromJson(orderForm.getItems(),
  		new TypeToken<List<OrderDetail>>() {
  	}.getType());
  } catch (Exception e) {
    log.error("【json转换】错误, string={}", orderForm.getItems());
    throw new OrderException(ResultEnum.PARAM_ERROR);
  }
  
  public class JsonUtil {
  
  	private static ObjectMapper objectMapper = new ObjectMapper();
  
  	/**
  	 * 转换为json字符串
  	 * @param object
  	 * @return
  	 */
  	public static String toJson(Object object) {
  		try {
  			return objectMapper.writeValueAsString(object);
  		} catch (JsonProcessingException e) {
  			e.printStackTrace();
  		}
  		return null;
  	}
  
  	/**
  	 * json转对象
  	 * @param string
  	 * @param classType
  	 * @return
  	 */
  	public static Object fromJson(String string, Class classType) {
  		try {
  			return objectMapper.readValue(string, classType);
  		} catch (IOException e) {
  			e.printStackTrace();
  		}
  		return null;
  	}
  
  
  	/**
  	 * json转对象
  	 * @param string
  	 * @param typeReference
  	 * @return
  	 */
  	public static Object fromJson(String string, TypeReference typeReference) {
  		try {
  			return objectMapper.readValue(string, typeReference);
  		} catch (IOException e) {
  			e.printStackTrace();
  		}
  		return null;
  	}
  }
  ```

## 请求规定

```java
// 接口指定一种请求方式，如Get或POST或DELETE等
@RequestParam：获取请求参数的值，不管使用什么方式都能接收到（url或form或json）
@PathVariable：从url中获取参数{id}
Get请求参数放到url后
Post请求参数放在form中
```

## 返回及统一异常处理

```java
// RunTimeException才事务回滚，Exception不事务回滚

// controller层：方法 throws Exception
// service层：方法 throws Exception
						 // throw new GirlException(ResultEnum.PRIMARY_SCHOOL);

/**  http请求返回的最外层对象 */
@Data
public class Result<T> {
    /** 错误码. */
    private Integer code;
    /** 提示信息. */
    private String msg;
    /** 具体的内容. */
    private T data;
}
```

```java
@Getter
public enum ResultEnum {
    UNKONW_ERROR(-1, "未知错误"),
    SUCCESS(0, "成功"),
    ;

    private Integer code;
    private String msg;

    ResultEnum(Integer code, String msg) {
        this.code = code;
        this.msg = msg;
    }
}
```

```java
@Data
public class GirlException extends RuntimeException{
    private Integer code;

    public GirlException(ResultEnum resultEnum) {
        super(resultEnum.getMsg());
        this.code = resultEnum.getCode();
    }
}
```

```java
@ControllerAdvice
public class ExceptionHandle {
    private final static Logger logger = LoggerFactory.getLogger(ExceptionHandle.class);

    @ExceptionHandler(value = Exception.class)
    @ResponseBody
    public Result handle(Exception e) {
        if (e instanceof GirlException) {
            GirlException girlException = (GirlException) e;
            return ResultUtil.error(girlException.getCode(), girlException.getMessage());
        }else {
            logger.error("【系统异常】{}", e);
            return ResultUtil.error(-1, "未知错误");
        }
    }
}
```

```java
// 工具
public class ResultUtil {
  public static Result success(Object object) {
    Result result = new Result();
    result.setCode(0);
    result.setMsg("成功");
    result.setData(object);
    return result;
  }
  
  public static Result success() {
        return success(null);
  }

  public static Result error(Integer code, String msg) {
    Result result = new Result();
    result.setCode(code);
    result.setMsg(msg);
    return result;
  }
}
```

## 配置

* 配置文件参数映射成Bean

```java
// application-dev.properties或application-dev.yaml(冒号后加空格)文件中
// 参数值 映射 成Bean
limit.
  minMoney=0.01
  maxMoney9999
	description=最少要发${limit.minMoney}元, 最多${limit.maxMoney}元
	
// 多个属性	
@Data
@Component
@ConfigurationProperties(prefix = "limit")
public class LimitConfig {
	private BigDecimal minMoney;
	private BigDecimal maxMoney;
  private String description;
}

// 单个属性
@Value("${limit.minMoney}")
private BigDecimal minMoney;
```

## 返回

```java
// 返回 json 指定名字
@Data
public class Product {
  @JsonProperty("name")
  private String categoryName;
  //...
}
```

## JPA

* 定义了一系列对象持久化的标准，有hibernate

```yaml
spring:
		jpa:
    	hibernate:
      	ddl-auto: update
    	show-sql: true
```

```java
// 数据库实体,主键类型
public interface LuckmoneyRepository extends JpaRepository<Luckymoney, Integer> {
}

// repository.findById(id).orElse(null);
// 更新数据：先查询，再更新
Optional<Luckymoney> optional = repository.findById(id);
if (optional.isPresent()) {
  Luckymoney luckymoney = optional.get();
  luckymoney.setConsumer(consumer);
  return repository.save(luckymoney);
}

@Data
@Entity
public class Luckymoney {

	@Id
	@GeneratedValue
	private Integer id;
  
	private BigDecimal money;
  /** 发送方 */
	private String producer;
	/**  接收方 */
	private String consumer;
	public Luckymoney() {
	}
}
```

## 事务

```java
// inodb
// Service层方法上加 @Transactional
```

## @Valid表单验证

```java
// Controller 方法中对象参数@Valid Girl girl  , 验证结果 BindingResult
@PostMapping(value = "/girls")
public Result<Girl> girlAdd(@Valid Girl girl, BindingResult bindingResult) {
  // 验证结果
  if (bindingResult.hasErrors()) {
    return ResultUtil.error(1, bindingResult.getFieldError().getDefaultMessage());
  }
  girl.setCupSize(girl.getCupSize());
  girl.setAge(girl.getAge());
  return ResultUtil.success(girlRepository.save(girl));
}

// 对象类
@Entity
public class Girl {
  @Min(value = 18, message = "xxxx")
  private Integer age;
  @NotNull(message = "金额必传")
  private Double money;
  @NotBlank(message = "这个字段必传")
  private String cupSize;
}
```

## 使用AOP 处理请求

```java
//统一处理日志请求
@Aspect
@Component
public class HttpAspect {
    private final static Logger logger = LoggerFactory.getLogger(HttpAspect.class);

    @Pointcut("execution(public * com.imooc.controller.GirlController.*(..))")
    public void log() {
    }

    @Before("log()")
    public void doBefore(JoinPoint joinPoint) {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        HttpServletRequest request = attributes.getRequest();

        //url
        logger.info("url={}", request.getRequestURL());

        //method
        logger.info("method={}", request.getMethod());

        //ip
        logger.info("ip={}", request.getRemoteAddr());

        //类方法
        logger.info("class_method={}", joinPoint.getSignature().getDeclaringTypeName() + "." + joinPoint.getSignature().getName());

        //参数
        logger.info("args={}", joinPoint.getArgs());
    }

    @After("log()")
    public void doAfter() {
        logger.info("222222222222");
    }
  
    // 获取返回参数
    @AfterReturning(returning = "object", pointcut = "log()")
    public void doAfterReturning(Object object) {
        logger.info("response={}", object.toString());
    }
}
```

## 单元测试

```java
// 打包自动测试
mvn clean package
// 打包时跳过测试
mvn clean package -Dmaven.test.skip=true

@RunWith(SpringRunner.class)
@SpringBootTest
@AutoConfigureMockMvc
public class GirlControllerTest {
  
    @Autowired
    private GirlService girlService;

    @Autowired
    private MockMvc mvc;

    @Test
    public void girlList() throws Exception {
        mvc.perform(MockMvcRequestBuilders.get("/girls"))
                .andExpect(MockMvcResultMatchers.status().isOk())
        .andExpect(MockMvcResultMatchers.content().string("abc"));
    }
  
    @Test
    public void findOneTest() {
        Girl girl = girlService.findOne(73);
        Assert.assertEquals(new Integer(13), girl.getAge());
    }
}
```