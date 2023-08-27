package arc.haldun.mylibrary.main;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import arc.haldun.database.objects.Book;
import arc.haldun.database.objects.CurrentUser;
import arc.haldun.database.objects.User;
import arc.haldun.mylibrary.R;

public class BookDetailsActivity extends AppCompatActivity implements View.OnClickListener {

    Book currentBook;
    String ownerName;

    EditText et_id, et_bookname, et_author, et_owner;
    Button btn_save;
    Toolbar actionbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_book_details);
        initCurrentBook(); // Get current book
        init(); // Init views

        //
        // Init actionbar
        //
        setSupportActionBar(actionbar);
        ActionBar supportActionbar = getSupportActionBar();
        if (supportActionbar != null) {
            supportActionbar.setTitle(getString(R.string.book_details));
            supportActionbar.setDisplayHomeAsUpEnabled(true);
        }


        if (!CurrentUser.user.getPriority().equals(User.SUPERUSER)) { // Check superuser
            et_id.setEnabled(false);
            et_bookname.setEnabled(false);
            et_author.setEnabled(false);
            et_owner.setEnabled(false);
            btn_save.setVisibility(View.GONE);
        }

        et_id.setText(String.valueOf(currentBook.getId()));
        et_bookname.setText(currentBook.getName());
        et_author.setText(currentBook.getAuthor());
        et_owner.setText(ownerName);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {

        if (item.getItemId() == android.R.id.home) finish();

        return true;
    }

    @Override
    public void onClick(View view) {

        if (view.equals(btn_save)) {
            // TODO: save book info changes
        }
    }

    private void init() {
        et_id = findViewById(R.id.activity_book_details_et_bookid);
        et_bookname = findViewById(R.id.activity_book_details_et_bookname);
        et_author = findViewById(R.id.activity_book_details_et_author);
        et_owner = findViewById(R.id.activity_book_details_et_owner);
        btn_save = findViewById(R.id.activity_book_details_btn_save);
        actionbar = findViewById(R.id.activity_book_details_actionbar);
    }

    private void initCurrentBook() {
        Bundle extras = getIntent().getExtras();

        if (extras != null) {
            int id = extras.getInt("id");
            String name = extras.getString("name");
            String author = extras.getString("author");
            ownerName = extras.getString("owner");

            currentBook = new Book(id, name, author);
        } else {
            Toast.makeText(this, "Kitap seçilmemiş", Toast.LENGTH_SHORT).show();
            finish();
        }
    }
}