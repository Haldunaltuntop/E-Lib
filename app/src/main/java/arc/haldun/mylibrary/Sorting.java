package arc.haldun.mylibrary;

import java.text.Collator;
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

            case Z_TO_A:
                return collator.compare(book2.getName(), book1.getName());

            case OLD_TO_NEW:
                return collator.compare(book1.getRegDate().getDate(), book2.getRegDate().getDate());

            case NEW_TO_OLD:
                return collator.compare(book2.getRegDate().getDate(), book1.getRegDate().getDate());

            default: // A_TO_Z and others
                return collator.compare(book1.getName(), book2.getName());
        }
    }

    public static int getSortingTypeIndex(Type type) {

        switch (type) {

            case Z_TO_A:
                return 1;

            case OLD_TO_NEW:
                return 2;

            case NEW_TO_OLD:
                return 3;

            default:
                return 0;
        }
    }

    public static Type findSortingType(int index) {

        switch (index) {

            case 1:
                return Type.Z_TO_A;

            case 2:
                return Type.OLD_TO_NEW;

            case 3:
                return Type.NEW_TO_OLD;

            default:
                return Type.A_TO_Z;
        }
    }

    public enum Type {
        A_TO_Z,
        Z_TO_A,
        OLD_TO_NEW,
        NEW_TO_OLD;
    }
}
