package texel.com.depoproject.Pages.SELLER.Activities.CreateSell;

import android.app.Activity;
import android.app.AlertDialog;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.HashMap;

import texel.com.depoproject.DataClasses.ProductInfo;
import texel.com.depoproject.HelperClasses.SharedClass;
import texel.com.depoproject.R;
import texel.com.depoproject.SqlLiteDatabase.DatabaseHelper;
import texel.com.depoproject.SqlLiteDatabase.Product;

public class CreateSellProductsRecycler extends RecyclerView.Adapter<CreateSellProductsRecycler.MyViewHolder> {

    private final ArrayList<String> nameList;
    private final ArrayList<String> searchNameList;
    private final HashMap<String, ProductInfo> hashMap;
    private final Activity activity;

    public CreateSellProductsRecycler(Activity activity,
                                      ArrayList<String> nameList,
                                      ArrayList<String> searchNameList,
                                      HashMap<String, ProductInfo> hashMap) {
        this.activity = activity;
        this.nameList = nameList;
        this.searchNameList = searchNameList;
        this.hashMap = hashMap;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View listItem = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_view_product_seller, parent, false);
        return new MyViewHolder(listItem);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, final int pos) {
        int position = nameList.indexOf(searchNameList.get(pos));

        String name = nameList.get(position);

        ProductInfo productInfo = hashMap.get(name);
        if (productInfo == null) return;

        holder.name.setText(productInfo.name);
        holder.price.setText(String.valueOf(productInfo.sellPrice));

        holder.cardViewProductSeller.setOnClickListener(v -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(activity);
            final EditText input = new EditText(activity);
            input.setInputType(InputType.TYPE_CLASS_NUMBER | InputType.TYPE_NUMBER_FLAG_DECIMAL);
            input.setHint("Miqdar daxil edin");
            builder.setView(input);

            builder.setPositiveButton("OK", (dialog, which) -> {
                try {
                    double amount = Double.parseDouble(input.getText().toString());
                    if (amount == 0) throw new Exception();

                    addToBasket(name, new Product(name, productInfo.name, productInfo.buyPrice, productInfo.sellPrice, amount));
                } catch (Exception e) {
                    SharedClass.showSnackBar(activity, "Yanlış dəyər daxiletdiniz!");
                }
            });
            builder.setNegativeButton(R.string.cancel, (dialog, which) -> dialog.cancel());
            builder.show();
        });
    }

    private void addToBasket(String name, Product product) {
        DatabaseHelper db = new DatabaseHelper(activity,
                CreateSellActivity.isNewSell ? DatabaseHelper.DATABASE_SELLS : DatabaseHelper.DATABASE_SELLER_RETURN);
        if (db.checkValueExist(name))
            SharedClass.showSnackBar(activity, "Bu Məhsul Artıq Səbətdə Var!");
        else {
            db.addProduct(product);
            db.close();
        }
    }

    @Override
    public int getItemCount() {
        return searchNameList.size();
    }

    static class MyViewHolder extends RecyclerView.ViewHolder {
        private final CardView cardViewProductSeller;
        private final TextView name;
        private final TextView price;

        MyViewHolder(View itemView) {
            super(itemView);
            cardViewProductSeller = itemView.findViewById(R.id.cardViewProductSeller);
            name = itemView.findViewById(R.id.cardTextViewProductName);
            price = itemView.findViewById(R.id.cardTextViewProductPrice);
        }
    }
}