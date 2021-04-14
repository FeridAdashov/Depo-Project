package texel.com.depoproject.Pages.DEPO.Fragments.Statistics;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Date;
import java.util.TreeMap;

import texel.com.depoproject.CustomDialogs.CustomProgressDialog;
import texel.com.depoproject.HelperClasses.CustomDateTime;
import texel.com.depoproject.HelperClasses.DatabaseFunctions;
import texel.com.depoproject.HelperClasses.SharedClass;
import texel.com.depoproject.R;

public class DepoStatisticsFragment extends Fragment implements DatePickerDialog.OnDateSetListener {

    private final ArrayList<String> nameList = new ArrayList<>();
    private final ArrayList<String> searchNameList = new ArrayList<>();
    private final TreeMap<String, Double> sumList = new TreeMap<>();
    private CustomProgressDialog progressDialog;
    private TextView textViewNoItemInfo, beginDateTextView, endDateTextView;
    private View view;
    private DatabaseReference drSells, drUsers, drMarkets;
    private DepoStatisticsRecycler adapter;

    private Activity activity;

    private boolean dateStatus, isSearchingBySellers = true;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_statistics_depo, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        this.view = view;
        activity = getActivity();
        textViewNoItemInfo = view.findViewById(R.id.textViewNoItemInfo);
        textViewNoItemInfo.setText("Statistika Yoxdur");

        progressDialog = new CustomProgressDialog(activity, getString(R.string.data_loading));

        drSells = DatabaseFunctions.getDatabases(getContext()).get(0).child("SELLS");
        drUsers = DatabaseFunctions.getDatabases(activity).get(0).child("USERS/SELLER");
        drMarkets = DatabaseFunctions.getDatabases(activity).get(0).child("MARKETS");

        configureListViewDepo();
        configureSearchEditText();
        configureBeginEndTextViews();
        configureRefreshButton();
        configureRadioButtons();
    }

    private void configureRadioButtons() {
        RadioGroup radioGroupUsers = view.findViewById(R.id.radioGroupStatistics);
        radioGroupUsers.setOnCheckedChangeListener((radioGroup, i) -> {
            switch (i) {
                case R.id.radioSeller:
                    isSearchingBySellers = true;
                    break;

                case R.id.radioMarket:
                    isSearchingBySellers = false;
                    break;
            }
        });
    }

    private void configureRefreshButton() {
        ImageButton refresh = view.findViewById(R.id.imageButtonRefresh);
        refresh.setOnClickListener(v -> {
            if (isSearchingBySellers) loadStatisticsBySeller();
            else loadStatisticsByMarket();
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
        adapter = new DepoStatisticsRecycler(activity, nameList, searchNameList, sumList);
        RecyclerView myView = view.findViewById(R.id.recyclerviewStatistics);
        myView.setHasFixedSize(true);
        myView.setAdapter(adapter);
        LinearLayoutManager llm = new LinearLayoutManager(activity);
        llm.setOrientation(LinearLayoutManager.VERTICAL);
        myView.setLayoutManager(llm);

        if (isSearchingBySellers) loadStatisticsBySeller();
        else loadStatisticsByMarket();
    }

    private void loadStatisticsBySeller() {
        progressDialog.show();

        drSells.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                nameList.clear();
                searchNameList.clear();
                sumList.clear();

                for (DataSnapshot snapshotSellDates : dataSnapshot.getChildren()) {
                    String date = snapshotSellDates.getKey();
                    if (!SharedClass.checkDate(beginDateTextView.getText().toString(),
                            endDateTextView.getText().toString(),
                            date)) continue;

                    for (DataSnapshot snapshotUsers : snapshotSellDates.getChildren()) {
                        drUsers.child(snapshotUsers.getKey() + "/ABOUT/name").addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(@NonNull DataSnapshot snapshotUserName) {

                                Double sum = snapshotUsers.child("sum").getValue(Double.class);
                                sum = sum == null ? 0.0 : sum;

                                String user = snapshotUserName.getValue(String.class);
                                if (user == null)
                                    user = getString(R.string.deleted_user) + " (" + snapshotUsers.getKey() + ")";

                                if (sumList.containsKey(user)) {
                                    double new_sum = sumList.get(user) + sum;

                                    nameList.remove(user);
                                    searchNameList.remove(user);
                                    sumList.remove(user);

                                    int index = 0;
                                    for (Double value : sumList.values())
                                        if (new_sum <= value) ++index;

                                    nameList.add(index, user);
                                    searchNameList.add(index, user);
                                    sumList.put(user, SharedClass.twoDigitDecimal(new_sum));
                                } else {
                                    int index = 0;
                                    for (Double value : sumList.values())
                                        if (sum <= value) ++index;

                                    sumList.put(user, sum);
                                    searchNameList.add(index, user);
                                    nameList.add(index, user);
                                }

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

    private void loadStatisticsByMarket() {
        GenericTypeIndicator<ArrayList<Double>> d = new GenericTypeIndicator<ArrayList<Double>>() {
        };
        progressDialog.show();

        drSells.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                nameList.clear();
                searchNameList.clear();
                sumList.clear();

                for (DataSnapshot snapshotSellDates : dataSnapshot.getChildren()) {
                    String date = snapshotSellDates.getKey();
                    if (!SharedClass.checkDate(beginDateTextView.getText().toString(),
                            endDateTextView.getText().toString(),
                            date)) continue;

                    for (DataSnapshot snapshotUsers : snapshotSellDates.getChildren()) {
                        for (DataSnapshot snapshotTimes : snapshotUsers.getChildren()) {
                            String time = snapshotTimes.getKey();
                            if (time == null || time.equals("sum") || time.equals("netIncome") || time.equals("percentSum"))
                                continue;

                            String market_id = snapshotTimes.child("market").getValue(String.class);
                            drMarkets.child(market_id + "/name").addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshotMarketName) {
                                    String market = snapshotMarketName.getValue(String.class);
                                    if (market == null)
                                        market = getString(R.string.deleted_user) + " (" + market_id + ")";

                                    ArrayList<Double> _priceList = snapshotTimes.child("price").getValue(d);
                                    ArrayList<Double> _amountList = snapshotTimes.child("amount").getValue(d);
                                    Double _percent = snapshotTimes.child("percent").getValue(Double.class);

                                    if (_percent == null) _percent = 0.0;

                                    double sum = -_percent;
                                    for (int i = 0; i < _priceList.size(); ++i)
                                        sum += _priceList.get(i) * _amountList.get(i);

                                    if (sumList.containsKey(market)) {
                                        double new_sum = sumList.get(market) + sum;

                                        nameList.remove(market);
                                        searchNameList.remove(market);
                                        sumList.remove(market);

                                        int index = 0;
                                        for (Double value : sumList.values())
                                            if (new_sum <= value) ++index;

                                        nameList.add(index, market);
                                        searchNameList.add(index, market);
                                        sumList.put(market, SharedClass.twoDigitDecimal(new_sum));
                                    } else {
                                        int index = 0;
                                        for (Double value : sumList.values())
                                            if (sum <= value) ++index;

                                        sumList.put(market, sum);
                                        searchNameList.add(index, market);
                                        nameList.add(index, market);
                                    }

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