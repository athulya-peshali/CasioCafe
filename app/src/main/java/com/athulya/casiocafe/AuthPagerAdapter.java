package com.athulya.casiocafe;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

public class AuthPagerAdapter extends FragmentStateAdapter {

    public AuthPagerAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        if (position == 1) {
            return new SignUpFragment();
        }
        return new SignInFragment();
    }

    @Override
    public int getItemCount() {
        return 2; // We have two tabs: Sign In and Sign Up
    }
}