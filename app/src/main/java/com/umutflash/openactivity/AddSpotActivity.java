package com.umutflash.openactivity;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.MenuItem;
import android.view.View;
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
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;
import com.umutflash.openactivity.data.model.Spot;
import com.umutflash.openactivity.ui.home.HomeFragment;
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

    @BindView(R.id.fab)
    FloatingActionButton mFab;


    private double mLatitude;
    private double mLongitude;
    private Uri mImageFilePath;

    private static final int REQUEST_CAPTURE_IMAGE = 100;
    private static final int REQUEST_PICK_IMAGE = 1;
    private static final String IMAGE_URI = "imageUri";
    public static final String REF_FIREBASE = "spots";
    public static final String ADD_SPOT_TITLE = "Add Spot";
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
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
            getSupportActionBar().setTitle(ADD_SPOT_TITLE);
        }

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

        checkPermission();

        mFab.setOnClickListener(new View.OnClickListener() {
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

    private void checkPermission() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.WRITE_EXTERNAL_STORAGE) + ContextCompat
                .checkSelfPermission(this,
                        Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {

            if (ActivityCompat.shouldShowRequestPermissionRationale
                    (this, Manifest.permission.WRITE_EXTERNAL_STORAGE) ||
                    ActivityCompat.shouldShowRequestPermissionRationale
                            (this, Manifest.permission.CAMERA)) {
                mFab.setEnabled(false);
                Snackbar.make(this.findViewById(android.R.id.content),
                        getString(R.string.grant_permissions_upload_Spot),
                        Snackbar.LENGTH_INDEFINITE).setAction(getString(R.string.enable) ,
                        v -> requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA},
                                10)).show();
            } else {
                requestPermissions( new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA},
                        10);
            }
        } else {
            mFab.setEnabled(true);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           @NonNull String[] permissions, @NonNull int[] grantResults) {

        switch (requestCode) {
            case 10:
                if (grantResults.length > 0) {
                    boolean cameraPermission = grantResults[1] == PackageManager.PERMISSION_GRANTED;
                    boolean readExternalFile = grantResults[0] == PackageManager.PERMISSION_GRANTED;

                    if (cameraPermission && readExternalFile) {
                        mFab.setEnabled(true);
                    } else {
                        mFab.setEnabled(false);
                        Snackbar.make(this.findViewById(android.R.id.content),
                                getString(R.string.grant_permissions_upload_Spot),
                                Snackbar.LENGTH_INDEFINITE).setAction( getString(R.string.enable),
                                new View.OnClickListener() {
                                    @Override
                                    public void onClick(View v) {
                                        requestPermissions(
                                                new String[]{Manifest.permission
                                                        .WRITE_EXTERNAL_STORAGE, Manifest.permission.CAMERA},
                                                10);
                                    }
                                }).show();
                    }
                }
                break;
        }
    }


    void getRetrofitImage(Spot spot) {

        View contextView = findViewById(android.R.id.content);

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
                Snackbar.make(contextView,  getString(R.string.upload_successful), Snackbar.LENGTH_LONG)
                        .show();
            }

            @Override
            public void onFailure(Call<Spot> call, Throwable t) {
                mProgressBar.setVisibility(View.GONE);
                Snackbar.make(contextView, getString(R.string.upload_error), Snackbar.LENGTH_LONG).show();
            }
        });

    }

    private void uploadFile() {
        View contextView = findViewById(android.R.id.content);
        if (mImageFilePath != null
                && !mTitleEditeText.getText().toString().matches("")
                && !mCategoryEditeText.getText().toString().matches("")
                && !mDescriptionEditeText.getText().toString().matches("")
                ) {
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
                        Snackbar.make(contextView, getString(R.string.upload_image_successful), Snackbar.LENGTH_LONG)
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
                        Snackbar.make(contextView, getString(R.string.upload_error), Snackbar.LENGTH_LONG).show();
                        mProgressBar.setVisibility(View.GONE);
                    }
                }
            });
        } else {
            Snackbar.make(contextView,  getString(R.string.all_fields_error), Snackbar.LENGTH_LONG).show();
        }
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
}
