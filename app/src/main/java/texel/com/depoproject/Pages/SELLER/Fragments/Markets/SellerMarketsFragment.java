package texel.com.depoproject.Pages.SELLER.Fragments.Markets;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
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
import texel.com.depoproject.DataClasses.MarketInfo;
import texel.com.depoproject.HelperClasses.DatabaseFunctions;
import texel.com.depoproject.HelperClasses.SharedClass;
import texel.com.depoproject.R;

public class SellerMarketsFragment extends Fragment {

    private final HashMap<String, MarketInfo> cardViewMarketInfoHashMap = new HashMap<>();
    private final ArrayList<String> nameList = new ArrayList<>();
    private final ArrayList<String> searchNameList = new ArrayList<>();
    private CustomProgressDialog progressDialog;
    private TextView textViewNoItemInfo;
    private View view;
    private DatabaseReference drMarkets, drDebts;
    private SellerRecyclerMarkets adapter;

    private Activity activity;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_markets_seller, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        this.view = view;
        activity = getActivity();
        textViewNoItemInfo = view.findViewById(R.id.textViewNoItemInfo);
        textViewNoItemInfo.setText("Qeydiyyatda Olan Market Yoxdur");

        progressDialog = new CustomProgressDialog(activity, getString(R.string.data_loading));
        progressDialog.show();

        drMarkets = DatabaseFunctions.getDatabases(getContext()).get(0).child("MARKETS");
        drDebts = DatabaseFunctions.getDatabases(getContext()).get(0).child("DEBTS/MARKET");

        configureListViewSeller();
        configureSearchEditText();
    }

    private void configureSearchEditText() {
        EditText etSearch = view.findViewById(R.id.editTextSearch);
        SharedClass.configureSearchEditText(etSearch, nameList, searchNameList, adapter);
    }

    private void configureListViewSeller() {
        adapter = new SellerRecyclerMarkets(activity, nameList, searchNameList, cardViewMarketInfoHashMap);
        RecyclerView myView = view.findViewById(R.id.recyclerviewMarkets);
        myView.setHasFixedSize(true);
        myView.setAdapter(adapter);
        LinearLayoutManager llm = new LinearLayoutManager(activity);
        llm.setOrientation(LinearLayoutManager.VERTICAL);
        myView.setLayoutManager(llm);

        loadMarketsFromDatabase();
    }

    private void loadMarketsFromDatabase() {
        drMarkets.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                cardViewMarketInfoHashMap.clear();
                nameList.clear();
                searchNameList.clear();

                for (DataSnapshot snapshotMarkets : dataSnapshot.getChildren()) {
                    Boolean active = snapshotMarkets.child("active").getValue(Boolean.class);
                    if (active == null || !active) continue;

                    String _name = snapshotMarkets.getKey();
                    String _phone = snapshotMarkets.child("phone").getValue(String.class);
                    String _address = snapshotMarkets.child("address").getValue(String.class);

                    MarketInfo marketInfo = new MarketInfo(_name, _phone, _address, true);

                    nameList.add(_name);
                    searchNameList.add(_name);
                    cardViewMarketInfoHashMap.put(_name, marketInfo);
                }

                progressDialog.dismiss();
                if (nameList.size() > 0) {
                    getDebts();
                    textViewNoItemInfo.setVisibility(View.GONE);
                    adapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                progressDialog.dismiss();
            }
        });
    }

    private void getDebts() {
        for (String name : nameList)
            drDebts.child(name + "/sum").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    Double debt = snapshot.getValue(Double.class);
                    if (debt == null) debt = 0.0;
                    cardViewMarketInfoHashMap.get(name).debt = debt;
                    adapter.notifyDataSetChanged();
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
    }

    @Override
    public void onResume() {
        super.onResume();
        if (nameList.size() > 0) getDebts();
    }
}