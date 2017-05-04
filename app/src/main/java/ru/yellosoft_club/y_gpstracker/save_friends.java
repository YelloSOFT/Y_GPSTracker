package ru.yellosoft_club.y_gpstracker;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.provider.Settings;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;

import static android.support.test.InstrumentationRegistry.getContext;

public class save_friends extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_save_friends);

        ListView listView = (ListView) findViewById(R.id.listView);
        final EditText editText = (EditText) findViewById(R.id.editText);

        // Создаём пустой массив для хранения
        final ArrayList<String> catnames = new ArrayList<String>();

        // Создаём адаптер ArrayAdapter, чтобы привязать массив к ListView
        final ArrayAdapter<String> adapter;
        adapter = new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_1, catnames);
        // Привяжем массив через адаптер к ListView
        listView.setAdapter(adapter);

        // Прослушиваем нажатия клавиш
        editText.setOnKeyListener(new View.OnKeyListener() {
            public boolean onKey(View v, int keyCode, KeyEvent event) {
                // TODO Auto-generated method stub
                if (event.getAction() == KeyEvent.ACTION_DOWN)
                    if (keyCode == KeyEvent.KEYCODE_ENTER) {
                        catnames.add(0, editText.getText().toString());
                        adapter.notifyDataSetChanged();
                        editText.setText("");
                        return true;
                    }
                return false;
            }
        });

        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View itemClicked, int position, long id) {
                AlertDialog.Builder builder = new AlertDialog.Builder(save_friends.this);
                builder.setTitle("Функции:");
                builder.setPositiveButton("Редактировать", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                       //Функция редактирования
                    }
                });
                builder.setNegativeButton("Удалить", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //Функция удаления
                    }
                });
                builder.create().show();
            }
        });


    }

}



