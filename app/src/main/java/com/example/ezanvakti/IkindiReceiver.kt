package com.example.ezanvakti

import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.navigation.fragment.FragmentNavigator

class IkindiReceiver : BroadcastReceiver(){
    override fun  onReceive(context: Context?, intent: Intent?){
        val i = Intent(context, FragmentNavigator.Destination::class.java) // buradan patlayabilir dikkat
        intent!!.flags = Intent.FLAG_ACTIVITY_NEW_TASK // or clear task
        val pendingIntent = PendingIntent.getActivity(context,0,i,
            PendingIntent.FLAG_MUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)

        /*val rawPathUri: Uri = Uri.parse("android.resource://" + "com.example.ezanvakti" + "/" + R.raw.ezan1)
        val r = RingtoneManager.getRingtone(context, rawPathUri)
        r.play()*/

        val builder = NotificationCompat.Builder(context!!,"ezanvakti")
            .setSmallIcon((R.drawable.ic_launcher_foreground))
            .setContentTitle("EZAN VAKTİ ALARM")
            .setContentText("ikindi")
            .setAutoCancel(true)
            .setDefaults((NotificationCompat.DEFAULT_ALL))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)

        val notificationManager = NotificationManagerCompat.from(context)
        notificationManager.notify(notificationID4,builder.build())



    }

}