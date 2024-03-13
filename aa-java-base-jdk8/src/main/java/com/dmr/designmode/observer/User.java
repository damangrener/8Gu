package com.dmr.designmode.observer;

/**
 * @author WTF
 * @date 2024/3/13 13:57
 */
public class User implements Observer{

    private String name;

    public User(String name) {
        this.name = name;
    }

    @Override
    public void update(int temperature) {
        System.out.println(name + ": Temperature updated to " + temperature);
    }
}
