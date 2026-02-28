package io.github.dbsearchaccel.common.model.response;

import lombok.Data;

import java.io.Serializable;

/**
 * 分页信息实体.
 * <p>
 * 封装分页相关信息，包括总记录数、页码、每页大小、总页数.
 * </p>
 *
 * @author DBSearchAccel Team
 * @since 1.0.0
 */
@Data
public class DsaPageInfo implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 总记录数.
     */
    private long total;

    /**
     * 当前页码.
     */
    private int pageNum;

    /**
     * 每页大小.
     */
    private int pageSize;

    /**
     * 总页数.
     */
    private int pages;

    /**
     * 默认构造方法.
     */
    public DsaPageInfo() {
    }

    /**
     * 构造方法.
     *
     * @param total    总记录数
     * @param pageNum  当前页码
     * @param pageSize 每页大小
     */
    public DsaPageInfo(long total, int pageNum, int pageSize) {
        this.total = total;
        this.pageNum = pageNum;
        this.pageSize = pageSize;
        this.pages = (int) Math.ceil((double) total / pageSize);
    }

    /**
     * 静态工厂方法.
     *
     * @param total    总记录数
     * @param pageNum  当前页码
     * @param pageSize 每页大小
     * @return 分页信息实体
     */
    public static DsaPageInfo of(long total, int pageNum, int pageSize) {
        return new DsaPageInfo(total, pageNum, pageSize);
    }

    /**
     * 判断是否有下一页.
     *
     * @return true表示有下一页
     */
    public boolean hasNextPage() {
        return pageNum < pages;
    }

    /**
     * 判断是否有上一页.
     *
     * @return true表示有上一页
     */
    public boolean hasPreviousPage() {
        return pageNum > 1;
    }
}
