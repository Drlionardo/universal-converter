package spring.serivce;

import spring.serivce.exceptions.UnableToConvertException;

import java.io.File;
import java.io.FileNotFoundException;
import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.util.*;

/**
 * Взвешенный направленный граф. Вершины имеют уникальные label
 * Поддерживает несколько различных вершин между одной упорядоченной парой вершин
 * Весом пути считается произведение весов ребер данного пути.
 * Алгоритмы графа работают при соблюдение следующих правил:
 * 1) Матрица смежности симметрична.
 * 2) Вес замкнутого пути равен 1, включая пустой путь или петли
 */
public class WeightedGraph {
    private final MathContext MATH_CONTEXT = new MathContext(30, RoundingMode.HALF_UP);

    /**
     * Хранит множество вершин графа, где ключ - label вершины.
     */
    private final Map<String, Vertex> vertexHashMap;
    /**
     * Являются ли вершины графа распределенными по своим компонентам связности.
     * Добавление новых вершин или изменение ребер может сбросить текущее распределение
     */
    private boolean isDistributedByComponents = false;
    /**
     * Список компонент связности графа, где каждая компонента
     * содержит множество собственных вершин.
     */
    private ArrayList<ArrayList<Vertex>> components = new ArrayList<>();

    public WeightedGraph() {
        this.vertexHashMap = new HashMap<>();
    }

    public WeightedGraph(File file) {
        this.vertexHashMap = new HashMap<>();
        readFromFile(file);
    }

    /**
     * Класс для вершин графа.
     * Хранит в себе входящие и исходящие вершины.
     */
    public static class Vertex {
        private final String label;
        private int componentIndex = -1;
        private final List<Edge> outgoingEdges = new ArrayList<>();
        private final List<Edge> incomingEdges = new ArrayList<>();

        public Vertex(String label) {
            this.label = label;
        }

        public String getLabel() {
            return label;
        }

        public int getComponentIndex() {
            return componentIndex;
        }

        public List<Edge> getOutgoingEdges() {
            return outgoingEdges;
        }

        public List<Edge> getIncomingEdges() {
            return incomingEdges;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) {
                return true;
            }
            if (o == null || getClass() != o.getClass()) {
                return false;
            }
            Vertex vertex = (Vertex) o;
            return label.equals(vertex.label);
        }
    }

    /**
     * Класс для ребер графа. Хранит в себе начальную и конечную вершину
     * а также вес ребра.
     */
    public static class Edge {
        private final Vertex fromVertex;
        private final Vertex toVertex;
        private final BigDecimal weight;

        public Edge(Vertex fromVertex, Vertex toVertex, BigDecimal weight) {
            this.fromVertex = fromVertex;
            this.toVertex = toVertex;
            this.weight = weight;
        }

        public Vertex getFromVertex() {
            return fromVertex;
        }

        public Vertex getToVertex() {
            return toVertex;
        }

        public BigDecimal getWeight() {
            return weight;
        }
    }

    /**
     * @return Список всех компонент связности, элементы которых - список вершин
     */
    public ArrayList<ArrayList<Vertex>> getComponents() {
        return components;
    }

    /**
     * Добавляет новую вершину в граф.
     * Поддерживает только уникальные названия для вершин.
     *
     * @param vertexLabel - название для новой вершины
     * @return Возвращает {@code true} (По аналогии с  {@link Collection#add})
     * если множество вершин в результате  было изменено. Иначе - {@code false}
     */
    public boolean addVertex(String vertexLabel) {
        if (!vertexHashMap.containsKey(vertexLabel)) {
            Vertex vertex = new Vertex(vertexLabel);
            this.vertexHashMap.put(vertexLabel, vertex);
            isDistributedByComponents = false;
            return true;
        } else {
            return false;
        }
    }

    /**
     * Добавляет новую пару ребер между двумя вершинами
     * Вес обратного ребра v2 -> v1 равен 1 / weight
     *
     * @param v1     Первая вершина
     * @param v2     Вторая вершина
     * @param weight Вес ребра для ребра v1 -> v2
     */
    public void addEdgesCouple(Vertex v1, Vertex v2, BigDecimal weight) {
        isDistributedByComponents = false;
        Edge directEdge = new Edge(v1, v2, weight);
        v1.outgoingEdges.add(directEdge);
        v2.incomingEdges.add(directEdge);

        Edge reverseEdge = new Edge(v2, v1, BigDecimal.ONE.divide(weight, MATH_CONTEXT));
        v2.outgoingEdges.add(reverseEdge);
        v1.incomingEdges.add(reverseEdge);
    }

    /**
     * Возвращает первое найденное ребро между упорядоченной парой вершин.
     * Требует распределение вершин по компонентам для более быстрой проверки
     * на отсутствие ребер, если вершины лежат в разных компонентах
     *
     * @param v1 Начальная вершина
     * @param v2 Конечная вершина
     * @return Ребро из v1 в v2, а в случае отсутствия null
     */
    public Edge findEdgeBetweenVertexes(Vertex v1, Vertex v2) {
        checkComponentsDistribution();
        if (v1.componentIndex == v2.componentIndex) {
            for (Edge edge : v1.outgoingEdges) {
                if (edge.toVertex.equals(v2)) {
                    return edge;
                }
            }
        }
        return null;
    }

    /**
     * Рассчитывает вес пути между двумя вершинами. Весом пути
     * считается произведение весов всех ребер данного пути.
     *
     * @param v1 начальная вершина
     * @param v2 конечная вершина
     * @return Итоговое
     * @throws UnableToConvertException Если не существует пути между вершинами
     */
    public BigDecimal calcRatioBetweenVertexes(Vertex v1, Vertex v2) {
        Stack<Vertex> path = findPathBetweenVertexes(v1, v2);
        if (path != null && !path.empty()) {
            BigDecimal ratio = BigDecimal.ONE;
            Vertex currentVertex = path.pop();
            while (!path.empty()) {
                Vertex nextVertex = path.peek();
                Edge edge = findEdgeBetweenVertexes(currentVertex, nextVertex);
                ratio = ratio.divide(edge.getWeight(), MATH_CONTEXT);
                currentVertex = path.pop();
            }
            return ratio;
        } else {
            throw new UnableToConvertException();
        }
    }

    /**
     * @param label - название вершины
     * @return Vertex с данным названием. В случае отсутствия - null
     */
    public Vertex getVertexByLabel(String label) {
        return vertexHashMap.get(label);
    }

    /**
     * Считывает данные из .csv файла с правилами
     * Данные должны иметь формат label1,label2,weight
     *
     * @param file Путь к csv файлу с данными
     */
    private void readFromFile(File file) {
        try (Scanner scan = new Scanner(file)) {
            while (scan.hasNextLine()) {
                String[] data = scan.nextLine().split(",");
                String label1 = data[0];
                String label2 = data[1];
                addVertex(label1);
                addVertex(label2);
                BigDecimal ratio = new BigDecimal(data[2]);
                addEdgesCouple(getVertexByLabel(label1), getVertexByLabel(label2), ratio);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        distributeByComponents();
    }

    /**
     * Находит путь между вершинами, используя обод в глубину
     *
     * @param v1 Начальная вершина
     * @param v2 Конечная вершина
     * @return Стек, содержащий в себе вершины в пути,
     * где верхний элемент стека - начальная вершина, а нижний элемент - конечная
     */
    private Stack<Vertex> findPathBetweenVertexes(Vertex v1, Vertex v2) {
        checkComponentsDistribution();
        if (v1.componentIndex != v2.componentIndex) {
            return null;
        } else {
            HashSet<Vertex> unvisitedVertexesInComponent = new HashSet<>(components.get(v1.componentIndex));
            Stack<Vertex> path = new Stack<>();
            path.push(v1);
            unvisitedVertexesInComponent.remove(v1);
            while (!path.empty() && !path.peek().equals(v2)) {
                Vertex neighborVertex = findUnvisitedVertexNeighbor(path.peek(), unvisitedVertexesInComponent);
                if (neighborVertex == null) {
                    path.pop();
                } else {
                    path.push(neighborVertex);
                    unvisitedVertexesInComponent.remove(neighborVertex);
                }
            }
            return path;
        }
    }

    /**
     * Находит случайную не посещенную соседнюю вершину
     *
     * @param vertex            Исходная вершина
     * @param unvisitedVertexes Множество не посещенных вершин из который идет поиск
     * @return Не посещенных соседнюю вершину в случае существования, иначе - null
     */
    private Vertex findUnvisitedVertexNeighbor(Vertex vertex, HashSet<Vertex> unvisitedVertexes) {
        for (Edge edge : vertex.outgoingEdges) {
            if (unvisitedVertexes.contains(edge.toVertex)) {
                return edge.toVertex;
            }
        }
        return null;
    }

    /**
     * Распределяет множество вершин по компонентам связности.
     * Начиная со случайной не посещенной вершины запускается обход в глубину.
     * По окончанию обхода все вершины на данном обходе добавляются в общую компоненту связности
     * Правильно функционирует только в графе с симметричной матрицей смежности
     */
    private void distributeByComponents() {
        this.components = new ArrayList<>();
        HashSet<Vertex> unvisitedVertexes = new HashSet<>(vertexHashMap.values());
        int componentCounter = -1;
        Stack<Vertex> path = new Stack<>();
        while (!unvisitedVertexes.isEmpty()) {
            Vertex startVertex = unvisitedVertexes.stream().findAny().get();
            unvisitedVertexes.remove(startVertex);
            componentCounter++;
            ArrayList<Vertex> currentComponent = new ArrayList<>(); // Новая компонента
            currentComponent.add(startVertex);
            startVertex.componentIndex = componentCounter;
            path.push(startVertex);
            //Обход всех вершин в данной компоненте
            while (!path.empty()) {
                Vertex currentVertex = findUnvisitedVertexNeighbor(path.peek(), unvisitedVertexes);
                if (currentVertex == null) {
                    path.pop();
                } else {
                    unvisitedVertexes.remove(currentVertex);
                    currentComponent.add(currentVertex);
                    currentVertex.componentIndex = componentCounter;
                    path.push(currentVertex);
                }
            }
            components.add(currentComponent); // Добавляет компоненту в список всех компонент
        }
        isDistributedByComponents = true;
    }

    private void checkComponentsDistribution() {
        if (!isDistributedByComponents) {
            distributeByComponents();
        }
    }
}