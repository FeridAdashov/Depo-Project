package texel.com.depoproject.Pages.SELLER;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.MenuItem;
import android.widget.DatePicker;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import texel.com.depoproject.CustomDialogs.CustomProgressDialog;
import texel.com.depoproject.HelperClasses.DatabaseFunctions;
import texel.com.depoproject.HelperClasses.SharedClass;
import texel.com.depoproject.Pages.AboutUs.AboutUsActivity;
import texel.com.depoproject.Pages.SELLER.Fragments.Expenses.SellerExpensesFragment;
import texel.com.depoproject.Pages.SELLER.Fragments.Markets.SellerMarketsFragment;
import texel.com.depoproject.Pages.SELLER.Fragments.ReturnToDepo.SellerReturnToDepoFragment;
import texel.com.depoproject.Pages.SignActivities.SignIn;
import texel.com.depoproject.Printer.PrinterActivity;
import texel.com.depoproject.R;

public class SellerProfile extends AppCompatActivity implements
        NavigationView.OnNavigationItemSelectedListener, DatePickerDialog.OnDateSetListener {

    private Activity activity;

    private final BottomNavigationView.OnNavigationItemSelectedListener navigationItemSelectedListener =
            item -> {
                switch (item.getItemId()) {
                    case R.id.navigation_markets:
                        openFragment(new SellerMarketsFragment());
                        return true;
                    case R.id.navigation_return_to_depo:
                        openFragment(new SellerReturnToDepoFragment());
                        return true;
                    case R.id.navigation_expenses:
                        openFragment(new SellerExpensesFragment());
                        return true;
                    case R.id.navigation_texture:
                        SharedClass.showDatePickerDialog(activity, this);
                        return true;
                }
                return false;
            };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_seller_profile);

        activity = this;

        BottomNavigationView bottomNavigation = findViewById(R.id.bottom_navigation_seller);
        bottomNavigation.setOnNavigationItemSelectedListener(navigationItemSelectedListener);

        NavigationView navigationView = findViewById(R.id.nav_view_seller);
        navigationView.setNavigationItemSelectedListener(this);

        Toolbar toolbar = findViewById(R.id.toolbarSeller);
        DrawerLayout drawer = findViewById(R.id.drawerSeller);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string.open, R.string.close);
        drawer.addDrawerListener(toggle);
        toggle.setDrawerIndicatorEnabled(true);
        toggle.getDrawerArrowDrawable().setColor(getResources().getColor(R.color.white));
        toggle.syncState();

        openFragment(new SellerMarketsFragment());
    }

    public void openFragment(Fragment fragment) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.container, fragment);
        transaction.commit();
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.nav_log_out) {
            {
                FirebaseAuth.getInstance().signOut();
                finish();
                startActivity(new Intent(this, SignIn.class));
            }
        } else if (item.getItemId() == R.id.nav_about_us) {
            startActivity(new Intent(this, AboutUsActivity.class));
        }
        return true;
    }

    private void openPrinterActivity(String date) {
        CustomProgressDialog progressDialog = new CustomProgressDialog(activity, getString(R.string.data_loading));
        progressDialog.show();

        String user = FirebaseAuth.getInstance().getCurrentUser().getEmail().split("@")[0];
        if (user == null) return;

        DatabaseReference dr = DatabaseFunctions.getDatabases(activity).get(0);
        dr.child("USERS/SELLER/" + user + "/ABOUT/name").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String name = snapshot.getValue(String.class);
                if (TextUtils.isEmpty(name)) return;

                dr.child("SELLS/" + date + "/" + user).addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        Double sum = snapshot.child("sum").getValue(Double.class);
                        Double percent = snapshot.child("percentSum").getValue(Double.class);
                        if (sum == null) sum = 0.;
                        if (percent == null) percent = 0.;

                        String message =
                                "Gun:             " + date +
                                "\nUmumi cem:       " + sum +
                                "\nFaizin miqdari:  " + percent +
                                "\nYekun Netice:    " + (sum - percent);

                        dr.child("EXPENSES/Other/" + user + "/" + date + "/sum").addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshot) {
                                Double sumExpenses = snapshot.getValue(Double.class);
                                if (sumExpenses == null) sumExpenses = 0.;

                                progressDialog.dismiss();

                                Intent intent = new Intent(activity, PrinterActivity.class);
                                intent.putExtra("message_1", name);
                                intent.putExtra("message_2",
                                        message + "\nXercler:         " + sumExpenses);
                                startActivity(intent);
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {
                                progressDialog.dismiss();
                            }
                        });
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        progressDialog.dismiss();
                    }
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                progressDialog.dismiss();
            }
        });
    }

    @Override
    public void onDateSet(DatePicker datePicker, int year, int month, int day) {
        ++month;
        String m = month < 10 ? "0" + month : "" + month;
        String d = day < 10 ? "0" + day : "" + day;

        openPrinterActivity(year + "_" + m + "_" + d);
    }
}