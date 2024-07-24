package com.example.PaymentOptimizationAlgorithm.Controller;

import com.example.PaymentOptimizationAlgorithm.CustomException.NoDefinedPathException;
import com.example.PaymentOptimizationAlgorithm.Service.PaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * REST controller for handling payment-related operations.
 */
@RestController
@RequestMapping("/payment")
public class PaymentController {

    @Autowired
    private PaymentService paymentService;

    /**
     * Adds a new branch with a specified cost.
     *
     * @param branch The identifier of the branch to be added.
     * @param cost   The cost associated with the branch.
     * @return A response indicating the result of the operation.
     */
    @PostMapping("/branch")
    public ResponseEntity<String> addBranch(@RequestParam String branch, @RequestParam int cost) {
        return paymentService.addBranch(branch, cost);
    }

    /**
     * Adds a new edge (connection) between two branches.
     *
     * @param from The identifier of the source branch.
     * @param to   The identifier of the target branch.
     * @return A response indicating the result of the operation.
     */
    @PostMapping("/edge")
    public ResponseEntity<String> addEdge(@RequestParam String from, @RequestParam String to) {
        return  paymentService.addConnections(from, to);
    }

    /**
     * Processes a payment from an origin branch to a destination branch.
     * Uses the Bidirectional Dijkstra's algorithm to find the optimal path.
     *
     * @param originBranch The starting branch for the payment.
     * @param destinationBranch The target branch for the payment.
     * @return A response containing the optimal path, or an error message if no path is found.
     * @throws NoDefinedPathException If no defined path exists between the origin and destination branches.
     */
    @GetMapping("/process")
    public ResponseEntity<String> processPayment(@RequestParam String originBranch, @RequestParam String destinationBranch) {
        String result = paymentService.processPayment(originBranch, destinationBranch);
        if (result == null) {
            // If no path is found, throw a custom exception
            throw new NoDefinedPathException("No defined path between " + originBranch + " and " + destinationBranch);
        } else {
            // Return the path found as the response
            return ResponseEntity.ok().body(result);
        }
    }
}
