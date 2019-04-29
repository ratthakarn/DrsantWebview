package gkrgroup.drsant.ultimatewebview;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.Log;
import android.view.WindowManager;

public class SplashScreen extends Activity {


    SharedPreferences sharedPreferences;
    SharedPreferences.Editor editor;
String message="",url="";
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);
        this.getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);
        try {
            Intent intent = getIntent();
             message = intent.getStringExtra("message");
             url = intent.getStringExtra("url");
            Log.d("notification Data", message + url);
        }catch (Exception e)
        {
            Log.d("error notification data",e.toString());
        }
        Thread thread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Thread.sleep(3000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                Intent intent = new Intent(SplashScreen.this, MainActivity.class);
                intent.putExtra("message",message);
                intent.putExtra("url",url);
                startActivity(intent);
                finish();
            }
        });
        thread.start();
    }

}
