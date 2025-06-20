package com.project.applicationa;

interface IEncryptionService {
    byte[] processEncrypted(in byte[] requestData); // Request-Response
    oneway void sendOneWay(in byte[] requestData);         // One-way
    oneway void sendMessage(in String requestData);         // One-way

}