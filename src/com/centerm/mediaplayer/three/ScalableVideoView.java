package com.centerm.mediaplayer.three;

import android.content.Context;
import android.content.res.AssetFileDescriptor;
import android.content.res.AssetManager;
import android.graphics.SurfaceTexture;
import android.media.MediaPlayer;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.RawRes;
import android.util.AttributeSet;
import android.view.Surface;
import android.view.TextureView;

import java.io.IOException;

/**
 * Created by yqritc on 2015/06/11.
 */
public class ScalableVideoView extends TextureView implements TextureView.SurfaceTextureListener,
MediaPlayer.OnVideoSizeChangedListener {

	protected MediaPlayer mMediaPlayer;

	public ScalableVideoView(Context context) {
		this(context, null);
	}

	public ScalableVideoView(Context context, AttributeSet attrs) {
		this(context, attrs, 0);
	}

	public ScalableVideoView(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	@Override
	public void onSurfaceTextureAvailable(SurfaceTexture surfaceTexture, int width, int height) {
		Surface surface = new Surface(surfaceTexture);
		if (mMediaPlayer != null) {
			mMediaPlayer.setSurface(surface);
		}
	}

	@Override
	public void onSurfaceTextureSizeChanged(SurfaceTexture surface, int width, int height) {
	}

	@Override
	public boolean onSurfaceTextureDestroyed(SurfaceTexture surface) {
		return false;
	}

	@Override
	public void onSurfaceTextureUpdated(SurfaceTexture surface) {
	}

	@Override
	protected void onDetachedFromWindow() {
		super.onDetachedFromWindow();
		if (mMediaPlayer == null) {
			return;
		}

		if (isPlaying()) {
			stop();
		}
		release();
		mMediaPlayer = null;
	}

	@Override
	public void onVideoSizeChanged(MediaPlayer mp, int width, int height) {
		scaleVideoSize(width, height);
	}

	private void scaleVideoSize(int videoWidth, int videoHeight) {
		if (videoWidth == 0 || videoHeight == 0) {
			return;
		}
	}

	public void setRawData(@RawRes int id) throws IOException {
		AssetFileDescriptor afd = getResources().openRawResourceFd(id);
		setDataSource(afd);
	}

	public void setAssetData(@NonNull String assetName) throws IOException {
		AssetManager manager = getContext().getAssets();
		AssetFileDescriptor afd = manager.openFd(assetName);
		setDataSource(afd);
	}

	public void setPathData(@NonNull String path) throws IOException {
		if(path == null){
			return;
		}
		setDataSource(path);
	}

	private void setDataSource(@NonNull String path) throws IOException {
		if (mMediaPlayer == null) {
			mMediaPlayer = new MediaPlayer();
			mMediaPlayer.setOnVideoSizeChangedListener(this);
			setSurfaceTextureListener(this);
		} else {
			mMediaPlayer.reset();
		}

		mMediaPlayer.setDataSource(path);
	}

	private void setDataSource(@NonNull AssetFileDescriptor afd) throws IOException {
		if (mMediaPlayer == null) {
			mMediaPlayer = new MediaPlayer();
			mMediaPlayer.setOnVideoSizeChangedListener(this);
			setSurfaceTextureListener(this);
		} else {
			mMediaPlayer.reset();
		}

		mMediaPlayer.setDataSource(afd.getFileDescriptor(), afd.getStartOffset(), afd.getLength());
		afd.close();
	}

	public void setOnPrepare(@Nullable MediaPlayer.OnPreparedListener listener)
			throws IOException, IllegalStateException {
		mMediaPlayer.setOnPreparedListener(listener);
		mMediaPlayer.prepare();
	}
	
	public void setOnComplete(@Nullable MediaPlayer.OnCompletionListener listener)
			throws IOException, IllegalStateException {
		mMediaPlayer.setOnCompletionListener(listener);
	}

	public void prepareAsync(@Nullable MediaPlayer.OnPreparedListener listener)
			throws IllegalStateException {
		mMediaPlayer.setOnPreparedListener(listener);
		mMediaPlayer.prepareAsync();
	}

	public void prepare() throws IOException, IllegalStateException {
		setOnPrepare(null);
	}

	public void prepareAsync() throws IllegalStateException {
		prepareAsync(null);
	}

	public int getCurrentPosition() {
		return mMediaPlayer.getCurrentPosition();
	}

	public int getDuration() {
		return mMediaPlayer.getDuration();
	}

	public int getVideoHeight() {
		return mMediaPlayer.getVideoHeight();
	}

	public int getVideoWidth() {
		return mMediaPlayer.getVideoWidth();
	}

	public boolean isLooping() {
		return mMediaPlayer.isLooping();
	}

	public boolean isPlaying() {
		return mMediaPlayer.isPlaying();
	}

	public void pause() {
		mMediaPlayer.pause();
	}

	public void seekTo(int msec) {
		mMediaPlayer.seekTo(msec);
	}

	public void setLooping(boolean looping) {
		mMediaPlayer.setLooping(looping);
	}

	public void setVolume(float leftVolume, float rightVolume) {
		mMediaPlayer.setVolume(leftVolume, rightVolume);
	}

	public void start() {
		mMediaPlayer.start();
	}

	public void stop() {
		mMediaPlayer.stop();
	}

	public void release() {
		mMediaPlayer.reset();
		mMediaPlayer.release();
	}
}