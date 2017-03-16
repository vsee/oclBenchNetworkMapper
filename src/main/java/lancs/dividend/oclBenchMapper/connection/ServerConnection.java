package lancs.dividend.oclBenchMapper.connection;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

import lancs.dividend.oclBenchMapper.message.response.ArchResponseMessage;
import lancs.dividend.oclBenchMapper.message.response.ResponseMessage;
import lancs.dividend.oclBenchMapper.message.response.ResponseMessage.ResponseType;
import lancs.dividend.oclBenchMapper.server.ServerDescription;


public class ServerConnection extends ConnectionHandler {

	private final ServerDescription serverDescr;
	
	public ServerConnection(String serverAddr) throws IOException {
		if(serverAddr == null)
			throw new IllegalArgumentException("Given server address must not be null.");
		
		String[] addrParts = serverAddr.split(":");
		if(addrParts.length != 2)  throw new IllegalArgumentException("Given address invalid: " + serverAddr);
		
		int port;
		try {
			port = Integer.parseInt(addrParts[1]);
		} catch (NumberFormatException e) {
			throw new IllegalArgumentException("Given address port invalid: " + serverAddr);
		}
		
		connectionSocket = new Socket(addrParts[0], port);
		
		oos = new ObjectOutputStream(connectionSocket.getOutputStream());
		ois = new ObjectInputStream(connectionSocket.getInputStream());
		connectionEstablished = true;

		serverDescr = new ServerDescription(serverAddr, receiveDescription());
	}
	
	private String receiveDescription() throws IOException {
		ResponseMessage resp = waitForResponse();
		if(resp.getType() != ResponseType.ARCH)
			throw new RuntimeException("Server connection failed. Missing architecture description message.");
		
		return ((ArchResponseMessage) resp).getArchDescription();
	}
	
	public ServerDescription getServerDescription() {
		return serverDescr;
	}

	@Override
	public String toString() {
		return serverDescr.address + " " + serverDescr.architecture;
	}
	
	public ResponseMessage waitForResponse() throws IOException {
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
