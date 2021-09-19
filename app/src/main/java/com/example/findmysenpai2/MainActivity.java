package com.example.findmysenpai2;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d("TESTINGPASTA", "KILLLME");
        this.setContentView(R.layout.activity_main);
//        Intent switchToMapActivity = new Intent(this, MapActivity.class);
//        this.startActivity(switchToMapActivity);
        addUser("ABC123", "Weiwei");
//        removeUser("ABC123", "Weiwei");


    }
    public void addUser(String roomcode, String name){
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        Senpai user = new Senpai();
        user.device = Settings.Secure.getString(this.getApplicationContext().getContentResolver(), Settings.Secure.ANDROID_ID);
        user.name = name;
        user.roomCode = roomcode;

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