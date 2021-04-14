package texel.com.depoproject.Pages.DEPO.Activities.ShowExpense;

import android.app.Activity;
import android.os.Bundle;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;

import texel.com.depoproject.CustomDialogs.CustomProgressDialog;
import texel.com.depoproject.HelperClasses.DatabaseFunctions;
import texel.com.depoproject.HelperClasses.SharedClass;
import texel.com.depoproject.R;

public class ShowExpenseActivity extends AppCompatActivity {

    private final ArrayList<String> dateTimeList = new ArrayList<>();
    private final ArrayList<String> searchDateTimeList = new ArrayList<>();
    private final ArrayList<String> nameList = new ArrayList<>();
    private final ArrayList<Double> valueList = new ArrayList<>();
    private CustomProgressDialog progressDialog;
    private DatabaseReference databaseReferenceExpenses;
    private RecyclerShowExpense adapter;

    private Activity activity;
    private String begin_date;
    private String end_date;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_show_expense);

        activity = this;

        progressDialog = new CustomProgressDialog(activity, getString(R.string.data_loading));

        String user_name = getIntent().getStringExtra("user_name");
        boolean is_percent = getIntent().getBooleanExtra("is_percent", true);
        begin_date = getIntent().getStringExtra("begin_date");
        end_date = getIntent().getStringExtra("end_date");

        String expense_type = is_percent ? "Percent" : "Other";
        databaseReferenceExpenses = DatabaseFunctions.getDatabases(this).get(0).child("EXPENSES/" + expense_type + "/" + user_name);

        configureListViewDepo();
        configureSearchEditText();
    }

    private void configureSearchEditText() {
        EditText etSearch = findViewById(R.id.editTextSearch);
        SharedClass.configureSearchEditText(etSearch, dateTimeList, searchDateTimeList, adapter);
    }

    private void configureListViewDepo() {
        adapter = new RecyclerShowExpense(dateTimeList, searchDateTimeList, nameList, valueList);
        RecyclerView myView = findViewById(R.id.recyclerviewProducts);
        myView.setHasFixedSize(true);
        myView.setAdapter(adapter);
        LinearLayoutManager llm = new LinearLayoutManager(activity);
        llm.setOrientation(LinearLayoutManager.VERTICAL);
        myView.setLayoutManager(llm);

        loadExpensesFromDatabase();
    }

    private void loadExpensesFromDatabase() {
        progressDialog.show();

        databaseReferenceExpenses.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                dateTimeList.clear();
                searchDateTimeList.clear();
                nameList.clear();
                valueList.clear();

                for (DataSnapshot snapshotDates : dataSnapshot.getChildren()) {
                    String date = snapshotDates.getKey();

                    if (SharedClass.checkDate(begin_date, end_date, date))
                        for (DataSnapshot snapshotTimes : snapshotDates.getChildren()) {
                            String time = snapshotTimes.getKey();
                            if (time == null || time.equals("sum") || time.equals("netIncome") || time.equals("percentSum"))
                                continue;

                            String name = date + "     " + snapshotTimes.getKey();

                            int index = 0;
                            for (String s : dateTimeList)
                                if (s.compareTo(name) > 0) index++;

                            dateTimeList.add(index, name);
                            nameList.add(index, snapshotTimes.child("name").getValue(String.class));
                            valueList.add(index, snapshotTimes.child("value").getValue(Double.class));
                        }
                }
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
}