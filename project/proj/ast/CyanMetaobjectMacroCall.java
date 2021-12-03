/**
  
 */
package ast;


import java.util.ArrayList;
import error.ErrorKind;
import lexer.CompilerPhase;
import lexer.Symbol;
import meta.Compiler_dsa;
import meta.CyanMetaobject;
import meta.CyanMetaobjectMacro;
import saci.CyanEnv;
import saci.Env;
import saci.Tuple2;
import saci.Tuple4;

/**
   @author José
   
 */
public class CyanMetaobjectMacroCall extends CyanMetaobjectAnnotation {


	public CyanMetaobjectMacroCall(CyanMetaobjectMacro cyanMacro, CompilationUnit compilationUnit, 
			 ProgramUnit programUnit, Symbol firstSymbol, boolean inExpr) {
		super(compilationUnit, inExpr);
		this.setProgramUnit(programUnit);
		this.setMetaobjectAnnotationNumber(programUnit.getIncMetaobjectAnnotationNumber());		
		this.cyanMacro = cyanMacro;
		this.firstSymbol = firstSymbol;
		lastSymbolMacroCall = null;
	}
	
	@Override
	public void genCyanReal(PWInterface pw, boolean printInMoreThanOneLine, CyanEnv cyanEnv, boolean genFunctions) {
	
		/*
		if ( this.replaceCodeByThis != null ) {
			pw.print(this.replaceCodeByThis);
		}
		else {}
		*/

		if ( codeMetaobjectAnnotationParseWithCompiler != null ) 
			pw.print(codeMetaobjectAnnotationParseWithCompiler);
		else
			pw.print(this.originalText);
			
			
	}

	/**
	 * should have been removed before code generation. Therefore this method should never be called.
	 */
	@Override
	public void genJava(PWInterface pw, Env env) {
	}
	
	@Override
	public Symbol getFirstSymbol() {
		return firstSymbol;
	}

	
	@Override
	public boolean isParsedWithCompiler() {
		return true;
	}
	
	
	@Override
	public void calcInternalTypes(Env env) {
		if ( env.getDuring_dsa_actions() ) {
			env.error(this.getFirstSymbol(), "A dsa action cannot occur inside another dsa actions. For example, you cannot have a macro expansion inside another macro expansion or even a literal object as r\"[a-z]+\" inside a macro");
		}
		try {
			env.begin_dsa_actions();
			super.calcInternalTypes(env);
			if ( env.getCompInstSet().contains(saci.CompilationInstruction.dsa_actions) ) {
				Compiler_dsa compiler_dsa = new Compiler_dsa(env, this);
				cyanMacro.setMetaobjectAnnotation(this);
				StringBuffer cyanCode = null;
				
				
				try {
					cyanCode = cyanMacro.dsa_codeToAdd(compiler_dsa);
					
				} 
				catch ( error.CompileErrorException e ) {
				}
				catch ( RuntimeException e ) {
					env.thrownException(this, this.getFirstSymbol(), e);
				}
				finally {
					env.errorInMetaobjectCatchExceptions(cyanMacro, this);
				}
				
				
				if ( cyanCode != null ) {
					
					if ( env.sizeStackMetaobjectAnnotationParseWithCompiler() > 1 ) {
						/*
						 * this metaobject annotation is a literal object that is inside other literal object
						 */
						this.setCodeMetaobjectAnnotationParseWithCompiler(cyanCode);
					}
					else {
						
						  /*
						   * macros are always removed from the source code
						   */
						env.removeCodeMetaobjectAnnotation(cyanMacro);
						Symbol lastSymbol = this.lastSymbolMacroCall;
						
						env.addCodeAtMetaobjectAnnotation(cyanMacro, cyanCode, lastSymbol.getOffset() + lastSymbol.getSymbolString().length());
						
						this.codeThatReplacesThisExpr = cyanCode;
						
					}
				}
				ProgramUnit pu = env.searchPackagePrototype(cyanMacro.getPackageOfType(), cyanMacro.getPrototypeOfType());
				if ( pu == null ) 
					env.error(true, 
							this.getFirstSymbol(), 
									"Macro has type '" + cyanMacro.getPackageOfType() + "." +  
											cyanMacro.getPrototypeOfType() + "' which was not found", cyanMacro.getPrototypeOfType(), ErrorKind.prototype_was_not_found_inside_method);
				else
					type = pu;			
				
				ArrayList<Tuple2<String, StringBuffer>> prototypeNameCodeList = null;
				try {
					prototypeNameCodeList = this.cyanMacro.dsa_NewPrototypeList(compiler_dsa);
				} 
				catch ( error.CompileErrorException e ) {
				}
				catch ( RuntimeException e ) {
					env.thrownException(this, this.getFirstSymbol(), e);
				}
				finally {
					env.errorInMetaobjectCatchExceptions(this.cyanMacro, this);
				}
				if ( prototypeNameCodeList != null ) {
					for ( Tuple2<String, StringBuffer> prototypeNameCode : prototypeNameCodeList ) {
						CompilationUnit cunit = (CompilationUnit ) this.compilationUnit;
						Tuple2<CompilationUnit, String> t = env.getProject().getCompilerManager().createNewPrototype(prototypeNameCode.f1, prototypeNameCode.f2, 
								cunit.getCompilerOptions(), cunit.getCyanPackage());
						if ( t != null && t.f2 != null ) {
							env.error(firstSymbol, t.f2);
						}
					}
				}
				
			}
			finalizeCalcInternalTypes(env);

			
		}
		finally {
			env.end_dsa_actions();
		}
	}

	


	@Override
	public CompilerPhase getPostfix() {
		return null;
	}

	@Override
	public CyanMetaobject getCyanMetaobject() {
		return cyanMacro;
	}
	

	public void setLastSymbolMacroCall(Symbol lastSymbolMacroCall) {
		this.lastSymbolMacroCall = lastSymbolMacroCall;
	}
	public Symbol getLastSymbolMacroCall() {
		return lastSymbolMacroCall;
	}

	@Override
	public ArrayList<Tuple4<Integer, Integer, Integer, Integer>> getColorTokenList() {
		if ( colorTokenList == null ) {
			if ( this.cyanMacro != null ) {
				colorTokenList = this.cyanMacro.getColorTokenList(this);
			}
		}
		return colorTokenList;
	}
	
	private Symbol firstSymbol;

	private CyanMetaobjectMacro cyanMacro;


	/**
	 * last symbol of the macro call
	 */
	private Symbol lastSymbolMacroCall;

}
