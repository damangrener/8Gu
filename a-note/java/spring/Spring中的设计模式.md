### IOC和DI
+ IOC是一个原则，以下模式实现了IOC原则
  + 工厂模式，抽象工厂
  + 模板方法
  + 策略
  + DI

Spring IOC容器就像一个工厂一样，当我们需要创建一个对象的时候，只需要配置好配置文件、注解即可，完全不用考虑对象是如何创建的。
IOC容器负责创建对象，将对象连接在一起，配置这些对象。

依赖注入：将实例变量传入到一个对象中去

### 工厂设计模式
Spring通过BeanFactory和ApplicationContext创建bean

+ BeanFactory：
  + 延迟注入，使用到某个bean才会注入
  + 程序启动速度快，比ApplicationContext占内存少
+ ApplicationContext
  + 一次性创建所有的Bean
  + 扩展了BeanFactory
  + 用的多

ApplicationContext的实现类
+ ClassPathXmlApplication
  + 把上下文文件当成类路径资源
+ FileSystemXmlApplication
  + 从文件系统中的XML文件载入上下文定义信息
+ XmlWebApplicationContext
  + 从web系统中的XML文件载入上下文定义信息

### 单例
好处：
  + 对于频繁使用的对象，可以省略创建对象所花费的时间
  + new操作次数减少，对系统内存的使用频率降低，减轻GC压力

Spring中bean默认单例。

Spring通过ConcurrentHashMap实现单例注册表

### 代理设计模式
AOP 面向切面编程，能够将与业务无关，却为业务模块所共同调用的逻辑或责任（事务管理、日志、权限控制等）封装起来，便于减少系统的重复代码，降低模块间的耦合度，
有利于扩展和维护。

Spring AOP基于动态代理。
+ 如果代理的对象实现了某个接口，Spring AOP会使用Jdk Proxy去创建代理对象
+ 没有实现接口的的对象，Spring AOP会使用Cglib生成一个被代理对象的子类作为代理
+ 运行时增强，更简单
AspectJ AOP
+ 编译时增强，基于字节码操作
+ 功能更强，更快

### 模板方法
+ 行为设计模式
+ 定义一个操作中算法的股价，将一些步骤延迟到子类中。
+ 模板方法使得子类可以不改变一个算法的结构即可重定义该算法的某些特定步骤的实现方式
#### 结构
+ 抽象类
  + 定义一个模板方法，该方法定义了算法的骨架，其中某些步骤可以抽象，留给子类实现
+ 具体子类
  + 实现了抽象类中的抽象方法，完成算法的具体步骤