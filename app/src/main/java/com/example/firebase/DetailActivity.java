package com.example.firebase;

import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.google.android.material.button.MaterialButton;
import com.squareup.picasso.Picasso;

public class DetailActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        ImageView imageView = findViewById(R.id.detailImageView);
        TextView textViewTitle = findViewById(R.id.detailTitleTextView);
        TextView textViewMetacritic = findViewById(R.id.detailMetacriticTextView);
        MaterialButton btnOpenCarrousel = findViewById(R.id.btnOpenCarrousel);

        int gameIndex = getIntent().getIntExtra("GAME_INDEX", -1);
        Game selectedGame = GameRepository.getGame(gameIndex);

        if (selectedGame == null) {
            Toast.makeText(this, "Erreur de chargement", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        textViewTitle.setText(selectedGame.getName());
        textViewMetacritic.setText("Metacritic : " + selectedGame.getMetacritic());

        Picasso.get().load(selectedGame.getImageUrl()).into(imageView);

        btnOpenCarrousel.setOnClickListener(v -> {
            if (selectedGame.getScreenshotsUrl() != null) {
                if (selectedGame.getScreenshotsUrl().isEmpty() == false) {
                    Intent intent = new Intent(DetailActivity.this, CarrouselActivity.class);
                    intent.putExtra("GAME_INDEX", gameIndex);
                    startActivity(intent);
                } else {
                    Toast.makeText(DetailActivity.this, "Aucun screenshot", Toast.LENGTH_SHORT).show();
                }
            } else {
                Toast.makeText(DetailActivity.this, "Aucun screenshot", Toast.LENGTH_SHORT).show();
            }
        });
    }
}