package meta.cyanLang;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.Vector;
import ast.ASTVisitor;
import ast.CompilationUnit;
import ast.CyanPackage;
import ast.Declaration;
import ast.ExprIdentStar;
import ast.ExprMessageSendUnaryChainToExpr;
import ast.ExprMessageSendUnaryChainToSuper;
import ast.ExprMessageSendWithSelectorsToExpr;
import ast.ExprMessageSendWithSelectorsToSuper;
import ast.IdentStarKind;
import ast.MetaobjectArgumentKind;
import ast.MethodDec;
import ast.ObjectDec;
import ast.ParameterDec;
import ast.Program;
import ast.ProgramUnit;
import javafx.util.Pair;
import lexer.Lexer;
import meta.CyanMetaobjectWithAt;
import meta.DeclarationKind;
import meta.IActionProgramUnit_ati;
import meta.ICheckProgramUnit_dsa2;
import meta.ICompiler_ati;
import meta.ICompiler_dsa;
import saci.NameServer;
import saci.Tuple2;
import saci.Tuple3;

public class CyanMetaobjectTreplicaAction extends CyanMetaobjectWithAt implements IActionProgramUnit_ati, ICheckProgramUnit_dsa2   {

	public CyanMetaobjectTreplicaAction() {
		super(MetaobjectArgumentKind.ZeroParameter);
		visited = new Vector<MethodDec>();

		methodsND = new HashSet<>();
		methodsND.add(new Pair<String, String>("Program", "behavior"));
		methodsND.add(new Pair<String, String>("Number", "nonDeterministic"));

	}
	

	HashSet<Pair<String, String>> methodsND;
	

	@Override
	public String getName() {
		return "treplicaAction";
	}
	
	@Override
	public DeclarationKind[] mayBeAttachedList() {
		return decKindList;
	}

	private static DeclarationKind[] decKindList = new DeclarationKind[] { DeclarationKind.METHOD_DEC };
	
	@Override
	public ArrayList<Tuple2<String, StringBuffer>> ati_codeToAddToPrototypes(
			ICompiler_ati compiler) { 
		// Method Name
		MethodDec md = (MethodDec ) this.getAttachedDeclaration();
		String methodName = md.getNameWithoutParamNumber();
		//System.out.println(methodName);
		if(methodName.charAt(methodName.length() - 1) == ':') {
			methodName = methodName.substring(0, methodName.length() - 1);
		}
		String funcPar = "";
		String hasFunPar = " ";
		String callPar = "";
		String hasCallPar = "";
		String funcRet = "";
		String hasCallRet = "";
		String retStatement = "";
		for( ParameterDec par : md.getMethodSignature().getParameterList() ) {
			funcPar += " " + par.getType().getName() + " " + par.getName() + ",";
			callPar += " " + par.getName() + ",";
		}
		
		if( md.getMethodSignature().getReturnTypeExpr() != null ) {
			hasCallRet = " -> ";
			funcRet = md.getMethodSignature().getReturnTypeExpr().getType().getName();
			retStatement = "@javacode<<<\n"
					+			"return (" + md.getMethodSignature().getReturnTypeExpr().getType().getJavaName() + ")_ret;\n"
					+		">>>\n";
		}
		
		if(funcPar.length() > 0){
			funcPar = funcPar.substring(0, funcPar.length() - 1);
			hasFunPar = ": ";
		}
		if(callPar.length() > 0 ){
			callPar = callPar.substring(0, callPar.length() - 1);
			hasCallPar = ": ";
		}
		
		// Object Name
		String objectName = compiler.getCurrentPrototypeName();
		String prototypeName = NameServer.removeQuotes(objectName + methodName);
		
		StringBuffer code = new StringBuffer(
				"\n\n"
			+	"func " + methodName + hasFunPar + funcPar + hasCallRet + funcRet + " {\n"
			+		"var action = " + prototypeName + " new" + hasCallPar + callPar + ";\n"
			+		"var Dyn ret = getTreplica execute: action;\n"
			+		retStatement
			+	"}\n"
		);
		ArrayList<Tuple2<String, StringBuffer>> result = new ArrayList<>();
		result.add(new Tuple2<String, StringBuffer>(objectName, code));
		return result;
	}
	
	@Override
	public ArrayList<Tuple3<String, String, String []>> ati_renameMethod(
			ICompiler_ati compiler_ati) {
		String prototypeName = compiler_ati.getCurrentPrototypeName();
		
		MethodDec md = (MethodDec ) this.getAttachedDeclaration();
		String oldMethodName = md.getName();
		
		String newMethodName[] = new String[1];
		String auxName = md.getNameWithoutParamNumber();
		if(auxName.charAt(auxName.length() - 1) == ':') {
			auxName = auxName.substring(0, auxName.length() - 1);
		}
		newMethodName[0] = auxName + "TreplicaAction";
		//System.out.println(oldMethodName + "  "+ auxName);
		ArrayList<Tuple3<String, String, String []>> tupleList = new ArrayList<>();
		tupleList.add( new Tuple3<String, String, String []>(prototypeName, oldMethodName, newMethodName));
		return tupleList;
	}
	
	@Override
	public ArrayList<Tuple2<String, StringBuffer>> ati_NewPrototypeList(
			ICompiler_ati compiler_ati) {
		// Method Name
		MethodDec md = (MethodDec ) this.getAttachedDeclaration();
		String methodName = md.getNameWithoutParamNumber();
		if(methodName.charAt(methodName.length() - 1) == ':') {
			methodName = methodName.substring(0, methodName.length() - 1);
		}
		String varDef = "";
		String funcPar = "";
		String hasFuncPar = " ";
		String funcAssign = "";
		String callPar = "";
		String hasCallPar = "";
		String retStatement = "";
		String retNullState = "";
		for( ParameterDec par : md.getMethodSignature().getParameterList() ) {
			varDef +=  "var " + par.getType().getName() + " " + par.getName() + "Var\n";
			funcPar += " " + par.getType().getName() + " " + par.getName() + ",";
			funcAssign += par.getName() + "Var = " + par.getName() + ";\n";
			callPar += " " + par.getName() + "Var,";
		}
		
		if( md.getMethodSignature().getReturnTypeExpr() != null ){
			retStatement = "return ";
		} else {
			retNullState = "return Nil;\n";
		}
		
		if(funcPar.length() > 0){
			funcPar = funcPar.substring(0, funcPar.length() - 1);
			hasFuncPar = ": ";
		}
		if(callPar.length() > 0 ){
			callPar = callPar.substring(0, callPar.length() - 1);
			hasCallPar = ": ";
		}
		
		// Object Name
		String objectName = compiler_ati.getCurrentPrototypeName();
		
		ArrayList<Tuple2<String, StringBuffer>> protoCodeList = new ArrayList<>();
		String prototypeName = NameServer.removeQuotes(objectName + methodName);
		StringBuffer code = new StringBuffer(
		
				"package main\n"
			+	"\n"
			+	"import treplica\n"
			+	"\n"
			+	"object " + prototypeName + " extends Action\n"
			+	"\n"  
			+	  varDef
			+	  "\n"
			+	  "func init" + hasFuncPar + funcPar + " {\n"
			+	    funcAssign
			+	  "}\n"
			+	  "\n"
			+	  "override\n"
			+	  "func executeOn: Context context -> Dyn {\n"    
		    // +     "    type context case " + objectName + " { "      
			+	    "var obj = " + objectName + " cast: context;\n"
			+	    retStatement + "obj " + methodName + "TreplicaAction" + hasCallPar + callPar + ";\n"
			+		retNullState
			+	  "}\n"
			+	"end\n"
				
		);
		String unescape = Lexer.unescapeJavaString(code.toString());
		protoCodeList.add( new Tuple2<String, StringBuffer>( prototypeName, new StringBuffer(unescape)));
		return protoCodeList; 
	}
	

	Program program;
	Vector<MethodDec> visited;

	public void actionNonDeterminism(MethodDec method, String pack, String func) {
		if (methodsND.contains(new Pair<String, String>(pack, func))) {
			this.addError("Found a non-deterministic method call. A message send may call method '" + func +
					"' of prototype '" + pack + "'");
		}
	}

	public boolean visitedContain(MethodDec item) {
		String packItem = item.getDeclaringObject().getCompilationUnit().getNamePublicPrototype();
		String funcItem = item.getNameWithoutParamNumber();

		for (MethodDec method : visited) {
			String packTemp = method.getDeclaringObject().getCompilationUnit().getNamePublicPrototype();
			String funcTemp = method.getNameWithoutParamNumber();

			if (packTemp.equals(packItem) && funcTemp.equals(funcItem)) {
				return true;
			}
		}
		return false;
	}

	public void visitorWalk(String pack, String func) {
		Pair<String, String> methodPair = new Pair<String, String>(pack, func);
		for (MethodDec method : findMethod(methodPair)) {
			if (!visitedContain(method)) {
				actionNonDeterminism(method, pack, func);
				visitorFunctionsTree(method);
			}
		}
	}

	public void visitorFunctionsTree(MethodDec method) {
		String packItem = method.getDeclaringObject().getCompilationUnit().getNamePublicPrototype();
		String funcItem = method.getNameWithoutParamNumber();
		System.out.println("visit: " + packItem + ", " + funcItem);

		visited.addElement(method);
		method.accept(new ASTVisitor() {
			@Override
			public void visit(ExprMessageSendWithSelectorsToExpr node) {
				String pack = node.getReceiverExpr().getType().getName();
				String func = node.getMessage().getMethodName();
				visitorWalk(pack, func);
			}

			@Override
			public void visit(ExprIdentStar node) {
				if (node.getIdentStarKind() == IdentStarKind.unaryMethod_t) {
					String pack = ((CompilationUnit) node.getFirstSymbol().getCompilationUnit())
							.getNamePublicPrototype();
					String func = node.getName();
					visitorWalk(pack, func);
				}
			}

			@Override
			public void visit(ExprMessageSendUnaryChainToExpr node) {
				String pack = node.getReceiver().getType().getName();
				String func = node.getMessageName();
				visitorWalk(pack, func);
			}

			@Override
			public void visit(ExprMessageSendWithSelectorsToSuper node) {
				String pack = node.getSuperobject().getType().getName();
				String func = node.getMessage().getMethodName();
				visitorWalk(pack, func);
			}

			@Override
			public void visit(ExprMessageSendUnaryChainToSuper node) {
				String pack = node.getReceiver().getType().getName();
				String func = node.getMessageName();
				visitorWalk(pack, func);
			}
		});
	}

	public ArrayList<MethodDec> findMethod(Pair<String, String> name) {
		ArrayList<MethodDec> list = new ArrayList<MethodDec>();
		for (CyanPackage pack : program.getPackageList()) {
			if (!pack.getPackageName().equals("cyan.lang")) {
				for (CompilationUnit compilerUnit : pack.getCompilationUnitList()) {
					for (ProgramUnit programUnit : compilerUnit.getProgramUnitList()) {
						Set<ProgramUnit> subProtoList = this.mapProtoSubProto
								.get(pack.getPackageName() + " " + programUnit.getName());
						for (ProgramUnit subProt : subProtoList) {
							ObjectDec dec = (ObjectDec) subProt;
							for (MethodDec method : dec.getMethodDecList()) {
								if (method.getNameWithoutParamNumber().equals(name.getValue())) {
									list.add(method);
								}
							}
						}

						ObjectDec dec = (ObjectDec) programUnit;
						for (MethodDec method : dec.getMethodDecList()) {
							if (method.getNameWithoutParamNumber().equals(name.getValue())) {
								list.add(method);
							}
						}
					}
				}
			}
		}
		return list;
	}

	@Override
	public void dsa2_checkProgramUnit(ICompiler_dsa compiler) {
		program = compiler.getEnv().getProject().getProgram();

		Declaration dec = this.getMetaobjectAnnotation().getDeclaration();
		if (!(dec instanceof MethodDec)) {
			this.addError("Internal error: metaobject '" + getName() + "' should be attached to a method");
			return;
		}
		mapProtoSubProto = compiler.getMapPrototypeSubtypeList();
		MethodDec method = (MethodDec) dec;
		visitorFunctionsTree(method);

	}

	HashMap<String, Set<ProgramUnit>> mapProtoSubProto;

	
}
