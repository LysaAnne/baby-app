import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;
import org.gradle.api.DefaultTask;
import org.gradle.api.GradleException;
import org.gradle.api.file.ConfigurableFileCollection;
import org.gradle.api.tasks.InputFiles;
import org.gradle.api.tasks.PathSensitive;
import org.gradle.api.tasks.PathSensitivity;
import org.gradle.api.tasks.TaskAction;

public abstract class CheckCodeStyleTask extends DefaultTask {
    @InputFiles
    @PathSensitive(PathSensitivity.RELATIVE)
    public abstract ConfigurableFileCollection getSourceFiles();

    @TaskAction
    public void checkStyle() throws IOException {
        List<String> violations = new ArrayList<>();

        for (File file : getSourceFiles().getFiles()) {
            List<String> lines = Files.readAllLines(file.toPath());
            for (int index = 0; index < lines.size(); index++) {
                String line = lines.get(index);
                if (line.indexOf('\t') >= 0) {
                    violations.add(file + ":" + (index + 1) + ": tab character");
                }
                if (!line.equals(line.stripTrailing())) {
                    violations.add(file + ":" + (index + 1) + ": trailing whitespace");
                }
            }
        }

        if (!violations.isEmpty()) {
            throw new GradleException(
                "Code style violations:\n" + String.join("\n", violations)
            );
        }
    }
}
