package texel.com.depoproject.Pages.BUYER;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.firebase.auth.FirebaseAuth;

import texel.com.depoproject.Pages.BUYER.Fragments.BuyerProductsFragment;
import texel.com.depoproject.Pages.SignActivities.SignIn;
import texel.com.depoproject.R;

public class BuyerProfile extends AppCompatActivity {
    private int selected_menu = R.id.navigation_home;
    private final BottomNavigationView.OnNavigationItemSelectedListener navigationItemSelectedListener =
            item -> {
                if (selected_menu != item.getItemId()) {
                    selected_menu = item.getItemId();
                    switch (item.getItemId()) {
                        case R.id.navigation_home:
                            openFragment(new BuyerProductsFragment());
                            return true;
                        case R.id.navigation_log_out:
                            FirebaseAuth.getInstance().signOut();
                            finish();
                            startActivity(new Intent(this, SignIn.class));
                            return true;
                    }
                }
                return false;
            };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_buyer_profile);

        BottomNavigationView bottomNavigation = findViewById(R.id.bottom_navigation_buyer);
        bottomNavigation.setOnNavigationItemSelectedListener(navigationItemSelectedListener);

        openFragment(new BuyerProductsFragment());
    }

    public void openFragment(Fragment fragment) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.container, fragment);
        transaction.commit();
    }
}