package backend.academy.linktracker.bot.util;

import org.springframework.aop.support.AopUtils;
import org.springframework.core.annotation.AnnotationUtils;

public final class HandlerAnnotationUtils {

    private HandlerAnnotationUtils() {}

    public static <A extends java.lang.annotation.Annotation> A findAnnotation(Object bean, Class<A> annotationType) {
        if (bean == null) {
            return null;
        }
        return AnnotationUtils.findAnnotation(AopUtils.getTargetClass(bean), annotationType);
    }
}
