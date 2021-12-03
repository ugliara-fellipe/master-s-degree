package ast;

import saci.Env;

/**
 * metaobjects that are attached to a program unit or a slot (method, instance 
 * variable, shared variable) and that should do some check after calculating the 
 * interface types should implement this interface. 
   @author jose
 */
public interface ICheck_cin {
	void check(Env env);
}
