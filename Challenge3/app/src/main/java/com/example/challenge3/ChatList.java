package com.example.challenge3;

import android.Manifest;
import android.app.AlertDialog;
import android.app.NotificationManager;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
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
import android.widget.AdapterView;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import java.util.ArrayList;
import java.text.SimpleDateFormat;
import java.util.Date;

public class ChatList extends Fragment {

    private ListView listView;
    private AdapterConversations adapter;
    private ArrayList<String> conversations = new ArrayList<>();
    private DatabaseHelper databaseHelper;
    private String username;
    private String clientId;
    //private String server = "tcp://broker.hivemq.com:1883";
    private ModelView chatViewModel;
    final String server = "tcp://test.mosquitto.org:1883";
    private MQTTHelper mqttHelper;

    public ChatList() {
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
        username = chatViewModel.getUsername().getValue();
        databaseHelper = new DatabaseHelper(requireContext());
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(requireContext(), android.Manifest.permission.POST_NOTIFICATIONS)
                    != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(requireActivity(),
                        new String[]{Manifest.permission.POST_NOTIFICATIONS}, 1);
            }
        }


        mqttHelper = new MQTTHelper();
        clientId = username+"111111";
        connectToMqtt();

    }


    /**
     * Connects to the MQTT broker and sets up message handling callbacks.
     */
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
                    // Handle incoming messages

                    String[] topicParts = topic.split("/");
                    System.out.println("CHAT LIST" + message.toString());
                    ArrayList<String> contacts = databaseHelper.getContactsWithUser(username);
                    if ((!topicParts[1].equals("create") && !topicParts[1].equals("delete")) || (topicParts[1].equals("create") && topicParts[2].equals(username) && contacts.contains(topicParts[3])) ) {
                        System.out.println("ChatList case 1");
                        String lastTopicPart = topicParts[topicParts.length - 1];
                        String contactName = topicParts[topicParts.length - 2];
                        System.out.println(lastTopicPart + " " + username);
                        if (lastTopicPart.equals(username) && !topicParts[1].equals("create")){
                            Log.d("MQTT", "Message arrived: " + message.toString());
                            Message msg = new Message(contactName, username, message.toString(), getCurrentTime(), 0);
                            // Insert message into database
                            databaseHelper.insertMessage(msg);
                            loadMessagesFromDatabase(username);
                            if(databaseHelper.getContactsForArduinoNotification(username).contains(contactName)){
                                System.out.println("Sending message to arduino");
                                String m = contactName + ":" + message.toString();
                                mqttHelper.publish("cmchatteste/arduinooooo", m);
                            }
                            showNotification("New Message!",contactName + " : " + message.toString());
                        }else if (lastTopicPart.contains(username) || contactName.contains(username)){
                            Log.d("MQTT2", "Message arrived: " + message.toString());
                            Message msg = new Message(lastTopicPart, username,message.toString(), getCurrentTime(), 0);
                            // Insert message into database
                            if(databaseHelper.getContactsForArduinoNotification(username).contains(contactName)){
                                System.out.println("Sending message to arduino");
                                String m = contactName + ":" + message.toString();
                                mqttHelper.publish("cmchatteste/arduinooooo", m);
                            }
                            databaseHelper.insertMessage(msg);
                            loadMessagesFromDatabase(username);
                            showNotification("New Message!",lastTopicPart + " : " + message.toString());
                        }

                    }else if (topicParts[1].equals("create") && topicParts[2].equals(username)) {
                        System.out.println("ChatList case 2");
                        String sender = topicParts[3];  //"chat/create/<sender>/<receiver>"
                        String messageContent = message.toString();
                        if (!conversations.contains(sender)) {
                            // If not, add the sender as a new conversation
                            requireActivity().runOnUiThread(() -> {
                                conversations.add(sender);
                                databaseHelper.insertMessage(new Message(sender, username, messageContent, getCurrentTime(), 0));  // Add to database as well
                                loadMessagesFromDatabase(username);
                                adapter.notifyDataSetChanged();
                                if(databaseHelper.getContactsForArduinoNotification(username).contains(sender)){
                                    System.out.println("Sending message to arduino");
                                    String m = sender + ":" + message.toString();
                                    mqttHelper.publish("cmchatteste/arduinooooo", m);
                                }
                                showNotification("New Conversation!",sender + " : " + message.toString());
                            });
                        }

                    }

                    // Notify the adapter
                    requireActivity().runOnUiThread(() -> {
                        adapter.notifyDataSetChanged();
                    });
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
     * Subscribes to the MQTT topic for chat-related messages.
     */
    private void subscribeToTopic(){
        String chatTopic2 = "cmchatteste/#";
        System.out.println("Subscribing to topic " + chatTopic2);
        mqttHelper.subscribe(chatTopic2); // Subscribe to all topics under "cmchatteste"
    }



    /**
     * Called when the fragment is first created.
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_chat_list, container, false);
        Toolbar toolbar = view.findViewById(R.id.toolbar);

        TextView titleTextView = new TextView(getContext());
        titleTextView.setText("Contacts");
        titleTextView.setTextColor(getResources().getColor(android.R.color.black));
        titleTextView.setTextSize(30);

        Toolbar.LayoutParams layoutParams = new Toolbar.LayoutParams(Toolbar.LayoutParams.WRAP_CONTENT, Toolbar.LayoutParams.MATCH_PARENT);
        layoutParams.gravity = Gravity.CENTER;
        titleTextView.setLayoutParams(layoutParams);
        toolbar.addView(titleTextView);

        loadMessagesFromDatabase(username);
        listView = view.findViewById(R.id.list_view);

        adapter = new AdapterConversations(requireContext(), conversations, username, databaseHelper);
        listView.setAdapter(adapter);
        // Enter into a Chat
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                chatViewModel.setContactName((String) listView.getItemAtPosition(position));
                ((MainActivity) requireActivity()).switchToChat();
            }
        });
        // Delete a conversation
        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                String contactToDelete = conversations.get(position); // Contato a ser excluído

                new AlertDialog.Builder(requireContext())
                        .setTitle("Delete Conversation")
                        .setMessage("Are you sure you want to delete this conversation?")
                        .setPositiveButton("Yes", (dialog, which) -> {
                            databaseHelper.deleteConversation(username, contactToDelete);
                            loadMessagesFromDatabase(username);
                            adapter.notifyDataSetChanged();
                            Toast.makeText(requireContext(), "Conversation deleted", Toast.LENGTH_SHORT).show();
                        })
                        .setNegativeButton("No", null)
                        .show();
                return true;
            }
        });

        view.findViewById(R.id.new_conversation_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showNewConversationDialog();

            }
        });

        view.findViewById(R.id.arduino_config_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                System.out.println("ARDUINO CONFIGS");
                ((MainActivity) requireActivity()).switchToArduinoConfigs();

            }
        });

        return view;
    }


    /**
     * This method is responsible for loading messages from the database related to a specific username.
     * @param username - Username logged
     */
    private void loadMessagesFromDatabase(String username) {
        ArrayList<String> dbMessages = databaseHelper.getContactsWithUser(username);
        conversations.clear();
        conversations.addAll(dbMessages);
    }



    /**
     * This method returns the current date and time in the format dd/MM/yyyy HH:mm:ss.
     */
    private String getCurrentTime() {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy HH:mm:ss");
        return sdf.format(new Date());
    }



    /**
     * Displays a dialog to start a new conversation.
     */
    private void showNewConversationDialog() {

        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("New Conversation");

        final EditText input = new EditText(requireContext());
        input.setHint("Enter username");
        builder.setView(input);


        builder.setPositiveButton("Start", (dialog, which) -> {
            String contact = input.getText().toString().trim();
            if (!contact.isEmpty()) { // check if input is empty
                chatViewModel.setContactName(contact);
                ((MainActivity) requireActivity()).switchToChat();
            } else {
                Toast.makeText(requireContext(), "Username cannot be empty", Toast.LENGTH_SHORT).show();
            }
        });
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());
        // Show the dialog
        builder.show();
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