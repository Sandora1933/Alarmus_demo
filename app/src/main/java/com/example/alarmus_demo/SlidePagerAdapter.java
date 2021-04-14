package com.example.alarmus_demo;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;
import androidx.viewpager2.adapt

import java.util.List;

public class SlidePagerAdapter extends FragmentStateAdapter {

    private List<Fragment> fragmentList;

    public SlidePagerAdapter(FragmentManager manager, List<Fragment> list){
        super(manager);
        this.fragmentList = list;
    }

    @NonNull
    @Override
    public Fragment getItem(int position) {
        return null;
    }

    @Override
    public int getCount() {
        return 0;
    }
}
