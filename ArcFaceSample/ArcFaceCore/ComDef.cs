using System;
using System.Collections.Generic;
using System.Linq;
using System.Runtime.InteropServices;
using System.Text;
using System.Threading.Tasks;

namespace ArcFaceCore
{
    public class ArcFaceCore
    {
        public static int ASVL_PAF_RGB24_B8G8R8 = 0x201;
    }

    public struct MRECT
    {
        public int left;
        public int top;
        public int right;
        public int bottom;
    }

    [StructLayoutAttribute(LayoutKind.Sequential)]
    public struct ASVLOFFSCREEN
    {
        public int u32PixelArrayFormat;
        public int i32Width;
        public int i32Height;
        [MarshalAsAttribute(UnmanagedType.ByValArray, SizeConst = 4, ArraySubType = UnmanagedType.SysUInt)]
        public System.IntPtr[] ppu8Plane;
        [MarshalAsAttribute(UnmanagedType.ByValArray, SizeConst = 4, ArraySubType = UnmanagedType.I4)]
        public int[] pi32Pitch;
    }

}
