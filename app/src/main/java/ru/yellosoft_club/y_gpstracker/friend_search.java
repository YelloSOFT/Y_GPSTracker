package ru.yellosoft_club.y_gpstracker;

import android.content.Intent;
import android.support.v4.util.Pair;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.List;


public class friend_search extends AppCompatActivity {

    private DatabaseReference mDatabase;
    private DatabaseReference userReference;
    private DatabaseReference userFriendsReference;
    private ChildEventListener userLocationListener;


    private CheckBox chek;
    private TextView history;
    private EditText udid;
    private EditText udid2;
    private EditText udid3;
    private Button date_butt;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friend_search);

        //mDatabase = FirebaseDatabase.getInstance().getReference();
        //userReference = mDatabase.child("users").child(FirebaseAuth.getInstance().getCurrentUser().getUid());
        //userLocationReference = userReference.child("friends");

        udid  = (EditText) findViewById(R.id.udid);
        udid2 = (EditText) findViewById(R.id.udid2);
        udid3 = (EditText) findViewById(R.id.udid3);
        date_butt = (Button) findViewById(R.id.date_butt);
        history = (TextView) findViewById(R.id.textView1);
        chek = (CheckBox) findViewById(R.id.checkBox);

        chek.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (chek.isChecked()) {
                    history.setVisibility(View.VISIBLE);
                    udid2.setVisibility(View.VISIBLE);
                    udid3.setVisibility(View.VISIBLE);
                    date_butt.setVisibility(View.VISIBLE);
                }
                else
                {
                    history.setVisibility(View.INVISIBLE);
                    udid2.setVisibility(View.INVISIBLE);
                    udid3.setVisibility(View.INVISIBLE);
                    date_butt.setVisibility(View.INVISIBLE);
                }
            }
        });
    }

    public class Friends {
        public String userId;
        public String friends;

        public Friends(String s) {
            // Default constructor required for calls to DataSnapshot.getValue(User.class)
        }
        public Friends(String usrId, String friends) {
            this.userId = usrId;
            this.friends = friends;
        }

    }

    private void writeNewUser(String userId, String friends) {
        Friends user = new Friends(userId, friends);

        userReference = mDatabase.child("users").child(FirebaseAuth.getInstance().getCurrentUser().getUid());
        userFriendsReference = userReference.child("friends");
    }
//Возвращает пустоту..
    public void AddClick(View v)  {
        userFriendsReference.push().setValue(new Friends(udid.getText().toString()));
    }

}

