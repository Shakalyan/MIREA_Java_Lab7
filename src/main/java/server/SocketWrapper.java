package server;

import general.Message;
import general.MessageBuilder;
import general.Response;

import java.io.*;
import java.net.Socket;

public class SocketWrapper
{
    private static int nextId = 1;

    private int id;
    private Socket socket;
    private String name;

    private BufferedReader reader;
    private BufferedWriter writer;

    private int receiverId;



    public SocketWrapper(Socket socket, String name, int id)
    {
        this.socket = socket;
        this.name = name;
        this.id = id;
        this.receiverId = ServerConfiguration.BROADCAST_ID;

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

    public void sendMessage(Message message) throws IOException
    {
        writer.write(MessageBuilder.convertToString(message));
        writer.flush();
    }

    public Message getMessage() throws IOException
    {
        String input = MessageBuilder.readMessage("Socket Wrapper",reader);
        Message message = MessageBuilder.convertToMessage(input);

        message.setSender(this);
        message.setReceiverId(receiverId);

        return message;
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

    public int getReceiverId()
    {
        return receiverId;
    }

    public void setReceiverId(int id)
    {
        receiverId = id;
    }

}
