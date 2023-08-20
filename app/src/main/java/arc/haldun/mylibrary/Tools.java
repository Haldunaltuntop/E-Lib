package arc.haldun.mylibrary;

import android.content.Context;
import android.widget.Toast;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import arc.haldun.database.driver.Connector;

public class Tools {

    public static void makeText(Context c, String message) {
        Toast.makeText(c, message, Toast.LENGTH_LONG).show();
    }

    public static Update hasUpdate(Context context) {

        Update update = new Update();

        try {

            String sql = "SELECT * FROM updates";
            Statement statement = Connector.connection.createStatement();
            ResultSet resultSet = statement.executeQuery(sql);

            while (resultSet.next()) {

                int versionCode = resultSet.getInt("VersionCode");

                if (versionCode > BuildConfig.VERSION_CODE) {
                    update.setHasNew(true);
                    update.setVersionCode(versionCode);
                    break;
                }
            }
        } catch (SQLException e) {
            // TODO Tools.startErrorActivity(context);
        }

        return update;
    }

    public static class Update {

        private boolean hasNew = false;
        private int versionCode;

        public boolean hasNew() {
            return hasNew;
        }

        public void setHasNew(boolean hasNew) {
            this.hasNew = hasNew;
        }

        public int getVersionCode() {
            return versionCode;
        }

        public void setVersionCode(int versionCode) {
            this.versionCode = versionCode;
        }
    }
}
