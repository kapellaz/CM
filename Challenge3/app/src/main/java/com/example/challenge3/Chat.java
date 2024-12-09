package com.example.challenge3;

import android.annotation.SuppressLint;
import android.app.NotificationChannel;
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
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

import com.google.android.material.snackbar.Snackbar;

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
    //get client id fromn main activity
    //MainActivity activity = (MainActivity) getActivity();
    private String clientId;
    private String server = "tcp://broker.hivemq.com:1883";
    //final String server = "tcp://test.mosquitto.org:1883";
    private String topic = "qweqweqwqweqweqhh";
    private MQTTHelper mqttHelper;
    private ArrayList<String> conversations = new ArrayList<>();
    private ModelView chatViewModel;


    public Chat() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        chatViewModel = new ViewModelProvider(requireActivity()).get(ModelView.class);

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
        createNotificationChannel();
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
                            System.out.println("ENVIOI");;
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

                            databaseHelper.insertMessage(msg);
                            //send message to arduino if the contact is selected to receive notifications
                            if(databaseHelper.getContactsForArduinoNotification(username).contains(contactName)){
                                System.out.println("Sending message to arduino");
                                String m = contactName + ":" + message.toString();
                                mqttHelper.publish("cmchatteste/arduinooooo", m);
                                System.out.println("ENVIOI");;
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
                            System.out.println("ENVIOI");;
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
                                System.out.println("ENVIOI");;
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

        // Configuração do campo de input e botão de envio
        inputMessage = view.findViewById(R.id.input_message);
        sendButton = view.findViewById(R.id.send_button);

        // Lógica para enviar mensagem
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
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
        return sdf.format(new Date());
    }


    @Override
    public void onDestroy() {
        mqttHelper.disconnect();
        super.onDestroy();

    }
    private void publishMessage(String messageText){
        String topic = "cmchatteste/" + username + "/" + contactName; // Define o tópico para o chat específico
        System.out.println("Publishing message: " + messageText);
        databaseHelper.insertMessage(new Message(username, contactName, messageText, getCurrentTime(),0));
        mqttHelper.publish(topic, messageText);
    }

    private void publishMessageCreate(String messageText){
        //subscribeToTopicCreate();
        System.out.println("Publishing message create: " + messageText);
        String topic = "cmchatteste/create/" + contactName + "/" + username;
        databaseHelper.insertMessage(new Message(username, contactName, messageText, getCurrentTime(),0));
        mqttHelper.publish(topic, messageText);
    }

    private void subscribeToTopicCreate(){
        String chatTopic = "cmchatteste/create/" + contactName + "/" + username; // Tópico do chat específico
        System.out.println("Subscribing to topic " + chatTopic);
        mqttHelper.subscribe(chatTopic);
    }

    private void subscribeToTopic(){
        //String chatTopic = "chat/" + username + "/" + contactName; // Tópico do chat específico
        //String chatTopic2 = "chat/" + contactName + "/" + username; // Tópico do chat específico
        //String chatTopic3 = "chat/create/" + username + "/#"; // Tópico do chat específico
        String chatTopic4 = "cmchatteste/#";
        //System.out.println("Subscribing to topic " + chatTopic);
        //System.out.println("Subscribing to topic " + chatTopic2);
        //System.out.println("Subscribing to topic " + chatTopic3);
        System.out.println("Subscribing to topic " + chatTopic4);
        //mqttHelper.subscribe(chatTopic2);
        //mqttHelper.subscribe(chatTopic);
        //mqttHelper.subscribe(chatTopic3);
        mqttHelper.subscribe(chatTopic4);
    }


    public void Notification(View view){
            Snackbar snackbar = Snackbar.make(view.findViewById(R.id.send_button), "User: Rui send a Message!", Snackbar.LENGTH_LONG);

    // Obter a visualização do Snackbar
            View snackbarView = snackbar.getView();

    // Mudar a posição do Snackbar para o topo
            FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) snackbarView.getLayoutParams();
            params.gravity = Gravity.TOP;
            snackbarView.setLayoutParams(params);

            snackbar.show();

    }




    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            System.out.println("CHANEL CRIARRARA<");
            String channelId = "chat_notifications";
            CharSequence name = "Chat Messages";
            String description = "Notifications for chat messages";
            int importance = NotificationManager.IMPORTANCE_HIGH;

            NotificationChannel channel = new NotificationChannel(channelId, name, importance);
            channel.setDescription(description);

            NotificationManager notificationManager = requireContext().getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }


    private void showNotification(String title,String message) {
        String channelId = "chat_notifications";

        // Construir a notificação
        NotificationCompat.Builder builder = new NotificationCompat.Builder(requireContext(), channelId)
                .setSmallIcon(R.drawable.ic_arrow_back) // Ícone pequeno da notificação
                .setContentTitle(title) // Título da notificação
                .setContentText(message) // Texto da notificação
                .setPriority(NotificationCompat.PRIORITY_HIGH) // Prioridade para mostrar no topo
                .setAutoCancel(true); // Fechar automaticamente ao clicar

        // Obter o NotificationManager para exibir a notificação
        NotificationManager notificationManager = (NotificationManager) requireContext().getSystemService(Context.NOTIFICATION_SERVICE);

        // Exibir a notificação com um ID único
        if (notificationManager != null) {

            notificationManager.notify(1, builder.build());
        } else {
            Log.e("Notification", "NotificationManager is null");
        }
    }
}
