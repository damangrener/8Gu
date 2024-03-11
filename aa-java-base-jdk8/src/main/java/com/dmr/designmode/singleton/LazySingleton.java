package com.dmr.designmode.singleton;

/**
 * @author WTF
 * @date 2024/3/11 13:38
 */
public class LazySingleton {

    private static LazySingleton instance;

    public LazySingleton() {
    }

    public static synchronized LazySingleton getInstance() {
        if (instance == null) {
            instance = new LazySingleton();
        }
        return instance;
    }
}
