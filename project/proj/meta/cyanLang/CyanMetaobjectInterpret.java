package meta.cyanLang;

import java.util.ArrayList;
import ast.ASTVisitor;
import ast.CyanMetaobjectWithAtAnnotation;
import ast.MetaobjectArgumentKind;
import ast.MethodDec;
import ast.ObjectDec;
import meta.CyanMetaobject;
import meta.CyanMetaobjectWithAt;
import meta.DeclarationKind;
import meta.IActionProgramUnit_ati;
import meta.IAction_dpa;
import meta.IAction_dsa;
import meta.ICompilerAction_dpa;
import meta.ICompiler_ati;
import meta.ICompiler_dsa;
import saci.Tuple2;

public class CyanMetaobjectInterpret  extends CyanMetaobjectWithAt implements IAction_dpa, IActionProgramUnit_ati, IAction_dsa {

	public CyanMetaobjectInterpret() {
		super(MetaobjectArgumentKind.OneOrMoreParameters);
	}

	@Override
	public String getName() {
		return "interpret";
	}
	
	
	
	@Override
	public StringBuffer dpa_codeToAdd(ICompilerAction_dpa compiler) {
		/*
		CyanMetaobjectWithAtAnnotation annot = this.getMetaobjectAnnotation();
		ArrayList<Object> paramList = annot.getJavaParameterList();
		if ( paramList == null ) { 
			System.out.println("paramList == null");
		}
		// getDeclaration returns null because the declaration have not
		// been parsed at this point
		if ( annot.getDeclaration() instanceof ObjectDec ) {
		}
		*/
		return null;
	}

	@SuppressWarnings("unchecked")
	@Override
	public ArrayList<Tuple2<String, StringBuffer>> ati_codeToAddToPrototypes(
			ICompiler_ati compiler) {  
	
		
		CyanMetaobjectWithAtAnnotation annot = this.getMetaobjectAnnotation();
		
		if ( annot.getDeclaration() instanceof ObjectDec ) {
			((ObjectDec) annot.getDeclaration()).accept( new ASTVisitor() { 
				@Override
				public void visit(MethodDec node) { 
					System.out.println(node.getName());
				}
			} 
			);
			ArrayList<Object> paramList = annot.getJavaParameterList();
			StringBuffer out = new StringBuffer();
			int n = 0;
			for ( Object obj : paramList ) {
				String s = (String ) obj;
				s = CyanMetaobject.removeQuotes(s);
				
				out.append("    func " + s + " -> Int = " + n + ";\n" );
				++n;
			}
			ArrayList<Tuple2<String, StringBuffer>> array = new ArrayList<>();
			array.add( new Tuple2<String, StringBuffer>(annot.getPrototypeOfAnnotation(), out));
			return array;
		}
		return null; 
	}
	
	@Override
	public StringBuffer dsa_codeToAdd(ICompiler_dsa compiler_dsa) {
		return null;
	}

	/*
	@Override
	public ArrayList<Tuple3<String, String, StringBuffer>> ati_CodeToAdd(ICompilerProgramView_ati compiler) {
		CyanMetaobjectWithAtAnnotation annot = this.getMetaobjectAnnotation();
		
		if ( annot.getDeclaration() instanceof Program ) {
			((Program) annot.getDeclaration()).accept( new ASTVisitor() { 
				@Override
				public void visit(MethodDec node) { 
					System.out.println(node.getName());
				}
			} 
			);
			
		}
		
		return null; 
	}
	*/

	@Override
	public DeclarationKind []mayBeAttachedList() {
		return decKindList;
	}


	
	private static DeclarationKind []decKindList = new DeclarationKind[] { DeclarationKind.INSTANCE_VARIABLE_DEC,
		DeclarationKind.METHOD_DEC, DeclarationKind.PROTOTYPE_DEC,
		DeclarationKind.PACKAGE_DEC, DeclarationKind.PROGRAM_DEC };
	
}
