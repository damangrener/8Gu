package com.dmr.designmode.factory;

/**
 * @author WTF
 * @date 2024/3/11 17:07
 */
//定义产品接口
public interface Product {
    void doSth();
}

// 实现接口的具体产品类
class ConcreteProduct1 implements Product{

    public void doSth() {
        System.out.println("ConcreteProduct1 doSth");
    }
}

// 实现接口的具体产品类
class ConcreteProduct2 implements Product{

    public void doSth() {
        System.out.println("ConcreteProduct2 doSth");
    }
}

class Factory{
    public static Product createProduct(Integer type){
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
class Main{
    public static void main(String[] args) {
        // 通过工厂类创建产品实例
        Product product1 = Factory.createProduct(1);
        product1.doSth();

        Product product2 = Factory.createProduct(2);
        product2.doSth();
    }
}
