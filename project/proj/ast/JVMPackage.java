package ast;

import java.util.HashMap;
import java.util.Map;

public class JVMPackage implements ASTNode {


	public JVMPackage(String packageName, Package jvmPackage) {
		this.packageName = packageName;
		this.jvmPackage = jvmPackage;
		jvmTypeClassMap = new HashMap<>();
	}
	
	@Override
	public void accept(ASTVisitor visitor) {

		visitor.preVisit(this);
		visitor.visit(this);
	}	

	
	public Package getJvmPackage() {
		return jvmPackage;
	}


	public String getPackageName() {
		return packageName;
	}


	public void addJVMClass(TypeJavaClass javaClass) {
		jvmTypeClassMap.put(javaClass.getName(),  javaClass);
	}
	
	public TypeJavaRef searchJVMClass(String name) {
		return jvmTypeClassMap.get(name);
	}
	
	
	private String packageName;
	private Package jvmPackage;

	private Map<String, TypeJavaRef> jvmTypeClassMap;

}
