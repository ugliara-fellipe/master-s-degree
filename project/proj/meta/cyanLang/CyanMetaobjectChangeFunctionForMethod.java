package meta.cyanLang;

import java.util.ArrayList;
import ast.CompilationUnit;
import ast.CyanMetaobjectWithAtAnnotation;
import ast.Expr;
import ast.ExprLiteralCyanSymbol;
import ast.ExprLiteralString;
import ast.ExprMessageSendWithSelectorsToExpr;
import ast.MessageWithSelectors;
import ast.MetaobjectArgumentKind;
import ast.MethodDec;
import ast.MethodSignature;
import ast.MethodSignatureOperator;
import ast.MethodSignatureUnary;
import ast.MethodSignatureWithSelectors;
import ast.ParameterDec;
import ast.ProgramUnit;
import ast.SelectorWithParameters;
import ast.Type;
import meta.CyanMetaobjectWithAt;
import meta.DeclarationKind;
import meta.IActionMessageSend_dsa;
import saci.Env;
import saci.NameServer;
import saci.Tuple2;

public class CyanMetaobjectChangeFunctionForMethod extends CyanMetaobjectWithAt implements IActionMessageSend_dsa {

	public CyanMetaobjectChangeFunctionForMethod() {
		super(MetaobjectArgumentKind.ZeroParameter);
	}

	@Override
	public String getName() {
		return "changeFunctionForMethod";
	}

	
	
	@Override
	public Tuple2<StringBuffer, Type> dsa_analyzeReplaceMessageWithSelectors( 
			ExprMessageSendWithSelectorsToExpr messageSendExpr, Env env) { 
		
		CyanMetaobjectWithAtAnnotation annotation = this.getMetaobjectAnnotation();
		boolean ok = true;
		boolean withSelf = false;
		if ( !(annotation.getDeclaration() instanceof MethodDec) ) { ok = false; }
		else {
			String attachedMethodName = ((MethodDec) annotation.getDeclaration()).getName();
			if ( !attachedMethodName.equals("functionForMethod:1") && 
				 !(withSelf = attachedMethodName.equals("functionForMethodWithSelf:1"))	) {
				ok = false;
			}
		}
		
		
		if ( ! ok ) {
			//String s = ((MethodDec) annotation.getDeclaration()).getName();
			this.addError("This metaobject can only be attached to methods 'functionForMethod:' and 'functionForMethodWithSelf:' of prototype 'Any'");
			return null;
		}
		
		MessageWithSelectors message = messageSendExpr.getMessage();
		Expr paramExpr = message.getSelectorParameterList().get(0).getExprList().get(0);
		String methodName;
		if ( paramExpr instanceof ExprLiteralString) {
			ExprLiteralString es = (ExprLiteralString ) paramExpr;
			methodName = es.getStringJavaValue().toString();
			
		}
		else if ( paramExpr instanceof ExprLiteralCyanSymbol ) {
			ExprLiteralCyanSymbol es = (ExprLiteralCyanSymbol ) paramExpr;
			methodName = es.getStringJavaValue().toString();
		}
		else {
			CompilationUnit cunit = (CompilationUnit ) messageSendExpr.getFirstSymbol().getCompilationUnit();
			
			this.addError("The parameter to the annotation to 'functionForMethod:' or 'functionForMethodWithSelf:' "
					+ "should be a literal string. The annotation is in " + 
					  "line " + messageSendExpr.getFirstSymbol().getLineNumber() + " of " +
			       	"prototype '" +  cunit.getPublicPrototype().getFullName() + "' of file '" + cunit.getFullFileNamePath()  
				        + "'" );
			
			return null;
		}
		if ( env.getCurrentObjectDec() == null ) { return null; }
		
		methodName = NameServer.removeQuotes(methodName);
		
		String receiverAsString;
		
		Expr receiverExpr = messageSendExpr.getReceiverExpr();
		ProgramUnit receiverType = null;
		if ( receiverExpr == null ) {
			receiverType = env.getCurrentObjectDec();
			receiverAsString = "self";
		}
		else {
			if ( !(receiverExpr.getType() instanceof ProgramUnit) ) {
				this.addError("Methods 'functionForMethod:' and 'functionForMethodWithSelf:' can only be applied to Cyan prototypes. This is an internal error");
				return null;
			}
			receiverType = (ProgramUnit ) receiverExpr.getType();
			receiverAsString = receiverExpr.asString();
		}
		
		ArrayList<MethodSignature> msList = receiverType.searchMethodPrivateProtectedPublicSuperProtectedPublic(methodName, env);
		if ( msList == null || msList.size() == 0 ) {
			CompilationUnit cunit = (CompilationUnit ) messageSendExpr.getFirstSymbol().getCompilationUnit();
			this.addError("Method '" + methodName + "' that is parameter to the annotation 'functionForMethod:' or 'functionForMethodWithSelf:' in " + 
			  "line " + messageSendExpr.getFirstSymbol().getLineNumber() + " of " +
	       	"prototype '" +  cunit.getPublicPrototype().getFullName() + "' of file '" + cunit.getFullFileNamePath()  
		        + "' was not found. Make sure it is correctly spelled." + 
		           " It should be something like 'with:2 param:1 do:1'. Note the spaces. The number is the number of parameters");
			return null;
		}
		MethodSignature ms = msList.get(0);
		
		StringBuffer code = new StringBuffer();
		code.append(" { " );
		if ( ms instanceof MethodSignatureUnary ) {
			  /*
			   *         x getMethod: "open"
			   *  should be replaced by
			   *        { ^x open }
			   */
			MethodSignatureUnary msu = (MethodSignatureUnary ) ms;
			if ( withSelf ) {
				String myselfName = env.getNewUniqueVariableName();
				code.append( "(: " + receiverType.getFullName() + " " + myselfName + " :) ^" + myselfName + " " +  msu.getName() );
			}
			else {
				code.append( " ^" + receiverAsString + " " + msu.getName() );
			}
		}
		else if ( ms instanceof MethodSignatureOperator ) {
			MethodSignatureOperator mso = (MethodSignatureOperator ) ms;
			if ( mso.getOptionalParameter() == null ) {
				// unary method
				if ( withSelf ) {
					String myselfName = env.getNewUniqueVariableName();
					code.append( "(: " + receiverType.getFullName() + " " + myselfName + " :) ^ " + mso.getNameWithoutParamNumber() + 
							" " + myselfName + " "  );
				}
				else {
					code.append( " ^ " +  mso.getNameWithoutParamNumber() + " " + receiverAsString );
				}
			}
			else {
				// with parameter
				String paramName = env.getNewUniqueVariableName();
				if ( withSelf ) {
					String myselfName = env.getNewUniqueVariableName();

					code.append( " (: eval: " + receiverType.getFullName() + " " + myselfName + " eval: " +
							mso.getOptionalParameter().getType().getFullName() + " " + paramName + " " + 
				            ":) ^" + myselfName + " " +  mso.getNameWithoutParamNumber() + " " + paramName);
				}
				else {
					code.append( " (: " + mso.getOptionalParameter().getType().getFullName() + " " + paramName + " :) ^" + receiverAsString + "  " +  
				        mso.getNameWithoutParamNumber() + " " + paramName);
				}
			}
			
		}
		else if ( ms instanceof MethodSignatureWithSelectors ) {
			MethodSignatureWithSelectors mss = (MethodSignatureWithSelectors ) ms;
			/*
			 *     x getMethod: "at:1 put:2 with:1"
			 * 
			 * 	   { (: eval: Int p1 eval: Char p2, Float p3 eval: Int p4 :) 
			 *        ^x at: p1 put: p2, p3 with: p4 } 
			 */
			code.append("(: ");
			int i = 0;
			int size2 = mss.getSelectorArray().size();
			String myselfName = env.getNewUniqueVariableName();

			if ( withSelf ) {
				code.append( "eval: " + receiverType.getFullName() + " " + myselfName + " ");
			}
			ArrayList<String> paramNameList = new ArrayList<>();
			for ( SelectorWithParameters sel : mss.getSelectorArray() ) {
				code.append(  "eval: ");
				int size = sel.getParameterList().size();
				for ( ParameterDec param : sel.getParameterList() ) {
					String paramName = env.getNewUniqueVariableName();
					paramNameList.add(paramName);
					code.append( param.getType().getFullName() + " " + paramName);
					if ( --size > 0 ) 
						code.append(", ");
					++i;
				}
				if ( --size2 > 0 ) {
					code.append(" ");
				}
			}
			code.append(" :) ^");
			if ( withSelf ) {
				code.append(myselfName + " ");
			}
			else {
				code.append(messageSendExpr.getReceiverExpr().asString() + " ");
			}
			i = 0;
			int sizeSA = mss.getSelectorArray().size();
			for ( SelectorWithParameters sel : mss.getSelectorArray() ) {
				code.append(  sel.getName() + " ");
				int size = sel.getParameterList().size();
				
				for ( @SuppressWarnings("unused") ParameterDec param : sel.getParameterList() ) {
					code.append( " " + paramNameList.get(i));
					if ( --size > 0 ) 
						code.append(", ");
					++i;
				}
				if ( --sizeSA > 0 ) {
					code.append(" ");
				}
			}
			
			
			/*
			 * void createNewGenericPrototype(Symbol symUsedInError, CompilationUnitSuper compUnit, ProgramUnit currentPU,
				      String fullPrototypeName, String errorMessage);
			 */
			
					
		}
		String functionName = (withSelf ? ms.getFunctionNameWithSelf(receiverType.getFullName()) : ms.getFunctionName());
		
		Type type = env.createNewGenericPrototype(annotation.getFirstSymbol(), env.getCurrentCompilationUnit(), 
				env.getCurrentProgramUnit(), 
				NameServer.cyanLanguagePackageName + "." + functionName,
		            "Error caused by method dsa_codeToAddAtMetaobjectAnnotation of metaobject '" +
		            annotation.getCyanMetaobject().getName() + "' "
		            );

		code.append(" } ");
		
	
		
		return new Tuple2<StringBuffer, Type>(code, type);
	}

	
	@Override
	public DeclarationKind []mayBeAttachedList() {
		return decKindList;
	}

	private static DeclarationKind []decKindList = new DeclarationKind[] { DeclarationKind.METHOD_DEC };



}
