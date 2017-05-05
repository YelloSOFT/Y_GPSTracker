package ru.yellosoft_club.y_gpstracker;

import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.TextView;

public class UserFriendHolder extends RecyclerView.ViewHolder {

    private UserFriend friend;

    private TextView friendNameView;

    public UserFriendHolder(View itemView) {
        super(itemView);
        friendNameView = (TextView) itemView.findViewById(android.R.id.text1);
        itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (onFriendClickListener != null) {
                    onFriendClickListener.onFriendClicked(friend);
                }
            }
        });
    }

    public UserFriend getFriend() {
        return friend;
    }

    public void setFriend(UserFriend friend) {
        this.friend = friend;
        friendNameView.setText(friend.getUdid());
    }

    private OnFriendClickedListener onFriendClickListener;

    public OnFriendClickedListener getOnFriendClickListener() {
        return onFriendClickListener;
    }

    public void setOnFriendClickListener(OnFriendClickedListener onFriendClickListener) {
        this.onFriendClickListener = onFriendClickListener;
    }
}
