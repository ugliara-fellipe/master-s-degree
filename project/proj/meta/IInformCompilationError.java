package meta;

/**
 * this interface should be implemented by all metaobjects that need to inform the compiler that a compiler error should be issued.
 * The compiler should issue an error 
   @author jose
 */


public interface IInformCompilationError {
	int getLineNumber();
	String getId();
	String getErrorMessage();
	/**
	 * return true if this metaobject should be used in generic prototypes
	   @return
	 */
	default boolean activeInGenericPrototype() { return false; }
}
