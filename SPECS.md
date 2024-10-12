# Specification File
This file contains the spesification of the tasks, it allows user to understand how does this code behave and how this code is intended to be used

## Task3

### How does Event and Event-Based Tasks work:

Event-Pump is equivalent to executor in Java, it is a thread on it's own.

Event-Based Tasks get the EventPump along with their constructor,Those task have also 
a post function, and **only** those tasks are allowed to post Events on their Event-Pump.

Each Event knows the Task that Posted it.

Events **react** function should be non-blocking, any Blocking behavior in side this function stops EventPump and therefor all the others waiting tasks.

Tasks are responsible for breaking down their work into small, non-blocking Runnables *Events*.