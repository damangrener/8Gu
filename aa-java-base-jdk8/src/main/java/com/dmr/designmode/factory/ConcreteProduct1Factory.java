package com.dmr.designmode.factory;

import com.dmr.designmode.factory.simple.ConcreteProduct1;
import com.dmr.designmode.factory.simple.Product;

public class ConcreteProduct1Factory implements Factory {

    @Override
    public Product createProduct() {
        return new ConcreteProduct1();
    }
}
