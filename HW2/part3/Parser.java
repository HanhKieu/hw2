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
        //System.out.println("(block) 	 " + tok.string );
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
    }//COMPLETED

    private void declaration_list() {
        //System.out.println("(declaration_list) 	 " + tok.string );
	// below checks whether tok is in first set of declaration.
	// here, that's easy since there's only one token kind in the set.
	// in other places, though, there might be more.
	// so, you might want to write a general function to handle that.
		while( is(TK.DECLARE) ) {
		    declaration();
		}
    }//completed

    private void declaration() {
        //System.out.println("(declaration) 	 " + tok.string );
		mustbe(TK.DECLARE);
        if(is(TK.ID)){
            if(notDeclared(tok.string))
                scope.add(tok.string);
            else{
                redeclaration_error(tok.string,tok.lineNumber);
            }
        }
		mustbe(TK.ID);

		while( is(TK.COMMA) ) {
		    scan();
            if(is(TK.ID)){
                if(notDeclared(tok.string))
                    scope.add(tok.string);
                else{
                    redeclaration_error(tok.string,tok.lineNumber);
                }
            }
		    mustbe(TK.ID);
		}
    }//COMPLETED

    private void statement(){
        //System.out.println("(statement) 	 " + tok.string );
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
        //System.out.println("(statement_list) 	 " + tok.string );
    	while( is(TK.TILDE) || is(TK.ID) || is(TK.PRINT) || is(TK.DO) || is(TK.IF)){
    		statement();
    	}
    }//COMPLETED

    private void print(){
        //System.out.println("(print) 	 " + tok.string );
    	mustbe(TK.PRINT);
    	expr();
    }//COMPLETED

    private void assignment(){
        //System.out.println("(assignment) 	 " + tok.string );
    	ref_id();
    	mustbe(TK.ASSIGN);
    	expr();
    }//COMPLETED

    private void ref_id(){
        //System.out.println("(ref_id) 	 " + tok.string );
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
        //System.out.println("(doo) 	 " + tok.string );
    	mustbe(TK.DO);
    	guarded_command();
    	mustbe(TK.ENDDO);
    }//COMPLETED

    private void iff(){
        //System.out.println("(iff) 	 " + tok.string );
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
        //System.out.println("(guarded_command) 	 " + tok.string );
    	expr();
    	mustbe(TK.THEN);
    	block();
    }//COMPLETED

    private void expr(){
        //System.out.println("(expr) 	 " + tok.string );
    	term();
    	while( is(TK.PLUS) || is(TK.MINUS) ) {
	    	scan();
	    	term();
		}
    }//COMPLETED

    private void term(){
        //System.out.println("(term) 	 " + tok.string );
    	factor();
    	while(is(TK.TIMES) || is(TK.DIVIDE)){
    		scan();
    		factor();
    	}
    }//COMPLETED

    private void factor(){
        //System.out.println("(factor) 	 " + tok.string );
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
    private void redeclaration_error(String string, int lineNumber){
        System.err.println(string + " is a redeclared variable on line " + lineNumber);
        System.exit(1);
    }

    private void parse_error(String msg) {
	System.err.println( "can't parse: line "
			    + tok.lineNumber + " " + msg );
	System.exit(1);
    }
}
