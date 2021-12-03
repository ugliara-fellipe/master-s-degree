/**
 *
 */
package ast;

import lexer.Symbol;
import saci.CyanEnv;
import saci.Env;
import saci.NameServer;
import saci.Tuple2;

/**
 *  This is the superclass of all selectors.  The subclasses of
 *  this class represent things such as
 *       amount:
 *       name: (:newName String)
 *       name: String
 *       println: (:format String, :n int)
 *       amount: (gas: float | alcohol: float)
 *       add: (int)*
 *       + (:other int)
 *

 * @author José
 *
 */
abstract public class Selector implements GenCyan, ASTNode {

	public Selector() {
		astRootType = null;
	}
	
	public void genCyan(PWInterface pw, CyanEnv cyanEnv) { genCyan(pw, false, cyanEnv, true); }
	

	@Override
	abstract public void genCyan(PWInterface pw, boolean printInMoreThanOneLine, CyanEnv cyanEnv, boolean genFunctions);
	abstract public String getStringType();

	
	/**
	 * get the name of the method that would have this selector.
	 * Examples:
	 *     selector         amount:
	 *     value returned by getJavaName     amount
	 *
	 *     selector         amount: float
	 *     value returned by getJavaName     amount_p_float
	 *
	 *     selector         amount: (gas: float | alcohol: float)
	 *     value returned by getJavaName
	 *                      amount_left_gas_p_float_or_alcohol_p_float_right
	 *
	 *     selector         amount: (gas: float | alcohol: float)+
	 *     value returned by getJavaName
	 *                      amount_left_gas_p_float_or_alcohol_p_float_right_plus

	 * @param env
	 * @return
	 */
	abstract public String getJavaName();
	abstract public void calcInterfaceTypes(Env env);


	public String asString(CyanEnv cyanEnv) {
		PWCharArray pwChar = new PWCharArray();
		genCyan(pwChar, true, cyanEnv, true);
		return pwChar.getGeneratedString().toString();
	}
	
	@Override
	public String asString() {
		return asString(NameServer.cyanEnv);
	}

	public Type getAstRootType() {
		return astRootType;
	}


	/**
	 * this method would be used to generate the AST automatically from a prototype. 
	 * It has been temporally abandoned. It will be complex.
	   @param astRootType
	   @param env
	   @param first
	 */
	abstract void setAstRootType(ObjectDec astRootType, Env env, Symbol first);
	

	abstract public String getFullName(Env env);

	/**
	 * return true if this selector matches an empty input. For example, <br>
	 * <code> (add: Int)*</code><br>
	 * matches an emtpy input. So does  
	 * <code> (ch: Char with: String)?</code><br>
	 */
	abstract public boolean matchesEmptyInput();
	
	/**
	 * return the name of this selector, if it has just one selector as <code>add: Int, String</code>.
	 * A composite selector as one of those below return null<br>
	 * <code>
	 * add: (Int)*<br>
	 * add: Int | remove: Int<br>
	 * </code>
	 */
	abstract public String getName();
	
	/**
	 * In a grammar method call, this method is responsible to parse the input,
	 * which is a sequence of selectors with real parameters. <code>lexer</code> is used
	 * to get the next selector with its real parameters. 
	 * 
	   @param selList
	   @param next
	   @return a tuple in which the first field is the string with the code that should
	      replace the input that matched this selector. The second field is the
	      type of the matched selectors. 
	 */
	public abstract Tuple2<String, String> parse(SelectorLexer lexer, Env env);
	
	/**
	 * the ast for this grammar method has its root in type astRootType, a type given by the user.
	 */
	protected Type astRootType;

	public static final String annotAstBuildingMethod = "gmast";
}
