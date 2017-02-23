package lancs.dividend.oclBenchMapper.mapping;

import lancs.dividend.oclBenchMapper.message.cmd.CommandMessage;
import lancs.dividend.oclBenchMapper.message.response.ResponseMessage;

/**
 * An execution item holds a command specifying 
 * the workload to be executed on a server and 
 * the corresponding server response.
 * 
 * @author vseeker
 *
 */
public class ExecutionItem {

	private final CommandMessage cmd;
	private ResponseMessage response;
	
	public ExecutionItem(CommandMessage cmd) {
		this.cmd = cmd;
	}
	
	public boolean resultsAvailable() { return response != null; }
	
	public ResponseMessage getResponse() { return response; }
	public CommandMessage getCommand() { return cmd; }
	
	public void setResponse(ResponseMessage res) {
		if(resultsAvailable())
			throw new RuntimeException("Results are already set for this execution.");
		response = res; 
	}
	
}
