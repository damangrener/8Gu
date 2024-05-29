### spring cloud包含哪些组件
1. Eureka
各个微服务之间需要通信，Eureka就是做服务的注册与发现。
2. Config
集中管理配置文件，dev，sit，pro
3. Hystrix
提高系统容错能力，使用Hystrix进行熔断和降级处理
+ 当商品服务不可用时，订单服务可以返回默认响应
4. Gateway
+ 提供统一的入口和路由管理
+ 所有客户端请求都通过网关路由到后端相应的微服务。
+ 网关实现统一的认证、鉴权、限流。
4.1. 网关时所有客户端请求的入口，网关挂了怎么办？
+ 部署多个网关实例，使用负载均衡如nginx，将客户端请求分发到不同的网关实例。

5. feign
声明式的HTTP客户端，能方便的调用其他微服务的接口，就像调用本地方法一样。
```java
@FeignClient(name = "user-service", fallback = UserServiceFallback.class)
public interface UserServiceClient {

    @GetMapping("/users/{id}")
    User getUserById(@PathVariable("id") Long id);
}

@Component
public class UserServiceFallback implements UserServiceClient {

    @Override
    public User getUserById(Long id) {
        // 返回一个默认的用户信息，作为降级处理
        return new User(id, "Default User");
    }
}

```
6. ribbon
负载均衡和容错。

### eureka和nacos的区别
+ eureka提供服务发现和注册
+ nacos提供服务发现、配置管理和动态dns，支持配置的实时刷新
  + 使用@RefreshScope注解配置变化实时刷新
  + 当服务实例的IP地址或端口号变化是，nacos客户端会自动将新的信息更新到Nacos服务器上，从而市其他服务能够正确的发现并调用该服务。


