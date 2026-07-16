package com.aiworkspace.architecture;

import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.lang.ArchCondition;
import com.tngtech.archunit.lang.ConditionEvents;
import com.tngtech.archunit.lang.SimpleConditionEvent;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.springframework.scheduling.annotation.Async;
import org.springframework.transaction.event.TransactionalEventListener;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.methods;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

class ArchitectureRulesTest {

    static JavaClasses importedClasses;

    @BeforeAll
    static void importClasses() {
        importedClasses = new ClassFileImporter()
                .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
                .importPackages("com.aiworkspace");
    }

    // Rule 1: Domain layer must not depend on Spring, JPA, Hibernate, or Jackson
    @Test
    void domainShouldBeFrameworkFree() {
        noClasses()
                .that().resideInAPackage("..domain..")
                .should().dependOnClassesThat().resideInAnyPackage(
                        "org.springframework..",
                        "jakarta.persistence..",
                        "org.hibernate..",
                        "com.fasterxml.jackson.."
                )
                .check(importedClasses);
    }

    // Rule 2: Application layer must not depend on infrastructure
    @Test
    void applicationShouldNotDependOnInfrastructure() {
        noClasses()
                .that().resideInAPackage("..application..")
                .should().dependOnClassesThat().resideInAPackage("..infrastructure..")
                .check(importedClasses);
    }

    // Rule 3: No module may import another module's domain model classes.
    // Cross-module listeners are explicitly exempted — they consume domain events
    // from *.domain.event.*, which is allowed per ADR-033.
    @Test
    void modulesShouldNotImportOtherModulesDomainModel() {
        String[] modules = {"iam", "billing", "documents", "learning", "notifications", "audit"};

        for (String module : modules) {
            for (String other : modules) {
                if (module.equals(other)) continue;

                noClasses()
                        .that().resideInAPackage("com.aiworkspace." + module + "..")
                        .and().resideOutsideOfPackage(
                                "com.aiworkspace." + module + ".infrastructure.listener..")
                        .should().dependOnClassesThat()
                        .resideInAPackage("com.aiworkspace." + other + ".domain.model..")
                        .because("Module '" + module + "' must not import domain model from '" + other
                                 + "'. Use domain events (*.domain.event.*) for cross-module communication.")
                        .check(importedClasses);
            }
        }
    }

    // Rule 4: All @TransactionalEventListener methods must be @Async
    // Synchronous listeners block the originating transaction thread during SMTP/DB side effects
    @Test
    void transactionalListenersShouldBeAsync() {
        methods()
                .that().areAnnotatedWith(TransactionalEventListener.class)
                .should().beAnnotatedWith(Async.class)
                .because("@TransactionalEventListener without @Async blocks the originating "
                         + "transaction thread. Add @Async so the listener runs independently.")
                .check(importedClasses);
    }

    // Rule 5: Presentation layer must not depend on infrastructure directly
    @Test
    void presentationShouldNotDependOnInfrastructure() {
        noClasses()
                .that().resideInAPackage("..presentation..")
                .should().dependOnClassesThat().resideInAPackage("..infrastructure..")
                .check(importedClasses);
    }

    // Rule 6: Infrastructure adapters must implement at least one port interface
    @Test
    void infrastructureAdaptersShouldImplementPorts() {
        ArchCondition<JavaClass> implementsAtLeastOneInterface =
                new ArchCondition<>("implement at least one port interface") {
                    @Override
                    public void check(JavaClass item, ConditionEvents events) {
                        if (item.getInterfaces().isEmpty()) {
                            events.add(SimpleConditionEvent.violated(item,
                                    item.getName() + " does not implement any interface — "
                                    + "adapters must implement a port to enforce the dependency rule"));
                        }
                    }
                };

        classes()
                .that().resideInAPackage("..infrastructure.persistence.adapter..")
                .and().haveSimpleNameEndingWith("Adapter")
                .should(implementsAtLeastOneInterface)
                .check(importedClasses);
    }
}
