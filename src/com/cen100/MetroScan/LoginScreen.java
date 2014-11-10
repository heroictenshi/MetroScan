package com.cen100.MetroScan;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

public class LoginScreen extends Activity {
    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.loginscreen);
    }

    public void login (View view)
    {
        Intent intent = new Intent(this, MainScreen.class);
        EditText username = (EditText) findViewById(R.id.username);
        EditText password = (EditText) findViewById(R.id.password);

        if (username.getText().toString().equals("test1") && password.getText().toString().equals("test2"))
        {
            startActivity(intent);
        }
        else
        {
            Toast toast = Toast.makeText(getApplicationContext(), "Login Invalid", Toast.LENGTH_SHORT);
            toast.show();
        }
    }
}
