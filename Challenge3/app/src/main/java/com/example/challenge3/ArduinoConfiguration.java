package com.example.challenge3;

import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import java.util.ArrayList;

public class ArduinoConfiguration extends Fragment {

    private ListView contactListView;
    private DatabaseHelper databaseHelper;
    private String username;
    private ArrayList<String> contacts;
    private ModelView chatViewModel;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        chatViewModel = new ViewModelProvider(requireActivity()).get(ModelView.class);
        super.onCreate(savedInstanceState);
        username = chatViewModel.getUsername().getValue();
        databaseHelper = new DatabaseHelper(requireContext());
        contacts = databaseHelper.getContactsWithUser(username);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_arduino_configuration, container, false);

        // Inicializar a ListView e o botão de envio
        contactListView = view.findViewById(R.id.contact_list_view);
        Button submitButton = view.findViewById(R.id.submit_button);

        // Configurar a Toolbar
        Toolbar toolbar = view.findViewById(R.id.toolbar);

        // Adicionar o título dinamicamente na Toolbar
        TextView titleTextView = new TextView(getContext());
        titleTextView.setText("Select Contacts for Arduino Notification");
        titleTextView.setTextSize(15);

        // Definir os parâmetros de layout para o título
        Toolbar.LayoutParams layoutParams = new Toolbar.LayoutParams(
                Toolbar.LayoutParams.WRAP_CONTENT,
                Toolbar.LayoutParams.MATCH_PARENT);
        layoutParams.gravity = Gravity.CENTER;
        titleTextView.setLayoutParams(layoutParams);

        toolbar.addView(titleTextView);

        // Configurar a navegação na Toolbar
        toolbar.setNavigationOnClickListener(v -> ((MainActivity) requireActivity()).switchToChatList());

        // Carregar os contatos a partir do banco de dados
        contacts = databaseHelper.getContactsWithUser(username);

        // Configurar o adaptador da ListView
        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireContext(), android.R.layout.simple_list_item_multiple_choice, contacts);
        contactListView.setAdapter(adapter);
        contactListView.setChoiceMode(ListView.CHOICE_MODE_MULTIPLE);

        // Carregar configurações salvas do banco de dados para a seleção
        ArrayList<String> selectedContacts = databaseHelper.getContactsForArduinoNotification(username);
        for (int i = 0; i < contacts.size(); i++) {
            if (selectedContacts.contains(contacts.get(i))) {
                contactListView.setItemChecked(i, true);
            }
        }

        // Salvar a seleção ao clicar em um item
        contactListView.setOnItemClickListener((parent, view1, position, id) -> {
            String selectedContact = contacts.get(position);
            if (contactListView.isItemChecked(position)) {
                databaseHelper.saveContactForArduinoNotification(username, selectedContact);

            } else {
                databaseHelper.removeContactFromArduinoNotification(username, selectedContact);

            }
        });

        // Ação do botão de submissão
        submitButton.setOnClickListener(v -> {
            ArrayList<String> selectedContactsList = new ArrayList<>();

            for (int i = 0; i < contactListView.getCount(); i++) {
                if (contactListView.isItemChecked(i)) {
                    selectedContactsList.add(contacts.get(i));
                }
            }


            if (!selectedContactsList.isEmpty()) {

                Toast.makeText(requireContext(), "Selected contacts sent to Arduino: " + selectedContactsList, Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(requireContext(), "No contacts selected", Toast.LENGTH_SHORT).show();
            }
            ((MainActivity) requireActivity()).switchToChatList();
        });

        return view;
    }
}
