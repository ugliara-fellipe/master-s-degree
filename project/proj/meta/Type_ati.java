package meta;

import ast.Type;
import saci.Env;

public class Type_ati implements IType {

	public Type_ati(Type type) {
		this.type = type;
	}
	@Override
	public String getName() {
		return type.getName();
	}

	@Override
	public boolean isSupertypeOf(IType other, Env env) {
		if ( other instanceof Type_ati ) {
			return type.isSupertypeOf(((Type_ati) other).type, env);
		}
		else
			return false;
	}
	
	@Override
	public String getFullName() {
		return type.getFullName();
	}
	
	private Type type;


}
