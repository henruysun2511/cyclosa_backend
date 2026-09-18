package com.cyclosa.workflow.service;

import java.util.Map;

public interface ConditionEvaluatorService {

    /**
     * Đánh giá biểu thức điều kiện với biến ngữ cảnh (SpEL).
     *
     * @param expression chuỗi biểu thức, ví dụ: "#totalDays > 5" hoặc "#amount > 10000000"
     * @param contextVariables map chứa các biến ngữ cảnh
     * @return true nếu điều kiện thỏa mãn, ngược lại false
     */
    boolean evaluate(String expression, Map<String, Object> contextVariables);
}
