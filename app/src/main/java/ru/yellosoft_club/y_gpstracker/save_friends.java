package ru.yellosoft_club.y_gpstracker;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

public class save_friends extends AppCompatActivity {

    private RecyclerView recyclerView;

    private DatabaseReference mDatabase;
    private DatabaseReference userReference;
    private DatabaseReference userFriendsReference;
    private ChildEventListener userFriendsListener;

    private FriendsAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_save_friends);

        recyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        adapter = new FriendsAdapter();
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.setAdapter(adapter);

        mDatabase = FirebaseDatabase.getInstance().getReference();
        userReference = mDatabase.child("users").child(FirebaseAuth.getInstance().getCurrentUser().getUid());
        userFriendsReference = userReference.child("friends");

        adapter.setOnFriendClickListener(new OnFriendClickedListener() {
            @Override
            public void onFriendClicked(UserFriend friend) {
                SelectedFriends.getInstance().addFriend(friend);
                finish();
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        userFriendsListener = new ChildEventListener() {
            @Override
            public void onChildAdded(DataSnapshot dataSnapshot, String s) {
                UserFriend friend = dataSnapshot.getValue(UserFriend.class);
                adapter.addFriend(friend);
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


}
