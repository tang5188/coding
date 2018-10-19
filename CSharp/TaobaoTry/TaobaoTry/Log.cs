using log4net;
using log4net.Config;
using log4net.Core;
using System;
using System.Collections.Concurrent;
using System.Collections.Generic;
using System.Configuration;
using System.Diagnostics;
using System.IO;
using System.Linq;
using System.Text;
using System.Threading;
using System.Windows.Forms;

namespace TaobaoTry
{
    public class Log
    {
        public static bool Log2OutPut = true;
        private static ILog _logger = null;
        private static ConcurrentDictionary<string, ILog> _dicLogger = null;

        static Log()
        {
            var config = new FileInfo(AppDomain.CurrentDomain.BaseDirectory + Path.GetFileNameWithoutExtension(Application.ExecutablePath) + ".log4net.config");
            XmlConfigurator.ConfigureAndWatch(config);

            _logger = LogManager.GetLogger(typeof(Log));

            _dicLogger = new ConcurrentDictionary<string, ILog>();
        }

        public static void Debug(string format, params object[] args)
        {
            Debug(null, format, args);
        }

        public static void Debug(Exception ex, string format = null, params object[] args)
        {
            string message = GetMessage(format, args);
            Log2Console(message, LogLevel.Debug, ex);
            _logger.Debug(message, ex);
        }

        public static void Info(string format, params object[] args)
        {
            Info(LogType.Normal, string.Empty, null, format, args);
        }

        public static void Info(LogType logType, string logName, string format, params object[] args)
        {
            Info(logType, logName, null, format, args);
        }

        public static void Info(Exception ex, string format = null, params object[] args)
        {
            Info(LogType.Normal, string.Empty, ex, format, args);
        }

        public static void Info(LogType logType, string logName, Exception ex, string format = null, params object[] args)
        {
            string message = GetMessage(format, args);
            if (logType == LogType.Normal ||
                string.IsNullOrWhiteSpace(logName))
            {
                _logger.Info(message, ex);
            }
            else
            {
                ILog log = GetLoggerByName(logName);
                log.Info(message);
            }
            Log2Console(message, LogLevel.Info, ex);
        }

        public static void Warn(string format, params object[] args)
        {
            Warn(null, format, args);
        }

        public static void Warn(Exception ex, string format = null, params object[] args)
        {
            string message = GetMessage(format, args);
            Log2Console(message, LogLevel.Warn, ex);
            _logger.Warn(message, ex);
        }

        public static void Error(string format, params object[] args)
        {
            Error(null, format, args);
        }

        public static void Error(Exception ex, string format = null, params object[] args)
        {
            string message = GetMessage(format, args);
            Log2Console(message, LogLevel.Error, ex);
            _logger.Error(message, ex);
        }

        public static string GetMessage(string format, params object[] args)
        {
            if (format == null) return string.Empty;
            try
            {
                return string.Format(format, args);
            }
            catch (Exception ex)
            {
                return format;
            }
        }

        public static string GetErrMsg(Exception e)
        {
            if (e == null)
            {
                return String.Empty;
            }
            return "\n" + e.ToString() + GetErrMsg(e.InnerException);
        }

        private static ILog GetLoggerByName(string logName)
        {
            ILog log;
            if (_dicLogger.TryGetValue(logName, out log))
            {
                return log;
            }
            else
            {
                log = LogManager.GetLogger(logName);
                _dicLogger.TryAdd(logName, log);
            }
            if (log == null) log = _logger;
            return log;
        }

        public static void Log2Console(string msg, LogLevel level = LogLevel.Info, Exception ex = null)
        {
            if (Log2OutPut)
            {
                Console.WriteLine(string.Format("{0} [{1}][{2}]{3}{4}", DateTime.Now.ToString("yyyy/MM/dd HH:mm:ss.fffffff"), level, Thread.CurrentThread.ManagedThreadId, msg, GetErrMsg(ex)));
            }
        }

        public static bool IsDebugEnabled { get { return _logger.IsDebugEnabled; } }
        public static bool IsErrorEnabled { get { return _logger.IsErrorEnabled; } }
        public static bool IsFatalEnabled { get { return _logger.IsFatalEnabled; } }
        public static bool IsInfoEnabled { get { return _logger.IsInfoEnabled; } }
        public static bool IsWarnEnabled { get { return _logger.IsWarnEnabled; } }
    }

    /// <summary>
    /// Log等级
    /// </summary>
    public enum LogLevel
    {
        Debug,
        Info,
        Warn,
        Error,
        Fatal
    }

    /// <summary>
    /// Log类型
    /// </summary>
    public enum LogType
    {
        Normal = 0,
        Named = 1,
    }
}
