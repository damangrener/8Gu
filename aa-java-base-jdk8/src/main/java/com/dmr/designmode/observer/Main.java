package com.dmr.designmode.observer;

/**
 * @author WTF
 * @date 2024/3/13 13:58
 */
public class Main {
    public static void main(String[] args) {
        WeatherStation weatherStation = new WeatherStation();
        User user1 = new User("User 1");
        User user2 = new User("User 2");

        weatherStation.addObserver(user1);
        weatherStation.addObserver(user2);

        weatherStation.setTemperature(25);
        weatherStation.setTemperature(30);
    }
}
