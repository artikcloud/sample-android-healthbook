package sample.artik.cloud.healthbook;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import java.util.ArrayList;
import java.util.Map;

import cloud.artik.model.NormalizedMessage;

public class MessagesAdapter extends ArrayAdapter<NormalizedMessage> {
    public final static String TAG = MessagesAdapter.class.getSimpleName();
    public MessagesAdapter(Context context, ArrayList<NormalizedMessage> messages) {
        super(context, 0, messages);
    }

    @Override
    public int getViewTypeCount() {
        return 2;
    }

    @Override
    public int getItemViewType(int position) {
        NormalizedMessage item = getItem(position);
        switch (item.getSdtid()) {
            case Constants.DT_OPEN_WEATHER_MAP:
                return 0;
            case Constants.DT_PEDOMETER:
                return 1;
            default:
                return 2;
        }
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        // Get the data item for this position
        NormalizedMessage message = getItem(position);
        Map<String, Object> data = message.getData();
        // Check if an existing view is being reused, otherwise inflate the view
        if (convertView == null) {
            Log.v(TAG, "Creating view for: " + message.getSdtid());
            Log.v(TAG, data.toString());
            switch (getItemViewType(position)) {
                case 0:
                    // OpenWeatherMap
                    Log.i(TAG, "OpenWeatherMap");
                    convertView = LayoutInflater.from(getContext()).inflate(R.layout.list_item_openweathermap, parent, false);
                    // Populate the data into the template view using the data object
                    Utility.setText(convertView, R.id.list_item_date_textview, Utility.formatDate(message.getTs()));
                    Utility.setText(convertView, R.id.list_item_forecast_textview, Utility.getMapField(data, "name") + ": " + Utility.getMapSubField(data, "weather", "text"));
                    Utility.setText(convertView, R.id.list_item_high_textview, Utility.getMapSubField(data, "main", "temp_max"));
                    Utility.setText(convertView, R.id.list_item_low_textview, Utility.getMapSubField(data, "main", "temp_min"));
                    break;
                case 1:
                    Log.i(TAG, "MyPedoMeter");
                    convertView = LayoutInflater.from(getContext()).inflate(R.layout.list_item_pedometer, parent, false);
                    // Lookup view for data population

                    Utility.setText(convertView, R.id.list_item_steps_textview, Utility.getMapField(data, "steps"));
                    Utility.setText(convertView, R.id.list_item_distance_textview, Utility.getMapField(data, "distance"));
                    break;
                default:
                    Log.i(TAG, "Uh-oh");
            }
        }

        // Return the completed view to render on screen
        return convertView;
    }
}
