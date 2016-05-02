package sample.artik.cloud.healthbook;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.HashSet;
import java.util.List;
import java.util.Map;

import cloud.artik.api.DevicesApi;
import cloud.artik.api.UsersApi;
import cloud.artik.client.ApiCallback;
import cloud.artik.client.ApiClient;
import cloud.artik.client.ApiException;
import cloud.artik.model.Device;
import cloud.artik.model.DeviceEnvelope;
import cloud.artik.model.DevicesEnvelope;
import cloud.artik.model.UserEnvelope;

public class ProfileActivity extends AppCompatActivity {
    public final static String TAG = ProfileActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        updateProfile();

        Button messagesButton = (Button) findViewById(R.id.messages_button);
        messagesButton.setEnabled(false);
        messagesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                // Forward to MessagesActivity
                startActivity(new Intent(ProfileActivity.this, MessagesActivity.class));
            }
        });
    }

    private void updateProfile() {
        try {
            ApiClient client = new ApiClient();
            client.setAccessToken(Utility.getAccessToken(getApplicationContext()));
            client.setDebugging(true);
            final UsersApi api = new UsersApi(client);
            final DevicesApi devicesApi = new DevicesApi(client);

            final ProgressBar progressBar = (ProgressBar) findViewById(R.id.devices_progressbar);
            progressBar.setProgress(0);

            final Button messagesButton = (Button) findViewById(R.id.messages_button);

            api.getSelfAsync(new ApiCallback<UserEnvelope>() {
                @Override
                public void onFailure(ApiException e, int i, Map<String, List<String>> map) {
                    Utility.showError(e, ProfileActivity.this);
                }

                @Override
                public void onSuccess(UserEnvelope userEnvelope, int i, Map<String, List<String>> map) {
                    final String userId = userEnvelope.getData().getId();
                    final String fullName = userEnvelope.getData().getFullName();
                    ProfileActivity.this.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            TextView nameView = (TextView) findViewById(R.id.name_textview);
                            nameView.setText(fullName);
                        }
                    });

                    try {

                        // Create devices if they aren't already
                        api.getUserDevicesAsync(userId, null, null, false, new ApiCallback<DevicesEnvelope>() {
                            @Override
                            public void onFailure(ApiException e, int i, Map<String, List<String>> map) {
                                Utility.showError(e, ProfileActivity.this);
                            }

                            @Override
                            public void onSuccess(DevicesEnvelope devicesEnvelope, int i, Map<String, List<String>> map) {
                                try {

                                    final HashSet<String> missingDeviceTypes = new HashSet<String>();
                                    for (String deviceTypeId : Constants.DEVICE_TYPES) {
                                        missingDeviceTypes.add(deviceTypeId);
                                    }
                                    for (Device device : devicesEnvelope.getData().getDevices()) {
                                        if (missingDeviceTypes.contains(device.getDtid())) {
                                            missingDeviceTypes.remove(device.getDtid());
                                        }
                                    }
                                    ProfileActivity.this.runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            progressBar.setProgress(100 - (50 * missingDeviceTypes.size()));
                                            if (progressBar.getProgress() == 100) {
                                                messagesButton.setEnabled(true);
                                            }
                                        }
                                    });
                                    for (String dtId : missingDeviceTypes) {
                                        // Create a new device
                                        Device newDevice = new Device();
                                        newDevice.setDtid(dtId);
                                        newDevice.setName(dtId);
                                        newDevice.setUid(userId);
                                        devicesApi.addDeviceAsync(newDevice, new ApiCallback<DeviceEnvelope>() {
                                            @Override
                                            public void onFailure(ApiException e, int i, Map<String, List<String>> map) {

                                            }

                                            @Override
                                            public void onSuccess(DeviceEnvelope deviceEnvelope, int i, Map<String, List<String>> map) {
                                                Utility.setKeyValue(deviceEnvelope.getData().getDtid(), deviceEnvelope.getData().getId(), getApplicationContext());
                                                ProfileActivity.this.runOnUiThread(new Runnable() {
                                                    @Override
                                                    public void run() {
                                                        progressBar.setProgress(progressBar.getProgress() + 50);
                                                        if (progressBar.getProgress() == 100) {
                                                            messagesButton.setEnabled(true);
                                                        }
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
                                    }

                                } catch (ApiException aexc) {
                                    Utility.showError(aexc, ProfileActivity.this);
                                }

                            }

                            @Override
                            public void onUploadProgress(long l, long l1, boolean b) {

                            }

                            @Override
                            public void onDownloadProgress(long l, long l1, boolean b) {

                            }
                        });

                    } catch (ApiException aexc2) {
                        Utility.showError(aexc2, ProfileActivity.this);
                    }
                }

                @Override
                public void onUploadProgress(long l, long l1, boolean b) {

                }

                @Override
                public void onDownloadProgress(long l, long l1, boolean b) {

                }
            });


        } catch (ApiException exc) {
            Utility.showError(exc, ProfileActivity.this);
        }
    }


}
