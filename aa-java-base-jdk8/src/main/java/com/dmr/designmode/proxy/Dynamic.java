package com.dmr.designmode.proxy;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;

/**
 * @author WTF
 * @date 2024/3/12 13:26
 */
public class Dynamic {
    public static void main(String[] args) {
        RealSubject realSubject = new RealSubject();
        DynamicProxyHandler handler = new DynamicProxyHandler(realSubject);

        Subject proxy= (Subject) Proxy.newProxyInstance(
                Subject.class.getClassLoader(),
                new Class[]{Subject.class},
                handler
        );
        proxy.doSomething();
    }
}

class DynamicProxyHandler implements InvocationHandler{

    private final Object target;

    public DynamicProxyHandler(Object target) {
        this.target = target;
    }

    @Override
    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
        System.out.println("Before method invocation.");
        Object invoke = method.invoke(target, args);
        System.out.println("After method invocation.");
        return invoke;
    }
}
