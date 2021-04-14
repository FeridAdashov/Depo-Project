package texel.com.depoproject.Pages.SELLER.Fragments.ReturnToDepo.Basket;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.HashMap;

import texel.com.depoproject.HelperClasses.SharedClass;
import texel.com.depoproject.R;
import texel.com.depoproject.SqlLiteDatabase.DatabaseHelper;
import texel.com.depoproject.SqlLiteDatabase.Product;
import texel.com.depoproject.SqlLiteDatabase.TableInfo;

public class SellerBasketProductsRecycler extends RecyclerView.Adapter<SellerBasketProductsRecycler.MyViewHolder> {

    private final ArrayList<String> nameList;
    private final ArrayList<String> searchNameList;
    private final HashMap<String, Product> hashMap;
    private final Activity activity;
    private final TextView textViewResultPure, textViewResultRotten;

    public SellerBasketProductsRecycler(Activity activity,
                                        ArrayList<String> nameList,
                                        ArrayList<String> searchNameList,
                                        HashMap<String, Product> hashMap,
                                        TextView textViewResultPure,
                                        TextView textViewResultRotten) {
        this.activity = activity;
        this.nameList = nameList;
        this.searchNameList = searchNameList;
        this.hashMap = hashMap;
        this.textViewResultPure = textViewResultPure;
        this.textViewResultRotten = textViewResultRotten;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View listItem = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_view_return_to_depo_basket, parent, false);
        return new MyViewHolder(listItem);
    }

    @SuppressLint("DefaultLocale")
    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, final int pos) {
        int position = nameList.indexOf(searchNameList.get(pos));

        String name = nameList.get(position);

        Product product = hashMap.get(name);
        if (product == null) return;

        holder.name.setText(product.name);
        holder.price.setText(String.valueOf(product.sellPrice));
        holder.amountPure.setText(String.valueOf(product.wantedAmount));
        holder.amountRotten.setText(String.valueOf(product.wantedAmountRotten));
        holder.commonPricePure.setText(String.format("%.2f", product.wantedAmount * product.sellPrice));
        holder.commonPriceRotten.setText(String.format("%.2f", product.wantedAmountRotten * product.sellPrice));

        holder.price.setOnClickListener(v -> changeValuePrice(name, holder, product));
        holder.amountPure.setOnClickListener(v -> changeValueAmount(name, holder, product, true));
        holder.amountRotten.setOnClickListener(v -> changeValueAmount(name, holder, product, false));
    }

    private void changeValuePrice(String name, MyViewHolder holder, Product product) {
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        final EditText input = new EditText(activity);
        input.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);

        input.setHint("Yeni Qiymət");
        builder.setView(input);

        builder.setPositiveButton("OK", (dialog, which) -> {
            try {
                DatabaseHelper db = new DatabaseHelper(activity, DatabaseHelper.DATABASE_SELLER_PURE_ROTTEN);
                Double value = Double.parseDouble(input.getText().toString());

                holder.price.setText(String.valueOf(value));
                holder.commonPricePure.setText(String.valueOf(value * product.wantedAmount));
                holder.commonPriceRotten.setText(String.valueOf(value * product.wantedAmountRotten));

                product.sellPrice = value;

                db.changeColumnValue(name, TableInfo.ProductEntry.COLUMN_SELL_PRICE, value);

                double resultPure = 0.0, resultRotten = 0.0;
                for (Product p : db.getProductList()) {
                    resultPure += p.sellPrice * p.wantedAmount;
                    resultRotten += p.sellPrice * p.wantedAmountRotten;
                }
                resultPure = SharedClass.twoDigitDecimal(resultPure);
                resultRotten = SharedClass.twoDigitDecimal(resultRotten);

                textViewResultPure.setText("Saf: " + resultPure);
                textViewResultRotten.setText("Çürük: " + resultRotten);

                notifyDataSetChanged();
            } catch (Exception e) {
                SharedClass.showSnackBar(activity, "Yanlış dəyər daxiletdiniz!");
            }
        });
        builder.setNegativeButton(R.string.cancel, (dialog, which) -> dialog.cancel());
        builder.show();
    }

    private void changeValueAmount(String name, MyViewHolder holder, Product product, boolean isPure) {
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        final EditText input = new EditText(activity);
        input.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);

        input.setHint("Yeni Miqdar");
        builder.setView(input);

        builder.setPositiveButton("OK", (dialog, which) -> {
            try {
                DatabaseHelper db = new DatabaseHelper(activity, DatabaseHelper.DATABASE_SELLER_PURE_ROTTEN);
                Double value = Double.parseDouble(input.getText().toString());

                if (isPure) {
                    holder.amountPure.setText(String.valueOf(value));
                    holder.commonPricePure.setText(String.valueOf(product.sellPrice * value));
                    product.wantedAmount = value;

                    db.changeColumnValue(name, TableInfo.ProductEntry.COLUMN_WANTED_AMOUNT, value);
                } else {
                    holder.amountRotten.setText(String.valueOf(value));
                    holder.commonPriceRotten.setText(String.valueOf(product.sellPrice * value));
                    product.wantedAmountRotten = value;

                    db.changeColumnValue(name, TableInfo.ProductEntry.COLUMN_WANTED_AMOUNT_ROTTEN, value);
                }

                double result = 0.0;
                for (Product p : db.getProductList()) {
                    double d = isPure ? p.wantedAmount : p.wantedAmountRotten;
                    result += p.sellPrice * d;
                }
                result = SharedClass.twoDigitDecimal(result);

                if (isPure)
                    textViewResultPure.setText("Saf: " + result);
                else textViewResultRotten.setText("Çürük: " + result);

                notifyDataSetChanged();
            } catch (Exception e) {
                SharedClass.showSnackBar(activity, "Yanlış dəyər daxiletdiniz!");
            }
        });
        builder.setNegativeButton(R.string.cancel, (dialog, which) -> dialog.cancel());
        builder.show();
    }

    @Override
    public int getItemCount() {
        return searchNameList.size();
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