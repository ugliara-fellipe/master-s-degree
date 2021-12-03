package ast;

import saci.CyanEnv;
import saci.Env;
import saci.NameServer;


/**
 *  represents the message name and arguments. In message send
 *           anObject with: anotherObject do: aFunction1 aFunction2
 *  there would be created two objects of SelectorWithRealParameters
 *  for list  selectorParameterList. One represents
 *           with: anotherObject
 *  and the other represents
 *           do: aFunction1 aFunction2
 */
public abstract class Message {

	public Message() {
		super();
	}

	/**
	 * true if the selectors are preceded by '?' as in the example
	 *       person ?setAge: 30;
	 *  That would mean a dynamic call.
	 * No static type-checking is made.
	 */
	abstract public boolean isDynamicMessageSend();

	public abstract void genCyan(PWInterface pw, boolean printInMoreThanOneLine, CyanEnv cyanEnv, boolean genFunctions);

	public abstract String getJavaMethodName();
	
	/** calculates all types somehow linked to the value represented by this class
	 * */

	abstract public void calcInternalTypes(Env env);		

	
	public String asString(CyanEnv cyanEnv) {
		PWCharArray pwChar = new PWCharArray();
		genCyan(pwChar, true, cyanEnv, true);
		return pwChar.getGeneratedString().toString();
	}
	
	public String asString() {
		return asString(NameServer.cyanEnv);
	
	}

}