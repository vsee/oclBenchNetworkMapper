package lancs.dividend.oclBenchMapper.message.response;

import lancs.dividend.oclBenchMapper.message.Message;

public abstract class ResponseMessage extends Message {

	public enum ResponseType { 
		/** Response containing benchmark results */
		BENCHSTATS,
		/** Server reports an error */
		ERROR
	}
	
	private static final long serialVersionUID = 5253394986954716580L;

	protected final ResponseType type;
	
	public ResponseMessage(ResponseType resType) {
		this.type = resType;
	}
	
	public ResponseType getType() { return type; }
	
}
