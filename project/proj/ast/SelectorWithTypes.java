/**
 *
 */
package ast;

import java.util.ArrayList;
import lexer.Symbol;
import saci.CyanEnv;
import saci.Env;
import saci.NameServer;
import saci.Tuple2;

/**
 * Represents a selector with types such as
 *  1. "add: Int"  in
 *    public fun (add: Int)+
 *  2. "format: String, Int" in
 *    public fun (format: String, Int  println: (String)*)
 *
 * @author José
 *
 */
public class SelectorWithTypes extends Selector {

	public SelectorWithTypes(Symbol selector,
			ArrayList<Expr> typeList) {
		super();
		this.selector = selector;
		this.typeList = typeList;
	}

	@Override
	public void accept(ASTVisitor visitor) {
		for ( Expr e : typeList ) {
			e.accept(visitor);
		}
	}
	
	
	
	public SelectorWithTypes(Symbol selector) {
		this.selector = selector;
		typeList = new ArrayList<Expr>();
	}

	public void addTypeOneMany( Expr aType ) {
		typeList.add(aType);
	}

	@Override
	public void genCyan(PWInterface pw, boolean printInMoreThanOneLine, CyanEnv cyanEnv, boolean genFunctions) {

		if ( cyanEnv.getCreatingInstanceGenericPrototype() ) {
			pw.print(cyanEnv.formalGenericParamToRealParam(selector.getSymbolString()));
		}
		else {
			pw.print(selector.getSymbolString());
		}		
		pw.print( " " );
		
		int size = typeList.size();
		for ( Expr p : typeList ) {
			p.genCyan(pw, false, cyanEnv, genFunctions);
			--size;
			if ( size > 0 )
				pw.print(", ");
		}
	}

	/**
	 * return the Java name of a method that has this selector. If the
	 * selector is
	 *           case: char
	 *  the generated name will be
	 *      case_p_char
	 *  if it is
	 *  	add: int, String, Shape_Figure
	 *  the generated name will be
	 *      add_p_int_p_CyString_p_Shape__Figure
	 */
	@Override
	public String getJavaName() {
		String s;
		s = NameServer.getJavaNameOfSelector(selector.getSymbolString());
		int i = s.indexOf(':');
		if ( i >= 0 )
		   s = s.substring(0, i);
		typeList.size();
		for ( Expr p : typeList )
			s = s + "_p_" + p.getJavaName();

		return null;
	}


	public void setTypeList(ArrayList<Expr> typeList) {
		this.typeList = typeList;
	}
	public ArrayList<Expr> getTypeList() {
		return typeList;
	}

	@Override
	public String getStringType() {
		if ( typeList.size() == 0 )
			return "Any";
		else {
			String s = "";
			int size= typeList.size();
			for ( Expr aType : typeList) {
				s = s + aType.asString();
				if ( --size > 0 )
					s = s + ", ";
			}
			return typeList.size() > 1 ? "Tuple<" + s + ">" : s;
		}
	}

	@Override
	public void calcInterfaceTypes(Env env) {
		for ( Expr aType : typeList )
			aType.calcInternalTypes(env); 
	}

	@Override
	public String getFullName(Env env) {
		String s = selector.getSymbolString();
		if ( typeList != null && typeList.size() > 0 ) {
			int size = typeList.size();
			for ( Expr e : typeList ) {
				s = s + e.ifPrototypeReturnsItsName(env);
				if ( --size > 0 )
					s = s + " ";
			}
		}
		return s;
	}
	
	@Override
	public String getName() {
		return selector.getSymbolString();
	}	

	
	@Override
	public Tuple2<String, String> parse(SelectorLexer lexer, Env env) {
		SelectorWithRealParameters messageSelector = lexer.current();
		if ( messageSelector == null ) 
			return null;
		if ( ! messageSelector.getSelectorName().equals(selector.getSymbolString()) ) 
			return null;
		else {
			ArrayList<Expr> realExprList = messageSelector.getExprList();
			if ( realExprList.size() != typeList.size() )
				return null;
			else {
				if ( typeList.size() == 0 ) {
					lexer.next();
					return new Tuple2<String, String>("Any", "Any");
				}
				String tupleType = "Tuple<";
				int n = 0;
				int sizeTypeList = typeList.size();
				for ( Expr expr : realExprList ) {
					Type paramType = typeList.get(n).getType(env);
					if ( ! paramType.isSupertypeOf(expr.getType(env), env) )
						return null;
					tupleType += "f" + (n+1) + ", " + paramType.getFullName();
					if ( --sizeTypeList > 0 ) {
						tupleType += ", ";
					}
					++n;
				}
				tupleType += ">";
				lexer.next();
				if ( realExprList.size() == 1 ) 
					return new Tuple2<String, String>(realExprList.get(0).asString(), typeList.get(0).getType(env).getFullName() );
				else {
					String s = "[. ";
					int size = realExprList.size();
					for ( Expr expr : realExprList ) {
						s += expr.asString();
						if ( --size > 0 ) {
							s += ", ";
						}
					}
					s += " .] ";
					return new Tuple2<String, String>(s, tupleType) ;
				}
			}
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
	 *     func sel: Type1 p1 Type2 p2, ... Typen pn -> astRootType<br>
	 * </code>
	 * in which Type1, Type2, ... Typen are the type of the parameters of this selector.
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
				if ( ms.getParameterList().size() != typeList.size() ) {
					env.error(first,  "Method '" + t.f1.getName() + "' of Prototype '" + astRootType.getFullName() + 
							"' should have exactly " + typeList.size() + " parameters");
				}
				else {
					ArrayList<ParameterDec> paramList = ms.getParameterList();
					int n = 0;
					for ( Expr typeExpr : typeList ) {
						if ( typeExpr.getType(env) != paramList.get(n).getType(env) ) {
							env.error(first,  "Parameter number '" + n +  "' of Method '" + t.f1.getName() + 
									"' of Prototype '" + astRootType.getFullName() + 
									"' should have type '" + typeExpr.getType(env).getFullName() + "'");
						}
						++n;
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
	 * the selector. It is "println: in
	 *     println: String
	 * and "amount:" in
	 *     amount:  // no parameters
	 * In the first case, typeList contains a single element, "String"
	 */
	private Symbol selector;
	/**
	 * list of the types associated with selector. It may be empty
	 * for a selector may not have parameters.
	 */
	private ArrayList<Expr>  typeList;

}
