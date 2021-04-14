package texel.com.depoproject.Pages.SELLER.Fragments.ReturnToDepo;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
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

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;

import texel.com.depoproject.CustomDialogs.CustomProgressDialog;
import texel.com.depoproject.DataClasses.ReturnToDepoProductInfo;
import texel.com.depoproject.HelperClasses.DatabaseFunctions;
import texel.com.depoproject.HelperClasses.SharedClass;
import texel.com.depoproject.Pages.SELLER.Fragments.ReturnToDepo.Basket.SellerBasketReturnActivity;
import texel.com.depoproject.R;

public class SellerReturnToDepoFragment extends Fragment {

    private final HashMap<String, ReturnToDepoProductInfo> productInfoHashMap = new HashMap<>();
    private final ArrayList<String> nameList = new ArrayList<>();
    private final ArrayList<String> searchNameList = new ArrayList<>();
    private CustomProgressDialog progressDialog;
    private TextView textViewNoItemInfo;
    private View view;
    private DatabaseReference databaseReferenceProducts;
    private SellerRecyclerReturnToDepo adapter;

    private Activity activity;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_return_to_depo_seller, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        this.view = view;
        activity = getActivity();
        textViewNoItemInfo = view.findViewById(R.id.textViewNoItemInfo);
        textViewNoItemInfo.setText("Qeydiyyatda Olan Məhsul Yoxdur");

        progressDialog = new CustomProgressDialog(activity, getString(R.string.data_loading));
        progressDialog.show();

        databaseReferenceProducts = DatabaseFunctions.getDatabases(getContext()).get(0).child("PRODUCTS");

        configureBasketButton();
        configureListViewDepo();
        configureSearchEditText();
    }

    private void configureBasketButton() {
        ImageButton basketButton = view.findViewById(R.id.buttonBasket);
        basketButton.setOnClickListener(v -> {
            Intent intent = new Intent(activity, SellerBasketReturnActivity.class);
            startActivity(intent);
        });
    }

    private void configureSearchEditText() {
        EditText etSearch = view.findViewById(R.id.editTextSearch);
        SharedClass.configureSearchEditText(etSearch, nameList, searchNameList, adapter);
    }

    private void configureListViewDepo() {
        adapter = new SellerRecyclerReturnToDepo(activity, nameList, searchNameList, productInfoHashMap);
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
                    Double _sellPrice = snapshotTaxis.child("sellPrice").getValue(Double.class);

                    productInfoHashMap.put(name, new ReturnToDepoProductInfo(_name, _sellPrice));
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