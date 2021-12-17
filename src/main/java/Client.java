import java.io.*;
import java.net.Socket;
import java.util.Scanner;

public class Client
{

    public static Socket clientSocket;

    public static Scanner scanner = new Scanner(System.in);
    public static BufferedWriter bw;
    public static BufferedReader br;

    public static void main(String[] args)
    {



        try
        {
            clientSocket = new Socket("127.0.0.1", 8080);
            System.out.println(clientSocket.isConnected());

            bw = new BufferedWriter(new OutputStreamWriter(clientSocket.getOutputStream()));
            br = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));

            bw.write("Hello Server!\n");
            bw.flush();

            String input = "";
            String messageFromServer = "";
            do
            {
                input = scanner.nextLine();
                bw.write(input + "\n");
                bw.flush();
                messageFromServer = br.readLine();
                if(messageFromServer != null)
                    System.out.println("From server: " + messageFromServer);
            }
            while(input != "exit");


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
                if(bw != null) bw.close();
                scanner.close();
            }
            catch(IOException e)
            {
                System.out.println(e.getMessage());
            }
        }

    }

}
