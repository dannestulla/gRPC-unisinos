package org.mes.server;

import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.stub.StreamObserver;
import org.mes.Mes;
import org.mes.MesServiceGrpc;

import java.io.IOException;

public class DataProcessingServer extends MesServiceGrpc.MesServiceImplBase {


    @Override
    public void processData(Mes.DataRequest request, StreamObserver<Mes.DataReport> responseObserver) {
        System.out.println("Processing data for device " + request.getDeviceId());

        double oee = 85.0;
        String analysis = "Device is performing within expected thresholds.";

        Mes.DataReport report = Mes.DataReport.newBuilder()
                .setDeviceId(request.getDeviceId())
                .setOee(oee)
                .setAnalysis(analysis)
                .build();

        responseObserver.onNext(report);
        responseObserver.onCompleted();
    }


    public static void main(String[] args) throws IOException, InterruptedException {
        Server server = ServerBuilder.forPort(50052)
                .addService(new DataProcessingServer())
                .build()
                .start();

        System.out.println("DataProcessingServer started...");
        server.awaitTermination();
    }
}