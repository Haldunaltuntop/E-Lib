package arc.haldun.mylibrary;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.ActivityNotFoundException;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.io.IOException;
import java.sql.SQLException;

import arc.haldun.Observable;
import arc.haldun.Observer;
import arc.haldun.database.database.haldun;
import arc.haldun.database.driver.Connector;
import arc.haldun.database.objects.CurrentUser;
import arc.haldun.mylibrary.main.LibraryActivity;
import arc.haldun.mylibrary.network.DB;

public class SplashScreenActivity extends AppCompatActivity {

    TextView textView;

    ProgressBar progressBar;

    AlertDialog dialog;
    AlertDialog.Builder dialogBuilder;
    DialogInterface.OnClickListener onDialogPositiveClick, onDialogNegativeClick;

    Thread threadMain, threadNetwork;
    Handler handler;

    Tools.Update update;
    mObservable server;
    mObserver client;

    FirebaseAuth firebaseAuth;
    FirebaseUser firebaseUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash_screen);
        init();

        textView.setText("OnCreate");
        progressBar.setProgress(10);

        //
        // Init threads
        //
        threadMain = new Thread(() -> {
            runOnUiThread(() -> textView.setText("Thread main start"));
            progressBar.setProgress(20);
            threadNetwork.start();
            try {
                threadNetwork.join();
                runOnUiThread(() -> textView.setText("Thread main after join"));
                progressBar.setProgress(100);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        });

        threadNetwork = new Thread(() -> {
            Looper.prepare();
            runOnUiThread(() -> textView.setText("Thread network start"));
            progressBar.setProgress(30);

            //
            // Connect database
            //
            try {
                runOnUiThread(() -> textView.setText("Thread network connect database"));
                if (Connector.connection == null || Connector.connection.isClosed()) {
                    DB.connect(getApplicationContext());
                }
            } catch (SQLException e) {
                e.printStackTrace();
                Tools.makeText(getApplicationContext(), getString(R.string.connection_error));
            } catch (IOException e) {
                e.printStackTrace();
                // TODO start error activity
            } finally {
                if (Connector.connection == null) {
                    // TODO start error activity
                }
            }
            progressBar.setProgress(40);

            //
            // Check updates
            //
            try {
                runOnUiThread(() -> textView.setText("Thread network check updates"));
                update = Tools.hasUpdate(getApplicationContext());

                server.addObserver(client);

                if (update.hasNew()) {

                    handler.post(() -> {
                        server.setVersionCode(update.getVersionCode());
                    });
                }

            } catch (NullPointerException e) {
                e.printStackTrace();
            }
            progressBar.setProgress(50);

            if (firebaseUser != null) {
                runOnUiThread(() -> textView.setText("Thread network check user"));
                try {
                    CurrentUser.user = new haldun().getUser(firebaseUser.getUid());
                    startActivity(new Intent(SplashScreenActivity.this, LibraryActivity.class));
                    runOnUiThread(() -> textView.setText("Thread network redirected library activity"));
                } catch (SQLException e) {
                    e.printStackTrace();
                    Tools.makeText(getApplicationContext(), getString(R.string.connection_error));
                }
            } else {
                startActivity(new Intent(SplashScreenActivity.this, WelcomeActivity.class));
                runOnUiThread(() -> textView.setText("Thread network redirected welcome activity"));
            }
            progressBar.setProgress(70);

            finish();
        });

        threadMain.start();

        textView.setText("On create end");
    }
    private void init() {

        textView = findViewById(R.id.textView);
        progressBar = findViewById(R.id.activity_splashscreen_progressBar);

        server = new mObservable();
        client = new mObserver();
        handler = new Handler(Looper.getMainLooper());

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseUser = firebaseAuth.getCurrentUser();

        //
        // Init dialog
        //
        dialogBuilder = new AlertDialog.Builder(this);
        dialogBuilder.setTitle(getString(R.string.update))
                .setMessage(getString(R.string.new_version_released))
                .setPositiveButton(getString(R.string.download), onDialogPositiveClick)
                .setNegativeButton(getString(R.string.cancel), onDialogNegativeClick);

        dialog = dialogBuilder.create();

        onDialogPositiveClick = (dialogInterface, i) -> {

            //
            // Redirect download page
            //
            String url = "http://haldun.online";

            try {
                Intent intentBrowser = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                intentBrowser.addCategory(Intent.CATEGORY_BROWSABLE);
                intentBrowser.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                startActivity(intentBrowser);
            } catch (ActivityNotFoundException e) {
                e.printStackTrace();
            }
        };


    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        threadMain.interrupt();
        threadNetwork.interrupt();
    }

    class mObserver implements Observer {

        @Override
        public void update(Observable observable) {
            dialog.show();
        }
    }

    static class mObservable extends Observable {

        private int versionCode;

        @SuppressWarnings("unused")
        public int getVersionCode() {
            return versionCode;
        }

        public void setVersionCode(int versionCode) {
            this.versionCode = versionCode;
            check();
        }

        public void check() {

            if (versionCode > BuildConfig.VERSION_CODE) {
                notice();
            }
        }
    }
}