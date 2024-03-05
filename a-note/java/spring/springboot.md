# 面试模拟
## 1. 请解释一下 SpringApplication 类的作用和功能。
+ SpringApplication是SB提供的一个核心类，用于管理整个SB的启动过程
+ 创建应用程序上下文、加载配置、执行初始化操作等
## 2. run方法在启动过程中执行了哪些操作
+ 创建引导上下文
+ 配置环境
+ 创建应用程序上下文
+ 准备上下文
+ 刷新上下文
+ 执行初始化器
+ 执行Runner等
# 启动流程
## 启动入口
```java
@SpringBootApplication
public class AbJavaWebApplication {
    public static void main(String[] args) {
        SpringApplication.run(AbJavaWebApplication.class, args);
    }
}
```
点进去发现run分为两步，创建SpringApplication和执行run()
```java
	public static ConfigurableApplicationContext run(Class<?>[] primarySources, String[] args) {
		return new SpringApplication(primarySources).run(args);
	}
```
args用于在启动时传递给应用程序命令行参数，例如设置端口号，指定profile。
+ `java -jar your-application.jar --server.port=8080`
+ `java -jar your-application.jar --spring.profiles.active=dev`

## 创建SpringApplication
```java
	public SpringApplication(ResourceLoader resourceLoader, Class<?>... primarySources) {
		this.resourceLoader = resourceLoader;
		Assert.notNull(primarySources, "PrimarySources must not be null");
		this.primarySources = new LinkedHashSet<>(Arrays.asList(primarySources));
        //推断应用程序的类型
		this.webApplicationType = WebApplicationType.deduceFromClasspath();
        //初始化启动时的注册表初始化器
		this.bootstrapRegistryInitializers = new ArrayList<>(
				getSpringFactoriesInstances(BootstrapRegistryInitializer.class));
        //设置初始化器
		setInitializers((Collection) getSpringFactoriesInstances(ApplicationContextInitializer.class));
        //设置监听器
		setListeners((Collection) getSpringFactoriesInstances(ApplicationListener.class));
		this.mainApplicationClass = deduceMainApplicationClass();
	}
```
### 设置应用类型
+ reactive
+ none
+ servlet

### 设置初始化器
初始化器ApplicationContextInitializer
+ 用于IOC容器刷新之前初始化一些组件
#### loadSpringFactories()
+ loadFactoryName()最终是调用了loadSpringFactories()
+ 用于加载SB启动时所需的一些工厂类实现
+ 根据给定的类加载起和要加载的类，从`META-INF/spring.factories`中加载并实例化这些实现类，并返回他们的集合
  
在SB中，有一个约定，会在`META-INF/spring.factories`中列出了一些接口和抽象类，会在SB启动时被自动加载并
注册到Spring应用程序上下文中。通常用于扩展SB的功能或配置。
  
遍历spring.factories的内容，使用反射实例化。
```properties
# Application Context Factories
org.springframework.boot.ApplicationContextFactory=\
org.springframework.boot.web.reactive.context.AnnotationConfigReactiveWebServerApplicationContext.Factory,\
org.springframework.boot.web.servlet.context.AnnotationConfigServletWebServerApplicationContext.Factory
```

### 设置监听器
+ 监听特定的事件，如IOC容器刷新、容器关闭等
+ 从`META-INF/spring.factories`中获取

```properties
# Run Listeners
org.springframework.boot.SpringApplicationRunListener=\
org.springframework.boot.context.event.EventPublishingRunListener
```

## 执行run方法
1. 获取、启动运行过程监听器
2. 环境构建
3. 创建IOC容器
4. IOC容器的前置处理
5. 刷新容器
6. IOC容器的后置处理
7. 发出结束执行的事件
8. 执行Runners
### 1. 获取、启动运行过程监听器
SpringApplicationRunListener：监听应用程序启动过程
#### 如何获取
+ `SpringApplicationRunListeners listeners = getRunListeners(args);`
+ 还是调用了loadFactoryNames()
```properties
org.springframework.boot.SpringApplicationRunListener=\
org.springframework.boot.context.event.EventPublishingRunListener
```