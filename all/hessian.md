# Hessian

## 服务端

* 创建服务接口，实现接口并配置成`Bean`
  * 服务`Bean`的`name`以"/"开头，`class`属性为`HessianServiceExporter`，`service`属性为接口实现`Bean`的名称，`serviceInterface`属性为接口的全限定名称
* 配置`Bean`，`BeanNameUrlHandlerMapping`
  * `Spring`会自动对`name`以"/"开始的`Bean`进行Url映射
* `web.xml`中配置`DispatchServlet`，设置映射路径`/api/service/*`

## 客户端

* 添加接口依赖
* 配置服务`Bean`
  * 自定义`id`，`class`属性为`HessianProxyFactoryBean`，`serviceUrl`属性为服务端IP:端口/映射路径/服务端`Bean`的`name`，`serviceInterface`属性值为服务接口的全限定名称
* 通过`@Autowired`注入使用