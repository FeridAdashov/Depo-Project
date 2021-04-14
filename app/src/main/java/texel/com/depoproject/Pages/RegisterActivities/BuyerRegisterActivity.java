package texel.com.depoproject.Pages.RegisterActivities;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;

import java.util.ArrayList;

import texel.com.depoproject.CustomDialogs.CustomProgressDialog;
import texel.com.depoproject.HelperClasses.DatabaseFunctions;
import texel.com.depoproject.InformationClasses.DepoInformation;
import texel.com.depoproject.Pages.BUYER.BuyerProfile;
import texel.com.depoproject.R;


public class BuyerRegisterActivity extends AppCompatActivity {

    private EditText editTextName, editTextPhoneNumber;

    private FirebaseAuth firebaseAuth;
    private ArrayList<DatabaseReference> databases = new ArrayList<>();

    private CustomProgressDialog progressDialog;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_depo_register);
        reLoadSeed();
    }

    private void reLoadSeed() {
        firebaseAuth = FirebaseAuth.getInstance();
        databases = DatabaseFunctions.getDatabases(this);

        TextView buttonRegisterStudent = findViewById(R.id.textViewRegister);
        buttonRegisterStudent.setOnClickListener(view -> registerUser());

        editTextName = findViewById(R.id.editTextName);
        editTextPhoneNumber = findViewById(R.id.editTextPhoneNumber);
    }

    private void registerUser() {
        final String email = getIntent().getStringExtra("email");
        final String password = getIntent().getStringExtra("password");
        final String name = editTextName.getText().toString();
        final String phone = editTextPhoneNumber.getText().toString();

        if (TextUtils.isEmpty(name)) {
            Toast.makeText(this, R.string.enter_name, Toast.LENGTH_SHORT).show();
            return;
        }

        if (TextUtils.isEmpty(phone)) {
            Toast.makeText(this, R.string.enter_phone_number, Toast.LENGTH_SHORT).show();
            return;
        }

        progressDialog = new CustomProgressDialog(this, getString(R.string.registering));
        progressDialog.setCancelable(false);
        progressDialog.show();

        try {
            firebaseAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(BuyerRegisterActivity.this, task -> {
                progressDialog.dismiss();
                if (task.isSuccessful()) {
                    DepoInformation obj = new DepoInformation(
                            name,
                            phone,
                            password);

                    databases.get(0).child("USERS/BUYER/" + email.split("@")[0] + "/ABOUT").setValue(obj);

                    finishAffinity();
                    Intent intent = new Intent(getBaseContext(), BuyerProfile.class);
                    startActivity(intent);
                } else {
                    progressDialog.dismiss();
                    Toast.makeText(getBaseContext(), R.string.incorrect_email, Toast.LENGTH_SHORT).show();
                }
            }).addOnFailureListener(Throwable::printStackTrace);
        } catch (Exception e) {
            Log.d("AAAAA", e.toString());
            progressDialog.dismiss();
        }
    }
}
