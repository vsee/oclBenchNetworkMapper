package lancs.dividend.oclBenchMapper.message.cmd;


public class ExitCmdMessage extends CommandMessage {

	private static final long serialVersionUID = -9087115065579267953L;

	public ExitCmdMessage() {
		super(CmdType.EXIT);
	}
	
	@Override
	public String toString() {
		return type.name();
	}
}
