package sample.artik.cloud.healthbook;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Button;

/**
 * A login screen that offers OAuth2 Implicit Flow
 */
public class LoginActivity extends AppCompatActivity {
    private static final String TAG = MessagesActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        Intent intent = getIntent();
        if (intent.getData() != null && intent.getData().toString().startsWith("artikcloud://localhost")) {
            handleArtikCloudOauthCallback();
        }

        if (Utility.getAccessToken(getApplicationContext()) != null) {
            startActivity(new Intent(LoginActivity.this, ProfileActivity.class));
        } else {
            Button signInButton = (Button) findViewById(R.id.sign_in_button);
            signInButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    startOauth2ImplicitFlow();
                }
            });
        }
    }

    /**
     * Start the OAuth 2.0 Implicit Flow with the Artik Cloud Authorization Server
     * Redirect URL is artikcloud://localhost
     */
    private void startOauth2ImplicitFlow() {
        Uri uri = Uri.parse(Constants.AUTHORIZATION_IMPLICIT_SERVER_URL+"?client_id="+ Constants.CLIENT_ID+"&response_type=token&redirect_uri="+ Constants.REDIRECT_URL);
        Log.v(TAG, "OAuth2 " + uri);
        Intent browserIntent = new Intent(
                Intent.ACTION_VIEW,
                uri);
        startActivity(browserIntent);
    }

    /**
     * Handle the OAuth 2.0 token callback
     */
    private void handleArtikCloudOauthCallback() {
        final Uri data = this.getIntent().getData();
        if(data != null) {
            // Extract access_token from artikcloud://localhost#access_token=28cc908287de47f4a7077f6633d7fafa&refresh_token=4cd6b7c290344747aa7faca63368e5f8&token_type=bearer&expires_in=1209600
            String fragment = data.getFragment().replaceFirst("access_token=", "");
            final String accessToken = fragment.substring(0, fragment.indexOf("&refresh_token"));
            Log.i(TAG, "Got Access Token: " + accessToken);
            Utility.setAccessToken(accessToken, getApplicationContext());

        }
    }
}

