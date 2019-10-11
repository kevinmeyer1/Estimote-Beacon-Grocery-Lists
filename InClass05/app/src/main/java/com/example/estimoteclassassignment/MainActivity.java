package com.example.estimoteclassassignment;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.estimote.coresdk.common.requirements.SystemRequirementsChecker;
import com.estimote.coresdk.observation.region.beacon.BeaconRegion;
import com.estimote.coresdk.service.BeaconManager;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {

    //private ProximityObserver proximityObserver;

    private BeaconManager beaconManager;
    private BeaconRegion region;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        SystemRequirementsChecker.checkWithDefaultDialogs(this);

        Button button = findViewById(R.id.btnRequest);

        View.OnClickListener btnClick = new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                System.out.println("hello");

                String major = "15212";
                String minor = "31506";

                try {
                    getItems(major, minor);
                } catch (IOException e) {
                    System.out.println(e.getLocalizedMessage());
                }
            }

        };

        button.setOnClickListener(btnClick);


        /*

        beaconManager = new BeaconManager(this);
        region = new BeaconRegion("ranged region",
                UUID.fromString("B9407F30-F5F8-466E-AFF9-25556B57FE6D"), null, null);

                */


        /*EstimoteCloudCredentials cloudCredentials = new EstimoteCloudCredentials(getString(R.string.estimoteAppId), getString(R.string.estimoteAppToken));

        this.proximityObserver = new ProximityObserverBuilder(getApplicationContext(), cloudCredentials)
                .onError(new Function1<Throwable, Unit>() {
                    @Override
                    public Unit invoke(Throwable throwable) {
                        return null;
                    }
                })
                .withBalancedPowerMode()
                .build();*/
    }

    public void getItems(String major, String minor) throws IOException {
        System.out.println("hello");
        MediaType MEDIA_TYPE = MediaType.parse("application/json");
        String url = "http://10.0.2.2:3000/getItems";
        JSONObject json = new JSONObject();

        try {
            json.put("major", major);
            json.put("minor", minor);
        } catch (JSONException e) {
            System.out.println(e.getLocalizedMessage());
        }

        OkHttpClient client = new OkHttpClient();

        RequestBody body = RequestBody.create(MEDIA_TYPE, json.toString());

        Request request = new Request.Builder()
                .url(url)
                .post(body)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                String mMessage = e.getMessage().toString();
                System.out.println(mMessage);
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {

                String body = response.body().string();
                JSONArray jsonData;

                try {
                    jsonData = new JSONArray(body);

                    for (int i = 0; i < jsonData.length(); i++) {
                        System.out.println(jsonData.get(i).toString());
                    }
                } catch (JSONException e) {
                    System.out.println(e.getLocalizedMessage());
                }

                System.out.println(response.code());
                System.out.println(response.body().toString());
            }
        });
    }
}
