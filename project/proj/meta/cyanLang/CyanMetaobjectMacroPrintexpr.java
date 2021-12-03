package meta.cyanLang;

import ast.CyanMetaobjectMacroCall;
import ast.Expr;
import error.ErrorKind;
import lexer.Lexer;
import lexer.Token;
import meta.CyanMetaobjectMacro;
import meta.ICompilerMacro_dpa;
import meta.ICompiler_dsa;
import saci.Env;
import saci.Tuple2;

public class CyanMetaobjectMacroPrintexpr extends CyanMetaobjectMacro {

	public CyanMetaobjectMacroPrintexpr() {
		super();
	}
	

	@Override
	public String []getStartKeywords() {
		return new String[] { "printexpr" };
	}
	
	@Override
	public String[] getKeywords() {
		return new String[] { "printexpr" };
	}
	
	
	@Override
	public void dpa_parseMacro(ICompilerMacro_dpa compiler_dpa) {
		
		int offsetStartLine = compiler_dpa.getSymbol().getColumnNumber();

		compiler_dpa.next();
		Expr expr = compiler_dpa.expr();
		if ( compiler_dpa.getThereWasErrors() )
			return ;
		if ( compiler_dpa.getSymbol().token != Token.SEMICOLON ) {
			compiler_dpa.error(compiler_dpa.getSymbol(), "';' expected", null, ErrorKind.metaobject_error);
			return ;				
		}
		else
			compiler_dpa.next();
		
		((CyanMetaobjectMacroCall ) this.getMetaobjectAnnotation()).setInfo_dpa( new Tuple2<Expr, Integer>(expr, offsetStartLine) );
		return ;
	}

	@SuppressWarnings("unchecked")
	@Override
	public StringBuffer dsa_codeToAdd(ICompiler_dsa compiler_dsa) {
		
		Tuple2<Expr, Integer> info = (Tuple2<Expr, Integer> ) ((CyanMetaobjectMacroCall ) this.getMetaobjectAnnotation()).getInfo_dpa();
		Expr expr = info.f1;
		Env env = compiler_dsa.getEnv();
		if ( env.isThereWasError() ) 
			return null;
		
		int offsetStartLine = info.f2;
		StringBuffer s = new StringBuffer();
		if ( offsetStartLine > CyanMetaobjectMacro.sizeWhiteSpace )
			offsetStartLine = CyanMetaobjectMacro.sizeWhiteSpace;
		String identSpace = CyanMetaobjectMacro.whiteSpace.substring(0, offsetStartLine);
		String strExpr = expr.asString();
		//System.out.println(strExpr);
		s.append(identSpace + "Out println: \"'" + Lexer.escapeJavaString(expr.asString()) + "' == \" ++ (" + strExpr + ");\n");
		
		return s;
	}
}
