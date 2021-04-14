package texel.com.depoproject.Pages.DEPO.Fragments.Texture.Basket;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.graphics.Color;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
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
import java.util.Date;
import java.util.HashMap;
import java.util.Objects;

import texel.com.depoproject.DataClasses.DepoSellInfo;
import texel.com.depoproject.HelperClasses.CustomDateTime;
import texel.com.depoproject.HelperClasses.DatabaseFunctions;
import texel.com.depoproject.HelperClasses.SharedClass;
import texel.com.depoproject.R;
import texel.com.depoproject.SqlLiteDatabase.DatabaseHelper;
import texel.com.depoproject.SqlLiteDatabase.Product;

public class DepoTextureBasketActivity extends AppCompatActivity {

    private final HashMap<String, Product> cardViewProductHashMap = new HashMap<>();
    private final ArrayList<String> nameList = new ArrayList<>();
    private final ArrayList<String> searchNameList = new ArrayList<>();
    private TextView textViewNoItemInfo;
    private TextView textViewResult;
    private DatabaseReference databaseReference;
    private DepoTextureBasketProductsRecycler adapter;
    private Activity activity;

    private DatabaseHelper helper;

    private double result = 0.0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_depo_basket);

        activity = this;
        helper = new DatabaseHelper(activity, DatabaseHelper.DATABASE_TEXTURES);

        Toolbar toolbar = findViewById(R.id.toolbarDepo);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_arrow_back_white);
        toolbar.setTitleTextColor(Color.WHITE);
        getSupportActionBar().setTitle(getResources().getString(R.string.basket));

        textViewNoItemInfo = findViewById(R.id.textViewNoItemInfo);
        textViewNoItemInfo.setText("Səbət Boşdur");

        textViewResult = findViewById(R.id.textViewSum);

        databaseReference = DatabaseFunctions.getDatabases(activity).get(0);

        configureListViewProducts();
        configureSearchEditText();
        configureClearButton();
        configureNoteTextureButton();
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

    private void configureNoteTextureButton() {
        Button orderButton = findViewById(R.id.buttonNoteTexture);
        orderButton.setOnClickListener(v -> {
            if (helper.getProductList().size() == 0) {
                SharedClass.showSnackBar(activity, "Səbət boşdur!");
                return;
            }
            AlertDialog.Builder builder = new AlertDialog.Builder(activity);
            builder.setNeutralButton(R.string.note, (dialog, which) -> note());
            builder.setPositiveButton(R.string.cancel, (dialog, which) -> dialog.cancel());
            builder.show();
        });
    }

    private void note() {
        String user_name = getIntent().getStringExtra("user_name");
        String name = getIntent().getStringExtra("name");
        if (TextUtils.isEmpty(user_name)) {
            SharedClass.showSnackBar(activity, "Xəta baş verdi. Məlumat qeyd olunmadı");
            return;
        }

        ArrayList<String> nameList = new ArrayList<>();
        ArrayList<Double> priceList = new ArrayList<>();
        ArrayList<Double> amountList = new ArrayList<>();

        result = 0.0;
        for (Product product : helper.getProductList()) {
            nameList.add(product.name);
            priceList.add(product.sellPrice);
            amountList.add(product.wantedAmount);
            result += product.sellPrice * product.wantedAmount;
        }

        DepoSellInfo sellerSellInfo = new DepoSellInfo(nameList, priceList, amountList);

        String child = "TEXTURES/" + CustomDateTime.getDate(new Date()) + "/" + user_name + "_" + name;
        String time = CustomDateTime.getTime(new Date());

        databaseReference.child(child + "/" + time).setValue(sellerSellInfo);
        databaseReference.child(child + "/sum").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Double sum = snapshot.getValue(Double.class);
                sum = sum == null ? result : sum + result;

                sum = SharedClass.twoDigitDecimal(sum);

                snapshot.getRef().setValue(sum);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        clearBasket();
        SharedClass.showSnackBar(activity, "*** Uğurlu Əməliyyat ***");
    }

    private void clearBasket() {
        helper.deleteTable();
        nameList.clear();
        searchNameList.clear();
        cardViewProductHashMap.clear();
        adapter.notifyDataSetChanged();

        textViewNoItemInfo.setVisibility(View.VISIBLE);
        textViewResult.setText("0.0");
    }

    private void configureSearchEditText() {
        EditText etSearch = findViewById(R.id.editTextSearch);
        SharedClass.configureSearchEditText(etSearch, nameList, searchNameList, adapter);
    }

    private void configureListViewProducts() {
        adapter = new DepoTextureBasketProductsRecycler(this, searchNameList, cardViewProductHashMap, textViewResult);
        RecyclerView myView = findViewById(R.id.recyclerviewProducts);
        myView.setHasFixedSize(true);
        myView.setAdapter(adapter);
        LinearLayoutManager llm = new LinearLayoutManager(this);
        llm.setOrientation(LinearLayoutManager.VERTICAL);
        myView.setLayoutManager(llm);

        getBasketProducts();
    }

    @SuppressLint("DefaultLocale")
    private void getBasketProducts() {
        nameList.clear();
        searchNameList.clear();
        cardViewProductHashMap.clear();
        result = 0.0;

        for (Product product : helper.getProductList()) {
            nameList.add(product.id_name);
            cardViewProductHashMap.put(product.id_name, product);
            result += product.sellPrice * product.wantedAmount;
        }
        searchNameList.addAll(nameList);

        if (nameList.size() > 0) textViewNoItemInfo.setVisibility(View.GONE);

        result = SharedClass.twoDigitDecimal(result);
        textViewResult.setText(String.valueOf(result));
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}