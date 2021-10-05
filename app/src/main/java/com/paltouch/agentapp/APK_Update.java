package com.paltouch.agentapp;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

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
    // Progress Dialog
    private ProgressDialog pDialog;

    // Progress dialog type (0 - for Horizontal progress bar)
    public static final int progress_bar_type = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_apk_update);


        textView2 = (TextView) findViewById(R.id.textView2);
        btnUpdate = (Button)findViewById(R.id.btnUpdate);
        btnUpdate.setOnClickListener(new View.OnClickListener() {

            @RequiresApi(api = Build.VERSION_CODES.R)
            @Override
            public void onClick(View v) {
                // TODO Auto-generated method stub
                // If you have access to the external storage, do whatever you need
                new DownloadFilesTask().execute();
                /*
                if (Environment.isExternalStorageManager()){
                    File file = new File(Environment.getExternalStorageDirectory() + "/Intro-release.apk");
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
                 */
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

    private void installApk(View view){
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
        Uri uri = Uri.fromFile(new File(Environment.getExternalStorageDirectory(),"Intro-release.apk"));
        intent.setDataAndType(uri, "application/vnd.android.package-archive");
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);

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

        File file = new File(Environment.getExternalStorageDirectory() + "/Intro-release.apk");
        file.delete();
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
                String ip = GlobalVariables.apk_download_link;
                Session session = jsch.getSession("ateya", ip, 1989);
                session.setTimeout(30000000);
                java.util.Properties config = new java.util.Properties();
                config.put("StrictHostKeyChecking", "no");
                config.put("PreferredAuthentications", "password");
                session.setConfig(config);
                session.setPassword("intelinside");
                session.connect();

                ChannelSftp sftpChannel = (ChannelSftp) session.openChannel("sftp");
                sftpChannel.connect();

                File sdcard = Environment.getExternalStorageDirectory();
                File file = new File(sdcard, "Intro-release.apk");
                FileOutputStream out = new FileOutputStream(file);
                long filesize = sftpChannel.lstat("Intro-release.apk").getSize();

                //sftpChannel.get("Intro-release.apk", out, monitor);
                //Toast.makeText(APK_Update.this,"Starting Download",Toast.LENGTH_LONG).show();
                InputStream in = sftpChannel.get("Intro-release.apk");
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
                sftpChannel.exit();
                session.disconnect();
            } catch (JSchException e) {
                e.printStackTrace();
            } catch (SftpException e) {
                e.printStackTrace();
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
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
            //installApk(btnUpdate);
        }

    }
}