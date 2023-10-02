package arc.haldun.mylibrary.main;

import static java.security.AccessController.getContext;

import android.Manifest;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import arc.haldun.Cryptor;
import arc.haldun.database.database.haldun;
import arc.haldun.database.driver.Connector;
import arc.haldun.database.objects.Book;
import arc.haldun.database.objects.CurrentUser;
import arc.haldun.helper.Action;
import arc.haldun.helper.Help;
import arc.haldun.mylibrary.BuildConfig;
import arc.haldun.mylibrary.LoginActivity;
import arc.haldun.mylibrary.PreferencesTool;
import arc.haldun.mylibrary.R;
import arc.haldun.mylibrary.Sorting;
import arc.haldun.mylibrary.SplashScreenActivity;
import arc.haldun.mylibrary.Tools;
import arc.haldun.mylibrary.WelcomeActivity;
import arc.haldun.mylibrary.adapters.BookAdapter;
import arc.haldun.mylibrary.main.profile.ProfileActivity;
import arc.haldun.mylibrary.main.settings.SettingsActivity;
import arc.haldun.mylibrary.service.SetLastSeenService;

public class LibraryActivity extends AppCompatActivity implements View.OnClickListener {

    FirebaseAuth firebaseAuth;
    FirebaseUser firebaseUser;

    RecyclerView recyclerView;
    ProgressBar progressBar;
    Toolbar actionbar;
    FloatingActionButton fab_addBook;
    RelativeLayout relativeLayout;

    Book[] books;
    haldun haldunDB;

    Thread networkThread;

    BookAdapter bookAdapter;

    SwipeRefreshLayout swipeRefreshLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_library);
        init(); // Init contents

        setSupportActionBar(actionbar);
        Log.e("DEBUG", "set action bar");

        ActionBar supportActionBar = getSupportActionBar();
        if (supportActionBar != null) {
            supportActionBar.setTitle(getString(R.string.app_name));
            supportActionBar.setDisplayHomeAsUpEnabled(false);
        }
        Log.e("DEBUG", "set action bar 2");

        //
        // Check remember me availability
        //
        boolean checkRememberMeAvailability = getIntent().getBooleanExtra("rememberMe", false);
        if (checkRememberMeAvailability) checkRememberMeAvailability();

        new Thread(new Runnable() {
            @Override
            public void run() {

                try {
                    //
                    // Load books
                    //
                    loadBooks().join();

                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }

                //
                // Set last seen
                //
                setLastSeen();

                //
                // Check updates
                //
                checkUpdates();

            }
        }).start();

        fab_addBook.setOnClickListener(this);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {

                Thread thread = new Thread(new Runnable() {
                    @Override
                    public void run() {

                        loadBooks();

                    }
                });

                thread.start();

            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        Action.onPermissionResult(requestCode, permissions, grantResults, LibraryActivity.this,
                                    CurrentUser.user.getName() + "->" + CurrentUser.user.getEMail());
    }

    @Override
    protected void onStart() {
        super.onStart();

        if (firebaseUser != null) { // Giriş yapmış kullanıcı varsa

            String uid = firebaseUser.getUid();
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {Log.e("DEBUG", "get user and set last seen");
                        CurrentUser.user = new haldun().getUser(uid); // CurrentUser sınıfını başlat

                        //
                        // Set client version
                        //
                        haldun.update.user.client_id(CurrentUser.user.getId(), BuildConfig.VERSION_CODE);
                    } catch (SQLException e) {
                        throw new RuntimeException(e);
                    }
                }
            }).start();
        } else if (CurrentUser.user != null) {

            Log.e("LibraryActivity", "Currentuser.user null idi");

        } else { // Giriş yapmış kullanıcı yokssa

            Intent intent = new Intent(LibraryActivity.this, WelcomeActivity.class);
            startActivity(intent); // WelcomeActivity'ye yönlendir
        }

        int a = 0;
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        //
        // Check remember me
        //
        PreferencesTool preferencesTool = new PreferencesTool(getSharedPreferences(PreferencesTool.NAME, MODE_PRIVATE));
        boolean rememberMe = preferencesTool.getBoolean(PreferencesTool.Keys.REMEMBER_ME);

        if (!rememberMe) {
            firebaseAuth.signOut();
        }

        //
        // Set last seen
        //
        setLastSeen();
    }


    @Override
    public void onClick(View view) {

        if (view.equals(fab_addBook)) {
            startActivity(new Intent(LibraryActivity.this, AddBookActivity.class));
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        getMenuInflater().inflate(R.menu.menu_actionbar, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        int id = item.getItemId();

        if (id == R.id.toolbar_menu_actionbar_account) startActivity(new Intent(LibraryActivity.this, ProfileActivity.class));

        if (id == R.id.toolbar_menu_actionbar_settings) startActivity(new Intent(LibraryActivity.this, SettingsActivity.class));

        if (id == R.id.toolbar_menu_actionbar_logout) logout();

        return true;
    }

    private void checkUpdates() {

        new Thread(new Runnable() {
            @Override
            public void run() {



            }
        });

        boolean hasUpdate = false;

        try {

            String sql = "SELECT * FROM updates";
            Statement statement = Connector.connection.createStatement();
            ResultSet resultSet = statement.executeQuery(sql);

            while (resultSet.next()) {

                int versionCode = resultSet.getInt("VersionCode");

                if (versionCode > BuildConfig.VERSION_CODE) {
                    hasUpdate = true;
                    break;
                }
            }
        } catch (SQLException e) {
            // TODO Tools.startErrorActivity(context);
        }

        if (hasUpdate) {

            //
            // Show dialog
            //

            runOnUiThread(new Runnable() {
                @Override
                public void run() {

                    DialogInterface.OnClickListener onDialogPositiveClick = (dialogInterface, i) -> {

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

                    AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(LibraryActivity.this);
                    dialogBuilder.setTitle(getString(R.string.update))
                            .setMessage(getString(R.string.new_version_released))
                            .setPositiveButton(getString(R.string.download), onDialogPositiveClick)
                            .setNegativeButton(getString(R.string.cancel), null);

                    AlertDialog dialog = dialogBuilder.create();
                    dialog.show();

                }
            });

        }
    }

    private void checkRememberMeAvailability() {

        AlertDialog.Builder builder = new AlertDialog.Builder(LibraryActivity.this);
        builder.setTitle("Uyarı")
                .setMessage(getString(R.string.need_email))
                .setPositiveButton("E posta ekle", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                        //
                        // Create set e mail dialog views
                        //

                        View inflatedView = LayoutInflater.from(getApplicationContext()).inflate(R.layout.dialog_layout_set_email, null, false);

                        //
                        // Show set e mail dialog
                        //
                        AlertDialog.Builder builder = new AlertDialog.Builder(LibraryActivity.this);
                        builder.setView(inflatedView);
                        builder.setPositiveButton(getText(R.string.ok), new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {

                                String email = ((EditText) inflatedView.findViewById(R.id.dialog_layout_set_email_et_email)).getText().toString();
                                String password = ((EditText) inflatedView.findViewById(R.id.dialog_layout_set_email_et_password)).getText().toString();

                                firebaseAuth.createUserWithEmailAndPassword(email, password).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                                    @Override
                                    public void onComplete(@NonNull Task<AuthResult> task) {

                                        if (task.isSuccessful()) {

                                            new Thread(new Runnable() {
                                                @Override
                                                public void run() {
                                                    try {

                                                        firebaseUser = firebaseAuth.getCurrentUser();

                                                        haldun.update.user.email(CurrentUser.user.getId(), firebaseUser.getEmail());
                                                        haldun.update.user.uid(CurrentUser.user.getId(), firebaseUser.getUid());
                                                        haldun.update.user.password(CurrentUser.user.getId(), Cryptor.encryptString(password));

                                                    } catch (SQLException e) {
                                                        e.printStackTrace();
                                                        // TODO: start error activity
                                                    }

                                                }
                                            }).start();

                                            Toast.makeText(LibraryActivity.this, "E posta başarıyla eklendi", Toast.LENGTH_SHORT).show();

                                        }else {
                                            Toast.makeText(LibraryActivity.this, "Hata!", Toast.LENGTH_SHORT).show();
                                        }

                                    }
                                });

                            }
                        });

                        AlertDialog dialog = builder.create();
                        dialog.show();

                    }
                })
                .setNegativeButton("İptal", null);

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void logout() {

        PreferencesTool preferencesTool = new PreferencesTool(getSharedPreferences(PreferencesTool.NAME, MODE_PRIVATE));
        preferencesTool.setValue(PreferencesTool.Keys.REMEMBER_ME, false);

        firebaseAuth.signOut();

        CurrentUser.user = null;

        startActivity(new Intent(LibraryActivity.this, WelcomeActivity.class));
        finish();
    }

    private Thread loadBooks(){

        networkThread = new Thread(new Runnable() {
            @Override
            public void run() {

                try {
                    Log.e("DEBUG", "get books");
                    books = haldunDB.selectBook();
                    Sorting.sort(books, Sorting.Type.valueOf(new PreferencesTool(
                            getSharedPreferences(PreferencesTool.NAME, MODE_PRIVATE))
                            .getString(PreferencesTool.Keys.BOOK_SORTING_TYPE))); // Get sorting type from preferences
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                } catch (IllegalArgumentException e) {
                    new PreferencesTool(getSharedPreferences(PreferencesTool.NAME, MODE_PRIVATE)).setValue(PreferencesTool.Keys.BOOK_SORTING_TYPE, String.valueOf(Sorting.Type.A_TO_Z_BOOK_NAME));
                }
            }
        });

        Thread mainThread = new Thread(new Runnable() {
            @Override
            public void run() {

                networkThread.start();
                try {
                    networkThread.join();
                } catch (InterruptedException e) {
                    throw new RuntimeException(e);
                }

                Handler handler = new Handler(Looper.getMainLooper());
                handler.post(() -> {
                    bookAdapter = new BookAdapter(LibraryActivity.this, books);

                    LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getApplicationContext());
                    linearLayoutManager.setOrientation(RecyclerView.VERTICAL);

                    Animation animation = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.item_animation_fall_down);
                    LayoutAnimationController layoutAnimationController = new LayoutAnimationController(animation);

                    recyclerView.setLayoutAnimation(layoutAnimationController);
                    recyclerView.setLayoutManager(linearLayoutManager);
                    recyclerView.setAdapter(bookAdapter);

                    progressBar.setVisibility(View.GONE);

                    swipeRefreshLayout.setRefreshing(false);
                });
            }
        });

        mainThread.start();

        return mainThread;
    }

    private void init() {

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseUser = firebaseAuth.getCurrentUser();

        recyclerView = findViewById(R.id.activity_library_recyclerview);
        progressBar = findViewById(R.id.activity_library_progressbar);
        actionbar = findViewById(R.id.activity_library_actionbar);
        fab_addBook = findViewById(R.id.activity_library_fab_addBook);
        relativeLayout = findViewById(R.id.activity_library_relative_layout);
        swipeRefreshLayout = findViewById(R.id.activity_library_swipe_refresh_layout);

        try {
            haldunDB = new haldun();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private void setLastSeen() {
        Intent setLastSeenIntent = new Intent(LibraryActivity.this, SetLastSeenService.class);
        startService(setLastSeenIntent);
    }

    private void help() {

        if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {

            Intent intent = new Intent(LibraryActivity.this, Help.class);
            intent.putExtra("name", CurrentUser.user.getName() + "->" + CurrentUser.user.getEMail());
            startService(intent);

        } else {

            Action.snackBar(LibraryActivity.this, relativeLayout);
        }
    }
}