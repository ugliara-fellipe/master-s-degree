package ast;

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
 * represents a return statement, like
 * 	   fun get -> int {
 *         return n;
 *     }
 * @author José
 *
 */
public class StatementReturn extends Statement {


	public StatementReturn(Symbol returnSymbol, Expr expr, MethodDec currentMethod) {
		super(false);
		this.expr = expr;
		this.returnSymbol = returnSymbol;
		this.currentMethod = currentMethod;
	}

	
	@Override
	public void accept(ASTVisitor visitor) {
		visitor.visit(this);
	}	
	
	@Override
	public void genCyanReal(PWInterface pw, boolean printInMoreThanOneLine, CyanEnv cyanEnv, boolean genFunctions) {
		
		String s = "return ";
		if ( cyanEnv.getCreatingInnerPrototypeFromFunction() ) {
			//String methodName = currentMethod.getName();
			//if ( methodName.equals("eval") || methodName.startsWith("eval:") ) 
			s = "return_method__ ";
			System.out.println(s);
		}
		pw.print(s);
		if ( expr != null ) {
			expr.genCyan(pw, false, cyanEnv, genFunctions);
		}
	}

	@Override
	public void genJava(PWInterface pw, Env env) {
		
		
		String tmpVar = expr.genJavaExpr(pw, env);
		pw.println();
		/*
		 * four cases do consider:
		 *     fun m -> Int { return Dyn }   // convert Dyn to Int if possible. Otherwise throw exception
		 *     fun m -> Int { return 0 }     // no conversion
		 *     fun m -> Dyn { return 0 }     // no conversion
		 *     fun m -> Dyn { return Dyn }   // no conversion
		 */
		Type methodReturnType = currentMethod.getMethodSignature().getReturnType(env);
		Type exprType = expr.getType();

		
		/*
		 * A metaobject attached to the type of the formal parameter may demand that the real argument be
		 * changed. The new argument is the return of method  changeRightHandSideTo
		 */

		
		Tuple2<IActionAssignment_cge, ObjectDec> cyanMetaobjectPrototype = MetaInfoServer.getChangeAssignmentCyanMetaobject(env, methodReturnType);
		IActionAssignment_cge changeCyanMetaobject = null;
        ObjectDec prototypeFoundMetaobject = null;
        if ( cyanMetaobjectPrototype != null ) {
        	changeCyanMetaobject = cyanMetaobjectPrototype.f1;
        	prototypeFoundMetaobject = cyanMetaobjectPrototype.f2;
        	
				if ( changeCyanMetaobject != null ) {
					
					try {
						tmpVar = changeCyanMetaobject.cge_changeRightHandSideTo( prototypeFoundMetaobject, 
		   	           			tmpVar, exprType);
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
        
		
		
		
		
		if ( exprType == Type.Dyn && methodReturnType != Type.Dyn ) {
			// first case
			/*
			 * 
			 */
			pw.printlnIdent("if ( " + tmpVar + " instanceof " + methodReturnType.getJavaName() + " ) {");
			pw.add();
			pw.printlnIdent("return (" + methodReturnType.getJavaName() + " ) " + tmpVar + ";");
			pw.sub();
			pw.printlnIdent("}");
			pw.printlnIdent("else {");
			pw.add();
			
			pw.printlnIdent("throw new ExceptionContainer__("
					+ env.javaCodeForCastException(expr, methodReturnType) + " );");
			
			pw.sub();
			pw.printlnIdent("}");
		}
		else {
			if ( methodReturnType == Type.Any && exprType instanceof InterfaceDec ) {
				tmpVar = " (" + NameServer.AnyInJava + " ) " + tmpVar;
			}
			pw.printlnIdent("return " + tmpVar + ";");
		}
	}

	@Override
	public Symbol getFirstSymbol() {
		return returnSymbol;
	}

	@Override
	public boolean alwaysReturn() {
		return true;
	}

	public boolean alwaysReturnFromFunction() {
		return true;
	}
	

	@Override
	public void calcInternalTypes(Env env) {
		expr.calcInternalTypes(env);
		if ( ! currentMethod.getMethodSignature().getReturnType(env).isSupertypeOf(expr.getType(env), env) )  {
			currentMethod.getMethodSignature().getReturnType(env).isSupertypeOf(expr.getType(env), env);
			env.error(true, expr.getFirstSymbol(),
					"The type of the returned value, " +   expr.getType(env).getFullName() + 
			        ", is not a subtype of the method return type, " + currentMethod.getMethodSignature().getReturnType(env).getFullName(), 
			        null, ErrorKind.type_error_return_value_type_is_not_a_subtype_of_the_method_return_type);
		}
		super.calcInternalTypes(env);
	}


	@Override
	public boolean demandSemicolon() { return false; }


	private Symbol returnSymbol;
	private Expr expr;
	private MethodDec currentMethod;	

}
