package edu.cs.ucdavis.decal;

import java.util.Collection;
import java.util.HashSet;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.AnnotationTypeDeclaration;
import org.eclipse.jdt.core.dom.AnnotationTypeMemberDeclaration;
import org.eclipse.jdt.core.dom.AnonymousClassDeclaration;
import org.eclipse.jdt.core.dom.ArrayAccess;
import org.eclipse.jdt.core.dom.ArrayCreation;
import org.eclipse.jdt.core.dom.ArrayInitializer;
import org.eclipse.jdt.core.dom.ArrayType;
import org.eclipse.jdt.core.dom.AssertStatement;
import org.eclipse.jdt.core.dom.Assignment;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.BlockComment;
import org.eclipse.jdt.core.dom.BooleanLiteral;
import org.eclipse.jdt.core.dom.BreakStatement;
import org.eclipse.jdt.core.dom.CastExpression;
import org.eclipse.jdt.core.dom.CatchClause;
import org.eclipse.jdt.core.dom.CharacterLiteral;
import org.eclipse.jdt.core.dom.ClassInstanceCreation;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.ConditionalExpression;
import org.eclipse.jdt.core.dom.ConstructorInvocation;
import org.eclipse.jdt.core.dom.ContinueStatement;
import org.eclipse.jdt.core.dom.DoStatement;
import org.eclipse.jdt.core.dom.EmptyStatement;
import org.eclipse.jdt.core.dom.EnhancedForStatement;
import org.eclipse.jdt.core.dom.EnumConstantDeclaration;
import org.eclipse.jdt.core.dom.EnumDeclaration;
import org.eclipse.jdt.core.dom.ExpressionStatement;
import org.eclipse.jdt.core.dom.FieldAccess;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.ForStatement;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.IfStatement;
import org.eclipse.jdt.core.dom.ImportDeclaration;
import org.eclipse.jdt.core.dom.InfixExpression;
import org.eclipse.jdt.core.dom.Initializer;
import org.eclipse.jdt.core.dom.InstanceofExpression;
import org.eclipse.jdt.core.dom.Javadoc;
import org.eclipse.jdt.core.dom.LabeledStatement;
import org.eclipse.jdt.core.dom.LineComment;
import org.eclipse.jdt.core.dom.MarkerAnnotation;
import org.eclipse.jdt.core.dom.MemberRef;
import org.eclipse.jdt.core.dom.MemberValuePair;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.MethodRef;
import org.eclipse.jdt.core.dom.MethodRefParameter;
import org.eclipse.jdt.core.dom.Modifier;
import org.eclipse.jdt.core.dom.NormalAnnotation;
import org.eclipse.jdt.core.dom.NullLiteral;
import org.eclipse.jdt.core.dom.NumberLiteral;
import org.eclipse.jdt.core.dom.PackageDeclaration;
import org.eclipse.jdt.core.dom.ParameterizedType;
import org.eclipse.jdt.core.dom.ParenthesizedExpression;
import org.eclipse.jdt.core.dom.PostfixExpression;
import org.eclipse.jdt.core.dom.PrefixExpression;
import org.eclipse.jdt.core.dom.PrimitiveType;
import org.eclipse.jdt.core.dom.QualifiedName;
import org.eclipse.jdt.core.dom.QualifiedType;
import org.eclipse.jdt.core.dom.ReturnStatement;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SimpleType;
import org.eclipse.jdt.core.dom.SingleMemberAnnotation;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.StringLiteral;
import org.eclipse.jdt.core.dom.SuperConstructorInvocation;
import org.eclipse.jdt.core.dom.SuperFieldAccess;
import org.eclipse.jdt.core.dom.SuperMethodInvocation;
import org.eclipse.jdt.core.dom.SwitchCase;
import org.eclipse.jdt.core.dom.SwitchStatement;
import org.eclipse.jdt.core.dom.SynchronizedStatement;
import org.eclipse.jdt.core.dom.TagElement;
import org.eclipse.jdt.core.dom.TextElement;
import org.eclipse.jdt.core.dom.ThisExpression;
import org.eclipse.jdt.core.dom.ThrowStatement;
import org.eclipse.jdt.core.dom.TryStatement;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.TypeDeclarationStatement;
import org.eclipse.jdt.core.dom.TypeLiteral;
import org.eclipse.jdt.core.dom.TypeParameter;
import org.eclipse.jdt.core.dom.UnionType;
import org.eclipse.jdt.core.dom.VariableDeclarationExpression;
import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
import org.eclipse.jdt.core.dom.VariableDeclarationStatement;
import org.eclipse.jdt.core.dom.WhileStatement;
import org.eclipse.jdt.core.dom.WildcardType;

public class DumpAstVisitor extends ASTVisitor {

	Collection <CompilationUnit> units;
	Collection <String> bindingKeys;
	
	CompilationUnit currentUnit;
	OmniController controller;
	
	boolean resolve;
	
	public DumpAstVisitor() {
		units = new HashSet<CompilationUnit>();
		bindingKeys = new HashSet<String>();		
		currentUnit = null;
		controller = null;
		resolve = false;
	}

	public void register(OmniController controller) {
		this.controller = controller;
		controller.setVisitor(this);;
	}
	
	public DumpAstVisitor setController(OmniController controller) {
		this.controller = controller;
		return this;
	}
	
	public DumpAstVisitor setResolve(boolean resolve) {
		this.resolve = resolve;
		return this;
	}
	
	public DumpAstVisitor setCompilationUnits(Collection<CompilationUnit> units) {
		this.units = units;
		return this;
	}
	
	public Collection<CompilationUnit> getCompilatoinUnits() {
		return units;
	}
	
	public DumpAstVisitor setBindingKeys(Collection <String> bindingKeys) {
		this.bindingKeys = bindingKeys;
		return this;
	}
	
	public Collection <String> getBindingKeys() {
		return bindingKeys;
	}
	
	public DumpAstVisitor setCurrentCompilationUnit(CompilationUnit unit) {
		currentUnit = unit;
		return this;
	}
	
	private String getNodeInfoLeave(ASTNode node) {
		return String.format("<<< %s <<<", node.getClass().getSimpleName());
	}
	
	private String getNodeInfo2(ASTNode node) {
		int startPos = node.getStartPosition();
		int endPos = node.getStartPosition() + node.getLength();
				
		return String.format(">>> "
				+ "toString():\n%s\n"
				+ "name %s\n"
				+ "line #%d\n"
				+ "getFlags() %d\n"
				+ "getStartPosition() %d\n"
				+ "getLength() %d\n"
				+ "getLocationInParent() %s\n"
				+ "getNodeType() %d\n"
				+ "structuralPropertiesForType() %s\n"
				+ "subtreeType() %d >>>\n",
				node.toString(),
				node.getClass().getSimpleName(),
				currentUnit.getLineNumber(startPos),
				node.getFlags(),
				node.getStartPosition(),
				node.getLength(),
				node.getLocationInParent(),
				node.getNodeType(),
				node.getStartPosition(),
				node.subtreeBytes()
				);
	}
	
	private String getNodeInfo(ASTNode node) {
		int startPos = node.getStartPosition();
		int endPos = node.getStartPosition() + node.getLength();
	
		return String.format("\n>>> %s (%s) #%d %d->%d (%s->%s) >>>",
				node.toString(),                    // node string representation, ex: a
				node.getClass().getSimpleName(),    // class type, ex: SimpleName
				currentUnit.getLineNumber(startPos),
				startPos, endPos, 
				Integer.toHexString(startPos), Integer.toHexString(endPos));
	}
	
	private ASTNode getDeclaringNode(IBinding binding) {
		if (binding == null) {
			return null;
		}
		
		ASTNode node = null;
		
		if (currentUnit != null) {
			node = currentUnit.findDeclaringNode(binding);
		}
		
		if (node == null) {
			if (units != null) {
			for (CompilationUnit unit: units) {
				node = unit.findDeclaringNode(binding.getKey());
				if (node != null) {
					break;
				}
			}
			} else {
				System.err.println("units is empty");
			}
		} 
		
		return node;
	}

	@Override
	public boolean visit(VariableDeclarationFragment node) {
		System.out.println(getNodeInfo(node));
		return true;
	}

	@Override
	public boolean visit(SimpleName node) {

		System.out.println(getNodeInfo(node));
		IBinding binding = node.resolveBinding();
		ASTNode n2 = getDeclaringNode(binding);
	
		if (binding != null) {
			System.out.println(String.format("## %s (%s), %s, %s", binding, binding.getKey(), n2, binding.getJavaElement()));

			if (n2 == null && !resolve) {
				bindingKeys.add(binding.getKey());
			}
		}
		
		if (n2 != null) {
			System.out.println("is declared at " + getNodeInfo(n2) + "\n    XXX\n");
		}
		
		return true;
	}

	@Override
	public boolean visit(MethodRef node) {
		System.out.println(getNodeInfo(node));
		return true;
	}

	@Override
	public boolean visit(MethodRefParameter node) {
		System.out.println(getNodeInfo(node));
		return true;
	}

	@Override
	public boolean visit(MethodDeclaration node) {
		System.out.println(getNodeInfo(node));
		return true;
	}

	@Override
	public boolean visit(MethodInvocation node) {
		System.out.println(getNodeInfo(node));
		return true;
	}

	@Override
	public boolean visit(AnnotationTypeDeclaration node) {
		System.out.println(getNodeInfo(node));   // Auto-generated
		return super.visit(node);
	}

	@Override
	public boolean visit(AnnotationTypeMemberDeclaration node) {
		System.out.println(getNodeInfo(node));   // Auto-generated
		return super.visit(node);
	}

	@Override
	public boolean visit(AnonymousClassDeclaration node) {
		System.out.println(getNodeInfo(node));   // Auto-generated
		return super.visit(node);
	}

	@Override
	public boolean visit(ArrayAccess node) {
		System.out.println(getNodeInfo(node));   // Auto-generated
		return super.visit(node);
	}

	@Override
	public boolean visit(ArrayCreation node) {
		System.out.println(getNodeInfo(node));   // Auto-generated
		return super.visit(node);
	}

	@Override
	public boolean visit(ArrayInitializer node) {
		System.out.println(getNodeInfo(node));   // Auto-generated
		return super.visit(node);
	}

	@Override
	public boolean visit(ArrayType node) {
		System.out.println(getNodeInfo(node));   // Auto-generated
		return super.visit(node);
	}

	@Override
	public boolean visit(AssertStatement node) {
		System.out.println(getNodeInfo(node));   // Auto-generated
		return super.visit(node);
	}

	@Override
	public boolean visit(Assignment node) {
		System.out.println(getNodeInfo(node));   // Auto-generated
		return super.visit(node);
	}

	@Override
	public boolean visit(Block node) {
		System.out.println(getNodeInfo(node));   // Auto-generated
		return super.visit(node);
	}

	@Override
	public boolean visit(BlockComment node) {
		System.out.println(getNodeInfo(node));   // Auto-generated
		return super.visit(node);
	}

	@Override
	public boolean visit(BooleanLiteral node) {
		System.out.println(getNodeInfo(node));   // Auto-generated
		return super.visit(node);
	}

	@Override
	public boolean visit(BreakStatement node) {
		System.out.println(getNodeInfo(node));   // Auto-generated
		return super.visit(node);
	}

	@Override
	public boolean visit(CastExpression node) {
		System.out.println(getNodeInfo(node));   // Auto-generated
		return super.visit(node);
	}

	@Override
	public boolean visit(CatchClause node) {
		System.out.println(getNodeInfo(node));   // Auto-generated
		return super.visit(node);
	}

	@Override
	public boolean visit(CharacterLiteral node) {
		System.out.println(getNodeInfo(node));   // Auto-generated
		return super.visit(node);
	}

	@Override
	public boolean visit(ClassInstanceCreation node) {
		System.out.println(getNodeInfo(node));   // Auto-generated
		return super.visit(node);
	}

	@Override
	public boolean visit(CompilationUnit node) {
		setCurrentCompilationUnit(node);
		if (!resolve) {
			units.add(node);
		}
		System.out.println(getNodeInfo(node));   // Auto-generated
		return super.visit(node);
	}

	@Override
	public boolean visit(ConditionalExpression node) {
		System.out.println(getNodeInfo(node));   // Auto-generated
		return super.visit(node);
	}

	@Override
	public boolean visit(ConstructorInvocation node) {
		System.out.println(getNodeInfo(node));   // Auto-generated
		return super.visit(node);
	}

	@Override
	public boolean visit(ContinueStatement node) {
		System.out.println(getNodeInfo(node));   // Auto-generated
		return super.visit(node);
	}

	@Override
	public boolean visit(DoStatement node) {
		System.out.println(getNodeInfo(node));   // Auto-generated
		return super.visit(node);
	}

	@Override
	public boolean visit(EmptyStatement node) {
		System.out.println(getNodeInfo(node));   // Auto-generated
		return super.visit(node);
	}

	@Override
	public boolean visit(EnhancedForStatement node) {
		System.out.println(getNodeInfo(node));   // Auto-generated
		return super.visit(node);
	}

	@Override
	public boolean visit(EnumConstantDeclaration node) {
		System.out.println(getNodeInfo(node));   // Auto-generated
		return super.visit(node);
	}

	@Override
	public boolean visit(EnumDeclaration node) {
		System.out.println(getNodeInfo(node));   // Auto-generated
		return super.visit(node);
	}

	@Override
	public boolean visit(ExpressionStatement node) {
		System.out.println(getNodeInfo(node));   // Auto-generated
		return super.visit(node);
	}

	@Override
	public boolean visit(FieldAccess node) {
		System.out.println(getNodeInfo(node));   // Auto-generated
		return super.visit(node);
	}

	@Override
	public boolean visit(FieldDeclaration node) {
		System.out.println(getNodeInfo(node));   // Auto-generated
		return super.visit(node);
	}

	@Override
	public boolean visit(ForStatement node) {
		System.out.println(getNodeInfo(node));   // Auto-generated
		return super.visit(node);
	}

	@Override
	public boolean visit(IfStatement node) {
		System.out.println(getNodeInfo(node));   // Auto-generated
		return super.visit(node);
	}

	@Override
	public boolean visit(ImportDeclaration node) {
		System.out.println(getNodeInfo(node));   // Auto-generated
		return super.visit(node);
	}

	@Override
	public boolean visit(InfixExpression node) {
		System.out.println(getNodeInfo(node));   // Auto-generated
		return super.visit(node);
	}

	@Override
	public boolean visit(InstanceofExpression node) {
		System.out.println(getNodeInfo(node));   // Auto-generated
		return super.visit(node);
	}

	@Override
	public boolean visit(Initializer node) {
		System.out.println(getNodeInfo(node));   // Auto-generated
		return super.visit(node);
	}

	@Override
	public boolean visit(Javadoc node) {
		System.out.println(getNodeInfo(node));   // Auto-generated
		return super.visit(node);
	}

	@Override
	public boolean visit(LabeledStatement node) {
		System.out.println(getNodeInfo(node));   // Auto-generated
		return super.visit(node);
	}

	@Override
	public boolean visit(LineComment node) {
		System.out.println(getNodeInfo(node));   // Auto-generated
		return super.visit(node);
	}

	@Override
	public boolean visit(MarkerAnnotation node) {
		System.out.println(getNodeInfo(node));   // Auto-generated
		//return super.visit(node);
		return false;
	}

	@Override
	public boolean visit(MemberRef node) {
		System.out.println(getNodeInfo(node));   // Auto-generated
		return super.visit(node);
	}

	@Override
	public boolean visit(MemberValuePair node) {
		System.out.println(getNodeInfo(node));   // Auto-generated
		return super.visit(node);
	}

	@Override
	public boolean visit(Modifier node) {
		System.out.println(getNodeInfo(node));   // Auto-generated
		return super.visit(node);
	}

	@Override
	public boolean visit(NormalAnnotation node) {
		System.out.println(getNodeInfo(node));   // Auto-generated
		return super.visit(node);
	}

	@Override
	public boolean visit(NullLiteral node) {
		System.out.println(getNodeInfo(node));   // Auto-generated
		return super.visit(node);
	}

	@Override
	public boolean visit(NumberLiteral node) {
		System.out.println(getNodeInfo(node));   // Auto-generated
		return super.visit(node);
	}

	@Override
	public boolean visit(PackageDeclaration node) {
		System.out.println(getNodeInfo(node));   // Auto-generated
		return super.visit(node);
	}

	@Override
	public boolean visit(ParameterizedType node) {
		System.out.println(getNodeInfo(node));   // Auto-generated
		return super.visit(node);
	}

	@Override
	public boolean visit(ParenthesizedExpression node) {
		System.out.println(getNodeInfo(node));   // Auto-generated
		return super.visit(node);
	}

	@Override
	public boolean visit(PostfixExpression node) {
		System.out.println(getNodeInfo(node));   // Auto-generated
		return super.visit(node);
	}

	@Override
	public boolean visit(PrefixExpression node) {
		System.out.println(getNodeInfo(node));   // Auto-generated
		return super.visit(node);
	}

	@Override
	public boolean visit(PrimitiveType node) {
		System.out.println(getNodeInfo(node));   // Auto-generated
		return super.visit(node);
	}

	@Override
	public boolean visit(QualifiedName node) {
		System.out.println(getNodeInfo(node));   // Auto-generated
		return super.visit(node);
	}

	@Override
	public boolean visit(QualifiedType node) {
		System.out.println(getNodeInfo(node));   // Auto-generated
		return super.visit(node);
	}

	@Override
	public boolean visit(ReturnStatement node) {
		System.out.println(getNodeInfo(node));   // Auto-generated
		return super.visit(node);
	}

	@Override
	public boolean visit(SimpleType node) {
		System.out.println(getNodeInfo(node));   // Auto-generated
		return super.visit(node);
	}

	@Override
	public boolean visit(SingleMemberAnnotation node) {
		System.out.println(getNodeInfo(node));   // Auto-generated
		return super.visit(node);
	}

	@Override
	public boolean visit(SingleVariableDeclaration node) {
		System.out.println(getNodeInfo(node));   // Auto-generated
		return super.visit(node);
	}

	@Override
	public boolean visit(StringLiteral node) {
		System.out.println(getNodeInfo(node));   // Auto-generated
		return super.visit(node);
	}

	@Override
	public boolean visit(SuperConstructorInvocation node) {
		System.out.println(getNodeInfo(node));   // Auto-generated
		return super.visit(node);
	}

	@Override
	public boolean visit(SuperFieldAccess node) {
		System.out.println(getNodeInfo(node));   // Auto-generated
		return super.visit(node);
	}

	@Override
	public boolean visit(SuperMethodInvocation node) {
		System.out.println(getNodeInfo(node));   // Auto-generated
		return super.visit(node);
	}

	@Override
	public boolean visit(SwitchCase node) {
		System.out.println(getNodeInfo(node));   // Auto-generated
		return super.visit(node);
	}

	@Override
	public boolean visit(SwitchStatement node) {
		System.out.println(getNodeInfo(node));   // Auto-generated
		return super.visit(node);
	}

	@Override
	public boolean visit(SynchronizedStatement node) {
		System.out.println(getNodeInfo(node));   // Auto-generated
		return super.visit(node);
	}

	@Override
	public boolean visit(TagElement node) {
		System.out.println(getNodeInfo(node));   // Auto-generated
		return super.visit(node);
	}

	@Override
	public boolean visit(TextElement node) {
		System.out.println(getNodeInfo(node));   // Auto-generated
		return super.visit(node);
	}

	@Override
	public boolean visit(ThisExpression node) {
		System.out.println(getNodeInfo(node));   // Auto-generated
		return super.visit(node);
	}

	@Override
	public boolean visit(ThrowStatement node) {
		System.out.println(getNodeInfo(node));   // Auto-generated
		return super.visit(node);
	}

	@Override
	public boolean visit(TryStatement node) {
		System.out.println(getNodeInfo(node));   // Auto-generated
		return super.visit(node);
	}

	@Override
	public boolean visit(TypeDeclaration node) {
		System.out.println(getNodeInfo(node));   // Auto-generated
		return super.visit(node);
	}

	@Override
	public boolean visit(TypeDeclarationStatement node) {
		System.out.println(getNodeInfo(node));   // Auto-generated
		return super.visit(node);
	}

	@Override
	public boolean visit(TypeLiteral node) {
		System.out.println(getNodeInfo(node));   // Auto-generated
		return super.visit(node);
	}

	@Override
	public boolean visit(TypeParameter node) {
		System.out.println(getNodeInfo(node));   // Auto-generated
		return super.visit(node);
	}

	@Override
	public boolean visit(UnionType node) {
		System.out.println(getNodeInfo(node));   // Auto-generated
		return super.visit(node);
	}

	@Override
	public boolean visit(VariableDeclarationExpression node) {
		System.out.println(getNodeInfo(node));   // Auto-generated
		return super.visit(node);
	}

	@Override
	public boolean visit(VariableDeclarationStatement node) {
		System.out.println(getNodeInfo(node));   // Auto-generated
		return super.visit(node);
	}

	@Override
	public boolean visit(WhileStatement node) {
		System.out.println(getNodeInfo(node));   // Auto-generated
		return super.visit(node);
	}

	@Override
	public boolean visit(WildcardType node) {
		System.out.println(getNodeInfo(node));   // Auto-generated
		return super.visit(node);
	}

	@Override
	public void endVisit(AnnotationTypeDeclaration node) {
		System.out.println(getNodeInfoLeave(node));  // auto-generate leave
		super.endVisit(node);
	}

	@Override
	public void endVisit(AnnotationTypeMemberDeclaration node) {
		System.out.println(getNodeInfoLeave(node));  // auto-generate leave
		super.endVisit(node);
	}

	@Override
	public void endVisit(AnonymousClassDeclaration node) {
		System.out.println(getNodeInfoLeave(node));  // auto-generate leave
		super.endVisit(node);
	}

	@Override
	public void endVisit(ArrayAccess node) {
		System.out.println(getNodeInfoLeave(node));  // auto-generate leave
		super.endVisit(node);
	}

	@Override
	public void endVisit(ArrayCreation node) {
		System.out.println(getNodeInfoLeave(node));  // auto-generate leave
		super.endVisit(node);
	}

	@Override
	public void endVisit(ArrayInitializer node) {
		System.out.println(getNodeInfoLeave(node));  // auto-generate leave
		super.endVisit(node);
	}

	@Override
	public void endVisit(ArrayType node) {
		System.out.println(getNodeInfoLeave(node));  // auto-generate leave
		super.endVisit(node);
	}

	@Override
	public void endVisit(AssertStatement node) {
		System.out.println(getNodeInfoLeave(node));  // auto-generate leave
		super.endVisit(node);
	}

	@Override
	public void endVisit(Assignment node) {
		System.out.println(getNodeInfoLeave(node));  // auto-generate leave
		super.endVisit(node);
	}

	@Override
	public void endVisit(Block node) {
		System.out.println(getNodeInfoLeave(node));  // auto-generate leave
		super.endVisit(node);
	}

	@Override
	public void endVisit(BlockComment node) {
		System.out.println(getNodeInfoLeave(node));  // auto-generate leave
		super.endVisit(node);
	}

	@Override
	public void endVisit(BooleanLiteral node) {
		System.out.println(getNodeInfoLeave(node));  // auto-generate leave
		super.endVisit(node);
	}

	@Override
	public void endVisit(BreakStatement node) {
		System.out.println(getNodeInfoLeave(node));  // auto-generate leave
		super.endVisit(node);
	}

	@Override
	public void endVisit(CastExpression node) {
		System.out.println(getNodeInfoLeave(node));  // auto-generate leave
		super.endVisit(node);
	}

	@Override
	public void endVisit(CatchClause node) {
		System.out.println(getNodeInfoLeave(node));  // auto-generate leave
		super.endVisit(node);
	}

	@Override
	public void endVisit(CharacterLiteral node) {
		System.out.println(getNodeInfoLeave(node));  // auto-generate leave
		super.endVisit(node);
	}

	@Override
	public void endVisit(ClassInstanceCreation node) {
		System.out.println(getNodeInfoLeave(node));  // auto-generate leave
		super.endVisit(node);
	}

	@Override
	public void endVisit(CompilationUnit node) {
		System.out.println(getNodeInfoLeave(node));  // auto-generate leave
		super.endVisit(node);
	}

	@Override
	public void endVisit(ConditionalExpression node) {
		System.out.println(getNodeInfoLeave(node));  // auto-generate leave
		super.endVisit(node);
	}

	@Override
	public void endVisit(ConstructorInvocation node) {
		System.out.println(getNodeInfoLeave(node));  // auto-generate leave
		super.endVisit(node);
	}

	@Override
	public void endVisit(ContinueStatement node) {
		System.out.println(getNodeInfoLeave(node));  // auto-generate leave
		super.endVisit(node);
	}

	@Override
	public void endVisit(DoStatement node) {
		System.out.println(getNodeInfoLeave(node));  // auto-generate leave
		super.endVisit(node);
	}

	@Override
	public void endVisit(EmptyStatement node) {
		System.out.println(getNodeInfoLeave(node));  // auto-generate leave
		super.endVisit(node);
	}

	@Override
	public void endVisit(EnhancedForStatement node) {
		System.out.println(getNodeInfoLeave(node));  // auto-generate leave
		super.endVisit(node);
	}

	@Override
	public void endVisit(EnumConstantDeclaration node) {
		System.out.println(getNodeInfoLeave(node));  // auto-generate leave
		super.endVisit(node);
	}

	@Override
	public void endVisit(EnumDeclaration node) {
		System.out.println(getNodeInfoLeave(node));  // auto-generate leave
		super.endVisit(node);
	}

	@Override
	public void endVisit(ExpressionStatement node) {
		System.out.println(getNodeInfoLeave(node));  // auto-generate leave
		super.endVisit(node);
	}

	@Override
	public void endVisit(FieldAccess node) {
		System.out.println(getNodeInfoLeave(node));  // auto-generate leave
		super.endVisit(node);
	}

	@Override
	public void endVisit(FieldDeclaration node) {
		System.out.println(getNodeInfoLeave(node));  // auto-generate leave
		super.endVisit(node);
	}

	@Override
	public void endVisit(ForStatement node) {
		System.out.println(getNodeInfoLeave(node));  // auto-generate leave
		super.endVisit(node);
	}

	@Override
	public void endVisit(IfStatement node) {
		System.out.println(getNodeInfoLeave(node));  // auto-generate leave
		super.endVisit(node);
	}

	@Override
	public void endVisit(ImportDeclaration node) {
		System.out.println(getNodeInfoLeave(node));  // auto-generate leave
		super.endVisit(node);
	}

	@Override
	public void endVisit(InfixExpression node) {
		System.out.println(getNodeInfoLeave(node));  // auto-generate leave
		super.endVisit(node);
	}

	@Override
	public void endVisit(InstanceofExpression node) {
		System.out.println(getNodeInfoLeave(node));  // auto-generate leave
		super.endVisit(node);
	}

	@Override
	public void endVisit(Initializer node) {
		System.out.println(getNodeInfoLeave(node));  // auto-generate leave
		super.endVisit(node);
	}

	@Override
	public void endVisit(Javadoc node) {
		System.out.println(getNodeInfoLeave(node));  // auto-generate leave
		super.endVisit(node);
	}

	@Override
	public void endVisit(LabeledStatement node) {
		System.out.println(getNodeInfoLeave(node));  // auto-generate leave
		super.endVisit(node);
	}

	@Override
	public void endVisit(LineComment node) {
		System.out.println(getNodeInfoLeave(node));  // auto-generate leave
		super.endVisit(node);
	}

	@Override
	public void endVisit(MarkerAnnotation node) {
		System.out.println(getNodeInfoLeave(node));  // auto-generate leave
		super.endVisit(node);
	}

	@Override
	public void endVisit(MemberRef node) {
		System.out.println(getNodeInfoLeave(node));  // auto-generate leave
		super.endVisit(node);
	}

	@Override
	public void endVisit(MemberValuePair node) {
		System.out.println(getNodeInfoLeave(node));  // auto-generate leave
		super.endVisit(node);
	}

	@Override
	public void endVisit(MethodRef node) {
		System.out.println(getNodeInfoLeave(node));  // auto-generate leave
		super.endVisit(node);
	}

	@Override
	public void endVisit(MethodRefParameter node) {
		System.out.println(getNodeInfoLeave(node));  // auto-generate leave
		super.endVisit(node);
	}

	@Override
	public void endVisit(MethodDeclaration node) {
		System.out.println(getNodeInfoLeave(node));  // auto-generate leave
		super.endVisit(node);
	}

	@Override
	public void endVisit(MethodInvocation node) {
		System.out.println(getNodeInfoLeave(node));  // auto-generate leave
		super.endVisit(node);
	}

	@Override
	public void endVisit(Modifier node) {
		System.out.println(getNodeInfoLeave(node));  // auto-generate leave
		super.endVisit(node);
	}

	@Override
	public void endVisit(NormalAnnotation node) {
		System.out.println(getNodeInfoLeave(node));  // auto-generate leave
		super.endVisit(node);
	}

	@Override
	public void endVisit(NullLiteral node) {
		System.out.println(getNodeInfoLeave(node));  // auto-generate leave
		super.endVisit(node);
	}

	@Override
	public void endVisit(NumberLiteral node) {
		System.out.println(getNodeInfoLeave(node));  // auto-generate leave
		super.endVisit(node);
	}

	@Override
	public void endVisit(PackageDeclaration node) {
		System.out.println(getNodeInfoLeave(node));  // auto-generate leave
		super.endVisit(node);
	}

	@Override
	public void endVisit(ParameterizedType node) {
		System.out.println(getNodeInfoLeave(node));  // auto-generate leave
		super.endVisit(node);
	}

	@Override
	public void endVisit(ParenthesizedExpression node) {
		System.out.println(getNodeInfoLeave(node));  // auto-generate leave
		super.endVisit(node);
	}

	@Override
	public void endVisit(PostfixExpression node) {
		System.out.println(getNodeInfoLeave(node));  // auto-generate leave
		super.endVisit(node);
	}

	@Override
	public void endVisit(PrefixExpression node) {
		System.out.println(getNodeInfoLeave(node));  // auto-generate leave
		super.endVisit(node);
	}

	@Override
	public void endVisit(PrimitiveType node) {
		System.out.println(getNodeInfoLeave(node));  // auto-generate leave
		super.endVisit(node);
	}

	@Override
	public void endVisit(QualifiedName node) {
		System.out.println(getNodeInfoLeave(node));  // auto-generate leave
		super.endVisit(node);
	}

	@Override
	public void endVisit(QualifiedType node) {
		System.out.println(getNodeInfoLeave(node));  // auto-generate leave
		super.endVisit(node);
	}

	@Override
	public void endVisit(ReturnStatement node) {
		System.out.println(getNodeInfoLeave(node));  // auto-generate leave
		super.endVisit(node);
	}

	@Override
	public void endVisit(SimpleName node) {
		System.out.println(getNodeInfoLeave(node));  // auto-generate leave
		super.endVisit(node);
	}

	@Override
	public void endVisit(SimpleType node) {
		System.out.println(getNodeInfoLeave(node));  // auto-generate leave
		super.endVisit(node);
	}

	@Override
	public void endVisit(SingleMemberAnnotation node) {
		System.out.println(getNodeInfoLeave(node));  // auto-generate leave
		super.endVisit(node);
	}

	@Override
	public void endVisit(SingleVariableDeclaration node) {
		System.out.println(getNodeInfoLeave(node));  // auto-generate leave
		super.endVisit(node);
	}

	@Override
	public void endVisit(StringLiteral node) {
		System.out.println(getNodeInfoLeave(node));  // auto-generate leave
		super.endVisit(node);
	}

	@Override
	public void endVisit(SuperConstructorInvocation node) {
		System.out.println(getNodeInfoLeave(node));  // auto-generate leave
		super.endVisit(node);
	}

	@Override
	public void endVisit(SuperFieldAccess node) {
		System.out.println(getNodeInfoLeave(node));  // auto-generate leave
		super.endVisit(node);
	}

	@Override
	public void endVisit(SuperMethodInvocation node) {
		System.out.println(getNodeInfoLeave(node));  // auto-generate leave
		super.endVisit(node);
	}

	@Override
	public void endVisit(SwitchCase node) {
		System.out.println(getNodeInfoLeave(node));  // auto-generate leave
		super.endVisit(node);
	}

	@Override
	public void endVisit(SwitchStatement node) {
		System.out.println(getNodeInfoLeave(node));  // auto-generate leave
		super.endVisit(node);
	}

	@Override
	public void endVisit(SynchronizedStatement node) {
		System.out.println(getNodeInfoLeave(node));  // auto-generate leave
		super.endVisit(node);
	}

	@Override
	public void endVisit(TagElement node) {
		System.out.println(getNodeInfoLeave(node));  // auto-generate leave
		super.endVisit(node);
	}

	@Override
	public void endVisit(TextElement node) {
		System.out.println(getNodeInfoLeave(node));  // auto-generate leave
		super.endVisit(node);
	}

	@Override
	public void endVisit(ThisExpression node) {
		System.out.println(getNodeInfoLeave(node));  // auto-generate leave
		super.endVisit(node);
	}

	@Override
	public void endVisit(ThrowStatement node) {
		System.out.println(getNodeInfoLeave(node));  // auto-generate leave
		super.endVisit(node);
	}

	@Override
	public void endVisit(TryStatement node) {
		System.out.println(getNodeInfoLeave(node));  // auto-generate leave
		super.endVisit(node);
	}

	@Override
	public void endVisit(TypeDeclaration node) {
		System.out.println(getNodeInfoLeave(node));  // auto-generate leave
		super.endVisit(node);
	}

	@Override
	public void endVisit(TypeDeclarationStatement node) {
		System.out.println(getNodeInfoLeave(node));  // auto-generate leave
		super.endVisit(node);
	}

	@Override
	public void endVisit(TypeLiteral node) {
		System.out.println(getNodeInfoLeave(node));  // auto-generate leave
		super.endVisit(node);
	}

	@Override
	public void endVisit(TypeParameter node) {
		System.out.println(getNodeInfoLeave(node));  // auto-generate leave
		super.endVisit(node);
	}

	@Override
	public void endVisit(UnionType node) {
		System.out.println(getNodeInfoLeave(node));  // auto-generate leave
		super.endVisit(node);
	}

	@Override
	public void endVisit(VariableDeclarationExpression node) {
		System.out.println(getNodeInfoLeave(node));  // auto-generate leave
		super.endVisit(node);
	}

	@Override
	public void endVisit(VariableDeclarationStatement node) {
		System.out.println(getNodeInfoLeave(node));  // auto-generate leave
		super.endVisit(node);
	}

	@Override
	public void endVisit(VariableDeclarationFragment node) {
		System.out.println(getNodeInfoLeave(node));  // auto-generate leave
		super.endVisit(node);
	}

	@Override
	public void endVisit(WhileStatement node) {
		System.out.println(getNodeInfoLeave(node));  // auto-generate leave
		super.endVisit(node);
	}

	@Override
	public void endVisit(WildcardType node) {
		System.out.println(getNodeInfoLeave(node));  // auto-generate leave
		super.endVisit(node);
	}
}
