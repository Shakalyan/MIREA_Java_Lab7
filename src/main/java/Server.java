import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;

public class Server
{

    public static ServerSocket serverSocket;
    public static Socket clientSocket;

    public static BufferedReader br;
    public static BufferedWriter bw;

    public static final String password = "12345";



    public static void main(String[] args)
    {

        try
        {
            serverSocket = new ServerSocket(8080);
            System.out.println("Server has been run");

            if(connectWithClient())
                System.out.println("Connected!");
            else
                System.out.println("Some problems :(");

            String passwordFromClient = null;
            while(true)
            {
                passwordFromClient = getPasswordFromClient();
                if(passwordFromClient == password)
                {
                    bw.write("Password is correct!\n");
                    bw.flush();
                    break;
                }
                else
                {
                    System.out.println("Received password: " + passwordFromClient);
                    bw.write("Password is incorrect!\n");
                    bw.flush();
                }
            }


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
                if(clientSocket != null) clientSocket.close();
                if(br != null) br.close();
                if(bw != null) bw.close();
            }
            catch(IOException e)
            {
                System.out.println(e.getMessage());
            }

        }

    }

    private static boolean connectWithClient() throws IOException
    {
        clientSocket = serverSocket.accept();
        if(!clientSocket.isConnected())
            return false;

        br = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        bw = new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream()));

        return true;
    }

    private static String getPasswordFromClient() throws IOException, InterruptedException
    {
        bw.write("Put password\n");
        bw.flush();
        String input = null;
        do
        {
            input = br.readLine();
            if(input != null)
                return input;
            Thread.sleep(100);
        }
        while(true);
    }

}
