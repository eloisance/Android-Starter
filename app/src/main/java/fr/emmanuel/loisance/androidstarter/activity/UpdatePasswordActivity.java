package fr.emmanuel.loisance.androidstarter.activity;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mobsandgeeks.saripaar.ValidationError;
import com.mobsandgeeks.saripaar.Validator;
import com.mobsandgeeks.saripaar.annotation.ConfirmPassword;
import com.mobsandgeeks.saripaar.annotation.Length;
import com.mobsandgeeks.saripaar.annotation.NotEmpty;
import com.mobsandgeeks.saripaar.annotation.Password;

import java.util.List;

import fr.emmanuel.loisance.androidstarter.R;
import fr.emmanuel.loisance.androidstarter.classe.User;
import fr.emmanuel.loisance.androidstarter.global.Constants;
import fr.emmanuel.loisance.androidstarter.global.GlobalState;
import fr.emmanuel.loisance.androidstarter.service.APIService;
import fr.emmanuel.loisance.androidstarter.util.Security;
import retrofit.Call;
import retrofit.Callback;
import retrofit.GsonConverterFactory;
import retrofit.Response;
import retrofit.Retrofit;

public class UpdatePasswordActivity extends AppCompatActivity implements View.OnClickListener {

    private static final String TAG = "UpdatePasswordActivity";

    private GlobalState gs;
    private User user;

    private Toolbar mToolbar;
    private Validator validator;
    ProgressBar mProgressBar;

    @Length(min = 6, messageResId = R.string.app_input_password_invalid)
    private EditText inputPassword;

    @Password(min = 6, messageResId = R.string.app_input_password_invalid)
    private EditText inputNewPassword;

    @NotEmpty(messageResId = R.string.app_input_required)
    @ConfirmPassword(messageResId = R.string.app_input_password_confirm_invalid)
    private EditText inputNewPasswordConfirm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_password);

        gs = (GlobalState) getApplication();
        this.user = gs.getUser();

        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);
        if(getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        }

        inputPassword = (EditText) findViewById(R.id.update_password_input_password);
        inputNewPassword = (EditText) findViewById(R.id.update_password_input_new_password);
        inputNewPasswordConfirm = (EditText) findViewById(R.id.update_password_input_new_password_confirm);

        Button btnSubmit = (Button) findViewById(R.id.update_password_button_submit);
        btnSubmit.setOnClickListener(this);

        mProgressBar = (ProgressBar) findViewById(R.id.progressBarUpdatePassword);

        validator = new Validator(this);
        validator.setValidationListener(new Validator.ValidationListener() {
            @Override
            public void onValidationSucceeded() {
                if (!gs.isNetworkAvailable(getApplicationContext())) return;
                mProgressBar.setVisibility(View.VISIBLE);

                Gson gson = new GsonBuilder()
                        .setDateFormat("yyyy-MM-dd")
                        .create();

                Retrofit retrofit = new Retrofit.Builder()
                        .baseUrl(Constants.URL_API)
                        .addConverterFactory(GsonConverterFactory.create(gson))
                        .build();

                APIService api = retrofit.create(APIService.class);
                Call<User> call = api.updatePasswordUser(user.getId(), Security.SHA1(inputPassword.getText().toString()), Security.SHA1(inputNewPassword.getText().toString()));

                call.enqueue(new Callback<User>() {
                    @Override
                    public void onResponse(Response<User> response, Retrofit retrofit) {
                        switch (response.code()) {
                            case 200:
                                Log.d(TAG, "Password updated successfully");
                                Log.d(TAG, "New User: " + response.body().toString());
                                Toast.makeText(getApplicationContext(), getResources().getString(R.string.update_password_message_update_ok), Toast.LENGTH_LONG).show();
                                gs.setUser(response.body());
                                finish();
                                break;
                            case 400:
                                Log.d(TAG, "Password can't be updated");
                                Toast.makeText(getApplicationContext(), getResources().getString(R.string.update_password_message_error, 400), Toast.LENGTH_LONG).show();
                                break;
                            case 404:
                                Log.d(TAG, "User's password to update Not found");
                                Toast.makeText(getApplicationContext(), getResources().getString(R.string.update_password_message_error, 404), Toast.LENGTH_LONG).show();
                                break;
                        }
                        mProgressBar.setVisibility(View.INVISIBLE);
                    }

                    @Override
                    public void onFailure(Throwable t) {
                        Log.d(TAG, "connection fail: " + t.getMessage() + t.getCause().getMessage());
                        Toast.makeText(getApplicationContext(), getResources().getString(R.string.update_password_message_failure), Toast.LENGTH_LONG).show();
                        mProgressBar.setVisibility(View.INVISIBLE);
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

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                break;
        }
        return true;
    }

    @Override
    public void onClick(View view) {
        if(view.getId() == R.id.update_password_button_submit) {
            inputPassword.clearFocus();
            inputNewPassword.clearFocus();
            inputNewPasswordConfirm.clearFocus();
            // close keyboard
            View v = getCurrentFocus();
            if (v != null) {
                InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
            }
            if(isRightPassword()) {
                validator.validate();
            } else {
                Toast.makeText(getApplicationContext(), R.string.update_password_message_update_wrong, Toast.LENGTH_LONG).show();
            }
        }
    }

    /**
     * Check if password enter is right password of this account
     */
    private boolean isRightPassword() {
        return user.getPassword().equals(Security.SHA1(inputPassword.getText().toString()));
    }
}
