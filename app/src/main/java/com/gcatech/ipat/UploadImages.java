package com.gcatech.ipat;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.text.Editable;
import android.util.Base64;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.google.firebase.iid.FirebaseInstanceId;
import com.nguyenhoanglam.imagepicker.activity.ImagePicker;
import com.nguyenhoanglam.imagepicker.activity.ImagePickerActivity;
import com.nguyenhoanglam.imagepicker.model.Image;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONObject;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

public class UploadImages extends Activity implements View.OnClickListener {


    private Button btnAddPhots;
    private final int PICK_IMAGE_MULTIPLE = 1;
    private ProgressDialog dialogProgress = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.upload_images);
        btnAddPhots = (Button) findViewById(R.id.btnAddPhots);
        btnAddPhots.setOnClickListener(this);
        scanFolders();

    }

    private void scanFolders() {
        File rootFolder = new File(Environment.getExternalStorageDirectory().getPath() + "/Ipat/");
        File[] folders = rootFolder.listFiles();
        for (File folder : folders) {
            if (folder.isDirectory()) {
                scanFiles(folder);
            }
        }
    }

    private void scanFiles(File missionFolder) {
        for (File file : missionFolder.listFiles()) {
            if (file.isFile()) {
                galleryAddPic(file.getAbsolutePath());
            }
        }
    }

    private void galleryAddPic(String mCurrentPhotoPath) {
        Intent mediaScanIntent = new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
        File f = new File(mCurrentPhotoPath);
        Uri contentUri = Uri.fromFile(f);
        mediaScanIntent.setData(contentUri);
        this.sendBroadcast(mediaScanIntent);
    }


    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btnAddPhots:

                ImagePicker.create(this)
                        .folderMode(true) // folder mode (false by default)
                        .folderTitle("Seleccione") // folder selection title
                        .imageTitle("Click para seleccionar") // image selection title
                        .multi() // multi mode (default mode)
                        .limit(10) // max images can be selected (999 by default)
                        .showCamera(false) // show camera or not (true by default)
                        .imageDirectory("/Ipat") // dir1ectory name for captured image  ("Camera" folder by default)
                        .start(PICK_IMAGE_MULTIPLE); // start image picker activity with request code
                break;
        }

    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        final Activity context = this;
        if (resultCode == RESULT_OK) {
            try {
                if (requestCode == PICK_IMAGE_MULTIPLE) {
                    ArrayList<Image> images = data.getParcelableArrayListExtra(ImagePickerActivity.INTENT_EXTRA_SELECTED_IMAGES);
                    final int total = images.size();
                    int currentIndex = 0;
                    for (Image image : images) {
                        currentIndex++;
                        String imagePath = image.getPath();

                        String fileName = image.getName();
                        String[] nameParts = fileName.split("__");
                        final File file = new File(imagePath);
                        int size = (int) file.length();
                        byte[] bytes = new byte[size];
                        try {
                            BufferedInputStream buf = new BufferedInputStream(new FileInputStream(file));
                            buf.read(bytes, 0, bytes.length);
                            buf.close();
                        } catch (IOException e) {
                            Toast.makeText(UploadImages.this, e.getMessage(), Toast.LENGTH_LONG).show();
                        }

                        final String base64String = Base64.encodeToString(bytes, Base64.DEFAULT);
                        final String missionNameText = nameParts[0];

                        try {
                            final int finalCurrentIndex = currentIndex;
                            new AsyncTask<Void, Void, String>() {

                                @Override
                                protected String doInBackground(Void... params) {

                                    try {
                                        showLoading(context);
                                        // 1. create HttpClient
                                        HttpClient httpclient = new DefaultHttpClient();

                                        // 2. make POST request to the given URL
                                        String baseUrl = getString(R.string.apiBaseUrl);
                                        String apiUrl = String.format("%s%s", baseUrl, "api/File");
                                        HttpPost httpPost = new HttpPost(apiUrl);

                                        String json = "";

                                        // 3. build jsonObject
                                        JSONObject jsonObject = new JSONObject();
                                        jsonObject.accumulate("ContainerFileName", missionNameText);
                                        jsonObject.accumulate("NameFile", file.getName());
                                        jsonObject.accumulate("bytesFile", base64String);
                                        String token = FirebaseInstanceId.getInstance().getToken();
                                        jsonObject.accumulate("TokenNotificationPushUser", token);
                                        jsonObject.accumulate("FromDron", true);

                                        // 4. convert JSONObject to JSON to String
                                        json = jsonObject.toString();

                                        // ** Alternative way to convert Person object to JSON string usin Jackson Lib
                                        // ObjectMapper mapper = new ObjectMapper();
                                        // json = mapper.writeValueAsString(person);

                                        // 5. set json to StringEntity
                                        StringEntity se = new StringEntity(json);

                                        // 6. set httpPost Entity
                                        httpPost.setEntity(se);

                                        // 7. Set some headers to inform server about the type of the content
                                        httpPost.setHeader("Accept", "application/json");
                                        httpPost.setHeader("Content-type", "application/json");

                                        // 8. Execute POST request to the given URL
                                        httpclient.execute(httpPost);
                                        closeLoading(context);
                                        /*context.runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                Toast.makeText(context, "Se cargaron las fotos correctamente.", Toast.LENGTH_LONG).show();
                                            }
                                        });*/

                                    } catch (final Exception ex) {
                                        context.runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                Toast.makeText(context, ex.getMessage(), Toast.LENGTH_LONG).show();
                                            }
                                        });
                                    }

                                    return "ok";
                                }

                                @Override
                                protected void onPostExecute(String result) {
                                    super.onPostExecute(result);

                                    context.runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            String message = String.format("Imagen %s de %s enviada", finalCurrentIndex, total );
                                            Toast.makeText(UploadImages.this,message, Toast.LENGTH_LONG).show();
                                        }
                                    });


                                }
                            }.execute();
                        } catch (final Throwable t) {
                            context.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toast.makeText(context, t.getMessage(), Toast.LENGTH_LONG).show();
                                }
                            });
                        }

                    }
                }
            } catch (Throwable t) {
                Toast.makeText(context, t.getMessage(), Toast.LENGTH_LONG).show();
            }
        }

    }

    private void showLoading(final Context ctxUse) {
        ((Activity) ctxUse).runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (dialogProgress != null) {
                    dialogProgress.dismiss();

                }
                dialogProgress = new ProgressDialog(ctxUse);
                dialogProgress.setTitle("Transferencia");
                dialogProgress.setMessage("Transfiriendo fotos por favor espere..");
                dialogProgress.setCancelable(false); // disable dismiss by tapping outside of the dialog
                dialogProgress.show();
            }
        });
    }

    private void closeLoading(final Context ctxUse) {
        ((Activity) ctxUse).runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (dialogProgress != null) {
                    dialogProgress.dismiss();
                    dialogProgress = null;

                }
            }
        });
    }

}

