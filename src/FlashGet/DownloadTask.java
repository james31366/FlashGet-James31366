package FlashGet;

import javafx.concurrent.Task;

import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.util.Objects;

public class DownloadTask extends Task {
    private URL url;
    private File outFile;
    private long start;
    private long size;

    public DownloadTask(URL url, File file, long start, long size) {
        this.url = url;
        this.outFile = file;
        this.start = start;
        this.size = size;
    }

    @Override
    protected Long call() throws IOException {
        String range = null;
        if (size > 500) {
            range = String.format("bytes=%d-%d", start, start+size-1);
        } else range = String.format("bytes=%d-", start);
        final int BUFFERSIZE = 16 * 1024;
        URLConnection conn = url.openConnection();
        byte[] buffer = new byte[BUFFERSIZE];
        try (InputStream in = conn.getInputStream(); OutputStream out = new FileOutputStream(outFile)) {
            do {
                int n = in.read(buffer);
                if (n < 0) break; // n < 0 means end of the input
                out.write(buffer, 0, n); // write n bytes from buffer
            } while (true);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return null;
    }
}
