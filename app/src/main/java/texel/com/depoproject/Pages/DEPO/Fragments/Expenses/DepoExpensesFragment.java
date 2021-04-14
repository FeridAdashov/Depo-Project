package texel.com.depoproject.Pages.DEPO.Fragments.Expenses;

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
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Date;

import texel.com.depoproject.CustomDialogs.CustomProgressDialog;
import texel.com.depoproject.HelperClasses.CustomDateTime;
import texel.com.depoproject.HelperClasses.DatabaseFunctions;
import texel.com.depoproject.HelperClasses.SharedClass;
import texel.com.depoproject.R;

public class DepoExpensesFragment extends Fragment implements DatePickerDialog.OnDateSetListener {

    public static String begin_date, end_date;
    public static boolean isPercent = true;
    private final ArrayList<String> searchNameList = new ArrayList<>();
    private final ArrayList<String> nameList = new ArrayList<>();
    private final ArrayList<String> userNameList = new ArrayList<>();
    private final ArrayList<Double> valueList = new ArrayList<>();
    private CustomProgressDialog progressDialog;
    private TextView textViewNoItemInfo, beginDateTextView, endDateTextView;
    private View view;
    private DatabaseReference databaseReferenceExpenses;
    private DepoRecyclerExpenses adapter;
    private Activity activity;
    private boolean dateStatus;
    private int radioButtonStatus = 1;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_expenses_depo, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        this.view = view;
        activity = getActivity();
        textViewNoItemInfo = view.findViewById(R.id.textViewNoItemInfo);
        textViewNoItemInfo.setText("Xərc Yoxdur");

        progressDialog = new CustomProgressDialog(activity, getString(R.string.data_loading));

        databaseReferenceExpenses = DatabaseFunctions.getDatabases(getContext()).get(0).child("EXPENSES");

        configureRadioButtons();
        configureListViewDepo();
        configureSearchEditText();
        configureBeginEndTextViews();
        configureRefreshButton();
    }

    private void configureRadioButtons() {
        RadioGroup radioGroupUsers = view.findViewById(R.id.radioGroupBuyReturn);
        radioGroupUsers.setOnCheckedChangeListener((radioGroup, i) -> {
            switch (i) {
                case R.id.radioPercent:
                    radioButtonStatus = 1;
                    isPercent = true;
                    break;

                case R.id.radioOther:
                    radioButtonStatus = 2;
                    isPercent = false;
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

    private void configureSearchEditText() {
        EditText etSearch = view.findViewById(R.id.editTextSearch);
        SharedClass.configureSearchEditText(etSearch, nameList, searchNameList, adapter);
    }

    private void configureListViewDepo() {
        adapter = new DepoRecyclerExpenses(activity, userNameList, nameList, searchNameList, valueList);
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

        String parent_child = radioButtonStatus == 1 ? "Percent" : "Other";

        databaseReferenceExpenses.child(parent_child).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                searchNameList.clear();
                userNameList.clear();
                nameList.clear();
                valueList.clear();
                adapter.notifyDataSetChanged();

                if (!dataSnapshot.exists()) {
                    progressDialog.dismiss();
                    return;
                }

                for (DataSnapshot snapshotUsers : dataSnapshot.getChildren()) {
                    String user_name = snapshotUsers.getKey();

                    DatabaseFunctions.getDatabases(activity).get(0).child("USERS/SELLER/" + user_name + "/ABOUT/name").addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshotName) {
                            String name = snapshotName.getValue(String.class);
                            if (name == null)
                                name = getString(R.string.deleted_user) + " (" + user_name + ")";

                            double sum = 0.0;
                            for (DataSnapshot snapshotDates : snapshotUsers.getChildren()) {
                                if (SharedClass.checkDate(beginDateTextView.getText().toString(),
                                        endDateTextView.getText().toString(),
                                        snapshotDates.getKey())) {
                                    Double value = snapshotDates.child("sum").getValue(Double.class);
                                    if (value != null) sum += value;
                                }
                            }
                            if (sum != 0) {
                                userNameList.add(user_name);
                                searchNameList.add(name);
                                nameList.add(name);
                                valueList.add(SharedClass.twoDigitDecimal(sum));

                                adapter.notifyDataSetChanged();
                                textViewNoItemInfo.setVisibility(View.GONE);
                            }
                            progressDialog.dismiss();
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            progressDialog.dismiss();
                        }
                    });
                }
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