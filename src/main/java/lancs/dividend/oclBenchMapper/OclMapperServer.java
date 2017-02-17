package lancs.dividend.oclBenchMapper;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;

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
	                // Decorate the streams so we can send characters
	                // and not just bytes.  Ensure output is flushed
	                // after every newline.
	                BufferedReader in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
	                PrintWriter out = new PrintWriter(socket.getOutputStream(), true);
	
	                // Send a welcome message to the client acknowledging the successful connection.
	                out.println("READY");
	                out.flush();
	                
	                // Get messages from the client, line by line; return them
	                // capitalized
	                while (true) {
	                    String input = in.readLine();
	                    if (input == null || input.equals(".")) {
	                        break;
	                    }
	                    System.out.println("RECEIVED: " + input);
	                    String response = input.toUpperCase();
	                    out.println(response);
		                out.flush();
	                    System.out.println("SENDING: " + response);
	                }
	                
	            } catch (IOException e) {
	            	System.out.println("Error handling client: " + e);
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
}
