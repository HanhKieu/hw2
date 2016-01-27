/* *** This file was created by Hanh Kieu *** */
import java.util.Stack;
import java.util.List;
import java.util.ArrayList;


public class SymbolTable {
	ArrayList<List<String>> myStack = new ArrayList<List<String>>();

	public boolean notInCurrentScope(String token, int currentStacksIndex){
		return !(this.myStack.get(currentStacksIndex).contains(token));
	}
	public boolean notInSpecifiedScope(String token, int levelNumber){
		int specifiedIndex = this.myStack.size() - levelNumber - 1;
		return !(this.myStack.get(specifiedIndex).contains(token));
	}//if it is in the array then return false
	
	public boolean notInGlobalScope(String token){
		for(List<String> myArray : this.myStack){
			if(myArray.contains(token))
				return false;
        }//if any of the arrays contain the tok.string return false, bc it is in global scope
        return true;
	}//not in global scope
	public void addToCurrentScope(String token, int currentStacksIndex){
		this.myStack.get(currentStacksIndex).add(token);
	}

}