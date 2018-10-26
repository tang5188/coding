using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Runtime.InteropServices;
using System.IO;
using System.Collections.Specialized;

namespace TiebaPost
{
    public class IniUtility
    {
        #region ini文件读取相关
        /// <summary>
        /// 概要：WritePrivateProfileString
        /// </summary>
        /// <param name="section">パラメータ：指定のSection名称</param>
        /// <param name="key"></param>
        /// <param name="val"></param>
        /// <param name="filePath"></param>
        /// <returns></returns>
        [DllImport("kernel32")]
        private static extern bool WritePrivateProfileString(string section, string key, string val, string filePath);

        /// <summary>
        /// 概要：GetPrivateProfileString
        /// </summary>
        /// <param name="section">パラメータ：指定のSection名称</param>
        /// <param name="key"></param>
        /// <param name="def"></param>
        /// <param name="retVal"></param>
        /// <param name="size"></param>
        /// <param name="filePath"></param>
        /// <returns></returns>
        [DllImport("kernel32", EntryPoint = "GetPrivateProfileString")]
        private static extern int GetPrivateProfileArrayToString(string section, string key, string def, byte[] retVal, int size, string filePath);

        /// <summary>
        /// 概要：GetPrivateProfileString
        /// </summary>
        /// <param name="section">パラメータ：指定のSection名称</param>
        /// <param name="key"></param>
        /// <param name="def"></param>
        /// <param name="retVal"></param>
        /// <param name="size"></param>
        /// <param name="filePath"></param>
        /// <returns></returns>
        [DllImport("kernel32")]
        private static extern int GetPrivateProfileString(string section, string key, string def, StringBuilder retVal, int size, string filePath);

        /// <summary>
        /// ReadString
        /// </summary>
        /// <param name="section">パラメータ：指定のSection名称</param>
        /// <param name="ident">パラメータ：指定のIdent名称</param>
        /// <param name="defvalue">读取值不存在时，默认返回值</param>
        /// <param name="fileName"></param>
        /// <returns></returns>
        public static string ReadString(string section, string ident, string defvalue, string fileName = "")
        {
            string iniFilePath = GetFilePath(fileName);

            if (!File.Exists(iniFilePath))
            {
                return defvalue;
            }

            StringBuilder strValue = new StringBuilder(2048);
            int bufLen = GetPrivateProfileString(section, ident, defvalue, strValue, strValue.Capacity, iniFilePath);

            string ret = strValue.ToString();
            if (string.IsNullOrEmpty(ret)) ret = defvalue;

            return ret;
        }

        /// <summary>
        /// ReadString
        /// </summary>
        /// <param name="section">パラメータ：指定のSection名称</param>
        /// <param name="ident">パラメータ：指定のIdent名称</param>
        /// <param name="defvalue">读取值不存在时，默认返回值</param>
        /// <param name="fileName"></param>
        /// <returns></returns>
        public static string ReadString(string section, string ident, int defvalue, string fileName = "")
        {
            return ReadString(section, ident, defvalue.ToString(), fileName);
        }

        /// <summary>
        /// WriteString
        /// </summary>
        /// <param name="section">パラメータ：指定のSection名称</param>
        /// <param name="ident">パラメータ：指定のIdent名称</param>
        /// <param name="value">写入的值</param>
        /// <param name="fileName">文件名</param>
        public static void WriteString(string section, string ident, string value, string fileName = "")
        {
            string iniFilePath = GetFilePath(fileName);

            if (!WritePrivateProfileString(section, ident, value, iniFilePath))
            {

            }
        }

        /// <summary>
        /// WriteString
        /// </summary>
        /// <param name="section">パラメータ：指定のSection名称</param>
        /// <param name="ident">パラメータ：指定のIdent名称</param>
        /// <param name="value">写入的值</param>
        /// <param name="fileName">文件名</param>
        public static void WriteString(string section, string ident, int value, string fileName = "")
        {
            WriteString(section, ident, value.ToString(), fileName);
        }

        /// <summary>
        /// 清除Section的设置内容
        /// </summary>
        /// <param name="Section"></param>
        /// <param name="fileName"></param>
        public static void EraseSection(string section, string fileName = "")
        {
            string iniFilePath = GetFilePath(fileName);
            if (!WritePrivateProfileString(section, null, null, iniFilePath))
            {
            }
        }

        /// <summary>
        /// 取得文件名称（含路径）
        /// </summary>
        /// <param name="fileName">文件名</param>
        /// <returns></returns>
        private static string GetFilePath(string fileName)
        {
            if (string.IsNullOrEmpty(fileName))
            {
                fileName = "config.ini";
            }

            if (!fileName.EndsWith(".ini", StringComparison.OrdinalIgnoreCase))
            {
                fileName += ".ini";
            }

            if (!fileName.Contains(":"))
            {
                fileName = Path.Combine(AppDomain.CurrentDomain.BaseDirectory, fileName);
            }

            if (!Directory.Exists(Path.GetDirectoryName(fileName)))
            {
                try
                {
                    Directory.CreateDirectory(Path.GetDirectoryName(fileName));
                }
                catch (Exception ex) { }
            }
            return fileName;
        }
        #endregion
    }
}
