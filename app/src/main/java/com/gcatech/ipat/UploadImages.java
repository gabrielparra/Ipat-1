package com.gcatech.ipat;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONObject;

/**
 * Created by Javier on 17/4/2017.
 */

public class UploadImages extends Activity implements View.OnClickListener {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.upload_images);



        btnAddPhots = (Button) findViewById(R.id.btnAddPhots);

        btnAddPhots.setOnClickListener(this);

    }


    private LinearLayout lnrImages;
    private Button btnAddPhots;
    private Button btnSaveImages;
    private ArrayList<String> imagesPathList;
    private Bitmap yourbitmap;
    private Bitmap resized;
    private final int PICK_IMAGE_MULTIPLE = 1;


    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btnAddPhots:
                Intent intent = new Intent(UploadImages.this, CustomPhotoGalleryActivity.class);
                startActivityForResult(intent, PICK_IMAGE_MULTIPLE);
                break;

        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            if (requestCode == PICK_IMAGE_MULTIPLE) {
                imagesPathList = new ArrayList<String>();
                String[] imagesPath = data.getStringExtra("data").split("\\|");

                for (int i = 0; i < imagesPath.length; i++) {

                    //yourbitmap = BitmapFactory.decodeFile(imagesPath[i]);
                    final File file = new File(imagesPath[i]);
                    int size = (int) file.length();
                    byte[] bytes = new byte[size];
                    try {
                        BufferedInputStream buf = new BufferedInputStream(new FileInputStream(file));
                        buf.read(bytes, 0, bytes.length);
                        buf.close();
                    } catch (FileNotFoundException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    } catch (IOException e) {
                        // TODO Auto-generated catch block
                        e.printStackTrace();
                    }

                    final String base64String = Base64.encodeToString(bytes, Base64.DEFAULT);
                    final String txtNombreMission = ((EditText) findViewById(R.id.txtNombreMisionCarga)).getText().toString();
                    final Activity context = this;
                    new AsyncTask<Void, Void, String>() {

                        @Override
                        protected String doInBackground(Void... params) {

                            InputStream inputStream = null;
                            String result = "";
                            try {

                                // 1. create HttpClient
                                HttpClient httpclient = new DefaultHttpClient();

                                // 2. make POST request to the given URL
                                HttpPost httpPost = new HttpPost("http://192.168.0.20:8080/api/File");

                                String json = "";

                                // 3. build jsonObject
                                JSONObject jsonObject = new JSONObject();
                                jsonObject.accumulate("ContainerFileName", "Mission_" + txtNombreMission);
                                jsonObject.accumulate("NameFile", file.getName());
                                jsonObject.accumulate("bytesFile", base64String);
                                jsonObject.accumulate("TokenNotificationPushUser", new utils.PreferenceUtils(getApplicationContext()).GetFromPreferences("tokenNotificationPush", String.class));
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
                                HttpResponse httpResponse = httpclient.execute(httpPost);

                                ((Activity) context).runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Toast.makeText(context, "Se cargaron las fotos correctamente.", Toast.LENGTH_LONG).show();
                                    }
                                });


                            } catch (Exception e) {
                                e.printStackTrace();
                            }

                            return  "ok";
                        }

                        @Override
                        protected void onPostExecute(String result) {
                            super.onPostExecute(result);

                        }
                    }.execute();

                }
            }
        }

    }

}

