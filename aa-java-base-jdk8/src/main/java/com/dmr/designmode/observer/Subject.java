package com.dmr.designmode.observer;

/**
 * @author WTF
 * @date 2024/3/13 13:54
 */
public interface Subject {
    void addObserver(Observer observer);
    void removeObserver(Observer observer);
    void notifyObservers();
}
