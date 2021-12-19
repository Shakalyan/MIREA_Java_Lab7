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

    public SocketWrapper(Socket socket, String name)
    {
        this.socket = socket;
        this.name = name;
        this.id = nextId++;

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
        this(socket, "Client" + nextId);
    }

    public void sendMessage(Message message) throws IOException
    {
        writer.write(   "<" + message.getSender().getName() + "#" + message.getSender().getId() + ">: " +
                            message.getText() + "\n");
        writer.flush();
    }

    public Message getMessage() throws IOException
    {
        String text = reader.readLine();
        if(text == null)
            return null;
        return new Message(this, 0, text);
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

}
