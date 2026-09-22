package com.cyclosa.attendance;

import com.cyclosa.attendance.util.GeoDistanceUtils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;

class GeoDistanceUtilsTest {

    @Test
    @DisplayName("Khoảng cách giữa hai điểm trùng nhau phải bằng 0 mét")
    void testSamePoint() {
        double lat = 21.028511;
        double lon = 105.854444;

        double distance = GeoDistanceUtils.calculateDistanceMeters(lat, lon, lat, lon);
        assertEquals(0.0, distance, 0.001);
    }

    @Test
    @DisplayName("Tính khoảng cách thực tế giữa hai điểm trong Hà Nội (Hồ Gươm đến Nhà Hát Lớn ~ 500m)")
    void testRealWorldDistance() {
        // Hồ Hoàn Kiếm: 21.028511, 105.854444
        // Nhà hát Lớn Hà Nội: 21.024347, 105.857639
        double distance = GeoDistanceUtils.calculateDistanceMeters(21.028511, 105.854444, 21.024347, 105.857639);

        // Khoảng cách thực tế ~ 570 - 600m
        assertTrue(distance > 500 && distance < 700, "Distance should be around 580m, was: " + distance);
    }

    @Test
    @DisplayName("Xử lý an toàn khi tọa độ null")
    void testNullHandling() {
        double distance = GeoDistanceUtils.calculateDistanceMeters((BigDecimal) null, null, null, null);
        assertEquals(Double.MAX_VALUE, distance);
    }
}
