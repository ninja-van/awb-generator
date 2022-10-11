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
These source codes (Airway Bill (AWB) Generator) incorporates source codes from iText (com.itextpdf.itextpdf
5.5.6, com.itextpdf.tool.xmlworker 5.5.11) and are published under GNU Affero General Public License version 3.
Copyright (C) <2022> <Ninja Logistics Pte. Ltd.>
Copyright (C) iText® 5.5.6 ©2000-2015 iText Group NV (AGPL-version) for abovementioned source
codes from iText

This program is free software: you can redistribute it and/or modify it under the terms of the GNU Affero General Public License as
published by the Free Software Foundation, either version 3 of the License, or (at your option) any later version.

This program is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY; without even the implied warranty of
MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more details.

You should have received a copy of the GNU Affero General Public License along with this program. If not, see https://www.gnu.org/licenses/.

## Contributing

Pull requests are welcome. For major changes, please start a conversation first to discuss what you would like to
change.
