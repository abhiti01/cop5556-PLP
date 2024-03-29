/**  This code is provided for solely for use of students in the course COP5556 Programming Language Principles at the 
 * University of Florida during the Fall Semester 2022 as part of the course project.  No other use is authorized. 
 */

package edu.ufl.cise.plpfa22.ast;

import edu.ufl.cise.plpfa22.IToken;
import edu.ufl.cise.plpfa22.ast.Types.Type;

public abstract class Declaration extends ASTNode {
	int nest;
	Type type;
	String procLocation = "";
	String parentProcLocation = "";

	public void setNest(int nest) {
		this.nest = nest;
	}

	public int getNest() {
		return nest;
	}

	public Declaration(IToken firstToken) {
		super(firstToken);
	}

	public Type getType() {
		return type;
	}

	public void setType(Type type) {
		this.type = type;
	}

	public String getProcLocation() {
		return procLocation;
	}

	public void setProcLocation(String procLocation) {
		this.procLocation = procLocation;
	}

	public String getParentProcLocation() {
		return parentProcLocation;
	}

	public void setParentProcLocation(String parentProcLocation) {
		this.parentProcLocation = parentProcLocation;
	}
}
