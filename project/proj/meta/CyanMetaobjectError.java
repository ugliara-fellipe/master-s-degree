package meta;

/**
 * this class represents an error in a metaobject
   @author José
 */

public class CyanMetaobjectError {
	
	public CyanMetaobjectError(String message, int line, int column, int offsetStartMetaobjectAnnotation) {
		this.message = message;
		this.line = line;
		this.column = column;
		this.offsetStartMetaobjectAnnotation = offsetStartMetaobjectAnnotation;
	}
	

	public String getMessage() {
		return message;
	}
	public int getLine() {
		return line;
	}
	public int getColumn() {
		return column;
	}
	public int getOffsetStartMetaobjectAnnotation() {
		return offsetStartMetaobjectAnnotation;
	}



	/**
	 * the error message
	 */
	private String	message;
	
	/**
	 * line of the error relative to the start of the metaobject annotation
	 */
	private int line;
	/**
	 * column of the error relative to the start of line of the error
	 */
	private int column;	
	/**
	 * offset of the error from the start of the metaobject annotation. offset is the number of characters. 
	 * -1 if not used 
	 */
	private int offsetStartMetaobjectAnnotation;
}
