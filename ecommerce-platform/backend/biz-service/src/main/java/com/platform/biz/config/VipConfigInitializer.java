package com.platform.biz.config;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.platform.biz.entity.VipConfig;
import com.platform.biz.mapper.VipConfigMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/**
 * VIP配置初始化器
 * 应用启动时检查vip_config表，确保5个等级的默认配置存在
 * 规范：每满50元减2/4/6/8/10元
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class VipConfigInitializer implements ApplicationRunner {

    private final VipConfigMapper vipConfigMapper;

    private static final int[] LEVELS = {1, 2, 3, 4, 5};
    private static final String[] NAMES = {"VIP I", "VIP II", "VIP III", "VIP IV", "VIP V"};
    private static final int[] DISCOUNT_AMOUNTS = {2, 4, 6, 8, 10};
    private static final String[] DESCRIPTIONS = {
            "每满50元减2元",
            "每满50元减4元",
            "每满50元减6元",
            "每满50元减8元",
            "每满50元减10元"
    };

    @Override
    public void run(ApplicationArguments args) {
        log.info("开始初始化VIP配置...");
        for (int i = 0; i < LEVELS.length; i++) {
            int level = LEVELS[i];
            LambdaQueryWrapper<VipConfig> wrapper = new LambdaQueryWrapper<>();
            wrapper.eq(VipConfig::getLevel, level);
            VipConfig exist = vipConfigMapper.selectOne(wrapper);
            if (exist == null) {
                // 不存在则插入
                VipConfig config = new VipConfig();
                config.setLevel(level);
                config.setName(NAMES[i]);
                config.setThreshold(50);
                config.setDiscountAmount(new BigDecimal(DISCOUNT_AMOUNTS[i]));
                config.setDescription(DESCRIPTIONS[i]);
                vipConfigMapper.insert(config);
                log.info("插入VIP等级配置: level={}, name={}", level, NAMES[i]);
            } else {
                // 存在则检查并更新为规范值
                boolean needUpdate = false;
                if (exist.getThreshold() == null || exist.getThreshold() != 50) {
                    exist.setThreshold(50);
                    needUpdate = true;
                }
                if (exist.getDiscountAmount() == null
                        || exist.getDiscountAmount().compareTo(new BigDecimal(DISCOUNT_AMOUNTS[i])) != 0) {
                    exist.setDiscountAmount(new BigDecimal(DISCOUNT_AMOUNTS[i]));
                    needUpdate = true;
                }
                if (exist.getName() == null || !exist.getName().equals(NAMES[i])) {
                    exist.setName(NAMES[i]);
                    needUpdate = true;
                }
                if (exist.getDescription() == null || !exist.getDescription().equals(DESCRIPTIONS[i])) {
                    exist.setDescription(DESCRIPTIONS[i]);
                    needUpdate = true;
                }
                if (needUpdate) {
                    vipConfigMapper.updateById(exist);
                    log.info("更新VIP等级配置: level={}, name={}", level, NAMES[i]);
                }
            }
        }
        log.info("VIP配置初始化完成");
    }
}
