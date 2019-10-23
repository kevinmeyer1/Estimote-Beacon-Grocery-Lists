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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
                    boolean foundUseableBeacon = false;

                    //Get the first beacon in the beacons list that appears in our whitelist (a beacon that we want to read from)
                    for (Beacon b : beacons) {
                        for (int x : whitelistMajor) {
                            if (x == b.getMajor()) {
                                firstBeacon = b;
                                foundUseableBeacon = true;
                                break;
                            }
                        }

                        if (foundUseableBeacon) {
                            break;
                        }
                    }

                    if (!foundUseableBeacon) {
                        //All beacons in the list were checked and none of the majors matched the whitelist
                        //Get all items because useable beacons are out of range
                        //getItems(-1, -1);
                        return;
                    }

                    if (beaconArrFull) {
                        //the beacon array is full, so we remove the oldest value, shift everything down one, and then add the newest value
                        Beacon[] newArr = new Beacon[5];

                        for (int i = 0; i < 5; i++) {
                            if (i != 4) {
                                newArr[i] = beaconArr[i+1];
                            } else {
                                newArr[i] = firstBeacon;
                            }
                        }

                        beaconArr = newArr;

                        //Use the new array to determine the list that we will display
                        determineList();
                    } else {
                        if (beaconArrIndex < 4) {
                            //beacon arr is not full and we are filling up spots 0, 1, 2, 3
                            beaconArr[beaconArrIndex] = firstBeacon;
                            beaconArrIndex++;
                        } else if (beaconArrIndex == 4) {
                            //The beacon array is now full so we can set the boolean to true so it starts calling determineList
                            beaconArr[beaconArrIndex] = firstBeacon;
                            beaconArrFull = true;
                        }
                    }
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

                //populte the list on the app
                runOnUiThread(new Runnable() {

                    @Override
                    public void run() {

                        mAdapter.notifyDataSetChanged();

                    }
                });
            }
        });
    }

    //determines the list to show based on beaconArr
    public void determineList() {
        Map<String, Integer> counterMap = new HashMap<>();
        int highestCount = -1;
        String highestKey = "";

        //put each beacon into the hashmap (by unique mac address) and then count the amount of times that beacon is found in the list
        for (Beacon b: beaconArr) {
            System.out.println(b.toString());
            String mac = b.getMacAddress().toString();

            if (counterMap.containsKey(mac)) {
                int currentCount = counterMap.get(mac);
                counterMap.replace(mac, currentCount + 1);
            } else {
                counterMap.put(mac, 1);
            }
        }

        //check which beacon appears the most in the list
        //this does not account for ties and will take which ever one comes first as the highest
        //With 2 beacons in range, this is not an issue because of the uneven number of the array (5), but with 3 or more beacons this becomes an issue
        for (String key : counterMap.keySet()) {
            System.out.println("Mac: " + key + ", count: " + counterMap.get(key));
            if (counterMap.get(key) > highestCount) {
                highestCount = counterMap.get(key);
                highestKey = key;
            }
        }

        //Figure out which beacon the mac address belongs to and use their major/minor to get items from the server
        for (Beacon b: beaconArr) {
            if (b.getMacAddress().toString().equals(highestKey)) {
                getItems(b.getMajor(), b.getMinor());
                break;
            }
        }
    }
}
