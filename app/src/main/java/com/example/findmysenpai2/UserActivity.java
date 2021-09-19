package com.example.findmysenpai2;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import java.io.IOException;

public class UserActivity extends AppCompatActivity {

    private static final int SELECT_AVATAR_REQUEST_CODE = 1338;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.user);

        // Add listener for changing avatar
        Button button = this.findViewById(R.id.upload_avatar);
        button.setOnClickListener(view -> {
            Intent intent = new Intent();
            intent.setType("image/*");
            intent.setAction(Intent.ACTION_GET_CONTENT);
            this.startActivityForResult(Intent.createChooser(intent, "Select Avatar"), SELECT_AVATAR_REQUEST_CODE);
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        if (requestCode == SELECT_AVATAR_REQUEST_CODE && resultCode == Activity.RESULT_OK) {
            Uri data = intent.getData();

            // Get image selected
            Bitmap bitmap;
            try {
                bitmap = MediaStore.Images.Media.getBitmap(this.getContentResolver(), data);
            } catch (IOException ignored) {
                Log.println(Log.ERROR, "Upload Failure", "failed to read file: " + ignored);
                return;
            }

            ImageView imageView = this.findViewById(R.id.avatar);
            imageView.setImageBitmap(bitmap);

            // TODO: upload avatar
        }
    }



}
