package edu.cs.ucdavis.decal;

import java.util.List;

import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.Annotation;
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

	CompilationUnit currentUnit;
	OmniController controller;

	private String snippet;
	private int currentAstnodeId;

	public DumpAstVisitor() {
		currentUnit = null;
		controller = null;
	}

	public void register(OmniController controller) {
		this.controller = controller;
		controller.setVisitor(this);
	}

	public DumpAstVisitor setController(OmniController controller) {
		this.controller = controller;
		return this;
	}

	public DumpAstVisitor setCurrentCompilationUnit(CompilationUnit unit) {
		currentUnit = unit;
		return this;
	}

	private void saveAstNodeInfo(ASTNode node) {
		controller.saveAstNodeInfo(node, currentUnit);
	}

	@Override
	public boolean preVisit2(ASTNode node) {
		saveAstNodeInfo(node);
		return true;
	}

	/////  custom visit methods
	@Override
	public boolean visit(AnnotationTypeDeclaration node) {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public boolean visit(AnnotationTypeMemberDeclaration node) {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public boolean visit(AnonymousClassDeclaration node) {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public boolean visit(ArrayAccess node) {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public boolean visit(ArrayCreation node) {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public boolean visit(ArrayInitializer node) {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public boolean visit(ArrayType node) {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public boolean visit(AssertStatement node) {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public boolean visit(Assignment node) {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public boolean visit(Block node) {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public boolean visit(BlockComment node) {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public boolean visit(BooleanLiteral node) {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public boolean visit(BreakStatement node) {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public boolean visit(CastExpression node) {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public boolean visit(CatchClause node) {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public boolean visit(CharacterLiteral node) {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public boolean visit(ClassInstanceCreation node) {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public boolean visit(CompilationUnit node) {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public boolean visit(ConditionalExpression node) {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public boolean visit(ConstructorInvocation node) {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public boolean visit(ContinueStatement node) {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public boolean visit(DoStatement node) {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public boolean visit(EmptyStatement node) {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public boolean visit(EnhancedForStatement node) {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public boolean visit(EnumConstantDeclaration node) {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public boolean visit(EnumDeclaration node) {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public boolean visit(ExpressionStatement node) {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public boolean visit(FieldAccess node) {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public boolean visit(FieldDeclaration node) {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public boolean visit(ForStatement node) {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public boolean visit(IfStatement node) {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public boolean visit(ImportDeclaration node) {
		final int parentId = controller.getAstnodeId(node);  // token's parent is current astnode

		String currentFileRaw = controller.getCurrentFileRaw();
		int node_end_pos = node.getStartPosition() + node.getLength();

		controller.saveTokenInfo(currentUnit, node.getStartPosition(),
				Token.IMPORT, parentId);

		if (node.isStatic()) {
			int static_start_pos = node.getStartPosition() + Token.IMPORT.getLength();
			String tail = currentFileRaw.substring(static_start_pos, node_end_pos);
			int ns = tail.indexOf(Token.STATIC.getToken());
			static_start_pos += ns;
			controller.saveTokenInfo(currentUnit, static_start_pos,
					Token.STATIC, parentId);
		}

		if (node.isOnDemand()) {
			int dot_start_pos = node.getName().getStartPosition() + node.getName().getLength();
			String tail = currentFileRaw.substring(dot_start_pos, node_end_pos);
			int ns = tail.indexOf(Token.DOT.getToken());
			dot_start_pos+=ns;
			controller.saveTokenInfo(currentUnit, dot_start_pos, Token.DOT, parentId);

			int star_start_pos = dot_start_pos + Token.DOT.getLength();
			String t2 = currentFileRaw.substring(star_start_pos, node_end_pos);
			int ns2 = t2.indexOf(Token.MUL.getToken());
			star_start_pos += ns2;

			controller.saveTokenInfo(currentUnit, star_start_pos, Token.MUL, parentId);
		}

		controller.saveTokenInfo(currentUnit, node_end_pos - Token.SEMI.getLength(),
				Token.SEMI,
				parentId);
		return true;
	}

	@Override
	public boolean visit(InfixExpression node) {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public boolean visit(InstanceofExpression node) {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public boolean visit(Initializer node) {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public boolean visit(Javadoc node) {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public boolean visit(LabeledStatement node) {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public boolean visit(LineComment node) {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public boolean visit(MarkerAnnotation node) {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public boolean visit(MemberRef node) {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public boolean visit(MemberValuePair node) {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public boolean visit(MethodRef node) {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public boolean visit(MethodRefParameter node) {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public boolean visit(MethodDeclaration node) {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public boolean visit(MethodInvocation node) {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public boolean visit(Modifier node) {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public boolean visit(NormalAnnotation node) {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public boolean visit(NullLiteral node) {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public boolean visit(NumberLiteral node) {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public boolean visit(PackageDeclaration node) {
		final int parentId = controller.getAstnodeId(node);
		// package ...... ;
		// save package
		// save ;
		final String rawFile = controller.getCurrentFileRaw();
		int package_start_pos = node.getStartPosition();

		if (node.getJavadoc() != null) {
			package_start_pos =  node.getJavadoc().getStartPosition() + node.getJavadoc().getLength();
		}

		if (node.annotations() != null && node.annotations().size() > 0) {
			List <Annotation> annoList = node.annotations();
			Annotation anno = annoList.get(annoList.size() - 1);
			package_start_pos = anno.getStartPosition() + anno.getLength();
		}

		String tail = rawFile.substring(package_start_pos, node.getStartPosition() + node.getLength());
		int ns = tail.indexOf(Token.PACKAGE.getToken());
		package_start_pos += ns;

		controller.saveTokenInfo(currentUnit, package_start_pos,
				Token.PACKAGE,
				parentId);
		controller.saveTokenInfo(currentUnit, node.getStartPosition() + node.getLength() - Token.SEMI.getLength(),
				Token.SEMI,
				parentId);
		return true;
	}

	@Override
	public boolean visit(ParameterizedType node) {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public boolean visit(ParenthesizedExpression node) {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public boolean visit(PostfixExpression node) {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public boolean visit(PrefixExpression node) {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public boolean visit(PrimitiveType node) {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public boolean visit(QualifiedName node) {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public boolean visit(QualifiedType node) {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public boolean visit(ReturnStatement node) {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public boolean visit(SimpleName node) {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public boolean visit(SimpleType node) {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public boolean visit(SingleMemberAnnotation node) {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public boolean visit(SingleVariableDeclaration node) {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public boolean visit(StringLiteral node) {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public boolean visit(SuperConstructorInvocation node) {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public boolean visit(SuperFieldAccess node) {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public boolean visit(SuperMethodInvocation node) {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public boolean visit(SwitchCase node) {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public boolean visit(SwitchStatement node) {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public boolean visit(SynchronizedStatement node) {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public boolean visit(TagElement node) {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public boolean visit(TextElement node) {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public boolean visit(ThisExpression node) {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public boolean visit(ThrowStatement node) {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public boolean visit(TryStatement node) {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public boolean visit(TypeDeclaration node) {
		settle(node);
		if (node.isInterface()) {
			int interface_start_pos = getTokenStartPosBeforeBackwardStartPos(snippet, node.getStartPosition(), Token.INTERFACE, node.getName().getStartPosition());
			controller.saveTokenInfo(currentUnit, interface_start_pos, Token.INTERFACE, this.currentAstnodeId);
		} else { // is class
			int class_start_pos = getTokenStartPosBeforeBackwardStartPos(snippet, node.getStartPosition(), Token.CLASS, node.getName().getStartPosition());
			controller.saveTokenInfo(currentUnit, class_start_pos, Token.CLASS, this.currentAstnodeId);
		}
		return true;
	}

	@Override
	public boolean visit(TypeDeclarationStatement node) {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public boolean visit(TypeLiteral node) {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public boolean visit(TypeParameter node) {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public boolean visit(UnionType node) {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public boolean visit(VariableDeclarationExpression node) {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public boolean visit(VariableDeclarationStatement node) {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public boolean visit(VariableDeclarationFragment node) {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public boolean visit(WhileStatement node) {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public boolean visit(WildcardType node) {
		// TODO Auto-generated method stub
		return true;
	}

	//    "    rawSnippet   "
	//    .................start_from ...........................................................................................
	//    node.start_pos .........token.start_pos ....token.start_pos + token.length ................node.start_pos + node.length
	//
	//   snippet_start
	// find token start pos in whole file

	// return -1 when can't find it
	public int getTokenStartPos(String rawSnippet, int snippet_start_pos, Token token, int skip_length) {
		final String raw = rawSnippet.substring(skip_length);
		final int token_pos = raw.indexOf(token.getToken());
		return (token_pos == -1) ? -1 : snippet_start_pos + skip_length + token_pos;
	}

	public int getTokenStartPosBackward(String rawSnippet, int snippet_start_pos, Token token, int backward_skip_length) {
		final String raw = rawSnippet.substring(0, rawSnippet.length() - backward_skip_length);
		final int token_pos = raw.lastIndexOf(token.getToken());
		return (token_pos == -1) ? -1 : snippet_start_pos + token_pos;
	}

	public int getTokenStartPosBeforeBackwardStartPos(String rawSnippet, int snippet_start_pos, Token token, int backward_start_pos) {
		final String raw = rawSnippet.substring(0, backward_start_pos - snippet_start_pos);
		final int token_pos = raw.lastIndexOf(token.getToken());
		return (token_pos == -1) ? -1 : snippet_start_pos + token_pos;
	}

	private void settle(ASTNode node) {
		this.currentAstnodeId = controller.getAstnodeId(node);
		this.snippet = controller.getCurrentFileRaw().substring(node.getStartPosition(), node.getStartPosition() + node.getLength());
	}

}