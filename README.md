# Project Overview

Projet Systèmes et Applications Répartis - INFO5 fait par:

- **FAWAZ Ali**
- **VITTET Brice**
- **MESBAH Abderrahmane**
- **BENHAMED Abdelhakim**
- **KHRIBECH Achraf**

## Project Structure

The project contains the following tasks:

1. **Task 1**: A simple broker and channel implementation, allowing to exchange bytes of data.
2. **Task 2**: Built on previous layer, this task introduce the MessageQueue and QueueBroker concept, that allows to send Messages in a Queue, that allow different tasks to communicate with each others, all this in a threaded envirenment.
3. **Task 3**: An event handling system, introducing event to handle some layers of the previous task in an event-driven approach, so in this task, we are implementing a mixt version somewhere between events world and threads world, keeping Channels and Broker threaded, and implementing MessageQueue and QueueBroker with events.
4. **Task 4**: This task re-implement the previous one in a fullu eventful way.

Each task is contained within its own package.

## Docs

Note that you can find the java docs in the `specification-javadoc` directory, 
you can open the `index.html` file in your browser to see the documentation.


## Running the Project in Eclipse

To work with this project in Eclipse, follow these steps:

### Step 1: Import the Project into Eclipse
1. Open Eclipse.
2. Go to `File` > `Import...`.
3. Select `Existing Gradle Project` and click `Next`.
4. Choose the root directory of this project and click `Finish`.
5. Eclipse will automatically detect the `build.gradle` file and set up the project for you.

> **Note**: To view the logs and any runtime configurations, check the `app.log` file in the `logs` directory.

### Step 3: Running Tests

To run tests for each task:
1. In the Gradle Tasks view, expand `verification`.
2. Double-click on `test` to execute all tests in the project.
3. Test results can be found in the `build/reports/tests/test/index.html` file after execution.

### Logging Configuration

Logging is handled by Tinylog, configured in the `tinylog.properties` file located in the root directory. You can adjust the logging level and other configurations here.


## Running the Project in Command Line

To run the project from the command line, follow these steps:

1. **Navigate to the Project Root**: Open a terminal and navigate to the root directory of the project.
   ```bash
   cd /path/to/project
   ```

2. **Build the Project**: Ensure that Gradle is installed. Run the following command to build the project and compile the code:
   ```bash
   ./gradlew build
   ```

3. **Run Tests from the Command Line**: To execute all tests across tasks, run:
   ```bash
   ./gradlew test
   ```

4. **View Test Reports**: After running tests, view the results by opening the HTML report:
   ```bash
   open build/reports/tests/test/index.html
   ```


# Deep Look

This project consists of four progressive tasks, each building on the previous one to develop a broker-based communication system in Java. The tasks focus on asynchronous message exchanges, inter-thread communication, and event-driven programming using brokers, channels, message queues, and an event pump.

## Task 1: Basic Asynchronous Broker Communication
### Overview
In Task 1, we set up the fundamental broker structure with minimal functionality, this is Task is implemented in threaded way, where tasks are running in parallel.

### Broker
Brokers  can connect to each other and exchange data via a simple connection mechanism using **Rdv** class that allows.

We can notice that the `BrokerManager` class is responsible for managing the brokers and their connections, 
providing a central point for broker creation and connection handling where brokers can find each other and establish connections.

`Rdv` class is used to establish a rendezvous point between two brokers, allowing them to wait for each other to connect, the first to 
call `rendezvous` will wait for the second to call `rendezvous` too, and then the connection will be established.

### Channel
Channels are abstracted to handle communication between tasks, managing basic read and write operations.

`Channel`is the result of the connection between two brokers, it is used to send and receive messages between them, it is a simple class that uses a `CircularBuffer` to store the messages.
`Channel`allows FIFO communication between brokers.

---

## Task 2: Asynchronous Communication with Message Queues
### Overview
Task 2 introduces asynchronous message handling through message queues, enabling brokers to manage multiple messages concurrently. 
This task implements a **QueueBroker** that uses `MessageQueue` instances to send and receive messages in a non-blocking way.

In `MessageQueue` Message sents will be stored in a queue waiting and a worker thread will be responsible for sending them, 
this will allow the broker to send multiple messages without blocking it's thread.

### QueueBroker
The **QueueBroker** class extends the, enabling brokers to manage message queues with more sophisticated handling of multiple messages and asynchronous event processing. Brokers can now use `MessageQueue` instances to send and receive messages without blocking each other.

`BrokerManager` continues to manage brokers and connections, now supporting more dynamic message handling across multiple brokers.

---

## Task 3: Mixt Communication with Events and Threads
### Overview
Task 3 builds upon Task 2 by introducing asynchronous message handling through message queues. This task implements an **EventPump** 
for non-blocking event processing, allowing brokers to operate asynchronously and manage multiple messages in parallel while 
keeping `QueueBroker` and `MessageQueue` synchronious .

### Broker and QueueBroker
The `QueueBroker` class is expanded to manage connections through **AcceptListener** and **ConnectListener** interfaces. 
This setup enables brokers to dynamically accept or connect to other brokers while having non Blocking behaviour on the higher level .

`BrokerManager` remains central in managing brokers, now with additional functionality for storing and handling queued connection requests via listeners.

### EventPump and Enhanced Events
Introducing an **EventPump** for asynchronous event processing, 
Task 3 uses events to manage broker connections, message exchanges, and queue operations. 
Events such as `ConnectEvent` and `AcceptEvent` are processed by the `EventPump`.

`EventPump` is sperated Thread that is responsible for handling events, it is a non-blocking way to handle events, 
it waits for events to be added to the queue, and then it will process them in a non-stop way.

### Tasks and ETasks
`Task` is an threaded Task where each instance of `Task` is a thread itself, this `Task` is used for Broker and Channel (The threaded part).

`ETask` is an event Task, it runs on the main Thread, this `ETask` is used for MessageQueue and QueueBroker (The event part), `Etask` also implements Runnable, 

the run function when called will process the events in the queue, thus jobs done by run function should be non-blocking and fast.

---

## Task 4: Full Event-Driven Communication
### Overview

Task 4 if the final task, it is a full eventful task, where all the communication is done through events,

> **Note**: In this task, there is no `Etask` as all task are eventful, only `Task` (Eventful) is used.

A design pdf (Mindmap) `Task4_Design.pdf` is provided in the root directory of the project, it shows the full architecture of this task.
