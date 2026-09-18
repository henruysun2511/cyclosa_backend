package com.cyclosa.workflow.service;

import com.cyclosa.common.exception.AppException;
import com.cyclosa.workflow.exception.WorkflowErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.expression.Expression;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.stereotype.Service;

import java.util.Map;

@Slf4j
@Service
public class ConditionEvaluatorService {

    private final ExpressionParser parser = new SpelExpressionParser();
    public boolean evaluate(String expression, Map<String, Object> contextVariables) {
        if (expression == null || expression.trim().isEmpty()) {
            return true;
        }

        try {
            StandardEvaluationContext context = new StandardEvaluationContext();
            if (contextVariables != null) {
                contextVariables.forEach(context::setVariable);
            }

            Expression exp = parser.parseExpression(expression);
            Boolean result = exp.getValue(context, Boolean.class);
            return Boolean.TRUE.equals(result);
        } catch (Exception e) {
            log.error("Lỗi đánh giá biểu thức điều kiện workflow: '{}' với context: {}. Lỗi: {}",
                    expression, contextVariables, e.getMessage());
            throw new AppException(WorkflowErrorCode.INVALID_CONDITION_EXPRESSION,
                    "Lỗi cú pháp biểu thức điều kiện: " + e.getMessage());
        }
    }
}
