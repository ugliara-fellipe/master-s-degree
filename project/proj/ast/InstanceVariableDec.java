package ast;

import java.util.ArrayList;
import lexer.Symbol;
import lexer.SymbolIdent;
import lexer.Token;
import meta.DeclarationKind;
import meta.IInstanceVariableDec_ati;
import meta.IType;
import saci.CyanEnv;
import saci.Env;
import saci.NameServer;

/**
 * Represents an instance variable declaration
 * @author José
 *
 */

public class InstanceVariableDec extends SlotDec implements VariableDecInterface, IInstanceVariableDec_ati {

	public InstanceVariableDec( SymbolIdent variableSymbol,
			                    Expr typeInDec,
			                    Expr expr,
			                    Token visibility,
			                    boolean shared,
			                    ArrayList<CyanMetaobjectWithAtAnnotation> nonAttachedSlotMetaobjectAnnotationList,
			                    ArrayList<CyanMetaobjectWithAtAnnotation> attachedSlotMetaobjectAnnotationList,
			                    Symbol firstSymbol, boolean isReadonly ) {
		super(visibility, attachedSlotMetaobjectAnnotationList, nonAttachedSlotMetaobjectAnnotationList);
		this.variableSymbol= variableSymbol;
		this.typeInDec = typeInDec;
		this.expr = expr;
		this.shared = shared;
		variableKind = VariableKind.COPY_VAR;
		this.firstSymbol = firstSymbol;
		javaName = NameServer.getJavaName(this.getName());
		this.isReadonly = isReadonly;
		javaPublic = false;
		typeWasChanged = false;
		this.setWasInitialized(expr != null);
	}
	
	

	@Override
	public void accept(ASTVisitor visitor) {
		visitor.visit(this);
	}
	
	
	@Override
	public void setTypeInDec(Expr typeInDec) {
		this.typeInDec = typeInDec;
	}
	@Override
	public Expr getTypeInDec() {
		return typeInDec;
	}
	public void setExpr(Expr expr) {
		this.expr = expr;
	}
	public Expr getExpr() {
		return expr;
	}
	public void setVariableSymbol(SymbolIdent variableSymbol) {
		this.variableSymbol = variableSymbol;
	}
	@Override
	public SymbolIdent getVariableSymbol() {
		return variableSymbol;
	}

	@Override
	public void genCyan(PWInterface pw, boolean printInMoreThanOneLine, CyanEnv cyanEnv, boolean genFunctions) {

		super.genCyan(pw, printInMoreThanOneLine, cyanEnv, genFunctions);
		pw.printIdent( NameServer.getVisibilityString(visibility) + " ");
		if ( shared )
			pw.print("shared ");
		if ( isReadonly ) 
			pw.print("let ");
		else
			pw.print("var ");
		if ( typeInDec != null ) 
			typeInDec.genCyan(pw, false, cyanEnv, genFunctions);
		else {
			   // used only in inner objects
			String name = type.getFullName();
			int indexOfCyanLang = name.indexOf(NameServer.cyanLanguagePackageName);
			if ( indexOfCyanLang >= 0 ) 
				name = name.substring(indexOfCyanLang);
			pw.print( name);
		}
		pw.print(" ");
		switch ( variableKind ) {
		case LOCAL_VARIABLE_REF:
			pw.print("&");
			break;
		default:
			break;
		}
		if ( cyanEnv.getCreatingInstanceGenericPrototype() ) {
			pw.print(cyanEnv.formalGenericParamToRealParam(variableSymbol.getSymbolString()));
		}
		else {
			pw.print(variableSymbol.getSymbolString());
		}
		
		if ( expr != null ) {
			pw.print(" = ");
			expr.genCyan(pw, printInMoreThanOneLine, cyanEnv, genFunctions);
		}
		pw.println();
	}

	@Override
	public void genJava(PWInterface pw, Env env) {

		super.genJava(pw, env);
		pw.printIdent("");
		if ( shared )
			pw.print("static ");
		if ( this.javaPublic ) 
			pw.print("public ");
		else if ( this.visibility == Token.PROTECTED )
			pw.print("protected ");
		else 
			pw.print("private ");
		
		

		String typeName;
		if ( type != null ) 
			typeName = type.getFullName();
		/* else if ( typeInDec != null ) 
			typeName = typeInDec.ifPrototypeReturnsItsName();  */
		else if ( expr != null ) 
			typeName = expr.getType(env).getFullName();
		else
			typeName = Type.Dyn.getName();
		
		if ( variableKind != VariableKind.COPY_VAR || refType )
			pw.printIdent("Ref<" + NameServer.getJavaName(typeName) + ">");
		else
			pw.printIdent(NameServer.getJavaName(typeName));
		pw.println(" " + NameServer.getJavaName(getName()) + ";");			
	}


	@Override
	public String getName() {
		return variableSymbol.getSymbolString();
	}

	@Override
	public Type getType() {
		return type;
	}
	@Override
	public void setType(Type type) {
		this.type = type;
	}
	
	@Override
	public IType getIType() {
		return type;
	}

	
	
	@Override
	public void calcInterfaceTypes(Env env) {
		if ( typeInDec != null && type == null ) 
			type = typeInDec.ifRepresentsTypeReturnsType(env);
		if ( type == null )
			type = Type.Dyn;
	}

	@Override
	public void calcInternalTypes(Env env) {
		
		if ( expr != null ) {
			expr.calcInternalTypes(env);
			if ( !(type.isSupertypeOf(expr.getType(), env)) ) {
				env.error(expr.getFirstSymbol(), "Expression type is not subtype of the instance variable type");
			}
			if ( shared ) {
				if ( ! expr.isNREForInitOnce(env) ) {
					env.error(expr.getFirstSymbol(), "The expression is not valid for initializing a shared variable. It should be"
							+ " a literal value or the creation of an object of a prototype of package cyan.lang."
							+ " See the Cyan manual for more information.", true, false);
				
				}
			}
			else {
				if ( ! expr.isNRE(env) ) {
					env.error(expr.getFirstSymbol(), "The expression is not valid for initializing an instance variable. It should be"
							+ " a literal value or the creation of an object."
							+ " See the Cyan manual for more information.", true, false);
				}
			}
			
		}
		super.calcInternalTypes(env);
	}
	
	public boolean isShared() {
		return shared;
	}

	public VariableKind getVariableKind() {
		return variableKind;
	}
	public void setVariableKind(VariableKind variableKind) {
		this.variableKind = variableKind;
		refType = variableKind == VariableKind.LOCAL_VARIABLE_REF;
	}	

	@Override
	public Symbol getFirstSymbol() {
		return firstSymbol;
	}

	@Override
	public boolean getRefType() {
		return refType;
	}

	@Override
	public void setRefType(boolean refType) {
		this.refType = refType;
		if ( refType ) 
			variableKind = VariableKind.LOCAL_VARIABLE_REF;
		else
			variableKind = VariableKind.COPY_VAR;			
	}	


	@Override
	public String getJavaName() {
		return javaName;
	}

	@Override
	public void setJavaName(String javaName) {
		this.javaName = javaName;
	}

	
	@Override
	public DeclarationKind getKind() {
		return DeclarationKind.INSTANCE_VARIABLE_DEC;
	}

	@Override
	public boolean isReadonly() {
		return isReadonly;
	}

	public boolean isContextParameter() {
		return false;
	}
	
	public void setJavaPublic(boolean javaPublic) {
		this.javaPublic = javaPublic;
	}
	
	
	/**
	 * see {@link VariableDecInterface#setTypeWasChanged(boolean)}
	 */

	@Override
	public void setTypeWasChanged(boolean typeWasChanged) {
		this.typeWasChanged = typeWasChanged;
	}
	/**
	 * see {@link VariableDecInterface#setTypeWasChanged(boolean)}
	 */
	@Override
	public boolean getTypeWasChanged() {
		return typeWasChanged;
	}

	
	public boolean getWasInitialized() {
		return wasInitialized;
	}



	public void setWasInitialized(boolean wasInitialized) {
		this.wasInitialized = wasInitialized;
	}


	/**
	 * see {@link VariableDecInterface#setTypeWasChanged(boolean)}
	 */
	private boolean typeWasChanged;
	

	private String javaName;
		
	
	
	protected SymbolIdent variableSymbol;
	protected Expr typeInDec;
	private Expr expr;
	/**
	 * type of this instance variable
	 */
	protected Type type;

	
	/**
	 * true if this variable is shared
	 */
	private boolean shared;

	/** the kind of variable. Regular variables are COPY_VAR variables. But when
	 * the compiler creates a regular prototype from the declaration of a context object,
	 * an instance variable may be "an instance variable parameter" or a "reference parameter".
	 * That is, from a context object  
	 *     object Test(:f1 &Int, :y *Char, :z %Boolean) ... end
	 * the compiler will create a regular object that has instance variables f1, y, and z:
	 * 
	 * object  Test
	 *     public init: (:f1 &Int, :y *Char, :z %Boolean) [ self.x = f1; self.y = y; self.z = z; ]
	 *     
	 *     private :f1 &Int; :y *Char;  :z Boolean;
	 * end
	 *     
	 */
	private VariableKind variableKind;

	/**
	 * true if this variable was used as a reference type. That is, refType is true for a
	 * variable p if p was used where a reference was expected. For example, suppose p 
	 * was used in 
	 *     Sum(p)
	 * in which Sum was declared as
	 *     object Sum(Int &s) ... end
	 * then refType should be true.  
	 */
	private boolean refType;

	/**
	 * first symbol of this declaration
	 */
	private Symbol firstSymbol;
	/**
	 * true if this instance variable is read only
	 */
	private boolean isReadonly;
	/**
	 * true if the generated code in Java for this instance variable should be a public instance variable
	 */
	private boolean javaPublic;

	/**
	 * true if this variable was initialized in the place of its declaration or previously in an 'init' or 'init:' method
	 */
	private boolean wasInitialized;

}
