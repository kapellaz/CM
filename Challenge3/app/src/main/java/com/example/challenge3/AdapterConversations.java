package com.example.challenge3;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import java.util.ArrayList;

public class AdapterConversations extends ArrayAdapter<String> {


        private Context context;
        private ArrayList<String> conversations;

        private DatabaseHelper databaseHelper;
        private String username;

        public AdapterConversations(Context context, ArrayList<String> conversations, String username, DatabaseHelper databaseHelper) {
            super(context, android.R.layout.simple_list_item_1, conversations);
            this.context = context;
            this.conversations = conversations;
            this.username = username;
            this.databaseHelper = databaseHelper;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            View view = convertView;
            if (view == null) {
                view = LayoutInflater.from(context).inflate(android.R.layout.simple_list_item_1, parent, false);
            }


            String contactName = getItem(position);


            Message lastMessage = databaseHelper.getLastMessage(contactName, username);


            boolean isUnread;
            if (lastMessage.getUserSend().equals(username)) {

                isUnread = false;
            } else {
                isUnread = lastMessage.getIsRead() != 1;
            }


            if (isUnread) {
                view.setBackgroundColor(context.getResources().getColor(R.color.light_blue)); // Cor para não lido
            } else {
                view.setBackgroundColor(context.getResources().getColor(R.color.white));
            }


            TextView textView = view.findViewById(android.R.id.text1);
            textView.setText(contactName);

            return view;
        }


    }


