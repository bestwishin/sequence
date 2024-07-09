package com.example.storage;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.nio.file.StandardOpenOption;
import java.nio.file.attribute.FileAttribute;

public class SimpleStorage implements Storage {
    private String filePath = "D:\\max_sequence.bin";
    private ByteBuffer buffer = ByteBuffer.allocate(Long.BYTES);

    @Override
    public boolean save(long counter) {
        try {
            Path tempFile = writeTempSequenceFile(counter);
            Path permanentFile = Paths.get(filePath);
            Files.move(tempFile, permanentFile, StandardCopyOption.REPLACE_EXISTING);
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    private Path writeTempSequenceFile(long counter) throws Exception {
        Path tempFile = Files.createTempFile(
                "temp",
                ".bin",
                new FileAttribute<?>[0]);
        tempFile.toFile().deleteOnExit();
        buffer.clear();
        buffer.putLong(counter);
        Files.write(tempFile, buffer.array(), StandardOpenOption.WRITE, StandardOpenOption.SYNC);
        return tempFile;
    }

    @Override
    public long load() throws IOException {
        Path path = Paths.get(filePath);
        if (!Files.exists(path)) {
            return 0;
        }
        byte[] bytes = Files.readAllBytes(path);
        ByteBuffer buffer = ByteBuffer.wrap(bytes);
        return buffer.getLong();
    }
}
