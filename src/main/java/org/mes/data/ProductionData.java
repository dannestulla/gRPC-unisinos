package org.mes.data;


import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ProductionData {
    private int deviceId;
    private String timestamp;
    private double temperature;
    private double vibration;
}
