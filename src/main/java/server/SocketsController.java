package server;

import general.*;

import java.io.IOException;
import java.util.ArrayDeque;
import java.util.LinkedList;

import static java.lang.Thread.interrupted;

public class SocketsController implements Runnable
{

    private ArrayDeque<Message> messagesPool;
    private LinkedList<Message> last10Messages;

    private LinkedList<SocketHandler> socketHandlers;

    private static final int checkTimer = 1000;

    public SocketsController()
    {
        messagesPool = new ArrayDeque<>();
        socketHandlers = new LinkedList<>();
        last10Messages = new LinkedList<>();
    }

    public void addHandler(SocketWrapper socketWrapper)
    {
        SocketHandler newHandler = new SocketHandler(socketWrapper, messagesPool);
        newHandler.start();

        synchronized(socketHandlers)
        {
            socketHandlers.add(newHandler);
        }

        synchronized(last10Messages)
        {
            for(var message : last10Messages)
            {
                newHandler.addMessage(message);
            }
        }
    }

    public void removeHandler(SocketWrapper socketWrapper)
    {
        synchronized(socketHandlers)
        {
            for(var handler : socketHandlers)
            {
                if(handler.getSocketWrapper().equals(socketWrapper))
                {
                    handler.terminate();
                    socketHandlers.remove(handler);
                    return;
                }
            }
        }
    }

    public void addMessage(Message message)
    {
        synchronized(messagesPool)
        {
            messagesPool.addFirst(message);
        }
    }

    public void addTask(Task task, SocketWrapper sender, int receiverId)
    {
        if(receiverId == 0)
        {
            synchronized(socketHandlers)
            {
                for(var handler : socketHandlers)
                    if(handler.getSocketWrapper() != sender)
                        handler.addTask(task);
                return;
            }
        }

        SocketHandler handler = getHandlerById(receiverId);
        if(handler == null)
            return;

        handler.addTask(task);
    }


    @Override
    public void run()
    {
        while(!interrupted())
        {
            try
            {
                synchronized(messagesPool)
                {
                    while(!messagesPool.isEmpty())
                    {
                        Message message = messagesPool.pollFirst();
                        message = handleMessage(message);
                        if(message != null && message.getReceiverId() == 0)
                        {
                            synchronized(last10Messages)
                            {
                                if(last10Messages.size() == 10)
                                    last10Messages.pollFirst();
                                last10Messages.addLast(message);
                            }
                        }
                        sendMessage(message);
                    }
                }

                Thread.sleep(checkTimer);
            }
            catch(IOException e)
            {
                Log.writeInfo("[SocketsController][run]: exception: " + e.getMessage());
            }
            catch(InterruptedException e)
            {
                Log.writeInfo("[SocketsController][run]: exception: " + e.getMessage());
                break;
            }

        }
        Log.writeInfo("[SocketsController][run]: terminated");
    }

    private Message handleMessage(Message message) throws IOException
    {

        if(message.getType() == Message.Type.File)
        {
            Task sendFile = new Task(String.format("Do you want to save %s from %s?[y/n]", message.getExtraInfo(), message.getSender().getName()), message)
            {
                @Override
                public Response doTask(SocketHandler client, String input) throws IOException
                {
                    Log.writeInfo("[SocketsController][handleMessage]: " + getArg(1) + " input: " + input);

                    if(input.equals("y"))
                    {
                        client.addMessage((Message)getArg(0));
                        return new Response(true, "File has been sent");
                    }
                    else if(input.equals("n"))
                    {
                        return new Response(true, "File sending denied");
                    }
                    return new Response(false, "Put answer[y/n]");
                }
            };
            addTask(sendFile, message.getSender(), message.getReceiverId());
            return null;
        }
        else if(message.getType() == Message.Type.Object)
        {

            Task sendObject = new Task(String.format("Do you want to save %s from %s?[y/n]", message.getExtraInfo(), message.getSender().getName()), message)
            {
                @Override
                public Response doTask(SocketHandler client, String input) throws IOException
                {
                    Log.writeInfo("[SocketsController][handleMessage]: " + getArg(1) + " input: " + input);

                    if(input.equals("y"))
                    {
                        client.addMessage((Message)getArg(0));
                        return new Response(true, "Object has been sent");
                    }
                    else if(input.equals("n"))
                    {
                        return new Response(true, "Object sending denied");
                    }
                    return new Response(false, "Put answer[y/n]");
                }
            };
            addTask(sendObject, message.getSender(), message.getReceiverId());
            return null;

        }
        else if(message.getType() == Message.Type.Command)
        {

            if(message.getExtraInfo().equals(Command.ChangeInterlocutor.getCommandText()))
            {
                int id = Integer.parseInt(message.getText());
                message.getSender().setReceiverId(id);
                sendMessage(MessageBuilder.build("Interlocutor has been changed", ServerConfiguration.serverSocketWrapper, message.getSender().getId()));
                return null;
            }
            else if(message.getExtraInfo().equals(Command.ChangeName.getCommandText()))
            {
                message.getSender().setName(message.getText());
                sendMessage(MessageBuilder.build("Name has been changed", ServerConfiguration.serverSocketWrapper, message.getSender().getId()));
                return null;
            }

        }

        return message;
    }

    private void sendMessage(Message message) throws IOException
    {
        if(message == null)
            return;
        if(message.getReceiverId() == 0)
        {
            synchronized(socketHandlers)
            {
                for(var handler : socketHandlers)
                {
                    if(!handler.getSocketWrapper().equals(message.getSender()))
                        handler.addMessage(message);
                }
            }
            return;
        }

        SocketHandler receiverHandler = getHandlerById(message.getReceiverId());
        if(receiverHandler == null)
            return;
        receiverHandler.addMessage(message);
    }

    private SocketHandler getHandlerById(int id)
    {
        synchronized(socketHandlers)
        {
            for(var socketHandler : socketHandlers)
            {
                if(socketHandler.getSocketWrapper().getId() == id)
                    return socketHandler;
            }
        }
        return null;
    }

}
