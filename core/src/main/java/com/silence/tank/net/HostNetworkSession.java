package com.silence.tank.net;

import com.silence.tank.GameSnapshot;
import com.silence.tank.InputCommand;

import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.atomic.AtomicReference;

public final class HostNetworkSession implements AutoCloseable {
    private final int port;
    private final AtomicReference<InputCommand> remoteInput = new AtomicReference<>(InputCommand.none());
    private final Object writeLock = new Object();
    private volatile boolean running = true;
    private volatile String status;
    private volatile ServerSocket serverSocket;
    private volatile Socket clientSocket;
    private volatile ObjectOutputStream output;

    public HostNetworkSession(int port) {
        this.port = port;
        this.status = "HOST waiting on port " + port;
        Thread thread = new Thread(this::acceptLoop, "tank-host-network");
        thread.setDaemon(true);
        thread.start();
    }

    public InputCommand remoteInput() {
        return remoteInput.get().copy();
    }

    public String status() {
        return status;
    }

    public boolean connected() {
        Socket socket = clientSocket;
        return socket != null && socket.isConnected() && !socket.isClosed();
    }

    public void sendSnapshot(GameSnapshot snapshot) {
        ObjectOutputStream stream = output;
        if (stream == null) {
            return;
        }
        synchronized (writeLock) {
            try {
                stream.writeObject(new HostSnapshotMessage(snapshot));
                stream.reset();
                stream.flush();
            } catch (IOException e) {
                closeClient();
                status = "HOST client disconnected";
            }
        }
    }

    private void acceptLoop() {
        try (ServerSocket server = new ServerSocket(port)) {
            serverSocket = server;
            while (running) {
                Socket socket = server.accept();
                closeClient();
                clientSocket = socket;
                status = "HOST connected to " + socket.getInetAddress().getHostAddress();
                ObjectOutputStream out = new ObjectOutputStream(socket.getOutputStream());
                out.flush();
                ObjectInputStream in = new ObjectInputStream(socket.getInputStream());
                try (out; in) {
                    output = out;
                    while (running && !socket.isClosed()) {
                        Object object = in.readObject();
                        if (object instanceof ClientInputMessage message) {
                            remoteInput.set(message.command());
                        }
                    }
                } catch (EOFException ignored) {
                    status = "HOST client disconnected";
                } catch (IOException | ClassNotFoundException e) {
                    if (running) {
                        status = "HOST network error: " + e.getClass().getSimpleName();
                    }
                } finally {
                    output = null;
                    remoteInput.set(InputCommand.none());
                    closeClient();
                }
            }
        } catch (IOException e) {
            if (running) {
                status = "HOST failed on port " + port + ": " + e.getMessage();
            }
        }
    }

    private void closeClient() {
        Socket socket = clientSocket;
        clientSocket = null;
        output = null;
        if (socket != null) {
            try {
                socket.close();
            } catch (IOException ignored) {
            }
        }
    }

    @Override
    public void close() {
        running = false;
        closeClient();
        ServerSocket server = serverSocket;
        if (server != null) {
            try {
                server.close();
            } catch (IOException ignored) {
            }
        }
    }
}
