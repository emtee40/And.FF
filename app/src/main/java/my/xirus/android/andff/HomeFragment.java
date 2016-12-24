package my.xirus.android.andff;

import android.app.Activity;
import android.app.Application;
import android.content.BroadcastReceiver;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.annimon.stream.Collectors;
import com.annimon.stream.Stream;
import com.github.hiteshsondhi88.libffmpeg.FFmpeg;
import com.github.hiteshsondhi88.libffmpeg.LoadBinaryResponseHandler;
import com.github.hiteshsondhi88.libffmpeg.exceptions.FFmpegNotSupportedException;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * A simple {@link Fragment} subclass.
 * Activities that contain this fragment must implement the
 * {@link OnFragmentInteractionListener} interface
 * to handle interaction events.
 * Use the {@link HomeFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class HomeFragment extends Fragment {
    private Spinner spinner_format;

    private String ffmpeg_input;
    private String ffmpeg_output;

    private Context _context;
    private Application _app;
    private FFmpegProgressBar _ffmpegProgressBar;

    private MyBroadcastReceiver myBroadcastReceiver;
    private MyBroadcastReceiver_Update myBroadcastReceiver_Update;

    private OnFragmentInteractionListener mListener;

    public HomeFragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment HomeFragment.
     */
    public static HomeFragment newInstance(String param1, String param2) {
        HomeFragment fragment = new HomeFragment();
        Bundle args = new Bundle();
        fragment.setArguments(args);
        return fragment;
    }

    /**
     * Get a file path from a Uri. This will get the the path for Storage Access
     * Framework Documents, as well as the _data field for the MediaStore and
     * other file-based ContentProviders.
     *
     * @param context The context.
     * @param uri     The Uri to query.
     * @author paulburke
     */
    public static String getPath(final Context context, final Uri uri) {

        final boolean isKitKat = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;

        // DocumentProvider
        if (isKitKat && DocumentsContract.isDocumentUri(context, uri)) {
            // ExternalStorageProvider
            if (isExternalStorageDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];


                if ("primary".equalsIgnoreCase(type)) {
                    return Environment.getExternalStorageDirectory() + "/" + split[1];
                } else {
                    final String[] xSplit = Environment.getExternalStorageDirectory().getPath().split("/");
                    String tmp = "/" + xSplit[1] + "/" + type + "/" + split[1];
                    return tmp;
                }
            }
            // DownloadsProvider
            else if (isDownloadsDocument(uri)) {

                final String id = DocumentsContract.getDocumentId(uri);
                final Uri contentUri = ContentUris.withAppendedId(
                        Uri.parse("content://downloads/public_downloads"), Long.valueOf(id));

                return getDataColumn(context, contentUri, null, null);
            }
            // MediaProvider
            else if (isMediaDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                Uri contentUri = null;
                if ("image".equals(type)) {
                    contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                } else if ("video".equals(type)) {
                    contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                } else if ("audio".equals(type)) {
                    contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                }

                final String selection = "_id=?";
                final String[] selectionArgs = new String[]{
                        split[1]
                };

                return getDataColumn(context, contentUri, selection, selectionArgs);
            }
        }
        // MediaStore (and general)
        else if ("content".equalsIgnoreCase(uri.getScheme())) {
            return getDataColumn(context, uri, null, null);
        }
        // File
        else if ("file".equalsIgnoreCase(uri.getScheme())) {
            return uri.getPath();
        }

        return null;
    }

    /**
     * Get the value of the data column for this Uri. This is useful for
     * MediaStore Uris, and other file-based ContentProviders.
     *
     * @param context       The context.
     * @param uri           The Uri to query.
     * @param selection     (Optional) Filter used in the query.
     * @param selectionArgs (Optional) Selection arguments used in the query.
     * @return The value of the _data column, which is typically a file path.
     */
    public static String getDataColumn(Context context, Uri uri, String selection,
                                       String[] selectionArgs) {

        Cursor cursor = null;
        final String column = "_data";
        final String[] projection = {
                column
        };

        try {
            cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs,
                    null);
            if (cursor != null && cursor.moveToFirst()) {
                final int column_index = cursor.getColumnIndexOrThrow(column);
                return cursor.getString(column_index);
            }
        } catch (Exception ex) {
            Toast.makeText(context, ex.toString(), Toast.LENGTH_LONG);
        } finally {
            if (cursor != null)
                cursor.close();
        }
        return null;
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is ExternalStorageProvider.
     */
    public static boolean isExternalStorageDocument(Uri uri) {
        return "com.android.externalstorage.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is DownloadsProvider.
     */
    public static boolean isDownloadsDocument(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
    }

    /**
     * @param uri The Uri to check.
     * @return Whether the Uri authority is MediaProvider.
     */
    public static boolean isMediaDocument(Uri uri) {
        return "com.android.providers.media.documents".equals(uri.getAuthority());
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        _app = getActivity().getApplication();
        _context = getContext();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_main, container, false);

        loadFFmpegLibrary();

        ArrayList<String> formats = new ArrayList<String>();
        formats.add(".mp4");
        formats.add(".mkv");
        formats.add(".avi");
        ArrayAdapter<String> adapterFormat = new ArrayAdapter<String>(_context, android.R.layout.simple_spinner_dropdown_item,
                Stream.of(formats).map(x -> x).sorted().collect(Collectors.toList()));
        spinner_format = (Spinner) view.findViewById(R.id.spinnerFormat);
        spinner_format.setAdapter(adapterFormat);

        addListenerOnButtonItemClicked(view);


        FloatingActionButton fab = (FloatingActionButton) container.getRootView().findViewById(R.id.fab);
        fab.setOnClickListener(childView -> {
            TextView tv = (TextView) getView().findViewById(R.id.textViewInputFileName);
            tv.setText("-- Please choose a file --");

            spinner_format = (Spinner) view.findViewById(R.id.spinnerFormat);
            String format = spinner_format.getSelectedItem().toString();

            ffmpeg_output += format;

            //Build cmd line to convert
            String[] cmd = new String[]{"-y", "-i", ffmpeg_input, ffmpeg_output};

            //Add current cmd to queue
            String id = String.valueOf((FFmpegListContent.ITEMS.size() + 1));
            FFmpegListContent.ITEMS.add(new FFmpegListContent.FFmpegItem(id, ffmpeg_input, ffmpeg_output, "", format, cmd));
            FFmpegItemFragment.rvAdapter.notifyDataSetChanged();

            //Check if there is data, if there is, run it
            if (FFmpegListContent.ITEMS.size() > 0) {
                RunFFmpegLibrary(cmd);
            }
        });

        // Inflate the layout for this fragment
        return view;
    }

    public void onButtonPressed(Uri uri) {
        if (mListener != null) {
            mListener.onFragmentInteraction(uri);
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    private void loadFFmpegLibrary() {
        FFmpeg ffmpeg = FFmpeg.getInstance(_context);
        try {
            ffmpeg.loadBinary(new LoadBinaryResponseHandler() {

                @Override
                public void onStart() {
                }

                @Override
                public void onFailure() {
                }

                @Override
                public void onSuccess() {
                }

                @Override
                public void onFinish() {
                }
            });
        } catch (FFmpegNotSupportedException e) {
            // Handle if FFmpeg is not supported by device
        }
    }

    private void addListenerOnButtonItemClicked(View view) {
        ImageButton imageButtonInput = (ImageButton) view.findViewById(R.id.imageButtonInput);
        imageButtonInput.setOnClickListener(v -> {
            Intent i = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            i.addCategory(Intent.CATEGORY_OPENABLE);
            i.setType("video/*");
            startActivityForResult(i, MainActivity.READ_REQUEST_CODE);
        });
    }

    private boolean RunFFmpegLibrary(String[] cmd) {
        Intent mServiceIntent = new Intent(_app, FFmpegIntentService.class);
        mServiceIntent.putExtra("cmd", cmd);
        _app.startService(mServiceIntent);

        myBroadcastReceiver = new MyBroadcastReceiver();
        myBroadcastReceiver_Update = new MyBroadcastReceiver_Update();

        IntentFilter intentFilter = new IntentFilter(FFmpegIntentService.ACTION_RESPONSE);
        intentFilter.addCategory(Intent.CATEGORY_DEFAULT);
        _app.registerReceiver(myBroadcastReceiver, intentFilter);

        IntentFilter intentFilter_update = new IntentFilter(FFmpegIntentService.ACTION_UPDATE);
        intentFilter_update.addCategory(Intent.CATEGORY_DEFAULT);
        _app.registerReceiver(myBroadcastReceiver_Update, intentFilter_update);

        _ffmpegProgressBar = new FFmpegProgressBar();
        return true;
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if ((requestCode == MainActivity.READ_REQUEST_CODE || requestCode == MainActivity.WRITE_REQUEST_CODE)
                && resultCode == Activity.RESULT_OK) {
            if (data == null) return;

            switch (requestCode) {
                case MainActivity.READ_REQUEST_CODE:
                    ffmpeg_input = getPath(_context, data.getData());

                    TextView tv1 = (TextView) getView().findViewById(R.id.textViewInputFileName);
                    tv1.setText(ffmpeg_input);

                    SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(_context);
                    int location = Integer.parseInt(preferences.getString("storage_location", "0"));

                    File[] caches = _context.getExternalMediaDirs();
                    ffmpeg_output = caches[location].getAbsolutePath() + "/" + new File(ffmpeg_input).getName().split("\\.")[0] + UUID.randomUUID().toString();
                    break;
                default:
                    break;
            }
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        try {
            //un-register BroadcastReceiver
            _app.unregisterReceiver(myBroadcastReceiver);
            _app.unregisterReceiver(myBroadcastReceiver_Update);
        } catch (Exception ex) {

        }
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        void onFragmentInteraction(Uri uri);
    }

    public class MyBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String result = intent.getStringExtra(FFmpegIntentService.KEY_RESPONSE);
            if (result.equals("complete_task")) {
                List<FFmpegListContent.FFmpegItem> list = Stream.of(FFmpegListContent.ITEMS)
                                                            .filter(x -> x.isCompleted == false && x.isFailed == false)
                                                            .collect(Collectors.toList());
                FFmpeg ffmpeg = FFmpeg.getInstance(_context);
                if (list.size() > 0 && !ffmpeg.isFFmpegCommandRunning()) {
                    RunFFmpegLibrary(list.get(0).cmd);
                }
            }
        }
    }

    public class MyBroadcastReceiver_Update extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            String update = intent.getStringExtra(FFmpegIntentService.KEY_UPDATE);
            List<FFmpegListContent.FFmpegItem> list = Stream.of(FFmpegListContent.ITEMS).filter(x -> x.isRunning == true).collect(Collectors.toList());
            if (list.size() > 0) {
                list.get(0).progress = _ffmpegProgressBar.Calculation(update);
            }
            FFmpegItemFragment.rvAdapter.notifyDataSetChanged();
        }
    }
}