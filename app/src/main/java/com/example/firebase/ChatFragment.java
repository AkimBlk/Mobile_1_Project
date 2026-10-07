package com.example.firebase;

import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import androidx.fragment.app.DialogFragment;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.ListenerRegistration;
import java.util.HashMap;
import java.util.Map;

public class ChatFragment extends DialogFragment {

    private LinearLayout messageContainer;
    private ScrollView scrollView;
    private EditText editMessage;
    private ListenerRegistration listener;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_chat, container, false);

        messageContainer = view.findViewById(R.id.message_container);
        scrollView = view.findViewById(R.id.scroll_view);
        editMessage = view.findViewById(R.id.edit_message);
        
        view.findViewById(R.id.btn_send).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sendMessage();
            }
        });

        getDialog().getWindow().setBackgroundDrawable(new ColorDrawable(Color.argb(200, 30, 30, 30)));

        listenMessages();
        return view;
    }

    @Override
    public void onStart() {
        super.onStart();
        int width = (int) (getResources().getDisplayMetrics().widthPixels * 0.85);
        int height = (int) (getResources().getDisplayMetrics().heightPixels * 0.55);

        android.view.WindowManager.LayoutParams params = getDialog().getWindow().getAttributes();
        params.width = width;
        params.height = height;
        params.gravity = android.view.Gravity.TOP | android.view.Gravity.CENTER_HORIZONTAL;
        params.x = 0;
        params.y = 80;
        getDialog().getWindow().setAttributes(params);
    }

    private void sendMessage() {
        String text = editMessage.getText().toString().trim();
        if (text.isEmpty() == true) {
            return;
        }

        String author = FirebaseAuth.getInstance().getCurrentUser().getEmail();

        Map<String, Object> msg = new HashMap<>();
        msg.put("author", author);
        msg.put("text", text);
        msg.put("timestamp", System.currentTimeMillis());

        FirebaseFirestore.getInstance().collection("chat").add(msg);
        editMessage.setText("");
    }

    private void listenMessages() {
        listener = FirebaseFirestore.getInstance()
                .collection("chat")
                .orderBy("timestamp")
                .addSnapshotListener((snapshot, e) -> {
                    messageContainer.removeAllViews();
                    for (DocumentSnapshot doc : snapshot.getDocuments()) {
                        String fullAuthor = doc.getString("author");
                        String[] parts = fullAuthor.split("@");
                        String author = parts[0];
                        String text = doc.getString("text");

                        TextView textViewMessage = new TextView(getContext());
                        textViewMessage.setText("👤 " + author + " : " + text);
                        textViewMessage.setTextColor(Color.WHITE);
                        textViewMessage.setPadding(8, 4, 8, 4);
                        messageContainer.addView(textViewMessage);
                    }
                    scrollView.post(new Runnable() {
                        @Override
                        public void run() {
                            scrollView.fullScroll(ScrollView.FOCUS_DOWN);
                        }
                    });
                });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        listener.remove();
    }
}