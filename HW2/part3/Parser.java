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


    //myTable.myStack.push();
    
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
        if(counter != 0)
            myTable.myStack.push(scope);
        scope.clear();
        counter++;

    	declaration_list();
    	statement_list();
        if(!myTable.myStack.empty())
            myTable.myStack.pop();
    }//COMPLETED

    private void declaration_list() {
	// below checks whether tok is in first set of declaration.
	// here, that's easy since there's only one token kind in the set.
	// in other places, though, there might be more.
	// so, you might want to write a general function to handle that.
		while( is(TK.DECLARE) ) {
		    declaration();
		}
    }//completed

    private void declaration() {
		mustbe(TK.DECLARE);
        if(is(TK.ID))
            scope.add(tok.string);
		mustbe(TK.ID);

		while( is(TK.COMMA) ) {
		    scan();
            if(is(TK.ID))
                scope.add(tok.string);
		    mustbe(TK.ID);
		}
    }//COMPLETED

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
    }//COMPLETED

    private void statement_list() {
    	while( is(TK.TILDE) || is(TK.ID) || is(TK.PRINT) || is(TK.DO) || is(TK.IF)){
    		statement();
    	}
    }//COMPLETED

    private void print(){
    	mustbe(TK.PRINT);
    	expr();
    }//COMPLETED

    private void assignment(){
    	ref_id();
    	mustbe(TK.ASSIGN);
    	expr();
    }//COMPLETED

    private void ref_id(){
    	if(is(TK.TILDE)){
    		mustbe(TK.TILDE);
    		if(is(TK.NUM))
    			mustbe(TK.NUM);
    	}
        if(notDeclared(tok.string))
            declaration_error(tok.string,tok.lineNumber);
    	mustbe(TK.ID);
    }//COMPLETED

    private void doo(){
    	mustbe(TK.DO);
    	guarded_command();
    	mustbe(TK.ENDDO);
    }//COMPLETED

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
    }//COMPLETED

    private void guarded_command(){
    	expr();
    	mustbe(TK.THEN);
    	block();
    }//COMPLETED

    private void expr(){
    	term();
    	while( is(TK.PLUS) || is(TK.MINUS) ) {
	    	scan();
	    	term();
		}

    }//COMPLETED

    private void term(){
    	factor();
    	while(is(TK.TIMES) || is(TK.DIVIDE)){
    		scan();
    		factor();
    	}
    }//COMPLETED

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

    }//COMPLETED

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

    private boolean notDeclared(String string){
        if(!scope.contains(string) && (myTable.myStack.search(string) == -1))
            return true;
        else{
            return false;
        }
    }

    private void declaration_error(String string, int lineNumber){
        System.err.println(string + " is an undeclared variable on line " + lineNumber);
        System.exit(1);
    }

    private void parse_error(String msg) {
	System.err.println( "can't parse: line "
			    + tok.lineNumber + " " + msg );
	System.exit(1);
    }
}
