package com.example.shon.boost4.fragment;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.example.shon.boost4.MainActivity;
import com.example.shon.boost4.R;
import com.example.shon.boost4.adapter.UsersRecAdapter;
import com.example.shon.boost4.entity.User;
import com.example.shon.boost4.listener.UserChildEventListner;

import java.util.ArrayList;
import java.util.List;

public class Users extends Fragment {

    private List<User> mUsers = new ArrayList<>();

    private RecyclerView mRecView;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        View user = inflater.inflate(R.layout.frag_users, container, false);
        mRecView = (RecyclerView) user.findViewById(R.id.rv_users);
        mRecView.setLayoutManager(new LinearLayoutManager(getContext()));
        mRecView.setAdapter(new UsersRecAdapter(mUsers));

        if(MainActivity.sUsersRef != null) {
            MainActivity.uChildEventListener = MainActivity.sUsersRef
                    .addChildEventListener(new UserChildEventListner(mRecView));
        }

        return user;
    }
}
