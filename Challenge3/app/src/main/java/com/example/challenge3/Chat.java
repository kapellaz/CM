package com.example.challenge3;

import android.annotation.SuppressLint;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;

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
import android.widget.Toast;

import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.IMqttActionListener;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.IMqttToken;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Date;
import org.eclipse.paho.android.service.MqttAndroidClient;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.MqttException;



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
    //get client id fromn main activity
    //MainActivity activity = (MainActivity) getActivity();
    private String clientId;
    //private String server = "tcp://broker.hivemq.com:1883";
    final String server = "tcp://test.mosquitto.org:1883";
    private String topic = "qweqweqwqweqweqhh";
    private MQTTHelper mqttHelper;
    private ArrayList<String> conversations = new ArrayList<>();



    public Chat() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        if (getArguments() != null) {
            username = getArguments().getString("username");
            contactName = getArguments().getString("userReceive");
        }

        clientId = username+"111111";
        databaseHelper = new DatabaseHelper(requireContext());

        mqttHelper = new MQTTHelper();

        connectToMqtt();


        loadMessagesFromDatabase();

    }


    private void connectToMqtt() {
        new Thread(() -> {
            // Set the callback inside the fragment itself
            mqttHelper.connect(server, clientId, username, requireContext(), new MqttCallbackExtended() {
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
                    if ((lastTopicPart.equals(username) && !topicParts[1].equals("create") && topicParts[topicParts.length - 2].equals(contactName)) || (topicParts[1].equals("create") && contacts.contains(contactName) && !lastTopicPart.equals(username))) {
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
                    }else if (!topicParts[1].equals("create") && !topicParts[topicParts.length - 2].equals(contactName)) {
                        System.out.println("Chat case 2");
                        String lastTopicParts = topicParts[topicParts.length - 1];
                        String contactName = topicParts[topicParts.length - 2];
                        if (lastTopicParts.equals(username)){
                            Log.d("MQTT", "Message arrived: " + message.toString());
                            Message msg = new Message(contactName, username, message.toString(), getCurrentTime(), 0);
                            // Insert message into database
                            databaseHelper.insertMessage(msg);
                        }
                    }else if(topicParts[1].equals("create") && !topicParts[3].equals(username) && topicParts[2].equals(username)){
                        System.out.println("Chat case 3");
                        String sender = topicParts[3];  //"chat/create/<sender>/<receiver>"
                        String messageContent = message.toString();
                        if (!conversations.contains(sender)) {
                            // If not, add the sender as a new conversation
                            requireActivity().runOnUiThread(() -> {
                                //conversations.add(sender);
                                //adapter.notifyDataSetChanged();
                                databaseHelper.insertMessage(new Message(sender, username, messageContent, getCurrentTime(), 0));  // Add to database as well
                            });
                        }
                    }                }

                @Override
                public void deliveryComplete(IMqttDeliveryToken token) {
                    // Handle delivery confirmation
                    Log.d("MQTT", "Message delivered: " + token.getMessageId());
                }
            });

            requireActivity().runOnUiThread(this::subscribeToTopic);
        }).start();
    }


    private void loadMessagesFromDatabase(String username) {

        ArrayList<String> dbMessages = databaseHelper.getContactsWithUser(username);
        conversations.clear();
        System.out.println(dbMessages);
        conversations.addAll(dbMessages);

        System.out.println(conversations);
        System.out.println(" LOADDINGGGG  ");


    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

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

        toolbar.setNavigationOnClickListener(v -> {((MainActivity) requireActivity()).switchToChatList(username);});


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

        // Configuração do campo de input e botão de envio
        inputMessage = view.findViewById(R.id.input_message);
        sendButton = view.findViewById(R.id.send_button);

        // Lógica para enviar mensagem
        sendButton.setOnClickListener(v -> {
            String messageText = inputMessage.getText().toString();
            if (!messageText.isEmpty()) {
                Message message = new Message(username, contactName, messageText, getCurrentTime(),0);
                messages.add(message);
                //databaseHelper.insertMessage(message);
                //check if this is the first message to that user
                if(messages.size() == 1){
                    publishMessageCreate(messageText);
                }else{
                    publishMessage(messageText);
                }

                adapter.notifyDataSetChanged();
                inputMessage.setText("");
                listView.setSelection(messages.size() - 1);
            }
        });
        System.out.println("SDHADIUSAHIDAHSD ");
        //databaseHelper.markMessagesAsRead(username,contactName);

        return view;
    }


    private void loadMessagesFromDatabase() {
        new Thread(() -> {
            ArrayList<Message> dbMessages = databaseHelper.getAllMessages(contactName, username);
            requireActivity().runOnUiThread(() -> {
                messages.clear();
                messages.addAll(dbMessages);
                adapter.notifyDataSetChanged();
            });
        }).start();
    }







    private String getCurrentTime() {
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
        return sdf.format(new Date());
    }



    @Override
    public void onDestroy() {
        mqttHelper.disconnect();
        super.onDestroy();

    }
    private void publishMessage(String messageText){
        String topic = "chat/" + username + "/" + contactName; // Define o tópico para o chat específico
        System.out.println("Publishing message: " + messageText);
        databaseHelper.insertMessage(new Message(username, contactName, messageText, getCurrentTime(),0));
        mqttHelper.publish(topic, messageText);
        String m = username + ":" + messageText;
        mqttHelper.publish("chat/arduinooooo", m);
        System.out.println("ENVIOI");
    }

    private void publishMessageCreate(String messageText){
        //subscribeToTopicCreate();
        System.out.println("Publishing message create: " + messageText);
        String topic = "chat/create/" + contactName + "/" + username;
        databaseHelper.insertMessage(new Message(username, contactName, messageText, getCurrentTime(),0));
        mqttHelper.publish(topic, messageText);
    }

    private void subscribeToTopicCreate(){
        String chatTopic = "chat/create/" + contactName + "/" + username; // Tópico do chat específico
        System.out.println("Subscribing to topic " + chatTopic);
        mqttHelper.subscribe(chatTopic);
    }

    private void subscribeToTopic(){
        //String chatTopic = "chat/" + username + "/" + contactName; // Tópico do chat específico
        //String chatTopic2 = "chat/" + contactName + "/" + username; // Tópico do chat específico
        //String chatTopic3 = "chat/create/" + username + "/#"; // Tópico do chat específico
        String chatTopic4 = "chat/#";
        //System.out.println("Subscribing to topic " + chatTopic);
        //System.out.println("Subscribing to topic " + chatTopic2);
        //System.out.println("Subscribing to topic " + chatTopic3);
        System.out.println("Subscribing to topic " + chatTopic4);
        //mqttHelper.subscribe(chatTopic2);
        //mqttHelper.subscribe(chatTopic);
        //mqttHelper.subscribe(chatTopic3);
        mqttHelper.subscribe(chatTopic4);
    }
}
