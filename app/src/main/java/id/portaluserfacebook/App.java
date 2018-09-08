package id.portaluserfacebook;

import android.app.Application;

import com.facebook.CallbackManager;

public class App extends Application {
    public static CallbackManager callbackManager = CallbackManager.Factory.create();

    @Override
    public void onCreate() {
        super.onCreate();
    }
}
