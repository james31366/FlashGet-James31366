package FlashGet;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.TextField;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

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
        DownloadTask downloadTask1 = new DownloadTask(url, outFile, 0, size);
    }

    public void clearHandle(ActionEvent event) {

    }

    public void cancelHandle(ActionEvent event) {

    }
}
