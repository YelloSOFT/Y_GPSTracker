package ru.yellosoft_club.y_gpstracker;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import static ru.yellosoft_club.y_gpstracker.R.id.et_email;

public class authorization extends AppCompatActivity implements View.OnClickListener {

    private FirebaseAuth mAuth;
    private FirebaseAuth.AuthStateListener mAuthListener;
    private static final String TAG = "EmailPassword";
    private EditText ETemail;
    private EditText ETpassword;


    boolean cancel = false;
    View focuscView = null;

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

        findViewById(R.id.btn_sign_in).setOnClickListener(this);
        findViewById(R.id.btn_registration).setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        if (TextUtils.isEmpty(ETemail.getText())) {
            ETemail.setError(("Введите Email"));
            return;
        }
        if (TextUtils.isEmpty(ETpassword.getText())) {
            ETemail.setError(("Введите Пароль"));
            return;
        }

        if (view.getId() == R.id.btn_sign_in) {
            signin(ETemail.getText().toString(), ETpassword.getText().toString());
        } else if (view.getId() == R.id.btn_registration) {
            registration(ETemail.getText().toString(), ETpassword.getText().toString());
        }
    }

    public void signin(String email, String password) {
        mAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    Toast.makeText(authorization.this, "Aвторизация успешна", Toast.LENGTH_SHORT).show();

                    Intent intent = new Intent(authorization.this, main_settings.class);
                    startActivity(intent);
                } else {
                    Toast.makeText(authorization.this, "Ошибка! Вы не авторизированы!", Toast.LENGTH_SHORT).show();
                }
            }

        });
    }

    public void registration(String email, String password) {
        mAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
            @Override
            public void onComplete(@NonNull Task<AuthResult> task) {
                if (task.isSuccessful()) {
                    Toast.makeText(authorization.this, "Регистрация прошла успешно", Toast.LENGTH_SHORT).show();

                    Intent intent = new Intent(authorization.this, main_settings.class);
                    startActivity(intent);
                } else
                    Toast.makeText(authorization.this, "Ошибка! Введите корректные данные", Toast.LENGTH_SHORT).show();
            }
        });

                //Вывести про непр=
       // MapFragment
    }
}