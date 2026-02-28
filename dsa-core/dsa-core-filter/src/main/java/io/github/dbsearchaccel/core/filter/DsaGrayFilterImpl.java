package io.github.dbsearchaccel.core.filter;

import io.github.dbsearchaccel.common.model.config.DsaSceneConfig;
import io.github.dbsearchaccel.common.model.request.DsaRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * 灰度过滤器默认实现.
 * <p>
 * 实现基于白名单/黑名单的灰度规则校验.
 * 校验逻辑：
 * 1. 若grayKey为空，默认允许通过
 * 2. 若grayKey在黑名单中，禁止通过
 * 3. 若白名单为空或grayKey在白名单中，允许通过
 * 4. 其他情况禁止通过
 * </p>
 *
 * @author DBSearchAccel Team
 * @since 1.0.0
 */
public class DsaGrayFilterImpl implements DsaGrayFilter {

    private static final Logger log = LoggerFactory.getLogger(DsaGrayFilterImpl.class);

    @Override
    public boolean allowAccess(DsaRequest request, DsaSceneConfig config) {
        String grayKey = request.getGrayKey();

        if (grayKey == null || grayKey.isEmpty()) {
            log.debug("Gray key is empty, allow access by default");
            return true;
        }

        List<String> blackList = config.getGrayBlackList();
        if (blackList != null && blackList.contains(grayKey)) {
            log.info("Gray key [{}] is in black list, deny access", grayKey);
            return false;
        }

        List<String> whiteList = config.getGrayWhiteList();
        if (whiteList == null || whiteList.isEmpty()) {
            log.debug("White list is empty, allow access for gray key [{}]", grayKey);
            return true;
        }

        boolean allowed = whiteList.contains(grayKey);
        log.debug("Gray key [{}] in white list: {}", grayKey, allowed);
        return allowed;
    }

    @Override
    public String getName() {
        return "defaultGrayFilter";
    }

    @Override
    public int getOrder() {
        return 100;
    }
}
