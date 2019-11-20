package com.example.fatec_ipi_noite_pojeto_p1;

import androidx.appcompat.app.AppCompatActivity;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.widget.ImageView;

public class DisplayImageActivity extends AppCompatActivity {

    private ImageView displayImageView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_display_image);

        displayImageView = findViewById(R.id.displayImageView);

        byte[] byteArray = getIntent().getByteArrayExtra("figura");
        Bitmap figura = BitmapFactory.decodeByteArray(byteArray, 0, byteArray.length);

        displayImageView.setImageBitmap(figura);

    }
}
