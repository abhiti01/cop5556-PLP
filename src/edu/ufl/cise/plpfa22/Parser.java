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
        System.out.println("Token after block in program: "+token.getKind());
        match(Kind.DOT);
        System.out.println("Token after match in program: "+token.getKind());
        match(Kind.EOF);
        return new Program(firstToken, block);
    }

    private Block block() throws SyntaxException, LexicalException {
        IToken firstToken = this.token;
        System.out.println("HERE FROM INSIDE PROC GOT TOKEN: "+token.getKind());
        List<ConstDec> constDecList = new ArrayList<ConstDec>();
        // List<ConstDec> constDecList;
        List<VarDec> varDecList = new ArrayList<VarDec>();
        List<ProcDec> procDecList = new ArrayList<ProcDec>();
        Statement statement = null;
        Kind kind = this.token.getKind();
        System.out.println("In Block");
        System.out.println("Kind in block"+kind);
        // while (isKind(token, Kind.KW_CONST)) {
        //     match(Kind.KW_CONST);
        //     ConstDec constDec = constDec();
        //     constDecList.add(constDec);
        //     System.out.println("Current token prior while: " + token.getKind());
        //     while (token.getKind() != Kind.SEMI){
        //         if (token.getKind() == Kind.COMMA){
        //             System.out.println("Current token 0: " + token.getKind());
        //             match(Kind.COMMA);
        //             consume();
        //             System.out.println("Current token: " + token.getKind());
        //             ConstDec constDec2 = constDec();
        //             constDecList.add(constDec2);
        //         }
        //     }
        //     match(Kind.SEMI);
        // }
        while (isKind(token, Kind.KW_CONST)) {
            match(Kind.KW_CONST);
            ConstDec constDec = constDec();
            constDecList.add(constDec);
            System.out.println("Current token prior while: " + token.getKind());
            // while (token.getKind() != Kind.SEMI){
            //     if (token.getKind() == Kind.COMMA){
            //         match(Kind.COMMA);
            //         ConstDec constDec2 = constDec();
            //         constDecList.add(constDec2);
            //     }
            // }
            // match(Kind.SEMI);
            while (token.getKind() != Kind.SEMI){
                if (token.getKind() == Kind.COMMA){
                    match(Kind.COMMA);
                    
                    ConstDec constDec2 = constDec();
                    constDecList.add(constDec2);
                }
            }
            match(Kind.SEMI);
        }
        // while (isKind(token,Kind.KW_CONST)){
        //     match(Kind.KW_CONST);
        //     ConstDec constDec = constDec();
        //     constDecList.add(constDec);
        //     while (token.getKind()==Kind.COMMA){
        //         match(Kind.COMMA);
        //         ConstDec constDec1 = constDec();
        //         constDecList.add(constDec1);

        //     }
        //     match(Kind.SEMI);
        // }   
        System.out.println("After CONST BLOCK "+token.getKind());
        while (isKind(token, Kind.KW_VAR)) {
            // consume();
            match(Kind.KW_VAR);
            VarDec varDec = varDec();
            varDecList.add(varDec);
            System.out.println("GOt token after matching first var: "+token.getKind());
            while (token.getKind()!=Kind.SEMI){
                if (token.getKind() == Kind.COMMA){
                    match(Kind.COMMA);
                    VarDec varDec2 = varDec();
                    varDecList.add(varDec2);
                    System.out.println("After matching var got:" +token.getKind());
                }
            }
            System.out.println("Token outside loop: "+token.getKind());
            match(Kind.SEMI);
            System.out.println("Token outside loop after match: "+token.getKind());
        }

        while (isKind(token, Kind.KW_PROCEDURE)) {
            System.out.println("INSide proc isKind");
            ProcDec procDec = procDec();
            procDecList.add(procDec);
            // match(Kind.SEMI);
        }

        while (isKind(token, Kind.IDENT) || isKind(token, Kind.KW_CALL) ||
        isKind(token, Kind.QUESTION)
        || isKind(token, Kind.BANG) || isKind(token, Kind.KW_BEGIN) || isKind(token,
        Kind.KW_IF)
        || isKind(token, Kind.KW_WHILE)) {
            statement = statement();
        }

        if(statement == null){
            statement = new StatementEmpty(firstToken);
        }

        return new Block(firstToken, constDecList, varDecList, procDecList, statement);
    }


    private ConstDec constDec() throws SyntaxException, LexicalException {
        IToken firstToken = this.token;
        Object val = null;
        // IToken ident = consume();
        // match(Kind.IDENT);
        // match(Kind.EQ);
        // IToken nextToken = consume();


        // old stuff below
        IToken ident = this.token;
        System.out.println("In constdec got"+token.getKind());
        match(Kind.IDENT);
        match(Kind.EQ);
        // Integer val = token.getIntValue();
        // match(Kind.NUM_LIT);
        if (isKind(token, Kind.NUM_LIT)){
            val = token.getIntValue();
        }

        else if (isKind(token, Kind.STRING_LIT)){
            val = token.getStringValue();
        }

        else if(isKind(token,Kind.BOOLEAN_LIT)){
            val = token.getBooleanValue();
        }
        consume();

        // Expression val = constVal();


        // new stuff

        

        return new ConstDec(firstToken, ident, val);

    }

    private ProcDec procDec() throws SyntaxException, LexicalException {
        System.out.println("INSIDE PROCDEC first token: "+token.getKind());
        IToken firstToken = this.token;
        match(Kind.KW_PROCEDURE);
        System.out.println("INSIDE PROCDEC second token: "+token.getKind());
        IToken name = this.token;
        match(Kind.IDENT);
        match(Kind.SEMI);
        System.out.println("GOT TOKEN INSIDE PROCDEC BEFORE GOING TO BLOCK: "+token.getKind());
        Block blockProc = block();
        match(Kind.SEMI);
        System.out.println("GOT TOKEN INSIDE PROC AFTER MATCHING SEMI: "+token.getKind());
        return new ProcDec(firstToken,name, blockProc);
    }

    private VarDec varDec() throws SyntaxException, LexicalException {
        IToken firstToken = this.token;
        // System.out.println("Token text before consume"+token.getStringValue());
        // System.out.println("Token kind before consume"+token.getKind());
        match(Kind.IDENT);
        // System.out.println("Token kind after consume"+token.getKind());
        // match(Kind.IDENT);
        // match(Kind.EQ);
        // IToken nextToken = consume();
        // match(Kind.IDENT);
        System.out.println("Token kind after match"+token.getKind());
        return new VarDec(firstToken, firstToken);
    }

    private Statement statement() throws LexicalException, SyntaxException {
        Statement statement = null;
        System.out.println("In statement");
        IToken firstToken = this.token;
        System.out.println(firstToken.getText());
        // System.out.println(token.getText());
        // consume();
        // System.out.println(token.getText());
        switch (token.getKind()) {
            case IDENT: {
                IToken identFirst = this.token;
                Ident ident = new Ident(identFirst);
                consume();
                match(Kind.ASSIGN);
                Expression expr = expression();
                statement = new StatementAssign(firstToken, ident, expr);
            }
                break;
            // add more statement cases here
            case BANG: {
                IToken bang = consume();
                System.out.println("Making expression from bang statement");
                Expression expr = expression();
                statement = new StatementOutput(firstToken, expr);
                System.out.println("token after creation bang "+token.getKind());
            }break;
            case KW_BEGIN: {
                IToken begin = match(Kind.KW_BEGIN);
                List<Statement> statementList= new ArrayList<Statement>();
                Statement statementOne = statement();
                statementList.add(statementOne);
                if (token.getKind() == Kind.SEMI){
                    match(Kind.SEMI);
                    while(token.getKind()!=Kind.KW_END){
                        System.out.println("Inside begin got token (after matching semi)"+token.getKind());
                        Statement statementTwo = statement();
                        System.out.println("token after creation statement2 "+token.getKind());
                        statementList.add(statementTwo);
                        if (token.getKind() == Kind.SEMI){
                            match(Kind.SEMI);
                        }
                    }
                    System.out.println("token after while loop in beegin "+token.getKind());
                    match(Kind.KW_END);
                }
                
                

                statement = new StatementBlock(firstToken, statementList);
            } break;
            case QUESTION:{
                IToken ques = match(Kind.QUESTION);
                Ident ident = null;
                if (token.getKind() == Kind.IDENT){
                    ident = new Ident(this.token);
                    consume();
                }
                // match(Kind.Ident);
                statement = new StatementInput(firstToken, ident);
            } break;
            case KW_CALL:{
                IToken call = match(Kind.KW_CALL);
                Ident ident = null;
                if (token.getKind() == Kind.IDENT){
                    ident = new Ident(this.token);
                    consume();
                }
                statement = new StatementCall(firstToken, ident);
            } break;
            case KW_IF:{
                System.out.println("In if statement got token"+ token.getKind());
                firstToken = this.token;
                match(Kind.KW_IF);
                Expression ifExpr = expression();
                match(Kind.KW_THEN);
                Statement ifStatement = statement();
                statement = new StatementIf(firstToken, ifExpr, ifStatement);
                System.out.println("Token afrer creating if:" + token.getKind());
            }break;
            case KW_WHILE:{
                System.out.println("In while statement got token"+ token.getKind());
                firstToken = this.token;
                match(Kind.KW_WHILE);
                Expression whileExpression = expression();
                match(Kind.KW_DO);
                Statement whileStatement = statement();
                statement = new StatementWhile(firstToken, whileExpression, whileStatement);
            }break;
            // case DOT: {
            //     consume();
            //     System.out.println("IN CASE DOT INSIDE STATEMENT");
            //     // statement = new StatementEmpty(firstToken);
            // }break;
            // case EOF: {
            //     System.out.println("IN CASE EOF INSIDE STATEMENT");
            //     statement = new StatementEmpty(firstToken);
            // }break;

        }
        if (statement == null){
            statement = new StatementEmpty(firstToken);
        }
        return statement;
    }

    private Expression expression() throws LexicalException, SyntaxException {

        IToken firstToken = this.token;
        Expression expression =null;
        IToken op = null;
        Expression nextExpr = null;
        System.out.println("In expression with token: "+ firstToken.getKind());
        Expression additiveExpr = additiveExpression();
        // consume();
        System.out.println("INside expression after additive, got additive: "+additiveExpr+"Current token: "+token.getKind());
        while(token.getKind() == Kind.LT || token.getKind() == Kind.GT || token.getKind()==Kind.EQ||token.getKind()==Kind.NEQ||token.getKind()==Kind.LE||token.getKind()==Kind.GE){
            switch(token.getKind()){
                case LT:
                case GT:
                case EQ:
                case NEQ:
                case LE:
                case GE:
                {
                    op = this.token;
                    consume();
                }
                    
                    break;
            }
            nextExpr = additiveExpression();

        }
        if (nextExpr == null){
            expression = additiveExpr;
        }
        else{
            expression = new ExpressionBinary(firstToken, additiveExpr, op, nextExpr);
        }
        return expression;
        // expression = new ExpressionBinary(firstToken, additiveExpr, op, nextExpr);
        // return expression;

    }

    private Expression additiveExpression() throws LexicalException, SyntaxException{
        System.out.println("In additive exppression");
        IToken firstToken = this.token;
        System.out.println("with token "+ firstToken.getKind());
        Expression expression =null;
        Expression nextExpr = null;
        IToken op = null;
        Expression multiplicativeExpr = multiplicativeExpression();
        // consume();
        while(token.getKind()==Kind.PLUS||token.getKind()==Kind.MINUS){
            switch(token.getKind()){
                case PLUS:
                case MINUS:
                {
                    op = this.token;
                    consume();
                }
                break;
            }
            nextExpr = multiplicativeExpression();
        }
        System.out.println(nextExpr);
        if (nextExpr == null){
            System.out.println("Inside additive expr got mutiplicativeExprt: "+multiplicativeExpr);
            expression = multiplicativeExpr;
        }
        else{
            expression = new ExpressionBinary(firstToken, multiplicativeExpr, op, nextExpr);
        }
        return expression;
        

    }

    private Expression multiplicativeExpression() throws LexicalException, SyntaxException{
        IToken firstToken = this.token;
        System.out.println("In multiplicative exppression with token: "+ firstToken.getKind());
        Expression expression = null;
        IToken op = null;
        Expression nextExpr = null;
        Expression primaryExpr = primaryExpression();
        System.out.println("Got token from primary Expr: " +token.getKind());
        System.out.println("primary Expr type: " +primaryExpr);
        // consume();
        // System.out.println("Got token from primary Expr: " +token.getKind());
        while(token.getKind()==Kind.TIMES||token.getKind()==Kind.DIV||token.getKind()==Kind.MOD){
            switch(token.getKind()){
                case TIMES:
                case DIV:
                case MOD:
                {
                    op = this.token;
                    consume();
                }
                    
            }
            nextExpr = primaryExpression();

        }
        System.out.println("next Expr type: " +nextExpr);
        if (nextExpr == null){
            expression = primaryExpr;
            System.out.println("EXpression: "+expression);
        }
        else{
            expression = new ExpressionBinary(firstToken, primaryExpr, op, nextExpr);
        }
        return expression;

    }

    private Expression primaryExpression() throws SyntaxException, LexicalException{
        IToken firstToken = this.token;
        System.out.println("In primary exppression with token kind: "+ firstToken.getKind());
        Expression expression = null;
        // consume();
        System.out.println("In primary exppression with token kind after consume: "+ token.getKind());
        switch(token.getKind()){
            case IDENT:{
                expression =  new ExpressionIdent(firstToken);
                System.out.println("Inside ident primary expression got: " +expression);
                consume();
            }
            break;
            case NUM_LIT:
            case STRING_LIT:
            case BOOLEAN_LIT:{
                System.out.println("in caseprimary expression got bool type"+token.getKind());
                expression = constVal();
            }
            break;
            case LPAREN:{
                expression = expression();
                match(Kind.RPAREN);
            }
            break;

        }

    return expression;

    }

    private Expression constVal() throws LexicalException{
        IToken firstToken = this.token;
        Expression expression = null;
        System.out.println("in constVal num lit with token"+token.getKind());
        // consume();
        switch (token.getKind()){
            case NUM_LIT:{
                System.out.println("in case numlit inside consnt val with token"+token.getIntValue());
                consume();
                expression = new ExpressionNumLit(firstToken);
                System.out.println("Token after consumption: "+token.getKind());
            }
            break;
            case STRING_LIT:{
                System.out.println("Got string lit inside constVal");
                consume();
                expression = new ExpressionStringLit(firstToken);
            }
            break;
            case BOOLEAN_LIT:{
                consume();
                expression = new ExpressionBooleanLit(firstToken);
            }
            break;
            default:
                System.out.println("in default of constVal");
                // expression = new Expression(firstToken);
        }
        return expression;
    }



    // private Expression expressionBinary(){

    //     IToken firstToken = this.token;
    //     Expression expression =null;

    // }

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
