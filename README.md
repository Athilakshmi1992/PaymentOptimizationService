Payment Optimization Algorithm

Overview

The PaymentOptimizationAlgorithm project provides a solution to compute the cheapest way to make a payment between two branches using Bidirectional Dijkstra's algorithm. The solution is implemented in Java and is designed to be thread-safe, scalable, and easy to extend with new branches and connections.

Problem Statement

Due to regulatory restrictions, direct payments between all bank branches are not possible. The goal is to determine the cheapest path for a payment between two branches given the following constraints:

Branch Connections: Branches have predefined connections.
Branch Costs: Each branch has a cost associated with processing outgoing payments.
Objective: Compute the cheapest sequence of branches from an origin branch to a destination branch.
Example
Given the branch connections and costs:

Branch Costs:

A: 5

B: 50

C: 10

D: 10

E: 20

F: 5

Branch Connections:

A to B

A to C

C to B

B to D

C to E

D to E

E to D

D to F

E to F

To find the cheapest way to make a payment from branch A to branch D, the solution would determine the optimal sequence of branches.

Solution

Key Components

BidirectionalDijkstraPaymentService: Service class implementing the PaymentService interface to calculate the cheapest path using Bidirectional Dijkstra's algorithm.

PaymentService Interface: Defines the contract for payment processing with a method to find the cheapest payment sequence.

Node Class: Represents a branch with associated cost for use in the priority queues.

How It Works

Initialization: Default branches and connections are added to the system.

Branch Addition: Allows adding new branches and their costs.

Connection Addition: Allows adding connections between branches.

Payment Processing: Uses Bidirectional Dijkstra's algorithm to find the cheapest path between the origin and destination branches.
Methods

addBranch(String branch, int cost): Adds a new branch with a specified cost.

addConnections(String from, String to): Adds a connection between two branches.

processPayment(String originBranch, String destinationBranch): Calculates and returns the cheapest path from the origin branch to the destination branch as a comma-separated string.

Concurrency and Thread Safety

The service implementation uses ReadWriteLock to ensure thread safety when accessing and modifying shared resources like the graph and branch costs. This allows multiple threads to safely process payments concurrently.

REST API

The solution exposes a REST API to interact with the PaymentService interface. You can integrate this API to interact with the payment optimization service.

Testing

The implementation includes sufficient functional test coverage to validate the correctness of the payment processing logic.

Running the Application
Clone the Repository: git clone <repository-url>

Build the Project: mvn clean install

Run the Application.

Future Extensions

Adding Branches: Use addBranch to add new branches.

Adding Connections: Use addConnections to add new links between branches.

Scaling: The solution is designed to handle the addition of new branches and connections efficiently.

