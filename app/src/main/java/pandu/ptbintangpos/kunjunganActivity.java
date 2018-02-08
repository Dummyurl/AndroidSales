package pandu.ptbintangpos;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.android.volley.Request;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import pandu.ptbintangpos.AppController;
import pandu.ptbintangpos.Config.BaseURL;
import pandu.ptbintangpos.util.Session_management;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;


public class kunjunganActivity extends AppCompatActivity {

    Button buttonChoose, buttonUpload;
    ImageView imageView;
    EditText namakunj, tokokunj, tokopemilik, alamatkunj, ketkunj;
    Bitmap bitmap, decoded;
    int success;
    Intent intent;
    Uri fileUri;
    public final int REQUEST_CAMERA = 0;
    public final int SELECT_FILE = 1;
    int bitmap_size = 60; // range 1 - 100
    int max_resolution_image = 800;

    private static final String TAG = kunjunganActivity.class.getSimpleName();

    private String UPLOAD_URL = BaseURL.BASE_URL+"kunjungan.php";

    private static final String TAG_SUCCESS = "success";
    private static final String TAG_MESSAGE = "message";
    private String KEY_IMAGE = "image";
    private String KEY_SALES = "sales";
    private String KEY_ID = "user_id";
    private String KEY_KETERANGAN = "keterangan";
    private String KEY_TOKO = "toko";
    private String KEY_PEMILIK = "pemilik";
    private String KEY_ALAMAT = "alamat";
    private Session_management sessionManagement;

    String tag_json_obj = "json_obj_req";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_kunjungan);


        buttonChoose = (Button) findViewById(R.id.buttonChoose);
        buttonUpload = (Button) findViewById(R.id.buttonUpload);

        //namakunj = (EditText) findViewById(R.id.namakunj);
        tokokunj = (EditText) findViewById(R.id.tokokunj);
        tokopemilik = (EditText) findViewById(R.id.tokoPemilik);
        alamatkunj = (EditText) findViewById(R.id.alamatkunj);
        ketkunj = (EditText) findViewById(R.id.ketkunj);
        sessionManagement = new Session_management(this);

        imageView = (ImageView) findViewById(R.id.imageView);

        buttonChoose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                selectImage();
            }
        });

        buttonUpload.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                uploadImage();
            }
        });
    }

    public String getStringImage(Bitmap bmp) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bmp.compress(Bitmap.CompressFormat.JPEG, bitmap_size, baos);
        byte[] imageBytes = baos.toByteArray();
        String encodedImage = Base64.encodeToString(imageBytes, Base64.DEFAULT);
        return encodedImage;
    }

    private void uploadImage() {
        //menampilkan progress dialog
        final ProgressDialog loading = ProgressDialog.show(this, "Uploading...", "Please wait...", false, false);
        StringRequest stringRequest = new StringRequest(Request.Method.POST, UPLOAD_URL,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        Log.e(TAG, "Response: " + response.toString());

                        try {
                            JSONObject jObj = new JSONObject(response);
                            success = jObj.getInt(TAG_SUCCESS);

                            if (success == 1) {
                                Log.e("v Add", jObj.toString());

                                Toast.makeText(kunjunganActivity.this, jObj.getString(TAG_MESSAGE), Toast.LENGTH_LONG).show();

                                kosong();

                            } else {
                                Toast.makeText(kunjunganActivity.this, jObj.getString(TAG_MESSAGE), Toast.LENGTH_LONG).show();
                            }
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }

                        //menghilangkan progress dialog
                        loading.dismiss();
                    }
                },
                new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        //menghilangkan progress dialog
                        loading.dismiss();

                        //menampilkan toast
                        Toast.makeText(kunjunganActivity.this, "Foto Kunjungan harus dimasukkan!", Toast.LENGTH_LONG).show();
                        Log.e(TAG, error.getMessage().toString());
                    }
                }) {
            @Override
            protected Map<String, String> getParams() {
                //membuat parameters
                Map<String, String> params = new HashMap<String, String>();

                //menambah parameter yang di kirim ke web servis
                params.put(KEY_IMAGE, getStringImage(decoded));
                params.put(KEY_ID, sessionManagement.getUserDetails().get(BaseURL.KEY_ID));
                params.put(KEY_SALES, sessionManagement.getUserDetails().get(BaseURL.KEY_NAME));
                //params.put(KEY_SALES, namakunj.getText().toString().trim());
                params.put(KEY_KETERANGAN, ketkunj.getText().toString().trim());
                params.put(KEY_TOKO, tokokunj.getText().toString().trim());
                params.put(KEY_PEMILIK, tokopemilik.getText().toString().trim());
                params.put(KEY_ALAMAT, alamatkunj.getText().toString().trim());

                //kembali ke parameters
                Log.e(TAG, "" + params);
                return params;
            }
        };

        AppController.getInstance().addToRequestQueue(stringRequest, tag_json_obj);
    }

    private void selectImage() {
        imageView.setImageResource(0);
        final CharSequence[] items = {"Camera", "Pilih dari Gallery",
                "Batal"};

        AlertDialog.Builder builder = new AlertDialog.Builder(kunjunganActivity.this);
        builder.setTitle("Ambil Foto!");
        builder.setIcon(R.mipmap.ic_logo_2);
        builder.setItems(items, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int item) {
                if (items[item].equals("Camera")) {
                    intent = new Intent(android.provider.MediaStore.ACTION_IMAGE_CAPTURE);
                    fileUri = getOutputMediaFileUri();
                    intent.putExtra(android.provider.MediaStore.EXTRA_OUTPUT, fileUri);
                    startActivityForResult(intent, REQUEST_CAMERA);
                } else if (items[item].equals("Pilih dari Gallery")) {
                    intent = new Intent();
                    intent.setType("image/*");
                    intent.setAction(Intent.ACTION_GET_CONTENT);
                    startActivityForResult(Intent.createChooser(intent, "Select Picture"), SELECT_FILE);
                } else if (items[item].equals("Batal")) {
                    dialog.dismiss();
                }
            }
        });
        builder.show();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        Log.e("onActivityResult", "requestCode " + requestCode + ", resultCode " + resultCode);

        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == REQUEST_CAMERA) {
                try {
                    Log.e("CAMERA", fileUri.getPath());

                    bitmap = BitmapFactory.decodeFile(fileUri.getPath());
                    setToImageView(getResizedBitmap(bitmap, max_resolution_image));
                } catch (Exception e) {
                    e.printStackTrace();
                }

            } else if (requestCode == SELECT_FILE && data != null && data.getData() != null) {
                try {
                    // mengambil gambar dari Gallery
                    bitmap = MediaStore.Images.Media.getBitmap(kunjunganActivity.this.getContentResolver(), data.getData());
                    setToImageView(getResizedBitmap(bitmap, max_resolution_image));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void kosong() {
        imageView.setImageResource(0);
        //namakunj.setText(null);
        tokokunj.setText(null);
        tokopemilik.setText(null);
        alamatkunj.setText(null);
        ketkunj.setText(null);


    }

    private void setToImageView(Bitmap bmp) {
        //compress image
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        bmp.compress(Bitmap.CompressFormat.JPEG, bitmap_size, bytes);
        decoded = BitmapFactory.decodeStream(new ByteArrayInputStream(bytes.toByteArray()));

        //menampilkan gambar yang dipilih dari camera/gallery ke ImageView
        imageView.setImageBitmap(decoded);
    }

    // fungsi resize image
    public Bitmap getResizedBitmap(Bitmap image, int maxSize) {
        int width = image.getWidth();
        int height = image.getHeight();

        float bitmapRatio = (float) width / (float) height;
        if (bitmapRatio > 1) {
            width = maxSize;
            height = (int) (width / bitmapRatio);
        } else {
            height = maxSize;
            width = (int) (height * bitmapRatio);
        }
        return Bitmap.createScaledBitmap(image, width, height, true);
    }
    public Uri getOutputMediaFileUri() {
        return Uri.fromFile(getOutputMediaFile());
    }

    private static File getOutputMediaFile() {

        // External sdcard location
        File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), "UPINDO JAYA");

        // Create the storage directory if it does not exist
        if (!mediaStorageDir.exists()) {
            if (!mediaStorageDir.mkdirs()) {
                Log.e("Monitoring", "Oops! Failed create Monitoring directory");
                return null;
            }
        }

        // Create a media file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
        File mediaFile;
        mediaFile = new File(mediaStorageDir.getPath() + File.separator + "IMG_Upindo_" + timeStamp + ".jpg");

        return mediaFile;
    }

}