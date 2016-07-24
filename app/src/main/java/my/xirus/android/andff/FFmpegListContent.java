package my.xirus.android.andff;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by zaidaiman on 12/06/2016.
 */
public class FFmpegListContent {
    public static final List<FFmpegItem> ITEMS = new ArrayList<FFmpegItem>();
    public static final Map<String, FFmpegItem> ITEM_MAP = new HashMap<String, FFmpegItem>();

    private static void addItem(FFmpegItem item) {
        ITEMS.add(item);
        ITEM_MAP.put(item.id, item);
    }

    public static class FFmpegItem {
        public final String id;
        public final String inputFile;
        public final String outputFile;
        public final String params;
        public final String extension;
        public final String[] cmd;
        public String inputFileName;
        public int progress;
        public boolean isCompleted;
        public boolean isFailed;

        public FFmpegItem(String id, String inputFile, String outputFile, String params, String extension, String[] cmd) {
            this.id = id;
            this.inputFile = inputFile;
            this.inputFileName = new File(inputFile).getName();
            this.outputFile = outputFile;
            this.params = params;
            this.extension = extension;
            this.cmd = cmd;
            this.isCompleted = false;
            this.isFailed = false;
        }
    }
}
