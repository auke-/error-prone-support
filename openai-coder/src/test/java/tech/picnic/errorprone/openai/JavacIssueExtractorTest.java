package tech.picnic.errorprone.openai;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.params.provider.Arguments.arguments;

import com.google.common.collect.ImmutableSet;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.stream.Stream;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import tech.picnic.errorprone.openai.IssueExtractor.Issue;

final class JavacIssueExtractorTest {
  private final IssueExtractor<String> issueExtractor = new JavacIssueExtractor();

  // XXX: Add column absent test case.
  // XXX: Add line absent test case (and update the code to cover this case; see the Plexus code).
  private static Stream<Arguments> extractTestCases() {
    /* { input, expected } */
    return Stream.of(
        arguments("", ImmutableSet.of()),
        arguments("foo", ImmutableSet.of()),
        arguments(
            """
            relative/path/to/MyClass.java:[30,22] no comment
            """,
            ImmutableSet.of()),
        arguments(
            """
            /absolute/path/to/MyClass.java:[30,22] no comment
            """,
            ImmutableSet.of(
                new Issue(
                    "no comment",
                    "/absolute/path/to/MyClass.java",
                    OptionalInt.of(30),
                    OptionalInt.of(22)))),
        arguments(
            """
            /absolute/path/to/another/Class.java:[10,17] cannot find symbol
              symbol:   class MySymbol
              location: class another.Class
            """,
            ImmutableSet.of(
                new Issue(
                    """
                    cannot find symbol
                      symbol:   class MySymbol
                      location: class another.Class""",
                    "/absolute/path/to/another/Class.java",
                    OptionalInt.of(10),
                    OptionalInt.of(17)))),
        arguments(
            """
            /file/with/errorprone/violation/Foo.java:[2,4] [UnusedVariable] The field 'X' is never read.
               (see https://errorprone.info/bugpattern/UnusedVariable)
              Did you mean to remove this line?
            """,
            ImmutableSet.of(
                new Issue(
                    """
                    [UnusedVariable] The field 'X' is never read.
                      Did you mean to remove this line?""",
                    "/file/with/errorprone/violation/Foo.java",
                    OptionalInt.of(2),
                    OptionalInt.of(4)))));
  }

  @ParameterizedTest
  @MethodSource("extractTestCases")
  void extract(String input, ImmutableSet<Issue<String>> expected) {
    assertThat(issueExtractor.extract(input)).containsExactlyElementsOf(expected);
  }
}