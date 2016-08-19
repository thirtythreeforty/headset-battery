package me.thirtythreeforty.headsetbattery;

import android.app.Notification;
import android.app.NotificationManager;
import android.bluetooth.BluetoothHeadset;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import java.util.Locale;

public final class HeadsetReceiver extends BroadcastReceiver {
    static final int HEADSET_NOTIFICATION_ID = 1;
    static final String TAG = HeadsetReceiver.class.getName();

    public HeadsetReceiver() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        final String action = intent.getAction();
        final String VENDOR_EVENT = context.getString(R.string.vendor_event_intent);
        final String CONNECTION_CHANGED = context.getString(R.string.connection_changed_intent);

        Log.d(TAG, "Got an intent");

        if (VENDOR_EVENT.equals(action)) {
            onVendorSpecificHeadsetEvent(context, intent);
        }
        else if (CONNECTION_CHANGED.equals(action)) {
            onHeadsetConnectionStateChange(context, intent);
        }
    }

    private static void onHeadsetConnectionStateChange(Context context, Intent intent) {
        int state = intent.getIntExtra(BluetoothHeadset.EXTRA_STATE, BluetoothHeadset.STATE_DISCONNECTED);

        if (state == BluetoothHeadset.STATE_DISCONNECTED) {
            getNotificationManager(context).cancel(HEADSET_NOTIFICATION_ID);
        }
    }

    private static void onVendorSpecificHeadsetEvent(Context context, Intent intent) {
        // Taken from http://review.cyanogenmod.org/#/c/153034/
        if (intent.hasExtra(BluetoothHeadset.EXTRA_VENDOR_SPECIFIC_HEADSET_EVENT_CMD)) {
            String command = intent.getStringExtra(BluetoothHeadset.EXTRA_VENDOR_SPECIFIC_HEADSET_EVENT_CMD);
            if ("+IPHONEACCEV".equals(command)) {
                Object[] args = (Object[]) intent.getSerializableExtra(BluetoothHeadset.EXTRA_VENDOR_SPECIFIC_HEADSET_EVENT_ARGS);
                if (args.length >= 3 && args[0] instanceof Integer && ((Integer)args[0])*2+1<=args.length) {
                    for (int i=0;i<((Integer)args[0]);i++) {
                        if (!(args[i*2+1] instanceof Integer) || !(args[i*2+2] instanceof Integer)) {
                            continue;
                        }
                        if (args[i*2+1].equals(1)) {
                            final float batteryLevel = (((Integer)args[i*2+2])+1)/10.0f;

                            if(context.getSharedPreferences("prefs", Context.MODE_PRIVATE).getBoolean("enabled", true)) {
                                notifyBatteryPercent(context, batteryLevel);
                            }
                            break;
                        }
                    }
                }
            }
        }
    }

    private static void notifyBatteryPercent(Context context, float batteryLevel) {
        final int icon;
        if(batteryLevel > 0.9) {
            icon = R.drawable.stat_sys_data_bluetooth_connected_battery_5;
        } else if(batteryLevel > 0.7) {
            icon = R.drawable.stat_sys_data_bluetooth_connected_battery_4;
        } else if(batteryLevel > 0.3) {
            icon = R.drawable.stat_sys_data_bluetooth_connected_battery_3;
        } else if(batteryLevel > 0.1) {
            icon = R.drawable.stat_sys_data_bluetooth_connected_battery_2;
        } else {
            icon = R.drawable.stat_sys_data_bluetooth_connected_battery_1;
        }

        final float percentage = batteryLevel * 100;

        final Notification.Builder builder = new Notification.Builder(context)
                .setContentTitle("Headset Battery")
                .setSmallIcon(icon)
                .setContentText(String.format(Locale.ENGLISH, "%.0f%%", percentage));

        final Notification notif;
        if(Build.VERSION.SDK_INT >= 16) {
            notif = builder.build();
        }
        else {
            notif = builder.getNotification();
        }

        getNotificationManager(context).notify(HEADSET_NOTIFICATION_ID, notif);
    }

    private static NotificationManager getNotificationManager(Context context) {
        return (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
    }
}
