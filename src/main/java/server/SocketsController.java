package server;

import entities.Message;

import javax.sound.sampled.Line;
import java.io.IOException;
import java.util.ArrayDeque;
import java.util.LinkedList;

import static java.lang.Thread.interrupted;

public class SocketsController implements Runnable
{

    private ArrayDeque<Message> messagesPool;

    private LinkedList<SocketHandler> socketHandlers;

    private static final int checkTimer = 1000;

    public SocketsController()
    {
        messagesPool = new ArrayDeque<>();
        socketHandlers = new LinkedList<>();
    }

    public void addHandler(SocketWrapper socketWrapper)
    {
        SocketHandler newHandler = new SocketHandler(socketWrapper, messagesPool);
        newHandler.start();

        synchronized(socketHandlers)
        {
            socketHandlers.add(newHandler);
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

    public void addTask(Task task, int receiverId)
    {
        if(receiverId == 0)
        {
            synchronized(socketHandlers)
            {
                for(var handler : socketHandlers)
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
                        sendMessage(message);
                        //messagesPool.remove(message);
                    }
                }

                Thread.sleep(checkTimer);
            }
            catch(IOException e)
            {
                System.out.println(e.getMessage());
            }
            catch(InterruptedException e)
            {
                System.out.println(e.getMessage());
            }

        }
    }

    private void sendMessage(Message message) throws IOException
    {
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
