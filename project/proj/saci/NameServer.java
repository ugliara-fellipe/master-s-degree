/**
 *
 */
package saci;

import java.io.File;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Hashtable;
import ast.ExprIdentStar;
import ast.MessageWithSelectors;
import ast.SelectorGrammar;
import ast.SelectorWithParameters;
import ast.SelectorWithRealParameters;
import ast.Type;
import lexer.Lexer;
import lexer.Symbol;
import lexer.SymbolIdent;
import lexer.Token;

/** This is the Compiler name server. It has methods to give the
 * names of temporary variables, method, objets, etc.
 *
 * @author José
 *
 */

public class NameServer {

	static private Hashtable<String, String> symbolToAlpha;
	static private Hashtable<String, String> cyanJavaBasicTypeTable;
	
	static {
		symbolToAlpha = new Hashtable<String, String>();

		
		symbolToAlpha.put("!", "exclamation");
		symbolToAlpha.put("?", "interrogation");
		symbolToAlpha.put("@", "at");
		symbolToAlpha.put("#", "numberSign");
		symbolToAlpha.put("$", "dollar");
		symbolToAlpha.put("=", "equal");
		symbolToAlpha.put("%", "percent");
		symbolToAlpha.put("&", "ampersand");
		symbolToAlpha.put("*", "mult");
		symbolToAlpha.put("+", "plus");
		symbolToAlpha.put("/", "slash");
		
		symbolToAlpha.put("<", "lessThan");
		symbolToAlpha.put("-", "minus");
		symbolToAlpha.put("^", "caret");
		symbolToAlpha.put("~", "tilde");
		symbolToAlpha.put(".", "dot");
		symbolToAlpha.put(":", "colon");
		symbolToAlpha.put(">", "greaterThan");
		symbolToAlpha.put("|", "verticalBar");
		symbolToAlpha.put("\\", "backslash");
		symbolToAlpha.put("(", "leftPar");
		symbolToAlpha.put(")", "rightPar");
		symbolToAlpha.put("[", "leftSquareBracket");
		symbolToAlpha.put("]", "rightSquareBracket");
		symbolToAlpha.put("{", "leftCurlyBracket");
		symbolToAlpha.put("}", "rightCurlyBracket");
		symbolToAlpha.put(",", "comma");

		
		cyanJavaBasicTypeTable = new Hashtable<String, String>();
		cyanJavaBasicTypeTable.put("Byte", "CyByte");
		cyanJavaBasicTypeTable.put("Short", "CyShort");
		cyanJavaBasicTypeTable.put("Int", "CyInt");
		cyanJavaBasicTypeTable.put("Long", "CyLong");
		cyanJavaBasicTypeTable.put("Float", "CyFloat");
		cyanJavaBasicTypeTable.put("Double", "CyDouble");
		cyanJavaBasicTypeTable.put("Char", "CyChar");
		cyanJavaBasicTypeTable.put("Boolean", "CyBoolean");
		cyanJavaBasicTypeTable.put("String", "CyString");
		cyanJavaBasicTypeTable.put("Dyn", "Object");
		
		

		
		
		
		
	}
	
	static final public String javaNameAddMethod = NameServer.getJavaNameOfMethodWith("add:", 1);
	static final public String javaNameAddMethodTwoParameters = NameServer.getJavaNameOfMethodWith("add:", 2);
	
	static final public String javaNameAtPutMethod = NameServer.getJavaNameOfMethodWith("at:", 1, "put:", 1);
	static final public String javaNameAtMethod = NameServer.getJavaNameOfMethodWith("at:", 1);
	static final public String javaNameDoesNotUnderstand = NameServer.getJavaNameOfMethodWith("doesNotUnderstand:", 2);
	static final public String javaName_asStringThisOnly = NameServer.getJavaNameOfMethodWith("asStringThisOnly:", 1);
	static final public String javaName_assign = NameServer.getJavaNameOfMethodWith("assign:", 1);
	static final public String javaName_eq = NameServer.getJavaNameOfMethodWith("eq:", 1);
	
	
	
	static final public String featureTypeJavaName = NameServer.getJavaName("Tuple<key, String, value, Any>");
	static final public String featureListTypeJavaName = NameServer.getJavaName("Array<Tuple<key, String, value, Any>>");
	static final public String slotFeatureTypeJavaName = NameServer
			.getJavaName("Tuple<slotName, String, key, String, value, Any>");
	static final public String slotFeatureListTypeJavaName = NameServer
			.getJavaName("Array<Tuple<slotName, String, key, String, value, Any>>");

	static final public String annotListTypeJavaName = NameServer.getJavaName("Array<Any>");
	static final public String slotAnnotTypeJavaName = NameServer
			.getJavaName("Tuple<slotName, String, value, Any>");
	static final public String slotAnnotListTypeJavaName = NameServer
			.getJavaName("Array<Tuple<slotName, String, value, Any>>");
	
	

	    // the extension of Cyan source files should be "cyan" as in "Program.cyan"
	static final public String cyanSourceFileExtension = "cyan";
	
    // the extension of Pyan source files should be "pyan" as in "myProject.pyan"
    static final public String pyanSourceFileExtension = "pyan";

	
	static final public int sizeCyanSourceFileExtensionPlusOne = cyanSourceFileExtension.length() + 1;
	   // dot cyanSourceFileExtension
    // the extension of Cyan source files should be "cyan" as in "Program.cyan"
    static final public String dotCyanSourceFileExtension = ".cyan";

	   // generic prototype instantiations of a prototype "Stack" that is
	   // in directory "util" are created in "util\tmp".
	public static final String temporaryDirName = "--tmp";
	   // generic prototype instantiations of a prototype "Stack" that is
	   // in directory "util" are created in "util\--tmp".
	public static final String dotTemporaryDirName = ".tmp";

	public static String prefixNonPackageDir = "--";
	
	public static String suffixTestPackageName = "_ut";
	
	
	
	   // name of the Cyan language package
	public static final String cyanLanguagePackageName = "cyan.lang";
	   // name of the Cyan language package
	public static final String cyanLanguagePackageNameDot = "cyan.lang.";
	
	   // name of the Cyan language package
	public static final String cyanLanguagePackageName_p_Dot = "cyan_p_lang_p_";
	public static final int sizeCyanLanguagePackageName_p_Dot = cyanLanguagePackageName_p_Dot.length();
	   // name of the Cyan language directory
	public static String	cyanLanguagePackageDirectory = "cyan" + File.separator + "lang";
	
	   // "nil" in Java 
	public static final String NilInJava = getJavaName("Nil");

	public static final String BooleanInJava = getJavaName("Boolean");
	public static final String CharInJava = getJavaName("Char");
	public static final String ByteInJava = getJavaName("Byte");
	public static final String ShortInJava = getJavaName("Short");
	public static final String IntInJava = getJavaName("Int");
	public static final String LongInJava = getJavaName("Long");
	public static final String FloatInJava = getJavaName("Float");
	public static final String DoubleInJava = getJavaName("Double");
	public static final String StringInJava = getJavaName("String");
	public static final String CySymbolInJava = getJavaName("CySymbol");
	public static final String AnyInJava = getJavaName("Any");
	
	
	public static final String evalInJava = getJavaName("eval"); 
	public static final String evalDotInJava = getJavaNameOfSelector("eval:");
	
	public static final String metaobjectPackageName = "--meta";
	public static final String metaobjectPackageNameCyanCompilerDot = "meta.";
	public static final int    dotClassLength = ".class".length();
	public static final String cyanMetaobjectClassName = meta.CyanMetaobject.class.getName();
	public static final String directoryNameLinkPastFuture = "--lpf";
	public static final String fileNameAfterSuccessfulCompilation = "afterSuccComp.txt";
	public static final String directoryNamePackageData = "--data";
	public static final String directoryNamePackageTests = "--test";
	public static final String directoryNamePackageDSL = "--dsl";
	public static final String directoryNamePackagePrototypeTmp = "--tmp";

	
	
	public static final String fileSeparatorAsString = System.getProperty("file.separator");
	public static final char fileSeparator = System.getProperty("file.separator").charAt(0);

	public static final String selfNameInnerPrototypes = "self__";
	public static final String javaSelfNameInnerPrototypes = getJavaName(selfNameInnerPrototypes);
	public static final String systemJavaName = getJavaName("System");
	
	public static final String selfNameContextObject = "newSelf__";
	public static final String javaSelfNameContextObject = getJavaName(selfNameContextObject);
	public static final String ArrayArrayDynInJava = getJavaName("Array<Array<Dyn>>");
	public static final String ArrayDynInJava = getJavaName("Array<Dyn>");
	

	/**
	 * name of type DYN
	 */
	public static final String dynName = "Dyn";
	/**
	 * name of type DYN in Java
	 */
	public static final String javaDynName = "Object";

	
	/**
	 * the Java name of the prototype Any 
	 */
	static public String javaNameObjectAny = NameServer.getJavaName("Any");

	
	/*
	 * converts something like  "Proto<Stack<Int>, String><Void>"  into "Proto(Stack(Int),String)(Void)" 
	 */
	static public String prototypeNameToFileName(String prototypeName) {
		String r = "";
		for (int i = 0; i < prototypeName.length(); ++i ) {
			char ch = prototypeName.charAt(i);
			if ( ch != ' ' ) {
				switch ( ch ) {
				case '<' : ch = '(';
				   break;
				case '>' : ch = ')';
				}
				r = r + ch;
			}
		}
		return r;
	}
	
	
	public static String fileNameToPrototypeName(String filename) {
		String r = "";
		for (int i = 0; i < filename.length(); ++i ) {
			char ch = filename.charAt(i);
			if ( ch != ' ' ) {
				switch ( ch ) {
				case '(' : ch = '<';
				   break;
				case ')' : ch = '>';
				}
				r = r + ch;
			}
		}
		return r;
	}
	
	
	
	static public String getVisibilityString(Token visibility) {
		switch ( visibility ) {
		case PUBLIC:    return "public";
		case PRIVATE:   return "private";
		case PROTECTED: return "protected";
		case PACKAGE:   return "";
		default:        return null;
		}
	}
	
	static public String nextJavaLocalVariableName() {
		return "tmp" + numberLocalVariable++; 
	}

	static public String nextLocalVariableName() {
		return "tmp" + numberLocalVariable++ + "__"; 
	}

	
	
	static public String nextPrototypeOfFunctionName() {
		return "Function" + numberPrototypeOfFunction++;
	}
	
	/**
	 * return the name of the codeg directory for prototype prototypeName
	 * The codeg information of a prototype "Proto" is stored in directory "--" + codegPrefix + "-" + Proto
	   @param prototypeName
	   @return
	 */
	static public String getCodegDirFor(String prototypeName) {
		return  "--" + prototypeName + NameServer.fileSeparatorAsString + codegPrefix ;
	}
	
	public static boolean isNameInnerPrototype(String name) {
		return (name.startsWith(functionProtoName) && name.endsWith(endsInnerProtoName)) || 
			   (name.startsWith(methodProtoName) && name.endsWith(endsInnerProtoName));
	}
	
	public static boolean isNameInnerProtoForMethod(String name) {
		return (name.startsWith(methodProtoName) && name.endsWith(endsInnerProtoName));	
	}

	
	public static boolean isNameInnerProtoForFunction(String name) {
		return name.startsWith(functionProtoName) && name.endsWith(endsInnerProtoName);
	}

	
	public static final String contextFunctionPrototypeName = "ContextFunction";
	private static final int MAX_CHAR_JAVA_NAME = 100;
	private static final int MAX_CHAR_PACKAGE_NAME = 50;
	private static final int NUM_CHARS_TO_KEEP = 40;
	
	public static boolean isNameInnerProtoForContextFunction(String name) {
		return name.startsWith(NameServer.contextFunctionProtoName) && name.endsWith(endsInnerProtoName);
	}
		
	/**
	 * return true if the method name is 'eval', 'eval:', 'eval:eval:' , etc. 
	 * The parameter should be of the form "eval:eval:eval:" or "eval: eval: ", without the number of parameters
	 */
	public static boolean isMethodNameEval(String name) {
		int indexOfColon = name.indexOf(':');
		if ( indexOfColon < 0 )
			return name.equals("eval");
		else {
			String s = "";
			for (int i = 0; i < name.length(); ++i) {
				if ( name.charAt(i) == ':' ) {
					if ( !s.equals("eval") )
						return false;
					while ( name.charAt(i) == ' ' || Character.isDigit(name.charAt(i)) ) {
						++i;
					}
					s = "";
				}
				else 
					s += name.charAt(i);
			}
			return s.length() == 0;
		}
	}
	
	

	/**
	 * return the name of an identifier which may have "." in it, as "java.lang.Int".
	 */
	static public String getJavaNameQualifiedIdentifier(ArrayList<Symbol> identSymbolArray) {
		
		String packageName = "";
		int max = identSymbolArray.size() - 1;
		for (int j = 0; j < max; ++j) {
			packageName = packageName + identSymbolArray.get(j).getSymbolString();
			if ( j < max - 1 )
				packageName = packageName + ".";
		}
		String fullName;
		if ( packageName.equals(NameServer.cyanLanguagePackageName) )
			fullName = "";
		else
			fullName = packageName;
		if ( fullName.length() > 0 ) 
			fullName = fullName + ".";
		fullName = fullName + getJavaName( identSymbolArray.get(identSymbolArray.size()-1).getSymbolString()  );
		return fullName.toString();
	}

	/**
	 * taken from https://www.javamex.com/tutorials/collections/strong_hash_code_implementation_2.shtml
	   @return
	 */
	private static final long []createLookupTable() {
		  long []byteTable1 = new long[256];
		  long h = 0x544B2FBACAAF1684L;
		  for (int i = 0; i < 256; i++) {
		    for (int j = 0; j < 31; j++) {
		      h = (h >>> 7) ^ h;
		      h = (h << 11) ^ h;
		      h = (h >>> 10) ^ h;
		    }
		    byteTable1[i] = h;
		  }
		  return byteTable1;
	}
	
	 private static final long[] byteTable = createLookupTable();
	  private static final long HSTART = 0xBB40E64DA205B064L;
	  private static final long HMULT = 7664345821815920749L;

	  public static long hash(CharSequence cs) {
		  long h = HSTART;
		  final long hmult = HMULT;
		  final long[] ht = byteTable;
		  final int len = cs.length();
		  for (int i = 0; i < len; i++) {
		    char ch = cs.charAt(i);
		    h = (h * hmult) ^ ht[ch & 0xff];
		    h = (h * hmult) ^ ht[(ch >>> 8) & 0xff];
		  }
		  return h > 0 ? h : -h;
		}
	
	static int numSuffixJavaName = 0;
	static String stubName(String javaName) {
		StringBuffer s = new StringBuffer(javaName.substring(0,  NameServer.NUM_CHARS_TO_KEEP));
		s.append("_" + NameServer.hash(javaName) + "_" + javaName.length());
		return s.toString();
	}

	/**
	 * return the Java name of 'cyanName' without the package.
	   @param cyanName
	   @return
	 */
	static public String getJavaName(String cyanName) {
		Tuple<String, String> t = getJavaNameTuple(cyanName);
		int protoNameSize = t.f2.length();
		if ( t.f1.length() > 0 ) {
			if ( protoNameSize >= NameServer.MAX_CHAR_JAVA_NAME ) {
				return t.f1 + "." + stubName(t.f2);
			}
			else {
				return t.f1 + "." + t.f2;
			}
		}
		else {
			if ( protoNameSize >= NameServer.MAX_CHAR_JAVA_NAME ) {
				return stubName(t.f2);
			}
			else {
				return t.f2;
			}
		}
	}

	
	/**
	 * 
	   @param cyanName
	   @return
	 */
	static public Tuple<String, String> getJavaNameTuple(String cyanName) {
		char ch;
		String packageName = "";
		


		int k = 0, lastDotIndex = 0;
		int size = cyanName.length();
		boolean done = false;
		while ( k < size ) {
			ch = cyanName.charAt(k);
			if ( ch == '.' ) { 
				lastDotIndex = k;
			}
			else if ( ch == '<' ) {
				if ( lastDotIndex > 0 ) {
					// '.' before '<' like in  cyan.lang.Function<Int>
					String protoName = cyanName.substring(lastDotIndex+1, k);
					String javaBasicTypeName = cyanJavaBasicTypeTable.get(protoName);
					if ( javaBasicTypeName != null ) {
						cyanName = javaBasicTypeName + cyanName.substring(k);
						done = true;
					}
						
				}
				break;
			}
			++k;
		}
		if ( lastDotIndex > 0 )
			packageName = cyanName.substring(0, lastDotIndex);
		if ( ! done ) {
			if ( cyanName.charAt(lastDotIndex) == '.' )
				++lastDotIndex;
			cyanName = cyanName.substring(lastDotIndex);
		}
		
		return new Tuple<String, String>(packageName, getJavaNameFromCyanNameWithoutPackage(cyanName));
		
	}


	/**
	   @param cyanName
	   @return
	 */
	private static String getJavaNameFromCyanNameWithoutPackage(String cyanName) {
		char ch;
		String innerProtoName = "";
		String javaName = "";
		for (int n = 0; n < cyanName.length(); ++n) {
			ch = cyanName.charAt(n);
			if ( Character.isWhitespace(ch) )
				continue;
			if ( Character.isAlphabetic(ch) || Character.isDigit(ch) ) {
				innerProtoName += ch;
			}
			else if ( ch == '_' ) {
				innerProtoName += "__";
			}
			else if ( ch == '<' ) {
				//++insideGPI;
				javaName += removeCyanLangChange(innerProtoName) + "_LT_GP_";
				innerProtoName = "";
			}
			else if ( ch == ',' ) {
				javaName += removeCyanLangChange(innerProtoName) + "_GP_";
				innerProtoName = "";
			}
			else if ( ch == '>' ) {
				javaName += removeCyanLangChange(innerProtoName) + "_GT";
				innerProtoName = "";
				//--insideGPI;
			}
			else if ( ch == '.' ){
				innerProtoName += "_p_";
			}

		}
		if ( innerProtoName.length() > 0 )
			javaName += removeCyanLangChange(innerProtoName);
			
		return javaName;
	}
	
	private static String removeCyanLangChange(String name) {
		String s;
		if ( name.startsWith(NameServer.cyanLanguagePackageName_p_Dot) ) {
			s = name.substring(sizeCyanLanguagePackageName_p_Dot);
		}
		else
			s = name;

		
		
		String javaBasicTypeName = cyanJavaBasicTypeTable.get(s);
		if ( javaBasicTypeName != null ) {
			return javaBasicTypeName;
		}
		
		/*
		 * put '_' in front of the prototype name, if there is one
		 */
		boolean start = true;
		boolean doNotAddUnderscore = false;
		StringBuffer ret = new StringBuffer("");
		for ( int j = 0; j < s.length(); ++j ) {
			char ch = s.charAt(j);
			if ( start && Character.isUpperCase(ch) ) {
				ret.append("_");
				doNotAddUnderscore = true;
			}
			if ( ch == '.' ) {
				start = true;
				doNotAddUnderscore = true;
			}
			else
				start = false;
			ret.append(ch);
		}
		if ( doNotAddUnderscore ) {
			return ret.toString();			
		}
		else {
			// a simple name such as 'other'
			return "_" + ret.toString();			
		}

	}
	
	

	
	/**
	 * return the alphanumeric name of a method composed by symbols. That is, if the 
	 * method is '+', this method returns "_plus". If the method were '<*' (if possible) this
	 * method would return "_lessThan_star"
	 */

	static public String alphaName(String symbolName) {
		int size = symbolName.length();
		String alpha = "";
		for (int i = 0; i < size; i++) {
			String s = symbolToAlpha.get("" + symbolName.charAt(i));
			if ( s == null ) 
				return null;
		   alpha = alpha + "_" + s;
		}
		return alpha;
	}

	/**
	 * return true if 'name' is the name of a prototype created from an interface.
	 * That is, 'name' is something like 'Proto_myInter__'.
	 */
	public static boolean isPrototypeFromInterface(String name) {
		int indexOfLessThan = name.indexOf('<');
		int i = name.indexOf('(');
		if ( i < 0 ) 
			i = indexOfLessThan;
		else if ( indexOfLessThan >= 0 && i >= indexOfLessThan )
			i = indexOfLessThan;
			
		String firstPart = name;
		if ( i >= 0 )
			firstPart = name.substring(0, i);
		int indexOfDot = firstPart.lastIndexOf('.');
		if ( indexOfDot >= 0 )
			firstPart = firstPart.substring(indexOfDot+1);
		return firstPart.startsWith(prefixProtoInterface) && firstPart.endsWith(endsProtoForInterfaceName);
	}

	/**
	 * To each interface Inter the compiler creates a regular object
	 * named Proto_Inter__. This method return the name of the prototype 
	 * created for the interface with name 'name'. Here 'name' can be a file name or a prototype name
	 * possibly with '<' and '>'. 
	   @param name
	   @return
	 */

	public static String prototypeFileNameFromInterfaceFileName(String name) {
		int indexOfLessThan = name.indexOf('<');
		int indexOfLeftPar = name.indexOf('(');
		if ( indexOfLeftPar > 0 )
			if ( indexOfLessThan < 0 || indexOfLessThan > indexOfLeftPar ) 
				indexOfLessThan = indexOfLeftPar;
		String firstPart = name; 
		String secondPart = "";
		String lastPart = "";
		String ret;
		if ( indexOfLessThan < 0 ) {
			int indexOfDot = firstPart.lastIndexOf('.');
			if ( indexOfDot < 0 ) {
				// no package or '<'
				ret = prefixProtoInterface + name + endsProtoForInterfaceName;
			} else {
				// package but no '<'
				secondPart = name.substring(indexOfDot+1);
				firstPart = name.substring(0, indexOfDot+1);
				ret = firstPart + prefixProtoInterface + secondPart + endsProtoForInterfaceName;
			}
		}
		else {
			// there is a '<'. Therefore name is from a generic prototype
			lastPart = name.substring(indexOfLessThan);
			firstPart = name.substring(0, indexOfLessThan);
			int indexOfDot = firstPart.lastIndexOf('.');
			if ( indexOfDot < 0 ) {
				// no package 
				ret = prefixProtoInterface + firstPart + endsProtoForInterfaceName + lastPart;
			} else {
				// package and '<'
				secondPart = firstPart.substring(indexOfDot+1);
				firstPart = firstPart.substring(0, indexOfDot+1);
				ret = firstPart + prefixProtoInterface + secondPart + endsProtoForInterfaceName + lastPart;
			}
		}
		return ret;
	}

	public static String javaPrototypeFileNameFromInterfaceFileName(String name) {
		return "_" + prefixProtoInterface + name + endsProtoForInterfaceName;
	}



	/**
	 * given the prototype name created from an interface, return
	 * the original name of the interface. Or null in error. 
	 * If the parameter is "Proto_Inter", the returned string is "Inter"
	 */
	public static String interfaceNameFromPrototypeName(String name) {
		int i = name.indexOf(prefixProtoInterface);
		if ( i != 0 || ! name.endsWith(endsProtoForInterfaceName) )
			return name;
		else {
			name = name.substring(prefixProtoInterface.length(), name.length() - endsProtoForInterfaceName.length());
			return name;
		}
	}


	public static String getJavaNameOfMethod(SelectorGrammar selectorGrammar) {
		return null;
	}	
	/**
	 * return the Java name corresponding to the regular method with selectors given as parameters.
	 * 
	 */
	static public String getJavaNameOfMethod(ArrayList<SelectorWithParameters>  selectorArray) {
		// int size = 0;
		String javaName = "";
		for ( SelectorWithParameters selector : selectorArray ) {
			/*if ( size > 0 )
				javaName = javaName + "_s_";  */
			javaName = javaName + getJavaNameOfSelector(selector.getName()) + selector.getParameterList().size();
			// size++;
		}
		return javaName;
	}
	
	
	/**
	 * return the Java name of the method whose selectors are given in the list selectorList and whose
	 * number of parameters of each selector is given by numParamList
	 */
	static public String getJavaNameOfMethod(String []selectorList, int []numParamList) {
		String javaName = "";
		int i = 0;
		for ( String selector : selectorList ) {
			javaName = javaName + getJavaNameOfSelector(selector) + "_" + numParamList[i];
			++i;
		}
		return javaName;
	}
	

	/**
	 * return the Java name of the method whose selector is 'selector1' with numParam1 parameters
	 */
	static public String getJavaNameOfMethodWith(String selector1, int numParam1) {
		return getJavaNameOfSelector(selector1) + numParam1;
	}
	

	/**
	 * return the Java name of the method whose selectors are 'selector1' and 'selector2' with numParam1  and numParam2 parameters
	 */
	static public String getJavaNameOfMethodWith(String selector1, int numParam1, String selector2, int numParam2) {
		return getJavaNameOfSelector(selector1) + numParam1 +
				getJavaNameOfSelector(selector2) + numParam2;
	}
	
	
	
	/**
	 * return the Java name of the method whose selectors are given in the list selectorArray
	 */
	static public String getJavaNameOfUnaryMethod(String selector) {
		return getJavaNameOfSelector(selector);
	}
		
	
	/**
	 * get the Java name corresponding to this selector. It is
	 * equal to "_symbolString" except when there is a underscore.
	 * All underscore characters are duplicated. So,
	 *       Is_A_Number
	 * results in
	 *       _Is__A__Number
	 * The ending character ':' is changed to "_dot". So
	 *    "eval:" produces  "_eval_dot"
	 * @param lineNumber
	 */
	static public String getJavaNameOfSelector(String symbolString) {
		
		String alpha = NameServer.alphaName(symbolString);
		if ( alpha != null ) {
			return alpha;
			/*
			int size = symbolString.length();
			alpha = "";
			for (int i = 0; i < size; i++) {
				String s = symbolToAlpha.get("" + symbolString.charAt(i));
				if ( s == null ) 
					return null;
			    alpha = alpha + "_" + s;
			}
			return alpha;
			*/
		}
		else {
			StringBuffer s = new StringBuffer("_");
			for ( int i = 0; i < symbolString.length(); i++ ) {
				char ch = symbolString.charAt(i);
				if ( ch == '_' )
					s.append("__");
				else if ( ch == ':' )
					s.append("_");
				else
					s.append(ch);
			}
			return s.toString();
			
		}
	}
	
	/**
	 * return the name of the Java method that should be called when the call is made with the
	 * selectors given as parameters.
	 * 
	 * @param messageWithSelectors
	 * @return
	 */
	static public String getJavaMethodNameOfMessageSend(MessageWithSelectors messageWithSelectors) {
		String methodName = "";
		// int size = messageWithSelectors.getSelectorParameterList().size();
		for ( SelectorWithRealParameters p : messageWithSelectors.getSelectorParameterList() ) {
			methodName = methodName +
			   NameServer.getJavaNameOfSelector(p.getSelectorName()) + p.getExprList().size();
			/* if ( --size > 0 )
				methodName = methodName + "_s_";  */
		}
		return methodName;
	}


	static public Method getJavaMethodByName(Class<?> aClass, String methodName) {
		java.lang.reflect.Method am[] = aClass.getMethods();
		for ( java.lang.reflect.Method aMethod : am ) {
			if ( aMethod.getName().equals(methodName) ) 
				return aMethod;
		}
		return null;
	}
	/**
	 * return the name of the Java method that should be called when the call is made with the
	 * selectors given as parameters.
	 * 
	 * @param messageWithSelectors
	 * @return
	 */
	static public String getJavaMethodNameOfMessageSend(String unaryOrSingleSelectorMessage) {
		return NameServer.getJavaNameOfSelector(unaryOrSingleSelectorMessage);
	}
	


	final public static CyanEnv cyanEnv = new CyanEnv(false, false);

	/**
	 * from a prototype name preceded by a package name create an expression.
	   @param fullName
	   @return
	 */
	public static ExprIdentStar stringToExprIdentStar(String fullName, Symbol source) {
		int indexOfDot = fullName.indexOf('.');
		ArrayList<Symbol> identSymbolArray = new ArrayList<>();
		String name = fullName;
		while ( indexOfDot >= 0 ) {
			identSymbolArray.add( new SymbolIdent(Token.IDENT, name.substring(0, indexOfDot), 
					source.getStartLine(), source.getLineNumber(), source.getColumnNumber(), source.getOffset(), source.getCompilationUnit()) );
			name = name.substring(indexOfDot + 1);
			indexOfDot = name.indexOf('.');
		}
		identSymbolArray.add( new SymbolIdent(Token.IDENT, name, 
					source.getStartLine(), source.getLineNumber(), source.getColumnNumber(), source.getOffset(), source.getCompilationUnit()) );
		
		return new ExprIdentStar(identSymbolArray, null);
	} 	


	public static Tuple2<String, String> splitPackagePrototype(String packageProtoName) {
		String packageName;
		String prototypeName;

		int indexOfLess = packageProtoName.indexOf('<');
		if ( indexOfLess >= 0 ) {

			String beforeLess = packageProtoName.substring(0, indexOfLess);
			int lastIndexOfDot = beforeLess.lastIndexOf('.');
			if ( lastIndexOfDot >= 0 ) {
				/*
				 * main.P<Int>  packageProtoName
				 *     ^ ^      lastIndexOfDot   indexOfLess
				 * main.P       beforeLess
				 */
				packageName = beforeLess.substring(0, lastIndexOfDot);
				prototypeName = packageProtoName.substring(lastIndexOfDot + 1);
			}
			else {
				/*
				 * P<Int>       packageProtoName
				 *  ^      		indexOfLess
				 * P            beforeLess
				 */
				packageName = "";
				prototypeName = packageProtoName;
				
			}
		}	
		else {
			/* no generic prototype instantiation. Something like
			       meta.tg.Proto
			   OR
			        Proto
			*/
			

			int lastIndexOfDot = packageProtoName.lastIndexOf('.');
			if ( lastIndexOfDot >= 0 ) {
				/*
				 * meta.tg.Proto    packageProtoName
				 *        ^         lastIndexOfDot  
				 */
				packageName = packageProtoName.substring(0, lastIndexOfDot);
				prototypeName = packageProtoName.substring(lastIndexOfDot + 1);
			}
			else {
				/*
				 * P<Int>       packageProtoName
				 *  ^      		indexOfLess
				 * P            beforeLess
				 */
				packageName = "";
				prototypeName = packageProtoName;
				
			}
			
		}
		
		return new Tuple2<String, String>(packageName, prototypeName);
	}
	
	public static boolean isBasicType(Type t) {
		return cyanJavaBasicTypeTable.get(t.getName()) != null;
	}
	
	public static boolean isBasicType(String name) {
		return cyanJavaBasicTypeTable.get(name) != null;
	}
	
	/**
	 * return the name of a private method corresponding to a superclass method. 
	 */
	public static String getNamePrivateMethodForSuperclassMethod(String name) {
		return name + "_super__" ;
	}
	static private int numberPrototypeOfFunction = 0;
	
	static public void setNumberLocalVariable() {
		numberLocalVariable = 0;
	}
	static private int numberLocalVariable = 0;
	public static String noneArgumentNameForFunctions = "none";
	/**
	 * prefix used to compose the name of a directory that stores the codeg information of a prototype.
	 * The codeg information of a prototype "Proto" is stored in directory "--" + codegPrefix + "-" + Proto
	 */
	public static final String	codegPrefix = "codeg";
	
	/**
	 * extension of source files of language Script Cyan
	 */
	public static final String	ScriptCyanExtension	= ".syan";
	
	/**
	 *  prefix added to prototypes that represent interfaces
	 */
	static final public String prefixProtoInterface = "Proto_";
	
	/**
	 * name, with number of parameters, of the main method of a context object 
	 */
	public static final Object bindToFunctionWithParamNumber = "bindToFunction:1";
	public static final String initOnce = "initOnce";
	public static final String popCompilationContextName = "popCompilationContext";
	public static final String pushCompilationContextName = "pushCompilationContext";
	public static final String pushCompilationContextStatementName = "pushCompilationContextStatement";
	public static final String []backupExtensionList = new String[] { "bak", "~" };


	public static final String markDeletedCodeName = "markDeletedCodeName";
	/**
	 * string that starts the name of every inner prototype created for a function
	 */
	public static final String functionProtoName = "Fun_";
	/**
	 * string that starts the name of every inner prototype created for a context function
	 */
	public static final String contextFunctionProtoName = "CFun_";	
	/**
	 * string that starts the name of every inner prototype created for a method
	 */
	public static final String methodProtoName = "M_";
	/**
	 * string that ends the name of every inner prototype, be it created for a function or a method
	 */
	public static final String endsInnerProtoName = "__";

	/**
	 * string that ends the name of every prototype created for an interface. So
	 * interface "MyInter" will cause the creation of the non-abstract prototype
	 * Proto_MyInter__
	 * 
	 */
	public static final String endsProtoForInterfaceName = "__";

	/**
	 * Name of the Cyan Exception prototype
	 */
	public static final String cyExceptionPrototype = "CyException";
	public static final java.lang.String IAny = getJavaName("IAny");

	public static final java.util.Random random = new java.util.Random();
	public static final String IMapName = "IMap";
	public static final String ISetName = "ISet";
	public static final String COMMUNICATE_IN_PACKAGE = "communicateInPackage";
	public static final String ON = "on";
	public static final String OFF = "off";
	
	public static long nextLong() {
		return random.nextLong();		
	}

	public static String addQuotes(String cyanStr) {
		if ( cyanStr.charAt(0) != '\"' ) 
			cyanStr = "\"" + Lexer.escapeJavaString(cyanStr) + "\"";
		return cyanStr;
	}
	
	public static String removeQuotes(String cyanStr) {
		if ( cyanStr.charAt(0) == '\"' ) {
			if ( cyanStr.charAt(cyanStr.length()-1) == '\"' )
				return cyanStr.substring(1,  cyanStr.length() - 1);
			else
				return cyanStr.substring(1);
		}
		else if ( cyanStr.charAt(cyanStr.length()-1) == '\"' )
			return cyanStr.substring(0,  cyanStr.length() - 1);
		else
			return cyanStr;
	}


	public static String removeSpaces(String str) {
		String s = "";
		for (int i = 0; i < str.length(); ++i) {
			if ( ! Character.isWhitespace(str.charAt(i)) ) {
				s += str.charAt(i);
			}
		}
		return s;
	}

	/**
	 * taken from stack overflow
	   @param i
	   @return
	 */
	public static String ordinal(int i) {
	    String[] sufixes = new String[] { "th", "st", "nd", "rd", "th", "th", "th", "th", "th", "th" };
	    switch (i % 100) {
	    case 11:
	    case 12:
	    case 13:
	        return i + "th";
	    default:
	        return i + sufixes[i % 10];

	    }
	}
}
