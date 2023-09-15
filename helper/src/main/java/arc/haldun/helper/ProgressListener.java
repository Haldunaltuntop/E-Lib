package arc.haldun.helper;

import android.util.Log;

import org.apache.commons.net.io.CopyStreamAdapter;
import org.apache.commons.net.io.CopyStreamEvent;

import java.io.File;

public class ProgressListener extends CopyStreamAdapter {

    private long fileSize;
    long totalBytesTransferred;

    public ProgressListener(File file) {
        this.fileSize = file.length();
    }

    @Override
    public void bytesTransferred(CopyStreamEvent event) {
        bytesTransferred(event.getTotalBytesTransferred(), event.getBytesTransferred(), event.getStreamSize());
    }

    @Override
    public void bytesTransferred(long totalBytesTransferred, int bytesTransferred, long streamSize) {

        if (fileSize == 0 && streamSize > 0) {
            fileSize = streamSize;
        }

        this.totalBytesTransferred = streamSize;

        if (fileSize > 0) {
            int percent = (int) (totalBytesTransferred * 100 / fileSize);
            System.out.println("Tamamlanma yüzdesi: %" + percent);
            Log.e("Store", "Tamamlanma yüzdesi: %" + percent);
        }
    }
}
