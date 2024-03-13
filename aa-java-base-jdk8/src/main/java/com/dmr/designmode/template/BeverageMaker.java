package com.dmr.designmode.template;

// 抽象类
abstract class BeverageMaker {
    // 模板方法
    public final void makeBeverage() {
        boilWater();
        addMaterials();
        stir();
        pourInCup();
    }

    // 具体方法
    public void boilWater() {
        System.out.println("Boiling water");
    }

    // 抽象方法
    public abstract void addMaterials();

    // 抽象方法
    public abstract void stir();

    // 具体方法
    public void pourInCup() {
        System.out.println("Pouring into cup");
    }
}