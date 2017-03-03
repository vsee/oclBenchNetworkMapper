package lancs.dividend.oclBenchMapper;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import lancs.dividend.oclBenchMapper.client.OclMapperClient;
import lancs.dividend.oclBenchMapper.mapping.MapperFactory.MapperType;
import lancs.dividend.oclBenchMapper.server.OclMapperServer;
import lancs.dividend.oclBenchMapper.ui.UserInterfaceFactory.UserInterfaceType;
import net.sourceforge.argparse4j.ArgumentParsers;
import net.sourceforge.argparse4j.impl.Arguments;
import net.sourceforge.argparse4j.inf.ArgumentParser;
import net.sourceforge.argparse4j.inf.Namespace;
import net.sourceforge.argparse4j.inf.Subparser;
import net.sourceforge.argparse4j.inf.Subparsers;

public class Main {

	private enum ExecutionRole { CLIENT, SERVER };
	
	private static final int DEFAULT_PORT = 9090;
	private static final String DEFAULT_SERVER_ADDRESS = "127.0.0.1";
	private static final Path DEFAULT_RODINIA_HOME = Paths.get("../rodinia_3.1");
	
	private static Namespace parseArguments(String[] args) {
		
		ArgumentParser parser = ArgumentParsers.newArgumentParser("oclBenchMapper")
			     .description("Run server or client side for mapping opencl benchmarks "
			     		+ "to heterogeneous devices available in the network.");
		
	    Subparsers subparsers = parser.addSubparsers().help("Execution role of oclBenchMapper instance.");

	    Subparser clientParser = subparsers.addParser("client")
	    		.help("The client connects to a server address and distributes benchmark work.")
	    		.setDefault("role", ExecutionRole.CLIENT)
			    .defaultHelp(true);
	   
	    final List<String> defaultAddressList = new ArrayList<>();
	    defaultAddressList.add(DEFAULT_SERVER_ADDRESS + ":" + DEFAULT_PORT);
	    clientParser.addArgument("-a","--addressList")
		    .metavar("ADDR").type(String.class).nargs("+")
		    .help("Space separated list of server addresses available for "
		    		+ "benchmark execution. Format 'address:port'.")
	        .setDefault(defaultAddressList);
	    clientParser.addArgument("-m","--mapperType")
	    	.type(MapperType.class).help("Workload to server mapper used by the client.")
	    	.setDefault(MapperType.FCFS);
	    clientParser.addArgument("--gui").action(Arguments.storeTrue())
			.help("Run the client with a graphical user interface.");

	    
	    Subparser serverParser = subparsers.addParser("server")
	    		.help("The server receives benchmark workloads from clients, "
	    				+ "executes them and returns execution statistics.")
	    		.setDefault("role", ExecutionRole.SERVER)
   			    .defaultHelp(true);
	    serverParser.addArgument("-p","--port").type(Integer.class)
    		.help("Port used by the server to listen for clients.").setDefault(DEFAULT_PORT);
	    serverParser.addArgument("-r","--rodiniaHome")
	    	.type(Arguments.fileType().verifyCanRead().verifyIsDirectory())
			.help("Home directory of rodinia benchmark suite.").setDefault(DEFAULT_RODINIA_HOME);
	    serverParser.addArgument("--dummy").action(Arguments.storeTrue())
			.help("Run the server as dummy executing no workloads and returning dummy results.");
	    
	    return parser.parseArgsOrFail(args);
	}
	
	public static void main(String[] args) {
	    
		Namespace ns = parseArguments(args);

		ExecutionRole role = ns.get("role");
		switch(role) {
			case CLIENT:
				try {
					new OclMapperClient(ns.get("addressList"), ns.get("mapperType"),
							ns.getBoolean("gui") ? UserInterfaceType.GUI : UserInterfaceType.CONSOLE).runClient();
				} catch (IOException e) {
					throw new UncheckedIOException("ERROR: Connecting to server failed: ", e);
				}
				break;
			case SERVER:
				try {
					int port = ns.getInt("port");
					Path rhome = Paths.get(ns.getString("rodiniaHome"));
					new OclMapperServer(port, rhome, ns.getBoolean("dummy")).runServer();
				} catch (IOException e) {
					throw new UncheckedIOException("ERROR: Starting server failed: ", e);
				}
				break;
			default:
				throw new RuntimeException("Unhandled role: " + role);
		}
	}
}
