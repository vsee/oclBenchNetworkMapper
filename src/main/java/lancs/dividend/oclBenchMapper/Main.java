package lancs.dividend.oclBenchMapper;

import java.io.IOException;
import java.io.UncheckedIOException;

public class Main {

	private enum ExecutionRole { CLIENT, SERVER };
	
	private static final String USAGE = "Usage: Supply either '"
			+ ExecutionRole.CLIENT + "' or '" + ExecutionRole.SERVER
			+ "' as argument to specify role.";
	
	private static final int DEFAULT_PORT = 9090;
	private static final String DEFAULT_SERVER_ADDRESS = "127.0.0.1";
	
	// TODO use argparse4j as argument parser
	// TODO constructor null checks for messages
	// TODO clean up log and exception messages
	public static void main(String[] args) {

		if(args.length < 1) throw new RuntimeException(USAGE);
		
		ExecutionRole role;
		try {
			role = ExecutionRole.valueOf(args[0]);
		} catch (IllegalArgumentException e) {
			throw new RuntimeException(USAGE);
		}

		switch(role) {
			case CLIENT:
				try {
					new OclMapperClient(DEFAULT_PORT, DEFAULT_SERVER_ADDRESS).runClient();
				} catch (IOException e) {
					throw new UncheckedIOException("ERROR: Connecting to server failed: ", e);
				}
				break;
			case SERVER:
				try {
					new OclMapperServer(DEFAULT_PORT).runServer();
				} catch (IOException e) {
					throw new UncheckedIOException("ERROR: Starting server failed: ", e);
				}
				break;
			default:
				throw new RuntimeException("Unhandled role: " + role);
		}
	}

}
