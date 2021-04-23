#!/bin/bash
rm -r output
javac -classpath `hadoop classpath` *.java;
jar -cvf TextAnalyzer.jar .;
hdfs dfs -rm -r /input
hdfs dfs -copyFromLocal input/ /input;
hdfs dfs -rm -r /output;
hadoop jar TextAnalyzer.jar TextAnalyzer /input /output;
hdfs dfs -copyToLocal /output ./output;
