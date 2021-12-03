package meta.cyanLang;

import java.util.ArrayList;
import lexer.Symbol;
import lexer.Token;
import meta.CyanMetaobjectLiteralObjectSeq;
import meta.ICompiler_dpa;
import meta.ICompiler_dsa;
import meta.IExpr;
import meta.IParseWithCyanCompiler_dpa;

public class CyanMetaobjectLiteralObjectSeqSet extends CyanMetaobjectLiteralObjectSeq implements IParseWithCyanCompiler_dpa {

	public CyanMetaobjectLiteralObjectSeqSet() {
	}

	@Override
	public void dpa_parse(ICompiler_dpa compiler_dpa) {
		/**
		 * elements of the set
		 */
		ArrayList<IExpr> exprList = new ArrayList<>();;
		
		while ( compiler_dpa.symbolCanStartExpr(compiler_dpa.getSymbol()) ) {
			exprList.add(compiler_dpa.expr());
			if ( compiler_dpa.getSymbol().token == Token.COMMA) {
				compiler_dpa.next();
			}
			else { 
				// not ',', the list should have ended
				break;
			}
		}
		if ( exprList.size() == 0 ) {
			compiler_dpa.error(compiler_dpa.getSymbol(), "A literal set should have at least one element");
			return ;
		}
		Symbol sy = compiler_dpa.getSymbol();
		if ( sy.token == Token.EOLO ) {
			this.getMetaobjectAnnotation().setInfo_dpa(exprList);
			return ;
		}
		else {
			compiler_dpa.error(compiler_dpa.getSymbol(), "Syntax error in literal set");
			return ;
		}
	}
	
	/*
	 * 
	 * 
	 */
	
	
	/**
	 * produces something like</p>
	 * <code> </p>
	 * ({ var1 = Set{@literal <}Int> new; </p>
	 *    var1 add: 1; </p>
	 *    var1 add: 2; </p>
	 *    var1 add: 3; </p>
	 *  } eval)</p>
	 * </code></p>
	 * for <code>"[* 1, 2, 3 *]"</code>
	   @param compiler_dsa
	   @return
	 */
	@SuppressWarnings("unchecked")
	@Override
	public StringBuffer dsa_codeToAdd(ICompiler_dsa compiler_dsa) {
		ArrayList<IExpr> exprList;
		exprList = (ArrayList<IExpr> ) getMetaobjectAnnotation().getInfo_dpa();
		StringBuffer s = new StringBuffer();
		s.append(" ( { var ");
		String varName = compiler_dsa.getEnv().getNewUniqueVariableName();
		s.append( varName + " = " + this.getPackageOfType() + "." + getPrototypeOfType() + " new;\n");
		for ( int i = 0; i < exprList.size(); ++i) {
			s.append(varName + " add: " + exprList.get(i).asString() + ";\n");
		}
		s.append("^" + varName + " } eval)");
		
		return s;
	}

	@Override
	public String leftCharSequence() {
		return "[*";
	}

	
	




	@Override
	public String getPackageOfType() {
		return "cyan.util";
	}

	@SuppressWarnings("unchecked")
	@Override
	public String getPrototypeOfType() {
		
		String ret = "Set<" + ((ArrayList<IExpr> ) getMetaobjectAnnotation().getInfo_dpa()).get(0).getIType().getFullName() + ">";
		return ret;
	}


}
