package dev.rylex.jeirecipepins;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BiConsumer;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;

class CompatIsolationTest {

    private static final Path SOURCE_ROOT = ProjectFiles.root().resolve(Paths.get("src", "main", "java"));
    private static final Path COMPAT_DIR = Paths.get("dev", "rylex", "jeirecipepins", "compat");

    private static final List<ForeignRule> FOREIGN_RULES = List.of(new ForeignRule(
            Paths.get("dev", "rylex", "jeirecipepins", "compat", "ftblibrary"),
            List.of("dev.ftb.mods", "dev.architectury")));

    private static final List<String> COMPAT_ENTRY_POINTS =
            List.of("dev.rylex.jeirecipepins.compat.ftblibrary.FtbLibraryCompat");

    @Test
    void foreignClassesStayInsideTheirCompatPackage() {
        List<String> violations = new ArrayList<>();
        forEachSourceFile((relative, source) -> {
            for (ForeignRule rule : FOREIGN_RULES) {
                if (relative.startsWith(rule.allowedDir())) {
                    continue;
                }
                for (String prefix : rule.forbiddenPrefixes()) {
                    if (source.contains(prefix)) {
                        violations.add(relative + " references '" + prefix + "'");
                    }
                }
            }
        });
        assertTrue(
                violations.isEmpty(),
                "Foreign mod classes leaked outside their compat package (breaks lazy loading; crashes when the "
                        + "integration is absent):\n  " + String.join("\n  ", violations));
    }

    @Test
    void coreReachesCompatOnlyThroughGuardedEntryPoints() {
        List<String> violations = new ArrayList<>();
        forEachSourceFile((relative, source) -> {
            if (relative.startsWith(COMPAT_DIR)) {
                return;
            }
            source.lines()
                    .map(String::trim)
                    .filter(line -> line.startsWith("import dev.rylex.jeirecipepins.compat."))
                    .map(line -> line.substring("import ".length(), line.indexOf(';'))
                            .trim())
                    .filter(fqn -> !COMPAT_ENTRY_POINTS.contains(fqn))
                    .forEach(fqn -> violations.add(relative + " imports '" + fqn + "'"));
        });
        assertTrue(
                violations.isEmpty(),
                "Core code names a compat class that is not a guarded entry point (pulls the compat package in "
                        + "eagerly). Add it to COMPAT_ENTRY_POINTS only if it is guarded by ModList.isLoaded:\n  "
                        + String.join("\n  ", violations));
    }

    private static void forEachSourceFile(BiConsumer<Path, String> visitor) {
        try (Stream<Path> files = Files.walk(SOURCE_ROOT)) {
            files.filter(p -> p.toString().endsWith(".java")).forEach(file -> {
                try {
                    visitor.accept(SOURCE_ROOT.relativize(file), Files.readString(file));
                } catch (IOException e) {
                    throw new UncheckedIOException(e);
                }
            });
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private record ForeignRule(Path allowedDir, List<String> forbiddenPrefixes) {}
}
