package meta.treplica;

import java.util.ArrayList;

import ast.CyanMetaobjectWithAtAnnotation;
import ast.Declaration;
import ast.MetaobjectArgumentKind;
import ast.StatementLocalVariableDecList;
import meta.CyanMetaobjectError;
import meta.CyanMetaobjectWithAt;
import meta.DeclarationKind;
import meta.IActionVariableDeclaration_dsa;

public class CyanMetaobjectTreplicaInit extends CyanMetaobjectWithAt
		implements IActionVariableDeclaration_dsa {

	public CyanMetaobjectTreplicaInit() {
		super(MetaobjectArgumentKind.ZeroOrMoreParameters);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see ast.CyanMetaobject#getName()
	 */
	@Override
	public String getName() {
		return "treplicaInit";
	}

	@Override
	public ArrayList<CyanMetaobjectError> check() {
		return null;
	}

	@Override
	public DeclarationKind[] mayBeAttachedList() {
		return decKindList;
	}

	private static DeclarationKind[] decKindList = new DeclarationKind[] { DeclarationKind.LOCAL_VAR_DEC };

	@Override
	public StringBuffer dsa_codeToAddAfter() {
		CyanMetaobjectWithAtAnnotation withAt = (CyanMetaobjectWithAtAnnotation) this.getMetaobjectAnnotation();
		Declaration dec = withAt.getDeclaration();
		StatementLocalVariableDecList varList = (StatementLocalVariableDecList) dec;
		
		ArrayList<Object> parameterList = withAt.getJavaParameterList();
		String []strList = new String[parameterList.size()];
		int i = 0;
		for ( Object elem : parameterList ) {
			if ( (elem instanceof Integer) ) {
				strList[i] = Integer.toString(((Integer) elem).intValue());
			}else if ( (elem instanceof String) ) {
				strList[i] = (String) elem;
			}
			++i;
		}
		
		String nameTreplicaVar = "treplica" + varList.getLocalVariableDecList().get(0).getName();
		StringBuffer code = new StringBuffer("var " + nameTreplicaVar + " = Treplica new;\n");
		code.append(nameTreplicaVar + " runMachine: ");
		code.append(varList.getLocalVariableDecList().get(0).getName());
		code.append(" numberProcess: ");
		code.append(strList[0]); 
		code.append(" rtt: ");
		code.append(strList[1]); 
		code.append(" path: ");
		code.append(strList[2]); 
		code.append(";\n");
		code.append(varList.getLocalVariableDecList().get(0).getName() + " setTreplica: " + nameTreplicaVar + ";\n");
		
		return code;
	}

}
