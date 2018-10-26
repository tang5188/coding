using System;
using System.Collections.Generic;
using System.ComponentModel;
using System.Data;
using System.Drawing;
using System.IO;
using System.IO.Compression;
using System.Linq;
using System.Net;
using System.Text;
using System.Threading;
using System.Threading.Tasks;
using System.Windows.Forms;

namespace TiebaPost
{
    public partial class FrmTiebaPost : Form
    {
        /// <summary>
        /// 文件中存储的回帖内容
        /// </summary>
        private List<string> lstPosts = new List<string>();

        public FrmTiebaPost()
        {
            InitializeComponent();
            this.txtCookie.Text = IniUtility.ReadString(this.GetType().Name, this.txtCookie.Name, string.Empty);
            this.txtBody.Text = IniUtility.ReadString(this.GetType().Name, this.txtBody.Name, string.Empty);
            this.cmbInterval.Text = IniUtility.ReadString(this.GetType().Name, this.cmbInterval.Name, 5);

            if (File.Exists("posts.txt"))
            {
                using (StreamReader sr = new StreamReader("posts.txt", Encoding.UTF8))
                {
                    string line = null;
                    do
                    {
                        line = sr.ReadLine();
                        if (string.IsNullOrWhiteSpace(line)) continue;
                        if (line.Contains("BIDUPSID=") || line.Contains("BAIDUID="))
                        {
                            this.txtCookie.Text = line;
                            continue;
                        }
                        else if (line.StartsWith("--ie="))
                        {
                            this.txtBody.Text = line.Substring(2);
                            continue;
                        }
                        lstPosts.Add(line);
                    } while (line != null);
                }
                this.lblPostsCount.Text = lstPosts.Count.ToString();
            }
            this.Text += " - " + Application.StartupPath;
        }

        private bool isPosting = false;
        private bool isPost2Stop = false;

        /// <summary>
        /// 开始发帖的线程
        /// </summary>
        /// <param name="sender"></param>
        /// <param name="e"></param>
        private void btnStart_Click(object sender, EventArgs e)
        {
            if (this.isPosting)
            {
                MessageBox.Show("正在工作中");
                return;
            }

            this.isPost2Stop = false;
            this._cookie = this.txtCookie.Text.Trim();
            this._body = this.txtBody.Text.Trim();
            this._interval = int.Parse(this.cmbInterval.Text);
            this._okCount = 0;
            this._ngCount = 0;
            this._erCount = 0;
            if (this._interval <= 1 || this._interval >= 61 ||
                string.IsNullOrEmpty(this._cookie) ||
                string.IsNullOrEmpty(this._body))
            {
                MessageBox.Show("请检查参数");
                return;
            }

            Thread thread = new Thread(new ThreadStart(DoPost));
            thread.IsBackground = true;
            thread.Start();
        }

        /// <summary>
        /// 停止请求
        /// </summary>
        /// <param name="sender"></param>
        /// <param name="e"></param>
        private void btnStop_Click(object sender, EventArgs e)
        {
            this.isPost2Stop = true;
        }

        private string _cookie = string.Empty;
        private string _body = string.Empty;
        private int _interval = 5;

        private int _okCount = 0;
        private int _ngCount = 0;
        private int _erCount = 0;
        private int _allCount
        {
            get { return _okCount + _ngCount + _erCount; }
        }

        /// <summary>
        /// 执行发帖
        /// </summary>
        private void DoPost()
        {
            this.isPosting = true;
            this.Invoke(new EventHandler(delegate
            {
                this.btnStop.Enabled = true;
                this.lblStartTime.Text = DateTime.Now.ToString("yyyy-MM-dd HH:mm:ss");
                this.lblStopTime.Text = string.Empty;
                this.txtCookie.ReadOnly = true;
                this.txtBody.ReadOnly = true;
                this.cmbInterval.Enabled = false;
                this.lblOK.Text = this._okCount.ToString();
                this.lblNG.Text = this._ngCount.ToString();
                this.lblER.Text = this._erCount.ToString();
            }));

            while (!this.isPost2Stop)
            {
                string strBody = this._body;
                int index = _allCount % (this.lstPosts.Count + 1);
                if (index != 0) strBody = this.lstPosts[index - 1];

                int retFlag = 0;     //0成功 1失败 2异常
                string retStr = string.Empty;       //回复的字符串
                HttpWebRequest request = null;
                HttpWebResponse response = null;
                try
                {
                    request = (HttpWebRequest)HttpWebRequest.Create("http://tieba.baidu.com/f/commit/post/add");
                    request.Method = "POST";
                    request.Accept = "application/json, text/javascript, */*; q=0.01";
                    request.Headers.Add("Accept-Encoding", "gzip, deflate");
                    request.Headers.Add("Accept-Language", "zh-CN,zh;q=0.9");
                    request.ContentType = "application/x-www-form-urlencoded; charset=UTF-8";
                    request.Headers.Add("Cookie", this._cookie);
                    request.Host = "tieba.baidu.com";
                    request.Headers.Add("Origin", "http://tieba.baidu.com");
                    request.Referer = "http://tieba.baidu.com/p/5922546060?pn=633";
                    request.UserAgent = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/68.0.3440.106 Safari/537.36";
                    //Body
                    byte[] body = Encoding.UTF8.GetBytes(strBody);
                    request.ContentLength = body.Length;
                    using (Stream s = request.GetRequestStream())
                    {
                        s.Write(body, 0, body.Length);
                    }

                    response = (HttpWebResponse)request.GetResponse();
                    retStr = GetResponseString(response);

                    TiebaPostResult postResult = SerializeUtility.JsonDeserizlize2<TiebaPostResult>(retStr);
                    if (postResult.err_code == 0)
                    {
                        retFlag = 0;
                        retStr = SerializeUtility.JsonSerialize2(postResult);
                    }
                    else
                    {
                        retFlag = 1;
                    }
                    Log.Info("请求结果：" + retStr);
                }
                catch (Exception ex)
                {
                    Log.Error(ex);
                    retStr += "\r\n" + ex.Message + ex.StackTrace;
                    retFlag = 2;
                }
                finally
                {
                    if (request != null)
                    {
                        try
                        {
                            request.Abort();
                            request = null;
                        }
                        catch { }
                    }
                    if (response != null)
                    {
                        try
                        {
                            response.Close();
                            response = null;
                        }
                        catch { }
                    }
                }
                this.UpdateResult(retFlag, retStr);

                int turn = 0;
                while (!this.isPost2Stop && turn++ <= this._interval)
                {
                    Thread.Sleep(500);
                }
            }
            this.isPosting = false;
            this.Invoke(new EventHandler(delegate
            {
                this.btnStop.Enabled = false;
                this.lblStopTime.Text = DateTime.Now.ToString("yyyy-MM-dd HH:mm:ss");
                this.txtCookie.ReadOnly = false;
                this.txtBody.ReadOnly = false;
                this.cmbInterval.Enabled = true;
            }));
        }

        /// <summary>
        /// 结果刷新
        /// </summary>
        private void UpdateResult(int retFlag, string retStr)
        {
            if (this.InvokeRequired)
            {
                this.Invoke(new Action<int, string>(this.UpdateResult), retFlag, retStr);
            }
            else
            {
                if (retFlag == 0)
                {
                    this._okCount++;
                    this.lblOK.ForeColor = Color.Blue;
                    this.lblNG.ForeColor = Color.Black;
                    this.lblER.ForeColor = Color.Black;
                }
                else if (retFlag == 1)
                {
                    this._ngCount++;
                    this.lblOK.ForeColor = Color.Black;
                    this.lblNG.ForeColor = Color.Blue;
                    this.lblER.ForeColor = Color.Black;
                }
                else if (retFlag == 2)
                {
                    this._erCount++;
                    this.lblOK.ForeColor = Color.Black;
                    this.lblNG.ForeColor = Color.Black;
                    this.lblER.ForeColor = Color.Blue;
                }

                this.lblOK.Text = this._okCount.ToString();
                this.lblNG.Text = this._ngCount.ToString();
                this.lblER.Text = this._erCount.ToString();
                this.txtResult.Text = retStr;
            }
        }

        /// <summary>
        /// 取得请求返参字符串
        /// </summary>
        /// <returns></returns>
        public string GetResponseString(HttpWebResponse response)
        {
            //入参参数
            string inputStr = string.Empty;

            string content_Encoding = response.Headers["content-encoding"];
            //经过gzip压缩
            if (content_Encoding == "gzip")
            {
                using (GZipStream gs = new GZipStream(response.GetResponseStream(), CompressionMode.Decompress, true))
                {
                    int length = 0;
                    byte[] buffer = new byte[1024];

                    using (MemoryStream ms = new MemoryStream())
                    {
                        while ((length = gs.Read(buffer, 0, buffer.Length)) != 0)
                        {
                            ms.Write(buffer, 0, length);
                        }
                        inputStr = Encoding.UTF8.GetString(ms.ToArray());
                    }
                }
            }
            else
            {
                using (StreamReader sr = new StreamReader(response.GetResponseStream()))
                {
                    inputStr = sr.ReadToEnd();
                }
            }
            return inputStr;
        }

        /// <summary>
        /// 
        /// </summary>
        /// <param name="sender"></param>
        /// <param name="e"></param>
        private void FrmTiebaPost_FormClosed(object sender, FormClosedEventArgs e)
        {
            IniUtility.WriteString(this.GetType().Name, this.txtCookie.Name, this.txtCookie.Text);
            IniUtility.WriteString(this.GetType().Name, this.txtBody.Name, this.txtBody.Text);
            IniUtility.WriteString(this.GetType().Name, this.cmbInterval.Name, this.cmbInterval.Text);
        }

        /// <summary>
        /// 
        /// </summary>
        /// <param name="sender"></param>
        /// <param name="e"></param>
        private void FrmTiebaPost_FormClosing(object sender, FormClosingEventArgs e)
        {
            if (this.isPosting)
            {
                e.Cancel = true;
                this.Hide();
            }
        }

        /// <summary>
        /// 
        /// </summary>
        /// <param name="sender"></param>
        /// <param name="e"></param>
        private void notifyIcon_MouseDoubleClick(object sender, MouseEventArgs e)
        {
            this.Show();
        }
    }

    public class TiebaPostResult
    {
        public int no;
        public int err_code;
        public string error;
        public TiebaPostData data;
    }

    public class TiebaPostData
    {
        public string autoMsg;
        public long fid;
        public string fname;
        public long tid;
        public int is_login;
        public string content;
        public string access_state;
        public int is_post_visible;
        public TiebaPostVcode vcode;
    }

    public class TiebaPostVcode
    {
        public int need_vcode;
        public string str_reason;
        public string captcha_vcode_str;
        public int captcha_code_type;
        public int userstatevcode;
    }
}
