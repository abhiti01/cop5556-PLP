package edu.ufl.cise.plpfa22;

import edu.ufl.cise.plpfa22.ast.ASTVisitor;
import edu.ufl.cise.plpfa22.ast.Block;
import edu.ufl.cise.plpfa22.ast.ConstDec;
import edu.ufl.cise.plpfa22.ast.Expression;
import edu.ufl.cise.plpfa22.ast.ExpressionBinary;
import edu.ufl.cise.plpfa22.ast.ExpressionBooleanLit;
import edu.ufl.cise.plpfa22.ast.ExpressionIdent;
import edu.ufl.cise.plpfa22.ast.ExpressionNumLit;
import edu.ufl.cise.plpfa22.ast.ExpressionStringLit;
import edu.ufl.cise.plpfa22.ast.Ident;
import edu.ufl.cise.plpfa22.ast.ProcDec;
import edu.ufl.cise.plpfa22.ast.Program;
import edu.ufl.cise.plpfa22.ast.Statement;
import edu.ufl.cise.plpfa22.ast.StatementAssign;
import edu.ufl.cise.plpfa22.ast.StatementBlock;
import edu.ufl.cise.plpfa22.ast.StatementCall;
import edu.ufl.cise.plpfa22.ast.StatementEmpty;
import edu.ufl.cise.plpfa22.ast.StatementIf;
import edu.ufl.cise.plpfa22.ast.StatementInput;
import edu.ufl.cise.plpfa22.ast.StatementOutput;
import edu.ufl.cise.plpfa22.ast.StatementWhile;
import edu.ufl.cise.plpfa22.ast.VarDec;
import edu.ufl.cise.plpfa22.ast.Types.Type;

public class TypeChecker implements edu.ufl.cise.plpfa22.ast.ASTVisitor{

    @Override
    public Object visitBlock(Block block, Object arg) throws PLPException {

        if (block.constDecs.size()!=0){
            for(int i=0;i<block.constDecs.size();i++){
                visitConstDec(block.constDecs.get(i), null);
            }
        }
        if (block.varDecs.size()!= 0){
            for (int i =0; i<block.varDecs.size();i++){
                visitVarDec(block.varDecs.get(i), arg);
            }
        }
        // TODO Auto-generated method stub
        if(block.procedureDecs.size()!=0){
            for(int i=0;i<block.procedureDecs.size();i++){
                visitProcedure(block.procedureDecs.get(i),null);
            }
        
        }
        visitStatement(block.statement, null);
        return null;
    }
    public Object visitStatement(Statement s, Object arg) throws PLPException {
        if (s != null) {
            if (s instanceof StatementAssign) {
                visitStatementAssign((StatementAssign) s, arg);
            } else if (s instanceof StatementInput) {
                visitStatementInput((StatementInput) s, arg);
            } else if (s instanceof StatementOutput) {
                visitStatementOutput((StatementOutput) s, arg);
            } else if (s instanceof StatementBlock) {
                visitStatementBlock((StatementBlock) s, arg);
            } else if (s instanceof StatementIf) {
                visitStatementIf((StatementIf) s, arg);
            } else if (s instanceof StatementCall) {
                visitStatementCall((StatementCall) s, arg);
            } else if (s instanceof StatementWhile) {
                visitStatementWhile((StatementWhile) s, arg);
            } else {
                visitStatementEmpty((StatementEmpty) s, null);
            }
        }
        return null;
    }
    @Override
    public Object visitProgram(Program program, Object arg) throws PLPException {
        // TODO Auto-generated method stub
        if (program != null) {
            visitBlock(program.block, arg);
        }
        return null;
    }

    @Override
    public Object visitStatementAssign(StatementAssign statementAssign, Object arg) throws PLPException {
        // TODO Auto-generated method stub
        if (statementAssign.expression!=null){
            visitExpression(statementAssign.expression, null);
            Type rhsType = statementAssign.expression.getType();
            if (statementAssign.ident.getDec().getType() == null){
                statementAssign.ident.getDec().setType(rhsType);
            }
            else if (statementAssign.ident.getDec().getType() != rhsType){
                throw new TypeCheckException("Type mismatch in assignment");
            }
        }

        return null;
    }

    @Override
    public Object visitVarDec(VarDec varDec, Object arg) throws PLPException {
        // TODO Auto-generated method stub
        
        return null;
    }

    @Override
    public Object visitStatementCall(StatementCall statementCall, Object arg) throws PLPException {
        // TODO Auto-generated method stub
        System.out.println("Inside visitStatementCall");
        visitIdent(statementCall.ident, null);
        if (statementCall.ident.getDec().getType()!=Type.PROCEDURE){
            throw new TypeCheckException("Calling a non-procedure ident");
        }
        return null;
    }

    @Override
    public Object visitStatementInput(StatementInput statementInput, Object arg) throws PLPException {
        // TODO Auto-generated method stub
        visitIdent(statementInput.ident, null);
        if (statementInput.ident.getDec().getType()==Type.PROCEDURE || statementInput.ident.getDec().getType() == null){
            throw new TypeCheckException("Invalid type for StatementInput, should be Num, String or Bool");
        }
        return null;

    }

    @Override
    public Object visitStatementOutput(StatementOutput statementOutput, Object arg) throws PLPException {
        // TODO Auto-generated method stub
        visitExpression(statementOutput.expression, null);
        if (statementOutput.expression.getType()==Type.PROCEDURE || statementOutput.expression.getType() == null){
            throw new TypeCheckException("Invalid type for StatementInput, should be Num, String or Bool");
        }
        return null;
    }

    @Override
    public Object visitStatementBlock(StatementBlock statementBlock, Object arg) throws PLPException {
        // TODO Auto-generated method stub
        if(statementBlock.statements.size()!=0){
            for(int i=0;i<statementBlock.statements.size();i++){
                visitStatement(statementBlock.statements.get(i), null);
            }
        }
        return null;
    }

    @Override
    public Object visitStatementIf(StatementIf statementIf, Object arg) throws PLPException {
        // TODO Auto-generated method stub
        //statement must be correctly typed, check for that here?
        visitExpression(statementIf.expression, null);
        if(statementIf.expression.getType()!=Type.BOOLEAN){
            throw new TypeCheckException("Invalid type for StatementIf, should be Boolean");
        }
        visitStatement(statementIf.statement, null);
        return null;
    }

    @Override
    public Object visitStatementWhile(StatementWhile statementWhile, Object arg) throws PLPException {
        // TODO Auto-generated method stub
        visitExpression(statementWhile.expression, null);
        if(statementWhile.expression.getType()!=Type.BOOLEAN){
            throw new TypeCheckException("Invalid type for StatementWhile, should be Boolean");
        }
        visitStatement(statementWhile.statement, null);
        return null;
    }

    public Object visitExpression(Expression expression, Object arg) throws PLPException {
        if (expression != null) {
            if (expression instanceof ExpressionNumLit){
                visitExpressionNumLit((ExpressionNumLit)expression, null);
            }
            if (expression instanceof ExpressionStringLit){
                visitExpressionStringLit((ExpressionStringLit)expression, null);
            }
            if (expression instanceof ExpressionBooleanLit){
                visitExpressionBooleanLit((ExpressionBooleanLit)expression, null);
            }
            if (expression instanceof ExpressionIdent){
                visitExpressionIdent((ExpressionIdent)expression, null);
            }
            if (expression instanceof ExpressionBinary){
                visitExpressionBinary((ExpressionBinary)expression, null);
            }
        }
        return null;
    }

    @Override
    public Object visitExpressionBinary(ExpressionBinary expressionBinary, Object arg) throws PLPException {
        // TODO Auto-generated method stub
        switch(expressionBinary.op.getKind()){
        case PLUS:{
            visitExpression(expressionBinary.e0, null);
            visitExpression(expressionBinary.e1, null);
            if (expressionBinary.e0.getType() == Type.NUMBER && expressionBinary.e1.getType() == Type.NUMBER){
                expressionBinary.setType(Type.NUMBER);
            }
            else if (expressionBinary.e0.getType() == Type.STRING && expressionBinary.e1.getType() == Type.STRING){
                expressionBinary.setType(Type.STRING);
            }
            else if (expressionBinary.e0.getType() == Type.BOOLEAN && expressionBinary.e1.getType() == Type.BOOLEAN){
                expressionBinary.setType(Type.STRING);
            }
            else if(expressionBinary.e0.getType() == null && expressionBinary.e1.getType() != null){
                expressionBinary.e0.setType(expressionBinary.e1.getType());
                visitExpressionBinary(expressionBinary, arg);
            }
            else if (expressionBinary.e0.getType() != null && expressionBinary.e1.getType() == null){
                expressionBinary.e1.setType(expressionBinary.e0.getType());
                visitExpressionBinary(expressionBinary, arg);
            }
            else{
                throw new TypeCheckException("Invalid type for ExpressionBinary, unequal types of e0 and e1");
            }
        }
        break;
        case MINUS:
        case DIV:
        case MOD:{
            visitExpression(expressionBinary.e0, null);
            visitExpression(expressionBinary.e1, null);
            if (expressionBinary.e0.getType() == Type.NUMBER && expressionBinary.e1.getType() == Type.NUMBER){
                expressionBinary.setType(Type.NUMBER);
            }
            //unsure if these statements go here
            else if(expressionBinary.e0.getType() == null && expressionBinary.e1.getType() != null){
                expressionBinary.e0.setType(expressionBinary.e1.getType());
                visitExpressionBinary(expressionBinary, arg);
            }
            else if (expressionBinary.e0.getType() != null && expressionBinary.e1.getType() == null){
                expressionBinary.e1.setType(expressionBinary.e0.getType());
                visitExpressionBinary(expressionBinary, arg);
            }
            else{
                throw new TypeCheckException("Invalid type for ExpressionBinary, unequal types of e0 and e1");
            }
        }
        break;
        case TIMES:{
            visitExpression(expressionBinary.e0, null);
            visitExpression(expressionBinary.e1, null);
            if (expressionBinary.e0.getType() == Type.NUMBER && expressionBinary.e1.getType() == Type.NUMBER){
                expressionBinary.setType(Type.NUMBER);
            }
            else if (expressionBinary.e0.getType() == Type.BOOLEAN && expressionBinary.e1.getType() == Type.BOOLEAN){
                expressionBinary.setType(Type.STRING);
            }
            //unsure if these statements go here
            else if(expressionBinary.e0.getType() == null && expressionBinary.e1.getType() != null){
                expressionBinary.e0.setType(expressionBinary.e1.getType());
                visitExpressionBinary(expressionBinary, arg);
            }
            else if (expressionBinary.e0.getType() != null && expressionBinary.e1.getType() == null){
                expressionBinary.e1.setType(expressionBinary.e0.getType());
                visitExpressionBinary(expressionBinary, arg);
            }
            else{
                throw new TypeCheckException("Invalid type for ExpressionBinary, unequal types of e0 and e1");
            }
        }
        break;
        case EQ:
        case NEQ:
        case LT:
        case GT:
        case LE:
        case GE:{
            visitExpression(expressionBinary.e0, null);
            visitExpression(expressionBinary.e1, null);
            if (expressionBinary.e0.getType() == Type.NUMBER && expressionBinary.e1.getType() == Type.NUMBER){
                expressionBinary.setType(Type.BOOLEAN);
            }
            else if (expressionBinary.e0.getType() == Type.STRING && expressionBinary.e1.getType() == Type.STRING){
                expressionBinary.setType(Type.BOOLEAN);
            }
            else if (expressionBinary.e0.getType() == Type.BOOLEAN && expressionBinary.e1.getType() == Type.BOOLEAN){
                expressionBinary.setType(Type.BOOLEAN);
            }
            //unsure if these statements go here
            else if(expressionBinary.e0.getType() == null && expressionBinary.e1.getType() != null){
                expressionBinary.e0.setType(expressionBinary.e1.getType());
                visitExpressionBinary(expressionBinary, arg);
            }
            else if (expressionBinary.e0.getType() != null && expressionBinary.e1.getType() == null){
                expressionBinary.e1.setType(expressionBinary.e0.getType());
                visitExpressionBinary(expressionBinary, arg);
            }
            else{
                throw new TypeCheckException("Invalid type for ExpressionBinary, unequal types of e0 and e1");
            }
        }
        break;
        default:
            throw new TypeCheckException("Invalid op for ExpressionBinary, should be PLUS, MINUS, TIMES, DIV, MOD, EQ, NEQ, LT, GT, LE, GE");
        }
        return null;
    }

    @Override
    public Object visitExpressionIdent(ExpressionIdent expressionIdent, Object arg) throws PLPException {
        // TODO Auto-generated method stub
        expressionIdent.setType(expressionIdent.getDec().getType());
        return null;
    }

    @Override
    public Object visitExpressionNumLit(ExpressionNumLit expressionNumLit, Object arg) throws PLPException {
        // TODO Auto-generated method stub
        expressionNumLit.setType(Type.NUMBER);
        return null;
    }

    @Override
    public Object visitExpressionStringLit(ExpressionStringLit expressionStringLit, Object arg) throws PLPException {
        // TODO Auto-generated method stub
        expressionStringLit.setType(Type.STRING);
        return null;
    }

    @Override
    public Object visitExpressionBooleanLit(ExpressionBooleanLit expressionBooleanLit, Object arg) throws PLPException {
        // TODO Auto-generated method stub
        expressionBooleanLit.setType(Type.BOOLEAN);
        return null;
    }

    @Override
    public Object visitProcedure(ProcDec procDec, Object arg) throws PLPException {
        // TODO Auto-generated method stub
        procDec.setType(Type.PROCEDURE);
        return null;
    }

    @Override
    public Object visitConstDec(ConstDec constDec, Object arg) throws PLPException {
        System.out.println("In visitConstdec");
        System.out.println("got value: "+constDec.val);
        // TODO Auto-generated method stub
        Object gotVal = constDec.val;
        if (gotVal instanceof Integer)
            constDec.setType(Type.NUMBER);
        if (gotVal instanceof String)
            constDec.setType(Type.STRING);  
        if (gotVal instanceof Boolean)
            constDec.setType(Type.BOOLEAN);
        return null;
    }

    @Override
    public Object visitStatementEmpty(StatementEmpty statementEmpty, Object arg) throws PLPException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Object visitIdent(Ident ident, Object arg) throws PLPException {
        // TODO Auto-generated method stub

        return null;
    }
    
}
