package com.example.finalchallenge.classes;



import android.content.Context;

import com.example.finalchallenge.DatabaseHelper;

import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;



/**
 * MQTTHelper class provides methods to connect, disconnect, publish, subscribe, and unsubscribe
 * from an MQTT broker.
 */
public class MQTThelper {
    private DatabaseHelper databaseHelper;
    public MqttClient client;

    //final String server = "tcp://2.80.198.184:1883";

    final String TAG = "MQTT";



    /**
     * Connects to the specified MQTT broker.
     *
     * @param brokerUrl The URL of the MQTT broker.
     * @param clientId The client ID to use for the connection.
     * @param username The username for the connection (if required).
     * @param context The application context.
     * @param mqttCallbackExtended The callback to handle MQTT events.
     */
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

    /**
     * Disconnects from the MQTT broker.
     */
    public void disconnect() {
        try {
            client.disconnect();
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    /**
     * Unsubscribes from a specific topic.
     *
     * @param topic The topic to unsubscribe from.
     */
    public void leaveTopic(String topic) {
        try {
            client.unsubscribe(topic);
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }


    /**
     * Unsubscribes from a specific topic.
     *
     * @param topic The topic to unsubscribe from.
     */
    public void publish(String topic, String message) {
        try {
            MqttMessage mqttMessage = new MqttMessage(message.getBytes());
            client.publish(topic, mqttMessage);
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }

    /**
     * Subscribes to a specific topic.
     *
     * @param topic The topic to subscribe to.
     */
    public void subscribe(String topic) {
        try {
            client.subscribe(topic);
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }
}