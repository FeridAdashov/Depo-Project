package texel.com.depoproject.Pages.DEPO.Fragments.Debts;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

import texel.com.depoproject.Pages.DEPO.Fragments.Debts.ShowDebt.ShowDebtActivity;
import texel.com.depoproject.R;

public class DepoRecyclerDebts extends RecyclerView.Adapter<DepoRecyclerDebts.MyViewHolder> {

    private final Activity activity;
    private final ArrayList<String> nameList;
    private final ArrayList<String> searchNameList;
    private final ArrayList<Double> debtList;

    public DepoRecyclerDebts(Activity activity,
                             ArrayList<String> nameList,
                             ArrayList<String> searchNameList,
                             ArrayList<Double> debtList) {
        this.activity = activity;
        this.nameList = nameList;
        this.searchNameList = searchNameList;
        this.debtList = debtList;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View listItem = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_view_depo_debts, parent, false);
        return new MyViewHolder(listItem);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, final int pos) {
        int position = nameList.indexOf(searchNameList.get(pos));

        String name = nameList.get(position);
        Double debt = debtList.get(position);

        holder.name.setText(name);
        holder.debt.setText(String.valueOf(debt));

        if (debt < 0)
            holder.debt.setTextColor(Color.parseColor(DepoDebtsFragment.isFabric ? "#3DDC84" : "#FF5722"));
        else
            holder.debt.setTextColor(Color.parseColor(DepoDebtsFragment.isFabric ? "#FF5722" : "#3DDC84"));

        holder.cardView.setOnClickListener(v -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(activity);
            builder.setMessage("Borc tarixçəsinə bax");
            builder.setPositiveButton(R.string.ok, (dialog, which) -> openDebtsHistoryActivity(name));
            builder.setNegativeButton(R.string.cancel, (dialog, which) -> dialog.cancel());
            builder.show();
        });
    }

    private void openDebtsHistoryActivity(String name) {
        Intent intent = new Intent(activity, ShowDebtActivity.class);
        intent.putExtra("isFabric", DepoDebtsFragment.isFabric);
        intent.putExtra("name", name);
        activity.startActivity(intent);
    }

    @Override
    public int getItemCount() {
        return searchNameList.size();
    }

    static class MyViewHolder extends RecyclerView.ViewHolder {
        private final CardView cardView;
        private final TextView name;
        private final TextView debt;

        MyViewHolder(View itemView) {
            super(itemView);
            cardView = itemView.findViewById(R.id.cardViewDebt);
            name = itemView.findViewById(R.id.cardTextViewDebtName);
            debt = itemView.findViewById(R.id.cardTextViewDebt);
        }
    }
}