package com.gathering.friends.util;

import android.widget.ImageView;

import androidx.databinding.BindingAdapter;

import com.bumptech.glide.Glide;
import com.gathering.friends.R;

public class BindingAdapters {

    @BindingAdapter("android:loadImage")
    public static void loadImage(ImageView imageView, String photoUrl) {
        if (photoUrl != null && !photoUrl.isEmpty())
            Glide.with(imageView).load(photoUrl).placeholder(R.drawable.ic_baseline_person_24).into(imageView);
        else imageView.setImageResource(R.drawable.user);
    }
}
