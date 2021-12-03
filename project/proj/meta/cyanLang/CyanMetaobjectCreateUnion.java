package meta.cyanLang;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import ast.GenericParameter;
import ast.MetaobjectArgumentKind;
import ast.Type;
import lexer.Lexer;
import meta.CyanMetaobjectWithAt;
import meta.IAction_dpa;
import meta.ICompilerAction_dpa;
import meta.ICompiler_ati;
import saci.Env;
import saci.NameServer;

public class CyanMetaobjectCreateUnion extends CyanMetaobjectWithAt 
	implements IAction_dpa, meta.IActionProgramUnit_ati {

		public CyanMetaobjectCreateUnion() { 
			super(MetaobjectArgumentKind.ZeroParameter);
		}

		@Override
		public String getName() {
			return "createUnion";
		}

		@Override
		public StringBuffer ati_codeToAdd(
				ICompiler_ati compiler_ati) {
			
			if ( (Boolean ) this.getMetaobjectAnnotation().getInfo_dpa() ) { return null; }
			
			Set<String> fullNameSet = new HashSet<String>();
			int n = 0;
			ArrayList<GenericParameter> gpList = compiler_ati.getProgramUnit().getGenericParameterListList().get(0);
			for ( GenericParameter gp : gpList ) {
				String fullName = gp.getParameter().getType().getFullName();
				if ( ! fullNameSet.add(fullName) ) {
					this.addError("Type '" + fullName + "' is used twice in the Union");
				}
				for (int k = 0; k < n; ++k) {
					if ( gpList.get(k).getParameter().getType().isSupertypeOf(gp.getParameter().getType(), (Env ) compiler_ati.getEnv()) 
							&& gp.getParameter().getType() != Type.Dyn ) {
						this.addError("Type '" + gpList.get(k).getParameter().getType().getFullName() + "' should appear after type '"
								+ gp.getParameter().getType().getFullName() + "' in the union because it is a supertype of it. The last type will never be used");
					}
				}
				++n;
			}
			
			return null;
		}

		@Override
		public StringBuffer dpa_codeToAdd(ICompilerAction_dpa compiler) {
			
			StringBuffer s = new StringBuffer();
			ArrayList<ArrayList<String>> strListList = compiler.getGenericPrototypeArgListList();
			if ( strListList == null || ! compiler.getCurrentPrototypeId().equals("Union") ) {
				compiler.error(this.getMetaobjectAnnotation().getFirstSymbol(), "Metaobject '" + getName() + 
						"' should only be used in generic prototype 'Union'");
				return null;
			}

			String protoName = Lexer.addSpaceAfterComma(this.metaobjectAnnotation.getPrototypeOfAnnotation());

			int sizeListList = strListList.size();
			if ( sizeListList != 1 ) {
				compiler.error(this.getMetaobjectAnnotation().getFirstSymbol(), 
						"Prototype 'Union' should have just one pair of '<' and '>' with parameters (like 'Union<Int, Char>')");
				return null;
			}
			this.strList = strListList.get(0);
			int sizeList = strList.size();
			if ( sizeList <= 1 ) {
				compiler.error(this.getMetaobjectAnnotation().getFirstSymbol(), 
						"Prototype 'Union' should have  at least two parameters");
				return null;
			}
			boolean hasNil = false;
			/**
			 * lowerCaseParameter[i] is true parameter strList.get(i) starts with a lower case letter 
			 */
			boolean []lowerCaseParameter = new boolean[strList.size()];
			this.hasLowerCase = false;
			int nilIndex = -1;
			int j = 0;
			for ( String str : strList ) {
				if ( str.equals("Nil") || str.equals(NameServer.cyanLanguagePackageName + ".Nil") ) {
					if ( nilIndex >= 0 ) {
						compiler.error(this.getMetaobjectAnnotation().getFirstSymbol(), 
								"Prototype 'Union' with more than one 'Nil' as parameter");
						return null;
					}
					nilIndex = j;
					hasNil = true;
				}
				// if there is a '.', then we should have a package preceding a prototype name as in
				//          Union<bank.Client, Nil>
				lowerCaseParameter[j] = Character.isLowerCase(str.charAt(0)) && str.indexOf(".") < 0 ;
				if ( lowerCaseParameter[j] )
					hasLowerCase = true;
				++j;
			}
			String javaNameCurrentPrototype = NameServer.getJavaName(compiler.getCurrentPrototypeName());
			/*
			 * valid: either Union<Person, Worker> or Union<person, Person, worker, Worker>
			 * lowerCaseParameter should be like 
			 *        false, false
			 * OR
			 *        true, false, true, false
			 */
			this.getMetaobjectAnnotation().setInfo_dpa( hasLowerCase );
			if ( hasLowerCase ) {
				/*
				 * check whether lowerCaseParameter is of the form "true, false, true, false, ..."
				 */
				if ( sizeList%2 != 0 || sizeList < 4 ) {
					compiler.error(this.getMetaobjectAnnotation().getFirstSymbol(), 
							"Prototype 'Union' with lower-case parameters should have an even number of parameters greater or equal to 4" 
							);
					return null;
				}
				for (int k = 0; k < sizeList/2; ++k ) {
					String symbolName = strList.get(2*k);
					String prototypeName = strList.get(2*k+1);
					if ( Character.isUpperCase(symbolName.charAt(0)) ||
						 (Character.isLowerCase(prototypeName.charAt(0)) && prototypeName.indexOf('.') < 0 )  ) {
						compiler.error(this.getMetaobjectAnnotation().getFirstSymbol(), 
								"Prototype 'Union' with lower-case parameters should have the form Union<lowerCase, UpperCase, lowerCase, UpperCase, ...>" 
								);
						return null;
						
					}
				}
				// Nil is not allowed when there are lower case parameters
				if ( hasNil ) {
					
					compiler.errorAtGenericPrototypeInstantiation("Prototype 'Union' with lower-case parameters cannot have 'Nil' as a type parameter");
					
					/*
					compiler.error(this.getMetaobjectAnnotation().getFirstSymbol(), 
							"Prototype 'Union' with lower-case parameters cannot have 'Nil' as a type parameter", 
							this.getMetaobjectAnnotation().getFirstSymbol().getSymbolString(), ErrorKind.metaobject_error);
					*/
					
					return null;
				}
			}

			int i;
			s.append("\n");
			//s.append("    Any elem\n");
			//s.append("    CySymbol which\n");
			
			if ( hasLowerCase ) {
				  // there cannot be any 'Nil' case. Then the type of 'elem' may be 'Any'
				s.append("       // the contained element\n");
				s.append("    @javaPublic var Any elem\n");
				s.append("       // which element is kept by 'elem'\n");
				s.append("    @javaPublic var CySymbol which\n\n");
				s.append("    func init { \n");
				s.append("        elem = Any;\n");
				s.append("        which = #none\n");
				s.append("    }\n");
				
				
				s.append("    @javacode{*\n");
				for (int k = 0; k < sizeList/2; ++k ) {
					s.append("    public static final " + NameServer.CySymbolInJava  + " " + strList.get(2*k) + " = new "
							+ NameServer.CySymbolInJava + "(\""
							 + strList.get(2*k) + "\");\n");
				}
				s.append("    *}\n");

				
			}
			else {
				  // 'elem' may be 'Nil'. Then the type of it should be Object 
				s.append("       // the contained element\n");
				s.append("    @javaPublic var Dyn elem\n");
				s.append("       // which element is kept by 'elem'\n");
				s.append("    @javaPublic var CySymbol which\n\n");
				s.append("    func init { \n");
				s.append("        elem = Any;\n");
				s.append("        which = #none\n");
				s.append("    }\n");
			}
			
			if ( ! hasNil ) {
 
				s.append("    override\n");
				//s.append("    func == (Dyn other) -> Boolean  = elem == other\n");
				s.append("    func == (Dyn other) -> Boolean  {\n");
				s.append("        @javacode{* \n");
				// s.append("            if ( _other == _Nil.prototype ) return new CyBoolean(_elem == _Nil.prototype); \n");
				s.append("            return _other == _Nil.prototype || ((_Any ) _other).getUnionElem() == null ?  "
						+ "((_Any )_elem)._equal_equal( _other ) : ((_Any )_elem)._equal_equal( ((_Any ) _other).getUnionElem() );");
				s.append("        *}");
				s.append("    }\n");
				s.append("    override\n");
				s.append("    func != (Dyn other) -> Boolean  {\n");
				s.append("        @javacode{* \n");
				// s.append("            if ( _other == _Nil.prototype ) return new CyBoolean(_elem != _Nil.prototype); \n");
				s.append("            return  ((_Any ) this)._equal_equal( _other )._exclamation();");
				s.append("        *}");
				s.append("    }\n");
			}
			else {
				/*
				// s.append("    func notNil -> Boolean { return which != #f" + (nilIndex+1) + " }\n\n");
				
				// hasLowerCase && sizeList == 4 means that there are two types in the union with tags
				// !hasLowerCase && sizeList == 2 means that there are two types in the union without tags
				if ( (hasLowerCase && sizeList == 4) || (!hasLowerCase && sizeList == 2) ) {
					   // metaobject changeTypeNotNil will change the type of the variable
					   // that is the receiver of the message (if any) to the other union type
					   / *
					    *   var Nil|Person p;
					    *   ...
					    *   p notNil: {
					    *       // here 'p' has type Person
					    *       (p name) println; 
					    *   };
					    * /
					
					s.append("    @changeTypeNotNil(\"");
					if ( hasLowerCase && sizeList == 4 ) {
						if ( nilIndex == 1 )
							s.append(strList.get(3));
						else
							s.append(strList.get(1));
					}
					else {
						s.append(strList.get(1-nilIndex));
					}

					s.append("\")\n");
					
					
					s.append("    func notNil: Function<Nil> f {\n"); 
					s.append("        let myself = self; \n"); 
					s.append("        notNil notNilValue = myself {\n"); 
					s.append("            f eval\n");
					s.append("        }\n");     
					s.append("    }\n\n");
					
				}

				s.append("    @checkIfNil\n");
				s.append("    func ifNil: (Any other) -> Any {\n");
				s.append("        if which == #f" + (nilIndex+1) + " {\n");
				s.append("            return other\n");
				s.append("        }\n");
				s.append("        else {\n");
				s.append("            @javacode{* return (_Any ) _elem; *}\n");
				s.append("        }\n");
				s.append("    }\n");
				*/
				
				
				/*
				 StringBuffer su = new StringBuffer("Union<");
				
				int m = 0;
				for ( String ss : strList) {
					su.append(ss);
					if ( m < sizeList - 1 ) 
						su.append(", ");
				}
				su.append(">");  */
				s.append("    override\n");
				s.append("    func ==  (Dyn other) -> Boolean {\n");
		        s.append("        @javacode<*<\n");
		        s.append("           if ( _which.s.equals(\"f" + (nilIndex+1) + "\") ) {\n");
		        s.append("               return _other == _Nil.prototype || ((_Any ) _other).getUnionElem() == null ? "
		        		+ "new CyBoolean(_other == _Nil.prototype) : "
		        		+ "new CyBoolean( ((_Any ) _other).getUnionElem() == _Nil.prototype);\n");
		        s.append("           }\n");
		        s.append("           else {\n");
		        s.append("               _Any another = (_Any ) _elem;\n");
		        s.append("               if ( another == null )\n");
		        s.append("                   return new CyBoolean(false);\n");
		        s.append("               else\n");
		        s.append("                   return _other == _Nil.prototype || ((_Any ) _other).getUnionElem() == null ? "
		        		+ "another._equal_equal(_other) : "
		        		+ "another._equal_equal( ((_Any ) _other).getUnionElem());\n");
		        s.append("           }\n");
		        s.append("        >*>\n");
				s.append("    }\n");
			}
			
			if ( hasLowerCase ) {
				/*
				s.append("    @changeTaggedUnionCase\n");
				s.append("    func ");
				i = 0;
				for (int k = 0; k < sizeList/2; ++k ) {
					if ( k != 0 ) {
						s.append("        ");
					}
					s.append(strList.get(2*k) + ": Function<Nil> f" + (k+1));
					if ( 2*k < sizeList - 1 )
						s.append("\n");
				}
				s.append("    {\n");
				
				for (int k = 0; k < sizeList/2; ++k ) {
					if ( k == 0 )
						s.append("        ");
					s.append("if which == #f" + (k+1) + " {\n");
					s.append("            f" + (k+1) + " eval\n");
					s.append("        }\n");
					s.append("        else ");
				}
				s.append("{\n");
				s.append("          // unless there is a compiler error, this will never be executed\n");
				s.append("            throw: CyException\n");
				s.append("        }\n");
				s.append("    }\n");
				*/
				
				for (int k = 0; k < sizeList/2; ++k ) {
					s.append("    func " + strList.get(2*k) + ": (" + strList.get(2*k+1) + " elem) -> " + protoName + " { \n");
					s.append("        which = #" + strList.get(2*k) + ";\n");
					s.append("        self.elem = elem;\n");
					s.append("        ^self\n");
					s.append("    }\n");
				}
				/*
				s.append("    @javacode{*\n");
			    s.append("    public static " + javaNameCurrentPrototype + " assign(Object _other) {\n");
			    s.append("        " + javaNameCurrentPrototype + " newUnion = new " + javaNameCurrentPrototype + "();\n");
		    	s.append("        newUnion._elem = (" + NameServer.AnyInJava + " ) _other; \n");
				for (int k = 0; k < sizeList/2; ++k )  {
			    	if ( k == 0 )
			    		s.append("        ");
				    s.append("if ( _other instanceof " + NameServer.getJavaName(strList.get(2*k+1)) + " ) { \n");
				    s.append("            newUnion._which = new _CySymbol(\"" + strList.get(2*k) + "\");\n");
				    s.append("        }\n");
				    if ( k != sizeList/2 - 1 )
					    s.append("        else ");
			    }
			    s.append("        else \n");
			    s.append("            throw new ExceptionContainer__(new _ExceptionCast());\n");
			    s.append("        return newUnion;\n");
			    s.append("    }\n\n");
			    s.append("    *}\n");				
				*/
			}
			else {

				s.append("    @javacode{*\n");
			    s.append("    public static " + javaNameCurrentPrototype + " assign(Object _other) {\n");
			    s.append("        " + javaNameCurrentPrototype + " newUnion = new " + javaNameCurrentPrototype + "();\n");
		    	s.append("        newUnion._elem = _other; \n");
			    for (int k = 0; k < strList.size(); ++k ) {
			    	if ( k == 0 )
			    		s.append("        ");
				    s.append("if ( _other instanceof " + NameServer.getJavaName(strList.get(k)) + " ) { \n");
				    s.append("            newUnion._which = new _CySymbol(\"f" + (k+1) + "\");\n");
				    s.append("        }\n");
				    if ( k != strList.size() - 1 )
					    s.append("        else ");
			    }
			    s.append("        else \n");
			    s.append("            throw new ExceptionContainer__(new _ExceptionCast( new CyString(\"Cannot cast expression to union\") ) );\n");
			    s.append("        return newUnion;\n");
			    s.append("    }\n\n");
			    s.append("    *}\n");				
				
			    /*
				s.append("    @checkUnionCase\n");
				s.append("    func ");
				i = 0;
				for ( String str: strList ) {
					if ( i != 0 ) {
						s.append("        ");
					}
					s.append("unionCase: " + str  + " do: Function<Nil> f" + (i+1));
					if ( i < sizeList - 1 )
						s.append("\n");
					++i;
				}
				s.append(" {\n");
				for ( i = 1; i <= strList.size(); ++i ) {
					if ( i == 1 ) 
						s.append("       ");
					s.append(" if which == #f" + i + " {\n");
					s.append("            f" + i + " eval\n");
					s.append("        }\n");
					s.append("        else");
				}
				s.append(" {\n");
				s.append("            // unless there is a compiler error, this will never be executed\n");
				s.append("            throw: CyException\n");
				s.append("        }\n");
				s.append("    }\n");
				*/
				i = 1;
				for (String str : strList) {
					
					/*
					if ( nilIndex + 1 == i) {
						s.append("    private func f" + i + ": { which = #f" + i  + " }\n");
					}
					else {
						s.append("    private func f" + i + ": (" + str + " other) { which = #f" + i  + "; elem = other }\n");
					}
					*/
					
					if ( nilIndex + 1 == i) {
						// s.append("    private func f" + i + ": { which = #f" + i  + " }\n");
					}
					else {
						/*s.append("    private func set: f" + i + ": (" + str + " other) " + 
						            " { which = #f" + i  + "; elem = other }\n");  */
						
						s.append("        @javacode{*\n");
						s.append("        public void f" + i + "(" + NameServer.getJavaName(str) + " other ) {\n");
						s.append("            _which = new _CySymbol(\"f" + i + "\"); _elem = other; }\n"); 
						s.append("        *}\n");
						
					    //        " { which = #f" + i  + "; elem = other }\n");
						
						
						s.append("    @prototypeCallOnly func f" + i + ": (" + str + " other) -> " + 
					           protoName + " { let aux = " + protoName + "(); @javacode{* _aux.f" + i + "(_other); *}\n        return aux; }\n");
					}
					
					++i;
				}
			}
			
			s.append("    override");
			s.append("    func asString -> String {\n");
			s.append("         return elem asString; ");
			s.append("    }\n");
			
			
			/*
			String currentPrototypeName = compiler.getCurrentPrototypeName();
			s.append("\n");
			s.append("    override\n");
			s.append("    func cast: Any other -> " + currentPrototypeName + " {\n");
			s.append("        if other isA: " + currentPrototypeName + " { @javacode{* return (" + javaNameCurrentPrototype + " ) _other; *} };\n");
			s.append("        throw: ExceptionCast(\"Cannot cast \" + (other prototypeName) + \" to '" + currentPrototypeName + "'\");\n");
			s.append("    }\n\n");
		    */
			
		    s.append("    @javacode<*<\n");

		    s.append("       @Override public CyBoolean _neq_1(Object other) {\n");
		    s.append("           return new CyBoolean(! _eq_1(other).b ); \n");
		    s.append("       }\n");
		    
		    
			if ( hasNil ) {
			    s.append("       @Override public CyBoolean _eq_1(Object other) {\n");
			    s.append("           if ( _which.s.equals(\"f" + (nilIndex+1) +  "\") ) {\n");
			    s.append("               return new CyBoolean(other == _Nil.prototype);\n");
			    s.append("           }\n");
			    s.append("           else {\n");
			    s.append("               _Any another = (_Any ) _elem;\n");
			    s.append("               if ( another == null )\n");
			    s.append("                   return new CyBoolean(false);\n");
			    s.append("               else\n");
			    s.append("                   return new CyBoolean(another." + NameServer.javaName_eq + "(other).b);\n");
			    s.append("           }\n");
			    s.append("       }\n");
			    s.append("\n");
			    /*
			     * 
			     */
			}
			else {
				s.append("        @Override public CyBoolean _eq_1(Object other) {\n");
				s.append("            _Any another = (_Any ) _elem;\n");
				s.append("            if ( another == null )\n");
				s.append("                return new CyBoolean(false);\n");
				s.append("            else\n");
				s.append("                return new CyBoolean((another." + NameServer.javaName_eq + "(other)).b);\n");
				s.append("        }\n");
			}
			s.append("        @Override public Object getUnionElem() { return _elem; }\n");
			s.append("    >*>\n");
			

			return s;
		}

		/**
		 * true if there is at least one parameter that starts with a lower case letter and that is not a prototype such as <br>
		 * {@code Union<watts, Float, joule, Float>}
		 */
		private boolean hasLowerCase;
		/**
		 * the list of parameters
		 */
		private ArrayList<String> strList;
	}
