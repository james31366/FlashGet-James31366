package flashGet;

import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TextField;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLConnection;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

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
    private ExecutorService executorService;

    public void downloadHandle(ActionEvent event) {
        File outFile = new File("");
        URL url = null;
        long size = 0;
        try {
            url = new URL(URLField.toString());
            URLConnection urlConnection = url.openConnection();
            size = urlConnection.getContentLengthLong();
        } catch (IOException e) {
            e.printStackTrace();
        }
        long chunkSize = size / 4;
        if (size > 0 && size <= 1000) {
            ExecutorService executorService = Executors.newFixedThreadPool(1);
            Task<Long> downloadTask = new DownloadTask(url, outFile, 0, size);
            executorService.execute(downloadTask);
        } else {
            executorService = Executors.newFixedThreadPool(4);
            Task<Long> downloadTask1 = new DownloadTask(url, outFile, 0, chunkSize);
            Task<Long> downloadTask2 = new DownloadTask(url, outFile, chunkSize, chunkSize * 2);
            Task<Long> downloadTask3 = new DownloadTask(url, outFile, chunkSize * 2, chunkSize * 3);
            Task<Long> downloadTask4 = new DownloadTask(url, outFile, chunkSize * 3, size - (chunkSize * 3));
            executorService.execute(downloadTask1);
            executorService.execute(downloadTask2);
            executorService.execute(downloadTask3);
            executorService.execute(downloadTask4);
        }

    }

    public void clearHandle(ActionEvent event) {

    }

    public void cancelHandle(ActionEvent event) {
        executorService.shutdownNow();
    }
}
