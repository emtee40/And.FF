package my.xirus.android.andff;

/**
 * Created by zaidaiman on 07/06/2016.
 */
public class FFmpegProgressBar {
    private float duration = 0, time = 0;
    private int progress = 0;

    public int Calculation(String update) {
        if (update != null) {
            if (update.indexOf("Duration: ") > 0) {
                String[] str = update.split(",");
                String rawDuration = str[0].substring(update.indexOf("Duration: "), str[0].length());
                rawDuration = rawDuration.replace("Duration: ", "");
                String[] ar = rawDuration.split(":");
                duration = Float.parseFloat(ar[2]);
                duration += Integer.parseInt(ar[1]) * 60;
                duration += Integer.parseInt(ar[0]) * 60 * 60;

            } else if (update.indexOf("time=") > 0) {
                String s = update.substring(update.indexOf("time="));
                s = s.replace("time=", "");
                String rawTime = s.substring(0, s.indexOf(" "));
                String[] ar = rawTime.split(":");
                time = Float.parseFloat(ar[2]);
                time += Integer.parseInt(ar[1]) * 60;
                time += Integer.parseInt(ar[0]) * 60 * 60;
            }

            try {
                int i = Math.round((time / duration) * 100);
                progress = i;
            } catch (Exception e) {

            }

            return progress;
        }
        return 0;
    }
}
