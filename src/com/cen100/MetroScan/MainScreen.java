package com.cen100.MetroScan;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.PendingIntent;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.nfc.NdefMessage;
import android.nfc.NdefRecord;
import android.nfc.NfcAdapter;
import android.nfc.Tag;
import android.nfc.tech.Ndef;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;
import org.w3c.dom.Text;

import java.io.UnsupportedEncodingException;
import java.util.Arrays;

/**
 * Created by Jeffry Lien on 17/11/2014.
 */
public class MainScreen extends Activity {

    Account currentAccount;
    private NfcAdapter nfcAdapt;
    private TextView nfcStatus;
    boolean transferActive = false;

    public static final String MIMETYPE_TEXT_PLAIN = "text/plain";
    public static final String TAG = "NfcTut";

    CountDownTimer timer;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.mainscreen);

        nfcStatus =(TextView) findViewById(R.id.nfcStatusDisplay);

        nfcAdapt = NfcAdapter.getDefaultAdapter(this);
        if (nfcAdapt == null) {
            // device doesn't support nfc
            Toast.makeText(this, "Your device doesn't support NFC.", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }
        if (!nfcAdapt.isEnabled()) {
            nfcStatus.setText("NFC Status: NFC is disabled");
        } else {
            nfcStatus.setText("NFC Status: Ready to scan");
        }

        Bundle extras = getIntent().getExtras();

        String username = extras.getString("EXTRA_USERNAME");
        String password = extras.getString("EXTRA_PASSWORD");
        double balance = extras.getDouble("EXTRA_BALANCE");

        currentAccount = new Account(username, password, balance);

        final TextView timerDisplay = (TextView) findViewById(R.id.timerDisplay);


        timer = new CountDownTimer(7200000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                timerDisplay.setText("Transfer time remaining: " + (int) ((millisUntilFinished / 1000) / 60) / 60 + ":" + (int) ((millisUntilFinished / 1000) / 60) % 60 + ":" + ((millisUntilFinished / 1000) % 60));
            }
            @Override
            public void onFinish()
            {
                timerDisplay.setText("Transfer time Remaining: Transfer expired");
                transferActive = false;
            }
        };
        updateTextViews();


    }

    public void startTimer ()
    {
        if (transferActive == false)
        {
            transferActive = true;
            currentAccount.setBalance(currentAccount.getBalance() - 3);
            updateTextViews();
            timer.start();
        }
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

    @Override
    protected void onResume() {
        super.onResume();
  /*
   * It's important, that the activity is in the foreground (resumed). Otherwise
   * an IllegalStateException is thrown.
   */
        requestForegroundDispatch(this, nfcAdapt);
    }

    @Override
    protected void onPause() {

        //Call this before onPause, to avoid an IllegalArgumentException.
        stopForegroundDispatch(this, nfcAdapt);
        super.onPause();
    }

    @Override
    protected void onNewIntent(Intent intent) {
  /*
   * This method gets called, when a new Intent gets associated with the current activity instance.
   * Instead of creating a new activity, onNewIntent will be called.
   * In our case this method gets called, when the user attaches a Tag to the device.
   */
        handleIntent(intent);
    }

    private void handleIntent(Intent intent) {
        //get action from intent
        String action = intent.getAction();
        //is action matches the NDEF_DISCOVERED
        if (NfcAdapter.ACTION_NDEF_DISCOVERED.equals(action)) {
            //what is the mime type
            String type = intent.getType();
            //is text plain or not
            if (MIMETYPE_TEXT_PLAIN.equals(type)) {
                //create tag instance and retrieve extended data from intent
                Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
                //execute background task
                new NdefReaderBgTask().execute(tag);

            } else {
                Log.d(TAG, "mime type is not text/plain: " + type);
            }
        }
        //is action matches the ACTION_TECH_DISCOVERED
        else if (NfcAdapter.ACTION_TECH_DISCOVERED.equals(action)) {

            // In case we would still use the Tech Discovered Intent
            Tag tag = intent.getParcelableExtra(NfcAdapter.EXTRA_TAG);
            //get the available technologies
            String[] techList = tag.getTechList();
            //get class name
            String searchedTech = Ndef.class.getName();

            for (String tech : techList) {
                //tag matched then execute background task
                if (searchedTech.equals(tech)) {
                    new NdefReaderBgTask().execute(tag);
                    break;
                }
            }
        }
    }

    /**
     * @param act The corresponding {@link Activity} requesting the foreground dispatch.
     * @param adp The {@link NfcAdapter} used for the foreground dispatch.
     */
    public static void requestForegroundDispatch(final Activity act, NfcAdapter adp) {
        //create instance of intent
        final Intent intent = new Intent(act.getApplicationContext(), act.getClass());
        //set flags on top
        intent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        //crate instance of pending intent
        final PendingIntent pendingIntent = PendingIntent.getActivity(act.getApplicationContext(), 0, intent, 0);
        //create intent filters array
        IntentFilter[] filters = new IntentFilter[1];
        //create 2D array of techlist String
        String[][] techList = new String[][]{};

        // Note: This is the same filter as in our manifest.
        filters[0] = new IntentFilter();
        filters[0].addAction(NfcAdapter.ACTION_NDEF_DISCOVERED);
        filters[0].addCategory(Intent.CATEGORY_DEFAULT);
        try {
            //add data type
            filters[0].addDataType(MIMETYPE_TEXT_PLAIN);
        } catch (IntentFilter.MalformedMimeTypeException e) {
            //throw exception on different mime type
            throw new RuntimeException("Check your mime type.");
        }
        //enable foreground dispatch to current activity
        adp.enableForegroundDispatch(act, pendingIntent, filters, techList);
    }

    /**
     * @param act The corresponding {@link //BaseActivity} requesting to stop the foreground dispatch.
     * @param adp The {@link NfcAdapter} used for the foreground dispatch.
     */
    public static void stopForegroundDispatch(final Activity act, NfcAdapter adp) {
        adp.disableForegroundDispatch(act);
    }

/*    @Override
    public void onClick(View v) {
        switch (v.getId())
        {
            case R.id.btncontinue:
                btncontinueClick();
                break;
        }
    }*/

    /**
     * Background task for reading the data. Do not block the UI thread while reading.
     * @author Hamad Shaikh
     *
     */
    private class NdefReaderBgTask extends AsyncTask<Tag, Void, String> {

        @Override
        protected String doInBackground(Tag... params) {
            Tag tag = params[0];

            Ndef ndef = Ndef.get(tag);
            if (ndef == null) {
                // when NDEF is not supported by this Tag.
                return null;
            }
            //Get the NdefMessage that was read from the tag at discovery time.
            NdefMessage ndefMessage = ndef.getCachedNdefMessage();
            //Get the NDEF Records inside this NDEF Message.
            NdefRecord[] records = ndefMessage.getRecords();
            for (NdefRecord ndefRecord : records) {
                if (ndefRecord.getTnf() == NdefRecord.TNF_WELL_KNOWN && Arrays.equals(ndefRecord.getType(), NdefRecord.RTD_TEXT)) {
                    try {
                        return readNDEFRecordText(ndefRecord);
                    } catch (UnsupportedEncodingException e) {
                        Log.e(TAG, "Unsupported Encoding", e);
                    }
                }
            }

            return null;
        }

        private String readNDEFRecordText(NdefRecord record) throws UnsupportedEncodingException {
   /*
    * See NFC forum specification for "Text Record Type Definition" at 3.2.1
    *
    * http://www.nfc-forum.org/specs/
    *
    * bit_7 defines encoding
    * bit_6 reserved for future use, must be 0
    * bit_5..0 length of IANA language code
    */
            // get record pay load variable length
            byte[] payload = record.getPayload();

            // Get the Text Encoding
            String textEncoding = ((payload[0] & 128) == 0) ? "UTF-8" : "UTF-16";

            // Get the Language Code
            int languageCodeLength = payload[0] & 0063;

            // String languageCode = new String(payload, 1, languageCodeLength, "US-ASCII");
            // e.g. "en"

            // Get the Text
            return new String(payload, languageCodeLength + 1, payload.length - languageCodeLength - 1, textEncoding);
        }

        @Override
        protected void onPostExecute(String result) {
            if (result != null) {
                //nfcStatus.setText("Transit Stop # " + result);
                startTimer();
            }
        }
    }
}