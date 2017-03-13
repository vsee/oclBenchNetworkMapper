package lancs.dividend.oclBenchMapper.mapping;

import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;

import lancs.dividend.oclBenchMapper.message.CommandMessage;
import lancs.dividend.oclBenchMapper.userCmd.UserCommand;

/**
 * The duplicate mapper assigns the entire workload
 * to each available server.
 * 
 * @author vseeker
 */
public class DuplicateMapper implements WorkloadMapper {

	@Override
	public Hashtable<String, List<ExecutionItem>> mapWorkload(String[] serverAdresses, UserCommand cmd) {
		if(serverAdresses == null || serverAdresses.length == 0)
			throw new IllegalArgumentException("Given server connections must not be null or empty.");
		if(cmd == null)
			throw new IllegalArgumentException("Given command must not be null.");
		
		Hashtable<String, List<ExecutionItem>> map = new Hashtable<>();
		
		for(String s : serverAdresses) {
			map.put(s, new ArrayList<>());
			map.get(s).add(new ExecutionItem(new CommandMessage(cmd), s));
		}
		
		return map;
	}



}
