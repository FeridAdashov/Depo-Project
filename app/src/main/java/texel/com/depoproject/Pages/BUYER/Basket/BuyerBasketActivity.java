package texel.com.depoproject.Pages.BUYER.Basket;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.graphics.Color;
import android.os.Bundle;
import android.text.InputType;
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

import texel.com.depoproject.DataClasses.BuyerBuyInfo;
import texel.com.depoproject.HelperClasses.CustomDateTime;
import texel.com.depoproject.HelperClasses.DatabaseFunctions;
import texel.com.depoproject.HelperClasses.SharedClass;
import texel.com.depoproject.R;
import texel.com.depoproject.SqlLiteDatabase.DatabaseHelper;
import texel.com.depoproject.SqlLiteDatabase.Product;

public class BuyerBasketActivity extends AppCompatActivity {

    private final HashMap<String, Product> cardViewProductHashMap = new HashMap<>();
    private final ArrayList<String> nameList = new ArrayList<>();
    private final ArrayList<String> searchNameList = new ArrayList<>();
    private TextView textViewNoItemInfo;
    private TextView textViewResult;
    private EditText editTextPercent;
    private DatabaseReference databaseReference;
    private BuyerBasketProductsRecycler adapter;
    private Activity activity;

    private DatabaseHelper helper;

    private double percentValue = 0.0, result = 0.0;
    private String fabric;
    private boolean isNewBuy;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_buyer_basket); //same view as seller basket

        activity = this;

        fabric = getIntent().getStringExtra("fabric");
        isNewBuy = getIntent().getBooleanExtra("isNewBuy", true);

        helper = new DatabaseHelper(this,
                isNewBuy ? DatabaseHelper.DATABASE_BUYS : DatabaseHelper.DATABASE_BUYER_RETURN);

        textViewNoItemInfo = findViewById(R.id.textViewNoItemInfo);
        textViewNoItemInfo.setText("Səbət Boşdur");

        editTextPercent = findViewById(R.id.editTextPercent);
        textViewResult = findViewById(R.id.textViewSum);

        databaseReference = DatabaseFunctions.getDatabases(activity).get(0);

        configureBackButton();
        configureListViewProducts();
        configureSearchEditText();
        configureClearButton();
        configureCalculateButton();
    }

    private void configureBackButton() {
        ImageButton imageButtonBack = findViewById(R.id.imageButtonBack);
        imageButtonBack.setOnClickListener(v -> finish());
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

    private void configureCalculateButton() {
        Button orderButton = findViewById(R.id.buttonCalculate);
        orderButton.setOnClickListener(v -> {
            if (helper.getProductList().size() == 0) {
                SharedClass.showSnackBar(activity, "Səbət boşdur!");
                return;
            }
            String message = calculate();

            AlertDialog.Builder builder = new AlertDialog.Builder(activity);
            EditText editText = new EditText(activity);
            editText.setText(result + "");
            editText.setTextColor(Color.parseColor("#FF5722"));
            editText.setHint("Ödəniləcək miqdarı daxil edin");
            editText.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
            builder.setMessage(message);
            builder.setView(editText);
            builder.setPositiveButton(R.string.ok, (dialog, which) -> {
                try {
                    double value = Double.parseDouble(editText.getText().toString());
                    beginBuy(SharedClass.twoDigitDecimal(value));
                } catch (Exception e) {
                    SharedClass.showSnackBar(activity, "Yanlış dəyər!!!");
                }
            });
            builder.setNegativeButton(R.string.cancel, (dialog, which) -> dialog.cancel());
            builder.show();
        });
    }

    @SuppressLint("DefaultLocale")
    private String calculate() {
        String textValue = editTextPercent.getText().toString();
        double percent = TextUtils.isEmpty(textValue) ? 0.0 : Double.parseDouble(textValue);
        double commonBuy = 0.0;

        for (Product product : helper.getProductList())
            commonBuy += product.buyPrice * product.wantedAmount;


        percentValue = SharedClass.twoDigitDecimal(commonBuy * percent / 100.);
        result = SharedClass.twoDigitDecimal(commonBuy - percentValue);

        return "Cəm : " + String.format("%.2f", commonBuy) +
                "\n\nFaiz : " + String.format("%.2f", percentValue) +
                "\n\nNəticə : " + String.format("%.2f", result);
    }

    private void beginBuy(double paid) {
        ArrayList<String> nameList = new ArrayList<>();
        ArrayList<Double> priceList = new ArrayList<>();
        ArrayList<Double> amountList = new ArrayList<>();

        for (Product product : helper.getProductList()) {
            nameList.add(product.name);
            priceList.add(product.buyPrice);
            amountList.add(product.wantedAmount);
        }

        BuyerBuyInfo buyInfo = new BuyerBuyInfo(nameList, priceList, amountList, percentValue, result);

        String time = CustomDateTime.getTime(new Date());
        String date = CustomDateTime.getDate(new Date());
        String child = isNewBuy ? "BUYS/" + date : "RETURN/BUY/" + date;

        databaseReference.child(child + "/" + time).setValue(buyInfo);
        databaseReference.child(child).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Double sum = snapshot.child("sum").getValue(Double.class);
                Double percentSum = snapshot.child("percentSum").getValue(Double.class);

                sum = sum == null ? result : sum + result;
                percentSum = percentSum == null ? percentValue : percentSum + percentValue;

                sum = SharedClass.twoDigitDecimal(sum);
                percentSum = SharedClass.twoDigitDecimal(percentSum);

                snapshot.getRef().child("sum").setValue(sum);
                snapshot.getRef().child("percentSum").setValue(percentSum);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        if (paid != result)
            databaseReference.child("DEBTS/FABRIC/" + fabric).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    Double debt = snapshot.child("sum").getValue(Double.class);
                    if (debt == null) debt = 0.0;

                    double d = isNewBuy ? result - paid : paid - result;
                    debt += d;

                    debt = SharedClass.twoDigitDecimal(debt);

                    snapshot.getRef().child(date + "/" + time).setValue(d);
                    snapshot.getRef().child("sum").setValue(debt);
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
        editTextPercent.setText("");
        textViewResult.setText("0.0");
    }

    private void configureSearchEditText() {
        EditText etSearch = findViewById(R.id.editTextSearch);
        SharedClass.configureSearchEditText(etSearch, nameList, searchNameList, adapter);
    }

    private void configureListViewProducts() {
        adapter = new BuyerBasketProductsRecycler(this, searchNameList, cardViewProductHashMap, textViewResult, isNewBuy);
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
        result = 0.0;

        for (Product product : helper.getProductList()) {
            nameList.add(product.id_name);
            cardViewProductHashMap.put(product.id_name, product);
            result += product.buyPrice * product.wantedAmount;
        }
        searchNameList.addAll(nameList);

        if (nameList.size() > 0) textViewNoItemInfo.setVisibility(View.GONE);

        result = SharedClass.twoDigitDecimal(result - percentValue);
        textViewResult.setText(String.valueOf(result));
    }
}