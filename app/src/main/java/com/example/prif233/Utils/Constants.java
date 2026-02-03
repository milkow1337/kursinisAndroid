package com.example.prif233.Utils;

public class Constants {
    public static final String HOME_URL = "http://192.168.50.103:8080/";
    public static final String VALIDATE_USER_URL = HOME_URL + "validateUser";
    public static final String GET_ALL_RESTAURANTS_URL = HOME_URL + "allRestaurants";
    public static final String CREATE_BASIC_USER_URL = HOME_URL + "register";
    public static final String CREATE_DRIVER_URL = HOME_URL + "insertDriver"; // Added driver creation URL
    public static final String GET_ORDERS_BY_USER = HOME_URL + "getOrderByUser/";
    public static final String GET_ORDERS_BY_DRIVER = HOME_URL + "orders/driver/";
    public static final String GET_MESSAGES_BY_ORDER = HOME_URL + "getMessagesForOrder/";
    public static final String SEND_MESSAGE = HOME_URL + "sendMessage";
    public static final String GET_RESTAURANT_MENU = HOME_URL + "getMenuRestaurant/";
    public static final String CREATE_ORDER = HOME_URL + "createOrder";
    public static final String GET_AVAILABLE_ORDERS = HOME_URL + "orders/available";
    public static final String ASSIGN_DRIVER = HOME_URL + "orders/";
}