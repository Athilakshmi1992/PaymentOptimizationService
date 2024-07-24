package com.example.PaymentOptimizationAlgorithm.ServiceImpl;

import com.example.PaymentOptimizationAlgorithm.Pojo.Node;
import com.example.PaymentOptimizationAlgorithm.Service.PaymentService;
import jakarta.annotation.PostConstruct;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Service implementation for handling payment optimization using Bidirectional Dijkstra's algorithm.
 */
@Service
public class BidirectionalDijkstraPaymentService implements PaymentService {
    private final Map<String, Map<String, Integer>> graph = new ConcurrentHashMap<>();
    private final Map<String, Integer> branchCosts = new ConcurrentHashMap<>();
    private final ReadWriteLock lock = new ReentrantReadWriteLock();
    private final Lock readLock = lock.readLock();
    private final Lock writeLock = lock.writeLock();

    /**
     * Initializes the service with default branches and connections.
     * This method is called after the bean has been initialized.
     */
    @PostConstruct
    public void initialize() {
        // Adding default branches
        addBranch("A", 5);
        addBranch("B", 50);
        addBranch("C", 10);
        addBranch("D", 10);
        addBranch("E", 20);
        addBranch("F", 5);

        // Adding default connections
        addConnections("A", "B");
        addConnections("A", "C");
        addConnections("C", "B");
        addConnections("B", "D");
        addConnections("C", "E");
        addConnections("D", "E");
        addConnections("E", "D");
        addConnections("D", "F");
        addConnections("E", "F");
    }

    /**
     * Adds a branch with a specified cost.
     *
     * @param branch The branch identifier.
     * @param cost   The cost associated with the branch.
     * @return
     */
    @Override
    public ResponseEntity<String> addBranch(String branch, int cost) {
        writeLock.lock();
        try {
            branchCosts.put(branch, cost);
            return ResponseEntity.ok("Branch added successfully.");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to add branch: " + e.getMessage());
        } finally {
            writeLock.unlock();
        }
    }

    /**
     * Adds a connection between two branches.
     *
     * @param from The source branch.
     * @param to   The target branch.
     * @return
     */
    @Override
    public ResponseEntity<String> addConnections(String from, String to) {
        writeLock.lock();
        try {
            if (!branchCosts.containsKey(from) || !branchCosts.containsKey(to)) {
                return ResponseEntity.badRequest()
                        .body("Both branches must be added before adding edges.");
            }
            graph.computeIfAbsent(from, k -> new ConcurrentHashMap<>()).put(to, branchCosts.get(from));
            return ResponseEntity.ok("Connection added successfully.");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Failed to add edge: " + e.getMessage());
        } finally {
            writeLock.unlock();
        }
    }

    /**
     * Processes a payment from an origin branch to a destination branch using Bidirectional Dijkstra's algorithm.
     *
     * @param originBranch The starting branch.
     * @param destinationBranch The target branch.
     * @return A string representing the optimal path, or null if no path exists.
     */
    @Override
    public String processPayment(String originBranch, String destinationBranch) {
        if (originBranch == null || originBranch.isEmpty() || destinationBranch == null || destinationBranch.isEmpty()) {
            throw new IllegalArgumentException("Origin and destination branches must be specified.");
        }

        readLock.lock();
        try {
            if (originBranch.equals(destinationBranch)) {
                return originBranch;
            }
            if (!graph.containsKey(originBranch)) {
                return null;
            }
        } catch (Exception e) {
            throw new RuntimeException("Error while checking branches: " + e.getMessage(), e);
        } finally {
            readLock.unlock();
        }

        Map<String, Integer> distFromSource = new HashMap<>();
        Map<String, Integer> distFromTarget = new HashMap<>();
        Map<String, String> prevFromSource = new HashMap<>();
        Map<String, String> prevFromTarget = new HashMap<>();
        PriorityQueue<Node> pqFromSource = new PriorityQueue<>();
        PriorityQueue<Node> pqFromTarget = new PriorityQueue<>();

        try {
            distFromSource.put(originBranch, 0);
            distFromTarget.put(destinationBranch, 0);
            pqFromSource.add(new Node(originBranch, 0));
            pqFromTarget.add(new Node(destinationBranch, 0));

            Set<String> visitedFromSource = new HashSet<>();
            Set<String> visitedFromTarget = new HashSet<>();

            while (!pqFromSource.isEmpty() && !pqFromTarget.isEmpty()) {
                if (!pqFromSource.isEmpty()) {
                    Node nodeFromSource = pqFromSource.poll();
                    if (visitedFromSource.contains(nodeFromSource.id)) {
                        continue;
                    }
                    visitedFromSource.add(nodeFromSource.id);

                    if (visitedFromTarget.contains(nodeFromSource.id)) {
                        return buildPath(prevFromSource, prevFromTarget, nodeFromSource.id, originBranch, destinationBranch);
                    }

                    processNeighbors(nodeFromSource.id, distFromSource, prevFromSource, pqFromSource, true);
                }

                if (!pqFromTarget.isEmpty()) {
                    Node nodeFromTarget = pqFromTarget.poll();
                    if (visitedFromTarget.contains(nodeFromTarget.id)) {
                        continue;
                    }
                    visitedFromTarget.add(nodeFromTarget.id);

                    if (visitedFromSource.contains(nodeFromTarget.id)) {
                        return buildPath(prevFromSource, prevFromTarget, nodeFromTarget.id, originBranch, destinationBranch);
                    }

                    processNeighbors(nodeFromTarget.id, distFromTarget, prevFromTarget, pqFromTarget, false);
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Error while processing payment: " + e.getMessage(), e);
        }

        return null;
    }

    /**
     * Processes neighbors of the current node, updating distances and priority queues.
     *
     * @param currentNode The current node being processed.
     * @param distMap The distance map from the source or target.
     * @param prevMap The previous node map from the source or target.
     * @param pq The priority queue for processing nodes.
     * @param forward True if processing from the source, false if from the target.
     */
    private void processNeighbors(String currentNode, Map<String, Integer> distMap, Map<String, String> prevMap,
                                  PriorityQueue<Node> pq, boolean forward) {
        readLock.lock();
        try {
            Map<String, Integer> neighbors = forward ? graph.getOrDefault(currentNode, Collections.emptyMap())
                    : getReverseNeighbors(currentNode);

            for (Map.Entry<String, Integer> entry : neighbors.entrySet()) {
                String neighbor = entry.getKey();
                Integer weight = entry.getValue();

                int newDist = distMap.get(currentNode) + weight;

                if (newDist < distMap.getOrDefault(neighbor, Integer.MAX_VALUE)) {
                    distMap.put(neighbor, newDist);
                    prevMap.put(neighbor, currentNode);
                    pq.add(new Node(neighbor, newDist));
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Error while processing neighbors: " + e.getMessage(), e);
        } finally {
            readLock.unlock();
        }
    }

    /**
     * Retrieves reverse neighbors for a given node.
     *
     * @param node The node for which to find reverse neighbors.
     * @return A map of reverse neighbors and their costs.
     */
    private Map<String, Integer> getReverseNeighbors(String node) {
        readLock.lock();
        Map<String, Integer> reverseNeighbors = new HashMap<>();
        try {
            for (Map.Entry<String, Map<String, Integer>> entry : graph.entrySet()) {
                String neighbor = entry.getKey();
                Map<String, Integer> neighbors = entry.getValue();
                if (neighbors.containsKey(node)) {
                    reverseNeighbors.put(neighbor, neighbors.get(node));
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Error while getting reverse neighbors: " + e.getMessage(), e);
        }finally {
            readLock.unlock();
        }
        return reverseNeighbors;
    }

    /**
     * Constructs the path from the source to the destination branch using the maps of previous nodes.
     *
     * @param prevFromSource The map of previous nodes from the source.
     * @param prevFromTarget The map of previous nodes from the target.
     * @param meetingPoint The node where the paths from the source and target meet.
     * @param originBranch The origin branch of the payment.
     * @param destinationBranch The destination branch of the payment.
     * @return A string representing the optimal path from origin to destination.
     */
    private String buildPath(Map<String, String> prevFromSource, Map<String, String> prevFromTarget,
                             String meetingPoint, String originBranch, String destinationBranch) {
        List<String> path = new LinkedList<>();
        try {
            // Build path from source to meeting point
            for (String at = meetingPoint; at != null; at = prevFromSource.get(at)) {
                path.add(at);
            }
            Collections.reverse(path);

            // Build path from meeting point to destination
            for (String at = prevFromTarget.get(meetingPoint); at != null; at = prevFromTarget.get(at)) {
                path.add(at);
            }
        } catch (Exception e) {
            throw new RuntimeException("Error while building path: " + e.getMessage(), e);
        }

        return String.join(",", path);
    }
}
