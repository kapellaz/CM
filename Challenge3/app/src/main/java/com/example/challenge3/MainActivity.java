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

    private ViewModelChat viewModelChat = new ViewModelChat();
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

        FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
        ft.replace(R.id.main, login);
        ft.commit();
    }


    /**switchToChat
     * Switch Fragment - First Fragment to Chat List
     */
    public void switchToChatList() {

        ChatList chatListFragment = new ChatList();

        getSupportFragmentManager().beginTransaction()
                .replace(R.id.main, chatListFragment, TAG_NOTE_EDIT)
                .addToBackStack(null)
                .commit();
    }

    public void switchToChat() {

        Chat chat = new Chat();


        getSupportFragmentManager().beginTransaction()
                .replace(R.id.main, chat, TAG_NOTE_EDIT)
                .addToBackStack(null)
                .commit();

    }

    public void switchToArduinoConfigs() {

        ArduinoConfiguration config = new ArduinoConfiguration();


        getSupportFragmentManager().beginTransaction()
                .replace(R.id.main, config, TAG_NOTE_EDIT)
                .addToBackStack(null)
                .commit();

    }


    public String getClientId() {
        return clientId;
    }
}