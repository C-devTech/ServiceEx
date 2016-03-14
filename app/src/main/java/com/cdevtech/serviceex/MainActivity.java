package com.cdevtech.serviceex;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;

// IntentService : Uses an intent to start a background service so as not to disturb the UI
// Android Broadcast : Triggers an event that a BroadcastReceiver can act on
// BroadcastReceiver : Acts when a specific broadcast is made

public class MainActivity extends AppCompatActivity {

    TextView downloadedTextView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

        downloadedTextView = (TextView) findViewById(R.id.downloaded_text_view);

        // Make the textView scrollable
        downloadedTextView.setMovementMethod(new ScrollingMovementMethod());

        // Reload the TextView if changing orientation
        if (savedInstanceState != null) {
            String text = savedInstanceState.getString("FILE_TEXT");

            if (text != null && !text.isEmpty()) {
                downloadedTextView.setText(text);
            }
        }

        /*
            An intent filter is an instance of the IntentFilter class. However, since the Android
            system must know about the capabilities of a component before it can launch that
            component, intent filters are generally not set up in Java code, but in the
            application's manifest file (AndroidManifest.xml) as elements. (The one exception would
            be filters for broadcast receivers that are registered dynamically by calling
            Context.registerReceiver(); they are directly created as IntentFilter objects.)
         */

        // Allows use to track when an intent with the id TRANSACTION_DONE is executed
        // We can call for an intent to execute something and then tell use when it finishes
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(FileService.TRANSACTION_DONE);

        // Prepare the main thread to receive a broadcast and act on it
        registerReceiver(downloadReceiver, intentFilter);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        // Save the value in the TextView using the key NOTES
        outState.putString("FILE_TEXT", downloadedTextView.getText().toString());

        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onStop() {
        super.onStop();

        // Unregister the previously registered BroadcastReceiver. All filters that have been
        // registered for this BroadcastReceiver will be removed.
        unregisterReceiver(downloadReceiver);
    }

    public void startFileService(View view) {
        // Create an intent to run the IntentService in the background
        Intent intent = new Intent(this, FileService.class);

        // Pass the URL that the IntentService will download from
        intent.putExtra("url", "https://www.newthinktank.com/wordpress/lotr.txt");

        // Start the intent service
        this.startService(intent);
    }

    // Is alerted when the IntentService broadcasts TRANSACTION_DONE
    private BroadcastReceiver downloadReceiver = new BroadcastReceiver() {
        // Called when the broadcast is received
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.e("FileService", "Service Received");
            showFileContents();
        }
    };

    // Will read our local file and put the text in the TextView
    public void showFileContents() {
        // Will build a String from the local file
        StringBuilder sb;
        BufferedReader bufferedReader = null;

        try {
            // Opens a stream so we can read from out local file
            FileInputStream inputStream = openFileInput("myFile");

            // Get an input stream for reading data. Need to use an InputStreamReader
            // instead of a FileReader to allow us to set the encoding
            InputStreamReader reader = new InputStreamReader(inputStream, "UTF-8");

            // Used to read the data in small bytes to minimize system load
            bufferedReader = new BufferedReader(reader);

            // Read the data in bytes until nothing is left
            sb = new StringBuilder();
            String line;
            while ((line = bufferedReader.readLine()) != null) {
                sb.append(line).append("\n");
            }

            // Put the downloaded text into the TextView
            downloadedTextView.setText(sb.toString());

        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            // Close the bufferedReader which closes the underlying InputStreamReader,
            // closing the InputStreamReader also closes the underlying FileInputStream
            close(bufferedReader);
        }
    }

    // Gracefully close that which is closeable
    public static void close(Closeable c) {
        if (c == null) {
            return;
        }

        try {
            c.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
