package com.paulrule.mathapi;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static org.assertj.core.api.Assertions.assertThat;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.lang.ArchRule;
import org.junit.jupiter.api.Test;

/** STANDARDS.md §2 dependency rules. */
class ArchitectureTest {

    private static final String ROOT = "com.paulrule.mathapi";
    private static final String FIXTURE = "com.paulrule.archfixture";

    /** Plain Java only; this also keeps adapter and config out of the domain. */
    static ArchRule domainDependsOnlyOnJava(String root) {
        return classes()
                .that()
                .resideInAPackage(root + ".domain..")
                .should()
                .onlyDependOnClassesThat()
                .resideInAnyPackage("java..", root + ".domain..")
                .allowEmptyShould(true);
    }

    static ArchRule applicationIsFrameworkFreeAndPointsInwards(String root) {
        return noClasses()
                .that()
                .resideInAPackage(root + ".application..")
                .should()
                .dependOnClassesThat()
                .resideInAnyPackage("org.springframework..", root + ".adapter..", root + ".config..")
                .allowEmptyShould(true);
    }

    static ArchRule adapterDoesNotDependOnConfig(String root) {
        return noClasses()
                .that()
                .resideInAPackage(root + ".adapter..")
                .should()
                .dependOnClassesThat()
                .resideInAPackage(root + ".config..")
                .allowEmptyShould(true);
    }

    @Test
    void mainClassesKeepToTheLayerRules() {
        JavaClasses main = new ClassFileImporter()
                .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
                .importPackages(ROOT);

        domainDependsOnlyOnJava(ROOT).check(main);
        applicationIsFrameworkFreeAndPointsInwards(ROOT).check(main);
        adapterDoesNotDependOnConfig(ROOT).check(main);
    }

    @Test
    void domainClassDependingOnSpringIsRejected() {
        JavaClasses fixture = new ClassFileImporter().importPackages(FIXTURE + ".domain");

        assertThat(domainDependsOnlyOnJava(FIXTURE).evaluate(fixture).hasViolation())
                .isTrue();
    }

    @Test
    void applicationClassDependingOnSpringIsRejected() {
        JavaClasses fixture = new ClassFileImporter().importPackages(FIXTURE + ".application");

        assertThat(applicationIsFrameworkFreeAndPointsInwards(FIXTURE)
                        .evaluate(fixture)
                        .hasViolation())
                .isTrue();
    }

    @Test
    void adapterClassDependingOnConfigIsRejected() {
        JavaClasses fixture = new ClassFileImporter().importPackages(FIXTURE + ".adapter", FIXTURE + ".config");

        assertThat(adapterDoesNotDependOnConfig(FIXTURE).evaluate(fixture).hasViolation())
                .isTrue();
    }
}
