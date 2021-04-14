package texel.com.depoproject.Pages.DEPO.Fragments.Return.Buys.ShowBuy;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.HashMap;

import texel.com.depoproject.R;
import texel.com.depoproject.SqlLiteDatabase.Product;

public class RecyclerShowReturnBuy extends RecyclerView.Adapter<RecyclerShowReturnBuy.MyViewHolder> {

    private final ArrayList<String> searchNameList;
    private final  HashMap<String, Product> cardViewProductHashMap;

    public RecyclerShowReturnBuy(
                                 ArrayList<String> searchNameList,
                                 HashMap<String, Product> cardViewProductHashMap
    ) {
        this.searchNameList = searchNameList;
        this.cardViewProductHashMap = cardViewProductHashMap;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View listItem = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_view_product_basket, parent, false);
        return new MyViewHolder(listItem);
    }

    @SuppressLint("DefaultLocale")
    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, final int position) {
        String name = searchNameList.get(position);
        Product product = cardViewProductHashMap.get(name);

        if(product == null) return;

        holder.name.setText(name);
        holder.price.setText(String.format("%.2f", product.buyPrice));
        holder.amount.setText(String.format("%.2f", product.wantedAmount));
        holder.commonPrice.setText(String.format("%.2f", product.buyPrice * product.wantedAmount));
    }

    @Override
    public int getItemCount() {
        return searchNameList.size();
    }

    static class MyViewHolder extends RecyclerView.ViewHolder {
        private final TextView name;
        private final TextView price;
        private final TextView amount;
        private final TextView commonPrice;

        MyViewHolder(View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.cardTextViewProductName);
            price = itemView.findViewById(R.id.cardTextViewProductPrice);
            amount = itemView.findViewById(R.id.cardTextViewProductAmount);
            commonPrice = itemView.findViewById(R.id.cardTextViewProductCommonPrice);
        }
    }
}