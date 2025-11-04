package edu.fcu.furniturerecyclingbackend.model;

/**
 * ApplicationStatus
 * 申請單狀態列舉，對應 applications.status 欄位。
 * 用於標準化申請流程的狀態管理。
 */
public enum ApplicationStatus {
    /** 已送出申請（預設狀態） */
    SUBMITTED,
    /** 已審核通過（受理） */
    APPROVED,
    /** 已審核拒絕 */
    REJECTED,
    /** 已排程（分配清運行程） */
    SCHEDULED,
    /** 已完成清運 */
    COMPLETED,
    /** 已取消申請 */
    CANCELLED
}
