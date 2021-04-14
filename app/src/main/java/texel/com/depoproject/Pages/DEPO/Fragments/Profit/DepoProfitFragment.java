package texel.com.depoproject.Pages.DEPO.Fragments.Profit;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.DatePicker;
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
import java.util.Calendar;
import java.util.Date;

import texel.com.depoproject.CustomDialogs.CustomProgressDialog;
import texel.com.depoproject.HelperClasses.CustomDateTime;
import texel.com.depoproject.HelperClasses.DatabaseFunctions;
import texel.com.depoproject.HelperClasses.SharedClass;
import texel.com.depoproject.R;

public class DepoProfitFragment extends Fragment implements DatePickerDialog.OnDateSetListener {

    private final ArrayList<String> nameList = new ArrayList<>();
    private final ArrayList<String> searchNameList = new ArrayList<>();
    private final ArrayList<Double> profitList = new ArrayList<>();
    private final ArrayList<Double> incomeList = new ArrayList<>();
    private CustomProgressDialog progressDialog;
    private TextView textViewNoItemInfo, textViewBeginDate, textViewEndDate, textViewProfit, textViewIncome;
    private View view;
    private DatabaseReference databaseReferenceProfit;
    private DepoProfitRecycler adapter;

    private Activity activity;

    private boolean dateStatus;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_profit_depo, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        this.view = view;
        activity = getActivity();

        textViewProfit = view.findViewById(R.id.textViewProfit);
        textViewIncome = view.findViewById(R.id.textViewIncome);
        textViewNoItemInfo = view.findViewById(R.id.textViewNoItemInfo);
        textViewNoItemInfo.setText("Satış Yoxdur");

        progressDialog = new CustomProgressDialog(activity, getString(R.string.data_loading));

        databaseReferenceProfit = DatabaseFunctions.getDatabases(getContext()).get(0).child("SELLS");

        configureListViewDepo();
        configureSearchEditText();
        configureBeginEndTextViews();
        configureRefreshButton();
    }

    private void configureRefreshButton() {
        ImageButton refresh = view.findViewById(R.id.imageButtonRefresh);
        refresh.setOnClickListener(v -> {
            loadProfitFromDatabase();
        });
    }

    private void configureBeginEndTextViews() {
        textViewBeginDate = view.findViewById(R.id.textViewBeginDate);
        textViewEndDate = view.findViewById(R.id.textViewEndDate);

        textViewBeginDate.setText(CustomDateTime.getDate(new Date()));
        textViewEndDate.setText(CustomDateTime.getDate(new Date()));

        textViewBeginDate.setOnClickListener(v -> {
            dateStatus = true;
            showDatePickerDialog();
        });

        textViewEndDate.setOnClickListener(v -> {
            dateStatus = false;
            showDatePickerDialog();
        });
    }

    private void configureSearchEditText() {
        EditText etSearch = view.findViewById(R.id.editTextSearch);
        SharedClass.configureSearchEditText(etSearch, nameList, searchNameList, adapter);
    }

    private void configureListViewDepo() {
        adapter = new DepoProfitRecycler(activity, nameList, searchNameList, profitList, incomeList);
        RecyclerView myView = view.findViewById(R.id.recyclerviewProducts);
        myView.setHasFixedSize(true);
        myView.setAdapter(adapter);
        LinearLayoutManager llm = new LinearLayoutManager(activity);
        llm.setOrientation(LinearLayoutManager.VERTICAL);
        myView.setLayoutManager(llm);

        loadProfitFromDatabase();
    }

    private void loadProfitFromDatabase() {
        progressDialog.show();

        databaseReferenceProfit.addListenerForSingleValueEvent(new ValueEventListener() {
            @SuppressLint("DefaultLocale")
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                nameList.clear();
                searchNameList.clear();
                profitList.clear();
                incomeList.clear();

                double all_sum = 0.0, all_income = 0.0;
                for (DataSnapshot snapshotSellDates : dataSnapshot.getChildren()) {
                    String date = snapshotSellDates.getKey();
                    if (!SharedClass.checkDate(textViewBeginDate.getText().toString(),
                            textViewEndDate.getText().toString(), date)) continue;

                    double sum = 0.0, income = 0.0;
                    for (DataSnapshot snapshotUser : snapshotSellDates.getChildren()) {
                        Double s = snapshotUser.child("sum").getValue(Double.class);
                        Double i = snapshotUser.child("netIncome").getValue(Double.class);
                        if (s == null || s == 0 || i == null || i == 0.0) continue;
                        sum += s;
                        income += i;
                    }

                    int index = 0;
                    for (String s : nameList)
                        if (s.compareTo(date) > 0) index++;

                    nameList.add(index, date);
                    profitList.add(index, SharedClass.twoDigitDecimal(sum));
                    incomeList.add(index, SharedClass.twoDigitDecimal(income));

                    all_sum += sum;
                    all_income += income;
                }
                if (nameList.size() > 0) textViewNoItemInfo.setVisibility(View.GONE);
                else textViewNoItemInfo.setVisibility(View.VISIBLE);

                textViewProfit.setText(String.format("%.2f", all_sum));
                textViewIncome.setText(String.format("%.2f", all_income));

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

    private void showDatePickerDialog() {
        DatePickerDialog datePickerDialog = new DatePickerDialog(getContext(), this,
                Calendar.getInstance().get(Calendar.YEAR),
                Calendar.getInstance().get(Calendar.MONTH),
                Calendar.getInstance().get(Calendar.DAY_OF_MONTH));
        datePickerDialog.setButton(DatePickerDialog.BUTTON_NEGATIVE, getString(R.string.cancel), datePickerDialog);
        datePickerDialog.show();
    }

    @Override
    public void onDateSet(DatePicker datePicker, int year, int month, int day) {
        ++month;
        String m = month < 10 ? "0" + month : "" + month;
        String d = day < 10 ? "0" + day : "" + day;

        if (dateStatus)
            textViewBeginDate.setText(year + "_" + m + "_" + d);
        else textViewEndDate.setText(year + "_" + m + "_" + d);
    }
}