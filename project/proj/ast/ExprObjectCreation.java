package ast;

import java.util.ArrayList;
import lexer.Symbol;
import meta.CyanMetaobjectWithAt;
import meta.IActionAssignment_cge;
import meta.MetaInfoServer;
import saci.CyanEnv;
import saci.Env;
import saci.NameServer;
import saci.Tuple2;


/**
 * Represents the creation of an object using ( and ) as in
 *     Person("Lívia")
 *     Manager("Carolina", 10000)
 *     Stack<Int>(10)
 *     Sum(v);
 *
 * @author José
 *
 */

public class ExprObjectCreation extends Expr {

	
	public ExprObjectCreation(Expr prototypeType,
			ArrayList<Expr> parameterList, Symbol leftParSymbol, Symbol rightParSymbol, ExprIdentStar packagePrototype) {
		super();
		this.prototype = prototypeType;
		this.parameterList = parameterList;
		this.leftParSymbol = leftParSymbol;
		this.rightParSymbol = rightParSymbol;
		this.packagePrototype = packagePrototype;
	}


	@Override
	public void accept(ASTVisitor visitor) {
		this.prototype.accept(visitor);
		for ( Expr e : this.parameterList ) {
			e.accept(visitor);
		}
		visitor.visit(this);
	}
	
	
	@Override
	public boolean isNRE(Env env) {
		for ( Expr e : parameterList ) {
			if ( !e.isNRE(env) )
				return false;
		}
		return true;
	}	
	
	@Override
	public boolean isNREForInitOnce(Env env) {
		String name = NameServer.removeSpaces(prototype.asString());
		ProgramUnit pu = env.getProject().getCyanLangPackage().searchPublicNonGenericProgramUnit(name);
		if ( pu == null ) {
			return false;
		}
		for ( Expr e : parameterList ) {
			if ( !e.isNREForInitOnce(env) )
				return false;
		}
		return true;
	}

	@Override
	public void genCyanReal(PWInterface pw, boolean printInMoreThanOneLine, CyanEnv cyanEnv, boolean genFunctions) {

		prototype.genCyan(pw, false, cyanEnv, genFunctions);
		pw.print("(");
		int size =  this.parameterList.size();
		for ( Expr e : this.parameterList ) {
			e.genCyan(pw, false, cyanEnv, genFunctions);
			--size;
			if ( size > 0 )
				pw.print(", ");
		}
		pw.print(")");
	}

	@Override
	public String genJavaExpr(PWInterface pw, Env env) {
		
		
		String tmpVar = NameServer.nextJavaLocalVariableName();
		//pw.printIdent(tmpVar + " = new " + NameServer.getJavaNameGenericPrototype(prototype.ifPrototypeReturnsItsName()) + "(");

		ArrayList<ParameterDec> formalParamList = this.initMethodSignature.getParameterList();
		int size = parameterList.size();
		int i = 0;
		
		//***************************
		

		/*
		 * if the type of the real argument is Dyn and the type of the formal parameter is not Dyn,
		 * first generate code that cast the real argument to the correct type 
		 */
		ArrayList<String> stringArray = new ArrayList<String>();
		
		int ii = 0;
		ArrayList<Expr> realParamExprList = new ArrayList<>();
		for ( Expr e : parameterList ) {
			String strExpr = null;
			ParameterDec formalParam = formalParamList.get(ii);

			realParamExprList.add(e);
			
			
			String tmpVar1 = null;

			
			if ( e instanceof ExprIdentStar || e instanceof ExprSelfPeriodIdent ) {
				if ( e instanceof ExprIdentStar ) {
					VariableDecInterface rightSideVar = env.searchVariable( ((ExprIdentStar ) e).getName());
					if ( rightSideVar != null ) {
						if  ( rightSideVar.getRefType() && ! formalParam.getRefType() )
							strExpr = rightSideVar.getJavaName() + ".elem";
						else
							strExpr = rightSideVar.getJavaName();
					}
				}
				else {
					InstanceVariableDec rightSideVar = env.searchInstanceVariable(
							((ExprSelfPeriodIdent ) e).getIdentSymbol().getSymbolString());
					if ( rightSideVar != null ) {
						if ( rightSideVar.getRefType() && ! formalParam.getRefType() )
							strExpr = rightSideVar.getJavaName() + ".elem";
						else
							strExpr = rightSideVar.getJavaName();
					}
				}
			}
			if ( strExpr == null )
				tmpVar1 = e.genJavaExpr(pw, env); 
			else
				tmpVar1 = strExpr;
			
			
			
			if ( formalParamList.get(ii).getType() != Type.Dyn && e.getType() == Type.Dyn ) {
				String otherTmpVar = NameServer.nextJavaLocalVariableName();
				pw.printlnIdent(parameterList.get(ii).getType().getJavaName() + " " + otherTmpVar + ";");
				pw.printlnIdent("if ( " + tmpVar1 + " instanceof " + formalParamList.get(ii).getType().getJavaName() + " ) ");
				pw.printlnIdent("    " + otherTmpVar + " = (" + formalParamList.get(ii).getType().getJavaName() + " ) " + tmpVar1 + ";");
				pw.printlnIdent("else");
				
				pw.printlnIdent("    throw new ExceptionContainer__("
						+ env.javaCodeForCastException(e, formalParamList.get(ii).getType()) + " );");
				
				stringArray.add(otherTmpVar);
			}
			else
				stringArray.add( tmpVar1 );
			++ii;
			
			/*
			tmpVarList[i] = strExpr;
			++i;
			 * 
			 */
					
		}
		
		/*
		 * A metaobject attached to the type of the formal parameter may demand that the real argument be
		 * changed. The new argument is the return of method  changeRightHandSideTo
		 */
		StringBuffer paramPassing = new StringBuffer();
		
		ii = 0;
		ParameterDec param;
		int size1 = stringArray.size();
		for ( String tmp : stringArray ) {
			param = formalParamList.get(ii);
			Type leftType = param.getType(env);
			Tuple2<IActionAssignment_cge, ObjectDec> cyanMetaobjectPrototype = MetaInfoServer.getChangeAssignmentCyanMetaobject(env, leftType);
			IActionAssignment_cge changeCyanMetaobject = null;
	        ObjectDec prototypeFoundMetaobject = null;
	        if ( cyanMetaobjectPrototype != null ) {
	        	changeCyanMetaobject = cyanMetaobjectPrototype.f1;
	        	prototypeFoundMetaobject = cyanMetaobjectPrototype.f2;
	        	
				
				try {
					tmp = changeCyanMetaobject.cge_changeRightHandSideTo( 
		        			prototypeFoundMetaobject, 
		        			tmp, realParamExprList.get(ii).getType(env));
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
			
			
			paramPassing.append(tmp);
			//pw.print(tmp);
			if ( --size1 > 0 )
				paramPassing.append(", ");
			++ii;
		}

		pw.printIdent( type.getJavaName() + " " + tmpVar + " = ");
		pw.print( "new " + type.getJavaName() + "( " +  
					paramPassing.toString()  + ");");
		
		//***************************

		/*
		pw.printIdent(type.getJavaName() + " " + tmpVar + " = new " + type.getJavaName() + "("); 
		for ( String s : tmpVarList ) {
			pw.print(s);
			if ( --size1 > 0 )
			    pw.print(", ");
		}
		
		pw.println(");");
		*/
		return tmpVar;
	}

	@Override
	public Symbol getFirstSymbol() {
		return prototype.getFirstSymbol();
	}


	public void setRightParSymbol(Symbol rightParSymbol) {
		this.rightParSymbol = rightParSymbol;
	}


	public Symbol getRightParSymbol() {
		return rightParSymbol;
	}


	@Override
	public void calcInternalTypes(Env env) {
		
		
		for ( Expr realParameter : parameterList ) {
			realParameter.calcInternalTypes(env);
		}
		

		if ( prototype.getType(env) == null ) {
			prototype.calcInternalTypes(env);
		}
		ProgramUnit pu = env.searchPackagePrototype(prototype.asString(), prototype.getFirstSymbol());
		type = pu; // prototype.ifRepresentsTypeReturnsType(env);
		/*
		if ( pu != type ) {
			env.error(prototype.getFirstSymbol(), "Internal error in ExprObjectCreation");
		}
		*/
		//type = prototype.getType();
		
		ExprIdentStar eis;
		if ( prototype instanceof ExprIdentStar ) {
			eis = (ExprIdentStar) prototype;
			String fullName = prototype.asString();
			if ( env.searchVisibleProgramUnit(fullName, prototype.getFirstSymbol(), true) == null) {
				env.error(prototype.getFirstSymbol(),  "Prototype '" + fullName + "' was not found");
			}
		}
		else if ( prototype instanceof ExprGenericPrototypeInstantiation )  {
			eis = ((ExprGenericPrototypeInstantiation ) prototype ).getTypeIdent();
		}
		else {
			env.error(prototype.getFirstSymbol(), "A prototype expected in an expression 'P(...)' or 'P()'. Found " + prototype.asString());
			return ;
		}
		  // first character should be in upper case
		if ( ! Character.isUpperCase( (eis).getIdentSymbolArray().get(eis.getIdentSymbolArray().size()-1).getSymbolString().charAt(0)) ) {
			env.error(prototype.getFirstSymbol(),  "A prototype was expected before '()' or '(...)'. Found " + prototype.asString());
		}
		
		/*
		String protoName = "";
		String packageName = "";
		ArrayList<Symbol> symList = packagePrototype.getIdentSymbolArray();
		int size = packagePrototype.getIdentSymbolArray().size();
		int i = 0;
		while ( i < size - 1 ) {
			packageName += symList.get(i).getSymbolString();
			++i;
		}
		protoName = symList.get(size-1).getSymbolString();
		ProgramUnit pu;
		if ( packageName.length() == 0 ) {
			pu = env.searchVisibleProgramUnit(protoName, prototype.getFirstSymbol(), );
		}
		
		if ( protoName == null ) {
			env.error(prototype.getFirstSymbol(),  "A prototype was expected before '()' or '(...)'. Found " + prototype.asString());
			return ;
		}
		*/
		if ( type == null ) {
			env.error(prototype.getFirstSymbol(), "Prototype " + prototype.ifPrototypeReturnsItsName(env) + " was not found", true, true);
		}
		else {
			if ( ! (type instanceof ObjectDec ) ) {
				env.error(prototype.getFirstSymbol(),  prototype.getFirstSymbol().getSymbolString() + " should be a prototype", true, true);
				return ;
			}
			ObjectDec proto = (ObjectDec ) type;
			if ( parameterList.size() == 0 ) {
				// search for init
				ArrayList<MethodSignature> methodSignatureList = proto.searchInitNewMethod("init");
				if ( methodSignatureList == null || methodSignatureList.size() == 0 ) {
					methodSignatureList = proto.searchInitNewMethod("new");
					if ( methodSignatureList == null || methodSignatureList.size() == 0 ) {
						env.error(getFirstSymbol(), "Since this object creation does not take parameters, prototype " + proto.getName() + 
								" should have an 'init' or 'new' method. It does not", true, true);
						return ;
					}
				}
				this.initMethodSignature = methodSignatureList.get(0);
			}
			else {
				// search for init: 
				ArrayList<MethodSignature> methodSignatureList = proto.searchInitNewMethod("new:");
				if ( methodSignatureList == null || methodSignatureList.size() == 0 )
					methodSignatureList = proto.searchInitNewMethod("init:");
				boolean foundMethod = false;
				for ( MethodSignature methodSignature : methodSignatureList ) {
					boolean typeError = false;
					if ( methodSignature instanceof MethodSignatureWithSelectors ) {
						ArrayList<SelectorWithParameters> selectorWithParameters = ((MethodSignatureWithSelectors) methodSignature).getSelectorArray();
						  // selectorWithParameters.size() == 1
						if ( selectorWithParameters.get(0).getParameterList().size() == parameterList.size() ) {
							int indexSignature = 0;
							for ( ParameterDec parameter : selectorWithParameters.get(0).getParameterList() ) {
								Expr realParameter = parameterList.get(indexSignature);
								if ( ! parameter.getType().isSupertypeOf(realParameter.getType(env), env) ) {
									typeError = true;
									parameter.getType().isSupertypeOf(parameterList.get(indexSignature).getType(env), env);
									break;
								}
								/*
								if ( parameter.getRefType() && realParameter instanceof ExprIdentStar ) {
									ExprIdentStar e = (ExprIdentStar ) realParameter;
									if ( e.getIdentSymbolArray().size() == 1 ) {
										e.setRefType(true);
									}
								}
								*/
								++indexSignature;
							}
						}
						else
							typeError = true;
					}
					else
						typeError = true;
					if ( ! typeError ) {
						foundMethod = true;
						int indexSignature = 0;
						this.initMethodSignature =  methodSignature;
						ArrayList<SelectorWithParameters> selectorWithParameters = ((MethodSignatureWithSelectors ) initMethodSignature).getSelectorArray();
						for ( ParameterDec parameter : selectorWithParameters.get(0).getParameterList() ) {
							Expr realParam = parameterList.get(indexSignature);
							++indexSignature;
							if ( parameter.getVariableKind() == VariableKind.LOCAL_VARIABLE_REF ) {
								if ( realParam instanceof ExprIdentStar ) {
									ExprIdentStar e = (ExprIdentStar ) realParam;
									 
									VariableDecInterface varDec = e.getVarDeclaration();
									
									  // env.searchVariable( ((ExprIdentStar ) realParam).getName());
									if ( varDec == null || 
											(! (varDec instanceof StatementLocalVariableDec) && 
											 ! (varDec instanceof InstanceVariableDec) ) &&
											 ! (varDec instanceof ParameterDec)  )
										env.error(realParam.getFirstSymbol(), "A local variable or instance variable was expected because the formal parameter was declared with '&'", true, true);
									else {
										if ( varDec.isReadonly() ) {
											env.error(realParam.getFirstSymbol(), "A non-read only local variable or instance variable was expected because the formal parameter was declared with '&'", true, true);
										}
										if ( varDec instanceof StatementLocalVariableDec || 
											 varDec instanceof InstanceVariableDec ) {
											varDec.setRefType(true);
										}
										realParam.calcInternalTypes(env);
									}
							    }
								else
									env.error(realParam.getFirstSymbol(), "A local variable or instance variable was expected because the formal parameter was declared with '&'", true, true);
							}
						}
						/*
							for (ParameterDec paramDec : selWithFormalParam.getParameterList() ) {
								Expr realParam = selectorWithRealParam.getExprList().get(parameterIndex);
								if ( ! paramDec.getType(env).isSupertypeOf(realParam.getType(env), env)) {
									typeErrorInParameterPassing = true;
									break;
								}
								
								
								if ( paramDec.getVariableKind() == VariableKind.LOCAL_VARIABLE_REF ) {
									if ( realParam instanceof ExprIdentStar ) {
										ExprIdentStar e = (ExprIdentStar ) realParam;
										 
										VariableDecInterface varDec = e.getVarDeclaration();
										
										  // env.searchVariable( ((ExprIdentStar ) realParam).getName());
										if ( varDec == null || 
												(! (varDec instanceof StatementLocalVariableDec) && 
												 ! (varDec instanceof InstanceVariableDec) &&
												 ! (varDec instanceof ParameterDec)
												 ) )
											env.error(realParam.getFirstSymbol(), "A local variable or instance variable was expected because the formal parameter was declared with '&'", true);
										else {
											if ( varDec instanceof StatementLocalVariableDec ) 
												((StatementLocalVariableDec ) varDec).setRefType(true);
											else if ( varDec instanceof InstanceVariableDec )
												((InstanceVariableDec ) varDec).setRefType(true);
											else {
												if ( ! ((ParameterDec ) varDec).getRefType() ) {
													env.error(realParam.getFirstSymbol(), "A parameter with reference type, declared with '&', was expected because the formal parameter was declared with '&'", true);
												}
											}
											realParam.calcInternalTypes(env);
										}
								    }
									else
										env.error(realParam.getFirstSymbol(), "A local variable or instance variable was expected because the formal parameter was declared with '&'", true);
								    break;
								}
						 * 
						 */
						break;
					}
				}
				if ( ! foundMethod ) {
					env.error(getFirstSymbol(), "No adequate 'init:' method was found in prototype " + proto.getName() + 
							" for this object creation", true, true);
				}
			}
		}
		super.calcInternalTypes(env);
		
	}


	/**
	 * Real parameter list
	 */
	private ArrayList<Expr> parameterList;
	/**
	 * the prototype such as Sum and Stack<Int> 
	 */
	private Expr prototype;
	/**
	 * the prototype with its package (if any) but without the generic prototype parameters (if any). 
	 * Then, in <code>'other.BinTree<Int>'</code>, this variable contains <code>'other.BinTree'</code>
	 */
	private ExprIdentStar packagePrototype;

	/** symbols of the left ( and right ) of the context object creation:
	 *     Sum(v)
	 *
	 */
	private Symbol leftParSymbol;
	private Symbol rightParSymbol;
	/**
	 * the 'init' or 'init:' method that should be used in this creation of object
	 */
	private MethodSignature initMethodSignature;
}
