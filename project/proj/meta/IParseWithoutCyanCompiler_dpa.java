package meta;



/**
 * this interface contains method interfaces to parse literal objects  
 * such as <code>r"[A-Z]+"</code>, <code>1011bin</code> and<br>
 * <code><br>
 *    {@literal @}fsm{*<br> 
 *        q0, 0, q1<br>
 *        q0, 1, q0<br>
 *        q1, 0, q0<br>
 *        q1, 1, q1<br>
 *        final states: q1<br>
 *    *}<br>
 * </code> 
 * 
 * The parsing is made without the help of the Cyan compiler.
 *  
   @author José
 */
public interface IParseWithoutCyanCompiler_dpa extends IParse_dpa {


	/**
	 * parse the code inside the literal object which is given by parameter <code>code</code>. The compilation
	 * errors can be retrieved by method {@link #getErrorMessageList}
	 */
	void dpa_parse(ICompilerAction_dpa compilerAction, String code);
	

}
