jdt.annotator
==============
This project is based on [Eclipse JDT](http://help.eclipse.org/kepler/index.jsp?nav=%2F3) and [ANTLR 4](http://antlr.org/) to annotate java source files. Results are saved in database, you could get all information in massive join `entity_all` view.

Binary file [jdt.annotator.jar](https://dl.dropboxusercontent.com/u/15553400/jdt.annotator.jar)

# Usage
`java -jar annotator.jar --src <path> [options]`, `--src` is required option to start annotating.

```
 -d,--dbname <arg>     database name to connect to (default: "entity")
 -H,--host <arg>       database server host ip (default: "localhost")
 -l,--lib <arg>        absolute root path of libraries (.jar)
 -P,--port <arg>       database server port (default: "5432")
 -p,--project <arg>    project name (default: folder name containing source code)
 -r,--reset            reset all annotated astnode information in database
 -s,--src <arg>        absolute root path of source code files
 -U,--username <arg>   (optional) username, must specify password as well
 -W,--password <arg>   (optional) password, must specify username as well
```

* If you omit `-p` option, program will use folder name containing source code as default project name.
* If your project use ant or maven to compile, it may specify dependencies in `build.xml` or `pom.xml`. If you want to annotate type, method or other information from those library, you need to download those dependencies first and specify the library folder in `-l` option.
  * Maven: type `mvn org.apache.maven.plugins:maven-dependency-plugin:2.7:copy-dependencies -DoutputDirectory=/your/library/folder` in project root folder. Then maven will download all dependencies .jars to that folder.
  * Ant: To Be Completed
  

# Schema
View `entity_all` combines table `project`, `file`, `entity`, `nodetype` and `cross_ref`. Table method store information about all methods in this source code.

| Table         | Column        | Description  |
| ------------- |--------------| --------|
| entity_all   | entity_id    | serial number of node added into database, it **DOESN'T** mean the order of appearance in source code |
| | start_pos | starting position of this node in file |
| | length | length of this node |
| | end_pos | ending position of this node in file (exclusive). If you store source code in a string, then `code.substring(start_pos, end_pos)` will give you source code snippet of this astnode or token. |
| | start_line_number | this entity starts at (line number, column number) in file, both start from `1` to conform with vim |
| | start_column_number | |
| | end_line_number | this enetity ends at (line number, column number) in file. |
| | end_column_number | |
| | nodetype_id | For ASTNode, `nodetype_id < 100`. For Token, `nodetype_id >= 100` |
| | nodetype | name of the type of this node |
| | file_id | |
| | file_name | absolute file name |
| | project_id | |
| | project_name | given project name |
| | project_path | given source code folder, `(project_name, project_path)` uniquely specify a "project"|
| | string | *formatted* code snippet of this node |
| | raw | code snippet of this node |
| | cross_ref_key | used to figure out where is this node declared. Only [Name](http://help.eclipse.org/kepler/index.jsp?topic=%2Forg.eclipse.jdt.doc.isv%2Freference%2Fapi%2Forg%2Feclipse%2Fjdt%2Fcore%2Fdom%2FName.html) type `ASTNode` will have non-null value |
| | parent_id | parent entity's `entity_id`, `parent_id` of root node is `-1`. |
| | declared_id | if this node is declared in another place **in this project**, this id could find out that node. if it's declared in another library, it won't show up.|
| method | entity_id | |
| | method_name | |
| | return_type | |
| | argument_type | |
| | full_signature | full type descriptor for this method |
| | is_declare | boolean value to indicate if this is a method declaration or not|


1 minute quick guide for the type descriptor used in method table: 

The rule for a method descriptor is `Class` . `method name` ( `method arguments (no comma)` ) `return type` `exceptions (each starting with |)` 

Therefore the descriptor for `main` method defined in `package demo.example` throwing `IOException` and `SQLException` is `Ldemo/example/Main;.main([Ljava/lang/String;)V|Ljava/io/IOException;|Ljava/sql/SQLException;`. 

| type descriptor | type | description |
|-----------------|------| ------------|
| B	| byte | |
| C | char | |
| D	| double | | 
| F	| float | |
| I	| int | |
| J	| long | |
| L *ClassName* ; | Class | java.lang.String -> Ljava/lang/String; <br /> Map <String, Integer> -> Ljava/util/Map<Ljava/lang/String;Ljava/lang/Integer;>;|
| S	| short	| |
| Z | boolean | |
| [	| reference	| int [][] -> [[I, open bracket only. `...` equals to `[` |
| V | void | |


Check [JVM type descriptor](http://docs.oracle.com/javase/specs/jvms/se7/html/jvms-4.html#jvms-4.3) for official documentation.



# How it works

* Use `JDT` to generate ASTs for all files and cross reference for all `Name` nodes.
* Whenever visitor visit an ASTNode, store ASTNode information in database.
* Collect all tokens, for each token, use visitor to check which is the deepest ASTNode containing that token.
* Take all cross reference keys generated in first step, figure out where is each `Name` node declared.

For documentation of each ASTNode, refer [jdt.core.dom](http://help.eclipse.org/kepler/index.jsp?topic=%2Forg.eclipse.jdt.doc.isv%2Freference%2Fapi%2Forg%2Feclipse%2Fjdt%2Fcore%2Fdom%2Fpackage-summary.html).

# How to modify
To rebuild, you need to install [Maven 3](http://maven.apache.org/). If you want to modify lexer, you need to install [ANTLR 4](http://antlr.org/) as well.

### To modify lexer generated by ANTLR

* Modify `Java.g4` in `src/org/antlr`
* run `generate.sh` to generate new lexer and parser
* rebuild `.jar`

### To rebuild `.jar`

* run `mvn clean package` in project root path, you will find the `.jar` file in `target/`


# Trouble shooting

* `outOfMemoryException`: Allocating more memory for JVM by `java -Xmx2048m -jar …`
* create database in postgresql: `$> createdb database_name` in shell or `CREATE DATABASE database_name` in SQL.