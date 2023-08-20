package arc.haldun.mylibrary.service;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

import androidx.annotation.Nullable;

import java.sql.SQLException;

import arc.haldun.database.database.haldun;
import arc.haldun.database.objects.CurrentUser;
import arc.haldun.database.objects.DateTime;

public class SetLastSeenService extends IntentService {

    public SetLastSeenService(String name) {
        super(name);
    }

    public SetLastSeenService() {
        super("Set Last Seen Service");
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {

        DateTime lastSeen = new DateTime();

        try {
            haldun.update.user.lastSeen(CurrentUser.user.getId(), lastSeen);
        } catch (SQLException e) {
            e.printStackTrace();
            Log.e("SetLastSeenService", "Son görülme veritabanına gönderilirken bir hata oluştu");
        } catch (NullPointerException e) {
            Log.e("SetLastSeenService", "CurrentUser.user nul idi. Mevcut kullanıcı çıkış yapmış olabilir.");
        }
    }
}