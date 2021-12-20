package server;

import java.net.Socket;

public class ServerConfiguration
{

    public static final int BROADCAST_ID = 0;
    public static final int SERVER_ID = Integer.MAX_VALUE;

    public static final int responseTimer = 60000;

    public static final SocketWrapper serverSocketWrapper = new SocketWrapper(new Socket(), "Server", ServerConfiguration.SERVER_ID);

}
