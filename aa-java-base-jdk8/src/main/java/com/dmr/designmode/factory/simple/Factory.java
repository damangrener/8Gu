package com.dmr.designmode.factory.simple;

public class Factory {
    public static Product createProduct(Integer type) {
        switch (type) {
            case 1:
                return new ConcreteProduct1();
            case 2:
                return new ConcreteProduct2();
            default:
                throw new IllegalArgumentException("Invalid product type: " + type);
        }
    }
}
