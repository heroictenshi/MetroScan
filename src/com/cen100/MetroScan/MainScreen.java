package com.cen100.MetroScan;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

/**
 * Created by Jeffry Lien on 17/11/2014.
 */
public class MainScreen extends Activity {
    Account currentAccount;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.mainscreen);

        Intent intent = getIntent();
        Bundle extras = getIntent().getExtras();

        String username = extras.getString("EXTRA_USERNAME");
        String password = extras.getString("EXTRA_PASSWORD");
        double balance = extras.getDouble("EXTRA_BALANCE");

        currentAccount = new Account(username, password, balance);
        updateTextViews();
    }

    public void updateTextViews() {
        TextView currentUser = (TextView) findViewById(R.id.mainUsernameDisplay);
        TextView currentBalance = (TextView) findViewById(R.id.mainBalanceDisplay);
        currentUser.setText("Current User: " + currentAccount.getUsername());
        currentBalance.setText("Current Balance: $" + currentAccount.getBalance());
    }

    public void reloadBalanceButton (View view) {
        String[] list = {"One fare - $3", "Two fares - $6", "Five fares - $15", "Ten fares - $30"};

        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle(R.string.addBalanceDialogTitle).setItems(list, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                if (which == 0) {
                    currentAccount.setBalance(currentAccount.getBalance() + 3);
                } else if (which == 1) {
                    currentAccount.setBalance(currentAccount.getBalance() + 6);
                } else if (which == 2) {
                    currentAccount.setBalance(currentAccount.getBalance() + 15);
                } else {
                    currentAccount.setBalance(currentAccount.getBalance() + 30);
                }
                updateTextViews();
            }
        });
        builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        AlertDialog alert = builder.create();
        alert.show();
    }
}