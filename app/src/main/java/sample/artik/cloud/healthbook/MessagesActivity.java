package sample.artik.cloud.healthbook;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import cloud.artik.api.MessagesApi;
import cloud.artik.client.ApiCallback;
import cloud.artik.client.ApiClient;
import cloud.artik.client.ApiException;
import cloud.artik.model.MessageAction;
import cloud.artik.model.MessageIDEnvelope;

public class MessagesActivity extends AppCompatActivity {
    private static final String TAG = MessagesActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_messages);
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, new MessagesFragment())
                    .commit();
        }

        Intent intent = getIntent();
        if (intent != null && intent.getExtras() != null) {
            Bundle bundle = intent.getExtras();
            String stepsStr = bundle.get("steps").toString();
            String distanceStr = bundle.get("distance").toString();

            Log.v(TAG, "Steps " + stepsStr);
            Log.v(TAG, "Distance " + distanceStr);
            int steps = Integer.parseInt(stepsStr);
            float distance = Float.parseFloat(distanceStr);

            sendPedometerMessage(steps, distance);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_messages, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            startActivity(new Intent(this, SettingsActivity.class));
            return true;
        }

        if (id == R.id.action_location) {
            sendOpenWeatherMapAction();
            return true;
        }

        if (id == R.id.action_pedometer) {
            startActivity(new Intent(this, PedometerActivity.class));
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void sendOpenWeatherMapAction() {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
        String location =  prefs.getString(getApplicationContext().getString(R.string.pref_location_key),
                getApplicationContext().getString(R.string.pref_location_default));
        Log.v(TAG, "Location: " + location);

        String deviceId = Utility.getKeyValue(Constants.DT_OPEN_WEATHER_MAP, getApplicationContext());
        Log.v(TAG, "Device ID: " + deviceId);

        if (deviceId != null) {
            MessageAction action = new MessageAction();
            action.setDdid(deviceId);
            action.setType("action");

            Type type = new TypeToken<Map<String, Object>>() {
            }.getType();
            GsonBuilder gsonBuilder = new GsonBuilder();
            Gson gson = gsonBuilder.create();
            Map<String, Object> actionData = gson.fromJson(
                    "{ 'actions': [ {" +
                            "'name':'getCurrentWeatherByCity'," +
                            "'parameters':{" +
                            "'city': '" + location + "'," +
                            "'countryCode': 'US'" +
                            "}" +
                            "} ]}",
                    type);
            action.setData(actionData);

            Log.v(TAG, "Sending Action: " + action);
            sendMessageAction(action);
        } else {
            Utility.showMessage("Open Weather Device doesnt exist", MessagesActivity.this);
        }
    }

    private void sendPedometerMessage(int steps, float distance) {
        Log.v(TAG, "Send Pedometer Message: " + steps + ", " + distance);

        String deviceId = Utility.getKeyValue(Constants.DT_PEDOMETER, getApplicationContext());
        Log.v(TAG, "Device ID: " + deviceId);

        if (deviceId != null) {
            MessageAction message = new MessageAction();
            message.setSdid(deviceId);
            message.setType("message");

            Map<String, Object> messageData = new HashMap<String, Object>();
            messageData.put("steps", steps);
            messageData.put("distance", distance);

            message.setData(messageData);

            sendMessageAction(message);
        } else {
            Utility.showMessage("Open Pedometer Device doesnt exist", MessagesActivity.this);
        }
    }

    private void sendMessageAction(final MessageAction messageAction) {
        try {
            ApiClient client = new ApiClient();
            client.setAccessToken(Utility.getAccessToken(getApplicationContext()));
            client.setDebugging(true);
            MessagesApi api = new MessagesApi(client);

            api.sendMessageActionAsync(messageAction, new ApiCallback<MessageIDEnvelope>() {
                @Override
                public void onFailure(ApiException exc, int i, Map<String, List<String>> map) {
                    Utility.showError(exc, MessagesActivity.this);
                }

                @Override
                public void onSuccess(MessageIDEnvelope messageIDEnvelope, int i, Map<String, List<String>> map) {
                    String mid = messageIDEnvelope.getData().getMid();
                    Log.v(TAG, " MID: " + mid);
                    Utility.showMessage("Sent " + messageAction.getType() + " to Artik Cloud: " + mid, MessagesActivity.this);
                }

                @Override
                public void onUploadProgress(long l, long l1, boolean b) {

                }

                @Override
                public void onDownloadProgress(long l, long l1, boolean b) {

                }
            });

        } catch (ApiException exc) {
            Utility.showError(exc, MessagesActivity.this);
        }
    }

}
