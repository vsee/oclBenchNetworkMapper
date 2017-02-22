package lancs.dividend.oclBenchMapper.connection;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

import lancs.dividend.oclBenchMapper.message.response.ResponseMessage;


public class ServerConnection extends ConnectionHandler {

	public ServerConnection(int port, String serverAddr) throws IOException {
		super(port);
		
		connectionSocket = new Socket(serverAddr, port);
		
		oos = new ObjectOutputStream(connectionSocket.getOutputStream());
		ois = new ObjectInputStream(connectionSocket.getInputStream());
		connectionEstablished = true;
	}
	
	public ResponseMessage waitForCmdResponse() throws IOException {
		if(!isConnected()) 
			throw new RuntimeException("Established connection needed before messages can be received.");
		
		try {
			ResponseMessage res = (ResponseMessage) ois.readObject();

			if (res == null) throw new IOException("ERROR: Invalid null response from server.");
			else return res;
			
		} catch (ClassNotFoundException e) {
			throw new IOException("ERROR: Reading server response failed.", e);
		}
	}
}
