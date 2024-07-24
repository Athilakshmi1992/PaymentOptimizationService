package com.example.PaymentOptimizationAlgorithm.Pojo;

public class Node implements Comparable<Node> {
    public String id;
    public int cost;

    public Node(String id, int cost) {
        this.id = id;
        this.cost = cost;
    }

    @Override
    public int compareTo(Node other) {
        return Integer.compare(this.cost, other.cost);
    }
}