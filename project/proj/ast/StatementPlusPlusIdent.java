/**
  
 */
package ast;

import lexer.Symbol;
import saci.CyanEnv;
import saci.Env;

/** Represents an statement like<br>
 * {@code ++i;}<br>
   @author jose
   
 */
public class StatementPlusPlusIdent extends Statement {


	public StatementPlusPlusIdent(Symbol plusPlus, ExprIdentStar varId) {
		this.plusPlus = plusPlus;
		this.varId = varId;
	}
	
	@Override
	public void genCyanReal(PWInterface pw, boolean printInMoreThanOneLine, CyanEnv cyanEnv, boolean genFunctions) {
		pw.print("++");
		varId.genCyan(pw, printInMoreThanOneLine, cyanEnv, genFunctions);
	}


	@Override
	public void calcInternalTypes(Env env) {
		varId.calcInternalTypes(env);
		VariableDecInterface aVar = varId.getVarDeclaration();
		if ( aVar == null || aVar.isReadonly() ) {
			env.error(varId.getFirstSymbol(), "Operator ++ can only be applied to variables that are not read only");
		}
		Type t = varId.getType(env);
		if ( t != Type.Byte && t != Type.Char && t != Type.Int && t != Type.Long && t != Type.Short && t != Type.Dyn ) {
			env.error(varId.getFirstSymbol(), "Operator ++ can only be applied to variables of types Byte, Char, Int, Long, Short, and Dyn");
		}
		super.calcInternalTypes(env);
		
	}

	@Override
	public Symbol getFirstSymbol() {
		return plusPlus;
	}

	public ExprIdentStar getVarId() {
		return varId;
	}

	@Override
	public void genJava(PWInterface pw, Env env) {
		Type t = varId.getType();
		String javaNameType = t.getJavaName();
		String javaNameId = varId.getJavaName();
		if ( varId.getVarDeclaration().getRefType() )
			javaNameId = javaNameId + ".elem";
		
		if ( t == Type.Dyn ) {
			pw.printlnIdent("if ( " + javaNameId + " instanceof CyChar ) ");
			pw.printlnIdent("    " + javaNameId + " = ((CyChar ) " + javaNameId + ")._succ();");
			pw.printlnIdent("else if ( " + javaNameId + " instanceof CyByte ) ");
			pw.printlnIdent("    " + javaNameId + " = ((CyByte ) " + javaNameId + ")._succ();");
			pw.printlnIdent("else if ( " + javaNameId + " instanceof CyInt ) ");
			pw.printlnIdent("    " + javaNameId + " = ((CyInt ) " + javaNameId + ")._succ();");
			
			pw.printlnIdent("else if ( " + javaNameId + " instanceof CyLong ) ");
			pw.printlnIdent("    " + javaNameId + " = ((CyLong ) " + javaNameId + ")._succ();");
			
			pw.printlnIdent("else if ( " + javaNameId + " instanceof CyShort ) ");
			pw.printlnIdent("    " + javaNameId + " = ((CyShort ) " + javaNameId + ")._succ();");
			
			pw.printlnIdent("else");
			
			pw.printlnIdent("    throw new ExceptionContainer__("
					+ env.javaCodeForCastException(varId, Type.Int) + " );");
			

		}
		else if ( t == Type.Char ) {
			// ++ch results in 
			// ch = ch succ that results in _ch = _ch._succ()
			pw.printlnIdent(javaNameId + " = " + javaNameId + "._succ();");
		}
		else {
			// ++i; results in   i = i + 1 which results in the Java code _i = _i._plus( new CyInt(1) ) 
			pw.printlnIdent(javaNameId + " = " + javaNameId + "._succ();");
		}
	}


	private ExprIdentStar varId;

	private Symbol plusPlus;



}
