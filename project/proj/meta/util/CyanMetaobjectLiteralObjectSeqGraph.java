package meta.util;

import java.util.ArrayList;
import ast.Expr;
import lexer.Token;
import meta.CyanMetaobjectLiteralObjectSeq;
import meta.ICompiler_dpa;
import meta.ICompiler_dsa;
import meta.IExpr;
import meta.IParseWithCyanCompiler_dpa;

/**
 * a graph between <code>[%</code> and <code>%]</code>. An example is
 * </p>
 * <code> 
 * var g = [% 1:2, 2:3, 3:1 %];</p>
 * </code> </p>
 * The numbers should be Int literals
   @author José
 */
public class CyanMetaobjectLiteralObjectSeqGraph extends CyanMetaobjectLiteralObjectSeq 
       implements IParseWithCyanCompiler_dpa {

	public CyanMetaobjectLiteralObjectSeqGraph() {
	}

	@Override
	public void dpa_parse(ICompiler_dpa compiler_dpa) {
		
		
		/**
		 * each edge of the graph is represented by two numbers in exprList
		 */
		ArrayList<IExpr> exprList = new ArrayList<>();

		
		while ( compiler_dpa.symbolCanStartExpr(compiler_dpa.getSymbol()) ) {
			exprList.add(compiler_dpa.expr());
			if ( compiler_dpa.getSymbol().token != Token.COLON ) {
				compiler_dpa.error(compiler_dpa.getSymbol(), "':' was expected");
				return ;
			}
			else {
				compiler_dpa.next();
				exprList.add(compiler_dpa.expr());
				if ( compiler_dpa.getSymbol().token == Token.COMMA) {
					compiler_dpa.next();
				}
				else { 
					// not ',', the list of pair should have ended
					break;
				}
				
			}
		}
		if ( compiler_dpa.getSymbol().token == Token.EOLO ) {
			this.getMetaobjectAnnotation().setInfo_dpa(exprList);
			return ;
		}
		else {
			compiler_dpa.error(compiler_dpa.getSymbol(), "Syntax error in literal graph");
			return ;
		}
	}
	
	/**
	 * produces something like</p>
	 * <code> </p>
	 * ({ var1 = Graph new; </p>
	 *    var1 add: 1, 2; </p>
	 *    var1 add: 2, 3; </p>
	 *  } eval)</p>
	 * </code></p>
	 * for <code>"[% 1:2, 2:3 %]"</code>
	   @param compiler_dsa
	   @return
	 */
	@Override
	public StringBuffer dsa_codeToAdd(ICompiler_dsa compiler_dsa) {
		
		ArrayList<Expr> exprList;
		exprList = (ArrayList<Expr> ) getMetaobjectAnnotation().getInfo_dpa();
		
		StringBuffer s = new StringBuffer();
		s.append(" ({ var cyan.util.Graph ");
		String varName = compiler_dsa.getEnv().getNewUniqueVariableName();
		s.append( varName + " = Graph new;\n");
		for ( int i = 0; i < exprList.size(); i+=2) {
			s.append(varName + " addNumberEdge:" + exprList.get(i).asString() + ", " + exprList.get(i+1).asString() + ";\n");
		}
		s.append("^" + varName + " } eval)");
		return s;
	}

	@Override
	public String leftCharSequence() {
		return "[%";
	}

	



	@Override
	public String getPackageOfType() {
		return "cyan.util";
	}

	@Override
	public String getPrototypeOfType() {
		return "Graph";
	}


}
