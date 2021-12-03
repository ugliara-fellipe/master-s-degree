/**
 *
 */
package ast;

import java.util.ArrayList;
import lexer.Symbol;
import lexer.Token;
import saci.CyanEnv;
import saci.Env;
import saci.NameServer;
import saci.Tuple2;


/**
 * This class represents selectors like
 *     add: (Int)*
 * or
 *     println: (String)+
 *
 * @author José
 *
 */
public class SelectorWithMany extends Selector {

	public SelectorWithMany(Symbol selector, Expr type) {
		super();
		this.selector = selector;
		this.typeExpr = type;
	}
	
	@Override
	public void accept(ASTVisitor visitor) {
		typeExpr.accept(visitor);
	}
	

	@Override
	public void genCyan(PWInterface pw, boolean printInMoreThanOneLine, CyanEnv cyanEnv, boolean genFunctions) {
		
		if ( cyanEnv.getCreatingInstanceGenericPrototype() ) {
			pw.print(cyanEnv.formalGenericParamToRealParam(selector.getSymbolString()));
		}
		else {
			pw.print(selector.getSymbolString());
		}
		
		pw.print(" (");
		typeExpr.genCyan(pw, printInMoreThanOneLine, cyanEnv, genFunctions);
		pw.print(")" + regularOperator.getSymbolString());

	}

	/** A selector like
	 *       add: (int)*
	 *  will have a Java name
	 *       add_int_star
	 *  In
	 *       add: (int | String | Is_a_boolean)+
	 *  the result of this method will be
	 *      left_int_CyString_Is__a__boolean_right_or_plus
	 *
	 */
	@Override
	public String getJavaName() {
		return NameServer.getJavaNameOfSelector(selector.getSymbolString()) + "_" + typeExpr.getJavaName() +
				"_" + regularOperator.getSymbolString();
	}


	public void setSelector(Symbol selector) {
		this.selector = selector;
	}
	public Symbol getSelector() {
		return selector;
	}


	public Symbol getRegularOperator() {
		return regularOperator;
	}

	public void setRegularOperator(Symbol regularOperator) {
		this.regularOperator = regularOperator;
	}

	public Expr getTypeExpr() {
		return typeExpr;
	}

	public void setTypeExpr(Expr type) {
		this.typeExpr = type;
	}



	@Override
	public String getStringType() {
		// add: (Int)*  has typeExpr  Array<Int>
		return "Array<" + typeExpr.asString() + ">";
	}

	@Override
	public void calcInterfaceTypes(Env env) {
		typeExpr.calcInternalTypes(env);
	}

	@Override
	public String getFullName(Env env) {
		return selector.getSymbolString() + " (" + typeExpr.ifPrototypeReturnsItsName(env) + ")" + regularOperator.getSymbolString();
	}
	
	@Override
	public String getName() {
		return selector.getSymbolString();
	}

	@Override
	public Tuple2<String, String> parse(SelectorLexer lexer, Env env) {
		SelectorWithRealParameters messageSelector = lexer.current();
		if ( messageSelector == null || ! messageSelector.getSelectorName().equals(selector.getSymbolString()) ) {
			return null;
		}
		ArrayList<Expr> realExprList = messageSelector.getExprList();
		if ( regularOperator.token == Token.PLUS ) {
			// one or more
			if ( realExprList == null || realExprList.size() == 0 )
				return null;
		}
		else if ( regularOperator.token == Token.QUESTION_MARK ) {
			// zero or one
			if ( realExprList != null && realExprList.size() > 1 )
				return null;
		}

		lexer.next();
		
		if ( realExprList == null || realExprList.size() == 0 ) {
			   // zero parameters, use something like "Array<Int>()"
			return new Tuple2<String, String>("Array<" + typeExpr.getType(env).getFullName() + ">()", 
					"Array<" + typeExpr.getType(env).getFullName() + ">");
		}
		else {
			int size = realExprList.size();
			String s = " [ ";
			Type formalParamType = typeExpr.getType(env);
			String formalTypeStr = formalParamType.getFullName();
			for ( Expr expr : realExprList ) {
				Type exprType = expr.getType(env);
				if ( ! formalParamType.isSupertypeOf(exprType, env) )
					return null;
				if ( formalParamType != exprType ) {
					// s += formalTypeStr + " cast: (" + expr.asString() + ")";
					boolean isUnion = false;
					int numF = -1;
					if ( formalParamType instanceof ObjectDec ) {
						ObjectDec proto = (ObjectDec ) formalParamType;
						isUnion = proto.getName().startsWith("Union<");
						for ( ArrayList<GenericParameter> gpList : proto.getGenericParameterListList() ) {
							int n = 1;
							for ( GenericParameter gp :  gpList ) {
								if ( gp.getParameter() != null ) {
									if ( gp.getParameter().getType().isSupertypeOf(exprType, env)) {
										numF = n;
										break;
									}
								}
								++n;
							}
						}
						
					}
					if ( isUnion ) {
						s += "( " + formalTypeStr + " f" + numF + ": (" + expr.asString() + ") )";
					}
					else {
						s += "( Cast<" + formalTypeStr + "> asReceiver: (" + expr.asString() + ") )";
					}
					
				}
				else {
					s += expr.asString();
				}
				if ( --size > 0 )
					s += ", ";
			}
			return new Tuple2<String, String>(s + " ] ", 
					"Array<" + typeExpr.getType(env).getFullName() + ">");
		}

	}

	@Override
	public boolean matchesEmptyInput() {
		return false;
	}

	/**
	 * astRootType should have a method<br>
	 * <code>
	 *     {@literal @}annot(gmast)<br>
	 *     func sel: Array<Type1> p1 -> astRootType<br>
	 * </code>
	 * in which Type1 is the type of this selector.
	 *      
	 */	
	
	@Override
	void setAstRootType(ObjectDec astRootType, Env env, Symbol first) {
		Tuple2<MethodDec, ExprAnyLiteral> t = astRootType.searchMethodByFeature(Selector.annotAstBuildingMethod);
		if ( t == null ) {
			env.error(first,  "Prototype '" + astRootType.getFullName() + "' should have a method with annotation '"
					+ Selector.annotAstBuildingMethod + "'. But it does not.");
		}
		else {
			MethodSignature mss = t.f1.getMethodSignature();
			if ( !(mss instanceof ast.MethodSignatureWithSelectors) || ( (MethodSignatureWithSelectors) mss).getParameterList() == null || 
					( (MethodSignatureWithSelectors) mss).getParameterList().size() != 1	) {
				env.error(first,  "Method '" + t.f1.getName() + "' of Prototype '" + astRootType.getFullName() + 
						"' should have exactly one selector and it cannot have an operator name such as '+'");
			}
			else {
				MethodSignatureWithSelectors ms = (MethodSignatureWithSelectors) mss;
				if ( ms.getParameterList().size() != 1 ) {
					env.error(first,  "Method '" + t.f1.getName() + "' of Prototype '" + astRootType.getFullName() + 
							"' should have exactly one parameter");
				}
				else {
					Type paramType = ms.getParameterList().get(0).getType(env);
					if ( !(paramType instanceof ObjectDec) ) {
						env.error(first,  "Method '" + t.f1.getName() + "' of Prototype '" + astRootType.getFullName() + 
								"' should have exactly one parameter whose type is a prototype (it cannot be Dyn or an interface)");
					}
					ObjectDec protoType = (ObjectDec ) paramType;
					if ( ! protoType.getFullName(env).equals("Array<" + typeExpr.getType(env).getFullName() + ">") ) {
						env.error(first,  "Method '" + t.f1.getName() + "' of Prototype '" + astRootType.getFullName() + 
								"' should have exactly one parameter whose type is '" + "Array<" + typeExpr.getType(env).getFullName() + ">'" );
					}
					if ( ms.getReturnType(env) != astRootType ) {
						env.error(first,  "Method '" + t.f1.getName() + "' of Prototype '" + astRootType.getFullName() + 
								"' should have '" + astRootType.getFullName() + " as return type");
					}
					
					this.astRootType = astRootType;
				}
			}
		}
	}

	
	
	/**
	 * the selector. It is "add:" in
	 *     add: (int)*
	 */
	private Symbol selector;

	/**
	 * the typeExpr between parenthesis such as int in
	 *     add: (int)*
	 * it may be several types separated by | such as in
	 *     add: (int | String | Person)+
	 */
	private Expr typeExpr;
	/**
	 *   + or *. It is Token.PLUS in
	 *        add: (int)+
	 */
	private Symbol regularOperator;
}
