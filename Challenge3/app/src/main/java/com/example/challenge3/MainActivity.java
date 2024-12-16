package com.example.challenge3;

import android.os.Bundle;
import android.util.Log;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import org.eclipse.paho.client.mqttv3.MqttClient;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.os.Build;
public class MainActivity extends AppCompatActivity {

    private ViewModelChat viewModelChat = new ViewModelChat();
    private FirstTimeFrag login;
    private static final String TAG_TEXTING = "TEXTING";
    private static final String TAG_LISTING = "LISTING";
    private static final String TAG_ARDUINO = "ARDUINO";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        //inicializar o login
        login = new FirstTimeFrag();


        if (savedInstanceState != null) {
            // Retrieve the saved fragment tag
            String fragmentTag = savedInstanceState.getString("currentFragmentTag");
            if (fragmentTag != null) {
                Fragment fragment = getSupportFragmentManager().findFragmentByTag(fragmentTag);
                if (fragment == null) {
                    // Fragment is not currently in the manager, recreate it based on the tag
                    if (fragmentTag.equals(TAG_LISTING)) {
                        fragment = new ChatList();
                    } else if (fragmentTag.equals(TAG_TEXTING)) {
                        fragment = new Chat();
                    } else if (fragmentTag.equals(TAG_ARDUINO)) {
                        fragment = new ArduinoConfiguration();
                    } else {
                        fragment = new FirstTimeFrag(); // Load your default fragment if needed
                    }
                    getSupportFragmentManager().beginTransaction()
                            .replace(R.id.main, fragment, fragmentTag)
                            .commit();
                }
            }
        } else {

            FragmentTransaction ft = getSupportFragmentManager().beginTransaction();
            ft.replace(R.id.main, login);
            ft.commit();
        }
        createNotificationChannel();
    }


    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            String channelId = "default_channel_id";
            CharSequence name = "Default Channel";
            String description = "Notifications for app events";
            int importance = NotificationManager.IMPORTANCE_HIGH;

            NotificationChannel channel = new NotificationChannel(channelId, name, importance);
            channel.setDescription(description);

            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }

    }


    /**switchToChat
     * Switch Fragment - First Fragment to Chat List
     */
    public void switchToChatList() {

        ChatList chatListFragment = new ChatList();
        Bundle args = new Bundle();
        chatListFragment.setArguments(args);


        getSupportFragmentManager().beginTransaction()
                .replace(R.id.main, chatListFragment, "LISTING")
                .addToBackStack(null)
                .commit();
    }


    /**
     * Switches the current fragment to the Chat fragment.
     * Creates a new instance of the Chat fragment, sets its arguments, and replaces the current fragment with it.
     * The transaction is added to the back stack.
     */
    public void switchToChat() {
        Chat chat = new Chat();

        Bundle args = new Bundle();
        chat.setArguments(args);


        getSupportFragmentManager().beginTransaction()
                .replace(R.id.main, chat, "TEXTING")
                .addToBackStack(null)
                .commit();

    }



    /**
     * Switches the current fragment to the ArduinoConfiguration fragment.
     * Creates a new instance of the ArduinoConfiguration fragment, sets its arguments, and replaces the current fragment with it.
     * The transaction is added to the back stack.
     */
    public void switchToArduinoConfigs() {
        ArduinoConfiguration config = new ArduinoConfiguration();


        getSupportFragmentManager().beginTransaction()
                .replace(R.id.main, config, "ARDUINO")
                .addToBackStack(null)
                .commit();

    }

    //Define a behaviour do botão de recuar
    @Override
    public void onBackPressed() {
        Fragment currentFragment = getSupportFragmentManager().findFragmentById(R.id.main);
        if (currentFragment instanceof Chat) {
            // Navigate to ChatList from Chat
            switchToChatList();
        } else if (currentFragment instanceof ChatList) {
            // Navigate to Login from ChatList
            getSupportFragmentManager().popBackStack(); // Clear ChatList from back stack
            FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
            transaction.replace(R.id.main, login, "LOGIN");
            transaction.commit();
        } else if (currentFragment instanceof ArduinoConfiguration) {
            // Navigate to ChatList from ArduinoConfiguration
            switchToChatList();
        } else {
            super.onBackPressed();
        }
    }


    //Metodo que faz o save do fragmento atual
    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);

        Fragment currentFragment = getSupportFragmentManager().findFragmentById(R.id.main);
        if (currentFragment != null) {
            Log.v("Main Activity","Save" + currentFragment.getTag());
            outState.putString("currentFragmentTag", currentFragment.getTag());
        }
    }

}