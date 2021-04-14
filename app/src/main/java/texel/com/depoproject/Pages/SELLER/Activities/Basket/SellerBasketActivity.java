package texel.com.depoproject.Pages.SELLER.Activities.Basket;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.InputType;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
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

import texel.com.depoproject.HelperClasses.CustomDateTime;
import texel.com.depoproject.HelperClasses.DatabaseFunctions;
import texel.com.depoproject.HelperClasses.SharedClass;
import texel.com.depoproject.Pages.SELLER.Activities.CreateSell.CreateSellActivity;
import texel.com.depoproject.Printer.PrinterActivity;
import texel.com.depoproject.R;
import texel.com.depoproject.SqlLiteDatabase.DatabaseHelper;
import texel.com.depoproject.SqlLiteDatabase.Product;

public class SellerBasketActivity extends AppCompatActivity {

    private final HashMap<String, Product> cardViewProductHashMap = new HashMap<>();
    private final ArrayList<String> nameList = new ArrayList<>();
    private final ArrayList<String> searchNameList = new ArrayList<>();
    private TextView textViewNoItemInfo;
    private TextView textViewResult;
    private EditText editTextPercent;
    private DatabaseReference databaseReference;
    private SellerBasketProductsRecycler adapter;
    private Activity activity;

    private DatabaseHelper helper;

    private double percentValue = 0.0;
    private double netIncome = 0.0, result = 0.0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_seller_basket);

        activity = this;
        helper = new DatabaseHelper(this,
                CreateSellActivity.isNewSell ?
                        DatabaseHelper.DATABASE_SELLS :
                        DatabaseHelper.DATABASE_SELLER_RETURN);

        textViewNoItemInfo = findViewById(R.id.textViewNoItemInfo);
        textViewNoItemInfo.setText("Səbət Boşdur");

        editTextPercent = findViewById(R.id.editTextPercent);
        textViewResult = findViewById(R.id.textViewSum);

        ImageButton imageButtonBack = findViewById(R.id.imageButtonBack);
        imageButtonBack.setOnClickListener(v -> finish());
        ImageButton imageButtonPrint = findViewById(R.id.imageButtonPrint);
        imageButtonPrint.setOnClickListener(v -> {
            Intent intent = new Intent(activity, PrinterActivity.class);
            intent.putExtra("message_1", getIntent().getStringExtra("market"));
            intent.putExtra("message_2", getAllDataMessage());
            startActivity(intent);
        });

        databaseReference = DatabaseFunctions.getDatabases(activity).get(0);

        configureListViewProducts();
        configureSearchEditText();
        configureClearButton();
        configureCalculateButton();
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
                SharedClass.showSnackBar(this, "Səbət boşdur!");
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
                    beginSell(SharedClass.twoDigitDecimal(value));
                } catch (Exception e) {
                    e.printStackTrace();
                    SharedClass.showSnackBar(this, "Yanlış dəyər!!!");
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
        double commonBuy = 0.0, commonSell = 0.0;

        for (Product product : helper.getProductList()) {
            commonBuy += product.buyPrice * product.wantedAmount;
            commonSell += product.sellPrice * product.wantedAmount;
        }

        percentValue = SharedClass.twoDigitDecimal(commonSell * percent / 100.);
        result = SharedClass.twoDigitDecimal(commonSell - percentValue);
        netIncome = SharedClass.twoDigitDecimal(result - commonBuy);

        return "Cəm : " + String.format("%.2f", commonSell) +
                "\n\nFaiz : " + String.format("%.2f", percentValue) +
                "\n\nNəticə : " + String.format("%.2f", result);
    }

    private void beginSell(double paid) {
        String user = FirebaseAuth.getInstance().getCurrentUser().getEmail().split("@")[0];

        boolean isNewSell = getIntent().getBooleanExtra("isNewSell", true);
        String market_id_name = getIntent().getStringExtra("market_id_name");
        if (TextUtils.isEmpty(market_id_name)) {
            SharedClass.showSnackBar(this, "Xəta baş verdi. Satış olmadı");
            return;
        }

        ArrayList<String> nameList = new ArrayList<>();
        ArrayList<Double> priceList = new ArrayList<>();
        ArrayList<Double> amountList = new ArrayList<>();

        for (Product product : helper.getProductList()) {
            nameList.add(product.name);
            priceList.add(product.sellPrice);
            amountList.add(product.wantedAmount);
        }

        SellerSellInfo sellerSellInfo = new SellerSellInfo(market_id_name, nameList, priceList, amountList, percentValue, netIncome);

        String time = CustomDateTime.getTime(new Date());
        String date = CustomDateTime.getDate(new Date());

        String child = isNewSell ? "SELLS/" : "RETURN/SELL/";
        child += date + "/" + user;

        databaseReference.child(child + "/" + time).setValue(sellerSellInfo);
        databaseReference.child(child).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (isNewSell) {
                    Double net = snapshot.child("netIncome").getValue(Double.class);
                    net = net == null ? netIncome : net + netIncome;

                    net = SharedClass.twoDigitDecimal(net);
                    snapshot.getRef().child("netIncome").setValue(net);
                }

                Double sum = snapshot.child("sum").getValue(Double.class);
                Double percent = snapshot.child("percentSum").getValue(Double.class);

                sum = sum == null ? result : sum + result;
                percent = percent == null ? percentValue : percent + percentValue;

                sum = SharedClass.twoDigitDecimal(sum);
                percent = SharedClass.twoDigitDecimal(percent);

                snapshot.getRef().child("sum").setValue(sum);
                snapshot.getRef().child("percentSum").setValue(percent);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        if (percentValue != 0 && isNewSell) {
            String name = FirebaseAuth.getInstance().getCurrentUser().getEmail().split("@")[0];
            if (name != null) {
                child = "EXPENSES/Percent/" + name + "/" + CustomDateTime.getDate(new Date());

                databaseReference.child(child + "/" + time + "/name").setValue(market_id_name);
                databaseReference.child(child + "/" + time + "/value").setValue(percentValue);

                databaseReference.child(child + "/sum").addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        Double sum = snapshot.getValue(Double.class);
                        sum = sum == null ? percentValue : sum + percentValue;
                        sum = SharedClass.twoDigitDecimal(sum);
                        snapshot.getRef().setValue(sum);
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {

                    }
                });
            } else SharedClass.showSnackBar(this, "*** Uğursuz Əməliyyat ***");
        }

        if (paid != result)
            databaseReference.child("DEBTS/MARKET/" + market_id_name).addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    Double debt = snapshot.child("sum").getValue(Double.class);
                    if (debt == null) debt = 0.0;

                    double d = isNewSell ? result - paid : paid - result;
                    debt += d;

                    snapshot.getRef().child(date + "/" + time).setValue(SharedClass.twoDigitDecimal(d));
                    snapshot.getRef().child("sum").setValue(SharedClass.twoDigitDecimal(debt));
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });

        clearBasket();
        SharedClass.showSnackBar(this, "*** Uğurlu Əməliyyat ***");
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
        adapter = new SellerBasketProductsRecycler(this, nameList, searchNameList, cardViewProductHashMap, textViewResult);
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
            result += product.sellPrice * product.wantedAmount;
        }
        searchNameList.addAll(nameList);

        if (nameList.size() > 0) textViewNoItemInfo.setVisibility(View.GONE);

        result = SharedClass.twoDigitDecimal(result - percentValue);
        textViewResult.setText(String.valueOf(result));
    }

    private String getAllDataMessage() {
        calculate();
        String percent = editTextPercent.getText().toString();
        if (percent.equals("")) percent = "0.0";

        StringBuilder text = new StringBuilder();

        ArrayList<String> nameList = new ArrayList<>();
        ArrayList<String> priceList = new ArrayList<>();
        ArrayList<String> amountList = new ArrayList<>();
        ArrayList<String> commonList = new ArrayList<>();

        nameList.add("Ad");
        priceList.add("Qiymet");
        amountList.add("Miqdar");
        commonList.add("Umumi");

        double sum = 0.;
        for (Product product : cardViewProductHashMap.values()) {
            double m = product.sellPrice * product.wantedAmount;
            sum += m;

            nameList.add(product.name);
            priceList.add("" + SharedClass.twoDigitDecimal(product.sellPrice));
            amountList.add(SharedClass.twoDigitDecimal(product.wantedAmount) + " Ed.");
            commonList.add("" + SharedClass.twoDigitDecimal(m));
        }

        int length = maxSizeInList(nameList);
        setSizeInList(nameList, length + 3);

        length = maxSizeInList(priceList);
        setSizeInList(priceList, length + 3);

        length = maxSizeInList(amountList);
        setSizeInList(amountList, length + 3);

        for (int i = 0; i < nameList.size(); ++i)
            text
                    .append(nameList.get(i))
                    .append(priceList.get(i))
                    .append(amountList.get(i))
                    .append(commonList.get(i)).append("\n");

        text
                .append("\n\n")
                .append("Umumi cem:        ")
                .append(SharedClass.twoDigitDecimal(sum)).append("\n")
                .append("Faiz:             ").append(percent).append(" %\n")
                .append("Faizin miqdari:   ").append(percentValue).append("\n")
                .append("Yekun netice:     ").append(result).append("\n\n\n");

        System.out.println(text);

        return text.toString();
    }

    private int maxSizeInList(ArrayList<String> list) {
        int length = 0;
        for (String text : list)
            if (text.length() > length) length = text.length();

        return length;
    }

    private void setSizeInList(ArrayList<String> list, int length) {
        for (int i = 0; i < list.size(); ++i) {
            int len = list.get(i).length();
            list.set(i, list.get(i) + getSpaces(length - len));
        }
    }

    private String getSpaces(int n) {
        StringBuilder spaces = new StringBuilder();
        for (int i = 0; i < n; ++i) spaces.append(" ");
        return spaces.toString();
    }
}