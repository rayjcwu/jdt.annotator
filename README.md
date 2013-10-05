jdt.annotator
==============
This project is based on Eclipse JDT to annotate java source files. Results are saved in database, you could get all information in massive join `astnode_all` view.

Download link of binary file [annotator.jar](https://dl.dropboxusercontent.com/u/15553400/annotator.jar)

# Usage
`java -jar annotator.jar [opts]`, `-s`, `-d`, `-p` are required options to start annotating.

```
usage: annotator.jar
 -c,--clear           clear all annotated astnode information in specified
                      project before annotating
 -d,--jdbc <arg>      jdbc url, currently only support postgresql
                      (jdbc:postgresql://ip:port/database)
 -p,--project <arg>   project name
 -r,--reset           reset all annotated astnode information in database
                      [need to specify --jdbc]
 -s,--src <arg>       absolute root path of files
```

# Trouble shooting

* `outOfMemoryException`: Allocating more memory for JVM by `java -Xmx2048m -jar …`
* `resolve more than on astnode`: use `-c` option to clear annotated result of specified project.
* create database in postgresql: `$> createdb database_name` in shell or `CREATE DATABASE database_name` in SQL.
