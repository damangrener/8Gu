package com.dmr.designmode.template;

/**
 * @author WTF
 * @date 2024/3/13 11:21
 */
public class Main {
    public static void main(String[] args) {
        BeverageMaker coffeeMaker=new CoffeeMaker();
        coffeeMaker.makeBeverage();

        System.out.println("-------------------");

        BeverageMaker teaMaker = new TeaMaker();
        teaMaker.makeBeverage();
    }
}
