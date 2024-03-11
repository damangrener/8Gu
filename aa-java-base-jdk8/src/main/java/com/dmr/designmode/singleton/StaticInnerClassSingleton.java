package com.dmr.designmode.singleton;

/**
 * @author WTF
 * @date 2024/3/11 13:44
 */
public class StaticInnerClassSingleton {

    private StaticInnerClassSingleton(){}

    private static class SingletonHolder{
        private static final StaticInnerClassSingleton instance=new StaticInnerClassSingleton();
    }

    public static StaticInnerClassSingleton getInstance(){
        return SingletonHolder.instance;
    }

}
