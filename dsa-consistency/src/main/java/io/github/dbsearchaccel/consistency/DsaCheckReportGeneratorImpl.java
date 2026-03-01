package io.github.dbsearchaccel.consistency;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * 校验报告生成器实现.
 * <p>
 * 负责生成可视化的校验报告.
 * </p>
 *
 * @author DBSearchAccel Team
 * @since 1.0.0
 */
@Component
public class DsaCheckReportGeneratorImpl implements DsaCheckReportGenerator {

    private static final Logger LOGGER = LoggerFactory.getLogger(DsaCheckReportGeneratorImpl.class);

    private static final String DATE_FORMAT = "yyyy-MM-dd HH:mm:ss";

    @Override
    public String generate(DsaCheckReport report, String format) {
        LOGGER.info("Generating report: taskId={}, format={}", report.getTaskId(), format);

        switch (format.toLowerCase()) {
            case "html":
                return generateHtml(report);
            case "json":
                return generateJson(report);
            case "csv":
                return generateCsv(report);
            default:
                return generateJson(report);
        }
    }

    @Override
    public String generateHtml(DsaCheckReport report) {
        StringBuilder sb = new StringBuilder();
        sb.append("<!DOCTYPE html>\n");
        sb.append("<html lang=\"zh-CN\">\n");
        sb.append("<head>\n");
        sb.append("    <meta charset=\"UTF-8\">\n");
        sb.append("    <title>数据一致性校验报告</title>\n");
        sb.append("    <style>\n");
        sb.append("        body { font-family: Arial, sans-serif; margin: 20px; }\n");
        sb.append("        .header { background-color: #f5f5f5; padding: 15px; border-radius: 5px; }\n");
        sb.append("        .summary { margin-top: 20px; padding: 15px; border: 1px solid #ddd; border-radius: 5px; }\n");
        sb.append("        .consistent { color: green; }\n");
        sb.append("        .inconsistent { color: red; }\n");
        sb.append("        table { width: 100%; border-collapse: collapse; margin-top: 20px; }\n");
        sb.append("        th, td { border: 1px solid #ddd; padding: 8px; text-align: left; }\n");
        sb.append("        th { background-color: #f5f5f5; }\n");
        sb.append("    </style>\n");
        sb.append("</head>\n");
        sb.append("<body>\n");

        sb.append("    <div class=\"header\">\n");
        sb.append("        <h1>数据一致性校验报告</h1>\n");
        sb.append("        <p>报告ID: ").append(report.getReportId()).append("</p>\n");
        sb.append("        <p>任务ID: ").append(report.getTaskId()).append("</p>\n");
        sb.append("        <p>场景编码: ").append(report.getSceneCode()).append("</p>\n");
        sb.append("        <p>生成时间: ").append(formatDate(report.getGenerateTime())).append("</p>\n");
        sb.append("    </div>\n");

        sb.append("    <div class=\"summary\">\n");
        sb.append("        <h2>校验摘要</h2>\n");
        String consistentClass = report.isOverallConsistent() ? "consistent" : "inconsistent";
        sb.append("        <p>总体结果: <span class=\"").append(consistentClass).append("\">")
                .append(report.isOverallConsistent() ? "一致" : "不一致").append("</span></p>\n");
        sb.append("        <p>总校验项: ").append(report.getTotalCheckCount()).append("</p>\n");
        sb.append("        <p>通过项: ").append(report.getPassedCheckCount()).append("</p>\n");
        sb.append("        <p>失败项: ").append(report.getFailedCheckCount()).append("</p>\n");
        sb.append("        <p>总耗时: ").append(report.getTotalElapsedMs()).append("ms</p>\n");
        sb.append("    </div>\n");

        if (report.getResults() != null && !report.getResults().isEmpty()) {
            sb.append("    <div class=\"details\">\n");
            sb.append("        <h2>校验详情</h2>\n");
            sb.append("        <table>\n");
            sb.append("            <tr>\n");
            sb.append("                <th>校验类型</th>\n");
            sb.append("                <th>ES数据量</th>\n");
            sb.append("                <th>DB数据量</th>\n");
            sb.append("                <th>差异数量</th>\n");
            sb.append("                <th>结果</th>\n");
            sb.append("                <th>耗时(ms)</th>\n");
            sb.append("            </tr>\n");

            for (DsaCheckResult result : report.getResults()) {
                sb.append("            <tr>\n");
                sb.append("                <td>").append(result.getCheckType().getName()).append("</td>\n");
                sb.append("                <td>").append(result.getEsCount()).append("</td>\n");
                sb.append("                <td>").append(result.getDbCount()).append("</td>\n");
                sb.append("                <td>").append(result.getDiffCount()).append("</td>\n");
                String resultClass = result.isConsistent() ? "consistent" : "inconsistent";
                sb.append("                <td class=\"").append(resultClass).append("\">")
                        .append(result.isConsistent() ? "一致" : "不一致").append("</td>\n");
                sb.append("                <td>").append(result.getElapsedMs()).append("</td>\n");
                sb.append("            </tr>\n");
            }

            sb.append("        </table>\n");
            sb.append("    </div>\n");
        }

        if (report.getRepairSuggestions() != null && !report.getRepairSuggestions().isEmpty()) {
            sb.append("    <div class=\"suggestions\">\n");
            sb.append("        <h2>修复建议</h2>\n");
            sb.append("        <ul>\n");
            for (String suggestion : report.getRepairSuggestions()) {
                sb.append("            <li>").append(suggestion).append("</li>\n");
            }
            sb.append("        </ul>\n");
            sb.append("    </div>\n");
        }

        sb.append("</body>\n");
        sb.append("</html>");

        return sb.toString();
    }

    @Override
    public String generateJson(DsaCheckReport report) {
        StringBuilder sb = new StringBuilder();
        sb.append("{\n");
        sb.append("  \"reportId\": \"").append(report.getReportId()).append("\",\n");
        sb.append("  \"taskId\": \"").append(report.getTaskId()).append("\",\n");
        sb.append("  \"sceneCode\": \"").append(report.getSceneCode()).append("\",\n");
        sb.append("  \"status\": \"").append(report.getStatus()).append("\",\n");
        sb.append("  \"overallConsistent\": ").append(report.isOverallConsistent()).append(",\n");
        sb.append("  \"totalCheckCount\": ").append(report.getTotalCheckCount()).append(",\n");
        sb.append("  \"passedCheckCount\": ").append(report.getPassedCheckCount()).append(",\n");
        sb.append("  \"failedCheckCount\": ").append(report.getFailedCheckCount()).append(",\n");
        sb.append("  \"totalElapsedMs\": ").append(report.getTotalElapsedMs()).append(",\n");
        sb.append("  \"generateTime\": \"").append(formatDate(report.getGenerateTime())).append("\",\n");
        sb.append("  \"summary\": \"").append(escapeJson(report.getSummary())).append("\",\n");
        sb.append("  \"results\": [\n");

        if (report.getResults() != null) {
            for (int i = 0; i < report.getResults().size(); i++) {
                DsaCheckResult result = report.getResults().get(i);
                sb.append("    {\n");
                sb.append("      \"checkType\": \"").append(result.getCheckType()).append("\",\n");
                sb.append("      \"consistent\": ").append(result.isConsistent()).append(",\n");
                sb.append("      \"esCount\": ").append(result.getEsCount()).append(",\n");
                sb.append("      \"dbCount\": ").append(result.getDbCount()).append(",\n");
                sb.append("      \"diffCount\": ").append(result.getDiffCount()).append(",\n");
                sb.append("      \"elapsedMs\": ").append(result.getElapsedMs()).append("\n");
                sb.append("    }").append(i < report.getResults().size() - 1 ? "," : "").append("\n");
            }
        }

        sb.append("  ]\n");
        sb.append("}");

        return sb.toString();
    }

    @Override
    public String generateCsv(DsaCheckReport report) {
        StringBuilder sb = new StringBuilder();
        sb.append("ReportId,TaskId,SceneCode,Status,OverallConsistent,TotalCheckCount,PassedCheckCount,FailedCheckCount,TotalElapsedMs,GenerateTime\n");
        sb.append(report.getReportId()).append(",");
        sb.append(report.getTaskId()).append(",");
        sb.append(report.getSceneCode()).append(",");
        sb.append(report.getStatus()).append(",");
        sb.append(report.isOverallConsistent()).append(",");
        sb.append(report.getTotalCheckCount()).append(",");
        sb.append(report.getPassedCheckCount()).append(",");
        sb.append(report.getFailedCheckCount()).append(",");
        sb.append(report.getTotalElapsedMs()).append(",");
        sb.append(formatDate(report.getGenerateTime())).append("\n");

        sb.append("\n");
        sb.append("CheckType,Consistent,EsCount,DbCount,DiffCount,ElapsedMs\n");

        if (report.getResults() != null) {
            for (DsaCheckResult result : report.getResults()) {
                sb.append(result.getCheckType()).append(",");
                sb.append(result.isConsistent()).append(",");
                sb.append(result.getEsCount()).append(",");
                sb.append(result.getDbCount()).append(",");
                sb.append(result.getDiffCount()).append(",");
                sb.append(result.getElapsedMs()).append("\n");
            }
        }

        return sb.toString();
    }

    private String formatDate(Date date) {
        if (date == null) {
            return "";
        }
        SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT);
        return sdf.format(date);
    }

    private String escapeJson(String str) {
        if (str == null) {
            return "";
        }
        return str.replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }
}
