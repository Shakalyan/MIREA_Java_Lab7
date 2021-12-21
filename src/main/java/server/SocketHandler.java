package server;

import general.Message;
import general.MessageBuilder;
import general.Response;

import java.io.*;
import java.util.ArrayDeque;
import java.util.ArrayList;

import static java.lang.Thread.interrupted;

public class SocketHandler
{

    private SocketWrapper socketWrapper;

    private ArrayDeque<Message> messagesPool;
    private ArrayDeque<Message> localMessagesPool;

    private ArrayDeque<Task> tasks;

    private Thread socketListener;
    private Thread socketWriter;


    public SocketHandler(SocketWrapper socketWrapper, ArrayDeque<Message> messagesPool)
    {
        this.socketWrapper = socketWrapper;
        this.messagesPool = messagesPool;

        localMessagesPool = new ArrayDeque<>();
        tasks = new ArrayDeque<>();

        socketListener = new Thread(new SocketListener());
        socketWriter = new Thread(new SocketWriter());
    }



    public SocketWrapper getSocketWrapper()
    {
        return socketWrapper;
    }

    public void addMessage(Message message)
    {
        localMessagesPool.addLast(message);
    }

    public void addTask(Task task)
    {
        synchronized(tasks)
        {
            tasks.addLast(task);
        }
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
                    try
                    {
                        Thread.sleep(readingDelay);
                    }
                    catch(InterruptedException e)
                    {
                        System.out.println(e.getMessage());
                    }

                    synchronized(tasks)
                    {
                        if(!tasks.isEmpty())
                        {
                            doTask();
                            continue;
                        }
                    }

                    Message message = socketWrapper.getMessage();
                    if(message == null)
                        continue;



                    synchronized(messagesPool)
                    {
                        messagesPool.addLast(message);
                    }

                }
            }
            catch(IOException e)
            {
                System.out.println(e.getMessage());
            }

        }

        private void doTask() throws IOException
        {
            synchronized(messagesPool)
            {
                localMessagesPool.addFirst(MessageBuilder.build(tasks.getFirst().getRequestPhrase(), ServerConfiguration.serverSocketWrapper, socketWrapper.getId()));
            }

            Message message = socketWrapper.getMessage();;
            if(message == null)
                return;

            Response response = tasks.getFirst().doTask(SocketHandler.this, message.getText());
            localMessagesPool.addFirst(MessageBuilder.build(response.getMessage(), ServerConfiguration.serverSocketWrapper, socketWrapper.getId()));
            if(response.isDone())
                tasks.pollFirst();
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
                        ArrayList<Message> serverMessages = new ArrayList<>(localMessagesPool.stream().filter(m -> m.getSender().getId() == ServerConfiguration.SERVER_ID).toList());
                        for(var message : serverMessages)
                        {
                            socketWrapper.sendMessage(message);
                        }
                        localMessagesPool.removeAll(serverMessages);

                        while(!localMessagesPool.isEmpty() && tasks.isEmpty())
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
