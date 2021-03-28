import spring.serivce.WeightedGraph;
import org.junit.jupiter.api.Test;
import java.io.File;
import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;

import static org.junit.jupiter.api.Assertions.*;
import static spring.serivce.WeightedGraph.Vertex;

class WeightedGraphTest {
    final static String filePath = "src/test/resources/INPUT.scv";
    private static final MathContext MATH_CONTEXT = new MathContext(30, RoundingMode.HALF_UP);

    @Test
    void addVertex() {
        WeightedGraph graph = new WeightedGraph();
        assertNull(graph.getVertexByLabel("newLabel"));
        Vertex vertex = new Vertex("newLabel");
        assertTrue(graph.addVertex("newLabel"));
        assertEquals(graph.getVertexByLabel("newLabel"), vertex);
        assertFalse(graph.addVertex("newLabel"));
    }

    @Test
    void addEdgeCouple() {
        WeightedGraph graph = new WeightedGraph();
        graph.addVertex("v1");
        graph.addVertex("v2");
        Vertex v1 = graph.getVertexByLabel("v1");
        Vertex v2 = graph.getVertexByLabel("v2");
        assertNull(graph.findEdgeBetweenVertexes(v1, v2));
        assertNull(graph.findEdgeBetweenVertexes(v2, v1));

        BigDecimal weight = BigDecimal.valueOf(Math.random());
        graph.addEdgesCouple(v1, v2, weight);
        var directEdge = graph.findEdgeBetweenVertexes(v1, v2);
        var reverseEdge = graph.findEdgeBetweenVertexes(v2, v1);

        assertEquals(directEdge.getWeight(), weight);
        assertEquals(directEdge.getFromVertex(), v1);
        assertEquals(directEdge.getToVertex(), v2);
        assertEquals(reverseEdge.getWeight(), BigDecimal.ONE.divide(weight, MATH_CONTEXT));
        assertEquals(reverseEdge.getFromVertex(), v2);
        assertEquals(reverseEdge.getToVertex(), v1);
    }

    @Test
    void findEdgeBetweenVertexes() {
        WeightedGraph graph = new WeightedGraph();
        graph.addVertex("v1");
        graph.addVertex("v2");
        graph.addVertex("v3");
        Vertex v1 = graph.getVertexByLabel("v1");
        Vertex v2 = graph.getVertexByLabel("v2");
        Vertex v3 = graph.getVertexByLabel("v3");
        graph.addEdgesCouple(v1, v2, BigDecimal.valueOf(2));
        graph.addEdgesCouple(v1, v3, BigDecimal.valueOf(10));

        assertTrue(v1.getOutgoingEdges().contains(graph.findEdgeBetweenVertexes(v1, v2)));
        assertTrue(v2.getIncomingEdges().contains(graph.findEdgeBetweenVertexes(v1, v2)));
        assertTrue(v1.getOutgoingEdges().contains(graph.findEdgeBetweenVertexes(v1, v3)));
        assertTrue(v3.getIncomingEdges().contains(graph.findEdgeBetweenVertexes(v1, v3)));
        assertNull(graph.findEdgeBetweenVertexes(v2, v3));
        assertNull(graph.findEdgeBetweenVertexes(v3, v2));

        assertEquals(BigDecimal.valueOf(2), graph.findEdgeBetweenVertexes(v1, v2).getWeight());
        assertEquals(BigDecimal.valueOf(0.5), graph.findEdgeBetweenVertexes(v2, v1).getWeight());
        assertEquals(BigDecimal.valueOf(10), graph.findEdgeBetweenVertexes(v1, v3).getWeight());
        assertEquals(BigDecimal.valueOf(0.1), graph.findEdgeBetweenVertexes(v3, v1).getWeight());
    }

    @Test
    void calcRationBetweenVertexes() {
        WeightedGraph graph4 = new WeightedGraph(new File(filePath));
        Vertex v1 = graph4.getVertexByLabel("км");
        Vertex v2 = graph4.getVertexByLabel("м");
        BigDecimal ratio = graph4.calcRatioBetweenVertexes(v1, v2);
        assertEquals(ratio.stripTrailingZeros().toPlainString(), "1000");
        ratio = graph4.calcRatioBetweenVertexes(v2, v1);
        assertEquals(ratio.stripTrailingZeros().toPlainString(), "0.001");
    }
}
