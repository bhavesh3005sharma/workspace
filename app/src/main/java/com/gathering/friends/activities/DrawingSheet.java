package com.gathering.friends.activities;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;

import com.gathering.friends.R;
import com.gathering.friends.databinding.ActivityDrawingSheetBinding;
import com.gun0912.tedpermission.PermissionListener;
import com.gun0912.tedpermission.TedPermission;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;

public class DrawingSheet extends AppCompatActivity implements OnClickListener {
    ActivityDrawingSheetBinding activityDrawingSheetBinding;

    private int lastColorIdSelected;
    private int lastSelectedChoice;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        activityDrawingSheetBinding = ActivityDrawingSheetBinding.inflate(getLayoutInflater());
        setContentView(activityDrawingSheetBinding.getRoot());

        // set tool bar
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeButtonEnabled(true);
        getSupportActionBar().setTitle(getString(R.string.drawing_sheet));

        // setup click listeners
        setUpClickListeners();

        lastColorIdSelected = R.id.color1;
        setColorSelected(lastColorIdSelected, lastColorIdSelected);
        lastSelectedChoice = R.id.draw_btn;
        setChoiceSelected(lastSelectedChoice, lastSelectedChoice, 2);

        activityDrawingSheetBinding.drawing.setColor("#6200EE");

        super.onCreate(savedInstanceState);
    }

    private void setUpClickListeners() {
        activityDrawingSheetBinding.newBtn.setOnClickListener(this);
        activityDrawingSheetBinding.drawBtn.setOnClickListener(this);
        activityDrawingSheetBinding.eraseBtn.setOnClickListener(this);
        activityDrawingSheetBinding.saveBtn.setOnClickListener(this);

        activityDrawingSheetBinding.color1.setOnClickListener(this);
        activityDrawingSheetBinding.color2.setOnClickListener(this);
        activityDrawingSheetBinding.color3.setOnClickListener(this);
        activityDrawingSheetBinding.color4.setOnClickListener(this);
        activityDrawingSheetBinding.color5.setOnClickListener(this);
        activityDrawingSheetBinding.color6.setOnClickListener(this);
        activityDrawingSheetBinding.color7.setOnClickListener(this);
        activityDrawingSheetBinding.color8.setOnClickListener(this);
        activityDrawingSheetBinding.color9.setOnClickListener(this);
        activityDrawingSheetBinding.color10.setOnClickListener(this);
        activityDrawingSheetBinding.color11.setOnClickListener(this);
        activityDrawingSheetBinding.color12.setOnClickListener(this);
        activityDrawingSheetBinding.color13.setOnClickListener(this);
        activityDrawingSheetBinding.color14.setOnClickListener(this);
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return super.onSupportNavigateUp();
    }

    private void setChoiceSelected(int lastSelectedChoice_1, int lastSelectedChoice_2, int a) {
        ImageButton btn1 = findViewById(lastSelectedChoice_1);
        ImageButton btn2 = findViewById(lastSelectedChoice_2);
        int clr = Color.parseColor("#D9D3D3");
        btn2.setBackgroundColor(clr);
        btn1.setElevation(10);
        btn1.setBackgroundResource(R.drawable.button_background);
        if (a == 1)
            btn1.setImageResource(R.drawable.new_drawing);
        else if (a == 2)
            btn1.setImageResource(R.drawable.drawing);
        else if (a == 3)
            btn1.setImageResource(R.drawable.erase);
        else if (a == 4)
            btn1.setImageResource(R.drawable.ic_save_black_24dp);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.draw_btn:
                activityDrawingSheetBinding.drawing.setupDrawing();
                setChoiceSelected(R.id.draw_btn, lastSelectedChoice, 2);
                lastSelectedChoice = R.id.draw_btn;
                break;
            case R.id.new_btn:
                setChoiceSelected(R.id.new_btn, lastSelectedChoice, 1);
                lastSelectedChoice = R.id.new_btn;
                AlertDialog.Builder newDialog = new AlertDialog.Builder(DrawingSheet.this);
                newDialog.setTitle("New drawing");
                newDialog.setMessage("Start new drawing (you will lose the current drawing)?");
                newDialog.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        activityDrawingSheetBinding.drawing.startNew();
                        dialog.dismiss();
                    }
                });
                newDialog.setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
                newDialog.show();
                break;
            case R.id.erase_btn:
                setChoiceSelected(R.id.erase_btn, lastSelectedChoice, 3);
                lastSelectedChoice = R.id.erase_btn;
                activityDrawingSheetBinding.drawing.setErase(true);
                activityDrawingSheetBinding.drawing.setBrushSize(activityDrawingSheetBinding.drawing.getLastBrushSize());
                break;
            case R.id.save_btn:
                setChoiceSelected(R.id.save_btn, lastSelectedChoice, 4);
                lastSelectedChoice = R.id.save_btn;
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        != PackageManager.PERMISSION_GRANTED) {
                    CheckForRequiredPermissions();
                }
                AlertDialog.Builder saveDialog = new AlertDialog.Builder(DrawingSheet.this);
                saveDialog.setTitle("Save drawing");
                saveDialog.setMessage("Save drawing to device Gallery?");
                saveDialog.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        activityDrawingSheetBinding.drawing.setDrawingCacheEnabled(true);
                        Log.d("DrawingCache ", "" + activityDrawingSheetBinding.drawing.getDrawingCache());
                        Uri uri = getImageUri(DrawingSheet.this, activityDrawingSheetBinding.drawing.getDrawingCache());
                        Log.d("uri ", "" + uri);
                        if (uri != null) {
                            Toast savedToast = Toast.makeText(DrawingSheet.this,
                                    "Drawing saved to Gallery!", Toast.LENGTH_SHORT);
                            savedToast.show();
                        } else {
                            Toast unsavedToast = Toast.makeText(DrawingSheet.this,
                                    "Oops! Image could not be saved.", Toast.LENGTH_SHORT);
                            unsavedToast.show();
                        }
                        activityDrawingSheetBinding.drawing.destroyDrawingCache();

                    }
                });
                saveDialog.setNegativeButton("No", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                });
                saveDialog.show();
                break;
            case R.id.color1:
                activityDrawingSheetBinding.drawing.setColor("#6200EE");
                setColorSelected(R.id.color1, lastColorIdSelected);
                lastColorIdSelected = R.id.color1;
                break;
            case R.id.color2:
                activityDrawingSheetBinding.drawing.setColor("#03DAC5");
                setColorSelected(R.id.color2, lastColorIdSelected);
                lastColorIdSelected = R.id.color2;
                break;
            case R.id.color3:
                activityDrawingSheetBinding.drawing.setColor("#91EC27");
                setColorSelected(R.id.color3, lastColorIdSelected);
                lastColorIdSelected = R.id.color3;
                break;
            case R.id.color4:
                activityDrawingSheetBinding.drawing.setColor("#FFFF6600");
                setColorSelected(R.id.color4, lastColorIdSelected);
                lastColorIdSelected = R.id.color4;
                break;
            case R.id.color5:
                activityDrawingSheetBinding.drawing.setColor("#FFFFCC00");
                setColorSelected(R.id.color5, lastColorIdSelected);
                lastColorIdSelected = R.id.color5;
                break;
            case R.id.color6:
                activityDrawingSheetBinding.drawing.setColor("#FF009900");
                setColorSelected(R.id.color6, lastColorIdSelected);
                lastColorIdSelected = R.id.color6;
                break;
            case R.id.color7:
                activityDrawingSheetBinding.drawing.setColor("#EC2516");
                setColorSelected(R.id.color7, lastColorIdSelected);
                lastColorIdSelected = R.id.color7;
                break;
            case R.id.color8:
                activityDrawingSheetBinding.drawing.setColor("#FF009999");
                setColorSelected(R.id.color8, lastColorIdSelected);
                lastColorIdSelected = R.id.color8;
                break;
            case R.id.color9:
                activityDrawingSheetBinding.drawing.setColor("#FF0000FF");
                setColorSelected(R.id.color9, lastColorIdSelected);
                lastColorIdSelected = R.id.color9;
                break;
            case R.id.color10:
                activityDrawingSheetBinding.drawing.setColor("#FF990099");
                setColorSelected(R.id.color10, lastColorIdSelected);
                lastColorIdSelected = R.id.color10;
                break;
            case R.id.color11:
                activityDrawingSheetBinding.drawing.setColor("#E91E63");
                setColorSelected(R.id.color11, lastColorIdSelected);
                lastColorIdSelected = R.id.color11;
                break;
            case R.id.color12:
                activityDrawingSheetBinding.drawing.setColor("#FFFFFFFF");
                setColorSelected(R.id.color12, lastColorIdSelected);
                lastColorIdSelected = R.id.color12;
                break;
            case R.id.color13:
                activityDrawingSheetBinding.drawing.setColor("#FF787878");
                setColorSelected(R.id.color13, lastColorIdSelected);
                lastColorIdSelected = R.id.color13;
                break;
            case R.id.color14:
                activityDrawingSheetBinding.drawing.setColor("#170202");
                setColorSelected(R.id.color14, lastColorIdSelected);
                lastColorIdSelected = R.id.color14;
                break;
        }
    }

    private void setColorSelected(int clr, int lastColorIdSelected_) {
        ImageButton selectedImg = findViewById(clr);
        ImageButton unSelectedImg = findViewById(lastColorIdSelected_);
        unSelectedImg.setImageResource(R.drawable.paint);
        selectedImg.setImageResource(R.drawable.paint_pressed);
        selectedImg.setElevation(10);
    }

    private Uri getImageUri(Context context, Bitmap inImage) {
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes);
        String path = MediaStore.Images.Media.insertImage(context.getContentResolver(), inImage, "Title", null);
        return Uri.parse(path);
    }

    public void CheckForRequiredPermissions() {
        PermissionListener permissionlistener = new PermissionListener() {
            @Override
            public void onPermissionGranted() {
                Toast.makeText(DrawingSheet.this, "Permission Granted", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onPermissionDenied(ArrayList<String> deniedPermissions) {
                Toast.makeText(DrawingSheet.this, "Permission Denied\n" + deniedPermissions.toString(), Toast.LENGTH_SHORT).show();
            }
        };

        TedPermission.with(DrawingSheet.this)
                .setPermissionListener(permissionlistener)
                .setDeniedMessage("Give Storage Permission\nPlease turn on permissions at\n [Setting] > [Permission]")
                .setPermissions(Manifest.permission.READ_CONTACTS, Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                .check();
    }
}
