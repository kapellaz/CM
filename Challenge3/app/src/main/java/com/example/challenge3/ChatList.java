package com.example.challenge3;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;

import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;


import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallbackExtended;
import org.eclipse.paho.client.mqttv3.MqttMessage;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.text.SimpleDateFormat;
import java.util.Date;

public class ChatList extends Fragment {

    private ListView listView;
    private AdapterConversations adapter;
    private ArrayList<String> conversations = new ArrayList<>();
    private DatabaseHelper databaseHelper;
    private String username;
    //MainActivity activity = (MainActivity) getActivity();
    private String clientId;
    final String server = "tcp://test.mosquitto.org:1883";
    private MQTTHelper mqttHelper;

    public ChatList() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            username = getArguments().getString("username");
        }
        databaseHelper = new DatabaseHelper(requireContext());

        mqttHelper = new MQTTHelper();
        clientId = username+"111111";
        connectToMqtt();

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
                    // Handle incoming messages

                    String[] topicParts = topic.split("/");
                    System.out.println(topic);

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
                        }else{
                            Log.d("MQTT2", "Message arrived: " + message.toString());
                            Message msg = new Message(lastTopicPart, username,message.toString(), getCurrentTime(), 0);
                            // Insert message into database
                            databaseHelper.insertMessage(msg);
                            loadMessagesFromDatabase(username);
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
    private void subscribeToTopic(){
        //String chatTopic = "chat/create/" + username + "/#"; // Tópico do chat específico
        String chatTopic2 = "chat/#"; // Tópico para todos os chats
        //System.out.println("Subscribing to topic " + chatTopic);
        System.out.println("Subscribing to topic " + chatTopic2);
        //mqttHelper.subscribe(chatTopic);
        mqttHelper.subscribe(chatTopic2);
    }



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

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                ((MainActivity) requireActivity()).switchToChat((String) listView.getItemAtPosition(position), username);
            }
        });

        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                String contactToDelete = conversations.get(position); // Contato a ser excluído

                new AlertDialog.Builder(requireContext())
                        .setTitle("Delete Conversation")
                        .setMessage("Are you sure you want to delete this conversation?")
                        .setPositiveButton("Yes", (dialog, which) -> {
                            // 1. Apagar conversa e mensagens no banco de dados
                            databaseHelper.deleteConversation(username, contactToDelete);


                            // 3. Recarregar a lista de conversas
                            loadMessagesFromDatabase(username);
                            adapter.notifyDataSetChanged();

                            // 4. Feedback ao usuário
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
                ((MainActivity) requireActivity()).switchToArduinoConfigs(username);

            }
        });

        return view;
    }

    private void loadMessagesFromDatabase(String username) {

        ArrayList<String> dbMessages = databaseHelper.getContactsWithUser(username);
        conversations.clear();
        System.out.println(dbMessages);
        conversations.addAll(dbMessages);

        System.out.println(conversations);
        System.out.println(" LOADDINGGGG  ");


    }



    private String getCurrentTime() {
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
        return sdf.format(new Date());
    }

    private void showNewConversationDialog() {

        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("New Conversation");

        final EditText input = new EditText(requireContext());
        input.setHint("Enter username");
        builder.setView(input);


        builder.setPositiveButton("Start", (dialog, which) -> {
            String contact = input.getText().toString().trim();
            if (!contact.isEmpty()) {

                ((MainActivity) requireActivity()).switchToChat(contact,username);
            } else {
                Toast.makeText(requireContext(), "Username cannot be empty", Toast.LENGTH_SHORT).show();
            }
        });
        builder.setNegativeButton("Cancel", (dialog, which) -> dialog.cancel());

        // Show the dialog
        builder.show();
    }






}
