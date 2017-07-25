package elephant.threadpool;

import java.util.concurrent.ThreadFactory;
import java.util.concurrent.atomic.AtomicInteger;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * 
 * @author skydu
 *
 */
public class WxThreadFactory implements ThreadFactory {
	private static Logger logger=LoggerFactory.getLogger(WxThreadFactory.class);
	private AtomicInteger threadCounter=new AtomicInteger();
	private String name;
	//
	public WxThreadFactory(String name) {
		this.name=name;
	}
	@Override
	public Thread newThread(Runnable r) {
		Thread t=new Thread(r);
		t.setName(name+"-"+threadCounter.incrementAndGet());
		Thread.UncaughtExceptionHandler logHander=new Thread.UncaughtExceptionHandler() {
			@Override
			public void uncaughtException(Thread t, Throwable e) {
				logger.error(e.getMessage(),e);
			}
		};
		t.setUncaughtExceptionHandler(logHander);
		return t;
	}
}
