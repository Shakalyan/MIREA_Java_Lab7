package server;

import general.Message;
import general.Response;

import java.io.*;
import java.net.ServerSocket;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.Scanner;

public class Server
{

    private static ServerSocket serverSocket;

    private static LinkedList<SocketWrapper> clients = new LinkedList<>();

    public static final String password = "12345";


    private static Task changeName = new Task("Put your name here:") {
        @Override
        public Response doTask(SocketHandler client, String input) throws IOException
        {
            client.getSocketWrapper().setName(input);
            return new Response(true, "Name's changed to " + client.getSocketWrapper().getName());
        }
    };

    private static Task requirePassword = new Task("Put password here:")
    {
        @Override
        public Response doTask(SocketHandler client, String input) throws IOException
        {
            if(input.equals(password))
                return new Response(true, "Password is correct");
            else
                return new Response(false, "Password is wrong");
        }
    };


    public static void main(String[] args)
    {

        ArrayList<Task> initialTasks = new ArrayList<>();
        initialTasks.add(requirePassword);
        initialTasks.add(changeName);

        try
        {
            serverSocket = new ServerSocket(8080);
            System.out.println("Server.Server has been run");

            ConnectionsController connectionsController = new ConnectionsController(serverSocket, clients, initialTasks);
            connectionsController.start();

            Scanner scanner = new Scanner(System.in);
            String input = "";
            int id = 0;
            do
            {

            }
            while(id != 239842);

            connectionsController.terminate();

            connectionsController.join();

        }
        catch(IOException e)
        {
            System.out.println(e.getMessage());
        }
        catch(InterruptedException e)
        {
            System.out.println(e.getMessage());
        }
        finally
        {
            try
            {
                if(serverSocket != null) serverSocket.close();
            }
            catch(IOException e)
            {
                System.out.println(e.getMessage());
            }

        }

    }

}
