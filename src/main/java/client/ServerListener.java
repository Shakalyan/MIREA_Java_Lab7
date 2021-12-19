package client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.Socket;

import static java.lang.Thread.interrupted;

public class ServerListener implements Runnable
{

    private Socket clientSocket;
    private BufferedReader reader;

    ServerListener(Socket clientSocket)
    {
        this.clientSocket = clientSocket;

        try
        {
            reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        }
        catch(IOException e)
        {
            System.out.println(e.getMessage());
        }

    }


    @Override
    public void run()
    {
        String serverMessage;
        try
        {
            while(!interrupted())
            {
                serverMessage = reader.readLine();
                if(serverMessage != null)
                    System.out.println(serverMessage);
            }
        }
        catch(IOException e)
        {
            System.out.println(e.getMessage());
        }
        finally
        {
            terminate();
        }

    }

    private void terminate()
    {
        try
        {
            if(reader != null)
                reader.close();
        }
        catch(IOException e)
        {
            System.out.println(e.getMessage());
        }
    }

}
