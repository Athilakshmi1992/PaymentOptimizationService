package com.example.PaymentOptimizationAlgorithm.UnitTests;

import com.example.PaymentOptimizationAlgorithm.Service.PaymentService;
import com.example.PaymentOptimizationAlgorithm.ServiceImpl.BidirectionalDijkstraPaymentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

import static org.junit.jupiter.api.Assertions.*;

class BidirectionalDijkstraPaymentServiceTest {

    private PaymentService paymentService;

    private static final int NUM_THREADS = 10;
    private static final int NUM_BRANCHES = 50;
    private static final int NUM_EDGES = 100;
    private static final int NUM_PAYMENTS = 20;

    private static final Logger logger = Logger.getLogger(BidirectionalDijkstraPaymentServiceTest.class.getName());
    @BeforeEach
    void setUp() {
        paymentService = new BidirectionalDijkstraPaymentService();
    }

    @Test
    void testAddBranch() {
        paymentService.addBranch("A", 5);
        assertDoesNotThrow(() -> paymentService.addBranch("B", 10));
    }

    @Test
    void testaddConnections() {
        paymentService.addBranch("A", 5);
        paymentService.addBranch("B", 10);
        assertDoesNotThrow(() -> paymentService.addConnections("A", "B"));
    }

    @Test
    void testaddConnectionsThrowsExceptionWhenBranchesNotAdded() {
        Exception exception = assertThrows(IllegalArgumentException.class, () -> paymentService.addConnections("A", "B"));
        assertEquals("Both branches must be added before adding edges.", exception.getMessage());
    }

    @Test
    void testProcessPaymentWithSingleBranch() {
        paymentService.addBranch("A", 5);
        assertEquals("A", paymentService.processPayment("A", "A"));
    }

    @Test
    void testProcessPaymentNoPath() {
        paymentService.addBranch("A", 5);
        paymentService.addBranch("B", 10);
        assertNull(paymentService.processPayment("A", "B"));
    }

    @Test
    void testProcessPaymentDirectPath() {
        paymentService.addBranch("A", 5);
        paymentService.addBranch("B", 10);
        paymentService.addConnections("A", "B");
        assertEquals("A,B", paymentService.processPayment("A", "B"));
    }

    @Test
    void testProcessPaymentIndirectPath() {
        paymentService.addBranch("A", 5);
        paymentService.addBranch("B", 10);
        paymentService.addBranch("C", 15);
        paymentService.addConnections("A", "B");
        paymentService.addConnections("B", "C");
        assertEquals("A,B,C", paymentService.processPayment("A", "C"));
    }

    @Test
    void testProcessPaymentBidirectional() {
        paymentService.addBranch("A", 5);
        paymentService.addBranch("B", 10);
        paymentService.addBranch("C", 15);
        paymentService.addConnections("A", "B");
        paymentService.addConnections("C", "B");
        assertNull( paymentService.processPayment("A", "C"));
    }

    @Test
    void testProcessPaymentWithNoSuchBranch() {
        paymentService.addBranch("A", 5);
        assertNull(paymentService.processPayment("A", "B"));
    }

    @Test
    void testProcessPaymentReversePath() {
        paymentService.addBranch("A", 5);
        paymentService.addBranch("B", 10);
        paymentService.addBranch("C", 15);
        paymentService.addConnections("A", "B");
        paymentService.addConnections("C", "B");
        assertNull(paymentService.processPayment("C", "A"));
    }

    @Test
    void testProcessPaymentComplexPath() {
        paymentService.addBranch("A", 5);
        paymentService.addBranch("B", 10);
        paymentService.addBranch("C", 15);
        paymentService.addBranch("D", 20);
        paymentService.addConnections("A", "B");
        paymentService.addConnections("B", "C");
        paymentService.addConnections("C", "D");
        paymentService.addConnections("A", "D");
        assertEquals("A,D", paymentService.processPayment("A", "D"));
    }

    @Test
    void testProcessPaymentCyclicPath() {
        paymentService.addBranch("A", 5);
        paymentService.addBranch("B", 10);
        paymentService.addBranch("C", 15);
        paymentService.addConnections("A", "B");
        paymentService.addConnections("B", "C");
        paymentService.addConnections("C", "A");
        assertEquals("A,B,C", paymentService.processPayment("A", "C"));
    }

    @Test
    void testProcessPaymentMultiplePaths() {
        paymentService.addBranch("A", 5);
        paymentService.addBranch("B", 10);
        paymentService.addBranch("C", 15);
        paymentService.addBranch("D", 20);
        paymentService.addBranch("E", 25);
        paymentService.addConnections("A", "B");
        paymentService.addConnections("B", "C");
        paymentService.addConnections("C", "D");
        paymentService.addConnections("D", "E");
        paymentService.addConnections("A", "C");
        paymentService.addConnections("B", "D");
        paymentService.addConnections("C", "E");
        assertEquals("A,C,E", paymentService.processPayment("A", "E"));
    }

    @Test
    void testProcessPaymentPathNotFound() {
        paymentService.addBranch("A", 5);
        paymentService.addBranch("B", 10);
        paymentService.addBranch("C", 15);
        paymentService.addBranch("D", 20);
        paymentService.addConnections("A", "B");
        paymentService.addConnections("C", "D");
        assertNull(paymentService.processPayment("A", "D"));
    }

    @Test
    public void testConcurrentAddBranchAndProcessPayment() throws InterruptedException {
        CountDownLatch latch = new CountDownLatch(2);

        Runnable addBranchTask = () -> {
            try {
                paymentService.addBranch("G", 30);
            } finally {
                latch.countDown();
            }
        };

        Runnable processPaymentTask = () -> {
            try {
                String result = paymentService.processPayment("A", "G");
                assertNotNull(result);
            } finally {
                latch.countDown();
            }
        };

        ExecutorService executor = Executors.newFixedThreadPool(2);
        executor.submit(addBranchTask);
        executor.submit(processPaymentTask);

        latch.await();
        executor.shutdown();
    }

    @Test
    public void performanceTest() throws InterruptedException {
        ExecutorService executor = Executors.newFixedThreadPool(NUM_THREADS);

        long startTime = System.nanoTime();

        // Add branches concurrently
        for (int i = 0; i < NUM_BRANCHES; i++) {
            final int branchId = i;
            executor.submit(() -> paymentService.addBranch("branch" + branchId, branchId));
        }

        // Add edges concurrently
        for (int i = 0; i < NUM_EDGES; i++) {
            final int fromId = i % NUM_BRANCHES;
            final int toId = (i + 1) % NUM_BRANCHES;
            executor.submit(() -> paymentService.addConnections("branch" + fromId, "branch" + toId));
        }

        // Process payments concurrently
        for (int i = 0; i < NUM_PAYMENTS; i++) {
            final int originId = i % NUM_BRANCHES;
            final int destinationId = (i + 2) % NUM_BRANCHES;
            executor.submit(() -> {
                String path = paymentService.processPayment("branch" + originId, "branch" + destinationId);
                assertNotNull(path, "Path should not be null");
                logger.info("Path from branch" + originId + " to branch" + destinationId + ": " + path);
            });
        }

        executor.shutdown();
        executor.awaitTermination(1, TimeUnit.MINUTES);

        long endTime = System.nanoTime();
        long duration = (endTime - startTime) / 1_000_000; // Convert to milliseconds
        logger.info("Performance test completed in " + duration + " ms");
    }
}
