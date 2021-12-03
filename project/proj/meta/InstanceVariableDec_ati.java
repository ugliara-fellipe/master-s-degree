package meta;

import ast.InstanceVariableDec;

public class InstanceVariableDec_ati implements IInstanceVariableDec_ati {

	public InstanceVariableDec_ati(InstanceVariableDec instVarDec) {
		this.instVarDec = instVarDec;
	}
	
	@Override
	public String getName() {
		return instVarDec.getName();
	}

	@Override
	public IType getIType() {
		return new Type_ati(instVarDec.getType());
	}

	private InstanceVariableDec instVarDec;
}
