package paintchat_client;

import java.applet.Applet;
import java.io.EOFException;
import java.io.IOException;
import java.net.InetAddress;
import java.net.Socket;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.Locale;
import paintchat.M;
import paintchat.MgText;
import paintchat.Res;
import syi.awt.Awt;
import syi.awt.TextPanel;
import syi.util.ByteStream;

public class Data {
   public Pl pl;
   public Res res;
   public Res config;
   public M.Info info;
   private boolean isDestroy = false;
   private int Port;
   private InetAddress address;
   private int ID = 0;
   private M mgDraw = new M();
   public Mi mi;
   private TextPanel text;
   public int imW;
   public int imH;
   public int MAX_KAIWA;
   public int MAX_KAIWA_BORDER;
   private EOFException EOF = new EOFException();
   private TLine tLine;
   private TText tText;
   public String strName = null;

   public Data(Pl var1) {
      this.pl = var1;
   }

   public synchronized void destroy() {
      if (!this.isDestroy) {
         this.isDestroy = true;

         try {
            if (this.tLine != null) {
               this.tLine.mRStop();
               this.tLine = null;
            }

            if (this.tText != null) {
               this.tText.mRStop();
               this.tText = null;
            }
         } catch (Throwable var1) {
            ;
         }

      }
   }

   private Socket getSocket() {
      if (this.address == null) {
         InetAddress var1 = null;
         String var2 = this.config.getP("Connection_Host", (String)null);

         try {
            if (var2 != null) {
               var1 = InetAddress.getByName(var2);
            }
         } catch (UnknownHostException var4) {
            var1 = null;
         }

         try {
            var2 = this.pl.applet.getCodeBase().getHost();
            if (var2 == null || var2.length() <= 0) {
               var2 = "localhost";
            }

            var1 = InetAddress.getByName(var2);
         } catch (UnknownHostException var7) {
            var1 = null;
         }

         if (var1 == null) {
            this.destroy();
            return null;
         }

         this.address = var1;
         String var3 = "Connection_Port_PaintChat";
         this.Port = this.config.getP(var3, 41411);
      }

      try {
         while(!this.isDestroy) {
            int var8 = 0;

            while(var8 < 2) {
               try {
                  return new Socket(this.address, this.Port);
               } catch (IOException var5) {
                  Thread.currentThread();
                  Thread.sleep(3000L);
                  ++var8;
               }
            }

            if (!this.mi.alert("reconnect", true)) {
               break;
            }
         }
      } catch (InterruptedException var6) {
         ;
      }

      this.destroy();
      return null;
   }

   public void init() {
      try {
         ByteStream var1 = new ByteStream();
         Applet var2 = this.pl.applet;
         URL var3 = var2.getCodeBase();
         String var4 = this.p("dir_resource", "./res");
         if (!var4.endsWith("/")) {
            var4 = var4 + '/';
         }

         URL var5 = new URL(var3, var4);
         this.res = new Res(var2, var5, var1);
         this.config = new Res(var2, var5, var1);
         this.config.loadZip(this.p("res.zip", "res.zip"));

         try {
            String var6 = "param_utf8.txt";
            this.config.load(new String((byte[])this.config.getRes(var6), "UTF8"));
            this.config.remove(var6);
         } catch (IOException var8) {
            var8.printStackTrace();
         }

         Me.res = this.res;
         Me.conf = this.config;
         this.pl.iPG(true);

         try {
            this.config.load(Awt.openStream(new URL(var3, this.config.getP("File_PaintChat_Infomation", this.config.getP("server", ".paintchat")))));
         } catch (IOException var7) {
            System.out.println((Object)var7);
         }

         this.pl.iPG(true);
         this.res.loadResource(this.config, "res", Locale.getDefault().getLanguage());
         this.pl.iPG(true);
         this.MAX_KAIWA_BORDER = this.config.getP("Cash_Text_Max", 120);
         this.imW = this.config.getP("Client_Image_Width", this.config.getP("image_width", 1200));
         this.imH = this.config.getP("Client_Image_Height", this.config.getP("image_height", 1200));
      } catch (IOException var9) {
         var9.printStackTrace();
      }

   }

   public void send(M var1) {
      this.tLine.send(var1);
   }

   public void send(MgText var1) {
      this.tText.send(var1);
   }

   public void start() throws IOException {
      Mi var1 = this.mi;
      this.info = var1.info;
      this.mgDraw.setInfo(var1.info);
      this.mgDraw.newUser(var1).wait = -1;
      Res var2 = new Res();
      var2.put("name", this.strName);
      var2.put("password", this.config.get("chat_password"));
      this.config.put(var2);
      this.mRunText(var2);
      this.mRunLine(var2);
   }

   private void mRunLine(Res var1) throws IOException {
      Res var2 = new Res();
      var2.put(var1);
      var2.put("protocol", "paintchat.line");
      this.tLine = new TLine(this, this.mgDraw);
      Socket var3 = this.getSocket();
      this.tLine.mConnect(var3, var2);
   }

   private void mRunText(Res var1) throws IOException {
      Res var2 = new Res();
      var2.put(var1);
      var2.put("protocol", "paintchat.text");
      this.tText = new TText(this.pl, this);
      Socket var3 = this.getSocket();
      this.tText.mConnect(var3, var2);
   }

   public String p(String var1, String var2) {
      try {
         String var3 = this.pl.applet.getParameter(var1);
         return var3 != null && var3.length() > 0 ? var3 : var2;
      } catch (Throwable var4) {
         return var2;
      }
   }

   public void addTextComp() {
      this.pl.addTextInfo(this.res.get("log_complete"), true);
      this.mPermission(this.tLine.getStatus().get("permission"));
   }

   public void mPermission(String var1) {
      int var2 = 0;
      int var4 = var1.length();

      int var3;
      do {
         var3 = var1.indexOf(59, var2);
         if (var3 < 0) {
            var3 = var4;
         }

         if (var3 - var2 > 0) {
            this.mP(var1.substring(var2, var3));
         }

         var2 = var3 + 1;
      } while(var3 < var4);

   }

   private void mP(String var1) {
      try {
         int var2 = var1.indexOf(58);
         if (var2 <= 0) {
            return;
         }

         String var3 = var1.substring(0, var2).trim();
         String var4 = var1.substring(var2 + 1).trim();
         boolean var5 = false;
         if (var4.length() > 0) {
            var5 = var4.charAt(0) == 't';
         }

         if (var3.equals("layer")) {
            this.info.permission = (long)(var4.equals("all") ? -1 : Integer.parseInt(var4));
         }

         if (var3.equals("layer_edit")) {
            this.info.isLEdit = var5;
         }

         if (var3.equals("canvas")) {
            this.mi.isEnable = var5;
         }

         if (var3.equals("fill")) {
            this.info.isFill = var5;
         }

         if (var3.equals("clean")) {
            this.info.isClean = var5;
         }

         if (var3.equals("unlayer")) {
            this.info.unpermission = (long)Integer.parseInt(var4);
         }
      } catch (RuntimeException var6) {
         var6.printStackTrace();
      }

   }
}
