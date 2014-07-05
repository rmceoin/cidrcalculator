/*
 *
 * Copyright (C) 2014 Randy McEoin
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package us.lindanrandy.cidrcalculator;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.app.NotificationCompat.WearableExtender;

/**
 * Helper class for handling notifications.
 */
public class NotifySubnet {

    /**
     *
     * Place a notification in the Notification area.  This is targeted for Android Wearables.
     */
    public static void SendNotify(Context context, String address, int bits, String subnetmask,
                                  String addressRange, String maximumAddresses, String wildcard) {
        int notificationId = 001;

        // Build intent for notification content
        Intent viewIntent = new Intent(context, CIDRCalculator.class);
//        viewIntent.putExtra(EXTRA_EVENT_ID, eventId);
        PendingIntent viewPendingIntent =
                PendingIntent.getActivity(context, 0, viewIntent, 0);

        String title = address + "/" + bits;

        String bigText = title + "\n" + subnetmask + "\n" + addressRange + "\n" +
                maximumAddresses + "\n" + wildcard;
        NotificationCompat.BigTextStyle bigStyle = new NotificationCompat.BigTextStyle();
        bigStyle.bigText(bigText);

        Bitmap bm = BitmapFactory.decodeResource(context.getResources(), R.drawable.wearable_background);

        NotificationCompat.WearableExtender wearableExtender =
                new NotificationCompat.WearableExtender()
                        .setBackground(bm);

        NotificationCompat.Builder notificationBuilder =
                new NotificationCompat.Builder(context)
                        .setSmallIcon(R.drawable.icon)
                        .setContentTitle(title)
                        .setContentText(subnetmask)
                        .setContentIntent(viewPendingIntent)
                        .setStyle(bigStyle)
                        .extend(wearableExtender);

        // Get an instance of the NotificationManager service
        NotificationManagerCompat notificationManager =
                NotificationManagerCompat.from(context);

        // Build the notification and issues it with notification manager.
        notificationManager.notify(notificationId, notificationBuilder.build());
    }
}
