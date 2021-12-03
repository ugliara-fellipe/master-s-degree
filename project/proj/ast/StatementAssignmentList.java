package ast;

import java.util.ArrayList;
import error.ErrorKind;
import lexer.Symbol;
import meta.CyanMetaobjectWithAt;
import meta.IActionAssignment_cge;
import meta.MetaInfoServer;
import saci.CyanEnv;
import saci.Env;
import saci.NameServer;
import saci.Tuple2;


/**
 * Represents a list of assignments such as
 *      a = b[0] = self.c = 0;
 *
 * A single assignment also represented by an object of
 * StatementAssignmentList.
 *
 * exprList will contain "a", "b[0]", "self.c", and "0" in the above
 * statement.
 *
 * @author José
 *
 */
public class StatementAssignmentList extends Statement {

	public StatementAssignmentList() {
		exprList = new ArrayList<Expr>();
	}

	@Override
	public void accept(ASTVisitor visitor) {
		visitor.visit(this);
	}	
	
	public void add(Expr expr) {
		exprList.add(expr);
	}

	public void setExprList(ArrayList<Expr> exprList) {
		this.exprList = exprList;
	}

	public ArrayList<Expr> getExprList() {
		return exprList;
	}


	@Override
	public void genCyanReal(PWInterface pw, boolean printInMoreThanOneLine, CyanEnv cyanEnv, boolean genFunctions) {
		int n = exprList.size();
		for ( Expr e : exprList ) {
			e.genCyan(pw, false, cyanEnv, genFunctions);
			n--;
			if ( n > 0 ) 
				pw.print(" = ");
		}
	}

	

	@Override
	public void genJava(PWInterface pw, Env env) {

		pw.printIdent("/* ");
		genCyan(pw, false, NameServer.cyanEnv, true);
		pw.println(" */");
		
		Expr rightExpr = exprList.get(exprList.size() - 1);
   		Type rightType = rightExpr.getType();
		Expr leftSide = exprList.get(exprList.size() - 2);
   		Type leftType = leftSide.getType();

		Tuple2<IActionAssignment_cge, ObjectDec> cyanMetaobjectPrototype = MetaInfoServer.getChangeAssignmentCyanMetaobject(env, leftType);
		IActionAssignment_cge changeCyanMetaobject = null;
        ObjectDec prototypeFoundMetaobject = null;
        if ( cyanMetaobjectPrototype != null ) {
        	changeCyanMetaobject = cyanMetaobjectPrototype.f1;
        	prototypeFoundMetaobject = cyanMetaobjectPrototype.f2;
        }
        
   		String rightExprTmpVar = rightExpr.genJavaExpr(pw, env);
   		String rightExprTmpVarOriginal = rightExprTmpVar; 
        
   		/*
   		 * cases to consider:
   		 *     a = expr;  // a non-ref
   		 *     a = expr;  // a is a ref-variable
   		 *     a[i] = expr;  // a non-ref
   		 *     a[i] = expr;  // a is a ref-variable
   		 *     
   		 *     
   		 *     a = expr;  // a non-ref, 'a' has type Dyn
   		 *     a = expr;  // a is a ref-variable, 'a' has type Dyn
   		 *     a[i] = expr;  // a non-ref, 'a' has type Dyn
   		 *     a[i] = expr;  // a is a ref-variable, 'a' has type Dyn
   		 *     a[i] = expr;  // a non-ref, 'i' has type Dyn
   		 *     a[i] = expr;  // a is a ref-variable, 'i' has type Dyn
   		 *     
   		 *     In any way, expr may have type Dyn or not and i may be ref or non-ref
   		 *     
   		 */
        
        if ( leftSide instanceof ExprIndexed ) {
       		/*
       		 * cases to consider:
       		 *     a[i] = expr;  // a non-ref
       		 *     a[i] = expr;  // a is a ref-variable
       		 *     a[i] = expr;  // a non-ref, 'a' has type Dyn. The type of 'i' does not matter
       		 *     a[i] = expr;  // a is a ref-variable, 'a' has type Dyn. The type of 'i' does not matter
       		 *     a[i] = expr;  // a non-ref, 'i' has type Dyn
       		 *     a[i] = expr;  // a is a ref-variable, 'i' has type Dyn
       		 *     In any way, expr may have type Dyn or not
       		 */
        	ExprIndexed leftSideIndexedExpr = (ExprIndexed ) leftSide;
        	Expr indexOfExpr = leftSideIndexedExpr.getIndexOfExpr();
        	Expr indexedExpr = leftSideIndexedExpr.getIndexedExpr();

        	String indexedExprTmpVar = indexedExpr.genJavaExpr(pw, env);
        	String indexOfExprTmpVar = indexOfExpr.genJavaExpr(pw, env);
        	
        	/*boolean nilSafeIndexing = exprIndexed.getFirstIndexOperator().token == Token.INTER_LEFTSB;
        	if ( nilSafeIndexing ) {
        		pw.printlnIdent("if ( " + indexedExprTmpVar + " != " + NameServer.NilInJava + " && " + 
        	        indexedExprTmpVar + " != null ) {");
        		pw.add();
        	}
        	*/
			//MethodSignatureWithSelectors ms = leftSideIndexedExpr.getIndexingMethod();
			Type mustBeTypeIndex = leftSideIndexedExpr.getDeclaredType_at_Parameter();
			Type mustBeRightExprType = leftSideIndexedExpr.getDeclaredType_put_Parameter();
			
			/*
			 * a[i] = expr;
			 * a at: i put: expr;
			 * The method to be called is leftSideIndexedExpr.getIndexingMethod() whose declared type of 
			 * parameter of at: is 
			 *     leftSideIndexedExpr.getIndexingMethod().getParameterList().get(0).getType(env)
			 * The declared type of parameter of put: is
			 *     leftSideIndexedExpr.getIndexingMethod().getParameterList().get(1).getType(env)
			 */

   			
   			// a[i] = expr, 'a' non-Dyn, 'i' non-Dyn, 
   			if ( indexedExpr.getType() != Type.Dyn && indexOfExpr.getType() != Type.Dyn ) {
   				String s = rightExprTmpVar;
   				if ( rightType == Type.Dyn ) {
   					// rightType != Type.Dyn 
        			pw.printlnIdent("if ( !(" + rightExprTmpVar + " instanceof " + mustBeRightExprType.getJavaName() + " ) ) ");
        			
					pw.printlnIdent("    throw new ExceptionContainer__("
							+ env.javaCodeForCastException(rightExpr, mustBeRightExprType) + " );");
					
        			
        			s = "((" + mustBeRightExprType.getJavaName() + " ) " + s + ")";
   				}
   				
   				if ( changeCyanMetaobject != null ) {
   					
					try {
	   					s = changeCyanMetaobject.cge_changeRightHandSideTo(
	   	           				prototypeFoundMetaobject, 
	   	           				s, rightExpr.getType(env));
						
						
					} 
					catch ( error.CompileErrorException e ) {
					}
					catch ( RuntimeException e ) {
						CyanMetaobjectAnnotation annotation = ((CyanMetaobjectWithAt) changeCyanMetaobject).getMetaobjectAnnotation();
						env.thrownException(annotation, annotation.getFirstSymbol(), e);
					}
					finally {
						env.errorInMetaobject( (meta.CyanMetaobject ) changeCyanMetaobject, this.getFirstSymbol());
					}
   					
   					
   				}
   				
   				/*
                if ( leftType == Type.Any && rightType instanceof InterfaceDec ) {
               		pw.println(" = (" + NameServer.AnyInJava + " ) " + rightExprTmpVar + ";");
                }
                else {
               		pw.println(" = " + rightExprTmpVar + ";");
                }
   				 * 
   				 */
   				
            	
                if ( mustBeTypeIndex == Type.Any && indexOfExpr.getType() instanceof InterfaceDec ) {
                	indexOfExprTmpVar = " (" + NameServer.AnyInJava + " ) " + indexOfExprTmpVar;
                }
                
                if ( mustBeRightExprType == Type.Any && rightType instanceof InterfaceDec ) {
                	s = " (" + NameServer.AnyInJava + " ) " + s;
                }
            	
            	pw.printlnIdent(indexedExprTmpVar + "." + NameServer.javaNameAtPutMethod + "(" + indexOfExprTmpVar + ", " 
            			+ s + ");");
            	
   			}
   			else if ( indexedExpr.getType() != Type.Dyn && indexOfExpr.getType() == Type.Dyn  ) {
       			// a[i] = expr, 'a' non-Dyn, 'i' Dyn

   				/*
   				 * try to cast the index 'i' to the correct type
   				 */

    			pw.printlnIdent("if ( !(" + indexOfExprTmpVar + " instanceof " + mustBeTypeIndex.getJavaName() + " ) ) ");
    			
				pw.printlnIdent("    throw new ExceptionContainer__("
						+ env.javaCodeForCastException(indexOfExpr, mustBeTypeIndex) + " );");
    			
    	    			
    			/*
    			 * cast rightExpr to the correct type
    			 */
   				String s = rightExprTmpVar;
   				if ( rightType == Type.Dyn ) {
   					// rightType != Type.Dyn 
        			pw.printlnIdent("if ( !(" + rightExprTmpVar + " instanceof " + mustBeRightExprType.getJavaName() + " ) ) ");
        			
					pw.printlnIdent("    throw new ExceptionContainer__("
							+ env.javaCodeForCastException(rightExpr, mustBeRightExprType) + " );");
        			
        			
        			s = "((" + mustBeRightExprType.getJavaName() + " ) " + s + ")";
   				}
   				
   				if ( changeCyanMetaobject != null ) {
   					
					try {
	   					s = changeCyanMetaobject.cge_changeRightHandSideTo(
	   	           				prototypeFoundMetaobject, 
	   	           				s, rightType);
					} 
					catch ( error.CompileErrorException e ) {
					}
					catch ( RuntimeException e ) {
						CyanMetaobjectAnnotation annotation = ((CyanMetaobjectWithAt) changeCyanMetaobject).getMetaobjectAnnotation();
						env.thrownException(annotation, annotation.getFirstSymbol(), e);
					}
					finally {
	   					env.errorInMetaobject( (meta.CyanMetaobject ) changeCyanMetaobject, this.getFirstSymbol());
					}
   					
   					
   				}
   				
                if ( mustBeTypeIndex == Type.Any && indexOfExpr.getType() instanceof InterfaceDec ) {
                	indexOfExprTmpVar = " (" + NameServer.AnyInJava + " ) " + indexOfExprTmpVar;
                }
                
                if ( mustBeRightExprType == Type.Any && rightType instanceof InterfaceDec ) {
                	s = " (" + NameServer.AnyInJava + " ) " + s;
                }
            	
   				
   				
            	pw.printlnIdent(indexedExprTmpVar + "." + NameServer.javaNameAtPutMethod + "( (" + mustBeTypeIndex.getJavaName()  + " ) " 
            	    + indexOfExprTmpVar + ", " 
            			+ s + ");");
   			}
   			else {
   				//  indexedExpr.getType() == Type.Dyn
   				
				String aMethodTmp = NameServer.nextJavaLocalVariableName();
				
				pw.printlnIdent("java.lang.reflect.Method " + aMethodTmp + " = CyanRuntime.getJavaMethodByName(" + indexedExprTmpVar + 
						".getClass(), \"" + 
						NameServer.javaNameAtPutMethod  + "\", 2);");
				int lineNumber = indexedExpr.getFirstSymbol().getLineNumber();
				pw.printlnIdent("if ( " + aMethodTmp + " == null ) throw new ExceptionContainer__( new _ExceptionMethodNotFound( new CyString(\"Method called at line \" + " + lineNumber +  
				     "+ \" of prototype '" + env.getCurrentProgramUnit().getFullName() + "' was not found\") ) );");
				String resultTmpVar = NameServer.nextJavaLocalVariableName();
				pw.printlnIdent("Object " + resultTmpVar + " = null;");
				pw.printlnIdent("try {");
				pw.add();
				
				
   				if ( changeCyanMetaobject != null ) {
   					
					try {
	   					rightExprTmpVar = changeCyanMetaobject.cge_changeRightHandSideTo(
	   	           				prototypeFoundMetaobject, 
	   	           				rightExprTmpVar, rightExpr.getType(env));
	   											
					} 
					catch ( error.CompileErrorException e ) {
					}
					catch ( RuntimeException e ) {
						CyanMetaobjectAnnotation annotation = ((CyanMetaobjectWithAt) changeCyanMetaobject).getMetaobjectAnnotation();
						env.thrownException(annotation, annotation.getFirstSymbol(), e);
					}	
					finally {
	   					env.errorInMetaobject( (meta.CyanMetaobject ) changeCyanMetaobject, this.getFirstSymbol());
					}
   					
   					
   				}				
   				pw.printlnIdent(aMethodTmp + ".setAccessible(true);");
				
				pw.printlnIdent(resultTmpVar + " = " + aMethodTmp + ".invoke(" + indexedExprTmpVar  + ", " +
						indexOfExprTmpVar + ", " + rightExprTmpVar +
						 ");");
				pw.sub();
				pw.printlnIdent("}");
				
				String ep = NameServer.nextJavaLocalVariableName();
				pw.printlnIdent("catch ( java.lang.reflect.InvocationTargetException " + ep + " ) {");
		        pw.printlnIdent("	Throwable t__ = " + ep + ".getCause();");
		        pw.printlnIdent("	if ( t__ instanceof ExceptionContainer__ ) {");
		        pw.printlnIdent("    	throw new ExceptionContainer__( ((ExceptionContainer__) t__).elem );");
		        pw.printlnIdent("	}");
		        pw.printlnIdent("	else"); 
		        pw.printlnIdent("		throw new ExceptionContainer__( new _ExceptionJavaException(t__));");
		        pw.printlnIdent("}");
				
				ep = NameServer.nextJavaLocalVariableName();
				pw.printlnIdent("catch (IllegalAccessException | IllegalArgumentException " + ep + ") {");
				pw.add();
				
				String dnuTmpVar = NameServer.nextJavaLocalVariableName();
				pw.printlnIdent("//	func doesNotUnderstand: (CySymbol methodName, Array<Array<Dyn>> args)");
				pw.printlnIdent("java.lang.reflect.Method " + dnuTmpVar + " = CyanRuntime.getJavaMethodByName(" + 
						indexedExprTmpVar + ".getClass(), \"" + 
				       NameServer.javaNameDoesNotUnderstand  			+ "\", 2);");
				resultTmpVar = NameServer.nextJavaLocalVariableName();
				pw.printlnIdent("Object " + resultTmpVar + " = null;");					
				pw.printlnIdent("try {");
				pw.add();
				pw.printlnIdent(aMethodTmp + ".setAccessible(true);");
				
				pw.printlnIdent(resultTmpVar + " = " + aMethodTmp + ".invoke(" + indexedExprTmpVar + ", " +
						indexOfExprTmpVar + ", " + rightExprTmpVar +
						 ");");
				pw.sub();
				
				pw.printlnIdent("}");
				ep = NameServer.nextJavaLocalVariableName();
				pw.printlnIdent("catch ( java.lang.reflect.InvocationTargetException " + ep + " ) {");
		        pw.printlnIdent("	Throwable t__ = " + ep + ".getCause();");
		        pw.printlnIdent("	if ( t__ instanceof ExceptionContainer__ ) {");
		        pw.printlnIdent("    	throw new ExceptionContainer__( ((ExceptionContainer__) t__).elem );");
		        pw.printlnIdent("	}");
		        pw.printlnIdent("	else"); 
		        pw.printlnIdent("		throw new ExceptionContainer__( new _ExceptionJavaException(t__));");
		        pw.printlnIdent("}");
				pw.printlnIdent("catch (IllegalAccessException | IllegalArgumentException " + ep + ") {");
				pw.printlnIdent("        throw new ExceptionContainer__( new _ExceptionMethodNotFound( new CyString(\"Method called at line \" + " + lineNumber +  
				"+ \" of prototype '" + env.getCurrentProgramUnit().getFullName() + "' was not found\") ) );");
				pw.printlnIdent("}");
				pw.sub();
				pw.printlnIdent("}");
				
   			}
   			
       	
        }
        else {		

            
			
			if ( changeCyanMetaobject != null ) {
	   			/*
	   			 * assignment is changed by the metaobject attached to the prototype that is
	   			 * the type of the right-hand side
	   			 */
				
				try {
					rightExprTmpVar = changeCyanMetaobject.cge_changeRightHandSideTo(
		        			prototypeFoundMetaobject, 
		        			rightExprTmpVar, rightExpr.getType(env));
				} 
				catch ( error.CompileErrorException e ) {
				}
				catch ( RuntimeException e ) {
					CyanMetaobjectAnnotation annotation = ((CyanMetaobjectWithAt) changeCyanMetaobject).getMetaobjectAnnotation();
					env.thrownException(annotation, annotation.getFirstSymbol(), e);
				}
				finally {
   					env.errorInMetaobject( (meta.CyanMetaobject ) changeCyanMetaobject, this.getFirstSymbol());
				}
					
				
			}                

   	   		/*
   	   		 * cases to consider:
   	   		 *     a = expr;  // a non-ref
   	   		 *     a = expr;  // a is a ref-variable
   	   		 */


    		if ( rightType == Type.Dyn && leftType != Type.Dyn ) {
           		/*
           		 * it is necessary a conversion if the assignment if of the kind
           		 *     nonDyn = Dyn;
           		 */
    			
    			
    			pw.printlnIdent("if ( " + rightExprTmpVarOriginal + " instanceof " + leftType.getJavaName() + " ) {");
    			pw.add();

    			pw.printIdent("");
                ((LeftHandSideAssignment ) leftSide).genJavaCodeVariable(pw, env);
           		pw.println(" = (" + leftType.getJavaName() + " ) " + rightExprTmpVar + ";");
           		
    			pw.sub();
    			pw.printlnIdent("}");
    			pw.printlnIdent("else {");
    			pw.add();
    			
				pw.printlnIdent("throw new ExceptionContainer__("
						+ env.javaCodeForCastException(rightExpr, leftType) + " );");
    			
    			pw.sub();
    			pw.printlnIdent("}");
    		}
    		else {
       			// regular assignment
                pw.printIdent("");
                ((LeftHandSideAssignment ) leftSide).genJavaCodeVariable(pw, env);
                if ( leftType == Type.Any && rightType instanceof InterfaceDec ) {
               		pw.println(" = (" + NameServer.AnyInJava + " ) " + rightExprTmpVar + ";");
                }
                else {
               		pw.println(" = " + rightExprTmpVar + ";");
                }
    		}
       		
   		        	
   		}
        
        //}
        
    	/* 
    	 * multiple assignment are no longer allowed
    	 */
    	/*
		for (int i = size - 3; i >= 1; i--) {
			exprList.get(i).genJavaExprWithoutTmpVar(pwChar, env);
			pwChar.printlnIdent(" = " + varTmpName + ";");
		}
		*/

		// return varTmpName;
	}


	

	@Override
	public Symbol getFirstSymbol() {
		// TODO Auto-generated method stub
		return exprList.get(0).getFirstSymbol();
	}


	@Override
	public void calcInternalTypes(Env env) {
		
		
		int size = exprList.size();
		for ( Expr expr : exprList ) {
			//if ( expr.asString().equals("fff") ) //&& env.getCurrentCompilationUnit().getFilename().contains("G1(protoA.X)") )
				//System.out.println("g2 = ");
			
			expr.calcInternalTypes(env, --size > 0);
		}
		
		size = exprList.size();
		for (int i = size - 1; i >= 1; i--) {
			Expr leftExpr = exprList.get(i-1);
			
			boolean leftHandSideIsValid = true;
       		if ( ! (leftExpr instanceof LeftHandSideAssignment) && ! (leftExpr instanceof ExprIndexed) 
       				) {
       			leftHandSideIsValid = false;
       		}
       		if ( leftExpr instanceof ExprIdentStar ) {
       			IdentStarKind kind = ((ExprIdentStar ) leftExpr).getIdentStarKind();
       			leftHandSideIsValid = kind == IdentStarKind.instance_variable_t || kind == IdentStarKind.variable_t;
       		}
       		if ( ! leftHandSideIsValid ) {
       			env.error(leftExpr.getFirstSymbol(), "The left-hand side of '=' is not valid. It should be a variable or an indexed expression");
       		}
       		boolean isInit = false;
       		boolean isInitOnce = false;
       		MethodDec m = env.getCurrentMethod();
       		if ( m != null ) {
       			String methodName = m.getNameWithoutParamNumber();
       			isInit = methodName.equals("init") || methodName.equals("init:");
       			isInitOnce = methodName.equals("initOnce");
       		}
       		if ( isInit ) {
       			/*
    			if ( leftExpr instanceof ExprIdentStar ) {
    				ExprIdentStar varId = (ExprIdentStar ) leftExpr;
    				if ( varId.getVarDeclaration() != null && varId.getVarDeclaration()  ) {
    					VariableDecInterface iv = varId.getVarDeclaration();
    					env.error(leftExpr.getFirstSymbol());
    				}
    			}
    			else if ( leftExpr instanceof ExprSelf__PeriodIdent ) {
    				InstanceVariableDec iv = ((ExprSelf__PeriodIdent ) leftExpr).getInstanceVariableDec(); 
    				if ( iv.isConst() ) {
    					env.error(true,  leftExpr.getFirstSymbol(), 
    							"Attempt to assign a value to a constant. To make this variable writable, declare it as 'var " + 
    					    iv.getType().getFullName() + " " + iv.getName() + "'", iv.getName(), ErrorKind.attempt_to_assign_a_value_to_a_readonly_variable);
    				}
    			}
    			else if ( leftExpr instanceof ExprSelfPeriodIdent ) {
    				InstanceVariableDec iv = ((ExprSelfPeriodIdent ) leftExpr).getInstanceVariableDec(); 
    				if ( iv.isConst() ) {
    					env.error(true,  leftExpr.getFirstSymbol(), 
    							"Attempt to assign a value to a constant. To make this variable writable, declare it as 'var " + 
    					    iv.getType().getFullName() + " " + iv.getName() + "'", iv.getName(), ErrorKind.attempt_to_assign_a_value_to_a_readonly_variable);
    				}
    				
    			}
       			*/
       		}
       		else if ( isInitOnce ) {
       			
       		}
       		else if ( ! isInit && ! isInitOnce ) {
       			/*
       			 * inside 'init' or 'init:' methods you can assign values to instance variables that are declared with 'let'. Outside these methods
       			 * you cannot.
       			 */
    			if ( leftExpr instanceof ExprIdentStar ) {
    				ExprIdentStar varId = (ExprIdentStar ) leftExpr;
    				if ( varId.getVarDeclaration() != null && varId.getVarDeclaration().isReadonly()  ) {
    					VariableDecInterface iv = varId.getVarDeclaration();
    					env.error(true,  leftExpr.getFirstSymbol(), 
    							"Attempt to assign a value to a read only variable. To make this variable writable, declare it as 'var " + 
    					    iv.getType().getFullName() + " " + iv.getName() + "'", iv.getName(), ErrorKind.attempt_to_assign_a_value_to_a_readonly_variable);
    				}
    			}
    			else if ( leftExpr instanceof ExprSelf__PeriodIdent ) {
    				InstanceVariableDec iv = ((ExprSelf__PeriodIdent ) leftExpr).getInstanceVariableDec(); 
    				if ( iv.isReadonly() ) {
    					env.error(true,  leftExpr.getFirstSymbol(), 
    							"Attempt to assign a value to a read only variable. To make this variable writable, declare it as 'var " + 
    					    iv.getType().getFullName() + " " + iv.getName() + "'", iv.getName(), ErrorKind.attempt_to_assign_a_value_to_a_readonly_variable);
    				}
    			}
    			else if ( leftExpr instanceof ExprSelfPeriodIdent ) {
    				InstanceVariableDec iv = ((ExprSelfPeriodIdent ) leftExpr).getInstanceVariableDec(); 
    				if ( iv.isReadonly() ) {
    					env.error(true,  leftExpr.getFirstSymbol(), 
    							"Attempt to assign a value to a read only variable. To make this variable writable, declare it as 'var " + 
    					    iv.getType().getFullName() + " " + iv.getName() + "'", iv.getName(), ErrorKind.attempt_to_assign_a_value_to_a_readonly_variable);
    				}
    				
    			}
       		}
			Type rightTypeExpr = exprList.get(i).getType(env);
			Type leftTypeExpr = exprList.get(i-1).getType(env);
			if ( ! leftTypeExpr.isSupertypeOf(rightTypeExpr, env) ) {
				// System.out.println( ((ObjectDec ) leftTypeExpr).getCompilationUnit().getFullFileNamePath() + "  " + ((ObjectDec ) rightTypeExpr).getCompilationUnit().getFullFileNamePath());
				leftTypeExpr.isSupertypeOf(rightTypeExpr, env);				
				env.error(true, exprList.get(i-1).getFirstSymbol(),
						"The type of the right-hand side of this assignment, " + rightTypeExpr.getFullName() + 
						" is not a subtype of the left-hand side, " + leftTypeExpr.getFullName(), null, ErrorKind.type_error_type_of_right_hand_side_of_assignment_is_not_a_subtype_of_the_type_of_left_hand_side);
			}
		}
		//type = exprList.get(0).getType(env);
		
		/*
		 * push variables and their levels into a stack of initialized variables
		 *
		for ( int i = 0; i < size - 1; ++i) {
			Expr expr = exprList.get(i);
			if ( expr instanceof ExprIdentStar ) {
				ExprIdentStar eis = (ExprIdentStar ) expr;
				if ( eis.getIdentSymbolArray().size() == 1 ) {
					// a variable is being initialized
					String name = eis.getIdentSymbolArray().get(0).symbolString;
					env.pushVariableAndLevel(name);
				}
			}
		}
		*/
		super.calcInternalTypes(env);
	}



	private ArrayList<Expr> exprList;




}
