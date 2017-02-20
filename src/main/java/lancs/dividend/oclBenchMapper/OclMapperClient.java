package lancs.dividend.oclBenchMapper;

import java.io.IOException;
import java.util.Scanner;

import lancs.dividend.oclBenchMapper.connection.ConnectionClient;
import lancs.dividend.oclBenchMapper.message.cmd.CommandMessage;
import lancs.dividend.oclBenchMapper.message.cmd.CommandMessage.CmdType;
import lancs.dividend.oclBenchMapper.message.cmd.ExitCmdMessage;
import lancs.dividend.oclBenchMapper.message.cmd.RunBenchCmdMessage;
import lancs.dividend.oclBenchMapper.message.response.BenchStatsResponseMessage;
import lancs.dividend.oclBenchMapper.message.response.ErrorResponseMessage;
import lancs.dividend.oclBenchMapper.message.response.ResponseMessage;
import lancs.dividend.oclBenchMapper.message.response.TextResponseMessage;

public class OclMapperClient {
	
	public enum RodiniaBins { KMEANS }

	private final ConnectionClient client;
	
	public OclMapperClient(int port, String serverAddr) throws IOException {
		client = new ConnectionClient(port, serverAddr);
	}
	
	public void runClient() {
		if(!client.isConnected()) {
			System.err.println("ERROR: Could not establish connection with server.");
			return;
		}
		
		handleMessages();
	}

	private void handleMessages() {

		boolean waitingForResponse = false;
        Scanner cmdIn = new Scanner(System.in);

		while(true) {
        	if(waitingForResponse) {
        		ResponseMessage response;
                try {
                	response = client.waitForCmdResponse();
                } catch (IOException e) {
                	System.err.println(e.getMessage());
                	e.printStackTrace();
                	break;
                }
                
            	processResponse(response);
            	waitingForResponse = false;
        	} else {
        		CommandMessage cmd = parseCmd(cmdIn.nextLine());

        		try {
					client.sendMessage(cmd);
				} catch (IOException e) {
					System.err.println("ERROR: sending command failed: " + e);
					e.printStackTrace();
					break;
				}
                
        		if(cmd.getType() == CmdType.EXIT) break;
                
                waitingForResponse = true;
        	}
		}
		
		cmdIn.close();
		client.closeConnection();
	}

	private CommandMessage parseCmd(String nextLine) {
		if(nextLine.equals("exit")) return new ExitCmdMessage();
		else return new RunBenchCmdMessage(RodiniaBins.KMEANS,"");
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
		case ERROR:
			System.err.println("ERROR: " + ((ErrorResponseMessage) response).getText());
			break;
		default:
			System.err.println("Unknown response type: " + response.getType());
			break;
		}
	}

}
