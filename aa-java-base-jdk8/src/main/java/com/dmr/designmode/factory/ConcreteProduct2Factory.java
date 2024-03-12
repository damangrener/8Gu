package com.dmr.designmode.factory;

import com.dmr.designmode.factory.simple.ConcreteProduct2;
import com.dmr.designmode.factory.simple.Product;

public class ConcreteProduct2Factory implements Factory {

    @Override
    public Product createProduct() {
        return new ConcreteProduct2();
    }
}
