package edu.fcu.furniturerecyclingbackend.model;

//固定 5 個地點，不會改變
//Application 裡面 dropPointCode 連到這個 DropPoint。
public enum DropPoint {
    DP_001("DP001", "一號定點"),
    DP_002("DP002", "二號定點"),
    DP_003("DP003", "三號定點"),
    DP_004("DP004", "四號定點"),
    DP_005("DP005", "五號定點");

    public final String code;
    public final String label;

    DropPoint(String code, String label) {
        this.code = code;
        this.label = label;
    }

    //驗證代碼是否合法
    public static boolean isValid(String code) {
        for (DropPoint d : values()) {
            if (d.code.equals(code)) return true;
        }
        return false;
    }
}
