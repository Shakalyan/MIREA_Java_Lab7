package server;

import general.Log;
import general.Message;
import general.MessageBuilder;

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

    private ArrayList<Task> initialTasks;


    public ConnectionsController(ServerSocket serverSocket, LinkedList<SocketWrapper> clients, ArrayList<Task> initialTasks)
    {
        this.clients = clients;
        this.serverSocket = serverSocket;
        this.initialTasks = (ArrayList<Task>)initialTasks.clone();

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

    public void sendServerMessage(String text, int receiverId)
    {

        Message message = MessageBuilder.build(text, ServerConfiguration.serverSocketWrapper, receiverId);

        synchronized(socketsController)
        {
            socketsController.addMessage(message);
        }
    }

    public void addTask(Task task, SocketWrapper sender, int receiverId)
    {
        synchronized(socketsController)
        {
            socketsController.addTask(task, sender, receiverId);
        }
    }

    private void addInitialTasks(SocketWrapper socketWrapper)
    {
        for(var task : initialTasks)
            addTask(task, ServerConfiguration.serverSocketWrapper, socketWrapper.getId());
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
                    Log.writeInfo("[ConnectionsAccepter][run]: accepted new socket");

                    synchronized(clients)
                    {
                        clients.add(socketWrapper);
                    }
                    synchronized(socketsController)
                    {
                        socketsController.addHandler(socketWrapper);
                    }
                    addInitialTasks(socketWrapper);
                }
            }
            catch(IOException e)
            {
                Log.writeInfo("[ConnectionsAccepter][run]: exception: " + e.getMessage());
            }
            Log.writeInfo("[ConnectionsAccepter][run]: terminated");
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
                    {
                        socketsController.removeHandler(client);
                        Log.writeInfo("[ConnectionsCleaner][run]: delete " + client.getName());
                    }
                }

                try
                {
                    Thread.sleep(cleanDelay);
                }
                catch(InterruptedException e)
                {
                    Log.writeInfo("[ConnectionsCleaner][run]: exception: " + e.getMessage());
                    break;
                }
            }
            Log.writeInfo("[ConnectionsCleaner][run]: terminated");
        }
    }


}
