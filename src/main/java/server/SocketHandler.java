package server;

import entities.Message;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayDeque;

import static java.lang.Thread.getAllStackTraces;
import static java.lang.Thread.interrupted;

public class SocketHandler
{

    private SocketWrapper socketWrapper;

    private ArrayDeque<Message> messagesPool;
    private ArrayDeque<Message> localMessagesPool;

    private Thread socketListener;
    private Thread socketWriter;


    public SocketHandler(SocketWrapper socketWrapper, ArrayDeque<Message> messagesPool)
    {
        this.socketWrapper = socketWrapper;
        this.messagesPool = messagesPool;
        localMessagesPool = new ArrayDeque<>();

        socketListener = new Thread(new SocketListener());
        socketWriter = new Thread(new SocketWriter());
    }

    public void addMessage(Message message)
    {
        localMessagesPool.addLast(message);
    }

    public SocketWrapper getSocketWrapper()
    {
        return socketWrapper;
    }

    public void start()
    {
        socketListener.start();
        socketWriter.start();
    }

    public void terminate()
    {
        socketListener.interrupt();
        socketWriter.interrupt();
    }

    public void join() throws InterruptedException
    {
        socketListener.join();
        socketWriter.join();
    }


    private class SocketListener implements Runnable
    {
        private static final int readingDelay = 100;

        @Override
        public void run()
        {
            try
            {
                while(!interrupted())
                {
                    Message message = null;
                    while(message == null)
                        message = socketWrapper.getMessage();

                    synchronized(messagesPool)
                    {
                        messagesPool.addLast(message);
                    }

                    try
                    {
                        Thread.sleep(readingDelay);
                    }
                    catch(InterruptedException e)
                    {
                        System.out.println(e.getMessage());
                    }

                }
            }
            catch(IOException e)
            {
                System.out.println(e.getMessage());
            }

        }

    }

    private class SocketWriter implements Runnable
    {
        private static final int writingDelay = 100;

        @Override
        public void run()
        {
            try
            {
                while(!interrupted())
                {
                    synchronized(localMessagesPool)
                    {
                        while(!localMessagesPool.isEmpty())
                        {
                            socketWrapper.sendMessage(localMessagesPool.pollFirst());

                        }
                    }

                    try
                    {
                        Thread.sleep(writingDelay);
                    }
                    catch(InterruptedException e)
                    {
                        System.out.println(e.getMessage());
                    }

                }
            }
            catch(IOException e)
            {
                System.out.println(e.getMessage());
            }
        }

    }



}
