package com.example.firebase;

import android.os.Bundle;
import android.os.CountDownTimer;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.constraintlayout.widget.ConstraintLayout;
import java.util.Random;

public class GameActivity extends AppCompatActivity {

    private int currentClicks = 0;
    private int requiredClicks = 0;
    private int gameIndex = -1;
    private boolean isArena = false;
    private TextView textViewCpt;
    private TextView textViewTimer;
    private Button buttonClic;
    private ConstraintLayout layout;
    private final Random random = new Random();
    private CountDownTimer timer;
    private boolean timerStarted = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_game);

        textViewCpt = findViewById(R.id.textView_cpt);
        textViewTimer = findViewById(R.id.textView_timer);
        buttonClic = findViewById(R.id.btn_clic);
        layout = findViewById(R.id.game_layout);

        gameIndex = getIntent().getIntExtra("GAME_INDEX", -1);
        isArena = getIntent().getBooleanExtra("IS_ARENA", false);

        Game game = GameRepository.getGame(gameIndex);

        if (isArena == true) {
            requiredClicks = game.getMetacritic() - 10;
            if (requiredClicks < 80) {
                requiredClicks = 80;
            }
            Toast.makeText(this, "COMBAT DE RAID : " + game.getName(), Toast.LENGTH_LONG).show();
        } else {
            requiredClicks = (game.getMetacritic() / 3) + 5;
            if (requiredClicks < 5) {
                requiredClicks = 5;
            }
            Toast.makeText(this, game.getName() + " rencontré !", Toast.LENGTH_SHORT).show();
        }

        textViewCpt.setText(currentClicks + " / " + requiredClicks);
        textViewTimer.setText("20s");
    }

    public void gameClic(View view) {
        if (timerStarted == false) {
            startTimer();
            timerStarted = true;
        }

        currentClicks = currentClicks + 1;
        textViewCpt.setText(currentClicks + " / " + requiredClicks);

        if (currentClicks >= requiredClicks) {
            winGame();
        } else {
            moveButton();
        }
    }

    private void startTimer() {
        timer = new CountDownTimer(20000, 1000) {
            @Override
            public void onTick(long millisUntilFinished) {
                textViewTimer.setText((millisUntilFinished / 1000) + "s");
            }

            @Override
            public void onFinish() {
                Toast.makeText(GameActivity.this, "Échec du combat...", Toast.LENGTH_SHORT).show();
                finish();
            }
        };
        timer.start();
    }

    private void winGame() {
        timer.cancel();

        Game game = GameRepository.getGame(gameIndex);
        game.setCaptured(true);
        FirestoreManager.saveCapturedGame(game);

        int xpReward = game.getMetacritic();
        FirestoreManager.addUserXP(this, xpReward);

        String message;
        if (isArena == true) {
            message = "Arène vaincue ! ";
        } else {
            message = "Jeu capturé ! ";
        }
        Toast.makeText(this, message + "+" + xpReward + " XP", Toast.LENGTH_SHORT).show();

        finish();
    }

    private void moveButton() {
        int marginTop = 180;
        int marginBottom = 180;
        int maxX = layout.getWidth() - buttonClic.getWidth();
        int maxY = layout.getHeight() - buttonClic.getHeight() - marginBottom;

        if (maxX > 0) {
            if (maxY > marginTop) {
                buttonClic.setX(random.nextInt(maxX));
                buttonClic.setY(random.nextInt(maxY - marginTop) + marginTop);
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (timer != null) {
            timer.cancel();
        }
    }
}