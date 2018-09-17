using Quartz;
using System;
using System.Collections.Generic;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace QuartzDotNetDemo
{
    public class HelloJob : IJob
    {
        public void Execute(IJobExecutionContext context)
        {
            Console.WriteLine(DateTime.Now.ToString("yyyy-MM-dd HH:mm:ss.ffffff") + "作业执行！");
        }
    }
}
