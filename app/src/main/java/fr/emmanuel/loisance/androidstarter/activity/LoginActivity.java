package fr.emmanuel.loisance.androidstarter.activity;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.plus.Plus;
import com.google.android.gms.plus.model.people.Person;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mobsandgeeks.saripaar.ValidationError;
import com.mobsandgeeks.saripaar.Validator;
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
import retrofit.Call;
import retrofit.Callback;
import retrofit.GsonConverterFactory;
import retrofit.Response;
import retrofit.Retrofit;

public class LoginActivity extends Activity implements View.OnClickListener, GoogleApiClient.ConnectionCallbacks, GoogleApiClient.OnConnectionFailedListener {

    private static final String TAG = "LoginActivity";
    private GlobalState gs;
    private Validator mValidator;
    private ProgressBar mLoader;

    // Google service
    private GoogleApiClient mGoogleApiClient;
    private static final int REQUEST_CODE_RESOLVE_ERR = 9000;

    // Is there a ConnectionResult resolution in progress ?
    private boolean mIsResolving = false;

    // Should we automatically resolve ConnectionResults when possible ?
    private boolean mShouldResolve = false;

    @NotEmpty(messageResId = R.string.app_input_required)
    @Email(messageResId = R.string.app_input_email_invalid)
    private EditText inputEmail;

    @NotEmpty(messageResId = R.string.app_input_required)
    @Password(min = 6, messageResId = R.string.app_input_password_invalid)
    private EditText inputPassword;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        gs = (GlobalState) getApplication();

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(Plus.API)
                .addScope(Plus.SCOPE_PLUS_LOGIN)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .build();

        final Button mSignInButton;
        final Button mBtnAnonymous;
        final Button mBtnConnection;
        final TextView txtRegister;
        final TextView txtForgotPassword;

        mLoader = (ProgressBar) findViewById(R.id.login_loader);
        mLoader.setVisibility(View.INVISIBLE);

        mSignInButton = (Button) findViewById(R.id.login_btn_signin_google);
        mSignInButton.setOnClickListener(this);

        mBtnAnonymous = (Button) findViewById(R.id.login_btn_anonymous);
        mBtnAnonymous.setOnClickListener(this);

        mBtnConnection = (Button) findViewById(R.id.login_btn_connection);
        mBtnConnection.setOnClickListener(this);

        inputEmail = (EditText) findViewById(R.id.login_input_email);
        inputPassword = (EditText) findViewById(R.id.login_input_password);

        txtRegister = (TextView) findViewById(R.id.login_txt_register);
        txtRegister.setOnClickListener(this);

        txtForgotPassword = (TextView) findViewById(R.id.login_txt_forgot_password);
        txtForgotPassword.setOnClickListener(this);

        mValidator = new Validator(this);
        mValidator.setValidationListener(new Validator.ValidationListener() {
            @Override
            public void onValidationSucceeded() {
                String email = inputEmail.getText().toString();
                String password = inputPassword.getText().toString();
                connectWithDefault(email, password);
            }

            @Override
            public void onValidationFailed(List<ValidationError> errors) {
                for (ValidationError error : errors) {
                    View view = error.getView();
                    String message = error.getCollatedErrorMessage(getApplicationContext());
                    if (view instanceof EditText) {
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
    public void onStart() {
        super.onStart();
        mGoogleApiClient.connect();
    }


    @Override
    public void onStop() {
        super.onStop();
        if (mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }
    }


    @Override
    public void onClick(View view) {
        // Connect with Google
        if (view.getId() == R.id.login_btn_signin_google) {
            mShouldResolve = true;
            mGoogleApiClient.connect();
        }
        // Anonymous user
        if (view.getId() == R.id.login_btn_anonymous) {
            Intent intent = new Intent(LoginActivity.this, MainActivity.class);
            startActivity(intent);
            finish();
        }
        // Connect with default form
        if (view.getId() == R.id.login_btn_connection) {
            mValidator.validate();
        }
        // Click on register
        if (view.getId() == R.id.login_txt_register) {
            Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
            startActivity(intent);
        }
        // Click on forgot password
        if (view.getId() == R.id.login_txt_forgot_password) {
            Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(Constants.URL_OPEN_FORGOT_PASSWORD));
            startActivity(intent);
        }
    }


    /**
     * User connected with Google, we get and save his information
     * and redirect him to main
     *
     * @param bundle bundle information
     */
    @Override
    public void onConnected(Bundle bundle) {
        Log.d(TAG, "onConnected");
        connectWithGoogle();
    }


    /**
     * @param i int cause
     */
    @Override
    public void onConnectionSuspended(int i) {
        Log.d(TAG, "onConnectionSuspended");
    }


    /**
     * Try to resolve google connection
     * @param connectionResult result
     */
    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.d(TAG, "onConnectionFailed");
        if (!mIsResolving && mShouldResolve) {
            if (connectionResult.hasResolution()) {
                try {
                    mIsResolving = true;
                    Log.d(TAG, "startIntentSenderForResult");
                    startIntentSenderForResult(connectionResult.getResolution().getIntentSender(), REQUEST_CODE_RESOLVE_ERR, null, 0, 0, 0);
                } catch (IntentSender.SendIntentException e) {
                    // The intent was canceled before it was sent. Return to the default
                    // state abd attempt to connet to get an updated ConnectionResult
                    mIsResolving = false;
                    mGoogleApiClient.connect();
                }
            } else {
                // could not resolve the connection result, show the user and error dialog
                Log.d(TAG, "Could not resolve the connection result, error code: " + String.valueOf(connectionResult.getErrorCode()));
                Toast.makeText(this, "Connection failed, status code : " + String.valueOf(connectionResult.getErrorCode()), Toast.LENGTH_LONG).show();
            }
        } else {
            // Show the signed-out UI
            Log.d(TAG, "onConnectionFailed, do not resolve");
        }
    }


    /**
     * @param requestCode requestCode
     * @param resultCode resultCode
     * @param intent intent
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        Log.d(TAG, "onActivityResult");
        if (requestCode == REQUEST_CODE_RESOLVE_ERR) {
            // If the error resolution was not successful we should not resolve further
            if (resultCode != Activity.RESULT_OK) {
                mShouldResolve = false;
            }

            mIsResolving = false;
            if (!mGoogleApiClient.isConnected()) {
                mGoogleApiClient.connect();
            }
        }
    }


    /**
     * Connect user with default form
     *
     * @param email email from login form
     * @param password password from login form
     */
    private void connectWithDefault(String email, String password) {
        if (!gs.isNetworkAvailable(this)) return;
        mLoader.setVisibility(View.VISIBLE);

        Gson gson = new GsonBuilder()
                .setDateFormat("yyyy-MM-dd")
                .create();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(Constants.URL_API)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build();

        APIService api = retrofit.create(APIService.class);
        Call<User> call = api.getUserFromDefault(email, password);

        call.enqueue(new Callback<User>() {
            @Override
            public void onResponse(Response<User> response, Retrofit retrofit) {
                switch (response.code()) {
                    case 200:
                        Log.d(TAG, "API User From Default Success");
                        Log.d(TAG, "User: " + response.body().toString());
                        GlobalState gs = (GlobalState) getApplicationContext();
                        gs.setIsConnected(true);
                        gs.setUser(response.body());
                        // redirect
                        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                        startActivity(intent);
                        finish();
                        break;
                    case 404:
                        Log.d(TAG, "Not found");
                        Toast.makeText(getApplicationContext(), "User not found", Toast.LENGTH_LONG).show();
                        break;
                }
                mLoader.setVisibility(View.INVISIBLE);
            }

            @Override
            public void onFailure(Throwable t) {
                Log.d(TAG, "connection default fail: " + t.getMessage() + t.getCause().getMessage());
                Toast.makeText(getApplicationContext(), "Connection failed", Toast.LENGTH_LONG).show();
                mLoader.setVisibility(View.INVISIBLE);
            }
        });
    }


    /**
     * Connect user with his google account,
     * if account do not exist yet, we try to create it with createUserWithGoogle function
     */
    private void connectWithGoogle() {
        if (!gs.isNetworkAvailable(this)) return;
        mLoader.setVisibility(View.VISIBLE);

        final String idGoogle = Plus.PeopleApi.getCurrentPerson(mGoogleApiClient).getId();

        Gson gson = new GsonBuilder()
                .setDateFormat("yyyy-MM-dd")
                .create();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(Constants.URL_API)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build();

        APIService api = retrofit.create(APIService.class);
        Call<User> call = api.getUserFromGoogle(idGoogle);

        call.enqueue(new Callback<User>() {
            @Override
            public void onResponse(Response<User> response, Retrofit retrofit) {
                switch (response.code()) {
                    case 200:
                        Log.d(TAG, "API User From Google Success");
                        Log.d(TAG, "User: " + response.body().toString());
                        GlobalState gs = (GlobalState) getApplicationContext();
                        gs.setIsConnected(true);
                        gs.setUser(response.body());
                        // redirect
                        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                        startActivity(intent);
                        mLoader.setVisibility(View.INVISIBLE);
                        finish();
                        break;
                    case 404:
                        Log.d(TAG, "Not found");
                        createUserWithGoogle(idGoogle);
                        break;
                }
            }

            @Override
            public void onFailure(Throwable t) {
                Toast.makeText(getApplicationContext(), "Connection failed", Toast.LENGTH_LONG).show();
                mLoader.setVisibility(View.INVISIBLE);
                if(mGoogleApiClient.isConnected()) {
                    // Logout to retry connection on next sign in click
                    mGoogleApiClient.clearDefaultAccountAndReconnect();
                    mGoogleApiClient.disconnect();
                }
            }
        });
    }


    /**
     * Add user in base with google information
     * API return this new user and redirect him to main if it's ok
     *
     * @param idGoogle user google id
     */
    private void createUserWithGoogle(String idGoogle) {
        if (!gs.isNetworkAvailable(this)) return;

        String emailGoogle = Plus.AccountApi.getAccountName(mGoogleApiClient);
        Person me = Plus.PeopleApi.getCurrentPerson(mGoogleApiClient);

        Gson gson = new GsonBuilder()
                .setDateFormat("yyyy-MM-dd")
                .create();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(Constants.URL_API)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build();

        APIService api = retrofit.create(APIService.class);
        Call<User> user = api.createUserWithGoogle(idGoogle, me.getName().getGivenName(), me.getName().getFamilyName(), emailGoogle);
        user.enqueue(new Callback<User>() {
            @Override
            public void onResponse(Response<User> response, Retrofit retrofit) {
                if (response.isSuccess()) {
                    Log.d(TAG, "API Create User From Google Success : " + response.code());
                    Log.d(TAG, "New User from Google : " + response.body().toString());
                    GlobalState gs = (GlobalState) getApplicationContext();
                    gs.setIsConnected(true);
                    gs.setUser(response.body());
                    // redirect
                    Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                    startActivity(intent);
                    finish();
                } else {
                    try {
                        Log.d(TAG, "API Create User From Google Fail : " + response.errorBody().string());
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                mLoader.setVisibility(View.INVISIBLE);
            }

            @Override
            public void onFailure(Throwable t) {
                Log.d(TAG, "API Create User From Google Fail");
                mLoader.setVisibility(View.INVISIBLE);
            }
        });
    }


    /**
     * Open alert dialog before leave application to ask
     * user if he really want to do this
     */
    @Override
    public void onBackPressed() {
        new AlertDialog.Builder(this)
                .setMessage(R.string.app_leave_application_txt)
                .setPositiveButton(R.string.app_leave_application_yes, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        finish();
                        System.exit(0);
                    }
                })
                .setNegativeButton(R.string.app_leave_application_no, null)
                .show();
    }

}
