using System;
using System.Collections.Generic;
using System.Linq;
using System.Runtime.InteropServices;
using System.Text;
using System.Threading.Tasks;

namespace ArcFaceCore
{
    /// <summary>
    /// 人脸检测API
    /// </summary>
    public class AFDFunction
    {
        /// <summary>
        /// 初始化脸部检测引擎 
        /// </summary>
        /// <param name="AppId">用户申请 SDK 时获取的 App Id </param>
        /// <param name="SDKKey">用户申请 SDK 时获取的 SDK Key </param>
        /// <param name="pMem">分配给引擎使用的内存地址 </param>
        /// <param name="lMemSize">分配给引擎使用的内存大小 </param>
        /// <param name="pEngine">引擎 handle </param>
        /// <param name="iOrientPriority">期望的脸部检测角度的优先级 </param>
        /// <param name="nScale">用于数值表示的最小人脸尺寸 有效值范围[2,50] 推荐值 16 </param>
        /// <param name="nMaxFaceNum">用户期望引擎最多能检测出的人脸数 有效值范围[1,100]</param>
        /// <returns>
        /// 成功返回 MOK，否则返回失败 code。失败 codes如下所列: 
        ///     MERR_INVALID_PARAM 参数输入非法 
        ///     MERR_NO_MEMORY  内存不足
        /// </returns>
        [DllImport(@".\assembly\libarcsoft_fsdk_face_detection.dll", CallingConvention = CallingConvention.Cdecl)]
        public static extern int AFD_FSDK_InitialFaceEngine(string AppId,
            string SDKKey,
            IntPtr pMem,
            int lMemSize,
            ref IntPtr pEngine,
            int iOrientPriority,
            int nScale,
            int nMaxFaceNum);

        /// <summary>
        /// 根据输入的图像检测出人脸位置，一般用于静态图像检测 
        /// </summary>
        /// <param name="hEngine">引擎 handle </param>
        /// <param name="pImgData">带检测图像信息 </param>
        /// <param name="pFaceRes">人脸检测结果 </param>
        /// <returns>成功返回 MOK，否则返回失败 code。</returns>
        [DllImport(@".\assembly\libarcsoft_fsdk_face_detection.dll", CallingConvention = CallingConvention.Cdecl)]
        public static extern int AFD_FSDK_StillImageFaceDetection(IntPtr hEngine, IntPtr pImgData, ref IntPtr pFaceRes);

        /// <summary>
        /// 销毁引擎，释放相应资源 
        /// </summary>
        /// <param name="hEngine">引擎 handle </param>
        /// <returns>
        /// 成功返回 MOK，否则返回失败 code。失败 codes如下所列:
        ///     MERR_INVALID_PARAM 参数输入非法
        /// </returns>
        [DllImport(@".\assembly\libarcsoft_fsdk_face_detection.dll", CallingConvention = CallingConvention.Cdecl)]
        public static extern int AFD_FSDK_UninitialFaceEngine(IntPtr hEngine);

        /// <summary>
        /// 获取 SDK版本信息 
        /// </summary>
        /// <param name="hEngine">引擎 handle </param>
        /// <returns></returns>
        [DllImport(@".\assembly\libarcsoft_fsdk_face_detection.dll", CallingConvention = CallingConvention.Cdecl)]
        public static extern IntPtr AFD_FSDK_GetVersion(IntPtr hEngine);
    }
}
