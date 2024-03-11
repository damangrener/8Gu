package com.dmr.designmode.singleton;

/**
 * @author WTF
 * @date 2024/3/11 13:41
 */
public class DoubleCheckLockSingleton {
   private volatile static DoubleCheckLockSingleton instance;

    public DoubleCheckLockSingleton() {
    }

    public static DoubleCheckLockSingleton getInstance(){
        if (null==instance) {
            synchronized (DoubleCheckLockSingleton.class){
                if (null==instance) {
                    instance = new DoubleCheckLockSingleton();
                }
            }
        }
        return instance;
    }
}
