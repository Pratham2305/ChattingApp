package com.example.chattingapp;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;

class TabPagerAdapter extends FragmentPagerAdapter {

    public TabPagerAdapter(FragmentManager fm) {
        super(fm);
    }

    @Override
    public Fragment getItem(int position) {

        switch (position){
            case 0:
                RequestsFrag requestsFrag = new RequestsFrag();
                return requestsFrag;
            case 1:
                ChatFrag chatFrag = new ChatFrag();
                return chatFrag;
            case 2:
                FriendsFrag friendsFrag = new FriendsFrag();
                return friendsFrag;
            default:
                return null;
        }

    }

    @Override
    public int getCount() {
        return 3;
    }
    public CharSequence getPageTitle(int position){
        switch (position){
            case 0:
                return "REQUESTS";
            case 1:
                return "CHATS";
            case 2:
                return "FRIENDS";
            default:
                return null;
        }
    }
}
