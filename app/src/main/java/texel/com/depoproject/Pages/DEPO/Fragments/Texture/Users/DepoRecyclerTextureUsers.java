package texel.com.depoproject.Pages.DEPO.Fragments.Texture.Users;

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

import texel.com.depoproject.Pages.DEPO.Fragments.Texture.History.TextureHistoryActivity;
import texel.com.depoproject.Pages.DEPO.Fragments.Texture.Products.TextureProductsDepoActivity;
import texel.com.depoproject.R;

public class DepoRecyclerTextureUsers extends RecyclerView.Adapter<DepoRecyclerTextureUsers.MyViewHolder> {

    private final Activity activity;
    private final ArrayList<String> userNameList;
    private final ArrayList<String> nameList;
    private final ArrayList<String> searchNameList;

    public DepoRecyclerTextureUsers(Activity activity,
                                    ArrayList<String> userNameList,
                                    ArrayList<String> nameList,
                                    ArrayList<String> searchNameList) {
        this.activity = activity;
        this.userNameList = userNameList;
        this.nameList = nameList;
        this.searchNameList = searchNameList;
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View listItem = LayoutInflater.from(parent.getContext()).inflate(R.layout.card_view_depo_texture_users, parent, false);
        return new MyViewHolder(listItem);
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, final int pos) {
        int position = nameList.indexOf(searchNameList.get(pos));

        String user_name = userNameList.get(position);
        String name = nameList.get(position);
        holder.name.setText(name);

        holder.cardView.setOnClickListener(v -> {
            AlertDialog.Builder builder = new AlertDialog.Builder(activity);
            builder.setNeutralButton("Tarixçə", (dialog, which) -> openTextureHistoryActivity(user_name, name));
            builder.setPositiveButton("Yeni", (dialog, which) -> openProductsActivity(user_name, name));
            builder.setNegativeButton(R.string.cancel, (dialog, which) -> dialog.cancel());
            builder.show();
        });
    }

    private void openTextureHistoryActivity(String user_name, String name) {
        Intent intent = new Intent(activity, TextureHistoryActivity.class);
        intent.putExtra("texture_child_name", user_name + "_" + name);
        activity.startActivity(intent);
    }

    private void openProductsActivity(String user_name, String name) {
        Intent intent = new Intent(activity, TextureProductsDepoActivity.class);
        intent.putExtra("user_name", user_name);
        intent.putExtra("name", name);
        activity.startActivity(intent);
    }

    @Override
    public int getItemCount() {
        return searchNameList.size();
    }

    static class MyViewHolder extends RecyclerView.ViewHolder {
        private final CardView cardView;
        private final TextView name;

        MyViewHolder(View itemView) {
            super(itemView);
            cardView = itemView.findViewById(R.id.cardViewTextureUser);
            name = itemView.findViewById(R.id.cardTextViewUserName);
        }
    }
}