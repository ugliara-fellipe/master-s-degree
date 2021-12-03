package ast;

import lexer.Symbol;
import saci.CyanEnv;
import saci.Env;

/** Represents an statement like<br>
 * {@code ++i;}<br>
   @author jose
   
 */
public class StatementMinusMinusIdent extends Statement {


	public StatementMinusMinusIdent(Symbol minusMinus, ExprIdentStar varId) {
		this.minusMinus = minusMinus;
		this.varId = varId;
	}
	
	@Override
	public void genCyanReal(PWInterface pw, boolean printInMoreThanOneLine, CyanEnv cyanEnv, boolean genFunctions) {
		pw.print("--");
		varId.genCyan(pw, printInMoreThanOneLine, cyanEnv, genFunctions);
	}


	@Override
	public void calcInternalTypes(Env env) {
		
		varId.calcInternalTypes(env);
		VariableDecInterface aVar = varId.getVarDeclaration();
		if ( aVar == null || aVar.isReadonly() ) {
			env.error(varId.getFirstSymbol(), "Operator -- can only be applied to variables that are not read only");
		}
		
		Type t = varId.getType(env);
		if ( t != Type.Byte && t != Type.Char && t != Type.Int && t != Type.Long && t != Type.Short && t != Type.Dyn ) {
			env.error(varId.getFirstSymbol(), "Operator -- can only be applied to variables of types Byte, Char, Int, Long, Short, and Dyn");
		}
		super.calcInternalTypes(env);
		
	}

	@Override
	public Symbol getFirstSymbol() {
		return minusMinus;
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
			pw.printlnIdent("    " + javaNameId + " = ((CyChar ) " + javaNameId + ")._pred();");
			pw.printlnIdent("else if ( " + javaNameId + " instanceof CyByte ) ");
			pw.printlnIdent("    " + javaNameId + " = ((CyByte ) " + javaNameId + ")._pred();");
			pw.printlnIdent("else if ( " + javaNameId + " instanceof CyInt ) ");
			pw.printlnIdent("    " + javaNameId + " = ((CyInt ) " + javaNameId + ")._pred();");
			
			pw.printlnIdent("else if ( " + javaNameId + " instanceof CyLong ) ");
			pw.printlnIdent("    " + javaNameId + " = ((CyLong ) " + javaNameId + ")._pred();");
			
			pw.printlnIdent("else if ( " + javaNameId + " instanceof CyShort ) ");
			pw.printlnIdent("    " + javaNameId + " = ((CyShort ) " + javaNameId + ")._pred();");
			
			pw.printlnIdent("else");

			
			pw.printlnIdent("    throw new ExceptionContainer__("
					+ env.javaCodeForCastException(varId, Type.Int) + " );");
			
		}
		else if ( t == Type.Char ) {
			// --ch results in 
			// ch = ch pred that results in _ch = _ch._pred()
			pw.printlnIdent(javaNameId + " = " + javaNameId + "._pred();");
		}
		else {
			// --i; results in   i = i - 1 which results in the Java code _i = _i._minus( new CyInt(1) ) 
			pw.printlnIdent(javaNameId + " = " + javaNameId + "._minus( new " + javaNameType + "(1) );");
		}
	}


	private ExprIdentStar varId;

	private Symbol minusMinus;



}
