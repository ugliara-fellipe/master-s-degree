package meta;

public interface ICompiler_dpp extends IAbstractCyanCompiler {
	void setProjectVariable(String variableName, String value);
	void setProjectVariable(String variableName, int value);
	void setProjectVariable(String variableName, boolean value);
	/**
	 * add 'value' to the list of values associated to 'variableName'
	   @param variableName
	   @param value
	 */
	void addToProjectSet(String variableName, String value);
}
