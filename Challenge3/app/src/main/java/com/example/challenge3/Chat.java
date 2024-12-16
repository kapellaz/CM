package com.example.challenge3;

import android.annotation.SuppressLint;
import android.app.NotificationManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.Manifest;
import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;


import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Date;


public class Chat extends Fragment {
    
    private ListView chatRecyclerView;
    private ListView listView;
    private ArrayAdapter<Message> adapter;
    private List<String> messageList;
    private EditText inputMessage;
    private ImageButton sendButton;
    private String contactName;
    private String username;
    private ArrayList<Message> messages = new ArrayList<>();
    private DatabaseHelper databaseHelper  ;
    public MqttAndroidClient client;
    private String clientId;
    //private String server = "tcp://broker.hivemq.com:1883";
    final String server = "tcp://test.mosquitto.org:1883";
    private MQTTHelper mqttHelper;
    private ArrayList<String> conversations = new ArrayList<>();

    private ModelView chatViewModel;



    public Chat() {
        // Required empty public constructor
    }

    /**
     * Initializes the fragment and sets up essential components such as the ViewModel and database.
     * Requests notification permissions if needed.
     * Establishes MQTT connection.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        chatViewModel = new ViewModelProvider(requireActivity()).get(ModelView.class);

        //retrieve dos nomes no viewmodel
        username = chatViewModel.getUsername().getValue();
        contactName = chatViewModel.getContactName().getValue();


        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(requireActivity(),
                        new String[]{Manifest.permission.POST_NOTIFICATIONS}, 1);
            }
        }



        clientId = username+"111111";
        databaseHelper = new DatabaseHelper(requireContext());

        mqttHelper = new MQTTHelper();

        connectToMqtt();



    }

    /**
     * Connects to the MQTT broker and sets up message handling callbacks.
     */
    private void connectToMqtt() {
        new Thread(() -> {

            // Set the callback inside the fragment itself
            mqttHelper.connect(server, clientId, username, getContext(), new MqttCallbackExtended() {
                @Override
                public void connectComplete(boolean reconnect, String serverURI) {
                    // Connection successful callback
                    Log.d("MQTT", "Connected to: " + serverURI);
                }

                @Override
                public void connectionLost(Throwable cause) {
                    // Connection lost callback
                    Log.d("MQTT", "Connection lost: " + cause.getMessage());


                }

                @Override
                public void messageArrived(String topic, MqttMessage message) throws Exception {
                    System.out.println("Topic:" + topic);
                    // Handle incoming messages
                    String[] topicParts = topic.split("/");
                    String lastTopicPart = topicParts[topicParts.length - 1];
                    System.out.println("CHAT " + message.toString() + lastTopicPart + topic);
                    ArrayList<String> contacts = databaseHelper.getContactsWithUser(username);
                    if ((lastTopicPart.equals(username) && !topicParts[1].equals("create") && topicParts[topicParts.length - 2].equals(contactName))) {
                        System.out.println("Chat case 1");
                        // Message is for this user
                        Log.d("MQTT", "Message arrived: " + message.toString());
                        Message msg = new Message(contactName, username, message.toString(), getCurrentTime(), 0);
                        // Insert message into database

                        databaseHelper.insertMessage(msg);
                        // Update UI
                        requireActivity().runOnUiThread(() -> {
                            messages.add(msg);
                            adapter.notifyDataSetChanged();
                            listView.setSelection(messages.size() - 1);
                        });
                        //send message to arduino if the contact is selected to receive notifications
                        if(databaseHelper.getContactsForArduinoNotification(username).contains(contactName)){
                            System.out.println("Sending message to arduino");
                            String m = contactName + ":" + message.toString();
                            mqttHelper.publish("cmchatteste/arduinooooo", m);

                        }


                    } else if (!topicParts[1].equals("create") && !topicParts[topicParts.length - 2].equals(contactName)) {
                        System.out.println("Chat case 2");
                        String lastTopicParts = topicParts[topicParts.length - 1];
                        String contactName = topicParts[topicParts.length - 2];
                        if (lastTopicParts.equals(username)) {
                            Log.d("MQTT", "Message arrived: " + message.toString());
                            Message msg = new Message(contactName, username, message.toString(), getCurrentTime(), 0);
                            // Insert message into database
                            showNotification("New Message!", contactName + " : " + message.toString());
                            System.out.println("Notificações");
                            databaseHelper.insertMessage(msg);
                            //send message to arduino if the contact is selected to receive notifications
                            if(databaseHelper.getContactsForArduinoNotification(username).contains(contactName)){
                                System.out.println("Sending message to arduino");
                                String m = contactName + ":" + message.toString();
                                mqttHelper.publish("cmchatteste/arduinooooo", m);

                            }

                        }
                    }
                    else if ((topicParts[1].equals("create") && contacts.contains(contactName) && !lastTopicPart.equals(username))) {
                        System.out.println("Chat case 4");
                        // Message is for this user
                        Log.d("MQTT", "Message arrived: " + message.toString());
                        Message msg = new Message(contactName, username, message.toString(), getCurrentTime(), 0);
                        // Insert message into database
                        databaseHelper.insertMessage(msg);
                        // Update UI
                        requireActivity().runOnUiThread(() -> {
                            messages.add(msg);
                            adapter.notifyDataSetChanged();
                            listView.setSelection(messages.size() - 1);
                        });
                        //send message to arduino if the contact is selected to receive notifications
                        if(databaseHelper.getContactsForArduinoNotification(username).contains(contactName)){
                            System.out.println("Sending message to arduino");
                            String m = contactName + ":" + message.toString();
                            mqttHelper.publish("cmchatteste/arduinooooo", m);

                        }

                    }
                    else if (topicParts[1].equals("create") && !topicParts[3].equals(username) && topicParts[2].equals(username)) {
                        System.out.println("Chat case 3");
                        String sender = topicParts[3];  //"chat/create/<sender>/<receiver>"
                        String messageContent = message.toString();
                        if (!conversations.contains(sender)) {

                            requireActivity().runOnUiThread(() -> {
                                showNotification("New Conversation!", sender + " : " + message.toString());
                                databaseHelper.insertMessage(new Message(sender, username, messageContent, getCurrentTime(), 0));  // Add to database as well
                            });
                            //send message to arduino if the contact is selected to receive notifications
                            if(databaseHelper.getContactsForArduinoNotification(username).contains(contactName)){
                                System.out.println("Sending message to arduino");
                                String m = contactName + ":" + message.toString();
                                mqttHelper.publish("cmchatteste/arduinooooo", m);

                            }

                        }

                    }
                }

                    @Override
                    public void deliveryComplete(IMqttDeliveryToken token) {
                        // Handle delivery confirmation
                        Log.d("MQTT", "Message delivered: " + token.getMessageId());
                    }
                });

            requireActivity().runOnUiThread(this::subscribeToTopic);
        }).start();
    }



    /**
     * Called when the fragment is first created.
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        viewModelChat = new ViewModelProvider(requireActivity()).get(ViewModelChat.class);
        username = viewModelChat.getUsername();
        contactName = viewModelChat.getContactName();
        loadMessagesFromDatabase();

        System.out.println(username +  "     " + contactName);
        databaseHelper.markMessagesAsRead(username, contactName);


        View view = inflater.inflate(R.layout.fragment_chat, container, false);
        Toolbar toolbar = view.findViewById(R.id.toolbar);

        TextView titleTextView = new TextView(getContext());
        titleTextView.setText(contactName);
        titleTextView.setTextColor(getResources().getColor(android.R.color.black));
        titleTextView.setTextSize(30);


        Toolbar.LayoutParams layoutParams = new Toolbar.LayoutParams(
                Toolbar.LayoutParams.WRAP_CONTENT,
                Toolbar.LayoutParams.MATCH_PARENT);
        layoutParams.gravity = Gravity.CENTER;
        titleTextView.setLayoutParams(layoutParams);

        toolbar.addView(titleTextView);

        toolbar.setNavigationOnClickListener(v -> {((MainActivity) requireActivity()).switchToChatList();});


        listView = view.findViewById(R.id.chat_recycler_view);
        adapter = new ArrayAdapter<Message>(requireContext(), android.R.layout.simple_list_item_2, android.R.id.text1, messages) {
            @NonNull
            @SuppressLint("SetTextI18n")
            @Override
            public View getView(int position, View convertView, @NonNull ViewGroup parent) {
                Message message = getItem(position);


                if (message != null && message.getUserSend().equals(username)) {
                    convertView = LayoutInflater.from(getContext()).inflate(R.layout.send, parent, false);
                } else {
                    convertView = LayoutInflater.from(getContext()).inflate(R.layout.received, parent, false);
                }


                TextView messageText = convertView.findViewById(R.id.message_text);
                TextView messageTime = convertView.findViewById(R.id.message_time);

                messageText.setText(message.getText());
                messageTime.setText(message.getTime());


                return convertView;
            }
        };


        listView.setAdapter(adapter);
        listView.setSelection(messages.size() - 1);



        inputMessage = view.findViewById(R.id.input_message);
        sendButton = view.findViewById(R.id.send_button);

        // Send Message
        sendButton.setOnClickListener(v -> {


            String messageText = inputMessage.getText().toString();
            if (!messageText.isEmpty()) {
                Message message = new Message(username, contactName, messageText, getCurrentTime(),0);
                messages.add(message);
                adapter.notifyDataSetChanged();
                //check if this is the first message to that user
                if(messages.size() == 1){
                    publishMessageCreate(messageText);
                }else{
                    publishMessage(messageText);
                }


                inputMessage.setText("");
                listView.setSelection(messages.size() - 1);
            }
        });

        return view;
    }


    /**
     * Loads messages from the database
     */
    private void loadMessagesFromDatabase() {
        ArrayList<Message> dbMessages = databaseHelper.getAllMessages(contactName, username);
        messages.clear();
        messages.addAll(dbMessages);
    }






    /**
     * This method returns the current date and time in the format dd/MM/yyyy HH:mm:ss.
     */
    private String getCurrentTime() {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
        return sdf.format(new Date());
    }




    /**
     * Disconnects from the MQTT broker when the fragment is destroyed.
     */
    @Override
    public void onDestroy() {

        super.onDestroy();

        if (mqttHelper != null) {
            try {
                mqttHelper.disconnect(); // Desconecta do MQTT
                Log.d("MQTT", "Disconnected from MQTT");
            } catch (Exception e) {
                Log.e("MQTT", "Error during MQTT disconnection: " + e.getMessage());
            }
        }
    }



    /**
     * Publishes a message to a specific MQTT topic and inserts the message into the database.
     *
     * @param messageText The text of the message to be published.
     */
    private void publishMessage(String messageText){
        String topic = "cmchatteste/" + username + "/" + contactName;

        databaseHelper.insertMessage(new Message(username, contactName, messageText, getCurrentTime(),0));
        mqttHelper.publish(topic, messageText);
    }



    /**
     * Publishes a message to a specific MQTT topic and inserts the message into the database.
     *
     * @param messageText The text of the message to be published.
     */
    private void publishMessageCreate(String messageText){
        System.out.println("Publishing message create: " + messageText);
        String topic = "cmchatteste/create/" + contactName + "/" + username;
        databaseHelper.insertMessage(new Message(username, contactName, messageText, getCurrentTime(),0));
        mqttHelper.publish(topic, messageText);
    }


    /**
     * Subscribes to a specific MQTT topic.
     * The topic is set to "cmchatteste/#" which subscribes to all subtopics under "cmchatteste".
     */
    private void subscribeToTopic(){
        //String chatTopic = "chat/" + username + "/" + contactName; // Tópico do chat específico
        //String chatTopic2 = "chat/" + contactName + "/" + username; // Tópico do chat específico
        //String chatTopic3 = "chat/create/" + username + "/#"; // Tópico do chat específico
        String chatTopic4 = "cmchatteste/#";
        System.out.println("Subscribing to topic " + chatTopic4);

        mqttHelper.subscribe(chatTopic4);
        System.out.println("Subscribing to topic chat/arduino");
        mqttHelper.subscribe("chat/arduinooooo");
    }





    /**
     * It displays a notification to the user.
     * @param title - Could be "New Conversation" or "New Message"
     * @param message - The content of message "Sender : Content"
     */
    private void showNotification(String title,String message) {
        String channelId = "chat_notifications";

        // Build the notification
        NotificationCompat.Builder builder = new NotificationCompat.Builder(requireContext(), channelId)
                .setSmallIcon(R.mipmap.ic_launcher_round)
                .setContentTitle(title)
                .setContentText(message)
                .setPriority(NotificationCompat.PRIORITY_HIGH)
                .setAutoCancel(true);


        NotificationManager notificationManager = (NotificationManager) requireContext().getSystemService(Context.NOTIFICATION_SERVICE);
        if (notificationManager != null) {
            notificationManager.notify(1, builder.build());
        } else {
            Log.e("Notification", "NotificationManager is null");
        }
    }
}
