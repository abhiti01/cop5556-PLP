package edu.ufl.cise.plpfa22;

import edu.ufl.cise.plpfa22.ast.ASTVisitor;
import edu.ufl.cise.plpfa22.ast.Block;
import edu.ufl.cise.plpfa22.ast.ConstDec;
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
        if (statementCall.ident.getDec().getType()!=Type.PROCEDURE){
            throw new TypeCheckException("Calling a non-procedure ident");
        }
        return null;
    }

    @Override
    public Object visitStatementInput(StatementInput statementInput, Object arg) throws PLPException {
        // TODO Auto-generated method stub
        if (statementInput.ident.getDec().getType()==Type.PROCEDURE || statementInput.ident.getDec().getType() == null){
            throw new TypeCheckException("Invalid type for StatementInput, should be Num, String or Bool");
        }
        return null;

    }

    @Override
    public Object visitStatementOutput(StatementOutput statementOutput, Object arg) throws PLPException {
        // TODO Auto-generated method stub
        // if (statementOutput.ident.getDec().getType()==Type.PROCEDURE || statementInput.ident.getDec().getType() == null){
        //     throw new TypeCheckException("Invalid type for StatementInput, should be Num, String or Bool");
        // }
        return null;
    }

    @Override
    public Object visitStatementBlock(StatementBlock statementBlock, Object arg) throws PLPException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Object visitStatementIf(StatementIf statementIf, Object arg) throws PLPException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Object visitStatementWhile(StatementWhile statementWhile, Object arg) throws PLPException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Object visitExpressionBinary(ExpressionBinary expressionBinary, Object arg) throws PLPException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Object visitExpressionIdent(ExpressionIdent expressionIdent, Object arg) throws PLPException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Object visitExpressionNumLit(ExpressionNumLit expressionNumLit, Object arg) throws PLPException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Object visitExpressionStringLit(ExpressionStringLit expressionStringLit, Object arg) throws PLPException {
        // TODO Auto-generated method stub
        return null;
    }

    @Override
    public Object visitExpressionBooleanLit(ExpressionBooleanLit expressionBooleanLit, Object arg) throws PLPException {
        // TODO Auto-generated method stub
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
