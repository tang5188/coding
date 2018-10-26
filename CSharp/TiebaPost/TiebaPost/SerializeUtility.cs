using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.IO;
using System.Xml;
using System.Xml.Serialization;
using Newtonsoft.Json;

namespace TiebaPost
{
    public class SerializeUtility
    {
        public static string JsonSerialize2<T>(T t)
            where T : class
        {
            string json = JsonConvert.SerializeObject(t);
            return json;
        }

        public static T JsonDeserizlize2<T>(string jsonStr, params JsonConverter[] converters)
             where T : class
        {
            try
            {
                T t = JsonConvert.DeserializeObject<T>(jsonStr, converters) as T;
                return t;
            }
            catch
            {
                return default(T);
            }
        }
    }
}
