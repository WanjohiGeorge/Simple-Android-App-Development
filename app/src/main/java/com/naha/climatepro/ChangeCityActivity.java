package com.naha.climatepro;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

public class ChangeCityActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.change_weather_city_name);

        final EditText cityName = (EditText)findViewById(R.id.queryET);
        final ImageButton backButton = (ImageButton)findViewById(R.id.backbtn);
        backButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });
        //on enter key press
        cityName.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                String cityEntered = cityName.getText().toString();

                //create new intent to navigate back to main activity
                Intent intent = new Intent(ChangeCityActivity.this,MainActivity.class);
                intent.putExtra("city",cityEntered);
                startActivity(intent);
                return false;
            }
        });
    }
}
