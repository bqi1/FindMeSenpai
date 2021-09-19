package com.example.findmysenpai2;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.Settings;
import android.util.Base64;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.io.ByteArrayOutputStream;
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

        // If we have a avatar saved, load it!
        this.useSavedAvatar();
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

            this.uploadAvatar(bitmap);
        }
    }

    // TODO: For the sake of time, these methods are in this class. However, it would be preferred to have it in a separate class that handles specifically modifications to Senpai uwu
    protected void uploadAvatar(Bitmap bitmap) {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream);
        String base64 = Base64.encodeToString(byteArrayOutputStream.toByteArray(), 0);

        String deviceId = Settings.Secure.getString(this.getApplicationContext().getContentResolver(), Settings.Secure.ANDROID_ID);
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("users")
                .whereEqualTo("device", deviceId)
                .get().addOnSuccessListener(task -> {
                    task.forEach(document -> {
                        db.collection("users").document(document.getId()).update("base64Image", base64);
                    });
                });
    }

    protected void useSavedAvatar() {
        String deviceId = Settings.Secure.getString(this.getApplicationContext().getContentResolver(), Settings.Secure.ANDROID_ID);

        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("users")
                .whereEqualTo("device", deviceId)
                .get().addOnSuccessListener(task -> {
            if (task.getDocuments().size() > 0) {
                DocumentSnapshot documentSnapshot = task.getDocuments().get(0);
                String base64Avatar = documentSnapshot.getString("base64Image");
                if (base64Avatar.length() > 0) {
                    byte[] decoded = Base64.decode(base64Avatar, 0);
                    Bitmap image = BitmapFactory.decodeByteArray(decoded, 0, decoded.length);

                    ImageView imageView = this.findViewById(R.id.avatar);
                    imageView.setImageBitmap(image);
                }
            }
        });
    }



}
