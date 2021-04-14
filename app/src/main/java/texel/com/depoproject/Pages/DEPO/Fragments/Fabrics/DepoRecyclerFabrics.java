package texel.com.depoproject.Pages.DEPO.Fragments.Fabrics;

import android.app.Activity;
import android.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

import texel.com.depoproject.HelperClasses.DatabaseFunctions;
import texel.com.depoproject.R;

public class DepoRecyclerFabrics extends RecyclerView.Adapter<DepoRecyclerFabrics.MyViewHolder> {

    private final ArrayList<String> nameList;
    private final ArrayList<String> searchNameList;
    private final Activity activity;

    public DepoRecyclerFabrics(Activity activity,
                               ArrayList<String> nameList,
                               ArrayList<String> searchNameList) {
        this.nameList = nameList;
        this.searchNameList = searchNameList;
        this.activity = activity;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View listItem = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_view_depo_fabric, parent, false);
        return new MyViewHolder(listItem);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, final int pos) {
        int position = nameList.indexOf(searchNameList.get(pos));

        String name = nameList.get(position);
        holder.name.setText(name);
        holder.buttonDelete.setOnClickListener(v -> deleteFabric(name));
    }

    private void deleteFabric(String name) {
        AlertDialog.Builder b = new AlertDialog.Builder(activity);
        b.setPositiveButton(R.string.delete, (dialog, which) -> {
            nameList.remove(name);
            searchNameList.remove(name);
            notifyDataSetChanged();

            DatabaseFunctions.getDatabases(activity).get(0).child("FABRICS").child(name).removeValue();
        });
        b.setNegativeButton(R.string.cancel, (dialog, which) -> dialog.dismiss());
        b.setTitle(name);
        b.setMessage("Bu Zavod Silinsinmi?");
        b.show();
    }

    @Override
    public int getItemCount() {
        return searchNameList.size();
    }

    static class MyViewHolder extends RecyclerView.ViewHolder {
        private final TextView name;
        private final ImageButton buttonDelete;

        MyViewHolder(View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.cardTextViewFabricName);
            buttonDelete = itemView.findViewById(R.id.cardButtonDeleteFabric);
        }
    }
}