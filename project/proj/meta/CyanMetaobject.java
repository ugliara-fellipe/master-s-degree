package meta;

import java.util.ArrayList;
import ast.CyanMetaobjectAnnotation;
import ast.CyanPackage;
import lexer.Lexer;
import saci.NameServer;
import saci.Tuple4;

/**
 * Represents a Cyan metaobject which may be:
 * - a macro
 * - a literal object
 * - a metaobject annotation with @ such as {@literal @}init(name, age)
 * 
   @author José
 */
abstract public class CyanMetaobject implements Cloneable {

	public CyanMetaobject() {
		errorList = null;
	}

	public CyanMetaobjectAnnotation getMetaobjectAnnotation() {
		return metaobjectAnnotation;
	}

	/**
	 * the name of the metaobject. A regular metaobject such as <code>{@literal @}checkStyle</code> has name
	 * <code>"checkStyle"</code>. A macro that use keywords "k1" and "k2" has name <code>"macro(k1, k2)"</code>.
	 * The name of a literal object that starts with a 
	 * sequence of symbols such as</p>
	 * <code>{* 1:2, 2:3 *}</code></p>
	 * is the left sequence of symbols, <code>"{{@literal *}"</code>. See comments for the names of
	 * literal strings and numbers.  
	   @return
	 */
	abstract public String getName();
	
	public String getFileName() {
		return fileName;
	}
	public void setFileName(String fileName) {
		this.fileName = fileName;
	}
	public String getPackageName() {
		return packageName;
	}
	public void setPackageName(String packageName) {
		this.packageName = packageName;
	}
	
	public void setMetaobjectAnnotation(CyanMetaobjectAnnotation metaobjectAnnotation) {
		this.metaobjectAnnotation = metaobjectAnnotation;
	}
	
	/**
	 * the replacement policy when creating an instantiation of a generic prototype.
	 * See enumeration {@see ReplacementPolicyInGenericInstantiation}. The default value
	 * is ReplacementPolicyInGenericInstantiation.REPLACE_BY_CYAN_VALUE
	 */
	public ReplacementPolicyInGenericInstantiation getReplacementPolicy() {
		return ReplacementPolicyInGenericInstantiation.REPLACE_BY_CYAN_VALUE;
	}
	

	/**
	 * If the metaobject annotation has type <code>packageName.prototypeName</code>, this method returns
	 * <code>packageName</code> which may be a list of identifiers separated by dots.  Not all metaobject
	 * calls have types (return null in those cases). For example, the annotation of <code>checkStyle</code> does not have a type:
	 * <code> <br>
	 * {@literal @}checkStyle  <br>
	 * object Person <br>
	 * ...<br>
	 * end<br>
	 * </code><br>
	 * But all literal objects have types. So do some macro calls and some calls to metaobjects with {@literal @}.
	 * <code> <br>
	 * var i = 101bin;<br>
	 * ok = neg notFound; // neg is a macro<br>
	 * var g = {@literal @}graph{* 1:2, 2:3 *};<br>
	 * <br>
	 * </code><br>
	 * 
	 * This method may be called even before the metaobject annotation is set. Therefore it should not 
	 * consider that the field cyanMetaobjectAnnotation is non-null. 
	 * 	   @return
	 */	
	abstract public String getPackageOfType(); // { return null; }
	/**
	 * If the metaobject annotation has type <code>packageName.prototypeName</code>, this method returns
	 * <code>prototypeName</code>.  See {@link CyanMetaobjectLiteralObject#getPackageOfType()}
	   @return
	 */
	
	abstract public String getPrototypeOfType();
	

	/**
	 * 
	 * This method should be called by a IDE plugin to show the text associated
	 * to the metaobject annotation <code>metaobjectAnnotation</code> in several colors
	 * (text highlighting).
	 * 
	 * Each tuple (color number, line number, column number, size). <br>
	 * The characters starting at line number, column number till column number
	 * + size - 1 should be highlighted in color "color number".
	 * 
	 * <code>metaobjectAnnotation</code> is redundant nowadays because this class
	 * already has an instance variable with the same contents.
	 * 
	 * @return
	 */
	public ArrayList<Tuple4<Integer, Integer, Integer, Integer>> getColorList(CyanMetaobjectAnnotation metaobjectAnnotation) {
		return null;
	}	

	/**
	 * add an error for a call to a method of this metaobject. This method should be called by
	 * methods of a metaobject that should signal an error. 
	 */
	protected ArrayList<CyanMetaobjectError> addError(String message) {
		if ( errorList == null ) {
			errorList = new ArrayList<>();
		}
		errorList.add(new CyanMetaobjectError(message,
				this.metaobjectAnnotation.getFirstSymbol().getLineNumber(), metaobjectAnnotation.getFirstSymbol().getColumnNumber(),
				metaobjectAnnotation.getFirstSymbol().getOffset()));
		return errorList;
	}
	
	
	/**
	 * add an error for a call to a method of this metaobject. This method should be called by
	 * methods of a metaobject that should signal an error. 
	 */
	protected ArrayList<CyanMetaobjectError> addError(String message, CyanMetaobjectAnnotation cyanMetaobjectAnnotation) {
		if ( errorList == null ) {
			errorList = new ArrayList<>();
		}
		
		errorList.add(new CyanMetaobjectError(message,
				cyanMetaobjectAnnotation.getFirstSymbol().getLineNumber(), cyanMetaobjectAnnotation.getFirstSymbol().getColumnNumber(),
				cyanMetaobjectAnnotation.getFirstSymbol().getOffset()));
		return errorList;
	}

	/**
	 * add an error for a call to a method of this metaobject. This method should be called by
	 * methods of a metaobject that should signal an error. <code>line</code> and <code>column</code> 
	 * are relative to the start of the metaobject annotation. <code>offset</code> is the number of characters from the
	 * start of the metaobject annotation.
	 */
	protected ArrayList<CyanMetaobjectError> addError(String message, int line, int column, int offset) {
		if ( errorList == null ) {
			errorList = new ArrayList<>();
		}
		errorList.add( new CyanMetaobjectError(message, line, column, offset) );
		return errorList;
	}
	

	
	public boolean isExpression() {
		return true;
	}
	

	/**
	 * After each call to a metaobject method, this method returns
	 * the error messages associated to that call. The error list is cleaned.
	   @return
	 */

	public final ArrayList<CyanMetaobjectError> getErrorMessageList() {
		ArrayList<CyanMetaobjectError> ret; 
		if ( errorList != null ) {
			ret = (ArrayList<CyanMetaobjectError>) errorList.clone();
			errorList.clear();
			return ret;
		}
		else {
			return null;
		}
	}
	

	/** check if all packages in the list shouldImportList are imported by the current compilation unit
	   @param compiler_dsa
	 */
	protected void checkIfPackageWasImported(ICompiler_ati compiler_ati, String ...shouldImportList) {
		for ( String shouldImportName : shouldImportList ) {
			boolean found = false;
			if ( compiler_ati.getEnv().getImportedPackageSet() != null ) {
				for ( CyanPackage aPackage : compiler_ati.getEnv().getImportedPackageSet() ) {
					String name = aPackage.getPackageName();
					if ( name.equals(shouldImportName) ) {
						found = true;
						break;
					}
				}
				if ( ! found ) {
					this.addError("This metaobject demands that package '" + shouldImportName + 
							"' be imported. Then put \n    import " + shouldImportName + "\nbefore the prototype declaration. " + 
							  "Do not forget to change the project file (.pyan) to include this package");
				}
			}
		}
	}

	
	
	/** check if all packages in the list shouldImportList are imported by the current compilation unit
	   @param compiler_dsa
	 */
	protected void checkIfPackageWasImported(ICompiler_dsa compiler_dsa, String ...shouldImportList) {
		for ( String shouldImportName : shouldImportList ) {
			boolean found = false;
			if ( compiler_dsa.getEnv().getImportedPackageSet() != null ) {
				for ( CyanPackage aPackage : compiler_dsa.getEnv().getImportedPackageSet() ) {
					String name = aPackage.getPackageName();
					if ( name.equals(shouldImportName) ) {
						found = true;
						break;
					}
				}
				if ( ! found ) {
					this.addError("This metaobject demands that package '" + shouldImportName + 
							"' be imported. Then put \n    import " + shouldImportName + "\nbefore the prototype declaration. " + 
							  "Do not forget to change the project file (.pyan) to include this package");
				}
			}
		}
	}

	
	/** check if all packages in the list shouldImportList are imported by the current compilation unit
	   @param compiler_dsa
	 */
	protected void checkIfPackageWasImported(ICompiler_dpa compiler_dpa, String ...shouldImportList) {
		for ( String shouldImportName : shouldImportList ) {
			boolean found = false;
			if ( compiler_dpa.getCompilationUnit().getImportPackageSet() != null ) {
				for ( CyanPackage aPackage : compiler_dpa.getCompilationUnit().getImportPackageSet() ) {
					String name = aPackage.getPackageName();
					if ( name.equals(shouldImportName) ) {
						found = true;
						break;
					}
				}
			}
			if ( ! found ) {
				this.addError("This metaobject demands that package '" + shouldImportName + 
						"' be imported. Then put \n    import " + shouldImportName + "\nbefore the prototype declaration. " + 
						  "Do not forget to change the project file (.pyan) to include this package");
			}
		}
	}


	static public String escapeString(String s) {
		return Lexer.escapeJavaString(s);
	}
	static public String unescapeString(String s) {
		return Lexer.unescapeJavaString(s);
	}	
	
	static public String removeQuotes(String s) {
		return NameServer.removeQuotes(s);
	}

	private ArrayList<CyanMetaobjectError> errorList;	

	/**
	 * name of the ".class" file in which this metaobject is.
	 */
	protected String fileName;

	/**
	 *  name of the package in which this metaobject was declared
	 */
	protected String packageName;
	/**
	 * the specific metaobject annotation of this metaobject. For each
	 * annotation an object of a class that inherits from CyanMetaobject is created.
	 * Then this field is set with the annotation.
	 */
	protected CyanMetaobjectAnnotation metaobjectAnnotation;

}
