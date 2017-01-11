package com.centerm.mediaplayer.three;

public class VideoInfo {
	
	private String url;

	private int videoImage;

	public VideoInfo(String url, int videoImage) {
		super();
		this.url = url;
		this.videoImage = videoImage;
	}

	public String getUrl() {
		return url;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public int getVideoImage() {
		return videoImage;
	}

	public void setVideoImage(int videoImage) {
		this.videoImage = videoImage;
	}
}
