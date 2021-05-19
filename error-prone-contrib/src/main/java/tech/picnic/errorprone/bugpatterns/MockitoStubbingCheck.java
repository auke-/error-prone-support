package tech.picnic.errorprone.bugpatterns;

import static com.google.errorprone.matchers.Matchers.staticMethod;

import com.google.auto.service.AutoService;
import com.google.common.collect.Iterables;
import com.google.errorprone.BugPattern;
import com.google.errorprone.BugPattern.LinkType;
import com.google.errorprone.BugPattern.SeverityLevel;
import com.google.errorprone.BugPattern.StandardTags;
import com.google.errorprone.VisitorState;
import com.google.errorprone.bugpatterns.BugChecker;
import com.google.errorprone.bugpatterns.BugChecker.MethodInvocationTreeMatcher;
import com.google.errorprone.fixes.SuggestedFix;
import com.google.errorprone.matchers.Description;
import com.google.errorprone.matchers.Matcher;
import com.sun.source.tree.ExpressionTree;
import com.sun.source.tree.MethodInvocationTree;
import java.util.List;

/**
 * A {@link BugChecker} which flags method invocations for which all arguments are wrapped using
 * {@link org.mockito.Mockito#eq}; this is redundant.
 */
@AutoService(BugChecker.class)
@BugPattern(
    name = "MockitoStubbing",
    summary = "Don't unnecessarily use Mockito's `eq(...)`",
    linkType = LinkType.NONE,
    severity = SeverityLevel.SUGGESTION,
    tags = StandardTags.SIMPLIFICATION)
public final class MockitoStubbingCheck extends BugChecker implements MethodInvocationTreeMatcher {
  private static final long serialVersionUID = 1L;
  private static final Matcher<ExpressionTree> MOCKITO_EQ_METHOD =
      staticMethod().onClass("org.mockito.ArgumentMatchers").named("eq");

  @Override
  public Description matchMethodInvocation(MethodInvocationTree tree, VisitorState state) {
    List<? extends ExpressionTree> arguments = tree.getArguments();
    if (arguments.isEmpty() || !arguments.stream().allMatch(arg -> isEqInvocation(arg, state))) {
      return Description.NO_MATCH;
    }

    SuggestedFix.Builder suggestedFix = SuggestedFix.builder();
    for (ExpressionTree arg : arguments) {
      suggestedFix.replace(
          arg,
          Util.treeToString(
              Iterables.getOnlyElement(((MethodInvocationTree) arg).getArguments()), state));
    }

    return describeMatch(tree, suggestedFix.build());
  }

  private static boolean isEqInvocation(ExpressionTree tree, VisitorState state) {
    return tree instanceof MethodInvocationTree && MOCKITO_EQ_METHOD.matches(tree, state);
  }
}