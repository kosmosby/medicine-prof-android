package org.jitsi.android.gui.account;

import android.app.Activity;
import android.os.Bundle;
import com.medicineprof.R;

/**
 * Created by neurons on 8/23/15.
 */
public class WaitActivity extends Activity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.wait);
    }
}