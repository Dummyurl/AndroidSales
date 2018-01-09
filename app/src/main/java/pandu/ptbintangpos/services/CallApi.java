package pandu.ptbintangpos.services;

/**
 * Created by PanduFarras on 01/01/2018.
 */

import android.util.Log;

import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;


/**
 * Created by Acer on 12/31/2017.
 */

public class CallApi {
    final OkHttpClient client = new OkHttpClient();
    public void getRequest(String url) {
        Request request = new Request.Builder()
                .url(url)
                .get()
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                String mMessage = e.getMessage().toString();
                Log.i("failure Response", mMessage);
                //call.cancel();
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {

                Log.i("success", "suscess");
                String mMessage = response.body().string();
                if (response.isSuccessful()) {
                    try {
                        JSONObject json = new JSONObject(mMessage);
                        final String serverResponse = json.getString("Your Index");

                    } catch (Exception e) {
                        e.printStackTrace();
                    }

                }
            }
        });
    }
}