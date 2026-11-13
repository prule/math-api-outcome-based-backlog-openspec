package com.paulrule.mathapi;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static org.assertj.core.api.Assertions.assertThat;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.lang.ArchRule;
import java.util.List;
import org.junit.jupiter.api.Test;

/** STANDARDS.md §2 dependency rules. */
class ArchitectureTest {

    static List<ArchRule> layerRules(String root) {
        return List.of(
                classes()
                        .that()
                        .resideInAPackage(root + ".domain..")
                        .should()
                        .onlyDependOnClassesThat()
                        .resideInAnyPackage("java..", root + ".domain..")
                        .allowEmptyShould(true),
                noClasses()
                        .that()
                        .resideInAPackage(root + ".application..")
                        .should()
                        .dependOnClassesThat()
                        .resideInAnyPackage("org.springframework..", root + ".adapter..", root + ".config..")
                        .allowEmptyShould(true),
                noClasses()
                        .that()
                        .resideInAPackage(root + ".domain..")
                        .should()
                        .dependOnClassesThat()
                        .resideInAnyPackage(root + ".adapter..", root + ".config..")
                        .allowEmptyShould(true));
    }

    @Test
    void mainClassesKeepToTheLayerRules() {
        JavaClasses main = new ClassFileImporter()
                .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
                .importPackages("com.paulrule.mathapi");

        layerRules("com.paulrule.mathapi").forEach(rule -> rule.check(main));
    }

    @Test
    void domainClassDependingOnSpringIsRejected() {
        JavaClasses fixture = new ClassFileImporter().importPackages("com.paulrule.archfixture");

        assertThat(layerRules("com.paulrule.archfixture")
                        .getFirst()
                        .evaluate(fixture)
                        .hasViolation())
                .isTrue();
    }
}
