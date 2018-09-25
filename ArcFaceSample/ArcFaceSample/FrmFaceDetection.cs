using ArcFaceCore;
using System;
using System.Collections.Generic;
using System.ComponentModel;
using System.Configuration;
using System.Data;
using System.Drawing;
using System.IO;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using System.Windows.Forms;

namespace ArcFaceSample
{
    public partial class FrmFaceDetection : Form
    {
        public FrmFaceDetection()
        {
            InitializeComponent();
        }

        /// <summary>
        /// 人脸检测
        /// </summary>
        /// <param name="sender"></param>
        /// <param name="e"></param>
        private void btnFaceDetect_Click(object sender, EventArgs e)
        {
            Bitmap bitmap = (Bitmap)this.pic1.Image;
            if (bitmap == null) return;

            string appID = ConfigurationManager.AppSettings["appid"];
            string afdKey = ConfigurationManager.AppSettings["fdKey"];

            ArcFaceDetection afd = new ArcFaceDetection(appID, afdKey);

            int ret = afd.InitFaceEngine();
            if (ret != 0)
            {
                MessageBox.Show("人脸检测引擎初始化失败！");
                return;
            }
            try
            {
                var facers = afd.DetectFace(bitmap);
                if (facers.nFace > 0)
                {
                    MRECT[] rects = afd.ConvertMRECT(facers.nFace, facers.rcFace);
                    Image rectImage = DrawRectToImage(bitmap, rects);
                    this.pic2.Image = rectImage;
                }
            }
            catch (Exception ex)
            {
                MessageBox.Show(ex.Message, "错误", MessageBoxButtons.OK, MessageBoxIcon.Error);
            }
            finally
            {
                afd.UninitFaceEngine();
            }
        }

        /// <summary>
        /// 检测到人脸后，描绘其范围
        /// </summary>
        /// <param name="bitmap"></param>
        /// <param name="rects"></param>
        /// <returns></returns>
        private Image DrawRectToImage(Bitmap bitmap, MRECT[] rects)
        {
            if (bitmap == null) return null;

            Image image = new Bitmap(bitmap.Width, bitmap.Height);
            Graphics g = Graphics.FromImage(image);
            //描绘原图
            g.DrawImage(bitmap, new PointF(0, 0));
            //人脸检测到的范围描绘
            if (rects != null)
            {
                foreach (var rect in rects)
                {
                    g.DrawRectangle(Pens.Red, new Rectangle(rect.left, rect.top, Math.Abs(rect.right - rect.left), Math.Abs(rect.bottom - rect.top)));
                }
            }
            g.Dispose();
            return image;
        }

        /// <summary>
        /// 图片文件选择
        /// </summary>
        /// <param name="sender"></param>
        /// <param name="e"></param>
        private void btnFileChoose_Click(object sender, EventArgs e)
        {
            OpenFileDialog openFile = new OpenFileDialog();
            openFile.Filter = "图片文件|*.bmp;*.jpg;*.jpeg;*.png";
            openFile.Multiselect = false;
            openFile.FileName = "";
            openFile.InitialDirectory = Environment.GetFolderPath(Environment.SpecialFolder.Desktop);
            openFile.RestoreDirectory = true;
            if (openFile.ShowDialog() == DialogResult.OK &&
                File.Exists(openFile.FileName))
            {
                Bitmap bitmap = new Bitmap(openFile.FileName);
                this.pic1.Image = bitmap;
                this.textBox1.Text = openFile.FileName;
            }
            openFile.Dispose();
        }
    }
}
