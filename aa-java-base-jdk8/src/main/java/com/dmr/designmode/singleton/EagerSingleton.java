package com.dmr.designmode.singleton;

/**
 * @author WTF
 * @date 2024/3/11 13:35
 */
public class EagerSingleton {

    private static final EagerSingleton instance=new EagerSingleton();

    public EagerSingleton() {
    }

    public static EagerSingleton getInstance(){
        return instance;
    }
}
