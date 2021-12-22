package client;

import fortask.Cat;
import general.Command;
import general.Message;
import general.MessageBuilder;
import general.Pair;

import java.io.*;
import java.net.Socket;
import java.util.Base64;
import java.util.LinkedList;
import java.util.Scanner;
import java.util.stream.Collectors;

public class Client
{

    private static Socket clientSocket;

    private static Scanner scanner = new Scanner(System.in);
    private static BufferedWriter writer;
    private static Thread serverListener;

    private static LinkedList<Pair<Object, String>> clientObjects = new LinkedList<>();


    public static void main(String[] args)
    {
        clientObjects.add(new Pair(new Cat("Boris", 10, "Black"), "Cat"));
        clientObjects.add(new Pair(new Cat[]{new Cat("Barsik", 30, "Barsovy"), new Cat("Murzik", 1, "SomeBreed")}, "Cat[]"));

        try
        {
            clientSocket = new Socket("127.0.0.1", 8080);

            System.out.println("System: Connected!");

            serverListener = new Thread(new ServerListener(clientSocket, clientObjects));
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

                    if(message.getExtraInfo().equals(Command.SendObject.getCommandText()))
                    {
                        int index = Integer.parseInt(message.getText().trim());
                        String byteString = toByteString(clientObjects.get(index).first);
                        message.setType(Message.Type.Object);
                        message.setExtraInfo(clientObjects.get(index).second);
                        message.setText(byteString);
                    }

                    if(message.getExtraInfo().equals(Command.PrintObjectsList.getCommandText()))
                    {
                        synchronized (clientObjects)
                        {
                            for(int i = 0; i < clientObjects.size(); ++i)
                                printObject(i);
                        }
                        continue;
                    }

                    if(message.getExtraInfo().equals(Command.PrintObject.getCommandText()))
                    {
                        int index = Integer.parseInt(message.getText().trim());
                        printObject(index);
                        continue;
                    }

                    if(message.getExtraInfo().equals(Command.DeleteObject.getCommandText()))
                    {
                        int index = Integer.parseInt(message.getText().trim());
                        synchronized (clientObjects)
                        {
                            clientObjects.remove(index);
                            System.out.println("Object has been deleted");
                        }
                        continue;
                    }

                    if(message.getExtraInfo().equals(Command.AddObject.getCommandText()))
                    {
                        String params = message.getText();
                        String objectClass = params.substring(0, params.indexOf(" "));
                        params = params.substring(params.indexOf(" ") + 1);

                        Object object = buildObject(objectClass, params);
                        if(object == null)
                        {
                            System.out.println("Cannot make an object");
                            continue;
                        }

                        clientObjects.add(new Pair(object, objectClass));
                        System.out.println("Object has been added");

                        continue;
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

    private static void printObject(int index)
    {
        synchronized (clientObjects)
        {
            if(clientObjects.get(index).second.contains("[]"))
            {
                Object[] objects = (Object[])clientObjects.get(index).first;
                System.out.println("Object[" + index + "]:");
                for(int i = 0; i < objects.length; ++i)
                    System.out.println(i + ": " + objects[i]);
            }
            else
                System.out.println("Object[" + index + "]: " + clientObjects.get(index).first);
        }
    }

    private static Object buildObject(String objectClass, String params)
    {
        if(objectClass.equals("Cat"))
        {
            int firstSpace = params.indexOf(" ");
            int secondSpace = params.indexOf(" ", firstSpace + 1);
            String name = params.substring(0, firstSpace);
            String age = params.substring(firstSpace + 1, secondSpace);
            String breed = params.substring(secondSpace + 1);
            return new Cat(name, Integer.parseInt(age), breed);
        }
        return null;
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


    private static String toByteString(Object object) throws IOException
    {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutputStream oos = new ObjectOutputStream(bos);
        oos.writeObject(object);
        return Base64.getEncoder().encodeToString(bos.toByteArray());
    }

}
