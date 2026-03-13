package framework.domain.constants;

/**
 * 缓存常量
 */
public class CacheConstants {
    /**
     * 缓存分割符
     */
    public final static String CACHE_SPLIT_COLON = ":";


    /**
     * 缓存有效期，默认720（分钟）
     * (临时改成两天)
     */
    public final static long EXPIRATION = 720 * 4;

    /**
     * 缓存刷新时间，默认120（分钟）
     */
    public final static long REFRESH_TIME = 120;
}
