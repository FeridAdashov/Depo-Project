package texel.com.depoproject.Pages.DEPO.Fragments.Debts.ShowDebt;

import android.annotation.SuppressLint;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

import texel.com.depoproject.R;

public class RecyclerShowDebt extends RecyclerView.Adapter<RecyclerShowDebt.MyViewHolder> {

    private final ArrayList<String> nameList;
    private final ArrayList<String> searchNameList;
    private final ArrayList<Double> debtList;

    public RecyclerShowDebt(
            ArrayList<String> nameList,
            ArrayList<String> searchNameList,
            ArrayList<Double> debtList) {
        this.nameList = nameList;
        this.searchNameList = searchNameList;
        this.debtList = debtList;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View listItem = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_view_depo_show_debt, parent, false);
        return new MyViewHolder(listItem);
    }

    @SuppressLint("DefaultLocale")
    @Override
    public void onBindViewHolder(MyViewHolder holder, final int pos) {
        int position = nameList.indexOf(searchNameList.get(pos));

        String name = nameList.get(position);
        Double debt = debtList.get(position);

        holder.name.setText(name);
        holder.debt.setText(String.format("%.2f", debt));

        if (debt < 0)
            holder.debt.setTextColor(Color.parseColor(ShowDebtActivity.isFabric ? "#3DDC84" : "#FF5722"));
        else
            holder.debt.setTextColor(Color.parseColor(ShowDebtActivity.isFabric ? "#FF5722" : "#3DDC84"));
    }

    @Override
    public int getItemCount() {
        return searchNameList.size();
    }

    static class MyViewHolder extends RecyclerView.ViewHolder {
        private final TextView name;
        private final TextView debt;

        MyViewHolder(View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.cardTextViewDebtName);
            debt = itemView.findViewById(R.id.cardTextViewDebt);
        }
    }
}