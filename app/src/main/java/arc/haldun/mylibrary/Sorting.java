package arc.haldun.mylibrary;

import java.text.Collator;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import arc.haldun.database.objects.Book;

public class Sorting {

    public static void sort(Book[] books, Type sortingType) {
        for(int increment = books.length / 2; increment > 0; increment = increment == 2 ? 1 : (int)Math.round((double)increment / 2.2)) {
            for(int i = increment; i < books.length; ++i) {
                Book temp = books[i];

                for(int j = i; j >= increment && compare(books[j - increment], temp, sortingType) > 0; j -= increment) {
                    books[j] = books[j - increment];
                    books[j - increment] = temp;
                }
            }
        }

    }

    private static int compare(Book book1, Book book2, Type sortingType) {
        Collator collator = Collator.getInstance(new Locale("tr", "TR"));

        switch (sortingType) {

            case Z_TO_A_BOOK_NAME:
                return collator.compare(book2.getName(), book1.getName());

            case OLD_TO_NEW:
                return collator.compare(book1.getRegDate().getDateTime(), book2.getRegDate().getDateTime());

                // TODO: Fix bug

            case NEW_TO_OLD:
                return collator.compare(book2.getRegDate().getDateTime(), book1.getRegDate().getDateTime());

                // TODO: Fix bug

            case A_TO_Z_AUTHOR_NAME:
                return collator.compare(book1.getAuthor(), book2.getAuthor());

            case Z_TO_A_AUTHOR_NAME:
                return collator.compare(book2.getAuthor(), book1.getAuthor());

            default: // A_TO_Z and others
                return collator.compare(book1.getName(), book2.getName());
        }
    }

    public static int getSortingTypeIndex(Type type) {

        switch (type) {

            case Z_TO_A_BOOK_NAME:
                return 1;

            case OLD_TO_NEW:
                return 2;

            case NEW_TO_OLD:
                return 3;

            case A_TO_Z_AUTHOR_NAME:
                return 4;

            case Z_TO_A_AUTHOR_NAME:
                return 5;

            default:
                return 0;
        }
    }

    public static Type findSortingType(int index) {

        switch (index) {

            case 1:
                return Type.Z_TO_A_BOOK_NAME;

            case 2:
                return Type.OLD_TO_NEW;

            case 3:
                return Type.NEW_TO_OLD;

            case 4:
                return Type.A_TO_Z_AUTHOR_NAME;

            case 5:
                return Type.Z_TO_A_AUTHOR_NAME;

            default: // Also index = 0
                return Type.A_TO_Z_BOOK_NAME;
        }
    }

    public enum Type {
        A_TO_Z_BOOK_NAME,
        Z_TO_A_BOOK_NAME,
        OLD_TO_NEW,
        NEW_TO_OLD,
        A_TO_Z_AUTHOR_NAME,
        Z_TO_A_AUTHOR_NAME
    }
}
