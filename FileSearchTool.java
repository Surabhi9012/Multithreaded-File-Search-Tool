import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * FileSearchTool is a simple Swing-based file search tool that allows users to search for files
 * within a specified directory and its subdirectories concurrently using multiple threads.
 */
public class FileSearchTool extends JFrame {

    private JTextField directoryField, filePatternField;
    private JTextArea resultArea;
    private JButton searchButton, cancelButton;

    private ExecutorService executorService;

    /**
     * Constructs the FileSearchTool GUI.
     */
    public FileSearchTool() {
        super("File Search Tool");
        initUI();
        initListeners();
        // Create a fixed-size thread pool for concurrent file searches
        executorService = Executors.newFixedThreadPool(5);
    }

    /**
     * Initializes the user interface components.
     */
    private void initUI() {
        directoryField = new JTextField(30);
        filePatternField = new JTextField(15);
        resultArea = new JTextArea(15, 50);
        resultArea.setEditable(false);
        searchButton = new JButton("Search");
        cancelButton = new JButton("Cancel");

        JPanel panel = new JPanel(new FlowLayout());
        panel.add(new JLabel("Directory:"));
        panel.add(directoryField);
        panel.add(new JLabel("File Pattern:"));
        panel.add(filePatternField);
        panel.add(searchButton);
        panel.add(cancelButton);

        setLayout(new BorderLayout());
        add(panel, BorderLayout.NORTH);
        add(new JScrollPane(resultArea), BorderLayout.CENTER);

        setSize(600, 400);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLocationRelativeTo(null);
        setVisible(true);
    }

    /**
     * Initializes action listeners for the Search and Cancel buttons.
     */
    private void initListeners() {
        searchButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                String directoryPath = directoryField.getText();
                String filePattern = filePatternField.getText();

                resultArea.setText("");
                if (!directoryPath.isEmpty() && !filePattern.isEmpty()) {
                    // Start the file search with the specified parameters
                    searchFiles(directoryPath, filePattern);
                } else {
                    resultArea.append("Please enter both directory and file pattern.");
                }
            }
        });

        cancelButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (executorService != null) {
                    // Cancel ongoing file searches by shutting down the thread pool
                    executorService.shutdownNow();
                    // Recreate the thread pool for potential future searches
                    executorService = Executors.newFixedThreadPool(5);
                }
            }
        });
    }

    /**
     * Initiates the file search process with the specified directory and file pattern.
     *
     * @param directoryPath The path of the directory to search.
     * @param filePattern   The file pattern or regular expression to match.
     */
    private void searchFiles(String directoryPath, String filePattern) {
        File directory = new File(directoryPath);
        if (!directory.exists() || !directory.isDirectory()) {
            resultArea.append("Invalid directory path.\n");
            return;
        }

        // Create a task for the file search
        FileSearchTask searchTask = new FileSearchTask(directory, filePattern, resultArea);
        // Submit the task to the thread pool for execution
        executorService.submit(searchTask);
    }

    /**
     * Entry point for the application. Creates an instance of FileSearchTool.
     *
     * @param args Command-line arguments (not used).
     */
    public static void main(String args[]) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                new FileSearchTool();
            }
        });
    }
}

/**
 * FileSearchTask represents a task for searching files in a specified directory and its subdirectories.
 * This task is designed to be executed by a thread in a thread pool.
 */
class FileSearchTask implements Runnable {

    private File directory;
    private String filePattern;
    private JTextArea resultArea;

    /**
     * Constructs a FileSearchTask with the specified parameters.
     *
     * @param directory   The directory to search.
     * @param filePattern The file pattern or regular expression to match.
     * @param resultArea  The JTextArea where search results will be displayed.
     */
    public FileSearchTask(File directory, String filePattern, JTextArea resultArea) {
        this.directory = directory;
        this.filePattern = filePattern;
        this.resultArea = resultArea;
    }

    /**
     * Executes the file search task.
     */
    @Override
    public void run() {
        searchFiles(directory, filePattern);
    }

    /**
     * Recursively searches for files in the specified directory and its subdirectories.
     * Matches files based on the provided file pattern.
     *
     * @param directory      The directory to search.
     * @param filePattern    The file pattern or regular expression to match.
     * @param matchingFiles  A list to store matching files.
     */
    private void searchFilesRecursively(File directory, String filePattern, List<File> matchingFiles) {
        File[] files = directory.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    searchFilesRecursively(file, filePattern, matchingFiles);
                } else if (file.getName().matches(filePattern)) {
                    matchingFiles.add(file);
                }
            }
        }
    }

    /**
     * Searches for files in the specified directory and displays the results in the JTextArea.
     *
     * @param directory   The directory to search.
     * @param filePattern The file pattern or regular expression to match.
     */
    private void searchFiles(File directory, String filePattern) {
        List<File> matchingFiles = new ArrayList<>();
        searchFilesRecursively(directory, filePattern, matchingFiles);

        // Update the GUI with the search results
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                displaySearchResults(matchingFiles);
            }
        });
    }

    /**
     * Displays the search results in the JTextArea.
     *
     * @param matchingFiles The list of matching files.
     */
    private void displaySearchResults(List<File> matchingFiles) {
        resultArea.append("Search Results:\n");

        if (matchingFiles.isEmpty()) {
            resultArea.append("No matching files found.\n");
        } else {
            for (File file : matchingFiles) {
                resultArea.append(file.getAbsolutePath() + "\n");
            }
        }
    }
}


