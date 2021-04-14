package texel.com.depoproject.Pages.DEPO;

import android.content.Intent;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.style.TextAppearanceSpan;
import android.view.Gravity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;

import texel.com.depoproject.Pages.AboutUs.AboutUsActivity;
import texel.com.depoproject.Pages.DEPO.Fragments.Backup.DepoBackupFragment;
import texel.com.depoproject.Pages.DEPO.Fragments.Buys.History.DepoBuysFragment;
import texel.com.depoproject.Pages.DEPO.Fragments.Debts.DepoDebtsFragment;
import texel.com.depoproject.Pages.DEPO.Fragments.Expenses.DepoExpensesFragment;
import texel.com.depoproject.Pages.DEPO.Fragments.Fabrics.DepoFabricsFragment;
import texel.com.depoproject.Pages.DEPO.Fragments.Markets.DepoMarketsFragment;
import texel.com.depoproject.Pages.DEPO.Fragments.Products.DepoProductsFragment;
import texel.com.depoproject.Pages.DEPO.Fragments.Profit.DepoProfitFragment;
import texel.com.depoproject.Pages.DEPO.Fragments.Return.Buys.History.DepoReturnBuysFragment;
import texel.com.depoproject.Pages.DEPO.Fragments.Return.Sells.DepoReturnSellsBySellerFragment;
import texel.com.depoproject.Pages.DEPO.Fragments.ReturnToDepo.DepoReturnToDepoFragment;
import texel.com.depoproject.Pages.DEPO.Fragments.Sells.DepoSellsBySellerFragment;
import texel.com.depoproject.Pages.DEPO.Fragments.Statistics.DepoStatisticsFragment;
import texel.com.depoproject.Pages.DEPO.Fragments.Texture.Users.DepoTextureUsersFragment;
import texel.com.depoproject.Pages.DEPO.Fragments.Users.DepoUsersFragment;
import texel.com.depoproject.Pages.SignActivities.SignIn;
import texel.com.depoproject.Pages.SignActivities.SignUp;
import texel.com.depoproject.R;

public class DepoProfile extends AppCompatActivity implements NavigationView.OnNavigationItemSelectedListener {

    private DrawerLayout drawer;
    private TextView toolBarTitle;
    private int selected_menu = R.id.navigation_markets;

    private final BottomNavigationView.OnNavigationItemSelectedListener navigationItemSelectedListener =
            item -> {
                if (selected_menu != item.getItemId()) {
                    selected_menu = item.getItemId();
                    switch (item.getItemId()) {
                        case R.id.navigation_texture:
                            openFragment(new DepoTextureUsersFragment(), getResources().getString(R.string.textures));
                            return true;
                        case R.id.navigation_buys:
                            openFragment(new DepoBuysFragment(), getResources().getString(R.string.buys));
                            return true;
                        case R.id.navigation_sells:
                            openFragment(new DepoSellsBySellerFragment(), getResources().getString(R.string.sells));
                            return true;
                        case R.id.navigation_return_to_depo:
                            openFragment(new DepoReturnToDepoFragment(), "İadə");
                            return true;
                    }
                }
                return false;
            };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_depo_profile);

        setNavigationDrawers();

        BottomNavigationView bottomNavigation = findViewById(R.id.bottom_navigation_depo);
        bottomNavigation.setOnNavigationItemSelectedListener(navigationItemSelectedListener);

        ImageButton depoToolbarSecondMenuButton = findViewById(R.id.depoToolbarSecondMenuButton);
        depoToolbarSecondMenuButton.setVisibility(View.VISIBLE);
        depoToolbarSecondMenuButton.setOnClickListener(v -> {
            if (drawer.isDrawerOpen(GravityCompat.END))
                drawer.closeDrawer(GravityCompat.END);
            else drawer.openDrawer(GravityCompat.END);
        });

        Toolbar toolbar = findViewById(R.id.toolbarDepo);
        drawer = findViewById(R.id.drawerDepo);
        toolBarTitle = drawer.findViewById(R.id.depoToolbarTitle);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(this, drawer, toolbar, R.string.open, R.string.close);
        drawer.addDrawerListener(toggle);
        toggle.setDrawerIndicatorEnabled(true);
        toggle.getDrawerArrowDrawable().setColor(getResources().getColor(R.color.white));
        toggle.syncState();

        openFragment(new DepoTextureUsersFragment(), getResources().getString(R.string.textures));
    }

    private void setNavigationDrawers() {
        NavigationView navigationView = findViewById(R.id.nav_view_depo);
        navigationView.setNavigationItemSelectedListener(this);
        Menu myMenu = navigationView.getMenu();

        MenuItem debt_title = myMenu.findItem(R.id.nav_debts_title);
        SpannableString s = new SpannableString(debt_title.getTitle());
        s.setSpan(new TextAppearanceSpan(this, R.style.NavMenuCategoryTitleStyle), 0, s.length(), 0);
        debt_title.setTitle(s);

        MenuItem return_title = myMenu.findItem(R.id.nav_return_title);
        s = new SpannableString(return_title.getTitle());
        s.setSpan(new TextAppearanceSpan(this, R.style.NavMenuCategoryTitleStyle), 0, s.length(), 0);
        return_title.setTitle(s);

        NavigationView navigationViewSecond = findViewById(R.id.nav_view_depo_second);
        navigationViewSecond.setNavigationItemSelectedListener(this);
    }

    public void openFragment(Fragment fragment, String name) {
        if (drawer.isDrawerOpen(GravityCompat.END))
            drawer.closeDrawer(GravityCompat.END);

        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.container, fragment);
        transaction.commit();

        toolBarTitle.setText(name);
    }

    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        selected_menu = item.getItemId();
        if (selected_menu == R.id.nav_profits)
            openFragment(new DepoProfitFragment(), getResources().getString(R.string.profits));
        else if (selected_menu == R.id.nav_expenses)
            openFragment(new DepoExpensesFragment(), getResources().getString(R.string.expenses));
        else if (selected_menu == R.id.nav_return_buys)
            openFragment(new DepoReturnBuysFragment(),
                    getResources().getString(R.string.return_product) + " - Zavoda");
        else if (selected_menu == R.id.nav_return_sells)
            openFragment(new DepoReturnSellsBySellerFragment(),
                    getResources().getString(R.string.return_product) + " - Marketdən");
        else if (selected_menu == R.id.nav_debt_fabric)
            openFragment(new DepoDebtsFragment(true),
                    getResources().getString(R.string.debts) + " - " +
                            getResources().getString(R.string.fabrics));
        else if (selected_menu == R.id.nav_debt_market)
            openFragment(new DepoDebtsFragment(false),
                    getResources().getString(R.string.debts) + " - " +
                            getResources().getString(R.string.markets));
        else if (selected_menu == R.id.nav_markets)
            openFragment(new DepoMarketsFragment(), getResources().getString(R.string.markets));
        else if (selected_menu == R.id.nav_products)
            openFragment(new DepoProductsFragment(), getResources().getString(R.string.products));
        else if (selected_menu == R.id.nav_fabrics)
            openFragment(new DepoFabricsFragment(), getResources().getString(R.string.fabrics));
        else if (selected_menu == R.id.nav_backup)
            openFragment(new DepoBackupFragment(), getResources().getString(R.string.backup));
        else if (selected_menu == R.id.nav_statistics)
            openFragment(new DepoStatisticsFragment(), getResources().getString(R.string.statistics));
        else if (selected_menu == R.id.nav_users)
            openFragment(new DepoUsersFragment(), getResources().getString(R.string.users));
        else if (selected_menu == R.id.nav_create_user)
            startActivity(new Intent(this, SignUp.class));
        else if (item.getItemId() == R.id.nav_about_us)
            startActivity(new Intent(this, AboutUsActivity.class));
        else if (selected_menu == R.id.nav_log_out) {
            {
                FirebaseAuth.getInstance().signOut();
                finish();
                startActivity(new Intent(this, SignIn.class));
            }
        }
        drawer.closeDrawer(Gravity.LEFT);
        return true;
    }
}