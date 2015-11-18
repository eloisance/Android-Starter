package fr.emmanuel.loisance.androidstarter.fragment;

import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.plus.Plus;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.mobsandgeeks.saripaar.ValidationError;
import com.mobsandgeeks.saripaar.Validator;
import com.mobsandgeeks.saripaar.annotation.Email;
import com.mobsandgeeks.saripaar.annotation.NotEmpty;

import java.util.List;

import fr.emmanuel.loisance.androidstarter.R;
import fr.emmanuel.loisance.androidstarter.activity.LoginActivity;
import fr.emmanuel.loisance.androidstarter.activity.UpdatePasswordActivity;
import fr.emmanuel.loisance.androidstarter.classe.User;
import fr.emmanuel.loisance.androidstarter.global.Constants;
import fr.emmanuel.loisance.androidstarter.global.GlobalState;
import fr.emmanuel.loisance.androidstarter.service.APIService;
import retrofit.Call;
import retrofit.Callback;
import retrofit.GsonConverterFactory;
import retrofit.Response;
import retrofit.Retrofit;

public class AccountFragment extends Fragment implements View.OnClickListener {

    private static final String TAG = "AccountFragment";
    private GlobalState gs;
    private GoogleApiClient mGoogleApiClient;
    private User user;
    private Validator mValidator;
    ProgressBar mProgressBar;

    @NotEmpty(messageResId = R.string.app_input_required)
    EditText inputAccountLastname;

    @NotEmpty(messageResId = R.string.app_input_required)
    EditText inputAccountFirstname;

    @NotEmpty(messageResId = R.string.app_input_required)
    @Email(messageResId = R.string.app_input_email_invalid)
    EditText inputAccountEmail;

    EditText inputAccountPhone;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(true);

        gs = (GlobalState) getActivity().getApplication();
        user = gs.getUser();

        mGoogleApiClient = new GoogleApiClient.Builder(getActivity())
                .addApi(Plus.API)
                .addScope(Plus.SCOPE_PLUS_LOGIN)
                .build();

        mValidator = new Validator(this);
        mValidator.setValidationListener(new Validator.ValidationListener() {
            @Override
            public void onValidationSucceeded() {
                if (!gs.isNetworkAvailable(getActivity())) return;
                mProgressBar.setVisibility(View.VISIBLE);

                Gson gson = new GsonBuilder()
                        .setDateFormat("yyyy-MM-dd")
                        .create();

                Retrofit retrofit = new Retrofit.Builder()
                        .baseUrl(Constants.URL_API)
                        .addConverterFactory(GsonConverterFactory.create(gson))
                        .build();

                APIService api = retrofit.create(APIService.class);
                Call<User> call = api.updateUser(user.getId(), inputAccountFirstname.getText().toString(), inputAccountLastname.getText().toString(), inputAccountEmail.getText().toString(), inputAccountPhone.getText().toString());

                call.enqueue(new Callback<User>() {
                    @Override
                    public void onResponse(Response<User> response, Retrofit retrofit) {
                        switch (response.code()) {
                            case 200:
                                Log.d(TAG, "User updated successfully");
                                Log.d(TAG, "New User: " + response.body().toString());
                                Toast.makeText(getContext(), getResources().getString(R.string.account_message_update_ok), Toast.LENGTH_LONG).show();
                                gs.setUser(response.body());
                                break;
                            case 400:
                                Log.d(TAG, "User can't be updated");
                                Toast.makeText(getContext(), getResources().getString(R.string.account_message_error, 400), Toast.LENGTH_LONG).show();
                                break;
                            case 404:
                                Log.d(TAG, "User to update Not found");
                                Toast.makeText(getContext(), getResources().getString(R.string.account_message_error, 404), Toast.LENGTH_LONG).show();
                                break;
                        }
                        mProgressBar.setVisibility(View.INVISIBLE);
                    }

                    @Override
                    public void onFailure(Throwable t) {
                        Log.d(TAG, "connection fail: " + t.getMessage() + t.getCause().getMessage());
                        Toast.makeText(getContext(), getResources().getString(R.string.account_message_failure), Toast.LENGTH_LONG).show();
                        mProgressBar.setVisibility(View.INVISIBLE);
                    }
                });
            }

            @Override
            public void onValidationFailed(List<ValidationError> errors) {
                for (ValidationError error : errors) {
                    View view = error.getView();
                    String message = error.getCollatedErrorMessage(getContext());
                    if (view instanceof EditText) {
                        Drawable drawable = getResources().getDrawable(R.drawable.ic_error_24dp);
                        drawable.setBounds(0, 0, drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight());
                        ((EditText) view).setError(message, drawable);
                    } else {
                        Toast.makeText(getContext(), message, Toast.LENGTH_LONG).show();
                    }
                }
            }
        });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.account_fragment, container, false);

        this.inputAccountLastname = (EditText) rootView.findViewById(R.id.account_input_lastname);
        this.inputAccountFirstname = (EditText) rootView.findViewById(R.id.account_input_firstname);
        this.inputAccountEmail = (EditText) rootView.findViewById(R.id.account_input_email);
        this.inputAccountPhone = (EditText) rootView.findViewById(R.id.account_input_phone);
        Button mSignOutButton = (Button) rootView.findViewById(R.id.account_button_logout);

        inputAccountLastname.setText(user.getLastname());
        inputAccountFirstname.setText(user.getFirstname());
        inputAccountEmail.setText(user.getEmail());
        inputAccountPhone.setText(user.getPhone());

        if(user.getProvider().equals("google")) {
            inputAccountLastname.setEnabled(false);
            inputAccountFirstname.setEnabled(false);
            inputAccountEmail.setEnabled(false);
        }

        mProgressBar = (ProgressBar) rootView.findViewById(R.id.progressBarAccount);
        mSignOutButton.setOnClickListener(this);

        return rootView;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_account, menu);

        if(user.getProvider().equals("google")) {
            MenuItem itemUpdatePassword = menu.findItem(R.id.menu_account_update_password);
            itemUpdatePassword.setEnabled(false);
        }

        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if(id == R.id.menu_account_save) {
            submitData(getActivity());
        } else if(id == R.id.menu_account_update_password) {
            Intent intent = new Intent(getActivity(), UpdatePasswordActivity.class);
            startActivity(intent);
        } else if(id == R.id.menu_account_delete_account) {
            dialogDeleteAccount();
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onStart() {
        super.onStart();
        mGoogleApiClient.connect();
    }

    @Override
    public void onStop() {
        super.onStop();
        if(mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }
    }

    @Override
    public void onClick(View view) {
        if(view.getId() == R.id.account_button_logout) {
            onSignOutClicked();
        }
    }

    /**
     * Check data in form are ok
     * and send them to the server with REST API
     */
    private void submitData(Activity activity) {
        // remove focus
        inputAccountLastname.clearFocus();
        inputAccountFirstname.clearFocus();
        inputAccountEmail.clearFocus();
        inputAccountPhone.clearFocus();
        // close keyboard
        View view = activity.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
        // check input's
        mValidator.validate();
    }

    /**
     * Display dialog to confirm delete account action
     */
    private void dialogDeleteAccount() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(getResources().getString(R.string.account_dialog_title));
        builder.setMessage(getResources().getString(R.string.account_dialog_message));
        builder.setPositiveButton(getResources().getString(R.string.account_dialog_positive), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                deleteAccount();
            }
        });
        builder.setNegativeButton(getResources().getString(R.string.account_dialog_negative), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        builder.show();
    }

    /**
     * Call API to delete account
     */
    private void deleteAccount() {
        if (!gs.isNetworkAvailable(getActivity())) return;
        mProgressBar.setVisibility(View.VISIBLE);

        Gson gson = new GsonBuilder()
                .setDateFormat("yyyy-MM-dd")
                .create();

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(Constants.URL_API)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build();

        APIService api = retrofit.create(APIService.class);
        Call<User> call = api.deleteUser(user.getId());

        call.enqueue(new Callback<User>() {
            @Override
            public void onResponse(Response<User> response, Retrofit retrofit) {
                switch (response.code()) {
                    case 200:
                        Log.d(TAG, "User deleted successfully");
                        Toast.makeText(getContext(), getResources().getString(R.string.account_message_delete_ok), Toast.LENGTH_LONG).show();
                        onSignOutClicked();
                        break;
                    case 400:
                        Log.d(TAG, "User can't be delete");
                        Toast.makeText(getContext(), getResources().getString(R.string.account_message_error, 400), Toast.LENGTH_LONG).show();
                        break;
                    case 404:
                        Log.d(TAG, "User to delete Not found");
                        Toast.makeText(getContext(), getResources().getString(R.string.account_message_error, 404), Toast.LENGTH_LONG).show();
                        break;
                }
                mProgressBar.setVisibility(View.INVISIBLE);
            }

            @Override
            public void onFailure(Throwable t) {
                Log.d(TAG, "connection fail: " + t.getMessage() + t.getCause().getMessage());
                Toast.makeText(getContext(), getResources().getString(R.string.account_message_failure), Toast.LENGTH_LONG).show();
                mProgressBar.setVisibility(View.INVISIBLE);
            }
        });

    }

    /**
     * Logout user from default or from google
     */
    private void onSignOutClicked() {
        if(user.getProvider().equals("google") && mGoogleApiClient.isConnected()) {
            mGoogleApiClient.clearDefaultAccountAndReconnect();
            mGoogleApiClient.disconnect();
            gs.setIsConnected(false);
            Intent intent = new Intent(getActivity(), LoginActivity.class);
            startActivity(intent);
            getActivity().finish();
        } else if(user.getProvider().equals("default")) {
            gs.setIsConnected(false);
            Intent intent = new Intent(getActivity(), LoginActivity.class);
            startActivity(intent);
            getActivity().finish();
        }
    }

}
