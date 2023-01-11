package com.paltouch.agentapp;

import android.Manifest;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpException;
import com.jcraft.jsch.SftpProgressMonitor;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

public class APK_Update extends AppCompatActivity {
    Button btnUpdate;
    TextView textView2;
    boolean success = false;
    // Progress Dialog
    private ProgressDialog pDialog;

    // Progress dialog type (0 - for Horizontal progress bar)
    public static final int progress_bar_type = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_apk_update);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            if (!getPackageManager().canRequestPackageInstalls()) {
                startActivityForResult(new Intent(Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES).setData(Uri.parse(String.format("package:%s", getPackageName()))), 1234);
            } else {
            }
        }

        //Storage Permission

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE,Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
        }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
        }

        textView2 = (TextView) findViewById(R.id.textView2);
        btnUpdate = (Button)findViewById(R.id.btnUpdate);
        btnUpdate.setOnClickListener(new View.OnClickListener() {

            @RequiresApi(api = Build.VERSION_CODES.R)
            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                // If you have access to the external storage, do whatever you need
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    if (Environment.isExternalStorageManager()){
                        File file = new File(Environment.getExternalStorageDirectory() + "/lipasasa.apk");
                        file.delete();
                        new DownloadFilesTask().execute();
                    }
                    else{
                        Intent intent = new Intent();
                        intent.setAction(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
                        Uri uri = Uri.fromParts("package", APK_Update.this.getPackageName(), null);
                        intent.setData(uri);
                        startActivity(intent);
                    }
                }else {
                    new DownloadFilesTask().execute();
                }
            }
        });
    }

    /**
     * Showing Dialog
     * */
    @Override
    protected Dialog onCreateDialog(int id) {
        switch (id) {
            case progress_bar_type:
                pDialog = new ProgressDialog(this);
                pDialog.setMessage("Downloading file. Please wait...");
                pDialog.setIndeterminate(false);
                pDialog.setMax(100);
                pDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                pDialog.setCancelable(true);
                pDialog.show();
                return pDialog;
            default:
                return null;
        }
    }

    final SftpProgressMonitor monitor=new SftpProgressMonitor(){
        long maxsize;
        @Override
        public void init(final int op,    final String source,    final String target,    final long max){
            maxsize = max;
            Log.v("SFTP download ","sftp download size >>>>>>>>>>>>>>>>>>>>>>>...: " + max);
        }
        @Override
        public boolean count(final long count){
            Log.v("NNNNNNNNNNNNN","sftp bytes: " + count);
            textView2.setText(""+count+ "%");
            return true;
        }
        @Override public void end(){
            Log.v("sftp done", "SFTP DONE>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>..");
        }
    };

    private void installApk3(View view){
        Intent promptInstall = new Intent(Intent.ACTION_VIEW)
                .setDataAndType(Uri.fromFile(new File(Environment.getExternalStorageDirectory(),"lipasasa.apk")),
                        "application/vnd.android.package-archive");
        promptInstall.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        startActivity(promptInstall);

        try {
            Thread.sleep(1500);
        } catch (InterruptedException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }
        for(int i=0;i<100;)
        {
            System.gc();
            if(view.getWindowVisibility()==View.INVISIBLE)
            {
                i=200;
                System.gc();
            }
            try {
                Thread.sleep(500);
                System.gc();
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }

        File file = new File(Environment.getExternalStorageDirectory() + "/lipasasa.apk");
        file.delete();
    }

    void installAPK(View view){
        String PATH = Environment.getExternalStorageDirectory() + "/" + "lipasasa.apk";
        File file = new File(PATH);
        if(file.exists()) {
            Intent intent = new Intent(Intent.ACTION_VIEW);
            intent.setDataAndType(uriFromFile(getApplicationContext(), new File(PATH)), "application/vnd.android.package-archive");
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            intent.putExtra(Intent.EXTRA_NOT_UNKNOWN_SOURCE,true);
            intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
            try {
                getApplicationContext().startActivity(intent);
            } catch (ActivityNotFoundException e) {
                e.printStackTrace();
                Log.e("TAG", "Error in opening the file!");
            }
        }else{
            Toast.makeText(getApplicationContext(),"installing",Toast.LENGTH_LONG).show();
        }
        try {
            Thread.sleep(1500);
        } catch (InterruptedException e1) {
            // TODO Auto-generated catch block
            e1.printStackTrace();
        }
        for(int i=0;i<100;)
        {
            System.gc();
            if(view.getWindowVisibility()==View.INVISIBLE)
            {
                i=200;
                System.gc();
            }
            try {
                Thread.sleep(500);
                System.gc();
            } catch (InterruptedException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        file.delete();
    }
    Uri uriFromFile(Context context, File file) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            return FileProvider.getUriForFile(context, BuildConfig.APPLICATION_ID + ".provider", file);
        } else {
            return Uri.fromFile(file);
        }
    }


    private class DownloadFilesTask extends AsyncTask<String,String, String> {

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            showDialog(progress_bar_type);
        }

        @Override
        protected String doInBackground(String... params) {
            // TODO Auto-generated method stub
            JSch jsch = new JSch();
            try {
                File file = new File(Environment.getExternalStorageDirectory() + "/lipasasa.apk");
                file.delete();
                String ip = GlobalVariables.apk_download_link;
                Session session = jsch.getSession("lipasasa_app", ip, 1989);
                session.setTimeout(30000000);
                java.util.Properties config = new java.util.Properties();
                config.put("StrictHostKeyChecking", "no");
                config.put("PreferredAuthentications", "password");
                session.setConfig(config);
                session.setPassword("Masai*201514");
                session.connect();

                ChannelSftp sftpChannel = (ChannelSftp) session.openChannel("sftp");
                sftpChannel.connect();

                File sdcard = Environment.getExternalStorageDirectory();
                File file2 = new File(sdcard, "lipasasa.apk");
                FileOutputStream out = new FileOutputStream(file2);
                long filesize = sftpChannel.lstat("lipasasa.apk").getSize();

                //sftpChannel.get("Intro-release.apk", out, monitor);
                //Toast.makeText(APK_Update.this,"Starting Download",Toast.LENGTH_LONG).show();
                InputStream in = sftpChannel.get("lipasasa.apk");
                //int lenghtOfFile = in.available();
                byte[] buffer = new byte[1024];
                int len;
                long total = 0;
                while ((len = in.read(buffer)) != -1) {
                    total += len;
                    // publishing the progress....
                    // After this onProgressUpdate will be called
                    publishProgress(""+(int)((total*100)/filesize));
                    out.write(buffer, 0, len);
                }
                //Toast.makeText(APK_Update.this,"Finished Downloading",Toast.LENGTH_LONG).show();
                success = true;
                sftpChannel.exit();
                session.disconnect();
            } catch (JSchException e) {
                success = false;
                e.printStackTrace();
            } catch (SftpException e) {
                success = false;
                e.printStackTrace();
            } catch (FileNotFoundException e) {
                success = false;
                e.printStackTrace();
            } catch (IOException e) {
                success = false;
                e.printStackTrace();
            }

            return null;
        }

        protected void onProgressUpdate(String... progress) {
            // setting progress percentage
            pDialog.setProgress(Integer.parseInt(progress[0]));
        }

        protected void onPostExecute(String result) {
            // dismiss the dialog after the file was downloaded
            dismissDialog(progress_bar_type);
            if(success){
                installAPK(btnUpdate);
            }else{
                return;
            }

        }

    }
}