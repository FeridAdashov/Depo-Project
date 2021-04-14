package texel.com.depoproject.Pages.RegisterActivities;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;

import java.util.ArrayList;

import texel.com.depoproject.CustomDialogs.CustomProgressDialog;
import texel.com.depoproject.HelperClasses.DatabaseFunctions;
import texel.com.depoproject.HelperClasses.SharedClass;
import texel.com.depoproject.InformationClasses.SellerInformation;
import texel.com.depoproject.Pages.SELLER.SellerProfile;
import texel.com.depoproject.R;

public class SellerRegisterActivity extends AppCompatActivity {

    private EditText editTextName, editTextSurname, editTextPhoneNumber;

    private CustomProgressDialog progressDialog;

    private FirebaseAuth firebaseAuth;
    private ArrayList<DatabaseReference> databases = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_seller_register);

        reLoadSeed();
    }

    private void reLoadSeed() {
        firebaseAuth = FirebaseAuth.getInstance();
        databases = DatabaseFunctions.getDatabases(this);

        TextView buttonRegister = findViewById(R.id.textViewRegister);
        buttonRegister.setOnClickListener(view -> registerUser());

        editTextName = findViewById(R.id.editTextName);
        editTextSurname = findViewById(R.id.editTextSurname);
        editTextPhoneNumber = findViewById(R.id.editTextPhoneNumber);
    }

    private void registerUser() {
        final String email = getIntent().getStringExtra("email");
        final String password = getIntent().getStringExtra("password");
        final String name = editTextName.getText().toString();
        final String surname = editTextSurname.getText().toString();
        final String phoneNumber = editTextPhoneNumber.getText().toString().trim();

        if (TextUtils.isEmpty(name)) {
            SharedClass.showSnackBar(this, getString(R.string.enter_name));
            return;
        }

        if (TextUtils.isEmpty(surname)) {
            SharedClass.showSnackBar(this, getString(R.string.enter_surname));
            return;
        }

        if (TextUtils.isEmpty(phoneNumber)) {
            SharedClass.showSnackBar(this, getString(R.string.enter_phone_number));
            return;
        }

        progressDialog = new CustomProgressDialog(this, getString(R.string.registering));
        progressDialog.setCancelable(false);
        progressDialog.show();

        firebaseAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(SellerRegisterActivity.this, task -> {
            progressDialog.dismiss();
            if (task.isSuccessful()) {
                try {
                    SellerInformation obj = new SellerInformation(
                            name + " " + surname,
                            phoneNumber,
                            password);

                    databases.get(0).child("USERS/SELLER/" + email.split("@")[0] + "/ABOUT").setValue(obj);

                    finishAffinity();
                    Intent intent = new Intent(getBaseContext(), SellerProfile.class);
                    startActivity(intent);
                } catch (Exception e) {
                    Log.d("AAAAA", e.toString());
                    progressDialog.dismiss();
                }
            } else {
                progressDialog.dismiss();
                SharedClass.showSnackBar(this, getString(R.string.incorrect_email));
            }
        });
    }
}
