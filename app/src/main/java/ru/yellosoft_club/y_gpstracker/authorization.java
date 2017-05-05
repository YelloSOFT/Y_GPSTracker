package ru.yellosoft_club.y_gpstracker;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import pl.droidsonroids.gif.GifImageView;

import static ru.yellosoft_club.y_gpstracker.R.id.et_email;

public class authorization extends AppCompatActivity implements View.OnClickListener {

    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private static final String TAG = "EmailPassword";
    private EditText ETemail;
    private EditText ETpassword;
    private TextView load;
    private GifImageView gif;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_authorization);

        mAuth = FirebaseAuth.getInstance();

        mAuthListener = new FirebaseAuth.AuthStateListener() {
            @Override
            public void onAuthStateChanged(@NonNull FirebaseAuth firebaseAuth) {
                FirebaseUser user = firebaseAuth.getCurrentUser();
                if (user != null) {
                    Log.d(TAG, "onAuthStateChanged:signed_in:" + user.getUid());

                } else {
                    Log.d(TAG, "onAuthStateChanged:signed_out");
                }
                // [START_EXCLUDE]
                // updateUI(user);
                // [END_EXCLUDE]
            }
        };

        ETemail = (EditText) findViewById(et_email);
        ETpassword = (EditText) findViewById(R.id.et_password);
        load = (TextView) findViewById(R.id.textView4);
        gif = (GifImageView) findViewById(R.id.gif);


        findViewById(R.id.btn_sign_in).setOnClickListener(this);
        findViewById(R.id.btn_registration).setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        if (TextUtils.isEmpty(ETemail.getText())) {
            ETemail.setError(("Введите Email"));
            load.setVisibility(View.INVISIBLE);
            gif.setVisibility(View.INVISIBLE);
            return;
        }
        if (TextUtils.isEmpty(ETpassword.getText())) {
            ETpassword.setError(("Введите Пароль"));
            load.setVisibility(View.INVISIBLE);
            gif.setVisibility(View.INVISIBLE);
            return;

        }
        ETpassword.setOnFocusChangeListener(new View.OnFocusChangeListener() {
            @Override
            public void onFocusChange(View v, boolean hasFocus) {
                if(ETpassword.getText().length() < 5)
                {
                    ETpassword.setError("Пароль слишком маленький");
                    load.setVisibility(View.INVISIBLE);
                    gif.setVisibility(View.INVISIBLE);
                }
            }
        });

        if (view.getId() == R.id.btn_sign_in) {
            signin(ETemail.getText().toString(), ETpassword.getText().toString());
        } else if (view.getId() == R.id.btn_registration) {
            registration(ETemail.getText().toString(), ETpassword.getText().toString());
        }
        //Вызовы "функций"//
        isGooglePlayServicesAvailable();
        //Вызовы "функций"//
    }

    public void signin(String email, String password) {
        mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    load.setVisibility(View.VISIBLE);
                    gif.setVisibility(View.VISIBLE);
                    Toast.makeText(authorization.this, "Aвторизация успешна", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(authorization.this, main_settings.class);
                    intent.putExtra("name", ETemail.getText().toString());
                    startActivity(intent);
                    finish();
                } else {
                    if (!hasConnection(authorization.this)) {
                        load.setVisibility(View.INVISIBLE);
                        gif.setVisibility(View.INVISIBLE);
                        AlertDialog.Builder builder = new AlertDialog.Builder(authorization.this);
                        builder.setTitle("Ошибка");
                        builder.setMessage("Проверьте подключение к сети \n Включите интернет или Wi-Fi");
                        builder.setPositiveButton("Настройки Wi-Fi", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Intent i = new Intent(Settings.ACTION_WIFI_SETTINGS);
                                int ACTION_WIFI_SETTINGS = 0;
                                startActivityForResult(i, ACTION_WIFI_SETTINGS);
                            }
                        });
                        builder.setNegativeButton("Ok", null);
                        builder.create().show();
                        } else {
                        load.setVisibility(View.INVISIBLE);
                        gif.setVisibility(View.INVISIBLE);
                        Toast.makeText(authorization.this, "Ошибка! Вы не авторизированы!", Toast.LENGTH_SHORT).show();
                    }
                }
            }

        });
    }

    public void registration(final String email, String password) {
        mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    load.setVisibility(View.VISIBLE);
                    gif.setVisibility(View.VISIBLE);
                    Toast.makeText(authorization.this, "Регистрация прошла успешно", Toast.LENGTH_SHORT).show();
                    Intent intent = new Intent(authorization.this, main_settings.class);
                    intent.putExtra("name", ETemail.getText().toString());
                    startActivity(intent);
                    finish();
                } else
                    load.setVisibility(View.INVISIBLE);
                    gif.setVisibility(View.INVISIBLE);
                    Toast.makeText(authorization.this, "Ошибка! Введите корректные данные", Toast.LENGTH_SHORT).show();
            }
        });
    }
    //Проверка на интеренет соединение
    public static boolean hasConnection(final Context context)
    {
        ConnectivityManager cm = (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo wifiInfo = cm.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        if (wifiInfo != null && wifiInfo.isConnected())
        {
            return true;
        }
        wifiInfo = cm.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
        if (wifiInfo != null && wifiInfo.isConnected())
        {
            return true;
        }
        wifiInfo = cm.getActiveNetworkInfo();
        if (wifiInfo != null && wifiInfo.isConnected())
        {
            return true;
        }
        return false;
    }

    //Проверка на Google сервисы
    private  boolean isGooglePlayServicesAvailable() {
        int status = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        if (ConnectionResult.SUCCESS == status) {
            return true;
        } else {
            GooglePlayServicesUtil.getErrorDialog(status, this, 0).show();
            Toast.makeText(authorization.this, "Google Play services - Отсутствуют! \n Программа будет работать не корректно!", Toast.LENGTH_SHORT).show();
            AlertDialog.Builder builder = new AlertDialog.Builder(authorization.this);
            builder.setTitle("Внимание!");
            builder.setMessage("Google Play services - Отсутствуют! \n Программа будет работать не корректно!");
            builder.setNegativeButton("Ok", null);
            builder.create().show();
            return false;
        }
    }
}