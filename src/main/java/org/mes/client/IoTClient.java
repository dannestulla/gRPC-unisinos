package org.mes.client;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import org.mes.Mes;
import org.mes.MesServiceGrpc;

import java.time.LocalDateTime;

public class IoTClient {

    public static void main(String[] args) {
        ManagedChannel channel = ManagedChannelBuilder.forAddress("localhost", 50051)
                .usePlaintext()
                .build();

        MesServiceGrpc.MesServiceBlockingStub stub = MesServiceGrpc.newBlockingStub(channel);

        Mes.ProductionData dataRequest = Mes.ProductionData.newBuilder()
                .setDeviceId(101)
                .setTimestamp(LocalDateTime.now().toString())
                .setTemperature(37.5)
                .setVibration(0.002)
                .build();

        Mes.DataResponse response = stub.collectData(dataRequest);
        System.out.println("Response: " + response.getStatus());

        channel.shutdown();
    }
}
