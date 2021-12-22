package general;

import java.util.ArrayList;

public class Log
{

    private static ArrayList<String> logHistory = new ArrayList<>();
    private static AutoSaveLogWorker autoSaveLogWorker;
    private static Thread logThread;

    static
    {
        autoSaveLogWorker = new AutoSaveLogWorker();
        logThread = new Thread(autoSaveLogWorker);
    }

    public static void writeInfo(String message)
    {
        System.out.println(message);
        logHistory.add(message);
        synchronized (autoSaveLogWorker.getLogQueue())
        {
            autoSaveLogWorker.getLogQueue().addLast(message);
        }
    }

    public static void printHistory()
    {
        for(var m : logHistory)
            System.out.println(m);
    }

    public static void startAutoLogger()
    {
        logThread.start();
    }

    public static void stopAutoLogger() throws InterruptedException
    {
        logThread.interrupt();
        logThread.join();
    }

}