package com.miranda.luis.prueba_xmpp;


import java.io.File;
import java.io.IOException;

import android.app.Activity;
import android.os.AsyncTask;
import android.os.Bundle;

import android.util.Log;
import android.view.View;
import android.widget.Toast;
// Smack libraries
import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.SASLAuthentication;
import org.jivesoftware.smack.SmackConfiguration;
import org.jivesoftware.smack.SmackException.NotConnectedException;
import org.jivesoftware.smack.chat.ChatManager;
import org.jivesoftware.smack.chat.Chat;
import org.jivesoftware.smack.chat.ChatMessageListener;
import org.jivesoftware.smack.AbstractXMPPConnection;
import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.sasl.SASLMechanism;
import org.jivesoftware.smack.sasl.provided.SASLDigestMD5Mechanism;
import org.jivesoftware.smack.sasl.provided.SASLExternalMechanism;
import org.jivesoftware.smack.tcp.XMPPTCPConnection;
import org.jivesoftware.smack.tcp.XMPPTCPConnectionConfiguration;

import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.Random; // required for Random Number Generation

import javax.net.ssl.HttpsURLConnection;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLContextSpi;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManagerFactory;

public class MainActivity extends Activity {
    String msg = "PM1139 : ";





    XMPPTCPConnectionConfiguration configChatIOT ;
    AbstractXMPPConnection conxChatIOT;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        SmackConfiguration.setDefaultPacketReplyTimeout(10000);


        try {

            SSLContext sc = SSLContext.getInstance("TLS");
            SSLContext context = SSLContext.getInstance("TLSv1");

            context.init(null, null, null);


            configChatIOT = XMPPTCPConnectionConfiguration.builder()
                    .setHost("techno-world.net")
                    .setPort(5222)
                    .setServiceName("techno-world.net")
                    .setCustomSSLContext(context)
                    .setUsernameAndPassword("android@techno-world.net", "android")
                    .setDebuggerEnabled(true)
                    .setEnabledSSLProtocols(new String[]{"TLS","SSL"})
                    .setEnabledSSLCiphers(new String[]{"ECDHE-RSA-RC4-SHA", "RC4-SHA"})
                    .setSecurityMode(ConnectionConfiguration.SecurityMode.disabled)
                    .setResource("chatIOT")
                    .setCompressionEnabled(true).build();

            conxChatIOT = new XMPPTCPConnection(configChatIOT);






        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (KeyManagementException e) {
            e.printStackTrace();
        }


    }

    private class pmConnect extends AsyncTask<Void, Void, Void> {
        protected Void doInBackground(Void...dummy) {

            try {
                conxChatIOT.connect();


                SASLMechanism sm = new SASLExternalMechanism();
                SASLAuthentication.registerSASLMechanism(sm.instanceForAuthentication(conxChatIOT));
                SASLAuthentication.blacklistSASLMechanism("SCRAM-SHA-1");
                SmackConfiguration.addDisabledSmackClass("org.jivesoftware.smack.debugger.JulDeb ugger");
                SASLAuthentication.blacklistSASLMechanism("DIGEST-MD5");


                XMPPTCPConnection.setUseStreamManagementDefault(true);


                Log.d(msg, "Connected to " + conxChatIOT.getHost());
                Log.d(msg, "isConnected: " + conxChatIOT.isConnected());
                conxChatIOT.login();
                Log.d(msg, "User " + conxChatIOT.getUser());

            } catch (SmackException | IOException | XMPPException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
                Log.d(msg, "Error :"+ e.getMessage()+"      ");
            }
            return null;
        }
/*
// this piece of code is kept just for the sake of completeness of Async Tasks
	     protected void onProgressUpdate(Void...progress) {
	        // setProgressPercent(progress[0]);
	     }

	     protected void onPostExecute(Void...result) {
	         //showDialog("Downloaded  bytes");
	     }
*/
    }
    private class pmSend extends AsyncTask<Void, Void, Void> {
        protected Void doInBackground(Void...dummy) {
            Random randomGenerator = new Random();
            int pseudoSensorData;
            ChatManager chatmanager = ChatManager.getInstanceFor(conxChatIOT);

            Chat ChatIOT = chatmanager.createChat("luis.flores@techno-world.net", new ChatMessageListener() {
                public void processMessage(Chat chat, Message message) {
                    Log.d(msg, "Received message: " + message);
                }
            });

            for(int i=0 ; i < 4 ; i++)
            {

                try {
                    if (i==0){
                        ChatIOT.sendMessage("Sound 2");
                    } else {
                        // this data needs to be generated from some Android physical sensor
                        // instead of using a random number generator
                        pseudoSensorData = randomGenerator.nextInt(100);
                        String payLoad = "Push " + String.valueOf(pseudoSensorData);
                        ChatIOT.sendMessage(payLoad);
                    }
                } catch (NotConnectedException e1) {
                    // TODO Auto-generated catch block
                    e1.printStackTrace();
                }
                try {
                    Thread.sleep(5000);                 //1000 milliseconds is one second.
                } catch(InterruptedException ex) {
                    Thread.currentThread().interrupt();
                }
            }
            return null;
        }

    }

    //Method to start the Connection
    public void startCon(View view) {
        Toast.makeText(this, "Connecting", Toast.LENGTH_LONG).show();
        new pmConnect ().execute();
    }

    //Method to start the Connection
    public void pushData(View view) {
        Toast.makeText(this, "Push Data", Toast.LENGTH_LONG).show();
        new pmSend ().execute();
    }

    //Method to Quit
    public void stopCon(View View) {
        Toast.makeText(this, "Disconnection", Toast.LENGTH_LONG).show();
        conxChatIOT.disconnect();
    }

}
