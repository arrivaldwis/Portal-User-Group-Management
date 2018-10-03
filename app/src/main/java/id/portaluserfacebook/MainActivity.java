package id.portaluserfacebook;

import android.content.Intent;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputEditText;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.facebook.AccessToken;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.HttpMethod;
import com.facebook.Profile;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.squareup.picasso.Picasso;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Arrays;

import id.portaluserfacebook.news.activity.ListNewsActivity;

import static id.portaluserfacebook.App.callbackManager;

public class MainActivity extends AppCompatActivity {
    private static final int RC_SIGN_IN = 2 ;
    AccessToken accessToken = AccessToken.getCurrentAccessToken();
    boolean isLoggedIn = accessToken != null && !accessToken.isExpired();
    ImageView imgUser;
    LinearLayout llProfile;
    TextInputEditText tvName;
    TextInputEditText tvGender;
    TextInputEditText tvBirthday;
    SignInButton signInButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        String[] perms = {
                "android.permission.CAMERA",
                "android.permission.WRITE_EXTERNAL_STORAGE",
                "android.permission.RECORD_AUDIO",
                "android.permission.READ_EXTERNAL_STORAGE",
                "android.permission.ACCESS_NETWORK_STATE",
                "android.permission.ACCESS_COARSE_LOCATION",
                "android.permission.ACCESS_FINE_LOCATION"
        };
        int permsRequestCode = 200;

        ActivityCompat.requestPermissions(this, perms, permsRequestCode);
        loginGoogle();

        if (isLoggedIn) {
            //goto main act
            isLogin();
        } else {
            setUI();
        }
    }

    GoogleSignInClient mGoogleSignInClient;

    private void loginGoogle() {
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestEmail()
                .build();

        mGoogleSignInClient = GoogleSignIn.getClient(this, gso);
    }

    private void signIn() {
        Intent signInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(signInIntent, RC_SIGN_IN);
    }

    private void setUI() {
        llProfile = findViewById(R.id.llProfile);
        tvName = findViewById(R.id.tvName);
        tvGender = findViewById(R.id.tvGender);
        tvBirthday = findViewById(R.id.tvBirthday);
        imgUser = findViewById(R.id.img_user);
        signInButton = findViewById(R.id.sign_in_button);

        signInButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                signIn();
            }
        });
        LoginButton loginButton = (LoginButton) findViewById(R.id.login_button);
        loginButton.setReadPermissions(Arrays.asList("public_profile", "email"));

        // Callback registration
        loginButton.registerCallback(callbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                // App code
                AccessToken accessToken = loginResult.getAccessToken();
                Profile profile = Profile.getCurrentProfile();

                // Facebook Email address
                GraphRequest request = GraphRequest.newMeRequest(
                        loginResult.getAccessToken(),
                        new GraphRequest.GraphJSONObjectCallback() {
                            @Override
                            public void onCompleted(
                                    JSONObject object,
                                    GraphResponse response) {
                                Log.v("LoginActivity Response ", response.toString());

                                try {
                                    String id = object.getString("id");
                                    String Name = object.getString("name");
                                    String FEmail = object.getString("email");
                                    String image_url = "http://graph.facebook.com/" + id + "/picture?type=large";
                                    Log.v("Emailss = ", " " + imgUser);
                                    Picasso.get().load(image_url).into(imgUser);
                                    tvName.setText(Name);
                                    tvGender.setText(FEmail);
                                    llProfile.setVisibility(View.VISIBLE);

                                    Intent intent = new Intent(MainActivity.this, NavMenuActivity.class);
                                    startActivity(intent);
                                    Toast.makeText(getApplicationContext(), "Welcome!", Toast.LENGTH_LONG).show();
                                } catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                        });

                new GraphRequest(
                        AccessToken.getCurrentAccessToken(),
                        "/me/feed",
                        null,
                        HttpMethod.GET,
                        new GraphRequest.Callback() {
                            public void onCompleted(GraphResponse response) {
                                /* handle the result */
                                Log.v("Feed Response ", response.toString());
                            }
                        }
                ).executeAsync();

                Bundle parameters = new Bundle();
                parameters.putString("fields", "id,name,email,gender,birthday");
                request.setParameters(parameters);
                request.executeAsync();
            }

            @Override
            public void onCancel() {
                // App code
            }

            @Override
            public void onError(FacebookException exception) {
                // App code
            }
        });
    }

    private void isLogin() {
        Intent intent = new Intent(MainActivity.this, NavMenuActivity.class);
        startActivity(intent);
        finish();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        callbackManager.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RC_SIGN_IN) {
            // The Task returned from this call is always completed, no need to attach
            // a listener.
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            handleSignInResult(task);
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void handleSignInResult(Task<GoogleSignInAccount> completedTask) {
        try {
            GoogleSignInAccount account = completedTask.getResult(ApiException.class);

            Picasso.get().load(account.getPhotoUrl()).into(imgUser);
            tvName.setText(account.getDisplayName());
            tvGender.setText(account.getEmail());
            llProfile.setVisibility(View.VISIBLE);

            Intent intent = new Intent(MainActivity.this, NavMenuActivity.class);
            startActivity(intent);
            Toast.makeText(getApplicationContext(), "Welcome!", Toast.LENGTH_LONG).show();
            // Signed in successfully, show authenticated UI.
            //updateUI(account);
        } catch (ApiException e) {
            // The ApiException status code indicates the detailed failure reason.
            // Please refer to the GoogleSignInStatusCodes class reference for more information.
            Log.w("", "signInResult:failed code=" + e.getStatusCode());
            //updateUI(null);
        }
    }
}
