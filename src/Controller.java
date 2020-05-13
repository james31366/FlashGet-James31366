import javafx.beans.value.ChangeListener;
import javafx.event.ActionEvent;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;

/**
 * Control UI of FlashGet and logic of applications.
 *
 * @author Vichisorn Wejsupakul
 */
public class Controller {

    public TextField URLField;
    public Button DownloadButton;
    public Button ClearButton;
    public Label FileNameLabel;
    public ProgressBar SingleThreadBar;
    public Button CancelButton;
    public ProgressBar ProgressThread1;
    public ProgressBar ProgressThread2;
    public ProgressBar ProgressThread3;
    public ProgressBar ProgressThread4;
    public Label downloadProgressLabel;
    public Label fileSize;
    public Label fileName;
    private File outFile;
    private ExecutorService executorService;
    private DownloadTask[] downloadTasks;
    private ChangeListener<Long> labelChangeListener;

    /**
     * Print an error message and StackTrace in Terminal.
     *
     * @param message is error text to print.
     * @param e       is exception from programs.
     */
    private static void error(String message, Exception e) {
        e.printStackTrace();
        System.out.println(message);
    }

    /**
     * For handle the Event that occur when user press "Download" button.
     * Download a file from url and using multi-thread for quicker download.
     *
     * @param event is a semantic event which indicates that a component-defined action occurred.
     */
    public void downloadHandle(ActionEvent event) {
        try {
            URL url = new URL(URLField.getText());
            URLConnection urlConnection = url.openConnection();
            long size = urlConnection.getContentLengthLong();
            fileSize.setText(String.valueOf(size));
            String fileNames = URLField.getText().substring(URLField.getText().lastIndexOf('/') + 1);
            FileChooser fileChooser = getFileChooser(fileNames);
            Optional<ButtonType> result;
            do {
                outFile = fileChooser.showSaveDialog(new Stage());
                Alert confirmAlert = getAlert(String.format("File Path '%s'", outFile.getAbsolutePath()),
                        Alert.AlertType.CONFIRMATION,
                        "Confirm Save to",
                        "Please, check your file path below");
                result = confirmAlert.showAndWait();
                if (result.isEmpty()) break;
            }
            while (result.get() == ButtonType.CANCEL);
            fileName.setText(outFile.getName());
            long chunkSize = size / 4;
            int nThread;
            if (size > 0 && size <= 1000) nThread = 1;
            else nThread = 4;
            ProgressBar[] progressThreads = {ProgressThread1, ProgressThread2, ProgressThread3, ProgressThread4};
            downloadTasks = new DownloadTask[nThread];
            executorService = Executors.newFixedThreadPool(nThread + 1);
            labelChangeListener = (observableValue, oldValue, newValue) -> {
                if (oldValue == null) oldValue = 0L;
                long downloaded = 0L;
                downloaded += (newValue - oldValue);
                downloadProgressLabel.setText(String.valueOf(downloaded));
            };
            manageThread(url, size, chunkSize, nThread, progressThreads);

            //Run Task
            for (int i = 0; i < nThread; i++) executorService.execute(downloadTasks[i]);
            executorService.shutdown();
            Alert successAlert = getAlert(String.format("File name: %s is download complete", outFile.getName()), Alert.AlertType.INFORMATION, "Success download", "Your download is complete!");
            if (!executorService.isShutdown()) successAlert.showAndWait();

            //After finish download a file.
            URLField.clear();

        } catch (IOException ioException) {
            Alert errorAlert = getAlert("Link is invalid", Alert.AlertType.ERROR, "Invalid Link Error", "Look, an link error dialog");
            errorAlert.showAndWait();
            error("Link is invalid", ioException);
        } catch (RejectedExecutionException executionException) {
            Alert errorAlert = getAlert("Old task does not finished yet", Alert.AlertType.ERROR, "Execution Error", "Look, an execution error dialog");
            errorAlert.showAndWait();
            error("Old task does not finished yet", executionException);
        } catch (NullPointerException nullPointerException) {
            Alert errorAlert = getAlert("Please choose destination file path", Alert.AlertType.ERROR, "Save As Error", "Look, an error dialog");
            errorAlert.showAndWait();
            error("Please choose destination file path", nullPointerException);
        }
    }

    /**
     * Use to assign task and manage thread bind progress bar and download label
     * to update and see progress of your download file.
     *
     * @param url             url that connect website that need to download a file.
     * @param size            size of file from url.
     * @param chunkSize       size that need a thread to download for using multi-thread.
     * @param nThread         number of thread that need to use.
     * @param progressThreads An array of progress bar.
     */
    private void manageThread(URL url, long size, long chunkSize, int nThread, ProgressBar[] progressThreads) {
        if (nThread > 1) {
            for (int i = 0; i < nThread; i++) {
                long start = chunkSize * i;
                if (i != nThread - 1) downloadTasks[i] = new DownloadTask(url, outFile, start, chunkSize);
                else downloadTasks[i] = new DownloadTask(url, outFile, start, size - (chunkSize * i));
                downloadTasks[i].valueProperty().addListener(labelChangeListener);
                progressThreads[i].progressProperty().bind(downloadTasks[i].progressProperty());
            }
            SingleThreadBar.progressProperty().bind(downloadTasks[0].progressProperty().multiply(0.25)
                    .add(downloadTasks[1].progressProperty().multiply(0.25)
                            .add(downloadTasks[2].progressProperty().multiply(0.25)
                                    .add(downloadTasks[3].progressProperty().multiply(0.25)))));
        } else {
            downloadTasks[0] = new DownloadTask(url, outFile, 0, size);
            downloadTasks[0].valueProperty().addListener((labelChangeListener));
            SingleThreadBar.progressProperty().bind(downloadTasks[0].progressProperty());
        }
    }

    /**
     * For handle the Event that occur when user press "Clear" button.
     * Clear URLField text.
     *
     * @param event is a semantic event which indicates that a component-defined action occurred.
     */
    public void clearHandle(ActionEvent event) {
        URLField.clear();
    }

    /**
     * For handle the Event that occur when user press "Cancel" button.
     * Cancel all task in thread and delete on loading file.
     *
     * @param event is a semantic event which indicates that a component-defined action occurred.
     */
    public void cancelHandle(ActionEvent event) {
        try {
            if (!executorService.isTerminated()) {
                for (DownloadTask task : downloadTasks) {
                    task.cancel();
                }
                if (outFile.delete()) {
                    Alert alertInfo = getAlert(String.format("File name %s already deleted!", outFile.getName()), Alert.AlertType.INFORMATION, "Delete File", "Look, an delete file dialog");
                    alertInfo.showAndWait();
                }
            }
        } catch (NullPointerException nullPointerException) {
            Alert alertError = getAlert("Please download file before cancel", Alert.AlertType.ERROR, "Cancel Error", "Look, a CancelError dialog");
            alertError.showAndWait();
            error("Please download file before cancel", nullPointerException);
        }
    }

    /**
     * Set up a fileChooser for save a file into computer.
     *
     * @param fileNames names of a file that you save.
     * @return a fully functional save as windows.
     */
    private FileChooser getFileChooser(String fileNames) {
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save As");
        String home = System.getProperty("user.home");
        fileChooser.setInitialDirectory(new File(home + "/Downloads/"));
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Text Files", "*.txt"),
                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.gif"),
                new FileChooser.ExtensionFilter("Audio Files", "*.wav", "*.mp3", "*.aac"),
                new FileChooser.ExtensionFilter("DOS executable", "*.exe"),
                new FileChooser.ExtensionFilter("Archive Files", "*.rar", "*.zip"),
                new FileChooser.ExtensionFilter("All Files", "*.*"));
        fileChooser.setInitialFileName(fileNames);
        return fileChooser;
    }

    /**
     * An popup box for any type of alert.
     * Type of alert that can use with this method are Warning and Error.
     *
     * @param message   for display the dialog that you need to remind user.
     * @param alertType is type of alert that need to remind user.
     * @param title     to display the alert title
     * @param header    to set the alert header dialog.
     * @return an Alert for remind user.
     */
    private Alert getAlert(String message, Alert.AlertType alertType, String title, String header) {
        Alert warnAlert = new Alert(alertType);
        warnAlert.setTitle(title);
        warnAlert.setHeaderText(header);
        warnAlert.setContentText(message);
        return warnAlert;
    }
}
