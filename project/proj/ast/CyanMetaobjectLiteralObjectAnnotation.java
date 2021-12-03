package ast;

import java.util.ArrayList;
import error.ErrorKind;
import lexer.CompilerPhase;
import lexer.IWithCompilerPhase;
import lexer.Symbol;
import meta.Compiler_dsa;
import meta.CyanMetaobject;
import meta.CyanMetaobjectLiteralObject;
import meta.IParseWithCyanCompiler_dpa;
import saci.Compiler;
import saci.CyanEnv;
import saci.Env;
import saci.Tuple2;
import saci.Tuple4;
/**
 * Represents a literal object such as
 * 
 *     [* 1:2, 3:1, 1:3 *]
 *  or
 *     n"c:\table\cup\file.txt"
 *  or
 *     r"[A-Z][0-9]+"
 *     
   @author José
 */
public class CyanMetaobjectLiteralObjectAnnotation extends CyanMetaobjectAnnotation {
	
	public CyanMetaobjectLiteralObjectAnnotation(CompilationUnitSuper compilationUnit, ProgramUnit programUnit, 
			CyanMetaobjectLiteralObject cyanMetaobjectLiteralObject) {
		super(compilationUnit, true);
		this.setProgramUnit(programUnit);
		this.setMetaobjectAnnotationNumber(programUnit.getIncMetaobjectAnnotationNumber());
		this.cyanMetaobjectLiteralObject = cyanMetaobjectLiteralObject;
	}

	
	@Override
	public void genCyanReal(PWInterface pw, boolean printInMoreThanOneLine, CyanEnv cyanEnv, boolean genFunctions) {
		/*
		if ( replaceCodeByThis != null ) {
			pw.print(this.replaceCodeByThis);
		}
		else {}
		*/

		if ( codeMetaobjectAnnotationParseWithCompiler != null ) 
			pw.print(codeMetaobjectAnnotationParseWithCompiler);
		else 
			pw.print(this.originalCode);
			
	}

	/**
	 * should have been removed before code generation. Therefore this method should never be called.
	 */
	@Override
	public void genJava(PWInterface pw, Env env) {
		pw.println(" ");
	}
	
	
	public CyanMetaobjectLiteralObject getCyanMetaobjectLiteralObject() {
		return cyanMetaobjectLiteralObject;
	}
	
	public Symbol getSymbolLiteralObject() {
		return symbol;
	}

	public void setSymbolLiteralObject(Symbol symbol) {
		this.symbol = symbol;
	}

	
	@Override
	public Symbol getFirstSymbol() {
		return symbol;
	}


	@Override
	public String genJavaExpr(PWInterface pw, Env env) {
		return " ";
	}

	/**
	 * The metaobject annotation has type <code>packageName.prototypeName</code>. This method returns
	 * <code>packageName</code> which may be a list of identifiers separated by dots.  
	   @return
	 */
	public String getPackageOfType() {
		return cyanMetaobjectLiteralObject.getPackageOfType();
	}
	
	/**
	 * The metaobject annotation has type <code>packageName.prototypeName</code>. This method returns
	 * <code>prototypeName</code>.  
	   @return
	 */
	public String getPrototypeOfType() {
		return cyanMetaobjectLiteralObject.getPrototypeOfType();
	}
		
	@Override
	public boolean isParsedWithCompiler() {
		return this.cyanMetaobjectLiteralObject instanceof IParseWithCyanCompiler_dpa;
	}
	
	@Override
	public void calcInternalTypes(Env env) {
		
		super.calcInternalTypes(env);
		if ( ! env.getCompInstSet().contains(saci.CompilationInstruction.dsa_actions) )
			/*
			 * literal objects should only exist till phase 6 of the compiler. After that
			 * they should be removed from the code.  
			 */
			env.error(true, this.getFirstSymbol(), "Internal error", null, ErrorKind.internal_error);
		else {
			
			
			if ( env.getDuring_dsa_actions() ) {
				env.error(this.getFirstSymbol(), "A dsa action cannot occur inside another dsa actions. For example, you cannot have a macro expansion inside another macro expansion or even a literal object as r\"[a-z]+\" inside a macro");
				//System.out.print("");
			}
			
			try {
				env.begin_dsa_actions();
				Compiler_dsa compiler_dsa = new Compiler_dsa(env, this);
				cyanMetaobjectLiteralObject.setMetaobjectAnnotation(this);


				type = Compiler.singleTypeFromString(cyanMetaobjectLiteralObject.getPackageOfType() + "." +
						cyanMetaobjectLiteralObject.getPrototypeOfType(), 
						this.getFirstSymbol(), "Error in literal object: ", env.getCurrentCompilationUnit(), 
						env.getCurrentProgramUnit(), env);
				
				StringBuffer cyanCode = null;
				
				try {
					cyanCode = cyanMetaobjectLiteralObject.dsa_codeToAdd(compiler_dsa);
				} 
				catch ( error.CompileErrorException e ) {
				}
				catch ( RuntimeException e ) {
					env.thrownException(this, this.getFirstSymbol(), e);
				}
				finally {
					env.errorInMetaobjectCatchExceptions(cyanMetaobjectLiteralObject, this);
				}
				
				
				if ( cyanCode != null ) {
					if ( this.isParsedWithCompiler() && 
							env.sizeStackMetaobjectAnnotationParseWithCompiler() > 1 ) {
						/*
						 * this metaobject annotation is a literal object that is inside other literal object
						 */
						this.setCodeMetaobjectAnnotationParseWithCompiler(cyanCode);
					}
					else {
						  /*
						   * literal objects are always removed from the source code
						   */
					
						env.removeCodeMetaobjectAnnotation(cyanMetaobjectLiteralObject);
						env.addCodeAtMetaobjectAnnotation(cyanMetaobjectLiteralObject, cyanCode, -1);
						this.codeThatReplacesThisExpr = cyanCode;
					}
				}
					

				ArrayList<Tuple2<String, StringBuffer>> prototypeNameCodeList = null;
				try {
					prototypeNameCodeList = this.cyanMetaobjectLiteralObject.dsa_NewPrototypeList(compiler_dsa);
				} 
				catch ( error.CompileErrorException e ) {
				}
				catch ( RuntimeException e ) {
					env.thrownException(this, this.getFirstSymbol(), e);
				}
				finally {
					env.errorInMetaobjectCatchExceptions(this.cyanMetaobjectLiteralObject, this);
				}
				if ( prototypeNameCodeList != null ) {
					for ( Tuple2<String, StringBuffer> prototypeNameCode : prototypeNameCodeList ) {
						CompilationUnit cunit = (CompilationUnit ) this.compilationUnit;
						Tuple2<CompilationUnit, String> t = env.getProject().getCompilerManager().createNewPrototype(prototypeNameCode.f1, prototypeNameCode.f2, 
								cunit.getCompilerOptions(), cunit.getCyanPackage());
						if ( t != null && t.f2 != null ) {
							env.error(symbol, t.f2);
						}
					}
				}
			}
			finally {
				env.end_dsa_actions();
				finalizeCalcInternalTypes(env);
			}
				
		}
	}

	@Override
	public CompilerPhase getPostfix() {
		if ( symbol instanceof IWithCompilerPhase )  
			return ((IWithCompilerPhase ) symbol).getPostfix();
		else
			return null;
	}

	@Override

	public CyanMetaobject getCyanMetaobject() {
		return cyanMetaobjectLiteralObject;
	}
	
	

	public Object getInfo() {
		return info;
	}
	public void setInfo(Object info) {
		this.info = info;
	}

	public String getOriginalCode() {
		return originalCode;
	}


	public void setOriginalCode(String originalCode) {
		this.originalCode = originalCode;
	}

	
	@Override
	public ArrayList<Tuple4<Integer, Integer, Integer, Integer>> getColorTokenList() {
		if ( colorTokenList == null ) {
			if ( this.cyanMetaobjectLiteralObject != null ) {
				colorTokenList = this.cyanMetaobjectLiteralObject.getColorTokenList(this);
			}
		}
		return colorTokenList;
	}
	
	
	/**
	 * information on the number gathered during parsing. It is usually the Cyan code that
	 * should replace the number during semantic analysis 
	 */
	private Object info;	
	
	private Symbol symbol;

	private CyanMetaobjectLiteralObject cyanMetaobjectLiteralObject;
	
	/**
	 * the original text of the metaobject annotation. In "101bin" it is "101bin". In "[* (1, 2), (2, 3), (3, 1) *]"
	 * it should be "(1, 2), (2, 3), (3, 1)" or "[* (1, 2), (2, 3), (3, 1) *]" (not sure which!!!).
	 */
	protected String originalCode;

}
