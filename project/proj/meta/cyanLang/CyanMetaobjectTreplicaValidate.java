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
import ast.Program;
import ast.ProgramUnit;
import javafx.util.Pair;
import meta.CyanMetaobjectWithAt;
import meta.DeclarationKind;
import meta.ICheckProgramUnit_dsa2;
import meta.ICompiler_dsa;

public class CyanMetaobjectTreplicaValidate extends CyanMetaobjectWithAt implements ICheckProgramUnit_dsa2 {

	HashSet<Pair<String, String>> methodsND;

	public CyanMetaobjectTreplicaValidate() {
		super(MetaobjectArgumentKind.ZeroParameter);
		visited = new Vector<MethodDec>();

		methodsND = new HashSet<>();
		methodsND.add(new Pair<String, String>("Program", "behavior"));
	}

	Program program;
	Vector<MethodDec> visited;

	public void actionNonDeterminism(MethodDec method, String pack, String func) {
		if (methodsND.contains(new Pair<String, String>(pack, func))) {
			this.addError("action: " + pack + ", " + func);
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
		System.out.println();
		MethodDec method = (MethodDec) dec;
		visitorFunctionsTree(method);

		System.out.println();
	}

	HashMap<String, Set<ProgramUnit>> mapProtoSubProto;

	@Override
	public String getName() {
		return "treplicaValidate";
	}

	@Override
	public DeclarationKind[] mayBeAttachedList() {
		return decKindList;
	}

	private static DeclarationKind[] decKindList = new DeclarationKind[] { DeclarationKind.METHOD_DEC };
}
