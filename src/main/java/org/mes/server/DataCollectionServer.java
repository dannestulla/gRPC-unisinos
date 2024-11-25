package org.mes.server;

import io.grpc.Server;
import io.grpc.ServerBuilder;
import io.grpc.stub.StreamObserver;
import org.mes.Mes;
import org.mes.MesServiceGrpc;

import java.io.IOException;
import java.util.Random;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class DataCollectionServer extends MesServiceGrpc.MesServiceImplBase {

    private final ReadWriteLock lock = new ReentrantReadWriteLock(true);

    @Override
    public void collectData(Mes.ProductionData request, StreamObserver<Mes.DataResponse> responseObserver) {
        System.out.println("Received data from device " + request.getDeviceId() + " - thread " + Thread.currentThread().threadId());
        Lock readLock = lock.readLock();
        Lock writeLock = lock.writeLock();
        readLock.lock();
        try {
            System.out.println("Processing data from device " + request.getDeviceId() + " - thread " + Thread.currentThread().threadId());
            // pre-processamento dos dados, incluindo leitura do que já está salvo, nao exige lock
            // writelock somente no momento de gravar
        } catch (Exception e) {
            System.out.println("Returning error while processing to device " + request.getDeviceId() + " - thread " + Thread.currentThread().threadId());
            e.printStackTrace();
            responseObserver.onError(e);
        } finally {
            readLock.unlock();
        }
        System.out.println("Data processed for device " + request.getDeviceId() + " - thread " + Thread.currentThread().threadId());

        if(true && new Random().nextInt() % 100 == 0) {
            writeLock.lock();
            try {
                System.out.println("Saving data for device " + request.getDeviceId() + " - thread " + Thread.currentThread().threadId());
                //Thread.sleep(new Random().nextInt(1, 10)); // simula gravaçao
                System.out.println("Saved data for device " + request.getDeviceId() + " - thread " + Thread.currentThread().threadId());
                System.out.println(writeLock.toString());
            } catch (Exception e) {
                System.out.println("Returning error while saving to device " + request.getDeviceId() + " - thread " + Thread.currentThread().threadId());
                e.printStackTrace();
                responseObserver.onError(e);
            } finally {
                //writeLock.unlock();
            }
        }
        Mes.DataResponse response = Mes.DataResponse.newBuilder().setStatus("Data Collected").build();
        responseObserver.onNext(response);
        responseObserver.onCompleted();
        System.out.println("Returned response to device " + request.getDeviceId() + " - thread " + Thread.currentThread().threadId());

    }

    public static void main(String[] args) throws IOException, InterruptedException {
        // Muito importante limitar a quantidade de threads pois o Executor padrao cria 1000 threads nativas, o ideal é manter de 1 a 2 vezes o numero de cores disponíveis
        Server server = ServerBuilder.forPort(50051)
                .executor(Executors.newFixedThreadPool(32))
                .addService(new DataCollectionServer())
                .build()
                .start();

        System.out.println("DataCollectionServer started" + " - thread " + Thread.currentThread().threadId());
        server.awaitTermination();
    }

}
