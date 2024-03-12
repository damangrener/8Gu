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