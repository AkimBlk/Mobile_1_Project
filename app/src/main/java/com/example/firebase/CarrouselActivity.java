package com.example.firebase;

import android.os.Bundle;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.viewpager2.adapter.FragmentStateAdapter;
import androidx.viewpager2.widget.ViewPager2;

import java.util.ArrayList;

public class CarrouselActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_carrousel);

        ViewPager2 viewPager = findViewById(R.id.view_pager);
        TextView textCompteur = findViewById(R.id.textView_compteur);

        int gameIndex = getIntent().getIntExtra("GAME_INDEX", -1);
        Game selectedGame = GameRepository.getGame(gameIndex);

        if (selectedGame == null) {
            Toast.makeText(this, "Erreur de chargement des images", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        ArrayList<String> listeImages = selectedGame.getScreenshotsUrl();

        viewPager.setAdapter(new FragmentStateAdapter(this) {
            @Override
            public Fragment createFragment(int position) {
                return ScreenshotFragment.newInstance(listeImages.get(position));
            }

            @Override
            public int getItemCount() {
                return listeImages.size();
            }
        });

        int totalImages = listeImages.size();
        textCompteur.setText("1 / " + totalImages);

        viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                textCompteur.setText((position + 1) + " / " + totalImages);
            }
        });
    }
}