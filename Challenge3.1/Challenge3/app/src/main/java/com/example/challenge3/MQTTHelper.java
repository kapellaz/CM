package com.example.challenge3;

import android.content.Context;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

import java.text.SimpleDateFormat;
import java.util.Date;


public class MQTTHelper {
    public MqttAndroidClient mqttAndroidClient;
    private DatabaseHelper databaseHelper;
    public MqttClient client;

    //final String server = "tcp://2.80.198.184:1883";
    final String server = "tcp://test.mosquitto.org:1883";

    final String TAG = "MQTT";



    public void connect(String brokerUrl, String clientId, String username, Context context, MqttCallbackExtended mqttCallbackExtended) {
        try {
            // Set up the persistence layer
            MemoryPersistence persistence = new MemoryPersistence();

            // Initialize the MQTT client
            client = new MqttClient(brokerUrl, clientId, persistence);
            databaseHelper = new DatabaseHelper(context);
            client.setCallback( mqttCallbackExtended);


            // Set up the connection options
            MqttConnectOptions connectOptions = new MqttConnectOptions();
            connectOptions.setCleanSession(true);

            // Connect to the broker
            client.connect(connectOptions);
            System.out.println("Connected to: " + brokerUrl);
        } catch (MqttException e) {
            System.out.println("Some error");
            e.printStackTrace();
        }
    }

    private String getCurrentTime() {
        java.text.SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
        return sdf.format(new Date());
    }

    public void disconnect() {
        try {
            client.disconnect();
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    //function to leave a specific topic
    public void leaveTopic(String topic) {
        try {
            client.unsubscribe(topic);
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }



    public void publish(String topic, String message) {
        try {
            MqttMessage mqttMessage = new MqttMessage(message.getBytes());
            client.publish(topic, mqttMessage);
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    public void subscribe(String topic) {
        try {
            client.subscribe(topic);
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }
}