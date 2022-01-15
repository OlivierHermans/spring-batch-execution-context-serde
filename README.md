Spring batch reads the last job run's execution parameters (see SimpleJobExplorer::getLastJobExecution).
It uses the serializer configured in the JobExplorer to do so. Configuring the same serializer for the JobRepository and JobExplorer should yield a solution where execution contexts can be read and written without problems.

Tested a last job run execution context with following content:
* {"@class":"java.util.HashMap"}
* {}
* {"person":{"firstName":"Pipo","lastName":"De Clown","birthDate":[2022,1,15]}}

All three execution contexts can be deserialized and the job runs to completion every time.
