package texel.com.depoproject.Pages.DEPO.Fragments.Texture.History;

import android.app.Activity;
import android.app.DatePickerDialog;
import android.os.Bundle;
import android.view.View;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
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

public class TextureHistoryActivity extends AppCompatActivity implements DatePickerDialog.OnDateSetListener {

    public static String texture_child_name;
    private final ArrayList<String> nameList = new ArrayList<>();
    private final ArrayList<Double> sumList = new ArrayList<>();
    private final ArrayList<String> searchNameList = new ArrayList<>();
    private CustomProgressDialog progressDialog;
    private TextView textViewNoItemInfo, beginDateTextView, endDateTextView, textViewCommonSum;
    private DatabaseReference databaseReferenceTexture;
    private DepoTextureHistoryRecycler adapter;
    private Activity activity;
    private boolean dateStatus;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_texture_history);

        texture_child_name = getIntent().getStringExtra("texture_child_name");

        activity = this;

        textViewCommonSum = findViewById(R.id.textViewCommonSum);
        textViewNoItemInfo = findViewById(R.id.textViewNoItemInfo);
        textViewNoItemInfo.setText("Faktura Yoxdur");

        Toolbar toolbar = findViewById(R.id.toolbarDepo);
        TextView title = toolbar.findViewById(R.id.depoToolbarTitle);
        title.setText(getResources().getString(R.string.history));
        toolbar.setNavigationOnClickListener(v -> finish());

        progressDialog = new CustomProgressDialog(activity, getString(R.string.data_loading));

        databaseReferenceTexture = DatabaseFunctions.getDatabases(activity).get(0).child("TEXTURES");

        configureListViewDepo();
        configureSearchEditText();
        configureBeginEndTextViews();
        configureRefreshButton();
    }

    private void configureRefreshButton() {
        ImageButton refresh = findViewById(R.id.imageButtonRefresh);
        refresh.setOnClickListener(v -> {
            loadSellsFromDatabase();
        });
    }

    private void configureBeginEndTextViews() {
        beginDateTextView = findViewById(R.id.textViewBeginDate);
        endDateTextView = findViewById(R.id.textViewEndDate);

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
        EditText etSearch = findViewById(R.id.editTextSearch);
        SharedClass.configureSearchEditText(etSearch, nameList, searchNameList, adapter);
    }

    private void configureListViewDepo() {
        adapter = new DepoTextureHistoryRecycler(activity, searchNameList, sumList);
        RecyclerView myView = findViewById(R.id.recyclerviewProducts);
        myView.setHasFixedSize(true);
        myView.setAdapter(adapter);
        LinearLayoutManager llm = new LinearLayoutManager(activity);
        llm.setOrientation(LinearLayoutManager.VERTICAL);
        myView.setLayoutManager(llm);

        loadSellsFromDatabase();
    }

    private void loadSellsFromDatabase() {
        progressDialog.show();

        databaseReferenceTexture.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                nameList.clear();
                searchNameList.clear();
                sumList.clear();

                double commonSum = 0.0;
                for (DataSnapshot snapshotSellDates : dataSnapshot.getChildren()) {
                    String date = snapshotSellDates.getKey();
                    if (!SharedClass.checkDate(
                            beginDateTextView.getText().toString(),
                            endDateTextView.getText().toString(),
                            date)) continue;

                    Double sum = snapshotSellDates.child(texture_child_name + "/sum").getValue(Double.class);
                    if (sum != null) {
                        int index = 0;
                        for (String s : nameList)
                            if (s.compareTo(date) > 0) index++;

                        nameList.add(index, date);
                        sumList.add(index, SharedClass.twoDigitDecimal(sum));
                        commonSum += sum;
                    }
                }
                if (nameList.size() > 0) textViewNoItemInfo.setVisibility(View.GONE);
                else textViewNoItemInfo.setVisibility(View.VISIBLE);

                progressDialog.dismiss();
                searchNameList.addAll(nameList);
                adapter.notifyDataSetChanged();

                textViewCommonSum.setText("Cəm: " + SharedClass.twoDigitDecimal(commonSum));
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

    @Override
    public boolean onSupportNavigateUp() {
        onBackPressed();
        return true;
    }
}