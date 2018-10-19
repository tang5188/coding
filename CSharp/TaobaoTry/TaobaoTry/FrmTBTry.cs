using Gma.QrCodeNet.Encoding;
using Gma.QrCodeNet.Encoding.Windows.Render;
using Newtonsoft.Json;
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
using System.Web;
using System.Windows.Forms;

namespace TaobaoTry
{
    /// <summary>
    /// 淘宝试用
    /// </summary>
    public partial class FrmTBTry : Form
    {
        private string apiUrl = "https://try.taobao.com/api3/call?what={0}&page={1}&pageSize={2}&api={3}";
        private string what = "show";
        private string curPage = "1";
        private string pageSize = "100";
        private string api = HttpUtility.UrlEncode("x/search");
        private string itemUrl = "https://try.taobao.com/item.htm?id={0}";
        private string qrCodeUrl = "https://h5.m.taobao.com/try/detail.htm?tid={0}";

        /// <summary>是否正在请求数据中</summary>
        private bool isRequesting = false;

        public FrmTBTry()
        {
            InitializeComponent();
        }

        protected override void OnShown(EventArgs e)
        {
            base.OnShown(e);
            this.cmbCurPage.SelectedIndex = 0;
            this.cmbPageSize.Text = "100";
        }

        #region 画面控件事件
        /// <summary>
        /// 加载试用数据
        /// </summary>
        /// <param name="sender"></param>
        /// <param name="e"></param>
        private void btnLoad_Click(object sender, EventArgs e)
        {
            if (this.isRequesting)
            {
                MessageBox.Show("请求中，请稍候……");
                return;
            }

            Thread t = new Thread(new ThreadStart(this.LoadTaobaoTryData));
            t.IsBackground = true;
            t.Start();
        }

        /// <summary>
        /// 加载全部试用数据
        /// </summary>
        /// <param name="sender"></param>
        /// <param name="e"></param>
        private void btnLoadAll_Click(object sender, EventArgs e)
        {
            if (this.isRequesting)
            {
                MessageBox.Show("请求中，请稍候……");
                return;
            }

            this.btnLoadStop.Enabled = true;
            this.btnLoadStop.BackColor = Color.Red;
            this.bContinueLoadAll = true;
            Thread t = new Thread(new ThreadStart(this.LoadAllTaobaoTryData));
            t.IsBackground = true;
            t.Start();
        }

        /// <summary>
        /// 终止Load
        /// </summary>
        /// <param name="sender"></param>
        /// <param name="e"></param>
        private void btnLoadStop_Click(object sender, EventArgs e)
        {
            this.bContinueLoadAll = false;
        }

        /// <summary>
        /// 当前页选择变化
        /// </summary>
        /// <param name="sender"></param>
        /// <param name="e"></param>
        private void cmbCurPage_SelectedIndexChanged(object sender, EventArgs e)
        {
            this.curPage = this.cmbCurPage.Text;
        }

        /// <summary>
        /// 每页显示数选择变化
        /// </summary>
        /// <param name="sender"></param>
        /// <param name="e"></param>
        private void cmbPageSize_SelectedIndexChanged(object sender, EventArgs e)
        {
            this.pageSize = this.cmbPageSize.Text;
        }

        /// <summary>
        /// cell双击，打开对应的画面
        /// </summary>
        /// <param name="sender"></param>
        /// <param name="e"></param>
        private void gridResult_CellDoubleClick(object sender, DataGridViewCellEventArgs e)
        {
            if (e.ColumnIndex < 0 || e.RowIndex < 0) return;
            string id = this.gridResult.Rows[e.RowIndex].Cells["id"].Value as string;
            if (string.IsNullOrWhiteSpace(id)) return;

            string url = string.Format(itemUrl, id);
            System.Diagnostics.Process.Start(url);
        }

        /// <summary>
        /// 选中行发生变化
        /// </summary>
        /// <param name="sender"></param>
        /// <param name="e"></param>
        private void gridResult_SelectionChanged(object sender, EventArgs e)
        {
            if (this.gridResult.SelectedRows == null ||
                this.gridResult.SelectedRows.Count == 0) return;

            string picUrl = this.gridResult.SelectedRows[0].Cells["picture"].Value as string;
            string id = this.gridResult.SelectedRows[0].Cells["id"].Value as string;

            this.DispPicture(picUrl);
            this.DispQRCode(id);
        }

        private StringFormat sf = new StringFormat(StringFormatFlags.NoWrap)
        {
            Alignment = StringAlignment.Far,
            LineAlignment = StringAlignment.Far,
        };
        /// <summary>
        /// 行序号描画
        /// </summary>
        /// <param name="sender"></param>
        /// <param name="e"></param>
        private void gridResult_RowPostPaint(object sender, DataGridViewRowPostPaintEventArgs e)
        {
            RectangleF rf = new RectangleF(e.RowBounds.Location.X + 6, e.RowBounds.Location.Y, this.gridResult.RowHeadersWidth - 8, e.RowBounds.Height - 2);
            e.Graphics.DrawString((e.RowIndex + 1).ToString(), this.gridResult.DefaultCellStyle.Font, Brushes.Red, rf, sf);
        }

        /// <summary>
        /// 商品图片点击
        /// </summary>
        /// <param name="sender"></param>
        /// <param name="e"></param>
        private void pbPicture_Click(object sender, EventArgs e)
        {
            if (this.gridResult.SelectedRows == null ||
               this.gridResult.SelectedRows.Count == 0) return;

            string id = this.gridResult.SelectedRows[0].Cells["id"].Value as string;
            if (string.IsNullOrWhiteSpace(id)) return;

            string url = string.Format(itemUrl, id);
            System.Diagnostics.Process.Start(url);
        }

        /// <summary>
        /// 过滤日期
        /// </summary>
        /// <param name="sender"></param>
        /// <param name="e"></param>
        private void chkToday_CheckedChanged(object sender, EventArgs e)
        {
            this.DoFilter();
        }

        /// <summary>
        /// 文字过滤
        /// </summary>
        /// <param name="sender"></param>
        /// <param name="e"></param>
        private void txtFilter_TextChanged(object sender, EventArgs e)
        {
            this.DoFilter();
        }

        /// <summary>
        /// 光标跳转
        /// </summary>
        /// <param name="sender"></param>
        /// <param name="e"></param>
        private void txtName_KeyDown(object sender, KeyEventArgs e)
        {
            if (e.KeyCode == Keys.Enter)
            {
                this.gridResult.Focus();
            }
        }
        #endregion

        #region 加载单次数据
        /// <summary>
        /// 加载试用数据
        /// </summary>
        /// <param name="curPage"></param>
        /// <param name="pageSize"></param>
        private void LoadTaobaoTryData()
        {
            this.isRequesting = true;
            try
            {
                string result = this.MakeRequest(curPage, pageSize);
                this.DispResult(result);
            }
            finally
            {
                this.isRequesting = false;
            }
        }

        /// <summary>
        /// 请求结果数据的处理
        /// </summary>
        /// <param name="result"></param>
        private void DispResult(string result)
        {
            if (this.InvokeRequired)
            {
                this.Invoke(new Action<string>(this.DispResult), result);
            }
            else
            {
                //解析json
                TBTryResponse tbResponse = SerializeUtility.JsonDeserizlize2<TBTryResponse>(result);
                if (tbResponse == null ||
                    tbResponse.result == null ||
                    tbResponse.success == false ||
                    tbResponse.result.items == null ||
                    tbResponse.result.items.Count == 0 ||
                    tbResponse.result.paging == null)
                {
                    this.lblCurPage.Text = "0";
                    this.lblPageSize.Text = "0";
                    this.ClearGrid();
                    if (tbResponse != null &&
                        tbResponse.success == false &&
                        !string.IsNullOrWhiteSpace(tbResponse.error))
                    {
                        MessageBox.Show("请求失败：\r\n" + tbResponse.error);
                    }
                    return;
                }
                else
                {
                    this.lblCurPage.Text = string.Format("{0}/{1}", tbResponse.result.paging.page, tbResponse.result.paging.pages);
                    this.lblPageSize.Text = tbResponse.result.items.Count.ToString();
                    //重新显示页码
                    this.cmbCurPage.Items.Clear();
                    for (int i = 0; i < tbResponse.result.paging.pages; i++)
                    {
                        this.cmbCurPage.Items.Add(i + 1);
                    }
                    this.cmbCurPage.Text = tbResponse.result.paging.page.ToString();
                    //显示数据
                    this.ClearGrid();
                    DataView view = this.MakeViewData(tbResponse.result.items);
                    this.gridResult.DataSource = view;
                    this.DoFilter();
                }
            }
        }
        #endregion

        #region 加载全部数据的处理
        private bool bContinueLoadAll = true;
        /// <summary>
        /// 加载全部试用数据
        /// </summary>
        private void LoadAllTaobaoTryData()
        {
            this.isRequesting = true;
            List<TBTryItem> items = new List<TBTryItem>();
            try
            {
                string pSize = "100";
                int pCount = 1;
                for (int i = 0; (i < pCount) && this.bContinueLoadAll; i++)
                {
                    string result = this.MakeRequest((i + 1).ToString(), pSize);
                    TBTryResponse tbResponse = SerializeUtility.JsonDeserizlize2<TBTryResponse>(result);
                    if (tbResponse == null ||
                       tbResponse.result == null ||
                       tbResponse.success == false ||
                       tbResponse.result.items == null ||
                       tbResponse.result.items.Count == 0 ||
                       tbResponse.result.paging == null)
                    {
                        break;
                    }
                    else
                    {
                        //总页数
                        pCount = tbResponse.result.paging.pages;
                        //将数据添加进结果集
                        TBTryItemComparer comparer = new TBTryItemComparer();
                        foreach (var item in tbResponse.result.items)
                        {
                            int index = items.BinarySearch(item, comparer);
                            if (index >= 0) continue;
                            items.Insert(~index, item);
                        }
                    }
                    this.RefershResult(i + 1, pCount, items.Count);
                    Thread.Sleep(200);
                }
                this.DispAllResult(items);
            }
            finally
            {
                this.isRequesting = false;
            }
        }

        /// <summary>
        /// 全部加载的数据表示
        /// </summary>
        /// <param name="items"></param>
        private void DispAllResult(List<TBTryItem> items)
        {
            if (this.InvokeRequired)
            {
                this.Invoke(new Action<List<TBTryItem>>(this.DispAllResult), items);
            }
            else
            {
                if (items == null ||
                    items.Count == 0) return;

                this.lblCurPage.Text = "1";
                this.lblPageSize.Text = items.Count.ToString();
                this.lblStatus.Text = "";
                this.btnLoadStop.Enabled = false;
                this.btnLoadStop.BackColor = this.btnLoadAll.BackColor;
                //展示数据
                DataView view = this.MakeViewData(items);
                this.gridResult.DataSource = view;
                this.DoFilter();
            }
        }

        /// <summary>
        /// 刷新进度
        /// </summary>
        /// <param name="pIndex"></param>
        /// <param name="pCount"></param>
        /// <param name="rCount"></param>
        private void RefershResult(int pIndex, int pCount, int rCount)
        {
            if (this.InvokeRequired)
            {
                this.Invoke(new Action<int, int, int>(this.RefershResult), pIndex, pCount, rCount);
            }
            else
            {
                this.lblStatus.Text = string.Format("当前页:{0}, 总页数:{1}, 已下载项目数:{2}", pIndex, pCount, rCount);
            }
        }
        #endregion

        #region Request、Response等处理
        /// <summary>
        /// 请求数据
        /// </summary>
        /// <param name="pageIndex"></param>
        /// <param name="pageSize"></param>
        /// <returns></returns>
        private string MakeRequest(string pageIndex, string pageSize)
        {
            string curUrl = string.Format(apiUrl, what, pageIndex, pageSize, api);
            Log.Info("请求：{0}", curUrl);
            string token = this.txtTBToken.Text.Trim();
            string cookie = string.Format("_tb_token_={0};", token);

            string result = string.Empty;
            HttpWebRequest request = null;
            HttpWebResponse response = null;
            try
            {
                request = (HttpWebRequest)HttpWebRequest.Create(curUrl);
                request.ServicePoint.ConnectionLimit = 10;
                request.KeepAlive = false;
                request.Method = "POST";
                request.Timeout = 10000;
                //Header
                request.Headers.Add("Postman-Token", Guid.NewGuid().ToString());
                request.Accept = "application/json, text/javascript, */*; q=0.01";
                request.Headers.Add("accept-encoding", "gzip, deflate, br");
                request.ContentType = "application/x-www-form-urlencoded; charset=UTF-8";
                request.Headers.Add("cookie", cookie);
                request.Headers.Add("origin", "https://try.taobao.com");
                request.Referer = "https://try.taobao.com/";
                request.Headers.Add("x-csrf-token", token);
                //Body
                byte[] body = Encoding.UTF8.GetBytes("status=1");
                request.ContentLength = body.Length;
                using (Stream s = request.GetRequestStream())
                {
                    s.Write(body, 0, body.Length);
                }

                response = (HttpWebResponse)request.GetResponse();
                result = GetResponseString(response);
            }
            catch (Exception ex)
            {
                Log.Error(ex);
                result = string.Empty;
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
            Log.Info("请求结果:{0}", result);
            return result;
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
        #endregion

        #region 其他处理
        /// <summary>
        /// 将数据转化为表示到画面上的数据
        /// </summary>
        /// <param name="items"></param>
        /// <returns></returns>
        private DataView MakeViewData(List<TBTryItem> items)
        {
            DataTable table = new DataTable();
            table.Columns.Add("title", typeof(string));
            table.Columns.Add("id", typeof(string));
            table.Columns.Add("picture", typeof(string));
            table.Columns.Add("price", typeof(decimal));
            table.Columns.Add("leftcount", typeof(int));
            table.Columns.Add("requestnum", typeof(int));
            table.Columns.Add("endtime", typeof(DateTime));
            table.Columns.Add("percent", typeof(decimal));
            foreach (var item in items)
            {
                DataRow row = table.NewRow();
                row["title"] = item.title + (item.isApplied ? "[已申请]" : "");
                row["id"] = item.id;
                row["picture"] = item.pic;
                row["price"] = item.price;
                row["leftcount"] = item.totalNum - item.acceptNum;
                row["requestnum"] = item.requestNum;

                DateTime timeBegin = new DateTime(1970, 1, 1);
                DateTime endTime = new DateTime(timeBegin.Ticks + item.endTime * 10000).AddHours(8);
                row["endtime"] = endTime;

                decimal percent = 100m;
                if (item.requestNum != 0)
                {
                    percent = decimal.Round(1000 * (decimal)(item.totalNum - item.acceptNum) / (decimal)item.requestNum, 5);
                }
                row["percent"] = percent;
                table.Rows.Add(row);
            }
            DataView view = new DataView(table);
            return view;
        }

        /// <summary>
        /// 过滤
        /// </summary>
        private void DoFilter()
        {
            DataView view = this.gridResult.DataSource as DataView;
            if (view == null) return;

            StringBuilder filter = new StringBuilder("1=1");
            if (this.chkToday.Checked)
            {
                filter.AppendFormat(" and endtime<'{0}'", DateTime.Today.AddDays(1).ToString("yyyy-MM-dd"));
            }

            string name = this.txtName.Text.Trim().Replace("'", "");
            if (!string.IsNullOrWhiteSpace(name))
            {
                filter.AppendFormat(" and title like '%{0}%'", name);
            }
            view.RowFilter = filter.ToString();
        }

        /// <summary>
        /// 清空grid数据
        /// </summary>
        private void ClearGrid()
        {
            DataView view = this.gridResult.DataSource as DataView;
            if (view == null)
            { }
            else
            {
                view.RowFilter = "1<>1";
            }
        }

        /// <summary>
        /// 商品图片预览
        /// </summary>
        /// <param name="picUrl"></param>
        private void DispPicture(string picUrl)
        {
            if (string.IsNullOrWhiteSpace(picUrl)) return;
            if (picUrl.Equals(this.pbPicture.Tag)) return;

            if (this.pbPicture.Image != null)
            {
                this.pbPicture.Image.Dispose();
                this.pbPicture.Image = null;
            }
            if (picUrl.StartsWith("//")) picUrl = picUrl.Insert(0, "http:");
            try
            {
                this.pbPicture.Image = Image.FromStream(WebRequest.Create(picUrl).GetResponse().GetResponseStream());
                this.pbPicture.Tag = picUrl;
            }
            catch (Exception ex)
            {
                Log.Error(ex);
            }
        }

        /// <summary>
        /// 商品申请二维码生成
        /// </summary>
        /// <param name="id"></param>
        private void DispQRCode(string id)
        {
            if (string.IsNullOrWhiteSpace(id)) return;
            if (id.Equals(this.pbQRCode.Tag)) return;

            if (this.pbQRCode.Image != null)
            {
                this.pbQRCode.Image.Dispose();
                this.pbQRCode.Image = null;
            }
            string idUrl = string.Format(qrCodeUrl, id);
            try
            {
                this.pbQRCode.Image = this.MakeTryQRCode(idUrl, id);
                this.pbQRCode.Tag = id;
            }
            catch (Exception ex)
            {
                Log.Error(ex);
            }
        }

        /// <summary>
        /// 二维码生成
        /// </summary>
        /// <param name="content"></param>
        /// <param name="title"></param>
        /// <returns></returns>
        private System.Drawing.Image MakeTryQRCode(string content, string title)
        {
            QrEncoder qrEncoder = new QrEncoder(ErrorCorrectionLevel.M);
            QrCode qrCode = qrEncoder.Encode(content);
            GraphicsRenderer render = new GraphicsRenderer(new FixedModuleSize(15, QuietZoneModules.Two), Brushes.Black, Brushes.White);
            DrawingSize dSize = render.SizeCalculator.GetSize(qrCode.Matrix.Width);

            Bitmap bitmap = new Bitmap(dSize.CodeWidth + 10, dSize.CodeWidth + 114);
            using (Graphics g = Graphics.FromImage(bitmap))
            {
                //背景色：白
                g.FillRectangle(Brushes.White, 0, 0, bitmap.Width, bitmap.Height);
                //描画二维码
                render.Draw(g, qrCode.Matrix, new Point(10, 10));

                g.SmoothingMode = System.Drawing.Drawing2D.SmoothingMode.HighQuality;
                //描画中间的log
                string logFile = Path.Combine(Application.StartupPath, "logo.png");
                if (File.Exists(logFile))
                {
                    Size size = new Size(160, 160);
                    Point point = new Point(bitmap.Width / 2 - size.Width / 2, bitmap.Width / 2 - size.Height / 2);
                    //描画背景
                    g.FillEllipse(Brushes.White, point.X, point.Y, size.Width, size.Height);
                    //描画logo
                    System.Drawing.Image img = Bitmap.FromFile(logFile);
                    TextureBrush texture = new TextureBrush(img);
                    texture.TranslateTransform(-75, -80);
                    texture.WrapMode = System.Drawing.Drawing2D.WrapMode.Tile;
                    g.FillEllipse(texture, point.X + 8, point.Y + 8, size.Width - 16, size.Height - 16);
                }
                //描画下面的描述性文字
                StringFormat sf = new StringFormat();
                sf.Alignment = StringAlignment.Center;
                sf.LineAlignment = StringAlignment.Near;
                sf.FormatFlags = StringFormatFlags.NoWrap;
                g.DrawString(title, new Font("Simhei", 32f, FontStyle.Bold), Brushes.Black, new RectangleF(30, bitmap.Height - 100, bitmap.Width - 60, 100), sf);
            }
            return bitmap;
        }
        #endregion
    }

    #region class
    public class TBTryResponse
    {
        public bool success;
        public TBTryResult result;
        public string error;
    }

    public class TBTryResult
    {
        public TBTryPaging paging;
        public List<TBTryItem> items;
    }

    public class TBTryPaging
    {
        [JsonProperty(PropertyName = "n")]
        public int total;
        public int page;
        public int pages;
    }

    public class TBTryItem
    {
        public int acceptNum;
        public long endTime;
        public string id;
        public bool isApplied;
        public string pic;
        public decimal price;
        public int reportNum;
        public int requestNum;
        public string shopItemId;
        public string shopName;
        public string shopUserId;
        public string showId;
        public long startTime;
        public int status;
        public string title;
        public int totalNum;
        [JsonProperty(PropertyName = "type")]
        public int Type;
    }

    public class TBTryItemComparer : IComparer<TBTryItem>
    {
        public int Compare(TBTryItem x, TBTryItem y)
        {
            return x.id.CompareTo(y.id);
        }
    }
    #endregion
}
