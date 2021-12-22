package general;

import java.io.*;
import java.util.LinkedList;

import static java.lang.Thread.interrupted;

public class AutoSaveLogWorker implements Runnable
{

    private static final File logFile = new File("Log.txt");

    private static final int timer = 5000;

    private LinkedList<String> logQueue = new LinkedList<>();


    @Override
    public void run()
    {
        FileWriter fileWriter = null;
        try
        {
            fileWriter = new FileWriter(logFile);
            while(!interrupted())
            {
                writeLog(fileWriter);
                Thread.sleep(timer);
            }
        }
        catch(IOException e)
        {
            Log.writeInfo("[AutoSaveLogWorker][run]: exception: " + e.getMessage());
        }
        catch(InterruptedException interruptedException)
        {
            Log.writeInfo("[AutoSaveLogWorker][run]: exception: " + interruptedException.getMessage());

            try
            {
                writeLog(fileWriter);
            }
            catch(IOException e)
            {
                Log.writeInfo("[AutoSaveLogWorker][run]: exception: " + e.getMessage());
            }

        }
        finally
        {
            try
            {
                if(fileWriter != null)
                    fileWriter.close();
            }
            catch(IOException e)
            {
                Log.writeInfo("[AutoSaveLogWorker][run]: exception: " + e.getMessage());
            }
        }
    }

    private void writeLog(FileWriter fileWriter) throws IOException
    {
        synchronized (logQueue)
        {
            while(!logQueue.isEmpty())
            {
                fileWriter.write(logQueue.pollFirst() + "\n");
            }
        }
        fileWriter.flush();
    }

    public LinkedList<String> getLogQueue()
    {
        return logQueue;
    }

}
