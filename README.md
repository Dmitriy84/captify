"# captify" 

## Test task
Problem:
You are provided with the *.csv.gz file containing some data about plane flights.

Data has the following schema
"YEAR","QUARTER","MONTH","DAY_OF_MONTH","DAY_OF_WEEK","FL_DATE","ORIGIN","DEST"

You'll need to extract several statistics out of the data file:
 � List of all airports with total number of planes for the whole period that arrived to each airport
 � Non-Zero difference in total number of planes that arrived to and left from the airport
 � Do the point 1 but sum number of planes separately per each week
 � Write some tests for the implemented functions
Each point 1-3 should produce a separate output file.

## Example of data processing:
Given the following input

"YEAR","QUARTER","MONTH","DAY_OF_MONTH","DAY_OF_WEEK","FL_DATE","ORIGIN","DEST",
2014,1,1,1,3,2014-01-01,"JFK","LAX",
2014,1,1,5,7,2014-01-05,"JFK","KBP",
2014,1,1,6,1,2014-01-06,"KBP","LAX",
2014,1,1,8,3,2014-01-08,"JFK","LAX",
2014,1,1,12,7,2014-01-12,"JFK","KBP",
2014,1,1,13,1,2014-01-13,"KBP","LAX",

The output will be:
 � First task
  � LAX 4
  � KBP 2
  � JFK 0
 � Second Task
  � JFK -4
  � LAX +4
 � Third task:
  � W1
   � LAX 2
   � KBP 1
   � JFK 0
  � W2
   � LAX 2
   � KBP 1
   � JFK 0
 � Fourth task - whatever tests you'd like to write.

## Run
```
mvn exec:java
```
or
```
mvn exec:java -Dexec.args="<path_to_csv_gz_file1> <path_to_csv_gz_file2> ... <path_to_csv_gz_fileN>"
``` 

## Run tests
```
mvn test allure:report allure:serve
```