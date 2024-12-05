package com.example.challenge3;

import android.os.Bundle;
import android.util.Log;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import org.eclipse.paho.client.mqttv3.MqttClient;

public class MainActivity extends AppCompatActivity {


    private FirstTimeFrag login;
    private static final String TAG_NOTE_EDIT = "NOTE_EDIT";
    private static final String TAG_NOTE_LIST = "NOTE_LIST";
    private String clientId = MqttClient.generateClientId();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        login = new FirstTimeFrag();
        System.out.println("asdkasjdkasd");
        //Chat c = new Chat();
        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.replace(R.id.main, login);
        ft.commit();
    }


    /**switchToChat
     * Switch Fragment - First Fragment to Chat List
     */
    public void switchToChatList(String username) {

        ChatList chatListFragment = new ChatList();
        Bundle args = new Bundle();
        args.putString("username", username);
        chatListFragment.setArguments(args);

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.main, chatListFragment, TAG_NOTE_EDIT)
                .addToBackStack(null)
                .commit();
    }

    public void switchToChat(String receive,String username) {

        Chat chat = new Chat();
        Bundle args = new Bundle();
        args.putString("userReceive", receive);
        args.putString("username", username);
        chat.setArguments(args);

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.main, chat, TAG_NOTE_EDIT)
                .addToBackStack(null)
                .commit();

    }

    public void switchToArduinoConfigs(String username) {

        ArduinoConfiguration config = new ArduinoConfiguration();
        Bundle args = new Bundle();
        args.putString("username", username);
        config.setArguments(args);

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.main, config, TAG_NOTE_EDIT)
                .addToBackStack(null)
                .commit();

    }


    public String getClientId() {
        return clientId;
    }
}