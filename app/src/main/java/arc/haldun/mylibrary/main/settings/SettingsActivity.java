package arc.haldun.mylibrary.main.settings;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.cardview.widget.CardView;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.util.Set;

import arc.haldun.database.objects.CurrentUser;
import arc.haldun.mylibrary.PreferencesTool;
import arc.haldun.mylibrary.R;
import arc.haldun.mylibrary.Sorting;
import arc.haldun.mylibrary.SplashScreenActivity;
import arc.haldun.mylibrary.main.LibraryActivity;
import arc.haldun.mylibrary.main.profile.ProfileActivity;

public class SettingsActivity extends AppCompatActivity implements View.OnClickListener {

    AlertDialog.Builder dialogBuilder;
    AlertDialog dialog;

    CardView cardLanguage, cardAccount,cardSortBooks;
    TextView tv_currentLang, tv_username, tv_email,tv_password;

    Toolbar actionbar;

    PreferencesTool preferencesTool;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);
        init(); // Init views

        //
        // Init actionbar
        //
        setSupportActionBar(actionbar);
        ActionBar supportActionbar = getSupportActionBar();
        if (supportActionbar != null) {
            supportActionbar.setTitle(getString(R.string.settings));
            supportActionbar.setDisplayHomeAsUpEnabled(true);
        }

        cardLanguage.setOnClickListener(this);
        cardAccount.setOnClickListener(this);
        cardSortBooks.setOnClickListener(this);

        PreferencesTool preferencesTool = new PreferencesTool(getSharedPreferences(PreferencesTool.NAME,MODE_PRIVATE));
        String lang = preferencesTool.getString(PreferencesTool.Keys.LANGUAGE);

        tv_currentLang.setText(lang);
        tv_username.setText(CurrentUser.user.getName());
        tv_email.setText(CurrentUser.user.getEMail());
        tv_password.setText(CurrentUser.user.decrypt());
    }

    @Override
    protected void onStop() {
        super.onStop();

        dialogBuilder = null;
        dialog = null;
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        if (item.getItemId() == android.R.id.home) {
            finish();
        }

        return super.onOptionsItemSelected(item);
    }

    private void init() {

        actionbar = findViewById(R.id.activity_settings_actionbar);

        cardLanguage = findViewById(R.id.activity_settings_card_language);
        cardAccount = findViewById(R.id.activity_settings_card_account);
        cardSortBooks = findViewById(R.id.activity_settings_card_sortingBook);

        tv_currentLang = findViewById(R.id.activity_settings_card_tv_currentLang);
        tv_username = findViewById(R.id.activity_settings_card_tv_userName);
        tv_email = findViewById(R.id.activity_settings_card_tv_email);
        tv_password = findViewById(R.id.activity_settings_card_tv_password);

        preferencesTool = new PreferencesTool(getSharedPreferences(PreferencesTool.NAME, MODE_PRIVATE));
    }

    @Override
    public void onClick(View view) {

        if (view.equals(cardLanguage)) showLanguageOptionsDialog();

        if (view.equals(cardAccount)) startActivity(new Intent(SettingsActivity.this, ProfileActivity.class));


        if (view.equals(cardSortBooks)) showSortingOptionsDialog();
    }

    private void showSortingOptionsDialog() {

        final int[] selectedSortingType = new int[1];

        String[] options = {getString(R.string.a_to_z), getString(R.string.z_to_a),
                getString(R.string.old_to_new), getString(R.string.new_to_old)};

        String currentSortingType = preferencesTool.getString(PreferencesTool.Keys.BOOK_SORTING_TYPE);

        dialogBuilder = new AlertDialog.Builder(this);
        dialogBuilder
                .setTitle(getString(R.string.sort))
                .setSingleChoiceItems(options, Sorting.getSortingTypeIndex(Sorting.Type.valueOf(currentSortingType)), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        selectedSortingType[0] = i;
                    }
                })
                .setPositiveButton(getString(R.string.ok), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                        Sorting.Type selectedType = Sorting.findSortingType(selectedSortingType[0]);
                        preferencesTool.setValue(PreferencesTool.Keys.BOOK_SORTING_TYPE, selectedType.toString());
                    }
                });

        dialog = dialogBuilder.create();
        dialog.show();
    }

    private void showLanguageOptionsDialog() {

        final int[] selectedLanguage = new int[1];

        String[] languages = {getString(R.string.turkish), getString(R.string.english), getString(R.string.german)};

        String currentLanguage = preferencesTool.getString(PreferencesTool.Keys.LANGUAGE);

        dialogBuilder = new AlertDialog.Builder(this);
        dialogBuilder
                .setTitle(getString(R.string.language))
                .setSingleChoiceItems(languages, Language.getCode(currentLanguage), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        selectedLanguage[0] = i;
                    }
                })
                .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {

                        PreferencesTool preferencesTool = new PreferencesTool(getSharedPreferences(PreferencesTool.NAME, Context.MODE_PRIVATE));
                        preferencesTool.setValue(PreferencesTool.Keys.LANGUAGE, Language.getLanguage(selectedLanguage[0]));

                        startActivity(new Intent(SettingsActivity.this, SplashScreenActivity.class));
                    }
                })
                .setNegativeButton(getString(R.string.cancel), null);
        dialog = dialogBuilder.create();
        dialog.show();
    }

    public static class Language {
        public final static int TURKISH = 0;
        public final static int ENGLISH = 1;
        public final static int GERMAN = 2;

        public static String getLanguage(int language) {

            switch (language) {

                case ENGLISH:
                    return "en";

                case GERMAN:
                    return "de";

                default:
                    return "tr";
            }
        }

        public static int getCode(String lang) {

            switch (lang) {

                case "en":
                    return ENGLISH;

                case "de":
                    return GERMAN;

                default:
                    return TURKISH;
            }
        }
    }
}