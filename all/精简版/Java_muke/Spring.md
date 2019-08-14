![image-20190810182207016](/Users/dingyuanjie/Documents/study/github/woodyprogram/img/image-20190810182207016.png)

![image-20190810182429950](/Users/dingyuanjie/Documents/study/github/woodyprogram/img/image-20190810182429950.png)

* 依赖注入的方式
  * Setter、Interface、Constructor、Annotation
* ![image-20190810195300644](/Users/dingyuanjie/Documents/study/github/woodyprogram/img/image-20190810195300644.png)
* ![image-20190810195359868](/Users/dingyuanjie/Documents/study/github/woodyprogram/img/image-20190810195359868.png)
* ![image-20190810195535801](/Users/dingyuanjie/Documents/study/github/woodyprogram/img/image-20190810195535801.png)
* Spring IOC支持的功能
  * 依赖注入、依赖检查、自动装配、支持集合、指定初始化方法和销毁方法、支持回调方法
* Spring IOC容器的核心接口
  * BeanFactory
  * ApplicationContext
* BeanDefinition
  * 主要用来描述Bean的定义
* BeanDefinitionRegistry
  * 提供向IOC容器注册BeanDefinition对象的方法
* BeanFactory
  * 提供IOC的配置机制
  * 包含Bean的各种定义，便于实例化Bean
  * 建立Bean之间的依赖关系
  * Bean的生命周期控制
* BeanFactory与ApplicationContext的比较
  * BeanFactory是Spring框架的基础设施，面向Spring
  * ApplicationContext面向使用Spring框架的开发者
* ![image-20190810200920363](/Users/dingyuanjie/Documents/study/github/woodyprogram/img/image-20190810200920363.png)
* getBean方法的代码逻辑
  * 转换BeanName、从缓存中加载实例、实例化Bean、检测parentBeanFactory、初始化依赖的Bean、创建Bean
* ![image-20190810202237906](/Users/dingyuanjie/Documents/study/github/woodyprogram/img/image-20190810202237906.png)
* ![image-20190810202253128](/Users/dingyuanjie/Documents/study/github/woodyprogram/img/image-20190810202253128.png)
* ![image-20190810202508079](/Users/dingyuanjie/Documents/study/github/woodyprogram/img/image-20190810202508079.png)
* ![image-20190810202617238](/Users/dingyuanjie/Documents/study/github/woodyprogram/img/image-20190810202617238.png)
* ![image-20190810202702063](/Users/dingyuanjie/Documents/study/github/woodyprogram/img/image-20190810202702063.png)
* ![image-20190810203223341](/Users/dingyuanjie/Documents/study/github/woodyprogram/img/image-20190810203223341.png)
* ![image-20190810203240946](/Users/dingyuanjie/Documents/study/github/woodyprogram/img/image-20190810203240946.png)
* AOP的主要名词概念
  * ![image-20190810203402884](/Users/dingyuanjie/Documents/study/github/woodyprogram/img/image-20190810203402884.png)
* ![image-20190810203436338](/Users/dingyuanjie/Documents/study/github/woodyprogram/img/image-20190810203436338.png)
* ![image-20190810203530533](/Users/dingyuanjie/Documents/study/github/woodyprogram/img/image-20190810203530533.png)
* ![image-20190810203559985](/Users/dingyuanjie/Documents/study/github/woodyprogram/img/image-20190810203559985.png)
* ![image-20190810203828854](/Users/dingyuanjie/Documents/study/github/woodyprogram/img/image-20190810203828854.png)
* ![image-20190810204132670](/Users/dingyuanjie/Documents/study/github/woodyprogram/img/image-20190810204132670.png)
* 