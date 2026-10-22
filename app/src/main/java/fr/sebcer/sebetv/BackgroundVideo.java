package fr.sebcer.sebetv;

import android.content.Context;
import android.graphics.Matrix;
import android.graphics.SurfaceTexture;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.view.Surface;
import android.view.TextureView;

/** Silent, local, looping background. Owns and releases its decoder and surface. */
final class BackgroundVideo extends TextureView implements TextureView.SurfaceTextureListener {
    private final Handler handler=new Handler(Looper.getMainLooper());
    private MediaPlayer player;
    private Surface surface;
    private String source="";
    private int videoWidth,videoHeight;
    private final Runnable start=this::open;
    private final Runnable fallback, visible;
    BackgroundVideo(Context context,Runnable fallback,Runnable visible) {
        super(context); this.fallback=fallback; this.visible=visible;
        setOpaque(false); setAlpha(0f); setFocusable(false); setClickable(false);
        setImportantForAccessibility(IMPORTANT_FOR_ACCESSIBILITY_NO);
        setSurfaceTextureListener(this);
    }
    void select(String uri) {
        handler.removeCallbacks(start); source=uri==null?"":uri; release();
        if(!source.isEmpty() && isAvailable())handler.postDelayed(start,450);
    }
    void stop() { handler.removeCallbacks(start); source="";release(); }
    private void release() {
        setAlpha(0f);
        if(player!=null) { player.setOnPreparedListener(null);player.setOnInfoListener(null);player.setOnErrorListener(null);player.release();player=null; }
        if(surface!=null) {surface.release();surface=null;}
        videoWidth=videoHeight=0;
    }
    private void open() {
        if(source.isEmpty() || !isAvailable())return;
        release();
        try {
            final MediaPlayer current=new MediaPlayer();player=current;
            surface=new Surface(getSurfaceTexture());current.setSurface(surface);
            current.setVolume(0f,0f);current.setLooping(true);
            current.setOnPreparedListener(mp->{if(player==mp){videoWidth=mp.getVideoWidth();videoHeight=mp.getVideoHeight();crop();mp.start();}});
            current.setOnInfoListener((mp,what,extra)->{
                if(player==mp && what==MediaPlayer.MEDIA_INFO_VIDEO_RENDERING_START){setAlpha(1f);visible.run();}
                return false;
            });
            current.setOnErrorListener((mp,what,extra)->{if(player==mp){release();fallback.run();}return true;});
            current.setDataSource(getContext(),Uri.parse(source));current.prepareAsync();
        } catch(Exception e) {release();fallback.run();}
    }
    private void crop() {
        if(videoWidth<=0 || videoHeight<=0 || getWidth()==0 || getHeight()==0)return;
        float scale=Math.max((float)getWidth()/videoWidth,(float)getHeight()/videoHeight);
        Matrix matrix=new Matrix();matrix.setScale(videoWidth*scale/getWidth(),videoHeight*scale/getHeight(),getWidth()/2f,getHeight()/2f);setTransform(matrix);
    }
    @Override public void onSurfaceTextureAvailable(SurfaceTexture t,int w,int h) {if(!source.isEmpty())handler.postDelayed(start,450);}
    @Override public void onSurfaceTextureSizeChanged(SurfaceTexture t,int w,int h) {crop();}
    @Override public boolean onSurfaceTextureDestroyed(SurfaceTexture t) {stop();return true;}
    @Override public void onSurfaceTextureUpdated(SurfaceTexture t) { }
    @Override protected void onDetachedFromWindow() {stop();super.onDetachedFromWindow();}
}
