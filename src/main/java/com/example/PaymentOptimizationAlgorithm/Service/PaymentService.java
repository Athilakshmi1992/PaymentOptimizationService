package com.example.PaymentOptimizationAlgorithm.Service;

import org.springframework.http.ResponseEntity;

/**
 * Service interface for handling payment-related operations.
 * Provides methods to add branches and connections to the payment network
 * and to process payments between branches.
 */
public interface PaymentService {

    /**
     * Processes a payment from an origin branch to a destination branch.
     *
     * @param originBranch      The ID of the origin branch from which the payment is to be sent.
     * @param destinationBranch The ID of the destination branch to which the payment is to be received.
     * @return The path representing the sequence of branches to use for the payment,
     *         or null if no valid path exists.
     * @throws IllegalArgumentException If the origin or destination branch is null or empty.
     */
    String processPayment(String originBranch, String destinationBranch);

    /**
     * Adds a new branch to the payment network with a specified processing cost.
     *
     * @param branch The ID of the branch to be added.
     * @param cost   The cost associated with processing payments through this branch.
     * @return
     * @throws IllegalArgumentException If the branch ID is null or empty, or if the cost is negative.
     */
    ResponseEntity<String> addBranch(String branch, int cost);

    /**
     * Adds a connection (edge) between two branches in the payment network.
     *
     * @param from The ID of the origin branch where the connection starts.
     * @param to   The ID of the destination branch where the connection ends.
     * @return
     * @throws IllegalArgumentException If either branch ID is null, empty, or does not exist in the network.
     */
    ResponseEntity<String> addConnections(String from, String to);
}
