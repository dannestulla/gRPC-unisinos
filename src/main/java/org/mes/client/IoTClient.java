package org.mes.client;

import com.google.common.util.concurrent.ThreadFactoryBuilder;
import io.grpc.ManagedChannel;
import io.grpc.ManagedChannelBuilder;
import org.mes.Mes;
import org.mes.MesServiceGrpc;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class IoTClient {

    private final MesServiceGrpc.MesServiceBlockingStub stub;
    private final Random random = new Random();

    public IoTClient(String host, int port) {
        ManagedChannel channel = ManagedChannelBuilder.forAddress(host, port)
                .usePlaintext()
                .build();

        stub = MesServiceGrpc.newBlockingStub(channel);
    }

    public static void main(String[] args) {
        // Executor para rodar os clients IoT simultaneamente
        ExecutorService executorService = Executors.newVirtualThreadPerTaskExecutor();

        // Número de clientes
        int numberOfClients = 500000;

        // statistics
        final List<Long> runtime = new ArrayList<>(numberOfClients);

        final IoTClient client = new IoTClient("localhost", 50051);

        Runnable task = () -> {
            LocalDateTime start = LocalDateTime.now();
            System.out.println("Thread started " + Thread.currentThread().threadId() + " at " + start);
            client.sendProductionData((int) Thread.currentThread().threadId());
            LocalDateTime end = LocalDateTime.now();
            Long diff = ChronoUnit.MILLIS.between(start, end);
            runtime.add(diff);
            System.out.println("Thread completed " + Thread.currentThread().threadId() + " at " + end + "\nThread " + Thread.currentThread().threadId() + " took " + diff + " ms");
        };
        List<Future> futures = new ArrayList<>();

        for (int i = 0; i < numberOfClients; i++) {
            futures.add(executorService.submit(task));
        }

        for (int i = 0; i < numberOfClients; i++) {
            try {
                futures.removeFirst().get();
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        System.out.println("Data simulation completed.");

        double mean = Statistics.mean(runtime.stream().mapToDouble(Long::doubleValue).toArray(), runtime.size());
        double stdDev = Statistics.standardDeviation(runtime.stream().mapToDouble(Long::doubleValue).toArray(), runtime.size());
        System.out.println("Number of clients     : " + numberOfClients);
        System.out.println("Mean runtime per task : " + mean + " ms");
        System.out.println("Variance per task     : " + stdDev + " ms");

        /*** No Locks - Default Server Pool ***/
        //Number of clients     : 5.000
        //Mean runtime per task : 668 ms
        //Variance per task     : 35 ms

        //Number of clients     : 50.000
        //Mean runtime per task : 3213 ms
        //Variance per task     : 824 ms

        //Number of clients     : 500.000
        //Exceçoes durante a execuçao por falta de memória

        /*** No locks ***/
        //Number of clients     : 5.000
        //Mean runtime per task : 669 ms
        //Variance per task     : 34 ms

        //Number of clients     : 50000
        //Mean runtime per task : 2768 ms
        //Variance per task     : 505 ms

        //Number of clients     : 500.000
        //Mean runtime per task : 16148 ms
        //Variance per task     : 6209 ms


        /*** Read Lock ***/
        //Number of clients     : 5.000
        //Mean runtime per task : 689 ms
        //Variance per task     : 37 ms

        //Number of clients     : 50.000
        //Mean runtime per task : 2771 ms
        //Variance per task     : 542 ms

        //Number of clients     : 500.000
        //Mean runtime per task : 17174 ms
        //Variance per task     : 6865 ms

        /*** Read Write Lock **/
        //Number of clients     : 5.000
        //Mean runtime per task : 691 ms
        //Variance per task     : 35 ms

        //Number of clients     : 50.000
        //Mean runtime per task : 3168 ms
        //Variance per task     : 587 ms

        //Number of clients     : 500000
        //Mean runtime per task : 17421 ms
        //Variance per task     : 6286 ms
    }

    void sendProductionData(int id) {
        // Criação de uma solicitação
        Mes.ProductionData dataRequest = Mes.ProductionData.newBuilder()
                .setDeviceId(id)
                .setTimestamp(LocalDateTime.now().toString())
                .setTemperature(100 * random.nextGaussian())
                .setVibration(random.nextGaussian() / 1000)
                .build();

        Mes.DataResponse response = stub.collectData(dataRequest);
        System.out.println("Response: " + response.getStatus());
    }

    static class Statistics {
        // Function for calculating
        // variance
        static double variance(double arr[],
                               int n) {
            double mean = mean(arr, n);
            // Compute sum squared
            // differences with mean.
            double sqDiff = 0;
            for (int i = 0; i < n; i++)
                sqDiff += (arr[i] - mean) *
                        (arr[i] - mean);

            return (double) sqDiff / n;

        }

        static double mean(double arr[], int n) {
            // Compute mean (average
            // of elements)
            double sum = 0;

            for (int i = 0; i < n; i++)
                sum += arr[i];
            return (double) sum /
                    (double) n;
        }

        static double standardDeviation(double arr[],
                                        int n) {
            return Math.sqrt(variance(arr, n));
        }
    }
}
