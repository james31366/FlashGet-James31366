import javafx.concurrent.Task;

import java.io.*;
import java.net.URL;
import java.net.URLConnection;

/**
 * To assign task to download file using multi-thread.
 *
 * @author Vichisorn Wejsupakul
 */
@SuppressWarnings({"FieldMayBeFinal", "CanBeFinal"})
public class DownloadTask extends Task<Long> {
    private URL url;
    private File outFile;
    private long start;
    private long size;

    /**
     * Use to initial a DownloadTask.
     *
     * @param url     url that connect website that need to download a file.
     * @param outFile path that need to write file at.
     * @param start   to seek where need to start location to download.
     * @param size    size of file from url.
     */
    public DownloadTask(URL url, File outFile, long start, long size) {
        this.url = url;
        this.outFile = outFile;
        this.start = start;
        this.size = size;
    }

    /**
     * Start a task for download files and write into computer.
     * And update for text and progress bar.
     *
     * @return result of byte after download.
     * @throws IOException throw when doesn't find input and output file or link.
     */
    @Override
    protected Long call() throws IOException {
        long downloaded = 0L;
        final int BUFFERSIZE = 16 * 1024;
        URLConnection conn = url.openConnection();
        String range;
        range = String.format("bytes=%d-%d", start, start + size - 1);
        conn.setRequestProperty("Range", range);
        InputStream in = conn.getInputStream();
        RandomAccessFile out = new RandomAccessFile(outFile, "rwd");
        out.seek(start);
        byte[] buffer = new byte[BUFFERSIZE];
        do {
            int read = in.read(buffer);
            if (read < 0) break; // read < 0 means end of the input
            out.write(buffer, 0, read); // write read bytes from buffer
            downloaded += read;
            updateValue(downloaded);
            updateProgress(downloaded, size);
            if (isCancelled()) {
                in.close();
                out.close();
                updateValue(0L);
                updateProgress(0L, 0L);
                break;
            }
        } while (downloaded < size);
        in.close();
        out.close();
        return downloaded;
    }
}
