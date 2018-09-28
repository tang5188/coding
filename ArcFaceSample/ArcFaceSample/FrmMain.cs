using System;
using System.Collections.Generic;
using System.ComponentModel;
using System.Data;
using System.Drawing;
using System.Linq;
using System.Text;
using System.Threading.Tasks;
using System.Windows.Forms;

namespace ArcFaceSample
{
    public partial class FrmMain : Form
    {
        public FrmMain()
        {
            InitializeComponent();

            this.CreateMenu();
            this.miDetection_Click(null, null);
        }

        /// <summary>
        /// 创建菜单项
        /// </summary>
        private void CreateMenu()
        {
            MainMenu mainMenu = new MainMenu();

            MenuItem item = new MenuItem("文件");
            item.MenuItems.Add("退出", this.miExit_Click);
            mainMenu.MenuItems.Add(item);

            item = new MenuItem("功能");
            item.MenuItems.Add("人脸检测", this.miDetection_Click);
            mainMenu.MenuItems.Add(item);

            item = new MenuItem("窗口(&W)");
            item.MdiList = true;
            item.MenuItems.Add("窗体层叠(&C)", new EventHandler(this.miCascade_Click));
            item.MenuItems.Add("水平平铺(&H)", new EventHandler(this.miTileH_Click));
            item.MenuItems.Add("垂直平铺(&V)", new EventHandler(this.miTileV_Click));
            item.MenuItems.Add("关闭全部", new EventHandler(this.miCloseAll_Click));
            mainMenu.MenuItems.Add(item);

            item = new MenuItem("帮助(&H)");
            item.MenuItems.Add("关于...(&A)", this.miAbout_Click);
            mainMenu.MenuItems.Add(item);

            this.Menu = mainMenu;
        }

        /// <summary>
        /// 退出
        /// </summary>
        /// <param name="sender"></param>
        /// <param name="e"></param>
        private void miExit_Click(object sender, EventArgs e)
        {
            this.Close();
        }

        /// <summary>
        /// 人脸检测
        /// </summary>
        /// <param name="sender"></param>
        /// <param name="e"></param>
        private void miDetection_Click(object sender, EventArgs e)
        {
            if (this.HaveOpened(typeof(FrmFaceDetection))) return;

            FrmFaceDetection frm = new FrmFaceDetection();
            frm.OnEngineStateChanged += this.OnEngineStateChanged;
            frm.MdiParent = this;
            frm.WindowState = FormWindowState.Maximized;
            frm.Show();
        }

        /// <summary>
        /// 层叠
        /// </summary>
        /// <param name="sender"></param>
        /// <param name="e"></param>
        private void miCascade_Click(object sender, EventArgs e)
        {
            this.LayoutMdi(MdiLayout.Cascade);
        }

        /// <summary>
        /// 水平平铺
        /// </summary>
        /// <param name="sender"></param>
        /// <param name="e"></param>
        private void miTileH_Click(object sender, EventArgs e)
        {
            this.LayoutMdi(MdiLayout.TileHorizontal);
        }

        /// <summary>
        /// 垂直平铺
        /// </summary>
        /// <param name="sender"></param>
        /// <param name="e"></param>
        private void miTileV_Click(object sender, EventArgs e)
        {
            this.LayoutMdi(MdiLayout.TileVertical);
        }

        /// <summary>
        /// 关闭全部
        /// </summary>
        /// <param name="sender"></param>
        /// <param name="e"></param>
        private void miCloseAll_Click(object sender, EventArgs e)
        {
            foreach (var frm in this.MdiChildren)
            {
                frm.Close();
            }
        }

        /// <summary>
        /// 关于
        /// </summary>
        /// <param name="sender"></param>
        /// <param name="e"></param>
        private void miAbout_Click(object sender, EventArgs e)
        {
            MessageBox.Show("人脸识别：\r\n1.未找到相似的人脸时，将特征码保存\r\n2.相似度在一定范围内时，不保存特征码，显示相似的照片");
        }

        /// <summary>
        /// 判断子窗体是否已打开
        /// </summary>
        /// <param name="type"></param>
        /// <returns></returns>
        private bool HaveOpened(Type type)
        {
            //查看窗口是否已经被打开
            bool bReturn = false;
            int index = -1;
            for (int i = 0; i < this.MdiChildren.Length; i++)
            {
                if (this.MdiChildren[i].GetType() == type)
                {
                    index = i;
                    bReturn = true;
                    break;
                }
            }
            if (bReturn &&
                index >= 0 &&
                index < this.MdiChildren.Length)
            {
                this.MdiChildren[index].BringToFront();
                this.MdiChildren[index].WindowState = FormWindowState.Maximized;
            }
            return bReturn;
        }

        /// <summary>
        /// 引擎状态发生变化
        /// </summary>
        /// <param name="detectState"></param>
        /// <param name="recognizeState"></param>
        private void OnEngineStateChanged(bool detectState, bool recognizeState)
        {
            this.lblDetectEngine.Text = detectState.ToString();
            this.lblRecognizeEngine.Text = recognizeState.ToString();
        }
    }
}
