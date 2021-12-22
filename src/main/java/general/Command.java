package general;

public enum Command
{
    Unknown("Unknown"), SendFile("SendFile"), ChangeInterlocutor("ChangeInterlocutor"),
    Exit("Exit"), ChangeName("ChangeName");

    private String command;
    Command(String command)
    {
        this.command = command;
    }

    public String getCommandText()
    {
        return this.command;
    }

}
