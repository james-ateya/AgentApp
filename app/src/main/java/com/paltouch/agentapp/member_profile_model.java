package com.paltouch.agentapp;

public class member_profile_model {
    private String v_date;
    private String receipt_number;
    private String money_in;
    private String money_out;
    private String balance;

    //Setters
    public void setV_date(String v_date) {
        this.v_date = v_date;
    }

    public void setReceipt_number(String receipt_number) {
        this.receipt_number = receipt_number;
    }

    public void setMoney_in(String money_in) {
        this.money_in = money_in;
    }

    public void setMoney_out(String money_out) {
        this.money_out = money_out;
    }

    public void setBalance(String balance) {
        this.balance = balance;
    }


//Getters
    public String getV_date() { return v_date; }

    public String getReceipt_number() {
        return receipt_number;
    }

    public String getMoney_in() {
        return money_in;
    }

    public String getMoney_out() {
        return money_out;
    }

    public String getBalance() {
        return balance;
    }

}
