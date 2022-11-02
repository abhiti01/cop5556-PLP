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

public class TypeChecker implements edu.ufl.cise.plpfa22.ast.ASTVisitor {

    @Override
    public Object visitBlock(Block block, Object arg) throws PLPException {
        int changesInPrevRun = 0;
        Boolean flag = true;
        Boolean debug = false;
        while (flag) {
            int changesInThisRun = 0;
            if (block.constDecs.size() != 0) {
                for (int i = 0; i < block.constDecs.size(); i++) {
                    changesInThisRun = changesInThisRun + (Integer) visitConstDec(block.constDecs.get(i), debug);
                }
            }
            if (block.varDecs.size() != 0) {
                for (int i = 0; i < block.varDecs.size(); i++) {
                    visitVarDec(block.varDecs.get(i), debug);
                }
            }
            // TODO Auto-generated method stub
            if (block.procedureDecs.size() != 0) {
                for (int i = 0; i < block.procedureDecs.size(); i++) {
                    changesInThisRun = changesInThisRun + (Integer) visitProcedure(block.procedureDecs.get(i), debug);
                }

            }
            visitStatement(block.statement, debug);
            // System.out.println("done with a run got count: " + changesInThisRun);
            if (changesInThisRun == 0 || changesInPrevRun == changesInThisRun) {
                flag = false;
                debug = true;
            } else {
                changesInPrevRun = changesInThisRun;
            }
        }
        if (arg == null) {
            if (block.constDecs.size() != 0) {
                for (int i = 0; i < block.constDecs.size(); i++) {
                    visitConstDec(block.constDecs.get(i), debug);
                }
            }
            if (block.varDecs.size() != 0) {
                for (int i = 0; i < block.varDecs.size(); i++) {
                    visitVarDec(block.varDecs.get(i), debug);
                }
            }
            // TODO Auto-generated method stub
            if (block.procedureDecs.size() != 0) {
                for (int i = 0; i < block.procedureDecs.size(); i++) {
                    visitProcedure(block.procedureDecs.get(i), debug);
                }

            }
            visitStatement(block.statement, debug);
        }
        return null;
    }

    public Object visitStatement(Statement s, Object arg) throws PLPException {
        int count = 0;
        if (s != null) {
            if (s instanceof StatementAssign) {
                count += (Integer) visitStatementAssign((StatementAssign) s, arg);
            } else if (s instanceof StatementInput) {
                visitStatementInput((StatementInput) s, arg);
            } else if (s instanceof StatementOutput) {
                visitStatementOutput((StatementOutput) s, arg);
            } else if (s instanceof StatementBlock) {
                count += (Integer) visitStatementBlock((StatementBlock) s, arg);
            } else if (s instanceof StatementIf) {
                count += (Integer) visitStatementIf((StatementIf) s, arg);
            } else if (s instanceof StatementCall) {
                visitStatementCall((StatementCall) s, arg);
            } else if (s instanceof StatementWhile) {
                count += (Integer) visitStatementWhile((StatementWhile) s, arg);
            } else {
                visitStatementEmpty((StatementEmpty) s, arg);
            }
        }
        return count;
    }

    @Override
    public Object visitProgram(Program program, Object arg) throws PLPException {
        // TODO Auto-generated method stub
        if (program != null) {
            visitBlock(program.block, null);
        }
        return null;
    }

    @Override
    public Object visitStatementAssign(StatementAssign statementAssign, Object arg) throws PLPException {
        // TODO Auto-generated method stub
        int count = 0;
        
        if (statementAssign.expression != null) {
            if ((statementAssign.ident.getDec() instanceof ConstDec
                    || statementAssign.ident.getDec() instanceof ProcDec) && (Boolean) arg) {
                throw new TypeCheckException("cannot reassign const");
            }
            count += (Integer) visitExpression(statementAssign.expression, arg);
            Type rhsType = statementAssign.expression.getType();
            if (statementAssign.ident.getDec().getType() == null) {
                statementAssign.ident.getDec().setType(rhsType);
                ++count;
            } else if (rhsType == null && statementAssign.ident.getDec().getType() != null) {
                statementAssign.expression.setType(statementAssign.ident.getDec().getType());

            } else if(statementAssign.ident.getDec().getType()!=null && rhsType!=null && statementAssign.ident.getDec().getType() != rhsType) {
                throw new TypeCheckException("type mismatch");
            }

            else if (statementAssign.ident.getDec().getType() != rhsType && (Boolean) arg) {
                throw new TypeCheckException("Type mismatch in assignment");
            }
            System.out.println("IN CASE Statement assign lhs: " +String.valueOf(statementAssign.ident.getFirstToken().getText())+" "+ statementAssign.ident.getDec().getType()+ "AND OF Rhs "
                        + String.valueOf(statementAssign.expression.getFirstToken().getText()) +" " + statementAssign.expression.getType());
        }

        // System.out.println("completed statement assign got count " + count);
        return count;
    }

    @Override
    public Object visitVarDec(VarDec varDec, Object arg) throws PLPException {
        // TODO Auto-generated method stub

        return null;
    }

    @Override
    public Object visitStatementCall(StatementCall statementCall, Object arg) throws PLPException {
        // TODO Auto-generated method stub
        visitIdent(statementCall.ident, arg);
        // System.out.println("Inside visit statement call got ident:" +
        // statementCall.ident.getFirstToken()
        // + "with type: " + statementCall.ident
        // .getDec().getType()
        // + " Debug is set to:" + (Boolean) arg);
        if (statementCall.ident.getDec().getType() != Type.PROCEDURE && (Boolean) arg) {
            throw new TypeCheckException("Calling a non-procedure ident");
        }
        return null;
    }

    @Override
    public Object visitStatementInput(StatementInput statementInput, Object arg) throws PLPException {
        // TODO Auto-generated method stub
        visitIdent(statementInput.ident, arg);
        if ((statementInput.ident.getDec().getType() == Type.PROCEDURE
                || statementInput.ident.getDec().getType() == null || statementInput.ident.getDec() instanceof ConstDec) && (Boolean) arg) {
            throw new TypeCheckException("Invalid type for StatementInput, should be Num, String or Bool");
        }
        return null;

    }

    @Override
    public Object visitStatementOutput(StatementOutput statementOutput, Object arg) throws PLPException {
        // TODO Auto-generated method stub
        visitExpression(statementOutput.expression, arg);
        if ((statementOutput.expression.getType() == Type.PROCEDURE || statementOutput.expression.getType() == null)
                && (Boolean) arg) {
            throw new TypeCheckException("Invalid type for StatementInput, should be Num, String or Bool");
        }
        return null;
    }

    @Override
    public Object visitStatementBlock(StatementBlock statementBlock, Object arg) throws PLPException {
        int count = 0;
        // TODO Auto-generated method stub
        if (statementBlock.statements.size() != 0) {
            for (int i = 0; i < statementBlock.statements.size(); i++) {
                count += (Integer) visitStatement(statementBlock.statements.get(i), arg);
            }
        }
        return count;
    }

    @Override
    public Object visitStatementIf(StatementIf statementIf, Object arg) throws PLPException {
        // TODO Auto-generated method stub
        // statement must be correctly typed, check for that here?
        // System.out.println("Inside statementIf, got expression: " +
        // statementIf.expression);
        int count = 0;
        count += (Integer) visitExpression(statementIf.expression, arg);
        if (statementIf.expression.getType() != Type.BOOLEAN && (Boolean) arg) {
            throw new TypeCheckException("Invalid type for StatementIf, should be Boolean");
        }
        count += (Integer) visitStatement(statementIf.statement, arg);
        return count;
    }

    @Override
    public Object visitStatementWhile(StatementWhile statementWhile, Object arg) throws PLPException {
        // TODO Auto-generated method stub
        int count = 0;
        count += (Integer) visitExpression(statementWhile.expression, arg);
        // System.out.println("visited while expr, type set to:" +
        // statementWhile.expression.getType());
        if (statementWhile.expression.getType() != Type.BOOLEAN && (Boolean) arg) {
            throw new TypeCheckException("Invalid type for StatementWhile, should be Boolean");
        }
        count += (Integer) visitStatement(statementWhile.statement, arg);
        return count;
    }

    public Object visitExpression(Expression expression, Object arg) throws PLPException {
        int count = 0;
        if (expression != null) {
            if (expression instanceof ExpressionNumLit) {
                count += (Integer) visitExpressionNumLit((ExpressionNumLit) expression, arg);
            }
            if (expression instanceof ExpressionStringLit) {
                count += (Integer) visitExpressionStringLit((ExpressionStringLit) expression, arg);
            }
            if (expression instanceof ExpressionBooleanLit) {
                count += (Integer) visitExpressionBooleanLit((ExpressionBooleanLit) expression, arg);
            }
            if (expression instanceof ExpressionIdent) {
                count += (Integer) visitExpressionIdent((ExpressionIdent) expression, arg);
            }
            if (expression instanceof ExpressionBinary) {
                count += (Integer) visitExpressionBinary((ExpressionBinary) expression, arg);
            }
        }
        return count;
    }

    @Override
    public Object visitExpressionBinary(ExpressionBinary expressionBinary, Object arg) throws PLPException {
        // TODO Auto-generated method stub
        int count = 0;
        switch (expressionBinary.op.getKind()) {
            case PLUS: {
                
                count += (Integer) visitExpression(expressionBinary.e0, arg);
                count += (Integer) visitExpression(expressionBinary.e1, arg);
                if (expressionBinary.e0.getType() == Type.NUMBER && expressionBinary.e1.getType() == Type.NUMBER) {
                    expressionBinary.setType(Type.NUMBER);
                    ++count;
                } else if (expressionBinary.e0.getType() == Type.STRING
                        && expressionBinary.e1.getType() == Type.STRING) {
                    expressionBinary.setType(Type.STRING);
                    ++count;
                } else if (expressionBinary.e0.getType() == Type.BOOLEAN
                        && expressionBinary.e1.getType() == Type.BOOLEAN) {
                    expressionBinary.setType(Type.BOOLEAN);
                    ++count;
                } else if (expressionBinary.e0.getType() == null && expressionBinary.e1.getType() != null) {
                    expressionBinary.e0.setType(expressionBinary.e1.getType());
                    if(expressionBinary.e0 instanceof ExpressionIdent && ((ExpressionIdent)expressionBinary.e0).getDec().getType() == null){
                        ((ExpressionIdent)expressionBinary.e0).getDec().setType(expressionBinary.e1.getType());
                    }
                    expressionBinary.setType(expressionBinary.e1.getType());
                    ++count;
                } else if (expressionBinary.e0.getType() != null && expressionBinary.e1.getType() == null) {
                    expressionBinary.e1.setType(expressionBinary.e0.getType());
                    if(expressionBinary.e1 instanceof ExpressionIdent && ((ExpressionIdent)expressionBinary.e1).getDec().getType() == null){
                        ((ExpressionIdent)expressionBinary.e1).getDec().setType(expressionBinary.e0.getType());
                    }
                    expressionBinary.setType(expressionBinary.e0.getType());
                    ++count;
                    // visitExpressionBinary(expressionBinary, arg);
                } 
                else if(expressionBinary.e0.getType() == null && expressionBinary.e1.getType() == null && expressionBinary.getType()!=null){
                    expressionBinary.e0.setType(expressionBinary.getType());
                    if(expressionBinary.e0 instanceof ExpressionIdent && ((ExpressionIdent)expressionBinary.e0).getDec().getType() == null){
                        ((ExpressionIdent)expressionBinary.e0).getDec().setType(expressionBinary.e0.getType());
                    }
                    ++count;
                    expressionBinary.e1.setType(expressionBinary.getType());
                    if(expressionBinary.e1 instanceof ExpressionIdent && ((ExpressionIdent)expressionBinary.e1).getDec().getType() == null){
                        ((ExpressionIdent)expressionBinary.e1).getDec().setType(expressionBinary.e1.getType());
                    }
                    ++count;
                }
                else {
                    if ((Boolean) arg)
                        throw new TypeCheckException(
                                "Invalid type for ExpressionBinary PLUS, unequal types of e0 and e1 at "
                                        + String.valueOf(expressionBinary.e0.getFirstToken().getText()) + " "
                                        + String.valueOf(expressionBinary.e1.getFirstToken().getText()));
                }
                System.out.println("IN CASE PLUS, SET TYPES OF EXPR0 " +String.valueOf(expressionBinary.e0.getFirstToken().getText())+" "+ expressionBinary.e0.getType() + "AND OF E1 "
                        + expressionBinary.e1.getType() +" " +String.valueOf(expressionBinary.e1.getFirstToken().getText()));
            }
                break;
            case MINUS:
            case DIV:
            case MOD: {
                count += (Integer) visitExpression(expressionBinary.e0, arg);
                count += (Integer) visitExpression(expressionBinary.e1, arg);
                if (expressionBinary.e0.getType() == Type.NUMBER && expressionBinary.e1.getType() == Type.NUMBER) {
                    expressionBinary.setType(Type.NUMBER);
                    ++count;
                }
                // unsure if these statements go here
                else if (expressionBinary.e0.getType() == null && expressionBinary.e1.getType() != null) {
                    expressionBinary.e0.setType(expressionBinary.e1.getType());
                    if(expressionBinary.e0 instanceof ExpressionIdent && ((ExpressionIdent)expressionBinary.e0).getDec().getType() == null){
                        ((ExpressionIdent)expressionBinary.e0).getDec().setType(expressionBinary.e1.getType());
                    }
                    expressionBinary.setType(expressionBinary.e1.getType());
                    ++count;
                    // visitExpressionBinary(expressionBinary, arg);
                } else if (expressionBinary.e0.getType() != null && expressionBinary.e1.getType() == null) {
                    expressionBinary.e1.setType(expressionBinary.e0.getType());
                    if(expressionBinary.e1 instanceof ExpressionIdent && ((ExpressionIdent)expressionBinary.e1).getDec().getType() == null){
                        ((ExpressionIdent)expressionBinary.e1).getDec().setType(expressionBinary.e0.getType());
                    }
                    expressionBinary.setType(expressionBinary.e0.getType());
                    ++count;
                    // visitExpressionBinary(expressionBinary, arg);
                } 
                else if(expressionBinary.e0.getType() == null && expressionBinary.e1.getType() == null && expressionBinary.getType()!=null){
                    expressionBinary.e0.setType(expressionBinary.getType());
                    if(expressionBinary.e0 instanceof ExpressionIdent && ((ExpressionIdent)expressionBinary.e0).getDec().getType() == null){
                        ((ExpressionIdent)expressionBinary.e0).getDec().setType(expressionBinary.e0.getType());
                    }
                    ++count;
                    expressionBinary.e1.setType(expressionBinary.getType());
                    if(expressionBinary.e1 instanceof ExpressionIdent && ((ExpressionIdent)expressionBinary.e1).getDec().getType() == null){
                        ((ExpressionIdent)expressionBinary.e1).getDec().setType(expressionBinary.e1.getType());
                    }
                    ++count;
                }
                else {
                    if ((Boolean) arg)
                        throw new TypeCheckException(
                                "Invalid type for ExpressionBinary MINUS DIV MOD, unequal types of e0 and e1 at "
                                        + expressionBinary.e0.getSourceLocation() + " "
                                        + expressionBinary.e1.getSourceLocation());
                }
                System.out.println(
                        "IN CASE MINUS DIV MOD, SET TYPES OF EXPR0 " + expressionBinary.e0.getType()+String.valueOf(expressionBinary.e0.getFirstToken().getText())+" " + "AND OF E1 "
                                + expressionBinary.e1.getType()+" "+String.valueOf(expressionBinary.e1.getFirstToken().getText()));
            }
                break;
            case TIMES: {
                // add ccase to throw error if e0 or e1 is string
                count += (Integer) visitExpression(expressionBinary.e0, arg);
                count += (Integer) visitExpression(expressionBinary.e1, arg);
                if (expressionBinary.e0.getType() == Type.NUMBER && expressionBinary.e1.getType() == Type.NUMBER) {
                    expressionBinary.setType(Type.NUMBER);
                    ++count;
                } else if (expressionBinary.e0.getType() == Type.BOOLEAN
                        && expressionBinary.e1.getType() == Type.BOOLEAN) {
                    expressionBinary.setType(Type.BOOLEAN);
                    ++count;
                }
                // unsure if these statements go here
                else if (expressionBinary.e0.getType() == null && expressionBinary.e1.getType() != null) {
                    expressionBinary.e0.setType(expressionBinary.e1.getType());
                    if (expressionBinary.e0 instanceof ExpressionIdent && ((ExpressionIdent)expressionBinary.e0).getDec().getType() == null) {
                        ((ExpressionIdent) expressionBinary.e0).getDec().setType(expressionBinary.e1.getType());
                    }
                    expressionBinary.setType(expressionBinary.e1.getType());
                    ++count;
                    // visitExpressionBinary(expressionBinary, arg);
                } else if (expressionBinary.e0.getType() != null && expressionBinary.e1.getType() == null) {
                    expressionBinary.e1.setType(expressionBinary.e0.getType());
                    if (expressionBinary.e1 instanceof ExpressionIdent && ((ExpressionIdent)expressionBinary.e1).getDec().getType() == null) {
                        ((ExpressionIdent) expressionBinary.e1).getDec().setType(expressionBinary.e0.getType());
                    }
                    expressionBinary.setType(expressionBinary.e0.getType());
                    ++count;
                    // visitExpressionBinary(expressionBinary, arg);
                } 
                else if(expressionBinary.e0.getType() == null && expressionBinary.e1.getType() == null && expressionBinary.getType()!=null){
                    expressionBinary.e0.setType(expressionBinary.getType());
                    if(expressionBinary.e0 instanceof ExpressionIdent && ((ExpressionIdent)expressionBinary.e0).getDec().getType() == null){
                        ((ExpressionIdent)expressionBinary.e0).getDec().setType(expressionBinary.e0.getType());
                    }
                    ++count;
                    expressionBinary.e1.setType(expressionBinary.getType());
                    if(expressionBinary.e1 instanceof ExpressionIdent && ((ExpressionIdent)expressionBinary.e1).getDec().getType() == null){
                        ((ExpressionIdent)expressionBinary.e1).getDec().setType(expressionBinary.e1.getType());
                    }
                    ++count;
                }
                else {
                    if ((Boolean) arg)
                        throw new TypeCheckException(
                                "Invalid type for ExpressionBinary TIMES, unequal types of e0 and e1 at "
                                        + expressionBinary.e0.getSourceLocation() + " "
                                        + expressionBinary.e1.getSourceLocation());
                }
                System.out.println(
                        "IN CASE TIMES SET TYPES OF EXPR0 " + expressionBinary.e0.getType() +" "+String.valueOf(expressionBinary.e0.getFirstToken().getText())+ "AND OF E1 "
                                + expressionBinary.e1.getType());
            }
                break;
            case EQ:
            case NEQ:
            case LT:
            case GT:
            case LE:
            case GE: {
                count += (Integer) visitExpression(expressionBinary.e0, arg);
                count += (Integer) visitExpression(expressionBinary.e1, arg);
                if ((expressionBinary.e0.getType() == Type.NUMBER && expressionBinary.e1.getType() == Type.NUMBER)
                        || (expressionBinary.e0
                                .getType() == Type.STRING
                                && expressionBinary.e1.getType() == Type.STRING)
                        || (expressionBinary.e0.getType() == Type.BOOLEAN
                                && expressionBinary.e1.getType() == Type.BOOLEAN)) {
                    expressionBinary.setType(Type.BOOLEAN);
                    ++count;
                } else if (expressionBinary.e0.getType() == null && expressionBinary.e1.getType() != null) {
                    expressionBinary.e0.setType(expressionBinary.e1.getType());
                    if(expressionBinary.e0 instanceof ExpressionIdent&& ((ExpressionIdent)expressionBinary.e0).getDec().getType() == null){
                        ((ExpressionIdent)expressionBinary.e0).getDec().setType(expressionBinary.e1.getType());
                    }
                    expressionBinary.setType(Type.BOOLEAN);
                    ++count;
                } else if (expressionBinary.e0.getType() != null && expressionBinary.e1.getType() == null) {
                    expressionBinary.e1.setType(expressionBinary.e0.getType());
                    if(expressionBinary.e1 instanceof ExpressionIdent && ((ExpressionIdent)expressionBinary.e1).getDec().getType() == null){
                        ((ExpressionIdent)expressionBinary.e1).getDec().setType(expressionBinary.e0.getType());
                    }
                    expressionBinary.setType(Type.BOOLEAN);
                    ++count;
                } 
                // else if(expressionBinary.e0.getType() == null && expressionBinary.e1.getType() == null && expressionBinary.getType()!=null){
                //     expressionBinary.e0.setType(expressionBinary.getType());
                //     if(expressionBinary.e0 instanceof ExpressionIdent && ((ExpressionIdent)expressionBinary.e0).getDec().getType() == null){
                //         ((ExpressionIdent)expressionBinary.e0).getDec().setType(expressionBinary.e0.getType());
                //     }
                //     ++count;
                //     expressionBinary.e1.setType(expressionBinary.getType());
                //     if(expressionBinary.e1 instanceof ExpressionIdent && ((ExpressionIdent)expressionBinary.e1).getDec().getType() == null){
                //         ((ExpressionIdent)expressionBinary.e1).getDec().setType(expressionBinary.e1.getType());
                //     }
                //     ++count;
                // }
                else {
                    if ((boolean) arg)
                        throw new TypeCheckException(
                                "Invalid type for ExpressionBinary EQ NEQ, unequal types of e0 and e1 at "
                                        + expressionBinary.e0.getSourceLocation() + " "
                                        + expressionBinary.e1.getSourceLocation());
                }
                System.out.println(
                        "IN CASE EQ,NEQ,LT SET TYPES OF EXPR0 " + expressionBinary.e0.getType()+" "+String.valueOf(expressionBinary.e0.getFirstToken().getText()) + "AND OF E1 "
                                + expressionBinary.e1.getType()+" "+String.valueOf(expressionBinary.e1.getFirstToken().getText()));
            }
                break;
            default:
                if ((Boolean) arg)
                    throw new TypeCheckException(
                            "Invalid op for ExpressionBinary, should be PLUS, MINUS, TIMES, DIV, MOD, EQ, NEQ, LT, GT, LE, GE");
        }
        return count;
    }

    @Override
    public Object visitExpressionIdent(ExpressionIdent expressionIdent, Object arg) throws PLPException {
        // TODO Auto-generated method stub
        int count = 0;

        if (expressionIdent.getType() == null) {
            expressionIdent.setType(expressionIdent.getDec().getType());
            ++count;
        }
        if ((boolean) arg && expressionIdent.getType() != expressionIdent.getDec().getType() && expressionIdent.getDec().getType() != null) {
            throw new TypeCheckException("BYE");
        }
        // String name = String.valueOf(expressionIdent.getFirstToken().getText());
        // System.out.println("Got expression ident with name: "+name+"Set type to: "+expressionIdent.getDec().getType());
        return count;
    }

    @Override
    public Object visitExpressionNumLit(ExpressionNumLit expressionNumLit, Object arg) throws PLPException {
        // TODO Auto-generated method stub
        int count = 0;
        if (expressionNumLit.getType() == null) {
            expressionNumLit.setType(Type.NUMBER);
            ++count;
        }

        return count;
    }

    @Override
    public Object visitExpressionStringLit(ExpressionStringLit expressionStringLit, Object arg) throws PLPException {
        // TODO Auto-generated method stub
        int count = 0;
        if (expressionStringLit.getType() == null) {
            expressionStringLit.setType(Type.STRING);
            ++count;
        }

        return count;
    }

    @Override
    public Object visitExpressionBooleanLit(ExpressionBooleanLit expressionBooleanLit, Object arg) throws PLPException {
        // TODO Auto-generated method stub
        int count = 0;
        if (expressionBooleanLit.getType() == null) {
            expressionBooleanLit.setType(Type.BOOLEAN);
            ++count;
        }
        return count;
    }

    @Override
    public Object visitProcedure(ProcDec procDec, Object arg) throws PLPException {
        int count = 0;
        // TODO Auto-generated method stub
        if (procDec.getType() == null) {
            procDec.setType(Type.PROCEDURE);
            ++count;
        }
        if ((Boolean) arg) {
            visitBlock(procDec.block, null);
        } else {
            visitBlock(procDec.block, "debug");
        }

        return count;
    }

    @Override
    public Object visitConstDec(ConstDec constDec, Object arg) throws PLPException {
        int count = 0;

        // System.out.println("In visitConstdec");
        // System.out.println("got value: " + constDec.val);
        // TODO Auto-generated method stub
        Object gotVal = constDec.val;
        if (constDec.getType() == null) {
            if (gotVal instanceof Integer) {
                constDec.setType(Type.NUMBER);
                count += 1;
            }

            if (gotVal instanceof String) {
                constDec.setType(Type.STRING);
                count += 1;
            }

            if (gotVal instanceof Boolean) {
                constDec.setType(Type.BOOLEAN);
                count += 1;
            }

        }

        return count;
    }

    @Override
    public Object visitStatementEmpty(StatementEmpty statementEmpty, Object arg) throws PLPException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Object visitIdent(Ident ident, Object arg) throws PLPException {
        // TODO Auto-generated method stub
        // if (ident.getDec() instanceof VarDec) {

        // }
        return null;
    }

}
