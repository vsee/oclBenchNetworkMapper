package lancs.dividend.oclBenchMapper;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.Scanner;

public class OclMapperClient {

	private int port;

	public OclMapperClient(int port) {
		this.port = port;
	}

	public void start() throws IOException {
		System.out.println("Running as client.");
		String serverAddress = "127.0.0.1";
		Socket s = new Socket(serverAddress, port);
        
        BufferedReader socketIn = new BufferedReader(new InputStreamReader(s.getInputStream()));
        PrintWriter socketOut = new PrintWriter(s.getOutputStream(), true);

        Scanner cmdIn = new Scanner(System.in);
        
        boolean serverReady = false;
        boolean waitingForResponse = false;

        while (true) {
        	if(!serverReady) {    	
	        	// wait for server to be ready
	        	String msg = socketIn.readLine();
	        	if(msg.equals("READY")) {
	        		serverReady = true;
	                System.out.println("Server ready to receive.");
	        	}
        	} else if(waitingForResponse) {
                String response;
                try {
                    response = socketIn.readLine();
                } catch (IOException ex) {
                       response = "Error: " + ex;
                }
                System.out.println("RECEIVED: " + response);
                waitingForResponse = false;
        	} else {
        		String command = cmdIn.nextLine();
        		
        		if(command.equals("exit")) break;
        		
        		System.out.println("SENDING: " + command);
                socketOut.println(command);
                waitingForResponse = true;
        	}
        }

        cmdIn.close();
        socketIn.close();
        socketOut.close();
		s.close();
		System.exit(0);
	}

}
