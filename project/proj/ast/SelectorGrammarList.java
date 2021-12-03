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
 * This class represents composite selectors such as
 *    (case: Char do: Function<Nil>)+
 *
 * @author José
 *
 */
public class SelectorGrammarList extends SelectorGrammar {


	public SelectorGrammarList(ArrayList<Selector> selectorArray, Symbol firstSymbol) {
		super(selectorArray, firstSymbol);
	}

	@Override
	public void genCyan(PWInterface pw, boolean printInMoreThanOneLine, CyanEnv cyanEnv, boolean genFunctions) {

		int size = this.getSelectorArray().size();
		pw.print("(");
		for ( Selector s : this.getSelectorArray() ) {
			s.genCyan(pw, printInMoreThanOneLine, cyanEnv, genFunctions);
			if ( --size > 0 )
				pw.print(" ");
		}
		pw.print(")");
		if ( this.getRegularOperator() != null )
			pw.print(this.getRegularOperator().getSymbolString());
	}

	/**
	 * return the Java name of a method that has this selector. If the
	 * selector is
	 *           (case: char do: Proto)+
	 *  the generated name will be
	 *      _left_s_case_p_char_s_do_p_Proto_right_PLUS
	 *  if it is
	 *  	     (case: char do: Proto)
	 *  the generated name will be
	 *      left_s_case_p_char_s_do_p_Proto_right
	 */
	@Override
	public String getJavaName() {
		String s = "left";
		for ( Selector selector : selectorArray ) {
			s = s + "_s_" + selector.getJavaName();
		}
		s = s + "_right";
		if ( regularOperator != null )
			s = s + "_" + regularOperator.getSymbolString();
		return s;
	}


	/**
	   @param lexer
	   @param env
	   @return
	 * @throws ExceptionSelectorExpected
	 */
	public Tuple2<String, String> parseOne(SelectorLexer lexer, Env env) throws ExceptionSelectorExpected {

		String s, aType;
		int size = selectorArray.size();
		if ( size == 1 ) {
			Selector gmSelector = selectorArray.get(0);
			Tuple2<String, String> t = gmSelector.parse(lexer, env);
			if ( t == null ) {
				return null;
			}
			s = t.f1;
			aType = t.f2;
		}
		else {
			int n = 0;
			s = " [.";
			int sizeSelectorArray = selectorArray.size();
			aType = "Tuple<";
			for ( Selector formalSelector : selectorArray ) {
				/*
				SelectorWithRealParameters currentSelector = lexer.current();
				if ( currentSelector == null ) {
					Token regOp = formalSelector.getRegularOperator();
					if ( regOp == Token.QUESTION_MARK ) {

					}
					else if( regOp == Token.)
					return null;
				}
				*/
				Tuple2<String, String> t = formalSelector.parse(lexer, env);
				if ( t == null ) {
					throw new ExceptionSelectorExpected();
				}
				String exprStr = t.f1;
				s = s + " " + exprStr;
				aType += "f" + (n+1) + ", " + t.f2;
				if ( --sizeSelectorArray > 0 ) {
					s = s + ", ";
					aType += ", ";
				}
				++n;
			}
			s =  s + " .] ";
			aType += ">";
		}
		return new Tuple2<String, String>(s, aType);
	}


	@Override
	public Tuple2<String, String> parse(SelectorLexer lexer, Env env) {

		Tuple2<String, String> t;
		try {
			t = parseOne(lexer, env);
		}
		catch ( ExceptionSelectorExpected e ) {
			return null;
		}


		if ( regularOperator == null ) {
			return t;
		}
 		else if ( regularOperator.token == Token.QUESTION_MARK ) {
			if ( t == null ) {
				return new Tuple2<String, String>("( " + this.getStringType() + "() none: Any ) ",
						this.getStringType() );

			}
			else {
				return new Tuple2<String, String>("( " + this.getStringType() + "() some:  " + t.f1 + " ) ",
						this.getStringType() );
			}
		}
		else {
			if ( regularOperator.token == Token.MULT ) {
				if ( t == null ) {
					  // it may be zero times
					return new Tuple2<String, String>(this.getStringType() + "() ", this.getStringType());
				}
			}
		}

		if ( t == null )
			return null;
		/*
		 * control only reaches this point at runtime if regularOperator.token == Token.MULT or Token.PLUS
		 */
		ArrayList<String> valueList = new ArrayList<>();
		valueList.add(t.f1);
		String s = " [ ";
		while ( true ) {
			try {
				t = parseOne(lexer, env);
			}
			catch ( ExceptionSelectorExpected e ) {
				return null;
			}
			if ( t == null ) {
				int size = valueList.size();
				for ( String value : valueList ) {
					s += value;
					if ( --size > 0 )
						s += ", ";
				}
				return new Tuple2<String, String>(s + " ] ",
						"Array<" + this.getStringType() + ">");

			}
			valueList.add(t.f1);
		}

	}

	@Override
	public String getStringType() {
		String s;
		int size = selectorArray.size();
		if ( size == 1 ) {
			s = selectorArray.get(0).getStringType();
			if ( regularOperator != null ) {
				switch ( regularOperator.token ) {
				case QUESTION_MARK:
					   // example: (add: Int)?,    UUnion<Int>
					return "Union<some, " + s + ", none, Any>";
				case PLUS:
				case MULT:
					   // example: (add: Int)*
					return "Array<" + s + ">";
				default:
					return "compile time error";
				}
			}
			else {
				// no operator. Example:  (add: int)
				return s;
			}
		}
		else {
			// size > 1
			s = "";
			int sizeSelectorArray = selectorArray.size();
			for ( Selector selector : selectorArray ) {
				s = s + selector.getStringType();
				if ( --sizeSelectorArray > 0 )
					s = s + ", ";
			}
			s = "Tuple<" + s + ">";
			if ( regularOperator != null ) {
				switch ( regularOperator.token ) {
				case QUESTION_MARK:
					   // example: (add: Int with: String)?,    Union<Int, String>
					return "Union<some, " + s + ", none, Any>";
				case PLUS:
				case MULT:
					   // example: (add: Int to: String)*
					return "Array<" + s + ">";
				default:
					return "compile time error";
				}
			}
			else {
				// no operator. Example:  (add: Int with: String)
				return s;
			}
		}
	}





	@Override
	public ProgramUnit getParameterType(Env env) {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public String getName() {
		// TODO Auto-generated method stub
		return null;
	}

	/**
	 * astRootType should have a method<br>
	 * <code>
	 *     {@literal @}annot(gmast)<br>
	 *     func sel: Array{@literal <}Type1> p -> astRootType<br>
	 * </code><br>
	 * if the regular operator is + or *.
	 * And it should have a method<br>
	 * <code>
	 *     {@literal @}annot(gmast)<br>
	 *     func sel: Type1 p some: Boolean b -> astRootType<br>
	 * </code><br>
	 * if the regular operator is ?.
	 * And it should have a method<br>
	 * <code>
	 *     {@literal @}annot(gmast)<br>
	 *     func sel: Type1 -> astRootType<br>
	 * </code><br>
	 * if the regular operator does not appear.<br>
	 *
	 * In any case, the root type of the array of selectors should be Type1.
	 *
	 *
	 *
	 */
	@Override
	void setAstRootType(ObjectDec astRootType, Env env, Symbol first) {
		Tuple2<MethodDec, ExprAnyLiteral> t = astRootType.searchMethodByFeature(Selector.annotAstBuildingMethod);
		if ( t == null ) {
			env.error(first,  "Prototype '" + astRootType.getFullName() + "' should have a method with annotation '"
					+ Selector.annotAstBuildingMethod + "'. But it does not.");
			return ;
		}

		MethodSignature mss = t.f1.getMethodSignature();
		if ( !(mss instanceof ast.MethodSignatureWithSelectors) || ( (MethodSignatureWithSelectors) mss).getParameterList() == null 	) {
			env.error(first,  "Method '" + t.f1.getName() + "' of Prototype '" + astRootType.getFullName() +
					"' should have exactly one selector and it cannot have an operator name such as '+'");
		}
		MethodSignatureWithSelectors ms = (MethodSignatureWithSelectors) mss;
		ObjectDec type1 = null;

		if ( this.regularOperator.token == Token.MULT || this.regularOperator.token == Token.PLUS ) {
			/*
			 * 	 func sel: Array<Type1> p -> astRootType
			 */
			if ( ms.getParameterList().size() != 1 )  {
				env.error(first,  "Method '" + t.f1.getName() + "' of Prototype '" + astRootType.getFullName() +
						"' should have exactly one selector and it cannot have an operator name such as '+'");
			}
			Type typeParam = ms.getParameterList().get(0).getType(env);
			if ( !(typeParam instanceof ObjectDec) ) {
				env.error(first,  "Method '" + t.f1.getName() + "' of Prototype '" + astRootType.getFullName() +
						"' should have exactly one parameter whose type is a prototype");
			}
			ObjectDec protoParam = (ObjectDec ) typeParam;
			if ( protoParam.getFullName().equals(NameServer.cyanLanguagePackageName + ".Array<") ||
					protoParam.getFullName().equals("Array<") ) {
				GenericParameter gp = protoParam.getGenericParameterListList().get(0).get(0);
				ProgramUnit pu = env.searchPackagePrototype(gp.getFullName(env), first);
				if ( !(pu instanceof ObjectDec) ) {
					env.error(first,  "Method '" + t.f1.getName() + "' of Prototype '" + astRootType.getFullName() +
							"' should have exactly one parameter Array<T> and T should be a prototype");
				}
				else {
					type1 = (ObjectDec ) pu;
				}
			}
		}
		else if ( this.regularOperator.token == Token.QUESTION_MARK ) {
			/*
			 *     func sel: Type1 p some: Boolean b -> astRootType
			 */


			if ( ms.getSelectorArray().size() != 2 )  {
				env.error(first,  "Method '" + t.f1.getName() + "' of Prototype '" + astRootType.getFullName() +
						"' should have exactly two selectors and it cannot have an operator name such as '+'. "
						+ "Its name should be like 'sel: Type p some: Boolean b -> AstRootType");
			}
			if ( ms.getSelectorArray().get(0).getParameterList().size() != 1 ||
				 !(ms.getSelectorArray().get(0).getParameterList().get(0).getType(env) instanceof ObjectDec)
					) {
				env.error(first,  "The first selector of Method '" + t.f1.getName() + "' of Prototype '" + astRootType.getFullName() +
						"' should have exactly one parameter whose type is a prototype. "
						+ "Its name should be like 'sel: Type p some: Boolean b -> AstRootType");

			}
			if ( ms.getSelectorArray().get(1).getParameterList().size() != 1 ||
				 !ms.getSelectorArray().get(1).getName().equals("some:") ||
				 ms.getSelectorArray().get(1).getParameterList().get(0).getType(env) != Type.Boolean
						) {
					env.error(first,  "The second selector of Method '" + t.f1.getName() + "' of Prototype '" + astRootType.getFullName() +
							"' should have exactly one parameter whose type is Boolean. "
							+ "Its name should be like 'sel: Type p some: Boolean b -> AstRootType");
			}
			type1 = (ObjectDec ) ms.getSelectorArray().get(0).getParameterList().get(0).getType(env);

		}
		else {
			/*
			 *     func sel: Type1 -> astRootType
			 */
			if ( ms.getSelectorArray().size() != 1 )  {
				env.error(first,  "Method '" + t.f1.getName() + "' of Prototype '" + astRootType.getFullName() +
						"' should have exactly one selector and it cannot have an operator name such as '+'. "
						+ "Its name should be like 'sel: Type p -> AstRootType");
			}
			if ( ms.getSelectorArray().get(0).getParameterList().size() != 1 ||
				 !(ms.getSelectorArray().get(0).getParameterList().get(0).getType(env) instanceof ObjectDec)
					) {
				env.error(first,  "The only selector of Method '" + t.f1.getName() + "' of Prototype '" + astRootType.getFullName() +
						"' should have exactly one parameter whose type is a prototype. "
						+ "Its name should be like 'sel: Type p -> AstRootType");

			}
			type1 = (ObjectDec ) ms.getSelectorArray().get(0).getParameterList().get(0).getType(env);

		}


		/**
		 * stopped here. type1 should be the type of the selectors together. See the example below.
		 *
              (aaa: (Int)+ (bbb: String | ccc: Char)* (ddd: Float eee: Char)+ )+

              object A
                  @annot(gmast)
                  func build: Array<B> array -> A { ... }
              end

              object B
                  @annot(gmast)
                  func build: Array<Int> a, Array<C> b, Array<D> c {  ... }
              end

              object C
                  @annot(gmast)
                  func bbb: String -> C { ... }
                  @annot(gmast)
                  func ccc: Char -> C { ... }
              end

              object D
                  @annot(gmast)
                  func build: Float d, Char e -> D { ... }

      		 *
		 *
		 */

		/*
		t = type1.searchMethodByFeature(Selector.annotAstBuildingMethod);
		if ( t == null ) {
			env.error(first,  "Prototype '" + type1.getFullName() + "' should have a method with annotation '"
					+ Selector.annotAstBuildingMethod + "'. But it does not.");
		}
		else {
			mss = t.f1.getMethodSignature();
			if ( !(mss instanceof ast.MethodSignatureWithSelectors) || ( (MethodSignatureWithSelectors) mss).getParameterList() == null ||
					( (MethodSignatureWithSelectors) mss).getParameterList().size() != 1	) {
				env.error(first,  "Method '" + t.f1.getName() + "' of Prototype '" + type1.getFullName() +
						"' should have exactly one selector and it cannot have an operator name such as '+'");
			}
			else {
				int numParam = 0;
				for ( Selector sel : this.selectorArray ) {
					numParam += sel.get
				}
				ms = (MethodSignatureWithSelectors) mss;
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
		*/

	}



}
