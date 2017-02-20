package lancs.dividend.oclBenchMapper.connection;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;

import lancs.dividend.oclBenchMapper.message.cmd.CommandMessage;
import lancs.dividend.oclBenchMapper.message.cmd.ExitCmdMessage;

public class ConnectionServer extends ConnectionHandler {

	private ServerSocket listener;
	
	public ConnectionServer(int port) throws IOException {
		super(port);
	
        listener = new ServerSocket(port);
	}
	
	public boolean establishConnection() {
		try {
			connectionSocket = listener.accept();
			
			oos = new ObjectOutputStream(connectionSocket.getOutputStream());
			ois = new ObjectInputStream(connectionSocket.getInputStream());
			
			return connectionEstablished = true;
			
		} catch (IOException e) {
			System.err.println("ERROR: establishing connection failed: " + e);
            try {
            	if(connectionSocket != null) connectionSocket.close();
            } catch (IOException ioSocketClose) {
            	System.err.println("ERROR: Closing socket failed " + ioSocketClose);
            }
		}
		
		return connectionEstablished = false;
	}
	
	public CommandMessage waitForCmd() {
		if(!isConnected()) 
			throw new RuntimeException("Established connection needed before messages can be received.");
		
		try {
			CommandMessage cmd = (CommandMessage) ois.readObject();
			if (cmd != null) return cmd;
			
		} catch (ClassNotFoundException | IOException e) {
			System.err.println("ERROR: Reading client message failed: " + e);
			e.printStackTrace();
		}
		
		return new ExitCmdMessage();
	}

	public void shutDown() throws IOException {
		listener.close();
	}

}
