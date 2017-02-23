package lancs.dividend.oclBenchMapper.connection;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

import lancs.dividend.oclBenchMapper.message.Message;

public abstract class ConnectionHandler {

	protected Socket connectionSocket;
	protected ObjectOutputStream oos;
	protected ObjectInputStream ois;
	
	protected boolean connectionEstablished;
	
	public boolean isConnected() {
		return connectionEstablished;
	}
	
	public void closeConnection() {
		if(isConnected()) {
            try {
            	connectionSocket.close();
            } catch (IOException e) {
            	System.err.println("ERROR: Closing socket failed " + e);
            	e.printStackTrace();
            } finally {
            	connectionEstablished = false;
            }
		}
	}
	
	public void sendMessage(Message msg) throws IOException {
		if(!isConnected()) 
			throw new RuntimeException("Established connection needed before message can be send.");
		if(msg == null)
			throw new IllegalArgumentException("Given messge must not be null.");

		oos.writeObject(msg);
		oos.flush();
	}
}
