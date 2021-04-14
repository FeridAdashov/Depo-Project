package texel.com.depoproject.Pages.DEPO.Fragments.Users;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
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

public class DepoUsersFragment extends Fragment {

    private final ArrayList<String> nameList = new ArrayList<>();
    private final ArrayList<String> searchNameList = new ArrayList<>();
    private final ArrayList<String> userNameList = new ArrayList<>();
    private CustomProgressDialog progressDialog;
    private TextView textViewNoItemInfo;
    private View view;
    private DatabaseReference databaseReferenceUsers;
    private DepoRecyclerUsers adapter;

    private Activity activity;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_depo_texture_users, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        this.view = view;
        activity = getActivity();
        textViewNoItemInfo = view.findViewById(R.id.textViewNoItemInfo);
        textViewNoItemInfo.setText("Qeydiyyatda Olan Ekspertr Yoxdur");

        progressDialog = new CustomProgressDialog(activity, getString(R.string.data_loading));
        progressDialog.show();

        databaseReferenceUsers = DatabaseFunctions.getDatabases(getContext()).get(0).child("USERS/SELLER");

        configureListViewDepo();
        configureSearchEditText();
    }

    private void configureSearchEditText() {
        EditText etSearch = view.findViewById(R.id.editTextSearch);
        SharedClass.configureSearchEditText(etSearch, nameList, searchNameList, adapter);
    }

    private void configureListViewDepo() {
        adapter = new DepoRecyclerUsers(activity, userNameList, nameList, searchNameList);
        RecyclerView myView = view.findViewById(R.id.recyclerviewUsers);
        myView.setHasFixedSize(true);
        myView.setAdapter(adapter);
        LinearLayoutManager llm = new LinearLayoutManager(activity);
        llm.setOrientation(LinearLayoutManager.VERTICAL);
        myView.setLayoutManager(llm);

        loadUsersFromDatabase();
    }

    private void loadUsersFromDatabase() {
        databaseReferenceUsers.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                userNameList.clear();
                nameList.clear();
                searchNameList.clear();

                for (DataSnapshot snapshotUser : dataSnapshot.getChildren()) {
                    String user_name = snapshotUser.getKey();
                    userNameList.add(user_name);

                    String _name = snapshotUser.child("ABOUT/name").getValue(String.class);
                    nameList.add(_name);
                }
                if (userNameList.size() > 0) textViewNoItemInfo.setVisibility(View.GONE);
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