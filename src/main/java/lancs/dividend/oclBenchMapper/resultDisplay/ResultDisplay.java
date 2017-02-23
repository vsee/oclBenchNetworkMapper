package lancs.dividend.oclBenchMapper.resultDisplay;

import java.util.Hashtable;

import lancs.dividend.oclBenchMapper.connection.ServerConnection;
import lancs.dividend.oclBenchMapper.mapping.ExecutionItem;

/**
 * This class merges execution results and displays them to the user.
 * 
 * @author vseeker
 *
 */
public interface ResultDisplay {

	void display(Hashtable<ServerConnection, ExecutionItem> executionMap);

}
