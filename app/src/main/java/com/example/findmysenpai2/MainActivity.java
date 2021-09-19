package com.example.findmysenpai2;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.example.findmysenpai2.Senpai;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d("TESTINGPASTA", "KILLLME");
        this.setContentView(R.layout.activity_main);
//        Intent switchToMapActivity = new Intent(this, MapActivity.class);
//        this.startActivity(switchToMapActivity);
        addUser("ABC123", "Weiwei");


    }
    public void addUser(String roomcode, String name){
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        Senpai user = new Senpai();
        user.setName(name);
        user.setRoomCode(roomcode);
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

    }

}