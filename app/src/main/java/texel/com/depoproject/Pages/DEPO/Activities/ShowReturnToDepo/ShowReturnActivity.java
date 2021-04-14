package texel.com.depoproject.Pages.DEPO.Activities.ShowReturnToDepo;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Objects;

import texel.com.depoproject.HelperClasses.DatabaseFunctions;
import texel.com.depoproject.HelperClasses.SharedClass;
import texel.com.depoproject.R;
import texel.com.depoproject.SqlLiteDatabase.Product;

public class ShowReturnActivity extends AppCompatActivity {

    private final HashMap<String, Product> cardViewProductHashMap = new HashMap<>();
    private final ArrayList<String> nameList = new ArrayList<>();
    private final ArrayList<String> searchNameList = new ArrayList<>();
    private TextView textViewResultPure, textViewResultRotten;
    private ShowReturnRecycler adapter;
    private Activity activity;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_return);

        activity = this;

        textViewResultPure = findViewById(R.id.textViewResultPure);
        textViewResultRotten = findViewById(R.id.textViewResultRotten);

        Toolbar toolbar = findViewById(R.id.toolbarDepo);
        setSupportActionBar(toolbar);
        Objects.requireNonNull(getSupportActionBar()).setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_arrow_back_white);
        toolbar.setTitleTextColor(Color.WHITE);
        getSupportActionBar().setTitle("İadə");

        configureListViewProducts();
        configureSearchEditText();
    }

    private void configureSearchEditText() {
        EditText etSearch = findViewById(R.id.editTextSearch);
        SharedClass.configureSearchEditText(etSearch, nameList, searchNameList, adapter);
    }

    private void configureListViewProducts() {
        adapter = new ShowReturnRecycler(searchNameList, cardViewProductHashMap);
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
                ArrayList<Double> _priceList = snapshot.child("price").getValue(d);
                ArrayList<Double> _pureAmountList = snapshot.child("pureAmount").getValue(d);
                ArrayList<Double> _rottenAmountList = snapshot.child("rottenAmount").getValue(d);

                if (_nameList == null || _nameList.size() == 0) return;

                nameList.addAll(_nameList);
                searchNameList.addAll(_nameList);

                double sumPure = 0.0, sumRotten = 0.0;
                for (int i = 0; i < nameList.size(); ++i) {
                    Product product = new Product(
                            nameList.get(i),
                            nameList.get(i),
                            0.0,
                            _priceList.get(i),
                            _pureAmountList.get(i));
                    product.wantedAmountRotten = _rottenAmountList.get(i);

                    cardViewProductHashMap.put(nameList.get(i), product);

                    sumPure += _priceList.get(i) * _pureAmountList.get(i);
                    sumRotten += _priceList.get(i) * _rottenAmountList.get(i);
                }
                textViewResultPure.setText(String.format("Saf: %.2f", sumPure));
                textViewResultRotten.setText(String.format("Çürük: %.2f", sumRotten));

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