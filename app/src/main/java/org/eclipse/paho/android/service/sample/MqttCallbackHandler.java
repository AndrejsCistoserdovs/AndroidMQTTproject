/*******************************************************************************
 * Copyright (c) 1999, 2014 IBM Corp.
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * and Eclipse Distribution License v1.0 which accompany this distribution. 
 *
 * The Eclipse Public License is available at 
 *    http://www.eclipse.org/legal/epl-v10.html
 * and the Eclipse Distribution License is available at 
 *   http://www.eclipse.org/org/documents/edl-v10.php.
 */
package org.eclipse.paho.android.service.sample;

import org.eclipse.paho.android.service.sample.R;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import android.content.Context;
import android.content.Intent;
import android.os.Environment;
import android.telephony.PhoneStateListener;
import android.telephony.SignalStrength;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.widget.Toast;

import org.eclipse.paho.android.service.sample.Connection.ConnectionStatus;
import java.io.*;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import message.publisher.MessagePublisher;

/**
 * Handles call backs from the MQTT Client
 *
 */
public class MqttCallbackHandler implements MqttCallback {






    // declaring global variables for objects
    OutputStreamWriter osw; // OutputStreamWriter sends out a stream of data
    File file;
    TelephonyManager TelephonManager;   // TelephonyManager provides an access to phone and services
    MyPhoneStateListener MyListener;    // MyPhoneStateListener provides an access to a state of the phone
    int counter;  // counter for counting the message
    boolean status=false;

  /** {@link Context} for the application used to format and import external strings**/
  private Context context;
  /** Client handle to reference the connection that this handler is attached to**/
  private String clientHandle;

    /**
     * Creates an <code>MqttCallbackHandler</code> object
     * @param context The application's context
     * @param clientHandle The handle to a {@link Connection} object
     */
    public MqttCallbackHandler(Context context, String clientHandle)
    {
        this.context = context;
        this.clientHandle = clientHandle;
    }


    // This method creates a text file in the external phone's memory and sets up FileOutputStream and OutputStreamWriter
public void createFile(){
    File sdCard = Environment.getExternalStorageDirectory();    // getting an absolute path of sdCard
    File directory = new File(sdCard.getAbsolutePath() + "/MyFiles");   // setting the path for the file
    directory.mkdirs();
     file = new File(directory, "logs.txt"); // creating a file
    try {
        FileOutputStream fOut = new FileOutputStream(file,true);  // instantiate FileOutputStream
        osw = new OutputStreamWriter(fOut); // instantiate OutputStreamWriter

    }
    catch (IOException e) {
        e.printStackTrace();
    }

}

    // this listener gets the signal strength each time the signal strength changes
   private class MyPhoneStateListener extends PhoneStateListener {
        @Override
        public void onSignalStrengthsChanged(SignalStrength signalStrength) {
            super.onSignalStrengthsChanged(signalStrength);
           String s= String.valueOf(signalStrength.getGsmSignalStrength())+"  "; // convert SignalStrength into String
            try{
            osw.append(s);  // append the String to the existing file to the end of the line
            osw.flush();    // flush the buffer
            }
            catch(IOException e) {
                e.printStackTrace();
            }


        }

    }





  /**
   * @see org.eclipse.paho.client.mqttv3.MqttCallback#connectionLost(java.lang.Throwable)
   */
  @Override
  public void connectionLost(Throwable cause) {
//	  cause.printStackTrace();
    if (cause != null) {
      Connection c = Connections.getInstance(context).getConnection(clientHandle);
      c.addAction("Connection Lost");
      c.changeConnectionStatus(ConnectionStatus.DISCONNECTED);

      //format string to use a notification text
      Object[] args = new Object[2];
      args[0] = c.getId();
      args[1] = c.getHostName();

      String message = context.getString(R.string.connection_lost, args);

      //build intent
      Intent intent = new Intent();
      intent.setClassName(context, "org.eclipse.paho.android.service.sample.ConnectionDetails");
      intent.putExtra("handle", clientHandle);

      //notify the user
      Notify.notifcation(context, message, intent, R.string.notifyTitle_connectionLost);
    }
  }

  /**
   * @see org.eclipse.paho.client.mqttv3.MqttCallback#messageArrived(java.lang.String, org.eclipse.paho.client.mqttv3.MqttMessage)
   */
  // This method gets invoked each time the message arrives
  @Override
  public void messageArrived(String topic, MqttMessage message) throws Exception {

    //Get connection object associated with this object
    Connection c = Connections.getInstance(context).getConnection(clientHandle);

    //create arguments to format message arrived notification string
    String[] args = new String[2];
    args[0] = new String(message.getPayload()); // storing the message in the array of Strings

   if (status) {
       DateFormat dateFormat = new SimpleDateFormat("mm:ss:ms");
       Date date = new Date();
       // appending the current time in the specified format to the existing file to the end of the line
       osw.append(dateFormat.format(date)+"  ");
       osw.flush();
   }
   else {
       this.createFile();
       status=true;     // true implies that the file has been created
       // registering the listener with a phone

       try {
           MyListener   = new MyPhoneStateListener();
           TelephonManager = (TelephonyManager)context.getSystemService(Context.TELEPHONY_SERVICE);
           TelephonManager.listen(MyListener, PhoneStateListener.LISTEN_SIGNAL_STRENGTHS);
       }
       catch (Exception ex) {

           ex.printStackTrace();

       }

   }

      System.out.println(counter);
      counter=counter+1;    // message counter

    args[1] = topic+";qos:"+message.getQos()+";retained:"+message.isRetained();

    //get the string from strings.xml and format
    String messageString = context.getString(R.string.messageRecieved, (Object[]) args);

    //create intent to start activity
    Intent intent = new Intent();
    intent.setClassName(context, "org.eclipse.paho.android.service.sample.ConnectionDetails");
    intent.putExtra("handle", clientHandle);

    //format string args
    Object[] notifyArgs = new String[3];
    notifyArgs[0] = c.getId();
    notifyArgs[1] = new String(message.getPayload());
    notifyArgs[2] = topic;

    //notify the user 
    Notify.notifcation(context, context.getString(R.string.notification, notifyArgs), intent, R.string.notifyTitle);

    //update client history
    c.addAction(messageString);

  }

  /**
   * @see org.eclipse.paho.client.mqttv3.MqttCallback#deliveryComplete(org.eclipse.paho.client.mqttv3.IMqttDeliveryToken)
   */
  @Override
  public void deliveryComplete(IMqttDeliveryToken token) {
    // Do nothing
  }

}
