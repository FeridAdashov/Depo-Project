package texel.com.depoproject.Pages.DEPO.Fragments.Users;

import android.app.Activity;
import android.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

import texel.com.depoproject.HelperClasses.DatabaseFunctions;
import texel.com.depoproject.R;

public class DepoRecyclerUsers extends RecyclerView.Adapter<DepoRecyclerUsers.MyViewHolder> {

    private final Activity activity;
    private final ArrayList<String> userNameList;
    private final ArrayList<String> nameList;
    private final ArrayList<String> searchNameList;

    public DepoRecyclerUsers(Activity activity,
                             ArrayList<String> userNameList,
                             ArrayList<String> nameList,
                             ArrayList<String> searchNameList) {
        this.activity = activity;
        this.userNameList = userNameList;
        this.nameList = nameList;
        this.searchNameList = searchNameList;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View listItem = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_view_depo_texture_users, parent, false);
        return new MyViewHolder(listItem);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, final int pos) {
        int position = nameList.indexOf(searchNameList.get(pos));

        String user_name = userNameList.get(position);
        String name = nameList.get(position);
        holder.name.setText(name);

        holder.cardView.setOnClickListener(v -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(activity);
            builder.setNeutralButton(R.string.delete, (dialog, which) -> deleteUser(user_name, name));
            builder.setPositiveButton(R.string.cancel, (dialog, which) -> dialog.cancel());
            builder.show();
        });
    }

    private void deleteUser(String user_name, String name) {
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setTitle("\"" + name + "\" hesabı silinsinmi?");
        builder.setNeutralButton(R.string.delete, (dialog, which) -> {
            DatabaseFunctions.getDatabases(activity).get(0).child("USERS/SELLER/" + user_name).removeValue();
            userNameList.remove(user_name);
            nameList.remove(name);
            searchNameList.remove(name);
            notifyDataSetChanged();
        });
        builder.setPositiveButton(R.string.cancel, (dialog, which) -> dialog.cancel());
        builder.show();
    }

    @Override
    public int getItemCount() {
        return searchNameList.size();
    }

    static class MyViewHolder extends RecyclerView.ViewHolder {
        private final CardView cardView;
        private final TextView name;

        MyViewHolder(View itemView) {
            super(itemView);
            cardView = itemView.findViewById(R.id.cardViewTextureUser);
            name = itemView.findViewById(R.id.cardTextViewUserName);
        }
    }
}