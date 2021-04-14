package texel.com.depoproject.Pages.DEPO.Fragments.Debts;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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

import texel.com.depoproject.CustomDialogs.CustomProgressDialog;
import texel.com.depoproject.HelperClasses.DatabaseFunctions;
import texel.com.depoproject.HelperClasses.SharedClass;
import texel.com.depoproject.R;

public class DepoDebtsFragment extends Fragment {

    public static boolean isFabric;
    private final ArrayList<String> nameList = new ArrayList<>();
    private final ArrayList<String> searchNameList = new ArrayList<>();
    private final ArrayList<Double> debtList = new ArrayList<>();
    private final String pageName;
    private CustomProgressDialog progressDialog;
    private TextView textViewNoItemInfo;
    private View view;
    private DatabaseReference databaseReferenceDebts;
    private DepoRecyclerDebts adapter;
    private Activity activity;

    public DepoDebtsFragment(boolean isFabric) {
        DepoDebtsFragment.isFabric = isFabric;
        this.pageName = isFabric ? "FABRIC" : "MARKET";
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_depo_debts, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        this.view = view;
        activity = getActivity();
        textViewNoItemInfo = view.findViewById(R.id.textViewNoItemInfo);
        textViewNoItemInfo.setText("Borc Yoxdur");

        progressDialog = new CustomProgressDialog(activity, getString(R.string.data_loading));

        databaseReferenceDebts = DatabaseFunctions.getDatabases(getContext()).get(0).child("DEBTS/" + pageName);

        configureListViewDepo();
        configureSearchEditText();
        configureRefreshButton();
    }

    private void configureRefreshButton() {
        ImageButton refresh = view.findViewById(R.id.imageButtonRefresh);
        refresh.setOnClickListener(v -> loadDebtsFromDatabase());
    }

    private void configureSearchEditText() {
        EditText etSearch = view.findViewById(R.id.editTextSearch);
        SharedClass.configureSearchEditText(etSearch, nameList, searchNameList, adapter);
    }

    private void configureListViewDepo() {
        adapter = new DepoRecyclerDebts(activity, nameList, searchNameList, debtList);
        RecyclerView myView = view.findViewById(R.id.recyclerviewUsers);
        myView.setHasFixedSize(true);
        myView.setAdapter(adapter);
        LinearLayoutManager llm = new LinearLayoutManager(activity);
        llm.setOrientation(LinearLayoutManager.VERTICAL);
        myView.setLayoutManager(llm);

        loadDebtsFromDatabase();
    }

    private void loadDebtsFromDatabase() {
        progressDialog.show();
        databaseReferenceDebts.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                nameList.clear();
                searchNameList.clear();
                debtList.clear();

                for (DataSnapshot snapshotName : dataSnapshot.getChildren()) {
                    String name = snapshotName.getKey();
                    nameList.add(name);
                    debtList.add(snapshotName.child("sum").getValue(Double.class));
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