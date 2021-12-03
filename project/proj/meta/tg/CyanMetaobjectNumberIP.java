package meta.tg;

import meta.CyanMetaobjectNumber;
import meta.ICompilerAction_dpa;
import meta.ICompiler_dsa;

public class CyanMetaobjectNumberIP extends CyanMetaobjectNumber {
	
	public CyanMetaobjectNumberIP(){
		super();
	}

	@Override
	public String[] getSuffixNames() {
		return new String[] { "IP", "Ip", "ip" };
	}



	@Override
	public void dpa_parse(ICompilerAction_dpa compilerAction, String code){
		String currentNumber = new String();
		String completeIP = new String();
		code = code.substring(0, code.length()-2);
		int n = code.length();
		int underscoreCount = 0;
		
		for(int i = 0; i < n; i++){
			char c = code.charAt(i);
			
			if(!Character.isDigit(c)){
				if(c != '_'){
					addError("Malformed expression: IP metaobject must contain only numbers and underscores");
					return ;
				}
				else{
					underscoreCount++;
					int aux = Integer.parseInt(currentNumber);
					if(aux < 0 || aux > 255){
						addError("Invalid IP address: each number must be in the range 0..255");
						return ;
					}
					else{
						completeIP += currentNumber;
						if(underscoreCount <= 3) completeIP += ".";
						currentNumber = "";
					}
				}
 			}else{
 				currentNumber += c;
 			}

 			if(underscoreCount > 4){
 				addError("Invalid IP address: address must contain exactly four numbers");
 				return ;
 			}
		}

		setInfo(new StringBuffer("IPAddress( \"" + completeIP + "\")"));
	}

	@Override
	public StringBuffer dsa_codeToAdd(ICompiler_dsa compiler_dsa) {
		return (StringBuffer ) getInfo();
	}
	
	@Override
	public String getPackageOfType() {
		return "tg";
	}

	@Override
	public String getPrototypeOfType() {
		return "IPAddress";
	}
}