import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;


public class Main {

    private static final String BASE_URL = "https://www.mbfaygroup.com";


    public static void main(String[] args) throws Exception {

        String currentDirectory = System.getProperty("user.dir"); // Get the current directory
        Path filePath = Paths.get(currentDirectory, "config.txt"); // Get the config file that specifies the directories to be modified

        File workingDirectory = new File(String.valueOf(Paths.get(currentDirectory, "out"))); //Get the output directory which, in this case, is where te build files are located

        List<String> configPaths = Files.readAllLines(filePath); // Get all the directories to be modified

        for (String config : configPaths) { // for each of the directory
            processConfig(workingDirectory, config); // process and modify
        }

        updateSitemap(workingDirectory, configPaths); // Update the sitemap
    }

    private static void processConfig(File workingDirectory, String config) throws IOException {

        String[] configPaths = config.split("/"); // Split the config by the / symbol. Essentially if config is like a/b, we would get [a, b]
        File currentHTMLFile, currentTXTFile, targetDirectory; // Declare files for the html file, txt file and target directory to be modified

        if (configPaths.length == 1) { // if the config is a single directory (a instead of a/b)
            String name = configPaths[0]; // Get the name of the builder directory
            targetDirectory = new File(workingDirectory, name); // Create the file that points to that target directory specified by the config name
            if (!targetDirectory.exists()) { // If the directory does not exist
                targetDirectory.mkdirs(); //create it
            }

            currentHTMLFile = new File(workingDirectory, name + ".html"); // Get and assign the HTML file in the working directory that corresponds to the name specified by the config
            if (!currentHTMLFile.exists()) { // If the file does not exist
                throw new IllegalStateException("File '" + currentHTMLFile.getAbsolutePath() + "' does not exist"); // Throw an exception
            }

            currentTXTFile = new File(workingDirectory, name + ".txt");  // Get and assign the TXT file in the working directory that corresponds to the name specified by the config

        } else { // if the config is a multi directory (a/b instead of a)
            StringBuilder builder = new StringBuilder(); // Create a new string builder
            for (int i = 0; i < configPaths.length - 1; ++i) {
                builder.append(configPaths[i]); // Append a config path;
                if (i != configPaths.length - 1) { // if this is not the last config split value
                    builder.append("/");  // append a slash
                }
            }

            String name = configPaths[configPaths.length - 1]; // Get the last config split value which is the name of the target directory

            Path targetPath = Paths.get(workingDirectory.getAbsolutePath(), builder.toString(), name); // Create a path from the working directory to the target directory
            targetDirectory = new File(String.valueOf(targetPath)); // Get the actual directory
            if (!targetDirectory.exists()) { // If the directory does not exist
                targetDirectory.mkdirs(); // Create it
            }

            Path currentPath = Paths.get(workingDirectory.getAbsolutePath(), builder.toString()); // Get the path to the parent directory of the target directory
            currentHTMLFile = new File(String.valueOf(currentPath), name + ".html"); // Get and assign the HTML file present
            if (!currentHTMLFile.exists()) { // If the HTML file does not exist
                throw new IllegalStateException("File '" + currentHTMLFile.getAbsolutePath() + "' does not exist"); // Throw an exception
            }

            currentTXTFile = new File(String.valueOf(currentPath), name + ".txt");  // Get and assign the TXT file in the working directory that corresponds to the name specified by the config
        }

        if (!currentTXTFile.exists()) { // If the TXT file does not exist
            throw new IllegalStateException("File '" + currentTXTFile.getAbsolutePath() + "' does not exist"); // Throw an exception
        }

        Files.deleteIfExists(Paths.get(targetDirectory.getAbsolutePath(), "index.html")); // Delete the HTML file in the target directory if it exists
        Files.deleteIfExists(Paths.get(targetDirectory.getAbsolutePath(), "index.txt")); // Delete the TXT file in the target directory if it exists

        Files.copy(currentHTMLFile.toPath(), Paths.get(targetDirectory.getAbsolutePath(), "index.html")); // Copy the output HTML to the target directory and name it index.html
        Files.copy(currentTXTFile.toPath(), Paths.get(targetDirectory.getAbsolutePath(), "index.txt")); // Copy the output TXT to the target directory and name it index.txt

        Files.deleteIfExists(currentHTMLFile.toPath()); // Delete the previous HTML file
        Files.deleteIfExists(currentTXTFile.toPath()); // Delete the previous TXT file
    }


    private static void updateSitemap(File workingDirectory, List<String> configs) throws IOException {
        Instant now = Instant.now(); // Get the current instant
        ZonedDateTime zdt = now.atZone(ZoneId.of("UTC")); // Convert to UTC
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ssXXX"); // Create a formatter
        String formattedDate = zdt.format(formatter); // Get the string representation

        StringBuilder xmlBuilder = new StringBuilder(); // Create a new string builder
        xmlBuilder.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>").append("\n"); // Append the xml tag
        xmlBuilder.append("<urlset\n" +
                "          xmlns=\"http://www.sitemaps.org/schemas/sitemap/0.9\"\n" +
                "          xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n" +
                "          xsi:schemaLocation=\"http://www.sitemaps.org/schemas/sitemap/0.9\n" +
                "                http://www.sitemaps.org/schemas/sitemap/0.9/sitemap.xsd\">\n"); // Append the urlset
        xmlBuilder.append(" <url>\n" +
                "      <loc>" + BASE_URL + "</loc>\n" +
                "      <lastmod>" + formattedDate + "</lastmod>\n" +
                "      <priority>1.00</priority>\n" +
                "    </url>\n"); // Append the base url

        for (String config : configs) {
            // Append all the configs
            xmlBuilder.append(" <url>\n" +
                    "      <loc>" + BASE_URL + "/" + config + "</loc>\n" +
                    "      <lastmod>" + formattedDate + "</lastmod>\n" +
                    "      <priority>0.80</priority>\n" +
                    "    </url>\n");
        }

        xmlBuilder.append("</urlset>"); // close the tag

        Path sitemapPath = Paths.get(workingDirectory.getAbsolutePath(), "sitemap.xml");
        Files.deleteIfExists(sitemapPath); // Delete the sitemap file in the target directory if it exists
        Files.writeString(sitemapPath, xmlBuilder.toString()); // Write the updated sitemap to the file
    }

}