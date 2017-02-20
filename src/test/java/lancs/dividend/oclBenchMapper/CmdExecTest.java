package lancs.dividend.oclBenchMapper;

import lancs.dividend.oclBenchMapper.utils.ShellCmdExecutor;

import org.testng.Assert;
import org.testng.annotations.Test;

public class CmdExecTest {

	@Test
	public void testBlockingCmd() {
		
		String res = ShellCmdExecutor.executeCmd(
				"ls src/main/java/lancs/dividend/oclBenchMapper/utils/ShellCmdExecutor.java", true);
		
		Assert.assertEquals(res.trim(), "src/main/java/lancs/dividend/oclBenchMapper/utils/ShellCmdExecutor.java");
		
		res = ShellCmdExecutor.executeCmd(
				"ls blabla.blub", true);
		
		Assert.assertEquals(res.trim(), "ls: cannot access blabla.blub: No such file or directory");
	}
	
	@Test(expectedExceptions = IllegalArgumentException.class)
	public void testCmdNull() {
		ShellCmdExecutor.executeCmd(null, false);
	}
}
