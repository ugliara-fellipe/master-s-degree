package meta;

public enum DeclarationKind { 
	EXPR("expression"), INSTANCE_VARIABLE_DEC("instance variable"), 
	LOCAL_VAR_DEC("local variable"), METHOD_DEC("method"), METHOD_SIGNATURE_DEC("method signature"),
	NONE_DEC("none"), PACKAGE_DEC("package"), PROGRAM_DEC("program"), PROTOTYPE_DEC("prototype"), STATEMENT_DEC("statement");
	
	private String name;
	DeclarationKind(String name) {
		this.name = name;
	}
	@Override
	public String toString() {
		return name;
	}
}

