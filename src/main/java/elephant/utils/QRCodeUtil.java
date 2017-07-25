package elephant.utils;

import java.awt.image.BufferedImage;
import java.io.File;
import java.util.Hashtable;

import javax.imageio.ImageIO;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.zxing.BarcodeFormat;
import com.google.zxing.BinaryBitmap;
import com.google.zxing.DecodeHintType;
import com.google.zxing.EncodeHintType;
import com.google.zxing.LuminanceSource;
import com.google.zxing.MultiFormatReader;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.Result;
import com.google.zxing.WriterException;
import com.google.zxing.client.j2se.BufferedImageLuminanceSource;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.common.HybridBinarizer;
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel;

/**
 * 
 * @author skydu
 *
 */
public class QRCodeUtil {
	//
	private static Logger logger=LoggerFactory.getLogger(QRCodeUtil.class);
	//
	public static String readQr(String qrPath) throws Exception{
    	BufferedImage image = ImageIO.read(new File(qrPath));
    	LuminanceSource source = new BufferedImageLuminanceSource(image);
		BinaryBitmap bitmap = new BinaryBitmap(new HybridBinarizer(source));
		Result result;
		Hashtable<DecodeHintType,String> hints=new Hashtable<DecodeHintType,String>();
		hints.put(DecodeHintType.CHARACTER_SET, "utf-8");
		result = new MultiFormatReader().decode(bitmap, hints);
		return result.getText();
	}
	
	public static void showQrcode(String filePath) throws Exception{
		if(OSUtil.isMacOS()){
			Runtime runtime = Runtime.getRuntime();
			runtime.exec("open " + filePath);
			logger.info(QRCodeUtil.getQr(QRCodeUtil.readQr(filePath)));
		}
		if(OSUtil.isLinux()){
			logger.info(QRCodeUtil.getQr(QRCodeUtil.readQr(filePath)));
		}
		if(OSUtil.isWindows()){
			Runtime runtime = Runtime.getRuntime();
			runtime.exec("cmd /c start " + filePath);
		}
	}
	
	public static String getQr(String text) throws WriterException {
		String s = "生成二维码失败";
		int width = 40;
		int height = 40;
		Hashtable<EncodeHintType, Object> qrParam = new Hashtable<EncodeHintType, Object>();
		qrParam.put(EncodeHintType.ERROR_CORRECTION, ErrorCorrectionLevel.L);
		qrParam.put(EncodeHintType.CHARACTER_SET, "utf-8");
		BitMatrix bitMatrix = new MultiFormatWriter().encode(text, BarcodeFormat.QR_CODE, width, height, qrParam);
		s = toAscii(bitMatrix);
		return s;
	}

	public static String toAscii(BitMatrix bitMatrix) {
		StringBuilder sb = new StringBuilder();
		for (int rows = 0; rows < bitMatrix.getHeight(); rows++) {
			for (int cols = 0; cols < bitMatrix.getWidth(); cols++) {
				boolean x = bitMatrix.get(rows, cols);
				if (!x) {
					sb.append("\033[47m  \033[0m");
				} else {
					sb.append("\033[40m  \033[0m");
				}
			}
			sb.append("\n");
		}
		return sb.toString();
	}
}