package texel.com.depoproject.Pages.SELLER.Activities.CreateSell;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;

import texel.com.depoproject.CustomDialogs.CustomProgressDialog;
import texel.com.depoproject.DataClasses.ProductInfo;
import texel.com.depoproject.HelperClasses.DatabaseFunctions;
import texel.com.depoproject.HelperClasses.SharedClass;
import texel.com.depoproject.Pages.SELLER.Activities.Basket.SellerBasketActivity;
import texel.com.depoproject.R;

public class CreateSellActivity extends AppCompatActivity {

    public static boolean isNewSell;
    private final HashMap<String, ProductInfo> cardViewProductInfoHashMap = new HashMap<>();
    private final ArrayList<String> nameList = new ArrayList<>();
    private final ArrayList<String> searchNameList = new ArrayList<>();
    private CustomProgressDialog progressDialog;
    private TextView textViewNoItemInfo;
    private DatabaseReference databaseReferenceProducts;
    private CreateSellProductsRecycler adapter;
    private String market_id_name, market_name;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_create_sell);

        Toolbar toolbar = findViewById(R.id.toolbarSeller);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_arrow_back_white);
        toolbar.setTitleTextColor(Color.WHITE);
        getSupportActionBar().setTitle(getResources().getString(R.string.products));

        market_name = getIntent().getStringExtra("market_name");
        market_id_name = getIntent().getStringExtra("market_id_name");
        isNewSell = getIntent().getBooleanExtra("isNewSell", true);

        textViewNoItemInfo = findViewById(R.id.textViewNoItemInfo);
        textViewNoItemInfo.setText("Qeydiyyatda Olan Məhsul Yoxdur");

        progressDialog = new CustomProgressDialog(this, getString(R.string.data_loading));
        progressDialog.show();

        databaseReferenceProducts = DatabaseFunctions.getDatabases(this).get(0).child("PRODUCTS");

        configureListViewProducts();
        configureSearchEditText();
        configureBasketButton();
    }

    private void configureBasketButton() {
        ImageButton basketButton = findViewById(R.id.buttonBasket);
        basketButton.setOnClickListener(v -> {
            Intent intent = new Intent(getApplicationContext(), SellerBasketActivity.class);
            intent.putExtra("market", market_name);
            intent.putExtra("market_id_name", market_id_name);
            intent.putExtra("isNewSell", isNewSell);
            startActivity(intent);
        });
    }

    private void configureSearchEditText() {
        EditText etSearch = findViewById(R.id.editTextSearch);
        SharedClass.configureSearchEditText(etSearch, nameList, searchNameList, adapter);
    }

    private void configureListViewProducts() {
        adapter = new CreateSellProductsRecycler(this, nameList, searchNameList, cardViewProductInfoHashMap);
        RecyclerView myView = findViewById(R.id.recyclerviewProducts);
        myView.setHasFixedSize(true);
        myView.setAdapter(adapter);
        LinearLayoutManager llm = new LinearLayoutManager(this);
        llm.setOrientation(LinearLayoutManager.VERTICAL);
        myView.setLayoutManager(llm);

        loadMarketsFromDatabase();
    }

    private void loadMarketsFromDatabase() {
        databaseReferenceProducts.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                cardViewProductInfoHashMap.clear();
                nameList.clear();
                searchNameList.clear();

                for (DataSnapshot snapshotTaxis : dataSnapshot.getChildren()) {
                    Boolean active = snapshotTaxis.child("active").getValue(Boolean.class);
                    if (active == null || !active) continue;

                    String name = snapshotTaxis.getKey();
                    nameList.add(name);

                    String _name = snapshotTaxis.child("name").getValue(String.class);
                    Double _buyPrice = snapshotTaxis.child("buyPrice").getValue(Double.class);
                    Double _sellPrice = snapshotTaxis.child("sellPrice").getValue(Double.class);

                    cardViewProductInfoHashMap.put(name,
                            new ProductInfo(_name, _buyPrice, _sellPrice, true));
                }

                if (nameList.size() > 0) textViewNoItemInfo.setVisibility(View.GONE);
                else textViewNoItemInfo.setVisibility(View.VISIBLE);

                progressDialog.dismiss();
                searchNameList.addAll(nameList);
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                progressDialog.dismiss();
            }
        });
    }


    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}