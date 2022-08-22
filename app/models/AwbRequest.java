package models;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class AwbRequest {

    private List<Order> orders = new ArrayList<>();

    private String printingSize;

    private PrinterOrientation printerOrientation;
}
