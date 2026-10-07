package com.example.firebase;

import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.squareup.picasso.Picasso;
import java.util.ArrayList;

public class ArenaLobbyActivity extends AppCompatActivity {

    private TextView textViewTimer;
    private CountDownTimer countDownTimer;
    private int arenaIndex;
    private Game currentGame;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_arena_lobby);

        textViewTimer = findViewById(R.id.tv_timer_lobby);
        TextView textViewArenaName = findViewById(R.id.tv_arena_name);
        TextView textViewGameName = findViewById(R.id.tv_game_name_lobby);
        ImageView imageViewGame = findViewById(R.id.iv_game_image);
        TextView textViewMetacritic = findViewById(R.id.tv_metacritic);
        TextView textViewClicksNeeded = findViewById(R.id.tv_clicks_needed);
        Button buttonSkip = findViewById(R.id.btn_skip_timer);

        arenaIndex = getIntent().getIntExtra("ARENA_INDEX", 0);
        Arena arena = ArenaRepository.getArenas().get(arenaIndex);
        currentGame = arena.getGame();

        textViewArenaName.setText(arena.getName());
        textViewGameName.setText(currentGame.getName());
        textViewMetacritic.setText("Metacritic: " + currentGame.getMetacritic());
        
        int requiredClicks = currentGame.getMetacritic() - 10;
        if (requiredClicks < 80) {
            requiredClicks = 80;
        }
        textViewClicksNeeded.setText("Objectif: " + requiredClicks + " clics");

        Picasso.get().load(currentGame.getImageUrl()).into(imageViewGame);

        buttonSkip.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (countDownTimer != null) {
                    countDownTimer.cancel();
                }
                launchMiniGame(currentGame);
            }
        });

        countDownTimer = new CountDownTimer(60000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                textViewTimer.setText("Lancement dans : " + (millisUntilFinished / 1000) + "s");
            }

            @Override
            public void onFinish() {
                launchMiniGame(currentGame);
            }
        }.start();
    }

    private void launchMiniGame(Game game) {
        int gameIndexInRepo = -1;
        ArrayList<Game> allGames = GameRepository.getGames();
        int i = 0;
        while (i < allGames.size()) {
            if (allGames.get(i).getName().equals(game.getName())) {
                gameIndexInRepo = i;
            }
            i = i + 1;
        }

        Intent intent = new Intent(ArenaLobbyActivity.this, GameActivity.class);
        intent.putExtra("GAME_INDEX", gameIndexInRepo);
        intent.putExtra("IS_ARENA", true);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (countDownTimer != null) {
            countDownTimer.cancel();
        }
    }
}