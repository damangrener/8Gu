package com.dmr.designmode.template;

class TeaMaker extends BeverageMaker {
    @Override
    public void addMaterials() {
        System.out.println("Adding tea leaves");
    }

    @Override
    public void stir() {
        System.out.println("Stirring tea");
    }
}