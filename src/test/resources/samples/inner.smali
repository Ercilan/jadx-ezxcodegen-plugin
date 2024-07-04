###### Class com.cmccit.webview.customview.WebViewEx.WebChromeClientEx.AnonymousClass1 (com.cmccit.webview.customview.WebViewEx$WebChromeClientEx$1)
.class Lcom/cmccit/webview/customview/WebViewEx$WebChromeClientEx$1;
.super Lcom/tencent/smtt/sdk/WebViewClient;
.source "WebViewEx.java"


# annotations
.annotation system Ldalvik/annotation/EnclosingMethod;
    value = Lcom/cmccit/webview/customview/WebViewEx$WebChromeClientEx;->onCreateWindow(Lcom/tencent/smtt/sdk/WebView;ZZLandroid/os/Message;)Z
.end annotation

.annotation system Ldalvik/annotation/InnerClass;
    accessFlags = 0x0
    name = null
.end annotation


# instance fields
.field final synthetic this$1:Lcom/cmccit/webview/customview/WebViewEx$WebChromeClientEx;


# direct methods
.method constructor <init>(Lcom/cmccit/webview/customview/WebViewEx$WebChromeClientEx;)V
    .registers 2

    .line 925
    iput-object p1, p0, Lcom/cmccit/webview/customview/WebViewEx$WebChromeClientEx$1;->this$1:Lcom/cmccit/webview/customview/WebViewEx$WebChromeClientEx;

    invoke-direct {p0}, Lcom/tencent/smtt/sdk/WebViewClient;-><init>()V

    return-void
.end method


# virtual methods
.method public onPageStarted(Lcom/tencent/smtt/sdk/WebView;Ljava/lang/String;Landroid/graphics/Bitmap;)V
    .registers 4

    .line 928
    invoke-super {p0, p1, p2, p3}, Lcom/tencent/smtt/sdk/WebViewClient;->onPageStarted(Lcom/tencent/smtt/sdk/WebView;Ljava/lang/String;Landroid/graphics/Bitmap;)V

    .line 929
    iget-object p1, p0, Lcom/cmccit/webview/customview/WebViewEx$WebChromeClientEx$1;->this$1:Lcom/cmccit/webview/customview/WebViewEx$WebChromeClientEx;

    iget-object p1, p1, Lcom/cmccit/webview/customview/WebViewEx$WebChromeClientEx;->this$0:Lcom/cmccit/webview/customview/WebViewEx;

    invoke-static {p1}, Lcom/cmccit/webview/customview/WebViewEx;->access$1200(Lcom/cmccit/webview/customview/WebViewEx;)Lcom/cmccit/webview/customview/ToastUrlListener;

    move-result-object p1

    if-eqz p1, :cond_18

    .line 930
    iget-object p1, p0, Lcom/cmccit/webview/customview/WebViewEx$WebChromeClientEx$1;->this$1:Lcom/cmccit/webview/customview/WebViewEx$WebChromeClientEx;

    iget-object p1, p1, Lcom/cmccit/webview/customview/WebViewEx$WebChromeClientEx;->this$0:Lcom/cmccit/webview/customview/WebViewEx;

    invoke-static {p1}, Lcom/cmccit/webview/customview/WebViewEx;->access$1200(Lcom/cmccit/webview/customview/WebViewEx;)Lcom/cmccit/webview/customview/ToastUrlListener;

    move-result-object p1

    invoke-interface {p1, p2}, Lcom/cmccit/webview/customview/ToastUrlListener;->onToastUrl(Ljava/lang/String;)V

    :cond_18
    return-void
.end method

.method public shouldOverrideUrlLoading(Lcom/tencent/smtt/sdk/WebView;Ljava/lang/String;)Z
    .registers 6

    .line 936
    new-instance p1, Landroid/os/Bundle;

    invoke-direct {p1}, Landroid/os/Bundle;-><init>()V

    const-string v0, "url"

    .line 937
    invoke-virtual {p1, v0, p2}, Landroid/os/Bundle;->putString(Ljava/lang/String;Ljava/lang/String;)V

    .line 938
    invoke-static {}, Lcom/cmccit/basemodule/bridge/PageIntentBridge;->getInstance()Lcom/cmccit/basemodule/bridge/PageIntentBridge;

    move-result-object p2

    iget-object v0, p0, Lcom/cmccit/webview/customview/WebViewEx$WebChromeClientEx$1;->this$1:Lcom/cmccit/webview/customview/WebViewEx$WebChromeClientEx;

    iget-object v0, v0, Lcom/cmccit/webview/customview/WebViewEx$WebChromeClientEx;->this$0:Lcom/cmccit/webview/customview/WebViewEx;

    invoke-static {v0}, Lcom/cmccit/webview/customview/WebViewEx;->access$600(Lcom/cmccit/webview/customview/WebViewEx;)Landroid/app/Activity;

    move-result-object v0

    const-string v1, "50000"

    const/4 v2, 0x0

    invoke-virtual {p2, v0, p1, v1, v2}, Lcom/cmccit/basemodule/bridge/PageIntentBridge;->action(Landroid/content/Context;Landroid/os/Bundle;Ljava/lang/String;Z)V

    const/4 p1, 0x1

    return p1
.end method
