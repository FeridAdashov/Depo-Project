package texel.com.depoproject.Pages.DEPO.Activities.ShowReturnToDepo;

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

public class ShowReturnRecycler extends RecyclerView.Adapter<ShowReturnRecycler.MyViewHolder> {

    private final ArrayList<String> nameList;
    private final HashMap<String, Product> hashMap;

    public ShowReturnRecycler(ArrayList<String> nameList,
                              HashMap<String, Product> hashMap) {
        this.nameList = nameList;
        this.hashMap = hashMap;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View listItem = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_view_return_to_depo_basket, parent, false);
        return new MyViewHolder(listItem);
    }

    @SuppressLint("DefaultLocale")
    @Override
    public void onBindViewHolder(MyViewHolder holder, final int position) {
        String name = nameList.get(position);

        Product product = hashMap.get(name);
        if (product == null) return;

        holder.name.setText(product.name);
        holder.price.setText(String.valueOf(product.sellPrice));
        holder.amountPure.setText(String.valueOf(product.wantedAmount));
        holder.amountRotten.setText(String.valueOf(product.wantedAmountRotten));
        holder.commonPricePure.setText(String.format("%.2f", product.wantedAmount * product.sellPrice));
        holder.commonPriceRotten.setText(String.format("%.2f", product.wantedAmountRotten * product.sellPrice));
    }

    @Override
    public int getItemCount() {
        return nameList.size();
    }

    static class MyViewHolder extends RecyclerView.ViewHolder {
        private final TextView name;
        private final TextView price;
        private final TextView amountPure, amountRotten;
        private final TextView commonPricePure, commonPriceRotten;

        MyViewHolder(View itemView) {
            super(itemView);
            name = itemView.findViewById(R.id.cardTextViewProductName);
            price = itemView.findViewById(R.id.cardTextViewProductPrice);
            amountPure = itemView.findViewById(R.id.cardTextViewProductAmountPure);
            amountRotten = itemView.findViewById(R.id.cardTextViewProductAmountRotten);
            commonPricePure = itemView.findViewById(R.id.cardTextViewProductCommonPricePure);
            commonPriceRotten = itemView.findViewById(R.id.cardTextViewProductCommonPriceRotten);
        }
    }
}