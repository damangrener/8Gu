### Spring,Spring MVC,SpringBoot

+ Spring框架是多模块的集合，提供了广泛的功能
    + DI-依赖注入
    + AOP-面向切面编程
    + 事务管理
    + MVC
    + ...

+ Spring MVC
    + 基于Model View Controller设计模式
    + Spring MVC是spring中的一个模块，赋予Spring快速构建MVC架构的Web程序的能力

+ SpringBoot
    + 简化了Spring程序的搭建和部署
    + 约定由于配置
    + 提供了很多开开箱即用的功能
        + 嵌入式Web服务器
        + 健康检查
        + ...

### Spring MVC 
#### 核心组件
+ DispatcherServlet：核心中央处理器
  + 负责接收请求、分发，并给予客户端响应
+ HandlerMapping：处理器映射器
  + 根据URL去匹配查找能处理的Handler
  + 将请求涉及到的拦截器和Handler一起封装
+ HandlerAdapter：处理器适配器
  + 根据HandlerMapping找到的Handler，适配执行对应的Handler
+ Handler：请求处理器
+ ViewResolver：视图解析器
  + 根据Handler返回的逻辑视图，解析并渲染真正的视图，并传递给DispatcherServlet响应客户端

#### 流程说明
1. 客户端发送请求，DispatcherServlet拦截请求
2. DispatcherServlet根据请求信息调用HandlerMapping，HandlerMapping根据URL去匹配能处理的Handler（Controller）
并将请求涉及到的拦截器和Handler一起封装
3. DispatcherServlet调用HandlerAdapter执行Handler
4. Handler处理后，返回一个ModelAndView对象给DispatchServlet，
包括结果数据Model，以及展示这些数据的视图View名称
5. ViewResolver根据逻辑View查找实际的View
6. DispatchServlet把返回的Model传给View
7. 把View返回给浏览器


### 谈谈对IOC的理解

Inversion of Control 控制反转：

+ 一种设计思想
+ 将原本在程序中手动创建对象的控制权，交由Spring框架来管理

#### 为什么叫控制反转

+ 控制：对象创建（实例化、管理）的权力
+ 反转：控制权交给外部环境（Spring框、IOC容器）
  IOC容器实际上就是个Map，存放各种对象

### 什么是AOP

Aspect Oriented Programming 面向切面编程

+ 目的是将横切关注点（如日志记录、十五管理、权限控制、接口限流等）
  从核心业务逻辑中分离出来
+ 通过动态代理、字节码操作等技术，实现代码的复用和解耦，提高代码的可维护性和可扩展性

#### 关键术语

+ 横切关注点cross-cutting concerns：
    + 多个类或对象中的公共行为
+ 切面Aspect：
    + 对横切关注点进行封装的类，一个切面是一个类。
    + 切面可以定义多个通知，用来实现具体的功能
    + `@Aspect`标识
+ 连接点JoinPoint：
    + 方法调用或执行时的某个特定时刻，如方法调用，异常抛出等
+ 通知Advice：切面在某个连接点要执行的操作，有5种类型
    + 前置通知-Before
    + 后置通知-After
    + 返回通知-AfterReturning
    + 异常通知-AfterThrowing
    + 环绕通知-Around：可以控制目标方法的执行过程
+ 切点Pointcut：
    + 一个切点是一个表达式，用来匹配那些连接点需要被切面所增强
    + 可以通过注解，正则表达式，逻辑运算符来定义
+ 织入Weaving：
    + 将切面和目标对象连接起来的过程
    + 编译器织入
      + 在编译阶段，AspectJ的编译器ajc将切面织入到目标类的字节码中，生成增强后的目标类
    + 运行期织入 **Spring使用这种** 
      + 在程序运行时，AOP框架动态的创建代理对象，并在代理对象的方法调用前后织入切面逻辑
    + 类加载时织入

```java

@Aspect //切面类标识
public class LoggingAspect {

    //通知 （切点）
    @Before("execution(* Calculator.*(..))")
    //beforeMethod 连接点
    public void beforeMethod(JoinPoint joinPoint) {
        String methodName = joinPoint.getSignature().getName();
        System.out.println("Method " + methodName + " is about to be called.");
    }
}
```

##### 常见的切点表达式

1. 匹配特定类的所有方法：

```txt
execution(* com.example.service.MyService.*(..))
com.example.service.MyService 类中的所有方法
```
2. 匹配特定包及其子包下的所有方法

```txt
execution(* com.example..*.*(..))
com.example 包及其子包下的所有类的所有方法
```
3. 匹配特定包下的所有方法

```txt
execution(* com.example.service.*.*(..))
com.example.service 包下的所有类的所有方法
```
4. 匹配特定注解标注的方法

```txt
@annotation(org.springframework.web.bind.annotation.GetMapping)
所有使用 @GetMapping 注解的方法
```
5. 匹配特定方法名的方法

```txt
execution(* com.example.service.MyService.get*(..))
com.example.service.MyService 类中以 "get" 开头的所有方法
```
6. 匹配特定返回类型的方法

```txt
execution(String com.example.service.MyService.*(..))
com.example.service.MyService 类中返回类型为 String 的所有方法
```
7. 组合切点 **&&** 
```txt
execution(* com.example.service.*.*(String))
&& @annotation(org.springframework.transaction.annotation.Transactional)
```