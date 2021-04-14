package texel.com.depoproject.Pages.DEPO.Fragments.Return.Sells;

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
import com.google.firebase.database.GenericTypeIndicator;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

import texel.com.depoproject.CustomDialogs.CustomProgressDialog;
import texel.com.depoproject.HelperClasses.CustomDateTime;
import texel.com.depoproject.HelperClasses.DatabaseFunctions;
import texel.com.depoproject.HelperClasses.SharedClass;
import texel.com.depoproject.R;

public class DepoReturnSellsByMarketFragment extends Fragment implements DatePickerDialog.OnDateSetListener {

    private final ArrayList<String> nameList = new ArrayList<>();
    private final ArrayList<String> searchNameList = new ArrayList<>();
    private final HashMap<String, ReturnSellByMarketModel> sellList = new HashMap<>();
    private CustomProgressDialog progressDialog;
    private TextView textViewNoItemInfo, beginDateTextView, endDateTextView;
    private View view;
    private DatabaseReference databaseReferenceReturnSell;
    private DepoReturnSellsByMarketsRecycler adapter;

    private Activity activity;

    private boolean dateStatus;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_sells_by_markets_depo, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        this.view = view;
        activity = getActivity();
        textViewNoItemInfo = view.findViewById(R.id.textViewNoItemInfo);
        textViewNoItemInfo.setText("Satış Yoxdur");

        progressDialog = new CustomProgressDialog(activity, getString(R.string.data_loading));

        databaseReferenceReturnSell = DatabaseFunctions.getDatabases(getContext()).get(0).child("RETURN/SELL");

        configureListViewDepo();
        configureSearchEditText();
        configureBeginEndTextViews();
        configureRefreshButton();
        configureFabButton();
    }

    private void configureFabButton() {
        ImageView imageView = view.findViewById(R.id.cardViewFabSearchBySeller);
        imageView.setOnClickListener(v -> {
            FragmentTransaction transaction = getFragmentManager().beginTransaction();
            transaction.replace(R.id.container, new DepoReturnSellsBySellerFragment());
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
        adapter = new DepoReturnSellsByMarketsRecycler(activity, nameList, searchNameList, sellList);
        RecyclerView myView = view.findViewById(R.id.recyclerviewProducts);
        myView.setHasFixedSize(true);
        myView.setAdapter(adapter);
        LinearLayoutManager llm = new LinearLayoutManager(activity);
        llm.setOrientation(LinearLayoutManager.VERTICAL);
        myView.setLayoutManager(llm);

        loadSellsFromDatabase();
    }

    private void loadSellsFromDatabase() {
        GenericTypeIndicator<ArrayList<Double>> d = new GenericTypeIndicator<ArrayList<Double>>() {
        };

        progressDialog.show();

        databaseReferenceReturnSell.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                nameList.clear();
                searchNameList.clear();
                sellList.clear();

                for (DataSnapshot snapshotSellDates : dataSnapshot.getChildren()) {
                    String date = snapshotSellDates.getKey();
                    if (!SharedClass.checkDate(beginDateTextView.getText().toString(),
                            endDateTextView.getText().toString(),
                            date)) continue;

                    for (DataSnapshot snapshotUsers : snapshotSellDates.getChildren()) {
                        String user = snapshotUsers.getKey();

                        for (DataSnapshot snapshotTimes : snapshotUsers.getChildren()) {
                            String time = snapshotTimes.getKey();
                            if (time == null || time.equals("sum") || time.equals("netIncome") || time.equals("percentSum"))
                                continue;

                            ArrayList<Double> _priceList = snapshotTimes.child("price").getValue(d);
                            ArrayList<Double> _amountList = snapshotTimes.child("amount").getValue(d);

                            Double _percent = snapshotTimes.child("percent").getValue(Double.class);
                            String _market = snapshotTimes.child("market").getValue(String.class);

                            _market = _market == null ? getString(R.string.deleted_market) : _market;

                            double sum = 0.0;
                            for (int i = 0; i < _priceList.size(); ++i)
                                sum += _priceList.get(i) * _amountList.get(i);

                            sum = _percent == null ? sum : sum - _percent;
                            sum = SharedClass.twoDigitDecimal(sum);

                            String key = _market + "    " + date;
                            if (sellList.containsKey(key)) {
                                sellList.get(key).sum += sum;
                                sellList.get(key).times.add(snapshotTimes.getKey());
                            } else {
                                int index = 0;
                                for (String s : nameList)
                                    if (s.compareTo(key) > 0) index++;

                                nameList.add(index, key);
                                sellList.put(key, new ReturnSellByMarketModel(user, sum, date, snapshotTimes.getKey()));
                            }
                        }
                    }
                }
                if (nameList.size() > 0) textViewNoItemInfo.setVisibility(View.GONE);
                else textViewNoItemInfo.setVisibility(View.VISIBLE);

                searchNameList.addAll(nameList);
                adapter.notifyDataSetChanged();
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