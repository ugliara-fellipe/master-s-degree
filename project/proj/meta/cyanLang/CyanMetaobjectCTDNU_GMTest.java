package meta.cyanLang;

import java.util.ArrayList;
import ast.Expr;
import ast.MessageWithSelectors;
import ast.MetaobjectArgumentKind;
import ast.SelectorWithRealParameters;
import ast.Type;
import lexer.Symbol;
import lexer.Token;
import meta.CyanMetaobjectWithAt;
import meta.DeclarationKind;
import meta.ICompileTimeDoesNotUnderstand_dsa;
import meta.ICompiler_dpa;
import meta.IParseWithCyanCompiler_dpa;
import saci.Env;
import saci.Tuple2;

/**
 * Grammar method test
   @author jose
 */

public class CyanMetaobjectCTDNU_GMTest extends CyanMetaobjectWithAt implements ICompileTimeDoesNotUnderstand_dsa,  
   IParseWithCyanCompiler_dpa  {

	public CyanMetaobjectCTDNU_GMTest() {
		super(MetaobjectArgumentKind.ZeroParameter);
	}

	@Override
	public String getName() {
		return "gmTest";
	}


	@Override
	public DeclarationKind []mayBeAttachedList() {
		return decKindList;
	}

	private static DeclarationKind []decKindList = new DeclarationKind[] { DeclarationKind.PROTOTYPE_DEC };

	@Override
	public Tuple2<StringBuffer, Type> dsa_analyzeReplaceMessageWithSelectors(Expr receiver, MessageWithSelectors message, Env env) {
		
		Object info = metaobjectAnnotation.getInfo_dpa();
		if ( ! (info instanceof Tuple2<?, ?>) ) {
			addError("Internal error of metaobject '" + this.getClass().getName() + "'");
			return null;
		}
		@SuppressWarnings("unchecked")
		Tuple2<String, String> t = (Tuple2<String, String> ) info;
		String source = t.f1;
		String target = t.f2;
		
		ArrayList<SelectorWithRealParameters> selList = message.getSelectorParameterList();
		StringBuffer sb = new StringBuffer();
		sb.append(receiver.asString() + " " + target + " [ ");
		int size = selList.size();
		for ( SelectorWithRealParameters sel : selList ) {
			if ( ! sel.getSelector().getSymbolString().equals(source) ) {
				return null;
			}
			else {
				ArrayList<Expr> exprList = sel.getExprList();
				if ( exprList.size() != 1 ) {
					return null;
				}
				sb.append("( " + exprList.get(0).asString() + " )");
				if ( --size > 0 ) 
					sb.append(", ");
			}
		}
		sb.append(" ] ");
		
		return new Tuple2<StringBuffer, Type>(sb, Type.Nil);
	}

	@Override
	public String getPackageOfType() { return "cyan.lang"; }
	
	@Override
	public String getPrototypeOfType() { return "Nil"; }


	@Override
	public void dpa_parse(ICompiler_dpa compiler_dpa) {
		
		compiler_dpa.next();
		Symbol sym = compiler_dpa.getSymbol(); 
		if ( sym.token != Token.IDENTCOLON ) {
			compiler_dpa.error(compiler_dpa.getSymbol().getLineNumber(), "selector expected. Something as 'add:'");
			return ;
		}
		String source = sym.getSymbolString();
		compiler_dpa.next();
		if ( compiler_dpa.getSymbol().token != Token.MULT ) {
			compiler_dpa.error(compiler_dpa.getSymbol().getLineNumber(), "'*' expected");
			return ;
		}
		compiler_dpa.next();
		if ( compiler_dpa.getSymbol().token != Token.RETURN_ARROW ) {
			compiler_dpa.error(compiler_dpa.getSymbol().getLineNumber(), "'->' expected");
			return ;
		}
		compiler_dpa.next();
		sym = compiler_dpa.getSymbol(); 
		if ( sym.token != Token.IDENTCOLON ) {
			compiler_dpa.error(compiler_dpa.getSymbol().getLineNumber(), "selector expected. Something as 'add:'");
			return ;
		}
		String target = sym.getSymbolString();
		compiler_dpa.next();
		sym = compiler_dpa.getSymbol(); 
		if ( compiler_dpa.getSymbol().token != Token.EOLO ) {
			compiler_dpa.error(compiler_dpa.getSymbol(), "Unexpected symbol: '" + compiler_dpa.getSymbol().getSymbolString() + "'");
		}
		this.metaobjectAnnotation.setInfo_dpa( new Tuple2<String, String>(source, target) );
		
		return ;
	}
	

	@Override
	public boolean shouldTakeText() { return true; }
	
	
}
