package texel.com.depoproject.Pages.DEPO.Fragments.Fabrics;

import android.app.Activity;
import android.app.AlertDialog;
import android.os.Bundle;
import android.text.TextUtils;
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

public class DepoFabricsFragment extends Fragment {

    private final ArrayList<String> nameList = new ArrayList<>();
    private final ArrayList<String> searchNameList = new ArrayList<>();
    private CustomProgressDialog progressDialog;
    private TextView textViewNoItemInfo;
    private View view;
    private DatabaseReference databaseReferenceFabrics;
    private DepoRecyclerFabrics adapter;

    private Activity activity;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_markets_depo, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        this.view = view;
        activity = getActivity();
        textViewNoItemInfo = view.findViewById(R.id.textViewNoItemInfo);
        textViewNoItemInfo.setText("Qeydiyyatda Olan Zavod Yoxdur");

        progressDialog = new CustomProgressDialog(activity, getString(R.string.data_loading));
        progressDialog.show();

        databaseReferenceFabrics = DatabaseFunctions.getDatabases(getContext()).get(0).child("FABRICS");

        configureListViewDepo();
        configureSearchEditText();

        ImageButton addMarket = view.findViewById(R.id.addMarket);
        addMarket.setOnClickListener(v -> addNewFabric());
    }

    private void addNewFabric() {
        final View view = getLayoutInflater().inflate(R.layout.new_fabric_dialog_view, null);
        final EditText name = view.findViewById(R.id.cardTextViewFabricName);

        AlertDialog.Builder b = new AlertDialog.Builder(getContext());
        b.setPositiveButton(R.string.save, (dialog, which) -> {
        });
        b.setNegativeButton(R.string.cancel, (dialog, which) -> dialog.dismiss());
        b.setView(view);

        final AlertDialog dialog = b.create();
        dialog.show();
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {

            String n = name.getText().toString().trim();

            if (TextUtils.isEmpty(n))
                SharedClass.showSnackBar(activity, "Yanlış dəyər daxil etdiniz!");

            else {
                saveFabric(n);
                dialog.dismiss();
            }
        });
    }

    private void saveFabric(String name) {
        nameList.add(name);
        searchNameList.add(name);
        adapter.notifyDataSetChanged();

        databaseReferenceFabrics.child(name).setValue(name);
        textViewNoItemInfo.setVisibility(View.GONE);
    }

    private void configureSearchEditText() {
        EditText etSearch = view.findViewById(R.id.editTextSearch);
        SharedClass.configureSearchEditText(etSearch, nameList, searchNameList, adapter);
    }

    private void configureListViewDepo() {
        adapter = new DepoRecyclerFabrics(activity, nameList, searchNameList);
        RecyclerView myView = view.findViewById(R.id.recyclerviewMarkets);
        myView.setHasFixedSize(true);
        myView.setAdapter(adapter);
        LinearLayoutManager llm = new LinearLayoutManager(activity);
        llm.setOrientation(LinearLayoutManager.VERTICAL);
        myView.setLayoutManager(llm);

        loadMarketsFromDatabase();
    }

    private void loadMarketsFromDatabase() {
        databaseReferenceFabrics.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                nameList.clear();
                searchNameList.clear();

                for (DataSnapshot snapshotTaxis : dataSnapshot.getChildren()) {
                    String name = snapshotTaxis.getKey();
                    nameList.add(name);
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