package texel.com.depoproject.Pages.BUYER.Fragments;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RadioGroup;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Date;

import texel.com.depoproject.CustomDialogs.CustomProgressDialog;
import texel.com.depoproject.HelperClasses.CustomDateTime;
import texel.com.depoproject.HelperClasses.DatabaseFunctions;
import texel.com.depoproject.HelperClasses.SharedClass;
import texel.com.depoproject.Pages.BUYER.Basket.BuyerBasketActivity;
import texel.com.depoproject.R;

public class BuyerProductsFragment extends Fragment implements AdapterView.OnItemSelectedListener {


    public static boolean isNewBuy = true;
    private final ArrayList<Double> priceList = new ArrayList<>();
    private final ArrayList<String> nameList = new ArrayList<>();
    private final ArrayList<String> childNameList = new ArrayList<>();
    private final ArrayList<String> searchChildNameList = new ArrayList<>();
    private final ArrayList<String> fabricList = new ArrayList<>();
    private CustomProgressDialog progressDialog;
    private TextView textViewNoItemInfo, textViewDebt;
    private ArrayAdapter<String> spinnerAdapter;
    private DatabaseReference drProducts, drFabrics, drDebts;
    private RecyclerBuysProductsBuyer adapter;
    private Activity activity;
    private View view;
    private String selectedFabric;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_buyer_products, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        this.view = view;
        activity = getActivity();

        textViewNoItemInfo = view.findViewById(R.id.textViewNoItemInfo);
        textViewNoItemInfo.setText("Qeydiyyatda Olan Məhsul Yoxdur");

        RadioGroup radioGroupUsers = view.findViewById(R.id.radioGroupBuyReturn);
        radioGroupUsers.setOnCheckedChangeListener((radioGroup, i) -> {
            switch (i) {
                case R.id.radioBuy:
                    isNewBuy = true;
                    break;

                case R.id.radioReturn:
                    isNewBuy = false;
                    break;
            }
        });

        progressDialog = new CustomProgressDialog(activity, getString(R.string.data_loading));
        progressDialog.show();

        drProducts = DatabaseFunctions.getDatabases(activity).get(0).child("PRODUCTS");
        drFabrics = DatabaseFunctions.getDatabases(activity).get(0).child("FABRICS");
        drDebts = DatabaseFunctions.getDatabases(activity).get(0).child("DEBTS/FABRIC");

        loadFabrics();
        configureListViewDepo();
        configureSearchEditText();
        configureBasketButton();
        configureTextViewDebt();
        configureSpinner();
    }

    private void configureTextViewDebt() {
        textViewDebt = view.findViewById(R.id.textViewDebt);
        textViewDebt.setOnClickListener(v -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(activity);
            EditText editText = new EditText(activity);
            editText.setTextColor(Color.parseColor("#FF5722"));
            editText.setHint("Silinəcək borcun miqdarı");
            editText.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
            builder.setView(editText);
            builder.setPositiveButton("Ödə", (dialog, which) -> {
                try {
                    double value = SharedClass.twoDigitDecimal(Double.parseDouble(editText.getText().toString()));
                    drDebts.child(selectedFabric).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            Double sum = snapshot.child("sum").getValue(Double.class);
                            if (sum != null && sum >= value) {
                                sum -= value;

                                sum = SharedClass.twoDigitDecimal(sum);

                                snapshot.getRef().child("sum").setValue(sum);
                                snapshot.getRef()
                                        .child(CustomDateTime.getDate(new Date()) + "/"
                                                + CustomDateTime.getTime(new Date()))
                                        .setValue(-value);

                                setDebtTextView(sum);
                            } else
                                Toast.makeText(activity, "Yanlış dəyər!!!", Toast.LENGTH_LONG).show();
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
                } catch (Exception e) {
                    Toast.makeText(activity, "Yanlış dəyər!!!", Toast.LENGTH_LONG).show();
                }
            });
            builder.setNegativeButton(R.string.cancel, (dialog, which) -> dialog.cancel());
            builder.show();
        });
    }

    private void setDebtTextView(Double sum) {
        textViewDebt.setText("Borc: " + sum);
        if (sum < 0) textViewDebt.setTextColor(Color.parseColor("#33FF33"));
        else if (sum > 0) textViewDebt.setTextColor(Color.parseColor("#FF5722"));
        else textViewDebt.setTextColor(Color.parseColor("#FFFFFF"));
    }

    private void configureSpinner() {
        Spinner spinner = view.findViewById(R.id.spinnerFabrics);
        spinner.setOnItemSelectedListener(this);

        spinnerAdapter = new ArrayAdapter<>(activity, R.layout.simple_spinner_item, fabricList);
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(spinnerAdapter);
    }

    private void loadFabrics() {
        drFabrics.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot snapshotFabric : snapshot.getChildren())
                    fabricList.add(snapshotFabric.getKey());
                spinnerAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    @Override
    public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
        selectedFabric = parent.getItemAtPosition(position).toString();
        getDebt();
    }

    private void getDebt() {
        if (selectedFabric == null) return;
        drDebts.child(selectedFabric + "/sum").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Double debt = snapshot.getValue(Double.class);
                if (debt == null) debt = 0.0;

                debt = SharedClass.twoDigitDecimal(debt);

                setDebtTextView(debt);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        getDebt();
    }

    public void onNothingSelected(AdapterView<?> arg0) {
        // TODO Auto-generated method stub
    }

    private void configureBasketButton() {
        ImageButton basketButton = view.findViewById(R.id.buttonBasket);
        basketButton.setOnClickListener(v -> {
            Intent intent = new Intent(activity, BuyerBasketActivity.class);
            intent.putExtra("fabric", selectedFabric);
            intent.putExtra("isNewBuy", isNewBuy);
            startActivity(intent);
        });
    }

    private void configureSearchEditText() {
        EditText etSearch = view.findViewById(R.id.editTextSearch);
        SharedClass.configureSearchEditText(etSearch, nameList, searchChildNameList, adapter);
    }

    private void configureListViewDepo() {
        adapter = new RecyclerBuysProductsBuyer(activity, nameList, childNameList, searchChildNameList, priceList);
        RecyclerView myView = view.findViewById(R.id.recyclerviewProducts);
        myView.setHasFixedSize(true);
        myView.setAdapter(adapter);
        LinearLayoutManager llm = new LinearLayoutManager(activity);
        llm.setOrientation(LinearLayoutManager.VERTICAL);
        myView.setLayoutManager(llm);

        loadProductsFromDatabase();
    }

    private void loadProductsFromDatabase() {
        drProducts.addListenerForSingleValueEvent(new ValueEventListener() {
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
                    Double _price = snapshotTaxis.child("buyPrice").getValue(Double.class);

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