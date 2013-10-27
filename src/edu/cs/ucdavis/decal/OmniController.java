package edu.cs.ucdavis.decal;

import java.io.File;
import java.io.IOException;
import java.sql.BatchUpdateException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Level;

import org.antlr.v4.runtime.Token;
import org.apache.commons.io.FileUtils;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.MethodInvocation;
import org.eclipse.jdt.core.dom.Name;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;

/**
 * Execute work flow in this controller.
 * @author jcwu
 *
 */
public class OmniController extends BaseController {
	private int currentProjectId;
	private int currentFileId;
	private String currentFileContent;

	private Map <CompilationUnit, String> compilaionUnitFileNameMap;
	private Collection <String> crossRefKeys;
	private List <Token> tokens;

	public OmniController() {
		super();

		this.currentProjectId = -1;
		this.currentFileId = -1;
		this.currentFileContent = null;

		this.compilaionUnitFileNameMap = new HashMap<CompilationUnit, String>();
		this.crossRefKeys = new HashSet<String>();
		this.tokens = null;
	}

	public OmniController addCrossRefKey(String crossRefKey) {
		crossRefKeys.add(crossRefKey);
		return this;
	}

	public Collection <String> getCrossRefKeys() {
		return crossRefKeys;
	}

	public OmniController addCompilationUnitFileName(CompilationUnit unit, String fileName) {
		compilaionUnitFileNameMap.put(unit, fileName);
		return this;
	}

	public String getCompilationUnitFileName(CompilationUnit unit) {
		return compilaionUnitFileNameMap.get(unit);
	}

	void retriveProjectId(String projectName, String sourcePath) {
		int id = database.retrieveProjectId(projectName, sourcePath);
		if (id == -1) {
			throw new IllegalStateException("retrieve project id error");
		}
		currentProjectId = id;
	}

	/**
	 * Retrieve file id for given sourceFilePath and
	 * tokenize that file and store result in self.token at the same time.
	 * @param sourceFilePath
	 */
	public void retriveCurrentFileNameId(String sourceFilePath) {
		int id = database.retrieveFileId(sourceFilePath, currentProjectId);
		if (id == -1) {
			throw new IllegalStateException("retrieve file id error");
		}
		try {
			this.currentFileContent = FileUtils.readFileToString(new File(sourceFilePath));
		} catch (IOException e) {
			logger.log(Level.SEVERE, "Read file content error", e);
		}
		currentFileId = id;
	}

	/**
	 * Main work flow.
	 */
	public void run() {
        System.out.println("Cleaning same project if exists...");
		clearProjectAstNodeInfo();
		System.out.println("Collecting ASTs...");
		collectAst();
		System.out.println("Annotating project "+ this.projectName + "...");
		annotateAst();
		System.out.println("Resolving cross reference...");
		resolveCrossReference();
	}

	void clearProjectAstNodeInfo() {
		retriveProjectId(this.projectName, this.sourcePath);
		database.clearProjectAstnode(currentProjectId);
	}

	void collectAst() {
		String[] sourceFilePaths = Util.collectFilePaths(this.sourcePath, new String[] {"java"});
		ASTParser parser = Util.getParser(this.libPath);
		parser.createASTs(sourceFilePaths, null, new String[0], requestor, null);
	}

	void annotateAst() {
		LabelAstVisitor labelVisitor = new LabelAstVisitor();
		for (Map.Entry<CompilationUnit, String> cuEntry: this.compilaionUnitFileNameMap.entrySet()) {
			String sourceFilePath = cuEntry.getValue();
			CompilationUnit unit = cuEntry.getKey();
			labelVisitor.reset();
			unit.accept(labelVisitor);
			this.showProgress(sourceFilePath);
			this.retriveCurrentFileNameId(sourceFilePath);
			this.tokens = Util.prepareTokens(this.currentFileContent);

			long nextVal = database.getNextSerial("entity_entity_id_seq") + 1; // nextval occupy 1 index
			labelVisitor.setNextVal(nextVal);

			batchAnnotateAstNode(unit, labelVisitor);
		    batchAnnotateMethodAstNode(labelVisitor);
			batchAnnotateToken(unit, labelVisitor);
		}
	}

	void batchAnnotateAstNode(CompilationUnit unit, LabelAstVisitor labelVisitor) {
		long nextVal = labelVisitor.getNextVal();
		Map<ASTNode, Integer> labelMapping = labelVisitor.getNodeLabel();
		Connection conn = database.getConnection();

		String insertAstStmt = "INSERT INTO entity ("
				+ "start_pos, length, "
				+ "start_line_number, start_column_number, "
				+ "end_line_number, end_column_number, "
				+ "nodetype_id, string, file_id, raw, parent_id) "
				+ "VALUES (?, ?, ?, ?, ?,  ?, ?, ?, ?, ?,  ?)";
		String insertCrossRefKeyStmt = "INSERT INTO cross_ref_key (entity_id, cross_ref_key) VALUES (?, ?);";
		PreparedStatement entityStmt = null;
		PreparedStatement crossRefStmt = null;

	    try {
	        entityStmt = conn.prepareStatement(insertAstStmt);
	        crossRefStmt = conn.prepareStatement(insertCrossRefKeyStmt);

	        for (Entry<ASTNode, Integer> entry: labelMapping.entrySet()) {
	        	ASTNode node = entry.getKey();
	            // offset starts from 0
	            final int start_pos = node.getStartPosition();
	            if (start_pos < 0) {
	                throw new IllegalStateException("containing astnode doesn't have physical location in source code " + node.toString());
	            }
	            Integer tmp_parent_id = labelMapping.get(node.getParent());  // 0 ~ # of ASTNodes, for root node its parent will be null
	            // collect info
	            final int length = node.getLength();
	            final int nodetype_id = node.getNodeType();
	            final String string = node.toString();  // code generated from AST node, not original source code
	            // make both line/column number start from 1, inclusive end
	            final int start_line_number = unit.getLineNumber(start_pos);
	            final int start_column_number = unit.getColumnNumber(start_pos) + 1;

	            final int end_line_number = unit.getLineNumber(start_pos + length - 1);
	            final int end_column_number = unit.getColumnNumber(start_pos + length - 1) + 1;

	            String crossRefKey = "";
	            if (node instanceof Name) {
	                Name n = (Name)node;
	                if (n.resolveBinding() != null) {
	                    crossRefKey = n.resolveBinding().getKey();
	                    crossRefKeys.add(crossRefKey);
	                    crossRefStmt.setLong(1, labelMapping.get(node) + nextVal);
	                    crossRefStmt.setString(2, crossRefKey);
	                    crossRefStmt.addBatch();
	                }
	            }

	            entityStmt.setInt(1, start_pos);
	            entityStmt.setInt(2, length);
	            entityStmt.setInt(3, start_line_number);
	            entityStmt.setInt(4, start_column_number);
	            entityStmt.setInt(5, end_line_number);
	            entityStmt.setInt(6, end_column_number);
	            entityStmt.setInt(7, nodetype_id);
	            entityStmt.setString(8, string);
	            entityStmt.setInt(9, currentFileId);
	            entityStmt.setString(10, currentFileContent.substring(start_pos, start_pos+length));
	            entityStmt.setLong(11, (tmp_parent_id == null)? -1 : tmp_parent_id + nextVal);

	            entityStmt.addBatch();
	        }
	        entityStmt.executeBatch();
	        crossRefStmt.executeBatch();
	    } catch (BatchUpdateException e) {
	        logger.log(Level.SEVERE, "Storing astnode information batch exception", e.getNextException());
	        e.getNextException().printStackTrace();
	    } catch (SQLException e) {
	        logger.log(Level.SEVERE, "Storing ASTNode information exception", e);
	        e.printStackTrace();
	    } finally {
	        PostgreSQLStorer.closeIt(entityStmt);
	    }
	}

	void batchAnnotateMethodAstNode(LabelAstVisitor labelVisitor) {
		long nextVal = labelVisitor.getNextVal();
		Map <ASTNode, Integer> labelMapping = labelVisitor.getNodeLabel();
		Connection conn = database.getConnection();
	    String insertMethodTable = "INSERT INTO method "
				+ "(entity_id, method_name, return_type, argument_type, full_signature, is_declare) VALUES "
				+ "(?,         ?,           ?,           ?,             ?,              ?); "
				;
		PreparedStatement methodStmt = null;

	    try {
	        methodStmt = conn.prepareStatement(insertMethodTable);
	        // update methods
	        for (Entry<ASTNode, Integer> entry: labelMapping.entrySet()) {
	        	ASTNode node = entry.getKey();
	            if (node instanceof MethodDeclaration) {
	                MethodDeclaration m = (MethodDeclaration)node;
	                IMethodBinding mbinding = m.resolveBinding();

	                String args = "";

	                try {
	                    for (SingleVariableDeclaration var: (List <SingleVariableDeclaration>) m.parameters()) {
	                        if (var.resolveBinding() == null || var.resolveBinding().getType() == null) {
	                            args = "";
	                            break;
	                        }
	                        args += var.resolveBinding().getType().getKey();
	                    }
	                    methodStmt.setLong(1, labelMapping.get(node) + nextVal);     // method_id
	                    methodStmt.setString(2, m.getName().toString());  // method_name
	                    methodStmt.setString(3, (m.getReturnType2() == null || m.getReturnType2().resolveBinding() == null)?
	                                            null : m.getReturnType2().resolveBinding().getKey());  // return_type
	                    methodStmt.setString(4, args);  // argument_type
	                    methodStmt.setString(5, (mbinding == null)? null : mbinding.getKey());  // full_signature
	                    methodStmt.setBoolean(6, true);  // is_declare
	                    methodStmt.addBatch();
	                } catch (NullPointerException e) {
	                    logger.log(Level.SEVERE, "Null pointer exception storing method declaration", e);
	                    e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
	                }
	            } else if (node instanceof MethodInvocation) {
	                MethodInvocation m = (MethodInvocation)node;
	                IMethodBinding mbinding = m.resolveMethodBinding();
	                String args = "";

	                try {
	                    for (Expression exp: (List <Expression>) m.arguments()) {
	                        if (exp.resolveTypeBinding() == null) {
	                            args = "";
	                            break;
	                        }
	                        args += exp.resolveTypeBinding().getKey();
	                    }
	                    methodStmt.setLong(1, labelMapping.get(node) + nextVal);     // method_id
	                    methodStmt.setString(2, m.getName().toString());  // method_name
	                    methodStmt.setString(3, (mbinding == null || mbinding.getReturnType() == null) ?
	                                             null : mbinding.getReturnType().getKey());  // return_type
	                    methodStmt.setString(4, args);  // argument_type
	                    methodStmt.setString(5, (mbinding == null) ? null : mbinding.getKey());  // full_signature
	                    methodStmt.setBoolean(6, false);  // is_declare
	                    methodStmt.addBatch();
	                } catch (NullPointerException e) {
	                    logger.log(Level.SEVERE, "Null pointer exception storing method invocation", e);
	                    e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
	                }
	            }

	        }
	        methodStmt.executeBatch();
	    } catch (BatchUpdateException e) {
	        logger.log(Level.SEVERE, "Storing method information batch exception", e.getNextException());
	        e.getNextException().printStackTrace();
	    } catch (SQLException e) {
	        logger.log(Level.SEVERE, "Store method node exception", e);
	        e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
	    } finally {
	        PostgreSQLStorer.closeIt(methodStmt);
	    }
	}

	void batchAnnotateToken(CompilationUnit unit, LabelAstVisitor labelVisitor) {
		long nextVal = labelVisitor.getNextVal();
		Map<ASTNode, Integer> labelMapping = labelVisitor.getNodeLabel();
		Connection conn = database.getConnection();

		String insertAstStmt = "INSERT INTO entity ("
				+ "start_pos, length, "
				+ "start_line_number, start_column_number, "
				+ "end_line_number, end_column_number, "
				+ "nodetype_id, string, file_id, raw, parent_id) "
				+ "VALUES (?, ?, ?, ?, ?,  ?, ?, ?, ?, ?,  ?)";
		PreparedStatement pstmt = null;

		LookupVisitor lookup = new LookupVisitor();

		try {
            pstmt = conn.prepareStatement(insertAstStmt);
            for (Token token: tokens) {

                try {
                    lookup.reset();
                    lookup.setToken(token);
                    // offset starts from 0
                    unit.accept(lookup);
                    ASTNode parentNode = lookup.getLastSeenNode();
                    Integer tmp_parent_id = labelMapping.get(parentNode);  // may be null
                    // collect info
                    final String string = token.getText();

                    final int token_start_pos = token.getStartIndex();
                    final int token_end_pos = token.getStopIndex(); // inclusive end pos

                    final int start_line_number = token.getLine();
                    final int start_column_number = token.getCharPositionInLine() + 1;

                    final int end_line_number = unit.getLineNumber(token_end_pos);
                    final int end_column_number = unit.getColumnNumber(token_end_pos) + 1;

                    final int nodetype_id = token.getType() + PostgreSQLStorer.tokenBase;

                    pstmt.setInt(1, token_start_pos);
                    pstmt.setInt(2, token_end_pos - token_start_pos + 1);
                    pstmt.setInt(3, start_line_number);
                    pstmt.setInt(4, start_column_number);
                    pstmt.setInt(5, end_line_number);
                    pstmt.setInt(6, end_column_number);

                    pstmt.setInt(7, nodetype_id);
                    pstmt.setString(8, string);
                    pstmt.setInt(9, currentFileId);
                    pstmt.setString(10, currentFileContent.substring(token_start_pos, token_end_pos + 1));
                    pstmt.setLong(11, tmp_parent_id + nextVal);

                    pstmt.addBatch();
                } catch (NullPointerException e) {
                    logger.log(Level.SEVERE, "Null Pointer Exception", e);
                    e.printStackTrace();
                }
            }
            pstmt.executeBatch();
        } catch (BatchUpdateException e) {
            logger.log(Level.SEVERE, "Storing token information batch exception", e.getNextException());
            e.getNextException().printStackTrace();
        } catch (SQLException e) {
            logger.log(Level.SEVERE, "Storing token information", e);
            e.printStackTrace();
        } finally {
           PostgreSQLStorer.closeIt(pstmt);
        }
    }

	void resolveCrossReference() {
		final int crossRefKeysSize = crossRefKeys.size();
		int i = 1;
		boolean resolved = false;
		for (String crossRefKey: crossRefKeys) {
			System.out.print(String.format("(%d/%d) resolve cross reference", i, crossRefKeysSize));
			i++;
			for (Map.Entry<CompilationUnit, String> entry: compilaionUnitFileNameMap.entrySet()) {
				CompilationUnit unit = entry.getKey();
				String filename = entry.getValue();
				ASTNode declaringNode = unit.findDeclaringNode(crossRefKey);
				if (declaringNode != null) {
					retriveCurrentFileNameId(filename);  // set current file to corresponding id
					saveCrossRefAstNode(declaringNode, crossRefKey);
					resolved = true;
					break;
				}
			}
			System.out.println((resolved ? " o ": " x " ) + crossRefKey);
			resolved = false;
		}
	}

	/**
	 * All entities having crossRefKey as cross reference key is declared in ASTNode node.
	 * Store this result in database
	 * @param declaringNode
	 * @param crossRefKey
	 */
	private void saveCrossRefAstNode(ASTNode declaringNode, String crossRefKey) {
		final int start_pos = declaringNode.getStartPosition();
		final int length = declaringNode.getLength();
		final int nodetype_id = declaringNode.getNodeType();
		database.saveCrossRefAstNode(start_pos, length, nodetype_id, currentFileId, crossRefKey);
	}

	private static int progressCount = 0;
	public void showProgress(String sourceFilePath) {
		final int projectSize = compilaionUnitFileNameMap.size();
		OmniController.progressCount++;
		System.out.println(String.format("(%d/%d) %s", OmniController.progressCount, projectSize, sourceFilePath));
	}
}