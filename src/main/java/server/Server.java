package server;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.LinkedList;
import java.util.Scanner;

public class Server
{

    private static ServerSocket serverSocket;

    public static final String password = "12345";



    public static void main(String[] args)
    {

        try
        {
            serverSocket = new ServerSocket(8080);
            System.out.println("Server.Server has been run");
            Thread connectionsHandler = new Thread(new ConnectionsHandler(serverSocket));
            connectionsHandler.start();
            new Scanner(System.in).nextLine();
            connectionsHandler.interrupt();

        }
        catch(IOException e)
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
