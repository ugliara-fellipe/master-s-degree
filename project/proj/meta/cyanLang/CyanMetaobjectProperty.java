package meta.cyanLang;

import java.util.ArrayList;
import ast.CyanMetaobjectWithAtAnnotation;
import ast.InstanceVariableDec;
import ast.MetaobjectArgumentKind;
import ast.MethodSignature;
import meta.CyanMetaobjectWithAt;
import meta.DeclarationKind;
import meta.IActionProgramUnit_ati;
import meta.ICompiler_ati;
import saci.Env;
import saci.Tuple2;

/**
 * generate methods getVarName and setVarName if instance variable varName does not start with '_'. For
 * an instance variable _varName methods varName and varName: are generated.
   @author jose
 */

public class CyanMetaobjectProperty extends CyanMetaobjectWithAt implements IActionProgramUnit_ati {

	public CyanMetaobjectProperty() {
		super(MetaobjectArgumentKind.ZeroParameter);

	}

	@Override
	public String getName() {
		return "property";
	}



	@Override
	public ArrayList<Tuple2<String, StringBuffer>> ati_codeToAddToPrototypes(
			ICompiler_ati compiler) {

		StringBuffer s = new StringBuffer();
		CyanMetaobjectWithAtAnnotation annotation = this.getMetaobjectAnnotation();
		InstanceVariableDec iv = (InstanceVariableDec ) annotation.getDeclaration();
		String name = iv.getName();

		String methodNameGet;
		String methodNameSet;

		if ( name.charAt(0) == '_' ) {
			methodNameGet = name.substring(1, name.length());
			methodNameSet = methodNameGet;
		}
		else {
			String nameUpper = (Character.toUpperCase(name.charAt(0))) + name.substring(1, name.length());
			methodNameGet = "get" + nameUpper;
			methodNameSet = "set" + nameUpper;
		}
		ArrayList<MethodSignature> mList;
		mList = compiler.getProgramUnit()
				.searchMethodPrivateProtectedPublicSuperProtectedPublic(methodNameGet, (Env ) compiler.getEnv());
		if ( mList != null && mList.size() > 0 ) {
			this.addError("Metaobject '" + this.getName() + "' called at line " + annotation.getSymbolMetaobjectAnnotation().getLineNumber()
					+ " needs to create a method called '" + methodNameGet + "'. However this method already exists");
		}
		if ( iv.isShared() ) {
			s.append("    @prototypeCallOnly\n");
		}
		String ivTypeName = iv.getType().getFullName();
		StringBuffer ivname = new StringBuffer();
		int sizeName = ivTypeName.length();
		for (int i = 0; i < sizeName; ++i) {
			char ch = ivTypeName.charAt(i);
			if ( ch == ',' && i < sizeName - 1 && !Character.isWhitespace(ivTypeName.charAt(i+1)) ) {
				ivname.append(", ");
			}
			else {
				ivname.append(ch);
			}
		}

		ivTypeName = ivname.toString();

		s.append("    func " + methodNameGet + " -> " + ivTypeName + " = " + name + ";\n");
		if ( ! iv.isReadonly() ) {

			mList = compiler.getProgramUnit()
					.searchMethodPrivateProtectedPublicSuperProtectedPublic(methodNameSet + ":1", (Env ) compiler.getEnv());
			if ( mList != null && mList.size() > 0 ) {
				this.addError("Metaobject annotation '" + this.getName() + "' at line " + annotation.getSymbolMetaobjectAnnotation().getLineNumber()
						+ " needs to create a method called '" + methodNameSet + ":'. However this method already exists");
			}

			if ( iv.isShared() ) {
				s.append("    @prototypeCallOnly\n");
			}
			s.append("    func " + methodNameSet + ": " + ivTypeName + " other { self." + name + " = other; }\n");
		}
		ArrayList<Tuple2<String, StringBuffer>> tupleArray = new ArrayList<>();
		tupleArray.add( new Tuple2<String, StringBuffer>(compiler.getCurrentPrototypeName(), s));
		return tupleArray;
	}

	@Override
	public DeclarationKind []mayBeAttachedList() {
		return decKindList;
	}

	private static DeclarationKind []decKindList = new DeclarationKind[] { DeclarationKind.INSTANCE_VARIABLE_DEC };


}
