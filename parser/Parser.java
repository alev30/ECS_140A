/* *** This file is given as part of the programming assignment. *** */
import java.util.*;
import java.io.*;

public class Parser {


    // tok is global to all these parsing methods;
    // scan just calls the scanner's scan method and saves the result in tok.
    private Token tok; // the current token
    private void scan() {
	tok = scanner.scan();
    }
    boolean print = false;
    ArrayList<ArrayList<String>> table = new ArrayList<ArrayList<String>>();
    ArrayList <String> var_block;
    public boolean declare_check(ArrayList<String> list, String item) {
    	boolean already_there = false;
    	for (int i = 0; i < list.size(); i++) {
    		if (list.contains(item)) {
    			System.out.println("redeclaration of variable " + item);
    			already_there = true;
    			break;
    		}
    	}
    	return already_there;
    }
    // any other reference check
    public void reference_check(ArrayList<ArrayList<String>> list,int index, String item, String name) {
		
    	boolean already_there= false;
    	for (int i = 0; i < list.size(); i++) {
    		if(list.get(i).contains(item)) {
    			already_there = true;
    		}
    	}
    	
    	if (!already_there) {
			System.out.println("no such variable " + name + " on line " + tok.lineNumber);
			System.exit(1);
    	}

    }
    // assignment check
    public void undeclared(ArrayList<ArrayList<String>> list, int index, String name) {
    	boolean already_there= false;
    	for (int i = 0; i< list.size() ; i++) {
    		if(list.get(i).contains(name)) {
    			already_there = true;
    		}
    	}
    	if (!already_there) {
    		System.out.println(name + " is an undeclared variable on line " + tok.lineNumber);
			System.exit(1);
    	}
    }
    
    
    // helper printer
    public void printer(ArrayList<ArrayList<String>> table) {
    	for (int i = 0; i < table.size(); i ++) {
    		for (int j = 0; j < table.get(i).size(); j++) {
    			System.out.println(table.get(i).get(j));
    		}
    	}
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
    	System.out.println("public class My_e2j{");
    	System.out.println("public static void main(String[] args){");
    	block();
    	System.out.println("}");
    	System.out.println("}");
    }

    private void block(){
    	var_block  = new ArrayList<String>();
		declaration_list();
		table.add(var_block);
		statement_list();
		table.remove(table.size()-1);
    }

    private void declaration_list() {
		// below checks whether tok is in first set of declaration.
		// here, that's easy since there's only one token kind in the set.
		// in other places, though, there might be more.
		// so, you might want to write a general function to handle that.
		while( is(TK.DECLARE) ) {
		    declaration();
		}
    }

    private void declaration() {
    	String output = "";
    	String temp = "";
    	output+= "int ";
		mustbe(TK.DECLARE);
		output+= tok.string;
		String current = tok.string;
		mustbe(TK.ID);
		if (!declare_check(var_block,current)) {
			var_block.add(current);
		}
		while( is(TK.COMMA) ) {
			temp = "";
			temp += tok.string;
		    scan();
		    temp += tok.string;
		    current = tok.string;
		    mustbe(TK.ID);
			if (!declare_check(var_block,current)) {
				var_block.add(current);
				output+= temp;
			}
		}
		System.out.println(output+ ";");
    }

    private void statement_list() {
    	while(is(TK.IF) || is(TK.PRINT) || is(TK.DO) || is (TK.TILDE) || is(TK.ID)) {
    		statement();
    	}
    //	if (is(TK.DECLARE)) {
    //		declaration_list();
    //	}
    }
    
    private void statement() {
    	if (is (TK.IF)) {
    		ifif();
    	}
    	else if (is (TK.PRINT)) {
    		print();
    	}
    	else if (is (TK.DO)) {
    		dodo();
    	}
    	else if (is(TK.TILDE)|| is(TK.ID)) {
    		assignment();
    	}
    	//else {
    	//	System.exit(1);
    	//}
    	
    }
 
    
    private void print() {
    	mustbe(TK.PRINT);
    	System.out.println("System.out.println( ");
    	print = true;
    	expr(false);
    	System.out.println(");");
    }
    
    private void assignment() {
    	ref_id(true);
    	mustbe(TK.ASSIGN);
    	System.out.println("=");
    	expr(false);
    	System.out.println(";");
    }

    private void ref_id(boolean assignment) {
    	int scope = 0;
    	String check;
    	String name = "";
    	if(is(TK.TILDE)) {
    		mustbe(TK.TILDE);
    		name  = name + "~";
    		if(is(TK.NUM)) {
    			//scope
    			scope = Integer.parseInt(tok.string);
    			mustbe(TK.NUM);
    			name  = name + tok.string;
    		}
    		else {
    			// global variable
    			scope = 0; // global index located at highest block (0)
    		}
    	}
    	else {
    		scope = 0;
    	}
    	
    	name += tok.string;
    	check = tok.string;
    	if (assignment || print == true) {
    		undeclared(table,scope, check);
    	}
    	else {
        	reference_check(table,scope, check, name);
    	}
    	String ID = tok.string;
    	mustbe(TK.ID);
    	System.out.println(ID);
    	print = false;
    }

    public void dodo() {
    	mustbe(TK.DO);
    	System.out.println("Do( ");
    	guarded_command(false);
    	System.out.println(";");
    	mustbe(TK.ENDDO);
    	System.out.println("}");
    }
    
    public void ifif() {
    	mustbe(TK.IF);
    	System.out.println("if ( ");
    	guarded_command(true);
    	System.out.println("}");
    	while (is (TK.ELSEIF)) {
    		System.out.println("else if (");
    		scan();
    		guarded_command(true);
    		System.out.println("}");
    	}
    	if (is (TK.ELSE)) {
    		System.out.println("else {");
    		scan();
    		block();
    		System.out.println("}");
    	}
    	mustbe(TK.ENDIF);
    	
    }
    
    public void guarded_command(boolean ifif) {
    	
    	expr(ifif);
    	System.out.println(") {");
    	mustbe(TK.THEN);
    	block();
    }
    
    private void expr(boolean ifif) {
    	
    	term(ifif);
    	while (is (TK.MINUS) || is(TK.PLUS)) {
    		addop();
    		term(false);
    	}
    }
    
    private void term(boolean ifif) {
    	factor(ifif);
    	while (is(TK.TIMES) || is(TK.DIVIDE)) {
    		multop();
    		factor(false);
    	}
    }
    
    private void factor(boolean ifif) {
    	int num;
    	if (is(TK.LPAREN)) {
    		System.out.println("(");
    		scan();
    		expr(false);
    		mustbe(TK.RPAREN);
    		System.out.println(")");
    	}
    	else if(is (TK.NUM)) {
    		num = Integer.parseInt(tok.string);
    		if (ifif) {
    			if (num >= 0) {
    				System.out.println("false");
    			}
    			else {
    				System.out.println("true");
    			}
    		}
    		else {
    			System.out.println(num);
    		}
    		scan();
    	}
    	else {
    		ref_id(false);
    	}
    	
    }
    
    private void addop() {
    	if (is(TK.PLUS)) {
    		mustbe(TK.PLUS);
    		System.out.println("+");
    	}
    	else {
    		mustbe(TK.MINUS);
    		System.out.println("-");
    	}
    }
    
    private void multop() {
    	if (is (TK.TIMES)) {
    		mustbe(TK.TIMES);
    		System.out.println("*");
    	}
    	else {
    		mustbe(TK.DIVIDE);
    		System.out.println("/");
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

    private void parse_error(String msg) {
	System.err.println( "can't parse: line "
			    + tok.lineNumber + " " + msg );
	System.exit(1);
    }
}
