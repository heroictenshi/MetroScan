package com.cen100.MetroScan;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import java.io.*;
import java.util.ArrayList;

public class LoginScreen extends Activity {

    ArrayList<Account> listOfAccounts = new ArrayList<Account>();
    int numOfAccounts;

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.loginscreen);
        loadAccountDatabase();
    }

    public void login (View view)
    {
        Intent intent = new Intent (this, MainScreen.class);
        EditText username = (EditText) findViewById(R.id.usernameField);
        EditText password = (EditText) findViewById(R.id.passwordField);
        boolean credentialsVerified = false;

        for (int i = 0; i < listOfAccounts.size(); i++)
        {
            if (username.getText().toString().equals (listOfAccounts.get(i).getUsername()) && password.getText().toString().equals(listOfAccounts.get(i).getPassword()))
            {
                credentialsVerified = true;
                Bundle extras = new Bundle();

                extras.putString("EXTRA_USERNAME", listOfAccounts.get(i).getUsername());
                extras.putString("EXTRA_PASSWORD", listOfAccounts.get(i).getPassword());
                extras.putDouble("EXTRA_BALANCE", listOfAccounts.get(i).getBalance());

                intent.putExtras(extras);
                startActivity(intent);
            }
        }
        if (credentialsVerified == false)
        {
            Toast toast = Toast.makeText(getApplicationContext(), "Login Invalid, please try again.", Toast.LENGTH_SHORT);
            toast.show();
        }
    }

    public void loadAccountDatabase ()
    {
        String filename = "accountDatabase.effthisproject";
        File file = new File(getFilesDir(), filename);

        if (file.exists() && !file.isDirectory())
        {
            try
            {
                BufferedReader in = new BufferedReader(new FileReader(file));
                numOfAccounts = Integer.parseInt(in.readLine());

                for (int i = 0; i<numOfAccounts; i++)
                {
                    listOfAccounts.add (new Account(in.readLine(), in.readLine(), Double.parseDouble(in.readLine())));
                }
            }
            catch (Exception e)
            {
                e.printStackTrace();
                createNewAccountDatabase();
                loadAccountDatabase();
            }
        }
        else
        {
            createNewAccountDatabase();
            loadAccountDatabase();
        }
    }

    public void createNewAccountDatabase ()
    {
        listOfAccounts.clear();
        listOfAccounts.add (new Account("test1", "test2", 20));
        numOfAccounts = listOfAccounts.size();

        String filename = "accountDatabase.effthisproject";
        File file = new File(getFilesDir(), filename);

        try
        {
            PrintWriter out = new PrintWriter(new FileWriter(file));
            out.println(listOfAccounts.size());
            out.println(listOfAccounts.get(0).getUsername());
            out.println(listOfAccounts.get(0).getPassword());
            out.println(listOfAccounts.get(0).getBalance());
            out.close();
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }
    }
}
