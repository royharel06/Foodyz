package com.example.foodyz.business;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.os.Bundle;
import android.view.MenuItem;

import com.example.foodyz.R;
import com.example.foodyz.databinding.ActivityBusinessMainBinding;
import com.example.foodyz.databinding.ActivityPersonalMainBinding;
import com.example.foodyz.personal.HistoryFragment;
import com.example.foodyz.personal.Personal_ProfileFragment;
import com.example.foodyz.personal.SearchFragment;
import com.google.android.material.navigation.NavigationBarView;

public class Business_MainActivity extends AppCompatActivity {

    ActivityBusinessMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityBusinessMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        replaceFragment(new OrdersFragment());

        binding.businessBottomNavigationView.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int id = item.getItemId();

                if (id == R.id.orders) {
                    replaceFragment(new OrdersFragment());
                } else if (id == R.id.edit_menu) {
                    replaceFragment(new EditMenuFragment());
                } else if (id == R.id.business_profile) {
                    replaceFragment(new Business_ProfileFragment());
                }

                return true;
            }
        });
    }

    private void replaceFragment(Fragment fragment) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.business_frame_layout, fragment);
        fragmentTransaction.commit();
    }
}