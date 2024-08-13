import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public class Main {

    public static void main(String[] args) throws Exception {

        String currentDirectory = System.getProperty("user.dir");
        Path filePath = Paths.get(currentDirectory, "config.txt");

        File workingDirectory = new File(String.valueOf(Paths.get(currentDirectory, "out")));

        List<String> configPaths = Files.readAllLines(filePath);

        for (String config : configPaths) {
            processConfig(workingDirectory, config);
        }

    }

    private static void processConfig(File directory, String config) throws IOException {
        String[] configPaths = config.split("/");
        File currentHTMLFile, currentTXTFile, targetDirectory;

        if (configPaths.length == 1) {
            String name = configPaths[0];
            targetDirectory = new File(directory, name);
            if (!targetDirectory.exists()) {
                targetDirectory.mkdirs();
            }

            currentHTMLFile = new File(directory, name + ".html");
            if (!currentHTMLFile.exists()) {
                throw new IllegalStateException("File '" + currentHTMLFile.getAbsolutePath() + "' does not exist");
            }

            currentTXTFile = new File(directory, name + ".txt");

        } else {
            StringBuilder first = new StringBuilder();
            for(int i = 0; i < configPaths.length - 1; ++i) {
                first.append(configPaths[i]);
            }

            String name = configPaths[configPaths.length - 1];

            Path targetPath = Paths.get(directory.getAbsolutePath(), first.toString(), name);
            targetDirectory = new File(String.valueOf(targetPath));
            if (!targetDirectory.exists()) {
                targetDirectory.mkdirs();
            }

            Path currentPath = Paths.get(directory.getAbsolutePath(), first.toString());
            currentHTMLFile = new File(String.valueOf(currentPath), name + ".html");
            if (!currentHTMLFile.exists()) {
                throw new IllegalStateException("File '" + currentHTMLFile.getAbsolutePath() + "' does not exist");
            }

            currentTXTFile = new File(String.valueOf(currentPath), name + ".txt");
        }

        if (!currentTXTFile.exists()) {
            throw new IllegalStateException("File '" + currentTXTFile.getAbsolutePath() + "' does not exist");
        }

        Files.deleteIfExists(Paths.get(targetDirectory.getAbsolutePath(), "index.html"));
        Files.deleteIfExists(Paths.get(targetDirectory.getAbsolutePath(), "index.txt"));

        Files.copy(currentHTMLFile.toPath(), Paths.get(targetDirectory.getAbsolutePath(),  "index.html"));
        Files.copy(currentTXTFile.toPath(), Paths.get(targetDirectory.getAbsolutePath(), "index.txt"));

    }

}