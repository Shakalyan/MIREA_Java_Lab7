package client;

import general.Command;
import general.Message;
import general.MessageBuilder;

import java.io.*;
import java.net.Socket;
import java.util.Scanner;
import java.util.stream.Collectors;

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

            String input = "";
            while(clientSocket.isConnected())
            {
                input = scanner.nextLine();
                Message message = MessageBuilder.build(input);

                if(message == null)
                {
                    System.out.println("ERROR");
                    continue;
                }

                if(message.getType().equals(Message.Type.Command))
                {
                    if(message.getExtraInfo().equals(Command.Exit.getCommandText()))
                        break;

                    if(message.getExtraInfo().equals(Command.SendFile.getCommandText()))
                    {
                        String fileData = readFile(message.getText());
                        if(fileData == null)
                            continue;

                        message.setExtraInfo(message.getText());
                        message.setText(fileData);
                        message.setType(Message.Type.File);
                    }
                }

                sendMessage(MessageBuilder.convertToString(message));
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


    private static void sendMessage(String input) throws IOException
    {
        String message = input;

        writer.write(message + "\n");
        writer.flush();
    }

    private static String readFile(String path)
    {
        File file = new File(path);
        String output;
        try(BufferedReader fileReader = new BufferedReader(new FileReader(file)))
        {
            output = fileReader.lines().collect(Collectors.joining(System.lineSeparator()));
        }
        catch(IOException e)
        {
            System.out.println(e.getMessage());
            return null;
        }

        return output;
    }


}
