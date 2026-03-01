package io.github.dbsearchaccel.consistency;

/**
 * 校验报告生成器接口.
 * <p>
 * 负责生成可视化的校验报告.
 * </p>
 *
 * @author DBSearchAccel Team
 * @since 1.0.0
 */
public interface DsaCheckReportGenerator {

    /**
     * 生成校验报告.
     *
     * @param report 校验报告数据
     * @param format 格式（json/csv/html）
     * @return 报告内容
     */
    String generate(DsaCheckReport report, String format);

    /**
     * 生成HTML格式报告.
     *
     * @param report 校验报告数据
     * @return HTML内容
     */
    String generateHtml(DsaCheckReport report);

    /**
     * 生成JSON格式报告.
     *
     * @param report 校验报告数据
     * @return JSON内容
     */
    String generateJson(DsaCheckReport report);

    /**
     * 生成CSV格式报告.
     *
     * @param report 校验报告数据
     * @return CSV内容
     */
    String generateCsv(DsaCheckReport report);
}
