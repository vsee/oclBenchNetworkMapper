package lancs.dividend.oclBenchMapper.mapping;

import lancs.dividend.oclBenchMapper.message.CommandMessage;
import lancs.dividend.oclBenchMapper.message.response.ResponseMessage;

/**
 * An execution item holds a command specifying 
 * the workload to be executed on a server and 
 * the corresponding server response.
 * 
 * In case of communication or execution error, an error
 * flag is set and an error message available.
 * 
 * @author vseeker
 *
 */
public class ExecutionItem {

	private final CommandMessage cmd;
	private final String serverAddress;
	
	private ResponseMessage response;
	private boolean error;
	private String errorMsg;
	
	public ExecutionItem(CommandMessage cmd, String server) {
		if(cmd == null) throw new IllegalArgumentException("Given command message must not be null.");
		if(server == null) throw new IllegalArgumentException("Given server address must not be null.");
		
		this.cmd = cmd;
		serverAddress = server;
	}
	
	public boolean hasError() { return error; }
	public String getErrorMsg() { return errorMsg; }
	public void setError(String msg) {
		if(msg == null) throw new IllegalArgumentException("Given error message must not be null.");

		error = true;
		errorMsg = msg;
	}
	
	public CommandMessage getCmdMsg() { return cmd; }
	public String getServer() { return serverAddress; }
	
	public ResponseMessage getResponse() { return response; }
	public void setResponse(ResponseMessage res) {
		if(res == null) throw new IllegalArgumentException("Given response message must not be null.");
		if(response != null) throw new RuntimeException("Results are already set for this execution.");
		response = res; 
	}
	
}
