package general;

public enum Command
{
    Unknown("UNKNOWN"), SendFile("SEND_FILE"), ChangeInterlocutor("CHANGE_INTERLOCUTOR"),
    Exit("EXIT");

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
