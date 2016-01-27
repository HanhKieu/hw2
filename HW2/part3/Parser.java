/* *** This file is given as part of the programming assignment. *** */
import java.util.List;
import java.util.ArrayList;

public class Parser {


    // tok is global to all these parsing methods;
    // scan just calls the scanner's scan method and saves the result in tok.
    private Token tok; // the current token
    private SymbolTable myTable = new SymbolTable(); //our Symbol Table
    int stackIndex = -1;
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

    //Keep in./ru mind that I use the term List to refer to the List of arrays.
    private void block(){
        stackIndex++;
        myTable.myStack.add(new ArrayList<String>());//when you enter a new block, push new array to List.
        declaration_list();
        statement_list();
        if(stackIndex != -1){
            myTable.myStack.remove(stackIndex);
            stackIndex--;//removes the last entry in our list
        }//if there is still something in our stack, remove it.
    }

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
        if(is(TK.ID)){
            if(myTable.notInCurrentScope(tok.string,stackIndex))
                myTable.addToCurrentScope(tok.string,stackIndex);
            else{
                redeclaration_error();
            }
        }//if an ID follows a declaration 
        //add it to the scope if it hasn't been declared
        //else have a redeclaration error because it has alreadya been declared
        mustbe(TK.ID);
        while( is(TK.COMMA) ) {
            scan();
            if(is(TK.ID)){
                if(myTable.notInCurrentScope(tok.string,stackIndex))
                    myTable.addToCurrentScope(tok.string,stackIndex);
                else{
                redeclaration_error();
                }
            }//if an ID follows a declaration 
            //add it to the scope if it hasn't been declared
            //else have a redeclaration error because it has alreadya been declared
            mustbe(TK.ID);
        }
    }//declares variable that hasn't been already declared

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
        boolean tildeExists = false;
        boolean numExists = false;
        int levelNumber = -1;

        if(is(TK.TILDE)){
            tildeExists = true;
            mustbe(TK.TILDE);
            if(is(TK.NUM))
            {   numExists = true;
                levelNumber = Integer.parseInt(tok.string);
                mustbe(TK.NUM);
            }//IF IT IS TILDA FOLLOWED BY A NUMBER AS SUCH ~2
        }//if contains TILDA in front as such ~

        if(tildeExists && is(TK.ID) && (levelNumber == -1) ){
            if(myTable.notInCurrentScope(tok.string, 0))
                scoping_error();
            //notIncurrentScope can also get specific scope level by passing in index in second paremter.
        }//if it follows form ~a

        else if(tildeExists && numExists && is(TK.ID)){
            if(levelNumber >= myTable.myStack.size())
                scoping_error(levelNumber);
            else if(myTable.notInSpecifiedScope(tok.string,levelNumber))
                scoping_error(levelNumber);
        }//if follows the form ~2a

        else if(tildeExists && is(TK.ID) && levelNumber == 0){
            if(myTable.notInCurrentScope(tok.string,stackIndex))
                scoping_error();
        }//if it just follows the form ~0a
        
        if(myTable.notInGlobalScope(tok.string))
            declaration_error();
        //if you're trying to use a variable before you declare it, then throw an error

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

    private void parse_error(String msg) {
        System.err.println( "can't parse: line "
                    + tok.lineNumber + " " + msg );
        System.exit(1);
    }

    private void declaration_error(){
        System.err.println(tok.string + " is an undeclared variable on line " + tok.lineNumber);
        System.exit(1);
    }

    private void redeclaration_error(){
        System.err.println("redeclaration of variable " + tok.string);
    }

    private void scoping_error(){
        System.err.println("no such variable " + "~"+ tok.string + " on line " + tok.lineNumber);
        System.exit(1);
    }
    private void scoping_error(int levelNumber){
        System.err.println("no such variable " + "~" + levelNumber + tok.string + " on line " + tok.lineNumber);
        System.exit(1);
    }//if it has a level number overloaded function

}