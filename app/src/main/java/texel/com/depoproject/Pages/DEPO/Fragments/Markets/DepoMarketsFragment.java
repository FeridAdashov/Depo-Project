package texel.com.depoproject.Pages.DEPO.Fragments.Markets;

import android.app.Activity;
import android.app.AlertDialog;
import android.os.Bundle;
import android.text.TextUtils;
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
import texel.com.depoproject.DataClasses.MarketInfo;
import texel.com.depoproject.HelperClasses.DatabaseFunctions;
import texel.com.depoproject.HelperClasses.SharedClass;
import texel.com.depoproject.R;

public class DepoMarketsFragment extends Fragment {

    private final HashMap<String, MarketInfo> marketInfoHashMap = new HashMap<>();
    private final ArrayList<String> nameList = new ArrayList<>();
    private final ArrayList<String> searchNameList = new ArrayList<>();
    private CustomProgressDialog progressDialog;
    private TextView textViewNoItemInfo;
    private View view;
    private DatabaseReference databaseReferenceMarkets;
    private DepoRecyclerMarkets adapter;

    private Activity activity;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_markets_depo, container, false);
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

        databaseReferenceMarkets = DatabaseFunctions.getDatabases(getContext()).get(0).child("MARKETS");

        configureListViewDepo();
        configureSearchEditText();

        ImageButton addMarket = view.findViewById(R.id.addMarket);
        addMarket.setOnClickListener(v -> addNewMarket());
    }

    private void addNewMarket() {
        final View view = getLayoutInflater().inflate(R.layout.new_market_dialog_view, null);
        final EditText name = view.findViewById(R.id.cardTextViewMarketName);
        final EditText phone = view.findViewById(R.id.cardTextViewMarketPhone);
        final EditText address = view.findViewById(R.id.cardTextViewMarketAddress);
        final SwitchMaterial switchActiveness = view.findViewById(R.id.switchActiveness);

        AlertDialog.Builder b = new AlertDialog.Builder(getContext());
        b.setPositiveButton(R.string.save, (dialog, which) -> {
        });
        b.setNegativeButton(R.string.cancel, (dialog, which) -> dialog.dismiss());
        b.setView(view);

        final AlertDialog dialog = b.create();
        dialog.show();
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {

            String n = name.getText().toString().trim();
            String p = phone.getText().toString().trim();
            String a = address.getText().toString().trim();
            boolean c = switchActiveness.isChecked();

            if (TextUtils.isEmpty(n) || TextUtils.isEmpty(p))
                SharedClass.showSnackBar(activity, "Yanlış dəyər daxil etdiniz!");
            else {
                saveMarket(n, new MarketInfo(n, p, a, c));
                dialog.dismiss();
            }
        });
    }

    private void saveMarket(String name, MarketInfo marketInfo) {
        marketInfoHashMap.put(name, marketInfo);
        nameList.add(name);
        searchNameList.add(name);
        adapter.notifyDataSetChanged();

        databaseReferenceMarkets.child(name).setValue(marketInfo);

        textViewNoItemInfo.setVisibility(View.GONE);
    }

    private void configureSearchEditText() {
        EditText etSearch = view.findViewById(R.id.editTextSearch);
        SharedClass.configureSearchEditText(etSearch, nameList, searchNameList, adapter);
    }

    private void configureListViewDepo() {
        adapter = new DepoRecyclerMarkets(activity, nameList, searchNameList, marketInfoHashMap);
        RecyclerView myView = view.findViewById(R.id.recyclerviewMarkets);
        myView.setHasFixedSize(true);
        myView.setAdapter(adapter);
        LinearLayoutManager llm = new LinearLayoutManager(activity);
        llm.setOrientation(LinearLayoutManager.VERTICAL);
        myView.setLayoutManager(llm);

        loadMarketsFromDatabase();
    }

    private void loadMarketsFromDatabase() {
        databaseReferenceMarkets.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                marketInfoHashMap.clear();
                nameList.clear();
                searchNameList.clear();

                for (DataSnapshot snapshotTaxis : dataSnapshot.getChildren()) {
                    String _name = snapshotTaxis.getKey();
                    String _phone = snapshotTaxis.child("phone").getValue(String.class);
                    String _address = snapshotTaxis.child("address").getValue(String.class);
                    Boolean _active = snapshotTaxis.child("active").getValue(Boolean.class);

                    nameList.add(_name);
                    marketInfoHashMap.put(_name, new MarketInfo(_name, _phone, _address, _active));
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