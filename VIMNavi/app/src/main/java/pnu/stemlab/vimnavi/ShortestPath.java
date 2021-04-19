package pnu.stemlab.vimnavi;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NavigableSet;
import java.util.TreeSet;

public class ShortestPath {
    public static List<String> getRoutingPath(DGraph.Edge edgeList[], String starVID, String endVID) {
        DGraph g = new DGraph(edgeList);
        g.dijkstra(starVID);
        return g.getPath(endVID);
    }
}

class DGraph {
    private final Map<String, Vertex> graph;

    public static class Edge {
        public final String v1, v2;
        public final double dist;

        public Edge(String v1, String v2, double dist) {
            this.v1 = v1;
            this.v2 = v2;
            this.dist = dist;
        }
    }

    public static class Vertex implements Comparable<Vertex> {
        public final String name;
        public double dist = Double.MAX_VALUE; // MAX_VALUE means infinity
        public Vertex previous = null;
        public final Map<Vertex, Double> neighbours = new HashMap<>();

        public Vertex(String name) {
            this.name = name;
        }
        private void getPath(List<String> routingPath) {
            if (this == this.previous) {
            	routingPath.add(this.name);
            } else if (this.previous == null) {
            	return; // unreached
            } else {
            	previous.getPath(routingPath);
            	routingPath.add(this.name);
            }
        }
        @Override
        public int compareTo(Vertex other) {
            if (dist == other.dist)
                return name.compareTo(other.name);

            return Double.compare(dist, other.dist);
        }
        @Override
        public String toString() {
            return "(" + name + ", " + dist + ")";
        }
    }
    public DGraph(Edge[] edges) {
        graph = new HashMap<>(edges.length);

        // finding all vertices
        for (Edge e : edges) {
            if (!graph.containsKey(e.v1)) 
            	graph.put(e.v1, new Vertex(e.v1));
            if (!graph.containsKey(e.v2)) 
            	graph.put(e.v2, new Vertex(e.v2));
        }

        // setting neighbouring vertices
        for (Edge e : edges) {
            graph.get(e.v1).neighbours.put(graph.get(e.v2), e.dist);
            // graph.get(e.v2).neighbours.put(graph.get(e.v1), e.dist); // for an undirected graph
        }
    }
    public void dijkstra(String startName) {
        if (!graph.containsKey(startName)) {
            System.err.printf("DGraph doesn't contain start vertex \"%s\"\n", startName);
            return;
        }
        final Vertex source = graph.get(startName);
        NavigableSet<Vertex> q = new TreeSet<>();

        // set-up vertices
        for (Vertex v : graph.values()) {
            v.previous = v == source ? source : null;
            v.dist = v == source ? 0.0 : Double.MAX_VALUE;
            q.add(v);
        }
        dijkstra(q);
    }
    private void dijkstra(final NavigableSet<Vertex> q) {
        Vertex u, v;
        while (!q.isEmpty()) {
            // vertex with shortest distance (first iteration will return source)
            u = q.pollFirst();
            if (u.dist == Double.MAX_VALUE)
                break; // we can ignore u (and any other remaining vertices) since they are unreachable

            // look at distances to each neighbour
            for (Map.Entry<Vertex, Double> a : u.neighbours.entrySet()) {
                v = a.getKey(); // the neighbour in this iteration

                final double alternateDist = u.dist + a.getValue();
                if (alternateDist < v.dist) { // shorter path to neighbour found
                    q.remove(v);
                    v.dist = alternateDist;
                    v.previous = u;
                    q.add(v);
                }
            }
        }
    }
    public List<String> getPath(String endName) {
    	List<String> path = new ArrayList<String>();
    	
        if (!graph.containsKey(endName)) {
        	Watch.putln("DGRAPH", "DGraph doesn't contain end vertex \"" + endName + "\"\n");
            return null;
        }
        graph.get(endName).getPath(path);
        return path;
    }
}