package ast;

import java.util.ArrayList;
import lexer.Symbol;
import saci.CyanEnv;
import saci.Env;

public class StatementNotNil extends Statement {


	// 	return new StatementIf(notNilSymbol, notNilExprList, ifStatementList, elseStatementList);

	
	/*
		return new StatementNotNil(notNilSymbol, notNilVariableSymbolList, notNilTypeList, 
				notNilStatementList,
                elseStatementList, rightCBEndsIf, lastElse);
	 * 
	 */
	public StatementNotNil(Symbol notNilSymbol, ArrayList<NotNilRecord> notNilRecordList,
						StatementList notNilStatementList,
                        StatementList elseStatementList, Symbol rightCBEndsIf, Symbol lastElse) {
		this.notNilSymbol = notNilSymbol;
		this.notNilRecordList = notNilRecordList;
		this.notNilStatementList = notNilStatementList;
		this.elseStatementList = elseStatementList;
		this.rightCBEndsIf = rightCBEndsIf;
		this.lastElse = lastElse;
	}

	@Override
	public void accept(ASTVisitor visitor) {
		
		for ( NotNilRecord notNilRecord : notNilRecordList ) {
			if ( notNilRecord.typeInDec != null ) {
				notNilRecord.typeInDec.accept(visitor);
			}
			notNilRecord.localVar.accept(visitor);
			notNilRecord.expr.accept(visitor);
		}
		notNilStatementList.accept(visitor);

		if ( elseStatementList != null ) 
			elseStatementList.accept(visitor);
		
		visitor.visit(this);
	}	
	

	/* (non-Javadoc)
	 * @see ast.Statement#genCyan(ast.PWInterface, boolean)
	 */
	@Override
	public void genCyanReal(PWInterface pw, boolean printInMoreThanOneLine, CyanEnv cyanEnv, boolean genFunctions) {

		printInMoreThanOneLine = true;
		pw.printIdent("notNil ");
		int size = this.notNilRecordList.size();
		for ( NotNilRecord rec : this.notNilRecordList ) {
			if ( rec.typeInDec != null ) {
				rec.typeInDec.genCyanReal(pw, printInMoreThanOneLine, cyanEnv, genFunctions);
				pw.print(" ");
			}
			pw.print(rec.localVar.getName() + " = ");
			rec.expr.genCyanReal(pw, printInMoreThanOneLine, cyanEnv, genFunctions);
			if ( --size > 0 ) {
				pw.print(", ");
			}
		}
		pw.println(" { ");
		pw.add();
		this.notNilStatementList.genCyan(pw, printInMoreThanOneLine, cyanEnv, genFunctions);
		pw.sub();
		pw.printlnIdent("}");
		if ( elseStatementList != null ) {
			if ( printInMoreThanOneLine )
				pw.printlnIdent("else {");
			else
				pw.print(" else {");
			pw.add();
			elseStatementList.genCyan(pw, printInMoreThanOneLine, cyanEnv, genFunctions);
			pw.sub();
			if ( printInMoreThanOneLine )
				pw.printlnIdent("}");
			else
				pw.print(" }");
		}

	}


	@Override
	public Symbol getFirstSymbol() {
		return notNilSymbol;
	}



	@Override
	public void genJava(PWInterface pw, Env env) {
		/*
		 *   notNil T1 v1 = e1, v2 = e2, T3 v3 = e3 {
                 S1
             }
             else {
                 S2
             }

			 {
                 T1 v1 = null; T2 v2 = null; T3 v3 = null;

                 tmp_e1 = e1.genJavaExpr(...);
                 if ( tmp_e1 != Nil ) {
                     v1 = tmp_e1._elem;
                     tmp_e2 = e2.genJavaExpr(...);
                     if ( tmp_e2 != Nil ) {
                         v2 = tmp_e2._elem;
                         tmp_e3 = e3.genJavaExpr(...);
                         if ( tmp_e3 != Nil ) {
                             v3 = tmp_e3._elem;
                         }
                     }
                 }
                 if ( v1 != null && v2 != null && v3 != null ) {
                     S1
                 else {
                     S2
                 }
                 
			 }

		String boolTmp = NameServer.nextJavaLocalVariableName();
		pw.printlnIdent("boolean " + boolTmp + " = false;");
		 * 
		 */

		pw.printlnIdent("{");
		pw.add();
		   //   T1 v1 = null; T2 v2 = null; T3 v3 = null;

		for ( NotNilRecord rec : this.notNilRecordList ) {
	        //String variableName = rec.localVar.getName();

	        String javaNameVar = rec.localVar.getJavaName(); // NameServer.getJavaName(variableName);
	        String javaTypeName = rec.localVar.getType().getJavaName();
	        if ( rec.localVar.getRefType() )
	            pw.printIdent("Ref<" + javaTypeName + ">");
	        else
	            pw.printIdent(javaTypeName);
	        pw.println(" " + javaNameVar + " = null;");
			
		}
		for ( NotNilRecord rec : this.notNilRecordList )  {

			/*
                 tmp_e1 = e1.genJavaExpr(...);
                 if ( tmp_e1 != Nil ) {
                     v1 = tmp_e1._elem;
			 * 
			 */
			String tmpVarString = rec.expr.genJavaExpr(pw, env);
			
			pw.printlnIdent("if ( " + tmpVarString + "._elem != _Nil.prototype ) {");
			
			pw.add();
			//       v1 = tmp_e1._elem;

	        //String variableName = rec.localVar.getName();

	        String javaNameVar = rec.localVar.getJavaName(); // NameServer.getJavaName(variableName);
	        String javaTypeName = rec.localVar.getType().getJavaName();


	        String tmpExpr = rec.expr.genJavaExpr(pw, env);

	        if ( rec.localVar.getRefType() ) {
	            pw.print(javaNameVar + " = new Ref<" + javaTypeName + ">();");
	        }

            if ( rec.localVar.getRefType() )
                javaNameVar = javaNameVar + ".elem";
            pw.printlnIdent(javaNameVar + " = (" + javaTypeName + " ) " + tmpExpr + "._elem;");
	    			
			
		}
		for ( NotNilRecord rec : this.notNilRecordList ) {
			pw.sub();
			pw.printlnIdent("}");
		}
		/*
                 if ( v1 != null && v2 != null && v3 != null ) {
                     S1
                 else {
                     S2
                 }
		 * 
		 */
		pw.printIdent("if ( ");
		int size = notNilRecordList.size();
		for ( NotNilRecord rec : this.notNilRecordList ) {
	        //String variableName = rec.localVar.getName();

	        String javaNameVar = rec.localVar.getJavaName(); // NameServer.getJavaName(variableName);
			pw.print(javaNameVar + " != null ");
			if ( --size > 0 ) {
				pw.print("&& ");
			}
			
		}
		pw.println(" ) {");
		pw.add();
		this.notNilStatementList.genJava(pw, env);
		pw.sub();
		pw.printlnIdent("}");

		if ( elseStatementList != null ) {
			pw.printlnIdent("else {");
			pw.add();
			elseStatementList.genJava(pw, env);
			pw.sub();
			pw.printlnIdent("}");
		}
		
		pw.sub();
		pw.printlnIdent("}");
	}
		



	
	@Override
	public void calcInternalTypes(Env env) {
		/*
             notNil T1 v1 = e1, v2 = e2, T3 v3 = e3 {
                 S1
             }
             else {
                 S2
             }
		 * 
		 */

		
		env.addLexicalLevel();
		int numLocalVariables = env.numberOfLocalVariables();
		
 		for ( NotNilRecord rec : this.notNilRecordList ) {
 			// [ typeInDec ] localVar = expr
 			
 			
 			
 			rec.expr.calcInternalTypes(env);

			if ( ! (rec.expr.getType() instanceof ObjectDec) ) {
				env.error(rec.expr.getFirstSymbol(), "An expression of type union was expected");
			}
 			
			ObjectDec proto = (ObjectDec ) rec.expr.getType();
			if ( ! proto.getName().startsWith("Union<") ) {
				env.error(rec.expr.getFirstSymbol(), "An expression of type union was expected");
			}
			ArrayList<GenericParameter> gpList = proto.getGenericParameterListList().get(0);
			if ( gpList.size() != 2 ) {
				env.error(rec.expr.getFirstSymbol(), "An expression of type Union<T, Nil> or Union<Nil, T> was expected");
			}
			String first  = gpList.get(0).getName();
			String second = gpList.get(1).getName();
			
			boolean isTaggedUnion = (Character.isLowerCase(first.charAt(0)) && first.indexOf('.') < 0) || 
					(Character.isLowerCase(second.charAt(0)) && second.indexOf('.') < 0);
			if ( isTaggedUnion ) {
				env.error(rec.expr.getFirstSymbol(), "'notNil' cannot be used with tagged unions");
			}
			
			
			boolean firstEqualsNil = first.equals("Nil");
			if ( !firstEqualsNil && !second.equals("Nil") ) {
				env.error(rec.expr.getFirstSymbol(), "An expression of type Union<T, Nil> or Union<Nil, T> was expected");
			}
 			Type exprNotNilType;
 			if ( firstEqualsNil ) {
 				exprNotNilType = gpList.get(1).getParameter().getType();
 			}
 			else {
 				exprNotNilType = gpList.get(0).getParameter().getType();
 			}
 			
 			
 			if ( rec.typeInDec != null ) {
 				rec.typeInDec.calcInternalTypes(env);
 				Type decTypeVar = rec.typeInDec.ifRepresentsTypeReturnsType(env);
 				if ( exprNotNilType != decTypeVar ) {
 					String decTypeVarName = decTypeVar.getFullName();
 					env.error(rec.expr.getFirstSymbol(), "The type of this expression should be "
 							+ decTypeVarName + "|Nil or " + decTypeVarName + "|Nil");
 					
 				}
 			}
 			rec.localVar.setType(exprNotNilType);
 			
 			String nameVar = rec.localVar.getName();
 			VariableDecInterface otherVar = env.searchLocalVariableParameter(nameVar);
 			if ( otherVar != null ) {
 				env.error(this.getFirstSymbol(), "Variable '" + nameVar + "' is being redeclared. The other declaration is in line " 
 						+ otherVar.getVariableSymbol().getLineNumber());
 			}

 			env.pushVariableDec(rec.localVar);
 			env.pushVariableAndLevel(rec.localVar, rec.localVar.getVariableSymbol().symbolString);
 			
		}
		
		
		this.notNilStatementList.calcInternalTypes(env);
		
		
		
		int numLocalVariablesToPop = env.numberOfLocalVariables() - numLocalVariables;
		
		env.popNumLocalVariableDec(numLocalVariablesToPop); 
		
		env.removeVariablesLastLevel();
		env.subLexicalLevel();
		
		if ( elseStatementList != null ) {
			elseStatementList.calcInternalTypes(env);
			numLocalVariablesToPop = env.numberOfLocalVariables() - numLocalVariables;
			
			env.popNumLocalVariableDec(numLocalVariablesToPop); //parameterList.size());			
		}
		super.calcInternalTypes(env);		
		
	}

	public StatementList getElseStatementList() {
		return elseStatementList;
	}


	@Override
	public boolean alwaysReturn() {
		if ( elseStatementList == null )
			//  without 'else', may not return
			return false;
		else {
			if ( ! this.notNilStatementList.alwaysReturn() ) {
				return false;
			}
			return elseStatementList.alwaysReturn();
		}
		
	}
	
	public boolean alwaysReturnFromFunction() {
		if ( elseStatementList == null )
			//  without 'else', may not return
			return false;
		else {
			if ( ! this.notNilStatementList.alwaysReturnFromFunction() ) {
				return false;
			}
			return elseStatementList.alwaysReturnFromFunction();
		}
		
	}

	
	@Override
	public boolean statementDoReturn() {
		return alwaysReturnFromFunction();
	}	


	public Symbol getRightCBEndsIf() {
		return rightCBEndsIf;
	}
	
	private ArrayList<NotNilRecord> notNilRecordList;
	/**
	 * the symbol 'notNil'
	 */
	private Symbol notNilSymbol;
	/**
	 * list of statements to be executed if the expressions are not Nil
	 */
	private StatementList notNilStatementList;
    
	/**
	 * list of else statements
	 */
	private StatementList  elseStatementList;

	/**
	 * the '}' symbol that ends an notNil
	 */
	private Symbol rightCBEndsIf;
	/**
	 * the last 'else' symbol of an 'if' statement. Of null if none
	 */
	private Symbol lastElse;

	public Symbol getLastElse() {
		return lastElse;
	}
}
