package com.example.firebase;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.fragment.app.Fragment;

import com.squareup.picasso.Picasso;

public class ScreenshotFragment extends Fragment {

    public static ScreenshotFragment newInstance(String imageUrl) {
        ScreenshotFragment fragment = new ScreenshotFragment();
        Bundle args = new Bundle();
        args.putString("LIEN_IMAGE", imageUrl);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_screenshot, container, false);
        ImageView imageView = view.findViewById(R.id.screenshotImageView);

        String url = getArguments().getString("LIEN_IMAGE");

        Picasso.get().load(url).into(imageView);

        return view;
    }
}