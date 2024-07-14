package tech.picnic.errorprone.testngjunit;

import com.google.errorprone.BugCheckerRefactoringTestHelper;
import com.google.errorprone.BugCheckerRefactoringTestHelper.TestMode;
import com.google.errorprone.CompilationTestHelper;
import org.junit.jupiter.api.Test;

final class TestNGJUnitMigrationTest {
  @Test
  void identification() {
    CompilationTestHelper.newInstance(TestNGJUnitMigration.class, getClass())
        .addSourceLines(
            "A.java",
            "import java.util.stream.Stream;",
            "import org.testng.annotations.DataProvider;",
            "import org.testng.annotations.Test;",
            "",
            "@Test",
            "public class A {",
            "  // BUG: Diagnostic contains:",
            "  public void classLevelAnnotation() {}",
            "",
            "  public static void staticNotATest() {}",
            "",
            "  private void notATest() {}",
            "",
            "  @Test(description = \"bar\")",
            "  // BUG: Diagnostic contains:",
            "  public void methodAnnotation() {}",
            "",
            "  @Test",
            "  public static class B {",
            "    // BUG: Diagnostic contains:",
            "    public void nestedClass() {}",
            "  }",
            "",
            "  @Test(dataProvider = \"\")",
            "  // BUG: Diagnostic contains:",
            "  public void dataProviderEmptyString() {}",
            "",
            "  @Test(dataProvider = \"dataProviderTestCases\")",
            "  // BUG: Diagnostic contains:",
            "  public void dataProvider(int foo) {}",
            "",
            "  @DataProvider",
            "  // BUG: Diagnostic contains:",
            "  private static Object[][] dataProviderTestCases() {",
            "    return new Object[][] {{1}, {2}};",
            "  }",
            "",
            "  @DataProvider",
            "  private static Object[][] unusedDataProvider() {",
            "    return new Object[][] {{1}, {2}};",
            "  }",
            "",
            "  @Test(dataProvider = \"notMigratableDataProviderTestCases\")",
            "  // BUG: Diagnostic contains:",
            "  public void notMigratableDataProvider(int foo) {}",
            "",
            "  @DataProvider",
            "  private static Object[][] notMigratableDataProviderTestCases() {",
            "    return Stream.of(1, 2, 3).map(i -> new Object[] {i}).toArray(Object[][]::new);",
            "  }",
            "}")
        .doTest();
  }

  @Test
  void identificationConservativeMode() {
    CompilationTestHelper.newInstance(TestNGJUnitMigration.class, getClass())
        .setArgs("-XepOpt:TestNGJUnitMigration:ConservativeMode=true")
        .addSourceLines(
            "A.java",
            "import java.util.stream.Stream;",
            "import org.testng.annotations.DataProvider;",
            "import org.testng.annotations.Test;",
            "",
            "@Test",
            "public class A {",
            "  public void classLevelAnnotation() {}",
            "",
            "  @Test(description = \"bar\")",
            "  public void methodAnnotation() {}",
            "",
            "  @Test",
            "  public static class B {",
            "    // BUG: Diagnostic contains:",
            "    public void nestedClass() {}",
            "  }",
            "",
            "  @Test(dataProvider = \"notMigratableDataProviderTestCases\")",
            "  public void notMigratableDataProvider(int foo) {}",
            "",
            "  @DataProvider",
            "  private static Object[][] notMigratableDataProviderTestCases() {",
            "    return Stream.of(1, 2, 3).map(i -> new Object[] {i}).toArray(Object[][]::new);",
            "  }",
            "}")
        .addSourceLines(
            "B.java",
            "import org.testng.annotations.Test;",
            "",
            "@Test",
            "public class B {",
            "  public void classLevelAnnotation() {}",
            "",
            "  @Test(description = \"bar\")",
            "  public void methodAnnotation() {}",
            "",
            "  @Test(testName = \"unsupportedAttribute\")",
            "  public void unsupportedAttribute() {}",
            "",
            "  @Test(testName = \"unsupportedAttribute\", suiteName = \"unsupportedAttribute\")",
            "  public void multipleUnsupportedAttributes() {}",
            "}")
        .addSourceLines(
            "C.java",
            "import org.testng.annotations.Test;",
            "",
            "@Test",
            "public class C {",
            "  // BUG: Diagnostic contains:",
            "  public void classLevelAnnotation() {}",
            "",
            "  @Test(description = \"bar\")",
            "  // BUG: Diagnostic contains:",
            "  public void methodAnnotation() {}",
            "}")
        .doTest();
  }

  @Test
  void replacement() {
    BugCheckerRefactoringTestHelper.newInstance(TestNGJUnitMigration.class, getClass())
        .addInputLines(
            "A.java",
            "import static org.testng.Assert.*;",
            "",
            "import org.testng.annotations.AfterClass;",
            "import org.testng.annotations.AfterMethod;",
            "import org.testng.annotations.AfterTest;",
            "import org.testng.annotations.BeforeClass;",
            "import org.testng.annotations.BeforeMethod;",
            "import org.testng.annotations.BeforeTest;",
            "import org.testng.annotations.DataProvider;",
            "import org.testng.annotations.Test;",
            "",
            "@Test",
            "class A {",
            "  @BeforeTest",
            "  private void setupTest() {}",
            "",
            "  @BeforeClass",
            "  private void setupClass() {}",
            "",
            "  @BeforeMethod",
            "  private void setup() {}",
            "",
            "  @AfterTest",
            "  private void teardownTest() {}",
            "",
            "  @AfterClass",
            "  private void teardownClass() {}",
            "",
            "  @AfterMethod",
            "  private void teardown() {}",
            "",
            "  public void classLevelAnnotation() {}",
            "",
            "  @Test(priority = 1, timeOut = 500, description = \"unit\")",
            "  public void priorityTimeOutAndDescription() {}",
            "",
            "  @Test(testName = \"unsupportedAttribute\")",
            "  public void unsupportedAttribute() {}",
            "",
            "  @Test(testName = \"unsupportedAttribute\", suiteName = \"unsupportedAttribute\")",
            "  public void multipleUnsupportedAttributes() {}",
            "",
            "  @Test(dataProvider = \"dataProviderTestCases\")",
            "  public void dataProvider(String string, int number) {}",
            "",
            "  @DataProvider",
            "  private Object[][] dataProviderTestCases() {",
            "    int[] values = new int[] {1, 2, 3};",
            "    return new Object[][] {",
            "      {\"1\", values[0], getClass()},",
            "      {\"2\", values[1], this.getClass()},",
            "      {\"3\", /* inline comment */ values[2], getClass()}",
            "    };",
            "  }",
            "",
            "  @Test(dataProvider = \"dataProviderFieldReturnValueTestCases\")",
            "  public void dataProviderFieldReturnValue(int foo, int bar) {}",
            "",
            "  @DataProvider",
            "  private Object[][] dataProviderFieldReturnValueTestCases() {",
            "    Object[][] foo = new Object[][] {{1, 2}};",
            "    return foo;",
            "  }",
            "",
            "  @Test(dataProvider = \"dataProviderThrowsTestCases\")",
            "  public void dataProviderThrows(String foo, int bar) {}",
            "",
            "  @DataProvider",
            "  private Object[][] dataProviderThrowsTestCases() throws RuntimeException {",
            "    return new Object[][] {",
            "      {\"1\", 0},",
            "    };",
            "  }",
            "",
            "  @Test(expectedExceptions = RuntimeException.class)",
            "  public void singleExpectedException() {",
            "    throw new RuntimeException(\"foo\");",
            "  }",
            "",
            "  @Test(expectedExceptions = {RuntimeException.class})",
            "  public void singleExpectedExceptionArray() {",
            "    throw new RuntimeException(\"foo\");",
            "  }",
            "",
            "  @Test(expectedExceptions = {})",
            "  public void emptyExpectedExceptions() {}",
            "",
            "  @Test(expectedExceptions = {IllegalArgumentException.class, RuntimeException.class})",
            "  public void multipleExpectedExceptions() {",
            "    throw new RuntimeException(\"foo\");",
            "  }",
            "",
            "  @Test(enabled = false)",
            "  public void disabledTest() {}",
            "",
            "  @Test(enabled = true)",
            "  public void enabledTest() {}",
            "",
            "  @Test(groups = \"foo\")",
            "  public void groupsTest() {}",
            "",
            "  @Test(groups = {\"foo\", \"bar\"})",
            "  public void multipleGroupsTest() {}",
            "",
            "  @Test(groups = {})",
            "  public void emptyGroupsTest() {}",
            "",
            "  @Test(groups = \"\")",
            "  public void invalidGroupsNameTest() {}",
            "",
            "  @Test(groups = \"   whitespace  \")",
            "  public void whitespaceGroupsNameTest() {}",
            "}")
        .addOutputLines(
            "A.java",
            "import static java.util.concurrent.TimeUnit.MILLISECONDS;",
            "import static org.junit.jupiter.params.provider.Arguments.arguments;",
            "",
            "import java.util.stream.Stream;",
            "import org.junit.jupiter.api.Disabled;",
            "import org.junit.jupiter.api.DisplayName;",
            "import org.junit.jupiter.api.MethodOrderer;",
            "import org.junit.jupiter.api.Order;",
            "import org.junit.jupiter.api.Tag;",
            "import org.junit.jupiter.api.TestMethodOrder;",
            "import org.junit.jupiter.api.Timeout;",
            "import org.junit.jupiter.params.ParameterizedTest;",
            "import org.junit.jupiter.params.provider.Arguments;",
            "import org.junit.jupiter.params.provider.MethodSource;",
            "import org.testng.annotations.AfterClass;",
            "import org.testng.annotations.AfterMethod;",
            "import org.testng.annotations.AfterTest;",
            "import org.testng.annotations.BeforeClass;",
            "import org.testng.annotations.BeforeMethod;",
            "import org.testng.annotations.BeforeTest;",
            "import org.testng.annotations.DataProvider;",
            "import org.testng.annotations.Test;",
            "",
            "@TestMethodOrder(MethodOrderer.OrderAnnotation.class)",
            "class A {",
            "  @org.junit.jupiter.api.BeforeEach",
            "  private void setupTest() {}",
            "",
            "  @org.junit.jupiter.api.BeforeAll",
            "  private static void setupClass() {}",
            "",
            "  @org.junit.jupiter.api.BeforeEach",
            "  private void setup() {}",
            "",
            "  @org.junit.jupiter.api.AfterEach",
            "  private void teardownTest() {}",
            "",
            "  @org.junit.jupiter.api.AfterAll",
            "  private static void teardownClass() {}",
            "",
            "  @org.junit.jupiter.api.AfterEach",
            "  private void teardown() {}",
            "",
            "  @org.junit.jupiter.api.Test",
            "  public void classLevelAnnotation() {}",
            "",
            "  @Order(1)",
            "  @Timeout(value = 500, unit = MILLISECONDS)",
            "  @DisplayName(\"unit\")",
            "  @org.junit.jupiter.api.Test",
            "  public void priorityTimeOutAndDescription() {}",
            "",
            "  // XXX: Attribute `testName` is not supported, value: `\"unsupportedAttribute\"`",
            "  @org.junit.jupiter.api.Test",
            "  public void unsupportedAttribute() {}",
            "",
            "  // XXX: Attribute `testName` is not supported, value: `\"unsupportedAttribute\"`",
            "  // XXX: Attribute `suiteName` is not supported, value: `\"unsupportedAttribute\"`",
            "  @org.junit.jupiter.api.Test",
            "  public void multipleUnsupportedAttributes() {}",
            "",
            "  @ParameterizedTest",
            "  @MethodSource(\"dataProviderTestCases\")",
            "  public void dataProvider(String string, int number) {}",
            "",
            "  private static Stream<Arguments> dataProviderTestCases() {",
            "    int[] values = new int[] {1, 2, 3};",
            "    return Stream.of(",
            "        arguments(\"1\", values[0], A.class),",
            "        arguments(\"2\", values[1], A.class),",
            "        arguments(\"3\", /* inline comment */ values[2], A.class));",
            "  }",
            "",
            "  // XXX: Attribute `dataProvider` is not supported, value:",
            "  // `\"dataProviderFieldReturnValueTestCases\"`",
            "",
            "  public void dataProviderFieldReturnValue(int foo, int bar) {}",
            "",
            "  @DataProvider",
            "  private Object[][] dataProviderFieldReturnValueTestCases() {",
            "    Object[][] foo = new Object[][] {{1, 2}};",
            "    return foo;",
            "  }",
            "",
            "  @ParameterizedTest",
            "  @MethodSource(\"dataProviderThrowsTestCases\")",
            "  public void dataProviderThrows(String foo, int bar) {}",
            "",
            "  private static Stream<Arguments> dataProviderThrowsTestCases() throws RuntimeException {",
            "    return Stream.of(arguments(\"1\", 0));",
            "  }",
            "",
            "  @org.junit.jupiter.api.Test",
            "  public void singleExpectedException() {",
            "    org.junit.jupiter.api.Assertions.assertThrows(",
            "        RuntimeException.class,",
            "        () -> {",
            "          throw new RuntimeException(\"foo\");",
            "        });",
            "  }",
            "",
            "  @org.junit.jupiter.api.Test",
            "  public void singleExpectedExceptionArray() {",
            "    org.junit.jupiter.api.Assertions.assertThrows(",
            "        RuntimeException.class,",
            "        () -> {",
            "          throw new RuntimeException(\"foo\");",
            "        });",
            "  }",
            "",
            "  @org.junit.jupiter.api.Test",
            "  public void emptyExpectedExceptions() {}",
            "",
            "  // XXX: Removed handling of `RuntimeException.class` because this migration doesn't support",
            "  // XXX: multiple expected exceptions.",
            "  @org.junit.jupiter.api.Test",
            "  public void multipleExpectedExceptions() {",
            "    org.junit.jupiter.api.Assertions.assertThrows(",
            "        IllegalArgumentException.class,",
            "        () -> {",
            "          throw new RuntimeException(\"foo\");",
            "        });",
            "  }",
            "",
            "  @Disabled",
            "  @org.junit.jupiter.api.Test",
            "  public void disabledTest() {}",
            "",
            "  @org.junit.jupiter.api.Test",
            "  public void enabledTest() {}",
            "",
            "  @Tag(\"foo\")",
            "  @org.junit.jupiter.api.Test",
            "  public void groupsTest() {}",
            "",
            "  @Tag(\"foo\")",
            "  @Tag(\"bar\")",
            "  @org.junit.jupiter.api.Test",
            "  public void multipleGroupsTest() {}",
            "",
            "  @org.junit.jupiter.api.Test",
            "  public void emptyGroupsTest() {}",
            "",
            "  // XXX: Attribute `groups` is not supported, value: `\"\"`",
            "  @org.junit.jupiter.api.Test",
            "  public void invalidGroupsNameTest() {}",
            "",
            "  @Tag(\"whitespace\")",
            "  @org.junit.jupiter.api.Test",
            "  public void whitespaceGroupsNameTest() {}",
            "}")
        .doTest(TestMode.TEXT_MATCH);
  }

  @Test
  void replacementMoreBehaviorPreserving() {
    BugCheckerRefactoringTestHelper.newInstance(TestNGJUnitMigration.class, getClass())
            .setArgs("-XepOpt:TestNGJUnitMigration:BehaviorPreserving=true")
            .addInputLines(
            "A.java",
            "import org.testng.annotations.DataProvider;",
            "import org.testng.annotations.Test;",
            "",
            "@Test",
            "class A {",
            "  @Test(dataProvider = \"dataProviderTestCases\")",
            "  public void dataProvider(String string, int number) {}",
            "",
            "  @DataProvider",
            "  private Object[][] dataProviderTestCases() {",
            "    int[] values = new int[] {1, 2, 3};",
            "    return new Object[][] {",
            "      {\"1\", values[0], getClass()},",
            "      {\"2\", values[1], this.getClass()},",
            "      {\"3\", /* inline comment */ values[2], getClass()}",
            "    };",
            "  }",
            "",
            "  @Test(dataProvider = \"dataProviderFieldReturnValueTestCases\")",
            "  public void dataProviderFieldReturnValue(int foo, int bar) {}",
            "",
            "  @DataProvider",
            "  private Object[][] dataProviderFieldReturnValueTestCases() {",
            "    Object[][] foo = new Object[][] {{1, 2}};",
            "    return foo;",
            "  }",
            "",
            "  @Test(dataProvider = \"dataProviderThrowsTestCases\")",
            "  public void dataProviderThrows(String foo, int bar) {}",
            "",
            "  @DataProvider",
            "  private Object[][] dataProviderThrowsTestCases() throws RuntimeException {",
            "    return new Object[][] {",
            "      {\"1\", 0},",
            "    };",
            "  }",
            "",
            "  @Test(expectedExceptions = RuntimeException.class)",
            "  public void singleExpectedException() {",
            "    throw new RuntimeException(\"foo\");",
            "  }",
            "",
            "  @Test(expectedExceptions = {RuntimeException.class})",
            "  public void singleExpectedExceptionArray() {",
            "    throw new RuntimeException(\"foo\");",
            "  }",
            "",
            "  @Test(expectedExceptions = {})",
            "  public void emptyExpectedExceptions() {}",
            "",
            "  @Test(expectedExceptions = {IllegalArgumentException.class, RuntimeException.class})",
            "  public void multipleExpectedExceptions() {",
            "    throw new RuntimeException(\"foo\");",
            "  }",
            "}")
        .addOutputLines(
            "A.java",
            "import static java.util.concurrent.TimeUnit.MILLISECONDS;",
            "import static org.junit.jupiter.params.provider.Arguments.arguments;",
            "",
            "import java.util.stream.Stream;",
            "import org.junit.jupiter.params.ParameterizedTest;",
            "import org.junit.jupiter.params.provider.Arguments;",
            "import org.junit.jupiter.params.provider.MethodSource;",
            "import org.testng.annotations.DataProvider;",
            "import org.testng.annotations.Test;",
            "",
            "class A {",
            "  @ParameterizedTest",
            "  @MethodSource(\"dataProviderTestCases\")",
            "  public void dataProvider(String string, int number) {}",
            "",
            "  private static Stream<Arguments> dataProviderTestCases() {",
            "    int[] values = new int[] {1, 2, 3};",
            "    return Stream.of(",
            "        arguments(\"1\", values[0], A.class),",
            "        arguments(\"2\", values[1], A.class),",
            "        arguments(\"3\", /* inline comment */ values[2], A.class));",
            "  }",
            "",
            "  // XXX: Attribute `dataProvider` is not supported, value:",
            "  // `\"dataProviderFieldReturnValueTestCases\"`",
            "",
            "  public void dataProviderFieldReturnValue(int foo, int bar) {}",
            "",
            "  @DataProvider",
            "  private Object[][] dataProviderFieldReturnValueTestCases() {",
            "    Object[][] foo = new Object[][] {{1, 2}};",
            "    return foo;",
            "  }",
            "",
            "  @ParameterizedTest",
            "  @MethodSource(\"dataProviderThrowsTestCases\")",
            "  public void dataProviderThrows(String foo, int bar) {}",
            "",
            "  private static Stream<Arguments> dataProviderThrowsTestCases() throws RuntimeException {",
            "    return Stream.of(arguments(\"1\", 0));",
            "  }",
            "",
            "  @org.junit.jupiter.api.Test",
            "  public void singleExpectedException() {",
            "    org.junit.jupiter.api.Assertions.assertThrows(",
            "        RuntimeException.class,",
            "        () -> {",
            "          throw new RuntimeException(\"foo\");",
            "        });",
            "  }",
            "",
            "  @org.junit.jupiter.api.Test",
            "  public void singleExpectedExceptionArray() {",
            "    org.junit.jupiter.api.Assertions.assertThrows(",
            "        RuntimeException.class,",
            "        () -> {",
            "          throw new RuntimeException(\"foo\");",
            "        });",
            "  }",
            "",
            "  @org.junit.jupiter.api.Test",
            "  public void emptyExpectedExceptions() {}",
            "",
            "  // XXX: Removed handling of `RuntimeException.class` because this migration doesn't support",
            "  // XXX: multiple expected exceptions.",
            "  @org.junit.jupiter.api.Test",
            "  public void multipleExpectedExceptions() {",
            "    org.junit.jupiter.api.Assertions.assertThrows(",
            "        IllegalArgumentException.class,",
            "        () -> {",
            "          throw new RuntimeException(\"foo\");",
            "        });",
            "  }",
            "}")
        .doTest(TestMode.TEXT_MATCH);
  }
}
