package ru.yellosoft_club.y_gpstracker;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.util.Pair;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.List;

public class friend_search extends AppCompatActivity {

    private DatabaseReference mDatabase;
    private DatabaseReference userReference;
    private DatabaseReference userFriendsReference;
    private ChildEventListener userFriendsListener;

    private CheckBox chek;
    private TextView history;
    private EditText udid;
    private EditText udid2;
    private EditText udid3;
    private Button date_butt;

    private List<Pair<String, UserFriend>> friends = new ArrayList<Pair<String, UserFriend>>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_friend_search);

        mDatabase = FirebaseDatabase.getInstance().getReference();
        userReference = mDatabase.child("users").child(FirebaseAuth.getInstance().getCurrentUser().getUid());
        userFriendsReference = userReference.child("friends");

        udid = (EditText) findViewById(R.id.udid);
        /*udid2 = (EditText) findViewById(R.id.udid2);
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
        });*/
    }

    @Override
    protected void onStart() {
        super.onStart();
        userFriendsListener = new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                friends.add(new Pair<String, UserFriend>(dataSnapshot.getKey(), dataSnapshot.getValue(UserFriend.class)));
            }

            @Override
            public void onChildChanged(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onChildRemoved(DataSnapshot dataSnapshot) {

            }

            @Override
            public void onChildMoved(DataSnapshot dataSnapshot, String s) {

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        };
        userFriendsReference.addChildEventListener(userFriendsListener);
    }

    @Override
    protected void onStop() {
        super.onStop();
        userFriendsReference.removeEventListener(userFriendsListener);
    }

    public void AddClick(View v) {
        if (TextUtils.isEmpty(udid.getText())) {
            udid.setError(("Введите udid пользователя"));
            return;
        }
        else {
        userFriendsReference.push().setValue(new UserFriend(udid.getText().toString()));
        Toast.makeText(this, "Пользователь добавлен в друзья! \uD83D\uDC4D \nСписок друзей можно посмотреть в меню 'Друзья'", Toast.LENGTH_SHORT).show();
        }
    }

    public void SearchClick(View v) {
        Intent intent = new Intent(this, save_friends.class);
        startActivity(intent);
        Toast.makeText(this, "Выберите друга для отображения его на карте \uD83C\uDF0D", Toast.LENGTH_SHORT).show();
    }


}
