package com.cdevtech.serviceex;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import java.io.Closeable;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;

/**
 * Created by bills on 3/13/2016.
 */


// We use an IntentService to handle long running processes off the UI thread
// Any new requests will wait for the 1st to end and it can't be interrupted

public class FileService extends IntentService {

    // Used to identify when the IntentService finishes
    public static final String TRANSACTION_DONE = "com.cdevtech.TRANSACTION_DONE";

    /*
        The derived class must have a Default constructor and that constructor
        must call super() passing a string for the name component of the
        worker thread name.
     */
    public FileService() {
        super(FileService.class.getName());
    }


    @Override
    protected void onHandleIntent(Intent intent) {
        Log.e("FileService", "Service Started");

        // Get the URL for the file to download
        String passedInUrl = intent.getStringExtra("url");

        downloadFile(passedInUrl);

        Log.e("FileService", "Service Stopped");

        // Broadcast an intent back to MainActivity when the file download is complete
        Intent i = new Intent(TRANSACTION_DONE);
        FileService.this.sendBroadcast(i);
    }

    protected void downloadFile(String theUrl) {
        // The name for the file we will save the data to
        String fileName = "myFile";

        HttpURLConnection urlConnection = null;
        FileOutputStream outputStream = null;
        InputStream inputStream = null;

        try {
            // Create an output stream to write data to the file
            // private to everyone except our app)
            outputStream = openFileOutput(fileName, Context.MODE_PRIVATE);

            // Set the file location
            URL fileUrl = new URL(theUrl);

            // Create a connection we can use to read the data from the URL
            urlConnection = (HttpURLConnection)fileUrl.openConnection();

            // We will use the GET method
            urlConnection.setRequestMethod("GET");

            // Set that we want to allow output for this connection
            urlConnection.setDoOutput(true);

            // Connect to the URL
            urlConnection.connect();

            // Get an input stream for reading the data
            inputStream = urlConnection.getInputStream();

            // Define read buffer
            byte[] buffer = new byte[1024];
            int bufferLen = 0;

            // Read bytes of data from the stream until there is nothing left
            while ((bufferLen = inputStream.read(buffer)) > 0) {
                // Write the data received to our file
                outputStream.write(buffer, 0, bufferLen);
            }
          } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (ProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            // Close our connection to our file
            close(outputStream);

            // Close the inputStream
            close(inputStream);

            // Release the URL connection
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
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
