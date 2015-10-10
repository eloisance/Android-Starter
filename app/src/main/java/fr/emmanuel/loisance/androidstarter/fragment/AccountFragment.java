package fr.emmanuel.loisance.androidstarter.fragment;

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
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.plus.Plus;

import fr.emmanuel.loisance.androidstarter.R;
import fr.emmanuel.loisance.androidstarter.activity.LoginActivity;
import fr.emmanuel.loisance.androidstarter.classe.User;
import fr.emmanuel.loisance.androidstarter.global.GlobalState;

public class AccountFragment extends Fragment implements View.OnClickListener {

    private static final String TAG = "AccountFragment";

    private GlobalState gs;
    private User user;

    private GoogleApiClient mGoogleApiClient;

    private TextView txtAccountName;
    private TextView txtAccountEmail;
    private TextView txtAccountPhone;
    private Button mSignOutButton;

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

        txtAccountName = (TextView) rootView.findViewById(R.id.txtName);
        txtAccountEmail = (TextView) rootView.findViewById(R.id.txtEmail);
        txtAccountPhone = (TextView) rootView.findViewById(R.id.txtPhone);

        txtAccountName.setText(user.getDisplayName());
        txtAccountEmail.setText(user.getEmail());

        mSignOutButton = (Button) rootView.findViewById(R.id.sign_out_button);
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
        if(view.getId() == R.id.sign_out_button) {
            Log.d(TAG, "onClick sign out button");
            onSignOutClicked();
        }
    }

    private void onSignOutClicked() {
        if(user.getProvider().equals("google") && mGoogleApiClient.isConnected()) {
            mGoogleApiClient.clearDefaultAccountAndReconnect();
            mGoogleApiClient.disconnect();
            gs.setIsConnected(false);
            Intent intent = new Intent(getActivity(), LoginActivity.class);
            startActivity(intent);
            getActivity().finish();
        }
        if(user.getProvider().equals("default")) {
            gs.setIsConnected(false);
            Intent intent = new Intent(getActivity(), LoginActivity.class);
            startActivity(intent);
            getActivity().finish();
        }
    }


}
