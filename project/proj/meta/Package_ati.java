package meta;

import ast.CyanPackage;

public class Package_ati implements IPackage_ati {
	
	public Package_ati(CyanPackage cyanPackage) { 
		this.cyanPackage = cyanPackage;
	}
	
	private CyanPackage cyanPackage;
}
