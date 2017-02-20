package lancs.dividend.oclBenchMapper.message.response;

import java.util.StringJoiner;


public class ErrorResponseMessage extends ResponseMessage {

	private static final long serialVersionUID = 4771939187979004422L;

	private final String text;
	
	public ErrorResponseMessage(String errorText) {
		super(ResponseType.ERROR);
		text = errorText;
	}
	
	public String getText() { return text; }
	
	@Override
	public String toString() {
		return new StringJoiner(CMD_SEP).add(type.name())
				.add(text).toString();
	}
}
