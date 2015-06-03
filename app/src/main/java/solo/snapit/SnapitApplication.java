package solo.snapit;

import android.app.Application;

import com.baidu.mapapi.SDKInitializer;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;

/**
 * Created by solo on 15/2/18.
 */
public class SnapitApplication extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        final ImageLoaderConfiguration config = new ImageLoaderConfiguration
                .Builder(getApplicationContext())
                .defaultDisplayImageOptions(new DisplayImageOptions.Builder()
                        .build())
                .build();
        ImageLoader.getInstance().init(config);

        SDKInitializer.initialize(getApplicationContext());
    }
}
