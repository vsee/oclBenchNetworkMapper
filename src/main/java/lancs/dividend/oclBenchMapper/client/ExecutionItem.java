package lancs.dividend.oclBenchMapper.client;

import lancs.dividend.oclBenchMapper.message.response.ResponseMessage;
import lancs.dividend.oclBenchMapper.server.ExecutionDevice;
import lancs.dividend.oclBenchMapper.userCmd.UserCommand;

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

	private final UserCommand cmd;
	private final ExecutionDevice device;
	private final String serverAddress;
	
	private ResponseMessage response;
	private boolean error;
	private String errorMsg;
	private Exception errorExcep;
	
	public ExecutionItem(UserCommand cmd, ExecutionDevice dev, String server) {
		if(cmd == null) throw new IllegalArgumentException("Given command message must not be null.");
		if(server == null) throw new IllegalArgumentException("Given server address must not be null.");
		if(dev == null) throw new IllegalArgumentException("Given execution device must not be null.");
		
		this.cmd = cmd;
		device = dev;
		serverAddress = server;
	}
	
	public boolean hasError() { return error; }
	public String getErrorMsg() { return errorMsg; }
	public Exception getErrorException() { return errorExcep; }
	public void setError(String msg) { setError(msg, null); }
	public void setError(String msg, Exception e) {
		if(msg == null) throw new IllegalArgumentException("Given error message must not be null.");

		error = true;
		errorMsg = msg;
		errorExcep = e;
	}
	
	public UserCommand getCmd() { return cmd; }
	public ExecutionDevice getExecDevice() { return device; }
	public String getServer() { return serverAddress; }
	
	public ResponseMessage getResponse() { return response; }
	public void setResponse(ResponseMessage res) {
		if(res == null) throw new IllegalArgumentException("Given response message must not be null.");
		if(response != null) throw new RuntimeException("Results are already set for this execution.");
		response = res; 
	}
	
}
