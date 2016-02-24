package com.example.shon.boost4.adapter;

import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.example.shon.boost4.MainActivity;
import com.example.shon.boost4.R;
import com.example.shon.boost4.entity.User;

import java.util.List;

public class UsersRecAdapter extends RecyclerView.Adapter<UsersRecAdapter.ViewHolder> {

    private  List<User> mUsers;

    public UsersRecAdapter(List<User> users) {
        this.mUsers = users;
    }

    @Override
    public UsersRecAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {

        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.card_user, parent, false);

        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(UsersRecAdapter.ViewHolder holder, int position) {
        holder.mUserName.setText("Name: " + String.valueOf(mUsers.get(position).getDisplayName()));
        holder.mUserProvider.setText("Provider: " + String.valueOf(mUsers.get(position).getProvider()));
    }
    public void add(User user){
        mUsers.add(MainActivity.FIRST_SAMPLE_POS, user);
        notifyItemInserted(MainActivity.FIRST_SAMPLE_POS);
    }

    @Override
    public int getItemCount() {
        return mUsers.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        private TextView mUserName;
        private TextView mUserProvider;

        public ViewHolder(View itemView) {
            super(itemView);
            mUserName = (TextView) itemView.findViewById(R.id.tv_user_name);
            mUserProvider = (TextView) itemView.findViewById(R.id.tv_provider);
        }
    }
}