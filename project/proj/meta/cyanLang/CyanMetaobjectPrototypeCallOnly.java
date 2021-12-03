/**
  
 */
package meta.cyanLang;

import ast.CompilationUnit;
import ast.Expr;
import ast.MessageWithSelectors;
import ast.MetaobjectArgumentKind;
import ast.MethodDec;
import ast.MethodSignature;
import ast.Type;
import error.ErrorKind;
import meta.CyanMetaobjectWithAt;
import meta.DeclarationKind;
import meta.ExprReceiverKind;
import meta.ICheckDeclaration_ati3;
import meta.ICheckMessageSend_dsa;
import meta.ICompiler_ati;
import saci.Env;

/** metaobject prototypeCallOnly should be attached to a method. The method
 * should only be called if the receiver of the message is a prototype. 
 * 
   @author José
 */

public class CyanMetaobjectPrototypeCallOnly extends CyanMetaobjectWithAt
             implements ICheckMessageSend_dsa, ICheckDeclaration_ati3 {

	public CyanMetaobjectPrototypeCallOnly() {
		super(MetaobjectArgumentKind.ZeroParameter);
	}

	@Override
	public String getName() {
		return "prototypeCallOnly";
	}


	
	
	@Override
	public void dsa_checkSelectorMessageSendMostSpecific(Expr receiverExpr, Type receiverType,
			ExprReceiverKind receiverKind, MessageWithSelectors message, Env env, MethodSignature ms, Type mostSpecificReceiver) {
		if ( receiverKind != ExprReceiverKind.PROTOTYPE_R ) {
			addError("The receiver of the message send '" + receiverExpr.asString() + " " + message.asString()  + 
					"' " + " in line " + receiverExpr.getFirstSymbol().getLineNumber() +
					" of prototype '" +
					((CompilationUnit ) receiverExpr.getFirstSymbol().getCompilationUnit()).getPublicPrototype().getFullName()  + 
					 
					"' should be a prototype");
			return ;
		}
		else {
			MethodDec md = (MethodDec ) this.getAttachedDeclaration();
			if ( mostSpecificReceiver != receiverType ) {
				addError("It was expected that the receiver was '" + md.getDeclaringObject().getFullName() + "'");
				return ;
			}
		}
	}
	

	@Override
	public void ati3_checkDeclaration(ICompiler_ati compiler_ati) {
		if ( compiler_ati.isCurrentProgramUnitInterface() )
			compiler_ati.error(this.metaobjectAnnotation.getFirstSymbol(), "This metaobject cannot be attached to a method of an interface", this.getName(), ErrorKind.metaobject_error);
		MethodDec md = (MethodDec ) this.getAttachedDeclaration();
		md.setAllowAccessToInstanceVariables(false);
	}

	
	@Override
	public DeclarationKind []mayBeAttachedList() {
		return decKindList;
	}

	private static DeclarationKind []decKindList = new DeclarationKind[] { DeclarationKind.METHOD_DEC };

	
}
