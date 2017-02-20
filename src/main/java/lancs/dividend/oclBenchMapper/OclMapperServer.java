package lancs.dividend.oclBenchMapper;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;

import lancs.dividend.oclBenchMapper.message.cmd.CommandMessage;
import lancs.dividend.oclBenchMapper.message.cmd.CommandMessage.CmdType;
import lancs.dividend.oclBenchMapper.message.response.ResponseMessage;
import lancs.dividend.oclBenchMapper.message.response.TextResponseMessage;

/**
 * 
 * @author vseeker
 *
 * listens for client commands
 * executes commands blocking further client communication until execution results are present
 * returns results and waits for new commands
 *
 */
public class OclMapperServer {

	public static final String READY_MSG = "READY";
	
	private final int port;
	
	public OclMapperServer(int port) {
		this.port = port;
	}

	public void start() throws IOException {
        ServerSocket listener = new ServerSocket(port);
        System.out.println("Running as server.");
        try {
            while (true) {
            	System.out.println("Listening on port " + port);
            	
                Socket socket = listener.accept();
                System.out.println("Connection successful.");
                
                try {
            		ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
            		ObjectInputStream ois = new ObjectInputStream(socket.getInputStream());
	
	                // Send a welcome message to the client acknowledging the successful connection.
            		oos.writeObject(new TextResponseMessage(READY_MSG));
            		oos.flush();
	                
	                while (true) {
	                    CommandMessage cmd = (CommandMessage) ois.readObject();
	                    if (cmd == null || cmd.getType() == CmdType.EXIT) {
	                        break;
	                    }
	                    
	                    System.out.println("RECEIVED: " + cmd);
	                    ResponseMessage response = executeCmd(cmd);
	                    
	            		oos.writeObject(response);
	            		oos.flush();
	                    System.out.println("SENDING: " + response);
	                }
	                
	                oos.close();
	                ois.close();
	                
	            } catch (IOException e) {
	            	System.err.println("Error handling client: " + e);
	            } catch (ClassNotFoundException e) {
	            	System.err.println("Error handling client: " + e);
				} finally {
	                try {
	                    socket.close();
	                } catch (IOException e) {
	                	System.out.println("Couldn't close a socket, what's going on?");
	                }
	                System.out.println("Connection with client closed");
	            }
            }
        }
        finally {
            listener.close();
        }
	}

	private ResponseMessage executeCmd(CommandMessage cmd) {
		switch (cmd.getType()) {
			case RUNBENCH:
				return new TextResponseMessage("executed: " + cmd);
			default:
				System.err.println("Unhandled command type: " + cmd.getType());
				return new TextResponseMessage("Unable to execute command: " + cmd);
			}
	}
}
