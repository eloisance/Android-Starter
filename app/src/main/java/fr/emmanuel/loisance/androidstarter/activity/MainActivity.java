package fr.emmanuel.loisance.androidstarter.activity;

import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.NavigationView;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import de.hdodenhof.circleimageview.CircleImageView;
import fr.emmanuel.loisance.androidstarter.R;
import fr.emmanuel.loisance.androidstarter.classe.User;
import fr.emmanuel.loisance.androidstarter.fragment.AccountFragment;
import fr.emmanuel.loisance.androidstarter.fragment.MainFragment;
import fr.emmanuel.loisance.androidstarter.global.GlobalState;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    private GlobalState gs;
    private User user;

    private Toolbar mToolbar;
    private DrawerLayout mDrawerLayout;
    private NavigationView mNavigationView;
    private ActionBarDrawerToggle mActionBarDrawerToggle;
    private MenuItem mPreviousItem;
    private FloatingActionButton mFloatingActionButton;

    private TextView username;
    private TextView email;
    private CircleImageView profileImage;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        gs = (GlobalState) getApplication();
        this.user = gs.getUser();

        final View coordinatorLayoutView = findViewById(R.id.coordinator_layout);

        username = (TextView) findViewById(R.id.username);
        email = (TextView) findViewById(R.id.email);
        profileImage = (CircleImageView) findViewById(R.id.profile_image);

        mToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(mToolbar);

        mDrawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        mActionBarDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout, mToolbar, R.string.openDrawer, R.string.closeDrawer) {
            @Override
            public void onDrawerOpened(View drawerView) { super.onDrawerOpened(drawerView); }
            @Override
            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);
            }
        };
        mDrawerLayout.setDrawerListener(mActionBarDrawerToggle);
        mActionBarDrawerToggle.syncState();

        mNavigationView = (NavigationView) findViewById(R.id.navigation_view);
        mNavigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(MenuItem menuItem) {
                if(menuItem == mPreviousItem) return false;
                menuItem.setCheckable(true);
                menuItem.setChecked(true);

                // Switch fragment to display
                switch (menuItem.getItemId()) {
                    case R.id.drawer_menu_account:
                        if(gs.getIsConnected()) {
                            setFragment(new AccountFragment(), (String) menuItem.getTitle());
                        } else {
                            mPreviousItem.setChecked(true);
                            menuItem.setChecked(false);
                            makeSnackbarNotConnected(coordinatorLayoutView);
                        }
                        break;
                    case R.id.drawer_menu_main:
                        setFragment(new MainFragment(), (String) menuItem.getTitle());
                        break;
                    case R.id.drawer_menu_settings:
                        break;
                    case R.id.drawer_menu_help:
                        break;
                    default:
                        break;
                }

                // Update mPreviousItem if menu can be access without connection
                if(mPreviousItem != null) {
                    mPreviousItem.setChecked(false);
                    if(menuItem.getTitle().equals(getResources().getString(R.string.menu_account)) && !gs.getIsConnected()) mPreviousItem.setChecked(true);
                    else mPreviousItem = menuItem;
                }

                mDrawerLayout.closeDrawers();
                return true;
            }
        });

        mFloatingActionButton = (FloatingActionButton)findViewById(R.id.fab);
        mFloatingActionButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(gs.getIsConnected()) {
                    // do something
                }
                else {
                    makeSnackbarNotConnected(coordinatorLayoutView);
                }
            }
        });

        // Update user informations in nav drawer
        if(gs.getIsConnected()) {
            username.setText(this.user.getDisplayName());
            email.setText(user.getEmail());
            //profileImage.setImageBitmap(gs.getProfileImage());
        } else {
            username.setText(getResources().getString(R.string.menu_header_username));
            email.setText(getResources().getString(R.string.menu_header_email));
            profileImage.setImageResource(R.drawable.ic_account_white_24dp);
        }

        // Default: AccountFragment
        mPreviousItem = mNavigationView.getMenu().getItem(1);
        mNavigationView.getMenu().getItem(1).setCheckable(true);
        mNavigationView.getMenu().getItem(1).setChecked(true);
        Fragment ListFragment = new MainFragment();
        setFragment(ListFragment, getResources().getString(R.string.menu_main));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onStart() {
        super.onStart();
    }

    @Override
    protected void onStop() {
        super.onStop();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        getSupportFragmentManager().findFragmentById(R.id.content_frame).onActivityResult(requestCode, resultCode, intent);
    }

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

    /**
     * Prepare new fragment
     * @param fragment New fragment to display
     * @param title Title of new fragment
     */
    public void setFragment(Fragment fragment, String title) {
        setTitle(title);

        FragmentManager fragmentManager = getSupportFragmentManager();
        fragmentManager.beginTransaction()
                .replace(R.id.content_frame, fragment)
                .commit();

        if(title.equals(getResources().getString(R.string.menu_main))) {
            mFloatingActionButton.setVisibility(View.VISIBLE);
        } else {
            mFloatingActionButton.setVisibility(View.INVISIBLE);
        }
    }

    /**
     * Display SnackBar
     * @param coordinatorLayoutView Coordinator layout of snackbar
     */
    public void makeSnackbarNotConnected(View coordinatorLayoutView) {
        Snackbar.make(coordinatorLayoutView, R.string.snackbar_not_connected, Snackbar.LENGTH_LONG)
                .setAction(R.string.snackbar_ok, new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        Intent intent = new Intent(MainActivity.this, LoginActivity.class);
                        startActivity(intent);
                        finish();
                    }
                })
                .setActionTextColor(getResources().getColor(R.color.ColorPrimary))
                .show();
    }

}
