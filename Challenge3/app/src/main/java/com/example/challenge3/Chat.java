package com.example.challenge3;

import android.annotation.SuppressLint;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

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
        System.out.println(messages);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        // Inflando o layout
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


        // Inicializando o ListView e o adaptador
        listView = view.findViewById(R.id.chat_recycler_view);
        adapter = new ArrayAdapter<Message>(requireContext(), android.R.layout.simple_list_item_2, android.R.id.text1, messages) {
            @Override
            public View getView(int position, View convertView, ViewGroup parent) {
                View view = super.getView(position, convertView, parent);
                Message message = (Message) getItem(position);
                if (message != null) {

                    TextView text1 = view.findViewById(android.R.id.text1);
                    TextView text2 = view.findViewById(android.R.id.text2);

                    text1.setText(message.getUserSend() + ": " + message.getText());
                    text2.setText(message.getTime());


                    if (message.getUserSend() != null && message.getUserSend().equals(username)) {
                        text1.setTextColor(getResources().getColor(R.color.blue));  // Cor para as mensagens do usuário
                    } else {
                        text1.setTextColor(getResources().getColor(R.color.green));  // Cor para as mensagens do contato
                    }

                }
                return view;
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
                Message message = new Message(username, contactName, messageText, getCurrentTime());
                messages.add(message);
                databaseHelper.insertMessage(message);
                adapter.notifyDataSetChanged();
                inputMessage.setText(""); // Limpa o campo de texto
                listView.setSelection(messages.size() - 1); // Rolando para a última mensagem
            }
        });

        return view;
    }


    private void loadMessagesFromDatabase() {
        // Carregar mensagens do banco de dados
        ArrayList<Message> dbMessages = databaseHelper.getAllMessages(contactName,username);
       // databaseHelper.deleteAllMessages();
        messages.addAll(dbMessages);

    }


    private String getCurrentTime() {
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss");
        return sdf.format(new Date());
    }


}
