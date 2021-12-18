package server;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

import static java.lang.Thread.interrupted;

public class SocketListener implements Runnable
{
    private ServerSocket serverSocket;
    private Socket clientSocket;

    private BufferedReader reader;
    private BufferedWriter writer;

    public SocketListener(ServerSocket serverSocket, Socket clientSocket)
    {
        this.serverSocket = serverSocket;
        this.clientSocket = clientSocket;

        try
        {
            reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
            writer = new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream()));
        }
        catch(IOException e)
        {
            System.out.println(e.getMessage());
        }

    }

    @Override
    public void run()
    {
        try
        {
            sendMessage("Hello! Put your name:");
            String input;
            while(!interrupted() || socketIsActive())
            {
                input = reader.readLine();
                if(input != null)
                    System.out.println("Client: " + input);
            }
        }
        catch(IOException e)
        {
            System.out.println(e.getMessage());
        }

        terminate();
    }

    private void sendMessage(String message) throws IOException
    {
        writer.write(message + "\n");
        writer.flush();
    }

    private void terminate()
    {
        try
        {
            if(clientSocket != null)
                clientSocket.close();
            if(reader != null)
                reader.close();
            if(writer != null)
                writer.close();
        }
        catch(IOException e)
        {
            System.out.println(e.getMessage());
        }
    }

    public boolean socketIsActive()
    {
        return clientSocket.isConnected();
    }

}
