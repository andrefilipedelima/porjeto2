package com.example.fatec_ipi_noite_pojeto_p1;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatImageButton;
import androidx.appcompat.widget.AppCompatImageView;

import android.app.ActivityOptions;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.Image;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.provider.Settings;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import javax.sql.DataSource;

public class ImageShareActivity extends AppCompatActivity {

    private ImageButton homeButton;
    private ImageButton textButton;
    private ImageButton imageButton;
    private AppCompatImageButton tagButton;

    private GridView imagesGridView;

    private FloatingActionButton floatingActionButton;

    private FloatingActionButton fabCamera;
    private FloatingActionButton fabGaleria;

    private EditText tagEditText;

    private CollectionReference collMensagensReference;

    private List<ImageShare> listImageShare;

    private static final int REQ_CODE_CAMERA = 1001;
    private static final int REQ_CODE_GALERIA = 1002;

    private boolean isFABOpen = false;

    private BaseAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_image_share);

        homeButton = findViewById(R.id.homeButton);
        textButton = findViewById(R.id.textButton);
        imageButton = findViewById(R.id.imageButton);
        floatingActionButton = findViewById(R.id.floatingActionButton);

        imagesGridView = findViewById(R.id.imagesGridView);

        fabCamera = findViewById(R.id.fabCamera);
        fabGaleria = findViewById(R.id.fabGaleria);

        tagEditText = findViewById(R.id.tagEditText);
        tagButton = findViewById(R.id.tagButton);

        listImageShare = new ArrayList<>();
        adapter = new ImageAdapterGridView(this, listImageShare);

        imagesGridView.setAdapter(adapter);

        imageButton.setBackgroundColor(getColor(R.color.colorMenuActive));

        textButton.setOnClickListener((view) -> {

            Intent intent = new Intent(ImageShareActivity.this,  TextShareActivity.class);
            startActivity(intent);

        });

        homeButton.setOnClickListener(view ->{

            Intent intent = new Intent(ImageShareActivity.this,  MainActivity.class);
            startActivity(intent);

        });

        floatingActionButton.setEnabled(false);
        fabGaleria.setEnabled(false);
        fabCamera.setEnabled(false);

        floatingActionButton.setOnClickListener((v) ->{
            if(!isFABOpen){
                showFABMenu();
            }else{
                closeFABMenu();
            }
        });

        fabCamera.setOnClickListener((v) -> {
            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            startActivityForResult(intent, REQ_CODE_CAMERA);
        });

        fabGaleria.setOnClickListener((v) -> {
            Intent intent = new Intent(Intent.ACTION_PICK, android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(intent , REQ_CODE_GALERIA);
        });

        tagButton.setOnClickListener((v) ->{
            if(tagEditText.getText().length() == 0){
                Toast.makeText(this, R.string.digit_tag, Toast.LENGTH_SHORT);
                return;
            }

            tagEditText.clearFocus();
            esconderTeclado(v);

            floatingActionButton.setEnabled(true);
            fabGaleria.setEnabled(true);
            fabCamera.setEnabled(true);

            collMensagensReference = FirebaseFirestore.getInstance().collection(String.format(
                    Locale.getDefault(),
                    "_%s",
                    tagEditText.getText().toString()
            ));

            collMensagensReference.addSnapshotListener((result, e) -> {
                listImageShare.clear();

                for(DocumentSnapshot doc: result.getDocuments())
                    listImageShare.add(new ImageShare(doc.get("imagePath").toString()));

                adapter.notifyDataSetChanged();
            });
        });

    }

    private void esconderTeclado (View v){
        InputMethodManager ims = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
        ims.hideSoftInputFromWindow(v.getWindowToken(), 0);
    }

    private void showFABMenu(){
        isFABOpen=true;
        fabCamera.animate().translationY(-getResources().getDimension(R.dimen.standard_55));
        fabGaleria.animate().translationY(-getResources().getDimension(R.dimen.standard_105));
    }

    private void closeFABMenu(){
        isFABOpen=false;
        fabCamera.animate().translationY(0);
        fabGaleria.animate().translationY(0);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(isFABOpen)
            closeFABMenu();

        if(resultCode == RESULT_OK && (requestCode == REQ_CODE_CAMERA || requestCode == REQ_CODE_GALERIA)) {
            Bitmap picture = null;

            if(requestCode == REQ_CODE_CAMERA)
                picture = (Bitmap) data.getExtras().get("data");
            else
            {
                try {
                    InputStream inputStream = getContentResolver().openInputStream(data.getData());
                    picture = BitmapFactory.decodeStream(inputStream);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
            }

            if(picture != null) {
                String android_id = Settings.Secure.getString(getBaseContext().getContentResolver(),
                        Settings.Secure.ANDROID_ID);
                Date date = new Date();

                String nomeArquivo = (android_id + DateHelper.format(date).replace("/", "-") + ".jpg").replace(" ", "-").replace(":", "-");

                StorageReference pictureStorageReference = FirebaseStorage.getInstance()
                        .getReference(
                                String.format(
                                        Locale.getDefault(),
                                        "dontpad/%s/%s",
                                        tagEditText.getText().toString(),
                                        nomeArquivo
                                )
                        );

                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                picture.compress(Bitmap.CompressFormat.JPEG, 100, baos);
                byte[] bytes = baos.toByteArray();

                pictureStorageReference.putBytes(bytes)
                        .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                            @Override
                            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                                ImageShare imageShare = new ImageShare(String.format(
                                        Locale.getDefault(),
                                        "dontpad/%s/%s",
                                        tagEditText.getText().toString(),
                                        nomeArquivo
                                ));

                                collMensagensReference.add(imageShare);
                            }
                        });
            }
        }
    }

    public class ImageAdapterGridView extends BaseAdapter {
        private Context mContext;
        private List<ImageShare> listImageShare;
        private HashMap<String, byte[]> buffer;

        public ImageAdapterGridView(Context c, List<ImageShare> listImageShare) {
            this.mContext = c;
            this.listImageShare = listImageShare;
            buffer = new HashMap<>();
        }

        public int getCount() {
            return listImageShare.size();
        }

        public Object getItem(int position) {
            return null;
        }

        public long getItemId(int position) {
            return 0;
        }

        public View getView(int position, View convertView, ViewGroup parent) {
            View v = convertView;
            ImageView imageItem;

            if (v == null) {
                v = LayoutInflater.from(mContext).inflate(R.layout.grid_item, parent, false);
                v.setTag(R.id.imageItem, v.findViewById(R.id.imageItem));
            }

            imageItem = (ImageView) v.getTag(R.id.imageItem);

            ImageShare imageShare = listImageShare.get(position);

            imageItem.setImageResource(R.drawable.ic_padrao_image);

            if(buffer.containsKey(imageShare.getImagePath())){
                Bitmap figura = BitmapFactory.decodeByteArray(buffer.get(imageShare.getImagePath()), 0, buffer.get(imageShare.getImagePath()).length);
                imageItem.setImageBitmap(figura);
            }
            else{
                String imagem = imageShare.getImagePath();
                StorageReference pictureStorageReference = FirebaseStorage.getInstance()
                        .getReference(imagem);

                pictureStorageReference.getBytes(1024 * 1024 * 1024).addOnSuccessListener(new OnSuccessListener<byte[]>() {
                    @Override
                    public void onSuccess(byte[] bytes) {
                        buffer.put(imageShare.getImagePath(), bytes);
                        Bitmap figura = BitmapFactory.decodeByteArray(bytes, 0, bytes.length);
                        imageItem.setImageBitmap(figura);

                        imageItem.setOnClickListener(vImage ->{
                            Drawable imagem = ((SquareImageView) vImage).getDrawable();

                            Bitmap bitmap = ((BitmapDrawable)imagem).getBitmap();
                            ByteArrayOutputStream baos = new ByteArrayOutputStream();
                            bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos);
                            byte[] b = baos.toByteArray();

                            Intent intent = new Intent(mContext, DisplayImageActivity.class);
                            intent.putExtra("figura", b);
                            mContext.startActivity(intent);

                        });
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        e.printStackTrace();
                    }
                });

            }

            return v;
        }
    }
}
