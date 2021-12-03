/**
 *
 */
package ast;

import java.util.ArrayList;
import lexer.Symbol;
import lexer.SymbolIdent;
import lexer.Token;
import saci.CyanEnv;
import saci.NameServer;

/** represents a context parameter such s in
 *
 * object Sum( Int &s)
 *     public fun eval: Int elem [
 *         s = s + elem
 *     ]
 * end
 *
 * If s is a value parameter, as in
 *
 * object F(Float %y)
 *    public fun eval: Float f1 -> float [
 *            ^ Math expr: f1, y;
 *    ]
 * end

  then variable VariableKind is value. Otherwise it is ref.
  
  Context parameters are just regular instance variables that may be declared 
  with types &T, %T, and *T 

 * @author José
 *
 */
public class ContextParameter extends InstanceVariableDec  {

	public ContextParameter(SymbolIdent variableSymbol, VariableKind variableKind, Expr typeInDec, Token visibility,
			Symbol firstSymbol, ArrayList<CyanMetaobjectWithAtAnnotation> nonAttachedMetaobjectAnnotationList, 		
			ArrayList<CyanMetaobjectWithAtAnnotation> attachedMetaobjectAnnotationList ) {
		super(variableSymbol, typeInDec, null, visibility, false, nonAttachedMetaobjectAnnotationList, attachedMetaobjectAnnotationList, firstSymbol, false);
		setVariableKind(variableKind);
		if ( variableKind != VariableKind.COPY_VAR ) 
			this.setRefType(true);
	}


	@Override
	public void accept(ASTVisitor visitor) {
		visitor.visit(this);
	}	
	
	@Override
	public void genCyan(PWInterface pw, boolean printInMoreThanOneLine, CyanEnv cyanEnv, boolean genFunctions) {
		
		Token visible = this.getVisibility();
		if ( visible != Token.PRIVATE ) {
			if ( visible == Token.PUBLIC )
				pw.print("public ");
			else if ( visible == Token.PROTECTED )
				pw.print("protected ");
		}
		/*else {
			pw.print("var ");
		} */
			
		if ( typeInDec != null ) 
			typeInDec.genCyan(pw, false, cyanEnv, genFunctions);
		else {
			   // used only in inner objects
			String name = type.getFullName();
			int indexOfCyanLang = name.indexOf(NameServer.cyanLanguagePackageName);
			if ( indexOfCyanLang >= 0 ) 
				name = name.substring(indexOfCyanLang);
			pw.print(name);
		}		
		
		
		pw.print(" ");
		if ( this.getVariableKind() == VariableKind.LOCAL_VARIABLE_REF )
			pw.print(getVariableKind().toString());
		pw.print(getName());
	}


	
	@Override
	public boolean isContextParameter() {
		return true;
	}

	

}
