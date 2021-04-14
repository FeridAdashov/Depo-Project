package texel.com.depoproject.Pages.SignActivities;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.RadioGroup;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import texel.com.depoproject.HelperClasses.SharedClass;
import texel.com.depoproject.Pages.RegisterActivities.BuyerRegisterActivity;
import texel.com.depoproject.Pages.RegisterActivities.DepoRegisterActivity;
import texel.com.depoproject.Pages.RegisterActivities.SellerRegisterActivity;
import texel.com.depoproject.R;

public class SignUp extends AppCompatActivity implements View.OnClickListener {

    private TextView buttonNextToInformationPage;
    private EditText editTextEmail, editTextPasswordConfirm;
    private EditText editTextPassword;
    private int statusId = 1;
    private Activity activity;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);
        activity = this;

        RadioGroup radioGroupUsers = findViewById(R.id.radioGroupUsers);
        radioGroupUsers.setOnCheckedChangeListener((radioGroup, i) -> {
            switch (i) {
                case R.id.radioSeller:
                    statusId = 1;
                    break;

                case R.id.radioBuyer:
                    statusId = 3;
                    break;

                case R.id.radioDepo:
                    statusId = 2;
                    break;
            }
        });

        switch (statusId) {
            case 1:
                radioGroupUsers.check(R.id.radioSeller);
                break;

            case 2:
                radioGroupUsers.check(R.id.radioDepo);
                break;

            case 3:
                radioGroupUsers.check(R.id.radioBuyer);
                break;
        }

        buttonNextToInformationPage = findViewById(R.id.buttonNextToInformationPage);
        buttonNextToInformationPage.setOnClickListener(this);
        editTextPasswordConfirm = findViewById(R.id.editTextPasswordConfirm);
        editTextPasswordConfirm.setOnClickListener(this);

        editTextEmail = findViewById(R.id.editTextEmail);
        editTextPassword = findViewById(R.id.editTextPassword);
    }


    @Override
    public void onClick(View view) {
        if (view == buttonNextToInformationPage) {
            String email = editTextEmail.getText().toString();
            String password = editTextPassword.getText().toString();
            String passwordConfirm = editTextPasswordConfirm.getText().toString();

            if (email.length() < 5) {
                SharedClass.showSnackBar(activity, getString(R.string.enter_email));
                return;
            } else email += "@mail.ru";

            if (password.length() < 8) {
                SharedClass.showSnackBar(activity, getString(R.string.enter_password));
                return;
            }

            if (!password.equals(passwordConfirm)) {
                SharedClass.showSnackBar(activity, getString(R.string.password_not_matched));
                return;
            }

            Intent intent;
            switch (statusId) {
                case 1:
                    intent = new Intent(activity, SellerRegisterActivity.class);
                    break;

                case 2:
                    intent = new Intent(activity, DepoRegisterActivity.class);
                    break;

                case 3:
                    intent = new Intent(activity, BuyerRegisterActivity.class);
                    break;

                default:
                    return;
            }
            intent.putExtra("email", email);
            intent.putExtra("password", password);
            startActivity(intent);
        }
    }
}
