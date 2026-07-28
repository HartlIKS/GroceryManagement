package de.iks.grocery_manager.server;

import io.quarkus.arc.Arc;
import io.quarkus.narayana.jta.QuarkusTransaction;
import io.quarkus.test.junit.callback.QuarkusTestBeforeEachCallback;
import io.quarkus.test.junit.callback.QuarkusTestMethodContext;
import jakarta.persistence.EntityManager;
import jakarta.persistence.Query;

import java.io.IOException;
import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.Comparator;
import java.util.function.Function;
import java.util.stream.Stream;

public class SqlCallback implements QuarkusTestBeforeEachCallback {
    private record AnnotationContext<T extends Annotation>(T value, Class<?> clazz) {
        public static <T extends Annotation> Function<T, AnnotationContext<T>> forClass(Class<?> clazz) {
            return v -> new AnnotationContext<T>(v, clazz);
        }
    }

    private static <T extends Annotation> Stream<AnnotationContext<T>> annotations(
        Class<T> annotationType,
        Class<?> clazz
    ) {
        if(clazz == null) return Stream.of();
        return Stream.concat(
            Arrays
                .stream(clazz.getDeclaredAnnotationsByType(annotationType))
                .map(AnnotationContext.forClass(clazz)),
            annotations(annotationType, clazz.getSuperclass())
        );
    }


    @Override
    public void beforeEach(QuarkusTestMethodContext context) {
        final var ctx = Arc.requireContainer();
        ctx.requestContext().activate();
        final EntityManager entityManager = ctx.instance(EntityManager.class).get();
        QuarkusTransaction.begin();
        Stream
            .of(
                Arrays
                    .stream(context
                                .getTestMethod()
                                .getAnnotationsByType(Sql.class))
                    .map(AnnotationContext.forClass(context
                                                        .getTestMethod()
                                                        .getDeclaringClass())),
                annotations(
                    Sql.class,
                    context
                        .getTestInstance()
                        .getClass()
                ),
                context
                    .getOuterInstances()
                    .stream()
                    .map(Object::getClass)
                    .flatMap(c -> annotations(Sql.class, c))
            )
            .flatMap(Function.identity())
            .sorted(Comparator.comparing(AnnotationContext::value, Comparator.comparing(Sql::order)))
            .map(c -> {
                try(
                    var resource = c
                        .clazz()
                        .getResourceAsStream(c
                                                 .value()
                                                 .value())
                ) {
                    assert resource != null;
                    return new String(resource.readAllBytes());
                } catch(IOException e) {
                    throw new RuntimeException(e);
                }
            })
            .map(entityManager::createNativeQuery)
            .forEachOrdered(Query::executeUpdate);
        QuarkusTransaction.commit();
        ctx.requestContext().deactivate();
    }
}
