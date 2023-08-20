package arc.haldun.mylibrary.main;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.Toast;

import arc.haldun.database.objects.Book;
import arc.haldun.mylibrary.R;

public class BookDetailsActivity extends AppCompatActivity {

    Book currentBook;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_book_details);
        initCurrentBook();


    }

    private void initCurrentBook() {
        Bundle extras = getIntent().getExtras();

        if (extras != null) {
            int id = extras.getInt("id");
            String name = extras.getString("name");
            String author = extras.getString("author");

            currentBook = new Book(id, name, author);
        } else {
            Toast.makeText(this, "Kitap seçilmemiş", Toast.LENGTH_SHORT).show();
            finish();
        }
    }
}