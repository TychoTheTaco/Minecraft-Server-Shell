package com.tycho.mss;

public interface ServerShellConnection {
    void attach(ServerShell serverShell);
    void detach(ServerShell serverShell);
}
