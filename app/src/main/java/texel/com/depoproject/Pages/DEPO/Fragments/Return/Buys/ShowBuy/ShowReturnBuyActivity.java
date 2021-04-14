package texel.com.depoproject.Pages.DEPO.Fragments.Return.Buys.ShowBuy;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;

import texel.com.depoproject.CustomDialogs.CustomProgressDialog;
import texel.com.depoproject.HelperClasses.DatabaseFunctions;
import texel.com.depoproject.HelperClasses.SharedClass;
import texel.com.depoproject.Pages.DEPO.Activities.ShowReturnToDepo.ShowReturnRecycler;
import texel.com.depoproject.Pages.DEPO.Activities.ShowSell.ShowSellRecycler;
import texel.com.depoproject.R;
import texel.com.depoproject.SqlLiteDatabase.Product;

public class ShowReturnBuyActivity extends AppCompatActivity {
    private final HashMap<String, Product> cardViewProductHashMap = new HashMap<>();
    private final ArrayList<String> nameList = new ArrayList<>();
    private final ArrayList<String> searchNameList = new ArrayList<>();
    private TextView textViewSum, textViewResult, textViewPercent;
    private RecyclerShowReturnBuy adapter;
    private Activity activity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_sell);

        activity = this;

        textViewSum = findViewById(R.id.textViewSum);
        textViewPercent = findViewById(R.id.textViewPercent);
        textViewResult = findViewById(R.id.textViewResult);

        Toolbar toolbar = findViewById(R.id.toolbarDepo);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_arrow_back_white);
        toolbar.setTitleTextColor(Color.WHITE);
        getSupportActionBar().setTitle("Geri Alım - Zavod");

        configureListViewProducts();
        configureSearchEditText();
    }

    private void configureSearchEditText() {
        EditText etSearch = findViewById(R.id.editTextSearch);
        SharedClass.configureSearchEditText(etSearch, nameList, searchNameList, adapter);
    }

    private void configureListViewProducts() {
        adapter = new RecyclerShowReturnBuy(searchNameList, cardViewProductHashMap);
        RecyclerView myView = findViewById(R.id.recyclerviewProducts);
        myView.setHasFixedSize(true);
        myView.setAdapter(adapter);
        LinearLayoutManager llm = new LinearLayoutManager(this);
        llm.setOrientation(LinearLayoutManager.VERTICAL);
        myView.setLayoutManager(llm);

        getProducts();
    }

    private void getProducts() {
        nameList.clear();
        searchNameList.clear();
        cardViewProductHashMap.clear();

        GenericTypeIndicator<ArrayList<String>> s = new GenericTypeIndicator<ArrayList<String>>() {
        };
        GenericTypeIndicator<ArrayList<Double>> d = new GenericTypeIndicator<ArrayList<Double>>() {
        };

        String childName = getIntent().getStringExtra("child_name");

        DatabaseFunctions.getDatabases(activity).get(0).child(childName).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                ArrayList<String> _nameList = snapshot.child("name").getValue(s);
                ArrayList<Double> _buyPriceList = snapshot.child("price").getValue(d);
                ArrayList<Double> _amountList = snapshot.child("amount").getValue(d);
                Double _percent = snapshot.child("percent").getValue(Double.class);

                if (_nameList == null || _nameList.size() == 0) return;
                if (_percent == null) _percent = 0.0;

                nameList.addAll(_nameList);
                searchNameList.addAll(_nameList);

                double sum = 0.0;
                for (int i = 0; i < nameList.size(); ++i) {
                    cardViewProductHashMap.put(nameList.get(i),
                            new Product(nameList.get(i),
                                    nameList.get(i),
                                    _buyPriceList.get(i),
                                    0.0,
                                    _amountList.get(i)));
                    sum += _buyPriceList.get(i) * _amountList.get(i);
                }
                textViewSum.setText(String.format("Cəm: %.2f", sum));
                textViewPercent.setText(String.format("Faiz: %.2f", _percent));
                textViewResult.setText(String.format("Nəticə: %.2f", sum - _percent));

                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}