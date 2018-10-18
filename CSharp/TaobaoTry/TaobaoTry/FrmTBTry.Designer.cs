namespace TaobaoTry
{
    partial class FrmTBTry
    {
        /// <summary>
        /// Required designer variable.
        /// </summary>
        private System.ComponentModel.IContainer components = null;

        /// <summary>
        /// Clean up any resources being used.
        /// </summary>
        /// <param name="disposing">true if managed resources should be disposed; otherwise, false.</param>
        protected override void Dispose(bool disposing)
        {
            if (disposing && (components != null))
            {
                components.Dispose();
            }
            base.Dispose(disposing);
        }

        #region Windows Form Designer generated code

        /// <summary>
        /// Required method for Designer support - do not modify
        /// the contents of this method with the code editor.
        /// </summary>
        private void InitializeComponent()
        {
            System.ComponentModel.ComponentResourceManager resources = new System.ComponentModel.ComponentResourceManager(typeof(FrmTBTry));
            this.btnLoad = new System.Windows.Forms.Button();
            this.cmbCurPage = new System.Windows.Forms.ComboBox();
            this.cmbPageSize = new System.Windows.Forms.ComboBox();
            this.label1 = new System.Windows.Forms.Label();
            this.label2 = new System.Windows.Forms.Label();
            this.label3 = new System.Windows.Forms.Label();
            this.lblCurPage = new System.Windows.Forms.Label();
            this.lblPageSize = new System.Windows.Forms.Label();
            this.label5 = new System.Windows.Forms.Label();
            this.gridResult = new System.Windows.Forms.DataGridView();
            this.title = new System.Windows.Forms.DataGridViewTextBoxColumn();
            this.picture = new System.Windows.Forms.DataGridViewTextBoxColumn();
            this.id = new System.Windows.Forms.DataGridViewTextBoxColumn();
            this.price = new System.Windows.Forms.DataGridViewTextBoxColumn();
            this.leftcount = new System.Windows.Forms.DataGridViewTextBoxColumn();
            this.requestnum = new System.Windows.Forms.DataGridViewTextBoxColumn();
            this.endtime = new System.Windows.Forms.DataGridViewTextBoxColumn();
            this.percent = new System.Windows.Forms.DataGridViewTextBoxColumn();
            this.chkToday = new System.Windows.Forms.CheckBox();
            this.pbPicture = new System.Windows.Forms.PictureBox();
            this.pbQRCode = new System.Windows.Forms.PictureBox();
            this.label4 = new System.Windows.Forms.Label();
            this.label6 = new System.Windows.Forms.Label();
            this.label8 = new System.Windows.Forms.Label();
            this.txtTBToken = new System.Windows.Forms.TextBox();
            this.groupBox1 = new System.Windows.Forms.GroupBox();
            this.txtName = new System.Windows.Forms.TextBox();
            ((System.ComponentModel.ISupportInitialize)(this.gridResult)).BeginInit();
            ((System.ComponentModel.ISupportInitialize)(this.pbPicture)).BeginInit();
            ((System.ComponentModel.ISupportInitialize)(this.pbQRCode)).BeginInit();
            this.groupBox1.SuspendLayout();
            this.SuspendLayout();
            // 
            // btnLoad
            // 
            this.btnLoad.Location = new System.Drawing.Point(283, 10);
            this.btnLoad.Name = "btnLoad";
            this.btnLoad.Size = new System.Drawing.Size(75, 23);
            this.btnLoad.TabIndex = 4;
            this.btnLoad.Text = "加载";
            this.btnLoad.UseVisualStyleBackColor = true;
            this.btnLoad.Click += new System.EventHandler(this.btnLoad_Click);
            // 
            // cmbCurPage
            // 
            this.cmbCurPage.DropDownStyle = System.Windows.Forms.ComboBoxStyle.DropDownList;
            this.cmbCurPage.FormattingEnabled = true;
            this.cmbCurPage.Items.AddRange(new object[] {
            "1",
            "2",
            "3",
            "4",
            "5",
            "6",
            "7",
            "8",
            "9",
            "10",
            "11",
            "12",
            "13",
            "14",
            "15",
            "16",
            "17",
            "18",
            "19",
            "20",
            "21",
            "22",
            "23",
            "24",
            "25",
            "26",
            "27",
            "28",
            "29",
            "30",
            "31",
            "32",
            "33",
            "34",
            "35",
            "36",
            "37",
            "38",
            "39",
            "40"});
            this.cmbCurPage.Location = new System.Drawing.Point(47, 12);
            this.cmbCurPage.Name = "cmbCurPage";
            this.cmbCurPage.Size = new System.Drawing.Size(72, 20);
            this.cmbCurPage.TabIndex = 1;
            this.cmbCurPage.SelectedIndexChanged += new System.EventHandler(this.cmbCurPage_SelectedIndexChanged);
            // 
            // cmbPageSize
            // 
            this.cmbPageSize.DropDownStyle = System.Windows.Forms.ComboBoxStyle.DropDownList;
            this.cmbPageSize.FormattingEnabled = true;
            this.cmbPageSize.Items.AddRange(new object[] {
            "10",
            "20",
            "30",
            "40",
            "50",
            "60",
            "70",
            "80",
            "90",
            "100"});
            this.cmbPageSize.Location = new System.Drawing.Point(205, 12);
            this.cmbPageSize.Name = "cmbPageSize";
            this.cmbPageSize.Size = new System.Drawing.Size(72, 20);
            this.cmbPageSize.TabIndex = 3;
            this.cmbPageSize.SelectedIndexChanged += new System.EventHandler(this.cmbPageSize_SelectedIndexChanged);
            // 
            // label1
            // 
            this.label1.AutoSize = true;
            this.label1.Location = new System.Drawing.Point(12, 15);
            this.label1.Name = "label1";
            this.label1.Size = new System.Drawing.Size(29, 12);
            this.label1.TabIndex = 0;
            this.label1.Text = "页码";
            // 
            // label2
            // 
            this.label2.AutoSize = true;
            this.label2.Location = new System.Drawing.Point(146, 15);
            this.label2.Name = "label2";
            this.label2.Size = new System.Drawing.Size(53, 12);
            this.label2.TabIndex = 2;
            this.label2.Text = "每页数量";
            // 
            // label3
            // 
            this.label3.AutoSize = true;
            this.label3.Location = new System.Drawing.Point(12, 46);
            this.label3.Name = "label3";
            this.label3.Size = new System.Drawing.Size(53, 12);
            this.label3.TabIndex = 5;
            this.label3.Text = "当前页码";
            // 
            // lblCurPage
            // 
            this.lblCurPage.AutoSize = true;
            this.lblCurPage.Location = new System.Drawing.Point(71, 46);
            this.lblCurPage.Name = "lblCurPage";
            this.lblCurPage.Size = new System.Drawing.Size(11, 12);
            this.lblCurPage.TabIndex = 6;
            this.lblCurPage.Text = "0";
            // 
            // lblPageSize
            // 
            this.lblPageSize.AutoSize = true;
            this.lblPageSize.Location = new System.Drawing.Point(205, 46);
            this.lblPageSize.Name = "lblPageSize";
            this.lblPageSize.Size = new System.Drawing.Size(11, 12);
            this.lblPageSize.TabIndex = 8;
            this.lblPageSize.Text = "0";
            // 
            // label5
            // 
            this.label5.AutoSize = true;
            this.label5.Location = new System.Drawing.Point(146, 46);
            this.label5.Name = "label5";
            this.label5.Size = new System.Drawing.Size(53, 12);
            this.label5.TabIndex = 7;
            this.label5.Text = "当前数量";
            // 
            // gridResult
            // 
            this.gridResult.AllowUserToAddRows = false;
            this.gridResult.AllowUserToDeleteRows = false;
            this.gridResult.AllowUserToResizeRows = false;
            this.gridResult.Anchor = ((System.Windows.Forms.AnchorStyles)((((System.Windows.Forms.AnchorStyles.Top | System.Windows.Forms.AnchorStyles.Bottom) 
            | System.Windows.Forms.AnchorStyles.Left) 
            | System.Windows.Forms.AnchorStyles.Right)));
            this.gridResult.ColumnHeadersHeightSizeMode = System.Windows.Forms.DataGridViewColumnHeadersHeightSizeMode.AutoSize;
            this.gridResult.Columns.AddRange(new System.Windows.Forms.DataGridViewColumn[] {
            this.title,
            this.picture,
            this.id,
            this.price,
            this.leftcount,
            this.requestnum,
            this.endtime,
            this.percent});
            this.gridResult.EditMode = System.Windows.Forms.DataGridViewEditMode.EditProgrammatically;
            this.gridResult.Location = new System.Drawing.Point(12, 61);
            this.gridResult.Name = "gridResult";
            this.gridResult.RowTemplate.Height = 23;
            this.gridResult.SelectionMode = System.Windows.Forms.DataGridViewSelectionMode.FullRowSelect;
            this.gridResult.Size = new System.Drawing.Size(827, 574);
            this.gridResult.TabIndex = 11;
            this.gridResult.CellDoubleClick += new System.Windows.Forms.DataGridViewCellEventHandler(this.gridResult_CellDoubleClick);
            this.gridResult.SelectionChanged += new System.EventHandler(this.gridResult_SelectionChanged);
            // 
            // title
            // 
            this.title.DataPropertyName = "title";
            this.title.HeaderText = "名称";
            this.title.Name = "title";
            this.title.Width = 350;
            // 
            // picture
            // 
            this.picture.DataPropertyName = "picture";
            this.picture.HeaderText = "picture";
            this.picture.Name = "picture";
            this.picture.Visible = false;
            // 
            // id
            // 
            this.id.DataPropertyName = "id";
            this.id.HeaderText = "id";
            this.id.Name = "id";
            this.id.ReadOnly = true;
            this.id.Visible = false;
            // 
            // price
            // 
            this.price.DataPropertyName = "price";
            this.price.HeaderText = "价值";
            this.price.Name = "price";
            this.price.Width = 60;
            // 
            // leftcount
            // 
            this.leftcount.DataPropertyName = "leftcount";
            this.leftcount.HeaderText = "剩余";
            this.leftcount.Name = "leftcount";
            this.leftcount.Width = 60;
            // 
            // requestnum
            // 
            this.requestnum.DataPropertyName = "requestnum";
            this.requestnum.HeaderText = "申请数";
            this.requestnum.Name = "requestnum";
            this.requestnum.Width = 80;
            // 
            // endtime
            // 
            this.endtime.DataPropertyName = "endtime";
            this.endtime.HeaderText = "截止";
            this.endtime.Name = "endtime";
            this.endtime.Width = 140;
            // 
            // percent
            // 
            this.percent.DataPropertyName = "percent";
            this.percent.HeaderText = "比例‰";
            this.percent.Name = "percent";
            this.percent.Width = 70;
            // 
            // chkToday
            // 
            this.chkToday.AutoSize = true;
            this.chkToday.Location = new System.Drawing.Point(283, 45);
            this.chkToday.Name = "chkToday";
            this.chkToday.Size = new System.Drawing.Size(72, 16);
            this.chkToday.TabIndex = 9;
            this.chkToday.Text = "今日截止";
            this.chkToday.UseVisualStyleBackColor = true;
            this.chkToday.CheckedChanged += new System.EventHandler(this.chkToday_CheckedChanged);
            // 
            // pbPicture
            // 
            this.pbPicture.Anchor = ((System.Windows.Forms.AnchorStyles)((System.Windows.Forms.AnchorStyles.Top | System.Windows.Forms.AnchorStyles.Right)));
            this.pbPicture.BackColor = System.Drawing.Color.White;
            this.pbPicture.BorderStyle = System.Windows.Forms.BorderStyle.FixedSingle;
            this.pbPicture.Cursor = System.Windows.Forms.Cursors.Hand;
            this.pbPicture.Location = new System.Drawing.Point(845, 80);
            this.pbPicture.Name = "pbPicture";
            this.pbPicture.Size = new System.Drawing.Size(204, 204);
            this.pbPicture.SizeMode = System.Windows.Forms.PictureBoxSizeMode.StretchImage;
            this.pbPicture.TabIndex = 11;
            this.pbPicture.TabStop = false;
            this.pbPicture.Click += new System.EventHandler(this.pbPicture_Click);
            // 
            // pbQRCode
            // 
            this.pbQRCode.Anchor = ((System.Windows.Forms.AnchorStyles)((System.Windows.Forms.AnchorStyles.Top | System.Windows.Forms.AnchorStyles.Right)));
            this.pbQRCode.BackColor = System.Drawing.Color.White;
            this.pbQRCode.BorderStyle = System.Windows.Forms.BorderStyle.FixedSingle;
            this.pbQRCode.Location = new System.Drawing.Point(845, 335);
            this.pbQRCode.Name = "pbQRCode";
            this.pbQRCode.Size = new System.Drawing.Size(204, 204);
            this.pbQRCode.SizeMode = System.Windows.Forms.PictureBoxSizeMode.StretchImage;
            this.pbQRCode.TabIndex = 12;
            this.pbQRCode.TabStop = false;
            // 
            // label4
            // 
            this.label4.Anchor = ((System.Windows.Forms.AnchorStyles)((System.Windows.Forms.AnchorStyles.Top | System.Windows.Forms.AnchorStyles.Right)));
            this.label4.AutoSize = true;
            this.label4.Location = new System.Drawing.Point(845, 65);
            this.label4.Name = "label4";
            this.label4.Size = new System.Drawing.Size(53, 12);
            this.label4.TabIndex = 13;
            this.label4.Text = "商品预览";
            // 
            // label6
            // 
            this.label6.Anchor = ((System.Windows.Forms.AnchorStyles)((System.Windows.Forms.AnchorStyles.Top | System.Windows.Forms.AnchorStyles.Right)));
            this.label6.AutoSize = true;
            this.label6.Location = new System.Drawing.Point(845, 320);
            this.label6.Name = "label6";
            this.label6.Size = new System.Drawing.Size(191, 12);
            this.label6.TabIndex = 14;
            this.label6.Text = "试用申请：使用淘宝或天猫app扫码";
            // 
            // label8
            // 
            this.label8.AutoSize = true;
            this.label8.Location = new System.Drawing.Point(6, 17);
            this.label8.Name = "label8";
            this.label8.Size = new System.Drawing.Size(65, 12);
            this.label8.TabIndex = 0;
            this.label8.Text = "_tb_token_";
            // 
            // txtTBToken
            // 
            this.txtTBToken.Location = new System.Drawing.Point(77, 14);
            this.txtTBToken.Name = "txtTBToken";
            this.txtTBToken.Size = new System.Drawing.Size(114, 21);
            this.txtTBToken.TabIndex = 1;
            this.txtTBToken.Text = "7578874ee33b3";
            // 
            // groupBox1
            // 
            this.groupBox1.Anchor = ((System.Windows.Forms.AnchorStyles)((System.Windows.Forms.AnchorStyles.Top | System.Windows.Forms.AnchorStyles.Right)));
            this.groupBox1.Controls.Add(this.label8);
            this.groupBox1.Controls.Add(this.txtTBToken);
            this.groupBox1.Location = new System.Drawing.Point(845, 10);
            this.groupBox1.Name = "groupBox1";
            this.groupBox1.Size = new System.Drawing.Size(204, 45);
            this.groupBox1.TabIndex = 12;
            this.groupBox1.TabStop = false;
            // 
            // txtName
            // 
            this.txtName.Location = new System.Drawing.Point(361, 40);
            this.txtName.Name = "txtName";
            this.txtName.Size = new System.Drawing.Size(114, 21);
            this.txtName.TabIndex = 10;
            this.txtName.TextChanged += new System.EventHandler(this.txtFilter_TextChanged);
            this.txtName.KeyDown += new System.Windows.Forms.KeyEventHandler(this.txtName_KeyDown);
            // 
            // FrmTBTry
            // 
            this.AutoScaleDimensions = new System.Drawing.SizeF(6F, 12F);
            this.AutoScaleMode = System.Windows.Forms.AutoScaleMode.Font;
            this.ClientSize = new System.Drawing.Size(1061, 647);
            this.Controls.Add(this.txtName);
            this.Controls.Add(this.groupBox1);
            this.Controls.Add(this.label6);
            this.Controls.Add(this.label4);
            this.Controls.Add(this.pbQRCode);
            this.Controls.Add(this.pbPicture);
            this.Controls.Add(this.chkToday);
            this.Controls.Add(this.gridResult);
            this.Controls.Add(this.lblPageSize);
            this.Controls.Add(this.label5);
            this.Controls.Add(this.lblCurPage);
            this.Controls.Add(this.label3);
            this.Controls.Add(this.label2);
            this.Controls.Add(this.label1);
            this.Controls.Add(this.cmbPageSize);
            this.Controls.Add(this.cmbCurPage);
            this.Controls.Add(this.btnLoad);
            this.Icon = ((System.Drawing.Icon)(resources.GetObject("$this.Icon")));
            this.Name = "FrmTBTry";
            this.StartPosition = System.Windows.Forms.FormStartPosition.CenterScreen;
            this.Text = "淘宝试用检索：https://try.taobao.com/";
            ((System.ComponentModel.ISupportInitialize)(this.gridResult)).EndInit();
            ((System.ComponentModel.ISupportInitialize)(this.pbPicture)).EndInit();
            ((System.ComponentModel.ISupportInitialize)(this.pbQRCode)).EndInit();
            this.groupBox1.ResumeLayout(false);
            this.groupBox1.PerformLayout();
            this.ResumeLayout(false);
            this.PerformLayout();

        }

        #endregion

        private System.Windows.Forms.Button btnLoad;
        private System.Windows.Forms.ComboBox cmbCurPage;
        private System.Windows.Forms.ComboBox cmbPageSize;
        private System.Windows.Forms.Label label1;
        private System.Windows.Forms.Label label2;
        private System.Windows.Forms.Label label3;
        private System.Windows.Forms.Label lblCurPage;
        private System.Windows.Forms.Label lblPageSize;
        private System.Windows.Forms.Label label5;
        private System.Windows.Forms.DataGridView gridResult;
        private System.Windows.Forms.CheckBox chkToday;
        private System.Windows.Forms.PictureBox pbPicture;
        private System.Windows.Forms.PictureBox pbQRCode;
        private System.Windows.Forms.DataGridViewTextBoxColumn title;
        private System.Windows.Forms.DataGridViewTextBoxColumn picture;
        private System.Windows.Forms.DataGridViewTextBoxColumn id;
        private System.Windows.Forms.DataGridViewTextBoxColumn price;
        private System.Windows.Forms.DataGridViewTextBoxColumn leftcount;
        private System.Windows.Forms.DataGridViewTextBoxColumn requestnum;
        private System.Windows.Forms.DataGridViewTextBoxColumn endtime;
        private System.Windows.Forms.DataGridViewTextBoxColumn percent;
        private System.Windows.Forms.Label label4;
        private System.Windows.Forms.Label label6;
        private System.Windows.Forms.Label label8;
        private System.Windows.Forms.TextBox txtTBToken;
        private System.Windows.Forms.GroupBox groupBox1;
        private System.Windows.Forms.TextBox txtName;
    }
}

