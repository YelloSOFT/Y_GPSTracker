package ru.yellosoft_club.y_gpstracker;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.EditText;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import static ru.yellosoft_club.y_gpstracker.R.id.et_email;
import static ru.yellosoft_club.y_gpstracker.R.id.uid;

public class friend_search extends AppCompatActivity {

    private DatabaseReference mDatabase;
    private DatabaseReference userReference;
    private DatabaseReference userLocationReference;
    private ChildEventListener userLocationListener;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friend_search);

        mDatabase = FirebaseDatabase.getInstance().getReference();
        userReference = mDatabase.child("users").child(FirebaseAuth.getInstance().getCurrentUser().getUid());
        userLocationReference = userReference.child("friends");
    }
    //


}
