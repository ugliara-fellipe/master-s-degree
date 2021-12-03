/**
  
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

/**
   @author José
   
 */
public class ExprLiteralTuple extends ExprAnyLiteral {

	/**
	 *
	 */
	public ExprLiteralTuple( Symbol startSymbol, Symbol endSymbol,
			                 ArrayList<Expr> exprList, boolean isNamedTuple ) {
		this.startSymbol = startSymbol;
		this.endSymbol = endSymbol;
		this.exprList = exprList;
		this.isNamedTuple = isNamedTuple;
	}

	public ArrayList<Expr> getExprList() {
		return exprList;
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
		if ( isNamedTuple ) {
			   // named tuple like  [. name: "Newton", age: 85 .]
			int i = 1;
			for ( Expr e : exprList ) {
				if ( i%2 == 0 ) {
					if ( ! e.isNRE(env) ) {
						return false;
					}
				}
				++i;
			}
		}
		else {
			for ( Expr e : exprList ) {
				if ( ! e.isNRE(env) ) {
					return false;
				}
			}
		}

		return true;
	}	
		

	@Override
	public void genCyanReal(PWInterface pw, boolean printInMoreThanOneLine, CyanEnv cyanEnv, boolean genFunctions) {
		pw.print("[. ");
		int n = exprList.size();
		if ( isNamedTuple ) {
			   // named tuple like  [. name: "Newton", age: 85 .]
			for ( Expr e : exprList ) {
				e.genCyan(pw, false, cyanEnv, genFunctions);
				--n;
				
				if ( n > 0 ) {
					if ( n%2 == 1 ) 
						pw.print(" = ");
					else
						pw.print(", ");
				}
			}
		}
		else {
			   // unnamed tuple like  [. "Lívia", 7, "Carolina", 4 .]
			for ( Expr e : exprList ) {
				e.genCyan(pw, false, cyanEnv, genFunctions);
				--n;
				if ( n > 0 ) {
					pw.print(", ");
				}
			}			
		}
		pw.print(" .]");

	}

	
	@Override
	public String metaobjectParameterAsString(Function0 inError) {
		StringBuffer s = new StringBuffer(); 
		s.append(" [. ");
		
		int n = exprList.size();
		if ( isNamedTuple ) {
			   // named tuple like  [. name: "Newton", age: 85 .]
			for ( Expr e : exprList ) {
				if ( !(e instanceof ExprAnyLiteral) ) {
					inError.eval();
				}
				s.append( ((ExprAnyLiteral) e).metaobjectParameterAsString(inError) );
				--n;
				
				if ( n > 0 ) {
					if ( n%2 == 1 ) 
						s.append(" = ");
					else
						s.append(", ");
				}
			}
		}
		else {
			   // unnamed tuple like  [. "Lívia", 7, "Carolina", 4 .]
			for ( Expr e : exprList ) {
				if ( !(e instanceof ExprAnyLiteral) ) {
					inError.eval();
				}
				s.append( ((ExprAnyLiteral) e).metaobjectParameterAsString(inError) );
				--n;
				if ( n > 0 ) {
					s.append(", ");
				}
			}			
		}
		s.append(" .] ");
		return s.toString();
	}			
	
	/*
	@Override
	public void genCyanReplacingGenericParameters(PWInterface pw, CyanEnv cyanEnv) {
		pw.print("[. ");
		int n = exprList.size();
		if ( isNamedTuple ) {
			   // named tuple like  [. name: "Newton", age: 85 .]
			for ( Expr e : exprList ) {
				if ( e instanceof ExprLiteral && n%2 == 0) 
					((ExprLiteral ) e).genCyanReplacingGenericParameters(pw, cyanEnv);
				else 
					e.genCyan(pw, false, cyanEnv, true);
				--n;
				
				if ( n > 0 ) {
					if ( n%2 == 1 ) 
						pw.print(": ");
					else
						pw.print(", ");
				}
			}
		}
		else {
			   // unnamed tuple like  [. "Lívia", 7, "Carolina", 4 .]
			for ( Expr e : exprList ) {
				
				if ( e instanceof ExprLiteral ) 
					((ExprLiteral ) e).genCyanReplacingGenericParameters(pw, cyanEnv);
				else 
					e.genCyan(pw, cyanEnv);
				--n;
				if ( n > 0 ) {
					pw.print(", ");
				}
			}			
		}
		pw.print(" .]");

	}
	*/
		
	/*	
	@Override
	public String javaNameAsType(Env env) {
		String name = "Tuple<";
		int size = exprList.size() - 1;
		if ( isNamedTuple ) {
			int i = 0;
			for ( Expr e : exprList ) {
				if ( i%2 == 0 ) 
					name += e.asString();
				else
					name += e.getType(env).getName();
				if ( i < size ) 
					name += ", ";
			    ++i;
			}
			
		}
		else {
			int i = 0;
			for ( Expr e : exprList ) {
				name += e.getType(env).getName();
				if ( i < size ) 
					name += ", ";
				++i;
			}
		}
		return NameServer.getJavaNameIdentifierAsType(name + ">");
	}
	*/
	@Override
	public String genJavaExpr(PWInterface pw, Env env) {

		String literalTupleTmpVar = NameServer.nextJavaLocalVariableName();

		ArrayList<String> tmpVarList = new ArrayList<String>();
		
		if ( isNamedTuple ) {
			/*
			 *     public fun init: (T1 g1, T2 g2, T3 g3, T4 g4, T5 g5, T6 g6) 
			 */
			int i = 0;
			for ( Expr expr : exprList ) {
				if ( i%2 == 1 ) 
					tmpVarList.add(expr.genJavaExpr(pw, env));
				++i;
			}
			
		}
		else {
			/*
			 *     public fun init: (T1 g1, T2 g2, T3 g3, T4 g4, T5 g5, T6 g6) 
			 */
			for ( Expr expr : exprList ) {
				tmpVarList.add(expr.genJavaExpr(pw, env));
			}
			
		}		
		
		String javaTupleType = getType(env).getJavaName(); 
		pw.printIdent(javaTupleType + " " + literalTupleTmpVar + " = new " + javaTupleType +  
				  // "." + NameServer.getJavaNameOfMethodWith("new", tmpVarList.size()) + 
				  "(");

		int size = tmpVarList.size();
		for ( String strExpr : tmpVarList ) {
			pw.print(strExpr);
			if ( --size > 0 )
				pw.print(", ");
		}
		pw.println(");");
		return literalTupleTmpVar;
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
		
		
		if ( isNamedTuple ) {
			/*
			int i = 0;
			for ( Expr expr : exprList ) {
				if ( i%2 == 1 ) {  
					expr.calcInternalTypes(env);
					Type t = expr.getType(env);
					if ( ! (t instanceof ProgramUnit) )
						env.error(expr.getFirstSymbol(), "The type of this expression should be a Cyan prototype", 
								expr.asString(), ErrorKind.prototype_as_type_expected_inside_method);
				}
				else if ( ! (expr instanceof ExprIdentStar ) ) {
					env.error(expr.getFirstSymbol(), "Field name expected", expr.asString(), ErrorKind.tuple_field_name_expected);
				}
				++i;
			} */
			Symbol sym = this.getFirstSymbol();

			SymbolIdent symbolIdent = new SymbolIdent(Token.IDENT, "Tuple", sym.getStartLine(), 
					sym.getLineNumber(), sym.getColumnNumber(), sym.getOffset(), sym.getCompilationUnit() );
			

			ExprIdentStar typeIdent = new ExprIdentStar(symbolIdent);
			ArrayList<ArrayList<Expr>> realTypeListList = new ArrayList<ArrayList<Expr>>(); 
			ArrayList<Expr> realTypeList = new ArrayList<Expr>();
			
			int i = 0;
			for ( Expr expr : exprList ) {
				if ( i%2 == 0 ) {
					if ( ! (expr instanceof ExprIdentStar ) ) {
						env.error(true, expr.getFirstSymbol(), "Field name expected", expr.asString(), ErrorKind.tuple_field_name_expected);
					}					
					realTypeList.add(expr);
				}
				else {
					expr.calcInternalTypes(env);
					Type typeExpr = expr.getType(env);
					if ( ! (typeExpr instanceof ProgramUnit) && typeExpr != Type.Dyn )
						env.error(true, expr.getFirstSymbol(), "The type expression '" + expr.asString() + "' should be a Cyan prototype", null, ErrorKind.prototype_as_type_expected_inside_method);
					//ProgramUnit pu = (ProgramUnit ) typeExpr;
					realTypeList.add( typeExpr.asExpr(this.getFirstSymbol()) ) ; // pu.asExpr(this.getFirstSymbol()));
				}
				++i;
			}
			realTypeListList.add(realTypeList);
			
			ExprGenericPrototypeInstantiation gpi = new ExprGenericPrototypeInstantiation( typeIdent, 
					realTypeListList, env.getCurrentProgramUnit(), null); 		
			type = CompilerManager.createGenericPrototype(gpi, env);			
			
		}
		else {
			
			Symbol sym = this.getFirstSymbol();

			SymbolIdent symbolIdent = new SymbolIdent(Token.IDENT, "Tuple", sym.getStartLine(), 
					sym.getLineNumber(), sym.getColumnNumber(), sym.getOffset(), sym.getCompilationUnit() );
			ExprIdentStar typeIdent = new ExprIdentStar(symbolIdent);
			ArrayList<ArrayList<Expr>> realTypeListList = new ArrayList<ArrayList<Expr>>(); 
			ArrayList<Expr> realTypeList = new ArrayList<Expr>();
			
			
			for ( Expr expr : exprList ) {
				expr.calcInternalTypes(env);
				Type typeExpr = expr.getType(env);
				if ( ! (typeExpr instanceof ProgramUnit) && typeExpr != Type.Dyn )
					env.error(true, expr.getFirstSymbol(), "An expression of a Cyan prototype was expected", null, ErrorKind.prototype_as_type_expected_inside_method);
				if ( typeExpr == Type.Dyn ) {
					ArrayList<Symbol> identSymbolArray = new ArrayList<>();
					Symbol first = this.getFirstSymbol();
					// insert the program unit name
					identSymbolArray.add( new SymbolIdent(Token.IDENT, "Dyn", first.getStartLine(), 
							first.getLineNumber(), first.getColumnNumber(), first.getOffset(), first.getCompilationUnit()) );
					ExprIdentStar newIdentStar = new ExprIdentStar(identSymbolArray, null);					
					realTypeList.add(newIdentStar);
				}
				else {
					ProgramUnit pu = (ProgramUnit ) typeExpr;
					realTypeList.add(pu.asExpr(this.getFirstSymbol()));
				}
			}
			realTypeListList.add(realTypeList);
			
			ExprGenericPrototypeInstantiation gpi = new ExprGenericPrototypeInstantiation( typeIdent, 
					realTypeListList, env.getCurrentProgramUnit(), null); 		
			type = CompilerManager.createGenericPrototype(gpi, env);			
			
		}
		super.calcInternalTypes(env);

	}

	@Override
	public Object getJavaValue() {
		
		Object []objArray = new Object[exprList.size()];
		int i = 0;
		if ( isNamedTuple ) {
			int j = 0;
			for ( Expr e : exprList ) {
				if ( j%2 == 0 && e instanceof ExprAnyLiteral ) {
				    objArray[i] = ((ExprAnyLiteral ) e).getJavaValue();
				    ++i;
				}
				++j;
			}
			
		}
		else {
			for ( Expr e : exprList ) {
				if ( e instanceof ExprAnyLiteral ) {
				    objArray[i] = ((ExprAnyLiteral ) e).getJavaValue();
				    ++i;
				}
			}
			
		}
		return objArray;
	}
	
	public boolean getIsNamedTuple() {
		return isNamedTuple;
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
			
			if ( isNamedTuple ) {
				int j = 0;
				for ( Expr e : exprList ) {
					if ( j%2 == 0 && e instanceof ExprAnyLiteral ) {
						s.append(  ((ExprAnyLiteral ) e).getStringJavaValue() );
						if ( --size > 0 ) 
							s.append(", ");
						--size;
					}
					++j;
				}
			}
			else {
				size = exprList.size();
				for ( Expr e : exprList ) {
					if ( e instanceof ExprAnyLiteral ) {
						s.append(  ((ExprAnyLiteral ) e).getStringJavaValue() );
						if ( --size > 0 ) 
							s.append(", ");
					}
				}
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
	
	
	
	/**
	 * symbols representing {# and #}
	 */
	private Symbol startSymbol, endSymbol;
	private ArrayList<Expr> exprList;

	private boolean	isNamedTuple;

}
