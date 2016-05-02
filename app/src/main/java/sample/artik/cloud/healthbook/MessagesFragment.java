package sample.artik.cloud.healthbook;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import cloud.artik.api.MessagesApi;
import cloud.artik.client.ApiCallback;
import cloud.artik.client.ApiClient;
import cloud.artik.client.ApiException;
import cloud.artik.model.NormalizedMessage;
import cloud.artik.model.NormalizedMessagesEnvelope;

public class MessagesFragment extends Fragment {
    MessagesAdapter messagesAdapter;
    public final static String TAG = MessagesFragment.class.getSimpleName();

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Add this line in order for this fragment to handle menu events.
        setHasOptionsMenu(true);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
        inflater.inflate(R.menu.menu_messages_fragment, menu);
    }

    private void updateWeather() {
        try {
            ApiClient client = new ApiClient();
            client.setAccessToken(Utility.getAccessToken(getContext()));
            client.setDebugging(true);
            MessagesApi api = new MessagesApi(client);

            StringBuffer sdids = null;
            for (String dtId: Constants.DEVICE_TYPES) {
                String deviceId = Utility.getKeyValue(dtId, getContext());
                if (deviceId != null) {
                    if (sdids == null) {
                        sdids = new StringBuffer();
                    } else {
                        sdids.append(',');
                    }
                    sdids.append(deviceId);
                }
            }

            api.getLastNormalizedMessagesAsync(5, sdids.toString(), null, new ApiCallback<NormalizedMessagesEnvelope>() {
                @Override
                public void onFailure(ApiException exc, int i, Map<String, List<String>> map) {
                    Log.e(TAG, "Error Getting Normalized Messages: " + exc.getMessage());
                }

                @Override
                public void onSuccess(final NormalizedMessagesEnvelope normalizedMessagesEnvelope, int i, Map<String, List<String>> map) {
                    Log.v(TAG, "Got Normalized Messages: " + normalizedMessagesEnvelope);

                    // Update the UI in the UI Thread
                    MessagesFragment.this.getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            messagesAdapter.clear();
                            messagesAdapter.addAll(normalizedMessagesEnvelope.getData().toArray(new NormalizedMessage[normalizedMessagesEnvelope.getData().size()]));
                        }
                    });
                }

                @Override
                public void onUploadProgress(long l, long l1, boolean b) {

                }

                @Override
                public void onDownloadProgress(long l, long l1, boolean b) {

                }
            });

        } catch (ApiException exc) {
            Log.e(TAG, exc.getMessage(), exc);
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.action_refresh) {
            updateWeather();
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onStart() {
        super.onStart();
        updateWeather();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        ArrayList<NormalizedMessage> normalizedMessages = new ArrayList<NormalizedMessage>();

        messagesAdapter = new MessagesAdapter(
                getActivity(),
                normalizedMessages
        );

        View rootView = inflater.inflate(R.layout.fragment_messages, container, false);

        ListView listView = (ListView) rootView.findViewById(
                R.id.listview_messages
        );
        listView.setAdapter(messagesAdapter);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                NormalizedMessage message = messagesAdapter.getItem(position);
                Log.v(TAG, message.toString());

                Intent intent = new Intent(getActivity(), DetailActivity.class)
                        .putExtra(Intent.EXTRA_TEXT, message.toString());
                startActivity(intent);
            }
        });

        return rootView;
    }
}