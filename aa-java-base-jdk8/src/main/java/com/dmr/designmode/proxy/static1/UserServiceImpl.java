package com.dmr.designmode.proxy.static1;

// 目标对象
public class UserServiceImpl implements UserService {
    @Override
    public void save() {
        System.out.println("Save user.");
    }
}
