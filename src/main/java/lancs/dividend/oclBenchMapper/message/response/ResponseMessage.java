package lancs.dividend.oclBenchMapper.message.response;

import lancs.dividend.oclBenchMapper.message.Message;

public abstract class ResponseMessage extends Message {

	public enum ResponseType { 
		/** Response containing plain text */
		TEXT,
		/** Response containing benchmark results */
		BENCHSTATS
	}
	
	private static final long serialVersionUID = 5253394986954716580L;

	protected final ResponseType type;
	
	public ResponseMessage(ResponseType t) {
		this.type = t;
	}
	
	public ResponseType getType() { return type; }
	
}
