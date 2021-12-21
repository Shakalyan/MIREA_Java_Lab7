package general;

public class Response
{

    private boolean done;
    private String message;

    public Response(boolean done, String message)
    {
        this.done = done;
        this.message = message;
    }

    public boolean isDone()
    {
        return done;
    }

    public String getMessage()
    {
        return message;
    }

}
