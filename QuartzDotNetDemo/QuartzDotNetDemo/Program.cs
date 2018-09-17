using Quartz;
using Quartz.Impl;
using System;
using System.Collections.Generic;
using System.Collections.Specialized;
using System.Linq;
using System.Text;
using System.Threading.Tasks;

namespace QuartzDotNetDemo
{
    class Program
    {
        static void Main(string[] args)
        {
            IScheduler scheduler = StdSchedulerFactory.GetDefaultScheduler();
            scheduler.Start();

            IJobDetail job1 = JobBuilder.Create<HelloJob>()
                .WithIdentity("name1", "group1")
                .Build();
            ITrigger trigger1 = TriggerBuilder.Create()
                .WithIdentity("trigger1", "group1")
                .StartNow()
                .WithSimpleSchedule(x => x
                    .WithIntervalInSeconds(5)
                    .RepeatForever())
                .Build();
            scheduler.ScheduleJob(job1, trigger1);

            IJobDetail job2 = JobBuilder.Create<DumbJob>()
                .WithIdentity("name2", "group2")
                .UsingJobData("jobSays", "hello World!")
                .Build();
            ITrigger trigger2 = TriggerBuilder.Create()
                .WithIdentity("trigger2", "group2")
                .StartNow()
                .WithCronSchedule("/5 * * ? * *")
                .Build();

            scheduler.ScheduleJob(job2, trigger2);
        }
    }
}