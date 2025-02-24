package com.feedbackssdk.myvault.Fragments;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.feedbackssdk.myvault.Activities.ImageAdapter;
import com.feedbackssdk.myvault.R;
import com.google.android.material.carousel.CarouselLayoutManager;
import com.google.android.material.carousel.CarouselSnapHelper;
import com.google.android.material.carousel.HeroCarouselStrategy;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ImagesFragment extends Fragment {

    private ProgressBar loadingIndicator;
    private RecyclerView recyclerView;

    public ImagesFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_images, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        loadingIndicator = view.findViewById(R.id.imageLoadingIndicator);
        recyclerView = view.findViewById(R.id.recyclerViewCarousel);
        setupRecyclerView();
        loadImages();
    }

    private void setupRecyclerView() {
        CarouselLayoutManager layoutManager = new CarouselLayoutManager(new HeroCarouselStrategy(),CarouselLayoutManager.VERTICAL);
        recyclerView.setLayoutManager(layoutManager);
        recyclerView.setHasFixedSize(true);
        CarouselSnapHelper snapHelper = new CarouselSnapHelper();
        layoutManager.setCarouselAlignment(CarouselLayoutManager.ALIGNMENT_CENTER);
        snapHelper.attachToRecyclerView(recyclerView);
    }

    private void loadImages() {
        File dir = requireContext().getFilesDir();
        File[] files = dir.listFiles((file, name) -> name.startsWith("secure_") && name.endsWith(".enc"));
        List<File> fileList = new ArrayList<>();
        if (files != null) {
            fileList.addAll(Arrays.asList(files));
        }
        ImageAdapter adapter = new ImageAdapter(requireContext(), fileList);
        recyclerView.setAdapter(adapter);
        loadingIndicator.setVisibility(View.GONE);
    }
}
