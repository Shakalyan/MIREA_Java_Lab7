package entities;

import server.SocketWrapper;

public class Message
{

    private SocketWrapper sender;
    private int receiverId;
    private String text;

    public Message(SocketWrapper sender, int receiverId, String text)
    {
        this.sender = sender;
        this.receiverId = receiverId;
        this.text = text;
    }

    public SocketWrapper getSender()
    {
        return sender;
    }

    public int getReceiverId()
    {
        return receiverId;
    }

    public String getText()
    {
        return text;
    }

}
