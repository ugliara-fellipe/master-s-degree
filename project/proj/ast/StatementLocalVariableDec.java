package ast;

import java.util.ArrayList;
import error.CompileErrorException;
import error.ErrorKind;
import lexer.Symbol;
import lexer.SymbolIdent;
import meta.CyanMetaobjectWithAt;
import meta.IActionAssignment_cge;
import meta.MetaInfoServer;
import saci.CompilationStep;
import saci.CyanEnv;
import saci.Env;
import saci.NameServer;
import saci.Tuple2;

/**
 *    Represents a local variable, which include parameters (a subclass)
 * @author José
 *
 */
public class StatementLocalVariableDec extends Statement implements VariableDecInterface {

	public StatementLocalVariableDec(SymbolIdent variableSymbol, Expr typeInDec, Expr expr, MethodDec declaringMethod, int level, boolean isReadonly) {
		this.variableSymbol = variableSymbol;
		this.typeInDec = typeInDec;
		this.expr = expr;
		this.declaringFunction = null;
		this.declaringMethod = declaringMethod;
		this.level = level;
		innerObjectNumberList = new ArrayList<>();
		javaName = NameServer.getJavaName(this.getName());
		this.isReadonly = isReadonly;
		typeWasChanged = false;
	}
	
	@Override
	public void accept(ASTVisitor visitor) {
		visitor.visit(this);
		if ( expr != null ) {
			expr.accept(visitor);
		}
	}	
	

	@Override
	public Expr getTypeInDec() {
		return typeInDec;
	}

	public void setVariableSymbol(SymbolIdent variableSymbol) {
		this.variableSymbol = variableSymbol;
	}

	@Override
	public SymbolIdent getVariableSymbol() {
		return variableSymbol;
	}

	public Expr getExpr() {
		return expr;
	}


	@Override
	public void genCyanReal(PWInterface pw, boolean printInMoreThanOneLine, CyanEnv cyanEnv, boolean genFunctions) {
		
		if ( typeInDec != null )
		    typeInDec.genCyan(pw, false, cyanEnv, genFunctions);
		if ( getName() != null ) 
			pw.print( (typeInDec != null ? " " : "") + getName());
		if ( expr != null ) {
			pw.print(" = ");
			expr.genCyan(pw, false, cyanEnv, genFunctions);
		}
	}

	@Override
	public void genJava(PWInterface pw, Env env) {


		String variableName = getName();
		env.pushVariableDec(this);
		
		
		
		String javaNameVar = NameServer.getJavaName(variableName);
		String javaTypeName;
		
		if ( typeInDec != null )
			javaTypeName =  this.typeInDec.getJavaName();
		else
			javaTypeName = type.getJavaName();
		
		
		
		String tmpExpr = "";
		if ( expr != null ) {
			tmpExpr = expr.genJavaExpr(pw, env);
			pw.println();
		}
		
		
		if ( refType )
			pw.printIdent("Ref<" + javaTypeName + ">");
		else
			pw.printIdent(javaTypeName);
		pw.print(" " + javaNameVar);
		if ( refType ) {
			pw.print(" = new Ref<" + javaTypeName + ">()");
		}
		
		if ( expr == null ) 
			pw.println(";");
		else {
			pw.println(";");
			if ( refType ) 
				javaNameVar = javaNameVar + ".elem";
       		Type rightType = expr.getType();
			
    		/*
    		 * A metaobject attached to the type of the formal parameter may demand that the real argument be
    		 * changed. The new argument is the return of method  changeRightHandSideTo
    		 */

    		
    		Tuple2<IActionAssignment_cge, ObjectDec> cyanMetaobjectPrototype = MetaInfoServer.getChangeAssignmentCyanMetaobject(env, type);
    		IActionAssignment_cge changeCyanMetaobject = null;
            ObjectDec prototypeFoundMetaobject = null;
            if ( cyanMetaobjectPrototype != null ) {
            	changeCyanMetaobject = cyanMetaobjectPrototype.f1;
            	prototypeFoundMetaobject = cyanMetaobjectPrototype.f2;
            	
    				if ( changeCyanMetaobject != null ) {
    					
    					try {
        					tmpExpr = changeCyanMetaobject.cge_changeRightHandSideTo( prototypeFoundMetaobject, 
        							tmpExpr, expr.getType(env));
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
    				
            	
            }
                 		
       		
    		if ( rightType == Type.Dyn && type != Type.Dyn ) {
    			String javaNameType = type.getJavaName();
    			pw.printlnIdent("if ( " + tmpExpr + " instanceof " + javaNameType + " ) {");
    			pw.add();
    			pw.printlnIdent(javaNameVar + " = (" + javaNameType + " ) " + tmpExpr + ";");
    			pw.sub();
    			pw.printlnIdent("}");
    			pw.printlnIdent("else {");
    			pw.add();
    			
				pw.printlnIdent("throw new ExceptionContainer__("
						+ env.javaCodeForCastException(expr, type) + " );");
    			
    			pw.sub();
    			pw.printlnIdent("}");
    		}
    		else {
				pw.printlnIdent(javaNameVar + " = " + tmpExpr + ";");
    		}
				/*
				if ( expr instanceof ExprIdentStar ) {
					VariableDecInterface rightSideVar = env.searchVariable( ((ExprIdentStar ) expr).getName());
					if ( rightSideVar != null && rightSideVar.getRefType() ) {
						pw.printlnIdent(javaNameVar + " = " + rightSideVar.getJavaName() + ";" );
					}
					else
						pw.printlnIdent(javaNameVar + ".elem = " + tmpExpr + ";");
				}
				else if ( expr instanceof ExprSelfPeriodIdent ) {
					InstanceVariableDec rightSideVar = env.searchInstanceVariable(
							((ExprSelfPeriodIdent ) expr).getIdentSymbol().getSymbolString());
					if ( rightSideVar != null && rightSideVar.getRefType() ) {
						pw.printlnIdent(javaNameVar + " = " + rightSideVar.getJavaName() + ";" );
					}
					else
						pw.printlnIdent(javaNameVar + ".elem = " + tmpExpr + ";");
				}
				else
					pw.printlnIdent(javaNameVar + ".elem = " + tmpExpr + ";");
				*/
		}
	}


	@Override
	public Symbol getFirstSymbol() {
		return variableSymbol;
	}



	@Override
	public String getName() {
		return variableSymbol.getSymbolString();
	}

		

	
	@Override
	public String getJavaName() {
		return javaName;
	}

	@Override
	public void setJavaName(String javaName) {
		this.javaName = javaName;
	}
	
	
	@Override
	public boolean isReadonly() {
		return isReadonly;
	}
	
	
	private String javaName;
		
	

	/**
	 * sets the function that is declaring this local variable.  
	 * @param declaringFunction
	 */
	public void setDeclaringFunction(ExprFunction declaringFunction) {
		this.declaringFunction = declaringFunction;		
	}


	public ExprFunction getDeclaringFunction() {
		return declaringFunction;
	}

	public MethodDec getDeclaringMethod() {
		return declaringMethod;
	}

	public int getLevel() {
		return level;
	}

	public void setLevel(int level) {
		this.level = level;
	}
	
	@Override
	public void calcInternalTypes(Env env) {
		
	
		String protoName = env.getCurrentProgramUnit().getName();
		/*
		 * if the method is bindToFunction  in a context function, all message sends
		 * to self are considered as message sends to newSelf__ whose type is
		 * the type of the first parameter of bindToFunction
		 */
		if ( NameServer.isNameInnerProtoForContextFunction(protoName) && 
				 env.getCurrentMethod().getName().equals(NameServer.bindToFunctionWithParamNumber)	) {
			/* t is the type of newSelf__, the first parameter of
                        func bindToFunction: IColor newSelf__ -> UFunction<String> {
                            return { (:  -> String :)
                                ^ colorTable[newSelf__  color]
                            }
                        }
			 */
			Type t = env.getCurrentMethod().getMethodSignature().getParameterList().get(0).getType();
			if ( t.searchMethodPublicSuperPublic(this.variableSymbol.getSymbolString(), env) != null ) {
				env.error(true, variableSymbol, 
						"Local variable '" + variableSymbol.getSymbolString() + 
								"' has the same name as an unary method of the type of parameter 'self' of the enclosing context object", variableSymbol.getSymbolString(), ErrorKind.local_variable_has_same_name_method_context_object);
			}
			
		}
		
		boolean exceptionThrown = false;
		if ( expr != null ) {
			try {
				expr.calcInternalTypes(env);
				// env.pushVariableAndLevel(variableSymbol.symbolString);
			}
			catch ( CompileErrorException e ) {
				exceptionThrown = true;
			}
		}
		env.pushVariableAndLevel(this, variableSymbol.symbolString);
		if ( typeInDec != null ) {
			try {
				typeInDec.calcInternalTypes(env);
				type = typeInDec.ifRepresentsTypeReturnsType(env);
			}
			catch ( CompileErrorException e ) {
				exceptionThrown = true;
			}
			
		}
		else {
			if ( expr == null ) {
				/*
				 * no expr and no type: declare as type Dyn
				 */
				type = Type.Dyn;
			}
			else 
				type = expr.getType(env);
		}
		if ( exceptionThrown ) {
			type = Type.Dyn;
		}
		else if ( expr != null ) {
			if ( ! (type.isSupertypeOf(expr.getType(env), env)) ) {
				type.isSupertypeOf(expr.getType(env), env);
				env.error(true, variableSymbol, 
			      "Type error: '" + expr.asString() + "' has a type that is not subtype of '" + type.getFullName() + "'", variableSymbol.getSymbolString(), ErrorKind.type_error_type_of_right_hand_side_of_assignment_is_not_a_subtype_of_the_type_of_left_hand_side
			      );
			}
		}
		
		String nameVar = this.getName();
		VariableDecInterface otherVar = env.searchLocalVariableParameter(nameVar);
		if ( otherVar != null ) {
			env.error(this.getFirstSymbol(), "Variable '" + nameVar + "' is being redeclared. The other declaration is in line " 
					+ otherVar.getVariableSymbol().getLineNumber());
		}
		/*
		for ( VariableDecInterface aVar : env.getVariableDecStack() ) {
			if ( aVar.getName().equals(nameVar) )
		}
		Stack<Tuple2<String, Integer>> stackVar = env.getStackVariableLevel();
		for ( Tuple2<String, Integer> t : stackVar ) {
			if ( t.f1.equals(nameVar) && t.f2 == env.getLexicalLevel() ) {
				VariableDecInterface varDec = env.searchLocalVariableParameter(this.getName());
				env.error(this.getFirstSymbol(), "Variable '" + nameVar + "' is being redeclared. The other declaration is in line " 
						+ varDec.getVariableSymbol().getLineNumber());
				
			}
		}
		*/
		
		env.pushVariableDec(this);
		
		/**
		 * see comments on  {@link #innerObjectNumberList}
		 */
		CompilationStep cs = env.getProject().getCompilerManager().getCompilationStep();
		if ( (cs == CompilationStep.step_8 || cs == CompilationStep.step_9) && 
		 	  env.getCurrentObjectDec().getOuterObject() == null ) {
				// outer must be null because only outer objects cause the creation of inner objects
			
			/**
			 * change the context parameter types of an inner object that uses this variable. That is, 
			 * the object is something like
                object Fun_0__(Program self__, Any &s12)  extends Function<Nil>
                    ...
    				func new: Program self__, Any &s12 -> Fun_0__ { ... }
    				...
    			end
    			
    			in which the type of s12 is Any, which is not the real type. The code below 
    			assigns to variables like s12 their correct type. Idem for parameters 
    			to method 'new:'.
                  
    			 * 
			 */
			ObjectDec currentObject = env.getCurrentObjectDec();
			if ( currentObject != null ) {
				ArrayList<ObjectDec> innerObjectDecList = currentObject.getInnerPrototypeList();
				String name = this.getName();
				for ( Integer n : this.innerObjectNumberList ) {
					ObjectDec toChange = innerObjectDecList.get(n);
					for ( ContextParameter cp : toChange.getContextParameterArray() ) {
						if ( cp.getName().equals(name)  ) {
							cp.setTypeInDec(this.typeInDec);
							cp.setType(type);
							break;
						}
					}
					/*
					 * change type of 'new:' parameters
					 */
					ArrayList<MethodSignature> methodSignatureList = toChange.searchInitNewMethod("new:");
					for ( MethodSignature ms: methodSignatureList ) {
						for ( ParameterDec param : ms.getParameterList() ) {
							if ( param.getName().equals(name) ) {
								param.setTypeInDec(this.typeInDec);
								param.setType(type);
							}
						}
					}
				}
			}
		}
		super.calcInternalTypes(env);
		
	}

	@Override
	public Type getType() {
		return type;
	}

	@Override
	public void setType(Type type) {
		this.type = type;
	}
	
	
	@Override
	public void setTypeInDec(Expr typeInDec) {
		this.typeInDec = typeInDec;
	}
	
	@Override
	public boolean getRefType() {
		return refType;
	}

	@Override
	public void setRefType(boolean refType) {
		if ( refType ) {
			if ( this.isReadonly ) {
				this.refType = false;
			}
			else {
				this.refType = true;
			}
		}
		else {
			this.refType = refType;
		}
	}	

	
	
	public ArrayList<Integer> getInnerObjectNumberList() {
		return innerObjectNumberList;
	}



	public void addInnerObjectNumberList(int innerObjectNumber) {
		
		for ( Integer n : this.innerObjectNumberList ) {
			if ( n == innerObjectNumber ) 
				return;
		}
		innerObjectNumberList.add(innerObjectNumber);
		  /*
		   * if the variable is used inside any function, it should be a ref type. Unless it is a read only variable
		   */
		
		this.setRefType( ! this.isReadonly );
	}

	
	/**
	 * see {@link VariableDecInterface#setTypeWasChanged(boolean)}
	 */

	@Override
	public void setTypeWasChanged(boolean typeWasChanged) {
		this.typeWasChanged = typeWasChanged;
	}
	/**
	 * see {@link VariableDecInterface#setTypeWasChanged(boolean)}
	 */
	@Override
	public boolean getTypeWasChanged() {
		return typeWasChanged;
	}
	
	/**
	 * see {@link VariableDecInterface#setTypeWasChanged(boolean)}
	 */
	private boolean typeWasChanged;
	
	
	private SymbolIdent variableSymbol;
	/**
	 * object that is the type of the variable. It must be a basic type,
	 * an object, a generic object instantiation, or an array.
	 */
	private Expr typeInDec;
	/**
	 * type of the variable. An object of a subclass of Type
	 */
	private Type type;
	/**
	 * expression to which the variable is initialized
	 */
	private Expr expr;

	/**
	 * the function that is declaring this variable. null if the declaration is outside any function
	 */
	private ExprFunction declaringFunction;
	/**
	 * the method that is declaring this variable. It is always non-null even in cases like
	 * the example below in which n is declared inside a function.
	 *     func test {
	 *         var b = { var Int n; n = 0; ^n }
	 *     }
	 */
	private MethodDec declaringMethod;
	/**
	 * level of the variable as defined by the Cyan manual. In the example below, ai is of level i.
	 * 
	 * public func test: (Int n) { 
	 *    // scope level 1 
	 *    var Int a1 = n; 
	 *    (n < 0) ifFalse: { 
	 *       // scope level 2 
	 *       var Int a2 = -a1;
	 *       (n > 0) ifTrue: { 
	 *           // scope level 3
	 *           var a3 = a2 + 1;
	 *           Out println: "> 0", a3
	 *       }
	 *       ifFalse: { Out println: "= 0" }
	 *    }
	 * } // a1 and n are removed from the stack here
	 */
	private int level;

	/**
	 * true if this variable was used as a reference type. That) is, refType is true for a
	 * variable p if p was used where a reference was expected. For example, suppose p 
	 * was used in 
	 *     Sum(p)
	 * in which Sum was declared as
	 *     object Sum(Int &s) ... end
	 * then refType should be true.  
	 */
	private boolean refType;

	/**
	 * list of numbers of inner objects. Each inner object corresponds to a function that 
	 * accesses this parameter. These inner objects have a parameter with name equal to this
	 * variable as a context parameter, as 'v1' in <br> 
  	  <code> 
      object F1(B self__, Any p1, Any v1) <br>
            extends Function{@literal <}Int, Int>  <br>
          func eval: Int a -> Int {  <br>
              ^a + p1 + v1 + iv1  <br>
          }<br>
      end<br>
      <code>	  
	 * The compiler should change the type of 'v1', which is initially 'Any' to the correct
	 * type, which is 'this.typeInDec'
	 */
	private ArrayList<Integer> innerObjectNumberList;

	/**
	 * true if this variable is read only
	 */
	private boolean isReadonly;
	
}
