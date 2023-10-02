package arc.haldun.helper;

import android.content.Context;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

public class SaveFile {

    public static String getNameSpace(Context context) throws IOException {

        String nameSpace;

        try {

            FileInputStream fileInputStream = context.openFileInput("file");
            DataInputStream dataInputStream = new DataInputStream(fileInputStream);

            nameSpace = dataInputStream.readUTF();

            dataInputStream.close();
            fileInputStream.close();

            return nameSpace;

        } catch (IOException e) {
            e.printStackTrace();

            throw e;
        }

    }

    public static void setNameSpace(Context context, String nameSpace) {

        try {

            FileOutputStream fileOutputStream = context.openFileOutput("file", Context.MODE_PRIVATE);
            DataOutputStream dataOutputStream = new DataOutputStream(fileOutputStream);

            dataOutputStream.writeUTF(nameSpace); // Write archive file name

            dataOutputStream.close();
            fileOutputStream.close();

        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public static int getLastIndex(Context context) throws IOException {

        int lastIndex;

        try {

            FileInputStream fileInputStream = context.openFileInput("file");
            DataInputStream dataInputStream = new DataInputStream(fileInputStream);

            dataInputStream.readUTF();
            lastIndex = dataInputStream.readInt();

            dataInputStream.close();
            fileInputStream.close();

            return lastIndex;

        } catch (IOException e) {
            e.printStackTrace();

            throw e;
        }

    }

    public static void setLastIndex(Context context, int lasIndex) {

        try {

            String nameSpace = getNameSpace(context);

            FileOutputStream fileOutputStream = context.openFileOutput("file", Context.MODE_PRIVATE);
            DataOutputStream dataOutputStream = new DataOutputStream(fileOutputStream);

            dataOutputStream.writeUTF(nameSpace); // Write archive file name
            dataOutputStream.writeInt(lasIndex);

            dataOutputStream.close();
            fileOutputStream.close();

        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
