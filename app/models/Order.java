package models;

import lombok.Data;

@Data
public class Order {

    private String trackingId;

    private String sentFrom;

    private String fromName;

    private String fromContact;

    private String fromAddress1;

    private String fromAddress2;

    private String fromCity;

    private String fromCountry;

    private String fromPostcode;

    private String toName;

    private String toAddress1;

    private String toAddress2;

    private String toCity;

    private String toContact;

    private String parcelSize;

    private String sortCode;

    private String companyUrl;

    private String deliveryInstructions;
}
