package meta;

import ast.InstanceVariableDec;

public class InstanceVariableDec_dsa implements IInstanceVariableDec_dsa {

	public InstanceVariableDec_dsa(InstanceVariableDec instVarDec) {
		this.instVarDec = instVarDec;
	}
	
	@Override
	public String getName() {
		return instVarDec.getName();
	}

	@Override
	public IType getType() {
		return new Type_ati(instVarDec.getType());
	}

	private InstanceVariableDec instVarDec;
}
