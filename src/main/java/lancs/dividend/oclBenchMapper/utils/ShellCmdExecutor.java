package lancs.dividend.oclBenchMapper.utils;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

/**
 * This class provides methods to executes shell commands.
 * 
 * @author vseeker
 * 
 */
public class ShellCmdExecutor {

	// TODO non blocking execution
	// TODO optional timeout for blocking execution
	
	/**
	 * Execute a given command string {@code cmd} as shell command 
	 * and returns the resulting standard output. The method blocks waiting
	 * for the execution process to return.
	 * 
	 * @param cmd shell command to be executed. Example: "{@code ls -a -l}"
	 * @param redirectStdErr specifies whether the standard error stream of the executed
	 * command is to be redirected into standard out and returned with the result.
	 * @throws IllegalArgumentException if {@code cmd} String is null.
	 * @return standart out of the executed command
	 */
	public static String executeCmd(String cmd, boolean redirectStdErr) {
		
		if(cmd == null) throw new IllegalArgumentException("Given console command must not be null.");	
		
		List<String> cmdList = new ArrayList<>();
		cmdList.add("sh");
		cmdList.add("-c");
		cmdList.add(cmd);
		
		ProcessBuilder pb = new ProcessBuilder(cmdList);
		pb.redirectErrorStream(redirectStdErr);
		StringBuilder result = new StringBuilder();
		
		try {
			Process cmdProc = pb.start();
			BufferedReader reader = new BufferedReader(
					new InputStreamReader(cmdProc.getInputStream()));
			
			String line = null;
			while((line = reader.readLine()) != null) {
				result.append(line).append("\n");
			}
			
			cmdProc.waitFor();
		} catch (IOException ioe) {
			System.err.println("IO Error executing shell command: " + ioe);
			result.append(ioe.getMessage());
		} catch (InterruptedException ire) {
			System.err.println("Interrupt Error executing shell command: " + ire);
			result.append(ire.getMessage());
		}
		
		return result.toString();
	}
	
}
