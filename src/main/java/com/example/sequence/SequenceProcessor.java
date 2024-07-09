package com.example.sequence;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.atomic.AtomicLong;

import com.example.storage.SimpleStorage;
import com.example.storage.Storage;

public class SequenceProcessor implements Runnable {
    private static SequenceProcessor instance;

    private ArrayBlockingQueue<SequenceRequest> queue;
    private AtomicLong record;
    private Map<String, AtomicLong> map;
    private int step = 10;
    private Storage storage;
    private long startup;

    public static SequenceProcessor getInstance() throws Exception {
        if (instance == null) {
            instance = new SequenceProcessor(new SimpleStorage());
            Thread t = new Thread(instance);
            t.start();
        }
        return instance;
    }

    public SequenceProcessor(Storage storage) throws Exception {
        this.queue = new ArrayBlockingQueue<>(500);
        this.storage = storage;
        this.startup = storage.load();
        this.record = new AtomicLong(startup);
        this.map = new HashMap<>();
    }

    public void publish(SequenceRequest event) {
        queue.offer(event);
    }

    @Override
    public void run() {
        while (true) {
            try {
                consume();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private void consume() throws Exception {
        SequenceRequest event = queue.take();
        String uid = event.getUid();
        if (!checkScope(uid)) {
            event.error();
            return;
        }

        map.computeIfAbsent(uid, k -> new AtomicLong(startup));
        AtomicLong al = map.get(uid);
        if (al.get() + 1 > record.get()) {
            boolean r = storage.save(record.get() + step);
            if (r) {
                record.addAndGet(step);
            } else {
                event.error();
                return;
            }
        }
        long ans = al.incrementAndGet();
        event.output(ans);
    }
    private boolean checkScope(String uid) {
        return true;
    }
}
