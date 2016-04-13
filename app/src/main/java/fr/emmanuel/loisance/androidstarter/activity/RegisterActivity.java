package fr.emmanuel.loisance.androidstarter.activity;

import android.app.Activity;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.mobsandgeeks.saripaar.ValidationError;
import com.mobsandgeeks.saripaar.Validator;
import com.mobsandgeeks.saripaar.annotation.Checked;
import com.mobsandgeeks.saripaar.annotation.Email;
import com.mobsandgeeks.saripaar.annotation.NotEmpty;
import com.mobsandgeeks.saripaar.annotation.Password;

import java.io.IOException;
import java.util.List;

import fr.emmanuel.loisance.androidstarter.R;
import fr.emmanuel.loisance.androidstarter.classe.User;
import fr.emmanuel.loisance.androidstarter.global.Constants;
import fr.emmanuel.loisance.androidstarter.global.GlobalState;
import fr.emmanuel.loisance.androidstarter.service.APIService;
import fr.emmanuel.loisance.androidstarter.util.Security;
import retrofit.Call;
import retrofit.Callback;
import retrofit.Response;
import retrofit.Retrofit;

public class RegisterActivity extends Activity {

    private static final String TAG = "RegisterActivity";
    private GlobalState gs;
    private Validator validator;
    private ProgressBar mLoader;

    @NotEmpty(messageResId = R.string.app_input_required)
    private EditText inputLastname;

    @NotEmpty(messageResId = R.string.app_input_required)
    private EditText inputFirstname;

    @NotEmpty(messageResId = R.string.app_input_required)
    @Email(messageResId = R.string.app_input_email_invalid)
    private EditText inputEmail;

    @NotEmpty(messageResId = R.string.app_input_required)
    @Password(min = 6, messageResId = R.string.app_input_password_invalid)
    private EditText inputPassword;

    @Checked(messageResId = R.string.app_checkbox_terms_invalid)
    private CheckBox checkBox;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        gs = (GlobalState) getApplication();

        final Button btnSend;
        final TextView txtAlreadySignup;
        final TextView txtTerms;

        mLoader = (ProgressBar) findViewById(R.id.register_loader);
        mLoader.setVisibility(View.INVISIBLE);

        inputLastname = (EditText) findViewById(R.id.register_input_lastname);
        inputFirstname = (EditText) findViewById(R.id.register_input_firstname);
        inputEmail = (EditText) findViewById(R.id.register_input_email);
        inputPassword = (EditText) findViewById(R.id.register_input_password);
        checkBox = (CheckBox) findViewById(R.id.register_checkbox_terms);

        txtAlreadySignup = (TextView) findViewById(R.id.register_txt_already_signup);
        txtAlreadySignup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        txtTerms = (TextView) findViewById(R.id.register_txt_terms);
        txtTerms.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(Constants.URL_OPEN_TERMS));
                startActivity(intent);
            }
        });

        btnSend = (Button) findViewById(R.id.register_btn_send);
        btnSend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                validator.validate();
            }
        });

        validator = new Validator(this);
        validator.setValidationListener(new Validator.ValidationListener() {
            @Override
            public void onValidationSucceeded() {
                if(!gs.isNetworkAvailable(RegisterActivity.this)) return;
                mLoader.setVisibility(View.VISIBLE);

                APIService api = gs.getRetrofit().create(APIService.class);
                Call<User> call = api.createUserWithDefault(inputFirstname.getText().toString(), inputLastname.getText().toString(), inputEmail.getText().toString(), Security.SHA1(inputPassword.getText().toString()));

                call.enqueue(new Callback<User>() {
                    @Override
                    public void onResponse(Response<User> response, Retrofit retrofit) {
                        if (response.isSuccess()) { // success si status code entre 200 et 300
                            Log.d(TAG, "API User From Default Success");
                            Log.d(TAG, "User: " + response.body().toString());
                            GlobalState gs = (GlobalState) getApplicationContext();
                            gs.setIsConnected(true);
                            gs.setUser(response.body());
                            // redirect
                            Intent intent = new Intent(RegisterActivity.this, MainActivity.class);
                            startActivity(intent);
                            finish();
                        } else {
                            try {
                                Log.d(TAG, "API User From Default Fail: " + response.errorBody().string());
                                switch (response.code()) {
                                    case 400:
                                        Toast.makeText(getApplicationContext(), "Unable to create your account", Toast.LENGTH_LONG).show();
                                        break;
                                    case 404:
                                        Toast.makeText(getApplicationContext(), "Error during the creation of your account", Toast.LENGTH_LONG).show();
                                        break;
                                    case 409:
                                        Toast.makeText(getApplicationContext(), "This email is already use", Toast.LENGTH_LONG).show();
                                        break;
                                }
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                        mLoader.setVisibility(View.INVISIBLE);
                    }

                    @Override
                    public void onFailure(Throwable t) {
                        Log.e(TAG, "register fail: " + t.getMessage());
                        mLoader.setVisibility(View.INVISIBLE);
                    }
                });
            }

            @Override
            public void onValidationFailed(List<ValidationError> errors) {
                for(ValidationError error : errors) {
                    View view = error.getView();
                    String message = error.getCollatedErrorMessage(getApplicationContext());
                    if(view instanceof EditText) {
                        Drawable drawable = getResources().getDrawable(R.drawable.ic_error_24dp);
                        drawable.setBounds(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
                        ((EditText) view).setError(message, drawable);
                    } else {
                        Toast.makeText(getApplicationContext(), message, Toast.LENGTH_LONG).show();
                    }
                }
            }
        });

    }

}
