package mx.com.desoft.hidrogas.scm;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;

public class SplashActivity extends AppCompatActivity {

    private final int DURACION_SPLASH = 3000; // 3 segundos

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent inicio = new Intent(SplashActivity.this, MainActivity.class);
                startActivity(inicio);
                finish();

            }
        },DURACION_SPLASH);
    }
}
