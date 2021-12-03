/**
 *
 */
package ast;

import java.util.ArrayList;
import error.ErrorKind;
import lexer.Symbol;
import lexer.SymbolIdent;
import lexer.Token;
import saci.CompilerManager;
import saci.CyanEnv;
import saci.Env;
import saci.Function0;
import saci.NameServer;


/** represents a literal array such as
 *    [ 1, 2, 3 [
 *    [ [ "um", "dois" ], [ "one", "two" ] ]
 *    
 * @author José
 *
 */
public class ExprLiteralArray extends ExprAnyLiteral {

	/**
	 *
	 */
	public ExprLiteralArray( Symbol startSymbol, Symbol endSymbol,
			                 ArrayList<Expr> exprList ) {
		this.startSymbol = startSymbol;
		this.endSymbol = endSymbol;
		this.exprList = exprList;
	}

	public void setExprList(ArrayList<Expr> exprList) {
		this.exprList = exprList;
	}

	public ArrayList<Expr> getExprList() {
		return exprList;
	}

	public void setStartSymbol(Symbol startSymbol) {
		this.startSymbol = startSymbol;
	}

	public Symbol getStartSymbol() {
		return startSymbol;
	}

	public void setEndSymbol(Symbol endSymbol) {
		this.endSymbol = endSymbol;
	}

	public Symbol getEndSymbol() {
		return endSymbol;
	}


	@Override
	public boolean isNRE(Env env) {
		for ( Expr e : exprList ) {
			if ( ! e.isNRE(env) )
				return false;
		}
		return true;
	}	
	
	@Override
	public void genCyanReal(PWInterface pw, boolean printInMoreThanOneLine, CyanEnv cyanEnv, boolean genFunctions) {
		pw.print(" [ ");
		int n = exprList.size();
		for ( Expr e : exprList ) {
			e.genCyan(pw, false, cyanEnv, genFunctions);
			--n;
			if ( n > 0 )
				pw.print(", ");
		}
		pw.print(" ] ");

	}

	
	@Override
	public String metaobjectParameterAsString(Function0 inError) {
		StringBuffer s = new StringBuffer(); 
		s.append(" [ ");
		int n = exprList.size();
		for ( Expr e : exprList ) {
			if ( !(e instanceof ExprAnyLiteral) ) {
				inError.eval();
			}
			s.append( ((ExprAnyLiteral) e).metaobjectParameterAsString(inError) );
			--n;
			if ( n > 0 )
				s.append(", ");
		}
		s.append(" ] ");
		return s.toString();
	}	
	
	
	/*
	public void genCyanReplacingGenericParameters(PWInterface pw, CyanEnv cyanEnv) {
		pw.print("{# ");
		int n = exprList.size();
		for ( Expr e : exprList ) {
			if ( e instanceof ExprLiteral ) 
				((ExprLiteral ) e).genCyanReplacingGenericParameters(pw, cyanEnv);
			else 
				e.genCyan(pw, cyanEnv);
			--n;
			if ( n > 0 )
				pw.print(", ");
		}
		pw.print(" #}");
	}
	*/
	
	
	@Override
	public String genJavaExpr(PWInterface pw, Env env) {

		String literalArrayTmpVar = NameServer.nextJavaLocalVariableName();

		// String javaTypeFirstExpr = exprList.get(0).getType(env).getJavaName();

		String javaArrayType = NameServer.getJavaName("Array<" + exprList.get(0).getType().getFullName() + ">"); 
		pw.printlnIdent( javaArrayType + " " + literalArrayTmpVar + " = new " + javaArrayType +  
				  "( new CyInt(" +
		                this.exprList.size() + ") );"   );
				
		String tmpVar;
		for ( Expr e : exprList ) {
			tmpVar = e.genJavaExpr(pw, env);
			pw.printlnIdent(literalArrayTmpVar + "." + NameServer.javaNameAddMethod + "( " + tmpVar + ");");
		}

		return literalArrayTmpVar;
	}

	/*
	@Override
	public void genJavaExprWithoutTmpVar(PWInterface pw, Env env) {
		throw new ExceptionGenJavaExprWithoutTmpVar();
	}
	*/
	
	
	@Override
	public Symbol getFirstSymbol() {
		return startSymbol;
	}




	@Override
	public void calcInternalTypes(Env env) {
		
		
		for ( Expr expr : exprList ) 
			expr.calcInternalTypes(env);
		
		arrayElementType = exprList.get(0).getType(env);
		
		
		
		if ( arrayElementType instanceof ProgramUnit || arrayElementType == Type.Dyn ) {
			
			for ( Expr expr : exprList ) { 
				if ( ! arrayElementType.isSupertypeOf(expr.getType(), env) ) {
					env.error(expr.getFirstSymbol(), "This expression should be subtype of the type of the first "
							+ "expression of this literal array, '" + arrayElementType.getFullName() + "'" 
							+ ". It is not");
				}
			}
			
			
			Symbol sym = this.getFirstSymbol();

			SymbolIdent symbolIdent = new SymbolIdent(Token.IDENT, "Array", sym.getStartLine(), 
					sym.getLineNumber(), sym.getColumnNumber(), sym.getOffset(), sym.getCompilationUnit() );
			ExprIdentStar typeIdent = new ExprIdentStar(symbolIdent);
			
			ArrayList<ArrayList<Expr>> realTypeListList = new ArrayList<ArrayList<Expr>>(); 
			ArrayList<Expr> realTypeList = new ArrayList<Expr>();
			
			/* # ExprIdentStar exprElementType = new ExprIdentStar( new SymbolIdent(Token.IDENT, p.getName(), -1, -1, -1, -1) );
			
			realTypeList.add(exprElementType); */
			realTypeList.add( arrayElementType.asExpr(this.getFirstSymbol()) ); 
			realTypeListList.add(realTypeList);
			
			ExprGenericPrototypeInstantiation gpi = new ExprGenericPrototypeInstantiation( typeIdent, 
					realTypeListList, env.getCurrentProgramUnit(), null); 		
			type = CompilerManager.createGenericPrototype(gpi, env);	
			
			/* String typeName = "Array<" + p.getName() + ">";
			type = env.searchVisibleProgramUnit(typeName, exprList.get(0).getFirstSymbol(), true); */
			assert type != null;
		}
		else //if ( arrayElementType instanceof TypeJava ) 
			env.error(true, exprList.get(0).getFirstSymbol(), "The type of this expression should be a Cyan prototype", null, ErrorKind.prototype_as_type_expected_inside_method);

		super.calcInternalTypes(env);

	}

	@Override
	public Object getJavaValue() {
		Object []objArray = new Object[exprList.size()];
		int i = 0;
		for ( Expr e : exprList ) {
			if ( e instanceof ExprAnyLiteral ) {
			    objArray[i] = ((ExprAnyLiteral ) e).getJavaValue();
			    ++i;
			}
		}
		return objArray;
	}
	
	@Override
	public StringBuffer getStringJavaValue() {
		StringBuffer s = new StringBuffer();
		s.append("new ");
		if ( exprList.size() == 0 )
			s.append("Object[] { } ");
		else {
			s.append(((ExprAnyLiteral ) exprList.get(0)).getJavaType());
			s.append("[] { ");
			int size = exprList.size();
			for ( Expr e : exprList ) {
				s.append( ((ExprAnyLiteral ) e).getStringJavaValue() );
				if ( --size > 0 ) 
					s.append(", ");
			}
			
			s.append(" }");
		}
		return s;
	}
	
	@Override
	public String getJavaType() {
		if ( exprList.size() == 0 )
			return "Object[]";
		else {
			return ((ExprAnyLiteral ) exprList.get(0)).getJavaType() + "[]";
		}
	}
	
	@Override
	public boolean isValidMetaobjectFeatureParameter() {
		String firstTypeName = exprList.get(0).getClass().getName();
		for (int i = 1; i < exprList.size() - 1; ++i) {
			if ( ! exprList.get(i).getClass().getName().equals(firstTypeName) ) {
				return false;
			}
		}
		return true;
	}	
	
	/**
	 * symbols representing [ and ]
	 */
	private Symbol startSymbol, endSymbol;
	private ArrayList<Expr> exprList;
	/**
	 * the type of the array elements, which is the type of the first array element
	 */
	private Type arrayElementType;
}
