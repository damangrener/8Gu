package com.dmr.designmode.template;

// 具体子类
class CoffeeMaker extends BeverageMaker {
    @Override
    public void addMaterials() {
        System.out.println("Adding coffee");
    }

    @Override
    public void stir() {
        System.out.println("Stirring coffee");
    }
}