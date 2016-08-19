package me.thirtythreeforty.headsetbattery;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.CompoundButton;

public class MainActivity extends Activity {
    CompoundButton enableSwitch;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.main_activity_layout);
        enableSwitch = (CompoundButton) findViewById(R.id.enableSwitch);
        enableSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                getSharedPreferences("prefs", Context.MODE_PRIVATE).edit().putBoolean("enabled", isChecked).apply();
            }
        });
    }
}
