package lancs.dividend.oclBenchMapper;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Hashtable;
import java.util.List;
import java.util.TreeMap;

import lancs.dividend.oclBenchMapper.benchmark.Benchmark;
import lancs.dividend.oclBenchMapper.benchmark.BenchmarkData;
import lancs.dividend.oclBenchMapper.client.OclMapperClient;
import lancs.dividend.oclBenchMapper.mapping.MapperFactory;
import lancs.dividend.oclBenchMapper.mapping.MapperFactory.MapperType;
import lancs.dividend.oclBenchMapper.mapping.PredictiveMapperConfig;
import lancs.dividend.oclBenchMapper.mapping.WorkloadMapper;
import lancs.dividend.oclBenchMapper.server.OclMapperServer;
import lancs.dividend.oclBenchMapper.ui.UserInterface;
import lancs.dividend.oclBenchMapper.ui.UserInterfaceFactory;
import lancs.dividend.oclBenchMapper.ui.UserInterfaceFactory.UserInterfaceType;
import lancs.dividend.oclBenchMapper.ui.console.NiConsoleConfig;
import lancs.dividend.oclBenchMapper.ui.gui.ClientGuiConfig;
import lancs.dividend.oclBenchMapper.utils.OclBenchMapperCsvHandler;
import net.sourceforge.argparse4j.ArgumentParsers;
import net.sourceforge.argparse4j.impl.Arguments;
import net.sourceforge.argparse4j.inf.ArgumentParser;
import net.sourceforge.argparse4j.inf.Namespace;
import net.sourceforge.argparse4j.inf.Subparser;
import net.sourceforge.argparse4j.inf.Subparsers;

public class Main {

	private enum ExecutionRole { CLIENT, SERVER, NICLIENT };
	
	private static final int DEFAULT_PORT = 9090;
	private static final String DEFAULT_SERVER_ADDRESS = "127.0.0.1";
	private static final Path DEFAULT_NICLIENT_OUTPUT = Paths.get("./");
	private static final Path DEFAULT_PREDICTION_FILE = Paths.get("./src/main/resources/serverConf/dividend_device_predictions.csv");
	private static final Path DEFAULT_EXECUTION_STATS_FILE = Paths.get("./src/main/resources/serverConf/dividend_device_executionStats.csv");
	private static final Path DEFAULT_BENCH_EXEC_CONFIG = Paths.get("./src/main/resources/benchmarkConf/dividend_rodinia_bench_config.csv");
	private static final Path DEFAULT_BENCH_ARGS_CONFIG = Paths.get("./src/main/resources/benchmarkConf/dividend_rodinia_data_config.csv");
	private static final Path DEFAULT_BENCH_CONFIG = Paths.get("./src/main/resources/benchmarkConf/dividend_benchmark_config.csv");
	
	private static Namespace parseArguments(String[] args) {
		
		ArgumentParser parser = ArgumentParsers.newArgumentParser("oclBenchMapper")
			     .description("Run server or client side for mapping opencl benchmarks "
			     		+ "to heterogeneous devices available in the network.").defaultHelp(true);
		parser.addArgument("--benchmarkConfig")
	    	.type(Arguments.fileType().verifyCanRead().verifyIsFile())
			.help("Configuration file for available benchmarks.").setDefault(DEFAULT_BENCH_CONFIG);
		parser.addArgument("--benchmarkDataConfig")
	    	.type(Arguments.fileType().verifyCanRead().verifyIsFile())
			.help("Configuration file for the benchmark's available data sets.").setDefault(DEFAULT_BENCH_ARGS_CONFIG);
		
	    Subparsers subparsers = parser.addSubparsers().help("Execution role of oclBenchMapper instance.");

	    addClientArgs(subparsers);
	    addServerArgs(subparsers);
	    addNonInteractiveClientArgs(subparsers);   
	    
	    return parser.parseArgsOrFail(args);
	}
	
	private static void addNonInteractiveClientArgs(Subparsers subparsers) {
		Subparser niClientParser = subparsers.addParser("non-interactive-client")
	    		.help("The non-interactive client connects to a single server, "
	    				+ "executes a given file of benchmark commands and saves the results.")
	    		.setDefault("role", ExecutionRole.NICLIENT)
			    .defaultHelp(true);
	   
	    final List<String> defaultAddressList = new ArrayList<>();
	    defaultAddressList.add(DEFAULT_SERVER_ADDRESS + ":" + DEFAULT_PORT);
	    niClientParser.addArgument("-a","--address")
		    .metavar("ADDR").type(String.class)
		    .help("Server addresses for benchmark execution. Format 'address:port'.")
	        .setDefault(defaultAddressList);
	    niClientParser.addArgument("-i","--cmdInput")
	    	.type(Arguments.fileType().verifyCanRead().verifyIsFile())
			.help("Csv file containing benchmark execution commands.").required(true);
	    niClientParser.addArgument("-o","--outputDir")
	    	.type(Arguments.fileType().verifyCanWrite().verifyIsDirectory())
			.help("Output directory for statistic results.").setDefault(DEFAULT_NICLIENT_OUTPUT);
	    niClientParser.addArgument("--fullStats").action(Arguments.storeTrue())
			.help("Save a full list of execution stats.");
	}
	
	private static void addServerArgs(Subparsers subparsers) {
		Subparser serverParser = subparsers.addParser("server")
	    		.help("The server receives benchmark workloads from clients, "
	    				+ "executes them and returns execution statistics.")
	    		.setDefault("role", ExecutionRole.SERVER)
   			    .defaultHelp(true);
	    serverParser.addArgument("-p","--port").type(Integer.class)
    		.help("Port used by the server to listen for clients.").setDefault(DEFAULT_PORT);
	    serverParser.addArgument("-A","--architecture")
		    .metavar("ARCH").type(String.class)
		    .help("Identifier describing the servers architecture.").required(true);
	    
	    serverParser.addArgument("--benchExecConfig")
	    	.type(Arguments.fileType().verifyCanRead().verifyIsFile())
			.help("Configuration file for benchmark execution arguments.").setDefault(DEFAULT_BENCH_EXEC_CONFIG);
	    serverParser.addArgument("--simulation")
	    	.type(Arguments.fileType().verifyCanRead().verifyIsFile())
			.help("Run a server simulation based on results from hardware measurements.");
	}

	private static void addClientArgs(Subparsers subparsers) {
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
	    clientParser.addArgument("--offlinePredictions")
	    	.type(Arguments.fileType().verifyCanRead().verifyIsFile())
			.help("Csv file containing ahead of time execution device predictions for specific architecture.")
			.setDefault(DEFAULT_PREDICTION_FILE);
	    clientParser.addArgument("--avgExecutionStats")
	    	.type(Arguments.fileType().verifyCanRead().verifyIsFile())
			.help("Csv file containing ahead of time measurements of benchmark execution statistics.")
			.setDefault(DEFAULT_EXECUTION_STATS_FILE);
	}
	
	public static void main(String[] args) {
	    
		Namespace ns = parseArguments(args);
		
		List<String> benchList = 
				OclBenchMapperCsvHandler.parseBenchmarkConfig(Paths.get(ns.getString("benchmarkConfig")));
		Benchmark.initialise(benchList);
		
		Hashtable<Benchmark, TreeMap<String, String>> dataArgConfig = 
				OclBenchMapperCsvHandler.parseBenchmarkDataConfig(Paths.get(ns.getString("benchmarkDataConfig")));
		BenchmarkData.initialise(dataArgConfig);
		
		ExecutionRole role = ns.get("role");
		switch(role) {
			case CLIENT:
				try {
					UserInterfaceType uitype = ns.getBoolean("gui") ? UserInterfaceType.GUI : UserInterfaceType.CONSOLE;
					UserInterface ui;
					if(uitype == UserInterfaceType.GUI) {
						Path execStats = Paths.get(ns.getString("avgExecutionStats"));
						ui = UserInterfaceFactory.createUserInterface(uitype, new ClientGuiConfig(execStats));
					} else {
						ui = UserInterfaceFactory.createUserInterface(uitype);
					}
					
					MapperType mtype = ns.get("mapperType");
					WorkloadMapper mapper;
					if(mtype == MapperType.PREDICTIVE) {
						Path predPreCalc = Paths.get(ns.getString("offlinePredictions"));
						mapper = MapperFactory.createWorkloadMapper(mtype, new PredictiveMapperConfig(predPreCalc));
					} else {
						mapper = MapperFactory.createWorkloadMapper(mtype);
					}
					
					new OclMapperClient(ns.get("addressList"), mapper, ui).runClient();
				} catch (IOException e) {
					throw new UncheckedIOException("ERROR: Connecting to server failed: ", e);
				}
				break;
			case SERVER:
				try {
					int port = ns.getInt("port");
					Path benchExecConf = Paths.get(ns.getString("benchExecConfig"));
					Path simulationConf = ns.getString("simulation") != null ? Paths.get(ns.getString("simulation")) : null;
						
					new OclMapperServer(port, benchExecConf, ns.get("architecture"), simulationConf).runServer();
				} catch (IOException e) {
					throw new UncheckedIOException("ERROR: Starting server failed: ", e);
				}
				break;
			case NICLIENT:
				try {
					Path input = Paths.get(ns.getString("cmdInput"));
					Path output = Paths.get(ns.getString("outputDir"));
					
					UserInterface ui = UserInterfaceFactory.createUserInterface(
							UserInterfaceType.NICONSOLE, new NiConsoleConfig(input, output, ns.getBoolean("fullStats")));
					WorkloadMapper mapper = MapperFactory.createWorkloadMapper(MapperType.FCFS);
					
					new OclMapperClient(Arrays.asList(new String[] { ns.get("address") }), mapper, ui).runClient();
					
				} catch (IOException e) {
					throw new UncheckedIOException("ERROR: Connecting to server failed: ", e);
				}
				break;
			default:
				throw new RuntimeException("Unhandled role: " + role);
		}
	}
}
