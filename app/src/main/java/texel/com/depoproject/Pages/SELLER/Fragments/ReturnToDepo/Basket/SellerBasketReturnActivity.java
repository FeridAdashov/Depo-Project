package texel.com.depoproject.Pages.SELLER.Fragments.ReturnToDepo.Basket;

import android.app.Activity;
import android.app.AlertDialog;
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

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Objects;

import texel.com.depoproject.HelperClasses.CustomDateTime;
import texel.com.depoproject.HelperClasses.DatabaseFunctions;
import texel.com.depoproject.HelperClasses.SharedClass;
import texel.com.depoproject.R;
import texel.com.depoproject.SqlLiteDatabase.DatabaseHelper;
import texel.com.depoproject.SqlLiteDatabase.Product;

public class SellerBasketReturnActivity extends AppCompatActivity {

    private final HashMap<String, Product> cardViewProductHashMap = new HashMap<>();
    private final ArrayList<String> nameList = new ArrayList<>();
    private final ArrayList<String> searchNameList = new ArrayList<>();
    private TextView textViewNoItemInfo;
    private TextView textViewResultPure, textViewResultRotten;
    private SellerBasketProductsRecycler adapter;
    private Activity activity;

    private DatabaseHelper helper;

    private double resultPure = 0.0, resultRotten = 0.0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_seller_return_basket);

        activity = this;
        helper = new DatabaseHelper(this, DatabaseHelper.DATABASE_SELLER_PURE_ROTTEN);

        Toolbar toolbar = findViewById(R.id.toolbarSeller);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_arrow_back_white);
        toolbar.setTitleTextColor(Color.WHITE);
        getSupportActionBar().setTitle(getResources().getString(R.string.basket));

        textViewNoItemInfo = findViewById(R.id.textViewNoItemInfo);
        textViewNoItemInfo.setText("Səbət Boşdur");

        textViewResultPure = findViewById(R.id.textViewSumPure);
        textViewResultRotten = findViewById(R.id.textViewSumRotten);

        configureListViewProducts();
        configureSearchEditText();
        configureClearButton();
        configureNoteReturnToDepoButton();
    }

    private void configureNoteReturnToDepoButton() {
        ImageButton noteReturnToDepo = findViewById(R.id.noteReturnToDepo);
        noteReturnToDepo.setOnClickListener(v -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(activity);
            builder.setPositiveButton(R.string.note, (dialog, which) -> note());
            builder.setNegativeButton(R.string.cancel, (dialog, which) -> dialog.cancel());
            builder.show();
        });
    }

    private void note() {
        String user = FirebaseAuth.getInstance().getCurrentUser().getEmail().split("@")[0];
        String date = CustomDateTime.getDate(new Date());
        String time = CustomDateTime.getTime(new Date());

        DatabaseReference dr = DatabaseFunctions.getDatabases(activity).get(0);
        DatabaseReference drUsername = dr.child("USERS/SELLER/" + user + "/ABOUT/name");
        DatabaseReference drReturn = dr.child("RETURN_TO_DEPO/" + date + "/" + user);

        drUsername.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                String sellerName = snapshot.getValue(String.class);
                if(sellerName == null) return;

                drReturn.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        Double pureSum = snapshot.child("pureSum").getValue(Double.class);
                        Double rottenSum = snapshot.child("rottenSum").getValue(Double.class);

                        pureSum = pureSum == null ? resultPure : pureSum + resultPure;
                        rottenSum = rottenSum == null ? resultRotten : rottenSum + resultRotten;

                        snapshot.getRef().child("pureSum").setValue(SharedClass.twoDigitDecimal(pureSum));
                        snapshot.getRef().child("rottenSum").setValue(SharedClass.twoDigitDecimal(rottenSum));
                        snapshot.getRef().child("seller").setValue(sellerName);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });

                ArrayList<String> _nameList = new ArrayList<>();
                ArrayList<Double> _priceList = new ArrayList<>();
                ArrayList<Double> _pureAmountList = new ArrayList<>();
                ArrayList<Double> _rottenAmountList = new ArrayList<>();

                for (Product product : cardViewProductHashMap.values()) {
                    _nameList.add(product.name);
                    _priceList.add(product.sellPrice);
                    _pureAmountList.add(product.wantedAmount);
                    _rottenAmountList.add(product.wantedAmountRotten);
                }

                drReturn.child(time + "/name").setValue(_nameList);
                drReturn.child(time + "/price").setValue(_priceList);
                drReturn.child(time + "/pureAmount").setValue(_pureAmountList);
                drReturn.child(time + "/rottenAmount").setValue(_rottenAmountList);

                clearBasket();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    private void configureClearButton() {
        ImageButton clearButton = findViewById(R.id.clearBasket);
        clearButton.setOnClickListener(v -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(activity);
            builder.setTitle("Səbəti təmizlə");
            builder.setPositiveButton("OK", (dialog, which) -> clearBasket());
            builder.setNegativeButton(R.string.cancel, (dialog, which) -> dialog.cancel());
            builder.show();
        });
    }

    private void clearBasket() {
        helper.deleteTable();
        nameList.clear();
        searchNameList.clear();
        cardViewProductHashMap.clear();
        adapter.notifyDataSetChanged();

        textViewNoItemInfo.setVisibility(View.VISIBLE);
        textViewResultPure.setText("Saf: 0.0");
        textViewResultRotten.setText("Çürük: 0.0");
    }

    private void configureSearchEditText() {
        EditText etSearch = findViewById(R.id.editTextSearch);
        SharedClass.configureSearchEditText(etSearch, nameList, searchNameList, adapter);
    }

    private void configureListViewProducts() {
        adapter = new SellerBasketProductsRecycler(this, nameList, searchNameList, cardViewProductHashMap, textViewResultPure, textViewResultRotten);
        RecyclerView myView = findViewById(R.id.recyclerviewProducts);
        myView.setHasFixedSize(true);
        myView.setAdapter(adapter);
        LinearLayoutManager llm = new LinearLayoutManager(this);
        llm.setOrientation(LinearLayoutManager.VERTICAL);
        myView.setLayoutManager(llm);

        getBasketProducts();
    }

    private void getBasketProducts() {
        nameList.clear();
        searchNameList.clear();
        cardViewProductHashMap.clear();

        for (Product product : helper.getProductList()) {
            nameList.add(product.id_name);
            cardViewProductHashMap.put(product.id_name, product);
            resultPure += product.sellPrice * product.wantedAmount;
            resultRotten += product.sellPrice * product.wantedAmountRotten;
        }
        searchNameList.addAll(nameList);

        if (nameList.size() > 0) textViewNoItemInfo.setVisibility(View.GONE);

        resultPure = SharedClass.twoDigitDecimal(resultPure);
        resultRotten = SharedClass.twoDigitDecimal(resultRotten);
        textViewResultPure.setText("Saf: " + resultPure);
        textViewResultRotten.setText("Çürük: " + resultRotten);
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}