# Airway Bill (AWB) Generator

## Overview

This service generates an Airway Bill based on request parameters passed in via a REST API call.

## Prerequisites

* Java JDK 8.x

## Getting started

### Step 1.
Clone this repository, and run with the following
```bash
sbt clean update compile run
```

## Usage examples
Generate the airway bill with the following. This assumes that you are running the service locally.
```bash
curl --location --request POST 'localhost:9000/1.0/reports/waybill' \
--header 'Content-Type: application/json' \
--data-raw '{
    "orders": [
        {
            "trackingId": "A0000001",
            "sentFrom": "Shipper A",
            "fromName": "Shipper A",
            "fromContact": "Shipper A'\''s Contact Number",
            "fromAddress1": "Shipper A'\''s Warehouse Address 1",
            "fromAddress2": "Shipper A'\''s Warehouse Address 2",
            "fromCity": "Shipper A'\''s Warehouse City",
            "fromPostcode": "Shipper A'\''s Warehouse Postcode",
            "fromCountry": "Shipper A'\''s Warehouse Country",
            "toName": "Consignee A",
            "toContact": "Consignee A'\''s Contact Number",
            "toAddress1": "Consignee A'\''s Home Address 1",
            "toAddress2": "Consignee A'\''s Home Address 2",
            "toCity": "Consignee A'\''s Home City",
            "parcelSize": "Consignee A'\''s Parcel Size",
            "sortCode": "Consignee A'\''s Parcel Sort Code",
            "deliveryInstructions": "Please place the parcel outside the door",
            "companyUrl": "Logistic Provider'\''s Company Url"
        }
    ],
    "printingSize": "A4",
    "printerOrientation": "PORTRAIT"
}'
```

## License
This source codes are published by Ninja Logistics Pte. Ltd. on open source licence terms pursuant to MIT License, and the codes incorporates `com.itextpdf.itextpdf 5.5.6`, `com.itextpdf.tool.xmlworker 5.5.11` GNU AGPL V.3"

## Contributing

Pull requests are welcome. For major changes, please start a conversation first to discuss what you would like to
change.
