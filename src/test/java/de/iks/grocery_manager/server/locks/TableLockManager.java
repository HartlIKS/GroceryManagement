package de.iks.grocery_manager.server.locks;

import de.iks.grocery_manager.server.jpa.BaseRepository;
import de.iks.grocery_manager.server.jpa.ProductGroupRepository;
import de.iks.grocery_manager.server.jpa.ShoppingListRepository;
import de.iks.grocery_manager.server.jpa.ShoppingTripRepository;
import de.iks.grocery_manager.server.jpa.masterdata.PriceRepository;
import de.iks.grocery_manager.server.jpa.masterdata.ProductRepository;
import de.iks.grocery_manager.server.jpa.masterdata.StoreRepository;
import de.iks.grocery_manager.server.jpa.mdi.ExternalAPIRepository;
import de.iks.grocery_manager.server.jpa.mdi.PriceEndpointRepository;
import de.iks.grocery_manager.server.jpa.mdi.ProductEndpointRepository;
import de.iks.grocery_manager.server.jpa.mdi.StoreEndpointRepository;
import de.iks.grocery_manager.server.jpa.share.JoinLinkRepository;
import de.iks.grocery_manager.server.jpa.share.ShareRepository;
import io.quarkus.arc.Arc;
import io.quarkus.narayana.jta.QuarkusTransaction;
import io.quarkus.test.junit.callback.QuarkusTestAfterEachCallback;
import io.quarkus.test.junit.callback.QuarkusTestBeforeEachCallback;
import io.quarkus.test.junit.callback.QuarkusTestMethodContext;
import io.vertx.core.impl.ConcurrentHashSet;
import jakarta.persistence.EntityManager;
import lombok.extern.jbosslog.JBossLog;
import org.jspecify.annotations.NullMarked;
import org.junit.jupiter.api.parallel.ResourceAccessMode;
import org.junit.jupiter.api.parallel.ResourceLocksProvider;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@JBossLog
public class TableLockManager implements ResourceLocksProvider,
    QuarkusTestBeforeEachCallback,
    QuarkusTestAfterEachCallback {
    private record ScriptAndDeps(
        String script,
        Set<Class<? extends BaseRepository<?>>> dependencies
    ) {
        @SafeVarargs
        public ScriptAndDeps(String script, Class<? extends BaseRepository<?>>... dependencies) {
            this(script, Set.of(dependencies));
        }
    }

    private static final Map<Class<? extends BaseRepository<?>>, ScriptAndDeps> INITIALIZERS = Map.ofEntries(
        Map.entry(StoreRepository.class, new ScriptAndDeps("store.sql")),
        Map.entry(ProductRepository.class, new ScriptAndDeps("product.sql")),
        Map.entry(
            PriceRepository.class, new ScriptAndDeps(
                "price.sql",
                StoreRepository.class, ProductRepository.class
            )
        ),
        Map.entry(
            ProductGroupRepository.class, new ScriptAndDeps(
                "product_group.sql",
                ProductRepository.class
            )
        ),
        Map.entry(
            ShoppingListRepository.class, new ScriptAndDeps(
                "shopping_list.sql",
                ProductRepository.class,
                ProductGroupRepository.class
            )
        ),
        Map.entry(
            ShoppingTripRepository.class, new ScriptAndDeps(
                "shopping_trip.sql",
                ProductRepository.class,
                StoreRepository.class
            )
        ),

        Map.entry(
            ExternalAPIRepository.class, new ScriptAndDeps(
                "external_api.sql",
                ProductRepository.class,
                StoreRepository.class
            )
        ),
        Map.entry(
            ProductEndpointRepository.class, new ScriptAndDeps(
                "product_endpoint.sql",
                ExternalAPIRepository.class
            )
        ),
        Map.entry(
            StoreEndpointRepository.class, new ScriptAndDeps(
                "store_endpoint.sql",
                ExternalAPIRepository.class
            )
        ),
        Map.entry(
            PriceEndpointRepository.class, new ScriptAndDeps(
                "price_endpoint.sql",
                ExternalAPIRepository.class
            )
        ),

        Map.entry(ShareRepository.class, new ScriptAndDeps("share.sql")),
        Map.entry(
            JoinLinkRepository.class, new ScriptAndDeps(
                "join_link.sql",
                ShareRepository.class
            )
        )
    );

    private static Map<Class<? extends BaseRepository<?>>, Boolean> directlyUsedTables(
        Stream<Class<?>> enclosingInstanceTypes,
        Class<?> testClass,
        Method testMethod
    ) {
        return Stream
            .concat(
                enclosingInstanceTypes,
                Stream.of(testClass, testMethod)
            )
            .flatMap(c -> Arrays.stream(c.getAnnotationsByType(EntityAccess.class)))
            .collect(Collectors.toUnmodifiableMap(
                EntityAccess::value,
                EntityAccess::writes,
                (a, b) -> b
            ));
    }

    private static Stream<Class<? extends BaseRepository<?>>> dependenciesOf(Class<? extends BaseRepository<?>> repository) {
        ScriptAndDeps scriptAndDeps = INITIALIZERS.get(repository);
        if(scriptAndDeps == null) return Stream.empty();
        return Stream
            .concat(
                scriptAndDeps.dependencies.stream(),
                scriptAndDeps.dependencies
                    .stream()
                    .flatMap(TableLockManager::dependenciesOf)
            );
    }

    private static Stream<Class<? extends BaseRepository<?>>> dependentsOf(Class<? extends BaseRepository<?>> repository) {
        return INITIALIZERS
            .keySet()
            .stream()
            .filter(d -> dependenciesOf(d).anyMatch(repository::equals));
    }

    private static Map<Class<? extends BaseRepository<?>>, Boolean> usedTables(
        Stream<Class<?>> enclosingInstanceTypes,
        Class<?> testClass,
        Method testMethod
    ) {
        var direct = directlyUsedTables(enclosingInstanceTypes, testClass, testMethod);
        return Stream
            .concat(
                direct
                    .entrySet()
                    .stream(),
                direct
                    .keySet()
                    .stream()
                    .flatMap(TableLockManager::dependenciesOf)
                    .map(c -> Map.entry(c, false))
            )
            .collect(Collectors.toUnmodifiableMap(
                Entry::getKey,
                Entry::getValue,
                (a, b) -> a || b
            ));
    }

    private static final Map<EntityManager, Set<Class<? extends BaseRepository<?>>>> INITIALIZED =
        new ConcurrentHashMap<>();

    private static synchronized void initialize(Set<Class<? extends BaseRepository<?>>> required) {
        if(required.isEmpty()) return;
        var container = Arc.requireContainer();
        container
            .requestContext()
            .activate();
        EntityManager entityManager = container
            .instance(EntityManager.class)
            .get();
        Set<Class<? extends BaseRepository<?>>> inited = INITIALIZED.computeIfAbsent(
            entityManager,
            em -> new ConcurrentHashSet<>()
        );
        Set<Class<? extends BaseRepository<?>>> toInitialize = required
            .stream()
            .filter(r -> !inited.contains(r))
            .collect(Collectors.toSet());
        QuarkusTransaction.begin();
        while(!toInitialize.isEmpty()) {
            Map<? extends Class<? extends BaseRepository<?>>, ScriptAndDeps> noPendingDependency = toInitialize
                .stream()
                .map(r -> Optional
                    .ofNullable(INITIALIZERS.get(r))
                    .map(s -> Map.entry(r, s))
                    .orElseThrow())
                .filter(e -> inited.containsAll(e
                                                    .getValue()
                                                    .dependencies()))
                .collect(Collectors.toUnmodifiableMap(Entry::getKey, Entry::getValue));
            if(noPendingDependency.isEmpty()) throw new IllegalStateException("Circular dependency found");
            for(var entry : noPendingDependency.entrySet()) {
                ScriptAndDeps scriptAndDeps = entry.getValue();
                if(scriptAndDeps.script != null) {
                    log.debugv("Initializing: {0}", entry.getKey().getSimpleName());
                    try(
                        var resource = TableLockManager.class.getResourceAsStream(scriptAndDeps.script())
                    ) {
                        assert resource != null;
                        entityManager
                            .createNativeQuery(new String(resource.readAllBytes()))
                            .executeUpdate();
                    } catch(IOException e) {
                        throw new RuntimeException(e);
                    }
                } else {
                    log.debugv("Meta-Entry: {0}", entry.getKey().getSimpleName());
                }
            }
            inited.addAll(noPendingDependency.keySet());
            toInitialize.removeAll(noPendingDependency.keySet());
        }
        QuarkusTransaction.commit();
        container
            .requestContext()
            .deactivate();
    }

    @Override
    public void afterEach(QuarkusTestMethodContext context) {
        Optional
            .ofNullable(Arc.container())
            .map(c -> c
                .instance(EntityManager.class)
                .orElse(null)
            )
            .ifPresent(
                em -> directlyUsedTables(
                    context
                        .getOuterInstances()
                        .stream()
                        .map(Object::getClass),
                    context
                        .getTestInstance()
                        .getClass(),
                    context.getTestMethod()
                ).forEach((c, w) -> {
                    if(w) {
                        Optional
                            .ofNullable(INITIALIZED.get(em))
                            .ifPresent(s -> {
                                if(s.remove(c)) {
                                    log.debugv("Dirtied: {0}", c.getSimpleName());
                                    dependentsOf(c)
                                        .forEach(d -> {
                                            if(s.remove(d)) {
                                                log.debugv("Dirtied: {0}", d.getSimpleName());
                                            }
                                        });
                                }
                            });
                    }
                })
            );
    }

    @Override
    public void beforeEach(QuarkusTestMethodContext context) {
        var tables = usedTables(
            context
                .getOuterInstances()
                .stream()
                .map(Object::getClass),
            context
                .getTestInstance()
                .getClass(),
            context.getTestMethod()
        );
        log.debugv(
            "entities used by {0}:\n\t{1}",
            context.getTestMethod(),
            tables
                .entrySet()
                .stream()
                .map(e -> String.format(
                    "%s: %s",
                    e.getValue() ? "WRITE" : "READ",
                    e.getKey().getSimpleName()
                ))
                .collect(Collectors.joining("\n\t"))
        );
        initialize(tables.keySet());
    }

    @Override
    @NullMarked
    public Set<Lock> provideForMethod(List<Class<?>> enclosingInstanceTypes, Class<?> testClass, Method testMethod) {
        return usedTables(enclosingInstanceTypes.stream(), testClass, testMethod)
            .entrySet()
            .stream()
            .map(e -> new Lock(
                e
                    .getKey()
                    .getCanonicalName(),
                e.getValue() ? ResourceAccessMode.READ_WRITE : ResourceAccessMode.READ
            ))
            .collect(Collectors.toUnmodifiableSet());
    }
}
