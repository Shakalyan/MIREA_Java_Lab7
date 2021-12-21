package general;

import server.SocketWrapper;

import java.net.Socket;

public class Message
{

    public enum Type
    {
        Text("Text"), File("File"), Command("Command");

        private String text;

        Type(String text)
        {
            this.text = text;
        }

        public String getText()
        {
            return text;
        }
    }

    private int length;
    private Type type;
    private String extraInfo;
    private String text;

    private SocketWrapper sender;
    private int receiverId;

    public Message(Type type, String extraInfo, String text, SocketWrapper sender, int receiverId)
    {
        length = text.length();
        this.type = type;
        this.extraInfo = extraInfo;
        this.text = text;

        this.sender = sender;
        this.receiverId = receiverId;
    }


    public int getLength()
    {
        return length;
    }

    public void setLength(int length)
    {
        this.length = length;
    }

    private void fixLength()
    {
        length = text.length();
    }

    public Type getType()
    {
        return type;
    }

    public void setType(Type type)
    {
        this.type = type;
    }

    public String getExtraInfo()
    {
        return extraInfo;
    }

    public void setExtraInfo(String extraInfo)
    {
        this.extraInfo = extraInfo;
    }

    public String getText()
    {
        return text;
    }

    public void setText(String text)
    {
        this.text = text;
        fixLength();
    }

    public SocketWrapper getSender()
    {
        return sender;
    }

    public void setSender(SocketWrapper sender)
    {
        this.sender = sender;
    }

    public int getReceiverId()
    {
        return receiverId;
    }

    public void setReceiverId(int id)
    {
        receiverId = id;
    }

    @Override
    public String toString()
    {
        return  length      + " " +
                type        + " " +
                extraInfo   + " " +
                text        + " " +
                sender      + " " +
                receiverId;
    }

}
