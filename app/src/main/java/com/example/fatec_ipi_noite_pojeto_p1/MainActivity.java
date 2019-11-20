package com.example.fatec_ipi_noite_pojeto_p1;

import androidx.appcompat.app.AppCompatActivity;

import android.app.ActivityOptions;
import android.content.Intent;
import android.os.Bundle;
import android.widget.ImageButton;

public class MainActivity extends AppCompatActivity {

    private ImageButton homeButton;
    private ImageButton textButton;
    private ImageButton imageButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        homeButton = findViewById(R.id.homeButton);
        textButton = findViewById(R.id.textButton);
        imageButton = findViewById(R.id.imageButton);

        homeButton.setBackgroundColor(getColor(R.color.colorMenuActive));

        textButton.setOnClickListener((view) -> {

            Intent intent = new Intent(MainActivity.this,  TextShareActivity.class);
            startActivity(intent, ActivityOptions.makeSceneTransitionAnimation(MainActivity.this).toBundle());

        });

        imageButton.setOnClickListener(view ->{

            Intent intent = new Intent(MainActivity.this,  ImageShareActivity.class);
            startActivity(intent);

        });

    }
}
