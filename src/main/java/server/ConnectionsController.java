package server;

import java.io.IOException;
import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.LinkedList;

import static java.lang.Thread.interrupted;

public class ConnectionsController
{

    private LinkedList<SocketWrapper> clients;

    private ServerSocket serverSocket;

    private Thread connectionsCleaner;
    private Thread connectionsAccepter;

    private SocketsController socketsController;
    private Thread socketsControllerThread;


    public ConnectionsController(ServerSocket serverSocket, LinkedList<SocketWrapper> clients)
    {
        this.clients = clients;
        this.serverSocket = serverSocket;

        connectionsAccepter = new Thread(new ConnectionsAccepter());
        connectionsCleaner = new Thread(new ConnectionsCleaner());

        socketsController = new SocketsController();
        socketsControllerThread = new Thread(socketsController);
    }

    public void start()
    {
        connectionsAccepter.start();
        connectionsCleaner.start();
        socketsControllerThread.start();
    }

    public void terminate()
    {
        connectionsAccepter.interrupt();
        connectionsCleaner.interrupt();
        socketsControllerThread.interrupt();
    }

    public void join() throws InterruptedException
    {
        connectionsAccepter.join();
        connectionsCleaner.join();
        socketsControllerThread.join();
    }


    private class ConnectionsAccepter implements Runnable
    {
        @Override
        public void run()
        {
            try
            {
                while(!interrupted())
                {
                    SocketWrapper socketWrapper = new SocketWrapper(serverSocket.accept());
                    synchronized(clients)
                    {
                        clients.add(socketWrapper);
                    }
                    synchronized(socketsController)
                    {
                        socketsController.addHandler(socketWrapper);
                    }
                }
            }
            catch(IOException e)
            {
                System.out.println("Connection accepter: " + e.getMessage());
            }
        }
    }

    private class ConnectionsCleaner implements Runnable
    {
        private static final int cleanDelay = 5000;

        @Override
        public void run()
        {
            ArrayList<SocketWrapper> clientsToDelete = new ArrayList<>();
            while(!interrupted())
            {
                synchronized(clients)
                {
                    for(var client : clients)
                        if(!client.isActive())
                            clientsToDelete.add(client);
                    clients.removeAll(clientsToDelete);
                }

                synchronized(socketsController)
                {
                    for(var client : clientsToDelete)
                        socketsController.removeHandler(client);
                }

                try
                {
                    Thread.sleep(cleanDelay);
                }
                catch(InterruptedException e)
                {
                    System.out.println(e.getMessage());
                }

            }
        }
    }


}
