package com.silence.tank.net;

import com.silence.tank.GameSnapshot;
import com.silence.tank.InputCommand;
import com.silence.tank.Texts;

import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.concurrent.atomic.AtomicReference;

public final class ClientNetworkSession implements AutoCloseable {
    private final String host;
    private final int port;
    private final AtomicReference<GameSnapshot> latestSnapshot = new AtomicReference<>();
    private final Object writeLock = new Object();
    private volatile boolean running = true;
    private volatile String status;
    private volatile Socket socket;
    private volatile ObjectOutputStream output;

    public ClientNetworkSession(String host, int port) {
        this.host = host;
        this.port = port;
        this.status = Texts.CLIENT_CONNECTING + host + ":" + port;
        Thread thread = new Thread(this::connectLoop, "tank-client-network");
        thread.setDaemon(true);
        thread.start();
    }

    public String status() {
        return status;
    }

    public GameSnapshot latestSnapshot() {
        return latestSnapshot.get();
    }

    public void sendInput(InputCommand command) {
        ObjectOutputStream stream = output;
        if (stream == null) {
            return;
        }
        synchronized (writeLock) {
            try {
                stream.writeObject(new ClientInputMessage(command));
                stream.reset();
                stream.flush();
            } catch (IOException e) {
                closeSocket();
                status = Texts.CLIENT_DISCONNECTED;
            }
        }
    }

    private void connectLoop() {
        while (running) {
            try {
                Socket next = new Socket();
                next.connect(new InetSocketAddress(host, port), 3000);
                socket = next;
                status = Texts.CLIENT_CONNECTED + host + ":" + port;
                ObjectOutputStream out = new ObjectOutputStream(next.getOutputStream());
                out.flush();
                ObjectInputStream in = new ObjectInputStream(next.getInputStream());
                try (out; in) {
                    output = out;
                    while (running && !next.isClosed()) {
                        Object object = in.readObject();
                        if (object instanceof HostSnapshotMessage message) {
                            latestSnapshot.set(message.snapshot());
                        }
                    }
                }
            } catch (EOFException ignored) {
                status = Texts.CLIENT_HOST_DISCONNECTED;
            } catch (IOException | ClassNotFoundException e) {
                if (running) {
                    status = Texts.CLIENT_RETRYING + host + ":" + port;
                }
            } finally {
                output = null;
                closeSocket();
            }
            sleepBeforeRetry();
        }
    }

    private void sleepBeforeRetry() {
        if (!running) {
            return;
        }
        try {
            Thread.sleep(1200L);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }

    private void closeSocket() {
        Socket current = socket;
        socket = null;
        if (current != null) {
            try {
                current.close();
            } catch (IOException ignored) {
            }
        }
    }

    @Override
    public void close() {
        running = false;
        closeSocket();
    }
}
