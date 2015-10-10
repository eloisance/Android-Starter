package fr.emmanuel.loisance.androidstarter.global;

import android.app.Application;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.widget.Toast;

import fr.emmanuel.loisance.androidstarter.R;
import fr.emmanuel.loisance.androidstarter.classe.User;

public class GlobalState extends Application {

    private boolean isConnected;
    private User user;

    public GlobalState() {
        this.isConnected = false;
    }

    public void setIsConnected(boolean isConnected) {
        this.isConnected = isConnected;
    }

    public boolean getIsConnected() {
        return this.isConnected;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    /**
     * Check network connection
     * @param context to display toast
     * @return false if not connected
     */
    public boolean isNetworkAvailable(Context context) {
        ConnectivityManager cm = (ConnectivityManager)getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo activeNetwork = cm.getActiveNetworkInfo();
        boolean isConnected = activeNetwork != null && activeNetwork.isConnectedOrConnecting();
        if(!isConnected) {
            Toast.makeText(context, R.string.app_check_network, Toast.LENGTH_LONG).show();
            return false;
        }
        return true;
    }

}
