package com.example.shon.boost4.listener;

import android.support.v7.widget.RecyclerView;

import com.firebase.client.ChildEventListener;
import com.firebase.client.DataSnapshot;
import com.firebase.client.FirebaseError;
import com.example.shon.boost4.entity.User;
import com.example.shon.boost4.adapter.UsersRecAdapter;
import com.example.shon.boost4.MainActivity;

public class UserChildEventListner implements ChildEventListener {

    private RecyclerView mRecView;
    private UsersRecAdapter usersRecAdapter;

    public UserChildEventListner(RecyclerView recView) {
        this.mRecView = recView;
        this.usersRecAdapter = (UsersRecAdapter) recView.getAdapter();
    }

    @Override
    public void onChildAdded(DataSnapshot dataSnapshot, String s) {
        usersRecAdapter.add(dataSnapshot.getValue(User.class));
        mRecView.scrollToPosition(MainActivity.FIRST_SAMPLE_POS);
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
    public void onCancelled(FirebaseError firebaseError) {

    }
}
