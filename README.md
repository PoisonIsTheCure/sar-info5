# Project Overview

This project is a collection of modular tasks that showcase various implementations related to message queues, broker management, and event handling systems. Each task builds upon a fundamental architecture to demonstrate different aspects of software design for handling messages and events, focusing on modularity and testing.

## Project Structure

The project contains the following tasks:

1. **Task 1**: A simple broker and channel implementation, allowing to exchange bytes of data.
2. **Task 2**: Built on previous layer, this task introduce the MessageQueue and QueueBroker concept, that allows to send Messages in a Queue, that allow different tasks to communicate with each others, all this in a threaded envirenment.
3. **Task 3**: An event handling system, introducing event to handle some layers of the previous task in an event-driven approach, so in this task, we are implementing a mixt version somewhere between events world and threads world, keeping Channels and Broker threaded, and implementing MessageQueue and QueueBroker with events.
4. **Task 4**: This task re-implement the previous one in a fullu eventful way.

Each task is contained within its own package.

## Running the Project in Eclipse

To work with this project in Eclipse, follow these steps:

### Step 1: Import the Project into Eclipse
1. Open Eclipse.
2. Go to `File` > `Import...`.
3. Select `Existing Gradle Project` and click `Next`.
4. Choose the root directory of this project and click `Finish`.
5. Eclipse will automatically detect the `build.gradle` file and set up the project for you.

### Step 2: Run Specific Tasks

*TODO ...*

> **Note**: To view the logs and any runtime configurations, check the `app.log` file in the `logs` directory.

### Step 3: Running Tests

To run tests for each task:
1. In the Gradle Tasks view, expand `verification`.
2. Double-click on `test` to execute all tests in the project.
3. Test results can be found in the `build/reports/tests/test/index.html` file after execution.

### Logging Configuration

Logging is handled by Tinylog, configured in the `tinylog.properties` file located in the root directory. You can adjust the logging level and other configurations here.
