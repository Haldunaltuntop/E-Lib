package arc.haldun.mylibrary.main.settings;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.preference.PreferenceFragmentCompat;
import androidx.preference.PreferenceManager;

import arc.haldun.mylibrary.PreferencesTool;
import arc.haldun.mylibrary.R;

public class SettingsActivity extends AppCompatActivity {

    Toolbar actionbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.settings_activity);
        if (savedInstanceState == null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.settings, new SettingsFragment())
                    .commit();
        }

        actionbar = findViewById(R.id.settings_activity_actionbar);
        setSupportActionBar(actionbar);
        ActionBar supportActionBar = getSupportActionBar();
        if (supportActionBar != null) {
            supportActionBar.setTitle(getString(R.string.settings));
            supportActionBar.setDisplayHomeAsUpEnabled(true);
        }

        //TODO make your oqn settings activity
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        if (item.getItemId() == android.R.id.home) {
            finish();
        }

        return true;
    }

    public static class SettingsFragment extends PreferenceFragmentCompat {
        @Override
        public void onCreatePreferences(Bundle savedInstanceState, String rootKey) {
            setPreferencesFromResource(R.xml.root_preferences, rootKey);

            SharedPreferences sharedPreferences = requireActivity().getSharedPreferences(PreferencesTool.NAME, MODE_PRIVATE);
            PreferenceManager.setDefaultValues(requireContext(), R.xml.root_preferences, false);
        }
    }
}