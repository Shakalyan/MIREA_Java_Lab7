package server;

import entities.Response;
import server.SocketWrapper;

import java.io.IOException;

public abstract class Task
{
    private String requestPhrase;

    public Task(String requestPhrase)
    {
        this.requestPhrase = requestPhrase;
    }

    public abstract Response doTask(SocketWrapper client, String input) throws IOException;

    public String getRequestPhrase()
    {
        return requestPhrase;
    }
}
