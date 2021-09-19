package com.example.findmysenpai2;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.Settings;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

public class MainActivity extends AppCompatActivity {

    private static final int SELECT_AVATAR_REQUEST_CODE = 1338;

    private Bitmap bitmap;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d("TESTINGPASTA", "KILLLME");
        this.setContentView(R.layout.activity_main);
//
        final Button button = findViewById(R.id.save_button);
        final EditText roomText = (EditText) findViewById(R.id.room_name);
        final EditText displayNameText = (EditText) findViewById(R.id.display_name);
        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Code here executes on main thread after user presses button
                System.out.println(roomText.getText().toString() + " annnnd " + displayNameText.getText().toString());
                if ((roomText.getText().toString().matches("")) || (displayNameText.getText().toString().matches(""))){
                    Context context = getApplicationContext();
                    CharSequence text = "Fill in all fields. baka";
                    int duration = Toast.LENGTH_SHORT;
                    Toast toast = Toast.makeText(context, text, duration);
                    toast.show();
                } else {
                    addUser(roomText.getText().toString(), displayNameText.getText().toString());
                    Intent switchToMapActivity = new Intent(MainActivity.this, MapActivity.class);
                    MainActivity.this.startActivity(switchToMapActivity);
                }
            }
        });


        // Add listener for changing avatar
        ImageView avatarImage = this.findViewById(R.id.upload_avatar);
        avatarImage.setOnClickListener(view -> {
            Intent intent = new Intent();
            intent.setType("image/*");
            intent.setAction(Intent.ACTION_GET_CONTENT);
            this.startActivityForResult(Intent.createChooser(intent, "Select Avatar"), SELECT_AVATAR_REQUEST_CODE);
        });

        try (InputStream defaultStream = this.getAssets().open("default_avatar.PNG")) {
            Drawable drawable = Drawable.createFromStream(defaultStream, null);
            avatarImage.setImageDrawable(drawable);
        } catch (IOException ignored) {}
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

            ImageView imageView = this.findViewById(R.id.upload_avatar);
            imageView.setImageBitmap(bitmap);
            this.bitmap = bitmap;
        }
    }

//    @Override
//    protected void onStop(){
//        super.onStop();
//        final EditText roomText = (EditText) findViewById(R.id.room_name);
//        final EditText displayNameText = (EditText) findViewById(R.id.display_name);
//        removeUser(roomText.getText().toString(),displayNameText.getText().toString());
//    }
    public void addUser(String roomcode, String name){
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        Senpai user = new Senpai();
        user.device = Settings.Secure.getString(this.getApplicationContext().getContentResolver(), Settings.Secure.ANDROID_ID);
        user.name = name;
        user.roomCode = roomcode;

        // Upload avatar if present
        if (this.bitmap != null) {
            ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream);
            String base64 = Base64.encodeToString(byteArrayOutputStream.toByteArray(), 0);
            user.base64Image = base64;
        }

        // Delete old users associated with device id
        db.collection("users").whereEqualTo("device", user.device).get().addOnSuccessListener(task -> {
            task.getDocuments().forEach(document -> db.collection("users").document(document.getId()).delete());

            // Add new user
            db.collection("users")
                    .add(user)
                    .addOnSuccessListener(new OnSuccessListener<DocumentReference>() {
                        @Override
                        public void onSuccess(DocumentReference documentReference) {
                            Log.d("TESTINGPASTA", "DocumentSnapshot added with ID: " + documentReference.getId());
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.w("TESTINGPASTA", "Error adding document", e);
                        }
                    });
        });
    }

    public void removeUser(String roomcode, String device){
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("users")
                .whereEqualTo("device", device).whereEqualTo("roomCode",roomcode)
                .get()
                .addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<QuerySnapshot> task) {
                        if (task.isSuccessful()) {
                            for (QueryDocumentSnapshot document : task.getResult()) {
                                Log.d("TESTINGPASTA", document.getId() + " => " + document.getData());
                                document.getReference().delete()
                                        .addOnSuccessListener(new OnSuccessListener<Void>() {
                                            @Override
                                            public void onSuccess(Void aVoid) {
                                                Log.d("TESTINGPASTA", "DocumentSnapshot successfully deleted!");
                                            }
                                        })
                                        .addOnFailureListener(new OnFailureListener() {
                                            @Override
                                            public void onFailure(@NonNull Exception e) {
                                                Log.w("TESTINGPASTA", "Error deleting document", e);
                                            }
                                        });
                            }
                        } else {
                            Log.d("TESTINGPASTA", "Error getting documents: ", task.getException());
                        }
                    }
                });
    }

}