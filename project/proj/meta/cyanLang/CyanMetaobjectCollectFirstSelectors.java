package meta.cyanLang;

import java.util.ArrayList;
import ast.ASTVisitor;
import ast.CompilationUnit;
import ast.CyanPackage;
import ast.MetaobjectArgumentKind;
import ast.MethodSignatureWithSelectors;
import ast.ObjectDec;
import ast.Program;
import ast.ProgramUnit;
import ast.SelectorWithParameters;
import meta.CyanMetaobjectWithAt;
import meta.IActionProgram_ati;
import meta.ICompilerProgramView_ati;
import saci.NameServer;
import saci.Tuple3;

public class CyanMetaobjectCollectFirstSelectors extends CyanMetaobjectWithAt implements IActionProgram_ati {

	public CyanMetaobjectCollectFirstSelectors() {
		super(MetaobjectArgumentKind.ZeroParameter);
	}

	@Override
	public String getName() {
		return "collectFirstSelectors";  
	}


	private String currentPackageName = "";

	@Override
	public ArrayList<Tuple3<String, String, StringBuffer>> ati_CodeToAdd(ICompilerProgramView_ati compiler) { 

		objList = new ArrayList<>();
		compiler.callVisitor( new ASTVisitor() {
			@Override
			public void visit(MethodSignatureWithSelectors node) {
				if ( ! currentPackageName.equals(NameServer.cyanLanguagePackageName) ) {
					ArrayList<SelectorWithParameters> spList = node.getSelectorArray();
					
					objList.add(spList.get(0).getName());
				}
			}
			@Override
			public void preVisit(CyanPackage node) {
				currentPackageName = node.getPackageName();
			}
			
		});
		
		
		if ( objList.size() > 0 ) {
			ArrayList<Tuple3<String, String, StringBuffer>> codeToAddList = new ArrayList<>();
			Program program = compiler.getProgram();
			for ( CyanPackage cyanPackage : program.getPackageList() ) {
				if ( cyanPackage.getPackageName().equals(NameServer.cyanLanguagePackageName) )
					continue;
				for ( CompilationUnit cunit : cyanPackage.getCompilationUnitList() ) {
					ProgramUnit pu = cunit.getPublicPrototype();
					if ( pu instanceof ObjectDec ) {
						String packageName1 = cyanPackage.getPackageName();
						String prototypeName = pu.getName();
						StringBuffer code = new StringBuffer();
						code.append("    func getObjectCreation -> Array<String> {\n");
						code.append("        return [ ");
						int size = objList.size();
						for ( String s : objList ) {
							code.append("\"" + s + "\"");
							if ( --size > 0 ) 
								code.append(", ");
						}
						code.append(" ];\n");
						code.append("    }\n");
						codeToAddList.add(new Tuple3<String, String, StringBuffer>(packageName1, prototypeName, code));						
					}
				}
			}
			
			return codeToAddList;
		}
		return null;
	}
	
	ArrayList<String> objList;
	
}
