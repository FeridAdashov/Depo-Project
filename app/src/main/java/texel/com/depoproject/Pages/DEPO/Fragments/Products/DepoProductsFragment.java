package texel.com.depoproject.Pages.DEPO.Fragments.Products;

import android.app.Activity;
import android.app.AlertDialog;
import android.nfc.FormatException;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.switchmaterial.SwitchMaterial;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;

import texel.com.depoproject.CustomDialogs.CustomProgressDialog;
import texel.com.depoproject.DataClasses.ProductInfo;
import texel.com.depoproject.HelperClasses.DatabaseFunctions;
import texel.com.depoproject.HelperClasses.SharedClass;
import texel.com.depoproject.R;

public class DepoProductsFragment extends Fragment {

    private final HashMap<String, ProductInfo> productInfoHashMap = new HashMap<>();
    private final ArrayList<String> nameList = new ArrayList<>();
    private final ArrayList<String> searchNameList = new ArrayList<>();
    private CustomProgressDialog progressDialog;
    private TextView textViewNoItemInfo;
    private View view;
    private DatabaseReference databaseReferenceProducts;
    private DepoRecyclerProducts adapter;

    private Activity activity;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_products_depo, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        this.view = view;
        activity = getActivity();
        textViewNoItemInfo = view.findViewById(R.id.textViewNoItemInfo);
        textViewNoItemInfo.setText("Qeydiyyatda Olan Məhsul Yoxdur");

        progressDialog = new CustomProgressDialog(activity, getString(R.string.data_loading));
        progressDialog.show();

        databaseReferenceProducts = DatabaseFunctions.getDatabases(getContext()).get(0).child("PRODUCTS");

        configureListViewDepo();
        configureSearchEditText();

        ImageButton addProduct = view.findViewById(R.id.addProduct);
        addProduct.setOnClickListener(v -> addNewProduct());
    }

    private void addNewProduct() {
        final View view = getLayoutInflater().inflate(R.layout.new_product_dialog_view, null);
        final EditText name = view.findViewById(R.id.cardTextViewProductName);
        final EditText buyPrice = view.findViewById(R.id.cardTextViewProductBuyPrice);
        final EditText sellPrice = view.findViewById(R.id.cardTextViewProductPrice);
        final SwitchMaterial switchActiveness = view.findViewById(R.id.switchActiveness);

        AlertDialog.Builder b = new AlertDialog.Builder(getContext());
        b.setPositiveButton(R.string.save, (dialog, which) -> {
        });
        b.setNegativeButton(R.string.cancel, (dialog, which) -> dialog.dismiss());
        b.setView(view);

        final AlertDialog dialog = b.create();
        dialog.show();
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            try {
                String _name = name.getText().toString().trim();
                Double _buyPrice = Double.parseDouble(buyPrice.getText().toString().trim());
                Double _sellPrice = Double.parseDouble(sellPrice.getText().toString().trim());
                boolean _activeness = switchActiveness.isChecked();

                if (TextUtils.isEmpty(_name)) throw new FormatException();
                else {
                    saveProduct(_name, new ProductInfo(_name, _buyPrice, _sellPrice, _activeness));
                    dialog.dismiss();
                }
            } catch (Exception e) {
                SharedClass.showSnackBar(activity, "Yanlış dəyər daxil etdiniz!");
                Log.d("AAAAAAAA", e.toString());
            }
        });
    }

    private void saveProduct(String name, ProductInfo ProductInfo) {
        productInfoHashMap.put(name, ProductInfo);
        nameList.add(name);
        searchNameList.add(name);
        adapter.notifyDataSetChanged();

        databaseReferenceProducts.child(name).setValue(ProductInfo);

        textViewNoItemInfo.setVisibility(View.GONE);
    }

    private void configureSearchEditText() {
        EditText etSearch = view.findViewById(R.id.editTextSearch);
        SharedClass.configureSearchEditText(etSearch, nameList, searchNameList, adapter);
    }

    private void configureListViewDepo() {
        adapter = new DepoRecyclerProducts(activity, nameList, searchNameList, productInfoHashMap);
        RecyclerView myView = view.findViewById(R.id.recyclerviewProducts);
        myView.setHasFixedSize(true);
        myView.setAdapter(adapter);
        LinearLayoutManager llm = new LinearLayoutManager(activity);
        llm.setOrientation(LinearLayoutManager.VERTICAL);
        myView.setLayoutManager(llm);

        loadProductsFromDatabase();
    }

    private void loadProductsFromDatabase() {
        databaseReferenceProducts.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                productInfoHashMap.clear();
                nameList.clear();
                searchNameList.clear();

                for (DataSnapshot snapshotTaxis : dataSnapshot.getChildren()) {
                    String name = snapshotTaxis.getKey();
                    nameList.add(name);

                    String _name = snapshotTaxis.child("name").getValue(String.class);
                    Double _buyPrice = snapshotTaxis.child("buyPrice").getValue(Double.class);
                    Double _sellPrice = snapshotTaxis.child("sellPrice").getValue(Double.class);
                    Boolean _active = snapshotTaxis.child("active").getValue(Boolean.class);

                    productInfoHashMap.put(name, new ProductInfo(_name, _buyPrice, _sellPrice, _active));
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
}