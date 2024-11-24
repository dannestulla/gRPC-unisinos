package org.mes.server;

import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.stub.StreamObserver;
import org.mes.Mes;
import org.mes.MesServiceGrpc;

import java.io.IOException;
import java.util.Random;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class DataCollectionServer extends MesServiceGrpc.MesServiceImplBase {

    private final ReadWriteLock lock = new ReentrantReadWriteLock();

    @Override
    public void collectData(Mes.ProductionData request, StreamObserver<Mes.DataResponse> responseObserver) {
        lock.readLock().lock();
        try {
            System.out.println("Data collected from device " + request.getDeviceId());
            // pre-processamento dos dados, incluindo leitura do que já está salvo, nao exige lock
            // writelock somente no momento de gravar
            lock.writeLock().lock();
            Thread.sleep(new Random().nextInt(10, 120)); // simula gravaçao
            lock.writeLock().unlock();
            Mes.DataResponse response = Mes.DataResponse.newBuilder().setStatus("Data Collected").build();
            responseObserver.onNext(response);
            responseObserver.onCompleted();
        } catch (Exception e) {
            responseObserver.onError(e);
        } finally {
            lock.readLock().unlock();
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
