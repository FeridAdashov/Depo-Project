package texel.com.depoproject.Pages.DEPO.Fragments.Expenses;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

import texel.com.depoproject.Pages.DEPO.Activities.ShowExpense.ShowExpenseActivity;
import texel.com.depoproject.R;

public class DepoRecyclerExpenses extends RecyclerView.Adapter<DepoRecyclerExpenses.MyViewHolder> {

    private final ArrayList<String> userNameList;
    private final ArrayList<String> nameList;
    private final ArrayList<String> searchNameList;
    private final ArrayList<Double> percentList;
    private final Activity activity;

    public DepoRecyclerExpenses(Activity activity,
                                ArrayList<String> userNameList,
                                ArrayList<String> nameList,
                                ArrayList<String> searchNameList,
                                ArrayList<Double> valueList) {
        this.activity = activity;
        this.userNameList = userNameList;
        this.nameList = nameList;
        this.searchNameList = searchNameList;
        this.percentList = valueList;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View listItem = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_view_expenses_depo, parent, false);
        return new MyViewHolder(listItem);
    }

    @SuppressLint("DefaultLocale")
    @Override
    public void onBindViewHolder(MyViewHolder holder, final int pos) {
        int position = nameList.indexOf(searchNameList.get(pos));

        String name = nameList.get(position);
        String user_name = userNameList.get(position);
        Double percent = percentList.get(position);

        holder.name.setText(name);
        holder.value.setText(String.format("%.2f", percent));

        holder.cardView.setOnClickListener(v -> {
            AlertDialog.Builder b = new AlertDialog.Builder(activity);
            b.setPositiveButton(R.string.ok, (dialog, which) -> {
                Intent intent = new Intent(activity, ShowExpenseActivity.class);
                intent.putExtra("user_name", user_name);
                intent.putExtra("begin_date", DepoExpensesFragment.begin_date);
                intent.putExtra("end_date", DepoExpensesFragment.end_date);
                intent.putExtra("is_percent", DepoExpensesFragment.isPercent);
                activity.startActivity(intent);
            });
            b.setNegativeButton(R.string.cancel, (dialog, which) -> dialog.dismiss());
            b.setTitle("Bütün xərclərə baxın");
            b.show();
        });
    }

    @Override
    public int getItemCount() {
        return searchNameList.size();
    }

    static class MyViewHolder extends RecyclerView.ViewHolder {
        private final CardView cardView;
        private final TextView name;
        private final TextView value;

        MyViewHolder(View itemView) {
            super(itemView);
            cardView = itemView.findViewById(R.id.cardExpense);
            name = itemView.findViewById(R.id.cardTextViewUserName);
            value = itemView.findViewById(R.id.cardTextViewExpenseValue);
        }
    }
}