package edu.ufl.cise.plpfa22.ast;

import java.util.ArrayList;
import java.util.Stack;
import java.util.HashMap;
import java.util.Random;

public class SymbolTable {
    Stack<Integer> scopeStack = new Stack<Integer>();
    HashMap<String, ArrayList<SpecialStructureTBD>> hashing = new HashMap<String, ArrayList<SpecialStructureTBD>>();
    int currentScope;
    // int scopeId;
    int nextScope;
    public SymbolTable() {
        this.scopeStack = new Stack<Integer>();
        this.hashing = new HashMap<String, ArrayList<SpecialStructureTBD>>();
        // this.scopeId = 100;
        this.currentScope = -1;
        this.nextScope = 0;
        scopeStack.push(currentScope);
    }
    // public int getScopeId(){
    //     return scopeId;
    // }
    // public void setScopeId(int id){
    //     this.scopeId = id;
    // }
    public int getCurrentScope() {
        return currentScope;
    }

    public void enterScope() {
        // currentScope = nextScope++;
        currentScope = nextScope;
        Random random = new Random();
        nextScope = random.nextInt(99999999)+currentScope;
        scopeStack.push(currentScope);
        System.out.println(scopeStack);
        System.out.println("curr scope"+ currentScope);
    }

    public void exitScope() {
        scopeStack.pop();
        currentScope = scopeStack.peek();
        // nextScope = currentScope+1;
        Random random = new Random();
        nextScope = random.nextInt(99999999)+currentScope;
        System.out.println(scopeStack);
        System.out.println("curr scope in exitscope"+ currentScope);
    }

    // public void pushDeclaration(String ident, Declaration dec, int scopeId)
    public void pushDeclaration(String ident, Declaration dec) {
        if (hashing.containsKey(ident)) {
            ArrayList<SpecialStructureTBD> newDec = hashing.get(ident);
            // newDec.add(new SpecialStructureTBD(currentScope, dec, scopeId));
            newDec.add(new SpecialStructureTBD(currentScope, dec));
            hashing.put(ident, newDec);
        } else {
            ArrayList<SpecialStructureTBD> newDec = new ArrayList<>();
            // newDec.add(new SpecialStructureTBD(currentScope, dec,scopeId));
            newDec.add(new SpecialStructureTBD(currentScope, dec));
            hashing.put(ident, newDec);
        }
    }

    public Declaration lookup(String ident) {
        ArrayList<SpecialStructureTBD> l = hashing.get(ident);
        if (l == null) {
            return null;
        }

        Declaration dec = null;
        // int delta = Integer.MAX_VALUE;
        for (SpecialStructureTBD p : l) {
            if (p.getScope() == currentScope && scopeStack.contains(p.getScope())){
            // && scopeStack.contains(p.getScope())) {
                dec = p.getDecVal();
                // if(p.getScopeId() != scopeId){
                //     dec = p.getDecVal();
                // }
                // && currentScope - p.getDecVal() <= delta)
                // delta = currentScope - p.getScope();
            }
            if (p.getScope()<currentScope && scopeStack.contains(p.getScope())){
                dec = p.getDecVal();
            }
        }
        return dec;
    }

    public boolean existsInScope(String ident) {
        ArrayList<SpecialStructureTBD> name = hashing.get(ident);
        if (name == null) {
            return false;
        }
        for (SpecialStructureTBD existing : name) {
            if (existing.getScope() == currentScope) {

                return true;
            }
            // if (existing.getScope() == currentScope && existing.getScopeId() == scopeId) {

            //     return true;
            // }
        }
        return false;
    }

    class SpecialStructureTBD {
        int scope;
        // int scopeId;
        Declaration dec;
        // int scopeNumber;
        // public SpecialStructureTBD(int scope, Declaration dec, int scopeId)
        public SpecialStructureTBD(int scope, Declaration dec) {
            this.scope = scope;
            this.dec = dec;
            // this.scopeNumber = scopeNumber;
            // this.scopeId = scopeId;
        }

        public int getScope() {
            return scope;
        }
        // public int getScopeId(){
        //     return scopeId;
        // }
        public Declaration getDecVal() {
            return dec;
        }

    }
}
