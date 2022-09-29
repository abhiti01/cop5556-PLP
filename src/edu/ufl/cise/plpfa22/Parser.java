package edu.ufl.cise.plpfa22;

import java.util.ArrayList;
import java.util.List;
import edu.ufl.cise.plpfa22.ast.ASTNode;
import edu.ufl.cise.plpfa22.ast.ProcDec;
import edu.ufl.cise.plpfa22.ast.Program;
import edu.ufl.cise.plpfa22.ast.VarDec;
import edu.ufl.cise.plpfa22.IToken.Kind;
import edu.ufl.cise.plpfa22.ast.Block;
import edu.ufl.cise.plpfa22.ast.ConstDec;
import edu.ufl.cise.plpfa22.ast.Declaration;
import edu.ufl.cise.plpfa22.ast.Statement;
import edu.ufl.cise.plpfa22.ast.Expression;
import edu.ufl.cise.plpfa22.ast.ExpressionNumLit;
import edu.ufl.cise.plpfa22.ast.StatementEmpty;

public class Parser implements IParser {

    ILexer lexer;
    IToken token;

    public Parser(ILexer lexer) throws LexicalException {
        this.lexer = lexer;
        consume();
    }

    @Override
    public ASTNode parse() throws PLPException {
        // TODO Auto-generated method stub
        IToken firstToken = this.token;
        Program program = program();
        // consume();
        // if (match(Kind.DOT).getKind() == Kind.EOF) {
        // return program;
        // }

        // else {
        // return null;
        // }
        return program;
    }

    private Program program() throws SyntaxException, LexicalException {
        IToken firstToken = this.token;
        Block block = block();
        // System.out.println(match(Kind.DOT));
        // match(Kind.DOT);
        // if (isKind(this.token, Kind.EOF)) {
        // return new Program(firstToken, block);
        // }
        return new Program(firstToken, block);
    }

    private Block block() throws SyntaxException, LexicalException {
        IToken firstToken = this.token;
        List<ConstDec> constDecList = new ArrayList<ConstDec>();
        List<VarDec> varDecList = new ArrayList<VarDec>();
        List<ProcDec> procDecList = new ArrayList<ProcDec>();
        Statement statement = null;
        Kind kind = this.token.getKind();

        // while (isKind(token, Kind.KW_CONST)) {
        // ConstDec constDec = constDec();
        // constDecList.add(constDec);
        // match(Kind.SEMI);
        // }

        // while (isKind(token, Kind.KW_VAR)) {
        // VarDec varDec = varDec();
        // varDecList.add(varDec);
        // match(Kind.SEMI);
        // }

        // while (isKind(token, Kind.KW_PROCEDURE)) {
        // ProcDec procDec = procDec();
        // procDecList.add(procDec);
        // match(Kind.SEMI);
        // }

        // while (isKind(token, Kind.IDENT) || isKind(token, Kind.KW_CALL) ||
        // isKind(token, Kind.QUESTION)
        // || isKind(token, Kind.BANG) || isKind(token, Kind.KW_BEGIN) || isKind(token,
        // Kind.KW_IF)
        // || isKind(token, Kind.KW_WHILE)) {
        // statement = statement();
        // }

        switch (kind) {
            case KW_CONST: {
                ConstDec constDec = constDec();
                constDecList.add(constDec);
                // IPLPToken valToken = match(PLPTokenKinds.Kind.KW_VAL);
                // INameDef nameDef = nameDef();
                // match(Kind.ASSIGN);
                // IExpression expression = expression();
                // match(PLPTokenKinds.Kind.SEMI);
                match(Kind.SEMI);

            }
                break;
            case KW_VAR: {
                VarDec varDec = varDec();
                varDecList.add(varDec);
            }
                break;
            case KW_PROCEDURE: {
                ProcDec procDec = procDec();
                procDecList.add(procDec);
            }
                break;
            // case IDENT: {
            // statement = statement();
            // }
            // break;
            default:
                statement = statement();
        }

        return new Block(firstToken, constDecList, varDecList, procDecList, statement);
    }

    private ConstDec constDec() throws SyntaxException, LexicalException {
        IToken firstToken = this.token;
        // IToken ident = consume();
        // match(Kind.IDENT);
        // match(Kind.EQ);
        IToken nextToken = consume();
        match(Kind.IDENT);
        IToken ident = nextToken;
        match(Kind.EQ);
        Integer val = token.getIntValue();
        match(Kind.NUM_LIT);
        // consume();
        // String ident = nextToken.getText().toString();

        // switch (nextToken.getKind()) {
        // case IDENT: {
        // IToken ident = nextToken;
        // consume();
        // switch (token.getKind()) {
        // case EQ: {
        // consume();
        // // constVal();
        // switch (token.getKind()) {
        // case NUM_LIT: {
        // int val = token.getIntValue();
        // ExpressionNumLit ex = new ExpressionNumLit(token);
        // }

        // }
        // }
        // break;
        // default: {
        // throw new SyntaxException("expected = after ident");
        // }

        // }
        // }
        // break;
        // default: {
        // throw new SyntaxException("expected ident after CONST");
        // }
        // }
        // IToken eq = match(Kind.IDENT);

        // switch (firstToken.getKind()) {
        // case IDENT:
        // {
        // this.token = match(Kind.IDENT);

        // if (nextToken.getKind() == Kind.EQ){
        // nextToken = consume();
        // }
        // }
        // }

        return new ConstDec(firstToken, ident, val);

    }

    private ProcDec procDec() {
        return null;
    }

    private VarDec varDec() {
        return null;
    }

    private Statement statement() throws LexicalException {
        Statement statement = null;
        System.out.println("In statement");
        IToken firstToken = this.token;
        consume();
        switch (token.getKind()) {
            case IDENT: {
                System.out.println("Unimplemented portion");
            }
                break;
            // add more statement cases here

            case BANG: {
                Expression expr = expression();
            }

            default: {
                statement = new StatementEmpty(firstToken);
            }

        }
        return statement;
    }

    private Expression expression() {

        return null;

    }

    private boolean isKind(IToken token, Kind kind) {
        return token.getKind() == kind;
    }

    private IToken consume() throws LexicalException {
        token = lexer.next();
        return token;
    }

    private IToken match(Kind kind) throws SyntaxException, LexicalException {
        if (isKind(token, kind)) {
            return consume();
        }

        else {
            throw new SyntaxException("Expected kind: " + kind + ", got: " + token.getKind());
        }
    }

}
