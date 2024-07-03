package tech.picnic.errorprone.testngjunit;

import static com.google.common.collect.ImmutableMap.toImmutableMap;
import static com.google.errorprone.matchers.Matchers.allOf;
import static com.google.errorprone.matchers.Matchers.anyOf;
import static com.google.errorprone.matchers.Matchers.hasAnnotation;
import static com.google.errorprone.matchers.Matchers.hasModifier;
import static com.google.errorprone.matchers.Matchers.not;
import static tech.picnic.errorprone.testngjunit.TestNGMatchers.TESTNG_TEST_ANNOTATION;
import static tech.picnic.errorprone.testngjunit.TestNGMatchers.TESTNG_VALUE_FACTORY_METHOD;

import com.google.common.collect.ImmutableMap;
import com.google.errorprone.VisitorState;
import com.google.errorprone.annotations.CanIgnoreReturnValue;
import com.google.errorprone.matchers.Matcher;
import com.google.errorprone.util.ASTHelpers;
import com.sun.source.tree.AssignmentTree;
import com.sun.source.tree.ClassTree;
import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.tree.IdentifierTree;
import com.sun.source.tree.MethodTree;
import com.sun.source.tree.Tree;
import com.sun.source.util.TreeScanner;
import java.util.Optional;
import javax.lang.model.element.Modifier;
import org.jspecify.annotations.Nullable;
import tech.picnic.errorprone.testngjunit.TestNgMetadata.AnnotationMetadata;
import tech.picnic.errorprone.testngjunit.TestNgMetadata.DataProviderMetadata;
import tech.picnic.errorprone.testngjunit.TestNgMetadata.SetupTeardownType;

/**
 * A {@link TreeScanner} which will scan a {@link com.sun.source.tree.CompilationUnitTree} and
 * collect data required for the migration from each class in the compilation unit.
 *
 * <p>This data can be retrieved using {@link #collectMetadataForClasses(CompilationUnitTree)}.
 */
final class TestNGScanner extends TreeScanner<@Nullable Void, TestNgMetadata.Builder> {
  private static final Matcher<MethodTree> TESTNG_TEST_METHOD =
      anyOf(
          hasAnnotation("org.testng.annotations.Test"),
          allOf(hasModifier(Modifier.PUBLIC), not(hasModifier(Modifier.STATIC))));

  private final ImmutableMap.Builder<ClassTree, TestNgMetadata> metadataBuilder =
      ImmutableMap.builder();
  private final VisitorState state;

  TestNGScanner(VisitorState state) {
    this.state = state;
  }

  @Override
  public @Nullable Void visitClass(ClassTree tree, TestNgMetadata.Builder unused) {
    TestNgMetadata.Builder builder = TestNgMetadata.builder();
    builder.setClassTree(tree);
    getTestNgAnnotation(tree, state).ifPresent(builder::setClassLevelAnnotationMetadata);
    super.visitClass(tree, builder);
    metadataBuilder.put(tree, builder.build());

    return null;
  }

  @Override
  public @Nullable Void visitMethod(MethodTree tree, TestNgMetadata.Builder builder) {
    if (ASTHelpers.isGeneratedConstructor(tree)) {
      return super.visitMethod(tree, builder);
    }

    if (TESTNG_VALUE_FACTORY_METHOD.matches(tree, state) && DataProviderMigrator.canFix(tree)) {
      builder
          .dataProviderMetadataBuilder()
          .put(tree.getName().toString(), DataProviderMetadata.create(tree));
      return super.visitMethod(tree, builder);
    }

    Optional<SetupTeardownType> setupTeardownType = SetupTeardownType.matchType(tree, state);
    if (setupTeardownType.isPresent()) {
      builder.setupTeardownBuilder().put(tree, setupTeardownType.orElseThrow());
      return super.visitMethod(tree, builder);
    }

    if (TESTNG_TEST_METHOD.matches(tree, state)) {
      getTestNgAnnotation(tree, state)
          .or(builder::getClassLevelAnnotationMetadata)
          .ifPresent(annotation -> builder.methodAnnotationsBuilder().put(tree, annotation));
    }

    return super.visitMethod(tree, builder);
  }

  public ImmutableMap<ClassTree, TestNgMetadata> collectMetadataForClasses(
      CompilationUnitTree tree) {
    scan(tree, null);
    return metadataBuilder.build();
  }

  @CanIgnoreReturnValue
  private static Optional<AnnotationMetadata> getTestNgAnnotation(Tree tree, VisitorState state) {
    return ASTHelpers.getAnnotations(tree).stream()
        .filter(annotation -> TESTNG_TEST_ANNOTATION.matches(annotation, state))
        .findFirst()
        .map(
            annotationTree ->
                AnnotationMetadata.create(
                    annotationTree,
                    annotationTree.getArguments().stream()
                        .filter(AssignmentTree.class::isInstance)
                        .map(AssignmentTree.class::cast)
                        .collect(
                            toImmutableMap(
                                assignment ->
                                    ((IdentifierTree) assignment.getVariable())
                                        .getName()
                                        .toString(),
                                AssignmentTree::getExpression))));
  }
}