package course.agents;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * AI06 L7: state outside the conversation. A manifest on disk survives a crash, and file
 * hashes show which earlier findings have gone stale. "Work" here is simulated.
 */
public final class HarnessState {

    private static final ObjectMapper JSON = new ObjectMapper();
    /** An engineering choice for this example, not a documented threshold. */
    private static final double FRESH_START_ABOVE = 0.30;

    private HarnessState() {
    }

    static void save(Path manifest, Map<String, Boolean> done, Map<String, Integer> hashes)
            throws Exception {
        ObjectNode root = JSON.createObjectNode();
        ArrayNode tasks = root.putArray("tasks");
        done.forEach((file, passed) -> tasks.addObject().put("file", file)
                .put("analyzed", passed).put("hash", hashes.getOrDefault(file, 0)));
        Path temp = manifest.resolveSibling("manifest.tmp");
        Files.writeString(temp,
                JSON.writerWithDefaultPrettyPrinter().writeValueAsString(root));
        Files.move(temp, manifest, java.nio.file.StandardCopyOption.REPLACE_EXISTING);
    }

    static int analyze(Map<String, String> files, Path manifest,
            int crashAfter) throws Exception {
        Map<String, Boolean> done = new LinkedHashMap<>();
        Map<String, Integer> hashes = new LinkedHashMap<>();
        if (Files.exists(manifest)) {
            for (JsonNode task : JSON.readTree(manifest.toFile()).get("tasks")) {
                done.put(task.get("file").asText(), task.get("analyzed").asBoolean());
                hashes.put(task.get("file").asText(), task.get("hash").asInt());
            }
        }
        files.keySet().forEach(file -> done.putIfAbsent(file, false));
        int worked = 0;
        for (Map.Entry<String, Boolean> task : done.entrySet()) {
            if (task.getValue()) {
                continue;
            }
            if (worked == crashAfter) {
                throw new IllegalStateException("simulated crash");
            }
            done.put(task.getKey(), true);
            hashes.put(task.getKey(), files.get(task.getKey()).hashCode());
            save(manifest, done, hashes); // after each task, and atomically
            worked++;
        }
        return worked;
    }

    static List<String> stale(Map<String, String> files, Path manifest) throws Exception {
        java.util.ArrayList<String> changed = new java.util.ArrayList<>();
        for (JsonNode task : JSON.readTree(manifest.toFile()).get("tasks")) {
            String file = task.get("file").asText();
            if (files.get(file).hashCode() != task.get("hash").asInt()) {
                changed.add(file);
            }
        }
        return changed;
    }

    static String decide(int staleCount, int total) {
        if (staleCount == 0) {
            return "resume";
        }
        return (double) staleCount / total > FRESH_START_ABOVE
                ? "start fresh, with a structured summary"
                : "resume, and name the changed files";
    }

    public static void main(String[] args) throws Exception {
        Path dir = Files.createTempDirectory("harness");
        Path manifest = dir.resolve("manifest.json");
        Map<String, String> files = new LinkedHashMap<>();
        for (int i = 1; i <= 5; i++) {
            files.put("Service" + i + ".java", "class Service" + i + " {}");
        }
        try {
            analyze(files, manifest, 2);
        } catch (IllegalStateException crash) {
            System.out.println("Run 1: " + crash.getMessage() + " after 2 of 5 files");
        }
        System.out.println("Run 2: analyzed " + analyze(files, manifest, -1)
                + " more, skipped the finished ones");

        files.put("Service2.java", "class Service2 { /* edited */ }");
        List<String> changed = stale(files, manifest);
        System.out.println("One edit:   stale " + changed + " -> "
                + decide(changed.size(), files.size()));

        files.put("Service3.java", "class Service3 { /* edited */ }");
        files.put("Service4.java", "class Service4 { /* edited */ }");
        changed = stale(files, manifest);
        System.out.println("Three edits: stale " + changed + " -> "
                + decide(changed.size(), files.size()));
        Files.deleteIfExists(manifest);
        Files.deleteIfExists(dir);
    }
}
