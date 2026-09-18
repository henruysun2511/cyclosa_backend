package com.cyclosa.workflow;

import com.cyclosa.common.exception.AppException;
import com.cyclosa.workflow.service.ConditionEvaluatorService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ConditionEvaluatorServiceTest {

    private ConditionEvaluatorService conditionEvaluator;

    @BeforeEach
    void setUp() {
        conditionEvaluator = new ConditionEvaluatorService();
    }

    @Test
    @DisplayName("Biểu thức null hoặc rỗng -> mặc định trả về true")
    void testEmptyExpression() {
        assertThat(conditionEvaluator.evaluate(null, Map.of())).isTrue();
        assertThat(conditionEvaluator.evaluate("", Map.of())).isTrue();
        assertThat(conditionEvaluator.evaluate("   ", Map.of())).isTrue();
    }

    @Test
    @DisplayName("Đánh giá số nguyên: totalDays > 5")
    void testIntegerComparison() {
        Map<String, Object> contextTrue = Map.of("totalDays", 6);
        Map<String, Object> contextFalse = Map.of("totalDays", 3);

        assertThat(conditionEvaluator.evaluate("#totalDays > 5", contextTrue)).isTrue();
        assertThat(conditionEvaluator.evaluate("#totalDays > 5", contextFalse)).isFalse();
    }

    @Test
    @DisplayName("Đánh giá chuỗi: departmentCode == 'IT'")
    void testStringComparison() {
        Map<String, Object> contextIT = Map.of("departmentCode", "IT");
        Map<String, Object> contextHR = Map.of("departmentCode", "HR");

        assertThat(conditionEvaluator.evaluate("#departmentCode == 'IT'", contextIT)).isTrue();
        assertThat(conditionEvaluator.evaluate("#departmentCode == 'IT'", contextHR)).isFalse();
    }

    @Test
    @DisplayName("Đánh giá kết hợp AND/OR: totalDays > 3 and departmentCode == 'SALES'")
    void testCombinedConditions() {
        Map<String, Object> contextMatch = Map.of("totalDays", 4, "departmentCode", "SALES");
        Map<String, Object> contextNotMatch = Map.of("totalDays", 2, "departmentCode", "SALES");

        assertThat(conditionEvaluator.evaluate("#totalDays > 3 and #departmentCode == 'SALES'", contextMatch)).isTrue();
        assertThat(conditionEvaluator.evaluate("#totalDays > 3 and #departmentCode == 'SALES'", contextNotMatch)).isFalse();
    }

    @Test
    @DisplayName("Cú pháp biểu thức sai -> ném AppException")
    void testInvalidSyntax() {
        assertThatThrownBy(() -> conditionEvaluator.evaluate("invalid syntax %%%", Map.of()))
                .isInstanceOf(AppException.class);
    }
}
