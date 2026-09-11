package com.order.util;

import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.expression.EvaluationContext;
import org.springframework.expression.ExpressionParser;
import org.springframework.expression.spel.standard.SpelExpressionParser;
import org.springframework.expression.spel.support.StandardEvaluationContext;
import org.springframework.stereotype.Component;

@Component
public class KeyGenerator {

    private static final String ORDER_PREFIX = "order";
    private final ExpressionParser expressionParser = new SpelExpressionParser();

    /**
     * @Return {CacheName}:{KeyValue}
     * */
    public String generateKey(JoinPoint joinPoint, String cacheName, String key) {
        EvaluationContext context = new StandardEvaluationContext();
        /*
        * ex)
            @Cacheable(
                cacheName = "userCache",
                key = "#userId"
            )
            public User getUser(Long userId, String type) {
                return db.find(userId);
            }
        * jointPoint는 해당 메서드의 정보(MethodsSignature)를 얻을 수 있는 통로
        * context에 key, value형태로 저장.
        * SpEL을 통해, 문자열을 파싱하여 특정 문자열을 반환하게 된다(getValue of key in SpEL context).
        * */
        String[] parameterNames = ((MethodSignature) joinPoint.getSignature()).getParameterNames();
        Object[] args = joinPoint.getArgs();

        for(int i = 0 ; i < args.length ; i++){
            context.setVariable(parameterNames[i], args[i]);
        }

        /*
         * cacheName + ":" + keyValue;
         * ex) order : 1
         * */
        return cacheName + ":" + expressionParser.parseExpression(key).getValue(context, String.class);
    }

    /*
     * Order Cache Key 생성
     * ex) 1L -> order:1
     */
    /**
     * @Return {CacheName}:{KeyValue}
     * */
    public String generateOrderKey(Long orderId) {
        return ORDER_PREFIX + ":" + orderId;
    }

    /**
     * @Return {CacheName}:{KeyValue}
     * */
    public String generateOrderKey(String orderId) {
        return ORDER_PREFIX + ":" + orderId;
    }
}
