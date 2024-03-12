package com.dmr.designmode.factory.simple;

public class Main {
    public static void main(String[] args) {
        // 通过工厂类创建产品实例
        Product product1 = Factory.createProduct(1);
        product1.doSth();

        Product product2 = Factory.createProduct(2);
        product2.doSth();
    }
}
