package lancs.dividend.oclBenchMapper.message.response;

import java.util.StringJoiner;


public class ArchResponseMessage extends ResponseMessage {

	private static final long serialVersionUID = 4771939187979004422L;

	private final String archDescr;
	
	public ArchResponseMessage(String architectureDescription) {
		super(ResponseType.ARCH);
		this.archDescr = architectureDescription;
	}
	
	public String getArchDescription() { return archDescr; }
	
	@Override
	public String toString() {
		return new StringJoiner(CMD_SEP).add(type.name())
				.add(archDescr).toString();
	}
}
