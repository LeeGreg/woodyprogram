# 权限

# 组织结构

# 审批流程

# 报表

# Excel导入/导出

# 邮件

# 数据对接



* `Network level connection to peer localhost; retrying after delay`、`“com.sun.jersey.api.client.ClientHandlerException : java.net.ConnectException : Connection”`

  * 虽然配置了eureka.client.fetch-registry=false，即禁止自己注册自己，但是eureka server貌似还是会尝试**寻找某一个eureka server**来注册自己，这种行为并没有被停止
  * 解决办法：在eureka server的applictaion.properties中加入自己的地址作为注册地址，虽然并没有实际注册：
    * `eureka.client.serviceUrl.defaultZone=http://localhost:9100/eureka/`

* 不需要登录就能访问

  ```java
  //主类中
  @Bean
  public FilterRegistrationBean testFilterRegistration(){...}
  @Bean
  public FilterRegistrationBean csrfFilterRegistration(){...}
  
  //网关模块端口：8000、服务模块context-path：faw_vk.qak-service
  //http://localhost:8000/faw_vk.qak-service/mgmt/issue/experiencesissue?page=1&size10?startTime=2018-01-21%2012:21:12&endTime=2019-07-12%2012:11:12
  ```

  
