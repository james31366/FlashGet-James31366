package flashGet;

import javafx.concurrent.Task;

import java.io.*;
import java.net.URL;
import java.net.URLConnection;

@SuppressWarnings({"FieldMayBeFinal", "CanBeFinal"})
public class DownloadTask extends Task<Long> {
    private URL url;
    private File outFile;
    private long start;
    private long size;

    public DownloadTask(URL url, File file) {
        this.url = url;
        this.outFile = file;
        this.start = 0;
        this.size = -1;
    }

    public DownloadTask(URL url, File file, long start, long size) {
        this.url = url;
        this.outFile = file;
        this.start = start;
        this.size = size;
    }

    @Override
    protected Long call() throws IOException {
        Long result = null;
        final int BUFFERSIZE = 16 * 1024;
        URLConnection conn = url.openConnection();
        String range;
        if (size > 0) {
            range = String.format("bytes=%d-%d", start, start + size - 1);
        } else range = String.format("bytes=%d-", start);
        conn.setRequestProperty("Range", range);
        InputStream in = conn.getInputStream();
        RandomAccessFile out = new RandomAccessFile(outFile, "rwd");
        out.seek(start);
        byte[] buffer = new byte[BUFFERSIZE];
        do {
            int n = in.read(buffer);
            if (n < 0) break; // n < 0 means end of the input
            updateValue(result += n);
            updateProgress(result, size);
            out.write(buffer, 0, n); // write n bytes from buffer
        } while (result < size);
        out.close();
        return result;
    }
}
