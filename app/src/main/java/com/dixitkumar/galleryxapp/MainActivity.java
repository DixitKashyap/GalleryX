package com.dixitkumar.galleryxapp;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import android.os.Bundle;

import com.dixitkumar.galleryxapp.databinding.ActivityMainBinding;
import com.google.android.material.tabs.TabLayout;

public class MainActivity extends AppCompatActivity {

   private ActivityMainBinding mainBinding;
   private Fragment_Adapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mainBinding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(mainBinding.getRoot());

        //Creating Fragment Adapter
        adapter = new Fragment_Adapter(getSupportFragmentManager(),getLifecycle());
        mainBinding.viewPager2.setAdapter(adapter);


        //Setting Up LabLayout Listener

        mainBinding.tabLayout.setOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                mainBinding.viewPager2.setCurrentItem(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });

        mainBinding.viewPager2.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
              mainBinding.tabLayout.selectTab(mainBinding.tabLayout.getTabAt(position));
            }
        });
    }
    @Override
    public void onBackPressed() {
        if (mainBinding.viewPager2.getCurrentItem() != 0) {
            // If the current view is not the first page,
            // then navigate to the previous page
            mainBinding.viewPager2.setCurrentItem(mainBinding.viewPager2.getCurrentItem() - 1);
        } else {
            // If the current view is the first page,
            // then proceed with the normal handling of the back button (i.e., close the app)
            super.onBackPressed();
            onDestroy();
        }
    }

}