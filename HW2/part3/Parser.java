/* *** This file is given as part of the programming assignment. *** */
import java.util.List;
import java.util.ArrayList;

public class Parser {

    // tok is global to all these parsing methods;
    // scan just calls the scanner's scan method and saves the result in tok.
    private Token tok; // the current token
    private SymbolTable myTable = new SymbolTable();
    List<String> scope = new ArrayList<String>();
    int counter = 0;
    
    private void scan() {
	   tok = scanner.scan();
    }

    private Scan scanner;

    Parser(Scan scanner) {
    	this.scanner = scanner;
    	scan();
    	program();

    	if( tok.kind != TK.EOF )
    	    parse_error("junk after logical end of program");
    }

    private void program() {
	   block();
    }

    private void block(){
        if(counter != 0 && (!scope.isEmpty()))
        {
            myTable.myStack.push(scope);
            scope.clear();
            counter++;
        }

    	declaration_list();
    	statement_list();

        if(!myTable.myStack.empty()){
            System.out.println(myTable.myStack.pop() + "boobs");
        }
    }

    private void declaration_list() {  
		while( is(TK.DECLARE) ) {
		    declaration();
		}
    }

    private void declaration() {
		mustbe(TK.DECLARE);
        if(is(TK.ID)){
            if(notDeclared(tok.string))
                scope.add(tok.string);
            else{
                redeclaration_error(tok.string);
            }
        }
		mustbe(TK.ID);

		while( is(TK.COMMA) ) {
		    scan();
            if(is(TK.ID)){
                if(notDeclared(tok.string))
                    scope.add(tok.string);
                else{
                    redeclaration_error(tok.string);
                }
            }
		    mustbe(TK.ID);
		}
    }

    private void statement(){
    	if(is(TK.TILDE) || is(TK.ID))
    		assignment();
    	else if(is(TK.PRINT))
    		print();
    	else if(is(TK.DO)){
    		doo();
    	}
    	else if(is(TK.IF))
    		iff();
    }

    private void statement_list() {
    	while( is(TK.TILDE) || is(TK.ID) || is(TK.PRINT) || is(TK.DO) || is(TK.IF)){
    		statement();
    	}
    }

    private void print(){
    	mustbe(TK.PRINT);
    	expr();
    }

    private void assignment(){
    	ref_id();
    	mustbe(TK.ASSIGN);
    	expr();
    }

    private void ref_id(){ 
    	if(is(TK.TILDE)){
    		mustbe(TK.TILDE);
    		if(is(TK.NUM))
    			mustbe(TK.NUM);
    	}

        // if(notDeclared(tok.string))
        //     declaration_error(tok.string,tok.lineNumber);
    	mustbe(TK.ID);
    }

    private void doo(){
    	mustbe(TK.DO);
    	guarded_command();
    	mustbe(TK.ENDDO);
    }

    private void iff(){
    	mustbe(TK.IF);
    	guarded_command();
    	while(is(TK.ELSEIF)) {
    		scan();
    		guarded_command();
    	}
    	if(is(TK.ELSE)){
    		mustbe(TK.ELSE);
    		block();
    	}
    	mustbe(TK.ENDIF);
    }

    private void guarded_command(){   
    	expr();
    	mustbe(TK.THEN);
    	block();
    }

    private void expr(){ 
    	term();
    	while( is(TK.PLUS) || is(TK.MINUS) ) {
	    	scan();
	    	term();
		}
    }

    private void term(){  
    	factor();
    	while(is(TK.TIMES) || is(TK.DIVIDE)){
    		scan();
    		factor();
    	}
    }

    private void factor(){    
    	if(is(TK.LPAREN)){
    		mustbe(TK.LPAREN);
    		expr();
    		mustbe(TK.RPAREN);
    	}
    	else if(is(TK.TILDE) || is(TK.ID)){
    		ref_id();
    	}
    	else if(is(TK.NUM))
    	{
    		mustbe(TK.NUM);
    	}
    }

    // is current token what we want?
    private boolean is(TK tk) {
        return tk == tok.kind;
    }

    // ensure current token is tk and skip over it.
    private void mustbe(TK tk) {
	if( tok.kind != tk ) {
	    System.err.println( "mustbe: want " + tk + ", got " +
				    tok);
	    parse_error( "missing token (mustbe)" );
	}
	scan();
    }

    /*
    //If the id has not been declared yet return true
    //else return false
    */
    private boolean notDeclared(String string){
        if(!scope.contains(string) && (myTable.myStack.search(string) == -1))
            return true;
        else{
            return false;
        }
    }

    /*
    //Takes two arguments string and linenumber
    //string is the id that has not been declared yet, but is being used
    //linenumber is the line in which it occured
    */
    private void declaration_error(String string, int lineNumber){
        System.err.println(string + " is an undeclared variable on line " + lineNumber);
        System.exit(1);
    }

    /*
    //Takes two arguments string and linenumber
    //string is the id that is being redeclared
    //linenumber is the line in which it occured
    */
    private void redeclaration_error(String string){
        System.err.println("redeclaration of variable " + string);
    }

    private void parse_error(String msg) {
	System.err.println( "can't parse: line "
			    + tok.lineNumber + " " + msg );
	System.exit(1);
    }
}
