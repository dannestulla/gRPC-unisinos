package org.mes.client;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import org.mes.Mes;
import org.mes.MesServiceGrpc;

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
        IoTClient client = new IoTClient("localhost", 50051);

        // Simula diversas chamadas para testar o Mutex
        for(int i=0; i<1000;i++){
            System.out.println("Sending data  " + i);
            client.sendProductionData();
        }
        System.out.println("Data simulation completed");
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
