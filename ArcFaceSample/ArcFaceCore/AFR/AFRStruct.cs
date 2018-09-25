using System;
using System.Collections.Generic;
using System.Linq;
using System.Runtime.InteropServices;
using System.Text;
using System.Threading.Tasks;

namespace ArcFaceCore
{
    /// <summary>
    /// 脸部信息  
    /// </summary>
    public struct AFR_FSDK_FACEINPUT
    {
        /// <summary>
        /// 脸部矩形框信息 
        /// </summary>
        public MRECT rcFace;
        /// <summary>
        /// 脸部旋转角度 
        /// </summary>
        public int lOrient;
    }

    /// <summary>
    /// 脸部特征信息
    /// </summary>
    public struct AFR_FSDK_FACEMODEL
    {
        /// <summary>
        /// 提取到的脸部特征 
        /// </summary>
        public IntPtr pbFeature;
        /// <summary>
        /// 特征信息长度 
        /// </summary>
        public int lFeatureSize;
    }

    /// <summary>
    /// 引擎版本信息. 
    /// </summary>
    public struct AFR_FSDK_VERSION
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
        /// 特征库版本号 
        /// </summary>
        public int lFeatureLevel;
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
    /// 基于逆时针的脸部方向枚举值 
    /// </summary>
    public enum AFR_FSDK_ORIENTCODE
    {
        /// <summary>
        /// 0 度 
        /// </summary>
        AFR_FSDK_FOC_0 = 0x1,
        /// <summary>
        /// 90 度 
        /// </summary>
        AFR_FSDK_FOC_90 = 0x2,
        /// <summary>
        /// 270 度 
        /// </summary>
        AFR_FSDK_FOC_270 = 0x3,
        /// <summary>
        /// 180 度 
        /// </summary>
        AFR_FSDK_FOC_180 = 0x4,
        /// <summary>
        /// 30 度 
        /// </summary>
        AFR_FSDK_FOC_30 = 0x5,
        /// <summary>
        /// 60 度 
        /// </summary>
        AFR_FSDK_FOC_60 = 0x6,
        /// <summary>
        /// 120 度 
        /// </summary>
        AFR_FSDK_FOC_120 = 0x7,
        /// <summary>
        /// 150 度 
        /// </summary>
        AFR_FSDK_FOC_150 = 0x8,
        /// <summary>
        /// 210 度 
        /// </summary>
        AFR_FSDK_FOC_210 = 0x9,
        /// <summary>
        /// 240 度 
        /// </summary>
        AFR_FSDK_FOC_240 = 0xa,
        /// <summary>
        /// 300 度 
        /// </summary>
        AFR_FSDK_FOC_300 = 0xb,
        /// <summary>
        /// 330 度 
        /// </summary>
        AFR_FSDK_FOC_330 = 0xc
    }
}
