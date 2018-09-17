using Quartz;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace QuartzDotNetDemo
{
    public class DumbJob : IJob
    {
        public void Execute(IJobExecutionContext context)
        {
            JobDataMap dataMap = context.JobDetail.JobDataMap;
            string content = dataMap.GetString("jobSays");
            Console.WriteLine(DateTime.Now.ToString("yyyy-MM-dd HH:mm:ss.ffffff") + "作业执行，jobSays:" + content);
        }
    }
}
