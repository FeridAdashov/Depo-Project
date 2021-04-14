package texel.com.depoproject.Pages.DEPO.Fragments.Debts.ShowDebt;

import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

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

public class ShowDebtActivity extends AppCompatActivity {

    private final ArrayList<String> nameList = new ArrayList<>();
    private final ArrayList<String> searchNameList = new ArrayList<>();
    private final ArrayList<Double> debtList = new ArrayList<>();

    private CustomProgressDialog progressDialog;
    private TextView textViewNoItemInfo;
    private DatabaseReference databaseReferenceDebt;
    private RecyclerShowDebt adapter;

    public static boolean isFabric = true;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.fragment_depo_texture_users);

        String name = getIntent().getStringExtra("name");
        isFabric = getIntent().getBooleanExtra("isFabric", true);

        textViewNoItemInfo = findViewById(R.id.textViewNoItemInfo);
        textViewNoItemInfo.setText("Qeyd Yoxdur");

        progressDialog = new CustomProgressDialog(this, getString(R.string.data_loading));
        progressDialog.show();

        String child = isFabric ? "FABRIC" : "MARKET";
        databaseReferenceDebt = DatabaseFunctions.getDatabases(this).get(0).child("DEBTS/" + child + "/" + name);

        configureListViewDepo();
        configureSearchEditText();
    }

    private void configureSearchEditText() {
        EditText etSearch = findViewById(R.id.editTextSearch);
        SharedClass.configureSearchEditText(etSearch, nameList, searchNameList, adapter);
    }

    private void configureListViewDepo() {
        adapter = new RecyclerShowDebt(nameList, searchNameList, debtList);
        RecyclerView myView = findViewById(R.id.recyclerviewUsers);
        myView.setHasFixedSize(true);
        myView.setAdapter(adapter);
        LinearLayoutManager llm = new LinearLayoutManager(this);
        llm.setOrientation(LinearLayoutManager.VERTICAL);
        myView.setLayoutManager(llm);

        loadProductsFromDatabase();
    }

    private void loadProductsFromDatabase() {
        databaseReferenceDebt.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                nameList.clear();
                searchNameList.clear();
                debtList.clear();

                for (DataSnapshot snapshotDates : dataSnapshot.getChildren())
                    for (DataSnapshot snapshotTimes : snapshotDates.getChildren()) {
                        Double debt = snapshotTimes.getValue(Double.class);

                        nameList.add(snapshotDates.getKey() + "  -  " + snapshotTimes.getKey());
                        debtList.add(debt);
                    }
                if (nameList.size() > 0) textViewNoItemInfo.setVisibility(View.GONE);
                else textViewNoItemInfo.setVisibility(View.VISIBLE);

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
}