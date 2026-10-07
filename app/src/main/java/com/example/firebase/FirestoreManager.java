package com.example.firebase;

import android.content.Context;
import android.widget.Toast;
import com.google.firebase.Timestamp;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class FirestoreManager {

    public static void createUserProfile(String uid, String email) {
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        Map<String, Object> user = new HashMap<>();
        user.put("email", email);
        String[] parts = email.split("@");
        user.put("username", parts[0]);
        user.put("createdAt", Timestamp.now());
        user.put("level", 1);
        user.put("xp", 0);

        db.collection("users").document(uid).set(user);
    }

    public static void addUserXP(Context context, int amount) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        db.collection("users").document(user.getUid()).get().addOnSuccessListener(document -> {
            long currentXp = document.getLong("xp");
            long currentLevel = document.getLong("level");

            long newXp = currentXp + amount;
            long newLevel = currentLevel;

            boolean levelUp = false;
            while (newXp >= 100) {
                newLevel = newLevel + 1;
                newXp = newXp - 100;
                levelUp = true;
            }

            if (levelUp == true) {
                Toast.makeText(context, "Niveau supérieur ! Niveau " + newLevel, Toast.LENGTH_SHORT).show();
            }

            db.collection("users").document(user.getUid())
                    .update("xp", newXp, "level", newLevel);
        });
    }

    public static void saveCapturedGame(Game game) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        FirebaseFirestore db = FirebaseFirestore.getInstance();

        Map<String, Object> data = new HashMap<>();
        data.put("name", game.getName());
        data.put("imageUrl", game.getImageUrl());
        data.put("metacritic", game.getMetacritic());
        data.put("captured", true);

        db.collection("users")
                .document(user.getUid())
                .collection("games")
                .document(game.getName())
                .set(data);
    }

    public static void loadCapturedGames(OnGamesLoadedListener listener) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();

        FirebaseFirestore.getInstance()
                .collection("users")
                .document(user.getUid())
                .collection("games")
                .get()
                .addOnSuccessListener(snapshot -> {
                    ArrayList<String[]> games = new ArrayList<>();
                    for (var doc : snapshot.getDocuments()) {
                        String name = doc.getString("name");
                        String imageUrl = doc.getString("imageUrl");
                        String[] gameData = new String[2];
                        gameData[0] = name;
                        gameData[1] = imageUrl;
                        games.add(gameData);
                    }
                    listener.onLoaded(games);
                });
    }

    public interface OnGamesLoadedListener {
        void onLoaded(ArrayList<String[]> games);
    }
}