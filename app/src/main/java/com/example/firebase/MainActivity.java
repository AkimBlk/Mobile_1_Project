package com.example.firebase;

import android.os.Bundle;
import android.widget.LinearLayout;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.fragment.app.Fragment;

public class MainActivity extends AppCompatActivity {

    private MapFragment mapFragment;
    private CollectionFragment collectionFragment;
    private ProfileFragment profileFragment;

    private Fragment activeFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets bars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(bars.left, bars.top, bars.right, bars.bottom);
            return insets;
        });

        GameRepository.loadGames(this);

        if (savedInstanceState == null) {
            mapFragment = new MapFragment();
            collectionFragment = new CollectionFragment();
            profileFragment = new ProfileFragment();

            getSupportFragmentManager()
                    .beginTransaction()
                    .add(R.id.fragment_layout, profileFragment, "PROFILE")
                    .hide(profileFragment)
                    .add(R.id.fragment_layout, collectionFragment, "COLLECTION")
                    .hide(collectionFragment)
                    .add(R.id.fragment_layout, mapFragment, "MAP")
                    .commit();

            activeFragment = mapFragment;
        } else {
            mapFragment = (MapFragment) getSupportFragmentManager().findFragmentByTag("MAP");
            collectionFragment = (CollectionFragment) getSupportFragmentManager().findFragmentByTag("COLLECTION");
            profileFragment = (ProfileFragment) getSupportFragmentManager().findFragmentByTag("PROFILE");

            if (mapFragment != null && mapFragment.isVisible()) {
                activeFragment = mapFragment;
            } else if (collectionFragment != null && collectionFragment.isVisible()) {
                activeFragment = collectionFragment;
            } else {
                activeFragment = profileFragment;
            }
        }

        ((LinearLayout) findViewById(R.id.btn_nav_map))
                .setOnClickListener(v -> switchFragment(mapFragment));

        ((LinearLayout) findViewById(R.id.btn_nav_collection))
                .setOnClickListener(v -> switchFragment(collectionFragment));

        ((LinearLayout) findViewById(R.id.btn_nav_profile))
                .setOnClickListener(v -> switchFragment(profileFragment));
    }

    private void switchFragment(Fragment targetFragment) {
        if (targetFragment == null || targetFragment == activeFragment) return;

        getSupportFragmentManager()
                .beginTransaction()
                .hide(activeFragment)
                .show(targetFragment)
                .commit();

        activeFragment = targetFragment;
    }
}