using System;
using System.Collections.Generic;
using System.Drawing;
using System.Drawing.Imaging;
using System.Linq;
using System.Runtime.InteropServices;
using System.Text;
using System.Threading.Tasks;

namespace ArcFaceCore
{
    /// <summary>
    /// 人脸识别
    /// </summary>
    public class ArcFaceDetection
    {
        private int detectSize = 40 * 1024 * 1024;
        private int nScale = 50;
        private int nMaxFaceNum = 10;

        private string AppID;
        private string AfdKey;

        private IntPtr hEngine = IntPtr.Zero;

        /// <summary>
        /// 初始化人脸检测
        /// </summary>
        /// <param name="appID"></param>
        /// <param name="afdKey"></param>
        public ArcFaceDetection(string appID, string afdKey)
        {
            this.AppID = appID;
            this.AfdKey = afdKey;
        }

        /// <summary>
        /// 初始化引擎内存缓冲区
        /// </summary>
        /// <returns></returns>
        public int InitFaceEngine()
        {
            if (this.hEngine != IntPtr.Zero) return 0;

            IntPtr detectEngine = IntPtr.Zero;
            IntPtr pMem = Marshal.AllocHGlobal(this.detectSize);
            int retCode = AFDFunction.AFD_FSDK_InitialFaceEngine(this.AppID, this.AfdKey, pMem, detectSize, ref detectEngine, (int)AFD_FSDK_OrientPriority.AFD_FSDK_OPF_0_HIGHER_EXT, nScale, nMaxFaceNum);

            if (retCode == 0)
            {
                this.hEngine = detectEngine;
            }
            return retCode;
        }

        /// <summary>
        /// 人脸检测
        /// </summary>
        /// <param name="image"></param>
        /// <returns></returns>
        public AFD_FSDK_FACERES DetectFace(Bitmap image)
        {
            if (this.hEngine == IntPtr.Zero) return default(AFD_FSDK_FACERES);

            int width = 0;
            int height = 0;
            int pitch = 0;
            byte[] imageData = ReadBmp(image, ref width, ref height, ref pitch);

            IntPtr imageDataPtr = Marshal.AllocHGlobal(imageData.Length);
            Marshal.Copy(imageData, 0, imageDataPtr, imageData.Length);

            ASVLOFFSCREEN offInput = new ASVLOFFSCREEN();
            offInput.u32PixelArrayFormat = ArcFaceCore.ASVL_PAF_RGB24_B8G8R8;
            offInput.ppu8Plane = new IntPtr[4];
            offInput.ppu8Plane[0] = imageDataPtr;
            offInput.i32Width = width;
            offInput.i32Height = height;
            offInput.pi32Pitch = new int[4];
            offInput.pi32Pitch[0] = pitch;
            //入参
            IntPtr offInputPtr = Marshal.AllocHGlobal(Marshal.SizeOf(offInput));
            Marshal.StructureToPtr(offInput, offInputPtr, false);
            //返参
            AFD_FSDK_FACERES faceRes = new AFD_FSDK_FACERES();
            IntPtr faceResPtr = Marshal.AllocHGlobal(Marshal.SizeOf(faceRes));

            int detectResult = AFDFunction.AFD_FSDK_StillImageFaceDetection(this.hEngine, offInputPtr, ref faceResPtr);
            if (detectResult == 0)
            {
                faceRes = (AFD_FSDK_FACERES)Marshal.PtrToStructure(faceResPtr, typeof(AFD_FSDK_FACERES));
            }
            return faceRes;
        }

        /// <summary>
        /// 引擎资源释放
        /// </summary>
        /// <returns></returns>
        public int UninitFaceEngine()
        {
            if (this.hEngine == IntPtr.Zero) return -1;

            int ret = AFDFunction.AFD_FSDK_UninitialFaceEngine(this.hEngine);
            if (ret == 0) this.hEngine = IntPtr.Zero;

            return ret;
        }

        /// <summary>
        /// 多个人脸范围的取得
        /// </summary>
        /// <param name="faceCount"></param>
        /// <param name="facePtr"></param>
        /// <returns></returns>
        public MRECT[] ConvertMRECT(int faceCount, IntPtr facePtr)
        {
            MRECT[] rects = new MRECT[faceCount];

            int perSize = Marshal.SizeOf(typeof(MRECT));
            for (int i = 0; i < faceCount; i++)
            {
                MRECT rect = (MRECT)Marshal.PtrToStructure(facePtr + i * perSize, typeof(MRECT));
                rects[i] = rect;
            }
            return rects;
        }

        private byte[] ReadBmp(Bitmap image, ref int width, ref int height, ref int pitch)
        {
            //将Bitmap锁定到系统内存中,获得BitmapData
            BitmapData data = image.LockBits(new Rectangle(0, 0, image.Width, image.Height), ImageLockMode.ReadOnly, PixelFormat.Format24bppRgb);
            //位图中第一个像素数据的地址。它也可以看成是位图中的第一个扫描行
            IntPtr ptr = data.Scan0;
            //定义数组长度
            int sourceBitArrayLength = data.Height * Math.Abs(data.Stride);
            byte[] sourceBitArray = new byte[sourceBitArrayLength];
            //将bitmap中的内容拷贝到ptr_bgr数组中
            Marshal.Copy(ptr, sourceBitArray, 0, sourceBitArrayLength);

            width = data.Width;
            height = data.Height;
            pitch = Math.Abs(data.Stride);

            int line = width * 3;
            int bgr_len = line * height;
            byte[] destBitArray = new byte[bgr_len];
            for (int i = 0; i < height; i++)
            {
                Array.Copy(sourceBitArray, i * pitch, destBitArray, i * line, line);
            }
            pitch = line;
            image.UnlockBits(data);
            return destBitArray;
        }
    }
}
