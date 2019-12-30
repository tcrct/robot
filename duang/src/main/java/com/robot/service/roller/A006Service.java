package com.robot.service.roller;

import com.robot.agv.common.telegrams.Request;
import com.robot.agv.common.telegrams.Response;
import com.robot.mvc.annotations.Service;
import com.robot.service.common.BaseService;
import com.robot.utils.RobotUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class A006Service extends BaseService {

    private static final Logger LOG =  LoggerFactory.getLogger(A006Service.class);

    /**
     * 下发车辆移动指令
     * @param request
     * @param response
     * @return
     */
    public String setRout(Request request, Response response) {
        return super.setRout(request, response);
    }

    /**
     * 上报卡号
     * @param request
     * @param response
     * @return
     */
    public String rptAc(Request request, Response response) {
        LOG.info("车辆[{}]行驶到达[{}]卡号", request.getProtocol().getDeviceId(), RobotUtil.getPoint(request.getProtocol()));
        return request.getRawContent();
    }

}
