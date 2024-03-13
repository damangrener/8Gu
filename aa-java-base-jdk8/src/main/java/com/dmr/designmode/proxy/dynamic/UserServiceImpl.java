package com.dmr.designmode.proxy.dynamic;

public class UserServiceImpl implements UserService {
    @Override
    public void save() {
        System.out.println("Save user.");
    }
}