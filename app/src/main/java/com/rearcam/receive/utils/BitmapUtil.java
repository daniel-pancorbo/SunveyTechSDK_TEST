package com.rearcam.receive.utils;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;

public class BitmapUtil {

	/**
	 * 图片压缩
	 * @param path 文件路径
	 * @param rqsW 宽
	 * @param rqsH 高
	 * @return
	 */
	public static Bitmap compressBitmap(String path, int rqsW, int rqsH) {
		final BitmapFactory.Options options = new BitmapFactory.Options();
		options.inJustDecodeBounds = true;//禁止为bitmap分配内存
		BitmapFactory.decodeFile(path, options);
		options.inSampleSize = caculateInSampleSize(options, rqsW, rqsH);
		options.inJustDecodeBounds = false;
		return BitmapFactory.decodeFile(path, options);
	}

	private static int caculateInSampleSize(BitmapFactory.Options options, int rqsW, int rqsH) {
		final int height = options.outHeight;
		final int width = options.outWidth;
//		Log.i("LocalVideoListAdapter", "height="+height+",width="+width);
		int inSampleSize = 4;
		if (rqsW == 0 || rqsH == 0) return 1;
		if (height > rqsH || width > rqsW) {
			final int heightRatio = Math.round((float) height/ (float) rqsH);
			final int widthRatio = Math.round((float) width / (float) rqsW);
			inSampleSize = heightRatio < widthRatio ? heightRatio : widthRatio;
		}
		return inSampleSize;
	}
	/**
	 * 图片反转
	 *
	 * @param bmp
	 * @param flag
	 *            0为水平反转，1为垂直反转
	 * @return
	 */
	public static Bitmap reverseBitmap(Bitmap bmp, int flag) {
		float[] floats = null;
		switch (flag) {
			case 0: // 水平反转
				floats = new float[] { -1f, 0f, 0f, 0f, 1f, 0f, 0f, 0f, 1f };
				break;
			case 1: // 垂直反转
				floats = new float[] { 1f, 0f, 0f, 0f, -1f, 0f, 0f, 0f, 1f };
				break;
		}

		if (floats != null) {
			Matrix matrix = new Matrix();
			matrix.setValues(floats);
			return Bitmap.createBitmap(bmp, 0, 0, bmp.getWidth(), bmp.getHeight(), matrix, true);
		}

		return null;
	}
}
