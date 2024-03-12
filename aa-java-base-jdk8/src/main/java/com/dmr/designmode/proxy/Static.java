package com.dmr.designmode.proxy;

// 接口：定义了真实对象和代理对象的公共接口
interface Subject {
    void doSomething();
}

// 真实对象：需要被代理的对象
class RealSubject implements Subject {
    @Override
    public void doSomething() {
        System.out.println("RealSubject is doing something.");
    }
}

// 代理对象：充当真实对象的代理，控制对真实对象的访问
class Proxy implements Subject {
    private RealSubject realSubject;

    public Proxy(RealSubject realSubject) {
        this.realSubject = realSubject;
    }

    @Override
    public void doSomething() {
        System.out.println("Proxy is doing something before calling the RealSubject.");
        realSubject.doSomething();
        System.out.println("Proxy is doing something after calling the RealSubject.");
    }
}

// 客户端：使用代理对象访问真实对象
public class Static {
    public static void main(String[] args) {
        RealSubject realSubject = new RealSubject();
        Proxy proxy = new Proxy(realSubject);
        proxy.doSomething();
    }
}
