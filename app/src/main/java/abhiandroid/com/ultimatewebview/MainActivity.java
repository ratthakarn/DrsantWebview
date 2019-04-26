package abhiandroid.com.ultimatewebview;

import android.content.BroadcastReceiver;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.Snackbar;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.google.firebase.analytics.FirebaseAnalytics;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import abhiandroid.com.ultimatewebview.Fragments.WebViewFragment;


public class MainActivity extends AppCompatActivity implements View.OnClickListener {


    RecyclerView recyclerView;
    ImageView menuHomeImage;
    public static DrawerLayout drawerLayout;
    public static List<String> menuTitles;
    public static ArrayList<Integer> menuIcons = new ArrayList<>(Arrays.asList(R.drawable.home_icon, R.drawable.star_icon, R.drawable.service_icon, R.drawable.support_icon, R.drawable.about_icon, R.drawable.contact_icon));
    public static CustomDrawerAdapter customDrawerAdapter;
    private AdView mAdView;
    public static ImageView menu, share;
    public static TextView title;
    private BroadcastReceiver mRegistrationBroadcastReceiver;
    boolean doubleBackToExitPressedOnce = false;
    private FirebaseAnalytics mFirebaseAnalytics;
    RelativeLayout rel_layout;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_main);
        //ButterKnife.bind(this);
        rel_layout=(RelativeLayout)findViewById(R.id.rel_layout);
        recyclerView=(RecyclerView)findViewById(R.id.recyclerview);
        menu=(ImageView)findViewById(R.id.menu);
        menuHomeImage=(ImageView)findViewById(R.id.menuHomeImage);
        menuHomeImage.setOnClickListener(this);
        menu.setOnClickListener(this);
        // Obtain the FirebaseAnalytics instance.
        mFirebaseAnalytics = FirebaseAnalytics.getInstance(this);
        menuTitles = Arrays.asList(getResources().getStringArray(R.array.menuArray));
        title = (TextView) findViewById(R.id.title);
        menu = (ImageView) findViewById(R.id.menu);
        share = (ImageView) findViewById(R.id.share);
        mAdView = (AdView) findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder()
                .addTestDevice("0EE7E9267C3B18C46C76B07B7DE04FBE")
                .build();
        mAdView.loadAd(adRequest);
        drawerLayout = (DrawerLayout) findViewById(R.id.drawer_layout);
        Bundle bundle = new Bundle();
        WebViewFragment webViewFragment = new WebViewFragment();
        bundle.putString("url", Config.homeUrl);
        webViewFragment.setArguments(bundle);
        loadFragment(webViewFragment, false,"webViewFragment");
        setRecyclerData();
        try {
            Intent intent = getIntent();
            String message = intent.getStringExtra("message");
            String url = intent.getStringExtra("url");
            Log.d("notification Data", message + url);
            if (url.length() > 0) {
                bundle.putString("url", url);
                webViewFragment.setArguments(bundle);
                loadFragment(webViewFragment, false,"webViewFragment");

            }
        } catch (Exception e) {
            Log.d("error notification data", e.toString());
        }
        getWindow().setSoftInputMode(
                WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        displayFirebaseRegId();
    }


    private void displayFirebaseRegId() {
        SharedPreferences pref = getApplicationContext().getSharedPreferences(Config.SHARED_PREF, 0);
        String regId = pref.getString("regId", null);

        Log.e("FCM", "Firebase reg id: " + regId);

        if (!TextUtils.isEmpty(regId)) {
//            txtRegId.setText("Firebase Reg Id: " + regId);
        } else
            Log.d("Firebase", "Firebase Reg Id is not received yet!");
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mAdView != null) {
            mAdView.resume();
        }
    }

    @Override
    protected void onPause() {
        if (mAdView != null) {
            mAdView.pause();
        }
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mRegistrationBroadcastReceiver);
        super.onPause();
    }


    @Override
    public void onDestroy() {
        if (mAdView != null) {
            mAdView.destroy();
        }
        super.onDestroy();
    }

    private void setRecyclerData() {

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(MainActivity.this);
        recyclerView.setLayoutManager(linearLayoutManager);
        customDrawerAdapter = new CustomDrawerAdapter(MainActivity.this, menuTitles, menuIcons);
        recyclerView.setAdapter(customDrawerAdapter);
    }



    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.menuHomeImage:
                drawerLayout.closeDrawers();
                CustomDrawerAdapter.selected_item = 0;
                customDrawerAdapter.notifyDataSetChanged();
                title.setText(menuTitles.get(0));
                Bundle bundle = new Bundle();
                WebViewFragment webViewFragment = new WebViewFragment();
                bundle.putString("url", Config.homeUrl);
                webViewFragment.setArguments(bundle);
                loadFragment(webViewFragment, false,"webViewFragment");

                break;
            case R.id.menu:
                if (!MainActivity.drawerLayout.isDrawerOpen(Gravity.LEFT)) {
                    MainActivity.drawerLayout.openDrawer(Gravity.LEFT);
                }
                break;
        }
    }

    @Override
    public void onBackPressed() {

        Fragment fragment = getSupportFragmentManager().findFragmentByTag("webViewFragment");
        if(fragment!=null && fragment instanceof WebViewFragment){
            if(((WebViewFragment) fragment).onBackPressed()){
                if (doubleBackToExitPressedOnce) {
                    super.onBackPressed();
                    return;
                }

                this.doubleBackToExitPressedOnce = true;
                // Toast.makeText(this, "Press back once more to exit", Toast.LENGTH_SHORT).show();
                Snackbar snackbar = Snackbar
                        .make(rel_layout, "Press back once more to exit", Snackbar.LENGTH_LONG);
                snackbar.setActionTextColor(Color.RED);
                View snackbarView = snackbar.getView();
                snackbarView.setBackgroundColor(Color.DKGRAY);
                TextView textView = (TextView) snackbarView.findViewById(android.support.design.R.id.snackbar_text);
                textView.setTextColor(Color.YELLOW);
                snackbar.show();

                new Handler().postDelayed(new Runnable() {

                    @Override
                    public void run() {
                        doubleBackToExitPressedOnce = false;
                    }
                }, 2000);
            }
        }

    }
    public void loadFragment(Fragment fragment, Boolean bool) {
        loadFragment(fragment, bool,null);
    }
    public void loadFragment(Fragment fragment, Boolean bool, String TAG) {
        showAds();
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        if(TAG == null) {
            transaction.replace(R.id.frameLayout, fragment);
        }else {
            transaction.replace(R.id.frameLayout,fragment,TAG);
        }
        if (bool)
            transaction.addToBackStack(null);
        transaction.commit();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent){

        Fragment fragment = getSupportFragmentManager().findFragmentByTag("webViewFragment");
        if(fragment!=null && fragment instanceof WebViewFragment){
            ((WebViewFragment)fragment).onActivityResult(requestCode, resultCode, intent);
        }
        super.onActivityResult(requestCode, resultCode, intent);

    }

    public void hideAds(){
        mAdView.setVisibility(View.GONE);
    }

    public void showAds(){
        mAdView.setVisibility(View.VISIBLE);
    }
}