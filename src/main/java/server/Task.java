package server;

import general.Response;

import java.io.IOException;

public abstract class Task
{
    private String requestPhrase;
    private Object[] args;

    public Task(String requestPhrase, Object... args)
    {
        this.requestPhrase = requestPhrase;
        this.args = args;
    }

    public abstract Response doTask(SocketHandler client, String input) throws IOException;

    public String getRequestPhrase()
    {
        return requestPhrase;
    }

    public Object[] getArgs()
    {
        return args;
    }

    public Object getArg(int index)
    {
        if(index < 0 || index >= args.length)
            return null;
        return args[index];
    }
}
