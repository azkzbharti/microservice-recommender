package com.ibm.research.msr.jarlist;

import java.util.List;

import org.codehaus.groovy.ast.CodeVisitorSupport;
import org.codehaus.groovy.ast.expr.ConstantExpression;
import org.codehaus.groovy.ast.expr.MapEntryExpression;
import org.codehaus.groovy.ast.expr.MapExpression;
import org.codehaus.groovy.ast.expr.MethodCallExpression;

public class GradleDependenciesVisitor extends CodeVisitorSupport {

	//@Override
	public void visitMethodCallExpression​(MethodCallExpression m)
	{
		System.out.println("visitMethodCallExpression​ ="+m.getMethodAsString());
		super.visitMethodCallExpression(m);
	}
	
	public void visitMapExpression(MapExpression m)
	{
		System.out.println("visitMapExpression​ ="+m.getText());
		
		List<MapEntryExpression> mapEntries = m.getMapEntryExpressions();
		for (MapEntryExpression me:mapEntries)
		{
			System.out.println("\t"+me.getKeyExpression().getText());
			System.out.println("\t"+me.getValueExpression().getText());
		}
		
		super.visitMapExpression(m);
	}
	
	public void visit​ConstantExpression(ConstantExpression m)
	{
		System.out.println("visitConstantExpression​ ="+m.getText());
		super.visitConstantExpression(m);
	}


	
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
