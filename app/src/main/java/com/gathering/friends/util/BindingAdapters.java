package com.gathering.friends.util;

import android.widget.ImageView;
import android.widget.TextView;

import androidx.databinding.BindingAdapter;

import com.bumptech.glide.Glide;
import com.gathering.friends.R;

import java.text.DateFormat;
import java.util.Date;
import java.util.Map;

import static java.text.DateFormat.getDateTimeInstance;

public class BindingAdapters {

    @BindingAdapter("android:loadImage")
    public static void loadImage(ImageView imageView, String photoUrl) {
        if (photoUrl != null && !photoUrl.isEmpty())
            Glide.with(imageView).load(photoUrl).placeholder(R.drawable.ic_baseline_person_24).into(imageView);
        else imageView.setImageResource(R.drawable.user);
    }

    @BindingAdapter("android:loadTime")
    public static void loadTime(TextView textView, Map map) {
        String time = null;
        long timeStamp = 0;
        if (map != null) {
            timeStamp = (long) map.get("timeStamp");
            DateFormat dateFormat = getDateTimeInstance();
            Date netDate = (new Date(timeStamp));
            time = dateFormat.format(netDate);
        }
        textView.setText(time);
    }
}
