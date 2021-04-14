package texel.com.depoproject.Pages.DEPO.Fragments.Buys.ShowBuy;

import android.annotation.SuppressLint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.HashMap;

import texel.com.depoproject.R;
import texel.com.depoproject.SqlLiteDatabase.Product;

public class RecyclerShowBuy extends RecyclerView.Adapter<RecyclerShowBuy.MyViewHolder> {
    private final ArrayList<String> nameList;
    private final HashMap<String, Product> hashMap;

    public RecyclerShowBuy(
            ArrayList<String> nameList,
            HashMap<String, Product> hashMap) {
        this.nameList = nameList;
        this.hashMap = hashMap;
    }

    @Override
    public RecyclerShowBuy.MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View listItem = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_view_product_basket, parent, false);
        return new RecyclerShowBuy.MyViewHolder(listItem);
    }

    @SuppressLint("DefaultLocale")
    @Override
    public void onBindViewHolder(RecyclerShowBuy.MyViewHolder holder, final int position) {
        String name = nameList.get(position);

        Product product = hashMap.get(name);
        if (product == null) return;

        holder.name.setText(product.name);
        holder.price.setText(String.valueOf(product.buyPrice));
        holder.amount.setText(String.valueOf(product.wantedAmount));
        holder.commonPrice.setText(String.format("%.2f", product.wantedAmount * product.buyPrice));
    }

    @Override
    public int getItemCount() {
        return nameList.size();
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