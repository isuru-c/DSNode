package dsnode;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * @author Isuru Chandima
 */
class Logger {

    private static int STD_OUT = 1;
    private static int FILE_OUT = 2;

    private int logType = 1;

    private boolean activeLogger = true;

    DateFormat dateFormat;
    Date date;

    Logger() {
        dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        date = new Date();
    }

    void log(String message) {

        if (!activeLogger)
            return;

        if (logType == STD_OUT) {
            // Write log status to the std_out

            System.out.print(String.format("%s - %s\n# ",dateFormat.format(date), message));
        }
        if (logType == FILE_OUT) {
            // Write log status to a file

        }
    }

    void useLogger(boolean activeLogger) {
        this.activeLogger = activeLogger;
    }
}
