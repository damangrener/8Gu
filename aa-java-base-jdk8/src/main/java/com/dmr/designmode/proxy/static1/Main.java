package com.dmr.designmode.proxy.static1;

// 使用示例
public class Main {
    public static void main(String[] args) {
        UserService userService = new UserServiceImpl();
        UserService userServiceProxy = new UserServiceProxy(userService);
        userServiceProxy.save();
    }
}
