package texel.com.depoproject.Pages.AboutUs;

import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;

import androidx.appcompat.app.AppCompatActivity;

import texel.com.depoproject.R;

public class AboutUsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about_us);

        ImageView whatsapp = findViewById(R.id.whatsapp);
        whatsapp.setOnClickListener(v -> {
            String url = "https://api.whatsapp.com/send?phone=+994705246774";
            Intent i = new Intent(Intent.ACTION_VIEW);
            i.setData(Uri.parse(url));
            startActivity(i);
        });

        ImageView call = findViewById(R.id.call);
        call.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_DIAL);
            intent.setData(Uri.parse("tel:+994705246774"));
            startActivity(intent);
        });

        ImageView instagram = findViewById(R.id.instagram);
        instagram.setOnClickListener(v -> {
            try {
                startActivity(new Intent(Intent.ACTION_VIEW,
                        Uri.parse("http://instagram.com/proqram_teminati1")));
            } catch (ActivityNotFoundException e) {
                try {
                    startActivity(new Intent(Intent.ACTION_VIEW,
                            Uri.parse("http://instagram.com/tebriz_qurbanov92")));
                } catch (Exception e2) {
                    Log.d("AAAAAA", e2.toString());
                }
            }
        });

        ImageView gmail = findViewById(R.id.gmail);
        gmail.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_SENDTO);
            intent.setData(Uri.parse("mailto:"));
            intent.putExtra(Intent.EXTRA_EMAIL, new String[]{"tebriz0705@gmail.com"});
            intent.putExtra(Intent.EXTRA_SUBJECT, "TexEl Group");
            intent.putExtra(Intent.EXTRA_TEXT, "Sizə necə kömək edə bilərik?");
            if (intent.resolveActivity(getPackageManager()) != null) {
                startActivity(intent);
            }
        });
    }
}