# Candidate Microservices Model

Here we describe the model of a candidate microservice (CM) when refactoring a monolithic application.
This is a very different scenario compared to when building a new microservice.
This document is not addressing the latter. A candidate microservice is essentially an overlay over the structure
of an existing monolithic application. This model uses the same semantics as the `Application Model`.

A CM will have a footprint and dependencies on the monolith that can be described in terms of these entities from the monolith:
1. `program` - A `program` may belong to one or more CMs
2. `user interface` - A `UI` may be interacting with one or more CMs 
3. `resources` - there are several types of `resources`, each of which may interact with one or more CMs:
    1. `table`
    2. `file`
    3. `document`
    4. `queue`
    5. `external application service`
    6. ...
4. `service` - this is a logical entity that may be associated with a corresponding physical `service entry`. One `service` will belong to only one `CM`
5. `transaction` - A `transaction` may span across one or more CMs..
6. `job` - this can have a second level of `job step`. It is not yet clear if / how CMs will map to `jobs` and `job steps`
7. `procedure` -  for example, a Java method or a C++ function. A `procedure` will belong to a `program`

The following relationships will be relevant to a CM. Cardinality mentioned in square brackets:
1. `Program` A -to- `program` B (this is actually at the `procedure` level and we may need to go there)
    1. A `procedure` in A calls a `procedure` in B statically - [n:n]
    2. A `procedure` in A calls a `procedure` in B dynamically - [n:n]
    3. A implements or extends B (for object-oriented languages) - [n:n]
    4. A instantiates B (for object-oriented languages) - [n:n]
2. `Program` A to  `resource` R (this is actually at the `procedure` level and we may need to go there)
    1. A `procedure` in A performs some operation on `resource` R. For example C/R/U/D with a `table` or send/receive with a `queue` - [n:n]
3. `User interface` U to `service` S
    1. U invokes S - [n:n]
4. `Service` S to `transaction` T
    1. The execution flow of S includes the execution of T - 1:n (in COBOL-CICS/IMS this may be 1:1 - need to validate)
5. `Transaction` T to `resource` R
    1. T performs operation on `resource` R - [n:n]
6. ...
