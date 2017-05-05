package ru.yellosoft_club.y_gpstracker;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

public class FriendsAdapter  extends RecyclerView.Adapter<UserFriendHolder> {

    private List<UserFriend> friends;

    public FriendsAdapter() {
        this.friends = new ArrayList<>();
    }

    public void addFriend(UserFriend friend) {
        this.friends.add(friend);
        notifyDataSetChanged();
    }

    @Override
    public UserFriendHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View cell = LayoutInflater.from(parent.getContext()).inflate(android.R.layout.simple_list_item_1, parent, false);
        UserFriendHolder holder = new UserFriendHolder(cell);
        holder.setOnFriendClickListener(new OnFriendClickedListener() {
            @Override
            public void onFriendClicked(UserFriend friend) {
                if (onFriendClickListener != null) {
                    onFriendClickListener.onFriendClicked(friend);
                }
            }
        });
        return holder;
    }

    @Override
    public void onBindViewHolder(UserFriendHolder holder, int position) {
        holder.setFriend(friends.get(position));
    }

    @Override
    public int getItemCount() {
        return friends.size();
    }

    private OnFriendClickedListener onFriendClickListener;

    public OnFriendClickedListener getOnFriendClickListener() {
        return onFriendClickListener;
    }

    public void setOnFriendClickListener(OnFriendClickedListener onFriendClickListener) {
        this.onFriendClickListener = onFriendClickListener;
    }
}
