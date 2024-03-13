package com.dmr.designmode.proxy.static1;

// 代理对象
public class UserServiceProxy implements UserService {
    private UserService target;

    public UserServiceProxy(UserService target) {
        this.target = target;
    }

    @Override
    public void save() {
        System.out.println("Before saving user.");
        target.save();
        System.out.println("After saving user.");
    }
}
