/**
 * 
 */
package ast;

import java.util.ArrayList;
import lexer.Symbol;
import saci.CyanEnv;
import saci.NameServer;

/** Represents a message with a binary operator such as '+' in prototype Int
 * @author jose
 *
 */
public class MessageBinaryOperator extends MessageWithSelectors {
	

	public MessageBinaryOperator(Symbol binaryOperator, Expr expr) {
		ArrayList<SelectorWithRealParameters> selectorParameterList = new ArrayList<SelectorWithRealParameters>();
		ArrayList<Expr> exprList = new ArrayList<Expr>();
		exprList.add(expr);
		selectorParameterList.add(new SelectorWithRealParameters(binaryOperator, false, exprList));
		this.setSelectorParameterList(selectorParameterList);
	}
	
	
	@Override
	public boolean isDynamicMessageSend() { return false; }
	
	@Override
	public void genCyan(PWInterface pw, boolean printInMoreThanOneLine, CyanEnv cyanEnv, boolean genFunctions) {
		pw.print( this.getSelectorParameterList().get(0).getSelector().getSymbolString() + " ");
		this.getSelectorParameterList().get(0).getExprList().get(0).genCyan(pw, printInMoreThanOneLine, cyanEnv, genFunctions);
	}
	
	@Override
	public String getJavaMethodName() {
		return NameServer.getJavaNameOfSelector(this.getSelectorParameterList().get(0).getSelectorNameWithoutSpecialChars());
	}


}
