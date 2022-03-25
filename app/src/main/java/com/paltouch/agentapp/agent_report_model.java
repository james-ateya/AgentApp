package com.paltouch.agentapp;

public class agent_report_model {
    private String full_name;
    private String account_name;
    private String amount;

    public void setFull_name(String full_name) {
        this.full_name = full_name;
    }

    public void setAccount_name(String account_name) {
        this.account_name = account_name;
    }

    public void setAmount(String amount) {
        this.amount = amount;
    }

    public String getFull_name() {
        return full_name;
    }

    public String getAccount_name() {
        return account_name;
    }

    public String getAmount() {
        return amount;
    }


}
