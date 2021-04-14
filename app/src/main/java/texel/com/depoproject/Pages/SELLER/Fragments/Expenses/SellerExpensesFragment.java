package texel.com.depoproject.Pages.SELLER.Fragments.Expenses;

import android.app.Activity;
import android.app.AlertDialog;
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
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
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

public class SellerExpensesFragment extends Fragment implements DatePickerDialog.OnDateSetListener {

    private final ArrayList<String> dateTimeList = new ArrayList<>();
    private final ArrayList<String> searchDateTimeList = new ArrayList<>();
    private final ArrayList<String> nameList = new ArrayList<>();
    private final ArrayList<Double> valueList = new ArrayList<>();
    private CustomProgressDialog progressDialog;
    private TextView textViewNoItemInfo, beginDateTextView, endDateTextView;
    private View view;
    private DatabaseReference databaseReferenceExpenses;
    private SellerRecyclerExpenses adapter;

    private Activity activity;
    private String user_name;
    private boolean dateStatus;
    private int radioButtonStatus = 1;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_expenses_seller, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        this.view = view;
        activity = getActivity();
        textViewNoItemInfo = view.findViewById(R.id.textViewNoItemInfo);
        textViewNoItemInfo.setText("Xərc Yoxdur");

        progressDialog = new CustomProgressDialog(activity, getString(R.string.data_loading));

        user_name = FirebaseAuth.getInstance().getCurrentUser().getEmail().split("@")[0];

        databaseReferenceExpenses = DatabaseFunctions.getDatabases(getContext()).get(0).child("EXPENSES");

        configureAddExpenseButton();
        configureRadioButtons();
        configureListViewDepo();
        configureSearchEditText();
        configureBeginEndTextViews();
        configureRefreshButton();
    }

    private void configureAddExpenseButton() {
        ImageButton addButton = view.findViewById(R.id.imageButtonAddOtherExpense);
        addButton.setOnClickListener(v -> {
            final View view = getLayoutInflater().inflate(R.layout.new_expense_dialog_view, null);
            final EditText name = view.findViewById(R.id.cardTextViewExpenseName);
            final EditText amount = view.findViewById(R.id.cardTextViewExpenseAmount);

            AlertDialog.Builder b = new AlertDialog.Builder(getContext());
            b.setPositiveButton(R.string.save, (dialog, which) -> {
            });
            b.setNegativeButton(R.string.cancel, (dialog, which) -> dialog.dismiss());
            b.setView(view);

            final AlertDialog dialog = b.create();
            dialog.show();
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v1 -> {
                try {
                    String n = name.getText().toString().trim();
                    double a = Double.parseDouble(amount.getText().toString().trim());
                    addOtherExpense(n, a);
                    dialog.dismiss();
                } catch (Exception e) {
                    Toast.makeText(activity, "Yanlış dəyər daxil etdiniz!", Toast.LENGTH_LONG).show();
                }
            });
        });
    }

    private void addOtherExpense(String name, double amount) {
        String date = CustomDateTime.getDate(new Date());
        String time = CustomDateTime.getTime(new Date());

        databaseReferenceExpenses.child("Other/" + user_name + "/" + date).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Double sum = snapshot.child("sum").getValue(Double.class);
                sum = sum == null ? amount : sum + amount;
                sum = SharedClass.twoDigitDecimal(sum);

                snapshot.child("sum").getRef().setValue(sum);
                snapshot.child(time + "/name").getRef().setValue(name);
                snapshot.child(time + "/value").getRef().setValue(amount);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
        loadExpensesFromDatabase();
    }

    private void configureRadioButtons() {
        RadioGroup radioGroupUsers = view.findViewById(R.id.radioGroupBuyReturn);
        radioGroupUsers.setOnCheckedChangeListener((radioGroup, i) -> {
            switch (i) {
                case R.id.radioPercent:
                    radioButtonStatus = 1;
                    break;

                case R.id.radioOther:
                    radioButtonStatus = 2;
                    break;
            }
        });
    }

    private void configureRefreshButton() {
        ImageButton refresh = view.findViewById(R.id.imageButtonRefresh);
        refresh.setOnClickListener(v -> loadExpensesFromDatabase());
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
        SharedClass.configureSearchEditText(etSearch, dateTimeList, searchDateTimeList, adapter);
    }

    private void configureListViewDepo() {
        adapter = new SellerRecyclerExpenses(dateTimeList, searchDateTimeList, nameList, valueList);
        RecyclerView myView = view.findViewById(R.id.recyclerviewProducts);
        myView.setHasFixedSize(true);
        myView.setAdapter(adapter);
        LinearLayoutManager llm = new LinearLayoutManager(activity);
        llm.setOrientation(LinearLayoutManager.VERTICAL);
        myView.setLayoutManager(llm);

        loadExpensesFromDatabase();
    }

    private void loadExpensesFromDatabase() {
        progressDialog.show();

        String parent_child = radioButtonStatus == 1 ? "Percent/" + user_name : "Other/" + user_name;

        databaseReferenceExpenses.child(parent_child).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                dateTimeList.clear();
                searchDateTimeList.clear();
                nameList.clear();
                valueList.clear();

                for (DataSnapshot snapshotDates : dataSnapshot.getChildren()) {
                    String date = snapshotDates.getKey();

                    if (SharedClass.checkDate(beginDateTextView.getText().toString(),
                            endDateTextView.getText().toString(),
                            date))
                        for (DataSnapshot snapshotTimes : snapshotDates.getChildren()) {
                            String time = snapshotTimes.getKey();
                            if (time == null || time.equals("sum") || time.equals("netIncome") || time.equals("percentSum"))
                                continue;

                            String name = date + "     " + time;

                            int index = 0;
                            for (String s : dateTimeList)
                                if (s.compareTo(name) > 0) index++;

                            dateTimeList.add(index, name);
                            nameList.add(index, snapshotTimes.child("name").getValue(String.class));
                            valueList.add(index, snapshotTimes.child("value").getValue(Double.class));
                        }
                }
                if (dateTimeList.size() > 0) textViewNoItemInfo.setVisibility(View.GONE);
                else textViewNoItemInfo.setVisibility(View.VISIBLE);

                searchDateTimeList.addAll(dateTimeList);
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