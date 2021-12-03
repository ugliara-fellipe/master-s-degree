package meta.cyanLang;

import java.util.HashSet;
import ast.CyanMetaobjectWithAtAnnotation;
import ast.MetaobjectArgumentKind;
import ast.VariableDecInterface;
import meta.CyanMetaobjectWithAt;
import meta.IAction_dsa;
import meta.ICommunicateInPrototype_ati_dsa;
import meta.ICompiler_dsa;
import saci.DirectoryPackage;
import saci.NameServer;
import saci.Tuple4;

public class CyanMetaobjectKeepValue extends CyanMetaobjectWithAt 
   implements IAction_dsa,
   ICommunicateInPrototype_ati_dsa {

	public CyanMetaobjectKeepValue() {
		super(MetaobjectArgumentKind.OneParameter);
	}

	@Override
	public String getName() {
		return "keepValue";
	}

	@Override
	public StringBuffer dsa_codeToAdd(ICompiler_dsa compiler_dsa) {
		
		checkIfPackageWasImported(compiler_dsa);
		StringBuffer s = new StringBuffer();
		
		// CyanMetaobjectWithAtAnnotation annotation = (ast.CyanMetaobjectWithAtAnnotation ) this.metaobjectAnnotation;
		CyanMetaobjectWithAtAnnotation annotation = this.getMetaobjectAnnotation();
		Object first = annotation.getJavaParameterList().get(0);
		boolean ok = true;
		if ( !(first instanceof String) ) {
			ok = false;
		}
		else {
			String p = NameServer.removeQuotes((String ) first);
			VariableDecInterface v = compiler_dsa.searchLocalVariableParameter(p);
			if ( v == null ) {
				v = compiler_dsa.searchInstanceVariable(p);
			}
			if ( v == null ) {
				ok = false;
			}
			else {
				String filename = compiler_dsa.escapeString(compiler_dsa.getPathFileHiddenDirectory(
						annotation.getPrototypeOfAnnotation(),
						annotation.getPackageOfAnnotation(), 
						DirectoryPackage.TMP) +  "keepValue_" + v.getName() + ".txt");
				/*
				 * the file name will be
				 * 
				 */
				/*
				s.append("OutTextFile open: \"C:\\\\Dropbox\\\\Cyan\\\\keep.txt\"\n" +  
                         "   		  maxNumLines: 5\n" + 
                         "            write: " + p + " asString ++ \"\\n\"\n" +
                         "            catch: CatchExceptionIOMessage;\n"); 
				 */
				// Tuple3<String, String, CyanPackage> t = compiler_dsa.getAbsolutePathDSLFile(String fileName, String packageName);
				s.append("OutTextFile open: \"" + filename + "\"\n" +  
                         "   		  maxNumLines: 5\n" + 
                         "            write: " + p + " asString ++ \"\\n\"\n" +
                         "            catch: CatchExceptionIOMessage;\n"); 
			}
		}

		if ( !ok ) {
			this.addError("A variable name was expected as paramter to this metaobject");
		}
		return s;
	}

	@Override
	public Object ati_dsa_shareInfoPrototype()  {
		CyanMetaobjectWithAtAnnotation annotation = this.getMetaobjectAnnotation();
		Object first = annotation.getJavaParameterList().get(0);
		return first;
	}
	
	@Override
	public void ati_dsa_receiveInfoPrototype(HashSet<Tuple4<String, Integer, Integer, Object>> moInfoSet)  {
		CyanMetaobjectWithAtAnnotation annotation = this.getMetaobjectAnnotation();
		if ( annotation.getJavaParameterList() == null || annotation.getJavaParameterList().size() == 0 )
			return ;
		Object first = annotation.getJavaParameterList().get(0);
		int count = 0;
		for ( Tuple4<String, Integer, Integer, Object> t : moInfoSet ) {
			if ( t.f1.equals("keepValue") && t.f4.equals(first) ) {
				++count;
			}
		}
		
		if ( count > 1 && annotation.getMetaobjectAnnotationNumberByKind() == 1 ) {
			this.addError("There is more than one annotation of metaobject keepValue with the same parameters");
		}
	}
}