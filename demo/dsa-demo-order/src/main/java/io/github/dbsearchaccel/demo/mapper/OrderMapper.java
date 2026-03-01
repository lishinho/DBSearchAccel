package io.github.dbsearchaccel.demo.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import io.github.dbsearchaccel.demo.entity.Order;
import org.apache.ibatis.annotations.Mapper;

/**
 * 订单Mapper接口
 *
 * @author DBSearchAccel Team
 */
@Mapper
public interface OrderMapper extends BaseMapper<Order> {

}
