package texel.com.depoproject.Pages.DEPO.Fragments.Texture.Basket;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.HashMap;

import texel.com.depoproject.HelperClasses.SharedClass;
import texel.com.depoproject.R;
import texel.com.depoproject.SqlLiteDatabase.DatabaseHelper;
import texel.com.depoproject.SqlLiteDatabase.Product;
import texel.com.depoproject.SqlLiteDatabase.TableInfo;

public class DepoTextureBasketProductsRecycler extends RecyclerView.Adapter<DepoTextureBasketProductsRecycler.MyViewHolder> {

    private final TextView textViewResult;
    private final ArrayList<String> nameList;
    private final HashMap<String, Product> hashMap;
    private final Activity activity;

    public DepoTextureBasketProductsRecycler(Activity activity,
                                             ArrayList<String> nameList,
                                             HashMap<String, Product> hashMap,
                                             TextView textViewResult) {
        this.activity = activity;
        this.nameList = nameList;
        this.hashMap = hashMap;
        this.textViewResult = textViewResult;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View listItem = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_view_product_basket, parent, false);
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
        holder.amount.setText(String.valueOf(product.wantedAmount));
        holder.commonPrice.setText(String.format("%.2f", product.wantedAmount * product.sellPrice));

        holder.price.setOnClickListener(v -> changeValue(holder.price, holder.commonPrice, name, product.sellPrice, product.wantedAmount, true));

        holder.amount.setOnClickListener(v -> changeValue(holder.amount, holder.commonPrice, name, product.sellPrice, product.wantedAmount, false));
    }

    private void changeValue(final TextView textViewFirst, final TextView textViewSecond, String name, double price, double amount, boolean b) {
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        final EditText input = new EditText(activity);
        input.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
        input.setHint(b ? "Yeni Qiymət" : "Yeni Miqdar");
        builder.setView(input);

        builder.setPositiveButton("OK", (dialog, which) -> {
            try {
                DatabaseHelper db = new DatabaseHelper(activity, DatabaseHelper.DATABASE_TEXTURES);
                Double value = Double.parseDouble(input.getText().toString());

                if (b) {
                    textViewFirst.setText(String.valueOf(value));
                    textViewSecond.setText(String.valueOf(value * amount));

                    hashMap.put(name, new Product(name, hashMap.get(name).name, 0.0, value, amount));

                    db.changeColumnValue(name, TableInfo.ProductEntry.COLUMN_BUY_PRICE, value);
                } else {
                    textViewFirst.setText(String.valueOf(value));
                    textViewSecond.setText(String.valueOf(price * value));

                    hashMap.put(name, new Product(name, hashMap.get(name).name, 0.0, price, value));

                    db.changeColumnValue(name, TableInfo.ProductEntry.COLUMN_WANTED_AMOUNT, value);
                }

                double result = 0.0;
                for (Product p : db.getProductList()) result += p.sellPrice * p.wantedAmount;
                result = SharedClass.twoDigitDecimal(result);
                textViewResult.setText(String.valueOf(result));

                notifyDataSetChanged();
            } catch (Exception e) {
                Toast.makeText(activity, "Yanlış dəyər daxiletdiniz!", Toast.LENGTH_SHORT).show();
            }
        });
        builder.setNegativeButton(R.string.cancel, (dialog, which) -> dialog.cancel());
        builder.show();
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