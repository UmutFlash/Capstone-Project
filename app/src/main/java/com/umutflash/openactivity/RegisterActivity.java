package com.umutflash.openactivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import androidx.annotation.NonNull;
import butterknife.BindView;
import butterknife.ButterKnife;

public class RegisterActivity extends BaseActivity {

    @BindView(R.id.email)
    EditText emailEditeText;
    @BindView(R.id.password)
    EditText passwordEditeText;
    @BindView(R.id.password_confirm)
    EditText passwordConfirmEditeText;
    @BindView(R.id.loading)
    ProgressBar loading;
    @BindView(R.id.register)
    Button registerBtn;

    private FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        firebaseAuth = FirebaseAuth.getInstance();
        ButterKnife.bind(this);

        registerBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loading.setVisibility(View.VISIBLE);
                registerUser();
            }
        });
    }

    private void registerUser() {
        closeKeyboard();
        if (isNetworkAvailable()) {
            String email = emailEditeText.getText().toString().trim();
            String password = passwordEditeText.getText().toString().trim();

            if (TextUtils.isEmpty(email)) {
                Toast.makeText(this, "Please enter email", Toast.LENGTH_SHORT).show();
                return;
            }

            if (TextUtils.isEmpty(password)) {
                Toast.makeText(this, "Please enter passwort", Toast.LENGTH_SHORT).show();
                return;
            }

            loading.setVisibility(View.VISIBLE);
            firebaseAuth.createUserWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                showSuccess();
                                loading.setVisibility(View.GONE);
                                startActivity(new Intent(getApplicationContext(), LoginActivity.class));
                                finish();
                            } else {
                                showError(task.getException().getMessage());
                                loading.setVisibility(View.GONE);
                            }
                        }
                    });
        } else {
            showError(getString(R.string.no_network));
            loading.setVisibility(View.GONE);
        }
    }
}
