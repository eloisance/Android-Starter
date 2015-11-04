package fr.emmanuel.loisance.androidstarter.fragment;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
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

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.plus.Plus;

import fr.emmanuel.loisance.androidstarter.R;
import fr.emmanuel.loisance.androidstarter.activity.LoginActivity;
import fr.emmanuel.loisance.androidstarter.classe.User;
import fr.emmanuel.loisance.androidstarter.global.GlobalState;

public class AccountFragment extends Fragment implements View.OnClickListener {

    private static final String TAG = "AccountFragment";
    private GlobalState gs;
    private GoogleApiClient mGoogleApiClient;
    private User user;

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
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.account_fragment, container, false);

        EditText inputAccountLastname = (EditText) rootView.findViewById(R.id.account_input_lastname);
        EditText inputAccountFirstname = (EditText) rootView.findViewById(R.id.account_input_firstname);
        EditText inputAccountEmail = (EditText) rootView.findViewById(R.id.account_input_email);
        EditText inputAccountPhone = (EditText) rootView.findViewById(R.id.account_input_phone);
        Button mSignOutButton = (Button) rootView.findViewById(R.id.account_button_logout);

        inputAccountLastname.setText(user.getLastname());
        inputAccountFirstname.setText(user.getFirstname());
        inputAccountEmail.setText(user.getEmail());
        inputAccountPhone.setText(user.getPhone());

        mSignOutButton.setOnClickListener(this);

        return rootView;
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_account, menu);
        super.onCreateOptionsMenu(menu, inflater);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if(id == R.id.menu_account_save) {
            submitData(getActivity());
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
            Log.d(TAG, "onClick sign out button");
            onSignOutClicked();
        }
    }

    /**
     * Check data in form are ok
     * and send them to the server with REST API
     */
    private void submitData(Activity activity) {
        // close keyboard
        View view = activity.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
        // TODO: 04/11/2015 Check data and send them
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
