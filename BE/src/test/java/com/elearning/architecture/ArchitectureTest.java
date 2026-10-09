package com.elearning.architecture;

import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.domain.JavaField;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.junit.AnalyzeClasses;
import com.tngtech.archunit.junit.ArchTest;
import com.tngtech.archunit.lang.ArchCondition;
import com.tngtech.archunit.lang.ArchRule;
import com.tngtech.archunit.lang.ConditionEvents;
import com.tngtech.archunit.lang.SimpleConditionEvent;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.ManyToMany;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.classes;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.fields;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.methods;
import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;
import static com.tngtech.archunit.library.dependencies.SlicesRuleDefinition.slices;

/**
 * Tự động kiểm tra các quy tắc kiến trúc của {@code .agents/spring-boot_struct.md} mỗi lần {@code mvn test}.
 */
@AnalyzeClasses(packages = "com.elearning", importOptions = ImportOption.DoNotIncludeTests.class)
class ArchitectureTest {

    private static final List<String> MODULES = List.of("user", "course", "learning", "info");

    // Rule 2.2: module khác chỉ được gọi qua Service interface, không import repository hay entity của nhau.
    @ArchTest
    static final ArchRule modules_do_not_import_repository_or_entity_of_other_modules = classes()
            .that().resideInAnyPackage(modulePackages())
            .should(new ArchCondition<>("not depend on the repository or entity package of another module") {
                @Override
                public void check(JavaClass item, ConditionEvents events) {
                    String own = moduleOf(item.getPackageName());
                    item.getDirectDependenciesFromSelf().forEach(dep -> {
                        String target = dep.getTargetClass().getPackageName();
                        String targetModule = moduleOf(target);
                        if (targetModule != null && !targetModule.equals(own)
                                && (target.contains(".repository") || target.contains(".entity"))) {
                            events.add(SimpleConditionEvent.violated(dep, dep.getDescription()));
                        }
                    });
                }
            });

    // Rule 2.2: không có vòng phụ thuộc giữa các module.
    @ArchTest
    static final ArchRule modules_are_free_of_cycles = slices()
            .matching("com.elearning.(*)..")
            .should().beFreeOfCycles();

    // common là hạ tầng dùng chung, không được biết tới module nghiệp vụ nào.
    @ArchTest
    static final ArchRule common_does_not_depend_on_business_modules = noClasses()
            .that().resideInAPackage("com.elearning.common..")
            .should().dependOnClassesThat().resideInAnyPackage(modulePackages());

    // Rule 2.3: không dùng quan hệ ORM trực tiếp; mọi tham chiếu giữa các entity (kể cả cùng module) là id thuần.
    @ArchTest
    static final ArchRule entities_reference_each_other_by_id_only = fields()
            .that().areDeclaredInClassesThat().areAnnotatedWith(Entity.class)
            .should().notBeAnnotatedWith(ManyToOne.class)
            .andShould().notBeAnnotatedWith(OneToMany.class)
            .andShould().notBeAnnotatedWith(OneToOne.class)
            .andShould().notBeAnnotatedWith(ManyToMany.class);

    // Rule 2.3: enum trong entity luôn lưu dạng chuỗi, tránh ánh xạ sai khi đổi thứ tự enum.
    @ArchTest
    static final ArchRule enum_fields_in_entities_are_stored_as_string = fields()
            .that().areDeclaredInClassesThat().areAnnotatedWith(Entity.class)
            .and(new com.tngtech.archunit.base.DescribedPredicate<JavaField>("have an enum type") {
                @Override
                public boolean test(JavaField field) {
                    return field.getRawType().isEnum();
                }
            })
            .should(new ArchCondition<>("be annotated with @Enumerated(EnumType.STRING)") {
                @Override
                public void check(JavaField field, ConditionEvents events) {
                    boolean ok = field.tryGetAnnotationOfType(Enumerated.class)
                            .map(a -> a.value() == EnumType.STRING).orElse(false);
                    if (!ok) {
                        events.add(SimpleConditionEvent.violated(field, field.getDescription() + " is not @Enumerated(STRING)"));
                    }
                }
            });

    // Rule 2.5: @Transactional của module nghiệp vụ chỉ đặt ở method của *ServiceImpl. Helper hạ tầng trong common
    // (vd IdempotencyTransactionHelper dùng REQUIRES_NEW để tránh self-invocation) nằm ngoài phạm vi luật này.
    @ArchTest
    static final ArchRule transactional_methods_belong_to_service_impl = methods()
            .that().areAnnotatedWith(Transactional.class)
            .and().areDeclaredInClassesThat().resideInAnyPackage(modulePackages())
            .should().beDeclaredInClassesThat().resideInAPackage("..service.impl..");

    @ArchTest
    static final ArchRule service_impl_is_not_transactional_at_class_level = classes()
            .that().resideInAPackage("..service.impl..")
            .and().resideInAnyPackage(modulePackages())
            .should().notBeAnnotatedWith(Transactional.class);

    // Controller không truy cập Repository, không chứa nghiệp vụ.
    @ArchTest
    static final ArchRule controllers_do_not_touch_repositories_or_entities = noClasses()
            .that().resideInAPackage("..controller..")
            .should().dependOnClassesThat().resideInAnyPackage("..repository..", "..entity..");

    // Mọi truy cập dữ liệu đi qua Service: controller chỉ gọi interface, không gọi thẳng *ServiceImpl.
    @ArchTest
    static final ArchRule controllers_depend_on_service_interfaces_only = noClasses()
            .that().resideInAPackage("..controller..")
            .should().dependOnClassesThat().resideInAPackage("..service.impl..");

    // Rule 2.6: tầng DTO không truy vấn dữ liệu.
    @ArchTest
    static final ArchRule dtos_do_not_depend_on_repositories_or_services = noClasses()
            .that().resideInAPackage("..dto..")
            .should().dependOnClassesThat().resideInAnyPackage("..repository..", "..service..", "..entity..");

    // Rule 2.12: không throw RuntimeException trần trong service; lỗi nghiệp vụ đi qua BusinessException.
    @ArchTest
    static final ArchRule services_do_not_throw_raw_runtime_exceptions = noClasses()
            .that().resideInAPackage("..service.impl..")
            .should().callConstructor(RuntimeException.class);

    // Rule 2.12: không @ExceptionHandler riêng lẻ trong controller.
    @ArchTest
    static final ArchRule controllers_have_no_exception_handlers = methods()
            .that().areDeclaredInClassesThat().resideInAPackage("..controller..")
            .should().notBeAnnotatedWith(org.springframework.web.bind.annotation.ExceptionHandler.class);

    private static String[] modulePackages() {
        return MODULES.stream().map(m -> "com.elearning." + m + "..").toArray(String[]::new);
    }

    private static String moduleOf(String packageName) {
        for (String module : MODULES) {
            if (packageName.equals("com.elearning." + module) || packageName.startsWith("com.elearning." + module + ".")) {
                return module;
            }
        }
        return null;
    }
}
