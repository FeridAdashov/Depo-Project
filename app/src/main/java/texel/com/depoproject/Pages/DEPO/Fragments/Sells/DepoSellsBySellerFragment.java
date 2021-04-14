package texel.com.depoproject.Pages.DEPO.Fragments.Sells;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
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
import texel.com.depoproject.R;

public class DepoSellsBySellerFragment extends Fragment implements DatePickerDialog.OnDateSetListener {

    private final ArrayList<String> nameList = new ArrayList<>();
    private final ArrayList<SellBySellerModel> sellList = new ArrayList<>();
    private final ArrayList<String> searchNameList = new ArrayList<>();
    private CustomProgressDialog progressDialog;
    private TextView textViewNoItemInfo, beginDateTextView, endDateTextView;
    private View view;
    private DatabaseReference databaseReferenceSells;
    private DepoSellsBySellerRecycler adapter;

    private Activity activity;

    private boolean dateStatus;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_sells_by_seller_depo, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        this.view = view;
        activity = getActivity();
        textViewNoItemInfo = view.findViewById(R.id.textViewNoItemInfo);
        textViewNoItemInfo.setText("Satış Yoxdur");

        progressDialog = new CustomProgressDialog(activity, getString(R.string.data_loading));

        databaseReferenceSells = DatabaseFunctions.getDatabases(getContext()).get(0).child("SELLS");

        configureListViewDepo();
        configureSearchEditText();
        configureBeginEndTextViews();
        configureRefreshButton();
        configureFabButton();
    }

    private void configureFabButton() {
        ImageView imageView = view.findViewById(R.id.cardViewFabSearchByMarkets);
        imageView.setOnClickListener(v -> {
            FragmentTransaction transaction = getFragmentManager().beginTransaction();
            transaction.replace(R.id.container, new DepoSellsByMarketFragment());
            transaction.commit();
        });
    }

    private void configureRefreshButton() {
        ImageButton refresh = view.findViewById(R.id.imageButtonRefresh);
        refresh.setOnClickListener(v -> {
            loadSellsFromDatabase();
        });
    }

    private void configureBeginEndTextViews() {
        beginDateTextView = view.findViewById(R.id.textViewBeginDate);
        endDateTextView = view.findViewById(R.id.textViewEndDate);

        beginDateTextView.setText(CustomDateTime.getDate(new Date()));
        endDateTextView.setText(CustomDateTime.getDate(new Date()));

        beginDateTextView.setOnClickListener(v -> {
            dateStatus = true;
            SharedClass.showDatePickerDialog(activity, this);
        });

        endDateTextView.setOnClickListener(v -> {
            dateStatus = false;
            SharedClass.showDatePickerDialog(activity, this);
        });
    }

    private void configureSearchEditText() {
        EditText etSearch = view.findViewById(R.id.editTextSearch);
        SharedClass.configureSearchEditText(etSearch, nameList, searchNameList, adapter);
    }

    private void configureListViewDepo() {
        adapter = new DepoSellsBySellerRecycler(activity, nameList, searchNameList, sellList);
        RecyclerView myView = view.findViewById(R.id.recyclerviewProducts);
        myView.setHasFixedSize(true);
        myView.setAdapter(adapter);
        LinearLayoutManager llm = new LinearLayoutManager(activity);
        llm.setOrientation(LinearLayoutManager.VERTICAL);
        myView.setLayoutManager(llm);

        loadSellsFromDatabase();
    }

    private void loadSellsFromDatabase() {
        nameList.clear();
        searchNameList.clear();
        sellList.clear();

        progressDialog.show();

        DatabaseReference drUsers = DatabaseFunctions.getDatabases(activity).get(0).child("USERS/SELLER");

        databaseReferenceSells.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                nameList.clear();
                searchNameList.clear();

                for (DataSnapshot snapshotSellDates : dataSnapshot.getChildren()) {
                    String date = snapshotSellDates.getKey();
                    if (!SharedClass.checkDate(beginDateTextView.getText().toString(),
                            endDateTextView.getText().toString(),
                            date)) continue;

                    for (DataSnapshot snapshotUsers : snapshotSellDates.getChildren()) {
                        drUsers.child(snapshotUsers.getKey() + "/ABOUT/name").addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshotUserName) {
                                String user = snapshotUserName.getValue(String.class);
                                user = user == null ? getString(R.string.deleted_user) : user;

                                Double sum = snapshotUsers.child("sum").getValue(Double.class);
                                Double netIncome = snapshotUsers.child("netIncome").getValue(Double.class);
                                Double percentSum = snapshotUsers.child("percentSum").getValue(Double.class);
                                sum = sum == null ? 0.0 : sum;

                                String key = user + "    " + date;

                                int index = 0;
                                for (String s : nameList)
                                    if (s.compareTo(key) > 0) index++;

                                nameList.add(index, key);
                                searchNameList.add(index, key);
                                sellList.add(index, new SellBySellerModel(date,
                                        snapshotUsers.getKey(),
                                        snapshotUserName.getKey(),
                                        sum,
                                        netIncome,
                                        percentSum));

                                if (nameList.size() > 0)
                                    textViewNoItemInfo.setVisibility(View.GONE);
                                else textViewNoItemInfo.setVisibility(View.VISIBLE);

                                adapter.notifyDataSetChanged();
                            }

                            @Override
                            public void onCancelled(@NonNull DatabaseError error) {

                            }
                        });
                    }
                }
                progressDialog.dismiss();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                progressDialog.dismiss();
            }
        });
    }

    @Override
    public void onDateSet(DatePicker datePicker, int year, int month, int day) {
        ++month;
        String m = month < 10 ? "0" + month : "" + month;
        String d = day < 10 ? "0" + day : "" + day;

        if (dateStatus)
            beginDateTextView.setText(year + "_" + m + "_" + d);
        else endDateTextView.setText(year + "_" + m + "_" + d);
    }
}