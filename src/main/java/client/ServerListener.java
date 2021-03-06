package client;

import general.Message;
import general.MessageBuilder;
import general.Pair;

import java.io.*;
import java.net.Socket;
import java.util.Base64;
import java.util.LinkedList;

import static java.lang.Thread.interrupted;

public class ServerListener implements Runnable
{

    private Socket clientSocket;
    private BufferedReader reader;
    private LinkedList<Pair<Object, String>> clientObjects;

    ServerListener(Socket clientSocket, LinkedList<Pair<Object, String>> clientObjects)
    {
        this.clientSocket = clientSocket;
        this.clientObjects = clientObjects;

        try
        {
            reader = new BufferedReader(new InputStreamReader(clientSocket.getInputStream()));
        }
        catch(IOException e)
        {
            System.out.println(e.getMessage());
        }

    }


    @Override
    public void run()
    {
        String serverMessage;
        try
        {
            while(!interrupted())
            {
                serverMessage = MessageBuilder.readMessage("Server listener", reader);
                String senderInfo = getSenderInfo(serverMessage);
                serverMessage = MessageBuilder.convertToString(handleMessage(MessageBuilder.convertToMessage(serverMessage)), senderInfo);
                if(serverMessage != null)
                    System.out.println(hideMessageInfo(serverMessage));
            }
        }
        catch(Exception e)
        {
            System.out.println(e.getMessage());
        }
        finally
        {
            terminate();
        }
    }

    private static Object objectFromByteString(String byteString) throws IOException, ClassNotFoundException
    {
        byte[] bytes = Base64.getDecoder().decode(byteString);
        ByteArrayInputStream ios = new ByteArrayInputStream(bytes);
        ObjectInputStream ois = new ObjectInputStream(ios);
        return ois.readObject();
    }

    private Message handleMessage(Message message) throws IOException, ClassNotFoundException
    {
        if(message.getType() == Message.Type.File)
        {
            int dotIndex = message.getExtraInfo().indexOf(".");
            String fileName = message.getExtraInfo().substring(0, dotIndex);
            String fileExtension = message.getExtraInfo().substring(dotIndex);
            String filePath = fileName + "C" + fileExtension;
            writeFile(filePath, message.getText());
            message.setText("File's been saved");
            return message;
        }

        if(message.getType() == Message.Type.Object)
        {
            Object object = objectFromByteString(message.getText());

            synchronized(clientObjects)
            {
                clientObjects.add(new Pair(object, message.getExtraInfo()));
            }

            message.setText("Object has been saved");
            return message;
        }

        return message;
    }

    private void writeFile(String filePath, String text) throws IOException
    {
        File file = new File(filePath);
        BufferedWriter writer = new BufferedWriter(new FileWriter(file));
        writer.write(text);
        writer.flush();
        writer.close();
    }


    private void terminate()
    {
        try
        {
            if(reader != null)
                reader.close();
        }
        catch(IOException e)
        {
            System.out.println(e.getMessage());
        }
    }

    private String hideMessageInfo(String message)
    {
        if(message == null)
            return null;
        int indexInfoEnd = message.indexOf(" ");
        if(indexInfoEnd == -1)
            return null;
        return message.substring(indexInfoEnd + 1);
    }

    private String getSenderInfo(String message)
    {
        message = hideMessageInfo(message);
        int endIndex = message.indexOf(">");
        return message.substring(0, endIndex + 1);
    }

}
