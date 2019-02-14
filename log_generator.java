import java.io.PrintWriter;
import java.text.SimpleDateFormat;
import java.util.Date;

public class log_generator
{
    static String[] log_buffer = new String[300];
    static int position = 0;
    static int file_index = 0;
    static String log_prefix = "log_";
    static String log_suffix = ".log";

    public static void put_log(String msg)
    {
        String log_line = "";

        /* Build log line */
        SimpleDateFormat dateString = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
        log_line += dateString.format(new Date());
        log_line += " " + msg;

        log_buffer[position] = log_line;
        position++;

        /* Write log buffer to file */
        if(position == 300)
        {
            /* replace this function */
            write_logfile(300);

            /* Reset position */
            position = 0;
            file_index += 1;
        }
    }

    public static void force_write_buffer()
    {
        write_logfile(position);
    }

    /* Currently, write log file to local filesystem */
    public static void write_logfile(int count)
    {
        /* Define log file name */
        String file_name = log_prefix + String.valueOf(file_index) + log_suffix;

        try
        {
            PrintWriter writer = new PrintWriter(file_name, "UTF-8");
            for (int i = 0; i < count; ++i)
                writer.println(log_buffer[i]);
            writer.close();
        } catch(Exception e){}
    }

    /* Debugging function */
    public static void show_buffer()
    {
        System.out.println("//--  Log buffer contents start  --//");
        for(int i = 0; i < position; ++i)
        {
            System.out.println(log_buffer[i]);
        }
        System.out.println("//--  Log buffer contents end  --//");
    }
}