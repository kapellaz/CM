package com.example.challenge3;

import android.annotation.SuppressLint;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;

import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;

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
        databaseHelper = new DatabaseHelper(requireContext());

        loadMessagesFromDatabase();

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

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
                databaseHelper.insertMessage(message);

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

        ArrayList<Message> dbMessages = databaseHelper.getAllMessages(contactName,username);
        messages.clear();
        messages.addAll(dbMessages);

    }


    private String getCurrentTime() {
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
        return sdf.format(new Date());
    }


}
