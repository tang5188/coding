package tang.com.arcfaceandroid;

import android.graphics.Bitmap;
import android.nfc.Tag;
import android.util.Log;

import com.arcsoft.facerecognition.AFR_FSDKEngine;
import com.arcsoft.facerecognition.AFR_FSDKError;
import com.arcsoft.facerecognition.AFR_FSDKFace;
import com.arcsoft.facerecognition.AFR_FSDKVersion;
import com.guo.android_extend.java.ExtInputStream;
import com.guo.android_extend.java.ExtOutputStream;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.file.FileStore;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class FaceDB {
    private final String TAG = this.getClass().toString();

    public static String appid = "8tWpRPqHyrqVEZxEwyJ8pPiRwCoXL4qCoapRmAE6PywE";
    public static String ft_key = "CGwuwgGKa97DaUaYPnXpR92xcoUsavsbPdyPMwtV2Fm9";
    public static String fd_key = "CGwuwgGKa97DaUaYPnXpR935nCk2AfGWjyeyKdLBosLo";
    public static String fr_key = "CGwuwgGKa97DaUaYPnXpR93aRonkkEoFtViNhUGzXbvH";
    public static String fage_key = "CGwuwgGKa97DaUaYPnXpR93pkcK7EDh965BaALqSGmHr";
    public static String fgender_key = "CGwuwgGKa97DaUaYPnXpR93wv1aHB4eGSRMR6VibRZCv";

    String mDBPath;
    List<FaceRegist> mRegister;
    AFR_FSDKEngine mFREngine;
    AFR_FSDKVersion mFRVersion;
    boolean mUpgrade;

    class FaceRegist {
        String mName;
        Map<String, AFR_FSDKFace> mFaceList;

        public FaceRegist(String name) {
            mName = name;
            mFaceList = new LinkedHashMap<>();
        }
    }

    public FaceDB(String path) {
        mDBPath = path;
        mRegister = new ArrayList<>();
        mFRVersion = new AFR_FSDKVersion();
        mUpgrade = false;
        mFREngine = new AFR_FSDKEngine();

        AFR_FSDKError error = mFREngine.AFR_FSDK_InitialEngine(FaceDB.appid, FaceDB.fr_key);
        if (error.getCode() != AFR_FSDKError.MOK) {
            Log.e(TAG, "AFR_FSDK_InitialEngine fail! error code=" + error.getCode());
        } else {
            mFREngine.AFR_FSDK_GetVersion(mFRVersion);
            Log.d(TAG, "AFR_FSDK_GetVersion=" + mFRVersion.toString());
        }
    }

    public void destory() {
        if (mFREngine != null) {
            mFREngine.AFR_FSDK_UninitialEngine();
        }
    }

    private boolean saveInfo() {
        try {
            FileOutputStream fos = new FileOutputStream(mDBPath + "/face.txt");
            ExtOutputStream eos = new ExtOutputStream(fos);
            eos.writeString(mFRVersion.toString() + "," + mFRVersion.getFeatureLevel());
            eos.close();
            fos.close();
            return true;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    private boolean loadInfo() {
        if (!mRegister.isEmpty()) {
            return false;
        }
        try {
            FileInputStream fis = new FileInputStream(mDBPath + "/face.txt");
            ExtInputStream eis = new ExtInputStream(fis);
            //load version
            String version_saved = eis.readString();
            if (version_saved.equals(mFRVersion.toString() + "," + mFRVersion.getFeatureLevel())) {
                mUpgrade = true;
            }
            //load all regist name
            if (version_saved != null) {
                for (String name = eis.readString(); name != null; name = eis.readString()) {
                    if (new File(mDBPath + "/" + name + ".data").exists()) {
                        mRegister.add(new FaceRegist(new String(name)));
                    }
                }
            }
            eis.close();
            fis.close();
            return true;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean loadFaces() {
        if (loadInfo()) {
            try {
                for (FaceRegist face : mRegister) {
                    Log.d(TAG, "load_name:" + face.mName + "'s face feature data.");
                    FileInputStream fis = new FileInputStream(mDBPath + "/" + face.mName + ".data");
                    ExtInputStream eis = new ExtInputStream(fis);
                    AFR_FSDKFace afr = null;
                    do {
                        if (afr != null) {
                            if (mUpgrade) {
                            }
                            String keyFile = eis.readString();
                            face.mFaceList.put(keyFile, afr);
                        }
                        afr = new AFR_FSDKFace();
                    } while (eis.readBytes(afr.getFeatureData()));
                    eis.close();
                    fis.close();
                    Log.d(TAG, "load name: size=" + face.mFaceList.size());
                }
                return true;
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    public void addFace(String name, AFR_FSDKFace face, Bitmap faceIcon) {
        try {
            String keyPath = mDBPath + "/" + System.nanoTime() + ".jpg";
            File keyFile = new File(keyPath);
            OutputStream os = new FileOutputStream(keyFile);
            if (faceIcon.compress(Bitmap.CompressFormat.JPEG, 80, os)) {
                Log.d(TAG, "saved face bitmap to jpg");
            }
            os.close();

            boolean add = true;
            for (FaceRegist rfFace : mRegister) {
                if (rfFace.mName.equals(name)) {
                    rfFace.mFaceList.put(keyPath, face);
                    add = false;
                    break;
                }
            }
            if (add) {
                FaceRegist frFace = new FaceRegist(name);
                frFace.mFaceList.put(keyPath, face);
                mRegister.add(frFace);
            }
            if (saveInfo()) {
                FileOutputStream fos = new FileOutputStream(mDBPath + "/face.txt", true);
                ExtOutputStream eos = new ExtOutputStream(fos);
                for (FaceRegist frFace : mRegister) {
                    eos.writeString(frFace.mName);
                }
                eos.close();
                fos.close();

                fos = new FileOutputStream(mDBPath + "/" + name + ".data", true);
                eos = new ExtOutputStream(fos);
                eos.writeBytes(face.getFeatureData());
                eos.writeString(keyPath);
                eos.close();
                fos.close();
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public boolean delete(String name) {
        try {
            boolean find = false;
            for (FaceRegist frFace : mRegister) {
                if (frFace.mName.equals(name)) {
                    File delFile = new File(mDBPath + "/" + name + ".data");
                    if (delFile.exists()) {
                        delFile.delete();
                    }
                    mRegister.remove(frFace);
                    find = true;
                    break;
                }
            }
            if (find) {
                if (saveInfo()) {
                    FileOutputStream fos = new FileOutputStream(mDBPath + "/face.txt", true);
                    ExtOutputStream eos = new ExtOutputStream(fos);
                    for (FaceRegist frFace : mRegister) {
                        eos.writeString(frFace.mName);
                    }
                    eos.close();
                    fos.close();
                }
            }
            return find;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    public boolean upgrade() {
        return false;
    }
}

