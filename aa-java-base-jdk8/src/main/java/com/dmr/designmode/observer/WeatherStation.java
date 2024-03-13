package com.dmr.designmode.observer;

import java.util.ArrayList;
import java.util.List;

/**
 * @author WTF
 * @date 2024/3/13 13:56
 */
public class WeatherStation implements Subject {
    private List<Observer> observers = new ArrayList<>();
    private int temperature;

    public void setTemperature(int temperature) {
        this.temperature = temperature;
        notifyObservers();
    }

    @Override
    public void addObserver(Observer observer) {
        observers.add(observer);
    }

    @Override
    public void removeObserver(Observer observer) {
        observers.remove(observer);
    }

    @Override
    public void notifyObservers() {
        for (Observer observer : observers) {
            observer.update(temperature);
        }
    }
}
