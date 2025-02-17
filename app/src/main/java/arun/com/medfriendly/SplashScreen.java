package arun.com.medfriendly;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;

import com.crashlytics.android.Crashlytics;
import com.facebook.shimmer.ShimmerFrameLayout;
import com.google.firebase.messaging.FirebaseMessaging;

import io.fabric.sdk.android.Fabric;
import utilities.Config;
import utilities.Globalpreferences;
import utilities.NotificationUtils;

/**
 * Created by arun_i on 26-Jul-17.
 */

public class SplashScreen extends AppCompatActivity {
    private BroadcastReceiver mRegistrationBroadcastReceiver;
    private Globalpreferences globalpreferences;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Fabric.with(this, new Crashlytics());
        setContentView(R.layout.splashscreen);
        globalpreferences = Globalpreferences.getInstances(SplashScreen.this);
        ShimmerFrameLayout layout = (ShimmerFrameLayout) findViewById(R.id.shimmerlayout);
        layout.startShimmerAnimation();
        registerFCM();
        Thread thread = new Thread() {
            public void run() {
                try {
                    sleep(3000);
                    if(globalpreferences.getBoolean("isFirstLaunch")){
                        startActivity(new Intent(SplashScreen.this, WelcomeActivity.class));
                        globalpreferences.putBoolean("isFirstLaunch",false);

                    }else {
                        if (!TextUtils.isEmpty(globalpreferences.getString("username"))) {
                            startActivity(new Intent(SplashScreen.this, MainNavigationDrawer.class));
                        } else {
                            startActivity(new Intent(SplashScreen.this, Login.class));
                        }
                    }
                    finish();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        };
        thread.start();
        //throw new RuntimeException("this is a crash");
    }


    /**
     * Get registration token from receiver
     */
    private void registerFCM() {
        mRegistrationBroadcastReceiver = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                // checking for type intent filter
                if (intent.getAction().equals(Config.REGISTRATION_COMPLETE)) {
                    // gcm successfully registered
                    // now subscribe to `global` topic to receive app wide notifications
                    FirebaseMessaging.getInstance().subscribeToTopic(Config.TOPIC_GLOBAL);
                    displayFirebaseRegId();
                } else if (intent.getAction().equals(Config.PUSH_NOTIFICATION)) {
                    // new push notification is received
                    String message = intent.getStringExtra("message");
                }
            }
        };
    }

    /**
     * Get firebase id
     */
    private void displayFirebaseRegId() {
        SharedPreferences pref = getApplicationContext().getSharedPreferences(Config.SHARED_PREF, 0);
        String regId = pref.getString("regId", null);
        System.out.println("firebaseId::" + regId);
    }


    @Override
    protected void onPause() {
        super.onPause();
        LocalBroadcastManager.getInstance(this).unregisterReceiver(mRegistrationBroadcastReceiver);
    }


    @Override
    protected void onResume() {
        super.onResume();
        LocalBroadcastManager.getInstance(this).registerReceiver(mRegistrationBroadcastReceiver,
                new IntentFilter(Config.REGISTRATION_COMPLETE));
        // register new push message receiver
        // by doing this, the activity will be notified each time a new message arrives
        LocalBroadcastManager.getInstance(this).registerReceiver(mRegistrationBroadcastReceiver,
                new IntentFilter(Config.PUSH_NOTIFICATION));
        // clear the notification area when the app is opened
        NotificationUtils.clearNotifications(getApplicationContext());
    }



}
