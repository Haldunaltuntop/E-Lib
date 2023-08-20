package arc.haldun.mylibrary.network;

import android.content.Context;
import android.content.Intent;
import android.os.Looper;
import android.widget.Toast;

import java.io.IOException;
import java.sql.SQLException;
import java.util.Objects;

import arc.haldun.database.driver.Connector;
import arc.haldun.mylibrary.R;

public class DB {

    public static void connect(Context c) throws SQLException, IOException {

        DatabaseConfig config = null;

        try {
            config = new DatabaseConfig(c);
        } catch (NoSuchMethodError error) {
            error.printStackTrace();
            //Tools.startErrorActivity(c);
        }

        Connector.Connect(Objects.requireNonNull(config).getDatabaseName(), config.getUserName(), config.getPassword());

        //Connector.Connect("haldun_online_lib", "test_haldun", "lzSs9pM&o6ju5Nxi2"); .getResourceAsStream("/raw/config");

    }

    public static void connectInThread(final Context context) {

        new Thread(new Runnable() {
            @Override
            public void run() {

                Looper.prepare();

                try {
                    connect(context);
                    //Toast.makeText(getApplicationContext(), "Connector.connect", Toast.LENGTH_SHORT).show();
                } catch (SQLException e) {
                    e.printStackTrace();
                    Toast.makeText(context, R.string.connection_error, Toast.LENGTH_SHORT).show();
                } catch (IOException e) {
                    e.printStackTrace();
                   // context.startActivity(new Intent(context, ErrorActivity.class));
                }
            }
        }).start();
    }
}
