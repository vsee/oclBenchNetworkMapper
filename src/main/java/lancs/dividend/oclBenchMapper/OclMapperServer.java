package lancs.dividend.oclBenchMapper;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Date;

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
                System.out.println("Connection succesfull. Sending Data.");
                try {
                    PrintWriter out =
                        new PrintWriter(socket.getOutputStream(), true);
                    out.println(new Date().toString());
                } finally {
                    socket.close();
                }
            }
        }
        finally {
            listener.close();
        }		
	}

}
