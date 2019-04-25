# 概览

* Servlet 容器是如何工作的 

  * 一个 servlet 容器要为一个 servlet 的请求提供服务 
    1. 创建一个 request 对象并填充那些有可能被所引用的 servlet 使用的信息，如参数、头 部、cookies、查询字符串、URI 等等。
    2. 创建一个 response 对象，所引用的 servlet 使用它来给客户端发送响应 
    3.  调用 servlet 的 service 方法，并传入 request 和 response 对象。从 request 对象取值，给 response 写值

* 架构图（tomcat4和tomcat5）

  * 连接器为接收到每一个 HTTP 请求构造一个 request 和 response 对象，然后它把流程传递给容器。容器从连接器接收到 requset 和 response 对象之后调用 servlet 的 service 方法用于响应

* 1. HTTP 服务器 

  * Socket 和 ServerSocket 

* servlet 容器是如何工作的 

  * 处理静态资源和简单的 servlet 请求 
  * 如何创建 request 和 response 对象，然后把它们传递给被请求的 servlet 的 service 方法。在 servlet 容器里边还有一个 servlet，可以从一个 web 浏览器中调用它

* tomcat连接器

* container 模块

*  Lifecycle 接口定义了一个 Catalina 组件的生命周期，并提供了一个优雅的方式，用来把在该组件发生的事件通知其他组件。另外，Lifecycle 接口提供了一个优雅的机制，用于在 Catalina 通过单一的 start/stop 来启动和停止组件

* 日志，该组件是用来记录错误信息和其他信息的

* 加载器(loader)，负责加载 servlet 和一个 web 应用所需的其他类

* 管理器(manager)。这个组件用来管理会话管理中的会话信息，如何把会话对象持久化的

* web 应用程序安全性的限制，用来限制进入某些内容

* 在一个 web 应用中代表一个 servlet 的org.apache.catalina.core.StandardWrapper 类，过滤器(filter)和一个 servlet 的 service 方法是怎样给调用的

* 在一个 web 应用中代表一个 servlet 的org.apache.catalina.core.StandardContext 类，一个 StandardContext 对象是如何给配置的，对于每个传入的 HTTP 请求在它里面会发生什么，是怎样支持自动重新加载的

* 另外两个容器:host 和 engine，两个容器的标准实
  现:org.apache.catalina.core.StandardHost 和 org.apache.catalina.core.StandardEngine

* 服务器为整个 servlet 容器提供了一个优雅的启动和停止机制，而服务为容器和一个或多个连接器提供了一个支架

* 通过 Digester 来配置 web 应用，XML 文件中如何使用它来把节点转换为 Java 对象。然后解释了用来配置一个 StandardContext
  实例的 ContextConfig 对象

*  shutdown 钩子，Tomcat 使用它总能获得一个机会用于 clean-up，而无论用户是怎样停止它的

* 通过批处理文件和 shell 脚本对 Tomcat 进行启动和停止

* 部署工具(deployer)，这个组件是负责部署和安装 web 应用的

* 一个特殊的接口，ContainerServlet，能够让 servlet 访问 Catalina 的内部
  对象。特别是，它讨论了 Manager 应用，可以通过它来部署应用程序

*  JMX 以及 Tomcat 是如何通过为其内部对象创建 MBeans 使得这些对象可管理的