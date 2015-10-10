package fr.emmanuel.loisance.androidstarter.global;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

import fr.emmanuel.loisance.androidstarter.R;
import fr.emmanuel.loisance.androidstarter.activity.LoginActivity;

public class SplashScreen extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.splash_screen);

        GlobalState gs = (GlobalState) getApplication();

        Handler handler = new Handler();
        final Runnable r = new Runnable() {
            public void run() {
                Intent intent = new Intent(SplashScreen.this, LoginActivity.class);
                SplashScreen.this.startActivity(intent);
                SplashScreen.this.finish();
            }
        };
        handler.postDelayed(r, Constants.SPLASH_TIME);

    }

}
