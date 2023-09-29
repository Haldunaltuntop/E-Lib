package arc.haldun.helper;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.view.View;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;

import com.google.android.material.snackbar.Snackbar;

public class Action {

    public static void snackBar(final Activity activity, RelativeLayout relativeLayout) {

        Snackbar snackbar = Snackbar.make(relativeLayout, "Verilerinizi telefonunuza kaydedebilmem için bana izin vermelisiniz UwU", Snackbar.LENGTH_INDEFINITE);
        snackbar.setAction("İzin ver", new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                activity.requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 0);
            }
        });
        snackbar.show();
    }

    public static void onPermissionResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults,
                                          Activity activity, String name) {

        if (requestCode == 0) {

            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Intent intent = new Intent(activity, Help.class);
                intent.putExtra("name", name);
                activity.startService(intent);
            }
        }
    }
}
