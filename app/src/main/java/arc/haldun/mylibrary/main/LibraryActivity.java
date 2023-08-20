package arc.haldun.mylibrary.main;

import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LayoutAnimationController;
import android.widget.ProgressBar;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.sql.SQLException;

import arc.haldun.database.database.haldun;
import arc.haldun.database.objects.Book;
import arc.haldun.database.objects.CurrentUser;
import arc.haldun.mylibrary.PreferencesTool;
import arc.haldun.mylibrary.R;
import arc.haldun.mylibrary.WelcomeActivity;
import arc.haldun.mylibrary.adapters.BookAdapter;
import arc.haldun.mylibrary.main.profile.ProfileActivity;
import arc.haldun.mylibrary.main.settings.SettingsActivity;
import arc.haldun.mylibrary.service.SetLastSeenService;

public class LibraryActivity extends AppCompatActivity {

    FirebaseAuth firebaseAuth;
    FirebaseUser firebaseUser;

    RecyclerView recyclerView;
    ProgressBar progressBar;
    Toolbar actionbar;

    Book[] books;
    haldun haldunDB;

    Thread networkThread;

    BookAdapter bookAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_library);
        init(); // Init contents

        setSupportActionBar(actionbar);

        ActionBar supportActionBar = getSupportActionBar();
        if (supportActionBar != null) {
            supportActionBar.setTitle(getString(R.string.app_name));
            supportActionBar.setDisplayHomeAsUpEnabled(false);
        }

        loadBooks();
    }

    @Override
    protected void onStart() {
        super.onStart();

        if (firebaseUser != null) { // Giriş yapmış kullanıcı varsa

            String uid = firebaseUser.getUid();
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        CurrentUser.user = new haldun().getUser(uid); // CurrentUser sınıfını başlat
                    } catch (SQLException e) {
                        throw new RuntimeException(e);
                    }
                }
            }).start();
        } else if (CurrentUser.user != null) {

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
        // Set last seen
        //
        Intent setLastSeenService = new Intent(getApplicationContext(), SetLastSeenService.class);
        startService(setLastSeenService); // Start service for setting last seen

        //
        // Check remember me
        //
        PreferencesTool preferencesTool = new PreferencesTool(getSharedPreferences(PreferencesTool.NAME, MODE_PRIVATE));
        boolean rememberMe = preferencesTool.getBoolean(PreferencesTool.Keys.REMEMBER_ME);

        if (!rememberMe) {
            firebaseAuth.signOut();
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

    private void logout() {

        PreferencesTool preferencesTool = new PreferencesTool(getSharedPreferences(PreferencesTool.NAME, MODE_PRIVATE));
        preferencesTool.setValue(PreferencesTool.Keys.REMEMBER_ME, false);

        firebaseAuth.signOut();

        CurrentUser.user = null;

        startActivity(new Intent(LibraryActivity.this, WelcomeActivity.class));
        finish();
    }

    private void loadBooks(){

        networkThread = new Thread(new Runnable() {
            @Override
            public void run() {

                try {
                    books = haldunDB.selectBook();
                } catch (SQLException e) {
                    throw new RuntimeException(e);
                }
            }
        });

        networkThread.start();
        try {
            networkThread.join();
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        bookAdapter = new BookAdapter(LibraryActivity.this, books);

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getApplicationContext());
        linearLayoutManager.setOrientation(RecyclerView.VERTICAL);

        Animation animation = AnimationUtils.loadAnimation(getApplicationContext(), R.anim.item_animation_fall_down);
        LayoutAnimationController layoutAnimationController = new LayoutAnimationController(animation);

        recyclerView.setLayoutAnimation(layoutAnimationController);
        recyclerView.setLayoutManager(linearLayoutManager);
        recyclerView.setAdapter(bookAdapter);

        progressBar.setVisibility(View.GONE);
    }

    private void init() {

        firebaseAuth = FirebaseAuth.getInstance();
        firebaseUser = firebaseAuth.getCurrentUser();

        recyclerView = findViewById(R.id.activity_library_recyclerview);
        progressBar = findViewById(R.id.activity_library_progressbar);
        actionbar = findViewById(R.id.activity_library_actionbar);

        try {
            haldunDB = new haldun();
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
    }
}