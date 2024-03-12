package com.dmr.designmode.factory.abstractF;

/**
 * @author WTF
 * @date 2024/3/12 9:05
 */
// 抽象产品接口
interface Button {
    void display();
}

// 具体产品：简约风格按钮
class SimpleButton implements Button {
    @Override
    public void display() {
        System.out.println("Displaying a simple button.");
    }
}

// 具体产品：华丽风格按钮
class FancyButton implements Button {
    @Override
    public void display() {
        System.out.println("Displaying a fancy button.");
    }
}

// 抽象产品接口
interface TextField {
    void display();
}

// 具体产品：简约风格文本框
class SimpleTextField implements TextField {
    @Override
    public void display() {
        System.out.println("Displaying a simple text field.");
    }
}

// 具体产品：华丽风格文本框
class FancyTextField implements TextField {
    @Override
    public void display() {
        System.out.println("Displaying a fancy text field.");
    }
}

// 抽象工厂接口
interface UIFactory {
    Button createButton();
    TextField createTextField();
}

// 具体工厂：简约风格工厂
class SimpleUIFactory implements UIFactory {
    @Override
    public Button createButton() {
        return new SimpleButton();
    }

    @Override
    public TextField createTextField() {
        return new SimpleTextField();
    }
}

// 具体工厂：华丽风格工厂
class FancyUIFactory implements UIFactory {
    @Override
    public Button createButton() {
        return new FancyButton();
    }

    @Override
    public TextField createTextField() {
        return new FancyTextField();
    }
}
