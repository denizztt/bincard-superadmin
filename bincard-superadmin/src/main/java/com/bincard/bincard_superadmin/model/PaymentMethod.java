package com.bincard.bincard_superadmin.model;

public enum PaymentMethod {
    CASH("Nakit"),
    CREDIT_CARD("Kredi Kartı"),
    DEBIT_CARD("Banka Kartı"),
    MOBILE_APP("Mobil Uygulama"),
    QR_CODE("QR Kod");

    private final String displayName;

    PaymentMethod(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    @Override
    public String toString() {
        return displayName;
    }
}
