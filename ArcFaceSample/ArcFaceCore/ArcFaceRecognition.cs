using System;
using System.Collections.Generic;
using System.IO;
using System.Linq;
using System.Runtime.InteropServices;
using System.Text;
using System.Threading.Tasks;

namespace ArcFaceCore
{
    /// <summary>
    /// 人脸识别
    /// </summary>
    public class ArcFaceRecognition
    {        
        private int recognizeSize = 40 * 1024 * 1024;

        private string AppID;
        private string AfrKey;

        public IntPtr rEngine { get; private set; } = IntPtr.Zero;
        private IntPtr pMem = IntPtr.Zero;

        /// <summary>
        /// 人脸识别class初始化
        /// </summary>
        /// <param name="appID">appID</param>
        /// <param name="afrKey">人脸识别key</param>
        public ArcFaceRecognition(string appID, string afrKey)
        {
            this.AppID = appID;
            this.AfrKey = afrKey;
            this.pMem = Marshal.AllocHGlobal(recognizeSize);
        }

        /// <summary>
        /// 初始化人脸识别引擎
        /// </summary>
        /// <returns></returns>
        public int InitFaceEngine()
        {
            if (rEngine != IntPtr.Zero) return 0;

            IntPtr recognizeEngine = IntPtr.Zero;
            int retCode = AFRFunction.AFR_FSDK_InitialEngine(this.AppID, this.AfrKey, this.pMem, this.recognizeSize, ref recognizeEngine);

            if (retCode == 0)
            {
                this.rEngine = recognizeEngine;
            }
            return retCode;
        }

        /// <summary>
        /// 引擎资源释放
        /// </summary>
        /// <returns></returns>
        public int UninitFaceEngine()
        {
            if (this.rEngine == IntPtr.Zero) return -1;

            int ret = AFRFunction.AFR_FSDK_UninitialEngine(this.rEngine);
            if (ret == 0) this.rEngine = IntPtr.Zero;

            return ret;
        }
    }
}
