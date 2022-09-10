package edu.ufl.cise.plpfa22;

import java.io.IOException;
import java.io.StringReader;
import java.util.Arrays;
import java.util.List;
import edu.ufl.cise.plpfa22.Token;
import static edu.ufl.cise.plpfa22.Token.Kind;

public class Lexer implements ILexer {

	int currLine = 1;
	int currColumn = 0;
	String text;
	StringReader r;
	int ch;
	List <String> keyWords = Arrays.asList("CONST","VAR", "PROCEDURE", "CALL", "BEGIN", "END", "IF", "THEN", "WHILE", "DO");
	List <String> Bools = Arrays.asList("TRUE", "FALSE");

	public Lexer(String text) {
		this.text = text;
		this.r = new StringReader(text);
	}

	public enum State {
		START,
		IN_IDENT,
		HAVE_ZERO,
		HAVE_DOT,
		IN_FLOAT,
		IN_NUM,
		HAVE_EQ,
		HAVE_MINUS, 
		IN_STRINGLIT,
		IN_COMMENT
	}

	void updateLocation() throws IOException {
		System.out.println("Char inside ul: "+(char)ch);
		if (ch == '\n') {
			currLine = currLine + 1;
			ch = r.read();
			currColumn = 1;

		} else if (ch == '\r') {
			currLine++;
			ch = r.read();
			if (ch == '\n') {
				ch = r.read();
			}
			currColumn = 1;
		} else {
			ch = r.read();
			currColumn++;
		}
	}

	@Override
	public IToken next() throws LexicalException {
		int line = 0;
		int column = 0;
		Token t = null;
		StringBuilder st = null;
		State state = State.START;
		while (t == null) {
			System.out.println("State: "+state);
			System.out.println("Character "+(char)ch);
			try {
				switch (state) {
					case START: {
						while (Character.isWhitespace(ch)) {
							updateLocation();
						}
						column = currColumn;
						line = currLine;
						switch (ch) {
							case 0: {
								updateLocation();
							}
								break;
							case -1: {
								t = new Token(Kind.EOF, "EOF", line, column);
							}
								break;
							case 43: {

								t = new Token(Kind.PLUS, "+", line, column);
								updateLocation();

							}
								break;
							case 45: {
								t = new Token(Kind.MINUS, "-", line, column);
								updateLocation();
							}
								break;
							case 42:{
								t = new Token(Kind.TIMES,"*",line,column);
								updateLocation();
							}
							break;

							case 34: {
								state = State.IN_STRINGLIT;
								st = new StringBuilder();
								// st.append((char) 34);
								updateLocation();
							}
							break;
							case 47: {
								state = State.IN_COMMENT;
								updateLocation();
							}
							break;
							default:
							{
								// if (ch>=40 && ch<=90 || ch>=97 && ch<=122 || ch==95 || ch==36)
								//Changing this if condition here to check is it works
								if (Character.isJavaIdentifierStart(ch)){
									System.out.println((char)ch);
									state = State.IN_IDENT;
									st = new StringBuilder();
									st.append((char)ch);
									//commenting stuff below this to test alternate way line 124,127,129
									// st = new StringBuilder();
									//Adding while condition here to check for test6
									// while (Character.isJavaIdentifierPart(ch)){
										// st.append((char)ch);									}
									// st.append((char)ch);
									updateLocation();
								}
								else if(Character.isDigit(ch)){
									state = State.IN_NUM;
									st = new StringBuilder();
									st.append((char)ch);
									updateLocation();
								}
								else{
									throw new LexicalException("Invalid character for ident");
								}
							}
							break;
						}
					
					}
					break;
					case IN_STRINGLIT: {
						System.out.println("Current char:"+(char)ch);
						System.out.println("In case string lit");
						if (ch<0 || ch>127) {
							throw new LexicalException("Invalid character found in string literal");

						}
						else{
							if(ch == 34){
								// st.append((char)34);
								// String str = st.toString();
								t = new Token(Kind.STRING_LIT, st.toString(),line,column);
								state = State.START;
								updateLocation();
							}
							else if(ch == 92){
								updateLocation();
								switch(ch) {
									case 'b':{
										st.append("\b");
										updateLocation();
										break;
									}
									case 't': {
										st.append("\t");
										updateLocation();
										break;
									}
									case 'n': {
										st.append("\n");
										updateLocation();
										break;
									}
									case 'f': {
										st.append("\f");
										updateLocation();
										break;
									}
									case 'r': {
										st.append("\r");
										updateLocation();
										break;
									}
									case 34: {
										st.append("\"");
										updateLocation();
										break;
									}
									case 39: {
										st.append("\'");
										updateLocation();
										break;
									}
									case 92: {
										st.append("\'");
										updateLocation();
										break;
									}


									default: {
										throw new LexicalException("Could not match escape character after backslash");
									}
									

								}
							}
							else{
								st.append((char)ch);
								updateLocation();
							}
						}
						
					}
					break;
					case IN_COMMENT:{
						if (ch=='\n'||ch=='\r'||ch==-1){
							state = State.START;
							updateLocation();
						}
						else{
							updateLocation();
						}
					}
					break;
					case IN_IDENT:{

						if (Character.isJavaIdentifierPart(ch)){
							st.append((char)ch);
							updateLocation();
						}
						else if(ch == '\n'){
							t = new Token(Kind.IDENT, st.toString(), line, column);
							state = State.START;
							updateLocation();
						}
						else if(ch == 32){
							String txt;
							txt = st.toString();
							t = new Token(Kind.IDENT,txt,line,column);
							state = State.START;
							updateLocation();
						}
						else{
							String txt;
							txt=st.toString();
							if(keyWords.contains(txt) || Bools.contains(txt)){
								switch (txt){
									case "CONST": {
										t=new Token(Kind.KW_CONST,txt,line,column);
										state = State.START;
									}
									break;
									case "VAR":{
										t=new Token(Kind.KW_VAR,txt,line,column);
										state = State.START;
									}
									break;
									case "PROCEDURE":{
										t=new Token(Kind.KW_PROCEDURE,txt,line,column);
										state = State.START;
									}
									break;
									case "CALL":{
										t=new Token(Kind.KW_CALL,txt,line,column);
										state = State.START;
									}
									break;
									case "BEGIN":{
										t=new Token(Kind.KW_BEGIN,txt,line,column);
										state = State.START;
									}
									break;
									case "END":{
										t=new Token(Kind.KW_END,txt,line,column);
										state = State.START;
									}
									break;
									case "IF":{
										t=new Token(Kind.KW_IF,txt,line,column);
										state = State.START;
									}
									break;
									case "THEN":{
										t=new Token(Kind.KW_THEN,txt,line,column);
										state = State.START;
									}
									break;
									case "WHILE":{
										t=new Token(Kind.KW_WHILE,txt,line,column);
										state = State.START;
									}
									break;
									case "DO":{
										t=new Token(Kind.KW_DO,txt,line,column);
										state = State.START;
									}
									break;
									case "TRUE":{
										t=new Token(Kind.BOOLEAN_LIT,txt,line,column);
										state = State.START;
									}
									break;
									case "FALSE":{
										t=new Token(Kind.BOOLEAN_LIT,txt,line,column);
										state = State.START;
									}
									break;
									default:{
										t=new Token(Kind.IDENT,txt,line,column);
										state = State.START;
									}
									break;
								}
							}
							else{
								throw new LexicalException("Invalid character for Identifier");
							}
						}
					}
					break;
					case IN_NUM:{
						if (Character.isDigit(ch)){
							st.append((char)ch);
							updateLocation();
						}
						else{
							String txt;
							txt = st.toString();
							try {
								Integer.parseInt(txt);
								t = new Token(Kind.NUM_LIT,txt,line,column); state = State.START;
							} catch (NumberFormatException ex) {
								throw new LexicalException("The inputted integer is out of range at line " + (line+1) + " position " + (column+1));
							}
						}
					}break;
					default:
						throw new LexicalException("case not handled by lexer for " + (char)ch);
					
				}
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		return t;
	}

	@Override
	public IToken peek() throws LexicalException {
		// TODO Auto-generated method stub
		return null;
	}

}
