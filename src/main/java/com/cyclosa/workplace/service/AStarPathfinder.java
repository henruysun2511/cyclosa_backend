package com.cyclosa.workplace.service;

import com.cyclosa.workplace.dto.response.NavigationRouteResponse.Point2D;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

@Component
public class AStarPathfinder {

    public static final double DEFAULT_CELL_SIZE = 20.0; // Mỗi ô lưới 20px

    public static class ObstacleRect {
        public double minX;
        public double minY;
        public double maxX;
        public double maxY;

        public ObstacleRect(double x, double y, double width, double height) {
            this.minX = x;
            this.minY = y;
            this.maxX = x + width;
            this.maxY = y + height;
        }

        public boolean contains(double px, double py) {
            return px >= minX && px <= maxX && py >= minY && py <= maxY;
        }
    }

    private static class Node implements Comparable<Node> {
        int gx;
        int gy;
        double gCost;
        double fCost;
        Node parent;

        Node(int gx, int gy, double gCost, double fCost, Node parent) {
            this.gx = gx;
            this.gy = gy;
            this.gCost = gCost;
            this.fCost = fCost;
            this.parent = parent;
        }

        @Override
        public int compareTo(Node o) {
            return Double.compare(this.fCost, o.fCost);
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (!(obj instanceof Node other)) return false;
            return this.gx == other.gx && this.gy == other.gy;
        }

        @Override
        public int hashCode() {
            return Objects.hash(gx, gy);
        }
    }

    public List<Point2D> findPath(double startX, double startY,
                                 double targetX, double targetY,
                                 double canvasWidth, double canvasHeight,
                                 List<ObstacleRect> obstacles) {

        double cellSize = DEFAULT_CELL_SIZE;
        int gridCols = Math.max(10, (int) Math.ceil(canvasWidth / cellSize));
        int gridRows = Math.max(10, (int) Math.ceil(canvasHeight / cellSize));

        boolean[][] blocked = new boolean[gridCols][gridRows];
        for (ObstacleRect obs : obstacles) {
            int minCol = Math.max(0, (int) (obs.minX / cellSize));
            int maxCol = Math.min(gridCols - 1, (int) (obs.maxX / cellSize));
            int minRow = Math.max(0, (int) (obs.minY / cellSize));
            int maxRow = Math.min(gridRows - 1, (int) (obs.maxY / cellSize));

            for (int c = minCol; c <= maxCol; c++) {
                for (int r = minRow; r <= maxRow; r++) {
                    blocked[c][r] = true;
                }
            }
        }

        int startCol = Math.max(0, Math.min(gridCols - 1, (int) (startX / cellSize)));
        int startRow = Math.max(0, Math.min(gridRows - 1, (int) (startY / cellSize)));
        int targetCol = Math.max(0, Math.min(gridCols - 1, (int) (targetX / cellSize)));
        int targetRow = Math.max(0, Math.min(gridRows - 1, (int) (targetY / cellSize)));

        // Nếu điểm xuất phát hoặc điểm đích bị đè bởi vật thể, tìm ô trống gần nhất kề cạnh
        startCol = findNearestWalkableCol(startCol, startRow, blocked, gridCols, gridRows);
        startRow = findNearestWalkableRow(startCol, startRow, blocked, gridCols, gridRows);
        targetCol = findNearestWalkableCol(targetCol, targetRow, blocked, gridCols, gridRows);
        targetRow = findNearestWalkableRow(targetCol, targetRow, blocked, gridCols, gridRows);

        PriorityQueue<Node> openSet = new PriorityQueue<>();
        Map<String, Double> gScore = new HashMap<>();
        Set<String> closedSet = new HashSet<>();

        Node startNode = new Node(startCol, startRow, 0.0, heuristic(startCol, startRow, targetCol, targetRow), null);
        openSet.add(startNode);
        gScore.put(key(startCol, startRow), 0.0);

        int[][] directions = {
                {0, 1}, {1, 0}, {0, -1}, {-1, 0},     // 4 hướng trực giao
                {1, 1}, {1, -1}, {-1, 1}, {-1, -1}    // 4 hướng chéo
        };

        Node targetNode = null;
        while (!openSet.isEmpty()) {
            Node current = openSet.poll();
            String currentKey = key(current.gx, current.gy);

            if (current.gx == targetCol && current.gy == targetRow) {
                targetNode = current;
                break;
            }

            if (closedSet.contains(currentKey)) {
                continue;
            }
            closedSet.add(currentKey);

            for (int[] dir : directions) {
                int nx = current.gx + dir[0];
                int ny = current.gy + dir[1];

                if (nx < 0 || nx >= gridCols || ny < 0 || ny >= gridRows) {
                    continue;
                }
                if (blocked[nx][ny]) {
                    continue;
                }

                double stepCost = (dir[0] != 0 && dir[1] != 0) ? 1.414 : 1.0;
                double tentativeG = current.gCost + stepCost;

                String neighborKey = key(nx, ny);
                if (tentativeG < gScore.getOrDefault(neighborKey, Double.MAX_VALUE)) {
                    gScore.put(neighborKey, tentativeG);
                    double f = tentativeG + heuristic(nx, ny, targetCol, targetRow);
                    openSet.add(new Node(nx, ny, tentativeG, f, current));
                }
            }
        }

        List<Point2D> rawPoints = new ArrayList<>();
        if (targetNode != null) {
            Node curr = targetNode;
            while (curr != null) {
                rawPoints.add(0, Point2D.builder()
                        .x(BigDecimal.valueOf(curr.gx * cellSize + cellSize / 2).setScale(2, RoundingMode.HALF_UP))
                        .y(BigDecimal.valueOf(curr.gy * cellSize + cellSize / 2).setScale(2, RoundingMode.HALF_UP))
                        .build());
                curr = curr.parent;
            }
        } else {
            // Không tìm được đường né hoàn toàn -> fallback đường thẳng 2 điểm
            rawPoints.add(Point2D.builder()
                    .x(BigDecimal.valueOf(startX).setScale(2, RoundingMode.HALF_UP))
                    .y(BigDecimal.valueOf(startY).setScale(2, RoundingMode.HALF_UP))
                    .build());
            rawPoints.add(Point2D.builder()
                    .x(BigDecimal.valueOf(targetX).setScale(2, RoundingMode.HALF_UP))
                    .y(BigDecimal.valueOf(targetY).setScale(2, RoundingMode.HALF_UP))
                    .build());
        }

        // Đảm bảo điểm đầu và cuối chính xác là start và target
        if (!rawPoints.isEmpty()) {
            rawPoints.set(0, Point2D.builder().x(BigDecimal.valueOf(startX).setScale(2, RoundingMode.HALF_UP)).y(BigDecimal.valueOf(startY).setScale(2, RoundingMode.HALF_UP)).build());
            rawPoints.set(rawPoints.size() - 1, Point2D.builder().x(BigDecimal.valueOf(targetX).setScale(2, RoundingMode.HALF_UP)).y(BigDecimal.valueOf(targetY).setScale(2, RoundingMode.HALF_UP)).build());
        }

        return smoothPath(rawPoints);
    }

    private List<Point2D> smoothPath(List<Point2D> points) {
        if (points.size() <= 2) return points;

        List<Point2D> simplified = new ArrayList<>();
        simplified.add(points.get(0));

        for (int i = 1; i < points.size() - 1; i++) {
            Point2D prev = simplified.get(simplified.size() - 1);
            Point2D curr = points.get(i);
            Point2D next = points.get(i + 1);

            double dx1 = curr.getX().doubleValue() - prev.getX().doubleValue();
            double dy1 = curr.getY().doubleValue() - prev.getY().doubleValue();
            double dx2 = next.getX().doubleValue() - curr.getX().doubleValue();
            double dy2 = next.getY().doubleValue() - curr.getY().doubleValue();

            // Nếu góc đổi hướng đáng kể -> giữ lại làm waypoint bẻ cua
            double dot = dx1 * dx2 + dy1 * dy2;
            double mag1 = Math.sqrt(dx1 * dx1 + dy1 * dy1);
            double mag2 = Math.sqrt(dx2 * dx2 + dy2 * dy2);

            if (mag1 > 0.001 && mag2 > 0.001) {
                double cos = dot / (mag1 * mag2);
                if (cos < 0.98) { // Đổi hướng
                    simplified.add(curr);
                }
            }
        }

        simplified.add(points.get(points.size() - 1));
        return simplified;
    }

    private double heuristic(int x1, int y1, int x2, int y2) {
        int dx = Math.abs(x1 - x2);
        int dy = Math.abs(y1 - y2);
        return Math.sqrt(dx * dx + dy * dy);
    }

    private String key(int x, int y) {
        return x + ":" + y;
    }

    private int findNearestWalkableCol(int col, int row, boolean[][] blocked, int maxCols, int maxRows) {
        if (!blocked[col][row]) return col;
        for (int r = 1; r < 5; r++) {
            for (int dc = -r; dc <= r; dc++) {
                int c = col + dc;
                if (c >= 0 && c < maxCols && !blocked[c][row]) return c;
            }
        }
        return col;
    }

    private int findNearestWalkableRow(int col, int row, boolean[][] blocked, int maxCols, int maxRows) {
        if (!blocked[col][row]) return row;
        for (int r = 1; r < 5; r++) {
            for (int dr = -r; dr <= r; dr++) {
                int rw = row + dr;
                if (rw >= 0 && rw < maxRows && !blocked[col][rw]) return rw;
            }
        }
        return row;
    }

    public double calculateDistanceMeters(List<Point2D> pathPoints, double scaleRatio) {
        if (pathPoints == null || pathPoints.size() < 2) return 0.0;
        double totalPixels = 0.0;
        for (int i = 0; i < pathPoints.size() - 1; i++) {
            Point2D p1 = pathPoints.get(i);
            Point2D p2 = pathPoints.get(i + 1);
            double dx = p2.getX().doubleValue() - p1.getX().doubleValue();
            double dy = p2.getY().doubleValue() - p1.getY().doubleValue();
            totalPixels += Math.sqrt(dx * dx + dy * dy);
        }
        return Math.round(totalPixels * scaleRatio * 10.0) / 10.0;
    }

    public int estimateWalkSeconds(double distanceMeters) {
        return Math.max(5, (int) Math.round(distanceMeters / 1.2));
    }

    public List<String> generateTurnByTurnInstructions(List<Point2D> pathPoints) {
        if (pathPoints == null || pathPoints.isEmpty()) {
            return Collections.emptyList();
        }
        if (pathPoints.size() <= 2) {
            return List.of("Đi thẳng theo hướng chỉ dẫn để tới vị trí đích.");
        }

        List<String> steps = new ArrayList<>();
        for (int i = 1; i < pathPoints.size() - 1; i++) {
            Point2D prev = pathPoints.get(i - 1);
            Point2D curr = pathPoints.get(i);
            Point2D next = pathPoints.get(i + 1);

            double dx1 = curr.getX().doubleValue() - prev.getX().doubleValue();
            double dy1 = curr.getY().doubleValue() - prev.getY().doubleValue();
            double dx2 = next.getX().doubleValue() - curr.getX().doubleValue();
            double dy2 = next.getY().doubleValue() - curr.getY().doubleValue();

            double cross = dx1 * dy2 - dy1 * dx2;
            if (cross > 50) {
                steps.add("Rẽ phải tại tọa độ (" + curr.getX() + ", " + curr.getY() + ")");
            } else if (cross < -50) {
                steps.add("Rẽ trái tại tọa độ (" + curr.getX() + ", " + curr.getY() + ")");
            } else {
                steps.add("Tiếp tục đi thẳng qua tọa độ (" + curr.getX() + ", " + curr.getY() + ")");
            }
        }
        return steps;
    }
}
