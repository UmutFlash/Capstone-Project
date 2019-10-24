package com.umutflash.openactivity;

import android.Manifest;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.MenuItem;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.google.android.gms.tasks.Continuation;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;
import com.umutflash.openactivity.data.model.Spot;
import com.umutflash.openactivity.ui.home.HomeFragment;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import butterknife.BindView;
import butterknife.ButterKnife;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class AddSpotActivity extends AppCompatActivity {

    @BindView(R.id.title)
    EditText mTitleEditeText;
    @BindView(R.id.category)
    EditText mCategoryEditeText;
    @BindView(R.id.description)
    EditText mDescriptionEditeText;
    @BindView(R.id.imageView)
    ImageView mImageView;
    @BindView(R.id.upload)
    Button mUploadBtn;
    @BindView(R.id.loading)
    ProgressBar mProgressBar;

    private double mLatitude;
    private double mLongitude;
    private Uri mImageFilePath;

    private static final int REQUEST_CAPTURE_IMAGE = 100;
    private static final int REQUEST_PICK_IMAGE = 1;
    private static final String IMAGE_URI = "imageUri";
    private static final String REF_FIREBASE = "spots";
    private static final String FIREBASE_URL = "https://openactivity-7c70c.firebaseio.com";

    private StorageReference mStorageReference;
    private DatabaseReference mDatabaseReference;
    private FirebaseUser mUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_spot);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        ButterKnife.bind(this);
        Bundle extras = getIntent().getExtras();
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setTitle("Add Spot");
        if (savedInstanceState != null) {
            mImageFilePath = savedInstanceState.getParcelable(IMAGE_URI);
            mImageView.setImageURI(mImageFilePath);
        }

        if (extras != null) {
            mLatitude = extras.getDouble(HomeFragment.ARG_LATITUDE);
            mLongitude = extras.getDouble(HomeFragment.ARG_LONITUDE);

        }

        FirebaseAuth mFirebaseAuth = FirebaseAuth.getInstance();

        mUser = mFirebaseAuth.getCurrentUser();

        mStorageReference = FirebaseStorage.getInstance().getReference(REF_FIREBASE);
        mDatabaseReference = FirebaseDatabase.getInstance().getReference(REF_FIREBASE);

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            //takePictureButton.setEnabled(false);
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE}, 0);
        }


        FloatingActionButton fab = findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                captureFromCamera();
            }
        });

        mUploadBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                uploadFile();
            }
        });
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            onBackPressed();
            return true;
        }
        return super.onContextItemSelected(item);
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
    }

    @Override
    protected void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        if (mImageFilePath != null) {
            outState.putParcelable(IMAGE_URI, mImageFilePath);
        }
    }

    void getRetrofitImage(Spot spot) {

        View contextView = findViewById(android.R.id.content);

        String url = "https://openactivity-7c70c.firebaseio.com";

        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(FIREBASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        SpotAPI service = retrofit.create(SpotAPI.class);

        Call<Spot> call = service.setData(REF_FIREBASE, spot);

        call.enqueue(new Callback<Spot>() {
            @Override
            public void onResponse(Call<Spot> call, Response<Spot> response) {
                mProgressBar.setVisibility(View.GONE);
                Snackbar.make(contextView, "Upload Data successful", Snackbar.LENGTH_LONG)
                        .show();
            }

            @Override
            public void onFailure(Call<Spot> call, Throwable t) {
                mProgressBar.setVisibility(View.GONE);
                Snackbar.make(contextView, "FEHLER", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });

    }

    private void uploadFile() {
        View contextView = findViewById(android.R.id.content);
        if (mImageFilePath != null) {
            mProgressBar.setVisibility(View.VISIBLE);
            StorageReference fileReference = mStorageReference.child(System.currentTimeMillis() + ".jpeg");
            StorageTask<UploadTask.TaskSnapshot> uploadTask = fileReference.putFile(mImageFilePath);
            Task<Uri> urlTask = uploadTask.continueWithTask(new Continuation<UploadTask.TaskSnapshot, Task<Uri>>() {
                @Override
                public Task<Uri> then(@NonNull Task<UploadTask.TaskSnapshot> task) throws Exception {
                    if (!task.isSuccessful()) {
                        throw Objects.requireNonNull(task.getException());
                    }
                    return fileReference.getDownloadUrl();
                }
            }).addOnCompleteListener(new OnCompleteListener<Uri>() {
                @Override
                public void onComplete(@NonNull Task<Uri> task) {
                    if (task.isSuccessful()) {
                        Uri downloadUri = task.getResult();
                        assert downloadUri != null;
                        String downloadURL = downloadUri.toString();
                        Snackbar.make(contextView, "Upload Image successful", Snackbar.LENGTH_LONG)
                                .show();

                        String spotId = mDatabaseReference.push().getKey();
                        Spot spot = new Spot(spotId,
                                mUser.getUid(),
                                mTitleEditeText.getText().toString().trim(),
                                mCategoryEditeText.getText().toString().trim(),
                                mDescriptionEditeText.getText().toString().trim(),
                                downloadURL,
                                mLatitude,
                                mLongitude);

                        getRetrofitImage(spot);

                    } else {
                        Snackbar.make(contextView, "Upload error", Snackbar.LENGTH_LONG).show();
                        mProgressBar.setVisibility(View.GONE);
                    }
                }
            });
        } else {
            Snackbar.make(contextView, "Not file selected", Snackbar.LENGTH_LONG).show();
        }
    }

    private String getFileExtensions(Uri uri) {
        ContentResolver cr = getContentResolver();
        MimeTypeMap mime = MimeTypeMap.getSingleton();
        return mime.getExtensionFromMimeType(cr.getType(uri));

    }

    private void captureFromCamera() {
        try {
            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            intent.putExtra(MediaStore.EXTRA_OUTPUT, FileProvider.getUriForFile(this, BuildConfig.APPLICATION_ID + ".provider", createImageFile()));
            startActivityForResult(intent, REQUEST_CAPTURE_IMAGE);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }


    private void takePictureIntent() {

        Intent pictureIntent = new Intent(
                MediaStore.ACTION_IMAGE_CAPTURE
        );
        if (pictureIntent.resolveActivity(getPackageManager()) != null) {
            //Create a file to store the image
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                // Error occurred while creating the File

            }
            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(this, "com.umutflash.openactivity.fileprovider", photoFile);
                pictureIntent.putExtra(MediaStore.EXTRA_OUTPUT,
                        photoURI);
                startActivityForResult(pictureIntent,
                        REQUEST_CAPTURE_IMAGE);
            }
        }

    }

    private void pickPictureIntent() {

        //Create an Intent with action as ACTION_PICK
        Intent intent = new Intent(Intent.ACTION_PICK);
        // Sets the type as image/*. This ensures only components of type image are selected
        intent.setType("image/*");
        //We pass an extra array with the accepted mime types. This will ensure only components with these MIME types as targeted.
        String[] mimeTypes = {"image/jpeg", "image/png"};
        intent.putExtra(Intent.EXTRA_MIME_TYPES, mimeTypes);
        // Launching the Intent

        startActivityForResult(intent, REQUEST_PICK_IMAGE);
    }


    private File createImageFile() throws IOException {
        String timeStamp =
                new SimpleDateFormat("yyyyMMdd_HHmmss",
                        Locale.getDefault()).format(new Date());
        String imageFileName = "IMG_" + timeStamp + "_";

        File storageDir = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_DCIM), "Camera");
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        mImageFilePath = Uri.parse("file://" + image.getAbsolutePath());

        //mImageFilePath = image.getAbsolutePath();
        return image;
    }


    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        // Result code is RESULT_OK only if the user selects an Image
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == Activity.RESULT_OK)
            switch (requestCode) {
                case REQUEST_PICK_IMAGE:
                    break;
                case REQUEST_CAPTURE_IMAGE:
                    mImageView.setImageURI(mImageFilePath);
                    break;
            }
    }


    public void uploadImage(Bitmap bitmap) {

        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        StorageReference storageRef = FirebaseStorage.getInstance().getReference().child("pics/${FirebaseAuth.getInstance().getCurrentUser().getUid()}");
        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream);

        byte[] image = outputStream.toByteArray();
        UploadTask upload = storageRef.putBytes(image);
        upload.addOnCompleteListener(this, new OnCompleteListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<UploadTask.TaskSnapshot> task) {


            }
        });
    }


    private void upload() {


    }

}
