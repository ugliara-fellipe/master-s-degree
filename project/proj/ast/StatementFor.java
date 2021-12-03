package ast;

import java.util.ArrayList;
import lexer.Symbol;
import saci.CyanEnv;
import saci.Env;
import saci.NameServer;

public class StatementFor extends Statement {


	public StatementFor(Symbol forSymbol, Expr typeInDec, StatementLocalVariableDec localVariableDec, Expr forExpression, 
			StatementList statementList, Symbol rightCBEndsIf) {
		this.forSymbol = forSymbol;
		this.typeInDec = typeInDec;
		this.localVariableDec = localVariableDec;
		this.forExpression = forExpression;
		this.statementList = statementList;
		this.rightCBEndsIf = rightCBEndsIf;
	}

	@Override
	public void accept(ASTVisitor visitor) {
		this.forExpression.accept(visitor);
		this.localVariableDec.accept(visitor);
		this.statementList.accept(visitor);
		visitor.visit(this);
	}	
	
	@Override
	public void genCyanReal(PWInterface pw, boolean printInMoreThanOneLine, CyanEnv cyanEnv, boolean genFunctions) {
		pw.print("for " + localVariableDec.getName());
		pw.print(" in ");
		forExpression.genCyan(pw, printInMoreThanOneLine, cyanEnv, genFunctions);
		pw.println(" {");
		pw.add();
		statementList.genCyan(pw, printInMoreThanOneLine, cyanEnv, genFunctions);
		pw.sub();
		pw.printlnIdent("}");
	}

	@Override
	public Symbol getFirstSymbol() {
		return forSymbol;
	}

	@Override
	public void genJava(PWInterface pw, Env env) {
		
		pw.add();
		pw.printlnIdent("{");
		if ( localVariableDec.getType() == Type.Dyn ) {
			/*
			 *  // var Dyn s = expression;
			 * for s in container {
			 * }
			 * code for calling _iterator. The result is in variable nameVarReturnedValue
	        CyByte tmp141 = _container._iterator();
	        while ( tmp141._hasNext().b ) {
	         	_s = _container._next();
	         	// statement code
	        }
			 * 
			 */
			localVariableDec.genJava(pw, env);
			String tmpForExpression = forExpression.genJavaExpr(pw, env);
			
			String nameVarIterator = Statement.genJavaDynamicUnaryMessageSend(pw, tmpForExpression, "_iterator", env, 
					forExpression.getFirstSymbol().getLineNumber());
			pw.printlnIdent("while ( true ) { ");
			pw.add();
			String nameVarHasNextResult = Statement.genJavaDynamicUnaryMessageSend(pw, nameVarIterator, "_hasNext",
					env, localVariableDec.getFirstSymbol().getLineNumber());
			// add convertion from Dyn
			pw.printlnIdent("if ( !(" + nameVarHasNextResult + " instanceof CyBoolean) ) {");
			pw.add();
			
			pw.printlnIdent("throw new ExceptionContainer__("
					+ env.javaCodeForCastException(localVariableDec, Type.Boolean) + " );");
			
			pw.sub();
			pw.printlnIdent("}");
			pw.printlnIdent("if ( ! ((CyBoolean ) " + nameVarHasNextResult + ").b ) break;");

			String nameVarNextResult = Statement.genJavaDynamicUnaryMessageSend(pw, nameVarIterator, "_next",
					env, localVariableDec.getFirstSymbol().getLineNumber());
			
			pw.printIdent(localVariableDec.getJavaName());
			if ( localVariableDec.getRefType() ) {
				pw.print(".elem");
			}
			pw.println(" = " + nameVarNextResult + ";");

	 		if ( statementList != null )
			    statementList.genJava(pw, env);
			pw.sub();
			pw.printlnIdent("}");
			
		}
		else {
			/*
			 * for s in container {
			 * }
	        CyByte tmp141 = _container._iterator();
	        while ( tmp141._hasNext().b ) {
	         	_s = _container._next();
	         	// statement code
	        }
			 * 
			 */
			
			localVariableDec.genJava(pw, env);
			String tmpForExpression = forExpression.genJavaExpr(pw, env);
			String tmpIterator = NameServer.nextJavaLocalVariableName();
			// forExpression.getType()
			pw.printlnIdent( this.returnTypeInteratorMethod.getJavaName() + " " +  tmpIterator + " = " + 
			    tmpForExpression + "._iterator();");
			pw.printlnIdent("while ( " + tmpIterator + "._hasNext().b ) { ");
			pw.add();
			pw.printIdent(localVariableDec.getJavaName());
			if ( localVariableDec.getRefType() ) {
				pw.print(".elem");
			}
			pw.println(" = " + tmpIterator + "._next();");
	 		if ( statementList != null )
			    statementList.genJava(pw, env);
			pw.sub();
			pw.printlnIdent("}");
		}
		pw.sub();
		pw.printlnIdent("}");
	}



	
	@Override
	public void calcInternalTypes(Env env) {
		
		
		if ( typeInDec != null ) {
			typeInDec.calcInternalTypes(env);
		}
		
		env.addLexicalLevel();
		
		int numLocalVariables = env.numberOfLocalVariables();
		
		
		String name = localVariableDec.getName();
		if ( env.searchLocalVariableParameter(name) != null ) {
			env.error(localVariableDec.getFirstSymbol(), "Variable is being redeclared. A 'for' variable cannot have been previously declared in the same method");
		}
		forExpression.calcInternalTypes(env);
		Type t = forExpression.getType();
		Type elemType = Type.Dyn;

		/*
		 * do not demand method 'iterator' if the type is Dyn
		 */
		if ( t != Type.Dyn ) {
			ArrayList<MethodSignature> msList = t.searchMethodProtectedPublicSuperProtectedPublic("iterator", env);
			if ( msList == null || msList.size() == 0 ) {
				env.error(forExpression.getFirstSymbol(), "This expression should have a method 'iterator -> Iterator<T>'");
				return ;
			}
			MethodSignature ms = msList.get(0);
			Type returnType = ms.getReturnType(env);
			if ( returnType instanceof InterfaceDec ) {
				this.returnTypeInteratorMethod = (InterfaceDec ) returnType;
				ArrayList<ArrayList<GenericParameter>> gpListList = returnTypeInteratorMethod.getGenericParameterListList();
				if ( gpListList != null && gpListList.size() == 1 ) {
					ArrayList<GenericParameter> gpList = gpListList.get(0);
					if ( gpList.size() == 1 ) {
						elemType = gpList.get(0).getParameter().getType();
					}
				}
			}
			if ( elemType == null ) {
				env.error(forExpression.getFirstSymbol(), "This expression should have a method 'iterator -> Iterator<T>'");
				return ;
			}
		}
		localVariableDec.setType(elemType);
		
		if ( typeInDec != null ) {
			Type declaredVarType = typeInDec.ifRepresentsTypeReturnsType(env);
			if ( elemType !=  declaredVarType ) {
				env.error(localVariableDec.getFirstSymbol(), "According to the 'for' expression the "
						+ "type of this variable should be '" + localVariableDec.getType().getFullName() + "' "
								+ "but it is declared with type '" + declaredVarType.getFullName() + "'");
			}
		}
		
		env.pushVariableDec(localVariableDec);
		
		env.pushVariableAndLevel(localVariableDec, name);
		
		statementList.calcInternalTypes(env);
		
		
		int numLocalVariablesToPop = env.numberOfLocalVariables() - numLocalVariables;
		
		env.popNumLocalVariableDec(numLocalVariablesToPop); 
		
		env.removeVariablesLastLevel();
		env.subLexicalLevel();
		
		super.calcInternalTypes(env);
		
	}

	public StatementList getStatementList() {
		return statementList;
	}
	
	public Symbol getRightCBEndsIf() {
		return rightCBEndsIf;
	}
	
	public Expr getTypeInDec() {
		return typeInDec;
	}

	private Symbol forSymbol;
	private Expr forExpression;
	private Expr typeInDec;
	private StatementList statementList;
	private StatementLocalVariableDec localVariableDec;
	private InterfaceDec returnTypeInteratorMethod;
	/**
	 * the '}' symbol that ends a for
	 */
	private Symbol rightCBEndsIf;

}
