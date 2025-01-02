package com.example.finalchallenge;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link train_go#newInstance} factory method to
 * create an instance of this fragment.
 */
public class train_go extends Fragment {

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    public train_go() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment train_go.
     */
    // TODO: Rename and change types and number of parameters
    public static train_go newInstance(String param1, String param2) {
        train_go fragment = new train_go();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_train_go, container, false);

        // Inicializando o ListView
        ListView listView = view.findViewById(R.id.list_view);

        // Criando uma lista de itens (exemplo de treino ou lista de usuários)
        String[] items = {"Exercise 1", "Exercise 2", "Exercise 3", "Exercise 4", "Exercise 5","Exercise 1", "Exercise 2", "Exercise 3", "Exercise 4", "Exercise 5","Exercise 1", "Exercise 2", "Exercise 3", "Exercise 4", "Exercise 5","Exercise 1", "Exercise 2", "Exercise 3", "Exercise 4", "Exercise 5"};

        // Criando o Adapter para a lista (pode ser um ArrayAdapter ou CustomAdapter)
        ArrayAdapter<String> adapter = new ArrayAdapter<>(requireActivity(), android.R.layout.simple_list_item_checked, items);

        // Definindo o Adapter para o ListView
        listView.setAdapter(adapter);

        return view;
    }
}