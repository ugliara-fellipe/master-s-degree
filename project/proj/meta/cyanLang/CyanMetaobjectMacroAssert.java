package meta.cyanLang;

import ast.CyanMetaobjectMacroCall;
import ast.Expr;
import ast.Type;
import error.ErrorKind;
import lexer.Lexer;
import lexer.Token;
import meta.CyanMetaobjectMacro;
import meta.ICompilerMacro_dpa;
import meta.ICompiler_dsa;
import saci.Env;
import saci.Tuple2;

/**
 * This class represents macro'assert'
 * 
   @author José
 */
public class CyanMetaobjectMacroAssert extends CyanMetaobjectMacro {

	public CyanMetaobjectMacroAssert() { 
	}


	@Override
	public String []getStartKeywords() {
		return new String[] { "assert" };
	}
	
	@Override
	public String[] getKeywords() {
		return new String[] { "assert" };
	}
	
	
	@Override
	public void dpa_parseMacro(ICompilerMacro_dpa compiler_dpa) {
		
		int offsetStartLine = compiler_dpa.getSymbol().getColumnNumber();
		compiler_dpa.next();
		Expr assertExpr = compiler_dpa.expr();
		if ( compiler_dpa.getThereWasErrors() )
			return ;
		if ( compiler_dpa.getSymbol().token != Token.SEMICOLON ) {
			compiler_dpa.error(compiler_dpa.getSymbol(), "';' expected", null, ErrorKind.metaobject_error);
			/*
			errorList = new ArrayList<>();
			errorList.add( new CyanMetaobjectError("';' expected", compiler_dpa.getSymbol().getLineNumber() - startOffsetLine, 
					compiler_dpa.getSymbol().getColumnNumber(), -1) ); */
			return ;				
		}
		else
			compiler_dpa.next();
		
		((CyanMetaobjectMacroCall ) this.getMetaobjectAnnotation()).setInfo_dpa( new Tuple2<Expr, Integer>(assertExpr, offsetStartLine) );
		return ;
	}

	@SuppressWarnings("unchecked")
	@Override
	public StringBuffer dsa_codeToAdd(ICompiler_dsa compiler_dsa) {
		
		CyanMetaobjectMacroCall annotation = (CyanMetaobjectMacroCall ) this.getMetaobjectAnnotation();
		Tuple2<Expr, Integer> info = (Tuple2<Expr, Integer> ) ((CyanMetaobjectMacroCall ) this.getMetaobjectAnnotation()).getInfo_dpa();
		Expr assertExpr = info.f1;
		Env env = compiler_dsa.getEnv();
		//assertExpr.calcInternalTypes(compiler_dsa.getEnv());
		if ( env.isThereWasError() ) 
			return null;
		if ( assertExpr.getType(env) != Type.Boolean && assertExpr.getType(env) != Type.Dyn ) {
			compiler_dsa.error(assertExpr.getFirstSymbol(), "Expression of type Boolean or Dyn expected");
			return null;
		}
		
		int offsetStartLine = info.f2;
		StringBuffer s = new StringBuffer();
		if ( offsetStartLine > CyanMetaobjectMacro.sizeWhiteSpace )
			offsetStartLine = CyanMetaobjectMacro.sizeWhiteSpace;
		String identSpace = CyanMetaobjectMacro.whiteSpace.substring(0, offsetStartLine);
		s.append("\n");
		s.append(identSpace + "if !(");
		s.append(assertExpr.asString() + ") {\n");
		s.append(identSpace + "\"Assert failed in line " + annotation.getFirstSymbol().getLineNumber() + " of prototype '" + annotation.getPackageOfAnnotation() +
				"." + annotation.getPrototypeOfAnnotation() + "'\" println;\n");
		String str = Lexer.escapeJavaString(assertExpr.asString());
		s.append(identSpace + "\"Assert expression: '" + str + "'\" println;\n");
		s.append(identSpace + "};\n");
		return s;
	}
}
