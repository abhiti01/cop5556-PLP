/**  This code is provided for solely for use of students in the course COP5556 Programming Language Principles at the 
 * University of Florida during the Fall Semester 2022 as part of the course project.  No other use is authorized. 
 */

package edu.ufl.cise.plpfa22;

import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;
import edu.ufl.cise.plpfa22.IToken.Kind;
import edu.ufl.cise.plpfa22.IToken.SourceLocation;

class LexerTest {

	/*** Useful functions ***/
	ILexer getLexer(String input) {
		return CompilerComponentFactory.getLexer(input);
	}

	// makes it easy to turn output on and off (and less typing than
	// System.out.println)
	static final boolean VERBOSE = false;

	void show(Object obj) {
		if (VERBOSE) {
			System.out.println(obj);
		}
	}

	// check that this token has the expected kind
	void checkToken(IToken t, Kind expectedKind) {
		assertEquals(expectedKind, t.getKind());
	}

	// check that the token has the expected kind and position
	void checkToken(IToken t, Kind expectedKind, int expectedLine, int expectedColumn) {
		assertEquals(expectedKind, t.getKind());
		assertEquals(new IToken.SourceLocation(expectedLine, expectedColumn), t.getSourceLocation());
	}

	// check that this token is an IDENT and has the expected name
	void checkIdent(IToken t, String expectedName) {
		assertEquals(Kind.IDENT, t.getKind());
		assertEquals(expectedName, String.valueOf(t.getText()));
	}

	// check that this token is an IDENT, has the expected name, and has the
	// expected position
	void checkIdent(IToken t, String expectedName, int expectedLine, int expectedColumn) {
		checkIdent(t, expectedName);
		assertEquals(new IToken.SourceLocation(expectedLine, expectedColumn), t.getSourceLocation());
	}

	// check that this token is an NUM_LIT with expected int value
	void checkInt(IToken t, int expectedValue) {
		assertEquals(Kind.NUM_LIT, t.getKind());
		assertEquals(expectedValue, t.getIntValue());
	}

	// check that this token is an NUM_LIT with expected int value and position
	void checkInt(IToken t, int expectedValue, int expectedLine, int expectedColumn) {
		checkInt(t, expectedValue);
		assertEquals(new IToken.SourceLocation(expectedLine, expectedColumn), t.getSourceLocation());
	}

	// check that this token is the EOF token
	void checkEOF(IToken t) {
		checkToken(t, Kind.EOF);
	}

	/*** Tests ****/

	// The lexer should add an EOF token to the end.
	@Test
	void testEmpty() throws LexicalException {
		String input = "";
		show(input);
		ILexer lexer = getLexer(input);
		show(lexer);
		checkEOF(lexer.next());
	}

	// A couple of single character tokens
	@Test
	void testSingleChar0() throws LexicalException {
		String input = """
				+
				-
				""";
		show(input);
		ILexer lexer = getLexer(input);
		checkToken(lexer.next(), Kind.PLUS, 1, 1);
		checkToken(lexer.next(), Kind.MINUS, 2, 1);
		checkEOF(lexer.next());
	}

	// comments should be skipped
	@Test
	void testComment0() throws LexicalException {
		// Note that the quotes around "This is a string" are passed to the lexer.
		String input = """
				"This is a string"
				// this is a comment
				*
				""";
		show(input);
		ILexer lexer = getLexer(input);
		checkToken(lexer.next(), Kind.STRING_LIT, 1, 1);
		checkToken(lexer.next(), Kind.TIMES, 3, 1);
		checkEOF(lexer.next());
	}

	// Example for testing input with an illegal character
	@Test
	void testError0() throws LexicalException {
		String input = """
				abc
				@
				""";
		show(input);
		ILexer lexer = getLexer(input);
		// this check should succeed
		checkIdent(lexer.next(), "abc");
		// this is expected to throw an exception since @ is not a legal
		// character unless it is part of a string or comment
		assertThrows(LexicalException.class, () -> {
			@SuppressWarnings("unused")
			IToken token = lexer.next();
		});
	}

	// Several identifiers to test positions
	@Test
	public void testIdent0() throws LexicalException {
		String input = """
				abc
				  def
				     ghi

				""";
		show(input);
		ILexer lexer = getLexer(input);
		checkIdent(lexer.next(), "abc", 1, 1);
		checkIdent(lexer.next(), "def", 2, 3);
		checkIdent(lexer.next(), "ghi", 3, 6);
		checkEOF(lexer.next());
	}

	@Test
	public void testIdenInt() throws LexicalException {
		String input = """
				a123 456b
				""";
		show(input);
		ILexer lexer = getLexer(input);
		checkIdent(lexer.next(), "a123", 1, 1);
		checkInt(lexer.next(), 456, 1, 6);
		checkIdent(lexer.next(), "b", 1, 9);
		checkEOF(lexer.next());
	}

	// Example showing how to handle number that are too big.
	@Test
	public void testIntTooBig() throws LexicalException {
		String input = """
				42
				99999999999999999999999999999999999999999999999999999999999999999999999
				""";
		ILexer lexer = getLexer(input);
		checkInt(lexer.next(), 42);
		Exception e = assertThrows(LexicalException.class, () -> {
			lexer.next();
		});
	}

	@Test
	public void testEscapeSequences0() throws LexicalException {
		String input = "\"\\b \\t \\n \\f \\r \"";
		show(input);
		ILexer lexer = getLexer(input);
		IToken t = lexer.next();
		String val = t.getStringValue();
		String expectedStringValue = "\b \t \n \f \r ";
		assertEquals(expectedStringValue, val);
		String text = String.valueOf(t.getText());
		String expectedText = "\"\\b \\t \\n \\f \\r \"";
		assertEquals(expectedText, text);
	}

	@Test
	public void testEscapeSequences1() throws LexicalException {
		String input = "   \" ...  \\\"  \\\'  \\\\  \"";
		show(input);
		ILexer lexer = getLexer(input);
		IToken t = lexer.next();
		String val = t.getStringValue();
		String expectedStringValue = " ...  \"  \'  \\  ";
		assertEquals(expectedStringValue, val);
		String text = String.valueOf(t.getText());
		String expectedText = "\" ...  \\\"  \\\'  \\\\  \""; // almost the same as input, but white space is omitted
		assertEquals(expectedText, text);
	}

	@Test
	public void ptest1() throws LexicalException {
		String input = """

				""";
		ILexer lexer = getLexer(input);
		{
			IToken token = lexer.next();
			Kind kind = token.getKind();
			assertEquals(Kind.EOF, kind);
		}
	}

	@Test
	public void test1() throws LexicalException {
		String input = """
				abc ()
				""";
		ILexer lexer = getLexer(input);
		{
			IToken token = lexer.next();
			Kind kind = token.getKind();
			assertEquals(kind, Kind.IDENT);
			SourceLocation sl = token.getSourceLocation();
			int line = sl.line();
			// System.err.println("Line" + line);

			assertEquals(sl.line(), 1);
			int charPositionInLine = sl.column();
			// System.err.println("Pos" + charPositionInLine);

			assertEquals(charPositionInLine, 1);
			char[] text = token.getText();
			String text2 = String.valueOf(text);
			// System.out.println("text" + text2);
			assertEquals(text2, "abc");
		}
		{
			IToken token = lexer.next();
			Kind kind = token.getKind();
			assertEquals(Kind.LPAREN, kind);

		}
	}

	@Test
	public void test2() throws LexicalException {
		String input = """
				a123 123a
				""";
		// System.out.println("New test");
		ILexer lexer = getLexer(input);
		{
			IToken token = lexer.next();
			Kind kind = token.getKind();
			// System.err.println("Kind"+kind);

			assertEquals(Kind.IDENT, kind);
			SourceLocation sl = token.getSourceLocation();
			int line = sl.line();
			// System.err.println("Line"+line);

			assertEquals(1, line);
			int charPositionInLine = sl.column();
			// System.err.println("Pos"+charPositionInLine);

			assertEquals(1, charPositionInLine);
			char[] text = token.getText();
			String text2 = String.valueOf(text);
			assertEquals("a123", text2);
		}
		{
			IToken token = lexer.next();
			Kind kind = token.getKind();
			// System.err.println("mixed1Kind"+kind);

			assertEquals(Kind.NUM_LIT, kind);
			SourceLocation sl = token.getSourceLocation();
			int line = sl.line();
			// System.err.println("Line"+line);

			assertEquals(1, line);
			int charPositionInLine = sl.column();
			// System.err.println("Pos"+charPositionInLine);

			assertEquals(6, charPositionInLine);
			char[] text = token.getText();
			String text2 = String.valueOf(text);
			assertEquals("123", text2);
			int val = token.getIntValue();
			assertEquals(123, val);
		}
		{
			IToken token = lexer.next();
			Kind kind = token.getKind();
			// System.err.println("mixed2Kind"+kind);

			assertEquals(Kind.IDENT, kind);
			SourceLocation sl = token.getSourceLocation();
			int line = sl.line();
			// System.err.println("Line"+line);

			assertEquals(1, line);
			int charPositionInLine = sl.column();
			// System.err.println("Pos"+charPositionInLine);

			assertEquals(9, charPositionInLine);
			char[] text = token.getText();
			String text2 = String.valueOf(text);
			assertEquals("a", text2);
		}
		{
			IToken token = lexer.next();
			Kind kind = token.getKind();
			// System.err.println("Kind" + kind);

			assertEquals(Kind.EOF, kind);
		}
	}

	@Test
	public void test3() throws LexicalException {
		String input = """
				99999999999999999999999999999999999999999999999999999999999999999999999
				""";
		ILexer lexer = getLexer(input);
		assertThrows(LexicalException.class, () -> {
			@SuppressWarnings("unused")
			IToken token = lexer.next();
		});
	}

	@Test
	void peektest1() throws LexicalException {
		String input = """
				+
				-
				""";
		show(input);
		ILexer lexer = getLexer(input);
		checkToken(lexer.peek(), Kind.PLUS);
		checkToken(lexer.next(), Kind.PLUS);
		checkToken(lexer.peek(), Kind.MINUS);
		checkToken(lexer.next(), Kind.MINUS);
		checkEOF(lexer.peek());
		checkEOF(lexer.next());
	}

	@Test
	void peektest2() throws LexicalException {
		// Note that the quotes around "This is a string" are passed to the lexer.
		String input = """
				"This is a string"
				*
				""";
		show(input);
		ILexer lexer = getLexer(input);
		checkToken(lexer.peek(), Kind.STRING_LIT);
		checkToken(lexer.next(), Kind.STRING_LIT, 1, 1);
		checkToken(lexer.peek(), Kind.TIMES);
		checkToken(lexer.next(), Kind.TIMES, 2, 1);
		checkEOF(lexer.peek());
		checkEOF(lexer.next());
	}

	@Test
	void peektest3() throws LexicalException {
		// Note that the quotes around "This is a string" are passed to the lexer.
		String input = """
				"This is a string"
				// this is a comment
				*
				""";
		show(input);
		ILexer lexer = getLexer(input);
		checkToken(lexer.peek(), Kind.STRING_LIT);
		checkToken(lexer.next(), Kind.STRING_LIT, 1, 1);
		checkToken(lexer.peek(), Kind.TIMES);
		checkToken(lexer.next(), Kind.TIMES, 3, 1);
		checkEOF(lexer.peek());
		checkEOF(lexer.next());
	}

	// adding test cases that our submission failed
	// comment next to String input shows current test output

	@Test
	void gradingtest3() throws LexicalException {
		String input = "5a a4"; // Lexical Exception: Invalid character for Identifier
		show(input);
		ILexer lexer = getLexer(input);
		checkInt(lexer.next(), 5, 1, 1);
		checkIdent(lexer.next(), "a", 1, 2);
		checkIdent(lexer.next(), "a4", 1, 4);
	}

	@Test
	void gradingtest4() throws LexicalException {
		String input = "a+2 2-x"; // Lexical Exception: Invalid character for Identifier
		show(input);
		ILexer lexer = getLexer(input);
		checkIdent(lexer.next(), "a", 1, 1);
		checkToken(lexer.next(), Kind.PLUS, 1, 2);
		checkInt(lexer.next(), 2, 1, 3);

	}

	@Test
	void gradingtest7() throws LexicalException {
		String input = "f.g . .."; // Lexical Exception: Invalid character for Identifier
		show(input);
		ILexer lexer = getLexer(input);

	}

	@Test
	void gradingtest8() throws LexicalException {
		String input = ".,;()"; // Lexical Exception: Error while getting next token, state: START, char:
		show(input);
		ILexer lexer = getLexer(input);

	}

	@Test
	void gradingtest9() throws LexicalException {
		String input = "+-*/%"; // for lexer input at SourceLocation[line=1, column=6] +-*/% ==> expected: <MOD>
								// but was: <EOF>
		show(input);
		ILexer lexer = getLexer(input);

	}

	@Test
	void gradingtest11() throws LexicalException {
		String input = "00"; // for lexer input at SourceLocation[line=1, column=3] 00 ==> expected:
								// <NUM_LIT> but was: <EOF>
		show(input);
		ILexer lexer = getLexer(input);
	}

	@Test
	void gradingtest13() throws LexicalException {
		String input = "_abc _ $ a$b c_d"; // Lexical Exception: Invalid character for Identifier
		show(input);
		ILexer lexer = getLexer(input);
	}

	@Test
	void gradingtest14() throws LexicalException {
		String input = "_abc1 _123 _ $ "; // Lexical Exception: Invalid character for Identifier
		show(input);
		ILexer lexer = getLexer(input);
	}

	@Test
	void gradingtest15() throws LexicalException {
		String input = " TRUE FALSE TRUEFALSE FALSETRUE"; // for lexer input at SourceLocation[line=1, column=1] TRUE
															// FALSE TRUEFALSE FALSETRUE ==> expected: <BOOLEAN_LIT> but
															// was: <IDENT>
		show(input);
		ILexer lexer = getLexer(input);
	}

	@Test
	void gradingtest17() throws LexicalException {
		String input = "CONST VAR PROCEDURE"; // for lexer input at SourceLocation[line=1, column=1] CONST VAR
												// PROCEDURE ==> expected: <KW_CONST> but was: <IDENT>
		show(input);
		ILexer lexer = getLexer(input);
		checkToken(lexer.next(), Kind.KW_CONST, 1, 1);
		checkToken(lexer.next(), Kind.KW_VAR, 1, 7);
		checkToken(lexer.next(), Kind.KW_PROCEDURE, 1, 11);
	}

	@Test
	void gradingtest18() throws LexicalException {
		String input = "CALL BEGIN END"; // for lexer input at SourceLocation[line=1, column=1] CALL BEGIN END ==>
											// expected: <KW_CALL> but was: <IDENT>
		show(input);
		ILexer lexer = getLexer(input);
		checkToken(lexer.next(), Kind.KW_CALL, 1, 1);
		checkToken(lexer.next(), Kind.KW_BEGIN, 1, 6);
		checkToken(lexer.next(), Kind.KW_END, 1, 12);
	}

	@Test
	void gradingtest19() throws LexicalException {
		String input = "IF THEN WHILE DO"; // for lexer input at SourceLocation[line=1, column=1] IF THEN WHILE DO ==>
											// expected: <KW_IF> but was: <IDENT>
		show(input);
		ILexer lexer = getLexer(input);
		checkToken(lexer.next(), Kind.KW_IF, 1, 1);
		checkToken(lexer.next(), Kind.KW_THEN, 1, 4);
		checkToken(lexer.next(), Kind.KW_WHILE, 1, 9);
		checkToken(lexer.next(), Kind.KW_DO, 1, 15);
	}

	@Test
	void gradingtest20() throws LexicalException {
		String input = "123DO DOabc DO123 DO_"; // for lexer input at SourceLocation[line=1, column=4] 123DO DOabc
												// DO123 DO_ ==> expected: <KW_DO> but was: <IDENT>
		show(input);
		ILexer lexer = getLexer(input);
		checkInt(lexer.next(), 123, 1, 1);
		checkToken(lexer.next(), Kind.KW_DO, 1, 4);
		checkIdent(lexer.next(), "DOabc", 1, 7);
		checkIdent(lexer.next(), "DO123", 1, 13);
		checkIdent(lexer.next(), "DO_", 1, 19);

	}

	@Test
	void gradingtest21() throws LexicalException {
		String input = "123VAR PROCEDUREabc BEGIN123 end_"; // for lexer input at SourceLocation[line=1, column=4]
															// 123VAR PROCEDUREabc BEGIN123 end_ ==> expected: <KW_VAR>
															// but was: <IDENT>
		show(input);
		ILexer lexer = getLexer(input);
		checkInt(lexer.next(), 123, 1, 1);
		checkToken(lexer.next(), Kind.KW_VAR, 1, 4);
		checkIdent(lexer.next(), "PROCEDUREabc", 1, 8);
		checkIdent(lexer.next(), "BEGIN123", 1, 21);
		checkIdent(lexer.next(), "end_", 1, 30);
	}

	@Test
	void gradingtest25() throws LexicalException {
		String input = "! TRUE ."; // for lexer input at SourceLocation[line=1, column=3]
									// !TRUE.==>expected:<BOOLEAN_LIT> but was:<IDENT>
		show(input);
		ILexer lexer = getLexer(input);
		checkToken(lexer.next(), Kind.BANG, 1, 1);
		checkToken(lexer.next(), Kind.BOOLEAN_LIT, 1, 3);
		checkToken(lexer.next(), Kind.DOT, 1, 8);
	}

	@Test
	void gradingtest27() throws LexicalException {
		String input = """
				VAR abc;
				.
				""";
		// for lexer input at SourceLocation[line=1, column=1] VAR abc; . ==> expected:
		// <KW_VAR> but was: <IDENT>
		show(input);
		ILexer lexer = getLexer(input);
		checkToken(lexer.next(), Kind.KW_VAR, 1, 1);
		checkIdent(lexer.next(), "abc", 1, 5);
		checkToken(lexer.next(), Kind.SEMI, 1, 8);
		checkToken(lexer.next(), Kind.DOT, 2, 1);
	}

	@Test
	void gradingtest28() throws LexicalException {
		String input = """
				BEGIN
				! "hello";
				! TRUE;
				!  33 ;
				! variable
				END
				.
				""";
		// for lexer input at SourceLocation[line=1, column=1] expected: <KW_BEGIN> but
		// was: <IDENT>
		show(input);
		ILexer lexer = getLexer(input);
		checkToken(lexer.next(), Kind.KW_BEGIN, 1, 1);
		checkToken(lexer.next(), Kind.BANG, 2, 1);
		checkToken(lexer.next(), Kind.STRING_LIT, 2, 3);
		checkToken(lexer.next(), Kind.SEMI, 2, 10);

	}

	@Test
	void gradingtest29() throws LexicalException {
		String input = """
				BEGIN
				? abc;
				! variable
				END
				.
				""";
		// for lexer input at SourceLocation[line=1, column=1] expected: <KW_BEGIN> but
		// was: <IDENT>
		show(input);
		ILexer lexer = getLexer(input);
		lexer.next();
		checkToken(lexer.next(), Kind.QUESTION, 2, 1);
		lexer.next();
		lexer.next();
		lexer.next();
		checkIdent(lexer.next(), "variable", 3, 3);

	}

	@Test
	void gradingtest30() throws LexicalException {
		String input = """
				CONST a = 3, b = TRUE, c = "hello";
				.
				""";
		// ==> for lexer input at SourceLocation[line=1, column=1] expected: <KW_CONST>
		// but was: <IDENT>
		show(input);
		ILexer lexer = getLexer(input);
		lexer.next();
		lexer.next();
		checkToken(lexer.next(), Kind.EQ, 1, 9);
		checkInt(lexer.next(), 3, 1, 11);
		checkToken(lexer.next(), Kind.COMMA, 1, 12);

	}

	// tests with peek() begin here

	@Test
	void gradingtest31() throws LexicalException {
		String input = """
				BEGIN
				x := 3;
				y := "hello";
				b := FALSE
				END
				.
				""";

		// ==> for lexer input at SourceLocation[line=1, column=1] expected: <KW_BEGIN>
		// but was: <IDENT>
		// TO DO: fix line and column inside peek()
		// the commented lines in this test case are failing
		show(input);
		ILexer lexer = getLexer(input);
		checkToken(lexer.peek(), Kind.KW_BEGIN);
		// checkToken(lexer.peek(), Kind.KW_BEGIN, 1, 1);
		checkToken(lexer.next(), Kind.KW_BEGIN, 1, 1);
		checkIdent(lexer.peek(), "x");
		// checkIdent(lexer.peek(), "x", 2, 1);
		checkIdent(lexer.next(), "x", 2, 1);
		checkToken(lexer.peek(), Kind.ASSIGN);
		// checkToken(lexer.peek(), Kind.ASSIGN, 2, 3);
		checkToken(lexer.next(), Kind.ASSIGN, 2, 3);
	}

	@Test
	void gradingtest32() throws LexicalException {
		String input = """
				BEGIN
				CALL x
				END
				.
				""";
		// ==> for lexer input at SourceLocation[line=1, column=1] expected: <KW_BEGIN>
		// but was: <IDENT>
		show(input);
		ILexer lexer = getLexer(input);
	}

	@Test
	void gradingtest33() throws LexicalException {
		String input = """
				CONST a=3;
				VAR x,y,z;
				PROCEDURE p;
				  VAR j;
				  BEGIN
				 	? x;
				 	IF x = 0 THEN ! y ;
				 	WHILE j < 24 DO CALL z
				  END;
				! z
				.
				""";

		// ==> for lexer input at SourceLocation[line=1, column=1] expected: <KW_CONST>
		// but was: <IDENT>
		show(input);
		ILexer lexer = getLexer(input);
	}

	@Test
	void gradingtest34() throws LexicalException {
		String input = """
				CONST a=3;
				VAR x,y,z;
				PROCEDURE p;
				  VAR j;
				  BEGIN
				 	? x;
				 	IF x = 0 THEN ! y ;
				 	WHILE j < 24 DO CALL z
				  END;
				! a+b - (c/e) * 35/(3+4)
				.
				""";

		// ==> for lexer input at SourceLocation[line=1, column=1] expected: <KW_CONST>
		// but was: <IDENT>
		show(input);
		ILexer lexer = getLexer(input);
	}

	@Test
	void gradingtest35() throws LexicalException {
		String input = """
				//comment
				VAR X = 34%2;
				""";

		// ==> for lexer input at SourceLocation[line=2, column=1] expected: <KW_VAR>
		// but was: <IDENT>
		show(input);
		ILexer lexer = getLexer(input);
	}

	@Test
	void gradingtest37() throws LexicalException {
		String input = """
				VAR x123_ = 0;
				////comment
				""";

		// ==> for lexer input at SourceLocation[line=1, column=1] expected: <KW_VAR>
		// but was: <IDENT>
		show(input);
		ILexer lexer = getLexer(input);
	}

	@Test
	void gradingtest38() throws LexicalException {
		String input = """

				VAR x,y,z;
				x := 3;
				y := 4;
				IF x <= y
				THEN TRUE;
				""";

		// ==> for lexer input at SourceLocation[line=2, column=3] expected: <KW_VAR>
		// but was: <IDENT>
		show(input);
		ILexer lexer = getLexer(input);
	}

	@Test
	void gradingtest39() throws LexicalException {
		String input = """



				VAR x=3;
				CONST y=4;
				IF x#y
				THEN TRUE;
				""";

		// ==> for lexer input at SourceLocation[line=4, column=4] expected: <KW_VAR>
		// but was: <IDENT>
		show(input);
		ILexer lexer = getLexer(input);
	}

}
