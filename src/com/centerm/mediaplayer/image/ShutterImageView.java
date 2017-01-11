package com.centerm.mediaplayer.image;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.BitmapDrawable;
import android.util.AttributeSet;
import android.view.animation.Animation.AnimationListener;
import android.widget.ImageView;

/**
 * 百叶窗类
 */
public class ShutterImageView extends ImageView
{
	public static final int LEAF_COUNT = 16; // bitmap划分的叶子个数
	public static final int SUB_LEAF_COUNT = 12; // 显示1个叶子的子叶个数
	public static final int INTERVAL_TIME = 800 / SUB_LEAF_COUNT; // 隔多久刷新下一个子页

	public static final int VERTIAL = 0; // 叶子是水平的
	public static final int HORIZENTAL = 1; // 叶子是垂直的

	int orientation = VERTIAL; // 叶子的方向
	int mLeafCount = LEAF_COUNT;// 叶子的个数
	int mSubLeafCount = SUB_LEAF_COUNT;// 子叶的个数
	int step = 0; // 子叶的步骤
	
	private boolean bNoticeEnd = false; // 通知动画结束
	private AnimationListener mListener;
	
	public ShutterImageView(Context context)
	{
		super(context);
	}

	public ShutterImageView(Context context, AttributeSet attrs)
	{
		super(context, attrs);
	}

	public ShutterImageView(Context context, AttributeSet attrs, int defStyle)
	{
		super(context, attrs, defStyle);
	}
	
	/**
	 * 提供接口给container，用于设置动画结束的通知
	 * @param listener 动画结束监听器
	 */
	public void setAnimationListener(AnimationListener listener)
	{
		bNoticeEnd = true;
		mListener = listener;
	}
	
	/**
	 * 设置叶子的方向
	 * 
	 * @param orientation
	 *            方向
	 */
	public void setOrientation(int orientation)
	{
		this.orientation = orientation;
		if (orientation == HORIZENTAL)
		{
			mLeafCount /= 2;
		}
	}

	/**
	 * 更新下一个子页
	 */
	public void showNextLeaf()
	{
		step++;
		postInvalidateDelayed(INTERVAL_TIME);
	}

	@Override
	protected void onDraw(Canvas canvas)
	{
		// 获取绘图对象
		BitmapDrawable drawable = (BitmapDrawable) getDrawable();
		if (drawable == null)
		{
			System.out.println("Shutter Image view onDraw error!");
			return;
		}
		
		// 显示第N个子叶
		Bitmap bitmap = drawable.getBitmap(); // 获取原始的bitmap
		int width = drawable.getIntrinsicWidth(); // 图像宽度
		int height = drawable.getIntrinsicHeight(); // 图像高度
		int leafWidth = width / mLeafCount; // 叶子的宽度
		int leafHeight = height / mLeafCount; // 叶子的宽度
		int xOffset = (getWidth() - width) / 2; // 水平偏移大小
		int yOffset = (getHeight() - height) / 2; // y轴偏移大小

		// 定义绘制的矩形区域
		Rect srcRect = new Rect(0, 0, width, height);
		Rect desRect = new Rect(xOffset, yOffset, xOffset + width, yOffset
				+ height);

		// 图像太小了，就直接显示
		if( (width < mLeafCount && orientation == VERTIAL) || (height < mLeafCount
				&& orientation == HORIZENTAL))
		{
			canvas.drawBitmap(bitmap, srcRect, desRect, null);
			return;
		}

		// 宽度或高度太小，就不需要那么多步骤来显示了
		if (width < mLeafCount * mSubLeafCount && orientation == VERTIAL)
		{
			mSubLeafCount = leafWidth;
		}
		if (height < mLeafCount * mSubLeafCount && orientation == HORIZENTAL)
		{
			mSubLeafCount = leafHeight;
		}
		if (mSubLeafCount == 0)
		{
			mSubLeafCount = 1;
		}

		// 绘制各个叶
		for (int i = 0; i < mLeafCount; i++)
		{
			// 计算需要刷新的子叶的矩形
			if (orientation == VERTIAL)// 垂直
			{
				srcRect.left = i * leafWidth;
				srcRect.right = srcRect.left + step * leafWidth / mSubLeafCount;

				desRect.left = xOffset + srcRect.left;
				desRect.right = xOffset + srcRect.right;
			} else
			// 水平
			{
				srcRect.top = i * leafHeight;
				srcRect.bottom = srcRect.top + step * leafHeight
						/ mSubLeafCount;

				desRect.top = yOffset + srcRect.top;
				desRect.bottom = yOffset + srcRect.bottom;
			}
			canvas.drawBitmap(bitmap, srcRect, desRect, null);
		}// end of for
	
		// 未绘制完成前，就准备下一次绘制
		if (step < mSubLeafCount)
		{
			showNextLeaf();
		}
		
		if(step >= mSubLeafCount && bNoticeEnd)
		{
			mListener.onAnimationEnd(null);
			bNoticeEnd = false;
		}
	}// end of onDraw
}
