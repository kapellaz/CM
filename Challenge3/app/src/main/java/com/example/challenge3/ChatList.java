package com.example.challenge3;

import android.os.Bundle;

import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;


import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.text.SimpleDateFormat;
import java.util.Date;

public class ChatList extends Fragment {

    private ListView listView;
    private ArrayAdapter<String> adapter;
    private ArrayList<String> conversations = new ArrayList<>();
    private DatabaseHelper databaseHelper;
    private String username;

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
        loadMessagesFromDatabase(username);
        System.out.println(conversations);



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


        Toolbar.LayoutParams layoutParams = new Toolbar.LayoutParams(
                Toolbar.LayoutParams.WRAP_CONTENT,
                Toolbar.LayoutParams.MATCH_PARENT);
        layoutParams.gravity = Gravity.CENTER;
        titleTextView.setLayoutParams(layoutParams);


        toolbar.addView(titleTextView);



        listView = view.findViewById(R.id.list_view);

        adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_list_item_1, conversations);
        listView.setAdapter(adapter);


        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                ((MainActivity) requireActivity()).switchToChat((String) listView.getItemAtPosition(position),username);

            }
        });

        listView.setOnItemLongClickListener(new AdapterView.OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {

                return true;
            }
        });


        view.findViewById(R.id.new_conversation_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {


            }
        });

        // Configura o botão de configurações do Arduino
        view.findViewById(R.id.arduino_config_button).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

            }
        });

        return view;
    }

    private void loadMessagesFromDatabase(String username) {
        // Carregar mensagens do banco de dados
        ArrayList<String> dbMessages = databaseHelper.getContactsWithUser(username);
        // databaseHelper.deleteAllMessages();
        System.out.println(dbMessages);
        conversations.addAll(dbMessages);
        System.out.println(conversations);

    }





}
