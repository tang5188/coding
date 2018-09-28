using ArcFaceCore;
using System;
using System.Collections.Generic;
using System.ComponentModel;
using System.Configuration;
using System.Data;
using System.Drawing;
using System.Drawing.Imaging;
using System.IO;
using System.Linq;
using System.Runtime.InteropServices;
using System.Text;
using System.Threading.Tasks;
using System.Windows.Forms;

namespace ArcFaceSample
{
    public partial class FrmFaceDetection : Form
    {
        private bool detectState;
        /// <summary>
        /// 人脸检测
        /// </summary>
        private ArcFaceDetection faceDetection;

        private bool recognizeState;
        /// <summary>
        /// 人脸识别
        /// </summary>
        private ArcFaceRecognition faceRecognition;

        /// <summary>
        /// 人脸检测、人脸识别引擎状态发生变化
        /// </summary>
        public Action<bool, bool> OnEngineStateChanged;

        /// <summary>
        /// 简单起见，直接在内存中保存所有的脸部特征信息
        /// </summary>
        private List<FaceFeature> faceFeatures;

        /// <summary>
        /// 人脸目录库
        /// </summary>
        public static string FaceLibraryPath
        {
            get
            {
                string path = Path.Combine(AppDomain.CurrentDomain.BaseDirectory, @"faceDB\");
                if (!Directory.Exists(path)) Directory.CreateDirectory(path);
                return path;
            }
        }

        public FrmFaceDetection()
        {
            InitializeComponent();
        }

        protected override void OnShown(EventArgs e)
        {
            base.OnShown(e);

            string appID = ConfigurationManager.AppSettings["appid"];
            string afdKey = ConfigurationManager.AppSettings["fdKey"];
            string afrKey = ConfigurationManager.AppSettings["frKey"];
            //人脸检测初始化
            faceDetection = new ArcFaceDetection(appID, afdKey);
            this.detectState = faceDetection.InitFaceEngine() == 0;

            //人脸识别初始化
            faceRecognition = new ArcFaceRecognition(appID, afrKey);
            this.recognizeState = faceRecognition.InitFaceEngine() == 0;
            this.OnEngineStateChanged?.Invoke(this.detectState, this.recognizeState);
        }

        protected override void OnClosing(CancelEventArgs e)
        {
            base.OnClosing(e);
            if (detectState)
            {
                faceDetection.UninitFaceEngine();
                detectState = false;
            }
            if (recognizeState)
            {
                faceRecognition.UninitFaceEngine();
                recognizeState = false;
            }
            this.OnEngineStateChanged?.Invoke(this.detectState, this.recognizeState);
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

            try
            {
                List<string> lstMatchFiles = new List<string>();
                //检测图片内的人脸数据
                var facers = faceDetection.DetectFace(bitmap);
                if (facers.nFace > 0)
                {
                    int sizeRect = Marshal.SizeOf(typeof(MRECT));   //范围size
                    int sizeOrient = Marshal.SizeOf(typeof(int));   //角度size

                    List<MRECT> rects1 = new List<MRECT>();
                    List<MRECT> rects2 = new List<MRECT>();
                    for (int i = 0; i < facers.nFace; i++)
                    {
                        MRECT rect = (MRECT)Marshal.PtrToStructure(facers.rcFace + sizeRect * i, typeof(MRECT));
                        int orient = (int)Marshal.PtrToStructure(facers.lfaceOrient + sizeOrient * i, typeof(int));

                        //特征码提取
                        byte[] featureContent = this.RecognizeFace(bitmap, rect, orient);
                        var result = IsMatchFeature(featureContent);
                        //已经被识别过
                        if (result.Item1)
                        {
                            rects2.Add(rect);
                            lstMatchFiles.Add(result.Item2);
                        }
                        else
                        {
                            rects1.Add(rect);
                            string fileName = Guid.NewGuid().ToString();
                            //保存脸部区域到文件
                            Image image = CutFace(bitmap, rect);
                            image.Save(FaceLibraryPath + fileName + ".jpg", ImageFormat.Jpeg);
                            image.Dispose();

                            //保存特征码到.dat
                            File.WriteAllBytes(FaceLibraryPath + fileName + ".rfa", featureContent);
                            //添加特征码到内存
                            this.faceFeatures.Add(new FaceFeature()
                            {
                                FileName = fileName,
                                Feature = featureContent,
                            });
                        }

                    }
                    this.pic2.Image = DrawRectToImage(bitmap, rects1, rects2);
                }

                //移除现存的控件
                for (int i = this.panel.Controls.Count - 1; i >= 0; i--)
                {
                    PictureBox pictureBox = this.panel.Controls[i] as PictureBox;
                    pictureBox.Image.Dispose();
                    pictureBox.Image = null;
                    this.panel.Controls.RemoveAt(i);
                }
                //显示特征码存在的图片到panel
                foreach (var fileName in lstMatchFiles)
                {
                    PictureBox pictureBox = new PictureBox();
                    pictureBox.SizeMode = PictureBoxSizeMode.StretchImage;
                    pictureBox.Image = new Bitmap(FaceLibraryPath + fileName + ".jpg");
                    pictureBox.Size = new Size(this.panel.Height, this.panel.Height);
                    this.panel.Controls.Add(pictureBox);
                }
            }
            catch (Exception ex)
            {
                MessageBox.Show(ex.Message, "错误", MessageBoxButtons.OK, MessageBoxIcon.Error);
            }
        }

        /// <summary>
        /// 检测特征是否已存在
        /// </summary>
        /// <param name="featureContent"></param>
        /// <returns></returns>
        private Tuple<bool, string> IsMatchFeature(byte[] featureContent)
        {
            if (this.faceFeatures == null) this.LoadFaceFeatures();

            //待比较的特征数据
            IntPtr f2Ptr = Marshal.AllocHGlobal(featureContent.Length);
            Marshal.Copy(featureContent, 0, f2Ptr, featureContent.Length);

            AFR_FSDK_FACEMODEL model2 = new AFR_FSDK_FACEMODEL();
            model2.lFeatureSize = featureContent.Length;
            model2.pbFeature = f2Ptr;

            IntPtr model2Ptr = Marshal.AllocHGlobal(Marshal.SizeOf(model2));
            Marshal.StructureToPtr(model2, model2Ptr, false);

            foreach (var feature in this.faceFeatures)
            {
                //已有的特征数据
                IntPtr f1Ptr = Marshal.AllocHGlobal(feature.Feature.Length);
                Marshal.Copy(feature.Feature, 0, f1Ptr, feature.Feature.Length);

                AFR_FSDK_FACEMODEL model1 = new AFR_FSDK_FACEMODEL();
                model1.lFeatureSize = feature.Feature.Length;
                model1.pbFeature = f1Ptr;

                IntPtr model1Ptr = Marshal.AllocHGlobal(Marshal.SizeOf(model1));
                Marshal.StructureToPtr(model1, model1Ptr, false);

                float score = 0;
                int ret = AFRFunction.AFR_FSDK_FacePairMatching(faceRecognition.rEngine, model1Ptr, model2Ptr, ref score);
                Console.WriteLine("score:{0}", score);
                //相似度因子：0.75
                if (ret == 0 && score >= 0.75f)
                {
                    return new Tuple<bool, string>(true, feature.FileName);
                }
            }

            return new Tuple<bool, string>(false, string.Empty);
        }

        /// <summary>
        /// 人脸特征库全部数据的load
        /// </summary>
        private void LoadFaceFeatures()
        {
            this.faceFeatures = new List<FaceFeature>();

            DirectoryInfo di = new DirectoryInfo(FaceLibraryPath);
            if (!di.Exists) return;

            FileInfo[] fis = di.GetFiles("*.rfa");
            foreach (var fi in fis)
            {
                try
                {
                    string fileName = Path.GetFileNameWithoutExtension(fi.FullName);
                    if (!File.Exists(FaceLibraryPath + fileName + ".jpg")) continue;

                    byte[] feature = File.ReadAllBytes(fi.FullName);
                    this.faceFeatures.Add(new FaceFeature()
                    {
                        Feature = feature,
                        FileName = fileName
                    });
                }
                catch { }
            }
        }

        /// <summary>
        /// 人脸识别特征码提取
        /// </summary>
        /// <param name="image"></param>
        /// <param name="rect"></param>
        /// <param name="orient"></param>
        private byte[] RecognizeFace(Bitmap image, MRECT rect, int orient)
        {
            IntPtr offInputPtr = ArcFaceDetection.MakeImageInput_ASVLOFFSCREEN(image);

            AFR_FSDK_FACEINPUT faceInput = new AFR_FSDK_FACEINPUT();
            faceInput.lOrient = orient;
            faceInput.rcFace = rect;
            //入参
            IntPtr faceInputPtr = Marshal.AllocHGlobal(Marshal.SizeOf(faceInput));
            Marshal.StructureToPtr(faceInput, faceInputPtr, false);

            AFR_FSDK_FACEMODEL faceModel = new AFR_FSDK_FACEMODEL();
            IntPtr faceModelPtr = Marshal.AllocHGlobal(Marshal.SizeOf(faceModel));

            int ret = AFRFunction.AFR_FSDK_ExtractFRFeature(this.faceRecognition.rEngine, offInputPtr, faceInputPtr, faceModelPtr);
            if (ret == 0)
            {
                faceModel = (AFR_FSDK_FACEMODEL)Marshal.PtrToStructure(faceModelPtr, typeof(AFR_FSDK_FACEMODEL));
                Marshal.FreeHGlobal(faceModelPtr);

                byte[] featureContent = new byte[faceModel.lFeatureSize];
                Marshal.Copy(faceModel.pbFeature, featureContent, 0, faceModel.lFeatureSize);

                return featureContent;
            }
            return null;
        }

        /// <summary>
        /// 剪切人脸范围
        /// </summary>
        /// <param name="bitmap"></param>
        /// <param name="rect"></param>
        /// <returns></returns>
        private Image CutFace(Bitmap bitmap, MRECT rect)
        {
            int width = rect.right - rect.left;
            int height = rect.bottom - rect.top;

            Image image = new Bitmap(width, height);
            Graphics g = Graphics.FromImage(image);
            g.DrawImage(bitmap, 0, 0, new Rectangle(rect.left, rect.top, width, height), GraphicsUnit.Pixel);
            g.Dispose();
            return image;
        }

        /// <summary>
        /// 检测到人脸后，描绘其范围
        /// </summary>
        /// <param name="bitmap"></param>
        /// <param name="rects"></param>
        /// <returns></returns>
        private Image DrawRectToImage(Bitmap bitmap, List<MRECT> rects1, List<MRECT> rects2)
        {
            if (bitmap == null) return null;

            Image image = new Bitmap(bitmap.Width, bitmap.Height);
            Graphics g = Graphics.FromImage(image);
            //描绘原图
            g.DrawImage(bitmap, new PointF(0, 0));
            //人脸检测到的范围描绘
            if (rects1 != null)
            {
                using (Brush brush = new SolidBrush(Color.FromArgb(96, Color.Green)))
                {
                    foreach (var rect in rects1)
                    {
                        g.FillRectangle(brush, new Rectangle(rect.left, rect.top, Math.Abs(rect.right - rect.left), Math.Abs(rect.bottom - rect.top)));
                    }
                }
            }
            if (rects2 != null)
            {
                using (Brush brush = new SolidBrush(Color.FromArgb(96, Color.Blue)))
                {
                    foreach (var rect in rects2)
                    {
                        g.FillRectangle(brush, new Rectangle(rect.left, rect.top, Math.Abs(rect.right - rect.left), Math.Abs(rect.bottom - rect.top)));
                    }
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

        /// <summary>
        /// 特征码重新加载
        /// </summary>
        /// <param name="sender"></param>
        /// <param name="e"></param>
        private void btnReloadFeature_Click(object sender, EventArgs e)
        {
            this.faceFeatures = null;
        }
    }

    /// <summary>
    /// 脸部特征
    /// </summary>
    internal class FaceFeature
    {
        /// <summary>
        /// file name without extentions
        /// </summary>
        public string FileName;

        /// <summary>
        /// 
        /// </summary>
        public byte[] Feature;
    }
}
