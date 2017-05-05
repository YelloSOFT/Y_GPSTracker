package ru.yellosoft_club.y_gpstracker;

import java.util.ArrayList;
import java.util.List;

public enum SelectedFriends {
    INSTANCE;

    private List<UserFriend> selectedFriends = new ArrayList<>();

    public static SelectedFriends getInstance() {
        return INSTANCE;
    }

    public void addFriend(UserFriend friend) {
        selectedFriends.add(friend);
    }

    public List<UserFriend> getSelectedFriends() {
        return new ArrayList<>(selectedFriends);
    }

}
