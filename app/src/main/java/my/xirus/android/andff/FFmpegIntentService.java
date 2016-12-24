package my.xirus.android.andff;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.MediaScannerConnection;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.NotificationCompat;

import com.annimon.stream.Collectors;
import com.annimon.stream.Stream;
import com.github.hiteshsondhi88.libffmpeg.ExecuteBinaryResponseHandler;
import com.github.hiteshsondhi88.libffmpeg.FFmpeg;
import com.github.hiteshsondhi88.libffmpeg.exceptions.FFmpegCommandAlreadyRunningException;

import java.util.ArrayList;
import java.util.List;

/**
 * An {@link IntentService} subclass for handling asynchronous task requests in
 * a service on a separate handler thread.
 * <p>
 * helper methods.
 */
public class FFmpegIntentService extends IntentService {
    public static final String ACTION_RESPONSE = "android.xirus.andff.action.RESPONSE";
    public static final String ACTION_UPDATE = "android.xirus.andff.action.UPDATE";
    public static final String KEY_RESPONSE = "android.xirus.andff.KEY.RESPONSE";
    public static final String KEY_UPDATE = "android.xirus.andff.KEY.UPDATE";

    public ArrayList<String> str = new ArrayList<String>();
    NotificationManager notificationManager;
    Notification myNotification;
    FFmpegProgressBar _ffmpegProgressBar;

    public FFmpegIntentService() {
        super("FFmpegIntentService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        if (intent != null) {
            Bundle extras = intent.getExtras();
            String[] cmd = (String[]) extras.get("cmd");
            _ffmpegProgressBar = new FFmpegProgressBar();

            Intent mIntent = new Intent(getApplicationContext(), MainActivity.class);
            PendingIntent pmIntent = PendingIntent.getActivity(getApplicationContext(), 0, mIntent, 0);

            FFmpeg ffmpeg = FFmpeg.getInstance(getApplicationContext());
            try {
                // to execute "ffmpeg -version" command you just need to pass "-version"
                ffmpeg.execute(cmd, new ExecuteBinaryResponseHandler() {

                    @Override
                    public void onStart() {
                        //generate notification
                        myNotification = new NotificationCompat.Builder(getApplicationContext())
                                .setContentTitle("AndFF")
                                .setContentText("Starting up")
                                .setContentIntent(pmIntent)
                                .setTicker("AndFF Notification: Starting up")
                                .setWhen(System.currentTimeMillis())
                                .setDefaults(Notification.DEFAULT_LIGHTS)
                                .setAutoCancel(false)
                                .setOngoing(true)
                                .setSmallIcon(R.drawable.ic_stat_andff)
                                .build();

                        notificationManager.notify(0, myNotification);
                    }

                    @Override
                    public void onProgress(String message) {
                        Intent intentUpdate = new Intent();
                        intentUpdate.setAction(ACTION_UPDATE);
                        intentUpdate.addCategory(Intent.CATEGORY_DEFAULT);
                        intentUpdate.putExtra(KEY_UPDATE, message);
                        sendBroadcast(intentUpdate);

                        myNotification = new NotificationCompat.Builder(getApplicationContext())
                                .setContentTitle("AndFF")
                                .setContentText("In progress...")
                                .setContentIntent(pmIntent)
                                .setTicker("AndFF Notification: In progress...")
                                .setWhen(System.currentTimeMillis())
                                .setDefaults(Notification.DEFAULT_LIGHTS)
                                .setAutoCancel(false)
                                .setOngoing(true)
                                .setSmallIcon(R.drawable.ic_stat_andff)
                                .setProgress(100, _ffmpegProgressBar.Calculation(message), false)
                                .build();

                        notificationManager.notify(0, myNotification);
                    }

                    @Override
                    public void onFailure(String message) {
                        List<FFmpegListContent.FFmpegItem> list = Stream.of(FFmpegListContent.ITEMS).filter(x -> x.isCompleted == false).collect(Collectors.toList());
                        list.get(0).isCompleted = true;
                        list.get(0).isFailed = true;

                        myNotification = new NotificationCompat.Builder(getApplicationContext())
                                .setContentTitle("AndFF: Failed")
                                .setContentText(message)
                                .setTicker("AndFF Notification: Failed")
                                .setContentIntent(pmIntent)
                                .setWhen(System.currentTimeMillis())
                                .setDefaults(Notification.DEFAULT_LIGHTS)
                                .setAutoCancel(true)
                                .setSmallIcon(R.drawable.ic_stat_andff)
                                .build();
                        notificationManager.notify(0, myNotification);

                        FFmpegItemFragment.rvAdapter.notifyDataSetChanged();
                    }

                    @Override
                    public void onSuccess(String message) {
                        myNotification = new NotificationCompat.Builder(getApplicationContext())
                                .setContentTitle("AndFF")
                                .setContentText("Completed!")
                                .setContentIntent(pmIntent)
                                .setTicker("Notification!")
                                .setWhen(System.currentTimeMillis())
                                .setDefaults(Notification.DEFAULT_LIGHTS)
                                .setAutoCancel(true)
                                .setSmallIcon(R.drawable.ic_stat_andff)
                                .build();
                        notificationManager.notify(0, myNotification);
                    }

                    @Override
                    public void onFinish() {
                        //mark as complete
                        List<FFmpegListContent.FFmpegItem> list = Stream.of(FFmpegListContent.ITEMS).filter(x -> x.isCompleted == false).collect(Collectors.toList());
                        if (list.size() > 0) {
                            list.get(0).isCompleted = true;
                            list.get(0).isFailed = false;

                            MediaScannerConnection.scanFile(
                                    getApplicationContext(),
                                    new String[]{list.get(0).outputFile},
                                    null,
                                    new MediaScannerConnection.OnScanCompletedListener() {
                                        @Override
                                        public void onScanCompleted(String path, Uri uri) {
                                        }
                                    });

                            FFmpegItemFragment.rvAdapter.notifyDataSetChanged();

                            //return result
                            Intent intentResponse = new Intent();
                            intentResponse.setAction(ACTION_RESPONSE);
                            intentResponse.addCategory(Intent.CATEGORY_DEFAULT);
                            intentResponse.putExtra(KEY_RESPONSE, "complete_task");
                            sendBroadcast(intentResponse);
                        }
                    }
                });
            } catch (FFmpegCommandAlreadyRunningException e) {
                // Handle if FFmpeg is already running
            }
        }
    }

    @Override
    public void onCreate() {
        super.onCreate();
        notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
    }
}
