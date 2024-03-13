package com.dmr.designmode.proxy.dynamic;

import java.lang.reflect.Proxy;

/**
 * @author WTF
 * @date 2024/3/13 10:26
 */
public class Main {
    public static void main(String[] args) {
        UserService userService = new UserServiceImpl();
        UserServiceInvocationHandler invocationHandler = new UserServiceInvocationHandler(userService);
        UserService proxy = (UserService) Proxy.newProxyInstance(
                userService.getClass().getClassLoader(),
                userService.getClass().getInterfaces(),
                invocationHandler
        );
        proxy.save();


    }
}
