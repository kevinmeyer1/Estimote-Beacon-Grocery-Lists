package com.example.estimoteclassassignment;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import com.estimote.coresdk.common.requirements.SystemRequirementsChecker;
import com.estimote.coresdk.observation.region.beacon.BeaconRegion;
import com.estimote.coresdk.service.BeaconManager;
import com.example.estimoteclassassignment.Model.Item;
import com.example.estimoteclassassignment.Model.MyAdapter;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
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

        //recycler view stuff
        recyclerView = findViewById(R.id.recyclerView);
        layoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(layoutManager);
        mAdapter = new MyAdapter(itemList);
        recyclerView.setAdapter(mAdapter);

        //These are hardcoded but should be pulled from the beacons
        String major = "30462";
        String minor = "43265";

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

    //sends request to heroku for the list of items per major and minor of beacon
    public void getItems(String major, String minor, final Callback callback) {
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
