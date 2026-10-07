package com.example.firebase;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.fragment.app.Fragment;

public class CollectionFragment extends Fragment {

    private LinearLayout gameContainer;
    private TextView textViewNoGame;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_collection, container, false);

        gameContainer = view.findViewById(R.id.gameContainer);
        textViewNoGame = view.findViewById(R.id.TextViewNoGame);

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();
        refreshCollection();
    }

    private void refreshCollection() {
        gameContainer.removeAllViews();
        textViewNoGame.setVisibility(View.GONE);

        FirestoreManager.loadCapturedGames(games -> {
            if (games.isEmpty()) {
                textViewNoGame.setVisibility(View.VISIBLE);
                return;
            }
            for (String[] game : games) {
                Button btn = new Button(getContext());
                btn.setText(game[0]); // name
                btn.setOnClickListener(v -> {
                    // trouve le bon index dans GameRepository
                    for (int i = 0; i < GameRepository.getGames().size(); i++) {
                        if (GameRepository.getGames().get(i).getName().equals(game[0])) {
                            Intent intent = new Intent(getContext(), DetailActivity.class);
                            intent.putExtra("GAME_INDEX", i);
                            startActivity(intent);
                            break;
                        }
                    }
                });
                gameContainer.addView(btn);
            }
        });
    }


    public void goToDetailActivity(int position) {
        Intent intent = new Intent(getContext(), DetailActivity.class);
        intent.putExtra("GAME_INDEX", position);
        startActivity(intent);
    }
}