package com.dripblood.myapplication.adapters;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.lifecycle.Lifecycle;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.dripblood.myapplication.fragments.FamilyTabFragment;
import com.dripblood.myapplication.fragments.MyselfTabFragment;

public class PostAdapter extends FragmentStateAdapter {

    private Context context;
    int totalTabs;

    public PostAdapter(@NonNull FragmentManager fragmentManager, @NonNull Lifecycle lifecycle, Context context, int totalTabs) {
        super(fragmentManager, lifecycle);
        this.context = context;
        this.totalTabs = totalTabs;
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch(position){
            case 0:
                MyselfTabFragment myselfTabFragment = new MyselfTabFragment();
                return myselfTabFragment;
            case 1:
                FamilyTabFragment familyTabFragment = new FamilyTabFragment();
                return familyTabFragment;
            default:
                return null;
        }
    }

    @Override
    public int getItemCount() {
        return totalTabs;
    }
}
