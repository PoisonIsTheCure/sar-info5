
You will specify, design, implement, and test a communication layer between “tasks”, 
useful to establish communication channels, used to send and receive bytes. 

Your specification will not assume if tasks run within the same process or the same machine.

Your design, corresponding to your specification, will assume that tasks run within
the same process. It will also assume the use of byte-oriented circular buffers
to implement channels.

You will create a local git repository in one of your directory,
let's say "info5.sar":

    $ mkdir ~/info5.sar
    $ cd ~/info5.sar
    $ git init

You will surrender this git at the end of today's classwork.

That git repository must be an Eclipse workspace, with projects.

    $ eclipse -data ~/info5.sar
    
You may use other IDEs, to do your work, but the Eclipse workspace
must be functional and the projects must compile and run.

The first project will be called "task1". Make sure to have
the proper .gitignore to avoid adding class files to the repository.

You will create a first branch called "task1" to work on your
project "task1". 

From this branch, you will create four other branches:
     - task1.specification
     - task1.design
     - task1.implementation
     - task1.tests

And you will use these different branches to track the evolution
of the specification, the design, the code, and tests as if they
were done by four different developers.

  - Updates to the specification must be done in "task1.specification"
    then merg it back to "task1". 
  - Updates to the design must be done in "task1.design"
    then merge  it back to "task1". 
  - Updates to the implementation must be done in "task1.implementation"
    then merge it back to "task1". 
  - Updates to the tests must be done in "task1.tests"
    then merge it back to "task1". 

The following organization of your work is **mandatory**
and should be visible in your git log as different commits
on the relevant branches.

  - You will write the specification first.
  - Then, you will write the tests.
  - Then the design
  - Then the implementation, as concrete classes
    extending the given abstract classes.
  - And finally the testing.

For the test, you will implement a simple echo server. An echo server
accepts the connect of any number of clients and echoes back anything
a client sends.

A test client will loop over and over the following steps:
  - connect to the server
  - send a sequence of bytes, representing the number from 1 to 255
  - test that these bytes are echoed properly by the server
  - disconnect.

You will test with one server and several clients.

At the end of the classwork, you will make sure to git-add and git-commit
and you will surrender your git repository as an archive (zip or tar).
Then, as homework, you will finish the given task. You will surrender
your git before the next classroom lecture, as an archive (zip or tar).