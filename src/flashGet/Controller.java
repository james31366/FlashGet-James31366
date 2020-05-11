package flashGet;

import javafx.beans.value.ChangeListener;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.scene.control.*;
import javafx.stage.FileChooser;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.RejectedExecutionException;

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
    private DownloadTask[] downloadTasks;

    public void downloadHandle(ActionEvent event) {
        File outFile;
        URL url;
        long size;
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Save File");
        String home = System.getProperty("user.home");
        fileChooser.setInitialDirectory(new File(home + "/Downloads/"));
        fileChooser.getExtensionFilters().addAll(
                new FileChooser.ExtensionFilter("Text Files", "*.txt"),
                new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.gif"),
                new FileChooser.ExtensionFilter("Audio Files", "*.wav", "*.mp3", "*.aac"),
                new FileChooser.ExtensionFilter("DOS executable", "*.exe"),
                new FileChooser.ExtensionFilter("All Files", "*.*"));
        outFile = fileChooser.showSaveDialog(new Stage());
        try {
            url = new URL(URLField.getText());
            URLConnection urlConnection = url.openConnection();
            size = urlConnection.getContentLengthLong();
            fileSize.setText(String.format("/%d", size));
            long chunkSize = size / 4;
            ExecutorService executorService;
            int nThread;
            if (size > 0 && size <= 1000) nThread = 1;
            else nThread = 4;
            ProgressBar[] progressThreads = {ProgressThread1, ProgressThread2, ProgressThread3, ProgressThread4};
            downloadTasks = new DownloadTask[nThread];
            executorService = Executors.newFixedThreadPool(nThread + 1);
            ChangeListener<String> labelChangeListener = (observableValue, oldValue, newValue) -> downloadProgressLabel.setText(newValue);
            if (nThread>1) {
                for (int i = 0; i < nThread; i++) {
                    long start = chunkSize * i;
                    if (i != nThread - 1) downloadTasks[i] = new DownloadTask(url, outFile, start, chunkSize);
                    else downloadTasks[i] = new DownloadTask(url, outFile, start, size - (chunkSize * i));
                    downloadTasks[i].messageProperty().addListener(labelChangeListener);
                    progressThreads[i].progressProperty().bind(downloadTasks[i].progressProperty());
                }
                SingleThreadBar.progressProperty().bind(downloadTasks[0].progressProperty().multiply(0.25)
                        .add(downloadTasks[1].progressProperty()).multiply(0.25)
                        .add(downloadTasks[2].progressProperty().multiply(0.25))
                        .add(downloadTasks[3].progressProperty()).multiply(0.25));
            } else {
                downloadTasks[0] = new DownloadTask(url, outFile);
                downloadTasks[0].messageProperty().addListener((labelChangeListener));
                SingleThreadBar.progressProperty().bind(downloadTasks[0].progressProperty());
            }
            for (int i = 0; i < nThread; i++) {
                executorService.execute(downloadTasks[i]);
            }
            executorService.shutdown();
        } catch (IOException ioException) {
            Alert errorAlert = getAlert("Link is invalid", Alert.AlertType.ERROR, "Invalid Link Error", "Look, an link error dialog");
            errorAlert.showAndWait();
            error("Link is invalid", ioException);
        } catch (RejectedExecutionException executionException) {
            Alert errorAlert = getAlert("Old task does not finished yet", Alert.AlertType.ERROR, "Execution Error", "Look, an execution error dialog");
            errorAlert.showAndWait();
            error("Old task does not finished yet", executionException);
        }
    }

    public void clearHandle(ActionEvent event) {
        URLField.clear();
    }

    public void cancelHandle(ActionEvent event) {
        try {
            for (DownloadTask task : downloadTasks) {
                task.cancel();
            }
        } catch (NullPointerException nullPointerException) {
            Alert alertError = getAlert("Please download file before cancel", Alert.AlertType.ERROR, "Cancel Error", "Look, a CancelError dialog");
            alertError.showAndWait();
            error("Please download file before cancel", nullPointerException);
        }
    }

    /**
     * Print an error message and exit with exit code 1.
     *
     * @param message is error text to print.
     * @param e is exception from programs.
     */
    private static void error(String message, Exception e) {
        e.printStackTrace();
        System.out.println(message);
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
