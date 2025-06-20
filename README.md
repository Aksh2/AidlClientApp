This is the client app (Application A) in the AIDL IPC.
- The client app binds to the service running on the server app.
- The communication happens via AIDL
- The message is encrypted using AES and android keystore before sending it to the server.
