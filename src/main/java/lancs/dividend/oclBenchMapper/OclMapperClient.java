package lancs.dividend.oclBenchMapper;

import java.io.IOException;
import java.util.Scanner;
import java.util.StringJoiner;

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
	
	public enum RodiniaBin { KMEANS, LUD }

	private final ConnectionClient client;
	
	private final String EXIT_CMD = "exit";
	private final String BENCHMARK_LIST;
	private final String BENCHMARK_TOP_MENU;
	
	public OclMapperClient(int port, String serverAddr) throws IOException {
		client = new ConnectionClient(port, serverAddr);
		System.out.println("Connecting client with " + serverAddr + ":" + port + " ...");
		
		StringJoiner join = new StringJoiner(",","{","}");
		for (RodiniaBin b : RodiniaBin.values()) join.add(b.name());
		BENCHMARK_LIST = join.toString();
		
		BENCHMARK_TOP_MENU = 
		"\n1. Enter Benchmark name from selection: " + BENCHMARK_LIST + "\n" +
		"2. Enter '" + EXIT_CMD + "' to shut down client.\n>> ";
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
        		CommandMessage cmd = parseCmd(cmdIn);

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

	private CommandMessage parseCmd(Scanner cmdIn) {
		
		while(true) {
			System.out.print(BENCHMARK_TOP_MENU);
			String line = cmdIn.nextLine();
			
			RodiniaBin rbin = null;
			if(line.trim().equals(EXIT_CMD)) {
				return new ExitCmdMessage();
			}
			else if((rbin = isBenchmarkBin(line.trim())) != null) {
				System.out.println("Benchmark: " + rbin + " selected. Running on server ...");
				return new RunBenchCmdMessage(rbin, "");
			} else {
				System.err.println("ERROR: Invalid input.");
			}
		}
	}

	private RodiniaBin isBenchmarkBin(String rodiniaBin) {
		try {
			return RodiniaBin.valueOf(rodiniaBin);
		} catch(IllegalArgumentException e) {
			return null;
		}
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
