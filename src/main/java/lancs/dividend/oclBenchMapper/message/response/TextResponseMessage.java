package lancs.dividend.oclBenchMapper.message.response;

import java.util.StringJoiner;


public class TextResponseMessage extends ResponseMessage {

	private static final long serialVersionUID = 6224196156678875019L;

	private final String text;
	
	public TextResponseMessage(String responseText) {
		super(ResponseType.TEXT);
		text = responseText;
	}
	
	public String getText() { return text; }
	
	@Override
	public String toString() {
		return new StringJoiner(CMD_SEP).add(type.name())
				.add(text).toString();
	}
}
