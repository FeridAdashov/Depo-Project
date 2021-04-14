package texel.com.depoproject.Pages.DEPO.Fragments.Profit;

import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

import texel.com.depoproject.R;

public class DepoProfitRecycler extends RecyclerView.Adapter<DepoProfitRecycler.MyViewHolder> {

    private final ArrayList<String> dateList;
    private final ArrayList<String> searchDateList;
    private final ArrayList<Double> profitList;
    private final ArrayList<Double> incomeList;
    private final Activity activity;

    public DepoProfitRecycler(Activity activity,
                              ArrayList<String> dateList,
                              ArrayList<String> searchDateList,
                              ArrayList<Double> profitList,
                              ArrayList<Double> incomeList) {
        this.activity = activity;
        this.dateList = dateList;
        this.searchDateList = searchDateList;
        this.profitList = profitList;
        this.incomeList = incomeList;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View listItem = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_view_profit_depo, parent, false);
        return new MyViewHolder(listItem);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, final int pos) {
        int position = dateList.indexOf(searchDateList.get(pos));

        String date = dateList.get(position);

        holder.date.setText(date);
        holder.profit.setText(String.valueOf(profitList.get(position)));
        holder.netIncome.setText(String.valueOf(incomeList.get(position)));
    }

    @Override
    public int getItemCount() {
        return searchDateList.size();
    }

    static class MyViewHolder extends RecyclerView.ViewHolder {
        private final TextView date;
        private final TextView profit;
        private final TextView netIncome;

        MyViewHolder(View itemView) {
            super(itemView);
            date = itemView.findViewById(R.id.cardTextViewDate);
            profit = itemView.findViewById(R.id.cardTextViewProfit);
            netIncome = itemView.findViewById(R.id.cardTextViewNetIncome);
        }
    }
}