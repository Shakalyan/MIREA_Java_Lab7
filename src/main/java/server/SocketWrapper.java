package server;

import entities.Message;

import java.io.*;
import java.net.Socket;
import java.nio.Buffer;

public class SocketWrapper
{
    private static int nextId = 1;

    private int id;
    private Socket socket;
    private String name;

    private BufferedReader reader;
    private BufferedWriter writer;



    public SocketWrapper(Socket socket, String name, int id)
    {
        this.socket = socket;
        this.name = name;
        this.id = id;

        try
        {
            reader = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            writer = new BufferedWriter(new OutputStreamWriter(socket.getOutputStream()));
        }
        catch(IOException e)
        {
            System.out.println(e.getMessage());
        }
    }

    public SocketWrapper(Socket socket)
    {
        this(socket, "Client" + nextId, nextId);
        ++nextId;
    }

    public void sendMessage(String text, int receiverId) throws IOException
    {
        sendMessage(new Message(ServerConfiguration.serverSocketWrapper, receiverId, text));
    }

    public void sendMessage(Message message) throws IOException
    {
        String text = String.format("<%s%s>: %s\n"  , message.getSender().getName()
                                                    , (message.getSender().getId() == ServerConfiguration.SERVER_ID)? "" : "#" + message.getSender().getId()
                                                    , message.getText());

        writer.write(text);
        writer.flush();
    }

    public Message getMessage() throws IOException
    {
        String input = reader.readLine();
        if(input == null)
            return null;

        int id = getIdFromInput(input);
        if(id == -1)
            return null;

        String text;
        if(id == 0)
            text = input;
        else
            text = input.substring(0, input.lastIndexOf("<#"));

        return new Message(this, id, text);
    }

    private int getIdFromInput(String input)
    {
        int openIndex = input.lastIndexOf("<#");
        int closeIndex = input.lastIndexOf(">");

        if(openIndex == -1 && closeIndex == -1)
            return 0;

        if(openIndex == -1 || closeIndex == -1 || openIndex > closeIndex)
            return -1;

        String id = input.substring(openIndex + 2, closeIndex);

        for(int i = 0; i < id.length(); ++i)
            if(!Character.isDigit(id.charAt(i)))
                return -1;

        return Integer.parseInt(id);
    }

    public void terminate()
    {
        try
        {
            if(socket != null) socket.close();
            if(reader != null) reader.close();
            if(writer != null) writer.close();
        }
        catch(IOException e)
        {
            System.out.println(e.getMessage());
        }
    }

    @Override
    public boolean equals(Object o)
    {
        if(o instanceof SocketWrapper && ((SocketWrapper) o).id == id)
                return true;
        return false;
    }

    public boolean isActive()
    {
        return socket.isConnected();
    }

    public int getId()
    {
        return id;
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

}
