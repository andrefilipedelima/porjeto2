package com.example.fatec_ipi_noite_pojeto_p1;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.PersistableBundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatImageButton;

import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.SetOptions;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

import javax.annotation.Nullable;

public class TextShareActivity extends AppCompatActivity {

    private ImageButton homeButton;
    private ImageButton textButton;
    private ImageButton imageButton;
    private AppCompatImageButton tagButton;
    private EditText textShareEditText;
    private EditText tagEditText;

    private DocumentReference docRef;
    private CollectionReference collMensagensReference;

    private boolean tagSetada = false;
    private Date dataUltimaDigitacao;
    private boolean editouTexto =false;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.telacompartilhartexto);

        homeButton = findViewById(R.id.homeButton);
        textButton = findViewById(R.id.textButton);
        imageButton = findViewById(R.id.imageButton);
        textShareEditText = findViewById(R.id.textShareEditText);
        tagButton = findViewById(R.id.tagButton);
        tagEditText = findViewById(R.id.tagEditText);
        dataUltimaDigitacao = new Date();

        textButton.setBackgroundColor(getColor(R.color.colorMenuActive));
        textShareEditText.setEnabled(false);

        homeButton.setOnClickListener((view) -> {

            Intent intent = new Intent(TextShareActivity.this,  MainActivity.class);
            startActivity(intent);

        });

        imageButton.setOnClickListener(view ->{

            Intent intent = new Intent(TextShareActivity.this,  ImageShareActivity.class);
            startActivity(intent);

        });

        tagButton.setOnClickListener((v) ->{
            if(tagEditText.getText().length() == 0){
                Toast.makeText(this, R.string.digit_tag, Toast.LENGTH_SHORT);
                return;
            }

            if(collMensagensReference == null)
                collMensagensReference = FirebaseFirestore.getInstance().collection("TextShare");

            tagSetada = false;

            textShareEditText.setEnabled(false);
            textShareEditText.setText("");
            tagButton.clearFocus();
            esconderTeclado(v);

            docRef =null;
            docRef = collMensagensReference.document(tagEditText.getText().toString());

            docRef.addSnapshotListener(new EventListener<DocumentSnapshot>() {
                @Override
                public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                    tagSetada = true;

                    if(documentSnapshot.getData() == null)
                        return;

                    String mensagem = documentSnapshot.getData().get("mensagem").toString();

                    if(!mensagem.equals(textShareEditText.getText().toString())) {
                        textShareEditText.setEnabled(false);
                        textShareEditText.setText(mensagem);
                        textShareEditText.setEnabled(true);
                        textShareEditText.requestFocus(View.FOCUS_RIGHT);
                    }
                }
            });

            textShareEditText.setEnabled(true);
            textShareEditText.requestFocus();
        });


        Handler handler = new Handler();
        Runnable runnable = new Runnable() {
            @Override
            public void run() {
                if(tagSetada && editouTexto && new Date().getTime() - dataUltimaDigitacao.getTime() > 1000) {
                    Map<String, Object> mensagem = new HashMap<>();
                    mensagem.put("mensagem", textShareEditText.getText().toString());
                    collMensagensReference.document(tagEditText.getText().toString()).set(mensagem, SetOptions.merge());
                    editouTexto = false;
                }

                handler.postDelayed(this, 1000);
            }
        };

        handler.post(runnable);

        textShareEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }

            @Override
            public void afterTextChanged(Editable editable) {

                if(textShareEditText.isEnabled()){
                    dataUltimaDigitacao = new Date();
                    editouTexto = true;
                }
            }
        });

    }


    private void esconderTeclado (View v){
        InputMethodManager ims = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
        ims.hideSoftInputFromWindow(v.getWindowToken(), 0);
    }

}
