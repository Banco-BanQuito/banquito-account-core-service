package ec.edu.espe.banquito.core.accountcore.enums;

public enum TransactionSubtypeCode {
    DEP_VEN,
    RET_VEN,
    PAG_NOM_C,
    DEB_EMP,
    DEV_EMP,
    TRF_P2P_S,
    TRF_P2P_E,
    TRF_EXT_S,

    // RF-01: subtipos de reverso para compensacion explicita (uno por movimiento
    // local revertible en executeDeposit/executeWithdrawal/executeP2PTransfer/
    // executeBatchCredit/executeCorporateRefund).
    DEP_REV,
    RET_REV,
    TRF_P2P_S_REV,
    TRF_P2P_E_REV,
    PAG_NOM_C_REV,
    DEV_EMP_REV
}
