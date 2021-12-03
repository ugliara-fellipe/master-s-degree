package saci;


/**
 * kinds of directories associated to packages
   @author jose
 */
public enum DirectoryPackage {

	/*
	 * data directory that keeps information that is read-only by metaobjects and by the compiler
	 */
	DATA(NameServer.directoryNamePackageData, true, DirectoryWhere.PACKAGE), 
	/*
	 * test directory in which metaobjects and the compiler can add prototypes for testing.
	 * It can be written by metaobjects and by the compiler
	 */
	TEST(NameServer.directoryNamePackageTests, false, DirectoryWhere.PROJECT),
	/*
	 * Domain Specific Language (DSL) directory containing source code of DSLs that
	 * can be compiled by metaobjects. It is read-only by metaobjects and by the compiler.
	 */
	DSL(NameServer.directoryNamePackageDSL, true, DirectoryWhere.PACKAGE),
	/*
	 * directory in which the files for link past-future can be kept
	 */
	LPF(NameServer.directoryNameLinkPastFuture, false, DirectoryWhere.PACKAGE),
	/*
	 * directory in which temporary files can be written and read by metaobjects. 
	 */
	TMP(NameServer.directoryNamePackagePrototypeTmp, false, DirectoryWhere.PROTOTYPE);
	
	@Override public String toString() {
		return dirName;
	}

	DirectoryPackage(String dirName, boolean readOnly, DirectoryWhere where) {
		this.dirName = dirName;
		this.readOnly = readOnly;
		this.where = where;
	}
	
	public boolean isReadOnly() {
		return readOnly;
	}

	public DirectoryWhere getWhere() {
		return where;
	}


	private String dirName;
	private boolean readOnly;
	private DirectoryWhere where;
	
}
