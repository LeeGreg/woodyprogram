# Readme

```java
// 上传文件到服务器
multipartFile.transferTo(file);
// taskRepository.findByNameLike("%" + name + "%")
// MessageFormat.format("123{0}", "456"); // 123456
```

## 角色、权限、菜单

```java
T_SYS_USER
T_SYS_ROLE
	DEPTID、NAME、NUM、PID、TIPS、VERSION
T_SYS_RELATION
	MENUID、ROLEID
T_SYS_MENU
	code、icon、ismenu、isopen、levels、name、num、pcode、pcodes、status、tips、url
T_SYS_DEPT
	fullname、num、pid、pids、simplename、tips、version
```

* 部门

  ```java
  // 部门首页显示部门列表，可以叠起来，可根据名称（fullname、simplename）进行模糊搜索
  id: "1"、pid: "0"、pids: "[0],"  fullname: "山迪亚集团"、simplename: "总公司"
  id: "2"、pid: "1"、pids: "[0],[1],"、fullname: "开发部"、simplename: "开发部"
    
  // 添加，点击上级部门进行填写时会调用接口获取部门树，最少会有“顶级”部门（代码生成返回）
  id: 
  simplename: test1
  fullname: 测试
  tips: 测试用
  num: 1  //排序
  pid: 0
  //完善pids,根据pid拿到pid的pids  
    如果pid为空或者0，则pid=0，pids="[0],"
    否则，根据pid获取其Dept，pids=Dept.pids + "[" + pid + "],"   // 为了好展示
    存入Dept表
  
  // 修改
  // 删除，根据模糊查询pids，删除其子部门
  
  // 部门树
  直接返回List（单表Dept）
  id: "0"、pId: "0"、name: "顶级"、open: true、isOpen: true、checked: true // 代码生成的记录
  // pid为0或者null是isOpen为ture
  id: "1"、pId: "0"、name: "总公司"、open: true、isOpen: true、checked: ""
  id: "2"、pId: "1"、name: "开发部"、open: false、isOpen: false、checked: ""
  ```

* 菜单

  ```java
  // 新增菜单
  	// 获取菜单列表(选择父级菜单用)
  			SELECT m1.id                                                            AS id,
         	(CASE WHEN (m2.id = 0 OR m2.id IS NULL) THEN 0 ELSE m2.id END)        AS pId,
         	m1.NAME                                                               AS NAME,
         	(CASE WHEN (m2.id = 0 OR m2.id IS NULL) THEN 'true' ELSE 'false' END) AS isOpen,
         	m1.code																															  AS code
  			FROM t_sys_menu m1
           LEFT JOIN t_sys_menu m2 ON m1.pcode = m2.CODE
  			ORDER BY m1.id ASC
  				name: "顶级"、id: "0"、pId: "0"、open: true、isOpen: true、checked: true
          name: "系统管理"、id: "1"、pId: "0"、open: true、isOpen: true、checked: ""
  				name: "用户管理"、id: "4"、pId: "1"、open: false、isOpen: false、checked: ""
          name: "添加用户"、id: "5"、pId: "4"、open: false、isOpen: false、checked: ""  
  id: 
  name: 测试
  code: test_add
  pcode: 0
  url: /test
  num: 1
  icon: 
  ismenu: 1
  	 //判断是否存在该编号   
     //如果pCode为空或0，
    		//则pCode设置为“0”，pCodes为“[0],”，Levels为1
     	  //否则，根据pCode找到其menu-pMenu，menu的levels为pMenu.levels+1，menu的pCodes为pMenu.getPcodes() + "[" + pMenu.getCode() + "],"
      //保存menu到表中
  
  // 删除
    根据menuId找到menu，然后根据Pcodes模糊查询出所有子菜单，删除所有子菜单
    根据menuId删除当前菜单，根据menuId删除关联的t_sys_relation
  ```

* 角色

  ```java
  // 新增
  	// 选择上级名称（角色树，pId为空或0，则open为true）
  name: "顶级"、id: "0"、pId: "0"、open: true、isOpen: true、checked: true  //代码生成
  name: "超级管理员"、id: "1"、pId: "0"、open: true、isOpen: true、checked: ""
  	// 选择部门名称（部门树）  
  id: 
  name: test_role
  pid: 2
  deptid: 2
  tips: 测试role
  num: 1  
  // 编辑
  // 删除（删除角色、删除角色拥有的权限）  
    
  // 查看权限（List），显示权限树，如果之前有勾选权限，则返回显示的是带有勾选的
    根据roleId查找menuId(t_sys_relation)
    	// 如果查出为空（之前没勾选过权限），则查出所有的menuId并返回
    		（
    			id:1、pId:0、name:系统管理、isOpen:true、code:system
    			id:2、pId:0、name:CMS管理、isOpen:true、code:cms
    			id:3、pId:0、name:运维管理、isOpen:true、code:operationMgr
    			id:4、pId:1、name:用户管理、isOpen:false、code:mgr
    			id:5、pId:4、name:添加用户、isOpen:false、code:mgr.add
    			...
    			）
    	SELECT m1.id                                                             AS id,
         (CASE WHEN (m2.id = 0 OR m2.id IS NULL) THEN 0 ELSE m2.id END)        AS pId,
         m1.NAME                                                               AS NAME,
         (CASE WHEN (m2.id = 0 OR m2.id IS NULL) THEN 'true' ELSE 'false' END) AS isOpen,
         m1.code                                                               As code
  FROM t_sys_menu m1
           LEFT JOIN t_sys_menu m2 ON m1.pcode = m2.CODE
  ORDER BY m1.id ASC
      // 如过查出不为空，还是查出所有menu，不过要带上是否已勾选(checked=true)
      SELECT m1.id                                                             AS id,
         (CASE WHEN (m2.id = 0 OR m2.id IS NULL) THEN 0 ELSE m2.id END)        AS pId,
         m1.NAME                                                               AS NAME,
         (CASE WHEN (m2.id = 0 OR m2.id IS NULL) THEN 'true' ELSE 'false' END) AS isOpen,
         (CASE WHEN (m3.ID = 0 OR m3.ID IS NULL) THEN 'false' ELSE 'true' END) "checked"
  FROM t_sys_menu m1
           LEFT JOIN t_sys_menu m2 ON m1.pcode = m2.CODE
           LEFT JOIN (SELECT ID FROM t_sys_menu WHERE ID IN (?1)) m3 ON m1.id = m3.id
  ORDER BY m1.id ASC
  		// 返回格式为List
  name: "系统管理"、id: "1"、pId: "0"、open: true、isOpen: true、checked: true   //一级菜单
  name: "用户管理"、id: "4"、pId: "1"、open: false、isOpen: false、checked: true //二级菜单
  name: "添加用户"、id: "5"、pId: "4"、open: false、isOpen: false、checked: true //权限
  
  // 保存权限
  roleId: 3
  ids:1,4,5,6,7,8,9,10,11,40,41,42,12,13,14,15,16,37,38,39,17,18,19,20,28,29,21,23,24,30,31,32,36,22,25,26,27,33,34,35,43,44,45,46,47,48,49,50
    // 先根据roleId删除角色所有权限（t_sys_relation）
    // 遍历ids添加新权限（roleId、menuId）
  ```

* 用户

  ```java
  // 用户首页，左边是部门树，右边对应的是用户分页列表，默认显示所有用户
  // 查询条件：选择一个左边部门、用户名称、注册起始时间
  order: desc
  offset: 0
  limit: 14
  deptid: 3
  name: 
  beginTime: 
  endTime: 
  // 判断当前用户是否是超级管理员
  // 单表查询（User）
  
  // 新增(账户、姓名、密码、部门(部门树中选择一个)、...)
  1. 如果id为null，则查询数据库是否已存在用户，不存在则随机5位salt和pwd进行md5加密密码，插入用户表
  2. 如果id不为null，则更新
  // 删除（逻辑删除，状态字段）
  // 重置密码（111111）
  // 冻结、解冻（状态字段）
  
  // 选中一个用户分配角色
  	根据userId查找用户
  		//如果用户roleId为空，则说明没分配过角色，则查出所有角色（t_sys_role）返回供其选择
  			name: "超级管理员"、id: "1"、pId: "0"、open: true、isOpen: true、checked: false
  			name: "网站管理员"、id: "2"、pId: "1"、open: false、isOpen: false、checked: false
  			name: "test_role"、id: "3"、pId: "2"、open: false、isOpen: false、checked: false
      //如果不为空，in（roleIds）
      SELECT r.id                                                              AS id,
         pId                                                                   AS pId,
         NAME                                                                  AS NAME,
         (CASE WHEN (pId = 0 OR pId IS NULL) THEN 'true' ELSE 'false' END)     "open",
         (CASE WHEN (r1.ID = 0 OR r1.ID IS NULL) THEN 'false' ELSE 'true' END) AS checked
  		FROM t_sys_role r
           LEFT JOIN (SELECT ID FROM t_sys_role WHERE ID IN (?1)) r1 ON r.ID = r1.ID
  		ORDER BY pId, num ASC
  			// 返回
  			name: "超级管理员"、id: "1"、pId: "0"、open: true、isOpen: true、checked: false
  			name: "网站管理员"、id: "2"、pId: "1"、open: false、isOpen: false、checked: false
  			name: "test_role"、id: "3"、pId: "2"、open: false、isOpen: false、checked: true
  // 保存角色（更新User角色字段值roleId）
      roleIds: 3
  		userId: 3
  ```

## 权限验证

* 自定义注解@Permission添加到需要权限才能访问的方法上
* 通过AOP利用反射获取方法上的@Permission的值（权限）
  * 如果没有，则可直接访问不需要权限
  * 否则，校验当前用户是否有权限SecurityUtils.getSubject().hasRole(方法所需要的权限)

## 审计功能

* Spring Data的AuditorAware审计功能

  * 即由谁在什么时候创建或修改实体

  * Spring Data提供了在实体类的属性上增加@CreatedBy，@LastModifiedBy，@CreatedDate，@LastModifiedDate注解，并配置相应的配置项，即可实现审计功能，有系统自动记录`createdBy`、`CreatedDate`、`lastModifiedBy`、`lastModifiedDate`四个属性的值

  * 启动类：`@EnableJpaAuditing`

    ```java
    // 其他实体继承BaseEntity
    @MappedSuperclass
    @Data
    public abstract class BaseEntity implements Serializable {
        @Id
        @GeneratedValue(strategy = GenerationType.IDENTITY)
        private Long id;
        @CreatedDate
        @Column(name = "create_time", columnDefinition = "DATETIME COMMENT '创建时间/注册时间'")
        private Date createTime;
        @Column(name = "create_by", columnDefinition = "bigint COMMENT '创建人'")
        @CreatedBy
        private Long createBy;
        @LastModifiedDate
        @Column(name = "modify_time", columnDefinition = "DATETIME COMMENT '最后更新时间'")
        private Date modifyTime;
        @LastModifiedBy
        @Column(name = "modify_by", columnDefinition = "bigint COMMENT '最后更新人'")
        private Long modifyBy;
    }
    
    @Entity
    @Table(name = "T_ATTACHMENT")
    @Data
    @EntityListeners(AuditingEntityListener.class)
    public class Attachment extends BaseEntity {
      ...
    }
    ```

    ```java
    /**
     * 审计功能配置
     */
    @Configuration
    public class UserIDAuditorConfig implements AuditorAware<Long> {
        @Override
        public Optional<Long> getCurrentAuditor() {
            try {
                String token = HttpKit.getRequest().getHeader("Authorization");
                if (StringUtils.isNotEmpty(token)) {
                    return Optional.of(JwtUtil.getUserId(token));
                }
            } catch (Exception e) {
                //返回系统用户id
                return Optional.of(-1L);
            }
            //返回系统用户id
            return Optional.of(-1L);
        }
    }
    ```

  * 即完成配置，在使用`repository`保存对象时，`createdBy``CreatedDate``lastModifiedBy``lastModifiedDate`有审计功能自动插入

## Shiro + JWT

* [github](https://github.com/Smith-Cruise/Spring-Boot-Shiro)

* 特性

  * 完全使用了 Shiro 的注解配置，保持高度的灵活性
  * 放弃 Cookie ，Session ，使用JWT进行鉴权，完全实现无状态鉴权
  * JWT 密钥支持过期时间
  * 对跨域提供支持

* 逻辑

  * POST 用户名与密码到 `/login` 进行登入，如果成功返回一个加密 token，失败的话直接返回 401 错误
  * 用户访问每一个需要权限的网址请求必须在 `header` 中添加 `Authorization` 字段，例如 `Authorization: token`，`token` 为密钥
  * 后台会进行 `token` 的校验，如果有误会直接返回 401

* token加密说明

  * 携带了 `username` 信息在 token 中
  * 设定了过期时间
  * 使用用户登入密码对 `token` 进行加密

* token校验流程

  * 获得 `token` 中携带的 `username` 信息
  * 进入数据库搜索这个用户，得到他的密码
  * 使用用户的密码来检验 `token` 是否正确

* maven

  * `shiro-spring`、`java-jwt`、

* 表

  * `username`、`password`、`role`、`permission`

* 配置jwt

  * 写一个简单的 JWT 加密，校验工具，并且使用用户自己的密码充当加密密钥，这样保证了 token 即使被他人截获也无法破解。并且在 `token` 中附带了 `username` 信息，并且设置密钥5分钟就会过期

  ```java
  public class JWTUtil {
      // 过期时间5分钟
      private static final long EXPIRE_TIME = 5*60*1000;
  
      /**
       * 校验token是否正确
       * @param token 密钥
       * @param secret 用户的密码
       * @return 是否正确
       */
      public static boolean verify(String token, String username, String secret) {
          try {
              Algorithm algorithm = Algorithm.HMAC256(secret);
              JWTVerifier verifier = JWT.require(algorithm)
                      .withClaim("username", username)
                      .build();
              DecodedJWT jwt = verifier.verify(token);
              return true;
          } catch (Exception exception) {
              return false;
          }
      }
  
      /**
       * 获得token中的信息无需secret解密也能获得
       * @return token中包含的用户名
       */
      public static String getUsername(String token) {
          try {
              DecodedJWT jwt = JWT.decode(token);
              return jwt.getClaim("username").asString();
          } catch (JWTDecodeException e) {
              return null;
          }
      }
  
      /**
       * 生成签名,5min后过期
       * @param username 用户名
       * @param secret 用户的密码
       * @return 加密的token
       */
      public static String sign(String username, String secret) {
          try {
              Date date = new Date(System.currentTimeMillis()+EXPIRE_TIME);
              Algorithm algorithm = Algorithm.HMAC256(secret);
              // 附带username信息
              return JWT.create()
                      .withClaim("username", username)
                      .withExpiresAt(date)
                      .sign(algorithm);
          } catch (UnsupportedEncodingException e) {
              return null;
          }
      }
  }
  ```

  ```java
  // login   String passwdMd5 = MD5.md5(password, user.getSalt());
  	// 校验用户输入的username、password，成功则返回token=JWTUtil.sign(username, password)
  		// 用户登录:验证没有注册、验证密码错误、登录成功
  @RequestMapping(value = "/login", method = RequestMethod.POST)
  public Object login(@RequestParam("username") String userName,
                      @RequestParam("password") String password) {
    try {
      User user = userService.findByAccount(userName);
      if (user == null) {
        return Rets.failure("该用户不存在");
      }
      String passwdMd5 = MD5.md5(password, user.getSalt());
      if (!user.getPassword().equals(passwdMd5)) {
        return Rets.failure("输入的密码错误");
      }
      String token = JwtUtil.sign(user);
      Map<String, String> result = new HashMap<>(1);
      logger.info("token:{}", token);
      result.put("token", token);
      return Rets.success(result);
    } catch (Exception e) {
      logger.error(e.getMessage(), e);
    }
    return Rets.failure("登录时失败");
  }
  
  // 校验是否登录
   @GetMapping("/article")
  public ResponseBean article() {
    Subject subject = SecurityUtils.getSubject();  //框架
    if (subject.isAuthenticated()) {
      return new ResponseBean(200, "You are already logged in", null);
    } else {
      return new ResponseBean(200, "You are guest", null);
    }
  }
  // 登录才能访问的接口上标注
  @RequiresAuthentication
  // 需要某个角色才能访问的接口上标注
  @RequiresRoles("admin")
  // 需要权限才能访问的接口上标注
  @RequiresPermissions(logical = Logical.AND, value = {"view", "edit"})
  ```

  ```java
  // 退出登录
  1. 从header中获取Authorization-token
  2. 根据token从缓存CacheManager中删除用户信息
  // 获取用户信息（权限、菜单）
  
  // 修改密码
  if (!MD5.md5(oldPassword, user.getSalt()).equals(user.getPassword())) {
  	return Rets.failure("旧密码输入错误");
  }
  ```

* 配置Shiro

  * 实现JWTToken

    * `JWTToken` 差不多就是 `Shiro` 用户名密码的载体。因为是前后端分离，服务器无需保存用户状态，所以不需要 `RememberMe` 这类功能，简单的实现下 `AuthenticationToken` 接口即可。因为 `token` 自己已经包含了用户名等信息，所以这里就弄了一个字段

      ```java
      @Data
      public class JWTToken implements AuthenticationToken {
          // 密钥
          private String token;
          public JWTToken(String token) {
              this.token = token;
          }
      }
      ```

  * 实现Realm

    * `realm` 的用于处理用户是否合法，需要自己实现

      ```java
      @Service
      public class MyRealm extends AuthorizingRealm {
        @Override
        public boolean supports(AuthenticationToken token) {
          return token instanceof JWTToken;
        }
        
        // 只有当需要检测用户权限的时候才会调用此方法，例如checkRole,checkPermission之类的
        @Override
        protected AuthorizationInfo doGetAuthorizationInfo(PrincipalCollection principals) {
          String username = JWTUtil.getUsername(principals.toString());
          UserBean user = userService.getUser(username);
          SimpleAuthorizationInfo simpleAuthorizationInfo = new SimpleAuthorizationInfo();
          simpleAuthorizationInfo.addRole(user.getRole());
          Set<String> permission = new HashSet<>(Arrays.asList(user.getPermission().split(",")));
          simpleAuthorizationInfo.addStringPermissions(permission);
          return simpleAuthorizationInfo;
        }
        
        //默认使用此方法进行用户名正确与否验证，错误抛出异常即可
        @Override
        protected AuthenticationInfo doGetAuthenticationInfo(AuthenticationToken auth) throws AuthenticationException {
          String token = (String) auth.getCredentials();
          // 解密获得username，用于和数据库进行对比
          String username = JWTUtil.getUsername(token);
          if (username == null) {
            throw new AuthenticationException("token invalid");
          }
      
          UserBean userBean = userService.getUser(username);
          if (userBean == null) {
            throw new AuthenticationException("User didn't existed!");
          }
      
          if (! JWTUtil.verify(token, username, userBean.getPassword())) {
            throw new AuthenticationException("Username or password error");
          }
      
          return new SimpleAuthenticationInfo(token, token, "my_realm");
        }
      }
      ```

  * 重写Filter

    * 所有的请求都会先经过 `Filter`，所以继承官方的 `BasicHttpAuthenticationFilter` ，并且重写鉴权的方法

    * 代码的执行流程 `preHandle` -> `isAccessAllowed` -> `isLoginAttempt` -> `executeLogin`

      ```java
      public class JWTFilter extends BasicHttpAuthenticationFilter {
         //对跨域提供支持
        @Override
        protected boolean preHandle(ServletRequest request, ServletResponse response) throws Exception {
          HttpServletRequest httpServletRequest = (HttpServletRequest) request;
          HttpServletResponse httpServletResponse = (HttpServletResponse) response;
          httpServletResponse.setHeader("Access-control-Allow-Origin", httpServletRequest.getHeader("Origin"));
          httpServletResponse.setHeader("Access-Control-Allow-Methods", "GET,POST,OPTIONS,PUT,DELETE");
          httpServletResponse.setHeader("Access-Control-Allow-Headers", httpServletRequest.getHeader("Access-Control-Request-Headers"));
          // 跨域时会首先发送一个option请求，这里我们给option请求直接返回正常状态
          if (httpServletRequest.getMethod().equals(RequestMethod.OPTIONS.name())) {
            httpServletResponse.setStatus(HttpStatus.OK.value());
            return false;
          }
          return super.preHandle(request, response);
        }
        
        /**
      	* 为什么最终返回的都是true，即允许访问
      	* 例如提供一个地址 GET /article
      	* 登入用户和游客看到的内容是不同的
      	* 如果在这里返回了false，请求会被直接拦截，用户看不到任何东西
        * 所以在这里返回true，Controller中可以通过 subject.isAuthenticated() 来判断用户是否登入
      	* 如果有些资源只有登入用户才能访问，只需要在方法上面加上 @RequiresAuthentication 注解即可
        * 但是这样做有一个缺点，就是不能够对GET,POST等请求进行分别过滤鉴权(因为重写了官方的方法)，但实际上对应用影响不大
      	*/
        @Override
        protected boolean isAccessAllowed(ServletRequest request, ServletResponse response, Object mappedValue) {
          if (isLoginAttempt(request, response)) {
            try {
              executeLogin(request, response);
            } catch (Exception e) {
              response401(request, response);
            }
          }
          return true;
        }
        
        /**
      	* 判断用户是否想要登入。
      	* 检测header里面是否包含Authorization字段即可
      	*/
        @Override
        protected boolean isLoginAttempt(ServletRequest request, ServletResponse response) {
          HttpServletRequest req = (HttpServletRequest) request;
          String authorization = req.getHeader("Authorization");
          return authorization != null;
        }
      
        @Override
        protected boolean executeLogin(ServletRequest request, ServletResponse response) throws Exception {
          HttpServletRequest httpServletRequest = (HttpServletRequest) request;
          String authorization = httpServletRequest.getHeader("Authorization");
      
          JWTToken token = new JWTToken(authorization);
          // 提交给realm进行登入，如果错误他会抛出异常并被捕获
          getSubject(request, response).login(token);
          // 如果没有抛出异常则代表登入成功，返回true
          return true;
        }
        
        //  将非法请求跳转到 /401
        private void response401(ServletRequest req, ServletResponse resp) {
          try {
            HttpServletResponse httpServletResponse = (HttpServletResponse) resp;
            httpServletResponse.sendRedirect("/401");
          } catch (IOException e) {
            LOGGER.error(e.getMessage());
          }
        }
      }
      ```

  * 配置Shiro

    ```java
    @Configuration
    public class ShiroConfig {
      
      @Bean("securityManager")
      public DefaultWebSecurityManager getManager(MyRealm realm) {
        DefaultWebSecurityManager manager = new DefaultWebSecurityManager();
        // 使用自己的realm
        manager.setRealm(realm);
    
        // 关闭shiro自带的session，详情见文档
        // http://shiro.apache.org/session-management.html#SessionManagement-StatelessApplications%28Sessionless%29
        DefaultSubjectDAO subjectDAO = new DefaultSubjectDAO();
        DefaultSessionStorageEvaluator defaultSessionStorageEvaluator = new DefaultSessionStorageEvaluator();
        defaultSessionStorageEvaluator.setSessionStorageEnabled(false);
        subjectDAO.setSessionStorageEvaluator(defaultSessionStorageEvaluator);
        manager.setSubjectDAO(subjectDAO);
        return manager;
      }
      
      @Bean("shiroFilter")
      public ShiroFilterFactoryBean factory(DefaultWebSecurityManager securityManager) {
        ShiroFilterFactoryBean factoryBean = new ShiroFilterFactoryBean();
        // 添加自己的过滤器并且取名为jwt
        Map<String, Filter> filterMap = new HashMap<>();
        filterMap.put("jwt", new JWTFilter());
        factoryBean.setFilters(filterMap);
        factoryBean.setSecurityManager(securityManager);
        factoryBean.setUnauthorizedUrl("/401");
    
        // 自定义url规则
        // http://shiro.apache.org/web.html#urls-
        Map<String, String> filterRuleMap = new HashMap<>();
        // 所有请求通过自己的JWT Filter
        filterRuleMap.put("/**", "jwt");
        // 访问401和404页面不通过自己的Filter
        filterRuleMap.put("/401", "anon");
        factoryBean.setFilterChainDefinitionMap(filterRuleMap);
        return factoryBean;
      }
      
      // 下面的代码是添加注解支持
      @Bean
      @DependsOn("lifecycleBeanPostProcessor")
      public DefaultAdvisorAutoProxyCreator defaultAdvisorAutoProxyCreator() {
        DefaultAdvisorAutoProxyCreator defaultAdvisorAutoProxyCreator = new DefaultAdvisorAutoProxyCreator();
        // 强制使用cglib，防止重复代理和可能引起代理出错的问题
        // https://zhuanlan.zhihu.com/p/29161098
        defaultAdvisorAutoProxyCreator.setProxyTargetClass(true);
        return defaultAdvisorAutoProxyCreator;
      }
    
      @Bean
      public LifecycleBeanPostProcessor lifecycleBeanPostProcessor() {
        return new LifecycleBeanPostProcessor();
      }
    
      @Bean
      public AuthorizationAttributeSourceAdvisor authorizationAttributeSourceAdvisor(DefaultWebSecurityManager securityManager) {
        AuthorizationAttributeSourceAdvisor advisor = new AuthorizationAttributeSourceAdvisor();
        advisor.setSecurityManager(securityManager);
        return advisor;
      }
    }
    ```

## 跨域配置

```java
@Configuration
public class CORSConfiguration {
  @Bean
  public WebMvcConfigurer corsConfigurer() {
    return new WebMvcConfigurer() {
      @Override
      public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
          .allowCredentials(true)
          .allowedHeaders("*")
          .allowedMethods("*")
          .allowedOrigins("*");
      }
    };
  }
}
```

## Cache

* CacheDao接口给上层应用提供缓存支持，CacheDao接口负责和底层的缓存组件打交道，比如Ehcache、Redis、ssdb，甚至自己实现的缓存系统皆可

* 针对CacheDao的默认实现类是EhcacheDao，使用的缓存组件就是Ehcache

  * Ehcahe的开箱即用（直接整合到项目中，不需要部署专门的缓存服务），所以在默认支持Ehcache
  * 想用Redis也很简单，参考EhcacheDao实现一个RedisCacheDao即可

* 到缓存的地方有两个，一个是系统参数的管理，一个是字典管理

  * 系统启动的时候通过CacheListener将数据加载到缓存
    * 通过实现Spring Boot接口CommandLineRunner 来实现项目服务启动的时候就去加载一些数据或做一些事情，@Component，@Order规定运行顺序
  * 具体的功能中使用的时候注入对应的缓存类使用即可
  * 数据更新的时候重新刷新缓存
    * 目前针对全局参数Cfg和字典Dict表的进行更新操作的时候分别调用ConfigCache和DictCache的cache()重新将数据库中的数据加载到缓存中

* 具体用法实现

  ```xml
  <!--ehcache.xml-->
  <?xml version="1.0" encoding="UTF-8"?>
  <ehcache xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
           xsi:noNamespaceSchemaLocation="ehcache.xsd"
           updateCheck="false" monitoring="autodetect"
           dynamicConfig="true">
      <diskStore path="java.io.tmpdir/ehcache"/>
      <defaultCache
              maxElementsInMemory="50000"
              eternal="false"
              timeToIdleSeconds="3600"
              timeToLiveSeconds="3600"
              overflowToDisk="true"
              diskPersistent="false"
              diskExpiryThreadIntervalSeconds="120"
      />
      <!-- 全局变量：永不过期-->
      <cache name="CONSTANT"
             maxElementsInMemory="50000"
             eternal="true"
             clearOnFlush="false"
             overflowToDisk="true"
             diskSpoolBufferSizeMB="1024"
             maxElementsOnDisk="100000"
             diskPersistent="false"
             diskExpiryThreadIntervalSeconds="120"
             memoryStoreEvictionPolicy="LFU"
             transactionalMode="off">
      </cache>
      <!--SESSION缓存-->
      <cache name="SESSION"
             maxElementsInMemory="50000"
             timeToIdleSeconds="3600"
             timeToLiveSeconds="3600"
             eternal="true"
             clearOnFlush="false"
             overflowToDisk="true"
             diskSpoolBufferSizeMB="1024"
             maxElementsOnDisk="100000"
             diskPersistent="false"
             diskExpiryThreadIntervalSeconds="120"
             memoryStoreEvictionPolicy="LFU"
             transactionalMode="off">
      </cache>
      <!--app数据缓存-->
      <cache name="APPLICATION"
             maxElementsInMemory="50000"
             timeToIdleSeconds="3600"
             timeToLiveSeconds="3600"
             eternal="true"
             clearOnFlush="false"
             overflowToDisk="true"
             diskSpoolBufferSizeMB="1024"
             maxElementsOnDisk="100000"
             diskPersistent="false"
             diskExpiryThreadIntervalSeconds="120"
             memoryStoreEvictionPolicy="LFU"
             transactionalMode="off">
      </cache>
  </ehcache>
  <!--
              maxElementsInMemory="10000" 	//Cache中最多允许保存的数据对象的数量
              external="false" 			//缓存中对象是否为永久的，如果是，超时设置将被忽略，对象从不过期
              timeToLiveSeconds="3600"  	//缓存的存活时间，从开始创建的时间算起
              timeToIdleSeconds="3600"  	//多长时间不访问该缓存，那么ehcache 就会清除该缓存
  
              这两个参数很容易误解，看文档根本没用，我仔细分析了ehcache的代码。结论如下：
              1、timeToLiveSeconds的定义是：以创建时间为基准开始计算的超时时长；
              2、timeToIdleSeconds的定义是：在创建时间和最近访问时间中取出离现在最近的时间作为基准计算的超时时长；
              3、如果仅设置了timeToLiveSeconds，则该对象的超时时间=创建时间+timeToLiveSeconds，假设为A；
              4、如果没设置timeToLiveSeconds，则该对象的超时时间=min(创建时间，最近访问时间)+timeToIdleSeconds，假设为B；
              5、如果两者都设置了，则取出A、B最少的值，即min(A,B)，表示只要有一个超时成立即算超时。
              overflowToDisk="true"    //内存不足时，是否启用磁盘缓存
              diskSpoolBufferSizeMB	//设置DiskStore（磁盘缓存）的缓存区大小。默认是30MB。每个Cache都应该有自己的一个缓冲区
              maxElementsOnDisk		//硬盘最大缓存个数
              diskPersistent			//是否缓存虚拟机重启期数据The default value is false
              diskExpiryThreadIntervalSeconds	//磁盘失效线程运行时间间隔，默认是120秒。
              memoryStoreEvictionPolicy="LRU" //当达到maxElementsInMemory限制时，Ehcache将会根据指定的策略去清理内存。默认策略是LRU（最近最少使用）。你可以设置为FIFO（先进先出）或是LFU（较少使用）。
              clearOnFlush	//内存数量最大时是否清除
              maxEntriesLocalHeap="0"  //堆内存中最大缓存对象数,0没有限制
              maxEntriesLocalDisk="1000" //硬盘最大缓存个数。
          -->
  ```

  ```java
  public interface CacheDao {
      // 设置hash key值
      void hset(Serializable key, Serializable k, Object val);
      // 获取hash key值
      Serializable hget(Serializable key, Serializable k);
      // 获取hash key值
      <T> T hget(Serializable key, Serializable k, Class<T> klass);
      // 设置key值，超时失效
      void set(Serializable key, Object val);
      // 获取key值
      <T> T get(Serializable key, Class<T> klass);
      String get(Serializable key);
      void del(Serializable key);
      void hdel(Serializable key, Serializable k);
  }
  ```

  ```java
  @Component
  public class EhcacheDao implements CacheDao {
      //缓存常量，永不过期
      public static final String CONSTANT = "CONSTANT";
      public static final String SESSION = "SESSION";
      @Resource
      private CacheManager cacheManager;
  
      @Override
      public void hset(Serializable key, Serializable k, Object val) {
          Cache cache = cacheManager.getCache(String.valueOf(key));
          cache.put(k, val);
      }
  
      @Override
      public Serializable hget(Serializable key, Serializable k) {
          Cache cache = cacheManager.getCache(String.valueOf(key));
          return cache.get(k, String.class);
      }
  
      @Override
      public <T> T hget(Serializable key, Serializable k, Class<T> klass) {
          Cache cache = cacheManager.getCache(String.valueOf(key));
          return cache.get(k, klass);
      }
  
      @Override
      public void set(Serializable key, Object val) {
          Cache cache = cacheManager.getCache(CONSTANT);
          cache.put(key, val);
      }
  
      @Override
      public <T> T get(Serializable key, Class<T> klass) {
          return cacheManager.getCache(CONSTANT).get(String.valueOf(key), klass);
      }
  
      @Override
      public String get(Serializable key) {
          return cacheManager.getCache(CONSTANT).get(String.valueOf(key), String.class);
      }
  
      @Override
      public void del(Serializable key) {
          cacheManager.getCache(CONSTANT).put(String.valueOf(key), null);
      }
  
      @Override
      public void hdel(Serializable key, Serializable k) {
          cacheManager.getCache(String.valueOf(key)).put(String.valueOf(k), null);
      }
  }
  ```

  ```java
  // 具体使用，用户登录时，生成的Token与用户ID的对应关系
  @Service
  public class TokenCache {
  
      @Autowired
      private EhcacheDao ehcacheDao;
  
      public void put(String token, Long idUser) {
          ehcacheDao.hset(EhcacheDao.SESSION, token, idUser);
      }
  
      public Long get(String token) {
          return ehcacheDao.hget(EhcacheDao.SESSION, token, Long.class);
      }
  
      public Long getIdUser() {
          return ehcacheDao.hget(EhcacheDao.SESSION, HttpKit.getToken(), Long.class);
      }
  
      public void remove(String token) {
          ehcacheDao.hdel(EhcacheDao.SESSION, token + "user");
      }
  
      public void setUser(String token, ShiroUser shiroUser) {
          ehcacheDao.hset(EhcacheDao.SESSION, token + "user", shiroUser);
      }
  
      public ShiroUser getUser(String token) {
          return ehcacheDao.hget(EhcacheDao.SESSION, token + "user", ShiroUser.class);
      }
  }
  ```

  ```java
  @Component
  public class CacheListener implements CommandLineRunner {
  
      @Autowired
      private ConfigCache configCache;
      @Autowired
      private DictCache dictCache;
      private Logger logger = LoggerFactory.getLogger(CacheListener.class);
  
      public void loadCache() {
          configCache.cache();  // 查出所有配置，存入缓存中
          dictCache.cache();
      }
  
      @Override
      public void run(String... strings) throws Exception {
          logger.info(".....................load cache........................");
          Thread thread = new Thread(new Runnable() {
              @Override
              public void run() {
                  loadCache();
              }
          });
          thread.start();
      }
  }
  ```

## 定时任务

* **job**

  * 接口，只有一个方法void execute(JobExecutionContext context)，实现该接口定义运行任务，JobExecutionContext类提供了调度上下文的各种信息。Job运行时的信息保存在 JobDataMap实例中

* **JobDetail**

  * Quartz在每次执行Job时，都重新创建一个Job实例，所以它不直接接受一个Job的实例，相反它接收一个Job实现 类，以便运行时通过newInstance()的反射机制实例化Job
  * 因此需要通过一个类来描述Job的实现类及其它相关的静态信息，如Job名字、描述、关联监听器等信息，JobDetail承担了这一角色
  * 通过该类的构造函数可以更具体地了解它的功用：JobDetail(java.lang.String name, java.lang.String group,java.lang.Class jobClass)，该构造函数要求指定Job的实现类，以及任务在Scheduler中的组名和Job名称

* **Scheduler**

  * 调度器，代表一个Quartz的独立运行容器，Trigger和JobDetail可以注册到Scheduler中，两者在Scheduler中拥有各自的组及名称，组及名称是Scheduler查找定位容器中某一对象的依据，Trigger的组及名称必须唯一，JobDetail的组和名称也必须唯一（但可以和Trigger的组和名称相同，因为它们是不同类型的）
  * Scheduler定义了多个接口方法，允许外部通过组及名称访问和控制容器中Trigger和JobDetail
    * Scheduler可以将Trigger绑定到某一JobDetail中，这样当Trigger触发时，对应的Job就被执行
    * 一个Job可以对应多个Trigger，但一个Trigger只能对应一个Job
  * 可以通过SchedulerFactory创建一个Scheduler实例。Scheduler拥有一个SchedulerContext，它类似于ServletContext，保存着Scheduler上下文信息，Job和Trigger都可以访问SchedulerContext内的信息。SchedulerContext内部通过一个Map，以键值对的方式维护这些上下文数据，SchedulerContext为保存和获取数据提供了多个put()和getXxx()的方法。可以通过Scheduler# getContext()获取对应的SchedulerContext实例
  * 调度器负责管理Quartz应用运行时环境
    * 调度器不是靠自己做所有的工作，而是依赖框架内一些非常重要的部件
    * Quartz不仅仅是线程和线程管理。为确保可伸缩性，Quartz采用了基于多线程的架构
    * 启动时，框架初始化一套worker线程，这套线程被调度器用来执行预定的作业
    * Quartz依赖一套松耦合的线程池管理部件来管理线程环境

* **Trigger**

  * 是一个类，描述触发Job执行的时间触发规则
  * Quartz 中五种类型的Trigger：SimpleTrigger，CronTirgger，DateIntervalTrigger，NthIncludedDayTrigger和Calendar 类（ org.quartz.Calendar）
    * 最常用的是SimpleTrigger和CronTrigger这两个子类
    * 当仅需触发一次或者以固定时间间隔周期执行，SimpleTrigger是最适合的选择
    * CronTrigger则可以通过Cron表达式定义出各种复杂时间规 则的调度方案

* **ThreadPool**

  * Scheduler使用一个线程池作为任务运行的基础设施，任务通过共享线程池中的线程提高运行效率
    * `QuartzSchedulerThread`：负责执行向QuartzScheduler注册的触发Trigger的工作的线程

  ![image-20190827155151896](/Users/dingyuanjie/Documents/study/github/woodyprogram/img/image-20190827155151896.png)

* 场景

  * 定时给用户发送一些消息
  * 定时进行一些报表的计算
  * 定时去指定的接口get一些数据
  * 定时降一些报表发送到指定的邮箱

* 需求

  * 需要添加一个定时任务，做一些事情。但是什么时候做要自己配置，而且还想配置一些参数进去，比如想定时给指定的email发送邮件
  * 可以临时禁用一个任务
  * 看定时任务执行的历史日志

* 简单demo

  ```java
  public class QuartzTestDemo {
  	public static void main(String[] args) throws SchedulerException {
  		// 1. 创建Scheduler的工厂(如果未指定配置文件，默认根据jar包中/org/quartz/quartz.properties文件初始化工厂)
  		SchedulerFactory sf = new StdSchedulerFactory();
  		// 2. 从工厂中获取调度器实例
  		Scheduler scheduler = sf.getScheduler();
  		// 1、2 两步可以简写为一步完成，内部实现相同 
  //		Scheduler scheduler = StdSchedulerFactory.getDefaultScheduler();
  		// 3. 创建JobDetail，此处传入自定义的Job类MyJob
  		JobDetail jobDetail = JobBuilder.newJob(MyJob.class)
  				.withDescription("job_desc")
  				.withIdentity("job_name", "job_group")
  				.build();
  		// 4. 创建Trigger
  		// 4.1 Trigger the job with CronTrigger, and then repeat every 3 seconds
  		Trigger trigger1 = TriggerBuilder.newTrigger()
  				.withSchedule(CronScheduleBuilder.cronSchedule("0/2 * * * * ?")) //两秒执行一次，可以使用SimpleScheduleBuilder或者CronScheduleBuilder
  				.withDescription("cronTrigger_tigger_desc")
  				.withIdentity("trigger_name", "trigger_group")
  				.startNow()
  				.build();
  		// 4.2 Trigger the job with SimpleTrigger, and then repeat every 3 seconds
  		Trigger trigger2 = TriggerBuilder.newTrigger()
  		        .withIdentity("trigger1", "group1")
  		        .withDescription("simpleTrigger_tigger_desc")
  		        .startNow()
  		        .withSchedule(SimpleScheduleBuilder.repeatSecondlyForever(3))
  		        .build();
  		// 5. 注册任务和定时器
  		scheduler.scheduleJob(jobDetail, trigger2);
  		// 6. 启动调度器
  		scheduler.start();
  		System.out.println("启动时间 ： " + new Date());
  	}
  }
  ```

  ```java
  public class MyJob implements Job {
  	@Override
  	public void execute(JobExecutionContext context) throws JobExecutionException {
  		String description = context.getTrigger().getDescription();
  		System.out.println("hello quartz. description:" + description + ", current time:" + new Date());
  	}
  }
  ```

* 表

  * t_sys_task

    ```sql
    CREATE TABLE `t_sys_task` (
      `id` bigint(64) NOT NULL AUTO_INCREMENT COMMENT '自增主键',
      `name` varchar(50) DEFAULT NULL COMMENT '任务名',
      `job_group` varchar(50) DEFAULT NULL COMMENT '任务组',
      `job_class` varchar(255) DEFAULT NULL COMMENT '执行类',
      `note` varchar(255) DEFAULT NULL COMMENT '任务说明',
      `cron` varchar(50) DEFAULT NULL COMMENT '定时规则',
      `data` text COMMENT '执行参数',
      `exec_at` datetime DEFAULT NULL COMMENT '执行时间',
      `exec_result` text COMMENT '执行结果',
      `disabled` tinyint(1) DEFAULT NULL COMMENT '是否禁用',
      `createtime` datetime DEFAULT NULL,
      `creator` bigint(20) DEFAULT NULL,
      `concurrent` tinyint(4) DEFAULT '0' COMMENT '是否允许并发',
      PRIMARY KEY (`id`)
    ) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8; 
    ```

  * t_sys_task_log

    ```sql
    CREATE TABLE `t_sys_task_log` (
      `id` bigint(64) NOT NULL AUTO_INCREMENT COMMENT '自增主键',
      `name` varchar(50) DEFAULT NULL COMMENT '任务名',
      `exec_at` datetime DEFAULT NULL COMMENT '执行时间',
      `exec_success` int(11) DEFAULT NULL COMMENT '执行结果（成功:1、失败:0)',
      `job_exception` varchar(255) DEFAULT NULL COMMENT '抛出异常',
      `id_task` bigint(20) DEFAULT NULL,
      PRIMARY KEY (`id`)
    ) ENGINE=InnoDB AUTO_INCREMENT=1 DEFAULT CHARSET=utf8; 
    ```

  * 配置定时任务

    ```java
    @Configuration
    public class QuartzConfigration {
        @Bean(name = "scheduler")
        public SchedulerFactoryBean schedulerFactory() {
            //  创建Scheduler的工厂(如果未指定配置文件，默认根据jar包中/org/quartz/quartz.properties文件初始化工厂)
            SchedulerFactoryBean bean = new SchedulerFactoryBean();
            return bean;
        }
    }
    ```

  * 逻辑

    ```java
    //QuartzJob
    private String jobId;
    private String jobName;
    private String jobGroup;
    private String jobClass;
    private String description;
    private String cronExpression;
    private boolean concurrent;
    private String jobStatus;
    private Date nextTime;
    private Date previousTime;
    private boolean disabled;
    private Map<String, Object> dataMap;
    ```

    ```java
    @Component
    public abstract class JobExecuter {
      // 设置QuartzJob属性
      private QuartzJob job;
    	public void setJob(QuartzJob job) { this.job = job; }
      public void execute() {
        // 根据job的jobName从数据库中查找任务Task（taskId）
        // 执行任务
        	execute(dataMap);
        // 保存执行日志TaskLog
        // 更新定时任务
      }
      // @param dataMap 数据库配置的参数
    	public abstract void execute(Map<String, Object> dataMap) throws Exception;
    }
    ```

    ```java
    // 自定义定时任务
    @Component
    public class HelloJob extends JobExecuter{
      @Override
      public void execute(Map<String, Object> dataMap){
    		// 业务逻辑
      }
    }
    ```

    ```java
    public class TaskUtils {
      // 通过反射调用job中定义的方法
      public static void executeJob(QuartzJob job){
        // 获取定时任务Class对象（例如HelloJob，@Component及extends JobExecuter）
        	clazz = Class.forName(job.getJobClass());
        // 获取定时任务对象
        	jobExecuter = (JobExecuter) SpringContextHolder.getBean(clazz);
        // 调用定时任务的执行方法 
        	jobExecuter.execute();
      }
      // 判断cron时间表达式正确性 框架里CronTriggerImpl
    }
    ```

    ```java
    // 任务服务
    @Service
    public class JobService {
      @Autowired
    	private Scheduler scheduler;
      
      // 添加任务
      public boolean addJob(QuartzJob job){
        // 任务名称和任务组设置规则：    // 名称：task_1 ..    // 组 ：group_1 ..
    		TriggerKey triggerKey = TriggerKey.triggerKey(job.getJobName(), job.getJobGroup());
    		CronTrigger trigger = (CronTrigger) scheduler.getTrigger(triggerKey);
        if(null == trigger} {
          // Job
          Class<? extends Job> clazz = BaseJob.class;
        	// 设置JobDetail
        	JobDetail jobDetail = JobBuilder.newJob(clazz).withIdentity(job.getJobName(), job.getJobGroup()).build();
    			jobDetail.getJobDataMap().put("job", job);
    			// 表达式调度构建器
    			CronScheduleBuilder scheduleBuilder = CronScheduleBuilder.cronSchedule(job.getCronExpression());
    			// 按新的表达式构建一个新的trigger
    			trigger = TriggerBuilder.newTrigger().withIdentity(triggerKey).withSchedule(scheduleBuilder).build();
    		scheduler.scheduleJob(jobDetail, trigger);
        } else {
          CronScheduleBuilder scheduleBuilder = CronScheduleBuilder.cronSchedule(job.getCronExpression());
    			// 按新的cronExpression表达式重新构建trigger
    			trigger = trigger.getTriggerBuilder().withIdentity(triggerKey).withSchedule(scheduleBuilder).build();
    			// 按新的trigger重新设置job执行
    			scheduler.rescheduleJob(triggerKey, trigger);
        }
        return true;
      }
      
      // 获取单个任务
      public QuartzJob getJob(String jobName, String jobGroup){
        TriggerKey triggerKey = TriggerKey.triggerKey(jobName, jobGroup);
    		Trigger trigger = scheduler.getTrigger(triggerKey);
        Trigger.TriggerState triggerState = scheduler.getTriggerState(trigger.getKey());
        // 设置 QuartzJob job 并返回
        ...
        job.setJobStatus(triggerState.name()); 
      }
      
      // 删除任务
      public boolean deleteJob(QuartzJob job){
        JobKey jobKey = JobKey.jobKey(job.getJobName(), job.getJobGroup());
        scheduler.deleteJob(jobKey);
    		return true;
      }
           
      // 根据Task获取QuartzJob 
    	public QuartzJob getJob(Task task){
    		// 根据Task构造QuartzJob返回
    	}
    }
    ```

    ```java
    @Component
    public class BaseJob implements Job {
        @Override
        public void execute(JobExecutionContext context) throws JobExecutionException {
            JobDataMap data = context.getJobDetail().getJobDataMap();
            QuartzJob job = (QuartzJob) data.get("job");
            try {
                TaskUtils.executeJob(job);
            } catch (Exception e) {
                throw new JobExecutionException(e);
            }
        }
    }
    ```

    ```java
    // 任务计划服务
    @Service
    public class TaskService{
      @Autowired
    	private JobService jobService;
      
      // 新增定时任务
      public Task save(Task task){
        // 保存task到数据库
        // 任务服务添加任务  jobService.getJob(task) 将task转为QuartzJob
        jobService.addJob(jobService.getJob(task));
      }
      
      // 更新定时任务
      public Task update(Task task){
        // 先删除再保存task到数据库
        // 先查出定时任务进行删除，然后新增
        QuartzJob job = jobService.getJob(task.getId().toString(), task.getJobGroup());
        if (job != null) {
          jobService.deleteJob(job);
        }
        jobService.addJob(jobService.getJob(task));
      }
      
      // 禁用定时任务
      public Task disable(Long id){
        // 根据的id将task查找出来，设置distable为true，再保存到数据库
        // 删除定时任务
      }
      
      // 启动定时任务
      public Task enable(Long id){
        // 根据的id将task查找出来，设置distable为false，再保存到数据库
        // 查找定时任务，存在则删除
        // 新增定时任务
      }
      
      // 删除定时任务
      public void delete(Long id){
        // 表中删除task
        // 查找并删除定时任务
      }
    }
    ```

    

  



