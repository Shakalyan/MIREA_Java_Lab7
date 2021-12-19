package server;

import entities.Message;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayDeque;
import java.util.LinkedList;
import java.util.Scanner;

public class Server
{

    private static ServerSocket serverSocket;

    private static LinkedList<SocketWrapper> clients = new LinkedList<>();

    public static final String password = "12345";


    public static void main(String[] args)
    {

        try
        {
            serverSocket = new ServerSocket(8080);
            System.out.println("Server.Server has been run");

            ConnectionsController connectionsController = new ConnectionsController(serverSocket, clients);
            connectionsController.start();

            Scanner scanner = new Scanner(System.in);
            String input = scanner.nextLine();
            while(input != "exit")
                input = scanner.nextLine();

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
