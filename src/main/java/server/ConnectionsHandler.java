package server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedList;

import static java.lang.Thread.interrupted;

public class ConnectionsHandler implements Runnable
{

    private LinkedHashMap<SocketListener, Thread> sockets;
    private ServerSocket serverSocket;
    private Thread connectionsCleaner;


    public ConnectionsHandler(ServerSocket serverSocket)
    {
        sockets = new LinkedHashMap<>();
        this.serverSocket = serverSocket;
        connectionsCleaner = new Thread(new ConnectionsCleaner());
    }



    @Override
    public void run()
    {
        connectionsCleaner.start();
        try
        {
            while(!interrupted())
            {
                Socket newSocket = serverSocket.accept();
                System.out.println("Accepted!");
                Thread socketListener = new Thread(new SocketListener(serverSocket, newSocket));
                socketListener.start();
            }
        }
        catch(IOException e)
        {
            System.out.println(e.getMessage());
        }

        terminate();

    }

    private void terminate()
    {
        connectionsCleaner.interrupt();
        for(var socket : sockets.entrySet())
        {
            socket.getValue().interrupt();
        }
    }


    private class ConnectionsCleaner implements Runnable
    {
        private static final int cleanDelay = 5000;

        @Override
        public void run()
        {
            while(!interrupted())
            {
                synchronized(sockets)
                {
                    ArrayList<SocketListener> socketsToDelete = new ArrayList<>();
                    for(var socket : sockets.entrySet())
                    {
                        if(!socket.getKey().socketIsActive())
                        {
                            socket.getValue().interrupt();
                            socketsToDelete.add(socket.getKey());
                        }
                    }
                    for(var socket : socketsToDelete)
                        sockets.remove(socket);
                }

                try
                {
                    Thread.sleep(cleanDelay);
                }
                catch(InterruptedException e)
                {

                }

            }
        }
    }


}
