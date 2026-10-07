package com.example.firebase;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.fragment.app.Fragment;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;

public class ProfileFragment extends Fragment {

    private TextView textViewUsername;
    private TextView textViewEmail;
    private TextView textViewLevel;
    private TextView textViewXp;
    private Button buttonLogout;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_profile, container, false);

        textViewUsername = view.findViewById(R.id.tv_username);
        textViewEmail = view.findViewById(R.id.tv_email);
        textViewLevel = view.findViewById(R.id.tv_level);
        textViewXp = view.findViewById(R.id.tv_xp);
        buttonLogout = view.findViewById(R.id.btn_logout);

        buttonLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                FirebaseAuth.getInstance().signOut();
                Toast.makeText(getActivity(), "Déconnexion réussie", Toast.LENGTH_SHORT).show();
                Intent intent = new Intent(getActivity(), Login.class);
                startActivity(intent);
                getActivity().finish();
            }
        });

        loadUserData();
        return view;
    }

    @Override
    public void onHiddenChanged(boolean hidden) {
        super.onHiddenChanged(hidden);
        if (hidden == false) {
            loadUserData();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        loadUserData();
    }

    private void loadUserData() {
        String uid = FirebaseAuth.getInstance().getUid();

        FirebaseFirestore.getInstance().collection("users").document(uid).get()
                .addOnSuccessListener(document -> {
                    if (document.exists() == true) {
                        textViewUsername.setText(document.getString("username"));
                        textViewEmail.setText(document.getString("email"));

                        long level = document.getLong("level");
                        textViewLevel.setText(String.valueOf(level));

                        long xp = document.getLong("xp");
                        textViewXp.setText(String.valueOf(xp));
                    }
                });
    }
}