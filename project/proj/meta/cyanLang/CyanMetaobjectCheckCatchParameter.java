package meta.cyanLang;

import java.util.ArrayList;
import ast.Expr;
import ast.GenericParameter;
import ast.GenericParameter.GenericParameterKind;
import ast.MessageWithSelectors;
import ast.MetaobjectArgumentKind;
import ast.MethodSignature;
import ast.ObjectDec;
import ast.ParameterDec;
import ast.SelectorWithRealParameters;
import ast.Type;
import meta.CyanMetaobjectWithAt;
import meta.DeclarationKind;
import meta.ExprReceiverKind;
import meta.ICheckMessageSend_dsa;
import saci.Env;
import saci.NameServer;

/**
 * This metaobject checks whether each parameter to a catch: selector has at least one 'eval:' 
 * method,  each of them accepting one parameter whose type is sub-prototype of {\tt CyException}.
   @author José
 */
public class CyanMetaobjectCheckCatchParameter extends CyanMetaobjectWithAt 
    implements ICheckMessageSend_dsa {

	public CyanMetaobjectCheckCatchParameter() { 
		super(MetaobjectArgumentKind.ZeroParameter);
	}
	
	@Override
	public String getName() {
		return "checkCatchParameter";
	}


	@Override
	public DeclarationKind []mayBeAttachedList() {
		return decKindList;
	}

	private static DeclarationKind []decKindList = new DeclarationKind[] { DeclarationKind.METHOD_DEC };


	@Override
	public void dsa_checkSelectorMessageSend(Expr receiverExpr, Type receiverType,
			ExprReceiverKind receiverKind, MessageWithSelectors message, Env env, MethodSignature ms) {

		int i = 1;
		for ( SelectorWithRealParameters sel : message.getSelectorParameterList() ) {
			if ( sel.getSelectorNameWithoutSpecialChars().equals("catch") ) {
				Expr expr = sel.getExprList().get(0);
				Type t = expr.getType();
				ArrayList<MethodSignature> emsList = null;
				
				emsList = t.searchMethodPublicSuperPublic("eval:1", env);
				checkCatchParameter(env, i, emsList);
			}
			++i;
		}
		
	}

	/**
	   @param env
	   @param i
	   @param emsList
	 */
	private void checkCatchParameter(Env env, int i, ArrayList<MethodSignature> emsList) {
		if ( emsList == null ) {
			/*
			 * each parameter to a catch: selector should have at least one 'eval:' method  
			 */
			addError("Parameter to the 'catch:' selector number " + i + 
					" has a type that does not define an 'eval:' method");
			return ;
		}
		else {
			for ( MethodSignature ems : emsList ) {
				ParameterDec param = ems.getParameterList().get(0);
				/*
				 * each 'eval:' method should accept one parameter whose type is sub-prototype of CyException
				 */
				Type paramType = param.getType();
				
				
				Type cyException = env.getCyException();
				if ( !(cyException.isSupertypeOf(paramType, env)) ) {
					
					boolean signalError = true;
					if ( paramType instanceof ObjectDec ) {
						ObjectDec proto = (ObjectDec ) paramType;
						if ( proto.getGenericParameterListList() != null && proto.getGenericParameterListList().size() == 1 ) {
							/* 
							 * it may be an Union
							 */
							String cyanName = proto.getIdent();
							if ( cyanName.equals("Union") ) {
		
								/*
								 * the catch parameter may be an union as in<br>
								 * {@code  
								 * { <br>
								 *     ... <br>
								 * } <br>
        					 	 *	catch: { (: ExceptionTest1Int | ExceptionTest2Int e :) control = "GT0EQ0" }; <br>
								 */
								ArrayList<GenericParameter> gpList = proto.getGenericParameterListList().get(0);
								for ( GenericParameter gp : gpList ) {
									if ( gp.getKind() != GenericParameterKind.PrototypeCyanLang &&
										 gp.getKind() != GenericParameterKind.PrototypeWithPackage ) {
										addError("The type of the parameter to the 'catch:' selector number " + i + 
												" defines an 'eval:' method that accepts a parameter that is not subtype of '" + 
												NameServer.cyanLanguagePackageName + "." + NameServer.cyExceptionPrototype + "'");
										return ;
									}
									Type unionElemType = gp.getParameter().getType(env);
									if ( !(unionElemType instanceof ObjectDec) ) {
										addError("The type of the parameter to the 'catch:' selector number " + i + 
												" defines an 'eval:' method that accepts a parameter that is not subtype of '" + 
												NameServer.cyanLanguagePackageName + "." + NameServer.cyExceptionPrototype + "'");
										return ;
									}
									if ( !(cyException.isSupertypeOf(unionElemType, env)) ) {
										addError("The type of the parameter to the 'catch:' selector number " + i + 
												" defines an 'eval:' method that accepts a parameter that is not subtype of '" + 
												NameServer.cyanLanguagePackageName + "." + NameServer.cyExceptionPrototype + "'");
										return ;
									}
								}
								signalError = false;
							}
						}
					}					
					
					
					if ( signalError ) {
						addError("The type of the parameter to the 'catch:' selector number " + i + 
								" defines an 'eval:' method that accepts a parameter that is not subtype of '" + 
								NameServer.cyanLanguagePackageName + "." + NameServer.cyExceptionPrototype + "'", 
								this.metaobjectAnnotation);
					}
					
					
				}
			}
		}
	}

}
