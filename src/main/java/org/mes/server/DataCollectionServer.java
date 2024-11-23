package org.mes.server;

import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.stub.StreamObserver;
import org.mes.Mes;
import org.mes.MesServiceGrpc;

import java.io.IOException;
import java.util.concurrent.locks.ReentrantLock;

public class DataCollectionServer extends MesServiceGrpc.MesServiceImplBase {

    private final ReentrantLock lock = new ReentrantLock();

    @Override
    public void collectData(Mes.ProductionData request, StreamObserver<Mes.DataResponse> responseObserver) {
        lock.lock();
        try {
            System.out.println("Data collected from device " + request.getDeviceId());
            Thread.sleep(5000);
            Mes.DataResponse response = Mes.DataResponse.newBuilder().setStatus("Data Collected").build();
            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onError(e);
        } finally {
            lock.unlock();
        }
    }

    public static void main(String[] args) throws IOException, InterruptedException{
        Server server = ServerBuilder.forPort(50051)
                .addService(new DataCollectionServer())
                .build()
                .start();

        System.out.println("DataCollectionServer started");
        server.awaitTermination();
    }

}
