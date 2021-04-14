package texel.com.depoproject.Pages.DEPO.Fragments.Return.Buys.History;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.DatePicker;
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
import java.util.Date;

import texel.com.depoproject.CustomDialogs.CustomProgressDialog;
import texel.com.depoproject.HelperClasses.CustomDateTime;
import texel.com.depoproject.HelperClasses.DatabaseFunctions;
import texel.com.depoproject.HelperClasses.SharedClass;
import texel.com.depoproject.R;

public class DepoReturnBuysFragment extends Fragment implements DatePickerDialog.OnDateSetListener {

    public static String begin_date, end_date;
    private final ArrayList<String> searchDateList = new ArrayList<>();
    private final ArrayList<String> dateList = new ArrayList<>();
    private final ArrayList<Double> buyValueList = new ArrayList<>();
    private final ArrayList<Double> percentValueList = new ArrayList<>();
    private CustomProgressDialog progressDialog;
    private TextView textViewNoItemInfo, beginDateTextView,
            endDateTextView, textViewCommonSum, textViewPercentSum, textViewPaidSum;
    private View view;
    private DatabaseReference drBuys;
    private DepoRecyclerReturnBuyHistory adapter;
    private Activity activity;
    private boolean dateStatus;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_buys_depo, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        this.view = view;
        activity = getActivity();

        textViewCommonSum = view.findViewById(R.id.textViewCommonSum);
        textViewPercentSum = view.findViewById(R.id.textViewPercentSum);
        textViewPaidSum = view.findViewById(R.id.textViewPaidSum);
        textViewNoItemInfo = view.findViewById(R.id.textViewNoItemInfo);
        textViewNoItemInfo.setText("Xərc Yoxdur");

        progressDialog = new CustomProgressDialog(activity, getString(R.string.data_loading));

        drBuys = DatabaseFunctions.getDatabases(activity).get(0).child("RETURN/BUY");

        configureListViewDepo();
        configureBeginEndTextViews();
        configureRefreshButton();
    }

    private void configureRefreshButton() {
        ImageButton refresh = view.findViewById(R.id.imageButtonRefresh);
        refresh.setOnClickListener(v -> loadExpensesFromDatabase());
    }

    private void configureBeginEndTextViews() {
        beginDateTextView = view.findViewById(R.id.textViewBeginDate);
        endDateTextView = view.findViewById(R.id.textViewEndDate);

        begin_date = end_date = CustomDateTime.getDate(new Date());
        beginDateTextView.setText(begin_date);
        endDateTextView.setText(begin_date);

        beginDateTextView.setOnClickListener(v -> {
            dateStatus = true;
            SharedClass.showDatePickerDialog(activity, this);
        });

        endDateTextView.setOnClickListener(v -> {
            dateStatus = false;
            SharedClass.showDatePickerDialog(activity, this);
        });
    }

    private void configureListViewDepo() {
        adapter = new DepoRecyclerReturnBuyHistory(activity, dateList, searchDateList, buyValueList, percentValueList);
        RecyclerView myView = view.findViewById(R.id.recyclerviewProducts);
        myView.setHasFixedSize(true);
        myView.setAdapter(adapter);
        LinearLayoutManager llm = new LinearLayoutManager(activity);
        llm.setOrientation(LinearLayoutManager.VERTICAL);
        myView.setLayoutManager(llm);

        loadExpensesFromDatabase();
    }

    private void loadExpensesFromDatabase() {
        textViewNoItemInfo.setVisibility(View.VISIBLE);
        progressDialog.show();

        drBuys.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                searchDateList.clear();
                dateList.clear();
                buyValueList.clear();
                percentValueList.clear();
                adapter.notifyDataSetChanged();

                if (!dataSnapshot.exists()) {
                    progressDialog.dismiss();
                    return;
                }

                double commonSum = 0.0, percentSum = 0.0, paidSum = 0.0;

                String begin = beginDateTextView.getText().toString();
                String end = endDateTextView.getText().toString();
                for (DataSnapshot snapshotDates : dataSnapshot.getChildren()) {
                    String child_date = snapshotDates.getKey();
                    if (SharedClass.checkDate(begin, end, child_date)) {
                        Double sum = snapshotDates.child("sum").getValue(Double.class);
                        if (sum != null) {
                            Double percentValue = snapshotDates.child("percentSum").getValue(Double.class);
                            if (percentValue == null) percentValue = 0.0;

                            int index = 0;
                            for (String s : dateList)
                                if (s.compareTo(child_date) > 0) index++;

                            dateList.add(index, child_date);
                            searchDateList.add(index, child_date);
                            buyValueList.add(index, SharedClass.twoDigitDecimal(sum));
                            percentValueList.add(index, SharedClass.twoDigitDecimal(percentValue));

                            paidSum += sum;
                            percentSum += percentValue;
                            commonSum += sum + percentValue;
                        }
                    }
                }
                if (dateList.size() > 0) {
                    textViewNoItemInfo.setVisibility(View.GONE);
                    setResultTexts(commonSum, percentSum, paidSum);
                    adapter.notifyDataSetChanged();
                }
                progressDialog.dismiss();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                progressDialog.dismiss();
            }
        });
    }

    private void setResultTexts(double common, double percent, double paid) {
        textViewCommonSum.setText("Ümumi: " + common);
        textViewPercentSum.setText("Faiz: " + percent);
        textViewPaidSum.setText("Nəticə: " + paid);
    }

    @Override
    public void onDateSet(DatePicker datePicker, int year, int month, int day) {
        ++month;
        String m = month < 10 ? "0" + month : "" + month;
        String d = day < 10 ? "0" + day : "" + day;
        String date = year + "_" + m + "_" + d;

        if (dateStatus) {
            beginDateTextView.setText(date);
            begin_date = date;
        } else {
            endDateTextView.setText(date);
            end_date = date;
        }
    }
}