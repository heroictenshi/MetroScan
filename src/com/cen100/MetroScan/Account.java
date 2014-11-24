package com.cen100.MetroScan;

/**
 * Created by Jeffry Lien on 17/11/2014.
 */
public class Account
{

    private String username;
    private String password;
    private double balance;

    public Account (String user, String pass, double bal)
    {
        username = user;
        password = pass;
        balance = bal;
    }

    public String getUsername ()
    {
        return username;
    }

    public String getPassword ()
    {
        return password;
    }

    public double getBalance ()
    {
        return balance;
    }

    public void setUsername (String user)
    {
        username = user;
    }

    public void setPassword (String pass)
    {
        password = pass;
    }

    public void setBalance (double bal)
    {
        balance = bal;
    }
}
