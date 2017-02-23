package lancs.dividend.oclBenchMapper.resultDisplay;

import java.util.Hashtable;

import lancs.dividend.oclBenchMapper.connection.ServerConnection;
import lancs.dividend.oclBenchMapper.mapping.ExecutionItem;
import lancs.dividend.oclBenchMapper.message.response.BenchStatsResponseMessage;
import lancs.dividend.oclBenchMapper.message.response.ErrorResponseMessage;
import lancs.dividend.oclBenchMapper.message.response.ResponseMessage;
import lancs.dividend.oclBenchMapper.message.response.TextResponseMessage;
import lancs.dividend.oclBenchMapper.userCmd.UserCommand;

/**
 * The simple console display lists for each participating server
 * which command was send and which answer was received.
 * 
 * @author vseeker
 *
 */
public class SimpleConsoleDisplay implements ResultDisplay {

	@Override
	public void display(Hashtable<ServerConnection, ExecutionItem> executionMap, UserCommand cmd) {
		if(executionMap == null || executionMap.size() == 0)
			throw new RuntimeException("Given execution map must not be null or empty.");
		if(cmd == null)
			throw new RuntimeException("Given user command must not be null.");
		
		System.out.println("###############################################");
		System.out.println("Original User Command:\n\t" + cmd);
		
		for (ServerConnection s : executionMap.keySet()) {
			ExecutionItem item = executionMap.get(s);

			System.out.println("\nExecution result of server " + s);
			System.out.println("Command:\n\t" + item.getCommand());
			System.out.println("Response:");
			
			if(!item.resultsAvailable())
				System.out.println("ERROR: No results received!");
			else {
				ResponseMessage response = item.getResponse();
				
				switch (response.getType()) {
				case TEXT:
					System.out.println("\t" + ((TextResponseMessage) response).getText());
					break;
				case BENCHSTATS:
					BenchStatsResponseMessage br = (BenchStatsResponseMessage) response;
					System.out.println("\tEnergy: " + br.getEnergy() + " - Runtime: " + br.getRuntime() + "ms");
					break;
				case ERROR:
					System.err.println("\tERROR: " + ((ErrorResponseMessage) response).getText());
					break;
				default:
					System.err.println("\tUnknown response type: " + response.getType());
					break;
				}
			}
		}
		
		System.out.println("###############################################");
	}

}
