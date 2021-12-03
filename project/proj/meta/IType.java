package meta;

import saci.Env;

public interface IType {
	String getName();
	String getFullName();
	boolean isSupertypeOf(IType other, Env env);
}
