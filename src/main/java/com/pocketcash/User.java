package com.pocketcash;

public class User {
    private int id;
    private String name;
    private String mobileNumber;
    private String email;
    private String pinCode;
    private double balance; // or double balance
    private String role;

    private String status; // "NOT_REGISTERED", "WRONG_PIN", null = success

    // Constructor for DB users
    public User(int id, String name, String mobileNumber,
                String email, String pinCode,
                int balance, String role) {
        this.id = id;
        this.name = name;
        this.mobileNumber = mobileNumber;
        this.email = email;
        this.pinCode = pinCode;
        this.balance = balance;
        this.role = role;
        this.status = null;
    }

    public User(String status) {
        this.status = status;
    }

    // GETTERS
    public int getId() { return id; }
    public String getName() { return name; }
    public String getMobileNumber() { return mobileNumber; }
    public String getEmail() { return email; }
    public String getPinCode() { return pinCode; }
    public double getBalance() { return balance; }
    public String getRole() { return role; }
    public String getStatus() { return status; }


    public void setBalance(double balance) {
        this.balance = balance;
    }
}
