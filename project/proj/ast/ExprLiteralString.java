/**
 *
 */
package ast;

import java.util.ArrayList;
import lexer.Lexer;
import lexer.Symbol;
import lexer.SymbolStringLiteral;
import meta.ReplacementPolicyInGenericInstantiation;
import saci.CyanEnv;
import saci.Env;
import saci.Function0;
import saci.NameServer;

/** Represents a string literal such as
 *    "olá, tudo bem?"
 *
 * @author José
 *
 */
public class ExprLiteralString extends ExprLiteral {

	private static final int MAX_OCTAL_ESCAPE = 377;
	ArrayList<String> subStringList;
	ArrayList<String> varNameList;
	ArrayList<VariableDecInterface> varList;
	/**
	 * @param symbol
	 */
	public ExprLiteralString(Symbol symbol) {
		
		super(symbol);
		subStringList = new ArrayList<>();
		varNameList = null;
		StringBuffer s = new StringBuffer();
		literalString = symbol.getSymbolString();
		int size = literalString.length();
		int i = 0; 
		while ( i < size ) {
			if ( literalString.charAt(i) == '\\' && i < literalString.length() - 1 && literalString.charAt(i+1) == '$' ) {
				s.append('$');
				++i;
			}
			else if ( literalString.charAt(i) == '$' && i + 1 < size && 
					(Character.isLetter(literalString.charAt(i+1)) || literalString.charAt(i+1) == '_')) {
				// a variable name is expected after '$'
				++i;
				StringBuffer s1 = new StringBuffer();
				if ( i < size && (Character.isLetter(literalString.charAt(i)) || literalString.charAt(i) == '_') ) {
					while ( i < size && ( Character.isLetterOrDigit(literalString.charAt(i)) || literalString.charAt(i) == '_') ) {
						s1.append(literalString.charAt(i));
						i++;
					}
					if ( s1.toString().equals("self") && literalString.charAt(i) == '.' ) {
						++i;
						s1.append('.');
						if ( i < size && (Character.isLetter(literalString.charAt(i)) || literalString.charAt(i) == '_' ) ) {
							while ( i < size && ( Character.isLetterOrDigit(literalString.charAt(i)) || literalString.charAt(i) == '_') ){
								s1.append(literalString.charAt(i));
								i++;
							}
						}
					}
				}
				if ( i < size ) {
					subStringList.add(s.toString());
					s = new StringBuffer();
				}
				--i;

				if ( varNameList == null ) 
					varNameList = new ArrayList<>();
				varNameList.add(s1.toString());
			}
			else
				s.append(literalString.charAt(i));
			++i;
		}
		subStringList.add(s.toString());
	}

	@Override
	public void genCyanReal(PWInterface pw, boolean printInMoreThanOneLine, CyanEnv cyanEnv, boolean genFunctions) {
		String quote;
		if ( ((SymbolStringLiteral) symbol).getTripleQuote() ) 
			quote = "\"\"\"";
		else
			quote = "\"";
	    pw.print(quote + literalString + quote);
	}
	
	public void genCyanReplacingGenericParameters(PWInterface pw, CyanEnv cyanEnv) {
		if ( cyanEnv.getCreatingInstanceGenericPrototype() ) {
			CyanMetaobjectWithAtAnnotation.replacePrint( ("\"" + getSymbol().getSymbolString() + "\"").toCharArray(), pw, 
					cyanEnv.getFormalParamToRealParamTable(), "",
					ReplacementPolicyInGenericInstantiation.REPLACE_BY_CYAN_VALUE);
		}
	}



	@Override
	public void calcInternalTypes(Env env) {
		
		if ( symbol instanceof SymbolStringLiteral ) {
			SymbolStringLiteral sym = (SymbolStringLiteral ) symbol;
			if ( ! sym.getTripleQuote() ) {
				/*
				 * multi-line string, with """, do not use escape characters 
				 */
				
				char previous = '\0';
				for (int i = 0; i < literalString.length(); ++i) {
					char ch = literalString.charAt(i);
					/*
					 * allowed escape characters: \b, \n, \t, \r, \f, \", \\
					 */
					if ( previous == '\\' ) {
						if ( Character.isDigit(ch) ) {
							String num = "";
							while ( i < literalString.length() && Character.isDigit(ch = literalString.charAt(i)) ) {
								++i;
								num += ch;
							}
							if ( num.length() > 3 || Integer.valueOf(num) > MAX_OCTAL_ESCAPE) {
								env.error(this.symbol, "Illegal number in escape : '\\" + num + "'");
							}
							previous = ch;
						}
						else {
							if ( "\'$bntrf\"\\".indexOf(ch) < 0 ) {
								// ch is not one of the list
								env.error(this.symbol, "Illegal escape character: '\\" + ch + "'");
							}
							if ( ch == '\\' )
								previous = '\0';
							else
								previous = ch;
						}

					}
					else 
						previous = ch;
				}
			}
		}
		
		type = Type.String;
		if ( varNameList != null ) {
			varList = new ArrayList<>();
			for ( String id : varNameList ) {
				if ( id.startsWith("self.") ) {
					id = id.substring(5);
					VariableDecInterface aVar = env.searchInstanceVariable(id); 
					if ( aVar == null ) {
						env.error(symbol, "Instance variable '" + id + "' was not found");
					}
					varList.add(aVar);
				}
				else {
					if ( id.length() == 0 || id.equals("self.") ) {
						env.error(symbol, "Variable expected after '$' in a literal string");
					}
					
					VariableDecInterface aVar = env.searchLocalVariableParameter(id);
					if ( aVar == null ) 
						aVar = env.searchInstanceVariable(id);
					if ( aVar == null ) {
						env.error(symbol, "Variable '" + id + "' was not found");
					}
					varList.add(aVar);
				}
			}
		}
		super.calcInternalTypes(env);
	}
	

	@Override
	public String genJavaExpr(PWInterface pw, Env env) {
		/*
		String s = this.genJavaString(env);
		String varName = NameServer.nextLocalVariableName();
		pw.printlnIdent(varName + " = " + s + ";");
		return varName;
		*/
		boolean isTripleQuote = ((SymbolStringLiteral ) this.symbol).getTripleQuote();
		if ( varList == null ) {
			if ( isTripleQuote ) {
				return "(new CyString(\"" + Lexer.escapeJavaString(subStringList.get(0)) + "\"))";
			}
			else {
				return "(new CyString(\"" + subStringList.get(0) + "\"))";
			}
		}
		else {
			/* var Dyn x;
			 * "x = $x i = $i"
			 * Java code:
			 *     String tmp23;
			 *     if ( _x instanceof _Any )
			 *         tmp23 = ((_Any) _x)._asString().s;
			 *     else if ( _x instanceof _Nil ) 
			 *         tmp23 = ((_Nil) _x)._asString().s;
			 *	   else
			 *         tmp23 = _x.toString();
			 *     String tmp24 = _i._asString().s;
			 *              
			 *     new CyString("x = " + tmp23 + " i = " + tmp24 )
			 *     
			 *     
			 *     
			 */
			String []tmpVar = new String[varList.size()];
			int i = 0;
			for ( VariableDecInterface v : varList ) {
				tmpVar[i] = v.getJavaName(); 
				if ( v.getRefType() ) 
					tmpVar[i] += ".elem";
				if ( v.getType() == Type.Dyn ) {
					String tmp = NameServer.nextJavaLocalVariableName();
					pw.printlnIdent("String " + tmp + ";");
					pw.printlnIdent("if ( " + tmpVar[i] + " instanceof _Any ) " );
					pw.add();
					pw.printlnIdent(tmp + " = ((_Any )" + tmpVar[i] + ")._asString().s;");
					pw.sub();
					pw.printlnIdent("else if ( " + tmpVar[i] + " instanceof _Nil )");
					pw.add();
					pw.printlnIdent(tmp + " = ((_Nil )" + tmpVar[i] + ")._asString().s;");
					pw.sub();
					pw.printlnIdent("else ");
					pw.add();
					pw.printlnIdent(tmp + " = " + tmpVar[i] + ".toString();");
					pw.sub();
					tmpVar[i] = tmp;
				}
				else {
					tmpVar[i] = tmpVar[i] + "._asString().s";
				}
				++i;
			}
			i = 0;
			int size = varList.size();
			StringBuffer ret = new StringBuffer();
			ret.append("(new CyString(");
			for ( String s : subStringList ) {
				
				ret.append("\"");
				if ( isTripleQuote ) {
					ret.append(Lexer.escapeJavaString(s));
				}
				else {
					ret.append(s);
				}
				ret.append("\"");
				if ( i < size ) {
					ret.append( " + " + tmpVar[i] );
					if ( i < subStringList.size() - 1 )
						ret.append(" + ");
				}
				++i;
			}
			ret.append("))");
			return ret.toString();
		}
		//return genJavaString();
	}
	
	/*
	public void genJavaExprWithoutTmpVar(PWInterface pw, Env env) {
		pw.print(genJavaString()); 
	}
	*/
	

	@Override
	public Object getJavaValue() {
		if ( literalStringInJava == null ) {
			calcLiteralStringInJava();
		}
		return literalStringInJava;
	}

	@Override
	public StringBuffer getStringJavaValue() {
		if ( literalStringInJava == null ) {
			calcLiteralStringInJava();
		}
		return new StringBuffer(literalStringInJava);
	}

	private void calcLiteralStringInJava() {
		String s = ((SymbolStringLiteral ) this.symbol).getJavaString();

		s = s.replace("\\$", "$");
		literalStringInJava = s.replace("\\$", "\\\\$");
		/*
		literalStringInJava = "";
		for ( int i = 0; i < s.length(); ++i ) {
			if ( s.charAt(i) == '$' && i >= 1 && s.charAt(i-1) == '\\' ) {
				if ( i >= 2 && s.charAt(i-2) != '\\' ) {
					
				}
				else {
					literalStringInJava += '$';
				}
			}
			else {
				literalStringInJava += s.charAt(i);
			}
		}
		*/
	}

	@Override
	public String getJavaType() {
		return "String";
	}

	/**
	 * the same as literalString except that a string "It is 100\$ true" is translated into
	 * "It is 100$ true"
	 */
	private String literalStringInJava;

	private String literalString;
	public ArrayList<String> getVarNameList() {
		return varNameList;
	}

	
	@Override
	public String metaobjectParameterAsString(Function0 inError) {
		return asString();
	}	
}
