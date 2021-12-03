/*
 * Eduardo Romao da Rocha
 * */

package meta.tg;

import java.util.ArrayList;
import ast.MetaobjectArgumentKind;
import lexer.Token;
import meta.CyanMetaobjectWithAt;
import meta.IAction_dsa;
import meta.ICompiler_dpa;
import meta.ICompiler_dsa;
import meta.IExpr;
import meta.IParseWithCyanCompiler_dpa;
import saci.Tuple4;

public class CyanMetaobjectFSM extends CyanMetaobjectWithAt implements IAction_dsa, IParseWithCyanCompiler_dpa{
	
	public CyanMetaobjectFSM(){
		super(MetaobjectArgumentKind.ZeroParameter);
	}
	
	@Override
	public String getName(){
		return "fsm";
	}
	
	@Override
	public boolean shouldTakeText(){
		return true;
	}
	
	@Override
	public void dpa_parse(ICompiler_dpa comp) {
		ArrayList<Tuple4<String, String, String, IExpr>> tupleList = new ArrayList<>();
		comp.next();
		
		
		//alterar com o metodo nextLocalVariableName()
		String tempVarName = /*comp.nextLocalVariableName()*/ "tmp232323232__";
		String auxStr = "{ let "+ tempVarName + " = FSM new;\n";
		
		while ( comp.getSymbol().token == Token.IDENT ) {
			String origin = comp.getSymbol().getSymbolString();
			comp.next();
			if ( comp.getSymbol().token != Token.COMMA ) {
				comp.error(comp.getSymbol(), "A ',' was expected");
			}
			comp.next();
			if ( comp.getSymbol().token != Token.LITERALSTRING ) {
				comp.error(comp.getSymbol(), "A literal string was expected");
			}
			String input = comp.getSymbol().getSymbolString();
			
			comp.next();
			if ( comp.getSymbol().token != Token.COMMA ) {
				comp.error(comp.getSymbol(), "A ',' was expected");
			}
			comp.next();
			
			if ( comp.getSymbol().token != Token.IDENT ) {
				comp.error(comp.getSymbol(), "Identifier of a state was expected");
			}
			String target = comp.getSymbol().getSymbolString();
			comp.next();
			if ( comp.getSymbol().token != Token.COMMA ) {
				comp.error(comp.getSymbol(), "A ',' was expected");
			}
			comp.next();

			IExpr expr = comp.expr();
			if ( !(expr instanceof ast.ExprFunction) ) {
				comp.error(comp.getSymbol(), "An anonymous function was expected");
			}
			tupleList.add(new Tuple4<String, String, String, IExpr>(
					 origin, input, target, expr));
			
			
			auxStr += tempVarName + " add: #" + origin + ", #" + input 
					+ ", #" + target + ", " + expr.asString() + ";\n";
		}
		
		auxStr += "} eval";
		this.getMetaobjectAnnotation().setInfo_dpa(auxStr);
		if ( comp.getSymbol().token != Token.EOLO ) {
			comp.error(comp.getSymbol(), "Unexpected symbol: '" + comp.getSymbol().getSymbolString() + "'");
		}
		
		
	}
	
	@Override
	public StringBuffer dsa_codeToAdd(ICompiler_dsa ic) {
		return new StringBuffer((String) this.getMetaobjectAnnotation().getInfo_dpa());
		
		/*ArrayList<Tuple4<String, String, String, IExpr>> tupleList = (ArrayList<Tuple4<String, String, String, IExpr>> ) 
				this.getMetaobjectAnnotation().getInfo_dpa();
		StringBuffer s = new StringBuffer();

		int len = tupleList.size();
		
		for(int i = 0; i < len; i++){
			Tuple4<String, String, String, IExpr> t = tupleList.get(0);
			s.append("\"\"\"" + t.f1 + "," + t.f2 + "," + t.f3 + "," + t.f4.asString() + "\"\"\"");
			if(i != len - 1){
				s.append("\n");
			}
		}
		//byte[] bytelist = this.getMetaobjectAnnotation().getCodegInfo();
		String str = Lexer.escapeJavaString("FSM( " + "\"\"\"" + new String(s) + "\"\"\"" + " )");
		return new StringBuffer("" + str);*/
	}
	
	@Override
	public boolean isExpression(){
		return true;
	}
	
	@Override
	public String getPackageOfType() { return "cyan.lang"; }

	@Override
	public String getPrototypeOfType() { return "Any"; }
}
