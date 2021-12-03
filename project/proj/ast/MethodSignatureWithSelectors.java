/**
   Represents the method signature of a non-grammar method that is not a
   unary method. That is, a regular method such as
          fun width: (:w int)  height: (:h int) -> Rectangle [ ... ]
   or
          fun at: (:index int) put:  (:value String) [ ... ]

 *
 */
package ast;

import java.util.ArrayList;
import lexer.Symbol;
import saci.CyanEnv;
import saci.Env;
import saci.NameServer;
/**
 * @author José
 *
 */
public class MethodSignatureWithSelectors extends MethodSignature {


	public MethodSignatureWithSelectors(
			   ArrayList<SelectorWithParameters> selectorArray,
			   boolean indexingMethod, MethodDec method) {
		super(method);
		this.selectorArray = selectorArray;
		this.indexingMethod = indexingMethod;
	}

	@Override
	public void accept(ASTVisitor visitor) {
		visitor.preVisit(this);
		
		for ( SelectorWithParameters sel : selectorArray ) {
			sel.accept(visitor);
		}
		visitor.visit(this);
	}	
	
	
	@Override
	public void genCyan(PWInterface pw, boolean printInMoreThanOneLine, CyanEnv cyanEnv, boolean genFunctions) {

		if ( printInMoreThanOneLine ) {
			// super.genCyan(pw, printInMoreThanOneLine, cyanEnv);			
			int size = selectorArray.size();
			if ( indexingMethod )
				pw.print(" [] ");
			for ( SelectorWithParameters s : selectorArray ) {
				s.genCyan(pw, PWCounter.printInMoreThanOneLine(s), cyanEnv, genFunctions );
				if ( --size > 0 ) { pw.println(""); pw.printIdent("    "); }
			}
		}
		else {
			// super.genCyan(pw, false, cyanEnv);			
			if ( indexingMethod )
				pw.print(" [] ");
			for ( SelectorWithParameters s : selectorArray )
				s.genCyan(pw, false, cyanEnv, genFunctions);
		}
		super.genCyan(pw, printInMoreThanOneLine, cyanEnv, genFunctions);
	}

	/**
	 * 	In the general case:
	 *         selectorName + ("_p" + typeName)* +
	 *         ("_s" + selectorName + ("_p" + typeName)* )+
	 *
	 */

	@Override
	public String getJavaName() {
		if ( javaName == null ) {
			/*
			int size = 0;
			javaName = "";
			for ( SelectorWithParameters selector : selectorArray ) {
				if ( size > 0 )
					javaName = javaName + "_s_";
				javaName = javaName + selector.getJavaName();
				size++;
			}
			*/
			javaName = NameServer.getJavaNameOfMethod(selectorArray);

		}
		return javaName;
	}



	@Override
	public void genJava(PWInterface pw, Env env, boolean isMultiMethod) {
		
		super.genJava(pw, env, isMultiMethod);
		pw.print(" ");
		String javaName1 = getJavaName();
		
		if ( isMultiMethod ) {
			/*
			 * a method that is part of a multi-method. The method name is changed to use the parameter types.
			 * The methods are transformed into private methods whose names end with "__"
			 */
			StringBuffer methodJavaName = new StringBuffer(javaName1);
			int size = selectorArray.size();
			for ( SelectorWithParameters s : selectorArray ) {
				ArrayList<ParameterDec> parameterList = s.getParameterList();
				int size2 = parameterList.size();		
				for ( ParameterDec paramDec : parameterList ) {
					methodJavaName.append(paramDec.getType().getJavaName().replace('.', '_'));
					if ( --size2 > 0 )
						methodJavaName.append("_");
				}
				if ( --size > 0 )
					methodJavaName.append("_s_");
			}
			this.javaNameMultiMethod = methodJavaName.toString();
			pw.print(this.javaNameMultiMethod);
		}
		else {
			pw.print(javaName1);
		}
		pw.print("( ");
		
		ArrayList<ParameterDec> paramDecList = new ArrayList<>();
		for ( SelectorWithParameters selector : selectorArray )  {
			for ( ParameterDec paramDec : selector.getParameterList()) {
				paramDecList.add(paramDec);
			}
		}
		int size2 = paramDecList.size();
		for ( ParameterDec p : paramDecList ) {
			p.genJava(pw, env);
			if ( --size2 > 0 ) 
				pw.print(", ");
		}
		/*
		int size = selectorArray.size();
		
		int i = 0;
		boolean atLeastOneParameter = false;
		for ( SelectorWithParameters s : selectorArray ) {
			s.genJava(pw, env);
			if ( s.getParameterList().size() > 0 )
				atLeastOneParameter = true;
			if ( --size > 0 && atLeastOneParameter  && selectorArray.get(i+1).getParameterList().size() > 0 )
				pw.print(", ");
			++i;
		}
		
		*/
		
		pw.print(" ) ");
	}

	@Override
	public void genJavaAsConstructor(PWInterface pw, Env env, String javaNameDeclaringObject) {
		pw.printIdent( javaNameDeclaringObject + "(");
		int size = selectorArray.size();
		for ( SelectorWithParameters s : selectorArray) {
			ArrayList<ParameterDec> parameterList = s.getParameterList();
			int size2 = parameterList.size();		
			for ( ParameterDec paramDec : parameterList ) {
				paramDec.genJava(pw, env);
				if ( --size2 > 0 )
					pw.print(", ");
			}
			if ( --size > 0 )
				pw.print(", ");
		}
		
		pw.print(") ");
		
	}

	/**
	 * generate the signature of the method that will replace all multi-methods in
	 * a prototype
	   @param pw
	   @param env
	 */
	public void genJavaOverloadMethod(PWInterface pw, Env env) {
		
		super.genJava(pw, env, false);
		pw.print(" ");
		pw.print(getJavaName() + "( ");
		
		/*
		int size = selectorArray.size();
		for ( SelectorWithParameters s : selectorArray ) {
			ArrayList<ParameterDec> parameterList = s.getParameterList();
			int size2 = parameterList.size();		
			for ( ParameterDec paramDec : parameterList ) {
				paramDec.genJavaForMultiMethod(pw);
				if ( --size2 > 0 )
					pw.print(", ");
			}
			if ( --size > 0 )
				pw.print(", ");
		}
		*/
		
		ArrayList<ParameterDec> paramDecList = new ArrayList<>();
		for ( SelectorWithParameters selector : selectorArray )  {
			for ( ParameterDec paramDec : selector.getParameterList()) {
				paramDecList.add(paramDec);
			}
		}
		int size2 = paramDecList.size();
		for ( ParameterDec p : paramDecList ) {
			p.genJavaForMultiMethod(pw);
			if ( --size2 > 0 ) 
				pw.print(", ");
		}
		
		
		
		pw.print(" ) ");
	}
	
	@Override
	public String getFullName(Env env) {
		if ( fullName == null ) {
			String name = "";
			int size = selectorArray.size();
			for ( SelectorWithParameters s : selectorArray ) {
				name = name + s.getFullName(env);
				if ( --size > 0 ) 
					name = name + " ";
			}
			fullName = name;
		}
		return fullName;
	}
	
	@Override
	public String getSignatureWithoutReturnType() {
		String name = "";
		int size = selectorArray.size();
		for ( SelectorWithParameters s : selectorArray ) {
			name = name + s.getName() + " ";
			int size2 = s.getParameterList().size();
			for ( ParameterDec param : s.getParameterList() ) {
				Expr typeInDec = param.getTypeInDec();
				if ( typeInDec == null ) {
					name += Type.Dyn.getName();
				}
				else {
					name += param.getTypeInDec().asString();
				}
				if ( --size2 > 0 ) {
					name += ", ";
				}
			}
			
			if ( --size > 0 ) 
				name = name + " ";
		}
		return name;
	}

	
		
	/**
	 * return the names of all selectors concatenated
	 */
	@Override
	public String getNameWithoutParamNumber() {
		String name = "";
		for ( SelectorWithParameters s : selectorArray )
			name = name + s.getName();
		return name;
	}

	/**
	 * return the names of all selectors plus its number of parameters concatenated.
	 * That is, the return for method<br>
	 * <code>with: Int n, Char ch plus: Float f</code><br>
	 * would be <code>with:2 plus:1</code> 
	 */
	@Override
	public String getName() {
		String name = "";
		int size = selectorArray.size();
		for ( SelectorWithParameters s : selectorArray ) {
			name = name + s.getName() + s.getParameterList().size();
			if ( --size > 0 ) 
				name += " ";
		}
		return name;
	}
	
	
	
	@Override
	public String getPrototypeNameForMethod() {
		String name = "";
		for ( SelectorWithParameters s : selectorArray ) {
			String p = s.getName();
			if ( p.endsWith(":") )
				p = p.substring(0, p.length() - 1);
			int sizep = s.getParameterList().size();
			for ( ParameterDec param : s.getParameterList() ) {
				String fullName;
				if ( param.getType() != null ) 
					fullName = NameServer.getJavaName(param.getType().getFullName());
				else if ( param.getTypeInDec() != null ) 
					// fullName = NameServer.getJavaNameGenericPrototype(param.getTypeInDec().asString());
					fullName = NameServer.getJavaName(param.getTypeInDec().asStringToCreateJavaName());
				else
					fullName = NameServer.dynName;
				
				p = p + fullName;
				if ( --sizep > 0 )
					p = p + "_p_";
			}
			name = name + p + "_dot_";
		}
		return name;		
	}
	
	@Override
	public String getSuperprototypeNameForMethod() {
		String s = "UFunction";
		int size = selectorArray.size();
		for ( SelectorWithParameters selector : selectorArray ) {
			s += "<";
			int sizeParamList = selector.getParameterList().size();
			for ( ParameterDec parameter : selector.getParameterList() ) {
				String paramType;
				if ( parameter.getTypeInDec() == null )
					paramType = NameServer.dynName;
				else
					paramType = parameter.getTypeInDec().ifPrototypeReturnsItsName();
				s += paramType;
				if ( --sizeParamList > 0 )
					s += ", ";
			}
			if ( --size > 0 ) 
				s += ">";
		}
		String ret;
		if ( this.getReturnTypeExpr() == null ) 
			ret = "Nil";
		else
			ret = this.getReturnTypeExpr().ifPrototypeReturnsItsName();
		// the return type is added to the last set of '< >' as in
		//    UFunction<Int, String><Char, Int><Boolean, Int, ReturnType>
		size = selectorArray.size();
		if ( size > 0 && selectorArray.get(size - 1).getParameterList().size() > 0 )
			s = s + ", ";
		return s + ret + ">";
	}
	
	@Override
	public void genCyanEvalMethodSignature(StringBuffer s) {
		for ( SelectorWithParameters selector : selectorArray ) {
			s.append("eval: ");
			int size = selector.getParameterList().size();
			for ( ParameterDec parameter : selector.getParameterList() ) {
				 
				s.append( parameter.getTypeInDec().ifPrototypeReturnsItsName() );
				if ( parameter.getName() != null )
					s.append(" " + parameter.getName());
				if ( --size > 0 ) 
					s.append(", ");
			}
			s.append(" ");
		}
		if ( this.getReturnTypeExpr() != null ) {
			s.append("-> " + this.getReturnTypeExpr().ifPrototypeReturnsItsName() );
			
		}
	}
		
	

	public void setSelectorArray(ArrayList<SelectorWithParameters> selectorArray) {
		this.selectorArray = selectorArray;
	}

	public ArrayList<SelectorWithParameters> getSelectorArray() {
		return selectorArray;
	}

	@Override
	public String getSingleParameterType() {
		String s = "";
		int numParam = 0;
		for ( SelectorWithParameters p : selectorArray ) {
			int numParamSelector = p.getParameterList().size();
			if ( numParamSelector > 0 ) {
				numParam += numParamSelector;
				s = p.getParameterList().get(0).getTypeInDec().getJavaName();
			}
		}
		if ( numParam == 0 ) {
			return "Nil";
		}
		else if ( numParam == 1 ) {
			return s;
		}
		else {
			s = "UTuple<";
			for ( SelectorWithParameters selectorWithParameters : selectorArray ) {
				for ( ParameterDec paramDec : selectorWithParameters.getParameterList() ) {
					s = s + paramDec.getTypeInDec().getJavaName();
					if ( --numParam > 0 )
						s = s + ", ";
				}
			}
			return s + ">";
		}
	}


	@Override
	public void check(Env env) {
		super.check(env);
		for ( SelectorWithParameters selector : selectorArray ) {
			for ( ParameterDec parameterDec : selector.getParameterList() ) {
				if ( env.searchLocalVariableParameter(parameterDec.getName()) != null )
					env.error(parameterDec.getFirstSymbol(), "Parameter " +
							parameterDec.getName() + " is being redeclared", true, true);
				env.pushVariableDec(parameterDec);
			}
		}
	}
	

	@Override
	public Symbol getFirstSymbol() {
		return selectorArray.get(0).getSelector();
	}
	


	@Override
	public void calcInterfaceTypes(Env env) {
		
		if ( this.hasCalculatedInterfaceTypes ) 
			return ;
		
		super.calcInterfaceTypes(env);
		for ( SelectorWithParameters selector : selectorArray )
			selector.calcInternalTypes(env);
		hasCalculatedInterfaceTypes = true;

	}

	@Override 
	public void calcInternalTypes(Env env) {
		for ( SelectorWithParameters selector : selectorArray )
			for ( ParameterDec parameterDec : selector.getParameterList() )
				env.pushVariableDec(parameterDec);
	}
	
	@Override
	public boolean isIndexingMethod() {
		return indexingMethod;
	}

	
	@Override
	public ArrayList<ParameterDec> getParameterList() {
		ArrayList<ParameterDec> paramList = new ArrayList<>();
		for ( SelectorWithParameters s : selectorArray ) {
			for ( ParameterDec p : s.getParameterList() ) {
				paramList.add(p);
			}
		}
		return paramList;
	}

	
	@Override
	public String getFunctionName() {
		String s = "Function";
		int size = this.selectorArray.size();
		for ( SelectorWithParameters sel : this.selectorArray ) {
			s += "<";
			int sizeP = sel.getParameterList().size();
			if ( sizeP > 0 ) {
				for ( ParameterDec param : sel.getParameterList() ) {
					s += param.getType().getFullName();
					if ( --sizeP > 0 ) {
						s += ", ";
					}
				}
			}
			else {
				s += "none";
			}
			if ( --size > 0 ) {
				s += ">";
			}
		}
		s += ", ";
		
		if ( this.getReturnTypeExpr() != null && this.getReturnTypeExpr().getType() != null ) {
			s += this.getReturnTypeExpr().getType().getFullName() + ">";
		}
		else {
			s += "Nil>";
		}
		return s;
	}
	
	@Override
	public String getFunctionNameWithSelf(String fullNameReceiver) {

		String s = "Function";
		String r = "Function<" + fullNameReceiver + ">";
		int size = this.selectorArray.size();
		int count = 0;
		
		ArrayList<ArrayList<String>> typeListList = new ArrayList<>();
		ArrayList<String> typeList = new ArrayList<>();
		typeList.add(fullNameReceiver);
		typeListList.add(typeList);
		for ( SelectorWithParameters sel : this.selectorArray ) {
			typeList = new ArrayList<>();
			typeListList.add(typeList);
			int sizeP = sel.getParameterList().size();
			r += "<";
			if ( sizeP > 0 ) {
				
				for ( ParameterDec param : sel.getParameterList() ) {
					typeList.add( param.getType().getFullName() );
					r += param.getType().getFullName();
					if ( --sizeP > 0 ) 
						r += ", ";
				}			
			}
			else  { // && count < size - 1) || (this.selectorArray.size() == 1) ) {
				typeList.add(NameServer.noneArgumentNameForFunctions);
				r += NameServer.noneArgumentNameForFunctions;
			}
			
			if ( count == size - 1 ) {
				// last, insert the return type
				if ( this.getReturnTypeExpr() != null && this.getReturnTypeExpr().getType() != null ) {
					typeList.add( this.getReturnTypeExpr().getType().getFullName() );
					r += ", " + this.getReturnTypeExpr().getType().getFullName();
				}
				else {
					typeList.add( "Nil" );
					r += "Nil";
				}

			}			
			++count;
			r += ">";
		}
		
		for ( ArrayList<String> strList : typeListList ) {
			int sizeSL = strList.size();
			s += "<";
			for ( String str : strList ) {
				s += str;
				if ( --sizeSL > 0 ) {
					s += ", ";
				}
			}
			s += ">";
		}
		assert r.equals(s);
		return s;
			
	}
	
	
	private ArrayList<SelectorWithParameters>  selectorArray;

	/**
	 * true if there is a "[]" before the selectors of this method. After
	 * "[]" there should appear only selector "at:" or "at: ... put: ..."
	 */
	private boolean indexingMethod;


	private String javaNameMultiMethod;


	public String getJavaNameOverloadMethod() {
		return javaNameMultiMethod;
	}

}
