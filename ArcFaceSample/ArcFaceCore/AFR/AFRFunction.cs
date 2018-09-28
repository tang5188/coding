using System;
using System.Collections.Generic;
using System.Linq;
using System.Runtime.InteropServices;
using System.Text;
using System.Threading.Tasks;

namespace ArcFaceCore
{
    /// <summary>
    /// 人脸比对
    /// </summary>
    public class AFRFunction
    {
        /// <summary>
        /// 初始化引擎 
        /// </summary>
        /// <param name="AppId">用户申请 SDK 时获取的 id </param>
        /// <param name="SDKKey">用户申请 SDK 时获取的 id </param>
        /// <param name="pMem">分配给引擎使用的内存地址 </param>
        /// <param name="lMemSize">分配给引擎使用的内存大小 </param>
        /// <param name="phEngine">引擎 handle </param>
        /// <returns>
        /// 成功返回 MOK，否则返回失败 code。失败 codes如下所列: 
        ///     MERR_INVALID_PARAM 参数输入非法 
        ///     MERR_NO_MEMORY  内存不足 
        /// </returns>
        [DllImport(@".\assembly\libarcsoft_fsdk_face_recognition.dll", CallingConvention = CallingConvention.Cdecl)]
        public static extern int AFR_FSDK_InitialEngine(string AppId, string SDKKey, IntPtr pMem, int lMemSize, ref IntPtr phEngine);

        /// <summary>
        /// 获取脸部特征参数
        /// </summary>
        /// <param name="hEngine">引擎handle</param>
        /// <param name="pInputImage">输入的图像数据</param>
        /// <param name="pFaceRes">已检测到的脸部信息</param>
        /// <param name="pFaceModels">提取的脸部特征信息</param>
        /// <returns></returns>
        [DllImport(@".\assembly\libarcsoft_fsdk_face_recognition.dll", CallingConvention = CallingConvention.Cdecl)]
        public static extern int AFR_FSDK_ExtractFRFeature(IntPtr hEngine, IntPtr pInputImage, IntPtr pFaceRes, IntPtr pFaceModels);

        /// <summary>
        /// 脸部特征比较
        /// </summary>
        /// <param name="hEngine">引擎handle</param>
        /// <param name="reffeature">已有脸部特征信息</param>
        /// <param name="probefeature">被比较的脸部特征信息</param>
        /// <param name="pfSimilScore">脸部特征相似程度数值</param>
        /// <returns></returns>
        [DllImport(@".\assembly\libarcsoft_fsdk_face_recognition.dll", CallingConvention = CallingConvention.Cdecl)]
        public static extern int AFR_FSDK_FacePairMatching(IntPtr hEngine, IntPtr reffeature, IntPtr probefeature, ref float pfSimilScore);

        /// <summary>
        /// 销毁引擎，释放相应资源
        /// </summary>
        /// <param name="hEngine">引擎handle</param>
        /// <returns></returns>
        [DllImport(@".\assembly\libarcsoft_fsdk_face_recognition.dll", CallingConvention = CallingConvention.Cdecl)]
        public static extern int AFR_FSDK_UninitialEngine(IntPtr hEngine);

        /// <summary>
        /// 获取SDK版本信息参数
        /// </summary>
        /// <param name="hEngine">引擎handle</param>
        /// <returns></returns>
        [DllImport(@".\assembly\libarcsoft_fsdk_face_recognition.dll", CallingConvention = CallingConvention.Cdecl)]
        public static extern int AFR_FSDK_GetVersion(IntPtr hEngine);
    }
}
