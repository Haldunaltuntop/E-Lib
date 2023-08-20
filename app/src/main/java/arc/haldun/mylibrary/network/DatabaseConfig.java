package arc.haldun.mylibrary.network;

import android.content.Context;

import java.io.InputStream;
import java.util.Scanner;

import arc.haldun.Cryptor;
import arc.haldun.mylibrary.R;

@SuppressWarnings("unused")
public class DatabaseConfig {

    private String databaseName;
    private String userName;
    private String password;

    public DatabaseConfig(Context context) {

        try {
            InputStream fis = context.getResources().openRawResource(R.raw.config); // Get file stream
            InputStream stream = Cryptor.decryptStream(fis); // Decrypt stream
            Scanner scanner = new Scanner(stream); // Read stream

            while (scanner.hasNext()) {

                if (scanner.next().equals("databasename:")) {
                    databaseName = scanner.next();
                }

                if (scanner.next().equals("username:")) {
                    userName = scanner.next();
                }

                if (scanner.next().equals("password:")) {
                    password = scanner.next();
                }
            }
        } catch (NoSuchMethodError | Exception error) {
            error.printStackTrace();
            //Tools.startErrorActivity(context);
        }
    }

    public String getDatabaseName() {
        return databaseName;
    }

    public void setDatabaseName(String databaseName) {
        this.databaseName = databaseName;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
