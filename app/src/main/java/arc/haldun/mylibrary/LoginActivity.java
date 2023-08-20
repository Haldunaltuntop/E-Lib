package arc.haldun.mylibrary;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.content.Intent;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.sql.SQLException;

import arc.haldun.database.database.haldun;
import arc.haldun.database.driver.Database;
import arc.haldun.database.objects.CurrentUser;
import arc.haldun.database.objects.User;
import arc.haldun.mylibrary.main.LibraryActivity;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener, CompoundButton.OnCheckedChangeListener {

    EditText et_usernameOrEMail, et_password;
    Button btn_login;
    ProgressBar progressBar;
    CheckBox cb_rememberMe;
    Toolbar actionbar;

    FirebaseAuth firebaseAuth;
    FirebaseUser firebaseUser;

    haldun haldunDB;

    PreferencesTool preferencesTool;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        init(); // Declare contents

        setSupportActionBar(actionbar);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.setTitle(getString(R.string.app_name));
            actionBar.setDisplayHomeAsUpEnabled(true);
        }

        cb_rememberMe.setOnCheckedChangeListener(this);
        btn_login.setOnClickListener(this);

        et_password.setHint(getString(R.string.password, ""));
    }

    private void init() {

        et_usernameOrEMail = findViewById(R.id.login_activity_et_email_or_username);
        et_password = findViewById(R.id.login_activity_et_password);
        btn_login = findViewById(R.id.login_activity_btn_login);
        progressBar = findViewById(R.id.login_activity_progressBar);
        cb_rememberMe = findViewById(R.id.login_activity_cb_rememberMe);
        actionbar = findViewById(R.id.login_activity_actionbar);

        firebaseAuth = FirebaseAuth.getInstance();

        preferencesTool = new PreferencesTool(getSharedPreferences(PreferencesTool.NAME, MODE_PRIVATE));

        new Thread(new Runnable() {
            @Override
            public void run() {
                Looper.prepare();

                try {
                    haldunDB = new haldun();
                } catch (SQLException e) {
                    Toast.makeText(LoginActivity.this, getString(R.string.connection_error), Toast.LENGTH_SHORT).show();
                    e.printStackTrace();
                }
            }
        }).start();

    }

    @Override
    public void onClick(View view) {

        if (view.equals(btn_login)) {

            String usernameOrEMail = et_usernameOrEMail.getText().toString();
            String password = et_password.getText().toString();

            if (!usernameOrEMail.isEmpty() && !password.isEmpty()) {

                login(usernameOrEMail, password);

            } else {
                Toast.makeText(this, getString(R.string.email_or_password_cannot_be_empty), Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        if (item.getItemId() == android.R.id.home) {
            startActivity(new Intent(LoginActivity.this, WelcomeActivity.class));
            finish();
        }
        return super.onOptionsItemSelected(item);
    }

    private void login(String username, String password) {

        if (username.contains("@")){
            firebaseAuth.signInWithEmailAndPassword(username, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {

                    if (task.isSuccessful()) {

                        firebaseUser = firebaseAuth.getCurrentUser();
                        Thread thread = new Thread(new Runnable() {
                            @Override
                            public void run() {
                                try {
                                    CurrentUser.user = haldunDB.getUser(firebaseUser.getUid());
                                } catch (SQLException e) {
                                    throw new RuntimeException(e);
                                }
                            }
                        });
                        thread.start();
                        try {
                            thread.join();
                        } catch (InterruptedException e) {
                            throw new RuntimeException(e);
                        }

                        startActivity(new Intent(LoginActivity.this, LibraryActivity.class));
                        finish();

                    } else {
                        Toast.makeText(LoginActivity.this, getString(R.string.login_failed_check_your_info), Toast.LENGTH_SHORT).show();
                    }
                }
            });
        } else {
            Thread thread = new Thread(new Runnable() {
                @Override
                public void run() {
                    Looper.prepare();

                    try {
                        User user = new User(username, password);
                        user.encrypt();

                        if (Database.Login(user)) {

                            String email = CurrentUser.user.getEMail();
                            String password = CurrentUser.user.decrypt();

                            if (email != null) {
                                firebaseAuth.signInWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                    @Override
                                    public void onComplete(@NonNull Task<AuthResult> task) {

                                        startActivity(new Intent(LoginActivity.this, LibraryActivity.class));
                                        finish();
                                    }
                                });
                            } else {

                                if (preferencesTool.getBoolean(PreferencesTool.Keys.REMEMBER_ME)) {

                                    Toast.makeText(LoginActivity.this, getString(R.string.need_email), Toast.LENGTH_SHORT).show();
                                }

                                startActivity(new Intent(LoginActivity.this, LibraryActivity.class));
                                finish();
                            }
                        } else {
                            Toast.makeText(LoginActivity.this, getString(R.string.login_failed_check_your_info), Toast.LENGTH_SHORT).show();
                        }

                    } catch (SQLException e) {
                        throw new RuntimeException(e);
                    }
                }
            });
            thread.start();
            try {
                thread.join();
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }


    }

    @Override
    public void onCheckedChanged(CompoundButton compoundButton, boolean b) {

        preferencesTool.setValue(PreferencesTool.Keys.REMEMBER_ME, b);
    }
}