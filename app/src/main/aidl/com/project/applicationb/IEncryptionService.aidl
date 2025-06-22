package com.project.applicationb;

interface IEncryptionService {
    void sendOneWayMessage(String message); // One-way Messaging - Client Sends message to server
    String twoWayMessaging(String message); // Two-way messaging - Client and server communicate
}