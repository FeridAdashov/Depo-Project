package texel.com.depoproject.Pages.DEPO.Fragments.Texture.Products;

import android.content.Intent;
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

import texel.com.depoproject.CustomDialogs.CustomProgressDialog;
import texel.com.depoproject.HelperClasses.DatabaseFunctions;
import texel.com.depoproject.HelperClasses.SharedClass;
import texel.com.depoproject.Pages.DEPO.Fragments.Texture.Basket.DepoTextureBasketActivity;
import texel.com.depoproject.R;

public class TextureProductsDepoActivity extends AppCompatActivity {

    private final ArrayList<Double> priceList = new ArrayList<>();
    private final ArrayList<String> nameList = new ArrayList<>();
    private final ArrayList<String> childNameList = new ArrayList<>();
    private final ArrayList<String> searchChildNameList = new ArrayList<>();
    private CustomProgressDialog progressDialog;
    private TextView textViewNoItemInfo;
    private DatabaseReference databaseReferenceProducts;
    private RecyclerTextureProductsDepo adapter;
    private String user_name, name;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_texture_products_depo);

        Toolbar toolbar = findViewById(R.id.toolbarDepo);
        TextView title = toolbar.findViewById(R.id.depoToolbarTitle);
        title.setText(getResources().getString(R.string.products));
        toolbar.setNavigationOnClickListener(v -> finish());

        user_name = getIntent().getStringExtra("user_name");
        name = getIntent().getStringExtra("name");

        textViewNoItemInfo = findViewById(R.id.textViewNoItemInfo);
        textViewNoItemInfo.setText("Qeydiyyatda Olan Market Yoxdur");

        progressDialog = new CustomProgressDialog(this, getString(R.string.data_loading));
        progressDialog.show();

        databaseReferenceProducts = DatabaseFunctions.getDatabases(this).get(0).child("PRODUCTS");

        configureListViewDepo();
        configureSearchEditText();
        configureBasketButton();
    }

    private void configureBasketButton() {
        ImageButton basketButton = findViewById(R.id.buttonBasket);
        basketButton.setOnClickListener(v -> {
            Intent intent = new Intent(getApplicationContext(), DepoTextureBasketActivity.class);
            intent.putExtra("user_name", user_name);
            intent.putExtra("name", name);
            startActivity(intent);
        });
    }

    private void configureSearchEditText() {
        EditText etSearch = findViewById(R.id.editTextSearch);
        SharedClass.configureSearchEditText(etSearch, nameList, searchChildNameList, adapter);
    }

    private void configureListViewDepo() {
        adapter = new RecyclerTextureProductsDepo(this, nameList, childNameList, searchChildNameList, priceList);
        RecyclerView myView = findViewById(R.id.recyclerviewProducts);
        myView.setHasFixedSize(true);
        myView.setAdapter(adapter);
        LinearLayoutManager llm = new LinearLayoutManager(this);
        llm.setOrientation(LinearLayoutManager.VERTICAL);
        myView.setLayoutManager(llm);

        loadProductsFromDatabase();
    }

    private void loadProductsFromDatabase() {
        databaseReferenceProducts.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                nameList.clear();
                childNameList.clear();
                searchChildNameList.clear();
                priceList.clear();

                for (DataSnapshot snapshotTaxis : dataSnapshot.getChildren()) {
                    String child = snapshotTaxis.getKey();
                    childNameList.add(child);

                    String _name = snapshotTaxis.child("name").getValue(String.class);
                    Double _price = snapshotTaxis.child("sellPrice").getValue(Double.class);

                    nameList.add(_name);
                    priceList.add(_price);
                }
                if (nameList.size() > 0) textViewNoItemInfo.setVisibility(View.GONE);
                else textViewNoItemInfo.setVisibility(View.VISIBLE);

                progressDialog.dismiss();
                searchChildNameList.addAll(childNameList);
                adapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                progressDialog.dismiss();
            }
        });
    }
}