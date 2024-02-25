package com.example.foodyz.personal;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import android.os.Bundle;
import android.view.MenuItem;

import com.example.foodyz.R;
import com.example.foodyz.databinding.ActivityPersonalMainBinding;
import com.google.android.material.navigation.NavigationBarView;


public class Personal_MainActivity extends AppCompatActivity {

    ActivityPersonalMainBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        binding = ActivityPersonalMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        replaceFragment(new SearchFragment());

        binding.personalBottomNavigationView.setOnItemSelectedListener(new NavigationBarView.OnItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                int id = item.getItemId();

                if (id == R.id.search) {
                    // Handle the search button click
                    replaceFragment(new SearchFragment());
                } else if (id == R.id.history) {
                    replaceFragment(new HistoryFragment()); // Replace with HistoryFragment
                } else if (id == R.id.personal_profile) {
                    replaceFragment(new Personal_ProfileFragment());
                }

                return true;
            }

        });
    }

    public void replaceFragment(Fragment fragment) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.personal_frame_layout, fragment);
        fragmentTransaction.commit();
    }


}
