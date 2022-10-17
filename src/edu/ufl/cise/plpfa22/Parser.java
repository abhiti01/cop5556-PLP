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
import edu.ufl.cise.plpfa22.ast.StatementAssign;
import edu.ufl.cise.plpfa22.ast.StatementBlock;
import edu.ufl.cise.plpfa22.ast.StatementCall;
import edu.ufl.cise.plpfa22.ast.Expression;
import edu.ufl.cise.plpfa22.ast.ExpressionBinary;
import edu.ufl.cise.plpfa22.ast.ExpressionBooleanLit;
import edu.ufl.cise.plpfa22.ast.ExpressionIdent;
import edu.ufl.cise.plpfa22.ast.ExpressionNumLit;
import edu.ufl.cise.plpfa22.ast.ExpressionStringLit;
import edu.ufl.cise.plpfa22.ast.Ident;
import edu.ufl.cise.plpfa22.ast.StatementEmpty;
import edu.ufl.cise.plpfa22.ast.StatementIf;
import edu.ufl.cise.plpfa22.ast.StatementInput;
import edu.ufl.cise.plpfa22.ast.StatementOutput;
import edu.ufl.cise.plpfa22.ast.StatementWhile;

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
        return program;
    }

    private Program program() throws SyntaxException, LexicalException {
        IToken firstToken = this.token;
        Block block = block();
        match(Kind.DOT);
        match(Kind.EOF);
        return new Program(firstToken, block);
    }

    private Block block() throws SyntaxException, LexicalException {
        IToken firstToken = this.token;
        List<ConstDec> constDecList = new ArrayList<ConstDec>();
        List<VarDec> varDecList = new ArrayList<VarDec>();
        List<ProcDec> procDecList = new ArrayList<ProcDec>();
        Statement statement = null;
        Kind kind = this.token.getKind();
        while (isKind(token, Kind.KW_CONST)) {
            match(Kind.KW_CONST);
            ConstDec constDec = constDec();
            constDecList.add(constDec);
            while (token.getKind() != Kind.SEMI) {
                if (token.getKind() == Kind.COMMA) {
                    match(Kind.COMMA);

                    ConstDec constDec2 = constDec();
                    constDecList.add(constDec2);
                } else {
                    throw new SyntaxException("Missing comma");
                }
            }
            match(Kind.SEMI);
        }
        while (isKind(token, Kind.KW_VAR)) {
            match(Kind.KW_VAR);
            VarDec varDec = varDec();
            varDecList.add(varDec);
            while (token.getKind() != Kind.SEMI) {
                if (token.getKind() == Kind.COMMA) {
                    match(Kind.COMMA);
                    VarDec varDec2 = varDec();
                    varDecList.add(varDec2);
                } else {
                    throw new SyntaxException("Missing comma");
                }
            }
            match(Kind.SEMI);
        }

        while (isKind(token, Kind.KW_PROCEDURE)) {
            ProcDec procDec = procDec();
            procDecList.add(procDec);
        }

        while (isKind(token, Kind.IDENT) || isKind(token, Kind.KW_CALL) ||
                isKind(token, Kind.QUESTION)
                || isKind(token, Kind.BANG) || isKind(token, Kind.KW_BEGIN) || isKind(token,
                        Kind.KW_IF)
                || isKind(token, Kind.KW_WHILE)) {
            statement = statement();
        }

        if (statement == null) {
            statement = new StatementEmpty(firstToken);
        }

        return new Block(firstToken, constDecList, varDecList, procDecList, statement);
    }

    private ConstDec constDec() throws SyntaxException, LexicalException {
        IToken firstToken = this.token;
        Object val = null;

        IToken ident = this.token;
        match(Kind.IDENT);
        match(Kind.EQ);
        if (isKind(token, Kind.NUM_LIT)) {
            val = token.getIntValue();
            match(Kind.NUM_LIT);
        }

        else if (isKind(token, Kind.STRING_LIT)) {
            val = token.getStringValue();
            match(Kind.STRING_LIT);
        }

        else if (isKind(token, Kind.BOOLEAN_LIT)) {
            val = token.getBooleanValue();
            match(Kind.BOOLEAN_LIT);
        }

        return new ConstDec(firstToken, ident, val);

    }

    private ProcDec procDec() throws SyntaxException, LexicalException {
        IToken firstToken = this.token;
        match(Kind.KW_PROCEDURE);
        IToken name = this.token;
        match(Kind.IDENT);
        match(Kind.SEMI);
        Block blockProc = block();
        match(Kind.SEMI);
        return new ProcDec(firstToken, name, blockProc);
    }

    private VarDec varDec() throws SyntaxException, LexicalException {
        IToken firstToken = this.token;
        match(Kind.IDENT);
        return new VarDec(firstToken, firstToken);
    }

    private Statement statement() throws LexicalException, SyntaxException {
        Statement statement = null;
        IToken firstToken = this.token;
        switch (token.getKind()) {
            case IDENT: {
                IToken identFirst = this.token;
                Ident ident = new Ident(identFirst);
                match(Kind.IDENT);
                match(Kind.ASSIGN);
                Expression expr = expression();
                statement = new StatementAssign(firstToken, ident, expr);
            }
                break;
            case BANG: {
                IToken bang = this.token;
                match(Kind.BANG);
                Expression expr = expression();
                statement = new StatementOutput(firstToken, expr);
            }
                break;
            case KW_BEGIN: {
                IToken begin = match(Kind.KW_BEGIN);
                List<Statement> statementList = new ArrayList<Statement>();
                Statement statementOne = statement();
                statementList.add(statementOne);
                if (token.getKind() == Kind.SEMI) {
                    match(Kind.SEMI);
                    while (token.getKind() != Kind.KW_END) {
                        Statement statementTwo = statement();
                        statementList.add(statementTwo);
                        if (token.getKind() == Kind.SEMI) {
                            match(Kind.SEMI);
                        }
                    }
                }

                match(Kind.KW_END);

                statement = new StatementBlock(firstToken, statementList);
            }
                break;
            case QUESTION: {
                IToken ques = match(Kind.QUESTION);
                Ident ident = null;
                if (token.getKind() == Kind.IDENT) {
                    ident = new Ident(this.token);
                    match(Kind.IDENT);
                }
                statement = new StatementInput(firstToken, ident);
            }
                break;
            case KW_CALL: {
                IToken call = match(Kind.KW_CALL);
                Ident ident = null;
                if (token.getKind() == Kind.IDENT) {
                    ident = new Ident(this.token);
                    match(Kind.IDENT);
                }
                statement = new StatementCall(firstToken, ident);
            }
                break;
            case KW_IF: {
                firstToken = this.token;
                match(Kind.KW_IF);
                Expression ifExpr = expression();
                match(Kind.KW_THEN);
                Statement ifStatement = statement();
                statement = new StatementIf(firstToken, ifExpr, ifStatement);
            }
                break;
            case KW_WHILE: {
                firstToken = this.token;
                match(Kind.KW_WHILE);
                Expression whileExpression = expression();
                match(Kind.KW_DO);
                Statement whileStatement = statement();
                statement = new StatementWhile(firstToken, whileExpression, whileStatement);
            }
                break;

        }
        if (statement == null) {
            statement = new StatementEmpty(firstToken);
        }
        return statement;
    }

    private Expression expression() throws LexicalException, SyntaxException {

        IToken firstToken = this.token;
        System.out.println("INSIDE EXPRESSION PARSER WITH FIRSTTOKEN"+firstToken.getKind());
        Expression expression = null;
        IToken op = null;
        Expression nextExpr = null;
        Expression additiveExpr = additiveExpression();
        while (token.getKind() == Kind.LT || token.getKind() == Kind.GT || token.getKind() == Kind.EQ
                || token.getKind() == Kind.NEQ || token.getKind() == Kind.LE || token.getKind() == Kind.GE) {
            switch (token.getKind()) {
                case LT: {
                    op = this.token;
                    match(Kind.LT);
                }
                    break;
                case GT: {
                    op = this.token;
                    match(Kind.GT);
                }
                    break;
                case EQ: {
                    op = this.token;
                    match(Kind.EQ);
                }
                    break;
                case NEQ: {
                    op = this.token;
                    match(Kind.NEQ);
                }
                    break;
                case LE: {
                    op = this.token;
                    match(Kind.LE);
                }
                    break;
                case GE: {
                    op = this.token;
                    match(Kind.GE);
                }

                    break;
            }
            nextExpr = additiveExpression();
            additiveExpr = new ExpressionBinary(firstToken, additiveExpr, op, nextExpr);

        }
        return additiveExpr;

    }

    private Expression additiveExpression() throws LexicalException, SyntaxException {
        IToken firstToken = this.token;
        Expression expression = null;
        Expression nextExpr = null;
        IToken op = null;
        Expression multiplicativeExpr = multiplicativeExpression();
        while (token.getKind() == Kind.PLUS || token.getKind() == Kind.MINUS) {
            switch (token.getKind()) {
                case PLUS: {
                    op = this.token;
                    match(Kind.PLUS);
                }
                    break;
                case MINUS: {
                    op = this.token;
                    match(Kind.MINUS);
                }
                    break;
            }
            nextExpr = multiplicativeExpression();

            multiplicativeExpr = new ExpressionBinary(firstToken, multiplicativeExpr, op, nextExpr);
        }
        return multiplicativeExpr;

    }

    private Expression multiplicativeExpression() throws LexicalException, SyntaxException {
        IToken firstToken = this.token;
        Expression expression = null;
        IToken op = null;
        Expression nextExpr = null;
        Expression primaryExpr = primaryExpression();
        while (token.getKind() == Kind.TIMES || token.getKind() == Kind.DIV || token.getKind() == Kind.MOD) {
            switch (token.getKind()) {
                case TIMES: {
                    op = this.token;
                    match(Kind.TIMES);
                }
                    break;
                case DIV: {
                    op = this.token;
                    match(Kind.DIV);

                }
                    break;
                case MOD: {
                    op = this.token;
                    match(Kind.MOD);
                }
                    break;
            }
            nextExpr = primaryExpression();
            primaryExpr = new ExpressionBinary(firstToken, primaryExpr, op, nextExpr);

        }
        return primaryExpr;

    }

    private Expression primaryExpression() throws SyntaxException, LexicalException {
        IToken firstToken = this.token;
        Expression expression = null;

        switch (token.getKind()) {
            case IDENT: {
                expression = new ExpressionIdent(firstToken);
                match(Kind.IDENT);
            }
                break;
            case NUM_LIT:
            case STRING_LIT:
            case BOOLEAN_LIT: {
                expression = constVal();
            }
                break;
            case LPAREN: {
                match(Kind.LPAREN);
                expression = expression();
                match(Kind.RPAREN);

            }
                break;
            default: {
                throw new SyntaxException("Exception from primaryexpression");
            }

        }

        return expression;

    }

    private Expression constVal() throws LexicalException, SyntaxException {
        IToken firstToken = this.token;
        Expression expression = null;
        switch (token.getKind()) {
            case NUM_LIT: {
                match(Kind.NUM_LIT);
                expression = new ExpressionNumLit(firstToken);
            }
                break;
            case STRING_LIT: {
                match(Kind.STRING_LIT);
                expression = new ExpressionStringLit(firstToken);
            }
                break;
            case BOOLEAN_LIT: {
                match(Kind.BOOLEAN_LIT);
                expression = new ExpressionBooleanLit(firstToken);
            }
                break;
            default:
                throw new SyntaxException("Const val not recognised");
        }
        return expression;
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
