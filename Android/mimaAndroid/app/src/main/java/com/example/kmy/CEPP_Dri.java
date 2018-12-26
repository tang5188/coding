package com.example.kmy;

public class CEPP_Dri {
    public native int EPP_OpenDevice(String nPortId, long nBaudRate);

    public native int EPP_CloseDevice();

    public native int EPP_InitEPP(byte InitType);

    public native int EPP_GetVersion(byte[] pVersion);

    public native int EPP_GetSerialId(byte[] pSerialId);

    public native int EPP_GenSm2KeyPair(byte[] pSm2Pub, byte[] pSm2Pri);

    public native int EPP_LoadMasterKey(byte KeyId, byte DecryptKeyId, byte KeyLen, byte[] pKeyValue, byte[] pKCV);

    public native int EPP_LoadWorkKey(byte MasterKeyId, byte WorkKeyId, byte KeyLen, byte[] pKeyValue, byte[] pKCV);

    public native int EPP_ActiveKey(byte MasterKeyId, byte WorkKeyId);

    public native int EPP_UseEppPlainTextMode(byte EnabledBeep);

    public native int EPP_GetKey(byte[] pKeyValue);

    public native int EPP_ExitInputMode();

    public native int EPP_LoadCardNumber(byte[] pCardNo);

    public native int EPP_GetPin(byte PinFormat, byte PinMinLen, byte PinMaxLen, byte AutoEnd, int TimeOut, byte EnabledBeep);

    public native int EPP_GetPinBlock(byte[] pPinblock);

    public native int EPP_SetInitVector(byte Vectorlen, byte[] pVector);

    public native int EPP_MakeMac(byte MacAlgorithm, int MacDataLen, byte[] pMacData, byte[] pMacResult);

    public native int EPP_DataCompute(byte EncryptMode, int AlgorithmMode, int Datalen, byte[] pData, byte[] pDataResult);

    public native int EPP_LoadSm2Pub(byte[] pKeyValue);

    public native int EPP_LoadSm2Pri(byte[] pKeyValue);

    public native int EPP_Sm2Dec(int DataLen, byte[] pData, byte[] pDataResult);

    public native int EPP_SetAlgo(byte Algo);
}
