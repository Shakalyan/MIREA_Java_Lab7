package client;

import java.io.*;
import java.net.Socket;
import java.util.Scanner;

public class Client
{

    private static Socket clientSocket;

    private static Scanner scanner = new Scanner(System.in);
    private static BufferedWriter writer;
    private static Thread serverListener;

    public static void main(String[] args)
    {
        try
        {
            clientSocket = new Socket("127.0.0.1", 8080);

            System.out.println("System: Connected!");

            serverListener = new Thread(new ServerListener(clientSocket));
            serverListener.start();

            writer = new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream()));

            String message = "";
            while(clientSocket.isConnected() && !message.equals("/exit"))
            {
                System.out.print("send>");
                message = scanner.nextLine();
                sendMessage(message);
            }


        }
        catch(IOException e)
        {
            System.out.println(e.getMessage());
        }
        finally
        {
            try
            {
                if(clientSocket != null) clientSocket.close();
                if(writer != null) writer.close();
                scanner.close();
                serverListener.interrupt();
            }
            catch(IOException e)
            {
                System.out.println(e.getMessage());
            }
        }

    }


    private static void sendMessage(String message) throws IOException
    {
        writer.write(message + "\n");
        writer.flush();
    }

}
