package org.mes.client;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import org.mes.Mes;
import org.mes.MesServiceGrpc;

import javax.swing.plaf.TableHeaderUI;
import java.time.LocalDateTime;

public class IoTClient {

    private final MesServiceGrpc.MesServiceBlockingStub stub;

    public IoTClient(String host, int port){
        ManagedChannel channel = ManagedChannelBuilder.forAddress(host, port)
                .usePlaintext()
                .build();

        stub = MesServiceGrpc.newBlockingStub(channel);
    }

    public static void main(String[] args) {
        final IoTClient client = new IoTClient("localhost", 50051);

        // Número de threads para simular dispositivos IoT
        int numberOfThreads = 10;
        // Criação de um pool de threads para gerenciar envio simultâneo
        Thread[] threads = new Thread[numberOfThreads];

        for(int i=0; i<numberOfThreads;i++){
            threads[i] = new Thread(() -> {
                System.out.println("Thread started.");
                client.sendProductionData();
                System.out.println("Thread completed.");
            });

        }

        // Iniciar todas as threads
        for (Thread thread : threads) {
            thread.start();
        }

        // Aguarda todas as threads concluírem a execução
        for (Thread thread : threads) {
            try {
                thread.join();
            } catch (InterruptedException e) {
                System.err.println("Thread interrupted: " + e.getMessage());
                Thread.currentThread().interrupt();
            }
        }

        System.out.println("Data simulation completed.");
    }

    void sendProductionData() {
        // Criação de uma solicitação
        Mes.ProductionData dataRequest = Mes.ProductionData.newBuilder()
                .setDeviceId(101)
                .setTimestamp(LocalDateTime.now().toString())
                .setTemperature(37.5)
                .setVibration(0.002)
                .build();

        Mes.DataResponse response = stub.collectData(dataRequest);
        System.out.println("Response: " + response.getStatus());
    }
}
