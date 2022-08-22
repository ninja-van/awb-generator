package models;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class AwbPrintingStyle {

    int subtitleFontSize;
    int descriptionFontSize;
    int trackingNumberFontSize;
    int boxTitleFontSize;
    int boxContentHeight;
    int rowContentHeight;
    int boxContentTableFontSize;
    int boxContentTableWidthPercentage;
    int marginHeight;
    int marginFontSize;
    int footerMarginHeight;
    int footerMarginFontSize;
    int parcelSizeTitleFontSize;
    int parcelSizeImageHeight;
    int parcelSizeDescriptionFontSize;
    int boxSmallTitleFontSize;
    int boxCommentHeight;
    int boxCodFontSize;
    int boxCodHeight;
    int infoIconHeight;
    int addressFontSize;
    int timeslotFontSize;
    int codFontSize;
    int commentsFontSize;
    int sortCodeFontSize;
    int sortCodeGuideFontSize;
    int boxQRHeight;
    int boxBarcodeHeight;
}
