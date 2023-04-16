package tech.picnic.errorprone.openai;

import static com.google.common.base.Verify.verify;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.function.Predicate.not;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.file.FileSystems;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.PathMatcher;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.jspecify.annotations.Nullable;

// XXX: Consider using https://picocli.info/quick-guide.html. Can also be used for an interactive
// CLI.
public final class AiPatcher {
  private static final Pattern LOG_LINE_START_MARKER = Pattern.compile("^\\[([A-Z]+)\\] ");
  private static final ImmutableSet<String> ISSUE_LOG_LEVELS = ImmutableSet.of("ERROR", "WARNING");
  private static final Pattern FILE_LOCATION_MARKER =
      Pattern.compile("^(.*?\\.java):\\[(\\d+)(?:,(\\d+))?\\] ");
  // XXX: Rename
  private static final String OPENAI_TOKEN_VARIABLE = "openapi_token";
  @Nullable private static final String OPENAI_TOKEN = System.getenv(OPENAI_TOKEN_VARIABLE);

  // Allow a custom source lookup directory to be specified.
  // Group by file.
  public static void main(String... args) {
    if (OPENAI_TOKEN == null) {
      System.err.printf(
          "OpenAI API token not found in environment variable '%s'.%n", OPENAI_TOKEN_VARIABLE);
      System.exit(1);
    }

    try {
      suggestFixes(getIssuesByFile(getWarningAndErrorMessages(System.in)));
    } catch (IOException e) {
      // XXX: Fix
      throw new RuntimeException(e);
    }

    // Explicitly exit to prevent `mvn exec:java` from handing due to long-lived OkHTTP threads.
    System.exit(0);
  }

  private static void suggestFixes(ImmutableMap<Path, String> issuesByFile) throws IOException {
    try (OpenAi openAi = OpenAi.create(OPENAI_TOKEN)) {
      for (Map.Entry<Path, String> e : issuesByFile.entrySet()) {
        suggestFixes(e.getKey(), e.getValue(), openAi);
      }
    }
  }

  private static void suggestFixes(Path file, String issueDescriptions, OpenAi openAi)
      throws IOException {
    //  XXX: Cleanup

    String originalCode = Files.readString(file);

    if (file.toString().contains("RefasterRuleCollection")) {
      return;
    }

    System.out.println("Instruction: " + issueDescriptions);

    if (true) {
      //      return;
    }

    // XXX: Handle case with too much input/output (tokens).
    // XXX: Handle error messages.

    String result =
        openAi.requestEdit(
            originalCode, "Resolve the following Java compilation errors:\n\n" + issueDescriptions);

    // XXX: !!! Don't create diff in patch mode; just apply the patch.
    System.out.printf("Fix for %s:%n", Diffs.unifiedDiff(originalCode, result, file.toString()));
  }

  private static ImmutableMap<Path, String> getIssuesByFile(List<String> logMessages) {
    Map<Path, String> messages = new HashMap<>();

    for (String message : logMessages) {
      extractPathAndMessage(message, (path, m) -> messages.merge(path, m, String::concat));
    }

    return ImmutableMap.copyOf(messages);
  }

  // XXX: Clean this up.
  private static void extractPathAndMessage(String logLine, BiConsumer<Path, String> sink) {
    // XXX: Move this to the caller and drop the prefix.
    Matcher logLineStartMarker = LOG_LINE_START_MARKER.matcher(logLine);
    verify(logLineStartMarker.find(), "XXX: message");

    String message = logLine.substring(logLineStartMarker.end());
    Optional.of(FILE_LOCATION_MARKER.matcher(message))
        .filter(Matcher::find)
        .ifPresent(
            m ->
                findPath(m.group(1))
                    .ifPresent(
                        path ->
                            sink.accept(
                                path,
                                m.group(3) == null
                                    ? String.format(
                                        "- Line %s: %s", m.group(2), message.substring(m.end()))
                                    : String.format(
                                        "- Line %s, column %s: %s",
                                        m.group(2), m.group(3), message.substring(m.end())))));
  }

  // XXX: name
  private static Optional<Path> findPath(String pathDescription) {
    Path path = Path.of(pathDescription).toAbsolutePath();
    if (Files.exists(path)) {
      return Optional.of(path);
    }

    PathMatcher matcher = FileSystems.getDefault().getPathMatcher("glob:**" + pathDescription);

    List<Path> inexactMatches = new ArrayList<>();
    try {
      Files.walkFileTree(
          Path.of("."),
          new SimpleFileVisitor<>() {
            @Override
            public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
              if (matcher.matches(file)) {
                inexactMatches.add(file.toAbsolutePath());
              }
              return FileVisitResult.CONTINUE;
            }
          });
    } catch (IOException e) {
      // XXX: Log.
      return Optional.empty();
    }

    // XXX: Log if not exactly one match.
    return Optional.of(inexactMatches).filter(not(List::isEmpty)).map(matches -> matches.get(0));
  }

  //  [ERROR]
  // /home/sschroevers/workspace/picnic/error-prone-support/refaster-runner/src/main/java/tech/picnic/errorprone/refaster/runner/CodeTransformers.java:[36,12] cannot find symbol
  //  symbol:   variable ALL_CODE_TRANSFORMERx
  //  location: class tech.picnic.errorprone.refaster.runner.CodeTransformers

  private static List<String> getWarningAndErrorMessages(InputStream inputStream)
      throws IOException {
    List<String> messages = new ArrayList<>();

    boolean shouldRead = false;
    StringBuilder nextMessage = new StringBuilder();
    try (BufferedReader br = new BufferedReader(new InputStreamReader(inputStream, UTF_8))) {
      for (String line = br.readLine(); line != null; line = br.readLine()) {
        Optional<String> logLevel = getLogLevel(line);

        if (logLevel.isPresent()) {
          if (!nextMessage.isEmpty()) {
            messages.add(nextMessage.toString());
            nextMessage.setLength(0);
          }

          shouldRead = ISSUE_LOG_LEVELS.contains(logLevel.orElseThrow());
        }

        if (shouldRead) {
          nextMessage.append(line).append(System.lineSeparator());
        }
      }
    }

    if (shouldRead && !nextMessage.isEmpty()) {
      messages.add(nextMessage.toString());
    }

    return messages;
  }

  private static Optional<String> getLogLevel(String line) {
    return Optional.of(LOG_LINE_START_MARKER.matcher(line))
        .filter(Matcher::find)
        .map(m -> m.group(1));
  }
}
