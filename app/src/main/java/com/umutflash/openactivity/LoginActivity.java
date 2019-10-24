package com.umutflash.openactivity;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import androidx.annotation.NonNull;
import butterknife.BindView;
import butterknife.ButterKnife;

public class LoginActivity extends BaseActivity {

    @BindView(R.id.email)
    EditText emailEditeText;
    @BindView(R.id.password)
    EditText passwordEditeText;
    @BindView(R.id.loading)
    ProgressBar loading;
    @BindView(R.id.register)
    TextView register;
    @BindView(R.id.login)
    Button loginBtn;

    private FirebaseAuth firebaseAuth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        firebaseAuth = FirebaseAuth.getInstance();
        ButterKnife.bind(this);

        loginBtn.setOnClickListener(v -> {
            loading.setVisibility(View.VISIBLE);
            loginUser();
        });

        register.setOnClickListener(v -> startActivity(new Intent(getApplicationContext(), RegisterActivity.class)));
    }


    private void loginUser() {
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
            firebaseAuth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                showSuccess();
                                loading.setVisibility(View.GONE);
                                startActivity(new Intent(getApplicationContext(), MainActivity.class));
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

    @Override
    public void onBackPressed() { }
}





