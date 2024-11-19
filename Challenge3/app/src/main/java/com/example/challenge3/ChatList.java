package com.example.challenge3;

import android.app.AlertDialog;
import android.content.DialogInterface;
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
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;


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
                new AlertDialog.Builder(requireContext())
                        .setTitle("Delete Conversation")
                        .setMessage("Are you sure you want to delete this conversation?")
                        .setPositiveButton("Yes", (dialog, which) -> {

                            databaseHelper.deleteConversation(username, (String) listView.getItemAtPosition(position));
                            conversations.remove(position);
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

            }
        });

        return view;
    }

    private void loadMessagesFromDatabase(String username) {

        ArrayList<String> dbMessages = databaseHelper.getContactsWithUser(username);

        System.out.println(dbMessages);
        conversations.addAll(dbMessages);
        System.out.println(conversations);


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
