package texel.com.depoproject.Pages.DEPO.Activities.ShowExpense;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

import texel.com.depoproject.R;

public class RecyclerShowExpense extends RecyclerView.Adapter<RecyclerShowExpense.MyViewHolder> {

    private final ArrayList<String> dateTimeList;
    private final ArrayList<String> nameList;
    private final ArrayList<String> searchDateTimeList;
    private final ArrayList<Double> percentList;

    public RecyclerShowExpense(ArrayList<String> dateTimeList,
                               ArrayList<String> searchDateTimeList,
                               ArrayList<String> nameList,
                               ArrayList<Double> percentList) {
        this.dateTimeList = dateTimeList;
        this.nameList = nameList;
        this.searchDateTimeList = searchDateTimeList;
        this.percentList = percentList;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View listItem = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_view_expenses_seller, parent, false);
        return new MyViewHolder(listItem);
    }

    @SuppressLint("DefaultLocale")
    @Override
    public void onBindViewHolder(MyViewHolder holder, final int pos) {
        int position = dateTimeList.indexOf(searchDateTimeList.get(pos));

        String date_time = dateTimeList.get(position);
        String name = nameList.get(position);
        Double percent = percentList.get(position);

        holder.date_time.setText(date_time);
        holder.name.setText(name);
        holder.percent.setText(String.format("%.2f", percent));
    }

    @Override
    public int getItemCount() {
        return searchDateTimeList.size();
    }

    static class MyViewHolder extends RecyclerView.ViewHolder {
        private final TextView date_time;
        private final TextView name;
        private final TextView percent;

        MyViewHolder(View itemView) {
            super(itemView);
            date_time = itemView.findViewById(R.id.cardTextViewExpenseDate);
            name = itemView.findViewById(R.id.cardTextViewExpenseName);
            percent = itemView.findViewById(R.id.cardTextViewExpenseValue);
        }
    }
}