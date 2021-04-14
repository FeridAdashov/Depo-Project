package texel.com.depoproject.Pages.DEPO.Fragments.ReturnToDepo;

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
import java.util.Date;

import texel.com.depoproject.CustomDialogs.CustomProgressDialog;
import texel.com.depoproject.HelperClasses.CustomDateTime;
import texel.com.depoproject.HelperClasses.DatabaseFunctions;
import texel.com.depoproject.HelperClasses.SharedClass;
import texel.com.depoproject.R;

public class DepoReturnToDepoFragment extends Fragment implements DatePickerDialog.OnDateSetListener {

    private final ArrayList<String> nameList = new ArrayList<>();
    private final ArrayList<ReturnToDepoModel> returnList = new ArrayList<>();
    private final ArrayList<String> searchNameList = new ArrayList<>();
    private CustomProgressDialog progressDialog;
    private TextView textViewNoItemInfo, beginDateTextView, endDateTextView;
    private View view;
    private DatabaseReference drReturnToDepo;
    private ReturnToDepoRecycler adapter;

    private Activity activity;
    private boolean dateStatus;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_return_to_depo, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        this.view = view;
        activity = getActivity();
        textViewNoItemInfo = view.findViewById(R.id.textViewNoItemInfo);
        textViewNoItemInfo.setText("Satış Yoxdur");

        progressDialog = new CustomProgressDialog(activity, getString(R.string.data_loading));

        drReturnToDepo = DatabaseFunctions.getDatabases(getContext()).get(0).child("RETURN_TO_DEPO");

        configureListViewDepo();
        configureSearchEditText();
        configureBeginEndTextViews();
        configureRefreshButton();
    }

    private void configureRefreshButton() {
        ImageButton refresh = view.findViewById(R.id.imageButtonRefresh);
        refresh.setOnClickListener(v -> {
            loadReturnSellsFromDatabase();
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
        adapter = new ReturnToDepoRecycler(activity, nameList, searchNameList, returnList);
        RecyclerView myView = view.findViewById(R.id.recyclerviewProducts);
        myView.setHasFixedSize(true);
        myView.setAdapter(adapter);
        LinearLayoutManager llm = new LinearLayoutManager(activity);
        llm.setOrientation(LinearLayoutManager.VERTICAL);
        myView.setLayoutManager(llm);

        loadReturnSellsFromDatabase();
    }

    private void loadReturnSellsFromDatabase() {
        progressDialog.show();

        drReturnToDepo.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                nameList.clear();
                searchNameList.clear();
                returnList.clear();

                for (DataSnapshot snapshotReturnDates : dataSnapshot.getChildren()) {
                    String date = snapshotReturnDates.getKey();
                    if (!SharedClass.checkDate(beginDateTextView.getText().toString(),
                            endDateTextView.getText().toString(),
                            date)) continue;

                    for (DataSnapshot snapshotUser : snapshotReturnDates.getChildren()) {
                        String seller = snapshotUser.child("seller").getValue(String.class);

                        Double pureSum = snapshotUser.child("pureSum").getValue(Double.class);
                        Double rottenSum = snapshotUser.child("rottenSum").getValue(Double.class);
                        if(pureSum == null) pureSum = 0.0;
                        if(rottenSum == null) rottenSum = 0.0;

                        String key = seller + "    " + date;

                        int index = 0;
                        for (String s : nameList)
                            if (s.compareTo(key) > 0) index++;

                        nameList.add(index, key);
                        returnList.add(index, new ReturnToDepoModel(
                                date,
                                snapshotUser.getKey(),
                                seller,
                                pureSum,
                                rottenSum));
                    }
                }
                if (nameList.size() > 0) {
                    searchNameList.addAll(nameList);
                    textViewNoItemInfo.setVisibility(View.GONE);
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