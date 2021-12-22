package general;

import server.SocketWrapper;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.Scanner;

public class MessageBuilder
{

    // Correct input examples:
    // Hello!
    // Hello!
    // /send_file SomeFile.txt

    // Correct message examples:
    // <6:TEXT:> Hello!
    // <20:FILE:SomeFile.txt> file data...
    // <40:COMMAND:CHANGE_INTERLOCUTOR> 4

    public static void main(String[] args)
    {
        Scanner scanner = new Scanner(System.in);

        while(true)
        {


        }

    }

    public static String readMessage(String caller, BufferedReader reader) throws IOException
    {
        StringBuilder message = new StringBuilder("<");


        int b;
        while(((b = reader.read()) != -1) && ((char)b != '<'));

        int length;
        String lenStr = "";
        char c;
        while((c = (char)reader.read()) != ':')
            lenStr += c;
        length = Integer.parseInt(lenStr);
        message.append(lenStr + ':');

        int closeBracketCount = 2;
        while(closeBracketCount != 0)
        {
            c = (char)reader.read();
            message.append(c);
            if(c == '>')
                --closeBracketCount;
        }


        for(int i = 0; i < length + 1; ++i)
            message.append((char)reader.read());


        return message.toString();
    }

    public static String convertToString(Message builtMessage)
    {
        if(builtMessage == null)
            return null;

        String senderName = "null";
        String senderId = "null";
        if(builtMessage.getSender() != null)
        {
            senderName = builtMessage.getSender().getName();
            senderId = Integer.toString(builtMessage.getSender().getId());
        }

        String message = String.format("<%d:%s:%s> <%s#%s> %s"  , builtMessage.getLength()
                                                                , builtMessage.getType()
                                                                , builtMessage.getExtraInfo()
                                                                , senderName
                                                                , senderId
                                                                , builtMessage.getText());
        return message;
    }

    public static String convertToString(Message builtMessage, String senderInfo)
    {
        if(builtMessage == null)
            return null;

        String message = String.format("<%d:%s:%s> %s %s"  , builtMessage.getLength()
                , builtMessage.getType()
                , builtMessage.getExtraInfo()
                , senderInfo
                , builtMessage.getText());
        return message;
    }

    public static Message convertToMessage(String builtString)
    {
        if(builtString == null)
            return null;

        int firstColon = builtString.indexOf(":");
        int secondColon = builtString.indexOf(":", firstColon + 1);
        int firstCloseBracket = builtString.indexOf(">");
        int secondOpenBracket = builtString.indexOf("<", firstCloseBracket + 1);
        int secondCloseBracket = builtString.indexOf(">", secondOpenBracket);

        int length = Integer.parseInt(builtString.substring(builtString.indexOf("<") + 1, firstColon));
        Message.Type type = Message.Type.valueOf(builtString.substring(firstColon + 1, secondColon));
        String extraInfo = builtString.substring(secondColon + 1, firstCloseBracket);
        String text = builtString.substring(secondCloseBracket + 2);

        return new Message(type, extraInfo, text, null, 0);
    }

    public static Message build(String input, SocketWrapper sender, int receiverId)
    {
        int length;
        Message.Type type;
        String extraInfo;
        String text;

        if(input == null || input.isBlank())
            return null;

        Command command = getCommand(input);
        if(command == Command.Unknown)
            return null;

        if(command != null)
        {
            int textIndex = input.indexOf(" ");
            if(textIndex == -1 || textIndex + 1 == input.length())
                text = "";
            else
                text = input.substring(textIndex + 1);
            type = Message.Type.Command;
            extraInfo = command.getCommandText();
        }
        else
        {
            type = Message.Type.Text;
            extraInfo = "";
            text = input;
        }

        length = text.length();

        Message message = new Message(type, extraInfo, text, sender, receiverId);
        //System.out.println(message);

        return message;
    }

    public static Message build(String input)
    {
        return build(input, null, -1);
    }

    private static Command getCommand(String input)
    {
        if(input.charAt(0) != '/')
            return null;

        int endIndex = input.indexOf(" ");
        if(endIndex == -1)
            endIndex = input.length();

        String command = input.substring(0, endIndex);
        switch(command)
        {
            case "/send_file":
                return Command.SendFile;
            case "/change_interlocutor":
                return Command.ChangeInterlocutor;
            case "/change_name":
                return Command.ChangeName;
            case "/send_object":
                return Command.SendObject;
            case "/print_objects_list":
                return Command.PrintObjectsList;
            case "/print_object":
                return Command.PrintObject;
            case "/add_object":
                return Command.AddObject;
            case "/delete_object":
                return Command.DeleteObject;
            case "/exit":
                return Command.Exit;
            default:
                return Command.Unknown;
        }
    }

    private static String getFileName(String input)
    {
        int firstIndex = input.indexOf(" ");
        int lastIndex = input.indexOf(" ", firstIndex);
        if(firstIndex == -1 || lastIndex == -1 || firstIndex == lastIndex)
            return null;
        return input.substring(firstIndex, lastIndex);
    }

}
