package texel.com.depoproject.Pages.SignActivities;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import org.apache.commons.net.ntp.NTPUDPClient;
import org.apache.commons.net.ntp.TimeInfo;

import java.net.InetAddress;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

import texel.com.depoproject.CustomDialogs.CustomHorizontalProgressBar;
import texel.com.depoproject.HelperClasses.DatabaseFunctions;
import texel.com.depoproject.HelperClasses.SharedClass;
import texel.com.depoproject.Pages.AboutUs.AboutUsActivity;
import texel.com.depoproject.Pages.BUYER.BuyerProfile;
import texel.com.depoproject.Pages.DEPO.DepoProfile;
import texel.com.depoproject.Pages.SELLER.SellerProfile;
import texel.com.depoproject.R;

public class SignIn extends AppCompatActivity implements View.OnClickListener {

    public static final String TIME_SERVER = "time-a.nist.gov";
    public static String app_name;
    private final int TAG_WRITE_EXTERNAL_STORAGE = 1;
    private final int TAG_READ_EXTERNAL_STORAGE = 2;
    private final int PROJECT_VERSION = 2;
    private final SimpleDateFormat formatter = new SimpleDateFormat("dd/MM/yyyy hh:mm:SS");
    private final Timer timer = new Timer();
    private String rootName;
    private EditText editTextEmail;
    private EditText editTextPassword;
    private TextView textViewSignUp, buttonSignIn;
    private Button buttonAboutUs;
    private CustomHorizontalProgressBar progressBar;
    private FirebaseAuth firebaseAuth;
    private DatabaseReference databaseReference;
    private Activity activity;
    Thread thread = new Thread(new Runnable() {
        @Override
        public void run() {
            try {
                NTPUDPClient timeClient = new NTPUDPClient();
                InetAddress inetAddress = InetAddress.getByName(TIME_SERVER);
                TimeInfo timeInfo = timeClient.getTime(inetAddress);
                long returnTime = timeInfo.getMessage().getTransmitTimeStamp().getTime();   //server time

                Date date_1 = formatter.parse(formatter.format(new Date(returnTime)));
                Date date_2 = formatter.parse(formatter.format(new Date()));
                differenceBetweenGlobalAndLocalClock(date_1, date_2);
            } catch (Exception e) {
                Log.d("AAAAA", e.toString());
                runOnUiThread(new TimerTask() {
                    @Override
                    public void run() {
                        changeProgressBarVisibility(false);
                        Toast.makeText(activity, R.string.error_check_internet, Toast.LENGTH_SHORT).show();
                        activity.finish();
                    }
                });
            }
        }
    });

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_in);
        activity = this;

        app_name = getString(R.string.app_name);

        buttonSignIn = findViewById(R.id.buttonSignIn);
        progressBar = findViewById(R.id.progress);
        changeProgressBarVisibility(true);

//        thread.start();

        reLoadSeed();
    }

    public void differenceBetweenGlobalAndLocalClock(final Date startDate, final Date endDate) {
        //seconds
        final long different = (endDate.getTime() - startDate.getTime()) / 1000;

        activity.runOnUiThread(new TimerTask() {
            @Override
            public void run() {
                if (Math.abs(different) > 60 * 60) {
                    changeProgressBarVisibility(false);
                    Toast.makeText(activity, "Telefonunuzda SAAT SƏHVDİR! Saat: " + formatter.format(startDate) + " olmalıdır!", Toast.LENGTH_LONG).show();
                    finish();
                } else
                    reLoadSeed();
            }
        });
    }

    private void loadDatabases() {
        if (firebaseAuth.getCurrentUser() != null) {
            changeProgressBarVisibility(true);

            rootName = firebaseAuth.getCurrentUser().getEmail().split("@")[0];
            databaseReference = DatabaseFunctions.getDatabases(this).get(0);
            databaseReference.child("DatabaseInformation/version").addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                    Integer version = dataSnapshot.getValue(Integer.class);
                    if (version != null && version == PROJECT_VERSION)
                        goToProfile();
                    else {
                        changeProgressBarVisibility(false);
                        firebaseAuth.signOut();
                        Toast.makeText(getBaseContext(), "Proqramı Güncəlləyin!", Toast.LENGTH_LONG).show();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError databaseError) {
                    changeProgressBarVisibility(false);
                }
            });
        } else changeProgressBarVisibility(false);
    }

    private void reLoadSeed() {
        buttonSignIn.setOnClickListener(this);

        editTextEmail = findViewById(R.id.editTextEmail);
        editTextPassword = findViewById(R.id.editTextPassword);

        textViewSignUp = findViewById(R.id.textViewSignUp);
        textViewSignUp.setOnClickListener(this);

        buttonAboutUs = findViewById(R.id.buttonAboutUs);
        buttonAboutUs.setOnClickListener(this);

        firebaseAuth = FirebaseAuth.getInstance();

        checkMyPermissions();
    }

    private void checkMyPermissions() {
        int permissionWriteExternal = ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE);
        int permissionReadExternal = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE);

        if (permissionWriteExternal != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, TAG_WRITE_EXTERNAL_STORAGE);
            return;
        }
        if (permissionReadExternal != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, TAG_READ_EXTERNAL_STORAGE);
            return;
        }
        loadDatabases();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case TAG_WRITE_EXTERNAL_STORAGE: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    changeProgressBarVisibility(true);
                    checkMyPermissions();
                } else {
                    Toast.makeText(this, getString(R.string.write_permission_denied), Toast.LENGTH_SHORT).show();
                    finish();
                }
                return;
            }
            case TAG_READ_EXTERNAL_STORAGE: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    changeProgressBarVisibility(true);
                    checkMyPermissions();
                } else {
                    Toast.makeText(this, getString(R.string.read_permission_denied), Toast.LENGTH_SHORT).show();
                    finish();
                }
            }
        }
    }

    private void goToProfile() {
        databaseReference.child("USERS").addListenerForSingleValueEvent(new ValueEventListener() {
            @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN)
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                changeProgressBarVisibility(false);
                try {
                    Class user;
                    if (dataSnapshot.child("DEPO/" + rootName).exists())
                        user = DepoProfile.class;
                    else if (dataSnapshot.child("SELLER/" + rootName).exists())
                        user = SellerProfile.class;
                    else if (dataSnapshot.child("BUYER/" + rootName).exists())
                        user = BuyerProfile.class;
                    else return;

                    Intent intent = new Intent(getBaseContext(), user);
                    intent.putExtra("profile_name", user);
                    finish();
                    startActivity(intent);
                } catch (Exception e) {
                    changeProgressBarVisibility(false);
                    firebaseAuth.signOut();
                    Toast.makeText(getBaseContext(), R.string.maybe_profile_data_wrong, Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                changeProgressBarVisibility(false);
                Toast.makeText(getBaseContext(), R.string.error_check_internet, Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void userLogIn() {
        String email = editTextEmail.getText().toString().trim();
        String password = editTextPassword.getText().toString().trim();

        if (TextUtils.isEmpty(email)) {
            Toast.makeText(this, R.string.enter_email, Toast.LENGTH_SHORT).show();
            return;
        } else email += "@mail.ru";

        if (TextUtils.isEmpty(password)) {
            Toast.makeText(this, R.string.enter_password, Toast.LENGTH_SHORT).show();
            return;
        }

        firebaseAuth = FirebaseAuth.getInstance();
        changeProgressBarVisibility(true);
        firebaseAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(this, task -> {
            if (task.isSuccessful()) {
                loadDatabases();
            } else {
                changeProgressBarVisibility(false);
                SharedClass.showSnackBar(activity, getString(R.string.incorrect_email));
            }
        }).addOnFailureListener(Throwable::printStackTrace);
    }

    @Override
    public void onClick(View view) {
        if (view == buttonAboutUs) {
            startActivity(new Intent(this, AboutUsActivity.class));
        }

        if (view == buttonSignIn) {
            userLogIn();
        }

        if (view == textViewSignUp) {
            startActivity(new Intent(this, SignUp.class));
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (timer != null) timer.cancel();
    }

    private void changeProgressBarVisibility(boolean b) {
        progressBar.setVisibility(b ? View.VISIBLE : View.GONE);
        buttonSignIn.setEnabled(!b);
    }
}