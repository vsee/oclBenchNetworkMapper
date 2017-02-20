package lancs.dividend.oclBenchMapper;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.Scanner;

import lancs.dividend.oclBenchMapper.message.cmd.CommandMessage;
import lancs.dividend.oclBenchMapper.message.cmd.CommandMessage.CmdType;
import lancs.dividend.oclBenchMapper.message.cmd.ExitCmdMessage;
import lancs.dividend.oclBenchMapper.message.cmd.RunBenchCmdMessage;
import lancs.dividend.oclBenchMapper.message.response.BenchStatsResponseMessage;
import lancs.dividend.oclBenchMapper.message.response.ResponseMessage;
import lancs.dividend.oclBenchMapper.message.response.ResponseMessage.ResponseType;
import lancs.dividend.oclBenchMapper.message.response.TextResponseMessage;

public class OclMapperClient {

	private int port;
	private String saddr;

	public OclMapperClient(int port, String serverAddr) {
		this.port = port;
		saddr = serverAddr;
	}

	public void start() throws IOException {
		System.out.println("Running as client.");
		Socket socket = new Socket(saddr, port);
        
		ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
		ObjectInputStream ois = new ObjectInputStream(socket.getInputStream());
		
        Scanner cmdIn = new Scanner(System.in);
        
        boolean serverReady = false;
        boolean waitingForResponse = false;

        while (true) {
        	if(!serverReady || waitingForResponse) {
        		ResponseMessage response;
                try {
                	response = (ResponseMessage) ois.readObject();
                } catch (IOException ioEx) {
                       System.err.println("ERROR: " + ioEx);
                       continue;
                } catch (ClassNotFoundException cEx) {
                    	System.err.println("ERROR: " + cEx);
                    	continue;
				}
                
                if(!serverReady) {
		        	if(response.getType() == ResponseType.TEXT && 
		        	   ((TextResponseMessage) response).getText().equals(OclMapperServer.READY_MSG)) {
		        		
		        		serverReady = true;
		                System.out.println("Server ready to receive.");
		                System.out.println("Enter benchmark name and arguments or 'exit' to end the connection.");
		                System.out.println("Example: kmeans,args");
		        	}
                } else {
                	processResponse(response);
                	waitingForResponse = false;
                }
        	} else {
        		CommandMessage cmd = parseCmd(cmdIn.nextLine());

        		System.out.println("SENDING: " + cmd);
                oos.writeObject(cmd);
                oos.flush();
                
        		if(cmd.getType() == CmdType.EXIT) break;
                
                waitingForResponse = true;
        	}
        }

        cmdIn.close();
        ois.close();
        oos.close();
		socket.close();
		System.exit(0);
	}

	private CommandMessage parseCmd(String nextLine) {

		if(nextLine.equals("exit")) return new ExitCmdMessage();
		else return new RunBenchCmdMessage(nextLine,"testargs");
	}

	private void processResponse(ResponseMessage response) {

		switch (response.getType()) {
		case TEXT:
			System.out.println(((TextResponseMessage) response).getText());
			break;
		case BENCHSTATS:
			BenchStatsResponseMessage br = (BenchStatsResponseMessage) response;
			System.out.println(br.getEnergy() + " - " + br.getRuntime() + "ms");
			break;

		default:
			System.err.println("Unknown response type: " + response.getType());
			break;
		}
	}

}
