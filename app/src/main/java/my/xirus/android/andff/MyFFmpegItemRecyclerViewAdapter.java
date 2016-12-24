package my.xirus.android.andff;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.net.Uri;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.NotificationCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.github.hiteshsondhi88.libffmpeg.FFmpeg;

import java.io.File;
import java.util.List;

/**
 * {@link RecyclerView.Adapter} that can display a {@link FFmpegListContent.FFmpegItem} and makes a call to the
 * specified {@link FFmpegItemFragment.OnListFragmentInteractionListener}.
 */
public class MyFFmpegItemRecyclerViewAdapter extends RecyclerView.Adapter<MyFFmpegItemRecyclerViewAdapter.ViewHolder> {

    private final List<FFmpegListContent.FFmpegItem> mValues;
    private final FFmpegItemFragment.OnListFragmentInteractionListener mListener;
    private Context _context;

    public MyFFmpegItemRecyclerViewAdapter(List<FFmpegListContent.FFmpegItem> items, FFmpegItemFragment.OnListFragmentInteractionListener listener) {
        mValues = items;
        mListener = listener;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.fragment_ffmpegitem, parent, false);
        _context = parent.getContext();
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        int _progress = mValues.get(position).progress;

        holder.mItem = mValues.get(position);
        holder.mSubjectView.setText(mValues.get(position).outputFile);
        holder.mProgressView.setProgress(mValues.get(position).progress);

        if (_progress < 100) {
            mValues.get(position).isRunning = true;
            holder.mContentView.setText("Converting to " + mValues.get(position).extension);
        } else if (_progress == 100) {
            mValues.get(position).isRunning = false;
//            if (holder.mItem.isFailed) holder.mContentView.setText("Failed.");
//            else holder.mContentView.setText("Completed.");
        }

        holder.mView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (null != mListener) {
                    // Notify the active callbacks interface (the activity, if the
                    // fragment is attached to one) that an item has been selected.
                    mListener.onListFragmentInteraction(holder.mItem);

                    if (holder.mItem.isCompleted) {
                        Uri _uri = Uri.fromFile(new File(holder.mItem.outputFile));
                        Intent intent = new Intent(Intent.ACTION_VIEW, _uri);
                        _context.startActivity(intent);
                    }
                }
            }
        });

        ImageButton _img = (ImageButton) holder.mView.findViewById(R.id.imageButtonCancel);
        _img.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new AlertDialog.Builder(_context)
                        .setTitle("Delete entry")
                        .setMessage("Are you sure you want to delete this entry?")
                        .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                if (mValues.get(position).progress > 0) {
                                    FFmpeg ffmpeg = FFmpeg.getInstance(_context);
                                    if (ffmpeg.isFFmpegCommandRunning()) {
                                        boolean killedFFmpeg = ffmpeg.killRunningProcesses();

                                        Intent mIntent = new Intent(_context, MainActivity.class);
                                        PendingIntent pmIntent = PendingIntent.getActivity(_context, 0, mIntent, 0);

                                        Notification myNotification = new NotificationCompat.Builder(_context)
                                                .setContentTitle("AndFF")
                                                .setContentText("Canceled")
                                                .setContentIntent(pmIntent)
                                                .setTicker("AndFF Notification: Canceled")
                                                .setWhen(System.currentTimeMillis())
                                                .setDefaults(Notification.DEFAULT_LIGHTS)
                                                .setAutoCancel(true)
                                                .setSmallIcon(R.drawable.ic_stat_andff)
                                                .build();

                                        NotificationManager notificationManager = (NotificationManager) _context.getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
                                        notificationManager.notify(0, myNotification);

                                        File _output = new File(mValues.get(position).outputFile);
                                        _output.delete();
                                    }
                                }

                                Toast.makeText(_context, "Removed", Toast.LENGTH_SHORT).show();

                                mValues.remove(position);
                                notifyItemRemoved(position);
                                notifyItemRangeChanged(position, mValues.size());
                            }
                        })
                        .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                            public void onClick(DialogInterface dialog, int which) {
                                // do nothing
                            }
                        })
                        .setIcon(android.R.drawable.ic_dialog_alert)
                        .show();
            }
        });
    }

    @Override
    public int getItemCount() {
        return mValues.size();
    }

    public class ViewHolder extends RecyclerView.ViewHolder {
        public final View mView;
        public final TextView mSubjectView;
        public final TextView mContentView;
        public final ProgressBar mProgressView;
        public FFmpegListContent.FFmpegItem mItem;

        public ViewHolder(View view) {
            super(view);
            mView = view;
            mSubjectView = (TextView) view.findViewById(R.id.subject);
            mContentView = (TextView) view.findViewById(R.id.content);
            mProgressView = (ProgressBar) view.findViewById(R.id.ffmpegProgressBar);
        }

        @Override
        public String toString() {
            return super.toString() + " '" + mContentView.getText() + "'";
        }
    }
}
