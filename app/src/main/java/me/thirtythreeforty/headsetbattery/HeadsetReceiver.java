package me.thirtythreeforty.headsetbattery;

import android.app.Notification;
import android.app.NotificationManager;
import android.bluetooth.BluetoothHeadset;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.widget.Toast;

import java.util.Locale;

public final class HeadsetReceiver extends BroadcastReceiver {
    static final int HEADSET_NOTIFICATION_ID = 1;

    public HeadsetReceiver() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        final String action = intent.getAction();
        final String VENDOR_EVENT = context.getString(R.string.vendor_event_intent);
        final String CONNECTION_CHANGED = context.getString(R.string.connection_changed_intent);

        Toast.makeText(context, String.format("Got an intent! %s", action), Toast.LENGTH_LONG).show();

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

                            notifyBatteryPercent(context, batteryLevel);
                            break;
                        }
                    }
                }
            }
        }
    }

    private static void notifyBatteryPercent(Context context, float batteryLevel) {
        final Notification.Builder builder = new Notification.Builder(context)
                .setContentTitle("Headset Battery")
                .setSmallIcon(R.mipmap.ic_launcher)
                .setContentText(String.format(Locale.ENGLISH, "Headset battery %.2f", batteryLevel));

        final Notification notif;
        if(Build.VERSION.SDK_INT >= 16) {
            // builder.setPriority(Notification.PRIORITY_LOW);
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
