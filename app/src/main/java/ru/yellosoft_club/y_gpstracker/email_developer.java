package ru.yellosoft_club.y_gpstracker;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;

public class email_developer extends AppCompatActivity {
    private Button buttonSend;
    private EditText textTo;
    private EditText textSubject;
    private EditText textMessage;
    private ImageView img;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_email_developer);

        buttonSend = (Button) findViewById(R.id.buttonSend);
        textTo = (EditText) findViewById(R.id.editTextTo);
        textSubject = (EditText) findViewById(R.id.editTextSubject);
        textMessage = (EditText) findViewById(R.id.editTextMessage);
        img = (ImageView) findViewById(R.id.imageView1);

        buttonSend.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                if (TextUtils.isEmpty(textSubject.getText())) {
                    textSubject.setError(("Введите тему"));
                    return;
                }
                if (TextUtils.isEmpty(textMessage.getText())) {
                    textMessage.setError(("Введите сообщение"));
                    return;
                }
                String to = textTo.getText().toString();
                String subject = textSubject.getText().toString();
                String message = textMessage.getText().toString();

                Intent email = new Intent(Intent.ACTION_SEND);
                email.putExtra(Intent.EXTRA_EMAIL, new String[]{ to});
                email.putExtra(Intent.EXTRA_SUBJECT, subject);
                email.putExtra(Intent.EXTRA_TEXT, message);

                //для того чтобы запросить email клиент устанавливаем тип
                email.setType("message/rfc822");
                img.setVisibility(View.VISIBLE);
                startActivity(Intent.createChooser(email, "Выберите email клиент :"));

            }
        });

    }
}


