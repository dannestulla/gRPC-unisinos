package org.mes.client;

import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import org.mes.Mes;
import org.mes.MesServiceGrpc;

public class DashboardClient {

    public static void main(String[] args) {
        ManagedChannel channel = ManagedChannelBuilder.forAddress("localhost", 50052)
                .usePlaintext()
                .build();

        MesServiceGrpc.MesServiceBlockingStub stub = MesServiceGrpc.newBlockingStub(channel);

        Mes.DataRequest request = Mes.DataRequest.newBuilder().setDeviceId(101).build();
        Mes.DataReport report = stub.processData(request);

        System.out.println("OEE for Device " + report.getDeviceId() + ": " + report.getOee());
        System.out.println("Analysis: " + report.getAnalysis());

        channel.shutdown();
    }
}
