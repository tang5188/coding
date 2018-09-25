using System;
using System.Collections.Generic;
using System.Linq;
using System.Runtime.InteropServices;
using System.Text;
using System.Threading.Tasks;

namespace ArcFaceCore
{
    /// <summary>
    /// 检测到的脸部信息 
    /// </summary>
    public struct AFD_FSDK_FACERES
    {
        /// <summary>
        /// 人脸个数 
        /// </summary>
        public int nFace;
        /// <summary>
        /// 人脸矩形框信息 
        /// </summary>
        public IntPtr rcFace;
        /// <summary>
        /// 人脸角度信息 
        /// </summary>
        public IntPtr lfaceOrient;
    }

    /// <summary>
    /// SDK版本信息 
    /// </summary>
    public struct AFD_FSDK_VERSION
    {
        /// <summary>
        /// 代码库版本号 
        /// </summary>
        public int lCodebase;
        /// <summary>
        /// 主版本号 
        /// </summary>
        public int lMajor;
        /// <summary>
        /// 次版本号 
        /// </summary>
        public int lMinor;
        /// <summary>
        /// 编译版本号，递增 
        /// </summary>
        public int lBuild;
        /// <summary>
        /// 字符串形式的版本号 
        /// </summary>
        public IntPtr Version;
        /// <summary>
        /// 编译时间 
        /// </summary>
        public IntPtr BuildDate;
        /// <summary>
        /// copyright 
        /// </summary>
        public IntPtr CopyRight;
    }

    /// <summary>
    /// 定义脸部检测角度的优先级
    /// </summary>
    public enum AFD_FSDK_OrientPriority
    {
        /// <summary>
        /// 检测 0 度方向 
        /// </summary>
        AFD_FSDK_OPF_0_ONLY = 0x1,
        /// <summary>
        /// 检测 90 度方向
        /// </summary>
        AFD_FSDK_OPF_90_ONLY = 0x2,
        /// <summary>
        /// 检测 270 度方向
        /// </summary>
        AFD_FSDK_OPF_270_ONLY = 0x3,
        /// <summary>
        /// 检测 180 度方向
        /// </summary>
        AFD_FSDK_OPF_180_ONLY = 0x4,
        /// <summary>
        /// 检测 0， 90， 180， 270 四个方向,0 度更优先
        /// </summary>
        AFD_FSDK_OPF_0_HIGHER_EXT = 0x5
    }

    /// <summary>
    /// 定义检测结果中的人脸角度 
    /// </summary>
    public enum AFD_FSDK_OrientCode
    {
        /// <summary>
        /// 0 度 
        /// </summary>
        AFD_FSDK_FOC_0 = 0x1,
        /// <summary>
        /// 90 度 
        /// </summary>
        AFD_FSDK_FOC_90 = 0x2,
        /// <summary>
        /// 270 度 
        /// </summary>
        AFD_FSDK_FOC_270 = 0x3,
        /// <summary>
        /// 180 度 
        /// </summary>
        AFD_FSDK_FOC_180 = 0x4,
        /// <summary>
        /// 30 度 
        /// </summary>
        AFD_FSDK_FOC_30 = 0x5,
        /// <summary>
        /// 60 度 
        /// </summary>
        AFD_FSDK_FOC_60 = 0x6,
        /// <summary>
        /// 120 度 
        /// </summary>
        AFD_FSDK_FOC_120 = 0x7,
        /// <summary>
        /// 150 度 
        /// </summary>
        AFD_FSDK_FOC_150 = 0x8,
        /// <summary>
        /// 210 度 
        /// </summary>
        AFD_FSDK_FOC_210 = 0x9,
        /// <summary>
        /// 240 度 
        /// </summary>
        AFD_FSDK_FOC_240 = 0xa,
        /// <summary>
        /// 300 度 
        /// </summary>
        AFD_FSDK_FOC_300 = 0xb,
        /// <summary>
        /// 330 度 
        /// </summary>
        AFD_FSDK_FOC_330 = 0xc
    }
}
