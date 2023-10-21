package tech.picnic.errorprone.workshop.bugpatterns;

import com.google.errorprone.BugCheckerRefactoringTestHelper;
import com.google.errorprone.BugCheckerRefactoringTestHelper.TestMode;
import com.google.errorprone.CompilationTestHelper;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

@Disabled("Enable this when implementing the BugChecker.")
final class Assignment5DeleteIdentityConversionTest {
  @Test
  void identification() {
    CompilationTestHelper.newInstance(Assignment5DeleteIdentityConversion.class, getClass())
        .addSourceLines(
            "A.java",
            "import static com.google.errorprone.matchers.Matchers.instanceMethod;",
            "import static com.google.errorprone.matchers.Matchers.staticMethod;",
            "",
            "import com.google.common.collect.ImmutableBiMap;",
            "import com.google.common.collect.ImmutableList;",
            "import com.google.common.collect.ImmutableListMultimap;",
            "import com.google.common.collect.ImmutableMap;",
            "import com.google.common.collect.ImmutableMultimap;",
            "import com.google.common.collect.ImmutableMultiset;",
            "import com.google.common.collect.ImmutableRangeMap;",
            "import com.google.common.collect.ImmutableRangeSet;",
            "import com.google.common.collect.ImmutableSet;",
            "import com.google.common.collect.ImmutableSetMultimap;",
            "import com.google.common.collect.ImmutableTable;",
            "import com.google.errorprone.matchers.Matcher;",
            "import com.google.errorprone.matchers.Matchers;",
            "",
            "public final class A {",
            "  public void m() {",
            "    // BUG: Diagnostic contains:",
            "    Matcher allOf1 = Matchers.allOf(instanceMethod());",
            "    Matcher allOf2 = Matchers.allOf(instanceMethod(), staticMethod());",
            "    // BUG: Diagnostic contains:",
            "    Matcher anyOf1 = Matchers.anyOf(staticMethod());",
            "    Matcher anyOf2 = Matchers.anyOf(instanceMethod(), staticMethod());",
            "",
            "    // BUG: Diagnostic contains:",
            "    Boolean b1 = Boolean.valueOf(Boolean.FALSE);",
            "    // BUG: Diagnostic contains:",
            "    Boolean b2 = Boolean.valueOf(false);",
            "    // BUG: Diagnostic contains:",
            "    boolean b3 = Boolean.valueOf(Boolean.FALSE);",
            "    // BUG: Diagnostic contains:",
            "    boolean b4 = Boolean.valueOf(false);",
            "",
            "    // BUG: Diagnostic contains:",
            "    Byte byte1 = Byte.valueOf((Byte) Byte.MIN_VALUE);",
            "    // BUG: Diagnostic contains:",
            "    Byte byte2 = Byte.valueOf(Byte.MIN_VALUE);",
            "    // BUG: Diagnostic contains:",
            "    byte byte3 = Byte.valueOf((Byte) Byte.MIN_VALUE);",
            "    // BUG: Diagnostic contains:",
            "    byte byte4 = Byte.valueOf(Byte.MIN_VALUE);",
            "",
            "    // BUG: Diagnostic contains:",
            "    Character c1 = Character.valueOf((Character) 'a');",
            "    // BUG: Diagnostic contains:",
            "    Character c2 = Character.valueOf('a');",
            "    // BUG: Diagnostic contains:",
            "    char c3 = Character.valueOf((Character) 'a');",
            "    // BUG: Diagnostic contains:",
            "    char c4 = Character.valueOf('a');",
            "",
            "    // BUG: Diagnostic contains:",
            "    Double d1 = Double.valueOf((Double) 0.0);",
            "    // BUG: Diagnostic contains:",
            "    Double d2 = Double.valueOf(0.0);",
            "    // BUG: Diagnostic contains:",
            "    double d3 = Double.valueOf((Double) 0.0);",
            "    // BUG: Diagnostic contains:",
            "    double d4 = Double.valueOf(0.0);",
            "",
            "    // BUG: Diagnostic contains:",
            "    Float f1 = Float.valueOf((Float) 0.0F);",
            "    // BUG: Diagnostic contains:",
            "    Float f2 = Float.valueOf(0.0F);",
            "    // BUG: Diagnostic contains:",
            "    float f3 = Float.valueOf((Float) 0.0F);",
            "    // BUG: Diagnostic contains:",
            "    float f4 = Float.valueOf(0.0F);",
            "",
            "    // BUG: Diagnostic contains:",
            "    Integer i1 = Integer.valueOf((Integer) 1);",
            "    // BUG: Diagnostic contains:",
            "    Integer i2 = Integer.valueOf(1);",
            "    // BUG: Diagnostic contains:",
            "    int i3 = Integer.valueOf((Integer) 1);",
            "    // BUG: Diagnostic contains:",
            "    int i4 = Integer.valueOf(1);",
            "",
            "    // BUG: Diagnostic contains:",
            "    Long l1 = Long.valueOf((Long) 1L);",
            "    // BUG: Diagnostic contains:",
            "    Long l2 = Long.valueOf(1L);",
            "    // BUG: Diagnostic contains:",
            "    long l3 = Long.valueOf((Long) 1L);",
            "    // BUG: Diagnostic contains:",
            "    long l4 = Long.valueOf(1L);",
            "",
            "    Long l5 = Long.valueOf((Integer) 1);",
            "    Long l6 = Long.valueOf(1);",
            "    // BUG: Diagnostic contains:",
            "    long l7 = Long.valueOf((Integer) 1);",
            "    // BUG: Diagnostic contains:",
            "    long l8 = Long.valueOf(1);",
            "",
            "    // BUG: Diagnostic contains:",
            "    Short s1 = Short.valueOf((Short) Short.MIN_VALUE);",
            "    // BUG: Diagnostic contains:",
            "    Short s2 = Short.valueOf(Short.MIN_VALUE);",
            "    // BUG: Diagnostic contains:",
            "    short s3 = Short.valueOf((Short) Short.MIN_VALUE);",
            "    // BUG: Diagnostic contains:",
            "    short s4 = Short.valueOf(Short.MIN_VALUE);",
            "",
            "    // BUG: Diagnostic contains:",
            "    String boolStr = Boolean.valueOf(Boolean.FALSE).toString();",
            "    int boolHash = Boolean.valueOf(false).hashCode();",
            "    // BUG: Diagnostic contains:",
            "    int byteHash = Byte.valueOf((Byte) Byte.MIN_VALUE).hashCode();",
            "    String byteStr = Byte.valueOf(Byte.MIN_VALUE).toString();",
            "",
            "    String str1 = String.valueOf(0);",
            "    // BUG: Diagnostic contains:",
            "    String str2 = String.valueOf(\"1\");",
            "",
            "    // BUG: Diagnostic contains:",
            "    ImmutableBiMap<Object, Object> o1 = ImmutableBiMap.copyOf(ImmutableBiMap.of());",
            "    // BUG: Diagnostic contains:",
            "    ImmutableList<Object> o2 = ImmutableList.copyOf(ImmutableList.of());",
            "    ImmutableListMultimap<Object, Object> o3 =",
            "        // BUG: Diagnostic contains:",
            "        ImmutableListMultimap.copyOf(ImmutableListMultimap.of());",
            "    // BUG: Diagnostic contains:",
            "    ImmutableMap<Object, Object> o4 = ImmutableMap.copyOf(ImmutableMap.of());",
            "    // BUG: Diagnostic contains:",
            "    ImmutableMultimap<Object, Object> o5 = ImmutableMultimap.copyOf(ImmutableMultimap.of());",
            "    // BUG: Diagnostic contains:",
            "    ImmutableMultiset<Object> o6 = ImmutableMultiset.copyOf(ImmutableMultiset.of());",
            "    // BUG: Diagnostic contains:",
            "    ImmutableRangeMap<String, Object> o7 = ImmutableRangeMap.copyOf(ImmutableRangeMap.of());",
            "    // BUG: Diagnostic contains:",
            "    ImmutableRangeSet<String> o8 = ImmutableRangeSet.copyOf(ImmutableRangeSet.of());",
            "    // BUG: Diagnostic contains:",
            "    ImmutableSet<Object> o9 = ImmutableSet.copyOf(ImmutableSet.of());",
            "    ImmutableSetMultimap<Object, Object> o10 =",
            "        // BUG: Diagnostic contains:",
            "        ImmutableSetMultimap.copyOf(ImmutableSetMultimap.of());",
            "    // BUG: Diagnostic contains:",
            "    ImmutableTable<Object, Object, Object> o11 = ImmutableTable.copyOf(ImmutableTable.of());",
            "  }",
            "}")
        .doTest();
  }

  @Test
  void replacement() {
    BugCheckerRefactoringTestHelper.newInstance(Assignment5DeleteIdentityConversion.class, getClass())
        .addInputLines(
            "A.java",
            "import static com.google.errorprone.matchers.Matchers.staticMethod;",
            "import static org.mockito.Mockito.when;",
            "",
            "import com.google.common.collect.ImmutableCollection;",
            "import com.google.common.collect.ImmutableList;",
            "import com.google.common.collect.ImmutableSet;",
            "import java.util.ArrayList;",
            "import java.util.Collection;",
            "",
            "public final class A {",
            "  public void m() {",
            "    ImmutableSet<Object> set1 = ImmutableSet.copyOf(ImmutableSet.of());",
            "    ImmutableSet<Object> set2 = ImmutableSet.copyOf(ImmutableList.of());",
            "",
            "    ImmutableCollection<Integer> list1 = ImmutableList.copyOf(ImmutableList.of(1));",
            "    ImmutableCollection<Integer> list2 = ImmutableList.copyOf(new ArrayList<>(ImmutableList.of(1)));",
            "",
            "    Collection<Integer> c1 = ImmutableSet.copyOf(ImmutableSet.of(1));",
            "    Collection<Integer> c2 = ImmutableList.copyOf(new ArrayList<>(ImmutableList.of(1)));",
            "",
            "    Object o1 = ImmutableSet.copyOf(ImmutableList.of());",
            "    Object o2 = ImmutableSet.copyOf(ImmutableSet.of());",
            "",
            "    when(\"foo\".contains(\"f\")).thenAnswer(inv -> ImmutableSet.copyOf(ImmutableList.of(1)));",
            "  }",
            "}")
        .addOutputLines(
            "A.java",
            "import static com.google.errorprone.matchers.Matchers.staticMethod;",
            "import static org.mockito.Mockito.when;",
            "",
            "import com.google.common.collect.ImmutableCollection;",
            "import com.google.common.collect.ImmutableList;",
            "import com.google.common.collect.ImmutableSet;",
            "import java.util.ArrayList;",
            "import java.util.Collection;",
            "",
            "public final class A {",
            "  public void m() {",
            "    ImmutableSet<Object> set1 = ImmutableSet.of();",
            "    ImmutableSet<Object> set2 = ImmutableSet.copyOf(ImmutableList.of());",
            "",
            "    ImmutableCollection<Integer> list1 = ImmutableList.of(1);",
            "    ImmutableCollection<Integer> list2 = ImmutableList.copyOf(new ArrayList<>(ImmutableList.of(1)));",
            "",
            "    Collection<Integer> c1 = ImmutableSet.of(1);",
            "    Collection<Integer> c2 = ImmutableList.copyOf(new ArrayList<>(ImmutableList.of(1)));",
            "",
            "    Object o1 = ImmutableSet.copyOf(ImmutableList.of());",
            "    Object o2 = ImmutableSet.of();",
            "",
            "    when(\"foo\".contains(\"f\")).thenAnswer(inv -> ImmutableSet.copyOf(ImmutableList.of(1)));",
            "  }",
            "}")
        .doTest(TestMode.TEXT_MATCH);
  }
}
