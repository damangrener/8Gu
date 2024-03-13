package com.dmr.designmode.proxy.dynamic;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;

/**
 * @author WTF
 * @date 2024/3/13 10:17
 */
public class UserServiceInvocationHandler implements InvocationHandler {

    private Object target;

    public UserServiceInvocationHandler(Object target) {
        this.target = target;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        System.out.println("Before saving user.");
        Object result = method.invoke(target, args);
        System.out.println("After saving user.");
        return result;
    }
}
