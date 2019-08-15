package zhang.xiulu.com.annotationusage;

import android.content.Context;
import android.util.Log;
import android.widget.Toast;

public class UseCase {
    private static final String TAG = "UseCase";

    @LogRecord(tag = "MainActivity", msg = "testRecord method invoke")
    public void testRecord(Context context) {
        Toast.makeText(context, "show Toast", Toast.LENGTH_LONG).show();
        testLog();
    }


    public void testLog() {
        Log.d(TAG, "testLog");
    }
}
