package com.example.storage;

import java.io.IOException;

public interface Storage {
    boolean save(long counter);

    long load() throws IOException;
}
