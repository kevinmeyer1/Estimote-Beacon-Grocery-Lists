package com.example.estimoteclassassignment;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import com.estimote.coresdk.common.requirements.SystemRequirementsChecker;
import com.estimote.coresdk.observation.region.beacon.BeaconRegion;
import com.estimote.coresdk.recognition.packets.Beacon;
import com.estimote.coresdk.service.BeaconManager;
import com.example.estimoteclassassignment.Model.Item;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

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
    private BeaconManager beaconManager;
    private BeaconRegion region;

    private RecyclerView recyclerView;
    private RecyclerView.Adapter mAdapter;
    private RecyclerView.LayoutManager layoutManager;

    final ArrayList<Item> itemList = new ArrayList<>();
    public Beacon[] beaconArr = new Beacon[5];

    public int currentMajor;
    public int currentMinor;

    public int beaconArrIndex;
    public boolean beaconArrFull = false;


    //The only major and minor values that I want to find from beacons - ignore all others
    final int[] whitelistMajor = {47152, 15326, 41072, 7518, 30462};
    final int[] whitelistMinor = {61548, 56751, 44931, 47661, 43265};

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

        //initialize current major/minor to 0 - not a possible value
        currentMajor = 0;
        currentMinor = 0;

        beaconArrIndex = 0;

        beaconManager = new BeaconManager(this);

        region = new BeaconRegion("ranged region",
                UUID.fromString("B9407F30-F5F8-466E-AFF9-25556B57FE6D"), null, null);

        //Get all items first to populate the list
        getItems(-1, -1);

        beaconManager.connect(new BeaconManager.ServiceReadyCallback() {
            @Override
            public void onServiceReady() {
                beaconManager.startRanging(region);
                beaconManager.startMonitoring(region);
            }
        });

        beaconManager.setRangingListener(new BeaconManager.BeaconRangingListener() {
            @Override
            public void onBeaconsDiscovered(BeaconRegion beaconRegion, List<Beacon> beacons) {
                System.out.println("Beacons: " + beacons);

                Beacon firstBeacon = null;

                if (!beacons.isEmpty()) {
                    int major = -1;
                    int minor = -1;

                    //The returned closest beacon is not in the whitelist. check to see if any of the items are in the whitelist
                    boolean foundUseableBeacon = false;

                    for (Beacon b : beacons) {
                        for (int x : whitelistMajor) {
                            if (x == b.getMajor()) {
                                major = b.getMajor();
                                minor = b.getMinor();
                                firstBeacon = b;
                                foundUseableBeacon = true;
                                continue;
                            }
                        }
                    }

                    if (!foundUseableBeacon) {
                        //All beacons in the list were checked and none of the majors matched the whitelist
                        //Get all items because useable beacons are out of range
                        getItems(-1, -1);
                        return;
                    }

                    if (beaconArrFull) {
                        Beacon[] newArr = new Beacon[5];

                        for (int i = 0; i < 5; i++) {
                            if (i != 4) {
                                newArr[i] = beaconArr[i+1];
                            } else {
                                newArr[i] = firstBeacon;
                            }
                        }

                        beaconArr = newArr;
                        determineBeacon();
                        System.out.println("full: " + beaconArr);
                    } else {
                        if (beaconArrIndex < 4) {
                            //beacon arr is not full and we are filling up spots 0, 1, 2, 3
                            beaconArr[beaconArrIndex] = firstBeacon;
                            beaconArrIndex++;
                        } else if (beaconArrIndex == 4) {
                            beaconArr[beaconArrIndex] = firstBeacon;
                            beaconArrFull = true;
                        }
                    }

                    //reset currentMajor/Minor values
                    if (currentMajor == 0 || currentMinor == 0) {
                        //there is not a beacon saved currently on the app (major minor)
                        currentMajor = major;
                        currentMinor = minor;
                    } else if (currentMajor != major || currentMinor != minor){
                        //there is a beacon saved currently on the app but the closest beacon is a new one - new request
                        currentMajor = major;
                        currentMinor = minor;
                    } else {
                        //there is a beacon saved currently on the app but the closest beacon is the same - no request, same data
                        return;
                    }

                    //Sends request, calls function at bottom
                    getItems(major, minor);
                } else {
                    //this doesn't work at all either
                    getItems(-1, -1);
                }
            }
        });
    }

    //sends request to heroku for the list of items per major and minor of beacon
    public void getItems(int major, int minor) {
        MediaType MEDIA_TYPE = MediaType.parse("application/json");
        String url = "https://inclass05.herokuapp.com/getItems";
        JSONObject json = new JSONObject();

        try {
            if (major == -1 || minor == -1) {
                json.put("major", "");
                json.put("minor", "");
            } else {
                json.put("major", major);
                json.put("minor", minor);
            }
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
                        Item currentItem = new Item(
                                obj.getInt("discount"),
                                obj.getString("name"),
                                obj.getString("photo"),
                                obj.getDouble("price"),
                                obj.getString("region")
                        );
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

    public void determineBeacon() {

    }
}
