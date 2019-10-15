package com.example.estimoteclassassignment;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;

import com.estimote.coresdk.common.requirements.SystemRequirementsChecker;
import com.estimote.coresdk.observation.region.beacon.BeaconRegion;
import com.estimote.coresdk.recognition.packets.Beacon;
import com.estimote.coresdk.service.BeaconManager;
import com.estimote.proximity_sdk.api.EstimoteCloudCredentials;
import com.estimote.proximity_sdk.api.ProximityObserver;
import com.estimote.proximity_sdk.api.ProximityObserverBuilder;
import com.example.estimoteclassassignment.Model.Item;
import com.example.estimoteclassassignment.Model.MyAdapter;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import kotlin.Unit;
import kotlin.jvm.functions.Function1;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {

    private ProximityObserver proximityObserver;

    private BeaconManager beaconManager;
    private BeaconRegion region;

    private RecyclerView recyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.Adapter emptyAdapter;
    private RecyclerView.LayoutManager layoutManager;

    final ArrayList<Item> itemList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        SystemRequirementsChecker.checkWithDefaultDialogs(this);

        System.out.println("hello");

        //recycler view stuff
        recyclerView = findViewById(R.id.recyclerView);
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        mAdapter = new MyAdapter(itemList);
        recyclerView.setAdapter(mAdapter);


        beaconManager = new BeaconManager(this);

        beaconManager.setRangingListener(new BeaconManager.BeaconRangingListener() {
            @Override
            public void onBeaconsDiscovered(BeaconRegion beaconRegion, List<com.estimote.coresdk.recognition.packets.Beacon> beacons) {
                if (!beacons.isEmpty()) {
                    Log.d("demo", "onBeaconsDiscovered: " + beacons);

                    Beacon closestBeacon = beacons.get(0);
                    int major = closestBeacon.getMajor();
                    int minor = closestBeacon.getMinor();

                    Log.d("demo", "major: " + major + ", minor: " + minor);

                    //Sends request, calls function at bottom
                    getItems(major, minor, new Callback() {

                        @Override
                        public void onFailure(Call call, IOException e) {
                            String mMessage = e.getMessage();
                            System.out.println(mMessage);
                        }

                        @Override
                        public void onResponse(Call call, Response response) throws IOException {
                            String body = response.body().string();

                            try {
                                //Gets the JSONArray of items from server
                                JSONArray jsonResponseData = new JSONArray(body);

                                //remove old items from itemList
                                itemList.clear();

                                //put new items in itemList
                                for (int i = 0; i < jsonResponseData.length(); i++) {
                                    JSONObject obj = (JSONObject) jsonResponseData.get(i);
                                    Item currentItem = new Item(obj.getInt("discount"), obj.getString("name"), obj.getString("photo"), obj.getDouble("price"), obj.getString("region"));
                                    itemList.add(currentItem);
                                }
                            } catch (JSONException e) {
                                System.out.println(e.getLocalizedMessage());
                            }

                            //repopulate the list on the app
                            runOnUiThread(new Runnable() {

                                @Override
                                public void run() {

                                    mAdapter.notifyDataSetChanged();

                                }
                            });
                        }
                    });
                }
            }

        });

        //These are hardcoded but should be pulled from the beacons
        String major = "30462";
        String minor = "43265";

    }

    @Override
    protected void onResume() {
        super.onResume();

        System.out.println("in on resume");

        region = new BeaconRegion("ranged region",
                UUID.fromString("B9407F30-F5F8-466E-AFF9-25556B57FE6D"), null, null);

        SystemRequirementsChecker.checkWithDefaultDialogs(this);

        beaconManager.connect(new BeaconManager.ServiceReadyCallback() {
            @Override
            public void onServiceReady() {
                beaconManager.startRanging(region);
            }
        });
    }

    @Override
    protected void onPause() {
        beaconManager.stopRanging(region);

        super.onPause();
    }

    //sends request to heroku for the list of items per major and minor of beacon
    public void getItems(int major, int minor, final Callback callback) {
        MediaType MEDIA_TYPE = MediaType.parse("application/json");
        String url = "https://inclass05.herokuapp.com/getItems";
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

        Call call = client.newCall(request);
        call.enqueue(callback);
    }
}
